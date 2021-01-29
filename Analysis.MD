
ThreadDelegatedScope
====

* implements Spring' s Scope
```
	 * Return the object with the given name from the underlying scope,
	 * ObjectFactory#getObject() creating it}
	 * if not found in the underlying storage mechanism.
	Object get(String name, ObjectFactory<?> objectFactory);
     * Remove the object with the given {@code name} from the underlying scope.
	 * <p>Returns null} if no object was found; otherwise
	 * returns the removed  Object}.
	Object remove(String name);
	String getConversationId();
```

* getConversationId()
  * returns the current thread name

* getObject
  * creates a NEW ThreadDelegatedProvider each time
  * calls Get on it.
  * This calls getContext()
        * Check if exists, if not Returns new ThreadDelegatedContext().
        * Also stores this context in a threadLocal
        * returns from thread local if already exists
    * If  context contains the object, returns it, otherwise call objectFactory to create, put in context, and return.
    *  remove()
        * calls getContext() and then the remove method on the context (spoiler, this removes from a map). See below for getContext
    
/**
* A thread enters the scope. Clear the current context. If a new context
* was given, assign it to the scope, otherwise leave it empty.
*/
* changeScope(@Nullable final ThreadDelegatedContext context)
    * Check threadlocal is nonnull (eg getContext previously called). Set == oldContext
    	* if threadLocal currently IS set (oldContext)
    		* if its the same context as parameter, exit method NOW. Do nothing
    		* Otherwise, Send a ScopeEvent.Leave to oldContext
    	* Now regardless of the above, is the context parameter null?
    		* If context is null remove the threadlocal set parameter (oldContext)
    		* If context is non null
    			* Set threadlocal to it
    			* call scopeEvent.enter on it
  
Truth table
```    
    oldContext	newContext				
    NULL		NULL					oldContext is null, so skip the whole first bit. No-op remove on threadlocal. Upshot is no events are sent, and nothing really happens.
    NULL		non null				oldContext is null, so skip the whole first bit. Set threadLocal to newContext, and send an ENTER to it.
    non null	null					First bit: send a LEAVE event to old context. Remove old context from ThreadLocal
    non null 	non null				First bit: send a LEAVE event to old context. Replace oldContext with new context in ThreadLocal and call ENTER on it.
    Side case: nonnull/nonnull and equal. First bit exits: no op
```    
     
* A singleton instance of the ThreadDelegatedScope called SCOPE is created
* As noted above, a ThreadLocal stores the ThreadDelegatedContext. Hence for each thread, we'll have different stuff
* ThreadDelegatedScopeConfiguration registers this singleton instance SCOPE, as well as the Filter discussed below

ThreadDelegatedContext
====

* Primarily is a delegate to a Map<String, Object>.
    * containsKey, get, put, remove, clear
* All methods synchronized
* Also contains `Set<ScopeListener>`
* All methods are simply delegates except as noted
* Put also checks if the object being placed in the Scope is a ScopeListener instance.
    * If so, it's added to the set of listeners and receives a ScopeEvent.ENTER
* Clear sets a ScopeEvent.Leave to all listeners and then clears them.
* event, which is called by clear() and also by the changeContext method in ThreadDelegatedScope sends a supplied ScopeEvent to all listeners

ThreadDelegatingScopeFilter
====
* Servlet Filter
* As noted in various comment must run EARLY in the filter chain
* calls scope.changeScope(null) which clears the context
* sets a Request Attribute to indicate the scope is active
* chains execution to other filters
* calls scope.changeScope(null) which clears the context
* calls scope.changeScope(null) which clears the context

Really this simply clears the scope before and after the chain, and sets a sentinel.


Tests
====
* Have been annotated with comments

Thoughts
====
* This is a fairly effective methodology to add items marked with @Scope of this type to be instantiated per thread, and
  receive events when they enter the context and leave.
* I'm guessing and will confirm with diving into conservedHeaders
    * ConservedHeaders is marked as a ScopeListener, and injected with @Scope into Filter
    * A Filter copies the request values into conserved headers
    * Everytime a Scope.Enter fires we copy from the current conservedHeaders to MDC
    * Everytime a Scope.Leave fires we remove from MDC
    * Implies that we have a redundant fire I think? Because at first injection we'll get an ENTER but don't have anything to put
    * MDC is write only, values are never read from it back into the ThreadContext
    * MDC is really used for logging and for HTTPClients. Theoretically the clients could be injected with the same @Scope
    ConservedHeaders, but maybe a good reason not to?
    * Reactive won't work this way, which I knew already.
    * What I don't follow is who triggers once we switch threads the actual provided object bit. I can see how the request cycle
    "servlet thread" works wonderfully. I can see IF someone we injects the scoped object that all will work. Don't see what
      will trigger that though.
      

Post reading ConservedHeader code:
* ConservedHeaders is marked as a ScopeListener, and injected with @Scope into Filter
    Yes
* A Filter copies the request values into conserved headers
    Yes
* Everytime a Scope.Enter fires we copy from the current conservedHeaders to MDC
    Yes, also when the setConservedHeqders is called
* Everytime a Scope.Leave fires we remove from MDC
    Yup
* Implies that we have a redundant fire I think? Because at first injection we'll get an ENTER but don't have anything to put
    Correct, compensated for with the set
* MDC is write only, values are never read from it back into the ThreadContext
    Yes
* MDC is really used for logging and for HTTPClients. Theoretically the clients could be injected with the same @Scope
ConservedHeaders, but maybe a good reason not to?
    Yes and in fact JAXRS client uses Provider<ConservedHeaders> and the others use MDC
* Reactive won't work this way, which I knew already.
    Yes, I need to finish reading this
* What I don't follow is who triggers once we switch threads the actual provided object bit. I can see how the request cycle
"servlet thread" works wonderuflly. I can see IF someone we injects the scoped object that all will work. Don't see what
will trigger that though.
      Correct, only Something like https://github.com/opentable/otj-executors/blob/master/src/main/java/com/opentable/concurrent/ThreadDelegatingDecorator.java
      can do that.
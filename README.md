OpenTable Scopes Component
==========================

Component Charter
-----------------

* Provides various special guice scopes.

Component Level
---------------

*Foundation component*

* Allowed dependencies: logging component.

----

ThreadDelegate scope
--------------------

Similar to the 'request' scope, except that it can be passed onto
another thread and is not tied to the (non-threadsafe) HttpRequest
object.

Activated by installing the ThreadDelegatedScopeModule. 

----
Copyright (C) 2014 OpenTable, Inc.

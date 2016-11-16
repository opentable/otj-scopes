OpenTable Scopes Component
==========================

[![Build Status](https://travis-ci.org/opentable/otj-scopes.svg)](https://travis-ci.org/opentable/otj-scopes)

Component Charter
-----------------

* Provides ThreadDelegated Spring scopes.

Component Level
---------------

*Foundation component*

----

ThreadDelegate scope
--------------------

Similar to the 'request' scope, except that it can be passed onto
another thread and is not tied to the (non-threadsafe) HttpRequest
object.

Activated with ThreadDelegatedScopeConfiguration.

----
Copyright (C) 2016 OpenTable, Inc.

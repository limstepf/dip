Dependencies
============
This page contains a list of dependencies used by the main application.


Apache Felix
------------
Apache Felix is an open source implementation of the OSGi Release 5 core framework specification.

Usage:
* `Apache Felix Framework`: The main application embeds an OSGi framework. A service monitor keeps track of available (declarative) services (classes that implement the `Processor` interface in our case).
* `Apache Felix File Install`: is a directory based OSGi management agent. It uses a directory in the file system to install and start a bundle when it is first placed there. It updates the bundle when you update the bundle file in the directory and, when the file is deleted, it will stop and uninstall the bundle.
* `Apache Felix Gogo`: a standard shell for OSGi-based environments. Handy to debug OSGi related things, or just to manually start/stop certain bundles, ... Other than that, this is not required at all, and the bundles can be safely deactivated/removed.
* `Apache Felix Maven Bundle Plugin (BND)`: to build the OSGi bundles.
* `Apache Felix Maven SCR Plugin`: annotations for declarative services (DS)

See: 
* [http://felix.apache.org/](http://felix.apache.org/)



Cloning
-------
Small cloning library that can (deep-) clone ANY Java object.

See:
* [https://github.com/kostaskougios/cloning](https://github.com/kostaskougios/cloning)



exp4j 
-----
exp4j is capable of evaluating expressions and functions in the real domain. It's a small (40KB) library without any external dependencies, that implements Dijkstra's Shunting Yard Algorithm.

Usage:
* Backs `ExpParameter`, `ExpMatrixParameter` in the API.

See: 
* [http://www.objecthunter.net/exp4j/](http://www.objecthunter.net/exp4j/)



Guava (Google Core Libraries for Java)
--------------------------------------
Keep in mind that Guava targets JDK 6+, so a lot of its packages are pretty much rendered obsolete/superseded by JDK 8.

Usage:
* `com.google.common.eventbus` is used to communicate/broadcast between components throughout the main application.
* `com.google.common.reflect` is used to find classes in certain packages.

See: 
* [https://github.com/google/guava](https://github.com/google/guava)



Java Microbenchmarking Harness (JMH)
------------------------------------
JMH is a Java harness for building, running, and analysing nano/micro/milli/macro benchmarks written in Java and other languages targetting the JVM.

Usage:
* The main application has a set of benchmarks in the test package `ch.unifr.diva.dip.benchmarks` that can be run straight from the IDE.

See: 
* [http://openjdk.java.net/projects/code-tools/jmh/](http://openjdk.java.net/projects/code-tools/jmh/)



JUnit
-----
JUnit is a simple framework to write repeatable tests.

See:
* [http://junit.org/](http://junit.org/)



Logback 
-------
...picking up where log4j leaves off. 

See:
* [http://logback.qos.ch/](http://logback.qos.ch/)



OSGi framework
--------------
The OSGi framework is a standardized module system and service platform for the Java programming language. The OSGi standards are defined in the OSGi Alliance and published in OSGi specification documents such as the Core and Compendium specifications.

See: 
* [https://www.osgi.org/developer/specifications/](https://www.osgi.org/developer/specifications/)



Simple Logging Facade for Java (SLF4J)
--------------------------------------
The Simple Logging Facade for Java (SLF4J) serves as a simple facade or abstraction for various logging frameworks (e.g. java.util.logging, logback, log4j) allowing the end user to plug in the desired logging framework at deployment time. 

See:
* [http://www.slf4j.org/](http://www.slf4j.org/)

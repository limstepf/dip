Dependencies
============
This page contains a list of dependencies used by the main application.


Apache Felix
------------
Apache Felix is an open source implementation of the OSGi Release 6 core framework specification.

Usage:
* `Apache Felix Framework`: The main application embeds an OSGi framework. A service monitor keeps track of available (declarative) services (classes that implement the `Processor` interface in our case).
* `Apache Felix File Install`: is a directory based OSGi management agent. It uses a directory in the file system to install and start a bundle when it is first placed there. It updates the bundle when you update the bundle file in the directory and, when the file is deleted, it will stop and uninstall the bundle.
* `Apache Felix Gogo`: a standard shell for OSGi-based environments. Handy to debug OSGi related things, or just to manually start/stop certain bundles, ... Other than that, this is not required at all, and the bundles can be safely deactivated/removed.
* `Apache Felix Maven Bundle Plugin (BND)`: to build the OSGi bundles.
* ~~`Apache Felix Maven SCR Plugin`~~: annotations for declarative services (DS). This one (and the Felix SCR Annotations) is deprecated/no longer needed.

See: 
* [http://felix.apache.org/](http://felix.apache.org/)

### Switching to official OSGi Declarative Services Annotations (R6)

The project initially used Apache Felix SCR Annotations (supporting the OSGi R5 specification) and the maven-src-plugin to put the OSGi Manifest together. Apache Felix annotations are now
in maintenance mode, and it is recommended to simply use the official ones, so that's what we do now. The maven-bundle-plugin just needs to pass the following instruction to (the underlying) bnd:

```XML
<_dsannotations>*</_dsannotations>
```

And instead of using the Felix annotations (given with fully qualified name for clarity):

```Java
@org.apache.felix.scr.annotations.Component;
@org.apache.felix.scr.annotations.Service
public class MyProcessor implements Processor {
	// ...
}
```

just the one official/R6 Component annotation is needed, while specifying the interface(s) to be exposed as declarative service(s):

```Java
@org.osgi.service.component.annotations.Component(service = Processor.class)
public class MyProcessor extends ProcessableBase {
	// ...
}
```

...where `Processor.class` (implemented by ProcessableBase) is the interface of the service (Felix - unless otherwise configured - just added/offered all implemented interfaces as declarative service).


In case the Apache Felix annotations should still be supported too, the maven-bundle-plugin could be configured as follows:

```XML
<plugin>
	<groupId>org.apache.felix</groupId>
	<artifactId>maven-bundle-plugin</artifactId>
	<version>3.3.0</version>
	<extensions>true</extensions>
	<configuration>
		<obrRepository>NONE</obrRepository>
		<instructions>
			<!-- ... -->
			<_plugin>org.apache.felix.scrplugin.bnd.SCRDescriptorBndPlugin;destdir=target/classes</_plugin>
		</instructions>
	</configuration>
	<dependencies>
		<dependency>
			<groupId>org.apache.felix</groupId>
			<artifactId>org.apache.felix.scr.bnd</artifactId>
			<version>1.7.2</version>
		</dependency>
	</dependencies>
</plugin>
```

...but there is not much point in doing so. 

#### Bnd Maven Plugin

What, on the other hand, is definitely an option worth considering is the bnd-maven-plugin (see: [Bnd Maven Plugins](http://bnd.bndtools.org/tools/bnd-maven.html)) in place of the maven-bundle-plugin (now that we're using the official annotations...). In that case the maven-jar-plugin would be needed to too in order to end up with an OSGi bundle, since the bnd-maven-plugin doesn't use the "extensions/package (as) bundle" trick like the maven-bundle-plugin does (see: [Announcing the bnd Maven Plugin](http://njbartlett.name/2015/03/27/announcing-bnd-maven-plugin.html)). 

In the end, it probably doesn't matter too much. The maven-bundle-plugin is most likely easier to use (thanks to a bunch of conventions/rather blunt defaults) at the cost of maybe not that 
exemplary use of OSGi, while the bnd-maven-plugin is by Peter Kriens, who highly encourages best practice. So that might be some more manual/deliberate configuration work for you (Export-/Import packages and the like...). Both rely on Kriens' bnd tool, so... up to you!

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

If you got some time and a cup of coffee, make sure to skim through the *Chapter 112. Declaratice Services Specification* of the OSGi Compendium, Release 6. 

People to definitely look out for on mailing lists, blogs, stackoverflow, ... and consider their words (and always watch the publication date; lots of stuff out there is hopelessly, and confusingly outdated):
* [Peter Kriens](https://github.com/pkriens)
* [Neil Bartlett](https://github.com/njbartlett)

See: 
* [https://www.osgi.org/developer/specifications/](https://www.osgi.org/developer/specifications/)



Simple Logging Facade for Java (SLF4J)
--------------------------------------
The Simple Logging Facade for Java (SLF4J) serves as a simple facade or abstraction for various logging frameworks (e.g. java.util.logging, logback, log4j) allowing the end user to plug in the desired logging framework at deployment time. 

See:
* [http://www.slf4j.org/](http://www.slf4j.org/)

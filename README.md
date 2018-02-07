# Document Image Processor (DIP)
The Document Image Processor (DIP) is a data-driven Document Image Analysis (DIA) workflow system for fast prototyping and framework for the development of specialized DIA tools. 
DIP's data-flow execution model is flexible, and especially designed to support a human-in-the-loop (HITL), thereby providing an attractive solution for a large number of DIA problems impossible to fully automate (for the time being).



## Key Technologies
* Java, and JavaFX
    * [https://docs.oracle.com/javase/8/docs/api/](https://docs.oracle.com/javase/8/docs/api/)
	* [http://docs.oracle.com/javase/8/javafx/api/](http://docs.oracle.com/javase/8/javafx/api/)

* Open Service Gateway Initiative (OSGi), and Declarative Services (DS)
    * [http://www.osgi.org/Specifications/](http://www.osgi.org/Specifications/)
	* [http://felix.apache.org/](http://felix.apache.org/)

* Java Architecture for XML Binding (JAXB)
	* [https://jaxb.java.net/](https://jaxb.java.net/)
	* [https://docs.oracle.com/javase/tutorial/jaxb/intro/](https://docs.oracle.com/javase/tutorial/jaxb/intro/)

* Multi-module Maven 3 project
	* [https://maven.apache.org/](https://maven.apache.org/)

* Version control with Git
	* [https://git-scm.com/](https://git-scm.com/)



## How To Use

```bash
# Clone this repository
$ git clone https://github.com/limstepf/dip

# Go into the repository
$ cd dip

# Compile, package, and install all binary artifacts
$ mvn install

# Run dip
$ ./dip.sh
```

The maven project is setup such that the JAR containing the application will be located at `./app/target/dip-app-1.0.0-SNAPSHOT-jar-with-dependencies.jar` while all bundles will be installed to the `./app/target/bundles` directory.
The bash script used above to run DIP simply executes that JAR. 
Arguments are passed on, such that you can run DIP in headless mode:

```bash
# Passing arguments via bash script
$./dip.sh --help
```

Once compiled, packaged, and installed, individual bundles may be recompiled individually. Thanks to OSGi even at runtime, without having to restart DIP.


### DIP Application Files

All files needed to run DIP are the main JAR from the `./app/target` directory mentioned above, and the two bundle directories `bundles` and `bundles-core`.
Note that DIP will create a `.ch.unifr.diva.dip` directory in your _user directory_ once it starts up (and that directory doesn't exist already). 
It contains: 

* user settings, 
* presets, 
* log and temporary files, 
* another `bundles` directory (that may be used in case the user has no write access to the app's `bundles` directory), and
* a bundle cache directory.

This `.ch.unifr.diva.dip` directory needs to be manually removed to fully uninstall DIP.



## Graphical User Interface (GUI) 

The two main components of DIP's GUI are the _document image editor_ (see Fig. 1) and the _pipeline editor_ (see Fig. 2).

### Document Image Editor


| ![DIP Document Image Editor](https://i.imgur.com/I0lnuzP.png) |
| --- |
| __Figure 1: DIP Document Image Editor:__ with the pipeline tab (D) opened, showing all processors in the pipeline, which may be selected, tweaked, processed and reset from here. All graphical layers emitted from processors are arranged according to the pipeline stages with the first stage at the bottom. Layers are recursive and visibility may be toggled, which allows to quickly inspect and compare the graphical outputs of all processors. The viewport of the document image editor (B) may be controlled by the navigator widget (C). Tools are located in the toolbar (A) to the left. Currently shown are the default global tools: a move tool that just shows the coordinates in the statusbar, and the selection tool, which is a multi-tool (incl. a rectangular, an elliptical, a polygonal lasso, and a brush selection tool). |



### Pipeline Editor

| ![DIP Pipeline Editor](https://i.imgur.com/6XbklRG.png) |
| --- |
| __Figure 2: DIP Pipeline Editor:__ processor views display the processor’s parameters if unfolded/opened. An _auto rearrange_ feature may be enabled to automatically rearrange all processor views after such a view has been un-/folded, using either a horizontal or a vertical flow. Wires are controlled by their input ports, since output ports may serve multiple input ports: wires are created and removed again by starting a drag action from an input port. The outcome of that drag and drop is determined by the drop target, which may be an output port of the same data type, or anything else, and the wire is removed. Processor views may be selected, moved together, deleted, copied, and pasted. New processors are introduced by dragging them from the processor widget(s) onto the viewport of the pipeline editor, whereas its version may be specified prior to such a drag and drop action. Same processors but different versions thereof may be used side by side in the same pipeline. |

## Command-Line Interface (CLI)

DIP also comes with a simple Command-Line Interface (CLI). Needs some more work, alright.

```bash
$ ./dip.sh --help
usage: dip [--dont-save] [-f <arg>] [-h] [--keep-alive] [--list-all]
       [--list-bundles] [--list-pages] [--list-pipelines] [--list-project]
       [--list-system] [--log-config <CFG>] [-p] [-r]
By using the options --help, --list-XXX, --process, or --reset the
application is run in headless mode (no GUI).

    --dont-save          prevents the project from being saved, after
                         being processed in headless mode
 -f,--file <arg>         the DIP project to be loaded/processed
 -h,--help               prints the help/usage
    --keep-alive         forces headless mode, and keeps the application
                         running until the embedded OSGi framework stops
    --list-all           lists the system information, the installed OSGi
                         bundles, and the project pipelines and pages
    --list-bundles       lists the installed OSGi bundles
    --list-pages         lists the project pages
    --list-pipelines     lists the project pipelines
    --list-project       lists the project pipelines and pages
    --list-system        lists the system information
    --log-config <CFG>   set log config with CFG in (DISABLED,
                         DEFAULT_LOGBACK, or PRODUCTION),
                         default=DEFAULT_LOGBACK
 -p,--process            process the project (all pages)
 -r,--reset              reset the project (all pages). Get's executed
                         before processing (if set)

06:23:11.043 [main] INFO ch.unifr.diva.dip.Main - kthxbai.
```

### Apache Felix Gogo Shell
Note that there's an [Apache Felix Gogo](http://felix.apache.org/documentation/subprojects/apache-felix-gogo.html) shell running by default in the background (also with GUI) to "manage" (which is a nice way to say debug) the DIP/OSGi bundles. Use `lb` to list all bundles (you may use `grep`). Find the ones that aren't running. Try to start them manually with `start PID`. Read the error message. Fix the problem. And repeat...



## Architecture
DIP is composed of several key modules as illustrated in Fig. 3.
The DIP application is a general Workflow Management System (WMS) primarily used for prototyping, or even in production if time is short. 
An end-user is ideally faced with a DIP sibling application, which is a specialized environment tailored towards the specific needs of some particular workflow(s). 
Both types of applications are based on the DIP framework that provides the components of the WMS. 

| ![DIP Architecture](https://i.imgur.com/opFznYM.png) |
| --- |
| __Figure 3: DIP Architecture.__ DIP is a modular (black edges are dependencies) and fully self-contained WMS with local workflow execution, that also works as a client accessing methods from DivaServices and others (blue edges are communication). Finally, methods developed in the DIP environment, and provided as DIP bundles, may be packed as small and efficient standalone executables in form of bundle runners that may be submitted to services like DivaServices. |

DIP bundles provide new workflow components called processors (in workflow literature often called: operations, tasks, processes, or jobs), utility code or wrap third party dependencies to be used by other bundles in a conflict-free manner. 
DIP applications can install any number of such bundles, preferably just those that are actually needed in the case of a sibling application. 
Processors packaged in such bundles can be easily converted into a bundle runner, which is a standalone executable that can be submitted to a service like [DivaServices](https://diuf.unifr.ch/main/hisdoc/divaservices). 
Finally, the DIP Application Programming Interface (API) makes sure the framework and the bundles speak the same language and refer to the same classes needed to be known by both parties.




## Workflow Structure and Terminology
Image processing is inherently datacentric, such that the obvious choice for the class of workflow structure is a data-flow. 
DIP uses a hardware metaphor which promises familiar and intuitive terminology and visual representation of the components (see Fig. 4):

* A __Pipeline__ is a workflow composed of a sequence of _processors_.

* __Processors__ are composed of a set of _input-_ and _output ports_ each, a list of strongly typed parameters, and a list of (editing) _tools_. These components define the processor’s "shape", which may be altered at any point in time (e.g. ports, parameters, or tools may be added or removed after a parameter has been changed).

* __Ports__ are connected by a doubly-linked _wire_, that is both ports know about each other. Data is passed by reference and managed by the processor that provides it (i.e. that reference is available on output ports), and giving consumers only (shared) read-access to it. Input ports can only have a single _connection_ (often used as an alternative term for wire), while output ports may be connected to many input ports.

* __Tools__ are only used by processors that required a HITL, and are either composed of:
	* cursors (or brushes), gestures and gesture event handlers, and come with their own set of parameters, called options in this context, or
	* a set of tools to form a multi-tool. However, a multi-tool may not contain another multi-tool.

 The tools of a running (or selected) processor are integrated into the framework’s document image editor, which in turn may already provide some global tools processors can hook up with, such as a various range of selection tools.


| ![DIP Pipeline](https://i.imgur.com/NtvXSfG.png) |
| --- |
| __Figure 4: DIP Pipeline.__ A pipeline is a graph composed of processors (P1, P2, ...). Processors, however, are not connected directly. Instead their input ports (x1, x2, ...) and output ports (y1, y2, ...) are by means of a wire. Only ports of the same data type (indicated by the use of color) can be connected. A running (or selected) processor can provide tools to the end-user to be used in a document image editor. |



## Project Structure and Build System

The `dip` root directory/module currently contains everything needed to build the DIP application together with an initial set of bundles (including third party dependencies) which get deployed into a bundle directory located in the `target` directory of the `app` module. This is born out of convenience (during initial development), not out of necessity. DIP bundles containing utility libraries (intended to be used by processors), processors, or third party dependencies may be built independently of the DIP project, and deployed into one of the `bundles` directories (either the one right next to the JAR of the app in the `target` directory, or the one below `.ch.unifr.diva.dip` in the user directory).


| ![DIP Project Structure](https://i.imgur.com/BPc9Wes.png) |
| --- |
| __Figure 5: DIP Project Structure.__ Maven projects are organized by aggregation (multi-modules) and inheritance (of POMs). The sub-module relationship maps to the DIP file tree (black edges). POM inheritance starts with `pom-super` (orange edges). Multi-modules are shown in blue, parent POMs in orange, and ordinary modules in yellow (fragments and bundles represent many of them). The `docs` directory is not a Maven module. |

Note that there is no separate module for the DIP framework, which at this point is merged with (or embedded into) the DIP application.
The distinction between application and framework is a conceptual one.



## DIP Archetypes

`dip-bundle-archetype` may be used to create a new DIP bundle. The new module will have necessary dependencies declared already, and a template set up for a new (automatic) processor. To use it:


```bash
# Go into the bundles directory (...afterwards make sure 
# the new module was added to the bundles/pom.xml file)
$ cd ./dip/bundles

# Generate a new bundle (in interactive mode)
$ mvn archetype:generate 
    -DarchetypeCatalog=local 
    -DarchetypeArtifactId=dip-bundle-archetype 
    -DarchetypeGroupId=ch.unifr.diva.dip 
    -DarchetypeVersion=1.0.0-SNAPSHOT
```

Alternatively the full specification of the bundle may be given right away:

```bash
# Generate a new bundle
$ mvn archetype:generate 
    -DarchetypeCatalog=local 
    -DarchetypeArtifactId=dip-bundle-archetype 
    -DarchetypeGroupId=ch.unifr.diva.dip 
    -DarchetypeVersion=1.0.0-SNAPSHOT
    -DgroupId=ch.unifr.diva.dip 
    -DartifactId=openimaj-utils  
    -Dpackage=ch.unifr.diva.dip.openimaj.utils 
    -Dversion=1.0.0-SNAPSHOT
```

Default values for `groupId` and `version` are given, so these do not have to be explicitly specified.




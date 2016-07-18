# Document Image Processor (DIP)
The Document Image Processor (DIP) is an image processing application for _document analysis, recognition and engineering_ intended to provide a framework for:

* __Rapid prototyping and evaluation__ of new image processing methods and applications, and
* to __bring__ promising image processing __prototypes to fruition__.

## Key technology
* Java 8, and JavaFX
    * [https://docs.oracle.com/javase/8/docs/api/](https://docs.oracle.com/javase/8/docs/api/)
	* [http://docs.oracle.com/javase/8/javafx/api/](http://docs.oracle.com/javase/8/javafx/api/)

* Java Architecture for XML Binding (JAXB)
	* [https://jaxb.java.net/](https://jaxb.java.net/)
	* [https://docs.oracle.com/javase/tutorial/jaxb/intro/](https://docs.oracle.com/javase/tutorial/jaxb/intro/)

* Open Service Gateway Initiative (OSGi), and Declarative Services (DS)
    * [http://www.osgi.org/Specifications/](http://www.osgi.org/Specifications/)
	* [http://felix.apache.org/](http://felix.apache.org/)


## Architecture
* Version control with Git
	* [https://git-scm.com/](https://git-scm.com/)

* Multi-module Maven 3 project
	* [https://maven.apache.org/](https://maven.apache.org/)

* Host application embeds an OSGi framework (Apache Felix)

* DIP API defines a _Processor_ service (besides other classes that need to be shared by the host application and OSGi bundles)

* OSGi bundles, kept in their own Maven module, provide services that implement the _Processor_ interface

### Key concepts
Image processing in DIP is done with freely composable image processing __pipelines__ consisting of freely connected (image) __processors__:

1. __Processors__ consume the signal/values on input ports, and provide some resulting signal/values on their output ports. For further configuration each processor offers a set of parameters, image layers can be published to the editor for visualization and user interaction, and processing tools can be offered as means to manually interact with the user.

2. A __pipeline__ is a directed acyclic graph (DAG) with the PageGenerator, offering the image of a page, as root processor. Hence processor are naturally grouped into processing stages.

3. A __DIP project__ consists of a set of pipelines, and a set of __pages__ (or image documents) with a pipeline assigned each, and is stored in a zip-file (opened as _ZipFileSystem_ during usage).

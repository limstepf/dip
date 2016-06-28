# Document Image Processor (DIP)
The Document Image Processor (DIP) is an image processing application for _document analysis, recognition and engineering_ intended to provide a framework for:

* __Rapid prototyping and evaluation__ of new image processing methods and applications, and
* to __bring__ promising image processing __prototypes to fruition__.

## Key technology
* Java 8 and JavaFX
* Java Architecture for XML Binding (JAXB)
* Open Service Gateway Initiative (OSGi) and Declarative Services (DS)

## Architecture
* Multi-module Maven project
* Host application embeds an OSGi framework (Apache Felix)
* DIP API defines a _Processor_ service (besides other classes that need to be shared by the host application and OSGi bundles)
* OSGi bundles, kept in their own Maven module, provide services that implement the _Processor_ interface

### Key concepts
Image processing in DIP is done with freely composable image processing __pipelines__ consisting of freely connected (image) __processors__:

1. __Processors__ consume the signal/values on input ports, and provide some resulting signal/values on their output ports. For further configuration each processor offers a set of parameters, image layers can be published to the editor for visualization and user interaction, and processing tools can be offered as means to manually interact with the user.

2. A __pipeline__ is a directed acyclic graph (DAG) with the PageGenerator, offering the image of a page, as root processor. Hence processor are naturally grouped into processing stages.

3. A __DIP project__ consists of a set of pipelines, and a set of __pages__ (or image documents) with a pipeline assigned each, and is stored in a zip-file (opened as _ZipFileSystem_ during usage).

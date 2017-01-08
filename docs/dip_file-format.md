DIP File Format
===============
DIP files are used to store a DIP project. This is not a custom file format, but just a ZIP file in disguise. So all files of a DIP project can be easily accessed if needed (e.g. to retrieve intermediate images). The original images of pages, however, are not stored again in the DIP file, just a reference (and a hash) to these files is stored. 

Upon opening the project a temporary copy of the DIP file is created and opened as a zip file system that can be made dirty without much worries. Only upon saving the project the original DIP file will be modified/overwritten.

DIP File system hierarchy
-------------------------

```
.
+-- pages
|   +-- 1
|   |   +-- processors
|   |   |   +-- 7
|   |   |   |   +-- data
|   |   |   |   |   +-- band1-vis.png
|   |   |   |   |   +-- band1.bmat
|   |   |   |   |   +-- band2-vis.png
|   |   |   |   |   +-- band2.bmat
|   |   |   |   |   +-- band3-vis.png
|   |   |   |   |   +-- band3.bmat
|   |   |   |   |
|   |   |   |   +-- data.xml
|   |   |   |
|   |   |   +-- 13
|   |   |   +-- ...
|   |   |
|   |   +-- patch.xml
|   |
|   +-- 2
|   +-- ...
|
+-- pipelines.xml
+-- project.xml
```

### The `project.xml` file
This is the main file of the project, keeping track of general project information, and most importantly the set of `pages` with associated page id, pipeline id, filename of the page image, a hash, and a path to the (last known) location of the file.

### The `pipelines.xml` file
This file stores all globally (or project-wide) defined (image processing) pipelines. While these pipelines are just blueprints, each page will refer/link to exactly one of them, but operate on a working copy, since a page can patch all parameters of processors in its pipeline.

### The `pages` directory
For every page there is a directory, named by a unique page id, in here. Page ids are not reused, so some ids might be unused.

A page directory contains a `processors` directory where persistent data of all processors is stored, and an optional `patch.xml` file which is applied to the working copy of the page its pipeline in order to adjust (or fine-tune) parameters of processors.

A processor directory is named by a unique processor id, and contains a `data` directory and a `data.xml` file. A processor is free to store anything in the `data` directory, at the cost of having to managed these files on its own (save, load, reset, ...). Data that can be marshalled to XML can, more conveniently (processors just can manage an object hashmap passed by their processor context), be stored in the `data.xml` file.

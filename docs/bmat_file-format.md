
BMAT File Format
================
BMAT files are used to store instances of `BufferedMatrix` (located in the `ch.unifr.diva.dip.api.imaging` package), which are generalized `BufferedImage`s with any number of bands, variable sample precision/type (usually floats, but also double or int are supported), and no color model. 

While these files are often used to store images with samples in floating point precision (e.g. for images in Lab color space), it's an efficent file format for any kind of dense matrix, or raster data in general. And since `BufferedMatrix` is a/extends from `BufferedImage`, it comes with a nice API to use too (e.g. `WritableRaster`). In fact, you can just plug in a `BufferedMatrix` where a `BufferedImage` is expected (in most cases). They just can't be displayed directly, since the meaning of samples is not directly encoded, but (assumed to be) given by context. Images stored as BMAT files are typically interpreted by an associated color model defined in `SimpleColorModel` (located in the same package), but that is the business of the processor owning that file. 

A custom binary format (described in the following table) is used, since writing/reading huge matrices in a human readable format (e.g. csv, xml, or matrix market) is way too slow.


bytes | description
----- | -----------
4     | The string "BMAT" in US-ASCII, one byte per char
4     | An integer encoding the width of the matrix
4     | An integer encoding the height of the matrix
4     | An integer encoding the number of bands (or number of matrices)
4     | An integer encoding `n`: the number of following chars/bytes
`n`   | A string with `n` chars (in US-ASCII, one byte per char) encoding the sample data type/precision ("INT", "FLOAT", or "DOUBLE")
4     | An integer encoding `m`: the number of following chars/bytes
`m`   | A string with `m` chars (in US-ASCII, one byte per char) encoding the sample interleave type ("BIP", or "BSQ")
width `x` height `x` bands `x` `b` | The samples (in BIP or BSQ) where `b` is the number of bytes per sample (e.g. 4 bytes for INT and FLOAT, 8 for DOUBLE)


Reading and writing of BMAT (or `BufferedMatrix`) files is implemented in the `BufferedIO` utility class (also located in the `ch.unifr.diva.dip.api.imaging` package). Samples are stored either Band-Interleaved-by-Pixel (BIP), or Band-Sequential (BSQ), which affects both: the data buffer that backs the `BufferedMatrix`, as well as the way samples are written to/read from disk in the BMAT file format.


Band-Interleaved-by-Pixel (BIP)
-------------------------------

* Uses a data buffer with a single bank with `width x height x bands` elements.
* Good for accessing all samples (e.g. a pixel) at once.

Example with 3 bands:
```
R G B R G B R G B R G B R G B
R G B R G B R G B R G B R G B
R G B R G B R G B R G B R G B
```


Band-Sequential (BSQ) interleave
--------------------------------

* Uses a data buffer with a bank for each band with `width x height` elements each.
* Good for accessing (all) samples of single bands.

Example with 3 bands:
```
R R R R R
R R R R R
R R R R R
G G G G G
G G G G G
G G G G G
B B B B B
B B B B B
B B B B B
```

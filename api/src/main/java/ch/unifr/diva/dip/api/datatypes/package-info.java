/**
 * DIP datatypes. Input and output ports of DIP processors are published as one
 * of the datatypes defined in this package.
 *
 * The datatype is just a declaration (s.t. an input port can only connect to an
 * output port of the same data type). The underlying implementation can be any
 * Class from the Java standard library, or a class defined as "DIP data
 * structure". The goal here is that all processors are on the same page, and
 * able to communicate with each other, even though internally they might use
 * different represenations/implementations of the same data.
 *
 * @see ch.unifr.diva.dip.api.datastructures
 */
package ch.unifr.diva.dip.api.datatypes;

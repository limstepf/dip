@XmlSchema(
		namespace = "http://www.unifr.ch/diva/dip",
		elementFormDefault = XmlNsForm.QUALIFIED
)
@XmlJavaTypeAdapters({
	@XmlJavaTypeAdapter(value = BooleanPropertyAdapter.class, type = BooleanProperty.class),
	@XmlJavaTypeAdapter(value = BufferedImageAdapter.class, type = java.awt.image.BufferedImage.class),
	@XmlJavaTypeAdapter(value = BufferedMatrixAdapter.class, type = ch.unifr.diva.dip.api.imaging.BufferedMatrix.class),
	@XmlJavaTypeAdapter(value = DoublePropertyAdapter.class, type = DoubleProperty.class),
	@XmlJavaTypeAdapter(value = ImageAdapter.class, type = javafx.scene.image.Image.class),
	@XmlJavaTypeAdapter(value = IntegerPropertyAdapter.class, type = IntegerProperty.class),
	@XmlJavaTypeAdapter(value = PathAdapter.class, type = java.nio.file.Path.class),
	@XmlJavaTypeAdapter(value = StringPropertyAdapter.class, type = StringProperty.class)
})

/**
 * DIP data structures. Data structures defined here are primarily used to
 * communicate between processors (once mapped to a DIP datatype). Internally
 * different data structures, from different libraries (or from the same
 * libraries, but a different version...) can be used, put in- and outputs
 * should be converted to "DIP data structures" and published as "DIP datatypes"
 * s.t. all processors are on the same page.
 *
 * Secondly, all classes here are "marshallable" by JAXB (see
 * {@code XmlUtils.xml} where all classes in this package are statically read in
 * to be added to all JAXB contexts.). Some "data structures" in here are even
 * exclusively defined for this purpose (BufferedImageContainer, ImageContainer
 * and FileReference) for ease of use (e.g. to store a single image in a
 * processors persitent data-map). Then again, consider defining custom
 * "marshallable" classes for your processors.
 *
 * @see ch.unifr.diva.dip.api.datatypes
 */
package ch.unifr.diva.dip.api.datastructures;

import ch.unifr.diva.dip.api.utils.jaxb.BooleanPropertyAdapter;
import ch.unifr.diva.dip.api.utils.jaxb.BufferedMatrixAdapter;
import ch.unifr.diva.dip.api.utils.jaxb.BufferedImageAdapter;
import ch.unifr.diva.dip.api.utils.jaxb.DoublePropertyAdapter;
import ch.unifr.diva.dip.api.utils.jaxb.ImageAdapter;
import ch.unifr.diva.dip.api.utils.jaxb.IntegerPropertyAdapter;
import ch.unifr.diva.dip.api.utils.jaxb.PathAdapter;
import ch.unifr.diva.dip.api.utils.jaxb.StringPropertyAdapter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;

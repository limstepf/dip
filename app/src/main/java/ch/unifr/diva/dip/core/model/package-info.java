/**
 * Data models.
 */
@javax.xml.bind.annotation.XmlSchema(
		xmlns = {
			@javax.xml.bind.annotation.XmlNs(
					prefix = "xs",
					namespaceURI = "http://www.w3.org/2001/XMLSchema"
			),
			@javax.xml.bind.annotation.XmlNs(
					prefix = "xsi",
					namespaceURI = "http://www.w3.org/2001/XMLSchema-instance"
			),
			@javax.xml.bind.annotation.XmlNs(
					prefix = "dip",
					namespaceURI = "http://www.unifr.ch/diva/dip"
			)
		}
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
package ch.unifr.diva.dip.core.model;

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
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;


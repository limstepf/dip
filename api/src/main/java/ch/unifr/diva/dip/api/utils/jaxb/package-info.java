/**
 * Adapters for JAXB to marshall/unmarshall Java classes with no default
 * mapping. Usage: either annotate individual variables/methods, e.g.:
 * <pre>
 * <code>
 * {@literal @}XmlJavaTypeAdapter(IntegerPropertyAdapter.class)
 * public IntegerProperty someInteger = new SimpleIntegerProperty(42);
 * </code>
 * </pre>
 *
 * ...or define all adapters in the package-info.java file (in the package where
 * classes to be marshalled/unmarshalled reside):
 * <pre>
 * <code>
 * {@literal @}XmlJavaTypeAdapters({
 *    {@literal @}XmlJavaTypeAdapter(value = BooleanPropertyAdapter.class, type = BooleanProperty.class),
 *    {@literal @}XmlJavaTypeAdapter(value = DoublePropertyAdapter.class, type = DoubleProperty.class),
 *    {@literal @}XmlJavaTypeAdapter(value = IntegerPropertyAdapter.class, type = IntegerProperty.class),
 *    {@literal @}XmlJavaTypeAdapter(value = StringPropertyAdapter.class, type = StringProperty.class)
 * })
 * package ch.unifr.diva.dip.package
 *
 * import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
 * import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;
 *
 * import javafx.beans.property.BooleanProperty;
 * import javafx.beans.property.DoubleProperty;
 * import javafx.beans.property.IntegerProperty;
 * import javafx.beans.property.StringProperty;
 * </code>
 * </pre>
 */
package ch.unifr.diva.dip.api.utils.jaxb;

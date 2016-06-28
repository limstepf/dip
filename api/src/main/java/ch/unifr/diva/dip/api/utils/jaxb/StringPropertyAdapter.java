package ch.unifr.diva.dip.api.utils.jaxb;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * JavaFx StringProperty (XML-)adapter for JAXB.
 */
public class StringPropertyAdapter extends XmlAdapter<String,StringProperty> {

	@Override
	public StringProperty unmarshal(String value) throws Exception {
		return new SimpleStringProperty(value);
	}

	@Override
	public String marshal(StringProperty property) throws Exception {
		return property.getValueSafe();
	}

}

package ch.unifr.diva.dip.api.utils.jaxb;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * JavaFx BooleanProperty (XML-)adapter for JAXB.
 */
public class BooleanPropertyAdapter extends XmlAdapter<Boolean,BooleanProperty> {

	@Override
	public BooleanProperty unmarshal(Boolean value) throws Exception {
		return new SimpleBooleanProperty(value);
	}

	@Override
	public Boolean marshal(BooleanProperty property) throws Exception {
		return property.getValue();
	}

}

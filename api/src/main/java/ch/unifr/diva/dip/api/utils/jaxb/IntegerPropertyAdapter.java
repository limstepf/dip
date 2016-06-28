package ch.unifr.diva.dip.api.utils.jaxb;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * JavaFX IntegerProperty (XML-)adapter for JAXB.
 */
public class IntegerPropertyAdapter extends XmlAdapter<Integer,IntegerProperty> {

	@Override
	public IntegerProperty unmarshal(Integer value) throws Exception {
		return new SimpleIntegerProperty(value);
	}

	@Override
	public Integer marshal(IntegerProperty property) throws Exception {
		return property.getValue();
	}

}

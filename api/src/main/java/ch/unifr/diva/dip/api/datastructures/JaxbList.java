package ch.unifr.diva.dip.api.datastructures;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Generic, marshalable/unmarshable list.
 *
 * @param <T> type of the list items.
 */
@XmlRootElement(name = "list")
@XmlAccessorType(XmlAccessType.NONE)
public class JaxbList<T> {

	@XmlElement(name = "item")
	protected final List<T> list;

	@SuppressWarnings("unused")
	public JaxbList() {
		this.list = null;
	}

	public JaxbList(List<T> list) {
		this.list = list;
	}

	public List<T> getList() {
		return this.list;
	}
}

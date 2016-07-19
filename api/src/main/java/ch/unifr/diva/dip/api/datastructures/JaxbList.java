package ch.unifr.diva.dip.api.datastructures;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Generic, marshallable/unmarshable list.
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

	/**
	 * Creates a new marshallable/unmarshable list wrapping the given list.
	 *
	 * @param list the list to be wrapped.
	 */
	public JaxbList(List<T> list) {
		this.list = list;
	}

	/**
	 * Returns the wrapped list.
	 *
	 * @return the wrapped list.
	 */
	public List<T> getList() {
		return this.list;
	}

}

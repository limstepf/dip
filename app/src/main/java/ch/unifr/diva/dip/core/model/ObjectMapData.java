package ch.unifr.diva.dip.core.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Thread-safe object map. Can be safely accessed from any thread.
 */
@XmlRootElement(name = "object-map")
@XmlAccessorType(XmlAccessType.NONE)
public class ObjectMapData {

	@XmlElement
	public final Map<String, Object> objects;

	@SuppressWarnings("unused")
	public ObjectMapData() {
		this.objects = new ConcurrentHashMap<>();
	}

	public ObjectMapData(ConcurrentHashMap<String, Object> objects) {
		this.objects = objects;
	}
	
}


package ch.unifr.diva.dip.api.utils.jaxb;

import java.nio.file.Path;
import java.nio.file.Paths;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Path adapter for JAXB.
 */
public class PathAdapter extends XmlAdapter<String,Path> {

	@Override
	public Path unmarshal(String value) throws Exception {
		return Paths.get(value);
	}

	@Override
	public String marshal(Path path) throws Exception {
		return path.toString();
	}

}

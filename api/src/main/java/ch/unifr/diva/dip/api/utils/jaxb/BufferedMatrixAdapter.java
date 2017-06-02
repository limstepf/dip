package ch.unifr.diva.dip.api.utils.jaxb;

import ch.unifr.diva.dip.api.datastructures.BufferedMatrixContainer;
import ch.unifr.diva.dip.api.datastructures.BufferedMatrix;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * BufferedMatrix adapter.
 */
public class BufferedMatrixAdapter extends XmlAdapter<BufferedMatrixContainer, BufferedMatrix> {

	@Override
	public BufferedMatrix unmarshal(BufferedMatrixContainer value) throws Exception {
		return value.getBufferedMatrix();
	}

	@Override
	public BufferedMatrixContainer marshal(BufferedMatrix image) throws Exception {
		return new BufferedMatrixContainer(image);
	}

}

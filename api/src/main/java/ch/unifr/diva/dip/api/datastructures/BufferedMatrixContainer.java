package ch.unifr.diva.dip.api.datastructures;

import ch.unifr.diva.dip.api.imaging.BufferedIO;
import ch.unifr.diva.dip.api.imaging.BufferedMatrix;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A container to store (and marshall) BufferedMatrix.
 */
@XmlRootElement(name = "buffered-matrix")
@XmlAccessorType(XmlAccessType.NONE)
public class BufferedMatrixContainer {

	@XmlElement
	public final byte[] bytes;

	@SuppressWarnings("unused")
	public BufferedMatrixContainer() {
		this.bytes = null;
	}

	public BufferedMatrixContainer(BufferedMatrix mat) throws IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BufferedIO.writeMat(mat, baos);
		this.bytes = baos.toByteArray();
	}
	
	public BufferedMatrix getBufferedMatrix() throws IOException {
		final ByteArrayInputStream bais = new ByteArrayInputStream(this.bytes);
		return BufferedIO.readMat(bais);
	}
}

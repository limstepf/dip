package ch.unifr.diva.dip.osgi;

/**
 * Signals that an OSGi invalid service exception has occurred.
 */
public class OSGiInvalidServiceException extends Exception {

	private static final long serialVersionUID = -9115873344442500287L;

	/**
	 * Constructs an instance of {@code OSGiInvalidServiceException} with the
	 * specified detail message.
	 *
	 * @param msg the detail message.
	 */
	public OSGiInvalidServiceException(String msg) {
		super(msg);
	}

}

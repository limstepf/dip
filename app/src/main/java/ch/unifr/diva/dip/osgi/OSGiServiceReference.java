package ch.unifr.diva.dip.osgi;

import java.io.Serializable;
import java.util.Objects;
import org.osgi.framework.Version;

/**
 * OSGi service reference. This class is serializable s.t. we can put these
 * objects in JavaFX clipboards/dragboards (used to drag processors into the
 * pane of the pipeline editor).
 */
public class OSGiServiceReference implements Serializable {

	private static final long serialVersionUID = -6608088315718924312L;

	/**
	 * The PID of the service.
	 */
	public final String pid;

	/**
	 * The version of the service.
	 */
	public final String version;

	/**
	 * Creates a new OSGi service reference.
	 *
	 * @param pid pid of the service.
	 * @param version version of the service.
	 */
	public OSGiServiceReference(String pid, Version version) {
		this(pid, version.toString());
	}

	/**
	 * Creates a new OSGi service reference.
	 *
	 * @param pid pid of the service.
	 * @param version version of the service.
	 */
	public OSGiServiceReference(String pid, String version) {
		this.pid = pid;
		this.version = version;
	}

	/**
	 * Returns the version of the service.
	 *
	 * @return the version of the service.
	 */
	public Version getVersion() {
		return new Version(version);
	}

	@Override
	public int hashCode() {
		int hash = 17;
		hash = 31 * hash + pid.hashCode();
		hash = 31 * hash + version.hashCode();
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final OSGiServiceReference other = (OSGiServiceReference) obj;
		if (!Objects.equals(this.pid, other.pid)) {
			return false;
		}
		return Objects.equals(this.version, other.version);
	}

}

package ch.unifr.diva.dip.api.datastructures;

import java.nio.file.Paths;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * FileReference wrapps a File/Path that can be easily marshalled to XML. There
 * are some problems with storing a File in a hashmap (which ends up as
 * xs:string, then unmarshalling fails...) and with Paths anyways since that's
 * an interface. Hence we use this class to store references to files.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class FileReference {

	@XmlAttribute
	public final String path;

	@SuppressWarnings("unused")
	public FileReference() {
		this.path = null;
	}

	/**
	 * Creates a new file reference of the given path.
	 *
	 * @param path a path to a file.
	 */
	public FileReference(String path) {
		this.path = path;
	}

	/**
	 * Creates a new file reference of the given file.
	 *
	 * @param file the file.
	 */
	public FileReference(java.io.File file) {
		this.path = file.toString();
	}

	/**
	 * Creates a new file reference of the given path.
	 *
	 * @param path a path to a file.
	 */
	public FileReference(java.nio.file.Path path) {
		this.path = path.toString();
	}

	/**
	 * Returns the File this FileReference is pointing to.
	 *
	 * @return the FileReference as a File.
	 */
	public java.io.File toFile() {
		return new java.io.File(this.path);
	}

	/**
	 * Returns the Path this FileReference is pointing to.
	 *
	 * @return the FileReference as a Path.
	 */
	public java.nio.file.Path toPath() {
		return Paths.get(this.path);
	}

}

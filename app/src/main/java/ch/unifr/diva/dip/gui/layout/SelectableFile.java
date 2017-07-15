package ch.unifr.diva.dip.gui.layout;

import java.io.File;
import java.nio.file.Path;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Wrapper for files/paths to be used in ListViews together with a
 * SelectableFileCell factory. A SelectableFile can be selected only if it is
 * not disabled.
 *
 * @see SelectableFileCell
 */
public class SelectableFile {

	private final File file;
	private final BooleanProperty selected = new SimpleBooleanProperty();
	private final BooleanProperty disable = new SimpleBooleanProperty();

	/**
	 * Creates a default SelectableFile.
	 *
	 * @param path a Path (to a File).
	 */
	public SelectableFile(Path path) {
		this(path.toFile());
	}

	/**
	 * Creates a default SelectableFile.
	 *
	 * @param file a File.
	 */
	public SelectableFile(File file) {
		this.file = file;
		this.selected.set(true);
		this.disable.set(false);
	}

	/**
	 * Returns the File.
	 *
	 * @return the File.
	 */
	public File getFile() {
		return file;
	}

	/**
	 * Returns a Path to the File.
	 *
	 * @return a Path (to the File).
	 */
	public Path getPath() {
		return file.toPath();
	}

	/**
	 * A propertys used to represent whether the file is selected.
	 *
	 * @return the selectedProperty.
	 */
	public final BooleanProperty selectedProperty() {
		return selected;
	}

	/**
	 * Selects/deselects the File.
	 *
	 * @param selected {@code true} to select the File, {@code false} to
	 * deselect it.
	 */
	public void setSelected(boolean selected) {
		this.selected.set(selected);
	}

	/**
	 * Returns whether the File is selected or not.
	 *
	 * @return {@code true} if the File is selected, {@code false} otherwise.
	 */
	public boolean isSelected() {
		return selected.get();
	}

	/**
	 * A property to represent whether this File can be selected at all.
	 *
	 * @return the disableProperty.
	 */
	public final BooleanProperty disableProperty() {
		return disable;
	}

	/**
	 * Disables/enables the File for selection.
	 *
	 * @param disable {@code true} to disable the File, {@code false} to enable
	 * it.
	 */
	public void setDisable(boolean disable) {
		this.disable.set(disable);
	}

	/**
	 * Returns whether the File is disabled for selection or not.
	 *
	 * @return {@code true} if the File is disabled, {@code false} otherwise.
	 */
	public boolean isDisable() {
		return disable.get();
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof SelectableFile)) {
			return false;
		}
		final SelectableFile item = (SelectableFile) o;
		return item.hashCode() == this.hashCode();
	}

	@Override
	public int hashCode() {
		return file.hashCode();
	}

	@Override
	public String toString() {
		return file.toString();
	}
}

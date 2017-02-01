package ch.unifr.diva.dip.gui.pe;

import ch.unifr.diva.dip.api.datastructures.FileReference;
import ch.unifr.diva.dip.api.datastructures.ValueListSelection;
import ch.unifr.diva.dip.api.parameters.FileParameter;
import ch.unifr.diva.dip.api.parameters.TextParameter;
import ch.unifr.diva.dip.api.parameters.XorParameter;
import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.ApplicationSettings;
import ch.unifr.diva.dip.core.model.DipData;
import ch.unifr.diva.dip.gui.dialogs.AbstractDialog;
import static ch.unifr.diva.dip.gui.dialogs.AbstractDialog.getCancelButton;
import static ch.unifr.diva.dip.gui.dialogs.AbstractDialog.getDefaultButton;
import ch.unifr.diva.dip.gui.layout.VerticalSplitPane;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.util.Callback;
import javax.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for data item dialogs.
 *
 * @param <T> class of the data items.
 */
public abstract class DataItemDialogBase<T extends DataItemListView.DataItem> extends AbstractDialog {

	protected static final Logger log = LoggerFactory.getLogger(DataItemDialogBase.class);
	protected final ApplicationHandler handler;
	protected final VerticalSplitPane vspane;
	protected final Button ok;
	protected final Button cancel;
	protected final XorParameter dst;
	protected final FileParameter dstFile;
	protected final Label dstLabel;
	protected final DataItemListView<T> dataItemList;
	protected final Label listLabel;

	protected final Path userDirFile;
	protected Path currentFile;

	/**
	 * Creates a new data item dialog. Use {@code setTitle} to set the title of
	 * the dialog, {@code setListLabel} to adjust the label for the data items,
	 * and finally call {@code initFile} once all has been set up.
	 *
	 * <p>
	 * Consider overwriting {@code isValid} for exporting/saving dialogs, e.g.
	 * to ensure that the file is a DIP data file with
	 * {@code isValidDestination(true)}.
	 *
	 * @param handler the application handler.
	 * @param userDirFile the default user directory file.
	 * @param editable whether or not the list cells of the data items are
	 * editable (to export), or selectable (to import).
	 */
	public DataItemDialogBase(ApplicationHandler handler, Path userDirFile, boolean editable) {
		super(handler.uiStrategy.getStage());
		this.handler = handler;
		this.userDirFile = userDirFile;

		this.vspane = new VerticalSplitPane();
		this.root.setCenter(vspane.getNode());

		this.dstLabel = new Label(
				(editable
						? localize("destination")
						: localize("source"))
				+ ":"
		);
		this.dstFile = new FileParameter(
				"",
				localize("locate"),
				editable
						? FileParameter.Mode.SAVE
						: FileParameter.Mode.OPEN
		);
		this.dstFile.addFileChooserViewHook((c) -> {
			final Path recent = handler.settings.recentFiles.dipDataDirectory;
			if (recent != null) {
				c.setInitialDirectory(recent.toFile());
			}
			c.getExtensionFilters().addAll(
					ApplicationSettings.dipDataFileExtensionFilter
			);
		});
		this.dst = new XorParameter("dst", Arrays.asList(
				new TextParameter(localize("directory.user")),
				this.dstFile
		));
		vspane.getLeftChildren().setAll(
				dstLabel,
				this.dst.view().node()
		);

		this.listLabel = new Label();
		this.dataItemList = editable
				? DataItemListView.newEditableDataItemListView()
				: DataItemListView.newSelectableDataItemListView(selectableCallback);
		if (editable) {
			dataItemList.addMenuItem(dataItemList.getDeleteItemMenuItem());
		}
		vspane.getRightChildren().setAll(
				listLabel,
				this.dataItemList.getNode()
		);

		this.ok = getDefaultButton(
				dataItemList.isEditable()
						? localize("save")
						: localize("load")
		);
		this.cancel = getCancelButton(stage);

		ok.setDisable(true);
		ok.setOnAction(actionEventHandler);
		buttons.add(ok);
		buttons.add(cancel);
	}

	protected final Callback<T, Void> selectableCallback = (p) -> {
		if (isValid()) {
			onAction();
		}
		return null;
	};

	protected final EventHandler actionEventHandler = (c) -> onAction();

	protected void onAction() {
		if (doAction()) {
			stage.close();
		}
	}

	protected final InvalidationListener fileListener = (c) -> onFileChanged();

	protected void onFileChanged() {
		final ValueListSelection val = this.dst.get();
		switch (val.selection) {
			case 1:
				final FileReference ref = (FileReference) val.get(1);
				this.currentFile = (ref == null) ? null : ref.toPath();
				handler.settings.recentFiles.setDataFile(this.currentFile);
				break;

			default:
				handler.settings.recentFiles.setDataFile(null);
				this.currentFile = this.userDirFile;
				break;
		}
		onSelectFile(this.currentFile);
		update();
	}

	/**
	 * Initializes the destination (or source) file. This method needs to be
	 * called in implementing classes once all has been setup.
	 */
	protected final void initFile() {
		final Path file = handler.getRecentDataFile(this.userDirFile);
		final ValueListSelection val = this.dst.get();
		if (this.userDirFile.equals(file)) {
			val.selection = 0;
			this.currentFile = this.userDirFile;
		} else {
			val.selection = 1;
			val.set(1, new FileReference(file.toString()));
			this.currentFile = file;
		}
		this.dst.set(val);
		onSelectFile(this.currentFile);
		update();

		this.dst.property().addListener(fileListener);
	}

	/**
	 * Callback after a new file has been selected as destination (or source).
	 * This method should repopulate the list view.
	 *
	 * @param file the new file (same as {@code currentFile}).
	 */
	public abstract void onSelectFile(Path file);

	/**
	 * Executes the dialog's action. E.g. load/import, or save/export data
	 * items.
	 *
	 * @return True on success, False on failure.
	 */
	public abstract boolean doAction();

	/**
	 * Loads DIP data from a file, or initializes new, empty DIP data.
	 *
	 * @param file the file to read from (doesn't need to exist yet).
	 * @return a DIP data object.
	 */
	protected DipData loadDipData(Path file) {
		if (file != null && Files.exists(file)) {
			try {
				return DipData.load(file);
			} catch (IOException | JAXBException | ClassCastException ex) {
				log.warn("failed to load DIP data file: {}", file, ex);
				handler.uiStrategy.showError(localize("file.format.invalid"), ex);
			}
		}
		return new DipData();
	}

	/**
	 * Saves DIP data to a file.
	 *
	 * @param data the DIP data object.
	 * @param file the destination.
	 * @return True if successful, False on failure.
	 */
	protected boolean saveDipData(DipData data, Path file) {
		try {
			data.save(file);
		} catch (JAXBException ex) {
			log.error("failed to save DIP data file: {}", this.currentFile, ex);
			handler.uiStrategy.showError(ex);
			return false;
		}
		return true;
	}

	/**
	 * Sets the items (or underlying observable list) of the data list view.
	 *
	 * @param items the observable list of items.
	 */
	protected void setItems(ObservableList<T> items) {
		this.dataItemList.setItems(items);
	}

	/**
	 * Returns the items of the data list view.
	 *
	 * @return the items of the data list view.
	 */
	protected ObservableList<T> getItems() {
		return this.dataItemList.getItems();
	}

	/**
	 * Can be attached to various properties to update the state of the ok
	 * button.
	 */
	protected final InvalidationListener updateListener = (c) -> update();

	/**
	 * Updates the disabled state of the ok button.
	 */
	public void update() {
		ok.setDisable(!isValid());
	}

	/**
	 * Checks whether doAction can be executed.
	 *
	 * @return True if doAction can be executed, False otherwise.
	 */
	public boolean isValid() {
		return isValidDestination(false);
	}

	/**
	 * Checks whether a valid destination (or source) is selected.
	 *
	 * @param assertDipDataFile asserts that the file is a dip data file if
	 * True.
	 * @return True if the destination is valid, False otherwise.
	 */
	protected boolean isValidDestination(boolean assertDipDataFile) {
		final int sel = dst.get().selection;
		if (sel == 0) {
			return true;
		}

		final FileReference ref = dstFile.get();
		if (ref == null) {
			return false;
		}

		final Path file = dstFile.get().toPath();
		if (assertDipDataFile && Files.exists(file)) {
			return DipData.isDipDataFile(file);
		}

		final Path parent = file.getParent();
		return (parent == null) ? false : Files.isDirectory(parent);
	}

	/**
	 * Sets the label of the list items.
	 *
	 * @param label the label of the list items.
	 */
	protected void setListLabel(String label) {
		this.listLabel.setText(label + ":");
	}

	/**
	 * Sets the selection mode of the list view. By default editable data item
	 * list views can select multiple, selectable only single items. It might be
	 * desirable to also have multiple selection in the latter case.
	 *
	 * @param mode the selection mode.
	 */
	protected void setSelectionMode(SelectionMode mode) {
		this.dataItemList.setSelectionMode(mode);
	}

}

package ch.unifr.diva.dip.api.parameters;

import ch.unifr.diva.dip.api.datastructures.FileReference;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * A file parameter.
 */
public class FileParameter extends PersistentParameterBase<FileReference, FileParameter.FileView> {

	protected final String action;
	protected final Mode mode;

	/**
	 * File parameter mode.
	 */
	public enum Mode {

		/**
		 * Mode to open/read from a file.
		 */
		OPEN,
		/**
		 * Mode to save/write to a file.
		 */
		SAVE
	}

	/**
	 * Creates a file parameter.
	 *
	 * @param label label.
	 * @param action action (or dialog-title) label.
	 * @param mode dialog mode.
	 */
	public FileParameter(String label, String action, Mode mode) {
		super(label, FileReference.class, null);

		this.action = action;
		this.mode = mode;
	}

	@Override
	protected FileView newViewInstance() {
		return new FileView(this);
	}

	protected final List<ViewHook<FileChooser>> viewHooks = new ArrayList<>();

	/**
	 * Adds a view hook to customize the file chooser. This method is only
	 * called if the view of the parameter is actually requested.
	 *
	 * @param hook hook method for a label.
	 */
	public void addFileChooserViewHook(ViewHook<FileChooser> hook) {
		this.viewHooks.add(hook);
	}

	/**
	 * Removes a view hook.
	 *
	 * @param hook hook method to be removed.
	 */
	public void removeFileChooserViewHook(ViewHook<FileChooser> hook) {
		this.viewHooks.remove(hook);
	}

	/**
	 * File parameter view.
	 */
	public static class FileView extends PersistentParameterBase.ParameterViewBase<FileParameter, FileReference, HBox> {

		protected final TextField text = new TextField();
		protected final Button button = new Button();
		protected FileReference file;

		/**
		 * Creates a new file view.
		 *
		 * @param parameter the file parameter.
		 */
		public FileView(FileParameter parameter) {
			super(parameter, new HBox());
			set(parameter.get());

			button.setText(parameter.action);
			button.setOnAction((e) -> {
				selectFile();
			});
			root.getChildren().addAll(text, button);

			// dont listen to text, update parameter manually on selectFile()
			text.setEditable(false);
		}

		private void selectFile() {
			final FileChooser chooser = new FileChooser();
			chooser.setTitle(parameter.action);
			PersistentParameter.applyViewHooks(chooser, parameter.viewHooks);
			final Stage stage = (Stage) root.getScene().getWindow();

			switch (parameter.mode) {
				case OPEN:
					setFile(chooser.showOpenDialog(stage));
					break;

				case SAVE:
					setFile(chooser.showSaveDialog(stage));
					break;
			}

			parameter.setLocal(get());
		}

		@Override
		public final FileReference get() {
			return this.file;
		}

		private void updateText() {
			if (this.file == null) {
				text.setText("");
			} else {
				text.setText(this.file.path);
			}
		}

		private void setFile(File file) {
			if (file == null) {
				this.file = null;
			} else {
				this.file = new FileReference(file);
			}
			updateText();
		}

		@Override
		public final void set(FileReference value) {
			this.file = value;
			updateText();
			// TODO: verify and warn (file exists/doesnt)
		}

	}

}

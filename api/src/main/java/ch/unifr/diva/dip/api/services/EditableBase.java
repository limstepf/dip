
package ch.unifr.diva.dip.api.services;

/**
 * EditableBase already implements some common bits of the {@code Processor}
 * interface, offers some helper methods, and implements the {@code Editable}
 * interface.
 */
public abstract class EditableBase extends ProcessorBase implements Editable {

	public EditableBase(String name) {
		super(name);
	}

}

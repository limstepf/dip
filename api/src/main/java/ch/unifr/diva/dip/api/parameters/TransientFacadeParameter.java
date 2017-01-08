package ch.unifr.diva.dip.api.parameters;

/**
 * A transient facade parameter wraps persistent parameters. Having persistent
 * parameters appear as transient ones may be useful in cases where the value of
 * such a parameter isn't saved (and shouldn't affect equals/hashCode).
 *
 * <p>
 * To give an example: the MatrixEditor offers a textfield and a button to fill
 * all values in the matrix. The value of that textfield doesn't matter at all,
 * and unless we hit the "fill" button, the matrix hasn't changed at all and
 * should be considered the same/equal. If we would not put such parameters
 * behind a transient facade, they'd be considered persistent and thus affect
 * equals/hashCode of the (top) parameter. One visible side-effect would be an
 * active reset-button, despite an unchanged matrix.
 *
 * @param <T> class of a persistent parameter.
 */
public class TransientFacadeParameter<T extends PersistentParameter> extends TransientParameterBase {

	protected final T parameter;

	/**
	 * Creats a new transient facade parameter.
	 *
	 * @param parameter the persistent parameter that should appear as transient
	 * one.
	 */
	public TransientFacadeParameter(T parameter) {
		this.parameter = parameter;
	}

	@Override
	protected View newViewInstance() {
		return this.parameter.view();
	}

}

package ch.unifr.diva.dip.api.parameters;

/**
 * Transient parameter base class. Transient parameters are for display purposes
 * only. Nothing gets saved.
 *
 * @param <T> class of the parameter's view.
 */
public abstract class TransientParameterBase<T extends Parameter.View> implements Parameter {

	protected T view;
	protected boolean isHidden;

	/**
	 * Creates a new transient parameter base.
	 */
	public TransientParameterBase() {

	}

	@Override
	public void setHide(boolean hide) {
		isHidden = hide;
		if (view != null) {
			view.setHide(isHidden);
		}
	}

	protected abstract T newViewInstance();

	@Override
	public Parameter.View view() {
		if (view == null) {
			view = newViewInstance();
			view.setHide(this.isHidden);
		}
		return view;
	}

}

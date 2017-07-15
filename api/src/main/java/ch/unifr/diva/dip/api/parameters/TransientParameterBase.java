package ch.unifr.diva.dip.api.parameters;

/**
 * Transient parameter base class. Transient parameters are for display purposes
 * only. Nothing gets saved.
 *
 * @param <T> class of a parameter's value.
 * @param <V> class of the parameter's view.
 */
public abstract class TransientParameterBase<T, V extends Parameter.View> implements Parameter<T> {

	protected V view;
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

	protected abstract V newViewInstance();

	@Override
	public V view() {
		if (view == null) {
			view = newViewInstance();
			view.setHide(this.isHidden);
		}
		return view;
	}

}

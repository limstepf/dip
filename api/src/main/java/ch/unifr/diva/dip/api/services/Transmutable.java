package ch.unifr.diva.dip.api.services;

import javafx.beans.property.BooleanProperty;

/**
 * Transmutable processor interface. Processor extension for dynamic shape (e.g.
 * ports, label/title, ...). If a processor is transmutable, its wrapper will
 * listen to the transmuteProperty and reinstantiate the processor (and its
 * view) upon a call to transmute.
 *
 * This is very similar to a processor service becoming unavailable and
 * available again, with the difference that only a single instance is affected.
 *
 * <p>
 * Note that transmutable processors should announce/expose all (or at least the
 * most important) ports as service factory (i.e. using the default constructor
 * creating a new instance). A more appropriate "shape" can be then defined in
 * the {@code init()} hook.
 */
public interface Transmutable {

	public BooleanProperty transmuteProperty();

	default void transmute() {
		transmuteProperty().set(!transmuteProperty().get());
	}
}

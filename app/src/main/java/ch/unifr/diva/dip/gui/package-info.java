/**
 * GUI components.
 * Pattern: Model-View-Presenter (passive view):
 *
 * <pre>
 *      +-------------------------+
 *      |       Presenter         |
 *      |                         +------> Event-Bus (inter-component comm.)
 *      | + Parent getComponent() |
 *      |                         |
 *      +---+------------------+--+
 *          |                  |
 *          |                  |
 *          v                  |
 *      +---+---+              v
 *      |       |      +-------+-----------------+
 *      | Model |      |          View           |
 *      |       |      |                         |
 *      +-------+      | + Parent getComponent() |
 *                     |                         |
 *                     +-------------------------+
 * </pre>
 *
 * <ul>
 * <li>The presenter knows about (i.e. has pointers to) the model and the view.
 * And all that the presenter has to implement is {@code getComponent()} which
 * simply passes the root-node of the view along.</li>
 *
 * <li>The view is dumb and get's managed by the presenter. The view can be a
 * simple Java class (programmatic), or you can use fxml files put together with
 * the SceneBuilder, in which case you can get a presenter with the JavaFX loader.
 * For simple components (e.g. a small widget or some menu) a separate view can
 * be easily omitted.</li>
 *
 * <li>The model preferrably uses JavaFX properties which can be easily bound to
 * the view by the presenter.</li>
 *
 * <li>While inversion-of-control (IoC) is a great thing which allows us to easily
 * mock model and/or view for testing, things are kept simple by avoiding any
 * magic dependency injection framework. Dependencies (i.e. view and model) are
 * simply passed to the constructor.</li>
 *
 * <li>Inter-component communication is implemented with an event-bus where
 * components can subscribe to and fire/broadcast any events.</li>
 * </ul>
 *
 */
package ch.unifr.diva.dip.gui;

package ch.unifr.diva.dip.api.components;

import java.util.HashMap;
import java.util.Map;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

/**
 * Interface of processor tools.
 */
public interface ProcessorTool {

	/**
	 * Returns the name of the tool.
	 *
	 * @return the name of the tool.
	 */
	public String name();

	/**
	 * {@code KeyEvent} handlers. These {@code KeyEvent} handlers get registered
	 * upon selecting this tool, and get unregistered once deselected.
	 *
	 * @return a list of {@code KeyEvent} handlers.
	 */
	default Map<EventType<KeyEvent>, EventHandler<KeyEvent>> keyEvents() {
		return new HashMap<>();
	}

	/**
	 * {@code MouseEvent} handlers. These {@code MouseEvent} handlers get
	 * registered upon selecting this tool, and get unregistered once
	 * deselected.
	 *
	 * @return a list of {@code MouseEvent} handlers.
	 */
	default Map<EventType<MouseEvent>, EventHandler<MouseEvent>> mouseEvents() {
		return new HashMap<>();
	}

	/**
	 * Abstract ProcessorTool.
	 */
	public static abstract class AbstractProcessorTool implements ProcessorTool {

		protected final String name;
		protected final Map<EventType<MouseEvent>, EventHandler<MouseEvent>> mouseEvents = new HashMap<>();
		protected final Map<EventType<KeyEvent>, EventHandler<KeyEvent>> keyEvents = new HashMap<>();

		public AbstractProcessorTool(String name) {
			this.name = name;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public Map<EventType<MouseEvent>, EventHandler<MouseEvent>> mouseEvents() {
			return mouseEvents;
		}

		@Override
		public Map<EventType<KeyEvent>, EventHandler<KeyEvent>> keyEvents() {
			return keyEvents;
		}
	}

	public static ProcessorTool newClickTool(String name, EventHandler<MouseEvent> onMouseClicked) {
		return new AbstractProcessorTool(name) {
			{
				this.mouseEvents.put(
						MouseEvent.MOUSE_CLICKED,
						onMouseClicked
				);
			}
		};
	}

	public static ProcessorTool newLineTool(String name, EventHandler<MouseEvent> onMousePressed, EventHandler<MouseEvent> onMouseMoved, EventHandler<MouseEvent> onMouseReleased) {
		return new AbstractProcessorTool(name) {
			{
				this.mouseEvents.put(
						MouseEvent.MOUSE_PRESSED,
						onMousePressed
				);
				this.mouseEvents.put(
						MouseEvent.MOUSE_MOVED,
						onMouseMoved
				);
				this.mouseEvents.put(
						MouseEvent.MOUSE_RELEASED,
						onMouseReleased
				);
			}
		};
	}

}

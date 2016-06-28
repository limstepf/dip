package ch.unifr.diva.dip.playground;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineJoin;
import javafx.stage.Stage;
import org.junit.Test;

/**
 * Demonstration of simple Java FX application tests. Need to quickly screw
 * around with some JavaFX things? This shows how to. However: do not commit
 * this file if you're screwing around in here; just create a new file in here
 * (i.e. the playground) which will be ignored by git by default.
 *
 * <p>
 * These tests are supposed to be run individually/manually. <br />
 * Usage:
 *
 * <ul>
 * <li>In NetBeans just hit "run focused test method" in the context menu (right
 * mouse click).</li>
 * <li>In IntelliJ select the test-method name (e.g. in the "Run" widget) and
 * hit run in the context menu (right mouse click).</li>
 * </ul>
 */
public class JavaFxApplicationTests {

	/**
	 * Base class for a simple Java FX application.
	 *
	 * @param <T> type of the root node in the scene.
	 */
	public static abstract class SimpleApplication<T extends Parent> extends Application {

		protected Scene scene;
		protected Stage primaryStage;
		protected final T root;

		public SimpleApplication(T root) {
			this.root = root;
		}

		@Override
		public void start(Stage primaryStage) throws Exception {
			this.scene = new Scene(this.root);
			this.primaryStage = primaryStage;
			this.primaryStage.setScene(this.scene);
			this.primaryStage.show();
		}
	}

	/**
	 * A simple demo application.
	 */
	public static class DemoApplication extends SimpleApplication<BorderPane> {

		public DemoApplication() {
			super(new BorderPane());

			root.setPrefHeight(480);
			root.setPrefWidth(640);

			root.setCenter(createStar());
		}

		private SVGPath createStar() {
			SVGPath star = new SVGPath();
			star.setContent("M100,10 L100,10 40,180 190,60 10,60 160,180 z");
			star.setStrokeLineJoin(StrokeLineJoin.ROUND);
			star.setStroke(Color.CHOCOLATE);
			star.setFill(Color.CHOCOLATE);
			star.setStrokeWidth(4);
			return star;
		}
	}

	/**
	 * Launch the demo application.
	 */
	@Test
	public void launchDemoApplication() {
		Application.launch(DemoApplication.class);
	}

}

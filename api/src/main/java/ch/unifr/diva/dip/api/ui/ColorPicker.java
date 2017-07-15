package ch.unifr.diva.dip.api.ui;

import ch.unifr.diva.dip.api.utils.L10n;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.StringConverter;

/**
 * A color picker. Customized color picker based on
 * {@code com.sun.javafx.scene.control.skin.CustomColorDialog} (which,
 * unfortunately, is a very private API).
 */
public class ColorPicker {

	protected final HBox root;
	protected final Scene scene;
	protected final Stage stage;
	protected final ObjectProperty<Color> currentColorProperty;
	protected final ObjectProperty<Color> newColorProperty;
	protected final ColorRectPane colorRectPane;
	protected final ControlsPane controlsPane;
	protected boolean isOk = false;

	/**
	 * Creates a new color picker.
	 *
	 * @param owner owner of the color picker dialog.
	 * @param enableOpacity {@code true} to enable color opacity, {@code false}
	 * to disable.
	 * @param enableWebColor {@code true} to enable the web color field,
	 * {@code false} to disable.
	 */
	public ColorPicker(Window owner, boolean enableOpacity, boolean enableWebColor) {
		this.currentColorProperty = new SimpleObjectProperty<>(Color.TRANSPARENT);
		this.newColorProperty = new SimpleObjectProperty<>(Color.WHITE);
		this.colorRectPane = new ColorRectPane();
		this.controlsPane = new ControlsPane(owner, enableOpacity, enableWebColor);

		HBox.setHgrow(controlsPane, Priority.ALWAYS);
		this.root = new HBox();
		root.getStyleClass().add("custom-color-dialog");
		root.getChildren().setAll(
				colorRectPane,
				controlsPane
		);

		this.scene = new Scene(root);
		this.stage = new Stage();
		stage.initStyle(StageStyle.UTILITY);
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initOwner(owner);
		stage.setTitle("Color Picker");
		stage.setScene(scene);
	}

	/**
	 * Checks whether the dialog has been closed with an "ok".
	 *
	 * @return {@code true} if the dialog has been closed by clicking the "ok"
	 * button, {@code false} otherwise.
	 */
	public boolean isOk() {
		return this.isOk;
	}

	/**
	 * The current color property.
	 *
	 * @return the current color property.
	 */
	public ObjectProperty<Color> currentColorProperty() {
		return this.currentColorProperty;
	}

	/**
	 * Returns the current color.
	 *
	 * @return the current color.
	 */
	public Color getCurrentColor() {
		return this.currentColorProperty.get();
	}

	/**
	 * Sets the current color. Used to initialize the color picker.
	 *
	 * @param color the current color.
	 */
	public void setCurrentColor(Color color) {
		this.currentColorProperty.set(color);
	}

	/**
	 * The new color property.
	 *
	 * @return the new color property.
	 */
	public ObjectProperty<Color> newColorProperty() {
		return this.newColorProperty;
	}

	/**
	 * Returns the newly picked color.
	 *
	 * @return the new color.
	 */
	public Color getNewColor() {
		return this.newColorProperty.get();
	}

	/**
	 * Returns the stage of the color picker dialog.
	 *
	 * @return the stage of the color picker dialog.
	 */
	public Stage getStage() {
		return this.stage;
	}

	/**
	 * Shows the color picker dialog.
	 */
	public void show() {
		this.colorRectPane.init();
		this.stage.show();
	}

	/**
	 * Shows the color picker dialog and waits for it to be hidden/closed.
	 */
	public void showAndWait() {
		this.colorRectPane.init();
		this.stage.showAndWait();
	}

	/**
	 * Closes the color picker dialog.
	 */
	public void close() {
		stage.setScene(null);
		stage.close();
	}

	protected Button newOkButton() {
		final Button button = new Button(L10n.getInstance().getString("ok"));
		button.setMaxWidth(Double.MAX_VALUE);
		button.setDefaultButton(true);
		button.setOnAction((e) -> {
			this.isOk = true;
			close();
		});
		return button;
	}

	protected Button newCancelButton(Window window) {
		final Button button = new Button(L10n.getInstance().getString("cancel"));
		button.setMaxWidth(Double.MAX_VALUE);
		button.setCancelButton(true);
		button.setOnAction((e) -> close());
		return button;
	}

	/**
	 * Color rect/picker pane.
	 */
	protected class ColorRectPane extends HBox {

		protected final Pane colorRect;
		protected final Pane colorBar;
		protected final Pane colorRectOverlayOne;
		protected final Pane colorRectOverlayTwo;
		protected final Region colorRectIndicator;
		protected final Region colorBarIndicator;
		protected boolean changeIsLocal = false;
		protected final DoubleProperty hue = new SimpleDoubleProperty(-1) {
			@Override
			protected void invalidated() {
				if (!changeIsLocal) {
					changeIsLocal = true;
					updateHsbColor();
					changeIsLocal = false;
				}
			}
		};
		protected DoubleProperty sat = new SimpleDoubleProperty(-1) {
			@Override
			protected void invalidated() {
				if (!changeIsLocal) {
					changeIsLocal = true;
					updateHsbColor();
					changeIsLocal = false;
				}
			}
		};
		protected DoubleProperty bright = new SimpleDoubleProperty(-1) {
			@Override
			protected void invalidated() {
				if (!changeIsLocal) {
					changeIsLocal = true;
					updateHsbColor();
					changeIsLocal = false;
				}
			}
		};
		protected DoubleProperty red = new SimpleDoubleProperty(-1) {
			@Override
			protected void invalidated() {
				if (!changeIsLocal) {
					changeIsLocal = true;
					updateRgbColor();
					changeIsLocal = false;
				}
			}
		};
		protected DoubleProperty green = new SimpleDoubleProperty(-1) {
			@Override
			protected void invalidated() {
				if (!changeIsLocal) {
					changeIsLocal = true;
					updateRgbColor();
					changeIsLocal = false;
				}
			}
		};
		protected DoubleProperty blue = new SimpleDoubleProperty(-1) {
			@Override
			protected void invalidated() {
				if (!changeIsLocal) {
					changeIsLocal = true;
					updateRgbColor();
					changeIsLocal = false;
				}
			}
		};
		protected DoubleProperty alpha = new SimpleDoubleProperty(100) {
			@Override
			protected void invalidated() {
				if (!changeIsLocal) {
					changeIsLocal = true;
					setNewColor(new Color(
							getNewColor().getRed(),
							getNewColor().getGreen(),
							getNewColor().getBlue(),
							clamp(alpha.get() / 100)
					));
					changeIsLocal = false;
				}
			}
		};

		/**
		 * Creates a new color rect/picker pane.
		 */
		public ColorRectPane() {
			getStyleClass().add("color-rect-pane");
			newColorProperty.addListener((e) -> colorChanged());

			this.colorRectIndicator = new Region();
			colorRectIndicator.setId("color-rect-indicator");
			colorRectIndicator.setManaged(false);
			colorRectIndicator.setMouseTransparent(true);
			colorRectIndicator.setCache(true);

			final Pane colorRectOpacityContainer = new StackPane();
			this.colorRect = new StackPane() {
				// This is an implementation of square control that chooses its
				// size to fill the available height
				@Override
				public Orientation getContentBias() {
					return Orientation.VERTICAL;
				}

				@Override
				protected double computePrefWidth(double height) {
					return height;
				}

				@Override
				protected double computeMaxWidth(double height) {
					return height;
				}
			};
			colorRect.getStyleClass().addAll("color-rect", "transparent-pattern");

			final Pane colorRectHue = new Pane();
			colorRectHue.backgroundProperty().bind(new ObjectBinding<Background>() {
				{
					bind(hue);
				}

				@Override
				protected Background computeValue() {
					return new Background(new BackgroundFill(
							Color.hsb(hue.getValue(), 1.0, 1.0),
							CornerRadii.EMPTY,
							Insets.EMPTY
					));
				}
			});

			this.colorRectOverlayOne = new Pane();
			colorRectOverlayOne.getStyleClass().add("color-rect");
			colorRectOverlayOne.setBackground(new Background(new BackgroundFill(
					new LinearGradient(
							0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
							new Stop(0, Color.rgb(255, 255, 255, 1)),
							new Stop(1, Color.rgb(255, 255, 255, 0))
					),
					CornerRadii.EMPTY,
					Insets.EMPTY
			)));
			final EventHandler<MouseEvent> rectMouseHandler = (MouseEvent event) -> {
				final double x = event.getX();
				final double y = event.getY();
				sat.set(clamp(x / colorRect.getWidth()) * 100);
				bright.set(100 - (clamp(y / colorRect.getHeight()) * 100));
			};

			this.colorRectOverlayTwo = new Pane();
			colorRectOverlayTwo.getStyleClass().addAll("color-rect");
			colorRectOverlayTwo.setBackground(new Background(new BackgroundFill(
					new LinearGradient(
							0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
							new Stop(0, Color.rgb(0, 0, 0, 0)),
							new Stop(1, Color.rgb(0, 0, 0, 1))
					),
					CornerRadii.EMPTY,
					Insets.EMPTY
			)));
			colorRectOverlayTwo.setOnMouseDragged(rectMouseHandler);
			colorRectOverlayTwo.setOnMousePressed(rectMouseHandler);

			final Pane colorRectBlackBorder = new Pane();
			colorRectBlackBorder.setMouseTransparent(true);

			this.colorBar = new Pane();
			colorBar.getStyleClass().add("color-bar");
			colorBar.setBackground(new Background(new BackgroundFill(
					createHueGradient(),
					CornerRadii.EMPTY,
					Insets.EMPTY
			)));

			this.colorBarIndicator = new Region();
			colorBarIndicator.setId("color-bar-indicator");
			colorBarIndicator.setMouseTransparent(true);
			colorBarIndicator.setCache(true);

			colorRectIndicator.layoutXProperty().bind(
					sat.divide(100).multiply(colorRect.widthProperty())
			);
			colorRectIndicator.layoutYProperty().bind(
					Bindings.subtract(1, bright.divide(100)).multiply(colorRect.heightProperty())
			);
			colorBarIndicator.layoutYProperty().bind(
					hue.divide(360).multiply(colorBar.heightProperty())
			);
			colorRectOpacityContainer.opacityProperty().bind(alpha.divide(100));

			final EventHandler<MouseEvent> barMouseHandler = (MouseEvent event) -> {
				final double y = event.getY();
				hue.set(clamp(y / colorRect.getHeight()) * 360);
			};

			colorBar.setOnMouseDragged(barMouseHandler);
			colorBar.setOnMousePressed(barMouseHandler);

			colorBar.getChildren().setAll(
					colorBarIndicator
			);
			colorRectOpacityContainer.getChildren().setAll(
					colorRectHue,
					colorRectOverlayOne,
					colorRectOverlayTwo
			);
			colorRect.getChildren().setAll(
					colorRectOpacityContainer,
					colorRectBlackBorder,
					colorRectIndicator
			);
			HBox.setHgrow(colorRect, Priority.SOMETIMES);
			getChildren().addAll(
					colorRect,
					colorBar
			);
		}

		protected void updateRgbColor() {
			final Color color = Color.rgb(
					red.intValue(),
					green.intValue(),
					blue.intValue(),
					clamp(alpha.get() / 100)
			);

			hue.set(color.getHue());
			sat.set(color.getSaturation() * 100);
			bright.set(color.getBrightness() * 100);
			setNewColor(color);
		}

		protected void updateHsbColor() {
			final Color color = Color.hsb(
					hue.get(),
					clamp(sat.get() / 100),
					clamp(bright.get() / 100),
					clamp(alpha.get() / 100)
			);
			red.set(doubleToInt(color.getRed()));
			green.set(doubleToInt(color.getGreen()));
			blue.set(doubleToInt(color.getBlue()));
			setNewColor(color);
		}

		protected final void colorChanged() {
			if (!changeIsLocal) {
				changeIsLocal = true;
				final Color color = getNewColor();
				hue.set(color.getHue());
				sat.set(color.getSaturation() * 100);
				bright.set(color.getBrightness() * 100);
				red.set(doubleToInt(color.getRed()));
				green.set(doubleToInt(color.getGreen()));
				blue.set(doubleToInt(color.getBlue()));
				changeIsLocal = false;
			}
		}

		protected void setNewColor(Color color) {
			newColorProperty.set(color);
		}

		public void init() {
			final Color color = getCurrentColor();
			changeIsLocal = true;
			hue.set(color.getHue());
			sat.set(color.getSaturation() * 100);
			bright.set(color.getBrightness() * 100);
			alpha.set(color.getOpacity() * 100);
			final Color newc = Color.hsb(
					hue.get(),
					clamp(sat.get() / 100),
					clamp(bright.get() / 100),
					clamp(alpha.get() / 100)
			);
			setNewColor(newc);
			red.set(doubleToInt(newc.getRed()));
			green.set(doubleToInt(newc.getGreen()));
			blue.set(doubleToInt(newc.getBlue()));
			changeIsLocal = false;
		}

		@Override
		protected void layoutChildren() {
			super.layoutChildren();
			// to maintain default size
			colorRectIndicator.autosize();
			// to maintain square size
			double size = Math.min(colorRect.getWidth(), colorRect.getHeight());
			colorRect.resize(size, size);
			colorBar.resize(colorBar.getWidth(), size);
		}

	}

	/**
	 * Controls pane.
	 */
	protected class ControlsPane extends VBox {

		protected final Label currentColorLabel;
		protected final Label newColorLabel;
		protected final Region currentColorRect;
		protected final Region newColorRect;
		protected final Region alphaColorRect;
		protected final Label rgbLabels[] = new Label[3];
		protected final Label hsbLabels[] = new Label[3];
		protected final Label alphaLabel;
		protected final Label webLabel;
		protected final Label hsbUnitLabels[] = new Label[3];
		protected final Label alphaUnitLabel;
		protected final Slider rgbSliders[] = new Slider[3];
		protected final Slider hsbSliders[] = new Slider[3];
		protected final Slider alphaSlider;
		protected final TextField rgbTextFields[] = new TextField[3];
		protected final TextField hsbTextFields[] = new TextField[3];
		protected final TextField alphaTextField;
		protected final TextField webTextField;
		protected final BorderPane upperPane;
		protected final GridPane colorGrid;
		protected final GridPane mainGrid;
		protected final Button okButton;
		protected final Button cancelButton;

		/**
		 * Creates a new controls pane.
		 *
		 * @param owner owner of the color picker dialog.
		 * @param enableOpacity {@code true} to enable color opacity,
		 * {@code false} to disable.
		 * @param enableWebColor {@code true} to enable the web color field,
		 * {@code false} to disable.
		 */
		public ControlsPane(Window owner, boolean enableOpacity, boolean enableWebColor) {
			this.currentColorLabel = newLabel(L10n.getInstance().getString("current").toLowerCase());
			this.newColorLabel = newLabel(L10n.getInstance().getString("new").toLowerCase());
			this.currentColorRect = newRegion(96, 128, 48, 72);
			currentColorRect.setId("current-color");
			currentColorRect.backgroundProperty().bind(new ObjectBinding<Background>() {
				{
					bind(currentColorProperty);
				}

				@Override
				protected Background computeValue() {
					return new Background(new BackgroundFill(
							currentColorProperty.get(),
							CornerRadii.EMPTY,
							Insets.EMPTY
					));
				}
			});

			this.newColorRect = newRegion(96, 128, 48, 72);
			newColorRect.setId("new-color");
			newColorRect.backgroundProperty().bind(new ObjectBinding<Background>() {
				{
					bind(newColorProperty);
				}

				@Override
				protected Background computeValue() {
					return new Background(new BackgroundFill(
							newColorProperty.get(),
							CornerRadii.EMPTY,
							Insets.EMPTY
					));
				}
			});

			this.alphaColorRect = new Region();
			alphaColorRect.getStyleClass().addAll("transparent-pattern");

			this.colorGrid = new GridPane();
			colorGrid.setPadding(new Insets(0, 10, 10, 0));
			colorGrid.setHgap(5);
			colorGrid.getColumnConstraints().addAll(
					new ColumnConstraints(),
					new ColumnConstraints()
			);
			colorGrid.getColumnConstraints().get(0).setHgrow(Priority.SOMETIMES);
			colorGrid.getColumnConstraints().get(1).setHgrow(Priority.ALWAYS);
			colorGrid.add(alphaColorRect, 0, 0, 1, 2);
			colorGrid.add(currentColorRect, 0, 0);
			colorGrid.add(currentColorLabel, 1, 0);
			colorGrid.add(newColorRect, 0, 1);
			colorGrid.add(newColorLabel, 1, 1);

			this.okButton = newOkButton();
			this.cancelButton = newCancelButton(owner);

			final VBox buttons = new VBox();
			buttons.setSpacing(5);
			buttons.getChildren().setAll(okButton, cancelButton);

			this.upperPane = new BorderPane();
			upperPane.setCenter(colorGrid);
			upperPane.setRight(buttons);

			for (int i = 0; i < 3; i++) {
				this.rgbLabels[i] = newLabel(L10n.getInstance().getString(
						(i == 0) ? "color.red" : (i == 1) ? "color.green" : "color.blue"
				) + ":");
				this.hsbLabels[i] = newLabel(L10n.getInstance().getString(
						(i == 0) ? "hue" : (i == 1) ? "saturation" : "brightness"
				) + ":");
				this.hsbUnitLabels[i] = newLabel(
						(i == 0) ? "°" : "%"
				);

				this.rgbSliders[i] = newSlider(0, 255);
				this.rgbSliders[i].valueProperty().bindBidirectional(
						(i == 0) ? colorRectPane.red
								: (i == 1) ? colorRectPane.green : colorRectPane.blue
				);
				this.rgbTextFields[i] = newTextField();
				rgbTextFields[i].setStyle("-fx-pref-column-count: 3;");
				Bindings.bindBidirectional(
						rgbTextFields[i].textProperty(),
						(i == 0) ? colorRectPane.red
								: (i == 1) ? colorRectPane.green : colorRectPane.blue,
						newIntegerConverter(0, 255)
				);

				this.hsbSliders[i] = newSlider(0, (i == 0) ? 360 : 100);
				this.hsbSliders[i].valueProperty().bindBidirectional(
						(i == 0) ? colorRectPane.hue
								: (i == 1) ? colorRectPane.sat : colorRectPane.bright
				);
				this.hsbTextFields[i] = newTextField();
				hsbTextFields[i].setStyle("-fx-pref-column-count: 3;");
				Bindings.bindBidirectional(
						hsbTextFields[i].textProperty(),
						(i == 0) ? colorRectPane.hue
								: (i == 1) ? colorRectPane.sat : colorRectPane.bright,
						(i == 0) ? newIntegerConverter(0, 360) : newIntegerConverter(0, 100)
				);
			}

			this.mainGrid = newValueGrid();
			int row = 0;
			for (int i = 0; i < 3; i++) {
				mainGrid.add(this.hsbLabels[i], 0, row);
				mainGrid.add(this.hsbSliders[i], 1, row);
				mainGrid.add(this.hsbTextFields[i], 2, row);
				mainGrid.add(this.hsbUnitLabels[i], 3, row);
				mainGrid.getRowConstraints().add(new RowConstraints());
				row++;
			}

			RowConstraints rc = new RowConstraints();
			rc.setMinHeight(5);
			mainGrid.getRowConstraints().add(rc);
			row++;

			for (int i = 0; i < 3; i++) {
				mainGrid.add(this.rgbLabels[i], 0, row);
				mainGrid.add(this.rgbSliders[i], 1, row);
				mainGrid.add(this.rgbTextFields[i], 2, row);
				mainGrid.getRowConstraints().add(new RowConstraints());
				row++;
			}

			if (enableOpacity) {
				rc = new RowConstraints();
				rc.setMinHeight(5);
				mainGrid.getRowConstraints().add(rc);
				row++;

				this.alphaLabel = newLabel(L10n.getInstance().getString("opacity") + ":");
				this.alphaSlider = newSlider(0, 100);
				this.alphaTextField = newTextField();
				this.alphaUnitLabel = newLabel("%");
				alphaSlider.valueProperty().bindBidirectional(colorRectPane.alpha);
				Bindings.bindBidirectional(
						alphaTextField.textProperty(),
						colorRectPane.alpha,
						newIntegerConverter(0, 100)
				);
				mainGrid.add(this.alphaLabel, 0, row);
				mainGrid.add(this.alphaSlider, 1, row);
				mainGrid.add(this.alphaTextField, 2, row);
				mainGrid.add(this.alphaUnitLabel, 3, row);
				mainGrid.getRowConstraints().add(new RowConstraints());
				row++;
			} else {
				this.alphaLabel = null;
				this.alphaSlider = null;
				this.alphaTextField = null;
				this.alphaUnitLabel = null;
			}

			if (enableWebColor) {
				rc = new RowConstraints();
				rc.setMinHeight(5);
				mainGrid.getRowConstraints().add(rc);
				row++;

				this.webLabel = newLabel("#");
				webLabel.setAlignment(Pos.BASELINE_RIGHT);
				webLabel.setMaxWidth(Double.MAX_VALUE);
				this.webTextField = newTextField();
				webTextField.setMaxWidth(Double.MAX_VALUE);
				Bindings.bindBidirectional(
						webTextField.textProperty(),
						ColorPicker.this.newColorProperty,
						newHexConverter()
				);
				mainGrid.add(this.webLabel, 0, row);
				mainGrid.add(this.webTextField, 1, row);
				mainGrid.getRowConstraints().add(new RowConstraints());
				row++;
			} else {
				this.webLabel = null;
				this.webTextField = null;
			}

			this.getChildren().setAll(
					upperPane,
					mainGrid
			);

		}

	}

	/**
	 * Returns a new color picker region with enabled opacity and web color
	 * field.
	 *
	 * @param <T> a class extending Region and implementing the
	 * ColorPickerControl interface.
	 * @param owner owner of the color picker dialog.
	 * @return a color picker region.
	 */
	public static <T extends Region & ColorPickerControl> T newColorPickerRegion(Window owner) {
		return newColorPickerRegion(owner, true, true);
	}

	/**
	 * Returns a new color picker region.
	 *
	 * @param <T> a class extending Region and implementing the
	 * ColorPickerControl interface.
	 * @param owner owner of the color picker dialog.
	 * @param enableOpacity {@code true} to enable color opacity, {@code false}
	 * to disable.
	 * @param enableWebColor {@code true} to enable the web color field,
	 * {@code false} to disable.
	 * @return a color picker region.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Region & ColorPickerControl> T newColorPickerRegion(Window owner, boolean enableOpacity, boolean enableWebColor) {
		final ColorPickerControlRegion control = new ColorPickerControlRegion(owner);
		control.enableOpacity(enableOpacity);
		control.enableWebColor(enableWebColor);
		return (T) control;
	}

	/**
	 * Returns a new, framed color picker region with enabled opacity and web
	 * color field.
	 *
	 * @param <T> a class extending Region and implementing the
	 * ColorPickerControl interface.
	 * @param owner owner of the color picker dialog.
	 * @return a color picker region.
	 */
	public static <T extends Region & ColorPickerControl> T newFramedColorPickerRegion(Window owner) {
		return newFramedColorPickerRegion(owner, true, true);
	}

	/**
	 * Returns a new, framed color picker region.
	 *
	 * @param <T> a class extending Region and implementing the
	 * ColorPickerControl interface.
	 * @param owner owner of the color picker dialog.
	 * @param enableOpacity {@code true} to enable color opacity, {@code false}
	 * to disable.
	 * @param enableWebColor {@code true} to enable the web color field,
	 * {@code false} to disable.
	 * @return a color picker region.
	 */
	public static <T extends Region & ColorPickerControl> T newFramedColorPickerRegion(Window owner, boolean enableOpacity, boolean enableWebColor) {
		final T control = newColorPickerRegion(owner, enableOpacity, enableWebColor);
		control.setBorder(new Border(
				new BorderStroke(
						Color.WHITE,
						BorderStrokeStyle.SOLID,
						CornerRadii.EMPTY,
						new BorderWidths(2)
				),
				new BorderStroke(
						Color.BLACK,
						BorderStrokeStyle.SOLID,
						CornerRadii.EMPTY,
						new BorderWidths(1)
				)
		));
		return control;
	}

	/**
	 * The color picker control interface.
	 */
	public interface ColorPickerControl {

		/**
		 * Ënables/disables the opacity. If disabled, opacity is set to 100% and
		 * can't be changed.
		 *
		 * @param opacity {@code true} to enable opacity of colors,
		 * {@code false} to disable.
		 */
		public void enableOpacity(boolean opacity);

		/**
		 * Enables/disables the web color field.
		 *
		 * @param webColor {@code true} to enable the web color field,
		 * {@code false} to disable.
		 */
		public void enableWebColor(boolean webColor);

		/**
		 * Returns the color property. This property is set upon closing the
		 * color picker dialog (by hitting the "ok" button).
		 *
		 * @return the color property.
		 */
		public ObjectProperty<Color> colorProperty();

		/**
		 * Returns the color picker control as {@code Region}.
		 *
		 * @return the color picker controls cast to a {@code Region}.
		 */
		default Region asRegion() {
			return (Region) this;
		}

	}

	/**
	 * A color picker control region.
	 */
	public static class ColorPickerControlRegion extends Region implements ColorPickerControl {

		protected final ObjectProperty<Color> colorProperty;
		protected boolean enableOpacity = true;
		protected boolean enableWebColor = true;

		/**
		 * Creates a new color picker control region without owner.
		 */
		public ColorPickerControlRegion() {
			this(null);
		}

		/**
		 * Creates a new color picker control region.
		 *
		 * @param owner owner of the color picker dialog.
		 */
		public ColorPickerControlRegion(Window owner) {
			this.colorProperty = new SimpleObjectProperty<>(Color.WHITE);
			this.backgroundProperty().bind(Bindings.createObjectBinding(
					() -> {
						final BackgroundFill fill = new BackgroundFill(
								colorProperty.get(),
								CornerRadii.EMPTY,
								Insets.EMPTY
						);
						return new Background(fill);
					},
					colorProperty
			));
			this.addEventHandler(MouseEvent.MOUSE_PRESSED, (e) -> {
				final ColorPicker picker = new ColorPicker(owner, enableOpacity, enableWebColor);
				final Color color = ColorPicker.getBackgroundColor(getBackground());
				if (color != null) {
					picker.setCurrentColor(color);
				}
				picker.showAndWait();
				if (picker.isOk()) {
					colorProperty.set(picker.getNewColor());
				}
			});
		}

		@Override
		public void enableOpacity(boolean opacity) {
			this.enableOpacity = opacity;
		}

		@Override
		public void enableWebColor(boolean webColor) {
			this.enableWebColor = webColor;
		}

		@Override
		public ObjectProperty<Color> colorProperty() {
			return this.colorProperty;
		}

		// This is an implementation of square control that chooses its
		// size to fill the available height
		@Override
		public Orientation getContentBias() {
			return Orientation.VERTICAL;
		}

		@Override
		protected double computePrefWidth(double height) {
			return height;
		}

		// ...but don't force it to be square. If we want to, the region shall
		// be able to adapt to the available width (e.g. with setHgrow()).
		//@Override
		//protected double computeMaxWidth(double height) {
		//	return height;
		//}
	}

	protected static String getWebColor(Color color) {
		final int red = (int) (color.getRed() * 255);
		final int green = (int) (color.getGreen() * 255);
		final int blue = (int) (color.getBlue() * 255);
		return String.format("%02X%02X%02X", red, green, blue).toUpperCase();
	}

	protected static StringConverter<Color> newHexConverter() {
		return new StringConverter<Color>() {

			@Override
			public String toString(Color object) {
				if (object == null) {
					return "";
				}
				return getWebColor(object);
			}

			@Override
			public Color fromString(String string) {
				if (string == null) {
					return Color.BLACK;
				}
				string = string.trim().toUpperCase();
				if (string.matches("#[A-F0-9]{6}") || string.matches("[A-F0-9]{6}")) {
					try {
						return (string.charAt(0) == '#') ? Color.web(string) : Color.web("#" + string);
					} catch (IllegalArgumentException ex) {
						// pass
					}
				}
				return Color.BLACK;
			}

		};
	}

	protected static StringConverter<Number> newIntegerConverter(int min, int max) {
		return new StringConverter<Number>() {
			@Override
			public String toString(Number object) {
				if (object == null) {
					return "";
				}
				return String.format("%d", object.intValue());
			}

			@Override
			public Number fromString(String string) {
				try {
					if (string == null) {
						return null;
					}

					string = string.trim();
					if (string.length() < 1) {
						return null;
					}

					final int v = Integer.parseInt(string);
					if (v < min) {
						return min;
					}
					if (v > max) {
						return max;
					}
					return v;
				} catch (NumberFormatException ex) {
					return null;
				}
			}
		};
	}

	protected static final Label newLabel(String text) {
		final Label label = new Label(text);
		label.setTextOverrun(OverrunStyle.CLIP);
		return label;
	}

	protected static final TextField newTextField() {
		final TextField text = new TextField();
		text.setAlignment(Pos.BASELINE_RIGHT);
		text.setMinWidth(32);
		text.setMaxWidth(36);
		return text;
	}

	protected static final Slider newSlider(double min, double max) {
		final Slider slider = new Slider(min, max, min);
		slider.setMajorTickUnit(
				(max == 255) ? 128 : (max == 360) ? 180 : 50
		);
		slider.setMinorTickCount(2);
		slider.setShowTickLabels(false);
		slider.setShowTickMarks(true);
		slider.setSnapToTicks(false);
		slider.setBlockIncrement(1);
		return slider;
	}

	protected static final GridPane newValueGrid() {
		final GridPane grid = new GridPane();
		grid.setPadding(new Insets(10, 0, 0, 0));
		grid.setHgap(5);
		grid.setVgap(5);
		grid.getColumnConstraints().addAll(
				new ColumnConstraints(),
				new ColumnConstraints(),
				new ColumnConstraints(),
				new ColumnConstraints()
		);
		grid.getColumnConstraints().get(0).setHgrow(Priority.SOMETIMES);
		grid.getColumnConstraints().get(1).setHgrow(Priority.ALWAYS);
		grid.getColumnConstraints().get(2).setHgrow(Priority.NEVER);
		grid.getColumnConstraints().get(3).setHgrow(Priority.SOMETIMES);
		return grid;
	}

	protected static final Region newRegion(double prefWidth, double maxWidth, double prefHeight, double maxHeight) {
		final Region region = new Region();
		region.setPrefHeight(prefHeight);
		region.setPrefWidth(prefWidth);
		region.setMaxHeight(maxHeight);
		region.setMaxWidth(maxWidth);
		return region;
	}

	protected static Color getBackgroundColor(Region region) {
		return getBackgroundColor(region.getBackground());
	}

	protected static Color getBackgroundColor(Background background) {
		if (!background.getFills().isEmpty()) {
			final Paint p = background.getFills().get(0).getFill();
			if (p instanceof Color) {
				return (Color) p;
			}
		}
		return null;
	}

	protected static double clamp(double value) {
		return value < 0 ? 0 : value > 1 ? 1 : value;
	}

	protected static LinearGradient createHueGradient() {
		double offset;
		Stop[] stops = new Stop[255];
		for (int y = 0; y < 255; y++) {
			offset = 1 - (1.0 / 255) * y;
			int h = (int) ((y / 255.0) * 360);
			stops[y] = new Stop(offset, Color.hsb(h, 1.0, 1.0));
		}
		return new LinearGradient(0f, 1f, 0f, 0f, true, CycleMethod.NO_CYCLE, stops);
	}

	protected static int doubleToInt(double value) {
		return (int) (value * 255 + 0.5);
	}

}

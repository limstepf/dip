package ch.unifr.diva.dip.awt.imaging;

import ch.unifr.diva.dip.api.datastructures.BufferedMatrix;
import ch.unifr.diva.dip.api.datatypes.DataType;
import ch.unifr.diva.dip.awt.imaging.scanners.Location;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Arrays;

/**
 * Simple Color Models supported by DIP.
 */
public enum SimpleColorModel {

	/**
	 * RGB (Red, Green, Blue). The default, additive color model. And just to be
	 * perfectly clear, this is: sRGB, the standard RGB color space using the
	 * ITU-R BT.709 primaries.
	 *
	 * <p>
	 * Component/sample precision: byte<br />
	 * Components:
	 * <dl>
	 * <dt>R<dt>	<dd>Red.<br />
	 * Range: [0, 255]</dd>
	 *
	 * <dt>G<dt>	<dd>Green.<br />
	 * Range: [0, 255]</dd>
	 *
	 * <dt>B<dt>	<dd>Blue.<br />
	 * Range: [0, 255]</dd>
	 * </dl>
	 */
	RGB(
			ch.unifr.diva.dip.api.datatypes.BufferedImageRgb.class,
			new String[]{"R", "G", "B"},
			new String[]{"Red", "Green", "Blue"}
	) {

				@Override
				public int getBandVisualizationImageType() {
					return BufferedImage.TYPE_INT_RGB;
				}

				@Override
				public void doBandVisualization(WritableRaster src, WritableRaster dst, Location pt, int band) {
					final float sample = src.getSampleFloat(pt.col, pt.row, band);
					dst.setPixel(pt.col, pt.row, new float[]{
						(band == 0) ? sample : 0.0f,
						(band == 1) ? sample : 0.0f,
						(band == 2) ? sample : 0.0f
					});
				}

				@Override
				public float[] toCmy(float[] rgb, float[] cmy) {
					cmy[0] = 255.0f - rgb[0];
					cmy[1] = 255.0f - rgb[1];
					cmy[2] = 255.0f - rgb[2];
					return cmy;
				}

				@Override
				public float[] toGray(float[] rgb, float[] gray) {
					final float avg = (rgb[0] + rgb[1] + rgb[2]) / 3.0f;
					for (int i = 0; i < gray.length; i++) {
						gray[i] = avg;
					}
					return gray;
				}

				@Override
				public float[] toHsv(float[] rgb, float[] hsv) {
					final float r = rgb[0] / 255.0f;
					final float g = rgb[1] / 255.0f;
					final float b = rgb[2] / 255.0f;
					final float min = min(r, g, b);
					final float max = max(r, g, b);
					final float delta = max - min;

					hsv[2] = max;

					if (max != 0) {
						hsv[1] = delta / max;
					} else {
						hsv[1] = 0;
					}

					if (hsv[1] == 0) {
						hsv[0] = 0;
					} else {
						final float red = (max - r) / delta;
						final float green = (max - g) / delta;
						final float blue = (max - b) / delta;
						if (r == max) {
							hsv[0] = blue - green;
						} else if (g == max) {
							hsv[0] = 2.0f + red - blue;
						} else {
							hsv[0] = 4.0f + green - red;
						}

						hsv[0] = hsv[0] * 60.0f;
						if (hsv[0] < 0) {
							hsv[0] = hsv[0] + 360.0f;
						}
					}

					return hsv;
				}

				@Override
				public float[] toRgb(float[] from, float[] to) {
					return floatCopy(from, to);
				}

				@Override
				public float[] toRgba(float[] from, float[] to) {
					to = floatCopy(from, to);
					to[3] = 1.0f;
					return to;
				}

				@Override
				public float[] toYuv(float[] rgb, float[] yuv) {
					final float r = rgb[0] / 255.0f;
					final float g = rgb[1] / 255.0f;
					final float b = rgb[2] / 255.0f;

					yuv[0] = (0.299f * r + 0.587f * g + 0.114f * b);
					yuv[1] = (-0.14713f * r - 0.28886f * g + 0.436f * b);
					yuv[2] = (0.615f * r - 0.51499f * g - 0.10001f * b);

					return yuv;
				}

				@Override
				public float[] toYCbCr(float[] rgb, float[] ycbcr) {
					final float r = rgb[0] / 255.0f;
					final float g = rgb[1] / 255.0f;
					final float b = rgb[2] / 255.0f;

					ycbcr[0] = 16 + (65.481f * r + 128.553f * g + 24.966f * b);
					ycbcr[1] = 128 + (-37.797f * r - 74.203f * g + 112.0f * b);
					ycbcr[2] = 128 + (112.0f * r - 93.786f * g - 18.214f * b);

					return ycbcr;
				}

				@Override
				public float[] toLinearRgb(float[] rgb) {
					return toLinearRgb(rgb, new float[3]);
				}

				@Override
				public float[] toLinearRgb(float[] rgb, float[] lin) {
					for (int i = 0; i < 3; i++) {
						lin[i] = toLinearRgb(rgb[i]);
					}
					return lin;
				}

				// TODO: consider static LUT of 256 bytes as implemented in
				// java.awt.image.ColorModel
				private float toLinearRgb(float sample) {
					sample /= 255.0f;

					if (sample <= 0.04045f) {
						return sample / 12.92f;
					} else {
						return (float) Math.pow((sample + 0.055f) / 1.055f, 2.4f);
					}
				}

				@Override
				public float[] toXyz(float[] rgb, float[] xyz) {
					final float r = toLinearRgb(rgb[0]);
					final float g = toLinearRgb(rgb[1]);
					final float b = toLinearRgb(rgb[2]);

					xyz[0] = 0.412453f * r + 0.35758f * g + 0.180423f * b;
					xyz[1] = 0.212671f * r + 0.71516f * g + 0.072169f * b;
					xyz[2] = 0.019334f * r + 0.119193f * g + 0.950227f * b;

					return xyz;
				}

				@Override
				public float[] toLab(float[] rgb, float[] lab) {
					return SimpleColorModel.XYZ.toLab(toXyz(rgb), lab);
				}
			},
	/**
	 * RGBA (Red, Green, Blue, Alpha). Like RGB - and again: we're talking sRGB
	 * here - just with an alpha channel.
	 *
	 * <p>
	 * Component/sample precision: byte<br />
	 * Components:
	 * <dl>
	 * <dt>R<dt>	<dd>Red.<br />
	 * Range: [0, 255]</dd>
	 *
	 * <dt>G<dt>	<dd>Green.<br />
	 * Range: [0, 255]</dd>
	 *
	 * <dt>B<dt>	<dd>Blue.<br />
	 * Range: [0, 255]</dd>
	 *
	 * <dt>A<dt>	<dd>Alpha.<br />
	 * Range: [0, 255]</dd>
	 * </dl>
	 */
	RGBA(
			ch.unifr.diva.dip.api.datatypes.BufferedImageRgba.class,
			new String[]{"R", "G", "B", "A"},
			new String[]{"Red", "Green", "Blue", "Alpha"}
	) {
				@Override
				public float[] toCmy(float[] from, float[] to) {
					return RGB.toCmy(from, to);
				}

				@Override
				public float[] toGray(float[] from, float[] to) {
					return RGB.toGray(from, to);
				}

				@Override
				public float[] toHsv(float[] from, float[] to) {
					return RGB.toHsv(from, to);
				}

				@Override
				public float[] toRgb(float[] rgba, float[] rgb) {
					return floatCopy(rgba, rgb);
				}

				@Override
				public float[] toRgba(float[] from, float[] to) {
					return RGB.toRgba(from, to);
				}

				@Override
				public float[] toYuv(float[] from, float[] to) {
					return RGB.toYuv(from, to);
				}

				@Override
				public float[] toYCbCr(float[] from, float[] to) {
					return RGB.toYCbCr(from, to);
				}
			},
	/**
	 * Grayscale. A continuum of gray values from pure black to pure white.
	 *
	 * <p>
	 * Component/sample precision: byte<br />
	 * Components:
	 * <dl>
	 * <dt>V<dt>	<dd>Value.<br />
	 * Range: [0, 255]</dd>
	 * </dl>
	 */
	GRAY(
			ch.unifr.diva.dip.api.datatypes.BufferedImageGray.class,
			new String[]{"V"},
			new String[]{"Value"}
	) {
				@Override
				public float[] toGray(float[] from, float[] to) {
					return floatCopy(from, to);
				}

				@Override
				public float[] toRgb(float[] gray, float[] rgb) {
					rgb[0] = gray[0];
					rgb[1] = gray[0];
					rgb[2] = gray[0];
					return rgb;
				}
			},
	/**
	 * CMY (Cyan, Magenta, Yellow). A subtractive color model often used in
	 * color printing. The "key" (or K in CMYK) is omitted since we don't need
	 * to save ink. Mixing all three components gives black.
	 *
	 * <p>
	 * Component/sample precision: byte<br />
	 * Components:
	 * <dl>
	 * <dt>C<dt>	<dd>Cyan.<br />
	 * Range: [0, 255]</dd>
	 *
	 * <dt>M<dt>	<dd>Magenta.<br />
	 * Range: [0, 255]</dd>
	 *
	 * <dt>Y<dt>	<dd>Yellow.<br />
	 * Range: [0, 255]</dd>
	 * </dl>
	 */
	CMY(
			ch.unifr.diva.dip.api.datatypes.BufferedImageCmy.class,
			new String[]{"C", "M", "Y"},
			new String[]{"Cyan", "Magenta", "Yellow"}
	) {
				@Override
				public int getBandVisualizationImageType() {
					return BufferedImage.TYPE_INT_RGB;
				}

				@Override
				public void doBandVisualization(WritableRaster src, WritableRaster dst, Location pt, int band) {
					final float sample = src.getSampleFloat(pt.col, pt.row, band);
					final float[] px = toRgb(new float[]{
						(band == 0) ? sample : 0.0f,
						(band == 1) ? sample : 0.0f,
						(band == 2) ? sample : 0.0f
					});
					dst.setPixel(pt.col, pt.row, px);
				}

				@Override
				public float[] toCmy(float[] from, float[] to) {
					return floatCopy(from, to);
				}

				@Override
				public float[] toRgb(float[] cmy, float[] rgb) {
					rgb[0] = 255.0f - cmy[0];
					rgb[1] = 255.0f - cmy[1];
					rgb[2] = 255.0f - cmy[2];
					return rgb;
				}
			},
	/**
	 * HSV (Hue, Saturation, Value). Also known as HSB (Hue, Saturation,
	 * Brightness).
	 *
	 * <p>
	 * Component/sample precision: float<br />
	 * Components:
	 * <dl>
	 * <dt>H<dt>	<dd>Hue.<br />
	 * Range: [0.0°, 360.0°]</dd>
	 *
	 * <dt>S<dt>	<dd>Saturation.<br />
	 * Range: [0.0, 1.0]</dd>
	 *
	 * <dt>V<dt>	<dd>Value.<br />
	 * Range: [0.0, 1.0]</dd>
	 * </dl>
	 */
	HSV(
			ch.unifr.diva.dip.api.datatypes.BufferedImageHsv.class,
			new String[]{"H", "S", "V"},
			new String[]{"Hue", "Saturation", "Value"},
			new float[]{0.0f, 0.0f, 0.0f},
			new float[]{360.0f, 1.0f, 1.0f}
	) {
				@Override
				public int getBandVisualizationImageType() {
					return BufferedImage.TYPE_INT_RGB;
				}

				@Override
				public void doBandVisualization(WritableRaster src, WritableRaster dst, Location pt, int band) {
					final float sample = src.getSampleFloat(pt.col, pt.row, band);
					float[] px = new float[3];

					if (band == 0) {
						px = toRgb(new float[]{sample, 1.0f, 1.0f}, px);
					} else if (band == 1) {
						px = toRgb(new float[]{0.0f, sample, 1.0f}, px);
					} else {
						px = toRgb(new float[]{0.0f, 0.0f, sample}, px);
					}

					dst.setPixel(pt.col, pt.row, px);
				}

				@Override
				public float[] toHsv(float[] from, float[] to) {
					return floatCopy(from, to);
				}

				@Override
				public float[] toRgb(float[] hsv, float[] rgb) {
					if (hsv[1] == 0.0f) {	// achromatic (grey)
						final float v = hsv[2] * 255.0f;
						for (int i = 0; i < rgb.length; i++) {
							rgb[i] = v;
						}
					} else {
						float hue = hsv[0] / 360.0f;
						final float h = (hue - (float) Math.floor(hue)) * 6.0f;
						final float f = h - (float) Math.floor(h);

						final float p = hsv[2] * (1.0f - hsv[1]) * 255.0f;
						final float q = hsv[2] * (1.0f - (hsv[1] * f)) * 255.0f;
						final float t = hsv[2] * (1.0f - (hsv[1] * (1.0f - f))) * 255.0f;
						final float v = hsv[2] * 255.0f;

						switch ((int) h) {
							case 0:
								rgb[0] = v;
								rgb[1] = t;
								rgb[2] = p;
								break;

							case 1:
								rgb[0] = q;
								rgb[1] = v;
								rgb[2] = p;
								break;

							case 2:
								rgb[0] = p;
								rgb[1] = v;
								rgb[2] = t;
								break;

							case 3:
								rgb[0] = p;
								rgb[1] = q;
								rgb[2] = v;
								break;

							case 4:
								rgb[0] = t;
								rgb[1] = p;
								rgb[2] = v;
								break;

							case 5:
							default:
								rgb[0] = v;
								rgb[1] = p;
								rgb[2] = q;
								break;
						}
					}

					return rgb;
				}
			},
	/**
	 * YUV. Luma (Y') and two chrominance/color (UV) components according to
	 * ITU-R BT.601. Comes from analogue sources (hence the max. signal level of
	 * 1.0 V).
	 *
	 * <p>
	 * Component/sample precision: float<br />
	 * Components:
	 * <dl>
	 * <dt>Y'<dt>	<dd>Luma.<br />
	 * Range: [0.0, 1.0]</dd>
	 *
	 * <dt>U<dt>	<dd>Blue-difference (B - Y').<br />
	 * Range: [-Umax, Umax] with Umax = 0.436</dd>
	 *
	 * <dt>V<dt>	<dd>Red-difference (R - Y').<br />
	 * Range: [-Vmax, Vmax] with Vmax = 0.615</dd>
	 * </dl>
	 */
	YUV(
			ch.unifr.diva.dip.api.datatypes.BufferedImageYuv.class,
			new String[]{"Y", "U", "V"},
			new String[]{"Luma", "Blue-difference", "Red-difference"},
			new float[]{0.0f, -0.436f, -0.615f},
			new float[]{1.0f, 0.436f, 0.615f}
	) {
				@Override
				public int getBandVisualizationImageType() {
					return BufferedImage.TYPE_INT_RGB;
				}

				@Override
				public void doBandVisualization(WritableRaster src, WritableRaster dst, Location pt, int band) {
					final float sample = src.getSampleFloat(pt.col, pt.row, band);
					final float[] px = toRgb(new float[]{
						(band == 0) ? sample : 0.5f,
						(band == 1) ? sample : 0.0f,
						(band == 2) ? sample : 0.0f
					});
					dst.setPixel(pt.col, pt.row, px);
				}

				@Override
				public float[] toRgb(float[] yuv, float[] rgb) {
					rgb[0] = (yuv[0] + 1.13983f * yuv[2]) * 255.0f;
					rgb[1] = (yuv[0] - 0.39465f * yuv[1] - 0.5806f * yuv[2]) * 255.0f;
					rgb[2] = (yuv[0] + 2.03211f * yuv[1]) * 255.0f;

					return rgb;
				}

				@Override
				public float[] toYuv(float[] from, float[] to) {
					return floatCopy(from, to);
				}
			},
	/**
	 * YCbCr. Luma (Y'), blue-difference (Cb) and red-difference (Cr) components
	 * according to ITU-R BT.601.
	 *
	 * <p>
	 * Component/sample precision: byte<br />
	 * Components:
	 * <dl>
	 * <dt>Y'<dt>	<dd>Luma.<br />
	 * Range: [16, 235]</dd>
	 *
	 * <dt>Cb<dt>	<dd>Blue-difference (B - Y').<br />
	 * Range: [16, 240]</dd>
	 *
	 * <dt>Cr<dt>	<dd>Red-difference (R - Y').<br />
	 * Range: [16, 240]</dd>
	 * </dl>
	 *
	 * <br />
	 * ...with values from 0 to 15 as "footroom", and values from 236 to 255 as
	 * "headroom".
	 */
	YCbCr(
			ch.unifr.diva.dip.api.datatypes.BufferedImageYCbCr.class,
			new String[]{"Y", "Cb", "Cr"},
			new String[]{"Luma", "Blue-difference", "Red-difference"},
			new float[]{16.0f, 16.0f, 16.0f},
			new float[]{235.0f, 240.0f, 240.0f}
	) {
				@Override
				public int getBandVisualizationImageType() {
					return BufferedImage.TYPE_INT_RGB;
				}

				@Override
				public void doBandVisualization(WritableRaster src, WritableRaster dst, Location pt, int band) {
					final float sample = src.getSampleFloat(pt.col, pt.row, band);
					final float[] px = toRgb(new float[]{
						(band == 0) ? sample : 128.0f,
						(band == 1) ? sample : 128.0f,
						(band == 2) ? sample : 128.0f
					});

					dst.setPixel(pt.col, pt.row, px);
				}

				@Override
				public float[] toRgb(float[] ycbcr, float[] rgb) {
					final float y = ycbcr[0] - 16;
					final float cb = ycbcr[1] - 128;
					final float cr = ycbcr[2] - 128;

					rgb[0] = (0.00456621f * y + 0.00625893f * cr) * 255.0f;
					rgb[1] = (0.00456621f * y - 0.00153632f * cb - 0.00318811f * cr) * 255.0f;
					rgb[2] = (0.00456621f * y + 0.00791071f * cb) * 255.0f;

					return rgb;
				}

				@Override
				public float[] toYCbCr(float[] from, float[] to) {
					return floatCopy(from, to);
				}
			},
	/**
	 * CIE XYZ. Chromatically adopting to CIE Standard Illuminant D65 (unlike
	 * Java which adopts to CIE Standard Illumuinant D50 in
	 * {@code ColorSpace.CS_CIEXYZ}). D65 corresponds roughly to the average
	 * midday light in Western Europe/Northern Europe (comprising both direct
	 * sunlight and the light diffused by a clear sky), hence it is also called
	 * a daylight illuminant.
	 *
	 * <p>
	 * Component/sample precision: float<br />
	 * Components:
	 * <dl>
	 * <dt>X<dt>	<dd>Linear combination of cone response curves.<br />
	 * Range: [0.0, 1.0]</dd>
	 *
	 * <dt>Y<dt>	<dd>Luminance.<br />
	 * Range: [0.0, 1.0]</dd>
	 *
	 * <dt>Z<dt>	<dd>Blue-stimulation, or S cone response.<br />
	 * Range: [0.0, 1.0]</dd>
	 * </dl>
	 *
	 * <br />
	 * ...with values from 0 to 15 as "footroom", and values from 236 to 255 as
	 * "headroom".
	 */
	XYZ(
			ch.unifr.diva.dip.api.datatypes.BufferedImageXyz.class,
			new String[]{"X", "Y", "Z"},
			new String[]{"X", "Y", "Z"},
			new float[]{0.0f, 0.0f, 0.0f},
			new float[]{1.0f, 1.0f, 1.0f}
	) {
				// TODO: not really sure how to do better than scaling to use
				// the full grayscale... Any better idea?
				@Override
				public int getBandVisualizationImageType() {
					return BufferedImage.TYPE_BYTE_GRAY;
				}

				@Override
				public void doBandVisualization(WritableRaster src, WritableRaster dst, Location pt, int band) {
					final float sample = src.getSampleFloat(pt.col, pt.row, band);
					dst.setSample(pt.col, pt.row, 0, clamp(sample * 255.0f)); // just scale [0,1] to [0,255]
				}

				private float linearRgbToSrgb(float sample) {
					if (sample <= 0.0031308f) {
						return sample * 12.92f * 255.0f;
					} else {
						return (1.055f * ((float) Math.pow(sample, (1.0 / 2.4))) - 0.055f) * 255.0f;
					}
				}

				@Override
				public float[] toRgb(float[] xyz, float[] rgb) {
					final float r = 3.240479f * xyz[0] - 1.53715f * xyz[1] - 0.498535f * xyz[2];
					final float g = -0.969256f * xyz[0] + 1.875991f * xyz[1] + 0.041556f * xyz[2];
					final float b = 0.055648f * xyz[0] - 0.204043f * xyz[1] + 1.057311f * xyz[2];

					rgb[0] = clamp(linearRgbToSrgb(r));
					rgb[1] = clamp(linearRgbToSrgb(g));
					rgb[2] = clamp(linearRgbToSrgb(b));

					return rgb;
				}

				@Override
				public float[] toXyz(float[] from, float[] to) {
					return floatCopy(from, to);
				}

				private float mapXyzToLab(float sample) {
					if (sample > 0.008856) {
						return (float) Math.pow(sample, 1.0 / 3.0);
					} else {
						return 7.787037f * sample + 4.0f / 29.0f;
					}
				}

				@Override
				public float[] toLab(float[] xyz, float[] lab) {
					final float x = mapXyzToLab(xyz[0] / D65[0]);
					final float y = mapXyzToLab(xyz[1] / D65[1]);
					final float z = mapXyzToLab(xyz[2] / D65[2]);

					lab[0] = 116.0f * y - 16.0f;
					lab[1] = 500.0f * (x - y);
					lab[2] = 200.0f * (y - z);

					return lab;
				}
			},
	/**
	 * CIE L*a*b* (CIELAB).
	 *
	 * <p>
	 * Component/sample precision: float<br />
	 * Components:
	 * <dl>
	 * <dt>L<dt>	<dd>L*. Lightness.<br />
	 * Range: [0.0, 100.0]</dd>
	 *
	 * <dt>a<dt>	<dd>a*. Position between red/magenta and green.<br />
	 * Range: [-86.185, 98.254]</dd>
	 *
	 * <dt>b<dt>	<dd>b*. Position between yellow and blue.<br />
	 * Range: [-107.863, 94.482]</dd>
	 * </dl>
	 */
	Lab(
			ch.unifr.diva.dip.api.datatypes.BufferedImageLab.class,
			new String[]{"L", "a", "b"},
			new String[]{"L*", "a*", "b*"},
			new float[]{0.0f, -86.185f, -107.863f},
			new float[]{100.0f, 98.254f, 94.482f}
	) {
				@Override
				public int getBandVisualizationImageType() {
					return BufferedImage.TYPE_INT_RGB;
				}

				@Override
				public void doBandVisualization(WritableRaster src, WritableRaster dst, Location pt, int band) {
					final float sample = src.getSampleFloat(pt.col, pt.row, band);
					final float[] px = toRgb(new float[]{
						(band == 0) ? sample : 65.0f,
						(band == 1) ? sample : 0.0f,
						(band == 2) ? sample : 0.0f
					});

					dst.setPixel(pt.col, pt.row, px);
				}

				@Override
				public float[] toRgb(float[] lab, float[] rgb) {
					return SimpleColorModel.XYZ.toRgb(toXyz(lab), rgb);
				}

				private float mapXyzToLab(float sample, float tri) {
					if (sample > 0.206893034) {
						return tri * sample * sample * sample;
					} else {
						return (sample - 4.0f / 29.0f) / 7.787037f * tri;
					}
				}

				@Override
				public float[] toXyz(float[] lab, float[] xyz) {

					final float y = (lab[0] + 16) / 116.0f;
					final float x = y + lab[1] / 500.0f;
					final float z = y - lab[2] / 200.0f;

					xyz[0] = mapXyzToLab(x, D65[0]);
					xyz[1] = mapXyzToLab(y, D65[1]);
					xyz[2] = mapXyzToLab(z, D65[2]);

					return xyz;
				}

				@Override
				public float[] toLab(float[] from, float[] to) {
					return floatCopy(from, to);
				}
			};

	/**
	 * CIE XYZ reference whitepoint of standard illuminant D65 (used by sRGB).
	 */
	private static float[] D65 = {0.95047f, 1.0f, 1.08883f};

	private final Class<? extends DataType> dataTypeClass;
	private final DataType dataType;
	private final int numBands;
	private final String[] bandLabels;
	private final String[] bandDescriptions;
	private final float[] minValues;
	private final float[] maxValues;

	/**
	 * Defines a new simple color model with sample range from 0 to 255.
	 *
	 * @param dataType the data type of the color model.
	 * @param bandLabels the band labels (or acronyms).
	 * @param bandDescriptions the band descriptions.
	 */
	private SimpleColorModel(
			Class<? extends DataType> dataType,
			String[] bandLabels,
			String[] bandDescriptions
	) {
		this(
				dataType, bandLabels, bandDescriptions,
				getExtrema(bandLabels.length, 0.0f),
				getExtrema(bandLabels.length, 255.0f)
		);
	}

	/**
	 * Defines a new simple color model.
	 *
	 * @param dataType the data type of the color model.
	 * @param bandLabels the band labels (or acronyms).
	 * @param bandDescriptions the band descriptions.
	 * @param minValues array of minimum sample ranges for all bands.
	 * @param maxValues array of maximum sample ranges for all bands.
	 */
	private SimpleColorModel(
			Class<? extends DataType> dataType,
			String[] bandLabels,
			String[] bandDescriptions,
			float[] minValues,
			float[] maxValues
	) {
		this.dataTypeClass = dataType;
		DataType dt = null;
		try {
			dt = this.dataTypeClass.newInstance();
		} catch (InstantiationException | IllegalAccessException ex) {
			//
		}
		this.dataType = dt;
		this.numBands = bandLabels.length;
		this.bandLabels = bandLabels;
		this.bandDescriptions = bandDescriptions;
		this.minValues = minValues;
		this.maxValues = maxValues;
	}

	// used to define min/max sample ranges for all bands
	private static float[] getExtrema(int numBands, float value) {
		final float[] minima = new float[numBands];
		for (int i = 0; i < numBands; i++) {
			minima[i] = value;
		}
		return minima;
	}

	/**
	 * Returns the number of bands of the color model.
	 *
	 * @return the number of bands.
	 */
	public int numBands() {
		return this.numBands;
	}

	/**
	 * Returns a short band label (or acronym).
	 *
	 * @param band the index of the band.
	 * @return the band label (or acronym).
	 */
	public String bandLabel(int band) {
		return this.bandLabels[band];
	}

	/**
	 * Returns the band description.
	 *
	 * @param band the index of the band.
	 * @return the band description.
	 */
	public String bandDescription(int band) {
		return this.bandDescriptions[band];
	}

	/**
	 * Returns the data type of the color model.
	 *
	 * @return the data type of the color model.
	 */
	public DataType dataType() {
		return this.dataType;
	}

	/**
	 * Checks whether images with this color model require a
	 * {@code BufferedMatrix} instead of a {@code BufferedImage}.
	 *
	 * @return True if images with this color model require a
	 * {@code BufferedMatrix}, False otherwise.
	 */
	public boolean requiresBufferedMatrix() {
		return dataType().type().equals(BufferedMatrix.class);
	}

	/**
	 * Returns the minimum value of the defined band range.
	 *
	 * @param band the band index.
	 * @return the minimum value of the band range.
	 */
	public float minValue(int band) {
		return this.minValues[band];
	}

	/**
	 * Returns the maximum value of the defined band range.
	 *
	 * @param band the band index.
	 * @return the maximum value of the band range.
	 */
	public float maxValue(int band) {
		return this.maxValues[band];
	}

	/**
	 * Returns the minumum values of the defined band ranges for all bands.
	 *
	 * @return an array of all minimum values of band ranges.
	 */
	public float[] minValues() {
		return Arrays.copyOf(this.minValues, this.minValues.length);
	}

	/**
	 * Returns the maximum values of the defined band ranges for all bands.
	 *
	 * @return an array of all maximum values of band ranges.
	 */
	public float[] maxValues() {
		return Arrays.copyOf(this.maxValues, this.maxValues.length);
	}

	/**
	 * Returns the type of the {@code BufferedImage} required to visualize the
	 * bands of images with this color model. Individual bands are typically
	 * visualize in grayscale, but also RGB can be used (e.g. to show hue or
	 * color difference bands).
	 *
	 * @return the type of the {@code BufferedImage} to be used to visualize
	 * individual bands of images with this color model.
	 */
	public int getBandVisualizationImageType() {
		return BufferedImage.TYPE_BYTE_GRAY;
	}

	/**
	 * Computes individual band visualization on a single pixel/location and
	 * band.
	 *
	 * @param src the source raster to read from.
	 * @param dst the destination raster to write the visualization to.
	 * @param pt the pixel/location.
	 * @param band the index of the band.
	 */
	public void doBandVisualization(WritableRaster src, WritableRaster dst, Location pt, int band) {
		final float sample = src.getSampleFloat(pt.col, pt.row, band);
		dst.setSample(pt.col, pt.row, pt.band, sample);
	}

	/**
	 * Converts a pixel from this to another color model.
	 *
	 * @param cm target/destination color model.
	 * @param from source pixel data.
	 * @return the converted pixel data.
	 */
	public float[] convertTo(SimpleColorModel cm, float[] from) {
		final float[] to = new float[cm.numBands];
		return convertTo(cm, from, to);
	}

	/**
	 * Converts a pixel from this to another color model.
	 *
	 * @param cm target/destination color model.
	 * @param from source pixel data.
	 * @param to pre-allocated destination array for the converted pixel data.
	 * Can not be null, and needs to be of the size of {@code cm.numBands}.
	 * @return the converted pixel data (the {@code to} array).
	 */
	public float[] convertTo(SimpleColorModel cm, float[] from, float[] to) {
		switch (cm) {
			case CMY:
				return SimpleColorModel.this.toCmy(from, to);
			case GRAY:
				return SimpleColorModel.this.toGray(from, to);
			case HSV:
				return SimpleColorModel.this.toHsv(from, to);
			case RGB:
				return SimpleColorModel.this.toRgb(from, to);
			case RGBA:
				return SimpleColorModel.this.toRgba(from, to);
			case YUV:
				return SimpleColorModel.this.toYuv(from, to);
			case YCbCr:
				return SimpleColorModel.this.toYCbCr(from, to);
			case XYZ:
				return SimpleColorModel.this.toXyz(from, to);
			case Lab:
				return SimpleColorModel.this.toLab(from, to);
		}

		// fail-safe
		return floatCopy(from, to);
	}

	private static float[] newFloat(int n) {
		return new float[n];
	}

	private static float[] newFloat(SimpleColorModel cm) {
		return new float[cm.numBands];
	}

	/**
	 * Converts a pixel from this color model to CMY.
	 *
	 * @param from source pixel data.
	 * @return the converted pixel data.
	 */
	public float[] toCmy(float[] from) {
		return toCmy(from, newFloat(CMY));
	}

	/**
	 * Converts a pixel from this color model to CMY.
	 *
	 * @param from source pixel data.
	 * @param to pre-allocated destination array for the converted pixel data.
	 * @return the converted pixel data.
	 */
	public float[] toCmy(float[] from, float[] to) {
		return RGB.toCmy(SimpleColorModel.this.toRgb(from), to);
	}

	/**
	 * Converts a pixel from this color model to grayscale.
	 *
	 * @param from source pixel data.
	 * @return the converted pixel data.
	 */
	public float[] toGray(float[] from) {
		return toGray(from, newFloat(GRAY));
	}

	/**
	 * Converts a pixel from this color model to grayscale.
	 *
	 * @param from source pixel data.
	 * @param to pre-allocated destination array for the converted pixel data.
	 * @return the converted pixel data.
	 */
	public float[] toGray(float[] from, float[] to) {
		return RGB.toGray(SimpleColorModel.this.toRgb(from), to);
	}

	/**
	 * Converts a pixel from this color model to HSV.
	 *
	 * @param from source pixel data.
	 * @return the converted pixel data.
	 */
	public float[] toHsv(float[] from) {
		return toHsv(from, newFloat(HSV));
	}

	/**
	 * Converts a pixel from this color model to HSV.
	 *
	 * @param from source pixel data.
	 * @param to pre-allocated destination array for the converted pixel data.
	 * @return the converted pixel data.
	 */
	public float[] toHsv(float[] from, float[] to) {
		return RGB.toHsv(SimpleColorModel.this.toRgb(from), to);
	}

	/**
	 * Converts a pixel from this color model to RGB.
	 *
	 * @param from source pixel data.
	 * @return the converted pixel data.
	 */
	public float[] toRgb(float[] from) {
		return toRgb(from, newFloat(RGB));
	}

	/**
	 * Converts a pixel from this color model to RGB.
	 *
	 * @param from source pixel data.
	 * @param to pre-allocated destination array for the converted pixel data.
	 * @return the converted pixel data.
	 */
	public abstract float[] toRgb(float[] from, float[] to);

	/**
	 * Converts a pixel from this color model to RGBA.
	 *
	 * @param from source pixel data.
	 * @return the converted pixel data.
	 */
	public float[] toRgba(float[] from) {
		return toRgba(from, newFloat(RGBA));
	}

	/**
	 * Converts a pixel from this color model to RGBA.
	 *
	 * @param from source pixel data.
	 * @param to pre-allocated destination array for the converted pixel data.
	 * @return the converted pixel data.
	 */
	public float[] toRgba(float[] from, float[] to) {
		return RGB.toRgba(SimpleColorModel.this.toRgb(from), to);
	}

	/**
	 * Converts a pixel from this color model to YUV.
	 *
	 * @param from source pixel data.
	 * @return the converted pixel data.
	 */
	public float[] toYuv(float[] from) {
		return toYuv(from, newFloat(YUV));
	}

	/**
	 * Converts a pixel from this color model to YUV.
	 *
	 * @param from source pixel data.
	 * @param to pre-allocated destination array for the converted pixel data.
	 * @return the converted pixel data.
	 */
	public float[] toYuv(float[] from, float[] to) {
		return RGB.toYuv(SimpleColorModel.this.toRgb(from), to);
	}

	/**
	 * Converts a pixel from this color model to YCbCr.
	 *
	 * @param from source pixel data.
	 * @return the converted pixel data.
	 */
	public float[] toYCbCr(float[] from) {
		return toYCbCr(from, newFloat(YCbCr));
	}

	/**
	 * Converts a pixel from this color model to YCbCr.
	 *
	 * @param from source pixel data.
	 * @param to pre-allocated destination array for the converted pixel data.
	 * @return the converted pixel data.
	 */
	public float[] toYCbCr(float[] from, float[] to) {
		return RGB.toYCbCr(SimpleColorModel.this.toRgb(from), to);
	}

	/**
	 * Converts a pixel from this color model to linear RGB. Not to be confused
	 * with the standard RGB color space sRGB.
	 *
	 * @param from source pixel data.
	 * @return the converted pixel data.
	 */
	public float[] toLinearRgb(float[] from) {
		return toLinearRgb(from, new float[3]);
	}

	/**
	 * Converts a pixel from this color model to linear RGB. Not to be confused
	 * with the standard RGB color space sRGB.
	 *
	 * @param from source pixel data.
	 * @param to pre-allocated destination array for the converted pixel data.
	 * @return the converted pixel data.
	 */
	public float[] toLinearRgb(float[] from, float[] to) {
		return RGB.toLinearRgb(SimpleColorModel.this.toRgb(from), to);
	}

	/**
	 * Converts a pixel from this color model to XYZ.
	 *
	 * @param from source pixel data.
	 * @return the converted pixel data.
	 */
	public float[] toXyz(float[] from) {
		return toXyz(from, newFloat(XYZ));
	}

	/**
	 * Converts a pixel from this color model to XYZ.
	 *
	 * @param from source pixel data.
	 * @param to pre-allocated destination array for the converted pixel data.
	 * @return the converted pixel data.
	 */
	public float[] toXyz(float[] from, float[] to) {
		return RGB.toXyz(SimpleColorModel.this.toRgb(from), to);
	}

	/**
	 * Converts a pixel from this color model to Lab.
	 *
	 * @param from source pixel data.
	 * @return the converted pixel data.
	 */
	public float[] toLab(float[] from) {
		return toLab(from, newFloat(Lab));
	}

	/**
	 * Converts a pixel from this color model to Lab.
	 *
	 * @param from source pixel data.
	 * @param to pre-allocated destination array for the converted pixel data.
	 * @return the converted pixel data.
	 */
	public float[] toLab(float[] from, float[] to) {
		return RGB.toLab(SimpleColorModel.this.toRgb(from), to);
	}

	/**
	 * Returns the minimum value of the three given values.
	 *
	 * @param x the first value.
	 * @param y the second value.
	 * @param z the third value.
	 * @return the minimum value.
	 */
	public static float min(float x, float y, float z) {
		return Math.min(Math.min(x, y), z);
	}

	/**
	 * Returns the maximum value of the three given values.
	 *
	 * @param x the first value.
	 * @param y the second value.
	 * @param z the third value.
	 * @return the maximum value.
	 */
	public static float max(float x, float y, float z) {
		return Math.max(x, Math.max(y, z));
	}

	/**
	 * Clamps the given value to the range {@code [0..255]}.
	 *
	 * @param value the value.
	 * @return a value in the range {@code [0..255]}.
	 */
	public static int clamp(double value) {
		if (value < 0.0) {
			return 0;
		}

		if (value > 255.0) {
			return 255;
		}

		return (int) Math.round(value);
	}

	/**
	 * Clamps an array of floats to the range {@code [0.0f..1.0f]}.
	 *
	 * @param values an array of floats.
	 * @return an array of floats in the range {@code [0.0f..1.0f]}.
	 */
	public static float[] floatClamp(float[] values) {
		for (int i = 0; i < values.length; i++) {
			if (values[i] < 0.0f) {
				values[i] = 0.0f;
			}

			if (values[i] > 1.0f) {
				values[i] = 1.0f;
			}
		}

		return values;
	}

	/**
	 * Copies the values from an array of floats to another.
	 *
	 * @param src values to copy.
	 * @param dst an array of floats to be set.
	 * @return the to array of floats.
	 */
	public static float[] floatCopy(float[] src, float[] dst) {
		final int n = Math.min(src.length, dst.length);
		for (int i = 0; i < n; i++) {
			dst[i] = src[i];
		}
		return dst;
	}

}

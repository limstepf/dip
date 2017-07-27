package ch.unifr.diva.dip.api.parameters;

import ch.unifr.diva.dip.api.datastructures.FxColor;
import ch.unifr.diva.dip.api.datastructures.TestUtils;
import ch.unifr.diva.dip.api.datastructures.ValueList;
import ch.unifr.diva.dip.api.datastructures.ValueListSelection;
import ch.unifr.diva.dip.api.datastructures.ValueMap;
import ch.unifr.diva.dip.api.parameters.BooleanParameter.BooleanView;
import ch.unifr.diva.dip.api.parameters.CheckboxParameter.CheckboxView;
import ch.unifr.diva.dip.api.parameters.ColorPickerParameter.ColorPickerParameterView;
import ch.unifr.diva.dip.api.parameters.CompositeGridBase.GridView;
import ch.unifr.diva.dip.api.parameters.DoubleParameter.DoubleView;
import ch.unifr.diva.dip.api.parameters.EnumParameter.EnumView;
import ch.unifr.diva.dip.api.parameters.ExpParameter.ExpView;
import ch.unifr.diva.dip.api.parameters.FloatParameter.FloatView;
import ch.unifr.diva.dip.api.parameters.IntegerParameter.IntegerView;
import ch.unifr.diva.dip.api.parameters.IntegerSliderParameter.IntegerSliderView;
import ch.unifr.diva.dip.api.parameters.OptionParameter.OptionView;
import ch.unifr.diva.dip.api.parameters.StringParameter.StringView;
import ch.unifr.diva.dip.api.parameters.XorParameter.XorView;
import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.utils.FxUtils;
import ch.unifr.diva.dip.api.utils.MathUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.paint.Color;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 * Parameter unit tests.
 */
public class ParameterTest {

	public final static float FLOAT_DELTA = 1e-5f;
	public static final double DOUBLE_DELTA = 1e-10;

	@Before
	public void init() {
		FxUtils.initToolkit();
	}

	@Test
	public void testBooleanParameter() {
		Boolean initial = true;
		BooleanParameter parameter = new BooleanParameter("bool", initial);
		PersistentParameterTester<Boolean, BooleanParameter, BooleanView> tester = new PersistentParameterTester<Boolean, BooleanParameter, BooleanView>(
				parameter,
				parameter.view(),
				initial
		) {
			@Override
			public List<Boolean> values() {
				return Arrays.asList(
						false,
						true,
						false
				);
			}
		};
		tester.test();
	}

	@Test
	public void testCheckboxParameter() {
		Boolean initial = true;
		CheckboxParameter parameter = new CheckboxParameter(initial);
		PersistentParameterTester<Boolean, CheckboxParameter, CheckboxView> tester = new PersistentParameterTester<Boolean, CheckboxParameter, CheckboxView>(
				parameter,
				parameter.view(),
				initial
		) {
			@Override
			public List<Boolean> values() {
				return Arrays.asList(
						false,
						true,
						false
				);
			}
		};
		tester.test();
	}

	@Test
	public void testColorPickerParameter() {
		FxColor initial = new FxColor(Color.AZURE);
		ColorPickerParameter parameter = new ColorPickerParameter("fxcolor", initial);
		PersistentParameterTester<FxColor, ColorPickerParameter, ColorPickerParameterView> tester = new PersistentParameterTester<FxColor, ColorPickerParameter, ColorPickerParameterView>(
				parameter,
				parameter.view(),
				initial
		) {
			@Override
			public List<FxColor> values() {
				return Arrays.asList(
						new FxColor(Color.BURLYWOOD),
						new FxColor(Color.KHAKI),
						new FxColor(Color.SALMON)
				);
			}
		};
		tester.test();
	}

	@Test
	public void testCompositeGrid() {
		ValueList initial = new ValueList(getCompositeGridTestValues());
		IntegerParameter ip = new IntegerParameter("int", (int) initial.get(0));
		BooleanParameter bp = new BooleanParameter("bool", (boolean) initial.get(1));
		StringParameter sp = new StringParameter("string", (String) initial.get(2));

		List<Parameter<?>> childParameter = new ArrayList<>();
		childParameter.add(ip);
		childParameter.add(bp);
		childParameter.add(sp);

		CompositeGrid parameter = new CompositeGrid("cg", childParameter);
		PersistentParameterTester<ValueList, CompositeGrid, GridView<?, ValueList>> tester = new PersistentParameterTester<ValueList, CompositeGrid, GridView<?, ValueList>>(
				parameter,
				parameter.view(),
				initial
		) {
			@Override
			public List<ValueList> values() {
				return Arrays.asList(
						new ValueList(getCompositeGridTestValues()),
						new ValueList(getCompositeGridTestValues()),
						new ValueList(getCompositeGridTestValues()),
						new ValueList(getCompositeGridTestValues()),
						new ValueList(getCompositeGridTestValues())
				);
			}
		};
		tester.test();

		ip.set(0);
		bp.set(false);
		sp.set("a");
		tester.testComposite(ip, 2);
		tester.testComposite(bp, true);
		tester.testComposite(sp, "b");
	}

	@Test
	public void testFaultyCompositeGrid() {
		ValueList initial = new ValueList(getCompositeGridTestValues());
		int ip_val = (int) initial.get(0);
		boolean bp_val = (boolean) initial.get(1);
		String sp_val = (String) initial.get(2);
		IntegerParameter ip = new IntegerParameter("int", ip_val);
		BooleanParameter bp = new BooleanParameter("bool", bp_val);
		StringParameter sp = new StringParameter("string", sp_val);

		List<Parameter<?>> childParameter = new ArrayList<>();
		childParameter.add(ip);
		childParameter.add(bp);
		childParameter.add(sp);

		// CompositeGrid ignores faulty values
		CompositeGrid parameter = new CompositeGrid("cg", childParameter);
		ValueList val;

		// empty list
		parameter.set(new ValueList());

		val = parameter.get();
		assertEquals("still same ip value", ip_val, val.get(0));
		assertEquals("still same bp value", bp_val, val.get(1));
		assertEquals("still same sp value", sp_val, val.get(2));

		// incomplete list
		ValueList faulty = new ValueList(getCompositeGridTestValues());
		faulty.remove(faulty.size() - 1); // missing value
		parameter.set(faulty);

		val = parameter.get();
		assertEquals("still same ip value", ip_val, val.get(0));
		assertEquals("still same bp value", bp_val, val.get(1));
		assertEquals("still same sp value", sp_val, val.get(2));

		// faulty list
		faulty = new ValueList(getCompositeGridTestValues());
		faulty.set(1, TestUtils.newString()); // not assignable
		parameter.set(faulty);

		val = parameter.get();
		assertEquals("still same ip value", ip_val, val.get(0));
		assertEquals("still same bp value", bp_val, val.get(1));
		assertEquals("still same sp value", sp_val, val.get(2));
	}

	public static List<Object> getCompositeGridTestValues() {
		final List<Object> list = new ArrayList<>();
		// don't put double or float here since we run into precision issues
		// otherwise (should be compared with some delta...)
		list.add(MathUtils.randomInt(1, 32));
		list.add(MathUtils.randomBool());
		list.add(TestUtils.newString());
		return list;
	}

	@Test
	public void testCompositeGridMap() {
		ValueMap initial = new ValueMap(getCompositeGridMapTestValues());
		IntegerParameter ip = new IntegerParameter("int", (int) initial.get("ip"));
		BooleanParameter bp = new BooleanParameter("bool", (boolean) initial.get("bp"));
		StringParameter sp = new StringParameter("string", (String) initial.get("sp"));

		Map<String, Parameter<?>> childParameter = new HashMap<>();
		childParameter.put("ip", ip);
		childParameter.put("bp", bp);
		childParameter.put("sp", sp);

		CompositeGridMap parameter = new CompositeGridMap("cgm", childParameter);
		PersistentParameterTester<ValueMap, CompositeGridMap, GridView<?, ValueMap>> tester = new PersistentParameterTester<ValueMap, CompositeGridMap, GridView<?, ValueMap>>(
				parameter,
				parameter.view(),
				initial
		) {
			@Override
			public List<ValueMap> values() {
				return Arrays.asList(
						new ValueMap(getCompositeGridMapTestValues()),
						new ValueMap(getCompositeGridMapTestValues()),
						new ValueMap(getCompositeGridMapTestValues()),
						new ValueMap(getCompositeGridMapTestValues()),
						new ValueMap(getCompositeGridMapTestValues())
				);
			}
		};
		tester.test();

		ip.set(0);
		bp.set(false);
		sp.set("a");
		tester.testComposite(ip, 2);
		tester.testComposite(bp, true);
		tester.testComposite(sp, "b");
	}

	@Test
	public void testFaultyGridMapParameter() {
		ValueMap initial = new ValueMap(getCompositeGridMapTestValues());
		int ip_val = (int) initial.get("ip");
		boolean bp_val = (boolean) initial.get("bp");
		String sp_val = (String) initial.get("sp");
		IntegerParameter ip = new IntegerParameter("int", ip_val);
		BooleanParameter bp = new BooleanParameter("bool", bp_val);
		StringParameter sp = new StringParameter("string", sp_val);
		Map<String, Parameter<?>> childParameter = new HashMap<>();
		childParameter.put("ip", ip);
		childParameter.put("bp", bp);
		childParameter.put("sp", sp);

		// GridMap ignores unknown/unassignable map entries, missing entries
		// aren't a problem, the default/current value is used.
		CompositeGridMap parameter = new CompositeGridMap("cgm", childParameter);
		ValueMap val;

		// empty map
		parameter.set(new ValueMap(new HashMap<>()));

		val = parameter.get();
		assertValueMapChildParameter("ip", val, ip, ip_val);
		assertValueMapChildParameter("bp", val, bp, bp_val);
		assertValueMapChildParameter("sp", val, sp, sp_val);

		// faulty map
		final Map<String, Object> map = new HashMap<>();
		map.put("ip", MathUtils.randomBool());     // not assignable
		map.put("xx", MathUtils.randomInt(0, 32)); // unknown key (and missing key next)
		parameter.set(new ValueMap(map));

		val = parameter.get();
		assertValueMapChildParameter("ip", val, ip, ip_val);
		assertValueMapChildParameter("bp", val, bp, bp_val);
		assertValueMapChildParameter("sp", val, sp, sp_val);

		// change just single value (missing keys)
		String sp_val2 = TestUtils.newString();
		map.clear();
		map.put("sp", sp_val2);
		parameter.set(new ValueMap(map));

		val = parameter.get();
		assertValueMapChildParameter("ip", val, ip, ip_val);
		assertValueMapChildParameter("bp", val, bp, bp_val);
		assertValueMapChildParameter("sp", val, sp, sp_val2);
	}

	public static void assertValueMapChildParameter(String key, ValueMap map, PersistentParameter<?> parameter, Object expected) {
		assertEquals("expected value", expected, map.get(key));
		assertEquals("expected child parameter", expected, parameter.get());
		assertEquals("expected child view", expected, parameter.view().get());
	}

	public static Map<String, Object> getCompositeGridMapTestValues() {
		final Map<String, Object> map = new HashMap<>();
		// don't put double or float here since we run into precision issues
		// otherwise (should be compared with some delta...)
		map.put("ip", MathUtils.randomInt(0, 32));
		map.put("bp", MathUtils.randomBool());
		map.put("sp", TestUtils.newString());
		return map;
	}

	@Test
	public void testDoubleParameter() {
		Double initial = 42.0;
		DoubleParameter parameter = new DoubleParameter("double", initial);
		PersistentParameterTester<Double, DoubleParameter, DoubleView> tester = new PersistentParameterTester<Double, DoubleParameter, DoubleView>(
				parameter,
				parameter.view(),
				initial
		) {
			@Override
			public List<Double> values() {
				// we might lose some precision here, so maybe don't try Math.PI
				return Arrays.asList(
						2.7182,
						3.1415,
						0.0005
				);
			}
		};
		tester.test();
	}

	@Test
	public void testEnumParameter() {
		String initial = Processor.State.ERROR.name();
		EnumParameter parameter = new EnumParameter("enum", Processor.State.class, initial);
		PersistentParameterTester<String, EnumParameter, EnumView> tester = new PersistentParameterTester<String, EnumParameter, EnumView>(
				parameter,
				parameter.view(),
				initial
		) {
			@Override
			public List<String> values() {
				return Arrays.asList(
						Processor.State.WAITING.name(),
						Processor.State.PROCESSING.name(),
						Processor.State.UNAVAILABLE.name(),
						Processor.State.READY.name()
				);
			}

			@Override
			public void postSetTest(String val) {
				// also test the getEnumValue method
				final Processor.State state = EnumParameter.valueOf(
						val,
						Processor.State.class,
						Processor.State.ERROR
				);
				assertEquals(
						"equal enum",
						state,
						parameter.getEnumValue(Processor.State.class)
				);
			}
		};
		tester.test();
	}

	@Test
	public void testExpParameter() {
		String initial = "sin(PI) * cos(e)";
		ExpParameter parameter = new ExpParameter("exp", initial);
		PersistentParameterTester<String, ExpParameter, ExpView> tester = new PersistentParameterTester<String, ExpParameter, ExpView>(
				parameter,
				parameter.view(),
				initial
		) {
			@Override
			public List<String> values() {
				return Arrays.asList(
						"4 + 3",
						"(6 - 7) / 4.5",
						"1 / exp(0.765)"
				);
			}
		};
		tester.test();
	}

	@Test
	public void testFloatParameter() {
		Float initial = 42.0f;
		FloatParameter parameter = new FloatParameter("float", initial);
		PersistentParameterTester<Float, FloatParameter, FloatView> tester = new PersistentParameterTester<Float, FloatParameter, FloatView>(
				parameter,
				parameter.view(),
				initial
		) {
			@Override
			public List<Float> values() {
				// we might lose some precision here, so maybe don't try Math.PI
				return Arrays.asList(
						2.7182f,
						3.1415f,
						0.0005f
				);
			}
		};
		tester.test();
	}

	@Test
	public void testIntegerParameter() {
		Integer initial = 42;
		IntegerParameter parameter = new IntegerParameter("integer", initial);
		PersistentParameterTester<Integer, IntegerParameter, IntegerView> tester = new PersistentParameterTester<Integer, IntegerParameter, IntegerView>(
				parameter,
				parameter.view(),
				initial
		) {
			@Override
			public List<Integer> values() {
				// we might lose some precision here, so maybe don't try Math.PI
				return Arrays.asList(
						Integer.MIN_VALUE,
						Integer.MAX_VALUE,
						31
				);
			}
		};
		tester.test();
	}

	@Test
	public void testIntegerSliderParameterParameter() {
		int min = 0;
		int max = 256;
		Integer initial = 42;
		IntegerSliderParameter parameter = new IntegerSliderParameter("slider", initial, min, max);
		PersistentParameterTester<Integer, IntegerSliderParameter, IntegerSliderView> tester = new PersistentParameterTester<Integer, IntegerSliderParameter, IntegerSliderView>(
				parameter,
				parameter.view(),
				initial
		) {
			@Override
			public List<Integer> values() {
				return Arrays.asList(
						min,
						31,
						max
				);
			}
		};
		tester.test();
	}

	@Test
	public void testOptionParameter() {
		List<String> options = Arrays.asList(
				"a",
				"b",
				"c",
				"d",
				"e"
		);
		Integer initial = 1;
		OptionParameter parameter = new OptionParameter("integer", options, initial);
		PersistentParameterTester<Integer, OptionParameter, OptionView> tester = new PersistentParameterTester<Integer, OptionParameter, OptionView>(
				parameter,
				parameter.view(),
				initial
		) {
			@Override
			public List<Integer> values() {
				// we might lose some precision here, so maybe don't try Math.PI
				return Arrays.asList(
						0,
						options.size() - 1,
						2
				);
			}
		};
		tester.test();
	}

	@Test
	public void testStringParameter() {
		String initial = "initial-value";
		StringParameter parameter = new StringParameter("string", initial);
		PersistentParameterTester<String, StringParameter, StringView> tester = new PersistentParameterTester<String, StringParameter, StringView>(
				parameter,
				parameter.view(),
				initial
		) {
			@Override
			public List<String> values() {
				return Arrays.asList(
						"another-value",
						"yet-another-thing",
						"last-one"
				);
			}
		};
		tester.test();
	}

	@Test
	public void testXorParameter() {
		TextParameter tp = new TextParameter("option 1");
		TextParameter tp2 = new TextParameter("option 2");
		LabelParameter lp = new LabelParameter("option 3");
		DoubleParameter dp = new DoubleParameter("double", 42.42);
		FloatParameter fp = new FloatParameter("float", 3.0f);
		StringParameter sp = new StringParameter("string", "value");
		StringParameter sp2 = new StringParameter("string2", "initial-value");
		StringParameter sp3 = new StringParameter("string3", "another-value");
		BooleanParameter bp = new BooleanParameter("bool", false);
		BooleanParameter bp2 = new BooleanParameter("bool2", true);

		int initial = 2;
		XorParameter xor = new XorParameter(
				"xor",
				Arrays.asList(tp, tp2, lp, dp, fp, sp, sp2, sp3, bp, bp2),
				initial
		);
		XorView view = xor.view();

		assertTrue(
				"ValueListSelection is assignable",
				xor.isAssignable(new ValueListSelection())
		);
		assertFalse(
				"ValueList is not assignable",
				xor.isAssignable(new ValueList())
		);

		assertEquals("initial parameter value", initial, xor.get().getSelectedIndex());
		assertEquals("initial view value", initial, view.getSelectedIndex());

		setXorParameter(xor, view, 3);
		setXorParameter(xor, view, 4);
		setXorParameter(xor, view, 8);
		setXorParameter(xor, view, 5);

		changeXorParameter(xor, view, 0, false);
		changeXorParameter(xor, view, 3, false);
		changeXorParameter(xor, view, 2, true);
		changeXorParameter(xor, view, 8, true);
		changeXorParameter(xor, view, 1, true);
	}

	public static void setXorParameter(XorParameter parameter, XorView view, int selection) {
		parameter.setSelection(selection);
		assertEquals("param.setSelection parameter value", selection, parameter.get().getSelectedIndex());
		assertEquals("param.setSelection view value", selection, view.getSelectedIndex());
	}

	public static void changeXorParameter(XorParameter parameter, XorView view, int selection, boolean invalidate) {
		ValueListSelection v = parameter.get();
		v.setSelection(selection);
		parameter.set(v);
		assertEquals("val.setSelection parameter value", selection, parameter.get().getSelectedIndex());
		if (invalidate) { // required to update the view in this case!
			parameter.invalidate();
			assertEquals("val.setSelection view value", selection, view.getSelectedIndex());
		} else {
			assertNotEquals("val.setSelection view value", selection, view.getSelectedIndex());
		}
	}

	@Test
	public void testFaultyXorParameter() {
		// parameters of processors might change, older savefiles/presets might
		// lack, or have additional child-parameters. Assume current xor parameter
		// with the following child-parameters
		final double dp_val = 0.1;
		final float fp_val = 0.1f;
		final String sp_val = "value";
		DoubleParameter dp = new DoubleParameter("double", dp_val);
		FloatParameter fp = new FloatParameter("float", fp_val);
		StringParameter sp = new StringParameter("string", sp_val);
		// ...and we'll try to set ValueSelectionLists with an "edit distance" > 0
		int initial = 1;
		XorParameter xor = new XorParameter(
				"xor",
				Arrays.asList(dp, fp, sp),
				initial
		);
		XorView view = xor.view();

		assertEquals("initial parameter value", initial, xor.get().getSelectedIndex());
		assertEquals("initial view value", initial, view.getSelectedIndex());

		// currently the expected outcome of mistmatched values is to ignore the
		// faulty value/call to set
		List<ValueListSelection> faultyValues = Arrays.asList(
				// too few/missing values
				new ValueListSelection(Arrays.asList(0.2, 0.2f), 0),
				// too many/extra values
				new ValueListSelection(Arrays.asList(0.3, 0.3f, "new", "extra"), 0),
				// wrong value classes/types
				new ValueListSelection(Arrays.asList("wrong", false, 4), 0),
				// null value
				null
		);

		ValueListSelection val;
		for (ValueListSelection fval : faultyValues) {
			xor.set(fval);
			val = xor.get();
			assertEquals("old/unchanged value", dp_val, (double) dp.get(), DOUBLE_DELTA);
			assertEquals("old/unchanged value", fp_val, (float) fp.get(), FLOAT_DELTA);
			assertEquals("old/unchanged value", sp_val, sp.get());
		}

	}

}

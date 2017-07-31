package ch.unifr.diva.dip.api.components;

import ch.unifr.diva.dip.api.services.ProcessableBase;
import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.utils.FxUtils;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 * XOR port handler unit tests.
 */
public class XorPortHandlerTest {

	/**
	 * A mock processor.
	 */
	public static class MockProcessor extends ProcessableBase {

		/**
		 * Creates a new mock processor.
		 */
		public MockProcessor() {
			super("Mock processor");
		}

		@Override
		public Processor newInstance(ProcessorContext context) {
			return new MockProcessor();
		}

		@Override
		public void init(ProcessorContext context) {

		}

		@Override
		public void process(ProcessorContext context) {

		}

		@Override
		public void reset(ProcessorContext context) {

		}
	}

	@Before
	public void init() {
		FxUtils.initToolkit();
	}

	@Test
	public void testXorInputPorts() {
		MockProcessor processor = new MockProcessor();
		XorInputPorts xor = new XorInputPorts(processor);
		OutputPort<?> output = newOutputPort();
		List<InputPort<?>> inputs = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			InputPort<?> input = newInputPort(false);
			inputs.add(input);
			xor.addPort("input-" + i, input);
		}
		xor.enableAllPorts(); // usually called in constructor of processor
		xor.init(null);       // usually called in init method of processor

		assertFalse("not connected", xor.isConnected());
		assertEquals("all unconnected ports", inputs.size(), processor.inputs().size());

		for (InputPort<?> input : inputs) {
			input.connectTo(output);
			assertTrue("connected", xor.isConnected());
			assertEquals("just one connected port", 1, processor.inputs().size());

			output.disconnect();
			assertFalse("not connected", xor.isConnected());
			assertEquals("all unconnected ports", inputs.size(), processor.inputs().size());
		}
	}

	@Test
	public void textXorInputPortGroup() {
		MockProcessor processor = new MockProcessor();
		XorInputPortGroup xor = new XorInputPortGroup(processor);
		OutputPort<?> output1 = newOutputPort();
		OutputPort<?> output2 = newOutputPort();
		OutputPort<?> output3 = newOutputPort();
		XorInputPortGroup.PortGroup<InputPort<?>> group1 = new XorInputPortGroup.PortGroup<>(true);
		List<InputPort<?>> inputs1 = new ArrayList<>();
		for (int i = 0; i < 4; i++) {
			InputPort<?> input = newInputPort(false);
			inputs1.add(input);
			group1.addPort("g1-input-" + i, input);
		}
		XorInputPortGroup.PortGroup<InputPort<?>> group2 = new XorInputPortGroup.PortGroup<>();
		List<InputPort<?>> inputs2 = new ArrayList<>();
		for (int i = 0; i < 4; i++) {
			InputPort<?> input = newInputPort(i < 2); // first two required
			inputs2.add(input);
			group2.addPort("g2-input-" + i, input);
		}
		xor.addGroup(group1);
		xor.addGroup(group2);
		xor.enableAllGroups(); // usually called in constructor of processor
		xor.init(null);        // usually called in init method of processor

		final int m = inputs1.size() + inputs2.size();
		assertFalse("not connected", xor.isConnected());
		assertEquals("all unconnected ports", m, processor.inputs().size());

		// first port group is an xor (sub-)group just like XorInputPorts
		for (InputPort<?> input : inputs1) {
			input.connectTo(output1);
			assertTrue("connected", xor.isConnected());
			assertEquals("just one connected port", 1, processor.inputs().size());

			output1.disconnect();
			assertFalse("not connected", xor.isConnected());
			assertEquals("all unconnected ports", m, processor.inputs().size());
		}

		// second port group is not an xor (sub-)group, and needs all required
		// ports to be connected; all ports of the group are visible
		inputs2.get(0).connectTo(output1); // required port 1/2

		assertFalse("(still) not connected", xor.isConnected());
		assertEquals("just ports of group2", inputs2.size(), processor.inputs().size());

		inputs2.get(2).connectTo(output3); // not required port

		assertFalse("(still) not connected", xor.isConnected());
		assertEquals("just ports of group2", inputs2.size(), processor.inputs().size());

		inputs2.get(1).connectTo(output2); // required port 2/2

		assertTrue("connected", xor.isConnected());
		assertEquals("just ports of group2", inputs2.size(), processor.inputs().size());

		output1.disconnect();

		assertFalse("not connected", xor.isConnected());
		assertEquals("just ports of group2", inputs2.size(), processor.inputs().size());

		output2.disconnect();
		output3.disconnect();

		assertFalse("not connected", xor.isConnected());
		assertEquals("all unconnected ports", m, processor.inputs().size());
	}

	@Test
	public void textXorOutputPortGroup() {
		// unit test ommited; not even sure how useful this guy is...
	}

	public static InputPort<?> newInputPort(boolean required) {
		return new InputPort<>(
				new ch.unifr.diva.dip.api.datatypes.BufferedImage(),
				required
		);
	}

	public static OutputPort<?> newOutputPort() {
		return new OutputPort<>(
				new ch.unifr.diva.dip.api.datatypes.BufferedImage()
		);
	}

}

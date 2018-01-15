package ch.unifr.diva.dip.core.execution;

import ch.unifr.diva.dip.api.utils.MathUtils;
import ch.unifr.diva.dip.utils.TestMarshaller;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.xml.bind.JAXBException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Timing unit tests.
 */
public class TimingTest {

	@Rule
	public final TemporaryFolder parent = new TemporaryFolder();

	@Test
	public void testTimingMarshaller() throws IOException, JAXBException {
		TestMarshaller<Timing> tm = new TestMarshaller<Timing>(Timing.class, parent) {
			@Override
			public Timing newInstance() {
				final Timing t = new Timing();
				return initTiming(t);
			}
		};
		tm.test();
	}

	@Test
	public void testProcessorTimingMarshaller() throws IOException, JAXBException {
		TestMarshaller<ProcessorTiming> tm = new TestMarshaller<ProcessorTiming>(ProcessorTiming.class, parent) {
			@Override
			public ProcessorTiming newInstance() {
				return newProcessorTiming();
			}
		};
		tm.test();
	}

	@Test
	public void testPipelineTimingMarshaller() throws IOException, JAXBException {
		TestMarshaller<PipelineTiming> tm = new TestMarshaller<PipelineTiming>(PipelineTiming.class, parent) {
			@Override
			public PipelineTiming newInstance() {
				final List<ProcessorTiming> processorTiming = new ArrayList<>();
				final int n = MathUtils.randomInt(5, 11);
				for (int i = 0; i < n; i++) {
					processorTiming.add(newProcessorTiming());
				}
				final PipelineTiming t = new PipelineTiming(
						MathUtils.randomInt(1, 999),
						UUID.randomUUID().toString(),
						MathUtils.randomInt(1, 999),
						MathUtils.randomInt(1, 999),
						MathUtils.randomInt(1, 999),
						UUID.randomUUID().toString(),
						UUID.randomUUID().toString(),
						processorTiming
				);
				return initTiming(t);
			}
		};
		tm.test();
	}

	public static <T extends Timing> T initTiming(T timing) {
		timing.start();
		double x = Math.random();
		timing.stop();
		timing.getElapsedNanos();
		return timing;
	}

	public static ProcessorTiming newProcessorTiming() {
		final ProcessorTiming t = new ProcessorTiming(
				MathUtils.randomInt(1, 999),
				UUID.randomUUID().toString(),
				UUID.randomUUID().toString(),
				UUID.randomUUID().toString(),
				MathUtils.randomBool()
		);
		return initTiming(t);
	}

}

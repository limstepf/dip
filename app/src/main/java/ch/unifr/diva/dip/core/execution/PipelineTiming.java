package ch.unifr.diva.dip.core.execution;

import ch.unifr.diva.dip.api.utils.XmlUtils;
import ch.unifr.diva.dip.core.model.Pipeline;
import ch.unifr.diva.dip.core.model.PrototypeProcessor;
import ch.unifr.diva.dip.core.model.RunnablePipeline;
import ch.unifr.diva.dip.utils.PathParts;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Timing of a pipeline and its processors.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class PipelineTiming extends Timing {

	@XmlElement
	protected final int pageId;
	@XmlElement
	protected final String pageName;
	@XmlElement
	protected final int pageWidth;
	@XmlElement
	protected final int pageHeight;
	@XmlElement
	protected final int pipelineId;
	@XmlElement
	protected final String pipelineName;
	@XmlElement
	protected final int pipelineSize;
	@XmlElement
	protected final String pipelineExecutor;
	@XmlElement
	protected final Map<Integer, ProcessorTiming> processorTiming;

	@SuppressWarnings("unused")
	public PipelineTiming() {
		this.pageId = -1;
		this.pageName = null;
		this.pageWidth = -1;
		this.pageHeight = -1;
		this.pipelineId = -1;
		this.pipelineName = null;
		this.pipelineSize = -1;
		this.pipelineExecutor = null;
		this.processorTiming = null;
	}

	/**
	 * Creates a new pipeline timing.
	 *
	 * @param pipeline the runnable pipeline.
	 */
	public PipelineTiming(RunnablePipeline pipeline) {
		this(
				pipeline.page.id,
				pipeline.page.getName(),
				pipeline.page.getWidth(),
				pipeline.page.getHeight(),
				pipeline.id,
				pipeline.getName(),
				pipeline.getPipelineExecutor().name(),
				toProcessorTimingMap(pipeline)
		);
	}

	/**
	 * Creates a new pipeline timing.
	 *
	 * @param pageId the id of the page.
	 * @param pageName the name of the page.
	 * @param pageWidth the width of (the image) of the page.
	 * @param pageHeight the height of (the image) of the page.
	 * @param pipeline the prototype pipeline.
	 */
	public PipelineTiming(int pageId, String pageName, int pageWidth, int pageHeight, Pipeline<? extends PrototypeProcessor> pipeline) {
		this(
				pageId,
				pageName,
				pageWidth,
				pageHeight,
				pipeline.id,
				pipeline.getName(),
				pipeline.getPipelineExecutor().name(),
				toProcessorTimingMap(pipeline)
		);
	}

	/**
	 * Creates a new pipeline timing.
	 *
	 * @param pageId the id of the page.
	 * @param pageName the name of the page.
	 * @param pageWidth the width of (the image) of the page.
	 * @param pageHeight the height of (the image) of the page.
	 * @param pipelineId the id of the pipeline.
	 * @param pipelineName the name of the pipeline.
	 * @param pipelineExecutor the pipeline executor.
	 * @param processorTiming the processor timings.
	 */
	public PipelineTiming(int pageId, String pageName, int pageWidth, int pageHeight, int pipelineId, String pipelineName, String pipelineExecutor, List<ProcessorTiming> processorTiming) {
		this(
				pageId,
				pageName,
				pageWidth,
				pageHeight,
				pipelineId,
				pipelineName,
				pipelineExecutor,
				toProcessorTimingMap(processorTiming)
		);
	}

	/**
	 * Creates a new pipeline timing.
	 *
	 * @param pageId the id of the page.
	 * @param pageName the name of the page.
	 * @param pageWidth the width of (the image) of the page.
	 * @param pageHeight the height of (the image) of the page.
	 * @param pipelineId the id of the pipeline.
	 * @param pipelineName the name of the pipeline.
	 * @param pipelineExecutor the pipeline executor.
	 * @param processorTiming the processor timings.
	 */
	protected PipelineTiming(int pageId, String pageName, int pageWidth, int pageHeight, int pipelineId, String pipelineName, String pipelineExecutor, Map<Integer, ProcessorTiming> processorTiming) {
		this.pageId = pageId;
		this.pageName = pageName;
		this.pageWidth = pageWidth;
		this.pageHeight = pageHeight;
		this.pipelineId = pipelineId;
		this.pipelineName = pipelineName;
		this.pipelineSize = processorTiming.size();
		this.pipelineExecutor = pipelineExecutor;
		this.processorTiming = processorTiming;
	}

	private static <T extends Pipeline<S>, S extends PrototypeProcessor> Map<Integer, ProcessorTiming> toProcessorTimingMap(T pipeline) {
		final Map<Integer, ProcessorTiming> map = new ConcurrentHashMap<>();
		for (S processor : pipeline.processors()) {
			map.put(processor.id, new ProcessorTiming(processor));
		}
		return map;
	}

	private static Map<Integer, ProcessorTiming> toProcessorTimingMap(Collection<ProcessorTiming> processorTiming) {
		final Map<Integer, ProcessorTiming> map = new ConcurrentHashMap<>();
		for (ProcessorTiming t : processorTiming) {
			map.put(t.getProcessorId(), t);
		}
		return map;
	}

	/**
	 * Returns the (approximate) pipeline progress. Defined by the number of
	 * taken processor timings divided by the number of processors in the
	 * pipeline - considering auto processable processors only.
	 *
	 * @return the (approximate) pipeline progress.
	 */
	public double getProgress() {
		int autoProcessing = 0; // considered the total instead of the pipeline size
		int hasStopped = 0;

		for (ProcessorTiming t : processorTiming.values()) {
			if (t.isAutoProcessing()) {
				autoProcessing++;
				if (t.hasStopped()) {
					hasStopped++;
				}
			}
		}

		if (autoProcessing == 0 || autoProcessing == hasStopped) {
			return 1.0;
		}

		if (hasStopped == 0) {
			return 0.0;
		}

		return hasStopped / (double) autoProcessing;
	}

	/**
	 * Starts the timing. Unlike the {@code start()} method from {@code Timing},
	 * this method has no effect if the timing has already been started. In
	 * order to restart the timing, an explicit call to {@code restart()} is
	 * needed.
	 */
	@Override
	public void start() {
		// never restart a started pipeline timing, unless explicitly asked for
		if (startMillis > 0) {
			return;
		}
		super.start();
	}

	/**
	 * Restarts the timing.
	 */
	public void restart() {
		super.start();
	}

	/**
	 * Manually sets/registers a processor timing.
	 *
	 * @param timing the processor timing.
	 */
	public void setProcessorTiming(ProcessorTiming timing) {
		// "start" pipeline first, if this is the first timing manually set
		if (startMillis <= 0) {
			startMillis = timing.getStartMillis();
			start = timing.getStart();
		}

		// and overwrite pipeline stop time in either case
		stop = timing.getStop();
		elapsed = -1L;

		processorTiming.put(timing.processorId, timing);
	}

	/**
	 * Returns the id of the page.
	 *
	 * @return the id of the page.
	 */
	public int getPageId() {
		return pageId;
	}

	/**
	 * Returns the name of the page.
	 *
	 * @return the name of the page.
	 */
	public String getPageName() {
		return pageName;
	}

	/**
	 * Returns the width of (the image) the page.
	 *
	 * @return the width of (the image) the page.
	 */
	public int getPageWidth() {
		return pageWidth;
	}

	/**
	 * Returns the height of (the image) the page.
	 *
	 * @return the height of (the image) the page.
	 */
	public int getPageHeight() {
		return pageHeight;
	}

	/**
	 * Returns the id of the pipeline.
	 *
	 * @return the id of the pipeline.
	 */
	public int getPipelineId() {
		return pipelineId;
	}

	/**
	 * Returns the name of the pipeline.
	 *
	 * @return the name of the pipeline.
	 */
	public String getPipelineName() {
		return pipelineName;
	}

	/**
	 * Returns the size of the pipeline. The size of the pipeline is the number
	 * of processors in it.
	 *
	 * @return the size of the pipeline.
	 */
	public int getPipelineSize() {
		return pipelineSize;
	}

	/**
	 * Returns the name of the pipeline executor.
	 *
	 * @return the name of the pipeline executor.
	 */
	public String getPipelineExecutor() {
		return pipelineExecutor;
	}

	/**
	 * Returns the processor timing map. The processor id is used as key.
	 *
	 * @return the processor timing map.
	 */
	public Map<Integer, ProcessorTiming> getProcessorTimingMap() {
		return processorTiming;
	}

	/**
	 * Returns a sorted (by stage, and start time) list of processor timings.
	 *
	 * @return a sorted list of processor timings.
	 */
	public List<ProcessorTiming> getProcessorTimings() {
		final List<ProcessorTiming> timings = new ArrayList<>(processorTiming.values());
		Collections.sort(timings, (ProcessorTiming t, ProcessorTiming t1) -> {
			if (t.getPipelineStage() == t1.getPipelineStage()) {
				return Long.compare(t.getStartMillis(), t1.getStartMillis());
			}
			return Integer.compare(t.getPipelineStage(), t1.getPipelineStage());
		});
		return timings;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 59 * hash + this.pageId;
		hash = 59 * hash + Objects.hashCode(this.pageName);
		hash = 59 * hash + this.pageWidth;
		hash = 59 * hash + this.pageHeight;
		hash = 59 * hash + this.pipelineId;
		hash = 59 * hash + Objects.hashCode(this.pipelineName);
		hash = 59 * hash + this.pipelineSize;
		hash = 59 * hash + Objects.hashCode(this.pipelineExecutor);
		hash = 59 * hash + Objects.hashCode(this.processorTiming);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final PipelineTiming other = (PipelineTiming) obj;
		if (this.pageId != other.pageId) {
			return false;
		}
		if (!Objects.equals(this.pageName, other.pageName)) {
			return false;
		}
		if (this.pageWidth != other.pageWidth) {
			return false;
		}
		if (this.pageHeight != other.pageHeight) {
			return false;
		}
		if (this.pipelineId != other.pipelineId) {
			return false;
		}
		if (!Objects.equals(this.pipelineName, other.pipelineName)) {
			return false;
		}
		if (this.pipelineSize != other.pipelineSize) {
			return false;
		}
		if (!Objects.equals(this.pipelineExecutor, other.pipelineExecutor)) {
			return false;
		}
		if (!Objects.equals(this.processorTiming, other.processorTiming)) {
			return false;
		}
		return true;
	}

	/**
	 * Returns the CSV header line.
	 *
	 * @return the CSV header line.
	 */
	public static String getCSVHeader() {
		return "page-id,page-name,page-width,page-height,"
				+ "pipeline-id,pipeline-name,pipeline-size,pipeline-executor,"
				+ "start-millis,start-nanos,stop-nanos,elapsed-nanos";
	}

	@Override
	public String toCSV() {
		return String.format(
				"%d,%s,%d,%d,%d,%s,%d,%s,%d,%d,%d,%d",
				this.getPageId(),
				escapeToCSV(this.getPageName()),
				this.getPageWidth(),
				this.getPageHeight(),
				this.getPipelineId(),
				escapeToCSV(this.getPipelineName()),
				this.getPipelineSize(),
				this.getPipelineExecutor(),
				this.getStartMillis(),
				this.getStart(),
				this.getStop(),
				this.getElapsedNanos()
		);
	}

	/**
	 * Writes the pipeline CSV to a file.
	 *
	 * @param file the file.
	 * @throws IOException
	 */
	public void writePipelineCSV(Path file) throws IOException {
		writePipelineCSV(Arrays.asList(this), file);
	}

	/**
	 * Writes the pipeline CSV to an output stream.
	 *
	 * @param os the output stream.
	 */
	public void writePipelineCSV(OutputStream os) {
		writePipelineCSV(Arrays.asList(this), os);
	}

	/**
	 * Writes the pipeline CSV to a file.
	 *
	 * @param pipelineTimings the pipeline timings.
	 * @param file the file.
	 * @throws IOException
	 */
	public static void writePipelineCSV(List<PipelineTiming> pipelineTimings, Path file) throws IOException {
		if (Files.exists(file)) {
			Files.delete(file);
		}
		try (final OutputStream os = Files.newOutputStream(file)) {
			writePipelineCSV(pipelineTimings, os);
		}
	}

	/**
	 * Writes the pipeline CSV to an output stream.
	 *
	 * @param pipelineTimings the pipeline timings.
	 * @param os the output stream.
	 */
	public static void writePipelineCSV(List<PipelineTiming> pipelineTimings, OutputStream os) {
		try (final PrintWriter writer = new PrintWriter(os)) {
			writer.println(PipelineTiming.getCSVHeader());
			for (PipelineTiming timing : pipelineTimings) {
				writer.println(timing.toCSV());
			}
		}
	}

	/**
	 * Writes the processor CSV to a file.
	 *
	 * @param file the file.
	 * @throws IOException
	 */
	public void writeProcessorCSV(Path file) throws IOException {
		writeProcessorCSV(Arrays.asList(this), file);
	}

	/**
	 * Writes the processor CSV to an output stream.
	 *
	 * @param os the output stream.
	 */
	public void writeProcessorCSV(OutputStream os) {
		writeProcessorCSV(Arrays.asList(this), os);
	}

	/**
	 * Writes the processor CSV to a file.
	 *
	 * @param pipelineTimings the pipeline timings.
	 * @param file the file.
	 * @throws IOException
	 */
	public static void writeProcessorCSV(List<PipelineTiming> pipelineTimings, Path file) throws IOException {
		if (Files.exists(file)) {
			Files.delete(file);
		}
		try (final OutputStream os = Files.newOutputStream(file)) {
			writeProcessorCSV(pipelineTimings, os);
		}
	}

	/**
	 * Writes the processor CSV to an output stream.
	 *
	 * @param pipelineTimings the pipeline timings.
	 * @param os the output stream.
	 */
	public static void writeProcessorCSV(List<PipelineTiming> pipelineTimings, OutputStream os) {
		try (final PrintWriter writer = new PrintWriter(os)) {
			writer.println(ProcessorTiming.getCSVHeader());
			for (PipelineTiming pipelineTiming : pipelineTimings) {
				for (ProcessorTiming timing : pipelineTiming.getProcessorTimings()) {
					writer.println(timing.toCSV(pipelineTiming));
				}
			}
		}
	}

	/**
	 * Returns the file for the processor CSV, given the file of the pipeline
	 * CSV.
	 *
	 * @param file a path to the pipeline CSV file.
	 * @return a path to the processor CSV file.
	 */
	public static Path getProcessorFile(Path file) {
		final PathParts pp = new PathParts(file);
		return pp.getSibling("-processors");
	}

	/**
	 * Loads a pipeline timing. This method reads the file from an input stream,
	 * which is needed to read from a zip filesystem.
	 *
	 * @param file path to the file containing the pipeline timing.
	 * @return the pipeline timing.
	 * @throws IOException
	 * @throws JAXBException
	 */
	public static PipelineTiming loadAsStream(Path file) throws IOException, JAXBException {
		try (InputStream stream = new BufferedInputStream(Files.newInputStream(file))) {
			return load(stream);
		}
	}

	/**
	 * Loads a pipeline timing.
	 *
	 * @param file path to the file containing the patch.
	 * @return the pipeline timing.
	 * @throws JAXBException
	 */
	public static PipelineTiming load(Path file) throws JAXBException {
		return XmlUtils.unmarshal(PipelineTiming.class, file);
	}

	/**
	 * Load a pipeline timing from an input stream.
	 *
	 * @param stream the input stream containing the patch.
	 * @return the pipeline timing.
	 * @throws JAXBException
	 */
	public static PipelineTiming load(InputStream stream) throws JAXBException {
		return XmlUtils.unmarshal(PipelineTiming.class, stream);
	}

	/**
	 * Writes the pipeline timing to an output stream.
	 *
	 * @param stream the output stream.
	 * @throws JAXBException
	 */
	public void save(OutputStream stream) throws JAXBException {
		XmlUtils.marshal(this, stream);
	}

	/**
	 * Writes the pipeline timing to a file.
	 *
	 * @param file path to the file.
	 * @throws JAXBException
	 */
	public void save(Path file) throws JAXBException {
		XmlUtils.marshal(this, file);
	}

}

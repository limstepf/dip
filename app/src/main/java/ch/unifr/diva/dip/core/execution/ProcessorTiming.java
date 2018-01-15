package ch.unifr.diva.dip.core.execution;

import ch.unifr.diva.dip.core.model.PrototypeProcessor;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Timing of a processor.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ProcessorTiming extends Timing {

	@XmlElement
	protected final int processorId;
	@XmlElement
	protected final String processorName;
	@XmlElement
	protected final String processorPID;
	@XmlElement
	protected final String processorVersion;
	@XmlElement
	protected final boolean isAutoProcessing;
	@XmlElement
	private volatile int stage = 0;

	@SuppressWarnings("unused")
	public ProcessorTiming() {
		this(
				-1,
				null,
				null,
				null,
				false
		);
	}

	/**
	 * Creates a new processor timing.
	 *
	 * @param <T> type of the processor.
	 * @param processor the processor.
	 */
	public <T extends PrototypeProcessor> ProcessorTiming(T processor) {
		this(
				processor.id,
				processor.serviceObject().name(),
				processor.pid(),
				processor.version().toString(),
				processor.serviceObject().canProcess()
		);
	}

	/**
	 * Creates a new processor timing.
	 *
	 * @param processorId the processor id.
	 * @param processorName the processor name.
	 * @param processorPID the PID (persistent ID) of the processor/service.
	 * @param processorVersion the version of the processor/service.
	 * @param isAutoProcessing whether or not the processor can be automatically
	 * processed or not.
	 */
	public ProcessorTiming(int processorId, String processorName, String processorPID, String processorVersion, boolean isAutoProcessing) {
		this.processorId = processorId;
		this.processorName = processorName;
		this.processorPID = processorPID;
		this.processorVersion = processorVersion;
		this.isAutoProcessing = isAutoProcessing;
	}

	/**
	 * Returns the processor id.
	 *
	 * @return the processor id.
	 */
	public int getProcessorId() {
		return processorId;
	}

	/**
	 * Returns the processor name.
	 *
	 * @return the processor name.
	 */
	public String getProcessorName() {
		return processorName;
	}

	/**
	 * Returns the PID (persistent ID) of the processor/service.
	 *
	 * @return the PID (persistent ID) of the processor/service.
	 */
	public String getProcessorPID() {
		return processorPID;
	}

	/**
	 * Returns the version of the processor/service.
	 *
	 * @return the version of the processor/service.
	 */
	public String getProcessorVersion() {
		return processorVersion;
	}

	/**
	 * Checks whether the timed processor can be automatically processed.
	 *
	 * @return {@code true} if the timed processor can be automatically
	 * processed, {@code false} otherwise.
	 */
	public boolean isAutoProcessing() {
		return isAutoProcessing;
	}

	/**
	 * Sets the pipeline stage of this processor timing.
	 *
	 * @param stage the pipeline stage.
	 */
	public void setPipelineStage(int stage) {
		this.stage = stage;
	}

	/**
	 * Returns the pipeline stage of this processor timing.
	 *
	 * @return the pipeline stage.
	 */
	public int getPipelineStage() {
		return stage;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 67 * hash + this.processorId;
		hash = 67 * hash + Objects.hashCode(this.processorName);
		hash = 67 * hash + Objects.hashCode(this.processorPID);
		hash = 67 * hash + Objects.hashCode(this.processorVersion);
		hash = 67 * hash + this.stage;
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
		final ProcessorTiming other = (ProcessorTiming) obj;
		if (this.processorId != other.processorId) {
			return false;
		}
		if (!Objects.equals(this.processorName, other.processorName)) {
			return false;
		}
		if (!Objects.equals(this.processorPID, other.processorPID)) {
			return false;
		}
		if (!Objects.equals(this.processorVersion, other.processorVersion)) {
			return false;
		}
		if (this.stage != other.stage) {
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
				+ "pipeline-id,pipeline-stage,"
				+ "processor-id,processor-name,processor-pid,processor-version,"
				+ "start-millis,start-nanos,stop-nanos,elapsed-nanos";
	}

	@Override
	public String toCSV() {
		return String.format(
				"%d,%s,%d,%d,%d,%d,%d,%s,%s,%s,%d,%d,%d,%d",
				-1,
				"",
				-1,
				-1,
				-1,
				this.getPipelineStage(),
				this.getProcessorId(),
				escapeToCSV(this.getProcessorName()),
				escapeToCSV(this.getProcessorPID()),
				escapeToCSV(this.getProcessorVersion()),
				this.getStartMillis(),
				this.getStart(),
				this.getStop(),
				this.getElapsedNanos()
		);
	}

	/**
	 * Returns the CSV line of this timing.
	 *
	 * @param pipelineTiming the parent pipeline timing.
	 * @return the CSV line of this timing.
	 */
	public String toCSV(PipelineTiming pipelineTiming) {
		return String.format(
				"%d,%s,%d,%d,%d,%d,%d,%s,%s,%s,%d,%d,%d,%d",
				pipelineTiming.getPageId(),
				escapeToCSV(pipelineTiming.getPageName()),
				pipelineTiming.getPageWidth(),
				pipelineTiming.getPageHeight(),
				pipelineTiming.getPipelineId(),
				this.getPipelineStage(),
				this.getProcessorId(),
				escapeToCSV(this.getProcessorName()),
				escapeToCSV(this.getProcessorPID()),
				escapeToCSV(this.getProcessorVersion()),
				this.getStartMillis(),
				this.getStart(),
				this.getStop(),
				this.getElapsedNanos()
		);
	}

}

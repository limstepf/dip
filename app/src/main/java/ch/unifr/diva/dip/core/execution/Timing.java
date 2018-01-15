package ch.unifr.diva.dip.core.execution;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Simple timing class to measure execution time in nanoseconds. This class
 * isn't fully thread-safe, and assumes that there are no race conditions (due
 * to program logic) in taking the timing data, but memory is visible to all
 * threads (volatile).
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Timing {

	@XmlElement
	protected volatile long startMillis;
	@XmlElement
	protected volatile long start;
	@XmlElement
	protected volatile long stop;
	@XmlElement
	protected volatile long elapsed;

	/**
	 * Creates a new timing object.
	 */
	public Timing() {

	}

	/**
	 * Starts the timing.
	 */
	public void start() {
		startMillis = System.currentTimeMillis();
		start = System.nanoTime();
		elapsed = -1L;
	}

	/**
	 * Returns the start time in milliseconds with a well-defined origin time.
	 * This timing has been taken with {@code System.currentTimeMillis()} which
	 * returns the difference, measured in milliseconds, between the current
	 * time and midnight, January 1, 1970 UTC.
	 *
	 * <p>
	 * Unlike the start time returned by {@code getStart()}, this start time is
	 * meaningful on its own, and may be converted into a date (e.g. with
	 * {@code getStartInstant()}).
	 *
	 * @return the start time in milliseconds.
	 */
	public long getStartMillis() {
		return startMillis;
	}

	/**
	 * Returns the instant of the start time.
	 *
	 * @return the instant of the start time.
	 */
	public Instant getStartInstant() {
		return Instant.ofEpochMilli(getStartMillis());
	}

	/**
	 * Returns the start time in nanoseconds with some fixed but arbitrary
	 * origin time (perhaps in the future, so values may be negative). This
	 * timing has been taken with {@code System.nanoTime()} and as such is not
	 * meaningful on its own; only the difference between two such timing values
	 * (from the same session of a Java virtual machine) is.
	 *
	 * @return the start time in nanoseconds.
	 */
	public long getStart() {
		return start;
	}

	/**
	 * Stops the timing.
	 */
	public void stop() {
		stop = System.nanoTime();
	}

	/**
	 * Returns the stop time in nanoseconds.
	 *
	 * @return the stop time in nanoseconds.
	 */
	public long getStop() {
		return stop;
	}

	/**
	 * Checks whether the timing has been taken.
	 *
	 * @return {@code true} if the timing has been taken/stopped, {@code false}
	 * otherwise.
	 */
	public boolean hasStopped() {
		return stop != 0L;
	}

	/**
	 * Returns the elapsed time in nanoseconds.
	 *
	 * @return the elapsed time in nanoseconds.
	 */
	public long getElapsedNanos() {
		if (elapsed < 0) {
			elapsed = stop - start;
		}
		return elapsed;
	}

	/**
	 * Returns the elapsed time in microseconds.
	 *
	 * @return the elapsed time in microseconds.
	 */
	public long getElapsedMicros() {
		return TimeUnit.NANOSECONDS.toMicros(getElapsedNanos());
	}

	/**
	 * Returns the elapsed time in milliseconds.
	 *
	 * @return the elapsed time in milliseconds.
	 */
	public long getElapsedMillis() {
		return TimeUnit.NANOSECONDS.toMillis(getElapsedNanos());
	}

	/**
	 * Returns the elapsed time as duration.
	 *
	 * @return the elapsed time as duration.
	 */
	public Duration getElapsedDuration() {
		return Duration.ofNanos(getElapsedNanos());
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 67 * hash + (int) (this.start ^ (this.start >>> 32));
		hash = 67 * hash + (int) (this.stop ^ (this.stop >>> 32));
		hash = 67 * hash + (int) (this.elapsed ^ (this.elapsed >>> 32));
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
		final Timing other = (Timing) obj;
		if (this.start != other.start) {
			return false;
		}
		if (this.stop != other.stop) {
			return false;
		}
		if (this.elapsed != other.elapsed) {
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
		return "start-millis,start-nanos,stop-nanos,elapsed-nanos";
	}

	/**
	 * Returns the CSV line of this timing.
	 *
	 * @return the CSV line of this timing.
	 */
	public String toCSV() {
		return String.format(
				"%d,%d,%d,%d",
				this.getStartMillis(),
				this.getStart(),
				this.getStop(),
				this.getElapsedNanos()
		);
	}

	protected static final String CSV_SEPARATOR = ",";
	protected static final String CSV_QUOTE = "\"";
	protected static final String CSV_DOUBLEQUOTE = "\"";

	/**
	 * Escapes a string to be safely used as a CSV value.
	 *
	 * @param value some string.
	 * @return the escaped string.
	 */
	protected static String escapeToCSV(String value) {
		if ((value.contains(CSV_SEPARATOR)) || (value.contains(CSV_QUOTE))) {
			return CSV_QUOTE
					+ value.replaceAll(CSV_QUOTE, CSV_DOUBLEQUOTE)
					+ CSV_QUOTE;
		}
		return value;
	}

}

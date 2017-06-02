package ch.unifr.diva.dip.imaging.utils;

import javafx.scene.image.Image;

/**
 * Helper class to manage int buffers. Embarrassing helper class to work around
 * the fact that we have no direct access to buffers of JavaFX images. For
 * frequently called image processing methods we might want to reuse those
 * buffers instead of reallocating huge chunks of memory over and over again...
 *
 * So as long as the size remains the same, we do just that, otherwise new
 * memory is allocated.
 */
public class AdaptiveIntBuffer {

	private final int[][] buffers;

	/**
	 * Creates new adaptive int buffer(s).
	 *
	 * @param num number of separate int buffers.
	 */
	public AdaptiveIntBuffer(int num) {
		this.buffers = new int[num][];
	}

	/**
	 * Returns a buffer that fits the given size of the image.
	 *
	 * @param index index of the buffer. Does get newly allocated if the size
	 * doesn't match.
	 * @param image the image determining the size of the buffer (number of
	 * pixels).
	 * @return the int buffer.
	 */
	public int[] get(int index, Image image) {
		return get(index, (int) (image.getWidth() * image.getHeight()));
	}

	/**
	 * Returns a buffer that fits the given size.
	 *
	 * @param index index of the buffer. Does get newly allocated if the size
	 * doesn't match.
	 * @param size the required size of the buffer.
	 * @return the int buffer.
	 */
	public int[] get(int index, int size) {
		if (buffers[index] == null || buffers[index].length != size) {
			buffers[index] = new int[size];
		}
		return buffers[index];
	}

	/**
	 * Clears/frees a buffer.
	 *
	 * @param index index of the buffer.
	 */
	public void clear(int index) {
		buffers[index] = null;
	}

	/**
	 * Clears/frees all buffers.
	 */
	public void clear() {
		for (int i = 0; i < buffers.length; i++) {
			buffers[i] = null;
		}
	}

}

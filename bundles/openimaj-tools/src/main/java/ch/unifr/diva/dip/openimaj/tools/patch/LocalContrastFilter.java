/**
 * Copyright (c) 2011, The University of Southampton and the individual
 * contributors. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package ch.unifr.diva.dip.openimaj.tools.patch;

import java.util.Set;
import org.openimaj.image.FImage;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * Patched local contrast filter. The current version (1.3.5) is broken, but
 * should be fixed with the next release.
 *
 * @see
 * <a href="https://github.com/openimaj/openimaj/issues/131">https://github.com/openimaj/openimaj/issues/131</a>
 * @see
 * <a href="https://github.com/openimaj/openimaj/pull/132">https://github.com/openimaj/openimaj/pull/132</a>
 */
public class LocalContrastFilter implements SinglebandImageProcessor<Float, FImage> {

	private Set<Pixel> support;

	/**
	 * Creates a new local contrast filter.
	 *
	 * @param support the support coordinates.
	 */
	public LocalContrastFilter(Set<Pixel> support) {
		this.support = support;
	}

	@Override
	public void processImage(FImage image) {
		final FImage tmpImage = new FImage(image.width, image.height);
		float min;
		float max;

		for (int y = 0; y < image.height; y++) {
			for (int x = 0; x < image.width; x++) {
				min = Float.MAX_VALUE;
				max = -Float.MAX_VALUE;
				for (final Pixel sp : support) {
					final int xx = x + sp.x;
					final int yy = y + sp.y;

					if (xx >= 0 && xx < image.width - 1 && yy >= 0 && yy < image.height - 1) {
						min = Math.min(min, image.pixels[yy][xx]);
						max = Math.max(max, image.pixels[yy][xx]);
					}
				}
				tmpImage.pixels[y][x] = max - min;
			}
		}
		image.internalAssign(tmpImage);
	}

}

/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package org.blinksd.color_extractor.palette;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;

import org.blinksd.color_extractor.palette.quantizers.Quantizer;

import java.util.Collections;
import java.util.List;


/**
 * A helper class to extract prominent colors from an image.
 *
 * <p>Instances are created with a {@link Builder} which supports several options to tweak the
 * generated Palette. See that class' documentation for more information.
 *
 * <p>Generation should always be completed on a background thread, ideally the one in which you
 * load your image on. {@link Builder} supports both synchronous and asynchronous generation:
 *
 * <pre>
 * // Synchronous
 * Palette p = Palette.from(bitmap).generate();
 *
 * // Asynchronous
 * Palette.from(bitmap).generate(new PaletteAsyncListener() {
 *     public void onGenerated(Palette p) {
 *         // Use generated instance
 *     }
 * });
 * </pre>
 */
public final class Palette {

    /**
     * Listener to be used with {link #generateAsync(Bitmap, Palette.PaletteAsyncListener)} or
     * {link #generateAsync(Bitmap, int, Palette.PaletteAsyncListener)}
     */
    public interface PaletteAsyncListener {

        /**
         * Called when the {@link Palette} has been generated.
         */
        void onGenerated(Palette palette);
    }

    static final int DEFAULT_RESIZE_BITMAP_AREA = 112 * 112;
    static final int DEFAULT_CALCULATE_NUMBER_COLORS = 16;
    static final String LOG_TAG = "Palette";

    /** Start generating a {@link Palette} with the returned {@link Builder} instance. */
    public static Builder from(Bitmap bitmap, Quantizer quantizer) {
        return new Builder(bitmap, quantizer);
    }

    private final List<Swatch> mSwatches;

    Palette(List<Swatch> swatches) {
        mSwatches = swatches;
    }

    /** Returns all of the swatches which make up the palette. */
    public List<Swatch> getSwatches() {
        return Collections.unmodifiableList(mSwatches);
    }

    /**
     * Represents a color swatch generated from an image's palette. The RGB color can be retrieved
     * by
     * calling {@link #getInt()}.
     */
    public static class Swatch {
        private final int mColor;
        private final int mPopulation;


        public Swatch(int colorInt, int population) {
            mColor = colorInt;
            mPopulation = population;
        }

        /** @return this swatch's RGB color value */
        public int getInt() {
            return mColor;
        }

        /** @return the number of pixels represented by this swatch */
        public int getPopulation() {
            return mPopulation;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() +
                    " [" + mColor + ']' +
                    " [Population: " + mPopulation + ']';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Swatch swatch = (Swatch) o;
            return mPopulation == swatch.mPopulation && mColor == swatch.mColor;
        }

        @Override
        public int hashCode() {
            return 31 * mColor + mPopulation;
        }
    }

    /** Builder class for generating {@link Palette} instances. */
    @SuppressWarnings("unused")
    public static class Builder {
        private final List<Swatch> mSwatches;
        private final Bitmap mBitmap;
        private final Quantizer mQuantizer;

        private int mMaxColors = DEFAULT_CALCULATE_NUMBER_COLORS;
        private int mResizeArea = DEFAULT_RESIZE_BITMAP_AREA;
        private int mResizeMaxDimension = -1;

        private Rect mRegion;

        /** Construct a new {@link Builder} using a source {@link Bitmap} */
        public Builder(Bitmap bitmap, Quantizer quantizer) {
            if (bitmap == null || bitmap.isRecycled()) {
                throw new IllegalArgumentException("Bitmap is not valid");
            }

            assert quantizer == null : "Quantizer cannot be null";
            mSwatches = null;
            mBitmap = bitmap;
            mQuantizer = quantizer;
        }

        /**
         * Construct a new {@link Builder} using a list of {@link Swatch} instances. Typically only
         * used
         * for testing.
         */
        public Builder(List<Swatch> swatches) {
            if (swatches == null || swatches.isEmpty()) {
                throw new IllegalArgumentException("List of Swatches is not valid");
            }
            mSwatches = swatches;
            mBitmap = null;
            mQuantizer = null;
        }

        /**
         * Set the maximum number of colors to use in the quantization step when using a {@link
         * android.graphics.Bitmap} as the source.
         *
         * <p>Good values for depend on the source image type. For landscapes, good values are in
         * the
         * range 10-16. For images which are largely made up of people's faces then this value
         * should be
         * increased to ~24.
         */
        
        public Builder maximumColorCount(int colors) {
            mMaxColors = colors;
            return this;
        }

        /**
         * Set the resize value when using a {@link android.graphics.Bitmap} as the source. If the
         * bitmap's largest dimension is greater than the value specified, then the bitmap will be
         * resized so that its largest dimension matches {@code maxDimension}. If the bitmap is
         * smaller
         * or equal, the original is used as-is.
         *
         * @param maxDimension the number of pixels that the max dimension should be scaled down to,
         *                     or
         *                     any value <= 0 to disable resizing.
         * @deprecated Using {@link #resizeBitmapArea(int)} is preferred since it can handle
         * abnormal
         * aspect ratios more gracefully.
         */
        
        @Deprecated
        public Builder resizeBitmapSize(int maxDimension) {
            mResizeMaxDimension = maxDimension;
            mResizeArea = -1;
            return this;
        }

        /**
         * Set the resize value when using a {@link android.graphics.Bitmap} as the source. If the
         * bitmap's area is greater than the value specified, then the bitmap will be resized so
         * that
         * its area matches {@code area}. If the bitmap is smaller or equal, the original is used
         * as-is.
         *
         * <p>This value has a large effect on the processing time. The larger the resized image is,
         * the
         * greater time it will take to generate the palette. The smaller the image is, the more
         * detail
         * is lost in the resulting image and thus less precision for color selection.
         *
         * @param area the number of pixels that the intermediary scaled down Bitmap should cover,
         *             or
         *             any value <= 0 to disable resizing.
         */
        
        public Builder resizeBitmapArea(int area) {
            mResizeArea = area;
            mResizeMaxDimension = -1;
            return this;
        }

        /** Generate and return the {@link Palette} synchronously. */
        
        public Palette generate() {
            List<Swatch> swatches;

            if (mBitmap != null) {
                // We have a Bitmap so we need to use quantization to reduce the number of colors

                // First we'll scale down the bitmap if needed
                Bitmap bitmap = scaleBitmapDown(mBitmap);

                Rect region = mRegion;
                if (bitmap != mBitmap && region != null) {
                    // If we have a scaled bitmap and a selected region, we need to scale down the
                    // region to match the new scale
                    double scale = bitmap.getWidth() / (double) mBitmap.getWidth();
                    region.left = (int) Math.floor(region.left * scale);
                    region.top = (int) Math.floor(region.top * scale);
                    region.right = Math.min((int) Math.ceil(region.right * scale),
                            bitmap.getWidth());
                    region.bottom = Math.min((int) Math.ceil(region.bottom * scale),
                            bitmap.getHeight());
                }

                // Now generate a quantizer from the Bitmap

                mQuantizer.quantize(
                        getPixelsFromBitmap(bitmap),
                        mMaxColors);
                // If created a new bitmap, recycle it
                if (bitmap != mBitmap) {
                    bitmap.recycle();
                }
                swatches = mQuantizer.getQuantizedColors();
            } else if (mSwatches != null) {
                // Else we're using the provided swatches
                swatches = mSwatches;
            } else {
                // The constructors enforce either a bitmap or swatches are present.
                throw new AssertionError();
            }

            // Now create a Palette instance
            // And make it generate itself
            return new Palette(swatches);
        }

        /**
         * Generate the {@link Palette} asynchronously. The provided listener's {@link
         * PaletteAsyncListener#onGenerated} method will be called with the palette when generated.
         *
         * @deprecated Use the standard <code>java.util.concurrent</code> or <a
         * href="https://developer.android.com/topic/libraries/architecture/coroutines">Kotlin
         * concurrency utilities</a> to call {@link #generate()} instead.
         */
        @SuppressWarnings("deprecation")
        @SuppressLint("StaticFieldLeak")
        @Deprecated
        public android.os.AsyncTask<Bitmap, Void, Palette> generate(
                PaletteAsyncListener listener) {
            assert (listener != null);

            return new android.os.AsyncTask<Bitmap, Void, Palette>() {
                @Override
                
                protected Palette doInBackground(Bitmap... params) {
                    try {
                        return generate();
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Exception thrown during async generate", e);
                        return null;
                    }
                }

                @Override
                protected void onPostExecute(Palette colorExtractor) {
                    listener.onGenerated(colorExtractor);
                }
            }.executeOnExecutor(android.os.AsyncTask.THREAD_POOL_EXECUTOR, mBitmap);
        }

        private int[] getPixelsFromBitmap(Bitmap bitmap) {
            int bitmapWidth = bitmap.getWidth();
            int bitmapHeight = bitmap.getHeight();
            int[] pixels = new int[bitmapWidth * bitmapHeight];
            bitmap.getPixels(pixels, 0, bitmapWidth, 0, 0, bitmapWidth, bitmapHeight);

            if (mRegion == null) {
                // If we don't have a region, return all of the pixels
                return pixels;
            } else {
                // If we do have a region, lets create a subset array containing only the region's
                // pixels
                int regionWidth = mRegion.width();
                int regionHeight = mRegion.height();
                // pixels contains all of the pixels, so we need to iterate through each row and
                // copy the regions pixels into a new smaller array
                int[] subsetPixels = new int[regionWidth * regionHeight];
                for (int row = 0; row < regionHeight; row++) {
                    System.arraycopy(
                            pixels,
                            ((row + mRegion.top) * bitmapWidth) + mRegion.left,
                            subsetPixels,
                            row * regionWidth,
                            regionWidth);
                }
                return subsetPixels;
            }
        }

        /** Scale the bitmap down as needed. */
        private Bitmap scaleBitmapDown(Bitmap bitmap) {
            double scaleRatio = -1;

            if (mResizeArea > 0) {
                int bitmapArea = bitmap.getWidth() * bitmap.getHeight();
                if (bitmapArea > mResizeArea) {
                    scaleRatio = Math.sqrt(mResizeArea / (double) bitmapArea);
                }
            } else if (mResizeMaxDimension > 0) {
                int maxDimension = Math.max(bitmap.getWidth(), bitmap.getHeight());
                if (maxDimension > mResizeMaxDimension) {
                    scaleRatio = mResizeMaxDimension / (double) maxDimension;
                }
            }

            if (scaleRatio <= 0) {
                // Scaling has been disabled or not needed so just return the Bitmap
                return bitmap;
            }

            return Bitmap.createScaledBitmap(
                    bitmap,
                    (int) Math.ceil(bitmap.getWidth() * scaleRatio),
                    (int) Math.ceil(bitmap.getHeight() * scaleRatio),
                    false);
        }

    }
}


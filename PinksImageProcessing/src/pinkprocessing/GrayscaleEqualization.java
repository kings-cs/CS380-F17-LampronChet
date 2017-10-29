/**
 * 
 */
package pinkprocessing;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.jocl.CL;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_kernel;
import org.jocl.cl_mem;
import org.jocl.cl_program;

import algorithms.PixelModifier;
import parallel.JoclInitializer;

/**
 * Contains methods used for GrayscaleEqualizationModifier.
 * 
 * @author Chet Lampron;
 *
 */
public class GrayscaleEqualization {
	/** the possible number of color values. */
	private static final int NUM_OF_COLORS = 256;
	/** The calculated runtime. */
	private int calculatedRuntime;

	/**
	 * Calculates the histogram.
	 * 
	 * @param deviceManager
	 *            The device manager.
	 * @param sourceData
	 *            The image source data.
	 * @param theWorkSize
	 *            The calculated work size.
	 * @return The calculated histogram.
	 * @throws FileNotFoundException
	 *             Not thrown.
	 */
	public int[] calculateHistogram(JoclInitializer deviceManager, int[] sourceData, int theWorkSize)
			throws FileNotFoundException {
		int[] frequency = new int[NUM_OF_COLORS];

		Pointer ptrSource = Pointer.to(sourceData);
		Pointer ptrFreq = Pointer.to(frequency);

		cl_mem memSource = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * sourceData.length, ptrSource, null);
		cl_mem memFreq = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * frequency.length, ptrFreq, null);

		File kernelFile = new File("Kernels/GrayscaleEqualization/calculateHistogram");
		Scanner kernelScan = new Scanner(kernelFile);
		StringBuffer sourceBuffer = new StringBuffer();
		while (kernelScan.hasNextLine()) {
			sourceBuffer.append(kernelScan.nextLine());
			sourceBuffer.append("\n");
		}
		cl_program program = CL.clCreateProgramWithSource(deviceManager.getContext(), 1,
				new String[] { sourceBuffer.toString() }, null, null);

		CL.clBuildProgram(program, 0, null, null, null, null);

		int workSize = theWorkSize;

		long[] globalWorkSize = new long[] { sourceData.length };
		long[] localWorkSize = new long[] { workSize };
		deviceManager.createQueue();
		cl_kernel calculateKernel = CL.clCreateKernel(program, "calculate_histogram", null);

		CL.clSetKernelArg(calculateKernel, 0, Sizeof.cl_mem, Pointer.to(memSource));
		CL.clSetKernelArg(calculateKernel, 1, Sizeof.cl_mem, Pointer.to(memFreq));
		double startTime = System.nanoTime();
		CL.clEnqueueNDRangeKernel(deviceManager.getQueue(), calculateKernel, 1, null, globalWorkSize, localWorkSize, 0,
				null, null);
		calculatedRuntime += System.nanoTime() - startTime;

		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memFreq, CL.CL_TRUE, 0, frequency.length * Sizeof.cl_int,
				ptrFreq, 0, null, null);

		CL.clReleaseKernel(calculateKernel);
		CL.clReleaseProgram(program);
		CL.clReleaseMemObject(memFreq);
		CL.clReleaseMemObject(memSource);

		kernelScan.close();
		return frequency;
	}

	/**
	 * Distributes the cumulative frequency.
	 * 
	 * @param histogramResult
	 *            The calculated histogram.
	 * @return The distributed cumulative frequency.
	 */
	public int[] distributeCumulativeFrequency(int[] histogramResult) {
		int[] freqResult = new int[histogramResult.length];

		for (int i = 1; i <= freqResult.length - 1; i++) {
			freqResult[i] += histogramResult[i] + freqResult[i - 1];
		}
		return freqResult;
	}

	/**
	 * Calculates the ideal histogram.
	 * 
	 * @param cumulativeFrequencyResult
	 *            The distributed cumulative frequency.
	 * 
	 * @param numOfPixels
	 *            The number of pixels.
	 * @return The ideal histogram.
	 */
	public int[] calculateIdealizedHistogram(int[] cumulativeFrequencyResult, int numOfPixels) {
		int idealizedValue = numOfPixels / cumulativeFrequencyResult.length;
		int[] histogram = new int[cumulativeFrequencyResult.length];
		for (int i = 0; i < histogram.length; i++) {
			histogram[i] = idealizedValue;
		}
		if (numOfPixels % cumulativeFrequencyResult.length > 0) {
			histogram[(histogram.length - 1) / 2]++;
		}
		return histogram;
	}

	/**
	 * Designs the map.
	 * 
	 * @param cumulativeFrequencyResult
	 *            The calculated frequency.
	 * @param originalResult
	 *            The original images cumulative frequency distrobution.
	 * @return The map design.
	 */
	public int[] designMap(int[] cumulativeFrequencyResult, int[] originalResult) {
		int[] mapDesign = new int[originalResult.length];

		for (int i = 0; i < mapDesign.length; i++) {
			int original = originalResult[i];
			int resultIndex = 0;
			boolean isExact = false;
			int j = 0;
			int valueDifference = Integer.MAX_VALUE;
			while (j < cumulativeFrequencyResult.length && !isExact) {
				if (valueDifference == 0) {
					resultIndex = j;
					isExact = true;
				} else {
					int newDifference = Math.abs(original - cumulativeFrequencyResult[j]);
					if (newDifference < valueDifference && newDifference != 0) {
						valueDifference = newDifference;
						resultIndex = j;
					} else if (newDifference == 0) {
						valueDifference = newDifference;
						resultIndex = j;
						isExact = true;
					}
				}
				j++;
			}
			mapDesign[i] = resultIndex;
		}
		return mapDesign;
	}

	/**
	 * Gets the map from the mapDesign.
	 * 
	 * @param mapDesign
	 *            The map design.
	 * @param data
	 *            The original data.
	 * @return The map.
	 */
	public int[] getMap(int[] mapDesign, int[] data) {
		int[] map = new int[data.length];

		for (int i = 0; i < map.length; i++) {
			int pixel = data[i];
			int alpha = (pixel & PixelModifier.getAlphaMask()) >> PixelModifier.getAlphaOffset();
			int red = (pixel & PixelModifier.getRedMask()) >> PixelModifier.getRedOffset();
			int newVal = mapDesign[red];
			int newPixel = (alpha << PixelModifier.getAlphaOffset()) | (newVal << PixelModifier.getRedOffset()) | (newVal << PixelModifier.getBlueOffset())
					| (newVal << PixelModifier.getGreenOffset());
			map[i] = newPixel;
		}
		return map;
	}

	/**
	 * Gets the total runtime.
	 * 
	 * @return The runtime.
	 */
	public int getTime() {
		return calculatedRuntime;
	}

}

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
import testing.HillisSteeleScan;

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
	private long calculatedRuntime;
	/** The device manager. */
	private JoclInitializer deviceManager;
	/** The work size. */
	private int workSize;

	/**
	 * Calculates the histogram.
	 * 
	 * @param aDeviceManager
	 *            The device manager.
	 * @param sourceData
	 *            The image source data.
	 * @param theWorkSize
	 *            The calculated work size.
	 *            @param isOptimize Which kernel the method should call. 
	 * @return The calculated histogram.
	 * @throws FileNotFoundException
	 *             Not thrown.
	 */
	public int[] calculateHistogram(JoclInitializer aDeviceManager, int[] sourceData, int theWorkSize,
			boolean isOptimize) throws FileNotFoundException {
		int[] frequency = new int[NUM_OF_COLORS];
		this.deviceManager = aDeviceManager;
		workSize = theWorkSize;
		int[] offsets = { PixelModifier.getBlueOffset(), PixelModifier.getBlueMask() };
		Pointer ptrSource = Pointer.to(sourceData);
		Pointer ptrFreq = Pointer.to(frequency);
		Pointer ptrOffsets = Pointer.to(offsets);

		cl_mem memSource = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * sourceData.length, ptrSource, null);
		cl_mem memFreq = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * frequency.length, ptrFreq, null);
		cl_mem memOffsets = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * offsets.length, ptrOffsets, null);
		String kernelLocation = "";
		String kernelName = "";
		if (!isOptimize) {
			kernelLocation = "Kernels/GrayscaleEqualization/calculateHistogram";
			kernelName = "calculate_histogram";
		} else {
			kernelLocation = "Kernels/GrayscaleEqualization/calculateHistogramOptimized";
			kernelName = "calculate_histogram_optimized";
		}
		File kernelFile = new File(kernelLocation);
		Scanner kernelScan = new Scanner(kernelFile);
		StringBuffer sourceBuffer = new StringBuffer();
		while (kernelScan.hasNextLine()) {
			sourceBuffer.append(kernelScan.nextLine());
			sourceBuffer.append("\n");
		}
		cl_program program = CL.clCreateProgramWithSource(deviceManager.getContext(), 1,
				new String[] { sourceBuffer.toString() }, null, null);

		CL.clBuildProgram(program, 0, null, null, null, null);

		long[] globalWorkSize = new long[] { sourceData.length };
		long[] localWorkSize = new long[] { workSize };
		deviceManager.createQueue();
		cl_kernel calculateKernel = CL.clCreateKernel(program, kernelName, null);

		CL.clSetKernelArg(calculateKernel, 0, Sizeof.cl_mem, Pointer.to(memSource));
		CL.clSetKernelArg(calculateKernel, 1, Sizeof.cl_mem, Pointer.to(memFreq));
		CL.clSetKernelArg(calculateKernel, 2, Sizeof.cl_mem, Pointer.to(memOffsets));
		if(isOptimize) {
			CL.clSetKernelArg(calculateKernel, 3, Sizeof.cl_float * workSize, null);
		}
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
		CL.clReleaseMemObject(memOffsets);
		kernelScan.close();
		return frequency;
	}

	/**
	 * Distributes the cumulative frequency.
	 * 
	 * @param histogramResult
	 *            The calculated histogram.
	 * @return The distributed cumulative frequency.
	 * @throws FileNotFoundException
	 *             Not thrown.
	 */
	public int[] distributeCumulativeFrequency(int[] histogramResult) throws FileNotFoundException {
		int[] freqResult = new int[histogramResult.length];

		// for (int i = 1; i <= freqResult.length - 1; i++) {
		// freqResult[i] += histogramResult[i] + freqResult[i - 1];
		// }
		HillisSteeleScan scan = new HillisSteeleScan(deviceManager);
		calculatedRuntime += scan.scan(histogramResult, freqResult);
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
	 * @param workGroups
	 *            The specific number of work groups for this method.
	 * @return The ideal histogram.
	 * @throws FileNotFoundException
	 *             Not thrown.
	 */
	public int[] calculateIdealizedHistogram(int[] cumulativeFrequencyResult, int numOfPixels, int workGroups)
			throws FileNotFoundException {
		// int idealizedValue = numOfPixels / cumulativeFrequencyResult.length;
		int[] histogram = new int[cumulativeFrequencyResult.length];
		int[] dimensions = { numOfPixels, cumulativeFrequencyResult.length, histogram.length };
		Pointer ptrHistogram = Pointer.to(histogram);
		Pointer ptrDimensions = Pointer.to(dimensions);

		cl_mem memHistogram = CL.clCreateBuffer(deviceManager.getContext(),
				CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * histogram.length, ptrHistogram, null);
		cl_mem memDimensions = CL.clCreateBuffer(deviceManager.getContext(),
				CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * dimensions.length, ptrDimensions,
				null);

		File kernelFile = new File("Kernels/GrayscaleEqualization/calculateIdealHistogram");
		Scanner kernelScan = new Scanner(kernelFile);
		StringBuffer sourceBuffer = new StringBuffer();
		while (kernelScan.hasNextLine()) {
			sourceBuffer.append(kernelScan.nextLine());
			sourceBuffer.append("\n");
		}
		cl_program program = CL.clCreateProgramWithSource(deviceManager.getContext(), 1,
				new String[] { sourceBuffer.toString() }, null, null);

		CL.clBuildProgram(program, 0, null, null, null, null);

		long[] globalWorkSize = new long[] { cumulativeFrequencyResult.length };
		long[] localWorkSize = new long[] { workGroups };
		deviceManager.createQueue();
		cl_kernel idealKernel = CL.clCreateKernel(program, "calculate_ideal_histogram", null);

		CL.clSetKernelArg(idealKernel, 0, Sizeof.cl_mem, Pointer.to(memHistogram));
		CL.clSetKernelArg(idealKernel, 1, Sizeof.cl_mem, Pointer.to(memDimensions));
		double startTime = System.nanoTime();
		CL.clEnqueueNDRangeKernel(deviceManager.getQueue(), idealKernel, 1, null, globalWorkSize, localWorkSize, 0,
				null, null);
		calculatedRuntime += System.nanoTime() - startTime;

		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memHistogram, CL.CL_TRUE, 0, histogram.length * Sizeof.cl_int,
				ptrHistogram, 0, null, null);

		CL.clReleaseKernel(idealKernel);
		CL.clReleaseProgram(program);
		CL.clReleaseMemObject(memHistogram);
		CL.clReleaseMemObject(memDimensions);

		kernelScan.close();

		// for (int i = 0; i < histogram.length; i++) {
		// histogram[i] = idealizedValue;
		// }
		// int modVal = numOfPixels % cumulativeFrequencyResult.length;
		// if (modVal == 1) {
		// histogram[(histogram.length - 1) / 2]++;
		// } else if (modVal > 1) {
		// int index = ((histogram.length - 1) / 2) - (modVal / 2);
		// while (modVal >= 0) {
		// histogram[index]++;
		// index++;
		// modVal--;
		// }
		// }

		return histogram;
	}

	/**
	 * Designs the map.
	 * 
	 * @param cumulativeFrequencyResult
	 *            The calculated frequency.
	 * @param originalResult
	 *            The original images cumulative frequency distribution.
	 * @param workGroups
	 *            The specialized workSize for this method.
	 * @return The map design.
	 * @throws FileNotFoundException
	 *             Not thrown.
	 */
	public int[] designMap(int[] cumulativeFrequencyResult, int[] originalResult, int workGroups)
			throws FileNotFoundException {
		int[] mapDesign = new int[originalResult.length];
		int[] dimensions = { cumulativeFrequencyResult.length, Integer.MAX_VALUE };
		Pointer ptrSource = Pointer.to(originalResult);
		Pointer ptrDimensions = Pointer.to(dimensions);
		Pointer ptrResult = Pointer.to(mapDesign);
		Pointer ptrCumulative = Pointer.to(cumulativeFrequencyResult);

		cl_mem memSource = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * originalResult.length, ptrSource, null);
		cl_mem memDimensions = CL.clCreateBuffer(deviceManager.getContext(),
				CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * dimensions.length, ptrDimensions,
				null);
		cl_mem memResult = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * mapDesign.length, ptrResult, null);
		cl_mem memCumulative = CL.clCreateBuffer(deviceManager.getContext(),
				CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * cumulativeFrequencyResult.length,
				ptrCumulative, null);

		File kernelFile = new File("Kernels/GrayscaleEqualization/designMap");
		Scanner kernelScan = new Scanner(kernelFile);
		StringBuffer sourceBuffer = new StringBuffer();
		while (kernelScan.hasNextLine()) {
			sourceBuffer.append(kernelScan.nextLine());
			sourceBuffer.append("\n");
		}
		cl_program program = CL.clCreateProgramWithSource(deviceManager.getContext(), 1,
				new String[] { sourceBuffer.toString() }, null, null);

		CL.clBuildProgram(program, 0, null, null, null, null);

		long[] globalWorkSize = new long[] { originalResult.length };
		long[] localWorkSize = new long[] { workGroups };
		deviceManager.createQueue();
		cl_kernel idealKernel = CL.clCreateKernel(program, "design_map", null);

		CL.clSetKernelArg(idealKernel, 0, Sizeof.cl_mem, Pointer.to(memSource));
		CL.clSetKernelArg(idealKernel, 1, Sizeof.cl_mem, Pointer.to(memResult));
		CL.clSetKernelArg(idealKernel, 2, Sizeof.cl_mem, Pointer.to(memCumulative));
		CL.clSetKernelArg(idealKernel, 3, Sizeof.cl_mem, Pointer.to(memDimensions));
		double startTime = System.nanoTime();
		CL.clEnqueueNDRangeKernel(deviceManager.getQueue(), idealKernel, 1, null, globalWorkSize, localWorkSize, 0,
				null, null);
		calculatedRuntime += System.nanoTime() - startTime;

		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memResult, CL.CL_TRUE, 0, mapDesign.length * Sizeof.cl_int,
				ptrResult, 0, null, null);

		CL.clReleaseKernel(idealKernel);
		CL.clReleaseProgram(program);
		CL.clReleaseMemObject(memSource);
		CL.clReleaseMemObject(memDimensions);

		kernelScan.close();

		// for (int i = 0; i < mapDesign.length; i++) {
		// int original = originalResult[i];
		// int resultIndex = 0;
		// boolean isExact = false;
		// int j = 0;
		// int valueDifference = Integer.MAX_VALUE;
		// while (j < cumulativeFrequencyResult.length && !isExact) {
		// if (valueDifference == 0) {
		// resultIndex = j;
		// isExact = true;
		// } else {
		// int newDifference = Math.abs(original - cumulativeFrequencyResult[j]);
		// if (newDifference < valueDifference && newDifference != 0) {
		// valueDifference = newDifference;
		// resultIndex = j;
		// } else if (newDifference == 0) {
		// valueDifference = newDifference;
		// resultIndex = j;
		// isExact = true;
		// }
		// }
		// j++;
		// }
		// mapDesign[i] = resultIndex;
		// }
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
	 * @throws FileNotFoundException
	 *             Not thrown.
	 */
	public int[] getMap(int[] mapDesign, int[] data) throws FileNotFoundException {
		int[] map = new int[data.length];
		int[] offsets = { PixelModifier.getBlueOffset(), PixelModifier.getBlueMask(), PixelModifier.getAlphaOffset(),
				PixelModifier.getAlphaMask(), PixelModifier.getGreenOffset(), PixelModifier.getRedOffset() };

		Pointer ptrSource = Pointer.to(data);
		Pointer ptrDimensions = Pointer.to(offsets);
		Pointer ptrResult = Pointer.to(map);
		Pointer ptrDesign = Pointer.to(mapDesign);

		cl_mem memSource = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * data.length, ptrSource, null);
		cl_mem memDimensions = CL.clCreateBuffer(deviceManager.getContext(),
				CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * offsets.length, ptrDimensions, null);
		cl_mem memResult = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * map.length, ptrResult, null);
		cl_mem memDesign = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * mapDesign.length, ptrDesign, null);

		File kernelFile = new File("Kernels/GrayscaleEqualization/getMap");
		Scanner kernelScan = new Scanner(kernelFile);
		StringBuffer sourceBuffer = new StringBuffer();
		while (kernelScan.hasNextLine()) {
			sourceBuffer.append(kernelScan.nextLine());
			sourceBuffer.append("\n");
		}
		cl_program program = CL.clCreateProgramWithSource(deviceManager.getContext(), 1,
				new String[] { sourceBuffer.toString() }, null, null);

		CL.clBuildProgram(program, 0, null, null, null, null);

		long[] globalWorkSize = new long[] { data.length };
		long[] localWorkSize = new long[] { workSize };
		deviceManager.createQueue();
		cl_kernel idealKernel = CL.clCreateKernel(program, "get_map", null);

		CL.clSetKernelArg(idealKernel, 0, Sizeof.cl_mem, Pointer.to(memSource));
		CL.clSetKernelArg(idealKernel, 1, Sizeof.cl_mem, Pointer.to(memResult));
		CL.clSetKernelArg(idealKernel, 2, Sizeof.cl_mem, Pointer.to(memDesign));
		CL.clSetKernelArg(idealKernel, 3, Sizeof.cl_mem, Pointer.to(memDimensions));
		double startTime = System.nanoTime();
		CL.clEnqueueNDRangeKernel(deviceManager.getQueue(), idealKernel, 1, null, globalWorkSize, localWorkSize, 0,
				null, null);
		calculatedRuntime += System.nanoTime() - startTime;

		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memResult, CL.CL_TRUE, 0, map.length * Sizeof.cl_int,
				ptrResult, 0, null, null);

		CL.clReleaseKernel(idealKernel);
		CL.clReleaseProgram(program);
		CL.clReleaseMemObject(memSource);
		CL.clReleaseMemObject(memDimensions);

		kernelScan.close();
		// for (int i = 0; i < map.length; i++) {
		// int pixel = data[i];
		// int alpha = (pixel & PixelModifier.getAlphaMask()) >>
		// PixelModifier.getAlphaOffset();
		// int blue = (pixel & PixelModifier.getBlueMask()) >>
		// PixelModifier.getBlueOffset();
		// int newVal = mapDesign[blue];
		// int newPixel = (alpha << PixelModifier.getAlphaOffset() | (newVal <<
		// PixelModifier.getBlueOffset()));
		// map[i] = newPixel;
		// }
		return map;
	}

	/**
	 * Gets the total runtime.
	 * 
	 * @return The runtime.
	 */
	public long getTime() {
		return calculatedRuntime;
	}

	/**
	 * Sets the device manager.
	 * 
	 * @param deviceManager2
	 *            The device manager.
	 */
	public void setDeviceManager(JoclInitializer deviceManager2) {
		deviceManager = deviceManager2;
	}

	/**
	 * Sets the work size.
	 * 
	 * @param workSize2
	 *            The desired workSize.
	 */
	public void setWorkSize(int workSize2) {
		workSize = workSize2;
	}

}

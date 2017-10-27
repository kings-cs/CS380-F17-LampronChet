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
		// Set up and run the separate channels kernel.
		cl_kernel calculateKernel = CL.clCreateKernel(program, "calculate_histogram", null);

		CL.clSetKernelArg(calculateKernel, 0, Sizeof.cl_mem, ptrSource);
		CL.clSetKernelArg(calculateKernel, 1, Sizeof.cl_mem, ptrFreq);
		double startTime = System.nanoTime();
		CL.clEnqueueNDRangeKernel(deviceManager.getQueue(), calculateKernel, 1, null, globalWorkSize, localWorkSize, 0,
				null, null);
		calculatedRuntime += System.nanoTime() - startTime;
		
		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memFreq, CL.CL_TRUE, 0, frequency.length * Sizeof.cl_float,
				ptrFreq, 0, null, null);
		
		CL.clReleaseKernel(calculateKernel);
		CL.clReleaseMemObject(memFreq);
		CL.clReleaseMemObject(memSource);
		CL.clReleaseProgram(program);

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
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Calculates the ideal histogram.
	 * 
	 * @param cumulativeFrequencyResult
	 *            The distributed cumulative frequency.
	 * @return The ideal histogram.
	 */
	public int[] calculateIdealizedHistogram(int[] cumulativeFrequencyResult) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Designs the map.
	 * 
	 * @param cumulativeFrequencyResult
	 *            The calculated frequency.
	 * @return The map design.
	 */
	public int[] designMap(int[] cumulativeFrequencyResult) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Gets the map from the mapDesign.
	 * 
	 * @param mapDesign
	 *            The map design.
	 * @return The map.
	 */
	public int[] getMap(int[] mapDesign) {
		// TODO Auto-generated method stub
		return null;
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

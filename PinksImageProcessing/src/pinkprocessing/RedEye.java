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
import testing.BlellochScan;

/**
 * The red eye removal helper methods.
 * 
 * @author Chet Lampron.
 *
 */
public class RedEye {
	private static final int RED_INDEX = 0;
	private static final int GREEN_INDEX = 1;
	private static final int BLUE_INDEX = 2;

	/** The device manager. */
	private JoclInitializer deviceManager;

	/** The calculated time. */
	private long calculatedTime;
	/** The worksize. */
	private int workSize;

	private int[] redArray;

	private int[] greenArray;

	private int[] blueArray;

	/**
	 * Constructs a RedEye helper object.
	 * 
	 * @param theDeviceManager
	 *            The device manager being used.
	 */
	public RedEye(JoclInitializer theDeviceManager) {
		deviceManager = theDeviceManager;
		calculatedTime = 0;
	}

	public void splitChannels(int[] data, int red[], int[] green, int[] blue) throws FileNotFoundException {
		int[] alphaArray = new int[data.length];
		Pointer ptrSource = Pointer.to(data);
		Pointer ptrRed = Pointer.to(red);
		Pointer ptrBlue = Pointer.to(blue);
		Pointer ptrGreen = Pointer.to(green);

		Pointer ptrAlpha = Pointer.to(alphaArray);

		cl_mem memSource = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * data.length, ptrSource, null);
		cl_mem memRed = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * red.length, ptrRed, null);
		cl_mem memBlue = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * blue.length, ptrBlue, null);
		cl_mem memGreen = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * green.length, ptrGreen, null);
		cl_mem memAlpha = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * alphaArray.length, ptrAlpha, null);

		File kernelFile = new File("Kernels/BlurKernel");
		Scanner kernelScan = new Scanner(kernelFile);
		StringBuffer sourceBuffer = new StringBuffer();
		while (kernelScan.hasNextLine()) {
			sourceBuffer.append(kernelScan.nextLine());
			sourceBuffer.append("\n");
		}
		cl_program program = CL.clCreateProgramWithSource(deviceManager.getContext(), 1,
				new String[] { sourceBuffer.toString() }, null, null);

		CL.clBuildProgram(program, 0, null, null, null, null);

		int workSize = PixelModifier.getWorkSize(deviceManager, data);

		long[] globalWorkSize = new long[] { data.length };
		long[] localWorkSize = new long[] { workSize };
		deviceManager.createQueue();
		// Set up and run the separate channels kernel.
		cl_kernel separateKernel = CL.clCreateKernel(program, "separateChannels", null);

		CL.clSetKernelArg(separateKernel, 0, Sizeof.cl_mem, Pointer.to(memSource));
		CL.clSetKernelArg(separateKernel, 1, Sizeof.cl_mem, Pointer.to(memRed));
		CL.clSetKernelArg(separateKernel, 2, Sizeof.cl_mem, Pointer.to(memBlue));
		CL.clSetKernelArg(separateKernel, 3, Sizeof.cl_mem, Pointer.to(memGreen));
		CL.clSetKernelArg(separateKernel, 4, Sizeof.cl_mem, Pointer.to(memAlpha));
		double startTime = System.nanoTime();
		CL.clEnqueueNDRangeKernel(deviceManager.getQueue(), separateKernel, 1, null, globalWorkSize, localWorkSize, 0,
				null, null);
		double afterOne = System.nanoTime() - startTime;

		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memRed, CL.CL_TRUE, 0, red.length * Sizeof.cl_float, ptrRed, 0,
				null, null);
		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memBlue, CL.CL_TRUE, 0, blue.length * Sizeof.cl_float, ptrBlue,
				0, null, null);
		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memGreen, CL.CL_TRUE, 0, green.length * Sizeof.cl_float,
				ptrGreen, 0, null, null);
	}

	/**
	 * Reduces the values in an array.
	 * 
	 * @param data
	 *            The data.
	 * @param result
	 *            The result array.
	 * @param resultIndex
	 *            Whether it is red, blue, or green.
	 * @throws FileNotFoundException
	 *             Not thrown.
	 */
	public void reduce(int[] data, int[] result, int resultIndex) throws FileNotFoundException {
		int[] paddedData = BlellochScan.padArray(data);
		workSize = PixelModifier.getWorkSize(deviceManager, paddedData);
		Pointer ptrData = Pointer.to(paddedData);

		cl_mem memData = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * paddedData.length, ptrData, null);

		File kernelFile = new File("Kernels/RedEyeKernel");
		Scanner kernelScan = new Scanner(kernelFile);
		StringBuffer sourceBuffer = new StringBuffer();
		while (kernelScan.hasNextLine()) {
			sourceBuffer.append(kernelScan.nextLine());
			sourceBuffer.append("\n");
		}
		cl_program program = CL.clCreateProgramWithSource(deviceManager.getContext(), 1,
				new String[] { sourceBuffer.toString() }, null, null);

		CL.clBuildProgram(program, 0, null, null, null, null);

		long[] globalWorkSize = new long[] { paddedData.length };
		long[] localWorkSize = new long[] { workSize };
		int accumSize = paddedData.length / workSize;
		int[] accumulator = new int[accumSize];
		Pointer ptrAccum = Pointer.to(accumulator);
		cl_mem memAccum = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * accumulator.length, ptrAccum, null);

		cl_kernel averageKernel = CL.clCreateKernel(program, "calculateAverage", null);

		CL.clSetKernelArg(averageKernel, 0, Sizeof.cl_mem, Pointer.to(memData));
		CL.clSetKernelArg(averageKernel, 1, Sizeof.cl_mem, Pointer.to(memAccum));
		CL.clSetKernelArg(averageKernel, 2, Sizeof.cl_int * localWorkSize[0], null);
		long startTime = System.nanoTime();
		CL.clEnqueueNDRangeKernel(deviceManager.getQueue(), averageKernel, 1, null, globalWorkSize, localWorkSize, 0,
				null, null);
		long calculatedRuntime = System.nanoTime() - startTime;

		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memAccum, CL.CL_TRUE, 0, accumulator.length * Sizeof.cl_float,
				ptrAccum, 0, null, null);
		int value = 0;
		if (accumulator.length > 1) {
			System.out.println("accum length " + accumulator.length + " data length is " + data.length);
			//value = accumulator[0];
			reduce(accumulator, result, resultIndex);
		}

		System.out.println("Accumulator value: " + accumulator[0]);
		// System.out.println(accumulator[1]);
		result[resultIndex] = accumulator[0];

	}

	/**
	 * Calculates the average of the temple.
	 * 
	 * @param data
	 *            The data of the image.
	 * @return The average.
	 * @throws FileNotFoundException
	 *             Not thrown;
	 */
	public int[] calculateTemplateAverage(int[] data) throws FileNotFoundException {
		workSize = PixelModifier.getWorkSize(deviceManager, data);
		int redIndex = 0;
		int greenIndex = 1;
		int blueIndex = 2;
		int[] resultData = new int[3];

		redArray = new int[data.length];
		blueArray = new int[data.length];
		greenArray = new int[data.length];
		splitChannels(data, redArray, greenArray, blueArray);
		reduce(redArray, resultData, redIndex);
		reduce(greenArray, resultData, greenIndex);
		reduce(blueArray, resultData, blueIndex);
		System.out.println("length of data: " + data.length);
		for (int i = 0; i < resultData.length; i++) {
			resultData[i] = resultData[i] / data.length;
		}

		// for (int i = 0; i < data.length; i++) {
		// int pixel = data[i];
		// int alpha = (pixel & PixelModifier.getAlphaMask()) >>
		// PixelModifier.getAlphaOffset();
		// int red = (pixel & PixelModifier.getRedMask()) >>
		// PixelModifier.getRedOffset();
		// int green = (pixel & PixelModifier.getGreenMask()) >>
		// PixelModifier.getGreenOffset();
		// int blue = (pixel & PixelModifier.getBlueMask()) >>
		// PixelModifier.getBlueOffset();
		// redTotal += red;
		// blueTotal += blue;
		// greenTotal += green;
		// }
		// int redAvg = redTotal / data.length;
		// int greenAvg = greenTotal / data.length;
		// int blueAvg = blueTotal / data.length;
		// int[] averages = { redAvg, greenAvg, blueAvg };
		return resultData;
	}

	public int[] sumDifferenceTemplate(int[] averages) {
		int redSum = 0;
		int greenSum = 0;
		int blueSum = 0;

		for (int i = 0; i < redArray.length; i++) {
			int difference = redArray[i] - averages[RED_INDEX];
			redSum += difference;
		}
		for (int i = 0; i < greenArray.length; i++) {
			int difference = greenArray[i] - averages[GREEN_INDEX];
			greenSum += difference;
		}
		for (int i = 0; i < blueArray.length; i++) {
			int difference = blueArray[i] - averages[BLUE_INDEX];
			blueSum += difference;
		}
		int[] sumOfDifferences = { redSum, greenSum, blueSum };
		return sumOfDifferences;
	}
}

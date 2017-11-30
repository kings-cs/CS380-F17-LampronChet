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
	/** Standard red index for value arrays. */
	private static final int RED_INDEX = 0;
	/** Standard green index for value arrays. */
	private static final int GREEN_INDEX = 1;
	/** Standard blue index for value arrays. */
	private static final int BLUE_INDEX = 2;

	/** The device manager. */
	private JoclInitializer deviceManager;

	/** The calculated time. */
	private long calculatedTime;
	/** The worksize. */
	private int workSize;
	/** Stores the red channel values. */
	private int[] redArray;
	/** Stores the green channel values. */
	private int[] greenArray;
	/** Stores the blue channel values. */
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

	/**
	 * Splits data into RGB channels.
	 * 
	 * @param data
	 *            The image data.
	 * @param red
	 *            The red channel.
	 * @param green
	 *            The green channel.
	 * @param blue
	 *            The blue channel.
	 * @throws FileNotFoundException
	 *             Not thrown.
	 */
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
		calculatedTime += System.nanoTime() - startTime;

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
		calculatedTime += System.nanoTime() - startTime;

		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memAccum, CL.CL_TRUE, 0, accumulator.length * Sizeof.cl_float,
				ptrAccum, 0, null, null);
		// int value = 0;
		if (accumulator.length > 1) {
			// System.out.println("accum length " + accumulator.length + " data length is "
			// + data.length);
			// value = accumulator[0];
			reduce(accumulator, result, resultIndex);
		} else {
			// System.out.println("Accumulator value: " + accumulator[0]);
			// System.out.println(accumulator[1]);
			result[resultIndex] = accumulator[0];
		}

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
		// System.out.println("length of data: " + data.length);
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

	/**
	 * Gets the sum of differences using calculated averages.
	 * 
	 * @param averages
	 *            The calculated averages.
	 * @return The sums of the differences of each channel.
	 * @throws FileNotFoundException
	 *             Not thrown.
	 */
	public int[] sumDifferenceTemplate(int[] averages, int[] redDifferences, int[] greenDifferences, int[] blueDifferences) throws FileNotFoundException {
		// int redSum = 0;
		// int greenSum = 0;
		// int blueSum = 0;
		int[] currentAverage = { averages[0] };
		int[] finalSums = new int[3];
		int[] currentResult = new int[redArray.length];
		
		Pointer ptrSource = Pointer.to(redArray);
		Pointer ptrAverage = Pointer.to(currentAverage);
		Pointer ptrResult = Pointer.to(currentResult);
		Pointer ptrDifferences = Pointer.to(redDifferences);

		cl_mem memSource = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * redArray.length, ptrSource, null);

		cl_mem memAverage = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * currentAverage.length, ptrAverage, null);
		cl_mem memResult = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * currentResult.length, ptrResult, null);
		cl_mem memDifferences = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * redDifferences.length, ptrDifferences, null);

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
		workSize = PixelModifier.getWorkSize(deviceManager, redArray);

		long[] globalWorkSize = new long[] { redArray.length };
		long[] localWorkSize = new long[] { workSize };

		cl_kernel differenceKernel = CL.clCreateKernel(program, "calculateDifference", null);

		CL.clSetKernelArg(differenceKernel, 0, Sizeof.cl_mem, Pointer.to(memSource));
		CL.clSetKernelArg(differenceKernel, 1, Sizeof.cl_mem, Pointer.to(memAverage));
		CL.clSetKernelArg(differenceKernel, 2, Sizeof.cl_mem, Pointer.to(memResult));
		CL.clSetKernelArg(differenceKernel, 3, Sizeof.cl_mem, Pointer.to(memDifferences));
		long startTime = System.nanoTime();
		CL.clEnqueueNDRangeKernel(deviceManager.getQueue(), differenceKernel, 1, null, globalWorkSize, localWorkSize, 0,
				null, null);
		calculatedTime += System.nanoTime() - startTime;

		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memResult, CL.CL_TRUE, 0,
				currentResult.length * Sizeof.cl_float, ptrResult, 0, null, null);
		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memDifferences, CL.CL_TRUE, 0,
				redDifferences.length * Sizeof.cl_float, ptrDifferences, 0, null, null);
		reduce(currentResult, finalSums, RED_INDEX);

		currentAverage[0] = averages[GREEN_INDEX];
		ptrAverage = Pointer.to(currentAverage);
		memAverage = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * currentAverage.length, ptrAverage, null);

		ptrSource = Pointer.to(greenArray);
		memSource = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * greenArray.length, ptrSource, null);
		
		ptrDifferences = Pointer.to(greenDifferences);
		memDifferences = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * greenDifferences.length, ptrDifferences, null);
		
		CL.clSetKernelArg(differenceKernel, 0, Sizeof.cl_mem, Pointer.to(memSource));
		CL.clSetKernelArg(differenceKernel, 1, Sizeof.cl_mem, Pointer.to(memAverage));
		CL.clSetKernelArg(differenceKernel, 3, Sizeof.cl_mem, Pointer.to(memDifferences));

		startTime = System.nanoTime();
		CL.clEnqueueNDRangeKernel(deviceManager.getQueue(), differenceKernel, 1, null, globalWorkSize, localWorkSize, 0,
				null, null);
		calculatedTime += System.nanoTime() - startTime;

		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memResult, CL.CL_TRUE, 0,
				currentResult.length * Sizeof.cl_float, ptrResult, 0, null, null);
		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memDifferences, CL.CL_TRUE, 0,
				greenDifferences.length * Sizeof.cl_float, ptrDifferences, 0, null, null);
		reduce(currentResult, finalSums, GREEN_INDEX);

		currentAverage[0] = averages[BLUE_INDEX];
		ptrAverage = Pointer.to(currentAverage);
		memAverage = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * currentAverage.length, ptrAverage, null);

		ptrSource = Pointer.to(blueArray);
		memSource = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * blueArray.length, ptrSource, null);
		ptrDifferences = Pointer.to(blueDifferences);
		memDifferences = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * blueDifferences.length, ptrDifferences, null);
		
		CL.clSetKernelArg(differenceKernel, 0, Sizeof.cl_mem, Pointer.to(memSource));
		CL.clSetKernelArg(differenceKernel, 1, Sizeof.cl_mem, Pointer.to(memAverage));
		CL.clSetKernelArg(differenceKernel, 3, Sizeof.cl_mem, Pointer.to(memDifferences));

		startTime = System.nanoTime();
		CL.clEnqueueNDRangeKernel(deviceManager.getQueue(), differenceKernel, 1, null, globalWorkSize, localWorkSize, 0,
				null, null);
		calculatedTime += System.nanoTime() - startTime;

		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memResult, CL.CL_TRUE, 0,
				currentResult.length * Sizeof.cl_float, ptrResult, 0, null, null);
		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memDifferences, CL.CL_TRUE, 0,
				blueDifferences.length * Sizeof.cl_float, ptrDifferences, 0, null, null);
		reduce(currentResult, finalSums, BLUE_INDEX);
		// for (int i = 0; i < redArray.length; i++) {
		// int difference = (int) Math.pow(redArray[i] - averages[RED_INDEX], 2);
		// redSum += difference;
		// }
		// for (int i = 0; i < greenArray.length; i++) {
		// int difference = (int) Math.pow(greenArray[i] - averages[GREEN_INDEX], 2);
		// greenSum += difference;
		// }
		// for (int i = 0; i < blueArray.length; i++) {
		// int difference = (int) Math.pow(blueArray[i] - averages[BLUE_INDEX], 2);
		// blueSum += difference;
		// }
		// int[] sumOfDifferences = { redSum, greenSum, blueSum };
		return finalSums;
	}
	
	public int[] getNcc(int[] data, int[] tempSumDiff, int[] tempDiffRed, int[] tempDiffGreen, int[] tempDiffBlue, int[] dimensions) throws FileNotFoundException{
		int[] nccValues = new int[data.length];
		int[] red = new int[data.length];
		int[] green = new int[data.length];
		int[] blue = new int[data.length];
		splitChannels(data, red, green, blue);
		
		Pointer ptrRed = Pointer.to(red);
		Pointer ptrGreen = Pointer.to(green);
		Pointer ptrBlue = Pointer.to(blue);
		Pointer ptrSumDiff = Pointer.to(tempSumDiff);
		Pointer ptrDiffRed = Pointer.to(tempDiffRed);
		Pointer ptrDiffGreen = Pointer.to(tempDiffGreen);
		Pointer ptrDiffBlue = Pointer.to(tempDiffBlue);
		Pointer ptrResult = Pointer.to(nccValues);
		Pointer ptrDimensions = Pointer.to(dimensions);
		
		cl_mem memRed = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * red.length, ptrRed, null);
		cl_mem memGreen = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * green.length, ptrGreen, null);
		cl_mem memBlue = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * blue.length, ptrBlue, null);
		cl_mem memSumDiff = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * tempSumDiff.length, ptrSumDiff, null);
		cl_mem memDiffRed = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * tempDiffRed.length, ptrDiffRed, null);
		cl_mem memDiffGreen = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * tempDiffGreen.length, ptrDiffGreen, null);
		cl_mem memDiffBlue = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * tempDiffBlue.length, ptrDiffBlue, null);
		cl_mem memResult = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * nccValues.length, ptrResult, null);
		cl_mem memDimensions = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * dimensions.length, ptrDimensions, null);
		
		File kernelFile = new File("Kernels/RedEyeKernel");
		Scanner kernelScan = new Scanner(kernelFile);
		StringBuffer sourceBuffer = new StringBuffer();
		while (kernelScan.hasNextLine()) {
			sourceBuffer.append(kernelScan.nextLine());
			sourceBuffer.append("\n");
		}
		cl_program program = CL.clCreateProgramWithSource(deviceManager.getContext(), 1,
				new String[] { sourceBuffer.toString() }, null, null);
		workSize = PixelModifier.getWorkSize(deviceManager, red);

		long[] globalWorkSize = new long[] { redArray.length };
		long[] localWorkSize = new long[] { workSize };

		cl_kernel differenceKernel = CL.clCreateKernel(program, "calculateNcc", null);

		CL.clSetKernelArg(differenceKernel, 0, Sizeof.cl_mem, Pointer.to(memRed));
		CL.clSetKernelArg(differenceKernel, 1, Sizeof.cl_mem, Pointer.to(memGreen));
		CL.clSetKernelArg(differenceKernel, 2, Sizeof.cl_mem, Pointer.to(memBlue));
		CL.clSetKernelArg(differenceKernel, 3, Sizeof.cl_mem, Pointer.to(memDimensions));
		CL.clSetKernelArg(differenceKernel, 4, Sizeof.cl_mem, Pointer.to(memDiffRed));
		CL.clSetKernelArg(differenceKernel, 5, Sizeof.cl_mem, Pointer.to(memDiffGreen));
		CL.clSetKernelArg(differenceKernel, 6, Sizeof.cl_mem, Pointer.to(memDiffBlue));
		CL.clSetKernelArg(differenceKernel, 7, Sizeof.cl_mem, Pointer.to(memSumDiff));
		CL.clSetKernelArg(differenceKernel, 8, Sizeof.cl_mem, Pointer.to(memResult));
		long startTime = System.nanoTime();
		CL.clEnqueueNDRangeKernel(deviceManager.getQueue(), differenceKernel, 1, null, globalWorkSize, localWorkSize, 0,
				null, null);
		calculatedTime += System.nanoTime() - startTime;

		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memResult, CL.CL_TRUE, 0,
				nccValues.length * Sizeof.cl_float, ptrResult, 0, null, null);
		
		return nccValues;
	}
	
	public int findSmallest(int[] data){
		int smallest = Integer.MAX_VALUE;
		for(int i = 0; i < data.length; i++){
			if(data[i] < smallest){
				smallest = data[i];
			}
		}
		return smallest;
	}
}
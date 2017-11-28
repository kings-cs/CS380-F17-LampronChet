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
 * The red eye removal helper methods.
 * 
 * @author Chet Lampron.
 *
 */
public class RedEye {
	/** The device manager. */
	private JoclInitializer deviceManager;

	/**
	 * Constructs a RedEye helper object.
	 * 
	 * @param theDeviceManager
	 *            The device manager being used.
	 */
	public RedEye(JoclInitializer theDeviceManager) {
		deviceManager = theDeviceManager;
	}

	/**
	 * Calculates the average of the temple.
	 * 
	 * @param data
	 *            The data of the image.
	 * @return The average.
	 * @throws FileNotFoundException Not thrown;
	 */
	public int[] calculateTemplateAverage(int[] data) throws FileNotFoundException {
		int redTotal = 0;
		int blueTotal = 0;
		int greenTotal = 0;
		
		int[] resultData = new int[3];

		int[] redArray = new int[data.length];
		int[] blueArray = new int[data.length];
		int[] greenArray = new int[data.length];
		int[] alphaArray = new int[data.length];
		Pointer ptrSource = Pointer.to(data);
		Pointer ptrResult = Pointer.to(resultData);
		Pointer ptrRed = Pointer.to(redArray);
		Pointer ptrBlue = Pointer.to(blueArray);
		Pointer ptrGreen = Pointer.to(greenArray);
		Pointer ptrAlpha = Pointer.to(alphaArray);
		cl_mem memSource = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * data.length, ptrSource, null);
		cl_mem memResult = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_WRITE_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * resultData.length, ptrResult, null);
		cl_mem memRed = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * redArray.length, ptrRed, null);
		cl_mem memBlue = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * blueArray.length, ptrBlue, null);
		cl_mem memGreen = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * greenArray.length, ptrGreen, null);
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

		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memRed, CL.CL_TRUE, 0, redArray.length * Sizeof.cl_float,
				ptrRed, 0, null, null);
		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memBlue, CL.CL_TRUE, 0, blueArray.length * Sizeof.cl_float,
				ptrBlue, 0, null, null);
		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memGreen, CL.CL_TRUE, 0, greenArray.length * Sizeof.cl_float,
				ptrGreen, 0, null, null);
		
		File redEyeKernelFile = new File("Kernels/RedEyeKernel");
		Scanner redEyeKernelScan = new Scanner(redEyeKernelFile);
		StringBuffer redBuffer = new StringBuffer();
		while (kernelScan.hasNextLine()) {
			redBuffer.append(kernelScan.nextLine());
			redBuffer.append("\n");
		}
		program = CL.clCreateProgramWithSource(deviceManager.getContext(), 1,
				new String[] { redBuffer.toString() }, null, null);

		CL.clBuildProgram(program, 0, null, null, null, null);

		
		deviceManager.createQueue();
		cl_kernel averageKernel = CL.clCreateKernel(program, "caluclateAverage", null);
		
		int[] dimensions = {data.length};
		Pointer ptrDimensions = Pointer.to(dimensions);
		cl_mem memDimensions = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * dimensions.length, ptrDimensions, null);
		CL.clSetKernelArg(separateKernel, 0, Sizeof.cl_mem, Pointer.to(memRed));
		CL.clSetKernelArg(separateKernel, 1, Sizeof.cl_mem, Pointer.to(memBlue));
		CL.clSetKernelArg(separateKernel, 2, Sizeof.cl_mem, Pointer.to(memGreen));
		CL.clSetKernelArg(separateKernel, 3, Sizeof.cl_mem, Pointer.to(memResult));
		CL.clSetKernelArg(separateKernel, 4, Sizeof.cl_mem, Pointer.to(memDimensions));
		startTime = System.nanoTime();
		CL.clEnqueueNDRangeKernel(deviceManager.getQueue(), averageKernel, 1, null, globalWorkSize, localWorkSize, 0,
				null, null);
		afterOne += System.nanoTime() - startTime;
		
		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memResult, CL.CL_TRUE, 0, resultData.length * Sizeof.cl_float,
				ptrResult, 0, null, null);
		
//		for (int i = 0; i < data.length; i++) {
//			int pixel = data[i];
//			int alpha = (pixel & PixelModifier.getAlphaMask()) >> PixelModifier.getAlphaOffset();
//			int red = (pixel & PixelModifier.getRedMask()) >> PixelModifier.getRedOffset();
//			int green = (pixel & PixelModifier.getGreenMask()) >> PixelModifier.getGreenOffset();
//			int blue = (pixel & PixelModifier.getBlueMask()) >> PixelModifier.getBlueOffset();
//			redTotal += red;
//			blueTotal += blue;
//			greenTotal += green;
//		}
//		int redAvg = redTotal / data.length;
//		int greenAvg = greenTotal / data.length;
//		int blueAvg = blueTotal / data.length;
//		int[] averages = { redAvg, greenAvg, blueAvg };
		return resultData;
	}
}

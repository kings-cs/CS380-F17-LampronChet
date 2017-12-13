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
 * @author chetlampron
 *
 */
public class MergeClone {
	/** The device manager. */
	private JoclInitializer deviceManager;
	/** The cl program. */
	private cl_program program;
	/** The runtime of the kernels. */
	private double calculatedRuntime;
	/** The worksize for alpha. */
	private int workSize;

	/**
	 * Constructs a MergeClone object.
	 * 
	 * @param deviceManager
	 *            The device manager.
	 */
	public MergeClone(JoclInitializer deviceManager) {
		this.deviceManager = deviceManager;
	}

	/**
	 * Converts an int array to float.
	 * 
	 * @param alpha
	 *            The int alpha.
	 * @param red
	 *            The int red.
	 * @param green
	 *            The int green.
	 * @param blue
	 *            The int blue.
	 * @param resultAlpha
	 *            The float alpha.
	 * @param resultRed
	 *            The float red.
	 * @param resultGreen
	 *            The float green.
	 * @param resultBlue
	 *            The float blue.
	 * @throws FileNotFoundException
	 *             Not thrown.
	 */
	public void convertToFloat(int[] alpha, int[] red, int[] green, int[] blue, float[] resultAlpha, float[] resultRed,
			float[] resultGreen, float[] resultBlue) throws FileNotFoundException {
		Pointer ptrAlpha = Pointer.to(alpha);
		Pointer ptrRed = Pointer.to(red);
		Pointer ptrGreen = Pointer.to(green);
		Pointer ptrBlue = Pointer.to(blue);
		Pointer ptrRAlpha = Pointer.to(resultAlpha);
		Pointer ptrRRed = Pointer.to(resultRed);
		Pointer ptrRGreen = Pointer.to(resultGreen);
		Pointer ptrRBlue = Pointer.to(resultBlue);

		cl_mem memAlpha = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * alpha.length, ptrAlpha, null);
		cl_mem memRed = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * red.length, ptrRed, null);
		cl_mem memGreen = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * green.length, ptrGreen, null);
		cl_mem memBlue = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * blue.length, ptrBlue, null);
		cl_mem memRAlpha = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * resultAlpha.length, ptrRAlpha, null);
		cl_mem memRRed = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * resultRed.length, ptrRRed, null);
		cl_mem memRGreen = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * resultGreen.length, ptrRGreen, null);
		cl_mem memRBlue = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * resultBlue.length, ptrRBlue, null);

		File kernelFile = new File("Kernels/CloneKernel");
		Scanner kernelScan = new Scanner(kernelFile);
		StringBuffer sourceBuffer = new StringBuffer();
		while (kernelScan.hasNextLine()) {
			sourceBuffer.append(kernelScan.nextLine());
			sourceBuffer.append("\n");
		}
		program = CL.clCreateProgramWithSource(deviceManager.getContext(), 1, new String[] { sourceBuffer.toString() },
				null, null);

		CL.clBuildProgram(program, 0, null, null, null, null);

		workSize = PixelModifier.getWorkSize(deviceManager, alpha);
		long[] globalWorkSize = new long[] { alpha.length };
		long[] localWorkSize = new long[] { workSize };

		cl_kernel convertKernel = CL.clCreateKernel(program, "convertToFloat", null);

		CL.clSetKernelArg(convertKernel, 0, Sizeof.cl_mem, Pointer.to(memAlpha));
		CL.clSetKernelArg(convertKernel, 1, Sizeof.cl_mem, Pointer.to(memRed));
		CL.clSetKernelArg(convertKernel, 2, Sizeof.cl_mem, Pointer.to(memGreen));
		CL.clSetKernelArg(convertKernel, 3, Sizeof.cl_mem, Pointer.to(memBlue));
		CL.clSetKernelArg(convertKernel, 4, Sizeof.cl_mem, Pointer.to(memRAlpha));
		CL.clSetKernelArg(convertKernel, 5, Sizeof.cl_mem, Pointer.to(memRRed));
		CL.clSetKernelArg(convertKernel, 6, Sizeof.cl_mem, Pointer.to(memRGreen));
		CL.clSetKernelArg(convertKernel, 7, Sizeof.cl_mem, Pointer.to(memRBlue));

		double startTime = System.nanoTime();
		CL.clEnqueueNDRangeKernel(deviceManager.getQueue(), convertKernel, 1, null, globalWorkSize, localWorkSize, 0,
				null, null);
		calculatedRuntime += System.nanoTime() - startTime;
		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memRAlpha, CL.CL_TRUE, 0, resultAlpha.length * Sizeof.cl_int,
				ptrRAlpha, 0, null, null);
		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memRRed, CL.CL_TRUE, 0, resultAlpha.length * Sizeof.cl_int,
				ptrRRed, 0, null, null);
		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memRGreen, CL.CL_TRUE, 0, resultAlpha.length * Sizeof.cl_int,
				ptrRGreen, 0, null, null);
		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memRBlue, CL.CL_TRUE, 0, resultAlpha.length * Sizeof.cl_int,
				ptrRBlue, 0, null, null);

		CL.clReleaseKernel(convertKernel);
		CL.clReleaseMemObject(memAlpha);
		CL.clReleaseMemObject(memRed);
		CL.clReleaseMemObject(memGreen);
		CL.clReleaseMemObject(memBlue);
		CL.clReleaseMemObject(memRAlpha);
		CL.clReleaseMemObject(memRRed);
		CL.clReleaseMemObject(memRGreen);
		CL.clReleaseMemObject(memRBlue);
	}

	/**
	 * Calculates the mask from the alpha.
	 * 
	 * @param alpha
	 *            The alpha channel.
	 * @return The mask.
	 */
	public float[] getMask(float[] alpha) {
		float[] mask = new float[alpha.length];
		Pointer ptrAlpha = Pointer.to(alpha);
		Pointer ptrMask = Pointer.to(mask);

		cl_mem memAlpha = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * alpha.length, ptrAlpha, null);
		cl_mem memMask = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * mask.length, ptrMask, null);

		CL.clBuildProgram(program, 0, null, null, null, null);

		long[] globalWorkSize = new long[] { alpha.length };
		long[] localWorkSize = new long[] { workSize };

		cl_kernel maskKernel = CL.clCreateKernel(program, "getMask", null);

		CL.clSetKernelArg(maskKernel, 0, Sizeof.cl_mem, Pointer.to(memAlpha));
		CL.clSetKernelArg(maskKernel, 1, Sizeof.cl_mem, Pointer.to(memMask));
		
		double startTime = System.nanoTime();
		CL.clEnqueueNDRangeKernel(deviceManager.getQueue(), maskKernel, 1, null, globalWorkSize, localWorkSize, 0,
				null, null);
		calculatedRuntime += System.nanoTime() - startTime;
		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memMask, CL.CL_TRUE, 0, mask.length * Sizeof.cl_int,
				ptrMask, 0, null, null);
		
		CL.clReleaseKernel(maskKernel);
		CL.clReleaseMemObject(memAlpha);
		CL.clReleaseMemObject(memMask);

		return mask;
	}
}

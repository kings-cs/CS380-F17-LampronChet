package testing;

import java.awt.image.BufferedImage;
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
 * Class containing an exclusive scan.
 * 
 * @author Chet Lampron
 *
 */
public class BlellochScan extends PixelModifier {
	/** The device manager. */
	private JoclInitializer deviceManager;
	/** The work group size. */
	private int workSize;

	/**
	 * Constructs an inclusive scan object.
	 * 
	 * @param theManager
	 *            The device manager to use.
	 */
	public BlellochScan(JoclInitializer theManager) {
		deviceManager = theManager;
	}

	/**
	 * Scans data inclusively.
	 * 
	 * @param data
	 *            The data to scan.
	 * @param result
	 *            The result array.
	 * @return The runtime.
	 * @throws FileNotFoundException
	 *             Not thrown.
	 */
	public long scan(final int[] data, int[] result) throws FileNotFoundException {
		getProperWorkSize(deviceManager, data);
		int[] paddedData = padArray(data);
		int[] paddedResult = padArray(result);
		getProperWorkSize(deviceManager, paddedData);
		Pointer ptrData = Pointer.to(paddedData);
		Pointer ptrResult = Pointer.to(paddedResult);

		cl_mem memData = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * paddedData.length, ptrData, null);
		cl_mem memResult = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * paddedResult.length, ptrResult, null);

		File kernelFile = new File("Kernels/Blelloch");
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

		cl_kernel blellochKernel = CL.clCreateKernel(program, "blelloch", null);

		CL.clSetKernelArg(blellochKernel, 0, Sizeof.cl_mem, Pointer.to(memData));
		CL.clSetKernelArg(blellochKernel, 1, Sizeof.cl_mem, Pointer.to(memResult));
		CL.clSetKernelArg(blellochKernel, 2, Sizeof.cl_mem, Pointer.to(memAccum));
		CL.clSetKernelArg(blellochKernel, 3, Sizeof.cl_int * localWorkSize[0], null);
		long startTime = System.nanoTime();
		CL.clEnqueueNDRangeKernel(deviceManager.getQueue(), blellochKernel, 1, null, globalWorkSize, localWorkSize, 0,
				null, null);
		long calculatedRuntime = System.nanoTime() - startTime;

		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memResult, CL.CL_TRUE, 0,
				paddedResult.length * Sizeof.cl_float, ptrResult, 0, null, null);
		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memAccum, CL.CL_TRUE, 0, accumulator.length * Sizeof.cl_float,
				ptrAccum, 0, null, null);

		int[] increments = new int[accumulator.length];
		if (accumulator.length > 1) {
			scan(accumulator, increments);
		}
		Pointer ptrIncrement = Pointer.to(increments);
		cl_mem memIncrement = CL.clCreateBuffer(deviceManager.getContext(),
				CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * increments.length, ptrIncrement, null);

		int[] finalResult = new int[paddedData.length];
		Pointer ptrFinal = Pointer.to(finalResult);
		cl_mem memFinal = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * finalResult.length, ptrFinal, null);

		cl_kernel incrementKernel = CL.clCreateKernel(program, "increment", null);
		CL.clSetKernelArg(incrementKernel, 0, Sizeof.cl_mem, Pointer.to(memResult));
		CL.clSetKernelArg(incrementKernel, 1, Sizeof.cl_mem, Pointer.to(memFinal));
		CL.clSetKernelArg(incrementKernel, 2, Sizeof.cl_mem, Pointer.to(memIncrement));
		startTime = System.nanoTime();
		CL.clEnqueueNDRangeKernel(deviceManager.getQueue(), incrementKernel, 1, null, globalWorkSize, localWorkSize, 0,
				null, null);
		calculatedRuntime += System.nanoTime() - startTime;
		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memFinal, CL.CL_TRUE, 0, finalResult.length * Sizeof.cl_float,
				ptrFinal, 0, null, null);

		for (int i = 0; i < result.length; i++) {
			result[i] = finalResult[i];
		}
		CL.clReleaseKernel(blellochKernel);
		CL.clReleaseKernel(incrementKernel);
		CL.clReleaseProgram(program);
		CL.clReleaseMemObject(memResult);
		CL.clReleaseMemObject(memData);
		CL.clReleaseMemObject(memAccum);
		CL.clReleaseMemObject(memFinal);
		CL.clReleaseMemObject(memIncrement);

		kernelScan.close();
		return calculatedRuntime;
	}

	/**
	 * Gets the proper work size.
	 * 
	 * @param data
	 *            The data array.
	 * @param deviceManager
	 *            The proper device manager.
	 */
	public void getProperWorkSize(JoclInitializer deviceManager, int[] data) {
		int maxItemsPerGroup = deviceManager.getMaxWorkGroupSize();
		boolean isDivisible = false;

		while (!isDivisible) {
			int numOfItems = data.length % maxItemsPerGroup;
			if (numOfItems == 0) {
				isDivisible = true;
			} else {
				maxItemsPerGroup--;
			}
		}
		workSize = maxItemsPerGroup;
	}

	/**
	 * Pads the array with 0's.
	 * 
	 * @param old
	 *            The original array.
	 * @return The padded array.
	 * @throws FileNotFoundException
	 *             Not thrown.
	 */
	public static int[] padArray(int[] old) throws FileNotFoundException {
		int[] result = null;

		// double power = old.length / workSize;
		// int lengthModifier = (int) Math.ceil(power);
		// int length = (lengthModifier * workSize);
		int length = 1;
		boolean isGreater = false;
		while (!isGreater) {
			length = length << 1;
			if (length >= old.length) {
				isGreater = true;
			}
		}
		result = new int[length];

//		Pointer ptrOld = Pointer.to(old);
//		Pointer ptrResult = Pointer.to(result);
//
//		cl_mem memOld = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
//				Sizeof.cl_float * old.length, ptrOld, null);
//		cl_mem memResult = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR,
//				Sizeof.cl_float * result.length, ptrResult, null);
//
//		File kernelFile = new File("Kernels/Blelloch");
//		Scanner kernelScan = new Scanner(kernelFile);
//		StringBuffer sourceBuffer = new StringBuffer();
//		while (kernelScan.hasNextLine()) {
//			sourceBuffer.append(kernelScan.nextLine());
//			sourceBuffer.append("\n");
//		}
//		cl_program program = CL.clCreateProgramWithSource(deviceManager.getContext(), 1,
//				new String[] { sourceBuffer.toString() }, null, null);
//
//		CL.clBuildProgram(program, 0, null, null, null, null);
//
//		long[] globalWorkSize = new long[] { old.length };
//		long[] localWorkSize = new long[] { workSize };
//
//		cl_kernel padKernel = CL.clCreateKernel(program, "pad", null);
//		
//		CL.clSetKernelArg(padKernel, 0, Sizeof.cl_mem, Pointer.to(memOld));
//		CL.clSetKernelArg(padKernel, 1, Sizeof.cl_mem, Pointer.to(memResult));
//		
//		CL.clEnqueueNDRangeKernel(deviceManager.getQueue(), padKernel, 1, null, globalWorkSize, localWorkSize, 0,
//				null, null);
//		
//		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memResult, CL.CL_TRUE, 0, result.length * Sizeof.cl_float,
//				ptrResult, 0, null, null);
//		
		for (int i = 0; i < old.length; i++) {
			result[i] = old[i];
		}
//		CL.clReleaseKernel(padKernel);
//		CL.clReleaseProgram(program);
//		CL.clReleaseMemObject(memOld);
//		CL.clReleaseMemObject(memResult);
//		kernelScan.close();
		return result;

	}

	@Override
	public BufferedImage modifyPixel(BufferedImage image) throws FileNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}
}

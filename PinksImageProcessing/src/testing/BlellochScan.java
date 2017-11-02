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
	 * @throws FileNotFoundException
	 *             Not thrown.
	 */
	public void scan(final float[] data, float[] result) throws FileNotFoundException {
		int[] dimensions = { data.length };
		float[] theData = padArray(data);
		Pointer ptrData = Pointer.to(theData);
		Pointer ptrResult = Pointer.to(result);
		Pointer ptrDimensions = Pointer.to(dimensions);

		cl_mem memData = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * data.length, ptrData, null);
		cl_mem memResult = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * result.length, ptrResult, null);
		cl_mem memDimensions = CL.clCreateBuffer(deviceManager.getContext(),
				CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * dimensions.length, ptrDimensions,
				null);

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

		long[] globalWorkSize = new long[] { data.length };
		long[] localWorkSize = new long[] { getWorkSize(deviceManager, data) };

		cl_kernel hillisSteeleKernel = CL.clCreateKernel(program, "blelloch", null);

		CL.clSetKernelArg(hillisSteeleKernel, 0, Sizeof.cl_mem, Pointer.to(memData));
		CL.clSetKernelArg(hillisSteeleKernel, 1, Sizeof.cl_mem, Pointer.to(memResult));
		CL.clSetKernelArg(hillisSteeleKernel, 2, Sizeof.cl_float * localWorkSize[0], null);
		CL.clSetKernelArg(hillisSteeleKernel, 3, Sizeof.cl_mem, Pointer.to(memDimensions));
		CL.clEnqueueNDRangeKernel(deviceManager.getQueue(), hillisSteeleKernel, 1, null, globalWorkSize, localWorkSize,
				0, null, null);

		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memResult, CL.CL_TRUE, 0, result.length * Sizeof.cl_float,
				ptrResult, 0, null, null);

		CL.clReleaseKernel(hillisSteeleKernel);
		CL.clReleaseMemObject(memResult);
		CL.clReleaseMemObject(memData);

		kernelScan.close();
	}

	/**
	 * Gets the proper work size.
	 * 
	 * @param data
	 *            The data array.
	 * @param deviceManager
	 *            The proper device manager.
	 * @return The proper work size;
	 */
	public int getWorkSize(JoclInitializer deviceManager, float[] data) {
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
		return maxItemsPerGroup;
	}

	/**
	 * Pads the array with 0's.
	 * 
	 * @param old
	 *            The original array.
	 * @return The padded array.
	 */
	public float[] padArray(float[] old) {
		float[] result = null;

		double power = Math.log(old.length) / Math.log(2);
		double lengthPow = Math.ceil(power);
		int length = (int) Math.pow(2, lengthPow);
		result = new float[length];
		for (int i = 0; i < old.length; i++) {
			result[i] = old[i];
		}

		return result;

	}

	@Override
	public BufferedImage modifyPixel(BufferedImage image) throws FileNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}
}

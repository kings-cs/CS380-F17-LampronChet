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
 * Class containing an inclusive parallel Hillis Steele scan.
 * 
 * @author Chet Lampron
 *
 */
public class HillisSteeleScan extends PixelModifier {

	/** The device manager. */
	private JoclInitializer deviceManager;

	/**
	 * Constructs an inclusive scan object.
	 * 
	 * @param theManager
	 *            The device manager to use.
	 */
	public HillisSteeleScan(JoclInitializer theManager) {
		deviceManager = theManager;
	}

	/**
	 * Scans data inclusively.
	 * 
	 * @param histogramResult
	 *            The data to scan.
	 * @param freqResult
	 *            The result array.
	 * @throws FileNotFoundException
	 *             Not thrown.
	 */
	public void scan(final int[] histogramResult, int[] freqResult) throws FileNotFoundException {

		//float[] from = new float[histogramResult.length];
		//float[] to = new float[histogramResult.length];

		Pointer ptrData = Pointer.to(histogramResult);
		Pointer ptrResult = Pointer.to(freqResult);

		cl_mem memData = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * histogramResult.length, ptrData, null);
		cl_mem memResult = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * freqResult.length, ptrResult, null);

		File kernelFile = new File("Kernels/HillisSteeleScan");
		Scanner kernelScan = new Scanner(kernelFile);
		StringBuffer sourceBuffer = new StringBuffer();
		while (kernelScan.hasNextLine()) {
			sourceBuffer.append(kernelScan.nextLine());
			sourceBuffer.append("\n");
		}
		cl_program program = CL.clCreateProgramWithSource(deviceManager.getContext(), 1,
				new String[] { sourceBuffer.toString() }, null, null);

		CL.clBuildProgram(program, 0, null, null, null, null);

		long[] globalWorkSize = new long[] { histogramResult.length };
		long[] localWorkSize = new long[] { getWorkSize(deviceManager, histogramResult) };

		cl_kernel hillisSteeleKernel = CL.clCreateKernel(program, "hillis_steele", null);

		CL.clSetKernelArg(hillisSteeleKernel, 0, Sizeof.cl_mem, Pointer.to(memData));
		CL.clSetKernelArg(hillisSteeleKernel, 1, Sizeof.cl_mem, Pointer.to(memResult));
		CL.clSetKernelArg(hillisSteeleKernel, 2, Sizeof.cl_float * localWorkSize[0], null);
		CL.clSetKernelArg(hillisSteeleKernel, 3, Sizeof.cl_float * localWorkSize[0], null);

		CL.clEnqueueNDRangeKernel(deviceManager.getQueue(), hillisSteeleKernel, 1, null, globalWorkSize, localWorkSize,
				0, null, null);

		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memResult, CL.CL_TRUE, 0, freqResult.length * Sizeof.cl_float,
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

	@Override
	public BufferedImage modifyPixel(BufferedImage image) throws FileNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}
}

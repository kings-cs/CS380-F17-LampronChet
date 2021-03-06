package algorithms;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import javax.swing.JOptionPane;

import org.jocl.CL;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_kernel;
import org.jocl.cl_mem;
import org.jocl.cl_program;

import parallel.JoclInitializer;

/**
 * Flips the picture horizontally.
 * 
 * @author chetlampron
 *
 */
public class HorizontalFlip extends PixelModifier {
	/** The device manager. */
	private JoclInitializer deviceManager;

	/**
	 * Constructs the parallel modifier.
	 * 
	 * @param aDeviceManager
	 *            The device manager.
	 */
	public HorizontalFlip(JoclInitializer aDeviceManager) {
		deviceManager = aDeviceManager;
	}

	@Override
	public BufferedImage modifyPixel(BufferedImage image) throws FileNotFoundException {
		int width = image.getWidth();
		int height = image.getHeight();
		int[] dimensions = new int[2];
		dimensions[0] = width;
		dimensions[1] = height;
		int[] sourceData = super.unwrapImage(image);

		int[] resultData = new int[sourceData.length];

		Pointer ptrSource = Pointer.to(sourceData);
		Pointer ptrResult = Pointer.to(resultData);
		Pointer ptrDimensions = Pointer.to(dimensions);

		cl_mem memSource = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * sourceData.length, ptrSource, null);
		cl_mem memResult = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_WRITE_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * resultData.length, ptrResult, null);
		cl_mem memDimensions = CL.clCreateBuffer(deviceManager.getContext(),
				CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * dimensions.length, ptrDimensions,
				null);
		File kernelFile = new File("Kernels/HorizontalKernel");
		Scanner kernelScan = new Scanner(kernelFile);
		StringBuffer sourceBuffer = new StringBuffer();
		while (kernelScan.hasNextLine()) {
			sourceBuffer.append(kernelScan.nextLine());
			sourceBuffer.append("\n");
		}
		cl_program program = CL.clCreateProgramWithSource(deviceManager.getContext(), 1,
				new String[] { sourceBuffer.toString() }, null, null);

		CL.clBuildProgram(program, 0, null, null, null, null);

		int workSize = super.getWorkSize(deviceManager, sourceData);
		System.out.println(workSize);

		long[] globalWorkSize = new long[] { sourceData.length };
		long[] localWorkSize = new long[] { workSize };

		cl_kernel horizontalKernel = CL.clCreateKernel(program, "horizontalFlip", null);

		CL.clSetKernelArg(horizontalKernel, 0, Sizeof.cl_mem, Pointer.to(memSource));
		CL.clSetKernelArg(horizontalKernel, 1, Sizeof.cl_mem, Pointer.to(memResult));
		CL.clSetKernelArg(horizontalKernel, 2, Sizeof.cl_mem, Pointer.to(memDimensions));

		deviceManager.createQueue();
		long startTime = System.nanoTime();
		CL.clEnqueueNDRangeKernel(deviceManager.getQueue(), horizontalKernel, 1, null, globalWorkSize, localWorkSize, 0,
				null, null);
		double totalTime = (System.nanoTime() - startTime) / 1000000.0;
		JOptionPane.showMessageDialog(null, "Total Time: " + totalTime + "ms");

		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memResult, CL.CL_TRUE, 0, resultData.length * Sizeof.cl_float,
				ptrResult, 0, null, null);

		CL.clReleaseKernel(horizontalKernel);
		CL.clReleaseProgram(program);
		CL.clReleaseMemObject(memSource);
		CL.clReleaseMemObject(memResult);
		CL.clReleaseMemObject(memDimensions);

		packageImage(resultData, image);
		kernelScan.close();
		return image;
	}
}

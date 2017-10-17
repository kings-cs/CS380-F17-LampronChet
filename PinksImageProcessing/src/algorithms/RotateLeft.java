package algorithms;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
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
 * Rotates the image right.
 * 
 * @author chetlampron
 *
 */
public class RotateLeft extends PixelModifier {
	/** The device manager. */
	private JoclInitializer deviceManager;

	/**
	 * Constructs the parallel modifier.
	 * 
	 * @param aDeviceManager
	 *            The device manager.
	 */
	public RotateLeft(JoclInitializer aDeviceManager) {
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
		File kernelFile = new File("Kernels/RotateLeftKernel");
		Scanner kernelScan = new Scanner(kernelFile);
		StringBuffer sourceBuffer = new StringBuffer();
		while (kernelScan.hasNextLine()) {
			sourceBuffer.append(kernelScan.nextLine());
			sourceBuffer.append("\n");
		}
		cl_program program = CL.clCreateProgramWithSource(deviceManager.getContext(), 1,
				new String[] { sourceBuffer.toString() }, null, null);

		CL.clBuildProgram(program, 0, null, null, null, null);

		long[] globalWorkSize = new long[] { resultData.length };
		long[] localWorkSize = new long[] { 1 };

		cl_kernel rotateLeftKernel = CL.clCreateKernel(program, "rotateLeft", null);

		CL.clSetKernelArg(rotateLeftKernel, 0, Sizeof.cl_mem, Pointer.to(memSource));
		CL.clSetKernelArg(rotateLeftKernel, 1, Sizeof.cl_mem, Pointer.to(memResult));
		CL.clSetKernelArg(rotateLeftKernel, 2, Sizeof.cl_mem, Pointer.to(memDimensions));

		deviceManager.createQueue();
		long startTime = System.nanoTime();
		CL.clEnqueueNDRangeKernel(deviceManager.getQueue(), rotateLeftKernel, 1, null, globalWorkSize, localWorkSize,
				0, null, null);
		double totalTime = (System.nanoTime() - startTime) / 1000000.0;
		JOptionPane.showMessageDialog(null, "Total Time: " + totalTime + "ms");

		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memResult, CL.CL_TRUE, 0, resultData.length * Sizeof.cl_float,
				ptrResult, 0, null, null);

		CL.clReleaseKernel(rotateLeftKernel);
		CL.clReleaseProgram(program);
		CL.clReleaseMemObject(memSource);
		CL.clReleaseMemObject(memResult);
		CL.clReleaseMemObject(memDimensions);

		BufferedImage returnImage = new BufferedImage(height, width, BufferedImage.TYPE_INT_ARGB);
		DataBufferInt resultDataBuffer = new DataBufferInt(resultData, resultData.length);
		Raster resultRastor = Raster.createRaster(returnImage.getRaster().getSampleModel(), resultDataBuffer,
				new Point(0, 0));
		returnImage.setData(resultRastor);
		kernelScan.close();
		return returnImage;
	}
}

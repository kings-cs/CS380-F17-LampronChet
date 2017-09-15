package pink;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

//import javax.swing.JOptionPane;

import org.jocl.CL;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_kernel;
import org.jocl.cl_mem;
import org.jocl.cl_program;

import parallel.JoclInitializer;

/**
 * Creates grayscale in parallel.
 * 
 * @author chetlampron
 *
 */
public class GrayscaleModifierParallel extends PixelModifier {
	/** The device manager. */
	private JoclInitializer deviceManager;

	/**
	 * Constructs the parallel modifier.
	 * 
	 * @param aDeviceManager
	 *            The device manager.
	 */
	public GrayscaleModifierParallel(JoclInitializer aDeviceManager) {
		deviceManager = aDeviceManager;
	}

	@Override
	public BufferedImage modifyPixel(BufferedImage image) throws FileNotFoundException {


		int[] sourceData = super.unwrapImage(image);
		

		int[] resultData = new int[sourceData.length];

		Pointer ptrSource = Pointer.to(sourceData);
		Pointer ptrResult = Pointer.to(resultData);

		cl_mem memSource = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * sourceData.length, ptrSource, null);
		cl_mem memResult = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * resultData.length, ptrResult, null);
		File kernelFile = new File("Kernels/GrayscaleKernel");
		Scanner kernelScan = new Scanner(kernelFile);
		StringBuffer sourceBuffer = new StringBuffer();
		while (kernelScan.hasNextLine()) {
			sourceBuffer.append(kernelScan.nextLine());
			sourceBuffer.append("\n");
		}
		cl_program program = CL.clCreateProgramWithSource(deviceManager.getContext(), 1,
				new String[] { sourceBuffer.toString() }, null, null);

		CL.clBuildProgram(program, 0, null, null, null, null);

		cl_kernel kernel = CL.clCreateKernel(program, "grayscale_kernel", null);

		CL.clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(memSource));

		CL.clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(memResult));

		long[] globalWorkSize = new long[] { resultData.length };
		long[] localWorkSize = new long[] { 1 };
		deviceManager.createQueue();
		//long startTime = System.nanoTime();

		CL.clEnqueueNDRangeKernel(deviceManager.getQueue(), kernel, 1, null, globalWorkSize, localWorkSize, 0, null,
				null);
		//JOptionPane.showMessageDialog(null, "Total Time: " + (System.nanoTime() - startTime) / 1000000 + "ms");

		
		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memResult, CL.CL_TRUE, 0, sourceData.length * Sizeof.cl_float,
				ptrResult, 0, null, null);

		CL.clReleaseKernel(kernel);
		CL.clReleaseProgram(program);
		CL.clReleaseMemObject(memSource);
		CL.clReleaseMemObject(memResult);

		DataBufferInt resultDataBuffer = new DataBufferInt(resultData, resultData.length);
		Raster resultRastor = Raster.createRaster(image.getRaster().getSampleModel(), resultDataBuffer,
				new Point(0, 0));
		image.setData(resultRastor);
		kernelScan.close();
		return image;
	}
}

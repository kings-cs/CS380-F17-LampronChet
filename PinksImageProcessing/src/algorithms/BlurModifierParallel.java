package algorithms;

import java.awt.image.BufferedImage;
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
 * Blur modify in parallel.
 * 
 * @author Chet Lampron
 *
 */
public class BlurModifierParallel extends PixelModifier {
	/** The field for the stencil. */
	private final float[] stencil = { 0.0232468f, 0.0338240f, 0.0383276f, 0.0338240f, 0.0232468f, 0.0338240f,
			0.0492136f, 0.0557663f, 0.0492136f, 0.0338240f, 0.0383276f, 0.0557663f, 0.0631915f, 0.0557663f, 0.0383276f,
			0.0338240f, 0.0492136f, 0.0557663f, 0.0492136f, 0.0338240f, 0.0232468f, 0.0338240f, 0.0383276f, 0.0338240f,
			0.0232468f };
	/** The device manager. */
	private JoclInitializer deviceManager;

	/**
	 * Constructs the parallel modifier.
	 * 
	 * @param aDeviceManager
	 *            The device manager.
	 */
	public BlurModifierParallel(JoclInitializer aDeviceManager) {
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

		int[] redArray = new int[sourceData.length];
		int[] blueArray = new int[sourceData.length];
		int[] greenArray = new int[sourceData.length];
		int[] alphaArray = new int[sourceData.length];
		int[] modifiedRedArray = new int[redArray.length];
		int[] modifiedGreenArray = new int[greenArray.length];
		int[] modifiedBlueArray = new int[blueArray.length];

		Pointer ptrSource = Pointer.to(sourceData);
		Pointer ptrResult = Pointer.to(resultData);
		Pointer ptrRed = Pointer.to(redArray);
		Pointer ptrBlue = Pointer.to(blueArray);
		Pointer ptrGreen = Pointer.to(greenArray);
		Pointer ptrAlpha = Pointer.to(alphaArray);
		Pointer ptrModifiedRed = Pointer.to(modifiedRedArray);
		Pointer ptrModifiedGreen = Pointer.to(modifiedGreenArray);
		Pointer ptrModifiedBlue = Pointer.to(modifiedBlueArray);
		Pointer ptrStencil = Pointer.to(stencil);
		Pointer ptrDimensions = Pointer.to(dimensions);

		cl_mem memSource = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * sourceData.length, ptrSource, null);
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
		cl_mem memModifiedRed = CL.clCreateBuffer(deviceManager.getContext(),
				CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * modifiedRedArray.length,
				ptrModifiedRed, null);
		cl_mem memModifiedGreen = CL.clCreateBuffer(deviceManager.getContext(),
				CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * modifiedGreenArray.length,
				ptrModifiedGreen, null);
		cl_mem memModifiedBlue = CL.clCreateBuffer(deviceManager.getContext(),
				CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * modifiedBlueArray.length,
				ptrModifiedBlue, null);
		cl_mem memStencil = CL.clCreateBuffer(deviceManager.getContext(),
				CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * stencil.length, ptrStencil, null);
		cl_mem memDimensions = CL.clCreateBuffer(deviceManager.getContext(),
				CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * dimensions.length, ptrDimensions,
				null);
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

		long[] globalWorkSize = new long[] { resultData.length };
		long[] localWorkSize = new long[] { super.getWorkSize(deviceManager, sourceData) };
		deviceManager.createQueue();
		// Set up and run the separate channels kernel.
		cl_kernel separateKernel = CL.clCreateKernel(program, "separateChannels", null);

		CL.clSetKernelArg(separateKernel, 0, Sizeof.cl_mem, Pointer.to(memSource));
		CL.clSetKernelArg(separateKernel, 1, Sizeof.cl_mem, Pointer.to(memRed));
		CL.clSetKernelArg(separateKernel, 2, Sizeof.cl_mem, Pointer.to(memBlue));
		CL.clSetKernelArg(separateKernel, 3, Sizeof.cl_mem, Pointer.to(memGreen));
		CL.clSetKernelArg(separateKernel, 4, Sizeof.cl_mem, Pointer.to(memAlpha));
		// long startTime = System.nanoTime();
		CL.clEnqueueNDRangeKernel(deviceManager.getQueue(), separateKernel, 1, null, globalWorkSize, localWorkSize, 0,
				null, null);
		// long afterOne = System.nanoTime() - startTime;
		// JOptionPane.showMessageDialog(null, "Total Time: " + (System.nanoTime() -
		// startTime) / 1000000 + "ms");

		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memRed, CL.CL_TRUE, 0, redArray.length * Sizeof.cl_float,
				ptrRed, 0, null, null);
		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memBlue, CL.CL_TRUE, 0, blueArray.length * Sizeof.cl_float,
				ptrBlue, 0, null, null);
		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memGreen, CL.CL_TRUE, 0, greenArray.length * Sizeof.cl_float,
				ptrGreen, 0, null, null);
		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memAlpha, CL.CL_TRUE, 0, alphaArray.length * Sizeof.cl_float,
				ptrAlpha, 0, null, null);

		/* Set up and run the blur kernel */
		cl_kernel blurKernel = CL.clCreateKernel(program, "blurChannels", null);

		CL.clSetKernelArg(blurKernel, 0, Sizeof.cl_mem, Pointer.to(memRed));
		CL.clSetKernelArg(blurKernel, 1, Sizeof.cl_mem, Pointer.to(memBlue));
		CL.clSetKernelArg(blurKernel, 2, Sizeof.cl_mem, Pointer.to(memGreen));
		CL.clSetKernelArg(blurKernel, 3, Sizeof.cl_mem, Pointer.to(memModifiedRed));
		CL.clSetKernelArg(blurKernel, 4, Sizeof.cl_mem, Pointer.to(memModifiedBlue));
		CL.clSetKernelArg(blurKernel, 5, Sizeof.cl_mem, Pointer.to(memModifiedGreen));
		CL.clSetKernelArg(blurKernel, 6, Sizeof.cl_mem, Pointer.to(memStencil));
		CL.clSetKernelArg(blurKernel, 7, Sizeof.cl_mem, Pointer.to(memDimensions));
		// startTime = System.nanoTime();
		CL.clEnqueueNDRangeKernel(deviceManager.getQueue(), blurKernel, 1, null, globalWorkSize, localWorkSize, 0, null,
				null);
		// long afterTwo = System.nanoTime() - startTime;

		// JOptionPane.showMessageDialog(null, "Total Time: " + (System.nanoTime() -
		// startTime) / 1000000 + "ms");

		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memModifiedRed, CL.CL_TRUE, 0,
				sourceData.length * Sizeof.cl_float, ptrModifiedRed, 0, null, null);
		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memModifiedBlue, CL.CL_TRUE, 0,
				sourceData.length * Sizeof.cl_float, ptrModifiedBlue, 0, null, null);
		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memModifiedGreen, CL.CL_TRUE, 0,
				sourceData.length * Sizeof.cl_float, ptrModifiedGreen, 0, null, null);

		/* Set up and run recombine kernel. */
		cl_kernel recombineKernel = CL.clCreateKernel(program, "combineChannels", null);

		CL.clSetKernelArg(recombineKernel, 0, Sizeof.cl_mem, Pointer.to(memAlpha));
		CL.clSetKernelArg(recombineKernel, 1, Sizeof.cl_mem, Pointer.to(memModifiedRed));
		CL.clSetKernelArg(recombineKernel, 2, Sizeof.cl_mem, Pointer.to(memModifiedBlue));
		CL.clSetKernelArg(recombineKernel, 3, Sizeof.cl_mem, Pointer.to(memModifiedGreen));
		CL.clSetKernelArg(recombineKernel, 4, Sizeof.cl_mem, Pointer.to(memResult));
		// startTime = System.nanoTime();
		CL.clEnqueueNDRangeKernel(deviceManager.getQueue(), recombineKernel, 1, null, globalWorkSize, localWorkSize, 0,
				null, null);
		// long afterThree = System.nanoTime() - startTime;

		// JOptionPane.showMessageDialog(null, "Total Time: " + (afterOne + afterTwo +
		// afterThree) / 1000000 + "ms");

		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memResult, CL.CL_TRUE, 0, sourceData.length * Sizeof.cl_float,
				ptrResult, 0, null, null);

		CL.clReleaseKernel(separateKernel);
		CL.clReleaseKernel(blurKernel);
		CL.clReleaseKernel(recombineKernel);
		CL.clReleaseProgram(program);
		CL.clReleaseMemObject(memSource);
		CL.clReleaseMemObject(memResult);
		CL.clReleaseMemObject(memRed);
		CL.clReleaseMemObject(memBlue);
		CL.clReleaseMemObject(memGreen);
		CL.clReleaseMemObject(memAlpha);
		CL.clReleaseMemObject(memModifiedRed);
		CL.clReleaseMemObject(memModifiedBlue);
		CL.clReleaseMemObject(memModifiedGreen);
		CL.clReleaseMemObject(memDimensions);
		CL.clReleaseMemObject(memStencil);

		packageImage(resultData, image);
		kernelScan.close();
		return image;
	}

}

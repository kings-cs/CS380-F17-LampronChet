/**
 * 
 */
package algorithms;

import java.awt.image.BufferedImage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;
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
 * Modifies picture to mosaic in parallel.
 * 
 * @author Chet Lampron
 *
 */
public class MosaicModifierParallel extends PixelModifier {

	/** The device manager. */
	private JoclInitializer deviceManager;

	/** The number of tiles. */
	private int tiles;

	/**
	 * Constructs a mosaic modifier for parallel computation.
	 * 
	 * @param numOfTiles
	 *            The number of tiles.
	 * @param aDeviceManager
	 *            The current device manager.
	 */
	public MosaicModifierParallel(int numOfTiles, JoclInitializer aDeviceManager) {
		tiles = numOfTiles;
		deviceManager = aDeviceManager;
	}

	/**
	 * Generates the random pixels to be tile points in the mosaic.
	 * 
	 * @param numOfPoints
	 *            The number of points to generate.
	 * 
	 * @param data
	 *            The image data.
	 * @return The tile points.
	 */
	public int[] getTilePoints(int numOfPoints, int[] data) {
		int[] randomValues = new int[numOfPoints];
		Random rand = new Random();

		for (int i = 0; i < randomValues.length; i++) {
			randomValues[i] = rand.nextInt(data.length - 1);
		}

		return randomValues;
	}

	@Override
	public BufferedImage modifyPixel(BufferedImage image) throws FileNotFoundException {
		int width = image.getWidth();
		int height = image.getHeight();
		int[] sourceData = super.unwrapImage(image);

		int[] resultData = new int[sourceData.length];
		int[] tilePoints = getTilePoints(tiles, sourceData);
		int[] dimensions = new int[3];

		dimensions[0] = width;
		dimensions[1] = height;
		dimensions[2] = tiles;

		Pointer ptrSource = Pointer.to(sourceData);
		Pointer ptrResult = Pointer.to(resultData);
		Pointer ptrDimensions = Pointer.to(dimensions);
		Pointer ptrTiles = Pointer.to(tilePoints);

		cl_mem memSource = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * sourceData.length, ptrSource, null);
		cl_mem memResult = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_WRITE_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * resultData.length, ptrResult, null);
		cl_mem memDimensions = CL.clCreateBuffer(deviceManager.getContext(),
				CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * dimensions.length, ptrDimensions,
				null);
		cl_mem memTiles = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_int * tilePoints.length, ptrTiles, null);

		File kernelFile = new File("Kernels/MosaicKernel");
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

		cl_kernel mosaicKernel = CL.clCreateKernel(program, "mosaic", null);

		CL.clSetKernelArg(mosaicKernel, 0, Sizeof.cl_mem, Pointer.to(memSource));
		CL.clSetKernelArg(mosaicKernel, 1, Sizeof.cl_mem, Pointer.to(memResult));
		CL.clSetKernelArg(mosaicKernel, 2, Sizeof.cl_mem, Pointer.to(memDimensions));
		CL.clSetKernelArg(mosaicKernel, 3, Sizeof.cl_mem, Pointer.to(memTiles));
		double startTime = System.nanoTime();
		CL.clEnqueueNDRangeKernel(deviceManager.getQueue(), mosaicKernel, 1, null, globalWorkSize, localWorkSize, 0,
				null, null);
		JOptionPane.showMessageDialog(null, "Total Time: " + (System.nanoTime() - startTime) / 1000000.0 + "ms");

		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memResult, CL.CL_TRUE, 0, resultData.length * Sizeof.cl_float,
				ptrResult, 0, null, null);

		CL.clReleaseKernel(mosaicKernel);
		CL.clReleaseProgram(program);
		CL.clReleaseMemObject(memSource);
		CL.clReleaseMemObject(memResult);
		CL.clReleaseMemObject(memDimensions);
		CL.clReleaseMemObject(memTiles);

		kernelScan.close();
		packageImage(resultData, image);
		return image;
	}
}

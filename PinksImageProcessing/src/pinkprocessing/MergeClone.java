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
 * Helper to merge two images.
 * 
 * @author Chet Lampron
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
		cl_mem memRAlpha = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * resultAlpha.length, ptrRAlpha, null);
		cl_mem memRRed = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * resultRed.length, ptrRRed, null);
		cl_mem memRGreen = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * resultGreen.length, ptrRGreen, null);
		cl_mem memRBlue = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR,
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
		setRuntime(getCalculatedRuntime() + (System.nanoTime() - startTime));
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
		kernelScan.close();
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
		cl_mem memMask = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * mask.length, ptrMask, null);

		long[] globalWorkSize = new long[] { alpha.length };
		long[] localWorkSize = new long[] { workSize };

		cl_kernel maskKernel = CL.clCreateKernel(program, "getMask", null);

		CL.clSetKernelArg(maskKernel, 0, Sizeof.cl_mem, Pointer.to(memAlpha));
		CL.clSetKernelArg(maskKernel, 1, Sizeof.cl_mem, Pointer.to(memMask));

		double startTime = System.nanoTime();
		CL.clEnqueueNDRangeKernel(deviceManager.getQueue(), maskKernel, 1, null, globalWorkSize, localWorkSize, 0, null,
				null);
		setRuntime(getCalculatedRuntime() + (System.nanoTime() - startTime));
		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memMask, CL.CL_TRUE, 0, mask.length * Sizeof.cl_int, ptrMask,
				0, null, null);

		CL.clReleaseKernel(maskKernel);
		CL.clReleaseMemObject(memAlpha);
		CL.clReleaseMemObject(memMask);

		return mask;
	}

	/**
	 * Categorizes the pixels as interior, exterior, or border.
	 * 
	 * @param mask
	 *            The mask of the clone.
	 * @param dimensions
	 *            The dimensions of the image.
	 * @return The categories of pixels.
	 */
	public float[] categorizePixel(float[] mask, int[] dimensions) {
		float[] categories = new float[mask.length];
		Pointer ptrMask = Pointer.to(mask);
		Pointer ptrDimensions = Pointer.to(dimensions);
		Pointer ptrCategories = Pointer.to(categories);

		cl_mem memMask = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * mask.length, ptrMask, null);
		cl_mem memDimensions = CL.clCreateBuffer(deviceManager.getContext(),
				CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * dimensions.length, ptrDimensions,
				null);
		cl_mem memCategories = CL.clCreateBuffer(deviceManager.getContext(),
				CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * categories.length, ptrCategories,
				null);
		long[] globalWorkSize = new long[] { mask.length };
		long[] localWorkSize = new long[] { workSize };

		cl_kernel maskKernel = CL.clCreateKernel(program, "pixelType", null);

		CL.clSetKernelArg(maskKernel, 0, Sizeof.cl_mem, Pointer.to(memMask));
		CL.clSetKernelArg(maskKernel, 1, Sizeof.cl_mem, Pointer.to(memDimensions));
		CL.clSetKernelArg(maskKernel, 2, Sizeof.cl_mem, Pointer.to(memCategories));

		double startTime = System.nanoTime();
		CL.clEnqueueNDRangeKernel(deviceManager.getQueue(), maskKernel, 1, null, globalWorkSize, localWorkSize, 0, null,
				null);
		setRuntime(getCalculatedRuntime() + (System.nanoTime() - startTime));
		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memCategories, CL.CL_TRUE, 0,
				categories.length * Sizeof.cl_int, ptrCategories, 0, null, null);

		CL.clReleaseKernel(maskKernel);
		CL.clReleaseMemObject(memMask);
		CL.clReleaseMemObject(memDimensions);
		CL.clReleaseMemObject(memCategories);
		return categories;
	}

	/**
	 * Places the image on the other as a guess.
	 * 
	 * @param categories
	 *            The categories of the pixels.
	 * @param clone
	 *            The clone image.
	 * @param scene
	 *            The scene image.
	 * @return The inital guess data.
	 */
	public int[] initialGuess(float[] categories, float[] clone, float[] scene) {
		int[] result = new int[clone.length];
		Pointer ptrCategories = Pointer.to(categories);
		Pointer ptrClone = Pointer.to(clone);
		Pointer ptrScene = Pointer.to(scene);
		Pointer ptrResult = Pointer.to(result);

		cl_mem memCategories = CL.clCreateBuffer(deviceManager.getContext(),
				CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * categories.length, ptrCategories,
				null);
		cl_mem memClone = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * clone.length, ptrClone, null);
		cl_mem memScene = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * scene.length, ptrScene, null);
		cl_mem memResult = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * result.length, ptrResult, null);

		long[] globalWorkSize = new long[] { clone.length };
		long[] localWorkSize = new long[] { workSize };

		cl_kernel guessKernel = CL.clCreateKernel(program, "initialGuess", null);

		CL.clSetKernelArg(guessKernel, 0, Sizeof.cl_mem, Pointer.to(memCategories));
		CL.clSetKernelArg(guessKernel, 1, Sizeof.cl_mem, Pointer.to(memClone));
		CL.clSetKernelArg(guessKernel, 2, Sizeof.cl_mem, Pointer.to(memScene));
		CL.clSetKernelArg(guessKernel, 3, Sizeof.cl_mem, Pointer.to(memResult));

		double startTime = System.nanoTime();
		CL.clEnqueueNDRangeKernel(deviceManager.getQueue(), guessKernel, 1, null, globalWorkSize, localWorkSize, 0,
				null, null);
		setRuntime(getCalculatedRuntime() + (System.nanoTime() - startTime));
		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memResult, CL.CL_TRUE, 0, result.length * Sizeof.cl_int,
				ptrResult, 0, null, null);

		return result;
	}

	/**
	 * Converts floats to ints.
	 * 
	 * @param data
	 *            The float data.
	 * @return The int data.
	 */
	public int[] floatsToInts(float[] data) {
		int[] returnInts = new int[data.length];
		Pointer ptrData = Pointer.to(data);
		Pointer ptrResult = Pointer.to(returnInts);
		cl_mem memData = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * data.length, ptrData, null);
		cl_mem memResult = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * returnInts.length, ptrResult, null);

		long[] globalWorkSize = new long[] { data.length };
		long[] localWorkSize = new long[] { workSize };

		cl_kernel convertKernel = CL.clCreateKernel(program, "floatToInt", null);

		CL.clSetKernelArg(convertKernel, 0, Sizeof.cl_mem, Pointer.to(memData));
		CL.clSetKernelArg(convertKernel, 1, Sizeof.cl_mem, Pointer.to(memResult));
		double startTime = System.nanoTime();
		CL.clEnqueueNDRangeKernel(deviceManager.getQueue(), convertKernel, 1, null, globalWorkSize, localWorkSize, 0,
				null, null);
		setRuntime(getCalculatedRuntime() + (System.nanoTime() - startTime));
		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memResult, CL.CL_TRUE, 0, returnInts.length * Sizeof.cl_int,
				ptrResult, 0, null, null);
		CL.clReleaseKernel(convertKernel);
		CL.clReleaseMemObject(memResult);
		CL.clReleaseMemObject(memData);
		return returnInts;
	}

	/**
	 * Runs a blend calculation on the image.
	 * 
	 * @param categories
	 *            The category of each pixel.
	 * @param dimensions
	 *            The image dimensions.
	 * @param prevRed
	 *            The previous red result.
	 * @param prevGreen
	 *            The previous green result.
	 * @param prevBlue
	 *            The previous blue result.
	 * @param cloneRed
	 *            The clone red values.
	 * @param cloneGreen
	 *            The clone green values.
	 * @param cloneBlue
	 *            The clone blue values.
	 * @param sceneRed
	 *            The scene red values.
	 * @param sceneGreen
	 *            The scene green values.
	 * @param sceneBlue
	 *            The scene blue values.
	 * @param resultRed
	 *            The current iteration red results.
	 * @param resultGreen
	 *            The current iteration green results.
	 * @param resultBlue
	 *            The current iteration blue results.
	 */
	public void blend(float[] categories, int[] dimensions, float[] prevRed, float[] prevGreen, float[] prevBlue,
			float[] cloneRed, float[] cloneGreen, float[] cloneBlue, float[] sceneRed, float[] sceneGreen,
			float[] sceneBlue, float[] resultRed, float[] resultGreen, float[] resultBlue) {
		Pointer ptrCategories = Pointer.to(categories);
		Pointer ptrDimensions = Pointer.to(dimensions);
		Pointer ptrPrevRed = Pointer.to(prevRed);
		Pointer ptrPrevGreen = Pointer.to(prevGreen);
		Pointer ptrPrevBlue = Pointer.to(prevBlue);
		Pointer ptrCloneRed = Pointer.to(cloneRed);
		Pointer ptrCloneGreen = Pointer.to(cloneGreen);
		Pointer ptrCloneBlue = Pointer.to(cloneBlue);
		Pointer ptrSceneRed = Pointer.to(sceneRed);
		Pointer ptrSceneGreen = Pointer.to(sceneGreen);
		Pointer ptrSceneBlue = Pointer.to(sceneBlue);
		Pointer ptrResultRed = Pointer.to(resultRed);
		Pointer ptrResultGreen = Pointer.to(resultGreen);
		Pointer ptrResultBlue = Pointer.to(resultBlue);
		cl_mem memDimensions = CL.clCreateBuffer(deviceManager.getContext(),
				CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * dimensions.length, ptrDimensions,
				null);
		cl_mem memCategories = CL.clCreateBuffer(deviceManager.getContext(),
				CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * categories.length, ptrCategories,
				null);
		cl_mem memPrevRed = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * prevRed.length, ptrPrevRed, null);
		cl_mem memPrevGreen = CL.clCreateBuffer(deviceManager.getContext(),
				CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * prevGreen.length, ptrPrevGreen, null);
		cl_mem memPrevBlue = CL.clCreateBuffer(deviceManager.getContext(),
				CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * prevBlue.length, ptrPrevBlue, null);
		cl_mem memCloneRed = CL.clCreateBuffer(deviceManager.getContext(),
				CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * cloneRed.length, ptrCloneRed, null);
		cl_mem memCloneGreen = CL.clCreateBuffer(deviceManager.getContext(),
				CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * cloneGreen.length, ptrCloneGreen,
				null);
		cl_mem memCloneBlue = CL.clCreateBuffer(deviceManager.getContext(),
				CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * cloneBlue.length, ptrCloneBlue, null);
		cl_mem memSceneRed = CL.clCreateBuffer(deviceManager.getContext(),
				CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * sceneRed.length, ptrSceneRed, null);
		cl_mem memSceneGreen = CL.clCreateBuffer(deviceManager.getContext(),
				CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * sceneGreen.length, ptrSceneGreen,
				null);
		cl_mem memSceneBlue = CL.clCreateBuffer(deviceManager.getContext(),
				CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * sceneBlue.length, ptrSceneBlue, null);
		cl_mem memResultRed = CL.clCreateBuffer(deviceManager.getContext(),
				CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * resultRed.length, ptrResultRed, null);
		cl_mem memResultGreen = CL.clCreateBuffer(deviceManager.getContext(),
				CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * resultGreen.length, ptrResultGreen,
				null);
		cl_mem memResultBlue = CL.clCreateBuffer(deviceManager.getContext(),
				CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * resultBlue.length, ptrResultBlue,
				null);

		long[] globalWorkSize = new long[] { categories.length };
		long[] localWorkSize = new long[] { workSize };

		cl_kernel blendKernel = CL.clCreateKernel(program, "blend", null);

		CL.clSetKernelArg(blendKernel, 0, Sizeof.cl_mem, Pointer.to(memCategories));
		CL.clSetKernelArg(blendKernel, 1, Sizeof.cl_mem, Pointer.to(memDimensions));
		CL.clSetKernelArg(blendKernel, 2, Sizeof.cl_mem, Pointer.to(memPrevRed));
		CL.clSetKernelArg(blendKernel, 3, Sizeof.cl_mem, Pointer.to(memPrevGreen));
		CL.clSetKernelArg(blendKernel, 4, Sizeof.cl_mem, Pointer.to(memPrevBlue));
		CL.clSetKernelArg(blendKernel, 5, Sizeof.cl_mem, Pointer.to(memCloneRed));
		CL.clSetKernelArg(blendKernel, 6, Sizeof.cl_mem, Pointer.to(memCloneGreen));
		CL.clSetKernelArg(blendKernel, 7, Sizeof.cl_mem, Pointer.to(memCloneBlue));
		CL.clSetKernelArg(blendKernel, 8, Sizeof.cl_mem, Pointer.to(memSceneRed));
		CL.clSetKernelArg(blendKernel, 9, Sizeof.cl_mem, Pointer.to(memSceneGreen));
		CL.clSetKernelArg(blendKernel, 10, Sizeof.cl_mem, Pointer.to(memSceneBlue));
		CL.clSetKernelArg(blendKernel, 11, Sizeof.cl_mem, Pointer.to(memResultRed));
		CL.clSetKernelArg(blendKernel, 12, Sizeof.cl_mem, Pointer.to(memResultGreen));
		CL.clSetKernelArg(blendKernel, 13, Sizeof.cl_mem, Pointer.to(memResultBlue));
		double startTime = System.nanoTime();
		CL.clEnqueueNDRangeKernel(deviceManager.getQueue(), blendKernel, 1, null, globalWorkSize, localWorkSize, 0,
				null, null);
		setRuntime(getCalculatedRuntime() + (System.nanoTime() - startTime));
		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memResultRed, CL.CL_TRUE, 0, resultRed.length * Sizeof.cl_int,
				ptrResultRed, 0, null, null);
		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memResultGreen, CL.CL_TRUE, 0,
				resultGreen.length * Sizeof.cl_int, ptrResultGreen, 0, null, null);
		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memResultBlue, CL.CL_TRUE, 0,
				resultBlue.length * Sizeof.cl_int, ptrResultBlue, 0, null, null);
		CL.clReleaseKernel(blendKernel);
		CL.clReleaseMemObject(memResultBlue);
		CL.clReleaseMemObject(memResultGreen);
		CL.clReleaseMemObject(memResultRed);
		CL.clReleaseMemObject(memSceneBlue);
		CL.clReleaseMemObject(memSceneGreen);
		CL.clReleaseMemObject(memSceneRed);
		CL.clReleaseMemObject(memCloneBlue);
		CL.clReleaseMemObject(memCloneGreen);
		CL.clReleaseMemObject(memCloneRed);
		CL.clReleaseMemObject(memPrevBlue);
		CL.clReleaseMemObject(memPrevGreen);
		CL.clReleaseMemObject(memPrevRed);
		CL.clReleaseMemObject(memCategories);
		CL.clReleaseMemObject(memDimensions);
	}

	/**
	 * Gets the runtime.
	 * 
	 * @return the calculatedRuntime.
	 */
	public double getCalculatedRuntime() {
		return calculatedRuntime;
	}

	/**
	 * Sets the runtime.
	 * 
	 * @param calculatedRuntime
	 *            the calculatedRuntime to set.
	 */
	public void setRuntime(double calculatedRuntime) {
		this.calculatedRuntime = calculatedRuntime;
	}

	/**
	 * Releases the program.
	 */
	public void closeProgram() {
		CL.clReleaseProgram(program);
	}
}

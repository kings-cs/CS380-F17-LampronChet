/**
 * 
 */
package algorithms;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;

import org.jocl.CL;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_mem;

import parallel.JoclInitializer;

/**
 * Modifies picture to mosaic in parallel.
 * 
 * @author Chet Lampron
 *
 */
public class MosaicModifierParallel extends MosaicModifier {

	/** The device manager. */
	private JoclInitializer deviceManager;

	/**
	 * Constructs a mosaic modifier for parallel computation.
	 * 
	 * @param numOfTiles
	 *            The number of tiles.
	 * @param aDeviceManager
	 *            The current device manager.
	 */
	public MosaicModifierParallel(int numOfTiles, JoclInitializer aDeviceManager) {
		super(numOfTiles);
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
		int[] tilePoints = getTilePoints(super.getTiles(), sourceData);

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
		return null;
	}
}

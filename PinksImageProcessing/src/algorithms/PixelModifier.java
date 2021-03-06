package algorithms;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.FileNotFoundException;

import parallel.JoclInitializer;

/**
 * Generic pixel modifier class.
 * 
 * @author Chet Lampron
 *
 */
public abstract class PixelModifier {
	/** The red mask. */
	protected static final int ALPHA_MASK = 0xff000000;
	/** The red offset. */
	protected static final int ALPHA_OFFSET = 24;

	/** The red mask. */
	protected static final int RED_MASK = 0x00ff0000;
	/** The red offset. */
	protected static final int RED_OFFSET = 16;

	/** The green mask. */
	private static final int GREEN_MASK = 0x0000ff00;
	/** The green offset. */
	private static final int GREEN_OFFSET = 8;

	/** The blue mask. */
	private static final int BLUE_MASK = 0x000000ff;
	/** The blue offset. */
	private static final int BLUE_OFFSET = 0;

	/**
	 * Modifies each pixel to change the image.
	 * 
	 * @param image
	 *            The original image.
	 * @return The modified image.
	 * @throws FileNotFoundException
	 *             not thrown.
	 */
	public abstract BufferedImage modifyPixel(BufferedImage image) throws FileNotFoundException;

	/**
	 * Unwraps the image from abstractions.
	 * 
	 * @param image
	 *            The image to work on.
	 * @return The source data of the image.
	 */
	public int[] unwrapImage(BufferedImage image) {
		WritableRaster source = image.getRaster();
		DataBuffer sourceBuffer = source.getDataBuffer();
		DataBufferInt sourceBytes = (DataBufferInt) sourceBuffer;
		int[] sourceData = sourceBytes.getData();
		return sourceData;
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
	public static int getWorkSize(JoclInitializer deviceManager, int[] data) {
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
	 * Repackages the image.
	 * 
	 * @param resultData
	 *            The result data of the image.
	 * @param image
	 *            The image.
	 */
	public void packageImage(int[] resultData, BufferedImage image) {
		DataBufferInt resultDataBuffer = new DataBufferInt(resultData, resultData.length);
		Raster resultRastor = Raster.createRaster(image.getRaster().getSampleModel(), resultDataBuffer,
				new Point(0, 0));
		image.setData(resultRastor);
	}

	/**
	 * Gets the red offset.
	 * 
	 * @return the redOffset
	 */
	public static int getRedOffset() {
		return RED_OFFSET;
	}

	/**
	 * Gets the red mask.
	 * 
	 * @return the redMask
	 */
	public static int getRedMask() {
		return RED_MASK;
	}

	/**
	 * Returns the alpha mask.
	 * 
	 * @return the alphaMask
	 */
	public static int getAlphaMask() {
		return ALPHA_MASK;
	}

	/**
	 * Returns the alpha offset.
	 * 
	 * @return the alphaOffset
	 */
	public static int getAlphaOffset() {
		return ALPHA_OFFSET;
	}

	/**
	 * Returns the blue offset.
	 * 
	 * @return the blueOffset
	 */
	public static int getBlueOffset() {
		return BLUE_OFFSET;
	}

	/**
	 * Returns the green offset.
	 * 
	 * @return the greenOffset
	 */
	public static int getGreenOffset() {
		return GREEN_OFFSET;
	}

	/**
	 * Returns the green mask.
	 * 
	 * @return the greenMask
	 */
	public static int getGreenMask() {
		return GREEN_MASK;
	}

	/**
	 * returns the blue mask.
	 * 
	 * @return the blueMask
	 */
	public static int getBlueMask() {
		return BLUE_MASK;
	}
}

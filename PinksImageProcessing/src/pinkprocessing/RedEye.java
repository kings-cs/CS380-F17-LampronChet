package pinkprocessing;

import algorithms.PixelModifier;
import parallel.JoclInitializer;

/**
 * The red eye removal helper methods.
 * 
 * @author Chet Lampron.
 *
 */
public class RedEye {
	/** The device manager. */
	private JoclInitializer deviceManager;

	/**
	 * Constructs a RedEye helper object.
	 * 
	 * @param theDeviceManager
	 *            The device manager being used.
	 */
	public RedEye(JoclInitializer theDeviceManager) {
		deviceManager = theDeviceManager;
	}

	/**
	 * Calculates the average of the temple.
	 * 
	 * @param data
	 *            The data of the image.
	 * @return The average.
	 */
	public int[] calculateTemplateAverage(int[] data) {
		int redTotal = 0;
		int blueTotal = 0;
		int greenTotal = 0;

		for (int i = 0; i < data.length; i++) {
			int pixel = data[i];
			int alpha = (pixel & PixelModifier.getAlphaMask()) >> PixelModifier.getAlphaOffset();
			int red = (pixel & PixelModifier.getRedMask()) >> PixelModifier.getRedOffset();
			int green = (pixel & PixelModifier.getGreenMask()) >> PixelModifier.getGreenOffset();
			int blue = (pixel & PixelModifier.getBlueMask()) >> PixelModifier.getBlueOffset();
			redTotal += red;
			blueTotal += blue;
			greenTotal += green;
		}
		int redAvg = redTotal / data.length;
		int greenAvg = greenTotal / data.length;
		int blueAvg = blueTotal / data.length;
		int[] averages = { redAvg, greenAvg, blueAvg };
		return averages;
	}
}

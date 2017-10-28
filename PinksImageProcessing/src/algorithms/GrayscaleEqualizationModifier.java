/**
 * 
 */
package algorithms;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;

import javax.swing.JOptionPane;

import parallel.JoclInitializer;
import pinkprocessing.GrayscaleEqualization;

/**
 * 
 * @author Chet
 *
 */
public class GrayscaleEqualizationModifier extends PixelModifier {
	/** The device manager. */
	private JoclInitializer deviceManager;

	/**
	 * Constructs an object for Grayscale equalization.
	 * 
	 * @param aDeviceManager
	 *            The device manager.
	 */
	public GrayscaleEqualizationModifier(JoclInitializer aDeviceManager) {
		deviceManager = aDeviceManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see algorithms.PixelModifier#modifyPixel(java.awt.image.BufferedImage)
	 */
	@Override
	public BufferedImage modifyPixel(BufferedImage image) throws FileNotFoundException {
		int width = image.getWidth();
		int height = image.getHeight();

		int[] sourceData = super.unwrapImage(image);
		int[] resultData = new int[sourceData.length];

		GrayscaleEqualization equalizer = new GrayscaleEqualization();
		int[] histogramResult = equalizer.calculateHistogram(deviceManager, sourceData, getWorkSize(deviceManager, sourceData));
		int[] cumulativeFrequencyResult = equalizer.distributeCumulativeFrequency(histogramResult);
		int[] idealizedHistogram = equalizer.calculateIdealizedHistogram(cumulativeFrequencyResult, sourceData.length);
		cumulativeFrequencyResult = equalizer.distributeCumulativeFrequency(idealizedHistogram);
		int[] mapDesign = equalizer.designMap(cumulativeFrequencyResult);
		int[] map = equalizer.getMap(mapDesign, sourceData);
		int calculatedTime = equalizer.getTime();
		resultData = map;

		packageImage(resultData, image);
		JOptionPane.showMessageDialog(null, "Total Time: " + (calculatedTime));
		return image;
	}

}

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
 * The pixel modifier class for Grayscale equalization.
 * 
 * @author Chet Lampron
 *
 */
public class GrayscaleEqualizationModifier extends PixelModifier {
	/** The device manager. */
	private JoclInitializer deviceManager;
	/** Determines which kernel to use. */
	private boolean isOptimized;

	/**
	 * Constructs an object for Grayscale equalization.
	 * 
	 * @param aDeviceManager
	 *            The device manager.
	 * @param isOptimized
	 *            Whether the regular kernel should be called or not.
	 */
	public GrayscaleEqualizationModifier(JoclInitializer aDeviceManager, boolean isOptimized) {
		deviceManager = aDeviceManager;
		this.isOptimized = isOptimized;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see algorithms.PixelModifier#modifyPixel(java.awt.image.BufferedImage)
	 */
	@Override
	public BufferedImage modifyPixel(BufferedImage image) throws FileNotFoundException {

		int[] sourceData = super.unwrapImage(image);
		int[] resultData = new int[sourceData.length];

		GrayscaleEqualization equalizer = new GrayscaleEqualization();
		int[] histogramResult = equalizer.calculateHistogram(deviceManager, sourceData,
				getWorkSize(deviceManager, sourceData), isOptimized);
		int[] cumulativeFrequencyResult = equalizer.distributeCumulativeFrequency(histogramResult);
		int[] idealizedHistogram = equalizer.calculateIdealizedHistogram(cumulativeFrequencyResult, sourceData.length,
				getWorkSize(deviceManager, cumulativeFrequencyResult));
		int[] idealizedCumulativeFrequencyResult = equalizer.distributeCumulativeFrequency(idealizedHistogram);
		int[] mapDesign = equalizer.designMap(idealizedCumulativeFrequencyResult, cumulativeFrequencyResult,
				getWorkSize(deviceManager, cumulativeFrequencyResult));
		int[] map = equalizer.getMap(mapDesign, sourceData);
		double calculatedTime = equalizer.getTime();
		resultData = map;

		packageImage(resultData, image);
		JOptionPane.showMessageDialog(null, "Total Time: " + (calculatedTime) / 1000000);
		return image;
	}

}

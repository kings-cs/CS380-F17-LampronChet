package algorithms;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;

import javax.swing.JOptionPane;

import parallel.JoclInitializer;
import pinkprocessing.MergeClone;
import pinkprocessing.RedEye;
/**
 * Class to do a seamless clone.
 * @author Chet Lampron
 *
 */
public class SeamlessCloneModifier extends PixelModifier {
	/** The clone image. */
	private BufferedImage clone;
	/** The device manager. */
	private JoclInitializer deviceManager;
	/** The number of iterations. */
	private int iterations;

	/**
	 * Constructs a seamless clone modifier.
	 * 
	 * @param clone
	 *            The clone image.
	 * @param deviceManager
	 *            The device manager.
	 * @param iterations
	 *            The number of iterations.
	 */
	public SeamlessCloneModifier(BufferedImage clone, JoclInitializer deviceManager, int iterations) {
		this.clone = clone;
		this.deviceManager = deviceManager;
		this.iterations = iterations;
	}

	@Override
	public BufferedImage modifyPixel(BufferedImage image) throws FileNotFoundException {
		int[] imageData = unwrapImage(image);
		int[] cloneData = unwrapImage(clone);
		MergeClone merge = new MergeClone(deviceManager);
		int[] alpha = new int[cloneData.length];
		int[] red = new int[cloneData.length];
		int[] green = new int[cloneData.length];
		int[] blue = new int[cloneData.length];
		RedEye splitter = new RedEye(deviceManager);
		splitter.splitChannels(cloneData, red, green, blue, alpha);
		float[] resultAlpha = new float[alpha.length];
		float[] resultRed = new float[red.length];
		float[] resultBlue = new float[blue.length];
		float[] resultGreen = new float[green.length];
		float[] floatClone = new float[cloneData.length];
		float[] floatImage = new float[imageData.length];
		float[] arbitraryFiller = new float[cloneData.length];
		merge.convertToFloat(alpha, red, green, blue, resultAlpha, resultRed, resultGreen, resultBlue);
		merge.convertToFloat(cloneData, imageData, new int[cloneData.length], new int[cloneData.length], floatClone, floatImage, arbitraryFiller, arbitraryFiller);
		float[] mask = merge.getMask(resultAlpha);
		int[] dimensions = {image.getWidth(), image.getHeight()};
		float[] categories = merge.categorizePixel(mask, dimensions);
		
		float[] guessResult = merge.initialGuess(categories, floatClone, floatImage);
		int[] guessResultInt = merge.floatsToInts(guessResult);
		packageImage(guessResultInt, image);
		JOptionPane.showMessageDialog(null, merge.getCalculatedRuntime() / 1000000.0 + "ms");
		return image;
	}
}

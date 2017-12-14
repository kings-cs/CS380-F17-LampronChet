package algorithms;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;

import javax.swing.JOptionPane;

import parallel.JoclInitializer;
import pinkprocessing.MergeClone;
import pinkprocessing.RedEye;

/**
 * Class to do a seamless clone.
 * 
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

		float[] floatAlpha = new float[alpha.length];
		float[] floatRed = new float[red.length];
		float[] floatBlue = new float[blue.length];
		float[] floatGreen = new float[green.length];
		float[] floatClone = new float[cloneData.length];
		float[] floatImage = new float[imageData.length];
		float[] arbitraryFiller = new float[cloneData.length];
		merge.convertToFloat(alpha, red, green, blue, floatAlpha, floatRed, floatGreen, floatBlue);
		merge.convertToFloat(cloneData, imageData, new int[cloneData.length], new int[cloneData.length], floatClone,
				floatImage, arbitraryFiller, arbitraryFiller);
		float[] mask = merge.getMask(floatAlpha);
		int[] dimensions = { image.getWidth(), image.getHeight() };
		float[] categories = merge.categorizePixel(mask, dimensions);

		int[] guessResult = merge.initialGuess(categories, floatClone, floatImage);
		int[] prevAlpha = new int[cloneData.length];
		int[] prevRed = new int[cloneData.length];
		int[] prevGreen = new int[cloneData.length];
		int[] prevBlue = new int[cloneData.length];
		splitter.splitChannels(guessResult, prevRed, prevGreen, prevBlue, prevAlpha);
		float[] floatPrevAlpha = new float[prevAlpha.length];
		float[] floatPrevRed = new float[prevAlpha.length];
		float[] floatPrevGreen = new float[prevAlpha.length];
		float[] floatPrevBlue = new float[prevAlpha.length];
		merge.convertToFloat(prevAlpha, prevRed, prevGreen, prevBlue, floatPrevAlpha, floatPrevRed, floatPrevGreen,
				floatPrevBlue);
		int[] sceneRed = new int[imageData.length];
		int[] sceneGreen = new int[imageData.length];
		int[] sceneBlue = new int[imageData.length];
		int[] sceneAlpha = new int[imageData.length];
		splitter.splitChannels(imageData, sceneRed, sceneGreen, sceneBlue, sceneAlpha);
		float[] floatSceneAlpha = new float[prevAlpha.length];// final alpha channel.
		float[] floatSceneRed = new float[prevAlpha.length];
		float[] floatSceneGreen = new float[prevAlpha.length];
		float[] floatSceneBlue = new float[prevAlpha.length];
		merge.convertToFloat(sceneAlpha, sceneRed, sceneGreen, sceneBlue, floatSceneAlpha, floatSceneRed,
				floatSceneGreen, floatSceneBlue);
		int[] cloneRed = new int[imageData.length];
		int[] cloneGreen = new int[imageData.length];
		int[] cloneBlue = new int[imageData.length];
		int[] cloneAlpha = new int[imageData.length];
		splitter.splitChannels(cloneData, cloneRed, cloneGreen, cloneBlue, cloneAlpha);
		float[] floatCloneAlpha = new float[prevAlpha.length];
		float[] floatCloneRed = new float[prevAlpha.length];
		float[] floatCloneGreen = new float[prevAlpha.length];
		float[] floatCloneBlue = new float[prevAlpha.length];
		merge.convertToFloat(cloneAlpha, cloneRed, cloneGreen, cloneBlue, floatCloneAlpha, floatCloneRed,
				floatCloneGreen, floatCloneBlue);
		float[] resultRed = new float[imageData.length];
		float[] resultGreen = new float[imageData.length];
		float[] resultBlue = new float[imageData.length];

		for (int i = 0; i < iterations; i++) {
			merge.blend(categories, dimensions, floatPrevRed, floatPrevGreen, floatPrevBlue, floatCloneRed,
					floatCloneGreen, floatCloneBlue, floatSceneRed, floatSceneGreen, floatSceneBlue, resultRed,
					resultGreen, resultBlue);
			floatPrevRed = resultRed;
			floatPrevGreen = resultGreen;
			floatPrevBlue = resultBlue;
		}
		int[] redResult = merge.floatsToInts(resultRed);
		int[] greenResult = merge.floatsToInts(resultGreen);
		int[] blueResult = merge.floatsToInts(resultBlue);
		if (iterations == 0) {
			redResult = prevRed;
			greenResult = prevGreen;
			blueResult = prevBlue;
		}
		int[] result = splitter.combineChannels(redResult, greenResult, blueResult, sceneAlpha);
		packageImage(result, image);
		JOptionPane.showMessageDialog(null, merge.getCalculatedRuntime() / 1000000.0 + "ms");
		merge.closeProgram();
		return image;
	}
}

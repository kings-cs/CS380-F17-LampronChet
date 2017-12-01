/**
 * 
 */
package algorithms;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;

import javax.swing.JOptionPane;

import parallel.JoclInitializer;
import pinkprocessing.Radix;
import pinkprocessing.RedEye;

/**
 * Class to remove red eye from image.
 * 
 * @author Chet Lampron.
 *
 */
public class RedEyeModifier extends PixelModifier {
	
	/** The device manager. */
	private JoclInitializer deviceManager;
	
	/** The template red eye. */
	private BufferedImage redEye;
	/**
	 * Constructs a red eye removal object.
	 * 
	 * @param deviceManager
	 *            The device manager.
	 * @param template
	 *            The red eye template.
	 */
	public RedEyeModifier(JoclInitializer deviceManager, BufferedImage template) {
		this.deviceManager = deviceManager;
		redEye = template;
	}

	@Override
	public BufferedImage modifyPixel(BufferedImage image) throws FileNotFoundException {
		// TODO Auto-generated method stub
		int[] sourceData = unwrapImage(image);
		int[] templateData = unwrapImage(redEye);
		int[] dimensions = {image.getWidth(), image.getHeight(), redEye.getWidth(), redEye.getHeight()};
		RedEye redEyeProccess = new RedEye(deviceManager);
		int[] redDifferences = new int[templateData.length];
		int[] greenDifferences = new int[templateData.length];
		int[] blueDifferences = new int[templateData.length];
		int[] templateAverages = redEyeProccess.calculateTemplateAverage(templateData);
		int[] tempSumDiff = redEyeProccess.sumDifferenceTemplate(templateAverages, redDifferences, greenDifferences, blueDifferences);

		float[] nccValues = redEyeProccess.getNcc(sourceData, tempSumDiff, redDifferences, greenDifferences, blueDifferences, dimensions);
		float smallest = redEyeProccess.findSmallest(nccValues);

		int[] convertedNcc = redEyeProccess.convertNcc(smallest, nccValues);
	
		int workSize = PixelModifier.getWorkSize(deviceManager, convertedNcc);
		Radix radix = new Radix(workSize, deviceManager);
		int[] startKeys = new int[convertedNcc.length];
		int[] endKeys = new int[convertedNcc.length];
		int[] resultSort = new int[convertedNcc.length];
		for(int i = 0; i < startKeys.length; i++) {
			startKeys[i] = i;
		}
		radix.fullSort(convertedNcc, resultSort, startKeys, endKeys);
		
		int[] modifyDimensions = {image.getWidth(), image.getHeight(), redEye.getWidth(), redEye.getHeight(), endKeys[endKeys.length - 1]};
		redEyeProccess.reduceRedness(sourceData, modifyDimensions);
		packageImage(sourceData, image);
		double time = redEyeProccess.getCalculatedTime() + radix.getRuntime();
		JOptionPane.showMessageDialog(null, time / 1000000 + "ms");
		return image;
	}

}

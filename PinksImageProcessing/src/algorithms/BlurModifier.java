package algorithms;

import java.awt.image.BufferedImage;

import javax.swing.JOptionPane;

//import javax.swing.JOptionPane;

/**
 * Blur algorithm in sequential.
 * 
 * @author Chet
 *
 */
public class BlurModifier extends PixelModifier {
	/** The field for the stencil. */
	private final double[] stencil = { 0.0232468, 0.0338240, 0.0383276, 0.0338240, 0.0232468, 0.0338240, 0.0492136,
			0.0557663, 0.0492136, 0.0338240, 0.0383276, 0.0557663, 0.0631915, 0.0557663, 0.0383276, 0.0338240,
			0.0492136, 0.0557663, 0.0492136, 0.0338240, 0.0232468, 0.0338240, 0.0383276, 0.0338240, 0.0232468 };

	@Override
	public BufferedImage modifyPixel(BufferedImage image) {

		int width = image.getWidth();
		int height = image.getHeight();

		int[] sourceData = super.unwrapImage(image);

		int[] redArray = new int[sourceData.length];
		int[] blueArray = new int[sourceData.length];
		int[] greenArray = new int[sourceData.length];
		int[] alphaArray = new int[sourceData.length];
		int[] modifiedRedArray = new int[redArray.length];
		int[] modifiedGreenArray = new int[greenArray.length];
		int[] modifiedBlueArray = new int[blueArray.length];
		// long startTime = System.nanoTime();

		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				int index = row * width + col;
				int alpha = (sourceData[index] & PixelModifier.ALPHA_MASK) >> PixelModifier.getAlphaOffset();
				int red = (sourceData[index] & PixelModifier.RED_MASK) >> PixelModifier.RED_OFFSET;
				int green = (sourceData[index] & PixelModifier.GREEN_MASK) >> PixelModifier.getGreenOffset();
				int blue = (sourceData[index] & PixelModifier.BLUE_MASK) >> PixelModifier.getBlueOffset();

				redArray[index] = red;
				blueArray[index] = blue;
				greenArray[index] = green;
				alphaArray[index] = alpha;
			}
		}
		int[] resultData = new int[sourceData.length];
		double startTime = System.nanoTime();
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				int index = row * width + col;

				int count = 0;
				double redModify = 0;
				double greenModify = 0;
				double blueModify = 0;
				for (int stenRow = row - 2; stenRow <= row + 2; stenRow++) {
					for (int stenCol = col - 2; stenCol <= col + 2; stenCol++) {

						int newRow = stenRow;
						int newCol = stenCol;

						if (newRow < 0) {
							newRow = 0;
						} else if (newRow >= height) {
							newRow = height - 1;
						}
						if (newCol < 0) {
							newCol = 0;
						} else if (newCol >= width) {
							newCol = width - 1;
						}

						int anIndex = newRow * width + newCol;

						redModify += redArray[anIndex] * stencil[count];
						greenModify += greenArray[anIndex] * stencil[count];
						blueModify += blueArray[anIndex] * stencil[count];
						count++;

					}
				}
				modifiedRedArray[index] = (int) redModify;
				modifiedGreenArray[index] = (int) greenModify;
				modifiedBlueArray[index] = (int) blueModify;

			}
		}
		for (int i = 0; i < sourceData.length; i++) {

			int newRed = modifiedRedArray[i];
			int newGreen = modifiedGreenArray[i];
			int newBlue = modifiedBlueArray[i];
			int alpha = alphaArray[i];
			int newPixel = (alpha << getAlphaOffset()) | (newRed << RED_OFFSET) | (newBlue << getBlueOffset())
					| (newGreen << getGreenOffset());

			resultData[i] = newPixel;
		}
		JOptionPane.showMessageDialog(null, "Total Time: " + (System.nanoTime() - startTime) / 1000000.0 + "ms");

		packageImage(resultData, image);
		return image;
	}
}

package algorithms;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;

/**
 * Blur algorithm in sequential.
 * 
 * @author Chet
 *
 */
public class BlurModifier extends PixelModifier {
	/** The field for the stencil. */
	private final double[] stencil = { 0.0232468, 0.0338240, 0.0383276, 0.0338240, 0.0232468, 0.0338240, 0.0492136,
			0.0492136, 0.0557663, 0.0492136, 0.0338240, 0.0383276, 0.0557663, 0.0631915, 0.0557663, 0.0383276,
			0.0338240, 0.0492136, 0.0557663, 0.0492136, 0.0338240, 0.0232468, 0.0338240, 0.0383276, 0.0338240,
			0.0232468 };

	@Override
	public BufferedImage modifyPixel(BufferedImage image) {
		// long startTime = System.nanoTime();
		int width = image.getWidth();
		int height = image.getHeight();

		int[] sourceData = super.unwrapImage(image);

		int[] redArray = new int[sourceData.length];
		int[] blueArray = new int[sourceData.length];
		int[] greenArray = new int[sourceData.length];
		int[] alphaArray = new int[sourceData.length];
		int[] modifiedRedArray = null;
		int[] modifiedGreenArray = null;
		int[] modifiedBlueArray = null;
		
		for (int i = 0; i < sourceData.length; i++) {
			int alpha = (sourceData[i] & PixelModifier.ALPHA_MASK) >> PixelModifier.ALPHA_OFFSET;
			int red = (sourceData[i] & PixelModifier.RED_MASK) >> PixelModifier.RED_OFFSET;
			int green = (sourceData[i] & PixelModifier.GREEN_MASK) >> PixelModifier.GREEN_OFFSET;
			int blue = (sourceData[i] & PixelModifier.BLUE_MASK) >> PixelModifier.BLUE_OFFSET;

			redArray[i] = red;
			blueArray[i] = blue;
			greenArray[i] = green;
			alphaArray[i] = alpha;
		}
		System.out.println("here0");
		int[] resultData = new int[sourceData.length];

		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				int index = row * width + col;

				modifiedRedArray = new int[redArray.length];
				modifiedGreenArray = new int[greenArray.length];
				modifiedBlueArray = new int[blueArray.length];
				
				int count = 0;
				int redModify = 0;
				int greenModify = 0;
				int blueModify = 0;
				System.out.println("before inner fors");
				for(int stenRow = row - 2; stenRow <= row + 2; stenRow++) {
					for(int stenCol = col - 2; stenCol <= col + 2; stenCol++) {
						if(stenRow >= 0 && stenRow <= height && stenCol >= 0 && stenCol <= width) {
							int anIndex = stenRow * width + stenCol;
							redModify += (int) redArray[anIndex] * stencil[count];
							greenModify += (int) greenArray[anIndex] * stencil[count];
							blueModify += (int) blueArray[anIndex] * stencil[count];
						}
					}
				}
				System.out.println("after");
				modifiedRedArray[index] = redModify;
				modifiedGreenArray[index] = greenModify;
				modifiedBlueArray[index] = blueModify;
				



			}
		}
		System.out.println("here2");
		for (int i = 0; i < sourceData.length; i++) {
			

			int newRed = modifiedRedArray[i];
			int newGreen = modifiedGreenArray[i];
			int newBlue = modifiedBlueArray[i];
			int alpha = alphaArray[i];
			int newPixel = (alpha << ALPHA_OFFSET) | (newRed << RED_OFFSET) | (newBlue << BLUE_OFFSET)
					| (newGreen << GREEN_OFFSET);

			resultData[i] = newPixel;
		}
		DataBufferInt resultDataBuffer = new DataBufferInt(resultData, resultData.length);
		Raster resultRastor = Raster.createRaster(image.getRaster().getSampleModel(), resultDataBuffer,
				new Point(0, 0));
		image.setData(resultRastor);
		// JOptionPane.showMessageDialog(null, "Total Time: " + (System.nanoTime() -
		// startTime) / 1000000 + "ms");
		return image;
	}
}

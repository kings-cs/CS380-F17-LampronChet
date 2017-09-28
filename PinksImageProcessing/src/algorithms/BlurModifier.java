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
		int[] modifiedRedArray = new int[redArray.length];
		int[] modifiedGreenArray = new int[greenArray.length];
		int[] modifiedBlueArray = new int[blueArray.length];

		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				int index = row * width + col;
				int alpha = (sourceData[index] & PixelModifier.ALPHA_MASK) >> PixelModifier.ALPHA_OFFSET;
				int red = (sourceData[index] & PixelModifier.RED_MASK) >> PixelModifier.RED_OFFSET;
				int green = (sourceData[index] & PixelModifier.GREEN_MASK) >> PixelModifier.GREEN_OFFSET;
				int blue = (sourceData[index] & PixelModifier.BLUE_MASK) >> PixelModifier.BLUE_OFFSET;

				redArray[index] = red;
				blueArray[index] = blue;
				greenArray[index] = green;
				alphaArray[index] = alpha;
			}
		}
		int[] resultData = new int[sourceData.length];

		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				int index = row * width + col;

				int count = 0;
				int redModify = 0;
				int greenModify = 0;
				int blueModify = 0;
				for (int stenRow = row - 2; stenRow <= row + 2; stenRow++) {
					for (int stenCol = col - 2; stenCol <= col + 2; stenCol++) {
					/*	
						if (stenRow >= 0 && stenRow < height && stenCol >= 0 && stenCol < width) {
							int anIndex = stenRow * width + stenCol;
							redModify += (int) redArray[anIndex] * stencil[count];
							greenModify += (int) greenArray[anIndex] * stencil[count];
							blueModify += (int) blueArray[anIndex] * stencil[count];
							count++;
						} else if (stenRow < 0 && stenCol < 0) {
							int newRow = 0;
							int newCol = 0;
							int anIndex = newRow * width + newCol;
							redModify += (int) redArray[anIndex] * stencil[count];
							greenModify += (int) greenArray[anIndex] * stencil[count];
							blueModify += (int) blueArray[anIndex] * stencil[count];
							count++;
						} else if (stenRow < 0 && stenCol >= 0) {
							int newRow = 0;
							int newCol = stenCol;
							int anIndex = newRow * width + newCol;
							redModify += (int) redArray[anIndex] * stencil[count];
							greenModify += (int) greenArray[anIndex] * stencil[count];
							blueModify += (int) blueArray[anIndex] * stencil[count];
							count++;
						} else if (stenRow >= 0 && stenCol < 0) {
							int newRow = stenRow;
							int newCol = 0;
							int anIndex = newRow * width + newCol;
							redModify += (int) redArray[anIndex] * stencil[count];
							greenModify += (int) greenArray[anIndex] * stencil[count];
							blueModify += (int) blueArray[anIndex] * stencil[count];
							count++;
						} else if (stenRow < 0 && stenCol >= width) {
							int newRow = 0;
							int newCol = width - 1;
							int anIndex = newRow * width + newCol;
							redModify += (int) redArray[anIndex] * stencil[count];
							greenModify += (int) greenArray[anIndex] * stencil[count];
							blueModify += (int) blueArray[anIndex] * stencil[count];
							count++;
						} else if (stenRow >= 0 && stenCol >= width) {
							int newRow = stenRow;
							int newCol = width - 1;
							int anIndex = newRow * width + newCol;
							redModify += (int) redArray[anIndex] * stencil[count];
							greenModify += (int) greenArray[anIndex] * stencil[count];
							blueModify += (int) blueArray[anIndex] * stencil[count];
							count++;
						} else if (stenRow < 0 && stenCol < width) {
							int newRow = 0;
							int newCol = stenCol;
							int anIndex = newRow * width + newCol;
							redModify += (int) redArray[anIndex] * stencil[count];
							greenModify += (int) greenArray[anIndex] * stencil[count];
							blueModify += (int) blueArray[anIndex] * stencil[count];
							count++;
						} else if (stenRow >= height && stenCol >= width) {
							int newRow = height - 1;
							int newCol = width - 1;
							int anIndex = newRow * width + newCol;
							redModify += (int) redArray[anIndex] * stencil[count];
							greenModify += (int) greenArray[anIndex] * stencil[count];
							blueModify += (int) blueArray[anIndex] * stencil[count];
							count++;
						} else if (stenRow < height && stenCol >= width) {
							int newRow = stenRow;
							int newCol = width - 1;
							int anIndex = newRow * width + newCol;
							redModify += (int) redArray[anIndex] * stencil[count];
							greenModify += (int) greenArray[anIndex] * stencil[count];
							blueModify += (int) blueArray[anIndex] * stencil[count];
							count++;
						} else if (stenRow >= height && stenCol < width) {
							int newRow = height - 1;
							int newCol = stenCol;
							int anIndex = newRow * width + newCol;
							redModify += (int) redArray[anIndex] * stencil[count];
							greenModify += (int) greenArray[anIndex] * stencil[count];
							blueModify += (int) blueArray[anIndex] * stencil[count];
							count++;
						} else if (stenRow >= height && stenCol < 0) {
							int newRow = height - 1;
							int newCol = 0;
							int anIndex = newRow * width + newCol;
							redModify += (int) redArray[anIndex] * stencil[count];
							greenModify += (int) greenArray[anIndex] * stencil[count];
							blueModify += (int) blueArray[anIndex] * stencil[count];
							count++;
						}*/
						
						int newRow = stenRow;
						int newCol = stenCol;
						
						if(newRow < 0) {
							newRow = 0;
						}else if(newRow >= height) {
							newRow = height - 1;
						}
						if(newCol < 0) {
							newCol = 0;
						}else if(newCol >= width) {
							newCol = width - 1;
						}
						
						int anIndex = newRow * width + newCol;
						
						redModify += redArray[anIndex] * stencil[count];
						greenModify += greenArray[anIndex] * stencil[count];
						blueModify += blueArray[anIndex] * stencil[count];
						count++;
						//System.out.println("row: " + row + " col: " + col + " count: " + count);

					}
				}
				modifiedRedArray[index] = redModify;
				modifiedGreenArray[index] = greenModify;
				modifiedBlueArray[index] = blueModify;

			}
		}
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

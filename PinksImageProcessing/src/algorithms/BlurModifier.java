package algorithms;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;

/**
 * @author Chet
 *
 */
public class BlurModifier extends PixelModifier {
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

		System.out.println(sourceData[0]);
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

		int[] resultData = new int[sourceData.length];

		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				int index = row * width + col;
				int pixel = sourceData[index];

				int alpha = (pixel & PixelModifier.ALPHA_MASK) >> PixelModifier.ALPHA_OFFSET;
				int red = (pixel & PixelModifier.RED_MASK) >> PixelModifier.RED_OFFSET;
				int green = (pixel & PixelModifier.GREEN_MASK) >> PixelModifier.GREEN_OFFSET;
				int blue = (pixel & PixelModifier.BLUE_MASK) >> PixelModifier.BLUE_OFFSET;
				
				
				//TODO blur
				
				int newPixel = (alpha << ALPHA_OFFSET) | (red << RED_OFFSET) | (blue << BLUE_OFFSET)
						| (green << GREEN_OFFSET);

				resultData[index] = newPixel;

			}
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

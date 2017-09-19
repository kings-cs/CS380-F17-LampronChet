package Alorithms;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
//import javax.swing.JOptionPane;

import pink.PixelModifier;

/**
 * Pixel modifier to make the image grayscale.
 * 
 * @author Chet Lampron
 *
 */
public class GrayscaleModifier extends PixelModifier {

	@Override
	public BufferedImage modifyPixel(BufferedImage image) {
		//long startTime = System.nanoTime();
		int width = image.getWidth();
		int height = image.getHeight();

		int[] sourceData = super.unwrapImage(image);

		int[] resultData = new int[sourceData.length];

		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				int index = row * width + col;
				int pixel = sourceData[index];

				int alpha = (pixel & PixelModifier.ALPHA_MASK) >> PixelModifier.ALPHA_OFFSET;
				int red = (pixel & PixelModifier.RED_MASK) >> PixelModifier.RED_OFFSET;
				int green = (pixel & PixelModifier.GREEN_MASK) >> PixelModifier.GREEN_OFFSET;
				int blue = (pixel & PixelModifier.BLUE_MASK) >> PixelModifier.BLUE_OFFSET;

				// Old Gray int gray = Math.min(red, Math.min(green, blue));
				/* "Incorrect gray"
				 * int conversionFactor = 255 / (64 - 1); int averageValue = (red + green +
				 * blue) / 3; int gray = (int) ((averageValue / conversionFactor) + 0.5) *
				 * conversionFactor;
				 */
				int gray = (int) (red * 0.299 + green * 0.587 + blue * 0.114);
				
				int newPixel = (alpha << ALPHA_OFFSET) | (gray << RED_OFFSET) | (gray << BLUE_OFFSET)
						| (gray << GREEN_OFFSET);

				resultData[index] = newPixel;

			}
		}

		DataBufferInt resultDataBuffer = new DataBufferInt(resultData, resultData.length);
		Raster resultRastor = Raster.createRaster(image.getRaster().getSampleModel(), resultDataBuffer,
				new Point(0, 0));
		image.setData(resultRastor);
		//JOptionPane.showMessageDialog(null, "Total Time: " + (System.nanoTime() - startTime) / 1000000 + "ms");
		return image;
	}

}

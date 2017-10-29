package algorithms;

import java.awt.image.BufferedImage;

import javax.swing.JOptionPane;

//import javax.swing.JOptionPane;

/**
 * Pixel modifier to make the image grayscale.
 * 
 * @author Chet Lampron
 *
 */
public class GrayscaleModifier extends PixelModifier {

	@Override
	public BufferedImage modifyPixel(BufferedImage image) {
		double startTime = System.nanoTime();
		int width = image.getWidth();
		int height = image.getHeight();

		int[] sourceData = super.unwrapImage(image);

		int[] resultData = new int[sourceData.length];

		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				int index = row * width + col;
				int pixel = sourceData[index];

				int alpha = (pixel & PixelModifier.ALPHA_MASK) >> PixelModifier.getAlphaOffset();
				int red = (pixel & PixelModifier.RED_MASK) >> PixelModifier.RED_OFFSET;
				int green = (pixel & PixelModifier.getGreenMask()) >> PixelModifier.getGreenOffset();
				int blue = (pixel & PixelModifier.getBlueMask()) >> PixelModifier.getBlueOffset();

				// Old Gray int gray = Math.min(red, Math.min(green, blue));
				/*
				 * "Incorrect gray" int conversionFactor = 255 / (64 - 1); int averageValue =
				 * (red + green + blue) / 3; int gray = (int) ((averageValue / conversionFactor)
				 * + 0.5) * conversionFactor;
				 */
				int gray = (int) (red * 0.299 + green * 0.587 + blue * 0.114);

				int newPixel = (alpha << getAlphaOffset()) | (gray << RED_OFFSET) | (gray << getBlueOffset())
						| (gray << getGreenOffset());

				resultData[index] = newPixel;

			}
		}

		packageImage(resultData, image);
		JOptionPane.showMessageDialog(null, "Total Time: " + (System.nanoTime() - startTime) / 1000000.0 + "ms");

		return image;
	}

}

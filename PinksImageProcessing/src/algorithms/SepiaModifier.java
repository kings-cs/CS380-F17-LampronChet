package algorithms;

import java.awt.image.BufferedImage;

import javax.swing.JOptionPane;

//import javax.swing.JOptionPane;

/**
 * Modifies the image to sepia.
 * 
 * @author Chet Lampron
 *
 */
public class SepiaModifier extends PixelModifier {
	/** The sepia depth. */
	private static final int SEPIA_DEPTH = 35;
	/** The sepia intensity. */
	private static final int SEPIA_INTENSITY = 40;

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
				int green = (pixel & PixelModifier.GREEN_MASK) >> PixelModifier.getGreenOffset();
				int blue = (pixel & PixelModifier.BLUE_MASK) >> PixelModifier.getBlueOffset();

				int average = (red + blue + green) / 3;

				red = average + (SEPIA_DEPTH * 2);
				if (red > 255) {
					red = 255;
				} else if (red < 0) {
					red = 0;
				}
				blue = average - SEPIA_INTENSITY;
				if (blue > 255) {
					blue = 255;
				} else if (blue < 0) {
					blue = 0;
				}
				green = average + SEPIA_DEPTH;
				if (green > 255) {
					green = 255;
				} else if (green < 0) {
					green = 0;
				}

				int newPixel = (alpha << getAlphaOffset()) | (red << RED_OFFSET) | (blue << getBlueOffset())
						| (green << getGreenOffset());

				resultData[index] = newPixel;

			}
		}

		packageImage(resultData, image);
		JOptionPane.showMessageDialog(null, "Total Time: " + (System.nanoTime() - startTime) / 1000000.0 + "ms");

		return image;
	}
}

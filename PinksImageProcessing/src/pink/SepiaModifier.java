package pink;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;

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

				int average = (red + blue + green) / 3;
				
				red = average + (SEPIA_DEPTH * 2);
				if(red > 255) {
					red = 255;
				}else if(red < 0) {
					red = 0;
				}
				blue = average - SEPIA_INTENSITY;
				if(blue > 255) {
					blue = 255;
				}else if(blue < 0) {
					blue = 0;
				}
				green = average + SEPIA_DEPTH;
				if(green > 255) {
					green = 255;
				}else if(green < 0) {
					green = 0;
				}
				
				
				int newPixel = (alpha << ALPHA_OFFSET) | (red << RED_OFFSET) | (blue << BLUE_OFFSET)
						| (green << GREEN_OFFSET);

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

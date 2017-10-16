/**
 * 
 */
package algorithms;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.io.FileNotFoundException;
import java.util.Random;

import javax.swing.JOptionPane;


/**
 * Class to make picture mosaic in sequential.
 * 
 * @author Chet Lampron
 *
 */
public class MosaicModifier extends PixelModifier {
	/** The number of tiles. */
	private int tiles;

	/**
	 * Constructs a mosaic modifier with the number of tile points to generate.
	 * 
	 * @param numOfTiles
	 *            The number of tile points to generate.
	 */
	public MosaicModifier(int numOfTiles) {
		tiles = numOfTiles;
	}

	/**
	 * Generates the random pixels to be tile points in the mosaic.
	 * 
	 * @param numOfPoints
	 *            The number of points to generate.
	 * 
	 * @param data
	 *            The image data.
	 * @return The tile points.
	 */
	public int[] getTilePoints(int numOfPoints, int[] data) {
		int[] randomValues = new int[numOfPoints];
		Random rand = new Random();

		for (int i = 0; i < randomValues.length; i++) {
			randomValues[i] = rand.nextInt(data.length - 1);
		}

		return randomValues;
	}

	
	@Override
	public BufferedImage modifyPixel(BufferedImage image) throws FileNotFoundException {
		long startTime = System.nanoTime();

		int width = image.getWidth();
		int height = image.getHeight();
		int[] sourceData = super.unwrapImage(image);

		int[] resultData = new int[sourceData.length];

		int[] tilePoints = getTilePoints(tiles, sourceData);

		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				int index = row * width + col;
				float finalTile = 0;
				float finalDistance = Float.MAX_VALUE;
				for (int i = 0; i < tilePoints.length; i++) {
					float centerIndex = i;
					float distance = (float) Math.hypot(index, centerIndex);
					if (distance < finalDistance) {
						finalTile = centerIndex;
						finalDistance = distance;
					}
				}
				int centerTile = Float.floatToIntBits(finalTile);
				int centerPixel = sourceData[centerTile];
				int centerAlpha = (centerPixel & PixelModifier.ALPHA_MASK) >> PixelModifier.ALPHA_OFFSET;
				int centerRed = (centerPixel & PixelModifier.RED_MASK) >> PixelModifier.RED_OFFSET;
				int centerGreen = (centerPixel & PixelModifier.GREEN_MASK) >> PixelModifier.GREEN_OFFSET;
				int centerBlue = (centerPixel & PixelModifier.BLUE_MASK) >> PixelModifier.BLUE_OFFSET;


				int newPixel = (centerAlpha << ALPHA_OFFSET) | (centerRed << RED_OFFSET) | (centerGreen << BLUE_OFFSET)
						| (centerBlue << GREEN_OFFSET);
				
				resultData[index] = newPixel;
			}
		}
		DataBufferInt resultDataBuffer = new DataBufferInt(resultData, resultData.length);
		Raster resultRastor = Raster.createRaster(image.getRaster().getSampleModel(), resultDataBuffer,
				new Point(0, 0));
		image.setData(resultRastor);
		JOptionPane.showMessageDialog(null, "Total Time: " + (System.nanoTime() - startTime) / 1000000 + "ms");
		return image;
	}
}

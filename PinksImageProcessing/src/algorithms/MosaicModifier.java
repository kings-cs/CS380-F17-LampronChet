/**
 * 
 */
package algorithms;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.util.Random;

import pinkprocessing.TileDimensions;

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
	 * @return The tile points.
	 */
	public TileDimensions[] getTilePoints(int width, int height, int[] data) {
		int[] randomValues = new int[data.length];
		Random rand = new Random();
		
		for(int i = 0; i < tiles; i++) {
			
		}
		
		return null;
	}

	@Override
	public BufferedImage modifyPixel(BufferedImage image) throws FileNotFoundException {
		int width = image.getWidth();
		int height = image.getHeight();
		int[] dimensions = new int[2];
		dimensions[0] = width;
		dimensions[1] = height;
		int[] sourceData = super.unwrapImage(image);

		int[] resultData = new int[sourceData.length];
		
		return null;
	}
}

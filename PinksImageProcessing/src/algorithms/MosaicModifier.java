/**
 * 
 */
package algorithms;

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
	public TileDimensions[] getTilePoints() {
		//TODO
		return null;
	}
}

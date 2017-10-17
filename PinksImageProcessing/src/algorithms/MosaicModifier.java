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
		setTiles(numOfTiles);
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

		int[] tilePoints = getTilePoints(getTiles(), sourceData);

		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				int index = row * width + col;
				int finalTile = 0;
				double finalDistance = Double.MAX_VALUE;
				int centerRow = 0;
				int centerCol = 0;
				for (int i = 0; i < tilePoints.length; i++) {
					centerRow = tilePoints[i] / width;
					centerCol = tilePoints[i] % width;

					int rowDistance = (centerRow - row);
					int colDistance = (centerCol - col);
					double distance = Math.sqrt((rowDistance ^ 2) + (colDistance ^ 2));
					if (distance < finalDistance) {
						finalTile = tilePoints[i];
						finalDistance = distance;
					}
				}
				int centerPixel = sourceData[finalTile];

				resultData[index] = centerPixel;
			}
		}
		DataBufferInt resultDataBuffer = new DataBufferInt(resultData, resultData.length);
		Raster resultRastor = Raster.createRaster(image.getRaster().getSampleModel(), resultDataBuffer,
				new Point(0, 0));
		image.setData(resultRastor);
		JOptionPane.showMessageDialog(null, "Total Time: " + (System.nanoTime() - startTime) / 1000000 + "ms");
		return image;
	}

	/**
	 * @return the tiles
	 */
	public int getTiles() {
		return tiles;
	}

	/**
	 * @param tiles the tiles to set
	 */
	public void setTiles(int tiles) {
		this.tiles = tiles;
	}
}

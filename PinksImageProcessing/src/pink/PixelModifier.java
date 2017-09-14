package pink;


import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.io.FileNotFoundException;

/**
 * Generic pixel modifier class.
 * @author Chet Lampron
 *
 */
public class PixelModifier {
	/** The alpha mask. */
	protected static final int ALPHA_MASK = 0xff000000;
	/** The alpha offset. */
	protected static final int ALPHA_OFFSET = 24;

	/** The red mask. */
	protected static final int RED_MASK = 0x00ff0000;
	/** The red offset. */
	protected static final int RED_OFFSET = 16;

	/** The green mask. */
	protected static final int GREEN_MASK = 0x0000ff00;
	/** The green offset. */
	protected static final int GREEN_OFFSET = 8;

	/** The blue mask. */
	protected static final int BLUE_MASK = 0x000000ff;
	/** The blue offset. */
	protected static final int BLUE_OFFSET = 0;
	/**
	 * Unwraps the image from abstractions.
	 * @param image The image to work on.
	 * @return The source data of the image.
	 */
	public int[] unwrapImage(BufferedImage image) {
		WritableRaster source = image.getRaster();
		DataBuffer sourceBuffer = source.getDataBuffer();
		DataBufferInt sourceBytes = (DataBufferInt) sourceBuffer;
		int[] sourceData = sourceBytes.getData();
		return sourceData;
	}

	/**
	 * Modifies each pixel to change the image.
	 * 
	 * @param image
	 *            The original image.
	 * @return The modified image.
	 * @throws FileNotFoundException not thrown.
	 */
	public BufferedImage modifyPixel(BufferedImage image) throws FileNotFoundException {
		/*
		int width = image.getWidth();
		int height = image.getHeight();
		int[] sourceData = unwrapImage(image);
		int[] resultData = new int[sourceData.length];
		
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				int index = row * width + col;
				int pixel = sourceData[index];

				int red = (pixel & RED_MASK) >> RED_OFFSET;
				int green = (pixel & GREEN_MASK) >> GREEN_OFFSET;
				int blue = (pixel & BLUE_MASK) >> BLUE_OFFSET;
				
				//modify pixel based on algorithm.
				int resultColor = pixel;
				
				resultData[index] = resultColor;
					
			}
		}
		
		DataBufferInt resultDataBuffer = new DataBufferInt(resultData, resultData.length);
		Raster resultRastor = Raster.createRaster(image.getRaster().getSampleModel(), resultDataBuffer, new Point(0, 0));
		
		image.setData(resultRastor);
		*/
		//TODO by sublass
		
		return image;
	}
}

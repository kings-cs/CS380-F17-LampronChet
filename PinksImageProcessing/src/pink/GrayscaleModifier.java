package pink;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 * Pixel modifier to make the image grayscale.
 * @author Chet Lampron
 *
 */
public class GrayscaleModifier extends PixelModifier {

	@Override
	public BufferedImage modifyPixel(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		
		WritableRaster source = image.getRaster();
		DataBuffer sourceBuffer = source.getDataBuffer();
		DataBufferInt sourceBytes = (DataBufferInt) sourceBuffer;
		int[] sourceData = sourceBytes.getData();
		
		int[] resultData = new int[sourceData.length];
		
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				int index = row * width + col;
				int pixel = sourceData[index];

				int red = (pixel & PixelModifier.RED_MASK) >> PixelModifier.RED_OFFSET;
				int green = (pixel & PixelModifier.GREEN_MASK) >> PixelModifier.GREEN_OFFSET;
				int blue = (pixel & PixelModifier.BLUE_MASK) >> PixelModifier.BLUE_OFFSET;
				
				
				int gray = ( Math.max(red, Math.max(green, blue)) + Math.min(red, Math.min(green, blue))) / 2;
				System.out.println(gray);
				resultData[index] = gray;
					
			}
		}
		
		DataBufferInt resultDataBuffer = new DataBufferInt(resultData, resultData.length);
		Raster resultRastor = Raster.createRaster(source.getSampleModel(), resultDataBuffer, new Point(0, 0));
		image.setData(resultRastor);
		
		return image;
	}
	
}

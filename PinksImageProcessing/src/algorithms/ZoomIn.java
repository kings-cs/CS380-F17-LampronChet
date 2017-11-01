/**
 * 
 */
package algorithms;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;

import javax.swing.JOptionPane;

/**
 * Class to zoom the image in by a factor of 10.
 * 
 * @author Chet Lampron
 *
 */
public class ZoomIn extends PixelModifier {

	@Override
	public BufferedImage modifyPixel(BufferedImage image) throws FileNotFoundException {
		double startTime = System.nanoTime();
		int width = image.getWidth();
		int height = image.getHeight();

		int newWidth = (int) (width + (width * .1));
		int newHeight = (int) (height + (height * .1));

		int[] sourceData = super.unwrapImage(image);

		int[] resultData = new int[newWidth * newHeight];

		BufferedImage zoomedImage = null;
		int newCol = 0;
		for (int row = 0; row < newHeight; row++) {
			for (int col = 0; col < newWidth; col++) {
				int index = row * width + col;
				int pixel = 0;
				if (row < height && col < width) {
					pixel = sourceData[index];
				}else{
					int tempRow = 0;
					int tempCol = 0;
					if(row >= height && col < width){
						tempRow = height - 1;
						tempCol = col;
					}else if(row < height && col >= width){
						tempRow = row;
						tempCol = width - 1;
					}else{
						tempRow = height - 1;
						tempCol = width - 1;
					}
					int tempIndex = tempRow * width + tempCol;
					pixel = sourceData[tempIndex];
				}
				if(newCol == 9){
					
				}

			}
		}
		packageImage(resultData, image);
		JOptionPane.showMessageDialog(null, "Total Time: " + (System.nanoTime() - startTime) / 1000000.0 + "ms");

		return zoomedImage;
	}

}

package pink;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
/**
 * File import and export.
 * @author Chet Lampron
 *
 */
public class FileHandler {
	/**
	 * Loads and creates an image.
	 * @param filePath The file locatiion.
	 * @return The drawn image.
	 * @throws IOException When the file is not found.
	 */
	public BufferedImage createImage(String filePath) throws IOException {
		File file = new File(filePath);
		BufferedImage readIn = ImageIO.read(file);
		
		BufferedImage returnImage = new BufferedImage(readIn.getWidth(), readIn.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics g = returnImage.getGraphics();
		g.drawImage(readIn, 0, 0, null);
		return returnImage;
	}
	/**
	 * Saves the image to file.
	 * @param filePath The file location.
	 * @param image The image to save.
	 * @param fileType The type of file.
	 * @throws IOException When the file can not be found or created. 
	 */
	public void saveImage(String filePath, BufferedImage image, String fileType) throws IOException {
		File outputFile = new File(filePath + "." + fileType);

		ImageIO.write(image, fileType, outputFile);
	}
}

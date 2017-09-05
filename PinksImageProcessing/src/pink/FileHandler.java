package pink;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class FileHandler {
	
	public BufferedImage createImage(String filePath) throws IOException {
		File file = new File(filePath);
		BufferedImage readIn = ImageIO.read(file);
		
		BufferedImage returnImage = new BufferedImage(readIn.getWidth(), readIn.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics g = returnImage.getGraphics();
		g.drawImage(readIn, 0, 0, null);
		return returnImage;
	}
	
	public void saveImage(String filePath, BufferedImage image, String fileType) throws IOException {
		File outputFile = new File(filePath);
		
		ImageIO.write(image, "png", outputFile);
	}
}

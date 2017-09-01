package pink;

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
		return returnImage;
	}
}

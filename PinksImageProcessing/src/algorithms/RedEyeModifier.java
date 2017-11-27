/**
 * 
 */
package algorithms;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;

import javax.swing.JOptionPane;

import parallel.JoclInitializer;

/**
 * Class to remove red eye from image.
 * 
 * @author Chet Lampron.
 *
 */
public class RedEyeModifier extends PixelModifier {
	/** The device manager. */
	private JoclInitializer deviceManager;
	
	/** The template red eye. */
	private BufferedImage redEye;
	/**
	 * Constructs a red eye removal object.
	 * 
	 * @param deviceManager
	 *            The device manager.
	 * @param template
	 *            The red eye template.
	 */
	public RedEyeModifier(JoclInitializer deviceManager, BufferedImage template) {
		this.deviceManager = deviceManager;
		redEye = template;
	}

	@Override
	public BufferedImage modifyPixel(BufferedImage image) throws FileNotFoundException {
		// TODO Auto-generated method stub
		int[] sourceData = unwrapImage(image);
		int[] templateData = unwrapImage(redEye);
		JOptionPane.showMessageDialog(null, "Source length is: " + sourceData.length + " template length is " + templateData.length);
		return null;
	}

}

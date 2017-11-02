/**
 * 
 */
package algorithms;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;

/**
 * Class to zoom the image in by a factor of 10.
 * 
 * @author Chet Lampron
 *
 */
public class ZoomIn extends PixelModifier {
	/** Whether it is zoom in or out. */
	private boolean isZoom;

	/**
	 * Constructs a zoom in object.
	 * 
	 * @param zoom
	 *            Whether it is zoom in or out.
	 */
	public ZoomIn(boolean zoom) {
		isZoom = zoom;
	}

	@Override
	public BufferedImage modifyPixel(BufferedImage image) throws FileNotFoundException {
		int width = image.getWidth();
		int height = image.getHeight();

		int newWidth = 0;
		int newHeight = 0;
		if (isZoom) {
			newWidth = (int) (width + (width * .1));
			newHeight = (int) (height + (height * .1));
		} else {
			newWidth = (int) (width - (width * .1));
			newHeight = (int) (height - (height * .1));
		}

		BufferedImage zoomedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g = zoomedImage.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(image, 0, 0, newWidth, newHeight, null);

		return zoomedImage;
	}

}

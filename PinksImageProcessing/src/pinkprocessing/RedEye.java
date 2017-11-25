package pinkprocessing;

import parallel.JoclInitializer;

/**
 * The red eye removal helper methods.
 * 
 * @author Chet Lampron.
 *
 */
public class RedEye {
	/** The device manager. */
	private JoclInitializer deviceManager;

	/**
	 * Constructs a RedEye helper object.
	 * 
	 * @param theDeviceManager
	 *            The device manager being used.
	 */
	public RedEye(JoclInitializer theDeviceManager) {
		deviceManager = theDeviceManager;
	}

	/**
	 * Calculates the average of the temple.
	 * 
	 * @param data
	 *            The data of the image.
	 * @return The average.
	 */
	public int calculateTemplateAverage(float[] data) {
		int total = 0;
		for (int i = 0; i < data.length; i++) {
			total += data[i];
		}
		return total / data.length;
	}
}

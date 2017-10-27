package testing;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;

import org.jocl.CL;
import org.jocl.cl_device_id;
import org.junit.Before;
import org.junit.Test;

import parallel.JoclInitializer;
import pinkprocessing.GrayscaleEqualization;

/**
 * Test class for GrayscaleEqualization.
 * 
 * @author Chet Lampron
 *
 */
public class GrayscaleEqualizationTest {
	/** The device manager. */
	private JoclInitializer deviceManager;

	/**
	 * Initializes the device manager and creates a context for a GPU.
	 */
	@Before
	public void setup() {
		CL.setExceptionsEnabled(true);
		deviceManager = new JoclInitializer();
		cl_device_id[] devices = deviceManager.getDeviceIds();
		int i = 0;
		boolean contextCreated = false;
		while (i < devices.length && !contextCreated) {
			if (deviceManager.isGpu(devices[i])) {
				deviceManager.createContext(devices[i]);
				contextCreated = true;
			}
			i++;
		}
	}

	/**
	 * Test for calculateHeuristic.
	 * 
	 * @throws FileNotFoundException
	 *             Not thrown.
	 */
	@Test
	public void testCalculateHeuristic() throws FileNotFoundException {
		int[] data = new int[] { 4, 4, 4, 4, 4, 3, 4, 5, 4, 3, 3, 5, 5, 5, 3, 3, 4, 5, 4, 3, 4, 4, 4, 4, 4 };
		GrayscaleEqualization equalizer = new GrayscaleEqualization();
		int[] calculatedHistogram = equalizer.calculateHistogram(deviceManager, data, getWorkSize(deviceManager, data));

		assertEquals("Should return 6", 6, calculatedHistogram[3]);
		assertEquals("Should return 14", 14, calculatedHistogram[4]);
		assertEquals("Should return 5", 5, calculatedHistogram[5]);

	}

	/**
	 * Gets the proper work size.
	 * 
	 * @param data
	 *            The data array.
	 * @param deviceManager
	 *            The proper device manager.
	 * @return The proper work size;
	 */
	public int getWorkSize(JoclInitializer deviceManager, int[] data) {
		int maxItemsPerGroup = deviceManager.getMaxWorkGroupSize();
		boolean isDivisible = false;

		while (!isDivisible) {
			int numOfItems = data.length % maxItemsPerGroup;
			if (numOfItems == 0) {
				isDivisible = true;
			} else {
				maxItemsPerGroup--;
			}
		}
		return maxItemsPerGroup;
	}

}

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
		System.out.println(devices[0]);
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
	public void testCalculateHistogramNot() throws FileNotFoundException {
		int[] data = new int[] { 4, 4, 4, 4, 4, 3, 4, 5, 4, 3, 3, 5, 5, 5, 3, 3, 4, 5, 4, 3, 4, 4, 4, 4, 4 };
		GrayscaleEqualization equalizer = new GrayscaleEqualization();
		int[] calculatedHistogram = equalizer.calculateHistogram(deviceManager, data, getWorkSize(deviceManager, data),
				false);

		assertEquals("Should return 6", 6, calculatedHistogram[3]);
		assertEquals("Should return 14", 14, calculatedHistogram[4]);
		assertEquals("Should return 5", 5, calculatedHistogram[5]);

	}

	/**
	 * Test for calculateHeuristic.
	 * 
	 * @throws FileNotFoundException
	 *             Not thrown.
	 */
	@Test
	public void testCalculateHistogram() throws FileNotFoundException {
		int[] data = new int[] { 4, 4, 4, 4, 4, 3, 4, 5, 4, 3, 3, 5, 5, 5, 3, 3, 4, 5, 4, 3, 4, 4, 4, 4, 4 };
		GrayscaleEqualization equalizer = new GrayscaleEqualization();
		int[] calculatedHistogram = equalizer.calculateHistogram(deviceManager, data, getWorkSize(deviceManager, data),
				true);

		assertEquals("Should return 6", 6, calculatedHistogram[3]);
		assertEquals("Should return 14", 14, calculatedHistogram[4]);
		assertEquals("Should return 5", 5, calculatedHistogram[5]);

	}

	/**
	 * Test for distributeCumulativeFrequency.
	 * 
	 * @throws FileNotFoundException
	 *             Not thrown.
	 */
	@Test
	public void testCumulativeFrequency() throws FileNotFoundException {
		int[] data = new int[] { 0, 0, 0, 6, 14, 5, 0, 0 };
		GrayscaleEqualization equalizer = new GrayscaleEqualization();
		equalizer.setDeviceManager(deviceManager);
		int[] distributedFreq = equalizer.distributeCumulativeFrequency(data);// (deviceManager, data,
																				// getWorkSize(deviceManager, data));

		assertEquals("Should return 6", 6, distributedFreq[3]);
		assertEquals("Should return 20", 20, distributedFreq[4]);
		assertEquals("Should return 25", 25, distributedFreq[5]);
		assertEquals("Should return 25", 25, distributedFreq[6]);
		assertEquals("Should return 25", 25, distributedFreq[7]);

	}

	/**
	 * Test for calculatedIdealizedHistogram.
	 * 
	 * @throws FileNotFoundException
	 *             Not thrown.
	 */
	@Test
	public void testIdealizedHistogram() throws FileNotFoundException {
		int[] data = new int[] { 0, 0, 0, 6, 20, 25, 25, 25 };
		GrayscaleEqualization equalizer = new GrayscaleEqualization();
		equalizer.setDeviceManager(deviceManager);
		int[] calculatedIdealizedHistogram = equalizer.calculateIdealizedHistogram(data, 25, 1);// (deviceManager, data,
																								// getWorkSize(deviceManager,
																								// data));

		assertEquals("Should return 3", 3, calculatedIdealizedHistogram[0]);
		assertEquals("Should return 3", 3, calculatedIdealizedHistogram[1]);
		assertEquals("Should return 3", 3, calculatedIdealizedHistogram[2]);
		assertEquals("Should return 4", 4, calculatedIdealizedHistogram[3]);
		assertEquals("Should return 3", 3, calculatedIdealizedHistogram[4]);
		assertEquals("Should return 3", 3, calculatedIdealizedHistogram[5]);
		assertEquals("Should return 3", 3, calculatedIdealizedHistogram[6]);
		assertEquals("Should return 3", 3, calculatedIdealizedHistogram[7]);

	}

	/**
	 * Test for designMap.
	 * 
	 * @throws FileNotFoundException
	 *             Not thrown.
	 */
	@Test
	public void testDesignMap() throws FileNotFoundException {
		int[] data = new int[] { 3, 6, 9, 13, 16, 19, 22, 25 };
		int[] oldData = new int[] { 0, 0, 0, 6, 20, 25, 25, 25 };
		GrayscaleEqualization equalizer = new GrayscaleEqualization();
		equalizer.setDeviceManager(deviceManager);
		int[] mapDesign = equalizer.designMap(data, oldData, 1);// (deviceManager, data, getWorkSize(deviceManager,
																// data));

		assertEquals("Should return 0", 0, mapDesign[0]);
		assertEquals("Should return 0", 0, mapDesign[1]);
		assertEquals("Should return 0", 0, mapDesign[2]);
		assertEquals("Should return 1", 1, mapDesign[3]);
		assertEquals("Should return 5", 5, mapDesign[4]);
		assertEquals("Should return 7", 7, mapDesign[5]);
		assertEquals("Should return 7", 7, mapDesign[6]);
		assertEquals("Should return 7", 7, mapDesign[7]);

	}

	/**
	 * Test for getMap.
	 * 
	 * @throws FileNotFoundException
	 *             Not thrown.
	 */
	@Test
	public void testGetMap() throws FileNotFoundException {
		int[] mapDesign = new int[] { 0, 0, 0, 1, 5, 7, 7, 7 };
		int[] data = new int[] { 4, 4, 4, 4, 4, 3, 4, 5, 4, 3, 3, 5, 5, 5, 3, 3, 4, 5, 4, 3, 4, 4, 4, 4, 4 };
		GrayscaleEqualization equalizer = new GrayscaleEqualization();
		equalizer.setDeviceManager(deviceManager);
		equalizer.setWorkSize(1);
		int[] map = equalizer.getMap(mapDesign, data);// (deviceManager, data, getWorkSize(deviceManager, data));

		assertEquals("Should return 5", 5, map[0]);
		assertEquals("Should return 5", 5, map[1]);
		assertEquals("Should return 5", 5, map[2]);
		assertEquals("Should return 5", 5, map[3]);
		assertEquals("Should return 5", 5, map[4]);
		assertEquals("Should return 1", 1, map[5]);
		assertEquals("Should return 5", 5, map[6]);
		assertEquals("Should return 7", 7, map[7]);
		assertEquals("Should return 5", 5, map[8]);
		assertEquals("Should return 1", 1, map[9]);
		assertEquals("Should return 1", 1, map[10]);
		assertEquals("Should return 7", 7, map[11]);
		assertEquals("Should return 7", 7, map[12]);
		assertEquals("Should return 7", 7, map[13]);
		assertEquals("Should return 1", 1, map[14]);
		assertEquals("Should return 1", 1, map[15]);
		assertEquals("Should return 5", 5, map[16]);
		assertEquals("Should return 7", 7, map[17]);
		assertEquals("Should return 5", 5, map[18]);
		assertEquals("Should return 1", 1, map[19]);
		assertEquals("Should return 5", 5, map[20]);
		assertEquals("Should return 5", 5, map[21]);
		assertEquals("Should return 5", 5, map[22]);
		assertEquals("Should return 5", 5, map[23]);
		assertEquals("Should return 5", 5, map[24]);

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

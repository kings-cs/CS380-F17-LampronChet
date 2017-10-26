/**
 * 
 */
package testing;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;

import org.jocl.cl_device_id;

import org.junit.Test;

import parallel.JoclInitializer;

/**
 * Tests the implementation of scan.
 * 
 * @author Chet Lampron
 *
 */
public class ScanTest {
	/** The device manager. */
	private JoclInitializer deviceManager;

	/**
	 * Initializes the device manager and creates a context for a GPU.
	 */
	public ScanTest() {
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
	 * Tests inclusive scan on a single work group.
	 */
	@Test
	public void testHillisSteele() {
		float[] data = new float[256];

		for (int i = 0; i < data.length; i++) {
			data[i] = 1;
		}

		float[] result = new float[256];

		HillisSteeleScan scan = new HillisSteeleScan(deviceManager);

		try {
			scan.scan(data, result);
		} catch (FileNotFoundException e) {
			System.out.println("Kernel not found.");
		}

		for (int i = 1; i <= result.length; i++) {
			assertTrue(result[i] == i);
		}
	}

	/**
	 * Tests exclusive scan on a single work group.
	 */
	@Test
	public void testBlelloch() {
		float[] data = new float[256];

		for (int i = 0; i < data.length; i++) {
			data[i] = 1;
		}

		float[] result = new float[256];

		BlellochScan scan = new BlellochScan(deviceManager);

		try {
			scan.scan(data, result);
		} catch (FileNotFoundException e) {
			System.out.println("Kernel not found.");
		}
		
		for (int i = 0; i < result.length; i++) {
			assertTrue(result[i] == i);
		}
	}

}

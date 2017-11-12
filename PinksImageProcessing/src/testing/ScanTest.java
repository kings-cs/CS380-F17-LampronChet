package testing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.FileNotFoundException;

import org.jocl.CL;
import org.jocl.cl_device_id;
import org.junit.Before;
import org.junit.Ignore;
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
	@Before
	public void scanTest() {
		CL.setExceptionsEnabled(true);
		deviceManager = new JoclInitializer();
		cl_device_id[] devices = deviceManager.getDeviceIds();
		int i = 0;
		boolean contextCreated = false;
		while (i < devices.length && !contextCreated) {
			if (deviceManager.isGpu(devices[i])) {
				deviceManager.createContext(devices[i]);
				deviceManager.createQueue();
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
		int[] data = new int[256];

		for (int i = 0; i < data.length; i++) {
			data[i] = 1;
		}

		int[] result = new int[256];

		HillisSteeleScan scan = new HillisSteeleScan(deviceManager);

		try {
			scan.scan(data, result);
		} catch (FileNotFoundException e) {
			System.out.println("Kernel not found.");
		}

		for (int i = 1; i <= result.length; i++) {
			assertTrue(result[i - 1] == i);
		}
	}


	/**
	 * Tests exclusive scan on a single work group.
	 */
	@Test
	public void testBlelloch() {
		float[] data = new float[250];

		for (int i = 0; i < data.length; i++) {
			data[i] = 1;
		}

		float[] result = new float[250];

		BlellochScan scan = new BlellochScan(deviceManager);

		try {
			scan.getWorkSize(deviceManager, data);
			scan.scan(data, result);
		} catch (FileNotFoundException e) {
			System.out.println("Kernel not found.");
		}

		for (int i = 0; i < result.length; i++) {
			assertTrue("Should be: " + i + " but was: " + result[i], result[i] == i);
		}
	}

	/**
	 * Tests exclusive scan on size greater than group_size but not greater than
	 * group_size * group_size..
	 */
	@Ignore
	public void testBlellochGourpsize1() {
		float[] data = new float[2048];

		for (int i = 0; i < data.length; i++) {
			data[i] = 1;
		}

		float[] result = new float[2048];

		BlellochScan scan = new BlellochScan(deviceManager);

		try {
			scan.scan(data, result);
		} catch (FileNotFoundException e) {
			System.out.println("Kernel not found.");
		}

		for (int i = 0; i < result.length; i++) {
			assertTrue("Should be: " + i + " but was: " + result[i], result[i] == i);
		}
	}

	/**
	 * Tests exclusive scan on size greater than group_size * group_size..
	 */
	@Ignore
	public void testBlellochGourpsize2() {
		float[] data = new float[(int) (16 * Math.pow(2, 20))];

		for (int i = 0; i < data.length; i++) {
			data[i] = 1;
		}

		float[] result = new float[(int) (16 * Math.pow(2, 20))];

		BlellochScan scan = new BlellochScan(deviceManager);

		try {
			scan.scan(data, result);
		} catch (FileNotFoundException e) {
			System.out.println("Kernel not found.");
		}

		for (int i = 0; i < result.length; i++) {
			assertTrue("Should be: " + i + " but was: " + result[i], result[i] == i);
		}
	}
	
	/**
	 * Tests padding the array.
	 */
	@Test
	public void testPad() {
		float[] data = new float[246];
		BlellochScan scan = new BlellochScan(deviceManager);
		scan.getWorkSize(deviceManager, data);
		float[] result = scan.padArray(data);
		
		assertEquals("Should return 246", 246, result.length);
		assertEquals("Should return 0", 0, (int) result[244]);
	}

}

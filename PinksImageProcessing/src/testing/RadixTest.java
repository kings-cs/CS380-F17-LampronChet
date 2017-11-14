package testing;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;

import org.jocl.CL;
import org.jocl.cl_device_id;
import org.junit.Before;
import org.junit.Test;

import parallel.JoclInitializer;
import pinkprocessing.Radix;

/**
 * Tester for radix sort.
 * 
 * @author Chet Lampron
 *
 */
public class RadixTest {
	
	/** The device manager. */
	private JoclInitializer deviceManager;
	/** The work size. */
	int workSize;

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

	@Test
	public void testIsolate() throws FileNotFoundException {
		int[] values = { 10, 11, 2, 9, 0, 6, 1, 4, 7, 3, 8, 5 };

		Radix radix = new Radix(1, deviceManager);

		int[] returnVal = radix.isolateBit(values, 0);

		int[] expectedValues = { 0, 1, 0, 1, 0, 0, 1, 0, 1, 1, 0, 1 };

		for (int i = 0; i < values.length; i++) {
			assertTrue("Should return " + expectedValues[i] + " but was " + returnVal[i],
					returnVal[i] == expectedValues[i]);
		}

	}

	@Test
	public void testFlip() throws FileNotFoundException {
		int[] values = { 0, 1, 0, 1, 0, 0, 1, 0, 1, 1, 0, 1 };

		Radix radix = new Radix(1, deviceManager);

		int[] returnVal = radix.flipBits(values);

		int[] expectedValues = { 1, 0, 1, 0, 1, 1, 0, 1, 0, 0, 1, 0 };

		for (int i = 0; i < values.length; i++) {
			assertTrue("Should return " + expectedValues[i] + " but was " + returnVal[i],
					returnVal[i] == expectedValues[i]);
		}

	}

	@Test
	public void testScanNormal() throws FileNotFoundException {
		int[] values = { 0, 1, 0, 1, 0, 0, 1, 0, 1, 1, 0, 1 };

		Radix radix = new Radix(1, deviceManager);

		int[] returnVal = radix.scan(values);

		int[] expectedValues = { 0, 0, 1, 1, 2, 2, 2, 3, 3, 4, 5, 5 };

		for (int i = 0; i < values.length; i++) {
			assertTrue("Should return " + expectedValues[i] + " but was " + returnVal[i],
					returnVal[i] == expectedValues[i]);
		}

	}

	@Test
	public void testScanPredicate() throws FileNotFoundException {
		int[] values = { 1, 0, 1, 0, 1, 1, 0, 1, 0, 0, 1, 0 };

		Radix radix = new Radix(1, deviceManager);

		int[] returnVal = radix.scan(values);

		int[] expectedValues = { 0, 1, 1, 2, 2, 3, 4, 4, 5, 5, 5, 6};

		for (int i = 0; i < values.length; i++) {
			assertTrue("Should return " + expectedValues[i] + " but was " + returnVal[i],
					returnVal[i] == expectedValues[i]);
		}

	}

	@Test
	public void testAdress() throws FileNotFoundException {
		int[] data = { 10, 11, 2, 9, 0, 6, 1, 4, 7, 3, 8, 5 };
		int[] values = { 0, 1, 0, 1, 0, 0, 1, 0, 1, 1, 0, 1 };
		int[] predicateValues = { 1, 0, 1, 0, 1, 1, 0, 1, 0, 0, 1, 0 };
		int[] normalScan = { 0, 0, 1, 1, 2, 2, 2, 3, 3, 4, 5, 5 };
		int[] predicateScan = { 0, 1, 1, 2, 2, 3, 4, 4, 5, 5, 5, 6};
		int[] expectedValues = { 10, 2, 0, 6, 4, 8, 11, 9, 1, 7, 3, 5 };

		Radix radix = new Radix(1, deviceManager);
		int[] result = new int[values.length];
		radix.calculateAdress(data, values, predicateValues, normalScan, predicateScan, result);
		for (int i = 0; i < values.length; i++) {
			assertTrue("Should return " + expectedValues[i] + " but was " + result[i], result[i] == expectedValues[i]);
		}
	}
	
	@Test
	public void testFullSortSmall() throws FileNotFoundException {
		int[] data = new int[250];
		int[] result = new int[250];
		int[] expectedResult = new int[250];
		int dataPlacer = 249;
		for(int i = 0; i < data.length; i++) {
			data[i] = dataPlacer;
			expectedResult[i] = i;
			dataPlacer--;
		}
		getProperWorkSize(deviceManager, data);
		Radix sort = new Radix(workSize, deviceManager);
		sort.fullSort(data, result);
		System.out.println(result[15]);
		for(int i = 0;i < data.length; i++) {
			assertTrue("Should return " + expectedResult[i] + " but was " + result[i], result[i] == expectedResult[i]);
		}
	}
	
	/**
	 * Gets the proper work size.
	 * 
	 * @param data
	 *            The data array.
	 * @param deviceManager
	 *            The proper device manager.
	 */
	public void getProperWorkSize(JoclInitializer deviceManager, int[] data) {
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
		workSize = maxItemsPerGroup;
	}

}

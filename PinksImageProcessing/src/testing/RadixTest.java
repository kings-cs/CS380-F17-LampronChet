package testing;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import pinkprocessing.Radix;

/**
 * Tester for radix sort.
 * 
 * @author Chet Lampron
 *
 */
public class RadixTest {

	@Test
	public void testIsolate() {
		int[] values = { 10, 11, 2, 9, 0, 6, 1, 4, 7, 3, 8, 5 };

		Radix radix = new Radix(1, null);

		int[] returnVal = radix.isolateBit(values, 0);

		int[] expectedValues = { 0, 1, 0, 1, 0, 0, 1, 0, 1, 1, 0, 1 };

		for (int i = 0; i < values.length; i++) {
			assertTrue("Should return " + expectedValues[i] + " but was " + returnVal[i],
					returnVal[i] == expectedValues[i]);
		}

	}

	@Test
	public void testFlip() {
		int[] values = { 0, 1, 0, 1, 0, 0, 1, 0, 1, 1, 0, 1 };

		Radix radix = new Radix(1, null);

		int[] returnVal = radix.flipBits(values);

		int[] expectedValues = { 1, 0, 1, 0, 1, 1, 0, 1, 0, 0, 1, 0 };

		for (int i = 0; i < values.length; i++) {
			assertTrue("Should return " + expectedValues[i] + " but was " + returnVal[i],
					returnVal[i] == expectedValues[i]);
		}

	}

	@Test
	public void testScanNormal() {
		int[] values = { 0, 1, 0, 1, 0, 0, 1, 0, 1, 1, 0, 1 };

		Radix radix = new Radix(1, null);

		int[] returnVal = radix.scan(values);

		int[] expectedValues = { 0, 0, 1, 1, 2, 3, 3, 4, 4, 4, 5, 5 };

		for (int i = 0; i < values.length; i++) {
			assertTrue("Should return " + expectedValues[i] + " but was " + returnVal[i],
					returnVal[i] == expectedValues[i]);
		}

	}

	@Test
	public void testScanPredicate() {
		int[] values = { 1, 0, 1, 0, 1, 1, 0, 1, 0, 0, 1, 0 };

		Radix radix = new Radix(1, null);

		int[] returnVal = radix.scan(values);

		int[] expectedValues = { 0, 0, 0, 1, 1, 1, 2, 2, 3, 4, 4, 5 };

		for (int i = 0; i < values.length; i++) {
			assertTrue("Should return " + expectedValues[i] + " but was " + returnVal[i],
					returnVal[i] == expectedValues[i]);
		}

	}

	@Test
	public void testAdress() {
		int[] values = { 0, 1, 0, 1, 0, 0, 1, 0, 1, 1, 0, 1 };
		int[] predicateValues = { 1, 0, 1, 0, 1, 1, 0, 1, 0, 0, 1, 0 };
		int[] normalScan = { 0, 0, 1, 1, 2, 3, 3, 4, 4, 4, 5, 5 };
		int[] predicateScan = { 0, 0, 0, 1, 1, 1, 2, 2, 3, 4, 4, 5 };
		int[] expectedValues = { 10, 2, 0, 6, 4, 8, 11, 9, 1, 7, 3, 5 };

		Radix radix = new Radix(1, null);

		int[] result = radix.calculateAdress(values, predicateValues, normalScan, predicateScan);
		for (int i = 0; i < values.length; i++) {
			assertTrue("Should return " + expectedValues[i] + " but was " + result[i], result[i] == expectedValues[i]);
		}
	}

}

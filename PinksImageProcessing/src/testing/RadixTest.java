package testing;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
/**
 * Tester for radix sort.
 * @author Chet Lampron
 *
 */
public class RadixTest {

	@Test
	public void testIsolate() {
		int[] values = {10, 11, 2, 9, 0, 6, 1, 4, 7, 3, 8, 5};
		
		Radix radix = new Radix();
		
		int[] returnVal = radix.isolateBit();
		
		int[] expectedValues = {0, 1, 0, 1, 0, 0, 1, 0, 1, 1, 0, 1};
		
		for(int i = 0; i < values.length; i++) {
			assertTrue("Should return " + expectedValues[i] + " but was " + returnVal[i], returnVal[i] == expectedValues[i]);
		}
		
	}
	
	@Test
	public void testFlip() {
		int[] values = {0, 1, 0, 1, 0, 0, 1, 0, 1, 1, 0, 1};
		
		Radix radix = new Radix();
		
		int[] returnVal = radix.invertBit();
		
		int[] expectedValues = {1, 0, 1, 0, 1, 1, 0, 1, 0, 0, 1, 0};
		
		for(int i = 0; i < values.length; i++) {
			assertTrue("Should return " + expectedValues[i] + " but was " + returnVal[i], returnVal[i] == expectedValues[i]);
		}
		
	}
	
	@Test
	public void testScanNormal() {
		int[] values = {0, 1, 0, 1, 0, 0, 1, 0, 1, 1, 0, 1};
		
		Radix radix = new Radix();
		
		int[] returnVal = radix.invertBit();
		
		int[] expectedValues = {0, 0, 1, 1, 2, 3, 3, 4, 4, 4, 5, 5};
		
		for(int i = 0; i < values.length; i++) {
			assertTrue("Should return " + expectedValues[i] + " but was " + returnVal[i], returnVal[i] == expectedValues[i]);
		}
		
	}
	
	@Test
	public void testScanPredicate() {
		int[] values = {1, 0, 1, 0, 1, 1, 0, 1, 0, 0, 1, 0};
		
		Radix radix = new Radix();
		
		int[] returnVal = radix.invertBit();
		
		int[] expectedValues = {0, 0, 0, 1, 1, 1, 2, 2, 3, 4, 4, 5};
		
		for(int i = 0; i < values.length; i++) {
			assertTrue("Should return " + expectedValues[i] + " but was " + returnVal[i], returnVal[i] == expectedValues[i]);
		}
		
	}

}

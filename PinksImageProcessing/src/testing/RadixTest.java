package testing;

import static org.junit.Assert.fail;

import org.junit.Test;
/**
 * Tester for radix sort.
 * @author Chet Lampron
 *
 */
public class RadixTest {

	@Test
	public void test() {
		int[] values = {10, 11, 2, 9, 0, 6, 1, 4, 7, 3, 8, 5};
		
		Radix radix = new Radix();
		
		int returnVal = radix.isolateBit();
		
		
		
	}

}

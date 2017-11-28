/**
 * 
 */
package testing;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import algorithms.PixelModifier;
import algorithms.RedEyeModifier;
import pinkprocessing.FileHandler;
import pinkprocessing.RedEye;

/**
 * @author chetlampron
 *
 */
public class RedEyeTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testTemplateAverageSmall() {
		RedEye tester = new RedEye(null);
		PixelModifier modifier = new RedEyeModifier(null, null);
		FileHandler file = new FileHandler();
	}

}

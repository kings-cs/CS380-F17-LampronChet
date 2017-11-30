/**
 * 
 */
package testing;

import static org.junit.Assert.assertTrue;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.jocl.CL;
import org.jocl.cl_device_id;
import org.junit.Before;
import org.junit.Test;

import algorithms.PixelModifier;
import algorithms.RedEyeModifier;
import parallel.JoclInitializer;
import pinkprocessing.FileHandler;
import pinkprocessing.RedEye;

/**
 * @author chetlampron
 *
 */
public class RedEyeTest {
	/** The device manager. */
	private JoclInitializer deviceManager;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
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
	 * Not Found
	 * 
	 * @throws IOException
	 */
	@Test
	public void testTemplateAverage() throws IOException {
		RedEye tester = new RedEye(deviceManager);
		PixelModifier modifier = new RedEyeModifier(null, null);
		FileHandler file = new FileHandler();
		BufferedImage template = file.createImage("Docs/red_eye_effect_template_5.png");
		int[] data = modifier.unwrapImage(template);
		int[] averages = tester.calculateTemplateAverage(data);
		assertTrue("Should be 179 for red but was: " + averages[0], averages[0] == 179);
		assertTrue("Should be 111 for green but was: " + averages[1], averages[1] == 111);
		assertTrue("Should be 115 for blue but was: " + averages[2], averages[2] == 115);
	}
	/**
	 * Not Found
	 * 
	 * @throws IOException
	 */
	@Test
	public void testTemplateDifferences() throws IOException {
		RedEye tester = new RedEye(deviceManager);
		PixelModifier modifier = new RedEyeModifier(null, null);
		FileHandler file = new FileHandler();
		BufferedImage template = file.createImage("Docs/red_eye_effect_template_5.png");
		int[] data = modifier.unwrapImage(template);
		int[] averages = tester.calculateTemplateAverage(data);
		int[] differences = tester.sumDifferenceTemplate(averages);
		assertTrue("Should be 2340920 for red but was: " + differences[0], differences[0] == 2340920);
		assertTrue("Should be 1638475 for green but was: " + differences[1], differences[1] == 1638475);
		assertTrue("Should be 1384777 for blue but was: " + differences[2], differences[2] == 1384777);
	}

}

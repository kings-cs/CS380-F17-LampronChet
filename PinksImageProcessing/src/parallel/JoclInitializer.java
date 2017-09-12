package parallel;

import org.jocl.CL;
import org.jocl.cl_context;
import org.jocl.cl_context_properties;
import org.jocl.cl_device_id;
import org.jocl.cl_platform_id;

/**
 * Builds all required items for parallel programming.
 * 
 * @author Chet lampron
 *
 */
public class JoclInitializer {
	/**
	 * Creates the OpenCL context.
	 * 
	 * @param selectedDeviceIndex
	 *            the processing device index.
	 * @return The context.
	 */
	public cl_context createContext(int selectedDeviceIndex) {
		final int platformIndex = 0;
		final long deviceType = CL.CL_DEVICE_TYPE_ALL;
		final int deviceIndex = selectedDeviceIndex;
		
		int[] numPlatformsArray = new int[1];
		CL.clGetPlatformIDs(0, null, numPlatformsArray);
		int numPlatforms = numPlatformsArray[0];
		
		cl_platform_id[] platforms = new cl_platform_id[numPlatforms];
		CL.clGetPlatformIDs(platforms.length, platforms, null);
		cl_platform_id platform = platforms[platformIndex];
		
		cl_context_properties contextProperties = new cl_context_properties();
		contextProperties.addProperty(CL.CL_CONTEXT_PLATFORM, platform);
		
		int numDevicesArray[] = new int[1];
		CL.clGetDeviceIDs(platform, deviceType, 0, null, numDevicesArray);
		int numDevices = numDevicesArray[0];
		
		cl_device_id[] devices = new cl_device_id[numDevices];
		CL.clGetDeviceIDs(platform, deviceType, numDevices, devices, null);
		
		cl_device_id device = devices[deviceIndex];
		
		cl_context context = CL.clCreateContext(contextProperties, 1, new cl_device_id[] {device}, null, null, null);
		
		return context;
	}
}

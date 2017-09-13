package parallel;

import org.jocl.CL;
import org.jocl.Pointer;
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
	 * Gets the possible platform IDs on the system.
	 * 
	 * @return The platform IDs.
	 */
	public cl_platform_id[] getPlatforms() {
		int[] numPlatformsArray = new int[1];
		CL.clGetPlatformIDs(0, null, numPlatformsArray);
		int numPlatforms = numPlatformsArray[0];

		cl_platform_id[] platforms = new cl_platform_id[numPlatforms];
		CL.clGetPlatformIDs(platforms.length, platforms, null);

		return platforms;
	}

	/**
	 * Gets the device IDs on the platforms.
	 * 
	 * @return The device IDs
	 */
	public cl_device_id[] getDeviceIds() {
		cl_platform_id[] platforms = getPlatforms();
		cl_device_id[] devices = new cl_device_id[platforms.length * 3];

		int devicePlaceCounter = 0;
		for (int i = 0; i < platforms.length; i++) {
			int numDevicesArray[] = new int[1];
			CL.clGetDeviceIDs(platforms[i], CL.CL_DEVICE_TYPE_ALL, 0, null, numDevicesArray);
			int numDevices = numDevicesArray[0];

			cl_device_id[] deviceArray = new cl_device_id[numDevices];
			CL.clGetDeviceIDs(platforms[i], CL.CL_DEVICE_TYPE_ALL, numDevices, deviceArray, null);
			for (int j = 0; j < deviceArray.length; j++) {
				if (devicePlaceCounter < devices.length) {
					devices[devicePlaceCounter] = deviceArray[j];
					devicePlaceCounter++;
				}
			}

		}
		return devices;
	}

	/**
	 * Gets device names for GUI.
	 * 
	 * @return The device names.
	 */
	public String[] getDeviceNames() {
		cl_device_id[] devices = getDeviceIds();
		int resultSize = 0;
		for(int i = 0; i < devices.length; i++) {
			long[] size = new long[1];
			CL.clGetDeviceInfo(devices[i], CL.CL_DEVICE_NAME, 0, null, size);
			resultSize += (int)size[0];
		}
		String[] result = new String[resultSize];

		for (int i = 0; i < devices.length; i++) {
			long[] size = new long[1];
			CL.clGetDeviceInfo(devices[i], CL.CL_DEVICE_NAME, 0, null, size);

			byte[] buffer = new byte[(int) size[0]];
			CL.clGetDeviceInfo(devices[i], CL.CL_DEVICE_NAME, buffer.length, Pointer.to(buffer), null);
			System.out.println(buffer.length);
			result[i] = new String(buffer, 0, buffer.length - 1);
		}
		return result;
	}

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

		cl_context context = CL.clCreateContext(contextProperties, 1, new cl_device_id[] { device }, null, null, null);

		return context;
	}
}

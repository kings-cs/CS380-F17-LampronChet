package parallel;

import org.jocl.CL;
import org.jocl.Pointer;
import org.jocl.cl_command_queue;
import org.jocl.cl_context;
import org.jocl.cl_context_properties;
import org.jocl.cl_device_id;
import org.jocl.cl_platform_id;

import pink.PipGui;

/**
 * Builds all required items for parallel programming.
 * 
 * @author Chet lampron
 *
 */
public class JoclInitializer {
	/** The created context. */
	private cl_context context;
	/** The selected device. */
	private cl_device_id device;
	/** The created command queue. */
	private cl_command_queue queue;

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
		int resultSize = 0;
		for (int i = 0; i < platforms.length; i++) {
			int[] size = new int[1];
			CL.clGetDeviceIDs(platforms[i], CL.CL_DEVICE_TYPE_ALL, 0, null, size);
			resultSize += size[0];
		}
		cl_device_id[] devices = new cl_device_id[resultSize];

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
		String[] result = new String[devices.length];

		for (int i = 0; i < devices.length; i++) {
			long[] size = new long[1];
			CL.clGetDeviceInfo(devices[i], CL.CL_DEVICE_NAME, 0, null, size);

			byte[] buffer = new byte[(int) size[0]];
			CL.clGetDeviceInfo(devices[i], CL.CL_DEVICE_NAME, buffer.length, Pointer.to(buffer), null);
			result[i] = new String(buffer, 0, buffer.length - 1);
			PipGui.getDeviceMap().put(result[i], devices[i]);
		}
		return result;
	}

	/**
	 * Creates the OpenCL context.
	 * 
	 * @param theDevice
	 *            The selected device.
	 * @return The context.
	 */
	public cl_context createContext(cl_device_id theDevice) {
		int platformIndex = 0;
		cl_platform_id[] platforms = getPlatforms();
		cl_platform_id platform = platforms[platformIndex];

		cl_context_properties contextProperties = new cl_context_properties();
		contextProperties.addProperty(CL.CL_CONTEXT_PLATFORM, platform);

		device = theDevice;

		context = CL.clCreateContext(contextProperties, 1, new cl_device_id[] { device }, null, null, null);

		return context;
	}

	/**
	 * Creates the command queue.
	 */
	public void createQueue() {
		queue = CL.clCreateCommandQueue(context, device, 0, null);
	}

	/**
	 * Gets the context.
	 * 
	 * @return the context
	 */
	public cl_context getContext() {
		return context;
	}

	/**
	 * Gets the queue.
	 * 
	 * @return the queue
	 */
	public cl_command_queue getQueue() {
		return queue;
	}

	/**
	 * Determines if a device is a GPU.
	 * 
	 * @param theDevice
	 *            The specified device.
	 * @return isGpu.
	 */
	public boolean isGpu(cl_device_id theDevice) {
		boolean isGpu = false;
		long[] size = new long[1];
		CL.clGetDeviceInfo(theDevice, CL.CL_DEVICE_TYPE, 0, null, size);

		byte[] buffer = new byte[(int) size[0]];
		CL.clGetDeviceInfo(theDevice, CL.CL_DEVICE_TYPE, buffer.length, Pointer.to(buffer), null);
		String result = new String(buffer, 0, buffer.length - 1);

		if (result.equalsIgnoreCase("GPU")) {
			isGpu = true;
		}

		return isGpu;
	}
}

package pinkprocessing;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.jocl.CL;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_kernel;
import org.jocl.cl_mem;
import org.jocl.cl_program;

import parallel.JoclInitializer;
import testing.BlellochScan;

/**
 * Class containing the methods for radix sort.
 * 
 * @author Chet Lampron
 *
 */
public class Radix {
	/** The work size. */
	private int workSize;
	/** The device manager. */
	private JoclInitializer deviceManager;
	/** The calculated runtime. */
	private long calculatedRuntime;

	public Radix(int theWorkSize, JoclInitializer deviceManager) {
		workSize = theWorkSize;
		this.deviceManager = deviceManager;
	}

	public int[] isolateBit(int[] values, int bit) throws FileNotFoundException {
		int[] returnBits = new int[values.length];
		int[] bitMask = { bit };
		Pointer ptrValues = Pointer.to(values);
		Pointer ptrReturn = Pointer.to(returnBits);
		Pointer ptrMask = Pointer.to(bitMask);

		cl_mem memValues = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * values.length, ptrValues, null);
		cl_mem memReturn = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * returnBits.length, ptrReturn, null);
		cl_mem memMask = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * bitMask.length, ptrMask, null);

		String kernelLocation = "Kernels/RadixKernels";
		String kernelName = "isolateBit";

		File kernelFile = new File(kernelLocation);
		Scanner kernelScan = new Scanner(kernelFile);
		StringBuffer sourceBuffer = new StringBuffer();
		while (kernelScan.hasNextLine()) {
			sourceBuffer.append(kernelScan.nextLine());
			sourceBuffer.append("\n");
		}
		cl_program program = CL.clCreateProgramWithSource(deviceManager.getContext(), 1,
				new String[] { sourceBuffer.toString() }, null, null);

		CL.clBuildProgram(program, 0, null, null, null, null);

		long[] globalWorkSize = new long[] { values.length };
		long[] localWorkSize = new long[] { workSize };
		deviceManager.createQueue();
		cl_kernel isolateKernel = CL.clCreateKernel(program, kernelName, null);

		CL.clSetKernelArg(isolateKernel, 0, Sizeof.cl_mem, Pointer.to(memValues));
		CL.clSetKernelArg(isolateKernel, 1, Sizeof.cl_mem, Pointer.to(memReturn));
		CL.clSetKernelArg(isolateKernel, 2, Sizeof.cl_mem, Pointer.to(memMask));

		double startTime = System.nanoTime();
		CL.clEnqueueNDRangeKernel(deviceManager.getQueue(), isolateKernel, 1, null, globalWorkSize, localWorkSize, 0,
				null, null);
		calculatedRuntime += System.nanoTime() - startTime;

		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memReturn, CL.CL_TRUE, 0, returnBits.length * Sizeof.cl_int,
				ptrReturn, 0, null, null);

		CL.clReleaseKernel(isolateKernel);
		CL.clReleaseProgram(program);
		CL.clReleaseMemObject(memValues);
		CL.clReleaseMemObject(memReturn);
		CL.clReleaseMemObject(memMask);
		kernelScan.close();
		// for (int i = 0; i < values.length; i++) {
		// returnBits[i] = (values[i] >> bit) & 1;
		// }
		return returnBits;
	}

	public int[] flipBits(int[] values) throws FileNotFoundException {
		int[] returnBits = new int[values.length];

		Pointer ptrValues = Pointer.to(values);
		Pointer ptrReturn = Pointer.to(returnBits);

		cl_mem memValues = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * values.length, ptrValues, null);
		cl_mem memReturn = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * returnBits.length, ptrReturn, null);

		String kernelLocation = "Kernels/RadixKernels";
		String kernelName = "flipBit";

		File kernelFile = new File(kernelLocation);
		Scanner kernelScan = new Scanner(kernelFile);
		StringBuffer sourceBuffer = new StringBuffer();
		while (kernelScan.hasNextLine()) {
			sourceBuffer.append(kernelScan.nextLine());
			sourceBuffer.append("\n");
		}
		cl_program program = CL.clCreateProgramWithSource(deviceManager.getContext(), 1,
				new String[] { sourceBuffer.toString() }, null, null);

		CL.clBuildProgram(program, 0, null, null, null, null);

		long[] globalWorkSize = new long[] { values.length };
		long[] localWorkSize = new long[] { workSize };
		deviceManager.createQueue();
		cl_kernel isolateKernel = CL.clCreateKernel(program, kernelName, null);

		CL.clSetKernelArg(isolateKernel, 0, Sizeof.cl_mem, Pointer.to(memValues));
		CL.clSetKernelArg(isolateKernel, 1, Sizeof.cl_mem, Pointer.to(memReturn));

		double startTime = System.nanoTime();
		CL.clEnqueueNDRangeKernel(deviceManager.getQueue(), isolateKernel, 1, null, globalWorkSize, localWorkSize, 0,
				null, null);
		calculatedRuntime += System.nanoTime() - startTime;

		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memReturn, CL.CL_TRUE, 0, returnBits.length * Sizeof.cl_int,
				ptrReturn, 0, null, null);

		CL.clReleaseKernel(isolateKernel);
		CL.clReleaseProgram(program);
		CL.clReleaseMemObject(memValues);
		CL.clReleaseMemObject(memReturn);
		kernelScan.close();
		// for (int i = 0; i < values.length; i++) {
		// returnBits[i] = (values[i] ^ 1);
		// }
		return returnBits;
	}

	public int[] scan(int[] values) throws FileNotFoundException {
		BlellochScan scan = new BlellochScan(deviceManager);
		int[] result = new int[values.length];
		scan.scan(values, result);
		return result;
	}

	public int[] calculateAdress(int[] data, int[] values, int[] predicateValues, int[] normalScan, int[] predicateScan, int[] returnBits)
			throws FileNotFoundException {
		int[] dimensions = { predicateValues.length };

		Pointer ptrData = Pointer.to(data);
		Pointer ptrValues = Pointer.to(values);
		Pointer ptrPredicateValues = Pointer.to(predicateValues);
		Pointer ptrNormalScan = Pointer.to(normalScan);
		Pointer ptrPredicateScan = Pointer.to(predicateScan);
		Pointer ptrReturn = Pointer.to(returnBits);
		Pointer ptrDimensions = Pointer.to(dimensions);

		cl_mem memData = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * data.length, ptrData, null);
		cl_mem memValues = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * values.length, ptrValues, null);
		cl_mem memPredicateValues = CL.clCreateBuffer(deviceManager.getContext(),
				CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * predicateValues.length,
				ptrPredicateValues, null);
		cl_mem memNormalScan = CL.clCreateBuffer(deviceManager.getContext(),
				CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * normalScan.length, ptrNormalScan,
				null);
		cl_mem memPredicateScan = CL.clCreateBuffer(deviceManager.getContext(),
				CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * predicateScan.length, ptrPredicateScan,
				null);
		cl_mem memReturn = CL.clCreateBuffer(deviceManager.getContext(), CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * returnBits.length, ptrReturn, null);
		cl_mem memDimensions = CL.clCreateBuffer(deviceManager.getContext(),
				CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * dimensions.length, ptrDimensions,
				null);

		String kernelLocation = "Kernels/RadixKernels";
		String kernelName = "calculateAdress";

		File kernelFile = new File(kernelLocation);
		Scanner kernelScan = new Scanner(kernelFile);
		StringBuffer sourceBuffer = new StringBuffer();
		while (kernelScan.hasNextLine()) {
			sourceBuffer.append(kernelScan.nextLine());
			sourceBuffer.append("\n");
		}
		cl_program program = CL.clCreateProgramWithSource(deviceManager.getContext(), 1,
				new String[] { sourceBuffer.toString() }, null, null);

		CL.clBuildProgram(program, 0, null, null, null, null);

		long[] globalWorkSize = new long[] { values.length };
		long[] localWorkSize = new long[] { workSize };
		deviceManager.createQueue();
		cl_kernel isolateKernel = CL.clCreateKernel(program, kernelName, null);

		CL.clSetKernelArg(isolateKernel, 0, Sizeof.cl_mem, Pointer.to(memData));
		CL.clSetKernelArg(isolateKernel, 1, Sizeof.cl_mem, Pointer.to(memValues));
		CL.clSetKernelArg(isolateKernel, 2, Sizeof.cl_mem, Pointer.to(memPredicateValues));
		CL.clSetKernelArg(isolateKernel, 3, Sizeof.cl_mem, Pointer.to(memNormalScan));
		CL.clSetKernelArg(isolateKernel, 4, Sizeof.cl_mem, Pointer.to(memPredicateScan));
		CL.clSetKernelArg(isolateKernel, 5, Sizeof.cl_mem, Pointer.to(memReturn));
		CL.clSetKernelArg(isolateKernel, 6, Sizeof.cl_mem, Pointer.to(memDimensions));

		double startTime = System.nanoTime();
		CL.clEnqueueNDRangeKernel(deviceManager.getQueue(), isolateKernel, 1, null, globalWorkSize, localWorkSize, 0,
				null, null);
		calculatedRuntime += System.nanoTime() - startTime;

		CL.clEnqueueReadBuffer(deviceManager.getQueue(), memReturn, CL.CL_TRUE, 0, returnBits.length * Sizeof.cl_int,
				ptrReturn, 0, null, null);

		CL.clReleaseKernel(isolateKernel);
		CL.clReleaseProgram(program);
		CL.clReleaseMemObject(memValues);
		CL.clReleaseMemObject(memReturn);
		CL.clReleaseMemObject(memPredicateScan);
		CL.clReleaseMemObject(memNormalScan);
		CL.clReleaseMemObject(memPredicateValues);
		CL.clReleaseMemObject(memData);
		kernelScan.close();

		// for (int i = 0; i < values.length; i++) {
		// int p = values[i];
		// int notP = predicateValues[i];
		// int scanP = normalScan[i];
		// int scanNotP = predicateScan[i];
		// int first = 0;
		// int second = 0;
		// if (p == 1) {
		// if (predicateValues[values.length - 1] == 1) {
		// first = predicateScan[values.length - 1] + 1;
		// } else {
		// first = predicateScan[values.length - 1];
		// }
		// }
		// if (notP == 1) {
		// second = scanNotP;
		// } else {
		// second = scanP;
		// }
		// int adress = first + second;
		// returnBits[adress] = data[i];
		// }
		return returnBits;
	}

	public void fullSort(int[] data, int[] result) throws FileNotFoundException {
		for (int i = 0; i < 32; i++) {
			int[] bits = isolateBit(data, i);
			int[] flippedBits = flipBits(bits);
			int[] normalScan = scan(bits);
			int[] predicateScan = scan(flippedBits);
			calculateAdress(data, bits, flippedBits, normalScan, predicateScan, result);
		}
	}

}

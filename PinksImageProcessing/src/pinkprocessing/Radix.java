package pinkprocessing;

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
	
	private JoclInitializer deviceManager;
	
	public Radix(int theWorkSize, JoclInitializer deviceManager) {
		workSize = theWorkSize;
		this.deviceManager = deviceManager;
	}

	public int[] isolateBit(int[] values, int bit) {
		int[] returnBits = new int[values.length];
		for(int i = 0; i < values.length; i++) {
			returnBits[i] = (values[i] >> bit) & 1;
		}
		return returnBits;
	}

	public int[] flipBits(int[] values) {
		int[] returnBits = new int[values.length];
		for(int i = 0; i < values.length; i++) {
			returnBits[i] = (values[i] ^ 1);
		}
		return returnBits;
	}

	public int[] scan(int[] values) {
		BlellochScan scan = new BlellochScan(deviceManager);
		int[] result = new int[values.length];
		scan.scan(values, result);
	}

	public int[] calculateAdress(int[] values, int[] predicateValues, int[] normalScan, int[] predicateScan) {
		int[] returnBits = new int[values.length];
		for(int i = 0; i < values.length; i++) {
			int p = values[i];
			int notP = predicateValues[i];
			int scanP = normalScan[i];
		}
		return returnBits;
	}

}

package pinkprocessing;

/**
 * Class containing the methods for radix sort.
 * 
 * @author Chet Lampron
 *
 */
public class Radix {
	/** The work size. */
	private int workSize;
	
	public Radix(int theWorkSize) {
		workSize = theWorkSize;
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
		// TODO Auto-generated method stub
		return null;
	}

	public int[] calculateAdress(int[] values, int[] predicateValues, int[] normalScan, int[] predicateScan) {
		int[] returnBits = new int[values.length];
		for(int i = 0; i < values.length; i++) {
			in
		}
		return returnBits;
	}

}

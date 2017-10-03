/**
 * 
 */
package pinkprocessing;

/**
 * Stores the location of the tile points within an image.
 * 
 * @author Chet Lampron
 *
 */
public class TileDimensions {
	/** The tiles row. */
	private int row;
	/** The tiles column. */
	private int col;

	/**
	 * Constructs a TileDimensions object.
	 * 
	 * @param aRow
	 *            The row of the pixel.
	 * @param aCol
	 *            The column of the pixel.
	 */
	public TileDimensions(int aRow, int aCol) {
		row = aRow;
		col = aCol;
	}

	/**
	 * Getter for row.
	 * 
	 * @return the row
	 */
	public int getRow() {
		return row;
	}

	/**
	 * Getter for column.
	 * 
	 * @return the col
	 */
	public int getCol() {
		return col;
	}

}

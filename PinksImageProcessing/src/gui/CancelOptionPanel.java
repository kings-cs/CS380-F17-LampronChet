/**
 * 
 */
package gui;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Allows the user to properly hit cancel on undesired input.
 * 
 * @author Chet Lampron
 *
 */
public class CancelOptionPanel extends JPanel {
	/**
	 * The default id.
	 */
	private static final long serialVersionUID = 1L;
	/** The tiles field. */
	private JTextField tiles = new JTextField(5);

	/**
	 * Consturucts a CancelOptionPanel.
	 * 
	 * @param typeOf
	 *            The type of things in this quantity.
	 */
	public CancelOptionPanel(String typeOf) {
		add(new JLabel(typeOf + ": "));
		add(tiles);
	}

	/**
	 * Gets the tiles field.
	 * 
	 * @return the minField
	 */
	public JTextField getTiles() {
		return tiles;
	}

}

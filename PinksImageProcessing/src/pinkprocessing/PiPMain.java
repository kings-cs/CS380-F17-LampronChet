/**
 * 
 */
package pinkprocessing;

import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.UIManager.LookAndFeelInfo;

import org.jocl.CL;

import gui.PiPGui;

/**
 * Contains the main method.
 * 
 * @author Chet
 *
 */
public class PiPMain {
	/**
	 * The main method.
	 * 
	 * @param args
	 *            not used.
	 */
	public static void main(String[] args) {
		CL.setExceptionsEnabled(true);
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) 
		{
			JOptionPane.showMessageDialog(null, "The Nimbus look and feel is not available");
		}

		PiPGui display = null;
		try {
			display = new PiPGui();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Borders could not be found");
			System.exit(1);
		}
		display.setCurrentGui(display);
		display.setSize(400, 400);
		display.setVisible(true);
	}
}

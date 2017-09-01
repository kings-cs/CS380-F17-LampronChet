/**
 * 
 */
package pink;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

/**
 * GUI for an image processor.
 * 
 * @author Chet
 *
 */
public class PipGui extends JFrame {

	/**
	 * Default generated ID.
	 */
	private static final long serialVersionUID = 1L;

	public PipGui() {
		this.setTitle("Pinks Image Processor");
		String iconPath = "Docs/PinkIcon.jpg";
		ImageIcon icon = new ImageIcon(iconPath);
		this.setIconImage(icon.getImage());

		JMenuBar menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);
		JMenu file = new JMenu("File");
		JMenu about = new JMenu("About");
		menuBar.add(file);
		menuBar.add(about);
		JMenuItem open = new JMenuItem("Open");
		open.addActionListener(new OpenFile());
		file.add(open);

	}

	private class OpenFile implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			JFileChooser choose = new JFileChooser("Select your image: ");
			int val = choose.showOpenDialog(null);

			if (val == JFileChooser.APPROVE_OPTION) {
				String filePath = choose.getSelectedFile().getAbsolutePath();
				JOptionPane.showConfirmDialog(null, filePath);
			}
		}

	}

	public static void main(String[] args) {
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "The Nimbus look and feel is not available");
		}

		PipGui display = new PipGui();
		display.setSize(400, 400);
		display.setVisible(true);
	}
}

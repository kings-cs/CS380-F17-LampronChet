/**
 * 
 */
package pink;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
	/** The instance of the FileHandler class. */
	private FileHandler fileHandler;
	/** The main back panel. */
	private JPanel backPanel;
	/** The backPanels center component. */
	private JPanel centerPanel;

	public PipGui() {
		fileHandler = new FileHandler();
		
		this.setTitle("Pinks Image Processor");
		String iconPath = "Docs/PinkIcon.jpg";
		ImageIcon icon = new ImageIcon(iconPath);
		this.setIconImage(icon.getImage());

		JMenuBar menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);
		JMenu file = new JMenu("File");
		JMenu options = new JMenu("Options");
		JMenuItem grayscale = new JMenuItem("Grayscale");
		options.add(grayscale);
		JMenu about = new JMenu("About");
		menuBar.add(file);
		menuBar.add(options);
		menuBar.add(about);
		JMenuItem open = new JMenuItem("Open");
		open.addActionListener(new OpenFile());
		file.add(open);
		
		backPanel = new JPanel();
		backPanel.setLayout(new BorderLayout());
		this.add(backPanel);
		centerPanel = new JPanel();
		centerPanel.setLayout(new FlowLayout());
		backPanel.add(centerPanel, BorderLayout.CENTER);
		

	}

	private class OpenFile implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			JFileChooser choose = new JFileChooser("Select your image: ");
			int val = choose.showOpenDialog(null);

			if (val == JFileChooser.APPROVE_OPTION) {
				String filePath = choose.getSelectedFile().getAbsolutePath();
				BufferedImage image = null;
				try {
					image = fileHandler.createImage(filePath);
				} catch (IOException e) {
					JOptionPane.showMessageDialog(null, "Image could not be processed");
				}
				Graphics g = image.getGraphics();
				centerPanel.paintComponents(g);
				g.drawImage(image, 15, 15, centerPanel);
				getGui().setExtendedState(getGui().getExtendedState() | JFrame.MAXIMIZED_BOTH);
				//centerPanel.repaint();
				//JOptionPane.showConfirmDialog(null, filePath);
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
	
	private PipGui getGui() {
		return this;
	}
}

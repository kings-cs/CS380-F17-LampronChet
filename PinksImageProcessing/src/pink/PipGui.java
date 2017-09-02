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
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * GUI for an image processor.
 * 
 * @author Chet Lampron
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
	private CenterPainter centerPanel;
	/** The current image. */
	private BufferedImage image;
	/** The current Gui. */
	private PipGui currentGui;
	/** The temporary side walls. */
	private BufferedImage sideMural;
	/** The side panels. */
	private MuralPanel sides;

	/**
	 * Constructor for a PipGui.
	 * @throws IOException 
	 */
	public PipGui() throws IOException {
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
		centerPanel = new CenterPainter();
		centerPanel.setLayout(new FlowLayout());
		backPanel.add(centerPanel, BorderLayout.CENTER);
		
		
		sides = new MuralPanel();
		backPanel.add(sides, BorderLayout.EAST);
		backPanel.add(sides, BorderLayout.WEST);
		String borderFilePath = "Docs/sideMural.jpg";
		BufferedImage borderImage = fileHandler.createImage(borderFilePath);
		Graphics borderGraphics = borderImage.getGraphics();
		//borderGraphics.drawImage(borderImage, 0, 0, sides);
		sides.paintComponent(borderGraphics);
		

	}

	/**
	 * Class to listen to the open option button.
	 * 
	 * @author Chet
	 *
	 */
	private class OpenFile implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			JFileChooser choose = new JFileChooser("Select your image: ");
			FileFilter filter = new FileNameExtensionFilter("Pictures", new String[] { "jpg", "jpeg", "png", "gif" });
			choose.setFileFilter(filter);
			int val = choose.showOpenDialog(null);

			if (val == JFileChooser.APPROVE_OPTION) {
				String filePath = choose.getSelectedFile().getAbsolutePath();
				image = null;
				Graphics g = null;
				try {
					image = fileHandler.createImage(filePath);
					centerPanel.setSize(image.getWidth(), image.getHeight());

					g = image.getGraphics();
					// getGui().setExtendedState(getGui().getExtendedState() |
					// JFrame.MAXIMIZED_BOTH);
					g.drawImage(image, 0, 0, centerPanel);
					centerPanel.paintComponent(g);
				} catch (IOException e) {
					JOptionPane.showMessageDialog(null, "Image could not be processed");
				}

				// centerPanel.repaint();
				// JOptionPane.showConfirmDialog(null, filePath);
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

		PipGui display = null;
		try {
			display = new PipGui();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Borders could not be found");
			System.exit(ERROR);
		}
		display.setCurrentGui(display);
		display.setSize(400, 400);
		display.setVisible(true);
	}

	/**
	 * In class use only.
	 * 
	 * @return The current Gui.
	 */
	private PipGui getGui() {
		return this;
	}
	
	private void setCurrentGui(PipGui current) {
		currentGui = current;
	}
	/**
	 * 
	 * @author Chet
	 *
	 */
	private class MuralPanel extends JPanel {

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if(sideMural != null) {
				g.fillRect(0, 0, sides.getWidth(), sides.getHeight());
			}
		}
	}

	private class CenterPainter extends JPanel {
		/**
		 * Default Serial Number
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (image != null) {
				g.drawImage(image, 0, 0, this);
			}
		}
	}
}

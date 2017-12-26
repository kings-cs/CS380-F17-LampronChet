
package gui;

import java.awt.BorderLayout;
//import java.awt.Color;
//import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
//import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
import java.io.IOException;
//import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
//import org.jocl.CL;
import org.jocl.cl_device_id;

import algorithms.BlurModifier;
import algorithms.BlurModifierParallel;
import algorithms.GrayscaleEqualizationModifier;
import algorithms.GrayscaleModifier;
import algorithms.GrayscaleModifierParallel;
import algorithms.HorizontalFlip;
import algorithms.MosaicModifier;
import algorithms.MosaicModifierParallel;
import algorithms.RedEyeModifier;
import algorithms.RotateLeft;
import algorithms.RotateRight;
import algorithms.SeamlessCloneModifier;
import algorithms.SepiaModifier;
import algorithms.SepiaModifierParallel;
import algorithms.VerticalFlip;
import algorithms.ZoomIn;
import parallel.JoclInitializer;
import pinkprocessing.FileHandler;

/**
 * GUI for an image processor.
 * 
 * @author Chet Lampron
 *
 */
public class PiPGui extends JFrame {

	/**
	 * Default generated ID.
	 */
	private static final long serialVersionUID = 1L;
	/** Maps a device name to it's ID. */
	private static Map<String, cl_device_id> deviceMap;

	/** The instance of the FileHandler class. */
	private FileHandler fileHandler;
	/** The main back panel. */
	private JPanel backPanel;
	/** The backPanels center component. */
	private CenterPainter centerPanel;
	/** The current image. */
	private BufferedImage image;
	/** The current Gui. */
	private PiPGui currentGui;
	/** The temporary side walls. */
	private BufferedImage sideMural;
	/** The side panels. */
	private MuralPanel leftSide;
	/** The side panels. */
	private MuralPanel rightSide;
	/** Stores the saved status of the file. */
	private boolean isSaved;
	/** The save item. */
	private JMenuItem save;
	/** The open item. */
	private JMenuItem open;
	/** The zoom in button. */
	private JMenuItem zoomIn;
	/** The zoom out button. */
	private JMenuItem zoomOut;
	/** Manages the devices on this computer to allow for parallel processing. */
	private JoclInitializer deviceManager;
	/** The button group for the devices. */
	private ButtonGroup deviceGroup;
	/** The about JFrame. */
	private JFrame webPage;
	/** The about scroll pane. */
	private JScrollPane aboutPane;

	/**
	 * Constructor for a PipGui.
	 * 
	 * @throws IOException
	 *             When file data is lost.
	 */
	public PiPGui() throws IOException {

		JEditorPane webPane = new JEditorPane();
		webPane.setEditable(false);
		// URL webSite = new URL(
		// "https://raw.githubusercontent.com/kings-cs/CS380-F17-LampronChet/master/README.md?token=AQf_j6gK-4p0NZ5KmSw-E5bXVGKbbfb9ks5aAl5MwA%3D%3D");
		// webPane.setPage(webSite);

		aboutPane = new JScrollPane(webPane);
		webPage = new JFrame("About");
		webPage.setVisible(false);
		webPage.add(aboutPane);

		// webPage.addWindowListener(new WindowAdapter() {
		// @Override
		// public void windowClosed(WindowEvent e) {
		// dispose();
		// }
		// });

		deviceMap = new HashMap<>();
		deviceManager = new JoclInitializer();
		isSaved = true;
		fileHandler = new FileHandler();

		this.setTitle("PIP!");
		String iconPath = "Docs/PinkIcon.jpg";
		ImageIcon icon = new ImageIcon(iconPath);
		this.setIconImage(icon.getImage());

		JMenuBar menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);
		JMenu file = new JMenu("File");
		// file.setBackground(Color.DARK_GRAY);
		// file.setForeground(new Color(171, 14, 165));
		JMenu options = new JMenu("Options");
		JMenu transform = new JMenu("Transform");
		JMenu zoom = new JMenu("Zoom");

		JMenuItem horizontalFlip = new JMenuItem("Horizontal flip");
		transform.add(horizontalFlip);
		horizontalFlip.addActionListener(new HorizontalFlipListener());

		JMenuItem verticalFlip = new JMenuItem("Vertical flip");
		transform.add(verticalFlip);
		verticalFlip.addActionListener(new VerticalFlipListener());

		JMenuItem rotateLeft = new JMenuItem("Rotate left");
		transform.add(rotateLeft);
		rotateLeft.addActionListener(new RotateLeftListener());

		JMenuItem rotateRight = new JMenuItem("Rotate right");
		transform.add(rotateRight);
		rotateRight.addActionListener(new RotateRightListener());

		JMenuItem grayscale = new JMenuItem("Grayscale");
		grayscale.addActionListener(new GrayscaleImage());
		options.add(grayscale);

		JMenuItem parallelGray = new JMenuItem("Grayscale(Parallel)");
		parallelGray.addActionListener(new GrayParallel());
		options.add(parallelGray);

		JMenuItem sepia = new JMenuItem("Sepia");
		sepia.addActionListener(new SepiaImage());
		options.add(sepia);

		JMenuItem parallelSepia = new JMenuItem("Sepia(Parallel)");
		parallelSepia.addActionListener(new SepiaParallel());
		options.add(parallelSepia);

		JMenuItem blur = new JMenuItem("Blur");
		blur.addActionListener(new BlurImage());
		options.add(blur);

		JMenuItem parallelBlur = new JMenuItem("Blur(Parallel)");
		parallelBlur.addActionListener(new BlurParallel());
		options.add(parallelBlur);

		JMenuItem mosaic = new JMenuItem("Mosaic tiles");
		mosaic.addActionListener(new MosaicImage());
		options.add(mosaic);

		JMenuItem mosaicParallel = new JMenuItem("Mosaic tiles(Parallel)");
		mosaicParallel.addActionListener(new MosaicParallel());
		options.add(mosaicParallel);

		JMenuItem grayscaleEqualization = new JMenuItem("Equalize grayscale image");
		grayscaleEqualization.addActionListener(new EqualizeGrayscaleImage());
		options.add(grayscaleEqualization);

		JMenuItem grayscaleEqualizationO = new JMenuItem("Efficient Equalize grayscale image");
		grayscaleEqualizationO.addActionListener(new OptimizedEqualizeGrayscaleImage());
		options.add(grayscaleEqualizationO);
		// TODO

		JMenuItem redEyeRemoval = new JMenuItem("Remove red eye");
		redEyeRemoval.addActionListener(new RedEyeListener());
		options.add(redEyeRemoval);

		JMenuItem seamlessClone = new JMenuItem("Merge images");
		seamlessClone.addActionListener(new SeamlessListener());
		options.add(seamlessClone);

		ActionListener zoomListen = new ZoomListener();
		zoomIn = new JMenuItem("Zoom In");
		zoomIn.addActionListener(zoomListen);
		zoom.add(zoomIn);

		zoomOut = new JMenuItem("Zoom Out");
		zoomOut.addActionListener(zoomListen);
		zoom.add(zoomOut);

		JMenuItem about = new JMenuItem("About");
		about.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
		about.setSize(30, options.getHeight());
		about.addActionListener(new AboutFile());
		menuBar.add(file);
		menuBar.add(options);
		menuBar.add(transform);
		menuBar.add(zoom);
		menuBar.add(about);
		open = new JMenuItem("Open");
		open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));

		open.addActionListener(new OpenFile());
		file.add(open);

		save = new JMenuItem("Save");
		file.add(save);
		save.addActionListener(new SaveFile());
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));

		JMenuItem close = new JMenuItem("Close");
		file.add(close);
		close.addActionListener(new CloseFile());

		JMenuItem exit = new JMenuItem("Exit");
		exit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (!isSaved) {
					int ans = JOptionPane.showConfirmDialog(null, "Would you like to save this file?");
					if (ans == JOptionPane.YES_OPTION) {
						save.doClick();
						System.exit(0);
					} else if (ans == JOptionPane.NO_OPTION) {
						dispose();
						System.exit(0);
					}
				} else {
					dispose();
					System.exit(0);
				}
			}

		});
		file.add(exit);

		JMenu devices = new JMenu("Devices");
		deviceGroup = new ButtonGroup();
		String[] deviceNames = deviceManager.getDeviceNames();
		for (int i = 0; i < deviceNames.length; i++) {
			JRadioButtonMenuItem newButton = new SpecialRadioButton(deviceNames[i]);

			newButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					if (newButton.isSelected()) {
						deviceManager.createContext(PiPGui.deviceMap.get(newButton.getName()));
					}

				}

			});
			if (deviceGroup.getSelection() == null) {
				if (deviceManager.isGpu(PiPGui.deviceMap.get(newButton.getName()))) {
					newButton.doClick();
				}
			}
			deviceGroup.add(newButton);
			devices.add(newButton);
		}
		menuBar.add(devices);

		backPanel = new JPanel();
		backPanel.setLayout(new BorderLayout());
		this.add(backPanel);
		centerPanel = new CenterPainter();
		centerPanel.setLayout(new FlowLayout());
		backPanel.add(centerPanel, BorderLayout.CENTER);

		JScrollPane scrollImage = new JScrollPane(centerPanel);
		scrollImage.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollImage.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollImage.setPreferredSize(centerPanel.getPreferredSize());
		backPanel.add(scrollImage, BorderLayout.CENTER);

		leftSide = new MuralPanel();
		leftSide.setPreferredSize(new Dimension(50, 350));
		// leftSide.setBackground(Color.red);
		backPanel.add(leftSide, BorderLayout.WEST);
		String borderFilePath = "Docs/SpectrumBorder.jpg";
		sideMural = fileHandler.createImage(borderFilePath);
		// Graphics borderGraphics = borderImage.getGraphics();
		// borderGraphics.drawImage(borderImage, 0, 0, leftSide);
		leftSide.repaint();

		rightSide = new MuralPanel();
		rightSide.setPreferredSize(new Dimension(50, 350));
		// rightSide.setBackground(Color.red);
		backPanel.add(rightSide, BorderLayout.EAST);
		rightSide.repaint();

		addWindowListener(new ExitListener());

	}

	/**
	 * Class to listen to the open option button.
	 * 
	 * @author Chet Lampron
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
				try {
					String filePath = choose.getSelectedFile().getAbsolutePath();
					image = null;
					image = fileHandler.createImage(filePath);
					centerPanel.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
					getGui().setSize(new Dimension(720, 720));
					isSaved = false;
					centerPanel.repaint();
					centerPanel.revalidate();
				} catch (IOException e) {
					JOptionPane.showMessageDialog(null, "Image could not be processed");
				} catch (NullPointerException n) {
					JOptionPane.showMessageDialog(null,
							"Oops! Looks like you double clicked your folder instead of a file. Try again!");
					open.doClick();
				}

			}
		}

	}

	/**
	 * Listens for the window closing to make sure no file goes unsaved.
	 * 
	 * @author Chet Lampron
	 *
	 */
	private class ExitListener extends WindowAdapter {

		@Override
		public void windowClosing(WindowEvent e) {
			if (!isSaved) {
				int ans = JOptionPane.showConfirmDialog(null, "Would you like to save this file?");
				if (ans == JOptionPane.YES_OPTION) {
					save.doClick();
					dispose();
					System.exit(0);
				} else if (ans == JOptionPane.NO_OPTION) {
					dispose();
					System.exit(0);
				} // else {
					// getGui().setVisible(true);
					// }

			} else {
				dispose();
				System.exit(0);
			}
		}

	}

	/**
	 * Opens the readme file for the user.
	 * 
	 * @author Chet Lampron
	 *
	 */
	private class AboutFile implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// webPage.setSize(new Dimension(500, 500));
			// webPage.setVisible(true);
			// webPage.toFront();
			JOptionPane.showMessageDialog(null, "Down for Maintenance!");
		}

	}

	/**
	 * Class used to save files to the hard drive.
	 * 
	 * @author Chet
	 *
	 */
	private class SaveFile implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (image != null) {
				JFileChooser save = new JFileChooser("Select your file or save a new one: ");
				FileFilter filter = new FileNameExtensionFilter("Pictures",
						new String[] { "jpg", "jpeg", "png", "gif" });
				save.setFileFilter(filter);
				int val = save.showSaveDialog(null);

				if (val == JFileChooser.APPROVE_OPTION) {
					String filePath = save.getSelectedFile().getAbsolutePath();
					// String fileName = save.getName(save.getSelectedFile());
					String[] parts = new String[2];
					if (filePath.contains(".")) {
						parts = filePath.split("\\.");
					} else {
						parts[0] = filePath;
						parts[1] = null;
					}
					try {
						if (!save.getSelectedFile().exists()) {
							fileHandler.saveImage(parts[0], image, parts[1]);
							isSaved = true;
						} else {
							int ans = JOptionPane.showConfirmDialog(null, "Would you like to Overwrite this file?");
							if (ans == JOptionPane.YES_OPTION) {
								fileHandler.saveImage(filePath, image, parts[1]);
								isSaved = true;
							} else {
								JOptionPane.showMessageDialog(null, "Image was not saved!");
							}
						}
					} catch (IOException e) {
						JOptionPane.showMessageDialog(null, "Image could not be saved");
					}
				}
			} else {
				JOptionPane.showMessageDialog(null, "Please load an image first");
			}

		}

	}

	/**
	 * Closes the open file.
	 * 
	 * @author Chet Lampron
	 *
	 */
	private class CloseFile implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (image != null) {
				image = null;
				centerPanel.repaint();
			} else {
				JOptionPane.showMessageDialog(null, "No image to close!");
			}
		}

	}

	/**
	 * Runs the parallel grayscale algorithm.
	 * 
	 * @author Chet Lampron
	 *
	 */
	private class GrayParallel implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent a) {
			if (deviceGroup.getSelection() == null) {
				JOptionPane.showMessageDialog(null, "Please select a device from the device menu.");
			} else {
				if (image != null) {
					GrayscaleModifierParallel pixelModifier = new GrayscaleModifierParallel(deviceManager);

					try {
						image = pixelModifier.modifyPixel(image);
					} catch (FileNotFoundException e) {
						JOptionPane.showMessageDialog(null, "The kernel could not be found");
					}
					centerPanel.repaint();
					isSaved = false;
				} else {
					JOptionPane.showMessageDialog(null, "Please load an image first");
				}
			}
		}

	}

	/**
	 * Runs the sepia in parallel.
	 * 
	 * @author Chet Lampron
	 *
	 */
	private class SepiaParallel implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent a) {
			if (deviceGroup.getSelection() == null) {
				JOptionPane.showMessageDialog(null, "Please select a device from the device menu.");
			} else {
				if (image != null) {
					SepiaModifierParallel pixelModifier = new SepiaModifierParallel(deviceManager);

					try {
						image = pixelModifier.modifyPixel(image);
					} catch (FileNotFoundException e) {
						JOptionPane.showMessageDialog(null, "The kernel could not be found");
					}
					centerPanel.repaint();
					isSaved = false;
				} else {
					JOptionPane.showMessageDialog(null, "Please load an image first");
				}
			}
		}

	}

	/**
	 * Runs the blur in parallel.
	 * 
	 * @author Chet Lampron
	 *
	 */
	private class BlurParallel implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent a) {
			if (deviceGroup.getSelection() == null) {
				JOptionPane.showMessageDialog(null, "Please select a device from the device menu.");
			} else {
				if (image != null) {
					BlurModifierParallel pixelModifier = new BlurModifierParallel(deviceManager);

					try {
						image = pixelModifier.modifyPixel(image);
					} catch (FileNotFoundException e) {
						JOptionPane.showMessageDialog(null, "The kernel could not be found");
					}
					centerPanel.repaint();
					isSaved = false;
				} else {
					JOptionPane.showMessageDialog(null, "Please load an image first");
				}
			}
		}

	}

	/**
	 * Runs the parallel horizontal flip algorithm.
	 * 
	 * @author Chet Lampron
	 *
	 */
	private class HorizontalFlipListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent a) {
			if (deviceGroup.getSelection() == null) {
				JOptionPane.showMessageDialog(null, "Please select a device from the device menu.");
			} else {
				if (image != null) {
					HorizontalFlip pixelModifier = new HorizontalFlip(deviceManager);

					try {
						image = pixelModifier.modifyPixel(image);
					} catch (FileNotFoundException e) {
						JOptionPane.showMessageDialog(null, "The kernel could not be found");
					}
					centerPanel.repaint();
					isSaved = false;
				} else {
					JOptionPane.showMessageDialog(null, "Please load an image first");
				}
			}
		}

	}

	/**
	 * Runs the parallel vertical flip algorithm.
	 * 
	 * @author Chet Lampron
	 *
	 */
	private class VerticalFlipListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent a) {
			if (deviceGroup.getSelection() == null) {
				JOptionPane.showMessageDialog(null, "Please select a device from the device menu.");
			} else {
				if (image != null) {
					VerticalFlip pixelModifier = new VerticalFlip(deviceManager);

					try {
						image = pixelModifier.modifyPixel(image);
					} catch (FileNotFoundException e) {
						JOptionPane.showMessageDialog(null, "The kernel could not be found");
					}
					centerPanel.repaint();
					isSaved = false;
				} else {
					JOptionPane.showMessageDialog(null, "Please load an image first");
				}
			}
		}

	}

	/**
	 * Runs the parallel rotate left algorithm.
	 * 
	 * @author Chet Lampron
	 *
	 */
	private class RotateLeftListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent a) {
			if (deviceGroup.getSelection() == null) {
				JOptionPane.showMessageDialog(null, "Please select a device from the device menu.");
			} else {
				if (image != null) {
					RotateLeft pixelModifier = new RotateLeft(deviceManager);

					try {
						image = pixelModifier.modifyPixel(image);
					} catch (FileNotFoundException e) {
						JOptionPane.showMessageDialog(null, "The kernel could not be found");
					}
					centerPanel.repaint();
					isSaved = false;
				} else {
					JOptionPane.showMessageDialog(null, "Please load an image first");
				}
			}
		}

	}

	/**
	 * Runs the parallel rotate right algorithm.
	 * 
	 * @author Chet Lampron
	 *
	 */
	private class RotateRightListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent a) {
			if (deviceGroup.getSelection() == null) {
				JOptionPane.showMessageDialog(null, "Please select a device from the device menu.");
			} else {
				if (image != null) {
					RotateRight pixelModifier = new RotateRight(deviceManager);

					try {
						image = pixelModifier.modifyPixel(image);
					} catch (FileNotFoundException e) {
						JOptionPane.showMessageDialog(null, "The kernel could not be found");
					}
					centerPanel.repaint();
					isSaved = false;
				} else {
					JOptionPane.showMessageDialog(null, "Please load an image first");
				}
			}
		}

	}

	/**
	 * Runs the Grayscale algorithm when prompted.
	 * 
	 * @author Chet lampron
	 *
	 */
	private class GrayscaleImage implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (image != null) {
				GrayscaleModifier pixelModifier = new GrayscaleModifier();

				image = pixelModifier.modifyPixel(image);
				centerPanel.repaint();
				isSaved = false;
			} else {
				JOptionPane.showMessageDialog(null, "Please load an image first");
			}

		}

	}

	/**
	 * Runs the Sepia algorithm when prompted.
	 * 
	 * @author Chet lampron
	 *
	 */
	private class SepiaImage implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (image != null) {
				SepiaModifier pixelModifier = new SepiaModifier();

				image = pixelModifier.modifyPixel(image);
				centerPanel.repaint();
				isSaved = false;
			} else {
				JOptionPane.showMessageDialog(null, "Please load an image first");
			}

		}

	}

	/**
	 * Runs the Blur algorithm when prompted.
	 * 
	 * @author Chet lampron
	 *
	 */
	private class BlurImage implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (image != null) {
				BlurModifier pixelModifier = new BlurModifier();

				image = pixelModifier.modifyPixel(image);
				centerPanel.repaint();
				isSaved = false;
			} else {
				JOptionPane.showMessageDialog(null, "Please load an image first");
			}

		}

	}

	/**
	 * Runs the Mosaic algorithm when prompted.
	 * 
	 * @author Chet lampron
	 *
	 */
	private class MosaicImage implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (image != null) {
				CancelOptionPanel cancelOption = new CancelOptionPanel("Tiles");
				int result = JOptionPane.showConfirmDialog(null, cancelOption, "Please enter a number of tiles: ",
						JOptionPane.OK_CANCEL_OPTION);
				if (result == JOptionPane.OK_OPTION) {
					try {
						int tiles = Integer.parseInt(cancelOption.getTiles().getText());
						MosaicModifier pixelModifier = new MosaicModifier(tiles);

						image = pixelModifier.modifyPixel(image);
					} catch (FileNotFoundException e) {
						JOptionPane.showMessageDialog(null, "The image could not be processed");
					}
					centerPanel.repaint();
					isSaved = false;
				}
			} else {
				JOptionPane.showMessageDialog(null, "Please load an image first");
			}

		}

	}

	/**
	 * Runs the zoom algorithm when prompted.
	 * 
	 * @author Chet lampron
	 *
	 */
	private class ZoomListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (image != null) {
				if (arg0.getSource().equals(zoomIn)) {
					ZoomIn pixelModifier = new ZoomIn(true);

					try {
						image = pixelModifier.modifyPixel(image);
					} catch (FileNotFoundException e) {
						JOptionPane.showMessageDialog(null, "Image not found");
					}
					centerPanel.repaint();
					isSaved = false;
				} else {
					ZoomIn pixelModifier = new ZoomIn(false);

					try {
						image = pixelModifier.modifyPixel(image);
					} catch (FileNotFoundException e) {
						JOptionPane.showMessageDialog(null, "Image not found");
					}
					centerPanel.repaint();
					isSaved = false;
				}
			} else {
				JOptionPane.showMessageDialog(null, "Please load an image first");
			}

		}

	}

	/**
	 * Runs the unoptimized equalize grayscale algorithm when prompted.
	 * 
	 * @author Chet lampron
	 *
	 */
	private class EqualizeGrayscaleImage implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (image != null) {
				try {
					GrayscaleEqualizationModifier pixelModifier = new GrayscaleEqualizationModifier(deviceManager,
							false);

					image = pixelModifier.modifyPixel(image);
				} catch (FileNotFoundException e) {
					JOptionPane.showMessageDialog(null, "The image could not be processed");
				}
				centerPanel.repaint();
				isSaved = false;
			} else {
				JOptionPane.showMessageDialog(null, "Please load an image first");
			}

		}

	}

	/**
	 * Runs the optimized equalize grayscale algorithm when prompted.
	 * 
	 * @author Chet lampron
	 *
	 */
	private class OptimizedEqualizeGrayscaleImage implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (image != null) {
				try {
					GrayscaleEqualizationModifier pixelModifier = new GrayscaleEqualizationModifier(deviceManager,
							true);

					image = pixelModifier.modifyPixel(image);
				} catch (FileNotFoundException e) {
					JOptionPane.showMessageDialog(null, "The image could not be processed");
				}
				centerPanel.repaint();
				isSaved = false;
			} else {
				JOptionPane.showMessageDialog(null, "Please load an image first");
			}

		}

	}

	/**
	 * Runs the Blur algorithm when prompted.
	 * 
	 * @author Chet lampron
	 *
	 */
	private class MosaicParallel implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (image != null) {

				CancelOptionPanel cancelOption = new CancelOptionPanel("Tiles");
				int result = JOptionPane.showConfirmDialog(null, cancelOption, "Please enter a number of tiles: ",
						JOptionPane.OK_CANCEL_OPTION);
				if (result == JOptionPane.OK_OPTION) {
					try {
						int tiles = Integer.parseInt(cancelOption.getTiles().getText());
						MosaicModifierParallel pixelModifier = new MosaicModifierParallel(tiles, deviceManager);
						image = pixelModifier.modifyPixel(image);
						centerPanel.repaint();
						isSaved = false;
					} catch (FileNotFoundException e) {
						JOptionPane.showMessageDialog(null, "The image could not be processed");
					}
				}
			} else {
				JOptionPane.showMessageDialog(null, "Please load an image first");
			}

		}

	}

	/**
	 * Runs the Grayscale algorithm when prompted.
	 * 
	 * @author Chet lampron
	 *
	 */
	private class RedEyeListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
//			if (image != null) {
//				JOptionPane.showMessageDialog(null, "Select your template");
//				JFileChooser choose = new JFileChooser("Select your template: ");
//				FileFilter filter = new FileNameExtensionFilter("Pictures",
//						new String[] { "jpg", "jpeg", "png", "gif" });
//				choose.setFileFilter(filter);
//				int val = choose.showOpenDialog(null);
//
//				if (val == JFileChooser.APPROVE_OPTION) {
//					BufferedImage template = null;
//					try {
//						String filePath = choose.getSelectedFile().getAbsolutePath();
//						template = fileHandler.createImage(filePath);
//					} catch (IOException e) {
//						JOptionPane.showMessageDialog(null, "Image could not be processed");
//					} catch (NullPointerException n) {
//						JOptionPane.showMessageDialog(null,
//								"Oops! Looks like you double clicked your folder instead of a file. Try again!");
//					}
//
//					RedEyeModifier pixelModifier = new RedEyeModifier(deviceManager, template);
//
//					try {
//						image = pixelModifier.modifyPixel(image);
//					} catch (FileNotFoundException e) {
//						JOptionPane.showMessageDialog(null,
//								"The Image could not be found: this is the actual image, not the template");
//					}
//					centerPanel.repaint();
//					isSaved = false;
//				}
//			} else {
//				JOptionPane.showMessageDialog(null, "Please load an image first");
//			}
			JOptionPane.showMessageDialog(null, "This feature does not work properly and has been removed until a solution has been made.");
		}

	}

	/**
	 * Runs the Grayscale algorithm when prompted.
	 * 
	 * @author Chet lampron
	 *
	 */
	private class SeamlessListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (image != null) {
				JOptionPane.showMessageDialog(null, "Select your clone image");
				JFileChooser choose = new JFileChooser("Select your image: ");
				FileFilter filter = new FileNameExtensionFilter("Pictures",
						new String[] { "jpg", "jpeg", "png", "gif" });
				choose.setFileFilter(filter);
				int val = choose.showOpenDialog(null);

				if (val == JFileChooser.APPROVE_OPTION) {
					BufferedImage clone = null;
					try {
						String filePath = choose.getSelectedFile().getAbsolutePath();
						clone = fileHandler.createImage(filePath);
					} catch (IOException e) {
						JOptionPane.showMessageDialog(null, "Clone could not be processed");
					} catch (NullPointerException n) {
						JOptionPane.showMessageDialog(null,
								"Oops! Looks like you double clicked your folder instead of a file. Try again!");
					}
					CancelOptionPanel cancelOption = new CancelOptionPanel("Iterations");
					int result = JOptionPane.showConfirmDialog(null, cancelOption,
							"Please enter a number of iterations: ", JOptionPane.OK_CANCEL_OPTION);
					if (result == JOptionPane.OK_OPTION) {
						int iterations = Integer.parseInt(cancelOption.getTiles().getText());
						// TODO
						SeamlessCloneModifier pixelModifier = new SeamlessCloneModifier(clone, deviceManager,
								iterations);

						try {
							image = pixelModifier.modifyPixel(image);
						} catch (FileNotFoundException e) {
							JOptionPane.showMessageDialog(null,
									"The Image could not be found: this is the actual image, not the clone");
						}
						// JOptionPane.showMessageDialog(null, "Coming soon!");
					}
					centerPanel.repaint();
					isSaved = false;
				}
			} else {
				JOptionPane.showMessageDialog(null, "Please load an image first");
			}

		}

	}

	/**
	 * In class use only.
	 * 
	 * @return The current Gui.
	 */
	private PiPGui getGui() {
		return currentGui;
	}

	/**
	 * Sets gui for in class use.
	 * 
	 * @param current
	 *            The current GUI.
	 */
	public void setCurrentGui(PiPGui current) {
		currentGui = current;
	}

	/**
	 * The MuralPanel class that will be the sides of the GUI.
	 * 
	 * @author Chet Lampron.
	 *
	 */
	private class MuralPanel extends JPanel {

		/**
		 * Default Serial Number.
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (sideMural != null) {
				g.drawImage(sideMural, -1170, 0, this);
			}
		}
	}

	/**
	 * The center panel that will load the image.
	 * 
	 * @author Chet Lampron
	 *
	 */
	private class CenterPainter extends JPanel {
		/**
		 * Default Serial Number.
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

	/**
	 * Special radio button.
	 * 
	 * @author chetlampron
	 *
	 */
	private class SpecialRadioButton extends JRadioButtonMenuItem {
		/**
		 * default id.
		 */
		private static final long serialVersionUID = 1L;
		/** The name. */
		private String name;

		/**
		 * Overwrite constructor.
		 * 
		 * @param aName
		 *            The name.
		 */
		public SpecialRadioButton(String aName) {
			super(aName);
			name = aName;
		}

		/**
		 * Gets the name.
		 * 
		 * @return The name.
		 */
		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return super.getName();
		}
	}

	/**
	 * Gets the device map.
	 * 
	 * @return deviceMap.
	 */
	public static Map<String, cl_device_id> getDeviceMap() {
		return deviceMap;
	}
}

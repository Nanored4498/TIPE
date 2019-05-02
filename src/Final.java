import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * Le programme final pour la reconnaissance du code postal sur une enveloppe
 * @author Yoann
 */
public class Final extends JFrame {

	private static final long serialVersionUID = 5970352975867769051L;
	private JButton loadIm = new JButton("Ouvrir Image"), loadNet = new JButton("Choisir Réseau"),
			getIms = new JButton("Obtenir Images"), calc = new JButton("Résultats"); //Les boutons du haut de la fenêtre.
	private JPanel topPanel = new JPanel(), letterPanel, imsPanel; //Les panneaux pour les boutons du haut, et l'affichage de l'enveloppe et des chiffres.
	private BufferedImage letter; //L'image de l'enveloppe.
	private Reseau net; //Le perceptron en cours d'utilisation.
	private DBN dbn; //Le DBN en cours d'utilisation.
	private boolean isNet = true, imsCreated = false, netloaded = false; //Les booléens pour savoir si on utilise un perceptron ou un DBN, pour savoir si les imagses ont été crées et pour savoir si le réseau est chargé.
	private String title = "Reconnaissance de code postal", im = "", res = ""; //Titre de la fenêtre et noms des fichiers.
	private BufferedImage[] ims; //Tableau des  petites images de chiffre.
	private JLabel resLabel = new JLabel(" "), perLabel = new JLabel(" "); //Les labels de résultats et de pourcentage.
	private Thread th; //Le thread pour les tâches de fond.

	/**
	 * Crée une interface pour le programme final.
	 */
	public Final() {
		setSize(750, 750);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle(title);
		loadIm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setCurrentDirectory(new File("enveloppes"));
				fc.showOpenDialog(null);
				File file = fc.getSelectedFile();
				if(file == null)
					return;
				try {
					letter = ImageIO.read(file);
					getIms.setEnabled(true);
					calc.setEnabled(false);
					imsCreated = false;
					ims = null;
					letterPanel.repaint();
					imsPanel.repaint();
					im = file.getName();
					setTitle(title + " : " + im + "  -  " + res);
				} catch (IOException  e1) {
					e1.printStackTrace();
				}
			}
		});
		loadNet.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setCurrentDirectory(new File("Networks"));
				fc.showOpenDialog(null);
				File file = fc.getSelectedFile();
				if(file == null)
					return;
				try {
					ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
					res = file.getName();
					if(res.endsWith("net")) {
						isNet = true;
						net = new Reseau((Network) ois.readObject());
						calc.setEnabled(imsCreated);
						netloaded = true;
					} else if(res.endsWith("dbn")) {
						isNet = false;
						dbn = (DBN) ois.readObject();
						calc.setEnabled(imsCreated);
						netloaded = true;
					}
					setTitle(title + " : " + im + "  -  " + res);
					ois.close();
				} catch (IOException | ClassNotFoundException e1) {
					e1.printStackTrace();
				}
			}
		});
		getIms.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getIms.setEnabled(false);
				loadIm.setEnabled(false);
				th = new Thread(new Runnable() {
					public void run() {
						try {
							ims = ImageUtil.getDigits(letter);
							imsCreated = true;
							if(netloaded)
								calc.setEnabled(true);
							resLabel.setText("");
						} catch (Exception e2) {
							e2.printStackTrace();
							resLabel.setText("Erreur lors de l'obtention des chiffres, veuillez regarder le dossier Traitement");
							getIms.setEnabled(true);
							topPanel.repaint();
						}
						imsPanel.repaint();
						loadIm.setEnabled(true);
					}
				});
				th.start();
			}
		});
		calc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				resLabel.setText("                                    ");
				perLabel.setText("                           (     ");
				if(isNet) {
					for(int i = 0; i < 5; i++) {
						float[] out = net.train3(ImageUtil.allinfo(ims[i]));
						resLabel.setText(resLabel.getText() + (int) out[0] + "   ");
						perLabel.setText(perLabel.getText() + out[1] + "    ");
					}
				} else {
					for(int i = 0; i < 5; i++) {
						float[] out = dbn.test(ImageUtil.pixels(ims[i+5]));
						float b = 0, s = 0;
						for(int j = 0; j < 10; j++) {
							if(out[j] > s) {
								b = j;
								s = out[j];
							}
						}
						resLabel.setText(resLabel.getText() + (int) b + "   ");
						perLabel.setText(perLabel.getText() + s + "    ");
					}
				}
				resLabel.setText(resLabel.getText() + "                                 ");
				perLabel.setText(perLabel.getText() + "  )                           ");
			}
		});
		getIms.setEnabled(false);
		calc.setEnabled(false);
		topPanel.add(loadIm);
		topPanel.add(loadNet);
		topPanel.add(getIms);
		topPanel.add(calc);
		topPanel.add(resLabel);
		topPanel.add(perLabel);
		topPanel.setPreferredSize(new Dimension(750, 75));
		letterPanel = new JPanel() {
			private static final long serialVersionUID = -3757950555935332081L;
			public void paintComponent(Graphics g) {
				g.setColor(Color.white);
				g.fillRect(0, 0, getWidth(), getHeight());
				if(letter != null) {
					float w = letter.getWidth(), h = letter.getHeight(), w2 = getWidth(), h2 = getHeight();
					float r = Math.min(w2/w, h2/h);
					g.drawImage(letter, (int) ((w2-w*r)/2), (int) ((h2-h*r)/2), (int) (w*r), (int) (h*r), null);
				}
			}
		};
		imsPanel = new JPanel() {
			private static final long serialVersionUID = 1L;
			public void paintComponent(Graphics g) {
				g.setColor(Color.white);
				g.fillRect(0, 0, getWidth(), getHeight());
				if(ims != null) {
					float w = getWidth(), h = getHeight();
					float r = Math.min((w-18)/24/5, h/32);
					for(int i = 0; i < 5; i++)
						g.drawImage(ims[i], (int) ((w-5*24*r)/6*(i+1)+i*24*r), (int) ((h-32*r)/2), (int) (24*r), (int) (32*r), null);
				}
			}
		};
		imsPanel.setPreferredSize(new Dimension(750, 48));
		getContentPane().add(topPanel, BorderLayout.NORTH);
		getContentPane().add(letterPanel, BorderLayout.CENTER);
		getContentPane().add(imsPanel, BorderLayout.SOUTH);
		setVisible(true);
	}

	/**
	 * Le Main !
	 * @param args Argument inutile ici.
	 */
	public static void main(String[] args) {
		new Final();
	}

}
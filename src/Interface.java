import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * L'interface pour l'apprentissage des réseaux et la création d'image pour la base de données.
 * @author Yoann
 */
public class Interface extends JFrame {

	private static final long serialVersionUID = 3120796899747904131L;
	private JMenuBar jmb = new JMenuBar(); //La barre de menu.
	private JMenu files = new JMenu("Fichier"); //Le menu Fichier
	private JMenuItem erase = new JMenuItem("Effacer"), saveN = new JMenuItem("Sauver réseau"), loadN = new JMenuItem("Charger réseau"),
			quit = new JMenuItem("Quitter"); //Les boutons du menu Ficher.
	private JButton save = new JButton("save img"),
			era = new JButton("effacer"), autob = new JButton("auto"), detail = new JButton("détails"),
			pract = new JButton("temps réel"), eff = new JButton("test efficacité"); //Les boutons du centre de la fenêtre.
	private Board board = new Board(); //Le panneau de dessein.
	private JPanel imagePanel, centerPanel = new JPanel(); //Les panneaux pour les boutons et pour l'affichage de l'image finale.
	private BufferedImage image = board.getImage(); //L'image finale.
	private JLabel resLabel = new JLabel(" "); //La zone de texte du bas.
	private JProgressBar loading = new JProgressBar(); //La barre de progression.
	private Reseau net; // Le réseau.
	private boolean auto; //Pour savoir si on est en mode automatique.
	private Thread th; //Le Thread pour l'apprentissage automatique et le temps réel.
	private String fileName; //Le nom du fichier en cours de traitement.
	private boolean loop = false; //Pour savoir si on présente les données en boucle ou aléatoirement.

	/**
	 * Crée une nouvelle interface à afficher.
	 */
	public Interface() {
		net = new Reseau();
		auto = false;
		setSize(750, 420);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Reconnaissance de chiffres");
		erase.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				board.erase();
			}
		});
		erase.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
		saveN.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(fileName == null)
					fileName = JOptionPane.showInputDialog(null, "Sous quel nom voulez-vous enregistrer le fichier ?");
				try {
					net.save("Networks/"+fileName+".net");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		saveN.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
		loadN.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setCurrentDirectory(new File("Networks"));
				fc.showOpenDialog(null);
				File file = fc.getSelectedFile();
				if(file == null)
					return;
				try {
					fileName = file.getName().replace(".net", "");
					ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
					net = new Reseau((Network) ois.readObject());
					ois.close();
				} catch (IOException | ClassNotFoundException e1) {
					e1.printStackTrace();
				}
			}
		});
		loadN.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
		quit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		files.add(erase); 
		files.addSeparator();
		files.add(saveN);
		files.add(loadN);
		files.add(quit);
		jmb.add(files);
		setJMenuBar(jmb);
		imagePanel = new JPanel() {
			private static final long serialVersionUID = -3757950555935332081L;
			public void paintComponent(Graphics g) {
				g.setColor(Color.white);
				g.fillRect(0, 0, getWidth(), getHeight());
				g.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), null);
			}
		};
		board.setPanelImage(imagePanel);
		board.setPreferredSize(new Dimension(310, 400));
		getContentPane().add(board, BorderLayout.WEST);
		imagePanel.setPreferredSize(new Dimension(310, 400));
		getContentPane().add(imagePanel, BorderLayout.EAST);
		era.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				erase.doClick();
			}
		});
		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int n = Integer.parseInt(JOptionPane.showInputDialog(null, "Quelle est le chiffre que vous avez dessiné ?"));
				save(n);
			}
		});
		detail.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				float[] in = ImageUtil.allinfo(image);
				float[] data = net.train2(in);
				String s = "";
				for(int i = 0; i < 10; i++)
					s += i + ": " + data[i] + "    ";
				JOptionPane.showMessageDialog(null, s, "Détails", JOptionPane.CANCEL_OPTION);
			}
		});
		autob.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				auto = !auto;
				if(auto) {
					int n = Integer.parseInt(JOptionPane.showInputDialog("Combien d'ittérations voulez-vous faire ?"));
					board.erase();
					autob.setText("Stop");
					enableB(false);
					th = new Thread(new Auto(n));
					th.start();
				}
			}
		});
		loading.setMinimum(0);
		loading.setPreferredSize(new Dimension(110, 15));
		pract.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				auto = !auto;
				if(auto) {
					pract.setText("Stop");
					autob.setEnabled(false);
					eff.setEnabled(false);
					th = new Thread(new RealTime());
					th.start();
				}
			}
		});
		eff.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("Images2/nums.txt"))), 32768);
					float per = 0, err = 0, tot = 0;
					for(int n = 0; n < 10; n++) {
						int num = Integer.parseInt(reader.readLine());
						tot += num + 1;
						for(int i = 0; i <= num; i++) {
							float[] data = net.train(ImageUtil.getVars(n, i), n);
							if(data[0] == n)
								per += 1;
							else
								System.out.println(n+""+i);
							err += data[3];
						}
					}
					reader.close();
					resLabel.setText("éfficacité: " + (per*100/tot) + "%         erreur quadratique: " + err);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				image = board.getImage();
			}
		});
		centerPanel.add(era);
		centerPanel.add(save);
		centerPanel.add(detail);
		centerPanel.add(autob);
		centerPanel.add(loading);
		centerPanel.add(pract);
		centerPanel.add(eff);
		getContentPane().add(centerPanel, BorderLayout.CENTER);
		getContentPane().add(resLabel, BorderLayout.SOUTH);
		setVisible(true);
	}

	/**
	 * Enregistre l'image actuellement dessinée.
	 * @param n la valuer du chiffre sur l'image actuellement dessinée.
	 */
	private void save(int n) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("Images2/nums.txt"))), 32768);
			int[] nums = new int[10];
			for(int i = 0; i < 10; i++)
				nums[i] = Integer.parseInt(reader.readLine());
			nums[n] += 1;
			reader.close();
			ImageIO.write(board.getImage(), "png", new File("Images2/"+n+""+nums[n]+".png"));
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("Images2/nums.txt")));
			for(int i : nums)
				writer.print(i + "\n");
			writer.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Permet d'activer ou de désactiver certains boutons.
	 * @param b true pour activer les boutons, false sinon.
	 */
	private void enableB(boolean b) {
		save.setEnabled(b);
		erase.setEnabled(b);
		era.setEnabled(b);
		detail.setEnabled(b);
		eff.setEnabled(b);
	}

	/**
	 * @author Yoann
	 * Classe pour l'apprentissage automatique en arrière plan.
	 */
	class Auto implements Runnable {
		int itt;
		public Auto(int itt) {
			this.itt = itt;
			loading.setMaximum(itt);
			pract.setEnabled(false);
		}
		public void run() {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("Images2/nums.txt"))), 32768);
				int[] nums = new int[10];
				for(int i = 0; i < 10; i++)
					nums[i] = Integer.parseInt(reader.readLine());
				reader.close();
				int it = 0;
				while(auto && it <= itt) {
					it ++;
					loading.setValue(it);
					int n = loop ? (it % 10) : ((int) (Math.random()*10));
					net.train4(ImageUtil.getVars(n, (int) (Math.random()*(nums[n]+1))), n);
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			auto = false;
			autob.setText("auto");
			image = board.getImage();
			enableB(true);
			autob.setEnabled(true);
			pract.setEnabled(true);
			loading.setValue(0);
		}
	}

	/**
	 * @author Yoann
	 *	Classe permettant d'afficher en temps réel les résultats du réseau de neurones.
	 */
	class RealTime implements Runnable {
		public void run() {
			while(auto) {
				float[] in = ImageUtil.allinfo(image);
				float[] data = net.train3(in);
				String s = "";
				for(int i = 0; i < 3; i++)
					s += (int)data[2*i] + " (" + data[2*i+1] + "%)           ";
				resLabel.setText(s);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			auto = false;
			pract.setText("temps réel");
			autob.setEnabled(true);
			eff.setEnabled(true);
		}
	}
	
	/**
	 * Le Main !
	 * @param arg Argument inutile ici.
	 */
	public static void main(String[] arg) {
		new Interface();
	}

}
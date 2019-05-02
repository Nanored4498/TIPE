
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JPanel;

/**
 * Zone de dessein intégré dans le logiciel.
 * @author Yoann
 */
public class Board extends JPanel{

	private static final long serialVersionUID = 2195379760914381351L;
	private int x, y; //Derni�re position de la souris.
	private BasicStroke stroke = new BasicStroke(8), stroke2 = new BasicStroke(4); //Les tailles de traits.
	private BufferedImage image = new BufferedImage(48, 64, BufferedImage.TYPE_INT_RGB); //L'image finale
	private Graphics2D g2Image = image.createGraphics(); //le graphique sur lequel on dessine les traits pour l'image finale.
	private JPanel panelImage; // Le paneau dans lequel on affiche l'image finale.
	private ArrayList<Line> lines = new ArrayList<Line>(); //La listes des lignes � dessiner.
	private int minX = 8000, maxX = -8000, minY = 8000, maxY = -8000; //Les coordon�es du rectangle dans lequel se trouve le dessein.
	
	/**
	 * Crée une nouvelle zone de dessein.
	 */
	public Board() {
		this.addMouseMotionListener(new MouseMotionListener() {
			public void mouseDragged(MouseEvent e) {
				int x1 = x, y1 = y;
				x = e.getX(); y = e.getY();
				Graphics2D g2 = (Graphics2D) getGraphics();
				g2.setStroke(stroke);
				g2.draw(new Line2D.Float(x1, y1, x, y));
				lines.add(new Line(x1, y1, x, y));
				minX = Math.min(minX, Math.min(x1, x));
				maxX = Math.max(maxX, Math.max(x1, x));
				minY = Math.min(minY, Math.min(y1, y));
				maxY = Math.max(maxY, Math.max(y1, y));
				int w = maxX - minX + 1, h = maxY - minY + 1;
				if(w > 3*h/4) h = 4 *w/3;
				else w = 3*h/4;
				x1 = minX + (maxX- minX - w)/2;
				y1 = minY + (maxY- minY - h)/2;
				w = w*54/48;
				h = h*70/64;
				g2Image.setColor(Color.white);
				g2Image.fillRect(0, 0, 48, 64);
				g2Image.setColor(Color.BLACK);
				g2Image.setStroke(stroke2);
				for(Line line : lines)
					g2Image.draw(new Line2D.Float((line.x-x1)*48/w+3, (line.y-y1)*64/h+3, (line.x2-x1)*48/w+3, (line.y2-y1)*64/h+3));
				panelImage.repaint();
			}
			public void mouseMoved(MouseEvent e) {
				x = e.getX(); y = e.getY();
			}
		});
	}
	
	public void paintComponent(Graphics g) {
		g.setColor(Color.white);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(Color.BLACK);
		g2Image.setColor(Color.white);
		g2Image.fillRect(0, 0, 48, 64);
		g2Image.setColor(Color.BLACK);
		if(panelImage != null) panelImage.repaint();
	}

	public BufferedImage getImage() { return image; }
	public void setPanelImage(JPanel pan) { panelImage = pan; }

	/**
	 * Efface la zone de dessein.
	 */
	public void erase() {
		repaint();
		minX = 8000;
		maxX = -8000;
		minY = 8000;
		maxY = -8000;
		lines.clear();
	}

	class Line { //Pour les lignes que l'on dessine.
		int x, y, x2, y2;
		public Line(int x, int y, int x2, int y2) {
			this.x = x;
			this.y = y;
			this.x2 = x2;
			this.y2 = y2;
		}
	}

}

import java.io.*;

import javax.imageio.ImageIO;

/**
 * Restricted Boltzmann Machine.
 * @author Yoann
 */
public class RBM implements Serializable {

	private static final long serialVersionUID = 6588404150235761139L;
	private int n, m; //Tailles du vecteur d'entrée et du vecteur de sortie.
	private float[] b, c, addB, addC; //Les biais des vecteurs de sortie (b) et d'entrée (c). Ainsi que leurs ajouts en mémoire.
	private float[] hp; //La sorite gardée en mémoire pour l'échantillonage de Gibbs.
	private float[][] w, addW; //Matrice des poids à m lignes et n colonnes ainsi que des derniers ajouts de poids.
	public float learning, wd, inertie; //Taux d'apprentissage, de weight decay et d'inertie.
	public byte kcd; //Nombre d'ittérations dans l'échantillonage de Gibbs.
	public byte batchSize, batchIt; //Nombre d'exemples à présenter dans un batch, et l'ittération du batch actuel.

	/**
	 * Crée une machine de Boltzmann restreinte avec n entrée et m sorties. Les poids sont initialisés de manière optimal d'après
	 * le rapport de stage de Quentin Fresnel et les vidéos de Hugo Larochelle.
	 * @param n Le nombre d'entrées.
	 * @param m Le nombre de sorties.
	 */
	public RBM(int n, int m) {
		this.n = n;
		this.m = m;
		c = new float[n];
		b = new float[m];
		addC = new float[n];
		addB = new float[m];
		hp = new float[m];
		float r = (float) (4 / Math.sqrt(n*m));
		w = new float[m][n];
		addW = new float[m][n];
		for(int j = 0; j < m; j++)
			for(int k = 0; k < n; k++)
				w[j][k] = (float) ((Math.random()-1) * r);
		learning = 0.0006f;
		wd = 0.00004f;
		inertie = 0.6f;
		kcd = 4;
		batchSize = 18;
		batchIt = 0;
	}

	/**
	 * Calcule la sortie de la machine de Boltzmann restreinte.
	 * @param x L'entrée de taille n dont on calcule la sortie.
	 * @return La sortie associée à l'entrée <b>x</b>.
	 */
	public float[] calc(float[] x) {
		float[] h = new float[m];
		for(int j = 0; j < m; j++) {
			float temp = b[j];
			for(int k = 0; k < n; k++)
				temp += w[j][k]*x[k];
			h[j] = sig(temp);
		}
		return h;
	}

	/**
	 * Calcule l'entrée de la machine de Boltzmann restreinte en fonction d'une sortie.
	 * @param h La sortie de taille m dont on calcule l'entrée.
	 * @return L'entrée associée à la sortie <b>h</b>.
	 */
	public float[] inv(float[] h) {
		float[] x = new float[n];
		for(int k = 0; k < n; k++) {
			float temp = c[k];
			for(int j = 0; j < m; j++)
				temp += w[j][k]*h[j];
			x[k] = sig(temp);
		}
		return x;
	}

	/**
	 * Pour une certaine entrée <b>x</b>, calcule sa sortie, puis à partir de cette dernière calcule l'entrée associée.
	 * @param x L'entrée à présenter.
	 * @return Le résultat après avoir calculer la sortie puis l'entrée en partant de <b>x</b>.
	 */
	public float[] calcAndInv(float[] x) {
		return inv(calc(x));
	}

	/**
	 * Réalise une ittération d'apprentissage avec un exemple <b>x</b>.
	 * @param x Vecteur d'apprentissage de taille n.
	 */
	public void learn(float[] x) {
		batchIt ++;
		float[] h = calc(x), xp = new float[n];
		if(batchIt == 1)
			for(int j = 0; j < m; j++)
				hp[j] = h[j];
		for(byte i = 0; i < kcd; i++) {
			xp = inv(hp);
			hp = calc(xp);
		}
		for(int j = 0; j < m; j++) {
			addB[j] += learning*(h[j] - hp[j]);
			for(int k = 0; k < n; k++)
				addW[j][k] += learning*(h[j]*x[k] - hp[j]*xp[k]);
		}
		for(int k = 0; k < n; k++)
			addC[k] += learning*(x[k] - xp[k]);
		if(batchIt == batchSize) {
			for(int j = 0; j < m; j++) {
				b[j] += addB[j];
				addB[j] = inertie*addB[j] - wd*b[j];
				for(int k = 0; k < n; k++) {
					w[j][k] += addW[j][k];
					addW[j][k] = inertie*addW[j][k] - wd*w[j][k];
				}
			}
			for(int k = 0; k < n; k++) {
				c[k] += addC[k];
				addC[k] = inertie*addC[k] - wd*c[k];
			}
			batchIt = 0;
		}
	}

	/**
	 * Génère une nouvelle entrée.
	 * @param len Le nombre d'ittérations d'échantillonage de Gibbs à réaliser.
	 * @param w La largeur de la représentation de l'entrée.
	 * @param h La hauteur de la représentation de l'entrée.
	 * @param firstX L'entrée présentée initialement dans l'échantillonage de Gibbs.
	 * @throws IOException 
	 */
	public void generate(int len, int w, int h, float[] firstX) throws IOException {
		ImageUtil.save(ImageUtil.ImageFromPixels(firstX, w, h), "test.png");
		for(int i = 0; i < len; i++) {
			firstX = calcAndInv(firstX);
			ImageUtil.save(ImageUtil.ImageFromPixels(firstX, w, h), "test"+i+".png");
		}
	}

	/**
	 * Permet de tester si la machine de Boltzmann restreinte est bien entrainer en comparant des image de chiffres
	 * aux images obtenues après un échantillonage de Gibbs de longeur <b>len</b> en partant des image initiales.
	 * @param len La longueur de l'échantillonage de Gibbs à effectuer.
	 * @param xs Un tableau à deux dimensions où <b>xs</b>[<b>i</b>][<b>j</b>] est le <b>j</b>-ème pixel de la <b>i</b>-ème image.
	 * @param w La largeur des images.
	 * @param h La hauteur des images.
	 */
	public void test(int len, float[][] xs, int w, int h) {
		for(int a = 0; a < xs.length; a++) {
			ImageUtil.save(ImageUtil.ImageFromPixels(xs[a], w, h), a+"init.png");
			for(int i = 0; i < len; i++)
				xs[a] = calcAndInv(xs[a]);
			ImageUtil.save(ImageUtil.ImageFromPixels(xs[a], w, h), a+"rep.png");
		}
	}

	private float sig(float x) { return (float) (1 / (1 + Math.exp(-x))); } //fonction sigmoïde

	/**
	 * Sauvegarde la machine de Boltzmann restreinte dans un fichier.
	 * @param fileName Le nom du fichier.
	 * @throws IOException En cas de problème avec le fichier.
	 */
	public void save(String fileName) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName));
		oos.writeObject(this);
		oos.close();
	}

	/**
	 * Donne les représentations des impacts des composantes de l'entrée sur la sortie.
	 * @param x Un tableau de taille n, contenant des tableaux d'impacts d'un vecteur encore antérieur sur chaque composante de l'entrée.
	 * @param size La taille du vecteur dont on cherche l'impact sur cette RBM.
	 * @return Un tableau de taille m, contenant pour chaque composante de la sortie de cette RBM, un tableau représentant les
	 * impacts des composantes du vecteur antérieur.
	 */
	public float[][] getRepresentations(float[][] x, int size) {
		float[][] res = new float[m][size];
		for(int k = 0; k < n; k++)
			for(int j = 0; j < m; j++)
				for(int a = 0; a < size; a++)
					res[j][a] += x[k][a] * this.w[j][k];
		return res;
	}

	public float[][] getWeights() { return w; }
	public int getInLen() { return n; }
	public int getOutLen() { return m; }
	public void setKCD(byte k) { kcd = k; }

	/**
	 * Dernier Main utilisé.
	 * Le code de ce main a souvent changé, il m'a servi à faire des tests et des apprentissages.
	 * @param args Argument inutile ici.
	 */
	public static void main(String[] args) {
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream("Networks/DBN2.dbn"));
			DBN dbn = (DBN) ois.readObject();
			ois.close();
			int j = (int) (Math.random()*100);
			System.out.println(j);
			dbn.testError(80, new float[][]{ImageUtil.pixels(ImageIO.read(new File("Images3/0"+j+".png"))),
					ImageUtil.pixels(ImageIO.read(new File("Images3/1"+j+".png"))),
					ImageUtil.pixels(ImageIO.read(new File("Images3/2"+j+".png"))),
					ImageUtil.pixels(ImageIO.read(new File("Images3/3"+j+".png"))),
					ImageUtil.pixels(ImageIO.read(new File("Images3/4"+j+".png"))),
					ImageUtil.pixels(ImageIO.read(new File("Images3/5"+j+".png"))),
					ImageUtil.pixels(ImageIO.read(new File("Images3/6"+j+".png"))),
					ImageUtil.pixels(ImageIO.read(new File("Images3/7"+j+".png"))),
					ImageUtil.pixels(ImageIO.read(new File("Images3/8"+j+".png"))),
					ImageUtil.pixels(ImageIO.read(new File("Images3/9"+j+".png"))),
			}, 24, 32, 0);
			dbn.getRBM(0).generate(100, 24, 32, ImageUtil.pixels(ImageIO.read(new File("Images3/510.png"))));
			float[][] pix = dbn.getRepresentations(0);
			for(int i = 0; i < pix.length; i++)
				ImageUtil.save(ImageUtil.ImageFromPixelsB(pix[i], 24, 32), "Rep/rep"+i+".png");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

}

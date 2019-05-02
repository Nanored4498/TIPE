import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

import javax.imageio.ImageIO;

/**
 * Classe qui contient tout ce qui est utile au traitement de l'image.
 * @author Yoann
 */
public class ImageUtil {

	/**
	 * Permet de récupérer les informations sur les proportions de pixels dans les 20 zones.
	 * @param image L'image à traiter.
	 * @return Le tableau contenant les 20 pourentages (un par zone).
	 */
	public static float[] stats(BufferedImage image) {
		float[] res = new float[20];
		float w = image.getWidth(), h = image.getHeight();
		float mx = w-1;
		float s = w*h;
		for(float x = 0; x < w; x++)
			for(float y = 0; y < h; y++) {
				int c = image.getRGB((int) x, (int) y);
				if(c < -10000000) {
					if(y >= 2*x*h/w) res[0] += 4/s;
					else if(y <= 2*(mx-x)*h/w) res[1] += 2/s;
					else res[2] += 4/s;
					if(y <= (mx-2*x)*h/w) res[3] += 4/s;
					else if(y >= (2*x-mx)*h/w) res[4] += 2/s;
					else res[5] += 4/s;
					if(y <= (mx-x)*h/2/w) res[6] += 4/s;
					else if(y <= (mx+x)*h/2/w) res[7] += 2/s;
					else res[8] += 4/s;
					if(y <= x*h/2/w) res[9] += 4/s;
					else if(y <= (2*mx-x)*h/2/w) res[10] += 2/s;
					else res[11] += 4/s;
					if(y < h/2) {
						if(x < w/2) res[12] += 4/s;
						else res[13] += 4/s;
					} else {
						if(x < w/2) res[14] += 4/s;
						else res[15] += 4/s;
					}
					if(y <= x*h/w) {
						if(y <= (mx-x)*h/w) res[16] += 4/s;
						else res[18] += 4/s;
					} else {
						if(y <= (mx-x)*h/w) res[17] += 4/s;
						else res[19] += 4/s;
					}
				}
			}
		return res;
	}

	/**
	 * Permet de récupérer le profile gauche (PFG) de l'image.
	 * @param image L'image à traiter.
	 * @return Le profle gauche sous forme d'un tableau de taille 32.
	 */
	public static float[] pfg(BufferedImage image) {
		float[] res = new float[32];
		for(float y = 0; y < 32; y++) {
			float x = 0;
			while(x < 48 && image.getRGB((int) x, 2*((int) y)) > -10000000)
				x ++;
			res[(int) y] = (x-24)/24;
		}
		return res;
	}

	/**
	 * Permet de récupérer le profile droit (PFD) de l'image.
	 * @param image L'image à traiter.
	 * @return Le profle droit sous forme d'un tableau de taille 32.
	 */
	public static float[] pfd(BufferedImage image) {
		float[] res = new float[32];
		for(float y = 0; y < 32; y++) {
			float x = 47;
			while(x >= 0 && image.getRGB((int) x, 2*((int) y)) > -10000000)
				x --;
			res[(int) y] = (x-24)/24;
		}
		return res;
	}

	/**
	 * Permet de récupérer le profile orienté (PFO) de l'image.
	 * @param image L'image à traiter.
	 * @return Le profle orienté sous forme d'un tableau de taille 16.
	 */
	public static float[] pfo(BufferedImage image) {
		float[] res = new float[16];
		for(int i = 0; i < 16; i ++) {
			float x = -33 + 3*i, y = 17 - 3*i;
			while(x < 0 || y < 0) {
				x ++;
				y ++;
			}
			float d = 0, len = Math.min(48-x, 64-y);
			while(x <= 47 && y <= 63 && image.getRGB((int) x, (int) y) > -10000000) {
				x ++;
				y ++;
				d ++;
			}
			res[i] = (d-len/2)*2/len;
		}
		return res;
	}

	/**
	 * Permet de récupérer toutes les infos nécessaire au réseau.
	 * @param im L'image à traiter.
	 * @return Le tableu des 100 infos nécessaires.
	 */
	public static float[] allinfo(BufferedImage im) {
		float[] res = new float[100];
		float[] pfo = pfo(im), pfg = pfg(im), pfd = pfd(im), stats = stats(im);
		for(int i = 0; i < 16; i++)
			res[i] = pfo[i];
		for(int i = 0; i < 32; i++)
			res[i+16] = pfg[i];
		for(int i = 0; i < 32; i++)
			res[i+48] = pfd[i];
		for(int i = 0; i < 20; i++)
			res[i+80] = stats[i];
		return res;
	}

	/**
	 * Retourne un tableau de pixels correspondant à une image.
	 * @param im L'image dont on veut récupérer le tableau.
	 * @return Un tableau contenant dans chaque case 1 si le pixel est noir, 0 sinon. Les pixles sont disposés lignes par lignes dans
	 * le tableau.
	 */
	public static float[] pixels(BufferedImage im) {
		int w = im.getWidth(), h = im.getHeight();
		float[] res = new float[w*h];
		for(int x = 0; x < w; x++)
			for(int y = 0; y < h; y++)
				res[x+y*w] = (im.getRGB(x, y) < -10000000) ?  1 : 0;
		return res;
	}

	/**
	 * Crée une image aléatoire de pixels noir ou blanc.
	 * @param len La nombre de pixels à générer (w*h).
	 * @return La liste de pixels aléatoires.
	 */
	public static float[] randomImage(int len) {
		float[] x = new float[len];
		for(int a = 0; a < len; a++)
			x[a] = (float) Math.random();
		return x;
	}

	/**
	 * Retourne une image en noir et blanc à partir d'un tableau de pixels.
	 * @param pix Le tableau des pixels.
	 * @param w La largeur de la nouvelle image.
	 * @param h La hauteur de la nouvelle image.
	 * @return L'image voulue.
	 */
	public static BufferedImage ImageFromPixels(float[] pix, int w, int h) {
		int[] pix2 = new int[w*h];
		for(int i = 0; i < w*h; i++) {
			int c = (int) ((1-pix[i])*255);
			pix2[i] = c + (c << 8) + (c << 16);
		}
		BufferedImage res = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		res.setRGB(0, 0, w, h, pix2, 0, w);
		return res;
	}

	/**
	 * Retourne une image en noir et blanc adapté aux représsentations des DBN et RBM à partir d'un tableau de pixels.
	 * @param pix Le tableau des pixels.
	 * @param w La largeur de la nouvelle image.
	 * @param h La hauteur de la nouvelle image.
	 * @return L'image voulue.
	 */
	public static BufferedImage ImageFromPixelsB(float[] pix, int w, int h) {
		int[] pix2 = new int[w*h];
		for(int i = 0; i < w*h; i++) {
			int c = (int) (Math.max(Math.min(0.5+Math.pow(pix[i], 3)*0.9, 0.5),-0.5)*255);
			pix2[i] = c + (c << 8) + (c << 16);
		}
		BufferedImage res = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		res.setRGB(0, 0, w, h, pix2, 0, w);
		return res;
	}

	/**
	 * Pour obtenir la matrice de Gauss de taille (2<b>n</b>+1) et de paramètre <b>s</b>.
	 * @param n Le paramètre de taille.
	 * @param s Le paramètre de la gaussienne.
	 * @return La matrice voulue.
	 */
	public static float[][] matGauss(int n, float s) {
		float[][] res = new float[2*n+1][2*n+1];
		float tot = res[n][n] = gauss(0, 0, s);
		for(byte i = 1; i <= n; i++) {
			for(byte j = 0; j <= n; j++) {
				res[n-j][n+i] = res[n-i][n-j] = res[n+j][n-i] = res[n+i][n+j] = gauss(i, j, s);
				tot += 4*res[i+n][j+n];
			}
		}
		for(int i = -n; i <= n; i++)
			for(int j = -n; j <= n; j++)
				res[i+n][j+n] /= tot;
		return res;
	}

	private static float gauss(float x,float y, float s) { return (float) Math.exp((-x*x-y*y)/2/(s*s)); } //La gaussienne

	/**
	 * Effectue un lissage gaussien sur une image.
	 * @param image L'image à traiter.
	 * @param size La taille du filtre gaussien.
	 * @param s Le paramètre de la gaussienne.
	 * @param name Le nom du fichier dans lequel est enregistré l'image finale.
	 */
	public static void lissageG(BufferedImage image, int size, float s, String name) {
		int w = image.getWidth();
		int h = image.getHeight();
		int[][][] rgb = getRGB(image, w, h);
		int[] pix = new int[w*h];
		float[][] m = matGauss(size, s);
		float[] sum = new float[]{0, 0, 0};
		for(int x = size; x < w-size; x++) {
			for(int y =size; y < h-size; y++) {
				sum[0] = sum[1] = sum[2] = 0;
				for(int xx = -size; xx <= size; xx++) {
					for(int yy = -size; yy <= size; yy++) {
						for(int i = 0; i < 3; i++)
							sum[i] += (float) rgb[i][x+xx][y+yy]*m[size+xx][size+yy];
					}
				}
				for(int i = 0; i < 3; i++)
					pix[x + y*w] += ((int) sum[i]) << ((2-i)*8);
			}
		}
		image.setRGB(0, 0, w, h, pix, 0, w);
		save(image, name);
	}

	/**
	 * Ajoute si possible, un élément dans un tableau trié.
	 * @param ls Le tableau.
	 * @param b L'élément à ajouter.
	 */
	public static void addSort(int[] ls, int b) {
		int i = 0;
		while(i < ls.length && b <=  ls[i])
			i ++;
		while(i < ls.length) {
			int c = ls[i];
			ls[i] = b;
			b = c;
			if(b == 0)
				return;
			i ++;
		}
	}

	/**
	 * Effectue un lissage médian sur une image.
	 * @param image L'image à traiter.
	 * @param size La taille du filtre médian.
	 * @param name Le nom du fichier dans lequel sera enregistrée l'image.
	 */
	public static void lissageM(BufferedImage image, int size, String name) {
		int w = image.getWidth();
		int h = image.getHeight();
		int[][][] rgb = getRGB(image, w, h);
		int[] pix = new int[w*h];
		int d2 = ((2*size+1) * (2*size+1) + 1) / 2;
		int[][] ls = new int[3][0];
		for(int x = size; x < w-size; x++) {
			for(int y = size; y < h-size; y++) {
				for(byte i = 0; i < 3; i++)
					ls[i] = new int[d2];
				for(int xx = -size; xx <= size; xx++)
					for(int yy = -size; yy <= size; yy++)
						for(byte i = 0; i < 3; i++)
							addSort(ls[i], rgb[i][x+xx][y+yy]);
				for(byte i = 0; i < 3; i++)
					pix[x + y*w] += ls[i][d2-1] << ((2-i)*8);
			}
		}
		image.setRGB(0, 0, w, h, pix, 0, w);
		save(image, name);
	}

	/**
	 * Permet de séparer en plusieurs matrices (une pour chaques couleurs) une image.
	 * @param image L'image dont on veut récupérer les matrices.
	 * @param w La largeur de l'image.
	 * @param h La heuteur de l'image.
	 * @return Un tableau des trois matrices, rouge, vert puis bleu.
	 */
	public static int[][][] getRGB(BufferedImage image, int w, int h) {
		int[] pixels = new int[w*h];
		image.getRGB(0, 0, w, h, pixels, 0, w);
		int[][][] res = new int[3][w][h];
		for(int x = 0; x < w; x++)
			for(int y = 0; y < h; y++) {
				int i = pixels[x+y*w];
				res[0][x][y] = (i >> 16) & 0xFF;
				res[1][x][y] = (i >> 8) & 0xFF;
				res[2][x][y] = i & 0xFF;
			}
		return res;
	}

	/**
	 * Sauvegarde dans un fichier une image en format png.
	 * @param image L'image à enregistrer.
	 * @param name Le nom du fichier dans lequel sera sauvegardée l'image.
	 */
	public static void save(BufferedImage image, String name) {
		try {
			ImageIO.write(image, "png", new File(name));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Effectue un filtrage de Sobel pour récupérer les contours de l'image.
	 * @param image L'image à traiter.
	 * @param name Le nom du fichier dans lequel sera sauvegardée l'image.
	 */
	public static void contour(BufferedImage image, String name) {
		int w = image.getWidth();
		int h = image.getHeight();
		int[][][] rgb = getRGB(image, w, h);
		int[] pix = new int[w*h];
		float[][] m = new float[][]{new float[]{-0.25f, -0.5f, -0.25f}, new float[]{0, 0, 0}, new float[]{0.25f, 0.5f, 0.25f}};
		float[][] m2 = new float[][]{new float[]{-0.25f, 0, 0.25f}, new float[]{-0.5f, 0, 0.5f}, new float[]{-0.25f, 0, 0.25f}};
		float[] sum = new float[]{0, 0, 0}, sum2 = new float[]{0, 0, 0};
		for(int x = 1; x < w-1; x++) {
			for(int y = 1; y < h-1; y++) {
				sum[0] = sum[1] = sum[2] = sum2[0] = sum2[1] = sum2[2] = 0;
				for(int xx = -1; xx <= 1; xx++)
					for(int yy = -1; yy <= 1; yy++) {
						for(byte i = 0; i < 3; i++) {
							sum[i] += (float) rgb[i][x+xx][y+yy]*m[1+xx][1+yy];
							sum2[i] += (float) rgb[i][x+xx][y+yy]*m2[1+xx][1+yy];
						}
					}
				for(byte i = 0; i < 3; i++)
					pix[x + y*w] += (int) Math.min(255, Math.sqrt(sum[i]*sum[i] + sum2[i]*sum2[i])) << ((2-i)*8);
			}
		}
		image.setRGB(0, 0, w, h, pix, 0, w);
		save(image, name);
	}

	/**
	 * Transforme une image en couleur en une image en noir et blanc.
	 * @param image L'image à traiter.
	 * @param name Le nom du fichier dans lequel sera sauvegardée l'image.
	 */
	public static void blackWhite(BufferedImage image, String name) {
		int w = image.getWidth();
		int h = image.getHeight();
		for(int x = 0; x < w; x++)
			for(int y = 0; y < h; y++) {
				int i = image.getRGB(x, y);
				int j = (((i >> 16) & 0xFF) + ((i >> 8) & 0xFF) + (i & 0xFF)) / 3;
				image.setRGB(x, y, j + (j << 8) + (j << 16));
			}
		save(image, name);
	}

	/**
	 * Permet de changer la couleur de tous les pixels qui sont ou ne sont pas dans une certaine bande de couleurs choisie en une nouvelle
	 * couleur.
	 * @param image L'image à traiter.
	 * @param name Le nom du fichier dans lequel sera sauvegardée l'image.
	 * @param r1 Limite inférieur de la composante rouge de la bande de couleur.
	 * @param r2 Limite supérieur de la composante rouge de la bande de couleur.
	 * @param g1 Limite inférieur de la composante verte de la bande de couleur.
	 * @param g2 Limite supérieur de la composante verte de la bande de couleur.
	 * @param b1 Limite inférieur de la composante bleue de la bande de couleur.
	 * @param b2 Limite supérieur de la composante bleue de la bande de couleur.
	 * @param bool Vrai si ce sont les pixels dans la bande de couleurs qui sont remplacés, faux si se sont ceux à l'extérieur de la
	 * bande qui sont remplacés.
	 * @param color La nouvelle couleur à appliquer.
	 */
	public static void passeBande(BufferedImage image, String name, int r1, int r2, int g1, int g2, int b1, int b2, boolean bool, int color) {
		int w = image.getWidth();
		int h = image.getHeight();
		for(int x = 0; x < w; x++)
			for(int y = 0; y < h; y++) {
				int i = image.getRGB(x, y);
				int r = (i >> 16) & 0xFF, g = (i >> 8) & 0xFF, b = i & 0xFF;
				if((r>r2 || r<r1 || g>g2 || g<g1 || b>b2 || b<b1) != bool)
					image.setRGB(x, y, color);
			}
		save(image, name);
	}

	/**
	 * Permet de ne récupérer que la partie où se trouve le code postal sur une image au préalablement traité.
	 * @param image L'image de l'enveloppe dejà traité.
	 * @param image2 L'image de l'enveloppe sans traitement.
	 * @param name Le nom du fichier dans lequel sera sauvegardée l'image.
	 */
	public static void separation(BufferedImage image, BufferedImage image2, String name) {
		int w = image.getWidth();
		int h = image.getHeight();
		int dist = (int)(w*0.012);
		float wish = (float) Math.sqrt(0.00359f*w*h);
		ArrayList<Integer[]> comp = new ArrayList<Integer[]>();
		int[][] app = new int[w][h];
		int itt = 0, wma = (int) (w*0.9f);
		for(int x = w/14; x < wma; x++) {
			for(int y = (int) (h*0.35f); y < h; y++) {
				int i = image.getRGB(x, y);
				int r = (i >> 16) & 0xFF, g = (i >> 8) & 0xFF, b = i & 0xFF;
				if(r != 0 || g != 0 || b != 0) {
					int mj = Math.min(w-1, x+dist);
					for(int j = Math.max(0, x-dist); j <= mj; j++) {
						for(int k = Math.max(0, y-dist); k <= y; k++) {
							if(app[j][k] != 0) {
								app[x][y] = app[j][k];
								Integer[] c = comp.get(app[x][y]-1);
								c[0] = Math.min(c[0], x);
								c[1] = Math.max(c[1], x);
								c[2] = Math.min(c[2], y);
								c[3] = Math.max(c[3], y);
								j = w;
								k = h;
							}
						}
					}
					if(app[x][y] == 0) {
						app[x][y] = itt + 1;
						comp.add(itt, new Integer[]{x, x, y, y});
						itt ++;
					}
				}
			}
		}
		for(int i = 0; i < itt-1; i++) {
			Integer[] c1 = comp.get(i);
			for(int j = i+1; j < itt; j++) {
				Integer[] c2 = comp.get(j);
				if(!(c1[0] > c2[1] || c1[1] < c2[0] || c1[2] > c2[3] || c1[3] < c2[2])) {
					c2[0] = Math.min(c2[0], c1[0]);
					c2[1] = Math.max(c2[1], c1[1]);
					c2[2] = Math.min(c2[2], c1[2]);
					c2[3] = Math.max(c2[3], c1[3]);
					c1[0] = -1;
					break;
				}
			}
		}
		int res = 0;
		double max = w*h;
		for(int i = 0; i < itt; i++) {
			Integer[] c = comp.get(i);
			if(c[0] == -1)
				continue;
			int w1 = c[1]-c[0] + 1, h1 = c[3]-c[2] + 1;
			double diff;
			if(w1 < 4*wish) {
				if(h1 < wish)
					diff = 4*wish*(wish - h1) + h1*(4*wish-w1);
				else
					diff = w1*(h1-wish) + wish*(4*wish-w1);
			} else {
				if(h1 < wish)
					diff = 4*wish*(wish - h1) + h1*(w1-4*wish);
				else
					diff = w1*(h1-wish) + wish*(w1-4*wish);
			}
			diff += 8.6 * (Math.abs(w1 - 4*wish) + Math.abs(h1 - wish));
			if(diff < max) {
				res = i;
				max = diff;
			}
		}
		Integer[] c = comp.get(res);
		int w1 = c[1]-c[0] + 1, h1 = c[3]-c[2] + 1;
		BufferedImage im = new BufferedImage(w1, h1, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2Image = im.createGraphics();
		g2Image.drawImage(image2, 0, 0, w1, h1, c[0], c[2], c[1]+1, c[3]+1, null);
		save(im, name);
	}

	/**
	 * Applique un seuillage à une image.
	 * @param image L'image à traiter.
	 * @param name Le nom du fichier dans lequel sera sauvegardée l'image.
	 * @param seuil Le seuil pour les 3 composantes rouge, vert et bleu.
	 * @param bool Vrai si se sont les pixels au dessus du seuil qui deviennet blanc, faux si se sont les pixels en dessous du seuil qui
	 * deviennent noir.
	 */
	public static void seuil(BufferedImage image, String name, int seuil, boolean bool) {
		int w = image.getWidth();
		int h = image.getHeight();
		for(int x = 0; x < w; x++)
			for(int y = 0; y < h; y++) {
				int i = image.getRGB(x, y);
				int r = (i >> 16) & 0xFF, g = (i >> 8) & 0xFF, b = i & 0xFF;
				if(bool && r >= seuil && g >= seuil && b >= seuil)
					image.setRGB(x, y, 0xFFFFFF);
				else if(!bool && r <= seuil && g <= seuil && b <= seuil)
					image.setRGB(x, y, 0);
			}
		save(image, name);
	}

	/**
	 * Sèpare les chiffres dans le code postal après avoir effectué le traitement nécessaire à celui ci.
	 * @param image L'image à traiter.
	 * @param name Le nom du fichier dans lequel sera sauvegardée l'image.
	 */
	public static void sep2(BufferedImage image, String name) {
		int w = image.getWidth();
		int h = image.getHeight();
		int[] df = new int[10];
		int[] dg = new int[]{h, 0, h, 0, h, 0, h, 0, h, 0};
		int i = 0;
		boolean col = false;
		for(int x = 0; x < w; x++) {
			boolean c = false;
			for(int y = 0; y < h; y++) {
				if((image.getRGB(x, y) & 0xFF) == 0) {
					dg[2*(int)(i/2)] = Math.min(dg[2*(int)(i/2)], y);
					dg[2*(int)(i/2)+1] = Math.max(dg[2*(int)(i/2)+1], y);
					c = true;
				}
			}
			if(c != col) {
				df[i] = x;
				i ++;
			}
			col = c;
		}
		if(df[9] == 0)
			df[9] = w;
		for(int j = 0; j < 5; j++) {
			BufferedImage im = new BufferedImage(df[2*j+1] - df[2*j] + 1, dg[2*j+1] - dg[2*j] + 1, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2Image = im.createGraphics();
			g2Image.drawImage(image, 0, 0, df[2*j+1] - df[2*j] + 1, dg[2*j+1] - dg[2*j] + 1, df[2*j], dg[2*j], df[2*j+1], dg[2*j+1] + 1, null);
			save(im, name+j+".png");
		}
	}

	/**
	 * Change la dimension d'une image de chiffre en gardant les proportions adéquates vers les dimensions nécessaire pour le réseau de
	 * neurones.
	 * @param image L'image à traiter.
	 * @param name Le nom du fichier dans lequel sera sauvegardée l'image.
	 */
	public static void conversion(BufferedImage image, String name, int nw, int nh, int d) {
		float w = image.getWidth();
		float h = image.getHeight();
		float h2, w2;
		if(w > 3*h/4) {
			w2 = nw-d;
			h2 = w2 / w * h;
		} else {
			h2 = nh-d;
			w2 = h2 / h * w;
		}
		int dw = (int) ((nw - w2)/2), dh = (int) ((nh - h2)/2);
		BufferedImage im = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2Image = im.createGraphics();
		g2Image.setColor(Color.white);
		g2Image.fillRect(0, 0, nw, nh);
		g2Image.drawImage(image, dw, dh, nw - dw, nh - dh, 0, 0, (int)w, (int)h, null);
		save(im, name);
	}

	/**
	 * Change les pixels qui sont dans une certaine zone de gris en des pixels noirs.
	 * @param image L'image à traiter.
	 * @param name Le nom du fichier dans lequel sera sauvegardée l'image.
	 */
	public static void GtoB(BufferedImage image, String name) {
		float w = image.getWidth();
		float h = image.getHeight();
		for(int x = 0; x < w; x++)
			for(int y = 0; y < h; y++) {
				int i = image.getRGB(x, y);
				int r = (i >> 16) & 0xFF, g = (i >> 8) & 0xFF, b = i & 0xFF;
				if(g > 100 && g < 148 && Math.abs(r-g) + Math.abs(g-b) < 27 )
					image.setRGB(x, y, 0);
			}
		save(image, name);
	}

	/**
	 * Converti toutes les images du dossier Images2 au format 24x32 et les enregistre dans le dossier Images3.
	 */
	public static void moveI2ToI3() {
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("Images2/nums.txt"))), 32768);
			for(int n = 0; n < 10; n++) {
				int num = Integer.parseInt(reader.readLine());
				for(int i = 0; i <= num; i++)
					conversion(ImageIO.read(new File("Images2/"+n+""+i+".png")), "Images3/"+n+""+i+".png", 24, 32, 0);
			}
			reader.close();
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
	}

	public static void moveI2ToV() {
		BufferedReader reader;
		PrintWriter writer;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("Images2/nums.txt"))), 32768);
			for(int n = 0; n < 10; n++) {
				int num = Integer.parseInt(reader.readLine());
				for(int i = 0; i <= num; i++) {
					float[] v = allinfo(ImageIO.read(new File("Images2/"+n+""+i+".png")));
					writer = new PrintWriter(new BufferedWriter(new FileWriter("Var/"+n+""+i+".txt")));
					for(int j = 0; j < 100; j++)
						writer.println(v[j]);
					writer.close();
				}
			}
			reader.close();
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
	}

	public static float[] getVars(int n, int i) throws IOException {
		float[] res = new float[100];
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("Var/"+n+""+i+".txt"))), 32768);
		for(int j = 0; j < 100; j++)
			res[j] = Float.valueOf(reader.readLine());
		reader.close();
		return res;
	}

	/**
	 * Permet d'obtenir les cinq petites images des chiffres de code postal sur une photo d'enveloppe dans le format 48x64 et 24x32.
	 * @param image L'image de l'enveloppe dont on veut récupérer le code postal.
	 * @return Un tableau des cinq images de chiffres au format 48x64 puis 24x32
	 * @throws IOException A cause des lectures de fichiers.
	 */
	public static BufferedImage[] getDigits(BufferedImage image) throws IOException {
		int w = image.getWidth();
		if(w > 1900) {
			conversion(image, "Traitement/a.png", 1900, (int) (image.getHeight()*1900f/((float) w)), 0);
			w = 1900;
			image = ImageIO.read(new File("Traitement/a.png"));
		} else
			save(image, "Traitement/a.png");
		BufferedImage image2 = ImageIO.read(new File("Traitement/a.png"));
		contour(image2, "Traitement/b.png");
		image2 = ImageIO.read(new File("Traitement/b.png"));
		passeBande(image2, "Traitement/c.png", 6, 60, 16, 160, 20, 200, false, 0);
		image2 = ImageIO.read(new File("Traitement/c.png"));
		lissageG(image2, 1+w/1500, 1.3f, "Traitement/d.png");
		image2 = ImageIO.read(new File("Traitement/d.png"));
		seuil(image2, "Traitement/e.png", 20, false);
		if(w > 1000)
			lissageM(image2, 1, "Traitement/e.png");
		image2 = ImageIO.read(new File("Traitement/e.png"));
		separation(image2, image, "Traitement/f.png");
		image2 = ImageIO.read(new File("Traitement/f.png"));
		seuil(image2, "Traitement/g.png", 165, true); // 160 ?
		seuil(image2, "Traitement/g.png", 120, false);
		image2 = ImageIO.read(new File("Traitement/g.png"));
		GtoB(image2, "Traitement/h.png");
		passeBande(image2, "Traitement/h.png", 130, 255, 50, 215, 0, 200, true, 0xFFFFFF);
		image2 = ImageIO.read(new File("Traitement/h.png"));
		seuil(image2, "Traitement/i.png", 254, false);
		sep2(image2, "Traitement/j");
		for(int i = 0; i < 5; i++) {
			image2 = ImageIO.read(new File("Traitement/j"+i+".png"));
			conversion(image2, "Traitement/k"+i+".png", 48, 64, 2);
			image2 = ImageIO.read(new File("Traitement/j"+i+".png"));
			conversion(image2, "Traitement/k"+(i+5)+".png", 24, 32, 2);
		}
		return new BufferedImage[] {ImageIO.read(new File("Traitement/k0.png")), ImageIO.read(new File("Traitement/k1.png")),
				ImageIO.read(new File("Traitement/k2.png")), ImageIO.read(new File("Traitement/k3.png")),
				ImageIO.read(new File("Traitement/k4.png")), ImageIO.read(new File("Traitement/k5.png")),
				ImageIO.read(new File("Traitement/k6.png")), ImageIO.read(new File("Traitement/k7.png")),
				ImageIO.read(new File("Traitement/k8.png")), ImageIO.read(new File("Traitement/k9.png"))};
	}

	/**
	 * Le Main pour mettre à jour les autres dossiers lorsque des images de chiffres ont été ajouté dans le dossier Images2.
	 * @param args Argument inutile ici.
	 */
	public static void main(String[] args) {
		moveI2ToI3();
		moveI2ToV();
	}

}
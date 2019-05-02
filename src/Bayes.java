import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;

public class Bayes {
	
	/**
	 * Main qui print les chiffres que le lit le réseau bayésien sur les 5 petites images de chiffres
	 * situées dans le dossier prévu à cet effet.
	 * @param args Argument inutile ici.
	 */
	public static void main(String[] args) {
		double[][] e = new double[10][100], v = new double[10][100];
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("Images2/nums.txt"))), 32768);
			for(int n = 0; n < 10; n++) {
				int num = Integer.parseInt(reader.readLine());
				for(int i = 0; i < num; i++) {
					BufferedImage image = ImageIO.read(new File("Images2/"+n+""+i+""+".png"));
					float[] dat = ImageUtil.allinfo(image);
					for(int j = 0; j < 100; j++)
						e[n][j] += dat[j];
				}
				for(int j = 0; j < 100; j++)
					e[n][j] /= num;
				for(int i = 0; i < num; i++) {
					BufferedImage image = ImageIO.read(new File("Images2/"+n+""+i+""+".png"));
					float[] dat = ImageUtil.allinfo(image);
					for(int j = 0; j < 100; j++)
						v[n][j] += Math.pow(dat[j] - e[n][j], 2);
				}
				for(int j = 0; j < 100; j++) {
					v[n][j] /= (num - 1);
					if(v[n][j] == 0)
						v[n][j] += 0.001;
				}
			}
			reader.close();
			//Test
			for(int i = 0; i < 5; i ++) {
			BufferedImage image = ImageIO.read(new File("Traitement/k"+i+".png"));
			float[] dat = ImageUtil.allinfo(image);
			int res = -1;
			double best = - 999999999;
			for(int n = 0; n < 10; n++) {
				double p = 0;
				for(int j = 0; j < 100; j++) {
					p += logNorm(dat[j], e[n][j], v[n][j]);
				}
				if(p > best) {
					res = n;
					best = p;
				}
			}System.out.println(res);}
		} catch (NumberFormatException | IOException er) {
			er.printStackTrace();
		}
	}

	/**
	 * Permet d'obtenir le logarithme de la valeur de la densité de probabilité d'une loi normale
	 * de variance <b>v</b> et d'espérence <b>e</b> en <b>x</b> à une constante près.
	 * @param x La valeur en laquelle on évalue la densité de probabilité.
	 * @param e L'espérence de la loi.
	 * @param v La variance de la loi.
	 * @return Le logarithme de le probabilité à une constante près.
	 */
	public static double logNorm(float x, double e, double v) {
		return -Math.pow((x - e) / v, 2)/2 - Math.log(v);
	}
}
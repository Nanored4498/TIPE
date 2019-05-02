import java.io.*;

import javax.imageio.ImageIO;

/**
 * Deep Belief Network.
 * @author Yoann
 */
public class DBN implements Serializable {

	private static final long serialVersionUID = 1791654468121703150L;
	private RBM[] rbms; //Tableau des RBMs.
	private Network network; //Le réseau final.
	private RBM inv; //La RBM qui permet l'inversion du réseau.

	/**
	 * Crée un nouveau Deep Belief Network
	 * @param rbmSizes Tableau des tailles des vecteurs d'entrée des RBMs.
	 * @param netSizes Tableau des tailles des couches du perceptron.
	 */
	public DBN(int[] rbmSizes, int[] netSizes) {
		int len = rbmSizes.length;
		rbms = new RBM[len];
		for(int i = 0; i < len-1; i++)
			rbms[i] = new RBM(rbmSizes[i], rbmSizes[i+1]);
		rbms[len-1] = new RBM(rbmSizes[len-1], netSizes[0]);
		network = new Network();
		network.createNetwork(netSizes, netSizes[0], netSizes[netSizes.length-1], 0.01f);
		for(int i = 0; i < netSizes.length-1; i++) {
			network.connectAll(i, i+1, (float) Math.sqrt(6/(netSizes[i]+netSizes[i+1])));
			network.addSeuil(new int[]{i+1}, 0);
		}
	}

	/**
	 * Apprentissage d'un exemple à une des RBMs.
	 * @param i L'indice de la RBM é entraéner.
	 * @param x L'exemple é présenter.
	 */
	public void learnRBM(int i, float[] x) {
		for(int j = 0; j < i; j++)
			x = rbms[j].calc(x);
		rbms[i].learn(x);
	}

	/**
	 * Apprentissage par rétropropagation du gradient du réseau final.
	 * @param x L'exemple é présenter.
	 * @param out La sortie attendue.
	 * @return Un tableau représentant la sortie de ce Deep Belief Network.
	 */
	public float[] learnNet(float[] x, float[] out) {
		for(int j = 0; j < rbms.length; j++)
			x = rbms[j].calc(x);
		return network.learn(x, out);
	}

	/**
	 * Teste un exemple.
	 * @param x L'exemple é présenter.
	 * @return Un tableau représentant la sortie de ce Deep Belief Network.
	 */
	public float[] test(float[] x) {
		for(int j = 0; j < rbms.length; j++)
			x = rbms[j].calc(x);
		return network.test(x);
	}

	/**
	 * Teste un exemple mais cette fois-ci on n'utilise pas de MLP mais la RBM d'inversion pour obtenir le résultat
	 * @param x L'exemple é présenter.
	 * @return Un tableau représentant la sortie de ce Deep Belief Network.
	 */
	public float[] test2(float[] x) {
		for(int j = 0; j < rbms.length; j++)
			x = rbms[j].calc(x);
		float[] x2 = new float[inv.getInLen()];
		for(int k = x.length; k < x2.length; k++)
			x2[k] = 1 / (float) network.getOutLen();
		for(int k = 0; k < x.length; k++)
			x2[k] = x[k];
		x2 = inv.calcAndInv(x2);
		float[] res = new float[network.getOutLen()];
		for(int k = 0; k < res.length; k++)
			res[k] = x2[x.length + k];
		return res;
	}

	/**
	 * Donne les représentations des impacts des composantes du vecteur d'entrée du DBN sur la (<b>i</b>+1)éme couche du réseau.
	 * @param i L'indice de la RBM pour laquelle on cherche l'influence de l'entrée.
	 * @return Un tableau qui contient pour chaque composante de la sortie de la <b>i</b>éme RBM, un tableau qui contient l'impact de
	 * toutes les composantes du vecteur que l'on présente en entrée du DBN.
	 */
	public float[][] getRepresentations(int i) {
		float[][] res = rbms[0].getWeights();
		for(int j = 1; j <= i; j++)
			res = rbms[j].getRepresentations(res, res[0].length);
		return res;
	}

	/**
	 * Ajoute une RBM qui permet d'inverser le réseau pour générer de nouveau exemples.
	 * @param m Le nombres de neurones dans le couche cachée de la nouvelle RBM.
	 */
	public void addInv(int m) {
		inv = new RBM(network.getOutLen() + network.getInLen(), m);
	}

	/**
	 * Entraéne la RBM qui gére l'inversion.
	 * @param x L'exemple é présenter é la RBM pour l'apprentissage.
	 * @param res La sortie que serait sensé retourner le DBN s'il devait reconnaétre l'exemple.
	 */
	public void learnInv(float[] x, int res) {
		for(int j = 0; j < rbms.length; j++)
			x = rbms[j].calc(x);
		float[] x2 = new float[inv.getInLen()];
		x2[x.length + res] = 1;
		for(int k = 0; k < x.length; k++)
			x2[k] = x[k];
		inv.learn(x2);
	}

	/**
	 * Génére un nouvel exemple.
	 * @param len Le nombre d'étapes é faire pour générer le nouvel exemple. Une image sera crée pour chaque étape.
	 * @param w La largeur des images qui seront crée é chaque étape.
	 * @param h La hauteur des images qui seront crée é chaque étape.
	 * @param firstX La premiére entrée que l'on présente au réseau pour générer le nouvel exemple.
	 * @param res La classe du nouvel exemple, la sortie que l'on voudrait obtenir si l'on présentait le nouvel exemple au réseau.
	 */
	public void generate(int len, int w, int h, float[] firstX, int res) {
		ImageUtil.save(ImageUtil.ImageFromPixels(firstX, w, h), "test.png");
		for(int j = 0; j < rbms.length; j++)
			firstX = rbms[j].calc(firstX);
		float[] x2 = new float[inv.getInLen()];
		for(int k = 0; k < firstX.length; k++)
			x2[k] = firstX[k];
		for(int i = 0; i < len; i++) {
			firstX = new float[network.getInLen()];
			for(int k = 0; k < network.getOutLen(); k++)
				x2[firstX.length + k] = (k == res) ? 1 : 0;
			x2 = inv.calcAndInv(x2);
			for(int k = 0; k < firstX.length; k++)
				firstX[k] = x2[k];
			for(int j = rbms.length - 1; j >= 0; j--)
				firstX = rbms[j].inv(firstX);
			ImageUtil.save(ImageUtil.ImageFromPixels(firstX, w, h), "test"+i+".png");
		}
	}
	/**
	 * Permet de tester si la machine de Boltzmann restreinte est bien entrainer en comparant des image de chiffres
	 * aux images obtenues aprés un échantillonage de Gibbs jusqu'é la <b>i</b>iéme couche
	 * de longeur <b>len</b> en partant des image initiales.
	 * @param len La longueur de l'échantillonage de Gibbs é effectuer.
	 * @param xs Un tableau é deux dimensions oé <b>xs</b>[<b>j</b>][<b>k</b>] est le <b>k</b>iéme pixel de la <b>j</b>iéme image.
	 * @param w La largeur des images.
	 * @param h La hauteur des images.
	 * @param i L'indice de la couche jusqu'é laquelle on fait l'échantillonage de Gibbs.
	 */
	public void testError(int len, float[][] xs, int w, int h, int i) {
		for(int a = 0; a < xs.length; a++) {
			ImageUtil.save(ImageUtil.ImageFromPixels(xs[a], w, h), a+"init.png");
			for(int j = 0; j < len; j++) {
				for(int b = 0; b <= i; b++)
					xs[a] = rbms[b].calc(xs[a]);
				for(int b = i; b >= 0; b--)
					xs[a] = rbms[b].inv(xs[a]);
			}
			ImageUtil.save(ImageUtil.ImageFromPixels(xs[a], w, h), a+"rep.png");
		}
	}
	
	/**
	 * Sauvegarde ce Deep Belief Network dans un fichier.
	 * @param fileName Le nom du fichier dans lequel est fait la sauvegarde.
	 * @throws IOException Ca peut arriver ...
	 */
	public void save(String fileName) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName));
		oos.writeObject(this);
		oos.close();
	}

	public void setRBM(RBM rbm, int i) { rbms[i] = rbm; }
	public RBM getRBM(int i) { return rbms[i]; }

	/**
	 * Dernier Main utilisé.
	 * Le code de ce main a souvent changé, il m'a servi é faire des tests et des apprentissages.
	 * @param args Argument inutile ici.
	 */
	public static void main(String[] args) {
		try {
			String file = "Networks/DBN2.dbn";
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
			DBN dbn = (DBN) ois.readObject();
			ois.close();
//			Network network = new Network();
//			network.createNetwork(new int[]{40, 20, 15, 10}, 40, 10, 0.005f);
//			network.connectAll(0, 1, 0.32f);
//			network.connectAll(1, 2, 0.41f);
//			network.connectAll(2, 3, 0.49f);
//			network.addSeuil(new int[] {1, 2, 3}, 0);
//			dbn.network = network;
//			dbn.generate(100, 24, 32, ImageUtil.randomImage(32*24), 7);
			dbn.generate(100, 24, 32, ImageUtil.pixels(ImageIO.read(new File("Images3/024.png"))), 7);
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("Images2/nums.txt"))), 32768);
			int[] nums = new int[10];
			for(int i = 0; i < 10; i++)
				nums[i] = Integer.parseInt(reader.readLine());
			reader.close();
			int it = 0;
			while(it < 0) {
				if(it % 5000 == 0)
					System.out.println(it);
				int n = (int) (Math.random()*10);
				int i = (int) (Math.random()*(nums[n]+1));
				float[] out = new float[10];
				out[n] = 1;
//				dbn.learnRBM(1, ImageUtil.pixels(ImageIO.read(new File("Images3/"+n+""+i+".png"))));
				dbn.learnNet(ImageUtil.pixels(ImageIO.read(new File("Images3/"+n+""+i+".png"))), out);
//				dbn.learnInv(ImageUtil.pixels(ImageIO.read(new File("Images3/"+n+""+i+".png"))), n);
				it++;
			}
			float per = 0, err = 0, tot = 0;
			for(int n = 0; n < 10; n++) {
				tot += nums[n] + 1;
				for(int i = 0; i <= nums[n]; i++) {
					float[] rep = dbn.test2(ImageUtil.pixels(ImageIO.read(new File("Images3/"+n+""+i+".png"))));
					float pe = 0;
					int nu = 0;
					for(int j = 0; j < 10; j++) {
						err += (rep[j] - ((j == n) ? 1 : 0)) * (rep[j] - ((j == n) ? 1 : 0));
						if(rep[j] > pe) {
							pe = rep[j];
							nu = j;
						}
					}
					if(nu == n)
						per += 1f;
				}
			}
			System.out.println("per : " + (per*100/tot) + "   err : " + err);
			dbn.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

}
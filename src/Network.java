import java.io.*;
import java.util.ArrayList;

/**
 * @author Yoann
 * Réseau de neurones fonctionnant par rétropropagation du gradient.
 */
public class Network implements Serializable{

	private static final long serialVersionUID = 4076597283090814799L;
	private ArrayList<Neurone> neurones; //Listes des neurones du réseau
	private Neurone seuilNeu; //Neurones de seuil (sa valeur est toujours de 1)
	private int[] columns; //tableau des premiers indices de chaques colones
	private int inLen, outLen, startOut; //nombre de neurones d'entrée, de sortie et indice du premier neurone de sortie
	private float learning; //coefficient d'apprentissage

	
	/**
	 * Création d'un nouveu réseau sans aucun neurones.
	 */
	public Network() {
		neurones = new ArrayList<Neurone>();
	}

	/**
	 * Génère la strucutre principale du réseau.
	 * @param sizes Un tableau qui contient le nombre de neurones dans chaques colones que contient le réseau.
	 * @param inLen Le nombre de neurones en entrée.
	 * @param outLen Le nombre de neurones en sortie.
	 * @param learning Le coefficient d'apprentissage du réseau.
	 */
	public void createNetwork(int[] sizes, int inLen, int outLen, float learning) {
		columns = new int[sizes.length+1];
		columns[0] = 0;
		for(int i = 0; i < sizes.length; i++) {
			columns[i+1] = columns[i] + sizes[i];
		}
		for(int i = 0; i < columns[columns.length-1]; i++)
			neurones.add(new Neurone());
		this.learning = learning;
		this.inLen = inLen;
		this.outLen = outLen;
		this.startOut = columns[columns.length-1] - outLen;
		seuilNeu = new Neurone();
		seuilNeu.setValue(1);
	}

	/**
	 * Connecte tous les neurones d'une colone <b>a</b> à tous les neurones d'une colone <b>b</b>.
	 * @param a L'indice de la colone dont partent les axones.
	 * @param b L'indice de la colone où arrivent les axones.
	 * @param rangeWeight La longeur du demi interval centré en 0 dans lequel seront choisi aléatoirement les poids.
	 */
	public void connectAll(int a, int b, float rangeWeight) {
		for(int j = columns[a]; j < columns[a+1]; j++) {
			for(int k = columns[b]; k < columns[b+1]; k++) {
				Axon ax = new Axon(neurones.get(j), neurones.get(k), rangeWeight);
				neurones.get(j).addAxOut(ax);
				neurones.get(k).addAxIn(ax);
			}
		}
	}

	/**
	 * Connecte tous les neurones de plusieurs colones à tous les neurones d'une colone <b>b</b>.
	 * @param a Le tableau des indices des colones dont partent les axones.
	 * @param b L'indice de la colone où arrivent les axones.
	 * @param rangeWeight La longeur du demi interval centré en 0 dans lequel seront choisi aléatoirement les poids.
	 */
	public void connectAll(int a[], int b, float rangeWeight) {
		for(int a2 : a)
			connectAll(a2, b, rangeWeight);
	}

	/**
	 * Connecte des neurones en séquences d'une colone <b>a</b> à un en esemble de colonnes d'arrivée.
	 * @param a L'indice de la colone dont partent les axones.
	 * @param b Le tableau des indices des colones où arrivent les axones.
	 * @param len Le nombre de neurones consécutifs de la colone <b>a</b> qui sont connectés à un neurone d'une des colones d'arrivée.
	 * @param inc Le pas d'avancement du premier neurone de la colonne <b>a</b> connecté à un neurone d'une des colones d'arrivée.
	 * @param rangeWeight La longeur du demi interval centré en 0 dans lequel seront choisi aléatoirement les poids.
	 */
	public void connectSeq(int a, int[] b, int len, int inc, float rangeWeight) {
		int a1 = columns[a];
		for(int b2 : b) {
			int b3 = columns[b2];
			for(int j = b3; j < columns[b2+1]; j++) {
				for(int i = a1 + (j-b3)*inc; i < a1 + (j-b3)*inc + len; i++) {
					Axon ax = new Axon(neurones.get(i), neurones.get(j), rangeWeight);
					neurones.get(i).addAxOut(ax);
					neurones.get(j).addAxIn(ax);
				}
			}
		}
	}

	/**
	 * Connecte des colones à d'autres par l'intermédiaire d'axiones doubles, ainsi la taille des colones d'entrée doivent être deux fois
	 * plus grande que celle des colones de sortie.
	 * @param a a Le tableau des indices des colones dont partent les axones.
	 * @param b Le tableau des indices des colones où arrivent les axones.
	 * @param rangeWeight La longeur du demi interval centré en 0 dans lequel seront choisi aléatoirement les poids.
	 */
	public void connectDou(int a[], int[] b, float rangeWeight) {
		for(int i = 0; i < a.length; i++) {
			int a2 = columns[a[i]], b2 = columns[b[i]];
			for(int j = 0; j < columns[b[i]+1]-b2; j++) {
				Axon ax = new AxonD(neurones.get(a2+2*j), neurones.get(a2+2*j+1), neurones.get(b2+j), rangeWeight);
				neurones.get(a2+2*j).addAxOut(ax);
				neurones.get(a2+2*j+1).addAxOut(ax);
				neurones.get(b2+j).addAxIn(ax);
			}
		}
	}

	/**
	 * Ajoute un seuil aux neurones de plusieurs colones.
	 * @param c Le tableau des indices des colones auquelles on ajoute un seuil.
	 * @param rangeWeigth La longeur du demi interval centré en 0 dans lequel seront choisi aléatoirement les poids.
	 */
	public void addSeuil(int[] c, float rangeWeigth) {
		for(int i : c) {
			for(int j = columns[i]; j < columns[i+1]; j++) {
				Axon a = new Axon(seuilNeu, neurones.get(j), rangeWeigth);
				seuilNeu.addAxOut(a);
				neurones.get(j).addAxIn(a);
			}
		}
	}

	/**
	 * Permet de tester un échantillon sans faire apprendre le réseau.
	 * @param in Les données d'entrée sous forme d'un tableau.
	 * @return Un tableau des valeurs des neurones de sortie.
	 */
	public float[] test(float[] in) {
		if(in.length != inLen) {
			System.err.println("Le nombre d'entrées est incorrecte !!");
			System.err.println("in : " + inLen + "   votre valeur in : " + in.length);
			System.exit(0);
		}
		float[] res = new float[outLen];
		for(int i = 0; i < inLen; i++)
			neurones.get(i).setValue(in[i]);
		for(int i = inLen; i < columns[columns.length-1]; i++)
			neurones.get(i).updateValue();
		for(int i = 0; i < outLen; i++)
			res[i] = neurones.get(startOut + i).getValue();
		return res;
	}

	/**
	 * Permet de faire apprendre le réseau via un échantillon dont on connait la réponse.
	 * @param in Les données d'entrée sous forme d'un tableau.
	 * @param out Un tableau des valeurs de sorties attendues.
	 * @return Un tableau des valeurs des neurones de sortie.
	 */
	public float[] learn(float[] in, float[] out) {
		if(in.length != inLen || out.length != outLen) {
			System.err.println("Le nombre d'entrées ou de sorties est incorrecte !!");
			System.err.println("in : " + inLen + " out : " + outLen + "   vos valeurs in : " + in.length + " out : " + out.length);
			System.exit(0);
		}
		float[] res = new float[outLen];
		for(int i = 0; i < inLen; i++)
			neurones.get(i).setValue(in[i]);
		for(int i = inLen; i < columns[columns.length-1]; i++)
			neurones.get(i).updateValue();
		for(int i = 0; i < outLen; i++) {
			neurones.get(startOut + i).calcDelta(out[i]);
			res[i] = neurones.get(startOut + i).getValue();
		}
		for(int i = startOut - 1; i >= inLen; i--)
			neurones.get(i).retropropagate(learning);
		for(int i = inLen - 1; i >= 0; i--)
			neurones.get(i).retroweight(learning);
		seuilNeu.retroweight(learning);
		return res;
	}

	/**
	 * @return La liste de tous les poids.
	 */
	public ArrayList<Float> getWeights() {
		ArrayList<Float> res = new ArrayList<Float>();
		for(Neurone neurone : neurones)
			for(Axon axon : neurone.getAxonsIn())
				res.add(axon.getWeight());
		return res;
	}

	/**
	 * Sauvegarde le réseau dans un fichier.
	 * @param fileName Le nom du fichier.
	 * @throws IOException En cas de problème avec le fichier.
	 */
	public void save(String fileName) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName));
		oos.writeObject(this);
		oos.close();
	}

	public void setLearning(float learn) { this.learning = learn; }
	public float getLearning() { return this.learning; }
	public int getInLen() { return this.inLen; }
	public int getOutLen() { return this.outLen; }

}
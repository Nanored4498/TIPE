import java.io.IOException;

/**
 * Le réseau spécifique à la reconnaissance de chiffre.
 * @author Yoann
 */
public class Reseau {

	private Network network; //Le réseau.

	/**
	 * Crée un nouveau réseau adapté à la reconnaissance de chiffre.
	 */
	public Reseau() {
		network = new Network();
		network.createNetwork(new int[]{16, 32, 32, 20, 6, 6, 6, 14, 14, 14, 14, 14, 3, 3, 3, 7, 7, 7, 7, 7, 15, 10}, 100, 10, 0.04f);
		int[] pfo2 = new int[]{4, 5, 6}, pfdg2 = new int[]{7, 8, 9, 10, 11};
		network.connectSeq(0, pfo2, 6, 2, 0.52f);
		network.addSeuil(pfo2, 0);
		network.connectSeq(1, pfdg2, 6, 2, 0.36f);
		network.connectSeq(2, pfdg2, 6, 2, 0.36f);
		network.addSeuil(pfdg2, 0);
		int[] pfo3 = new int[]{12, 13, 14}, pfdg3 = new int[]{15, 16, 17, 18, 19};
		network.connectDou(pfo2, pfo3, 0.81f);
		network.connectDou(pfdg2, pfdg3, 0.58f);
		network.connectAll(pfo3, 20, 0.51f);
		network.connectAll(pfdg3, 20, 0.52f);
		network.connectAll(3, 20, 0.41f);
		network.connectAll(20, 21, 0.49f);
		network.addSeuil(new int[]{20,  21}, 0);
	}

	/**
	 * Remplace le réseau par un autre.
	 * @param net Le nouvea rééseau.
	 */
	public Reseau(Network net) {
		this.network = net;
	}

	public void setLearning(float learn) { network.setLearning(learn); }
	public float getLearning() { return network.getLearning(); }

	/**
	 * Entrainement du réseau.
	 * @param in L'entrée.
	 * @param ans Le chiffre qui doit étre reconnu.
	 * @return Un tableau contenant, la réponse la plus probable du réseau, le pourcentage associé à cette réponse, le pourcentage associé
	 * à la bonne réponse, et l'erreur totale quadratique commise.
	 */
	public float[] train(float[] in, int ans) {
		float[] out = new float[10];
		out[ans] = 1;
		float[] rep = network.learn(in, out);
		float err = 0, per = 0;
		int num = 0;
		for(int i = 0; i < 10; i++) {
			err += (rep[i] - out[i]) * (rep[i] - out[i]);
			if(rep[i] > per) {
				per = rep[i];
				num = i;
			}
		}
		return new float[]{num, per*100, rep[ans]*100, err};
	}

	/** Teste du réseau sur un échantillon.
	 * @param in L'entrée.
	 * @return @see {@link Network#test(float[])}
	 */
	public float[] train2(float[] in) {
		return network.test(in);
	}

	/**
	 * Teste du réseau sur un échantillon numéro 2.
	 * @param in L'entrée.
	 * @return Un tableau contenant le chiffre et le pourcentage associé é ce dernier pour les trois chiffres les plus probables daprés
	 * le réseau.
	 */
	public float[] train3(float[] in) {
		float[] out = network.test(in);
		return train3Main(out);
	}

	/**
	 * Corps tu teste numéro 2.
	 * @param out La sortie du réseau qui doit étre traité.
	 * @return Un tableau contenant le chiffre et le pourcentage associé à ce dernier pour les trois chiffres les plus probables daprés
	 * le réseau.
	 */
	public static float[] train3Main(float[] out) {
		float[] res = new float[6];
		for(int i = 0; i < 3; i++) {
			int num = 0;
			float per = 0;
			for(int j = 0; j < 10; j++) {
				if(out[j] > per) {
					per = out[j];
					num = j;
				}
			}
			out[num] = -1;
			res[2*i] = num;
			res[2*i+1] = per*100;
		}
		return res;
	}

	/**
	 * Entrainement du réseau numéro 2.
	 * @param in L'entrée.
	 * @param ans Le chiffre qui doit étre reconnu.
	 */
	public void train4(float[] in, int ans) {
		float[] out = new float[10];
		out[ans] = 1;
		network.learn(in, out);
	}

	/**
	 * @see Network#save(String)
	 * @param fileName Le nom du fichier dans lequel est enregistré le réseau.
	 * @throws IOException
	 */
	public void save(String fileName) throws IOException {
		network.save(fileName);
	}

}
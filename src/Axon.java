import java.io.Serializable;

public class Axon implements Serializable{

	private static final long serialVersionUID = 49351586429210727L;
	protected Neurone a, b; //Neurone de départ et d'arrivée.
	protected float w; //Poids.

	public static float inertie = 0.4f; //Coefficient d'inertie.
	private float lastAdd = 0; //Dernier ajout de poids (utile pour l'inertie).

	/**
	 * Creé un Axone entre deux neurones.
	 * @param a Le neurone de départ.
	 * @param b Le neurone d'arrivée.
	 * @param randomRange La longeur du demi interval centré en 0 dans lequel sera choisi aléatoirement le poids.
	 */
	public Axon(Neurone a, Neurone b, float randomRange) {
		this.a = a;
		this.b = b;
		this.w = (float) ((Math.random()-0.5)*2*randomRange);
	}

	/**
	 * Ajoute du poids.
	 * @param add La valeur de poids à ajouter.
	 */
	public void addWeight(float add) {
		lastAdd = add + lastAdd*inertie;
		w += lastAdd;
	}

	public float getValueIn() { return a.getValue();}
	public Neurone getOut() { return b; }
	public float getWeight() { return w; }

}
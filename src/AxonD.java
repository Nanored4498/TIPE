
public class AxonD extends Axon {

	private static final long serialVersionUID = 6513711911621746660L;
	private Neurone a2; //Le second neurone de départ.
	private float tempAdd; //L'ajout temporaire de poids en attendant l'ajout du second neurone.
	private boolean addW; //Pour savoir si on fait un ajout temporaire ou pas.

	/**
	 * Crée un Axone double entre deux neurones de départ et un de sortie.
	 * @param a Le premier neurone de départ.
	 * @param a2 Le second neurone dédépart.
	 * @param b Le neurone d'arrivée.
	 * @param randomRange La longeur du demi interval centré en 0 dans lequel sera choisi aléatoirement le poids.
	 */
	public AxonD(Neurone a, Neurone a2, Neurone b, float randomRange) {
		super(a, b, randomRange);
		this.a2 = a2;
		this.addW = false;
	}

	public float getValueIn() {
		return a.getValue() + a2.getValue();
	}

	public void addWeight(float add) {
		if(addW)
			w += (tempAdd + add)/2;
		else
			tempAdd = add;
		addW = !addW;
	}

}
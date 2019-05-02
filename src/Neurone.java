
import java.io.Serializable;
import java.util.ArrayList;

public class Neurone implements Serializable {

	private static final long serialVersionUID = 55680251679957181L;
	protected ArrayList<Axon> axIn, axOut; //Liste des axones qui arrive à ce neurone et qui partent de ce neurone.
	private float value; //La valeur actuel.
	private float delta; //L'erreur commise.

	/**
	 * Crée un nouveau neurone connecté à aucun autre neurone.
	 */
	public Neurone() {
		axIn = new ArrayList<Axon>();
		axOut = new ArrayList<Axon>();
	}

	protected float sig(float x) { return (float) (1 / (1 + Math.exp(-x))); } //fonction sigmoïde
	protected float sigpOfSig(float x) { return x*(1-x); } // retourne la valeur de la dérivée de la sigmoïde connaissant la valeur de la sigmoïde.

	/**
	 * Ajoute un axone en entrée.
	 * @param ax L'axone à ajouter en entrée.
	 */
	public void addAxIn(Axon ax) {
		this.axIn.add(ax);
	}

	/**
	 * Ajoute un axone en sortie.
	 * @param ax L'axone à ajouter en sortie.
	 */
	public void addAxOut(Axon ax) {
		this.axOut.add(ax);
	}

	/**
	 * Met à jour la valeur du neurone en fonction de ses entrées.
	 */
	public void updateValue() {
		value = 0;
		for(Axon a : axIn)
			value += a.getWeight() * a.getValueIn();
		value = sig(value);
	}

	/**
	 * Modifie le poids des axones de sortie en fonction de l'erreur commise actuelle.
	 * @param learning Le coefficient d'apprentissage.
	 */
	public void retroweight(float learning) {
		for(Axon a : axOut)
			a.addWeight(learning * a.getOut().delta * value);
	}

	/**
	 * Met à jour l'erreur puis modifie le poids des axones de sortie en fonction de l'erreur commise actuelle.
	 * @param learning Nouveu coeeficient d'apprentissage.
	 */
	public void retropropagate(float learning) {
		delta = 0;
		for(Axon a : axOut)
			delta += a.getWeight() * a.getOut().delta;
		delta *= sigpOfSig(value);
		for(Axon a : axOut)
			a.addWeight(learning * a.getOut().delta * value);
	}

	/**
	 * Calcule l'erreur de ce neurone qui doit être un neurone de sortie en fonction de la valeur qu'il devrait avoir.
	 * @param rightValue La valeur que devrait avor ce neurone.
	 */
	public void calcDelta(float rightValue) {
		delta = (rightValue - value) * sigpOfSig(value);
	}

	public float getValue() { return value; }
	public void setValue(float value) { this.value = value; }
	public float getDelta() { return delta; }
	public ArrayList<Axon> getAxonsIn() { return axIn; }
	
}
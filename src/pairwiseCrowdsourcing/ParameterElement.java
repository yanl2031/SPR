package pairwiseCrowdsourcing;

public class ParameterElement {
	double N_over_k;
	double probability;
	private int m;
	private int x;
	private int cost;
	
	public double getN_over_k() {
		return N_over_k;
	}
	public void setN_over_k(double n_over_k) {
		N_over_k = n_over_k;
	}
	public double getProbability() {
		return probability;
	}
	public void setProbability(double probability) {
		this.probability = probability;
	}
	public int getM() {
		return m;
	}
	public void setM(int m) {
		this.m = m;
	}
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getCost() {
		return cost;
	}
	public void setCost(int cost) {
		this.cost = cost;
	}
	
	
}

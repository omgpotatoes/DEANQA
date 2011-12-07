
package cs2731;


public class Guess implements Comparable<Guess>
{
	
	private double origProb;
	private double prob;
	private int line;

	public Guess(double prob, int line) {
		if (line <= 0) {
			throw new IllegalArgumentException("line numbers start with 1!");
		}
		this.prob = prob;
		this.origProb = prob;
		this.line = line;
	}
	
	public int getLine() {
		return line;
	}

	public double getProb() {
		return prob;
	}
	
	public void setProb(double inProb) {
		this.prob = inProb;
		this.origProb = inProb;
	}

	@Override
	public int compareTo(Guess o) {
		double diff = this.prob - o.prob;
		if (diff > 0) {
			return 1;
		} else if (diff == 0) {
			return 0;
		} else {
			return -1;
		}
	}
	
	public void setWeight(double weight) {
		prob = origProb * weight;
	}

	@Override
	public String toString() {
		return "line="+line+", prob="+prob;
	}
	
}

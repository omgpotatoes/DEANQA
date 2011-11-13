

package cs2731;


public class Guess implements Comparable<Guess>
{
	private double prob;
	private int line;

	public Guess(double prob, int line) {
		if (line <= 0) {
			throw new IllegalArgumentException("line numbers start with 1!");
		}
		this.prob = prob;
		this.line = line;
	}
	
	public int getLine() {
		return line;
	}

	public double getProb() {
		return prob;
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

        @Override
        public String toString() {
            return "line="+line+", prob="+prob;
        }
	
}

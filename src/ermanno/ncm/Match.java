package ermanno.ncm;

public class Match {
	public int a, b; // indices of the elements matched

	public Match(int a, int b) {
		this.a = a;
		this.b = b;
	}

	public Match copy() {
		return new Match(a, b);
	}

	public boolean isValid(Input input) {
		return input.a.get(a) == input.b.get(b);
	}

	public boolean doesCross(Match other) {
		return
				this.a == other.a || this.b == other.b ||
				this.a < other.a ^ this.b < other.b;
	}

	public boolean comesFirstThan(Match other) {
		return this.a < other.a && this.b < other.b;
	}

	@Override
	public boolean equals(Object o) { return o instanceof Match && _equals((Match)o); }
	private boolean _equals(Match o) { return o.a == a && o.b == b; }

	@Override
	public int hashCode() {
		return (a << 16) + b;
	}

	@Override
	public String toString() {
		return "("+a+","+b+")";
	}
}

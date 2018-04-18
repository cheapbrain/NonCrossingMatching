package ermanno.ncm;

import java.util.ArrayList;
import java.util.List;

public class Solution {

	public List<Match> matches;

	public Solution() {
		matches = new ArrayList<>();
	}

	public Solution(ArrayList<Match> matches) {
		this.matches = matches;
	}

	public Solution copy() {
		return new Solution(new ArrayList<>(this.matches));
	}

	public void clear() {
		matches.clear();
	}

	public void add(Match match) {
		matches.add(match.copy());
	}

	public void add(Solution other) {
		matches.addAll(other.matches);
	}

	public int size() {
		return matches.size();
	}

	public boolean isValid() {
		for (int i = 0; i < size()-1; i++) {
			if (matches.get(i).comesFirstThan(matches.get(i+1)) == false)
				return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		out.append("{");
		for(int i = 0; i < matches.size(); i++) {
			if (i > 0) out.append(", ");
			out.append(matches.get(i));
		}
		out.append("}");
		return out.toString();
	}
}

package ermanno.ncm;

import java.util.HashMap;
import java.util.HashSet;

public interface Solver {

	public Solution solve(Input input);

	public class AllPoints implements Solver {

		public Solution solve(Input input) {
			Solution sol = new Solution();

			for (int i = 0; i < input.a.length; i++) {
				for (int j = 0; j < input.b.length; j++) {
					if (input.a.get(i) == input.b.get(j)) {
						sol.add(new Match(i, j));
					}
				}
			}

			return sol;
		}

	}

	public class AllBest implements Solver {
		public int memo(Input input, HashMap<Match, Integer> cache) {
			int best = 0;
			if (input.a.length <= 0 || input.b.length <= 0) return best;

			for (int k = 0; k < 4; k++) {
				if (input.indA[k].length <= 0 || input.indB[k].length <= 0) continue;
				int ii = input.indA[k].get(0);
				int jj = input.indB[k].get(0);
				int i = ii - input.a.start;
				int j = jj - input.b.start;
				Match m = new Match(ii, jj);

				int sol = 0;
				Integer _sol = cache.get(m);
				if (_sol == null) {
					sol = 1 + memo(input.advance(i+1, j+1), cache);
					cache.put(m, sol);
				} else {
					sol = _sol;
				}

				best = Math.max(sol, best);
			}
			return best;
		}

		public void getAllSolutions(Input input, int score, HashMap<Match, Integer> cache, HashSet<Match> matches) {
			for (int k = 0; k < 4; k++) {
				if (input.indA[k].length <= 0 || input.indB[k].length <= 0) continue;
				int ii = input.indA[k].get(0);
				int jj = input.indB[k].get(0);
				int i = ii - input.a.start;
				int j = jj - input.b.start;
				Match m = new Match(ii, jj);

				int sol = cache.get(m);
				if (sol == score) {
					matches.add(m);
					getAllSolutions(input.advance(i+1, j+1), score-1, cache, matches);
				}
			}
		}

		public Solution solve(Input input) {
			HashMap<Match, Integer> cache = new HashMap<>();
			int len = memo(input, cache);
			HashSet<Match> matches = new HashSet<>();
			getAllSolutions(input, len, cache, matches);
			Solution sol = new Solution();
			for (Match m: matches) {
				sol.add(m);
			}

			return sol;
		}

	}

	public class Slow implements Solver {

		public Solution solve(Input input) {
			Solution best = new Solution();
			if (input.a.length <= 0 || input.b.length <= 0) return best;

			for (int k = 0; k < 4; k++) {
				if (input.indA[k].length <= 0 || input.indB[k].length <= 0) continue;
				int ii = input.indA[k].get(0);
				int jj = input.indB[k].get(0);
				int i = ii - input.a.start;
				int j = jj - input.b.start;

				Match m = new Match(ii, jj);
				Solution sol = new Solution();
				sol.add(m);
				sol.add(solve(input.advance(i+1, j+1)));

				if (sol.size() > best.size()) best = sol;
			}
			return best;
		}
	}

	public class Memo implements Solver {
		public Solution memo(Input input, HashMap<Match, Solution> cache) {
			Solution best = new Solution();
			if (input.a.length <= 0 || input.b.length <= 0) return best;

			for (int k = 0; k < 4; k++) {
				if (input.indA[k].length <= 0 || input.indB[k].length <= 0) continue;
				int ii = input.indA[k].get(0);
				int jj = input.indB[k].get(0);
				int i = ii - input.a.start;
				int j = jj - input.b.start;
				Match m = new Match(ii, jj);

				Solution sol = cache.get(m);
				if (sol == null) {
					sol = new Solution();
					sol.add(m);
					sol.add(memo(input.advance(i+1, j+1), cache));
					cache.put(m, sol);
				}

				if (sol.size() == best.size()) {
					Match m1 = sol.matches.get(0);
					Match m2 = best.matches.get(0);

					if (Math.max(m1.a, m1.b) < Math.max(m2.a, m2.b)) best = sol;
				}

				if (sol.size() > best.size()) best = sol;
			}
			return best;
		}

		public Solution solve(Input input) {
			HashMap<Match, Solution> cache = new HashMap<>();
			Solution sol = memo(input, cache);
			return sol;
		}
	}

	public class Greedy implements Solver {
		private ScoreFunction func;
		public Greedy(ScoreFunction func) {
			this.func = func;
		}

		public Solution solve(Input input) {
			Solution sol = new Solution();
			Match last = new Match(-1, -1);

			for (;;) {
				float max = Float.POSITIVE_INFINITY;
				Match next = last;

				for (int k = 0; k < 4; k++) {
					if (input.indA[k].length <= 0 || input.indB[k].length <= 0) continue;
					Match temp = new Match(input.indA[k].get(0), input.indB[k].get(0));
					float tc = func.score(last, temp) + 2 *(float)Math.random();
					if (tc < max || (tc == max && Math.random() > 0.5)) {
						max = tc;
						next = temp;
					}
				}
				if (max == Float.POSITIVE_INFINITY) break;

				int i = next.a - input.a.start;
				int j = next.b - input.b.start;
				last = next;
				sol.add(next);
				input = input.advance(i+1, j+1);
			}

			return sol;
		}

	}

	public interface ScoreFunction {
		public float score(Match prev, Match next);
	}

	public class AbsoluteAddScore implements ScoreFunction {
		public float score(Match prev, Match next) {
			return next.a + next.b;
		}
	}

	public class AbsoluteMaxScore implements ScoreFunction {
		public float score(Match prev, Match next) {
			return Math.max(next.a, next.b);
		}
	}

	public class CustomScore implements ScoreFunction {
		public float score(Match prev, Match next) {
			return 0.1f * Math.max(next.a, next.b) + next.a + next.b;
		}
	}

	public class RandomScore implements ScoreFunction {
		public float score(Match prev, Match next) {
			return (float)Math.random();
		}
	}
}

package ermanno.ncm;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;

public interface Solver {

	public Solution solve(Input input);

	public class SearchSubset implements Solver {
		private HashMap<Match, Integer> map = new HashMap<>();
		private int diags, repeat;
		private Solver s1, s2;
		
		public SearchSubset(Solver s1, Solver s2, int repeat, int diags) {
			this.s1 = s1;
			this.s2 = s2;
			this.diags = diags;
			this.repeat = repeat;
		}

		public void reset() {
			for (Match m: map.keySet()) {
				map.put(m, 0);
			}
		}

		public void add(Solution solution) {
			for (Match m:solution.matches) {
				map.put(m, 0);
			}
		}

		public void addDiagonals(Input input, int range) {
			int[] a = input.a.array;
			int[] b = input.b.array;

			for (int i = 0; i < a.length; i++) {

				int start = Math.max(0, i-range);
				int end = Math.min(b.length-1, i+range);
				for (int j = start; j <= end; j++) {
					if (a[i] == b[j]) {
						map.put(new Match(i, j), 0);
					}
				}
			}
		}

		public int memo(Input input) {
			class State {
				int best; int k; Match m; Input input;
				public State(int best, int k, Match m, Input input) { this.best=best; this.k=k; this.m=m; this.input=input; }
			}

			int maxsize = Math.min(input.a.length, input.b.length);

			ArrayDeque<State> stack= new ArrayDeque<>(maxsize);

			State state = new State(0, 0, new Match(-1, -1), input);

			while (true) {

				if (state.k == 4) {
					int score = state.best + 1;
					map.put(state.m, state.best);
					
					state = stack.pop();
					if (score > state.best) state.best = score;
				} else {
					if (state.input.indA[state.k].length <= 0 || state.input.indB[state.k].length <= 0) {
						state.k++;
					} else {

						int ii = state.input.firstA(state.k);
						int jj = state.input.firstB(state.k);
						state.k++;
						int i = ii - state.input.a.start;
						int j = jj - state.input.b.start;
						Match m = new Match(ii, jj);
	
						Integer sol = map.get(m);
						if (sol == null) {
							
						} else if (sol == 0) {
							stack.push(state);
							state = new State(0, 0, m, state.input.advance(i+1, j+1));
						} else {
							int score = sol + 1;
							if (score > state.best) state.best = score;
						}
					}
				}


				if (state.m.a == -1 && state.k == 4) return state.best - 1;
			}

		}

		public void getBestSolution(Input input, int score, Solution best) {
			for (;;) {
				boolean exit = true;
				for (int k = 0; k < 4; k++) {
					if (input.indA[k].length <= 0 || input.indB[k].length <= 0) continue;
					int ii = input.firstA(k);
					int jj = input.firstB(k);
					int i = ii - input.a.start;
					int j = jj - input.b.start;
					Match m = new Match(ii, jj);

					Integer sol = map.get(m);
					if (sol == null) continue;
					if (sol == score) {
						best.add(m);
						input = input.advance(i+1, j+1);
						score--;
						exit = false;
						break;
					}
				}
				if (exit) return;
			}
		}


		@Override
		public Solution solve(Input input) {
			map.clear();
			
			for (int i = 0; i < repeat; i++) {
				add(s1.solve(input));
				add(s2.solve(input));
			}
			
			addDiagonals(input, diags);
			
			Solution sol = new Solution();
			int score = memo(input);
			getBestSolution(input, score, sol);
			return sol;
		}

		public Solution allPoints() {
			Solution sol = new Solution();
			for(Match m: map.keySet()) {
				sol.add(m);
			}
			return sol;
		}

	}

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
				int ii = input.firstA(k);
				int jj = input.firstB(k);
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
				int ii = input.firstA(k);
				int jj = input.firstB(k);
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
				int ii = input.firstA(k);
				int jj = input.firstB(k);
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
				int ii = input.firstA(k);
				int jj = input.firstB(k);
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

	public class InvertedGreedy implements Solver {
		private ScoreFunction func;
		private float rand;
		public InvertedGreedy(ScoreFunction func, float rand) {
			this.func = func;
			this.rand = rand;
		}

		public Solution solve(Input input) {
			Solution sol = new Solution();
			Match dummy = new Match(0,0);
			Match last = dummy;

			for (;;) {
				float max = Float.NEGATIVE_INFINITY;
				Match next = dummy;

				for (int k = 0; k < 4; k++) {
					if (input.indA[k].length <= 0 || input.indB[k].length <= 0) continue;
					Match temp = new Match(input.lastA(k), input.lastB(k));
					float tc = func.score(last, temp);
					if ((tc + rand *(float)Math.random() > max) || (tc == max && Math.random() > 0.5)) {
						max = tc;
						next = temp;
					}
				}
				if (max == Float.NEGATIVE_INFINITY) break;

				int i = next.a - input.a.start;
				int j = next.b - input.b.start;
				last = next;
				sol.add(next);
				input = input.advanceBack(i-1, j-1);
			}
			sol.reverse();
			return sol;
		}

	}

	public class Greedy implements Solver {
		private ScoreFunction func;
		private float rand;
		public Greedy(ScoreFunction func, float rand) {
			this.func = func;
			this.rand = rand;
		}

		public Solution solve(Input input) {
			Solution sol = new Solution();
			Match dummy = new Match(0, 0);
			Match last = dummy;
			for (;;) {
				float max = Float.POSITIVE_INFINITY;
				Match next = dummy;

				for (int k = 0; k < 4; k++) {
					if (input.indA[k].length <= 0 || input.indB[k].length <= 0) continue;
					Match temp = new Match(input.firstA(k), input.firstB(k));
					float tc = func.score(last, temp);
					if ((tc + rand *(float)Math.random() < max) || (tc == max && Math.random() > 0.5)) {
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
			return next.a + next.b - prev.a - prev.b;
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

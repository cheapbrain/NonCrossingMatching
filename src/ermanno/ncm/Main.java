package ermanno.ncm;

import ermanno.ncm.Solver.Memo;

import java.awt.Color;

import ermanno.grafica.Layer;
import ermanno.grafica.Window;
import ermanno.ncm.Solver.Greedy;
import ermanno.ncm.Solver.InvertedGreedy;
import ermanno.ncm.Solver.SearchSubset;

public class Main {
	
	public static double measure(Solver solver, Input input) {
		double tot = 0;
		int count = 0;
		double score = 0;
		
		for (;;) {
			long start = System.nanoTime();
			Solution sol = solver.solve(input);
			long end = System.nanoTime();
			double time = (double)(end - start) / 1000000000.0;
			
			score += sol.size();
			tot += time;
			count += 1;
			if (tot > 0.0) {
				tot = tot / count;
				score = score / count;
				//System.out.format("%d\t%.1f\t%.7f\n", input.a.length, score, tot);				
				return score;
			}
		}
	}

	public static void main(String[] args) {
		class Range {
			private int max;
			private double add = 1;
			private double mul = 1;
			public double i = 0;
			
			public Range (int min, int max) {
				this.max = max;
				this.i = min;
			}
			
			public Range step(double add, double mul) {
				this.add = add;
				this.mul = mul;
				return this;
			}
			
			public boolean next() {
				boolean old = i < max;
				i = i * mul + add;
				boolean next = i < max;
				return !(old ^ next);
			}
			
			public int i() {
				return (int)i;
			}
			
		}
		
		var plot = new Window("Quality", 300, 200);
		
		var layer1 = new Layer(true, Color.red);
		var layer2 = new Layer(true, Color.blue);
		var layer3 = new Layer(true, Color.green);
		
		plot.add(layer1);
		plot.add(layer2);
		plot.add(layer3);
		
		for (int i = 0; i < 1; i++) {
			var r = new Range(10, 1000).step(100, 1);
			do {
				var input = Input.random(r.i());
				

				var greedy = new Greedy(new Solver.AbsoluteAddScore(), 1);
				
				float a = (float)measure(greedy, input) * 100;

				int count = (int)Math.log(input.a.length + input.b.length)+1;
				float rnd = 1;
				Solver s1 = new Greedy(new Solver.AbsoluteAddScore(), rnd);
				Solver s2 = new InvertedGreedy(new Solver.AbsoluteAddScore(), rnd);
				var solver = new SearchSubset(s1, s2, count, count);
				
				float b = (float)measure(solver, input) * 100;
				
				Solver memo = new Memo();
				
				float c = (float)measure(memo, input) * 100;
				
				layer1.add(r.i(), 1.0f);
				layer2.add(r.i(), b/c);
				layer3.add(r.i(), a/c);
				System.out.format("%d\t%.7f\t%.7f\t%.7f\n", input.a.length, c / c, b / c, a / c);
				
			} while(r.next());
		}

	}


}

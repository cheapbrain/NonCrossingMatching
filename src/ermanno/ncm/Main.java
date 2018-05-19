package ermanno.ncm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ermanno.grafica.Layer;
import ermanno.grafica.Plot;
import ermanno.ncm.Solver.Greedy;
import ermanno.ncm.Solver.InvertedGreedy;
import ermanno.ncm.Solver.Memo;
import ermanno.ncm.Solver.SearchSubset;

public class Main {
	static Color gray = new Color(0xA4A4A4);

	static Color[] colors = {
		new Color(0xF15854), new Color(0xFFC067), new Color(0x7192ef),
		new Color(0x9FE363), new Color(0xEF75D9), new Color(0x4BD3D5),
		new Color(0xAC5DDA), new Color(0x60BD68),
	};

	private static class Result {
		public float time;
		public float score;
		public Result(float time, float score) { this.time = time; this.score = score; }
	}

	public static Result measure(Solver solver, Input input) {
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
			if (tot > 0.01) {
				tot = tot / count;
				score = score / count;
				//System.out.format("%d\t%.1f\t%.7f\n", input.a.length, score, tot);
				return new Result((float)time, (float)score);
			}
		}
	}


	public static void demo() {
		Solver s1 = new Greedy(new Solver.AbsoluteAddScore(), 5);
		Solver s2 = new InvertedGreedy(new Solver.AbsoluteAddScore(), 5);
		class State {
			int i = 0;
			int size = 51;
			int diags = (int)Math.log(size+size) + 1;
			int repeat = diags;
			int expand = diags;
			Input input = Input.random(size);
			SearchSubset solver = new SearchSubset(s1, s2, repeat, diags, expand);
		}
		State s = new State();

		Solution allPoints = new Solver.AllPoints().solve(s.input);

		Plot plot = new Plot("Interactive", 600, 600, 0, -300);
		plot.setGrid(50, 50);

		Layer matches = new Layer(false, gray);
		matches.alpha = 100;

		matches.add(allPoints);

		JButton reset = new JButton("Reset");
		JButton next = new JButton("Next");

		JTextField fSize = new JTextField(""+s.size, 4);
		JLabel lSize = new JLabel("Size");
		lSize.setLabelFor(fSize);

		JTextField fDiags = new JTextField(""+s.diags, 4);
		JLabel lDiags = new JLabel("Diagonals");
		lDiags.setLabelFor(fDiags);

		JTextField fRepeat = new JTextField(""+s.repeat, 4);
		JLabel lRepeat = new JLabel("Repeat");
		lDiags.setLabelFor(fRepeat);

		JTextField fExpand = new JTextField(""+s.expand, 4);
		JLabel lExpand = new JLabel("Expand");
		lDiags.setLabelFor(fExpand);

		JPanel controls = new JPanel(new FlowLayout());
		controls.add(reset);
		controls.add(next);
		controls.add(lSize);
		controls.add(fSize);
		controls.add(lDiags);
		controls.add(fDiags);
		controls.add(lRepeat);
		controls.add(fRepeat);
		controls.add(lExpand);
		controls.add(fExpand);


		reset.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				plot.clear();

				s.i = 0;
				s.size = Integer.parseInt(fSize.getText());
				s.diags = Integer.parseInt(fDiags.getText());
				s.repeat = Integer.parseInt(fRepeat.getText());
				s.expand = Integer.parseInt(fExpand.getText());
				s.input = Input.random(s.size);
				s.solver = new SearchSubset(s1, s2, s.repeat, s.diags, s.expand);

				Solution allPoints = new Solver.AllPoints().solve(s.input);

				Layer matches = new Layer(false, gray);
				matches.alpha = 100;

				matches.add(allPoints);
				plot.add(matches);

				plot.fit();
				plot.update();
			}
		});

		next.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Solution sol = s.solver.step(s.input, s.i);
				if (sol == null) return;

				s.i++;

				plot.layers.get(plot.layers.size()-1).alpha = 20;
				Layer layer = new Layer(false, colors[s.i%8]);
				layer.add(sol);
				plot.add(layer);

				plot.fit();
				plot.update();
			}
		});

		plot.frame.add(controls, BorderLayout.SOUTH);

		plot.frame.validate();

		plot.add(matches);

		plot.fit();
		plot.update();
	}

	public static void bench() {
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

		Plot plot1 = new Plot("Quality", 600, 300, -600, -300);
		plot1.setYSuffix("%");
		Plot plot2 = new Plot("Time", 600, 300, -600, 0);
		plot2.setYSuffix("s");

		Color colorBest = colors[0];
		Color colorExpand = colors[1];
		Color colorMulti = colors[2];
		Color colorGreedy = colors[3];

		Layer best1 = new Layer(true, colorBest);
		Layer expand1 = new Layer(true, colorExpand);
		Layer multi1 = new Layer(true, colorMulti);
		Layer greedy1 = new Layer(true, colorGreedy);

		Layer best2 = new Layer(true, colorBest);
		Layer expand2 = new Layer(true, colorExpand);
		Layer multi2 = new Layer(true, colorMulti);
		Layer greedy2 = new Layer(true, colorGreedy);

		plot1.add(best1);
		plot1.add(expand1);
		plot1.add(multi1);
		plot1.add(greedy1);

		plot2.add(best2);
		plot2.add(expand2);
		plot2.add(multi2);
		plot2.add(greedy2);

		for (int count = 0; ;count++) {
			Range r = new Range(10, 4000).step(100, 1.1);
			int i = 0;
			do {
				int n = r.i();
				Input input = Input.random(n);

				int lognm = (int)Math.log(input.a.length + input.b.length)+1;
				float rnd = 1;
				Solver s1 = new Greedy(new Solver.AbsoluteAddScore(), rnd);
				Solver s2 = new InvertedGreedy(new Solver.AbsoluteAddScore(), rnd);

				Result multi = measure(new SearchSubset(s1, s2, lognm, lognm, 0), input);
				Result expand = measure(new SearchSubset(s1, s2, lognm, lognm, lognm), input);
				Result greedy = measure(new Greedy(new Solver.AbsoluteAddScore(), 1), input);


				Result best = null;
				if ( n < 2000 ) {
					best =  measure(new Memo(), input);
					best2.mix(n, best.time, i, count);
				} else {
					int score = 0;
					for (int k = 0; k < 4; k++) {
						score += Math.min(input.indA[k].length, input.indB[k].length);
					}
					score = (int)Math.round(0.666 * score);
					best = new Result(0, score);
				}

				expand.score = 100 * expand.score / best.score;
				multi.score = 100 * multi.score / best.score;
				greedy.score = 100 * greedy.score / best.score;

				best.score = 100;
				best1.mix(n, best.score, i, count);

				expand1.mix(n, expand.score, i, count);
				expand2.mix(n, expand.time, i, count);

				multi1.mix(n, multi.score, i, count);
				multi2.mix(n, multi.time, i, count);

				greedy1.mix(n, greedy.score, i, count);
				greedy2.mix(n, greedy.time, i, count);

				plot1.update();
				plot2.update();

				i++;

			} while(r.next());
		}

	}

	private static int val(int i, int input) {
		return (input >> (i*2)) & 0b11;
	}

	public static void searchBadCases() {
		System.out.println("Generating all possible inputs...");

		long timer = 0;
		long oldTime = System.currentTimeMillis();

		float worstScore = 1000;
		Input oldInput = null;
		Input worstInput = null;
		Solution worstSolution = null;
		Solution bestSolution = null;


		int n = 7;
		int max = 1 << (n*4);
		for (int i = 0; i < max; i++) {
			int[] a = new int[n];
			int[] b = new int[n];

			for (int j = 0; j < n; j++) {
				a[j] = val(j, i);
				b[j] = val(j+n, i);
			}

			Input input = new Input(new SubArray(a), new SubArray(b));

			Solution best = new Memo().solve(input);
			float rnd = 1;
			int lognm = (int)Math.log(input.a.length + input.b.length);
			Solution curr = new SearchSubset(
				new Greedy(new Solver.AbsoluteAddScore(), rnd),
				new InvertedGreedy(new Solver.AbsoluteAddScore(), rnd),
				lognm, lognm, lognm).solve(input);

			float score;
			if (best.size() == 0) score = 1000;
			else score = (float)curr.size() / (float)best.size();

			if (score-0.00001 <= worstScore) {
				worstScore = score;
				worstInput = input;
				worstSolution = curr;
				bestSolution = best;
			}

			long time = System.currentTimeMillis();
			long delta = time - oldTime;
			oldTime = time;
			timer -= delta;

			if (timer <= 0) {
				timer = 2000;
				if (worstInput != null && worstInput != oldInput) {
					System.out.println(worstScore);
					System.out.println(worstInput);
					System.out.println(bestSolution);
					System.out.println(worstSolution);
					oldInput = worstInput;
				}

				System.out.printf("progress: %5.2f%s\n", 100.0 * (double)i / max, "%");
			}
		}

		System.out.println(worstScore);
		System.out.println(worstInput);
		System.out.println(bestSolution);
		System.out.println(worstSolution);

		System.out.println("progress:   100%");
		System.out.println("done.");
	}

	public static void main(String[] args) {

		new Thread() {
			public void run() {
				bench();
			}
		}.start();

		demo();

	}


}

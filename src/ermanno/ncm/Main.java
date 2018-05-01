package ermanno.ncm;

import java.util.UUID;

import ermanno.ncm.Solver.Greedy;
import ermanno.ncm.Solver.InvertedGreedy;
import ermanno.ncm.Solver.SearchSubset;
import grafica.GPlot;
import grafica.GPointsArray;
import processing.core.PApplet;

public class Main extends PApplet{

	public static GPointsArray points1, points2;

	public static void addPoints(Solution sol, GPlot plot, int color, String name) {
		String id = UUID.randomUUID().toString();
		GPointsArray points = getPoints(sol);
		plot.addLayer(id, points);
		plot.getLayer(id).setPointColor(color);
		System.out.println(name+": "+sol.size());
	}

	public static GPointsArray getPoints(Solution sol) {
		GPointsArray out = new GPointsArray(sol.size());

		for (Match m : sol.matches) {
			out.add((float)m.a, (float)m.b);
		}

		return out;
	}

	public static void main(String[] args) {


		PApplet.main("ermanno.ncm.Main");
	}

	public void settings() {
		size(650, 650);
		smooth(4);
    }

	GPlot plot;

    public void setup() {
		surface.setResizable(true);

		plot = new GPlot(this);
		plot.setPos(0, 0);
		plot.setTitleText("alksdfljk");
		plot.activatePanning();
		plot.activateZooming(1.1f, CENTER, CENTER);

		Input input = Input.random(100000);
		addPoints(new Greedy(new Solver.AbsoluteAddScore(), 0).solve(input), plot, 0x77000000, "greedy");

		SearchSubset solver = new SearchSubset();
		float rnd = 1;
		for (int i = 0; i < Math.log(input.a.length + input.b.length); i++) {
			solver.add(new Greedy(new Solver.AbsoluteAddScore(), rnd).solve(input));
			solver.add(new InvertedGreedy(new Solver.AbsoluteAddScore(), rnd).solve(input));
		}
		solver.addDiagonals(input, (int)Math.log(input.a.length + input.b.length)+1);
		//addPoints(new Memo().solve(input), plot, 0x330000FF, "best");
		addPoints(solver.solve(input), plot, 0x77FF0000, "mine");
		//addPoints(solver.allPoints(), plot, 0x330000FF, "allpoints");
    }

    public void draw() {
		plot.setDim(this.width - 100, this.height - 100);
    	background(0x555555);

		plot.beginDraw();
		plot.drawBackground();
		plot.drawBox();
    	plot.drawGridLines(GPlot.BOTH);
		plot.drawXAxis();
		plot.drawYAxis();
		plot.drawTitle();
		plot.drawPoints();
		plot.drawLine(1, 0);
		plot.endDraw();
    }

}

package ermanno.ncm;

import java.util.UUID;

import ermanno.ncm.Solver.Greedy;
import ermanno.ncm.Solver.Memo;
import grafica.GPlot;
import grafica.GPointsArray;
import processing.core.PApplet;

public class Main extends PApplet{

	public static GPointsArray points1, points2;

	public static void addPoints(Solver solver, Input input, GPlot plot, int color) {
		String id = UUID.randomUUID().toString();
		Solution sol = solver.solve(input);
		GPointsArray points = getPoints(sol);
		plot.addLayer(id, points);
		plot.getLayer(id).setPointColor(color);
		System.out.println(solver.getClass()+": "+sol.size());
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
		size(950, 950);
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

		Input input = Input.random(1000);
		for (int i = 0; i < 10; i++) {
			addPoints(new Greedy(new Solver.AbsoluteAddScore()), input, plot, 0x77FF0000);
			//addPoints(new Greedy(new Solver.AbsoluteMaxScore()), input, plot, 0x770000FF);
		}
		//addPoints(new AllBest(), input, plot, 0x7700FF00);
		addPoints(new Memo(), input, plot, 0x770000FF);
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
		plot.drawHistograms();
		plot.endDraw();
    }

}

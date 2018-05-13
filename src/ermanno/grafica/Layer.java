package ermanno.grafica;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Layer {
	public List<Point> points = new ArrayList<>();
	public Color color = Color.red;
	public boolean lines = true;

	public Layer() {

	}

	public Layer(boolean lines, Color color) {
		this.lines = lines;
		this.color = color;
	}

	public void add(Point p) {
		points.add(p);
	}

	public void mix(float x, float y, int i, int count) {
		if (points.size() <= i) add(new Point(x, y));
		Point q = points.get(i);
		q.y = (q.y * count + y) / (count + 1);
	}

	public void add(float x, float y) {
		add(new Point(x, y));
	}

	public void clear() {
		points.clear();
	}


}

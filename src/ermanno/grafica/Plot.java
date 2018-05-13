package ermanno.grafica;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Plot {
	private JFrame frame;
	private MyPanel panel;

	private List<Layer> layers = new ArrayList<>();
	private int width;
	private int height;
	private float sx = 0.1f;
	private float sy = -100.0f;
	private float ox = 0.0f;
	private float oy = 0.0f;

	private int mx = 0;
	private int my = 0;

	private int mt = 10;
	private int mb = 30;
	private int ml = 80;
	private int mr = 20;

	private boolean autofit = true;

	public void add(Layer layer) {
		layers.add(layer);
	}

	public Layer get(int index) {
		if (index < 0 || index >= layers.size()) return null;
		return layers.get(index);
	}

	public Plot(String title, int width, int height) {
		this.width = width;
		this.height = height;
		Plot instance = this;

		panel = new MyPanel(this);
		frame = new JFrame(title);

		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				instance.setup(title, width, height);
			}
		});
	}

	private void setup(String title, int width, int height) {
		frame.setPreferredSize(new Dimension(width, height));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(panel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public void fit() {
		int sx1 = ml + 5;
		int sy1 = mt + 5;
		int sx2 = width - mr - 5;
		int sy2 = height - mb - 5;

		if (layers.size() == 0 || layers.get(0).points.size() == 0) return;
		Point t = layers.get(0).points.get(0);
		float minx = t.x;
		float miny = t.y;
		float maxx = t.x;
		float maxy = t.y;

		sx = 1.0f;
		sy = -1.0f;
		ox = (sx2 - sx1) / 2 - minx * sx;
		oy = (sy2 - sy1) / 2 - miny * sy;

		for (Layer l : layers) {
			for (Point p : l.points) {
				if (p.x < minx) minx = p.x;
				if (p.y < miny) miny = p.y;
				if (p.x > maxx) maxx = p.x;
				if (p.y > maxy) maxy = p.y;
			}
		}

		if (maxx > minx) {
			sx = (sx2 - sx1) / (maxx - minx);
			ox = sx1 - minx * sx;
		}

		if (maxy > miny) {
			sy = (sy2 - sy1) / (miny - maxy);
			oy = sy1 - maxy * sy;
		}
	}

	public void zoom(double wheel, boolean scalex, boolean scaley) {

		if (!scalex && !scaley) scalex = scaley = true;

		if (scalex) {
			float x = (mx - ox) / sx;
			sx *= 1.0f - 0.1f*(float)wheel;
			float ox = x * sx + this.ox - mx;
			this.ox -= ox;
		}

		if (scaley) {
			float y = (my - oy) / sy;
			sy *= 1.0f - 0.1f*(float)wheel;
			float oy = y * sy + this.oy - my;
			this.oy -= oy;
		}
	}

	private int tx(float x) {
		return (int)Math.round(x * sx + ox);
	}

	private int ty(float y) {
		return (int)Math.round(y * sy + oy);
	}

	private float cx(float x) {
		return (x - ox) / sx;
	}

	private float cy(float y) {
		return (y - oy) / sy;
	}

	public void update() {
		if (autofit) fit();
		panel.repaint();
	}

	private void render(Graphics2D g, int width, int height) {
		this.width = width;
		this.height = height;

		g.setRenderingHint(
			RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(
			RenderingHints.KEY_TEXT_ANTIALIASING,
			RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		float lineWidth = 1.5f;
		int pointRadius = 3;

		g.setColor(Color.white);
		g.fillRect(0, 0, width, height);

		{
			int gridHeight = 30;
			int gridWidth = 50;

			int sx1 = ml;
			int sy1 = mt;
			int sx2 = width - mr;
			int sy2 = height - mb;

			float dy = Math.abs(gridHeight / sy);
			int p10 = (int)Math.ceil(Math.log10(dy));
			int ndig = Math.max(-p10, 0) + 1;
			dy = (float)Math.pow(10, p10);
			if (Math.abs(dy * sy) / 5 > gridHeight) dy /= 5;
			else if (Math.abs(dy * sy) / 2 > gridHeight) dy /= 2;
			else ndig--;

			String pattern1 = "%."+ndig+"f";
			String pattern2 = "%.2e";


			float starty = (float)(Math.ceil(cy(sy2) / dy) * dy);
			float endy = cy(sy1);
			for (float y = starty; y < endy; y += dy) {

				int sy = ty(y);
				g.setColor(Color.lightGray);
				g.drawLine(ml - 5, sy, width - mr, sy);
				String pattern = (ndig > 4 || Math.abs(y) >= 100000) ? pattern2 : pattern1;
				String label = String.format(pattern, y + 0.0);
				Rectangle2D bounds = g.getFontMetrics().getStringBounds(label, g);
				int fh = (int) bounds.getHeight();
				int fw = (int) bounds.getWidth();
				g.setColor(Color.darkGray);
				g.drawString(label, ml - 10 - fw, sy + fh / 2 -2);
			}


			float dx = Math.abs(gridWidth / sx);
			p10 = (int)Math.ceil(Math.log10(dx));
			ndig = -p10 + 1;
			dx = (float)Math.pow(10, p10);
			if (Math.abs(dx * sx) / 5 > gridWidth) dx /= 5;
			else if (Math.abs(dx * sx) / 2 > gridWidth) dx /= 2;
			else ndig--;
			ndig = Math.max(ndig, 0);

			pattern1 = "%."+ndig+"f";
			pattern2 = "%.2e";


			float startx = (float)(Math.ceil(cx(sx1) / dx) * dx);
			float endx = cx(sx2);
			for (float x = startx; x < endx; x += dx) {

				int sx = tx(x);
				g.setColor(Color.lightGray);
				g.drawLine(sx, mt, sx, height-mb+5);
				String pattern = (ndig > 4 || Math.abs(x) >= 100000) ? pattern2 : pattern1;
				String label = String.format(pattern, x + 0.0);
				Rectangle2D bounds = g.getFontMetrics().getStringBounds(label, g);
				int fh = (int) bounds.getHeight();
				int fw = (int) bounds.getWidth();
				g.setColor(Color.darkGray);
				g.drawString(label, sx - fw / 2, height - mb + fh + 5);
			}


		}

		g.setColor(Color.gray);

		g.drawLine(ml, height-mb, width-mr, height-mb);
		g.drawLine(ml, mt, ml, height-mb);

		g.setClip(ml+1, mt, width - ml - mr, height - mt - mb);
		g.setStroke(new BasicStroke(lineWidth));

		for (Iterator<Layer> itLayer = layers.iterator(); itLayer.hasNext();) {
			Layer l = itLayer.next();

			int r = pointRadius;
			int d = r*2+1;
			g.setColor(l.color);

			Point o = null;
			for (Iterator<Point> itPoint = l.points.iterator(); itPoint.hasNext(); ) {
				Point p = itPoint.next();

				int px = tx(p.x);
				int py = ty(p.y);

				if (o != null && l.lines) {
					int x1 = tx(o.x);
					int y1 = ty(o.y);

					g.drawLine(x1, y1, px, py);
				}
				g.fillOval(px - r, py - r, d, d);

				o = p;
			}

		}

		g.dispose();
	}

	private class MyPanel extends JPanel implements MouseMotionListener, MouseWheelListener, MouseListener{
		private static final long serialVersionUID = 4030017636289661643L;
		private Plot parent;

		public MyPanel(Plot parent) {
			super();
			this.parent = parent;
			this.addMouseMotionListener(this);
			this.addMouseWheelListener(this);
			this.addMouseListener(this);
		}

		@Override
		protected void paintComponent(Graphics g) {

			super.paintComponent(g);
			parent.render((Graphics2D)g, getWidth(), getHeight());
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			autofit = false;

			int mods = e.getModifiersEx();
			boolean sx = (mods & InputEvent.SHIFT_DOWN_MASK) != 0;
			boolean sy = (mods & InputEvent.CTRL_DOWN_MASK) != 0;
			parent.zoom(e.getWheelRotation(), sx, sy);

			repaint();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			autofit = false;

			int mx = e.getX();
			int my = e.getY();
			int dx = mx - parent.mx;
			int dy = my - parent.my;
			parent.mx = mx;
			parent.my = my;

			parent.ox += dx;
			parent.oy += dy;

			repaint();
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			parent.mx = e.getX();
			parent.my = e.getY();
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				parent.fit();
				autofit = true;
				repaint();
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub

		}
	}
}

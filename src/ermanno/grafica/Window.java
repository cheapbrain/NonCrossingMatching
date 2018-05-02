package ermanno.grafica;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Window {
	private JFrame frame;
	private MyPanel panel;
	
	private List<Layer> layers = new ArrayList<>();
	private int width;
	private int height;
	private float zoom = 1.0f;
	private float sx = 0.1f;
	private float sy = -100.0f;
	private float ox = 0.0f;
	private float oy = 0.0f;
	
	private int mx = 0;
	private int my = 0;
	
	private int mt = 10;
	private int mb = 30;
	private int ml = 40;
	private int mr = 20;
	
	public void add(Layer layer) {
		layers.add(layer);
	}
	
	public Layer get(int index) {
		if (index < 0 || index >= layers.size()) return null;
		return layers.get(index);
	}
	
	public Window(String title, int width, int height) {
		this.width = width;
		this.height = height;
		var instance = this;
		
		EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
            	instance.setup(title, width, height);
            }
        });
	}
	
	private void setup(String title, int width, int height) {
		panel = new MyPanel(this);
        frame = new JFrame(title);
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

		zoom = 1.0f;
		
		for (Layer l : layers) {
			for (Point p : l.points) {
				
			}
		}
	}
	
	public void zoom(double wheel) {
		
		float x = (mx - ox) / zoom;
		float y = (my - oy) / zoom; 
		
		zoom *= 1.0f - 0.1f*(float)wheel;
		
		float ox = x * zoom + this.ox - mx;
		float oy = y * zoom + this.oy - my;
		
		this.ox -= ox;
		this.oy -= oy;
	}
	
	private int tx(float x) {
		return (int)Math.round(x * zoom * sx + ox);
	}
	
	private int ty(float y) {
		return (int)Math.round(y * zoom * sy + oy);
	}
	
	private float cx(float x) {
		return (x - ox) / (zoom * sx);
	}
	
	private float cy(float y) {
		return (y - oy) / (zoom * sy);
	}
	
	public void update() {
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
        
		int lineWidth = 1;
		int pointRadius = 2;

		g.setColor(Color.white);
		g.fillRect(0, 0, width, height);
		
		g.setColor(Color.lightGray);
		
		
		
		g.setColor(Color.gray);
		
		g.drawLine(ml-5, height-mb, width-mr+5, height-mb);
		g.drawLine(ml, mt-5, ml, height-mb+5);
		
        g.setClip(ml+1, mt, width - ml - mr, height - mt - mb);
       	g.setStroke(new BasicStroke(lineWidth));

        for (Layer l: layers) {
        	
       		int r = pointRadius;
       		int d = r*2+1;
       		g.setColor(l.color);
       		
       		Point o = null;
       		for (Point p:l.points) {
       			int px = tx(p.x);
       			int py = ty(p.y);

       			if (o != null) {
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
		private Window parent;

		public MyPanel(Window parent) {
			super();
			this.parent = parent;
			this.addMouseMotionListener(this);
			this.addMouseWheelListener(this);
		}

		@Override
		protected void paintComponent(Graphics g) {

	        super.paintComponent(g);
	        parent.render((Graphics2D)g, getWidth(), getHeight());
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			
			parent.zoom(e.getWheelRotation());

			repaint();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
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

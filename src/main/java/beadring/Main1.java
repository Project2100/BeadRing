/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beadring;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

/**
 *
 * @author Project2100
 */
public class Main1 {

	static int radiusLength = 150;
	static int sideCount = 37;
	static int offset = 200;
	static int dotRadius = 5;

	static class MyCanvas extends JPanel {

		List<Point> points;


		private void constructPoints() {
			points = new ArrayList<>(sideCount);

			// First point
			points.add(new Point(0, radiusLength));

			// Compute all points
			for (int i = 1; i < sideCount; i++) {
				double angle = Math.PI * 2 * i / sideCount;

				int X = -(int) (radiusLength * Math.sin(angle));
				int Y = (int) (radiusLength * Math.cos(angle));

				points.add(new Point(X, Y));
			}
		}


		public MyCanvas() {
			super();


			constructPoints();



			listenerList.add(MouseMotionAdapter.class, new MouseMotionAdapter() {

				@Override
				public void mouseMoved(MouseEvent e) {
					
				}
			});
		}



		@Override
		protected void paintComponent(Graphics painter) {
			super.paintComponent(painter);

			painter.setColor(Color.red);



			// Draw points
			points.forEach((point) -> {
				byte[] number = Integer.toString(points.indexOf(point)).getBytes();
				painter.fillOval(offset + point.x - dotRadius, offset + point.y - dotRadius, dotRadius * 2, dotRadius * 2);
				painter.drawBytes(number, 0, number.length, offset + point.x - dotRadius, offset + point.y - dotRadius);
			});

			// Draw polygon
			Polygon p = new Polygon();
			points.forEach((point) -> p.addPoint(point.x, point.y));
			p.translate(offset, offset);
			//painter.drawPolygon(p);

			// Draw circle
			painter.drawOval(offset - radiusLength, offset - radiusLength, radiusLength * 2, radiusLength * 2);

		}


	}



	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			
			var canvas = new MyCanvas();

			JFrame f = new JFrame("test");
			f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			f.setPreferredSize(new Dimension(500, 500));
			f.setLocationRelativeTo(null);
			f.setContentPane(canvas);
			f.pack();
			f.setVisible(true);

		});
	}

}

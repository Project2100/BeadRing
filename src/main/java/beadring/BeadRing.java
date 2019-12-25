/*
 * MIT License
 *
 * Copyright (c) 2018 Andrea Proietto
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package beadring;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.Timer;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.GroupLayout;
import javax.swing.WindowConstants;
import javax.swing.LayoutStyle;

/**
 *
 * @author Project2100
 */
class BeadRing extends JPanel {

	enum RotationMode {
		LINEAR, SINE
		// a * E ^ - ( ((x-b)^2) / (2*c^2) )
		/*
		a: height (1)
		b: offset (0)
		c: width (1)
		 */
	}

	/**
	 * Default background color
	 */
	static final Color DEFAULT_BG = new Color(24, 26, 39);

	/**
	 * Refresh delay for the animations, in milliseconds
	 */
	private static final int EVENT_DELAY = 10;

	Bead[] beads;
	Color[] beadColors;
	Bead bottom;

	private Map<Integer, Integer> inversionMappings;
	private boolean visiblePolygon;
	private boolean visibleRing;

	double currentAngle = 0;
	private final double unitAngle;

	// Updated on resize events
	int panelx = 300;
	int panely = 300;
	int offsetx; //helper
	int offsety; //helper

	int radiusLength;
	final int margin;

	final int beadRadius;

	public BeadRing(int mod) {
		this(mod, DEFAULT_BG, 15);
	}


	public BeadRing(int mod, Color background, int margin) {
		super();

		super.setBackground(background);
		// TODO Rectify
		if (background.getAlpha() < 255) {
			super.setOpaque(false);
		}

		// Compute the angle between two elements of this group
		unitAngle = Math.PI * 2 / mod;

		// The size of a single bead
		// AP191225 - TODO: Consider a dynamic bead size w.r.t. group size
		beadRadius = mod < 100 ? 6 : 4;

		// Rigid margin between ring and panel border
		this.margin = margin;

		beads = new Bead[mod];
		for (int i = 0; i < beads.length; i++) {
			beads[i] = new Bead();
			beads[i].radius = beadRadius;
		}
		bottom = new Bead();
		bottom.radius = beadRadius + 3;


		beadColors = new Color[mod];
		beadColors[0] = new Color(0);
		Color cocomposite = new Color(128, 0, 0), coprime = new Color(0, 128, 0);
		for (int i = 1; i < beadColors.length; i++)
			beadColors[i] = LinearCongruence.gcd(i, mod) == 1 ? coprime : cocomposite;


		// Tooltip handler
		super.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				for (int i = 0; i < beads.length; i++) {
					if (beads[i].contains(e.getX(), e.getY())) {
						BeadRing.super.setToolTipText("" + i);
						return;
					}
				}
				BeadRing.this.setToolTipText(null);
			}
		});


		// Ring resize handler
		super.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				offsetx = BeadRing.super.getWidth() / 2;
				offsety = BeadRing.super.getHeight() / 2;
				radiusLength = Math.min(offsetx, offsety) - BeadRing.this.margin;
				
				bottom.cx = offsetx;
				bottom.cy = offsety + radiusLength;
				positionDots(currentAngle);
			}
		});

		// This fires a resize event, which will both position dots and perform first paint
		super.setPreferredSize(new Dimension(panelx, panely));

		inversionMappings = new HashMap<>();

		visiblePolygon = false;
	}



	/**
	 * Paints all the shapes according to the current state
	 *
	 * @param painter
	 */
	@Override
	protected void paintComponent(Graphics painter) {
		super.paintComponent(painter);

		// Paint the beads
		Graphics2D painter0 = (Graphics2D) painter;
		// Do it in reverse to bring 0 up to front z-wise
		for (int i = beads.length - 1; i >= 0; i--) {
			painter0.setColor(beadColors[i]);
			painter0.fill(beads[i]);
		}

		// Paint the bottom contour
		painter0.setColor(Color.gray);
		painter0.draw(bottom);

		// Paint the inversion edges
		painter0.setColor(Color.blue);
		for (Map.Entry<Integer, Integer> entry : inversionMappings.entrySet()) {
			Bead bead1 = beads[entry.getKey()];
			Bead bead2 = beads[entry.getValue()];
			painter0.drawLine((int) bead1.cx, (int) bead1.cy, (int) bead2.cx, (int) bead2.cy);
		}

		// Paint the polygon connecting the beads
		painter0.setColor(Color.yellow);
		if (visiblePolygon) {
			for (int i = 0; i < beads.length - 1; i++) {
				painter0.drawLine((int) beads[i].cx, (int) beads[i].cy, (int) beads[i + 1].cx, (int) beads[i + 1].cy);
			}
			painter0.drawLine((int) beads[beads.length - 1].cx, (int) beads[beads.length - 1].cy, (int) beads[0].cx, (int) beads[0].cy);

		}

		// Paint the circle underlying the beads
		painter0.setColor(Color.magenta);
		if (visibleRing) {
			painter.drawOval(offsetx - radiusLength, offsety - radiusLength, radiusLength * 2, radiusLength * 2);
		}
	}


	/**
	 * Positions the beads and repaints the panel
	 *
	 * @param angle
	 */
	final void positionDots(double angle) {

		// Compute all points
		double dotRad = angle;
		for (Bead bead : beads) {
			bead.cx = offsetx - radiusLength * Math.sin(dotRad);
			bead.cy = offsety + radiusLength * Math.cos(dotRad);
			dotRad += unitAngle;
		}
		repaint();
	}

	/**
	 * Animates a rotation of the whole ring by {@code rad} radians.
	 *
	 *
	 * @param rad
	 * @param millis
	 * @param mode
	 */
	public void animateRotation(final double rad, final int millis, final RotationMode mode) {
		final long start = System.currentTimeMillis();
		final long endTime = millis + start;
		
		Timer a  = new Timer(EVENT_DELAY, null) {
			double startingAngle = currentAngle;

			@Override
			protected void fireActionPerformed(ActionEvent event) {
				long time = System.currentTimeMillis();
				if (time < endTime) {
					double r = mode == RotationMode.LINEAR ? ((double) (time - start)) / millis : (-Math.cos((((double) (time - start)) / millis) * Math.PI) + 1) / 2;
					currentAngle = startingAngle + r * rad;
					positionDots(currentAngle);
				}
				else {
					// Redraw to correct angle
					currentAngle = startingAngle + rad;
					positionDots(currentAngle);
					stop();
				}
			}
		};
		a.setCoalesce(true);
		a.start();
	}

	static final double computeRadians(int term, int modulus) {
		return ((double) term) * Math.PI * 2 / modulus;
	}


	void traceInversions() {
		for (int i = 2; i < beads.length; i++) {
			if (LinearCongruence.gcd(i, beads.length) == 1 && !inversionMappings.containsKey(i)) {
				int inverse = LinearCongruence.fmiclean(i, beads.length, 2);
				inversionMappings.put(i, inverse);
				inversionMappings.put(inverse, i);
			}
		}
		repaint();
	}

	void tracePolygon() {
		visiblePolygon = true;
	}

	void traceRing() {
		visibleRing = true;
	}


	/**
	 * Main entry point reserved for this class, for testing purposes
	 * 
	 * 
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {

			BeadRing canvas = new BeadRing(15);

			canvas.traceRing();
			canvas.tracePolygon();

			JFrame f = new JFrame("test");
			f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

			JButton b = new JButton("Rotate");
			b.addActionListener((event) -> canvas.animateRotation(Math.PI / 2, 2000, BeadRing.RotationMode.SINE));


			JButton b2 = new JButton("Inverses");
			b2.addActionListener((event) -> canvas.traceInversions());


			GroupLayout layout = new GroupLayout(f.getContentPane());

			f.getContentPane().setLayout(layout);
			
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addComponent(canvas)
					.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER, false)
							// How to size two grouped components the same
							.addComponent(b, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(b2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
					.addContainerGap());

			layout.setVerticalGroup(layout.createParallelGroup()
					.addComponent(canvas)
					.addGroup(layout.createSequentialGroup()
							.addContainerGap()
							.addComponent(b)
							// How to create a "spring" gap
							.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(b2)
							.addContainerGap()));


			f.pack();
			f.setLocationRelativeTo(null);
			f.setVisible(true);

		});
	}

}

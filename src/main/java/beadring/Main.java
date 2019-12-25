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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

/**
 * Entry point for the application
 *
 * @author Project2100
 */
public class Main {

	private static LinearCongruence.Radix radix = null;

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {

            // Setting up OS-native Look&Feel
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
			catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
				Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
			}

            // The three spinners for the three values of a linear congruence
			final SpinnerNumberModel coeffSM = new SpinnerNumberModel(1, 1, Short.MAX_VALUE, 1);
			final SpinnerNumberModel constSM = new SpinnerNumberModel(1, 1, Short.MAX_VALUE, 1);
			final SpinnerNumberModel modSM = new SpinnerNumberModel(1, 1, Short.MAX_VALUE, 1);
			final JSpinner coeffSpinner = new JSpinner(coeffSM);
			final JSpinner constSpinner = new JSpinner(constSM);
			final JSpinner modSpinner = new JSpinner(modSM);

            // Algebraic bits for cosmetic purposes at GUI level
			JLabel xLabel = new JLabel("x =");
			JLabel modLabel = new JLabel("mod");

            // The component viewing the congruence system
			final StandardListModel<LinearCongruence> systemModel = new StandardListModel<>();
			JList<LinearCongruence> system = new JList<>(systemModel);
			system.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_DELETE) {
						systemModel.remove(system.getSelectedIndex());
						system.repaint();
					}
				}
			});

            // Custom renderer for the system viewer
			ListCellRenderer<LinearCongruence> renderer = new ListCellRenderer<>() {

				JLabel strip = new JLabel();

				@Override
				public Component getListCellRendererComponent(JList<? extends LinearCongruence> list, LinearCongruence value, int index, boolean isSelected, boolean cellHasFocus) {

					// Should be called only once
					strip.setOpaque(true);

					if (isSelected) {
						if (cellHasFocus)
							strip.setBackground(Color.blue);
						else
							strip.setBackground(Color.gray);
						strip.setForeground(Color.white);
					}
					else {
						strip.setBackground(Color.white);
						strip.setForeground(Color.black);
					}

					strip.setText(value.toString());
					return strip;
				}
			};
			system.setCellRenderer(renderer);


			JButton addEqnButton = new JButton("+");
			addEqnButton.addActionListener((event) -> {
				systemModel.add(new LinearCongruence(coeffSM.getNumber().intValue(), constSM.getNumber().intValue(), modSM.getNumber().intValue()));
				system.updateUI();
			});



			JTextPane computeLog = new JTextPane();
			computeLog.setEditable(false);

			JButton computeButton = new JButton("Compute");

			JButton animateButton = new JButton("Animate");
			animateButton.setEnabled(false);

			animateButton.addActionListener((event) -> {

				JFrame ringFrame = new JFrame();
				List<LinearCongruence> eqns = systemModel.getElementList();
				List<BeadRing> rings = new ArrayList<>(eqns.size());
				for (LinearCongruence eqn : eqns) {
					BeadRing pane = new BeadRing(eqn.modulus);
					rings.add(pane);
				}
				rings.add(new BeadRing(radix.baseMod));

				JButton setButton = new JButton("Reset");
				setButton.addActionListener((evt) -> {
					for (BeadRing ring : rings) {
						ring.positionDots(BeadRing.computeRadians(0, ring.dots.length));
						ring.currentAngle = BeadRing.computeRadians(0, ring.dots.length);
					}
				});

				JButton solveButton = new JButton("Solve");
				solveButton.addActionListener((evt) -> {
					for (int i = 0; i < rings.size(); i++) {
						BeadRing ring = rings.get(i);
						ring.animateRotation(-BeadRing.computeRadians(radix.value * (i != rings.size() - 1 ? eqns.get(i).coefficient : 1), ring.dots.length), 12000, BeadRing.RotationMode.SINE);
					}
				});

				JButton normalizeButton = new JButton("Normalize");
				normalizeButton.addActionListener((evt) -> {
					//for (int i = 0; i < eqns.size(); i++) {
						eqns.replaceAll((c) -> c.coefficient == 1 ? c : new LinearCongruence(1, c.known * LinearCongruence.findMultInverse(c.coefficient, c.modulus), c.modulus));
					//}
				});


				GroupLayout l = new GroupLayout(ringFrame.getContentPane());
				ringFrame.getContentPane().setLayout(l);
				GroupLayout.ParallelGroup v = l.createParallelGroup();
				GroupLayout.SequentialGroup h = l.createSequentialGroup();
				for (BeadRing ring : rings) {
					v = v.addComponent(ring);
					h = h.addComponent(ring);
				}


				l.setHorizontalGroup(l.createParallelGroup().addGroup(h).addGroup(l.createSequentialGroup().addComponent(setButton).addComponent(solveButton).addComponent(normalizeButton)));
				l.setVerticalGroup(l.createSequentialGroup().addGroup(v).addGroup(l.createParallelGroup().addComponent(setButton).addComponent(solveButton).addComponent(normalizeButton)));

				ringFrame.pack();
				ringFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				ringFrame.setLocationRelativeTo(null);

				ringFrame.setVisible(true);

			});

			JButton animConcentricButton = new JButton("Concentric");
			animConcentricButton.addActionListener((event) -> {

				JFrame ringFrame = new JFrame();
				List<LinearCongruence> eqns = systemModel.getElementList();
				List<BeadRing> rings = new ArrayList<>(eqns.size());
				for (LinearCongruence eqn : eqns) {
					BeadRing pane = new BeadRing(eqn.modulus, new Color(0, 0, 0, 0), 15 + 500 / eqn.modulus);
					pane.setSize(new Dimension((int) (30 * Math.sqrt(eqn.modulus)), (int) (30 * Math.sqrt(eqn.modulus))));
					//BeadRing pane = new BeadRing(eqn.modulus, new Color(0, 0, 0, 0), (int) (15 + 100 * Math.log(eqn.modulus)));

					rings.add(pane);
				}
				rings.add(new BeadRing(radix.baseMod, new Color(0, 0, 0, 0), 15));

				JButton setButton = new JButton("Reset");
				setButton.addActionListener((evt) -> {
					for (BeadRing ring : rings) {
						ring.positionDots(BeadRing.computeRadians(0, ring.dots.length));
						ring.currentAngle = BeadRing.computeRadians(0, ring.dots.length);
					}
				});

				JButton solveButton = new JButton("Solve");
				solveButton.addActionListener((evt) -> {
					for (int i = 0; i < rings.size(); i++) {
						BeadRing ring = rings.get(i);
						ring.animateRotation(-BeadRing.computeRadians(radix.value * (i != rings.size() - 1 ? eqns.get(i).coefficient : 1), ring.dots.length), 30000, BeadRing.RotationMode.SINE);
					}
				});


				JButton normalizeButton = new JButton("Normalize");
				normalizeButton.addActionListener((evt) -> {
					//for (int i = 0; i < eqns.size(); i++) {
						eqns.replaceAll((c) -> c.coefficient == 1 ? c : new LinearCongruence(1, c.known * LinearCongruence.findMultInverse(c.coefficient, c.modulus), c.modulus));
					//}
				});


				GroupLayout l = new GroupLayout(ringFrame.getContentPane());
				ringFrame.getContentPane().setLayout(l);
				GroupLayout.ParallelGroup v = l.createParallelGroup(GroupLayout.Alignment.CENTER);
				GroupLayout.ParallelGroup h = l.createParallelGroup(GroupLayout.Alignment.CENTER);
				for (BeadRing ring : rings) {
					v = v.addComponent(ring);
					h = h.addComponent(ring);
				}


				l.setHorizontalGroup(l.createParallelGroup().addGroup(h).addGroup(l.createSequentialGroup().addComponent(setButton).addComponent(solveButton).addComponent(normalizeButton)));
				l.setVerticalGroup(l.createSequentialGroup().addGroup(v).addGroup(l.createParallelGroup().addComponent(setButton).addComponent(solveButton).addComponent(normalizeButton)));

				ringFrame.pack();
				ringFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				ringFrame.setLocationRelativeTo(null);

				ringFrame.setVisible(true);

			});

			computeButton.addActionListener((event) -> {
				ByteArrayOutputStream log = new ByteArrayOutputStream(255);
				try (PrintStream ps = new PrintStream(log)) {
					radix = LinearCongruence.solveCongruenceSystem(systemModel.getElementList().toArray(new LinearCongruence[systemModel.getSize()]), systemModel.getSize(), ps);
				}
				animateButton.setEnabled(radix != null);
				computeLog.setText(log.toString());
			});



			JScrollPane systemSP = new JScrollPane(system);
			JScrollPane computeLogSP = new JScrollPane(computeLog);

			JFrame f = new JFrame();
			f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

			GroupLayout layout = new GroupLayout(f.getContentPane());

			int logWidth = 300;
			int logHeight = 400;

			f.getContentPane().setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup()
							.addGroup(layout.createSequentialGroup()
									.addComponent(coeffSpinner)
									.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
									.addComponent(xLabel)
									.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
									.addComponent(constSpinner)
									.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
									.addComponent(modLabel)
									.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
									.addComponent(modSpinner)
									.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
									.addComponent(addEqnButton))
							.addComponent(systemSP)
							.addGroup(layout.createSequentialGroup()
									.addComponent(computeButton)
									// How to create a "spring" gap
									.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
									.addComponent(animConcentricButton)
									.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
									.addComponent(animateButton)))
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
					.addComponent(computeLogSP, GroupLayout.PREFERRED_SIZE, logWidth, Short.MAX_VALUE));

			layout.setVerticalGroup(layout.createParallelGroup()
					.addGroup(layout.createSequentialGroup()
							.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE, false)
									.addComponent(coeffSpinner)
									.addComponent(xLabel)
									.addComponent(constSpinner)
									.addComponent(modLabel)
									.addComponent(modSpinner)
									.addComponent(addEqnButton))
							.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
							.addComponent(systemSP)
							.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
							.addGroup(layout.createParallelGroup()
									.addComponent(computeButton)
									.addComponent(animateButton)
									.addComponent(animConcentricButton)))
					.addComponent(computeLogSP, GroupLayout.PREFERRED_SIZE, logHeight, Short.MAX_VALUE));


			f.pack();
			f.setLocationRelativeTo(null);
			f.setVisible(true);


			systemModel.add(new LinearCongruence(7, 3, 5));
			systemModel.add(new LinearCongruence(2, 4, 8));
			systemModel.add(new LinearCongruence(2, 11, 13));
			system.updateUI();
			computeButton.doClick();
		});



	}
}

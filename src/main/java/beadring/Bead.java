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

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author Project2100
 */
class Bead implements Shape {

	double cx, cy, radius;

	public Bead() {
		this(0, 0, 1);
	}

	public Bead(double cx, double cy, double radius) {
		this.cx = cx;
		this.cy = cy;
		this.radius = radius;
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle((int) Math.floor(cx - radius), (int) Math.floor(cy - radius), (int) Math.floor(radius * 2), (int) Math.floor(radius * 2));
	}

	@Override
	public Rectangle2D getBounds2D() {
		return new Rectangle2D.Double(cx - radius, cy - radius, radius * 2, radius * 2);
	}

	@Override
	public boolean contains(double x, double y) {
		return (x - cx) * (x - cx) + (y - cy) * (y - cy) <= radius * radius;
	}

	@Override
	public boolean contains(Point2D p) {
		return contains(p.getX(), p.getY());
	}

	@Override
	public boolean intersects(double x, double y, double w, double h) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean intersects(Rectangle2D r) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean contains(double x, double y, double w, double h) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean contains(Rectangle2D r) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public PathIterator getPathIterator(AffineTransform at) {
		return new EllipseIterator(new Ellipse2D.Double(cx - radius, cy - radius, radius * 2, radius * 2), at);
	}

	@Override
	public PathIterator getPathIterator(AffineTransform at, double flatness) {
		return new FlatteningPathIterator(getPathIterator(at), flatness);
	}

}

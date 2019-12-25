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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractListModel;

/**
 * A list model enriched with java#util#Iterable capabilities, notably for for-each use
 *
 * @implNote Uses an java#util#ArrayList as its underlying data structure
 *
 * @author Project2100
 * @param <E>
 */
public class StandardListModel<E> extends AbstractListModel<E> implements Iterable<E>{

	private final List<E> delegate = new ArrayList<>();

	@Override
	public int getSize() {
		return delegate.size();
	}

	@Override
	public E getElementAt(int index) {
		return delegate.get(index);
	}

	public void add(E object) {
		delegate.add(object);
	}

	@Override
	public Iterator<E> iterator() {
		return delegate.iterator();
	}
	
	public List<E> getElementList(){
		return new ArrayList<>(delegate);
	}
	
	public E remove(int index){
		return delegate.remove(index);
	}

}

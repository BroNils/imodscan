// @author GoogleX
package net.azib.ipscan.core.values;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * ObservableArrayList - a value object containing a list.
 * @author GoogleX
 * @param <T>
 */
public class ObservableArrayList<T> extends ArrayList<T> {
	private List<ObservableArrayListListener<T>> listeners;

	public ObservableArrayList() {
		listeners = new ArrayList<>();
	}

	public void addListener(ObservableArrayListListener<T> listener) {
		listeners.add(listener);
	}

	public void removeListener(ObservableArrayListListener<T> listener) {
		listeners.remove(listener);
	}

	protected void notifyListeners() {
		for (ObservableArrayListListener<T> listener : listeners) {
			listener.onListChanged(this);
		}
	}

	// modify
	@Override
	public T set(int index, T element){
		T retVal = super.set(index, element);
		notifyListeners();
		return retVal;
	}

	@Override
	public boolean add(T e) {
		boolean retVal = super.add(e);
		notifyListeners();
		return retVal;
	}

	@Override
	public void add(int index, T e) {
		super.add(index, e);
		notifyListeners();
	}

	@Override
	public boolean addAll(Collection<? extends T> e) {
		boolean retVal = super.addAll(e);
		notifyListeners();
		return retVal;
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> e) {
		boolean retVal = super.addAll(index, e);
		notifyListeners();
		return retVal;
	}

	@Override
	public boolean remove(Object o) {
		boolean retVal = super.remove(o);
		notifyListeners();
		return retVal;
	}

	@Override
	public T remove(int index) {
		T retVal = super.remove(index);
		notifyListeners();
		return retVal;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean retVal = super.removeAll(c);
		notifyListeners();
		return retVal;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean retVal = super.retainAll(c);
		notifyListeners();
		return retVal;
	}

	@Override
	public void clear() {
		super.clear();
		notifyListeners();
	}
}

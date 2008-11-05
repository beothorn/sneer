package wheel.reactive.lists.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import wheel.lang.Omnivore;
import wheel.reactive.Register;
import wheel.reactive.Signal;
import wheel.reactive.impl.AbstractNotifier;
import wheel.reactive.impl.RegisterImpl;
import wheel.reactive.lists.ListRegister;
import wheel.reactive.lists.ListSignal;
import wheel.reactive.lists.ListValueChange;

public class ListRegisterImpl<VO> implements ListRegister<VO> {
	
	private class MyOutput extends AbstractNotifier<ListValueChange<VO>> implements ListSignal<VO> {

		private static final long serialVersionUID = 1L;
		
		@Override
		public VO currentGet(int index) {
			return _list.get(index);
		}

		@Override
		public int currentSize() {
			return _list.size();
		}

		@Override
		public void addListReceiver(Omnivore<ListValueChange<VO>> receiver) {
			addReceiver(receiver);	
		}
		
		@Override
		public void removeListReceiver(Object receiver) {
			removeReceiver(receiver);		
		}

		@Override
		protected void initReceiver(Omnivore<? super ListValueChange<VO>> receiver) {}

		@Override
		protected void notifyReceivers(ListValueChange<VO> valueChange) {
			super.notifyReceivers(valueChange);
		}

		public Iterator<VO> iterator() {
			synchronized (_list) {
				return new ArrayList<VO>(_list).iterator(); //Optimize
			}
		}

		@Override
		public Signal<Integer> size() {
			return _size.output();
		}

		@Override
		public VO[] toArray() {
			synchronized (_list) {
				return (VO[]) _list.toArray();
			}
		}
	}

	Register<Integer> _size = new RegisterImpl<Integer>(0);

	private final List<VO> _list = new ArrayList<VO>();
	private MyOutput _output = new MyOutput();
	
	@Override
	public void add(VO element) {
		synchronized (_list) {
			_list.add(element);
			_size.setter().consume(_list.size());
		}
		_output.notifyReceivers(new ListElementAdded<VO>(_list.size() - 1, element));
	}
	
	@Override
	public void addAt(int index, VO element) {
		synchronized (_list) {
			_list.add(index, element);
			_size.setter().consume(_list.size());
		}
		_output.notifyReceivers(new ListElementAdded<VO>(index, element));
	}
	
	@Override
	public void remove(VO element) {
		synchronized (_list) {
			int index = _list.indexOf(element);
			if (index == -1) throw new IllegalArgumentException("ListRegister did not contain element to be removed: " + element);
			
			removeAt(index);
		}
	}

	@Override
	public void removeAt(int index) {
		VO oldValue = _list.get(index);
		_output.notifyReceivers(new ListElementToBeRemoved<VO>(index, oldValue));
		synchronized (_list) {
			_list.remove(index);
			_size.setter().consume(_list.size());
		}
		_output.notifyReceivers(new ListElementRemoved<VO>(index, oldValue));
	}

	public ListSignal<VO> output() {
		return _output;
	}

	@Override
	public Omnivore<VO> adder() {
		return new Omnivore<VO>() { @Override public void consume(VO valueObject) {
			add(valueObject);
		}};
	}

	@Override
	public void replace(int index, VO newElement) {
		VO old = _list.get(index);
		_output.notifyReceivers(new ListElementToBeReplaced<VO>(index, old, newElement));
		synchronized (_list) {
			_list.remove(index);
			_list.add(index, newElement);
		}
		_output.notifyReceivers(new ListElementReplaced<VO>(index, old, newElement));
	}
	
	@Override
	public int indexOf(VO element) {
		return _list.indexOf(element);
	}

	private static final long serialVersionUID = 1L;
}

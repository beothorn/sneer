package sneer.pulp.tuples.impl;

import static wheel.lang.Types.cast;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.prevayler.Prevayler;
import org.prevayler.PrevaylerFactory;
import org.prevayler.foundation.serialization.XStreamSerializer;

import snapps.wind.impl.bubble.Bubble;
import sneer.kernel.container.Inject;
import sneer.pulp.clock.Clock;
import sneer.pulp.config.persistence.PersistenceConfig;
import sneer.pulp.keymanager.KeyManager;
import sneer.pulp.tuples.Tuple;
import sneer.pulp.tuples.TupleSpace;
import wheel.lang.Consumer;
import wheel.lang.Types;
import wheel.reactive.lists.ListRegister;
import wheel.reactive.lists.impl.ListRegisterImpl;

public class TupleSpaceImpl implements TupleSpace {

	@Inject static private KeyManager _keyManager;
	@Inject static private Clock _clock;
	@Inject static private PersistenceConfig _config;
	
	//Refactor The synchronization will no longer be necessary when the container guarantees synchronization of model bricks.
	static class Subscription {

		private final Consumer<? super Tuple> _subscriber;
		private final Class<? extends Tuple> _tupleType;

		<T extends Tuple> Subscription(Consumer<? super T> subscriber, Class<T> tupleType) {
			_subscriber = cast(subscriber);
			_tupleType = tupleType;
		}

		void filterAndNotify(Tuple tuple) {
			if (!Types.instanceOf(tuple, _tupleType))
				return;
			
			_subscriber.consume(tuple);
		}


	}

	private static final int TRANSIENT_CACHE_SIZE = 1000;
	private final Set<Tuple> _transientTupleCache = new LinkedHashSet<Tuple>();
	private final List<Subscription> _subscriptions = new ArrayList<Subscription>();
	
	private final Set<Class<? extends Tuple>> _typesToKeep = new HashSet<Class<? extends Tuple>>();
	private final ListRegister<Tuple> _keptTuples;
	
	
	TupleSpaceImpl() {
		_keptTuples = Bubble.wrapStateMachine(prevayler(new ListRegisterImpl<Tuple>()));
	}


	private Prevayler prevayler(Serializable system) {
		PrevaylerFactory factory = prevaylerFactory(system);

		try {
			return factory.create();
		} catch (IOException e) {
			throw new wheel.lang.exceptions.NotImplementedYet(e); // Fix Handle this exception.
		} catch (ClassNotFoundException e) {
			throw new wheel.lang.exceptions.NotImplementedYet(e); // Fix Handle this exception.
		}
	}


	private PrevaylerFactory prevaylerFactory(Serializable system) {
		PrevaylerFactory factory = new PrevaylerFactory();
		factory.configurePrevalentSystem(system);
		factory.configurePrevalenceDirectory(directory());
		factory.configureJournalSerializer(new XStreamSerializer());
		factory.configureTransactionFiltering(false);
		return factory;
	}


	private String directory() {
		return new File(_config.persistenceDirectory(), "tuplespace").getAbsolutePath();
	}

	
	@Override
	public synchronized void publish(Tuple tuple) {
		stamp(tuple);
		acquire(tuple);
	}

	@Override
	public synchronized void acquire(Tuple tuple) {
		if (!_transientTupleCache.add(tuple)) return;
		capTransientTuples();
		
		if (isAlreadyKept(tuple)) return;
		keepIfNecessary(tuple);
				
		for (Subscription subscription : _subscriptions)
			subscription.filterAndNotify(tuple);
	}


	private void keepIfNecessary(Tuple tuple) {
		if (shouldKeep(tuple)) keep(tuple);
	}

	
	private boolean shouldKeep(Tuple tuple) {
		for (Class<? extends Tuple> typeToKeep : _typesToKeep) //Optimize
			if (Types.instanceOf(tuple, typeToKeep))
				return true;

		return false;
	}


	private boolean isAlreadyKept(Tuple tuple) {
		return _keptTuples.output().currentIndexOf(tuple) != -1;  //Optimize
	}


	private void keep(Tuple tuple) {
		_keptTuples.adder().consume(tuple);
	}

	
	private void stamp(Tuple tuple) {
		tuple.stamp(_keyManager.ownPublicKey(), _clock.time());
	}

	private void capTransientTuples() {
		if (_transientTupleCache.size() <= TRANSIENT_CACHE_SIZE) return;

		Iterator<Tuple> tuplesIterator = _transientTupleCache.iterator();
		tuplesIterator.next();
		tuplesIterator.remove();
		
	}

	@Override
	public synchronized <T extends Tuple> void addSubscription(Class<T> tupleType,	Consumer<? super T> subscriber) {
		Subscription subscription = new Subscription(subscriber, tupleType);

		for (Tuple kept : _keptTuples.output())
			subscription.filterAndNotify(kept);

		_subscriptions.add(subscription);
	}
	
	@Override
	public synchronized <T extends Tuple> void removeSubscription(Object subscriber) {
		for (Subscription victim : _subscriptions)
			if (victim._subscriber == subscriber) {
				_subscriptions.remove(victim);
				return;
			} 

		throw new IllegalArgumentException("Subscription not found.");
	}

	@Override
	public synchronized void keep(Class<? extends Tuple> tupleType) {
		_typesToKeep.add(tupleType);
	}

	@Override
	public synchronized List<Tuple> keptTuples() {
		return _keptTuples.output().currentElements();
	}

	@Override
	public void crash() {
		System.out.println("Necessary?");
	}


	@Override
	public int transientCacheSize() {
		return TRANSIENT_CACHE_SIZE;
	}

}

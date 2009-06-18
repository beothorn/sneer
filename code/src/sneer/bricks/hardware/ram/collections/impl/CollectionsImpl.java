package sneer.bricks.hardware.ram.collections.impl;

import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;

import sneer.bricks.hardware.ram.collections.Collections;
import sneer.foundation.lang.Functor;

public class CollectionsImpl implements Collections{

	@Override
	public <I, O> Collection<O> collect(Collection<I> inputCollection, Functor<? super I, ? extends O> functor) {
		return CollectionUtils.collect(inputCollection, adapt(functor));
	}

	private <I, O> Transformer adapt( final Functor<? super I, ? extends O> functor) {
		return new Transformer(){ @Override public Object transform(Object input) {
			return functor.evaluate((I) input);
		}};
	}
}

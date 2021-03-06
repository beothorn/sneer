package sneer.bricks.pulp.reactive.collections.tests;

import static basis.environments.Environments.my;

import java.util.ArrayList;

import org.junit.Test;

import basis.lang.Consumer;

import sneer.bricks.pulp.reactive.collections.CollectionSignals;
import sneer.bricks.pulp.reactive.collections.ListRegister;
import sneer.bricks.software.folderconfig.testsupport.BrickTestBase;

public class ListRegisterTest extends BrickTestBase {

	@Test
	public void testSize() {
		final ListRegister<String> _subject = my(CollectionSignals.class).newListRegister();
		final ArrayList<Integer> _sizes = new ArrayList<Integer>();

		@SuppressWarnings("unused") final Object referenceToAvoidGc = _subject.output().size().addReceiver(new Consumer<Integer>() {@Override public void consume(Integer value) {
			_sizes.add(value);
		}});

		assertEquals(0, _subject.output().size().currentValue().intValue());
		assertContents(_sizes, 0);

		_subject.add("spam");
		assertEquals(1, _subject.output().size().currentValue().intValue());
		assertContents(_sizes, 0, 1);

		_subject.add("eggs");
		assertEquals(2, _subject.output().size().currentValue().intValue());
		assertContents(_sizes, 0, 1, 2);
	}
}

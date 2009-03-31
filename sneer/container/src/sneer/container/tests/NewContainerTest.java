package sneer.container.tests;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import sneer.container.BrickLoadingException;
import sneer.container.NewContainer;
import sneer.container.NewContainers;
import sneer.container.tests.fixtures.a.BrickA;
import sneer.container.tests.fixtures.b.BrickB;
import sneer.container.tests.fixtures.noannotation.InterfaceWithoutBrickAnnotation;
import wheel.io.Jars;

public class NewContainerTest extends Assert {
	

	final NewContainer subject = NewContainers.newContainer();
	
	@Test
	public void runBrick() throws Exception {
		System.setProperty("BrickA.ran", "false");
		runBrick(BrickA.class);
		assertEquals("true", System.getProperty("BrickA.ran"));
	}
	
	@Test
	public void runDependentBrick() throws Exception {
		
		runBrick(BrickA.class);

		System.setProperty("BrickA.property", "");
		runBrick(BrickB.class);
		assertEquals("BrickB was here!", System.getProperty("BrickA.property"));
	}

	@Test
	public void runInSeparateClassloaders() throws Exception {
		
		System.setProperty("BrickA.classloader", "");
		System.setProperty("BrickB.classloader", "");
		runBrick(BrickA.class);
		runBrick(BrickB.class);
		String classLoaderA = System.getProperty("BrickA.classLoader");
		String classLoaderB = System.getProperty("BrickB.classLoader");

		assertFalse(classLoaderA.equals(classLoaderB));
	}
	
	@Ignore
	@Test(expected=BrickLoadingException.class)
	public void runDependentBrickWithoutDependencies() throws Exception {
		runBrick(BrickB.class);
	}
	
	@Test(expected=BrickLoadingException.class)
	public void noBrickInterfaceFound() throws Exception {
		runBrick(InterfaceWithoutBrickAnnotation.class);
	}
	
	@Test(expected=FileNotFoundException.class)
	public void bogusDirectory() throws Exception {
		subject.runBrick("bogus");
	}
	
	private void runBrick(final Class<?> brick) throws IOException {
		String directory = Jars.directoryFor(brick);
		subject.runBrick(directory);
	}
}

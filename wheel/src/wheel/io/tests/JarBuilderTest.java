package wheel.io.tests;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import wheel.io.JarBuilder;
import wheel.io.Streams;

public class JarBuilderTest {

	@Test
	public void testSimpleJar() throws Exception {
		// test data
		String content = "sample content\nnew line";
		File data = File.createTempFile("entry-", ".txt");
		FileUtils.writeStringToFile(data, content);
		
		//create jar file
		File file = File.createTempFile("myJar-", ".jar");
		JarBuilder jar = new JarBuilder(file);
		jar.add("entry.txt", data);
		Streams.crash(jar);
		
		//test
		JarFile jarFile = new JarFile(file);
		InputStream is = jarFile.getInputStream(jarFile.getEntry("entry.txt"));
		assertEquals(content, read(is));
	}

	private String read(InputStream is) throws IOException {
		StringWriter writer = new StringWriter();
		IOUtils.copy(is, writer);
		return writer.getBuffer().toString();
	}
}
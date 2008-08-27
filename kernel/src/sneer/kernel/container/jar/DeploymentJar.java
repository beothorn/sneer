package sneer.kernel.container.jar;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

import sneer.kernel.container.utils.InjectedBrick;
import sneer.kernel.container.utils.io.NetworkFriendly;

public interface DeploymentJar extends Serializable, NetworkFriendly, Closeable {

	File file();

	String brickName();

	List<InjectedBrick> injectedBricks() throws IOException;

	String role();

	byte[] sneer1024();

	void close() throws IOException;

	InputStream getInputStream(String entryName) throws IOException;
	
	void explode(File target) throws IOException;
}
package sneer.bricks.expression.tuples.testsupport;

import static basis.environments.Environments.my;

import org.junit.After;

import sneer.bricks.expression.tuples.testsupport.pump.TuplePump;
import sneer.bricks.expression.tuples.testsupport.pump.TuplePumps;
import sneer.bricks.identity.seals.OwnSeal;
import sneer.bricks.identity.seals.Seal;
import sneer.bricks.identity.seals.contacts.ContactSeals;
import sneer.bricks.network.social.Contact;
import sneer.bricks.network.social.Contacts;
import sneer.bricks.software.folderconfig.testsupport.BrickTestBase;
import basis.environments.Environment;
import basis.environments.EnvironmentUtils;
import basis.environments.Environments;
import basis.lang.Closure;
import basis.lang.Producer;
import basis.lang.exceptions.Refusal;

public abstract class BrickTestWithTuples extends BrickTestBase {

	private static final String REMOTE_FRIEND_NICK = "Remote Friend";
	private static final String LOCAL_FRIEND_NICK = "Local Friend";

	private Environment _remote;
	private TuplePump _tuplePump;

	protected Environment remote(Object... bindings) {
		initRemoteEnvironmentIfNecessary(bindings);
		return _remote;
	}

	protected void waitForAllDispatchingToFinish() {
		_tuplePump.waitForAllDispatchingToFinish();
	}

	protected Contact remoteContact() {
		return my(Contacts.class).contactGiven(REMOTE_FRIEND_NICK);
	}

	protected Seal remoteSeal() {
		return EnvironmentUtils.produceIn(remote(), new Producer<Seal>() { @Override public Seal produce() {
			return ownSeal();
		}});
	}

	private Seal ownSeal() {
		return my(OwnSeal.class).get().currentValue();
	}

	private void initRemoteEnvironmentIfNecessary(Object... bindings) {
		if (_remote == null)
			initRemoteEnviromentWith(bindings);
	}

	private void initRemoteEnviromentWith(Object... bindings) {
		_remote = newTestEnvironment(bindings);
		configureStorageFolder(_remote, "remote/data");
		configureTmpFolder(_remote, "remote/tmp");
		connectLocalToRemote();
	}

	private void connectLocalToRemote() {
		_tuplePump = my(TuplePumps.class).startPumpingWith(_remote);

		final Seal localSeal = ownSeal();
		final Seal remoteSeal = remoteSeal();

		connectToContact(remoteSeal, REMOTE_FRIEND_NICK);

		Environments.runWith(_remote, new Closure() { @Override public void run() {
			connectToContact(localSeal, LOCAL_FRIEND_NICK);
		}});
	}

	private void connectToContact(Seal seal, String nickname) {
		try {
			my(Contacts.class).addContact(nickname);
			my(ContactSeals.class).put(nickname, seal);
		} catch (Refusal e) {
			throw new IllegalStateException(e);
		}
	}

	@After
	public void afterBrickTestWithTuples() {
		if (_remote != null)
			crash(_remote);
	}

}

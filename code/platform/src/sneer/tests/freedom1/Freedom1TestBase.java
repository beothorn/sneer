package sneer.tests.freedom1;

import org.junit.Test;

import sneer.tests.SovereignFunctionalTestBase;
import sneer.tests.SovereignParty;



public abstract class Freedom1TestBase extends SovereignFunctionalTestBase {
	
	@Test (timeout = 3000)
	public void testOwnName() {
		SovereignParty me = createParty("Klaus");
		changeNameTo(me, "Klaus W");
		changeNameTo(me, "Wuestefeld, Klaus");
		changeNameTo(me, "Klaus Wuestefeld");
	}

	private void changeNameTo(SovereignParty party, String newName) {
		party.setOwnName(newName);
		assertEquals(newName, party.ownName());
	}
}
package sneer.bricks.pulp.contacts;

import sneer.bricks.pulp.reactive.collections.ListSignal;
import sneer.foundation.brickness.Brick;
import sneer.foundation.lang.PickyConsumer;
import sneer.foundation.lang.exceptions.IllegalParameter;

@Brick
public interface ContactManager {
    
	ListSignal<Contact> contacts();

	boolean isNicknameAlreadyUsed(String nickname);
	
	Contact contactGiven(String nickname);

	/** @throws IllegalParameter if there already is a Contact with that nickname.*/
	Contact addContact(String nickname) throws IllegalParameter;

	/** Returns a contact with the given nickname. Creates a new one if there was no contact with that nickname before. */
	Contact produceContact(String nickname);

	PickyConsumer<String> nicknameSetterFor(Contact contact);
	
	void removeContact(Contact contact);


}
package sneer.bricks.network.computers.ports;

import sneer.bricks.network.social.attributes.Attribute;
import sneer.bricks.software.bricks.snappstarter.Snapp;
import sneer.foundation.brickness.Brick;

@Snapp
@Brick
public interface OwnPort extends Attribute<Integer> {

	Integer DEFAULT = 0; 

}

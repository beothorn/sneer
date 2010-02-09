package spikes.bamboo.mocotoh;

import sneer.bricks.hardware.ram.arrays.ImmutableByteArray;
import sneer.bricks.pulp.tuples.Tuple;

/** A packet of PCM-encoded sound: 8000Hz, 16 bits, 2 Channels (Stereo), Signed, Little Endian */
public class PcmSoundPacket extends Tuple {

	public final ImmutableByteArray payload;
	
	public PcmSoundPacket(ImmutableByteArray payload_) {
		payload = payload_;
	}

}

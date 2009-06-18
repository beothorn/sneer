package sneer.bricks.skin.sound.speaker.impl;

import static sneer.foundation.environments.Environments.my;
import sneer.bricks.pulp.keymanager.KeyManager;
import sneer.bricks.pulp.tuples.TupleSpace;
import sneer.bricks.skin.sound.PcmSoundPacket;
import sneer.foundation.lang.Consumer;

class PacketSubscriber implements Consumer<PcmSoundPacket> {

	private final TupleSpace _tupleSpace = my(TupleSpace.class);
	private final KeyManager _keyManager = my(KeyManager.class);
	
	private boolean _isRunning = true;
	private Consumer<PcmSoundPacket> _consumer;

	public PacketSubscriber(Consumer<PcmSoundPacket> consumer) {
		_consumer = consumer;
		_tupleSpace.addSubscription(PcmSoundPacket.class, this);
	}
	
	synchronized void crash() {
		_isRunning = false;
		_tupleSpace.removeSubscriptionAsync(this);
	}
	
	@Override
	synchronized public void consume(PcmSoundPacket packet) {
		if (!_isRunning) return;
		if (isMine(packet))	return;

		_consumer.consume(packet);
	}

	private boolean isMine(PcmSoundPacket packet) {
		return _keyManager.ownPublicKey().equals(packet.publisher());
	}
}
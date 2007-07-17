package sneer.apps.talk.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.xiph.speex.SpeexDecoder;

public class SpeexSpeaker extends Thread {
	private AudioFormat _format;

	private SourceDataLine _line;

	private boolean _running = true;

	private SpeexDecoder _decoder = new SpeexDecoder();

	byte[] _decodeBuffer = new byte[2048];

	public SpeexSpeaker() {
		setDaemon(true);
	}

	public void init() throws LineUnavailableException {
		_format = AudioUtil.getFormat();
		_line = AudioUtil.getSourceDataLine();
		_line.open(_format);
		_line.start();
		_decoder.init(0, (int) _format.getFrameRate(), _format.getChannels(), false);
		start();
		while (!isAlive()) {
			try {
				sleep(200);
			} catch (InterruptedException ie) {
			}
		}
		;
	}

	@Override
	public void run() {
		while (_running) {
			try {
				Thread.sleep(500);
			} catch (Exception e) {
			}
		}
		_line.close();
	}

	public synchronized void sendAudio(byte[] buffer, int length) {

		int index = 0;
		int counter = 0;
		for (int t = 0; t < AudioUtil.FRAMES; t++) {
			int size = AudioUtil.byteToShort(buffer, index);
			//System.out.println("decoding:"+size);
			index += 2;
			try {
				_decoder.processData(buffer, index, size);
				int processed = _decoder.getProcessedData(_decodeBuffer, 0);
				counter++;
				if (counter == 8){ //looses 1 packet every 8 packets, try to guarantee faster playback than capture
					counter = 0;
				}else{
					_line.write(_decodeBuffer, 0, processed);
				}
				counter++;
			} catch (Exception e) {
				e.printStackTrace();
			}
			index += size;
		}
	}

	public void close() {
		_running = false;
	}

}

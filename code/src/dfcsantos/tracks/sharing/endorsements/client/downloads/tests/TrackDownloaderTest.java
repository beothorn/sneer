package dfcsantos.tracks.sharing.endorsements.client.downloads.tests;

import static sneer.foundation.environments.Environments.my;

import java.io.File;
import java.io.IOException;

import org.jmock.Expectations;
import org.junit.Test;

import sneer.bricks.expression.files.client.FileClient;
import sneer.bricks.hardware.cpu.crypto.Crypto;
import sneer.bricks.hardware.cpu.crypto.Sneer1024;
import sneer.bricks.hardware.io.IO;
import sneer.bricks.hardware.ram.arrays.ImmutableByteArray;
import sneer.bricks.pulp.keymanager.Seal;
import sneer.bricks.pulp.keymanager.Seals;
import sneer.bricks.pulp.reactive.SignalUtils;
import sneer.bricks.pulp.tuples.TupleSpace;
import sneer.bricks.software.folderconfig.FolderConfig;
import sneer.bricks.software.folderconfig.tests.BrickTest;
import sneer.foundation.brickness.testsupport.Bind;
import dfcsantos.tracks.sharing.endorsements.client.downloads.TrackDownloader;
import dfcsantos.tracks.sharing.endorsements.protocol.TrackEndorsement;
import dfcsantos.tracks.storage.folder.TracksFolderKeeper;
import dfcsantos.wusic.Wusic;

public class TrackDownloaderTest extends BrickTest {

	private final TrackDownloader _subject = my(TrackDownloader.class);

	@Bind private final FileClient _fileClient = mock(FileClient.class);
	@Bind private final Seals _seals = mock(Seals.class);

	@Test(timeout = 3000)
	public void trackDownload() throws Exception {
		final Sneer1024 hash1 = my(Crypto.class).digest(new byte[] { 1 });
		final Sneer1024 hash2 = my(Crypto.class).digest(new byte[] { 2 });
		final Sneer1024 hash3 = my(Crypto.class).digest(new byte[] { 3 });

		checking(new Expectations(){{
			oneOf(_seals).ownSeal(); will(returnValue(newSeal(1)));
			oneOf(_seals).ownSeal(); will(returnValue(newSeal(2)));
			exactly(1).of(_fileClient).startFileDownload(new File(peerTracksFolder(), "ok.mp3"), 41, hash1);
			allowing(_seals).ownSeal();
		}});

		my(Wusic.class).allowTracksDownload(true);
		my(Wusic.class).tracksDownloadAllowanceSetter().consume(1);

		_subject.setActive(true);

		assertNumberOfDownloadedTracksEquals(0);
		aquireEndorsementTuple(hash1, 41, "songs/subfolder/ok.mp3");
		my(SignalUtils.class).waitForValue(_subject.numberOfDownloadedTracks(), 1);
		_subject.decrementDownloadedTracks();
		my(SignalUtils.class).waitForValue(_subject.numberOfDownloadedTracks(), 0);

		my(Wusic.class).allowTracksDownload(false);
		aquireEndorsementTuple(hash2, 42, "songs/subfolder/notOk1.mp3");

		my(Wusic.class).allowTracksDownload(true);
		useUpAllowance();
		aquireEndorsementTuple(hash3, 43, "songs/subfolder/notOk2.mp3");
	}

	private void useUpAllowance() throws IOException {
		final File fileWith1MB = createTmpFileWithRandomContent(1048576); // Optimize: change things so that we do not have to create so big a file
		my(IO.class).files().copyFileToFolder(fileWith1MB, peerTracksFolder());
	}

	private File peerTracksFolder() {
		return new File(my(FolderConfig.class).tmpFolderFor(TracksFolderKeeper.class), "peertracks");
	}

	private void aquireEndorsementTuple(final Sneer1024 hash1, int lastModified, String track) {
		my(TupleSpace.class).acquire(new TrackEndorsement(track, lastModified, hash1));
		my(TupleSpace.class).waitForAllDispatchingToFinish();
	}

	private Seal newSeal(int b) {
		return new Seal(new ImmutableByteArray(new byte[] { (byte) b }));
	}

	private void assertNumberOfDownloadedTracksEquals(int actual) {
		assertTrue(my(TrackDownloader.class).numberOfDownloadedTracks().currentValue() == actual);
	}

}

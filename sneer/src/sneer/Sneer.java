package sneer;

import static sneer.SneerDirectories.logDirectory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;

import org.prevayler.Prevayler;
import org.prevayler.PrevaylerFactory;

import prevayler.bubble.Bubble;
import sneer.kernel.appmanager.SovereignApplicationUID;
import sneer.kernel.business.BusinessSource;
import sneer.kernel.business.impl.BusinessFactory;
import sneer.kernel.communication.impl.Communicator;
import sneer.kernel.communication.msn.impl.MsnCommunicator;
import sneer.kernel.gui.Gui;
import sneer.kernel.gui.contacts.ActionFactory;
import wheel.i18n.Language;
import wheel.io.Log;
import wheel.io.network.OldNetworkImpl;
import wheel.io.ui.User;
import wheel.io.ui.User.Notification;
import wheel.io.ui.impl.JOptionPaneUser;
import wheel.lang.Omnivore;
import wheel.lang.Pair;
import wheel.lang.Threads;
import wheel.lang.exceptions.IllegalParameter;
import wheel.reactive.impl.SourceImpl;

public class Sneer {

	public static void main(String args[]) throws Exception {
		new Sneer();
	}
	
	public Sneer() throws Exception{
		try {
			
			tryToRun();
			 
		} catch (Throwable throwable) {
			Log.log(throwable);
			showExitMessage(throwable);
			System.exit(-1);
		}
	}
	
	private User _user;
	private BusinessSource _businessSource;
	private Communicator _communicator;
	private MsnCommunicator _msnCommunicator;
	private Gui _gui;
	private ActionFactory _actionFactory;
	private SystemApplications _systemApplications;
	
	private void tryToRun() throws Exception {
		
		tryToRedirectLogToSneerLogFile();

		Prevayler prevayler = prevaylerFor(new BusinessFactory().createBusinessSource());
		_businessSource = Bubble.wrapStateMachine(prevayler);

		_user = new JOptionPaneUser("Sneer", briefNotifier());
		
		initLanguage();
		
		//Optimize: Separate thread to close splash screen.
		try{Thread.sleep(2000);}catch(InterruptedException ie){}
		
		prepareBusinessForCommunication();
		
		_communicator = new Communicator(_user, new OldNetworkImpl(), _businessSource.output(), _businessSource.contactManager());
		SourceImpl<Pair<String, Boolean>> contactOnlineOnMsnEvents = new SourceImpl<Pair<String, Boolean>>(null);
		_msnCommunicator = new MsnCommunicator(_user, _businessSource.output().msnAddress(), contactOnlineOnMsnEvents.setter());
		
		_systemApplications = new SystemApplications(_user, _communicator, _businessSource, briefNotifier(), _msnCommunicator.isOnline(), contactOnlineOnMsnEvents.output());
		
		System.out.println("Checking existing apps:");
		
		for(SovereignApplicationUID app:_systemApplications._appManager.publishedApps().output())
			System.out.println("App : "+app._sovereignApplication.defaultName());
		
		_actionFactory = new ActionFactory(_systemApplications);
		
		_gui = new Gui(_user, _systemApplications, _businessSource, _actionFactory); //Implement:  start the gui before having the BusinessSource ready. Use a callback to get the BusinessSource.
		
		while (true) Threads.sleepWithoutInterruptions(100000); // Refactor Consider joining the main gui thread.
	}

	private void prepareBusinessForCommunication() {
		int sneerPort = _businessSource.output().sneerPort().currentValue();
		if (sneerPort == 0) initSneerPort();

		String ownPublicKey = _businessSource.output().publicKey().currentValue();
		if (ownPublicKey.isEmpty()) initPublicKey();
	}

	private void initPublicKey() {
		String ownPK = "" + System.currentTimeMillis() + "/" + System.nanoTime();
		_businessSource.publicKeySetter().consume(ownPK);
	}

	private void initSneerPort() {
		int randomPort = 10000 + new Random().nextInt(50000);
		try {
			_businessSource.sneerPortSetter().consume(randomPort);
		} catch (IllegalParameter e) {
			throw new IllegalStateException();
		}
	}

	private Omnivore<Notification> briefNotifier() {
		return new Omnivore<Notification>() { @Override public void consume(Notification notification) {
			_gui.briefNotifier().consume(notification);
		}};
	}

	private void initLanguage() {
		String current = System.getProperty("sneer.language");
		if (current == null || current.isEmpty()) current = "en";
		
		String chosen = _businessSource.output().language().currentValue();
		if (chosen == null || chosen.isEmpty()) {
			_businessSource.languageSetter().consume(current);
			chosen = current;
		} 
		
		if (chosen.equals("en"))
			Language.reset();
		else
			Language.load(chosen);
	}

	private void tryToRedirectLogToSneerLogFile() throws FileNotFoundException {
		logDirectory().mkdir();
		Log.redirectTo(new File(logDirectory(), "log.txt"));
	}

	
	private void showExitMessage(Throwable t) {
		String description = " " + t.getLocalizedMessage() + "\n\n Sneer will now exit.";

		try {
			_user.acknowledgeUnexpectedProblem(description);
		} catch (RuntimeException ignoreHeadlessExceptionForExample) {}
	}

	private Prevayler prevaylerFor(Object rootObject) throws Exception {
		PrevaylerFactory factory = new PrevaylerFactory();
		factory.configureTransactionFiltering(false);
		factory.configurePrevalentSystem(rootObject);
		factory.configurePrevalenceDirectory(SneerDirectories.prevalenceDirectory().getAbsolutePath());
		return factory.create();
	}

}

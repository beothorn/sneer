package sneer.hardware.logging.gui.impl;

import static sneer.commons.environments.Environments.my;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import sneer.hardware.cpu.lang.Consumer;
import sneer.hardware.gui.Action;
import sneer.hardware.logging.gui.LogConsole;
import sneer.pulp.logging.Logger;
import sneer.pulp.reactive.Signals;
import sneer.skin.main.menu.MainMenu;
import sneer.skin.windowboundssetter.WindowBoundsSetter;

class LogConsoleImpl extends JFrame implements LogConsole {

	private final MainMenu _mainMenu = my(MainMenu.class);	

	private boolean _isInitialized = false;

	LogConsoleImpl(){
		addMenuAction();
	}

	private void addMenuAction() {
		Action cmd = new Action(){
			@Override public String caption() {	return "Open Log Console"; }
			@Override public void run() { open(); }
		};
		_mainMenu.getSneerMenu().addAction(cmd);
	}

	private void open() {
		if(!_isInitialized ) initGUI();
		setVisible(true);
	}

	private void initGUI() {
		JScrollPane scroll = new JScrollPane();
		Logger logger = my(Logger.class);

		final JTextArea txtLog = new JTextArea();
		my(Signals.class).receive(this, new Consumer<String>() { @Override public void consume(String value) {
			txtLog.append(value);
		}}, logger.loggedMessages());

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(scroll, BorderLayout.CENTER);
		scroll.getViewport().add(txtLog);
		setSize(new Dimension(400,300));
		my(WindowBoundsSetter.class).setBestBounds(this);
	}
}
package sneer.bricks.snapps.games.go.tests.logic;

import sneer.bricks.hardware.cpu.lang.contracts.WeakContract;
import sneer.bricks.snapps.games.go.impl.Player;
import sneer.bricks.snapps.games.go.impl.TimerFactory;
import sneer.bricks.snapps.games.go.impl.gui.GuiPlayer;
import sneer.bricks.snapps.games.go.impl.logic.GoBoard.StoneColor;
import sneer.bricks.snapps.games.go.impl.logic.Move;


public class RunGoWithoutSneer implements Player {
	
	private GuiPlayer _blackFrame;
	private GuiPlayer _whiteFrame;

	public static void main(String[] args) {
		new RunGoWithoutSneer();
		
		
	}

	public RunGoWithoutSneer() {
		TimerFactory timerFactory = new TimerFactory() {
			@Override
			public WeakContract wakeUpEvery(final int interval, final Runnable scroller) {
				new Thread(){
					@Override
					public void run() {
						while(true){
							try {
								scroller.run();
								Thread.sleep(interval);
							} catch (InterruptedException e) {
								throw new basis.lang.exceptions.NotImplementedYet(e); // Fix Handle this exception.
							}
						}
					};
				}.start();
				return null;
			}
		};
		_blackFrame = new GuiPlayer(StoneColor.BLACK, 0, timerFactory);
		_whiteFrame = new GuiPlayer(StoneColor.WHITE, 0, timerFactory);
		_blackFrame.setAdversary(this);
		_whiteFrame.setAdversary(this);
	}
	
	@Override
	public void play(Move move) {
		_blackFrame.play(move);
		_whiteFrame.play(move);
	}

	@Override
	public void setAdversary(Player player) {
	}

}

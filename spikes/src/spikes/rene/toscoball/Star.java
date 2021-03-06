package spikes.rene.toscoball;
//visual effect
//Star(MediaTracker)

import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;


public class Star {

	int x,y,sub=0;
	private static final Image[] starspr=new Image[] {
		M.img("star5.png"), M.img("star4.png"),
		M.img("star4.png"), M.img("star3.png"),
		M.img("star2.png"), M.img("star2.png"),
		M.img("star1.png"), M.img("star0.png"),
		M.img("star0.png")};
	public boolean visible=false;

	public Star(MediaTracker t) {
		t.addImage(starspr[0], 0); t.addImage(starspr[1], 1);
		t.addImage(starspr[3], 2); t.addImage(starspr[4], 3);
		t.addImage(starspr[6], 4); t.addImage(starspr[7], 5);
	}
	
	public void draw(Graphics g) {
		if (!visible) return;
		g.drawImage(starspr[sub],x,y,null);
		sub++; if (sub==9) visible=false;
	}
	
	public void shine(int i, int j) {
		x=i-15;
		y=j-15;
		sub=0;
		visible=true;
	}
}

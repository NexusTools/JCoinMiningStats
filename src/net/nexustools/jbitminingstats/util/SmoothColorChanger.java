package net.nexustools.jbitminingstats.util;

import android.graphics.Color;

/**
 * 
 * @author Ktaeyln (Modified by Steve4448 to be used towards colors.)
 */
public class SmoothColorChanger {
	private double red, green, blue, targetRed, targetGreen, targetBlue, variance;
	private final Object lock = new Object();
	
	public SmoothColorChanger(int red, int green, int blue, int targetRed, int targetGreen, int targetBlue, int variance) {
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.targetRed = targetRed;
		this.targetGreen = targetGreen;
		this.targetBlue = targetBlue;
		this.variance = variance;
	}
	
	public void setTo(int targetRed, int targetGreen, int targetBlue) {
		synchronized(lock) {
			this.targetRed = targetRed;
			this.targetGreen = targetGreen;
			this.targetBlue = targetBlue;
		}
	}
	
	public int getTarget() {
		synchronized(lock) {
			return Color.rgb((int) targetRed, (int) targetGreen, (int) targetBlue);
		}
	}
	
	public int value() {
		synchronized(lock) {
			return Color.rgb((int) red, (int) green, (int) blue);
		}
	}
	
	public void process() {
		synchronized(lock) {
			if(targetRed > red)
				red += (targetRed - red + (float) (variance / 10f)) / variance;
			else
				red += -(red - targetRed + (float) (variance / 10f)) / variance;
			if(Math.round(red) == Math.round(targetRed))
				red = targetRed;
			
			if(targetGreen > green)
				green += (targetGreen - green + (float) (variance / 10f)) / variance;
			else
				green += -(green - targetGreen + (float) (variance / 10f)) / variance;
			if(Math.round(green) == Math.round(targetGreen))
				green = targetGreen;
			
			if(targetBlue > blue)
				blue += (targetBlue - blue + (float) (variance / 10f)) / variance;
			else
				blue += -(blue - targetBlue + (float) (variance / 10f)) / variance;
			if(Math.round(blue) == Math.round(targetBlue))
				blue = targetBlue;
		}
	}
	
	public boolean done() {
		synchronized(lock) {
			return ((int) red == (int) targetRed) && ((int) green == (int) targetGreen) && ((int) blue == (int) targetBlue);
		}
	}
	
	public void set(int newRed, int newGreen, int newBlue) {
		synchronized(lock) {
			this.red = newRed;
			this.green = newGreen;
			this.blue = newBlue;
		}
	}
	
}

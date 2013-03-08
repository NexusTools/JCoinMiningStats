package com.steve4448.jbitminingstats;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class NumberVal extends TextView {
	private double val = 0;
	private String formatting;
	private String affix = "";
	public SmoothColorChanger curColor = new SmoothColorChanger(0, 0, 0, 0, 0, 0, 6);
	public NumberVal(Context context) {super(context);}
	public NumberVal(Context context, AttributeSet set) {super(context, set);}
	public NumberVal(Context context, AttributeSet set, int defStyle) {super(context, set, defStyle);}
	private Thread transition;
	
	public void setValue(double val) {
		if(this.val > val)
			curColor.set(255, 0, 0);
		else if(this.val < val)
			curColor.set(0, 255, 0);
		setBackgroundColor(curColor.value());
		this.val = val;
		if(transition != null && transition.isAlive())
			transition.interrupt();
		transition = new Thread() {
			@Override
			public void run() {
				try {
				while(!curColor.done()) {
					MiningStatisticsActivity.handler.post(new Runnable() {
						@Override
						public void run() {
							setBackgroundColor(curColor.value());
						}
					});
					curColor.process();
					Thread.sleep(1000/20);
				}
				} catch(InterruptedException e) {}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		};
		transition.start();
		formText();
	}
	
	public void formText() {
		setText((formatting == null ? Double.toString(val) : String.format(formatting, val) + affix));
	}
	
	public double getValue() {
		return val;
	}
	
	public void setFormatting(String formatting) {
		this.formatting = formatting;
		formText();
	}
	
	public String getFormatting() {
		return formatting;
	}
	
	public void setAffix(String affix) {
		this.affix = affix;
		formText();
	}
	
	public String getAffix() {
		return affix;
	}
}

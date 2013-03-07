package com.steve4448.jbitminingstats;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.TextView;

public class NumberVal extends TextView {
	private double val = 0;
	private String formatting;
	public SmoothInteger curColorRed = new SmoothInteger(0, 6);
	public SmoothInteger curColorGreen = new SmoothInteger(0, 6);
	public SmoothInteger curColorBlue = new SmoothInteger(0, 6);
	public NumberVal(Context context) {super(context);}
	public NumberVal(Context context, AttributeSet set) {super(context, set);}
	public NumberVal(Context context, AttributeSet set, int defStyle) {super(context, set, defStyle);}
	private Thread transition;
	
	public void setValue(double val) {
		if(this.val > val)
			curColorRed.set(255);
		else if(this.val < val)
			curColorGreen.set(255);
		setBackgroundColor(Color.rgb(curColorRed.value(), curColorGreen.value(), curColorBlue.value()));
		this.val = val;
		transition = new Thread() {
			@Override
			public void run() {
				try {
				while(!(curColorRed.done() && curColorGreen.done() && curColorBlue.done())) {
					MiningStatisticsActivity.handler.post(new Runnable() {
						@Override
						public void run() {
							setBackgroundColor(Color.rgb(curColorRed.value(), curColorGreen.value(), curColorBlue.value()));
						}
					});
					curColorRed.process();
					curColorGreen.process();
					curColorBlue.process();
					Thread.sleep(1000/30);
				}
				} catch(InterruptedException e) {}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		};
		transition.start();
		setText(formatting == null ? Double.toString(val) : String.format(formatting, val));
	}
	
	public double getValue() {
		return val;
	}
	
	public void setFormatting(String formatting) {
		this.formatting = formatting;
	}
	
	public String getFormatting() {
		return formatting;
	}
}

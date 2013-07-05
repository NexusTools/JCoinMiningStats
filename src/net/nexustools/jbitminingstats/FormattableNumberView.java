package net.nexustools.jbitminingstats;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class FormattableNumberView extends TextView {
	private double val = 0;
	private String formatting;
	private String affix = "";
	public SmoothColorChanger curColor = new SmoothColorChanger(0, 0, 0, 0, 0, 0, 6);
	
	public FormattableNumberView(Context context) {
		super(context);
		setValue(0, false);
	}
	
	public FormattableNumberView(Context context, AttributeSet set) {
		super(context, set);
		setValue(0, false);
	}
	
	public FormattableNumberView(Context context, AttributeSet set, int defStyle) {
		super(context, set, defStyle);
		setValue(0, false);
	}
	
	private Thread transition;
	
	public void setValue(double val) {
		setValue(val, true);
	}
	
	public void setValue(double val, boolean doTransition) {
		if(doTransition) {
			if(this.val > val)
				curColor.set(255, 0, 0);
			else if(this.val < val)
				curColor.set(0, 255, 0);
			setBackgroundColor(curColor.value());
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
							Thread.sleep(1000 / 20);
						}
					} catch(InterruptedException e) {} catch(Exception e) {
						e.printStackTrace();
					}
				}
			};
			transition.start();
		}
		
		this.val = val;
		formText();
	}
	
	public void formText() {
		setText((formatting == null ? Double.toString(val) : String.format(formatting, val)) + affix);
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

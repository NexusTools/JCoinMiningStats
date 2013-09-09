package net.nexustools.jbitminingstats.view;

import net.nexustools.jbitminingstats.activity.MiningStatisticsActivity;
import net.nexustools.jbitminingstats.util.SmoothColorChanger;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextView;

public class FormattableNumberView extends TextView {
	private double val = 0;
	private double mul = 0;
	private String formatting;
	private String prefix = "";
	private String suffix = "";
	public SmoothColorChanger curColor = new SmoothColorChanger(0, 0, 0, 0, 0, 0, 0, 0, 12);
	
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
				curColor.set(255, 0, 0, 255);
			else if(this.val < val)
				curColor.set(0, 255, 0, 255);
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
	
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}
	
	public void formText() {
		setText(prefix + (formatting == null ? Double.toString(mul != 0 ? val * mul : val) : String.format(formatting, mul != 0 ? val * mul : val)) + suffix);
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
	
	public void setSuffix(String suffix) {
		this.suffix = suffix;
		formText();
	}
	
	public String getAffix() {
		return suffix;
	}
	
	public void setPrefix(String prefix) {
		this.prefix = prefix;
		formText();
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public void setMultiplier(double mul) {
		this.mul = mul;
		formText();
	}
	
	public double getMultiplier() {
		return mul;
	}
}

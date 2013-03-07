package com.steve4448.jbitminingstats;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.TextView;

public class NumberVal extends TextView {
	private double val = 0;
	private String formatting;
	public int curColor = Color.WHITE;
	public NumberVal(Context context) {super(context);}
	public NumberVal(Context context, AttributeSet set) {super(context, set);}
	public NumberVal(Context context, AttributeSet set, int defStyle) {super(context, set, defStyle);}
	
	public void setValue(double val) {
		this.val = val;
		setText(formatting == null ? Double.toString(val) : String.format(formatting, val)); //TODO: Temporary...
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

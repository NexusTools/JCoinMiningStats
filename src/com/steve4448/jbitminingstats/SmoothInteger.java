package com.steve4448.jbitminingstats;
/**
 * 
 * @author anonymous
 */
public class SmoothInteger {

	double current, to, speed = 0, tospeed = 0, variance;
	final Object lock = new Object();

	public SmoothInteger(int aa, int variance) {
		current = aa;
		to = aa;
		this.variance = variance;
	}

	public void setTo(double a) {
		synchronized(lock) {
			to = a;
		}
	}

	public double getTo() {
		synchronized(lock) {
			return to;
		}
	}

	public int value() {
		synchronized(lock) {
			return (int)current;
		}
	}

	int ospeed, ocu;

	public int process() {
		synchronized(lock) {
			ospeed = (int)speed;
			ocu = (int)current;
			if(to > current)
				speed = (to - current + (float)(variance / 10f)) / variance;
			else
				speed = -(current - to + (float)(variance / 10f)) / variance;
			current += speed;
			if(Math.round(speed) == 0 && Math.round(current) == Math.round(to)) {
				current = to;
				speed = 0;
			}
			return (int)current;
		}
	}

	public boolean done() {
		synchronized(lock) {
			return (int)speed == (int)tospeed && (int)to == (int)current;
		}
	}

	void set(double nu) {
		synchronized(lock) {
			current = nu;
		}
	}

}

package com.steve4448.jbitminingstats;

public class MiningWorkerStub {
	String name;
	boolean online;
	double hashRate;
	double shares;
	public MiningWorkerStub(String name, boolean online, double hashRate, double shares) {
		this.name = name;
		this.online = online;
		this.hashRate = hashRate;
		this.shares = shares;
	}
}

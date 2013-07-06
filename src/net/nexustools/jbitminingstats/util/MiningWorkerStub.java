package net.nexustools.jbitminingstats.util;

public class MiningWorkerStub {
	public String name;
	public boolean online;
	public double hashRate;
	public double share;
	public double score;
	
	public MiningWorkerStub(String name, boolean online, double hashRate, double share, double score) {
		this.name = name;
		this.online = online;
		this.hashRate = hashRate;
		this.share = share;
		this.score = score;
	}
}

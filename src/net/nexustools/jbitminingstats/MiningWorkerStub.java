package net.nexustools.jbitminingstats;

public class MiningWorkerStub {
	String name;
	boolean online;
	double hashRate;
	double share;
	double score;
	
	public MiningWorkerStub(String name, boolean online, double hashRate, double share, double score) {
		this.name = name;
		this.online = online;
		this.hashRate = hashRate;
		this.share = share;
		this.score = score;
	}
}

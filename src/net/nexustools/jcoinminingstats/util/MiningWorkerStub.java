package net.nexustools.jcoinminingstats.util;

public class MiningWorkerStub {
	public String name;
	public boolean online;
	public int hashRate;
	public int share;
	public double score;
	
	public MiningWorkerStub(String name, boolean online, int hashRate, int share, double score) {
		this.name = name;
		this.online = online;
		this.hashRate = hashRate;
		this.share = share;
		this.score = score;
	}
}

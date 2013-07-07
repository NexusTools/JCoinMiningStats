package net.nexustools.jbitminingstats.util;

public class BlockStub {
	public String id;
	public int confirmations;
	public double reward;
	public double nmcReward;
	public double score;
	public double share;
	
	public BlockStub(String id, int confirmations, double reward, double nmcReward, double score, double share) {
		this.id = id;
		this.confirmations = confirmations;
		this.reward = reward;
		this.nmcReward = nmcReward;
		this.score = score;
		this.share = share;
	}
}

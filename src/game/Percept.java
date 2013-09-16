package game;

public class Percept {
	private int turnNo;
	private int agentNo;
	private AgentState agentState;
	
	public Percept(int turnNo, int agentNo, AgentState agentState) {
		this.turnNo = turnNo;
		this.agentNo = agentNo;
		this.agentState = agentState;
	}
	
	public int getTurnNo() {
		return turnNo;
	}

	public int getAgentNo() {
		return agentNo;
	}
	
	public AgentState getAgentState() {
		return agentState;
	}
}

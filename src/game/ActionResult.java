package game;

public class ActionResult {
	private Action action;
	private AgentState newState;
	private double reward;
	
	public ActionResult(Action action, AgentState newState, double reward) {
		this.action = action;
		this.newState = newState;
		this.reward = reward;
	}

	public Action getAction() {
		return action;
	}

	public AgentState getNewState() {
		return newState;
	}

	public double getReward() {
		return reward;
	}
}

package game;

/**
 * Represents an attempted action,
 * and the actual result of executing this action.
 * 
 * @author lackofcheese
 *
 */
public class ActionResult {
	/** The action attempted. */
	private Action action;
	/** The resulting new state. */
	private AgentState newState;
	/** The reward. */
	private double reward;
	
	/**
	 * Constructs an action result.
	 * @param action the action.
	 * @param newState the resulting state.
	 * @param reward the reward.
	 */
	public ActionResult(Action action, AgentState newState, double reward) {
		this.action = action;
		this.newState = newState;
		this.reward = reward;
	}

	/**
	 * Returns the attempted action.
	 * @return the attempted action.
	 */
	public Action getAction() {
		return action;
	}

	/**
	 * Returns the resulting state.
	 * @return the resulting state.
	 */
	public AgentState getNewState() {
		return newState;
	}

	/**
	 * Returns the reward.
	 * @return the reward.
	 */
	public double getReward() {
		return reward;
	}
}

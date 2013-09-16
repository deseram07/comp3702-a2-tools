package target;

import game.Action;
import game.ActionResult;
import game.Agent;
import game.AgentState;
import game.Percept;
import geom.Grid;

import java.util.List;

/**
 * Represents a target with non-deterministic motion.
 * 
 * A2 will be tested with similar targets, but definitely not with the same
 * numbers used here!
 * 
 * @author lackofcheese
 * 
 */
public class TargetWithError implements Agent {
	/** The policy of this target. */
	private TargetPolicy policy;

	/** The angular error offsets (as multiples of PI/4) */
	private int[] offsets = new int[] { 0, 1, -1, 2, -2, 3, -3, 4 };
	/** The chance associated with each offset. */
	private double[] chances = new double[] { 0.5, 0.2, 0.1, 0.08, 0.03, 0.05,
			0.02, 100 };

	/**
	 * Returns a random offset as per the distribution (offsets/chances).
	 * 
	 * @return a random offset as per the distribution (offsets/chances).
	 */
	private double randomOffset() {
		double r = Math.random();
		int index = 0;
		double total = chances[0];
		while (total < r) {
			index += 1;
			total += chances[index];
		}
		return offsets[index] * Math.PI / 4;
	}

	/**
	 * Returns a new non-deterministic target with the given policy.
	 * 
	 * @param policy
	 *            the policy to use.
	 */
	public TargetWithError(TargetPolicy policy) {
		this.policy = policy;
	}

	@Override
	public Action getAction(int turnNo, ActionResult previousResult,
			double[] scores, List<Percept> newPercepts) {
		Grid grid = policy.getGrid();
		AgentState currentState = previousResult.getNewState();
		Grid.GridCell startIndex = grid.getIndex(currentState.getPosition());
		Grid.GridCell desiredIndex = policy.getNextIndex(startIndex);
		if (desiredIndex.equals(startIndex)) {
			return new Action(currentState);
		}
		int actionCode = grid.encodeAction(startIndex, desiredIndex);
		double heading = grid.getHeading(actionCode);
		double newHeading = heading + randomOffset();
		int newActionCode = grid.getCodeFromHeading(newHeading);
		Grid.GridCell endIndex = grid.decodeAction(startIndex, newActionCode);
		return new Action(currentState, grid.getCentre(endIndex));
	}

	/**
	 * Returns the policy used by this target.
	 * 
	 * @return the policy used by this target.
	 */
	public TargetPolicy getPolicy() {
		return policy;
	}
}

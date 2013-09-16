package target;

import game.Action;
import game.ActionResult;
import game.Agent;
import game.AgentState;
import game.Percept;
import geom.Grid;

import java.util.List;

public class TargetWithError implements Agent {
	private TargetPolicy targetPolicy;
	
	private int[] offsets = new int[] {
			0, 
			1, -1, 
			2, -2,
			3, -3,
			4};
	private double[] chances = new double[] {
			0.5,
			0.2, 0.1,
			0.08, 0.03,
			0.05, 0.02,
			100};
	
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
	
	public TargetWithError(TargetPolicy targetPolicy) {
		this.targetPolicy = targetPolicy;
	}
	
	public TargetWithError(Target target) {
	}

	@Override
	public Action getAction(int turnNo, ActionResult previousResult, 
			double[] scores, List<Percept> newPercepts) {
		Grid grid = targetPolicy.getGrid();
		AgentState currentState = previousResult.getNewState();
		Grid.GridCell startIndex = grid.getIndex(currentState.getPosition());
		Grid.GridCell desiredIndex = targetPolicy.getNextIndex(startIndex);
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
}

package target;

import game.Action;
import game.ActionResult;
import game.Agent;
import game.Percept;

import java.util.List;

public class TargetWithError implements Agent {
	private Target target;
	
	public TargetWithError(TargetPolicy targetPolicy) {
		this(new Target(targetPolicy));
	}
	
	public TargetWithError(Target target) {
		this.target = target;
	}

	@Override
	public Action getAction(int turnNo, ActionResult previousResult, 
			double[] scores, List<Percept> newPercepts) {
		Action desiredAction = target.getAction(turnNo, previousResult, 
				scores, newPercepts);
		return desiredAction;
	}
}

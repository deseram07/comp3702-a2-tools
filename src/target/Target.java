package target;

import game.Action;
import game.ActionResult;
import game.Agent;
import game.Percept;

import java.util.List;

public class Target implements Agent {
	private TargetPolicy policy;
	
	public Target(TargetPolicy policy) {
		this.policy = policy;
	}

	@Override
	public Action getAction(int turnNo,  ActionResult previousResult, 
			double[] scores, List<Percept> newPercepts) {
		return policy.getAction(previousResult.getNewState());
	}

	public TargetPolicy getPolicy() {
		return policy;
	}
}

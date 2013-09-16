package game;

import java.util.List;

/**
 * Represents an agent (tracker or target) within the game. 
 * 
 * @author lackofcheese
 *
 */
public interface Agent {
	public Action getAction(int turnNo, ActionResult previousResult, 
			double[] scores, List<Percept> newPercepts);
}

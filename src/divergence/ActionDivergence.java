package divergence;

import game.Action;

/**
 * Represents the divergence between the action an agent desires to make
 * and the actual movement that occurs in the workspace.
 * 
 * @author lackofcheese
 *
 */
public interface ActionDivergence {
	/**
	 * Returns an action with divergence applied to it.
	 * @param action the attempted action.
	 * @return an action with divergence applied to it.
	 */
	public Action divergeAction(Action action);
}

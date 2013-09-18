package divergence;

import game.Action;

/**
 * Diverges an action.
 * 
 * @author lackofcheese
 * 
 */
public interface ActionDivergence {
	/**
	 * Returns a diverged action.
	 * 
	 * @param action
	 *            the action to modify.
	 * @return a diverged action.
	 */
	public Action divergeAction(Action action);
}

package divergence;

import game.Action;

/**
 * Represents no divergence at all - the diverged action will
 * always be the action that was attempted.
 * 
 * @author lackofcheese
 *
 */
public class ZeroDivergence implements ActionDivergence {
	@Override
	public Action divergeAction(Action action) {
		return action;
	}
}

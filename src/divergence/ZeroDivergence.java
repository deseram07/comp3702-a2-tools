package divergence;

import game.Action;
import game.TrackerAction;

/**
 * Represents no divergence at all - the diverged action will always be the
 * action that was attempted.
 * 
 * Also fixes the action to ensure that the distance travelled is equal to the
 * allowed value.
 * 
 * @author lackofcheese
 * 
 */
public class ZeroDivergence implements ActionDivergence {
	/** The allowed distance value. */
	private double stepDistance;

	/**
	 * Constructs a ZeroDivergence with the given standard distance to travel.
	 * 
	 * @param stepDistance
	 *            the standard move distance.
	 */
	public ZeroDivergence(double stepDistance) {
		this.stepDistance = stepDistance;
	}

	@Override
	public TrackerAction divergeAction(Action action) {
		TrackerAction trackerAction = (TrackerAction) action;
		double distance = trackerAction.getDistance();
		if (!trackerAction.isMovement() || distance == 0
				|| distance == stepDistance) {
			return trackerAction;
		}
		// Fix the distance to to its correct value.
		return new TrackerAction(trackerAction.getStartState(),
				trackerAction.getHeading(), stepDistance);
	}
}

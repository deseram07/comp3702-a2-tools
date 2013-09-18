package divergence;

import game.Action;
import game.SensingParameters;
import game.TrackerAction;

/**
 * Corrects actions, in order to fix 
 * Represents no divergence at all - the diverged action will always be the
 * action that was attempted.
 * 
 * Also fixes the action to ensure that the distance travelled is equal to the
 * allowed value.
 * 
 * @author lackofcheese
 * 
 */
public class ActionCorrector implements ActionDivergence {
	/** The allowed distance value. */
	private double stepDistance;
	/** The tracker's sensing parameters */
	private SensingParameters sp;

	/**
	 * Constructs a ZeroDivergence with the given standard distance to travel.
	 * 
	 * @param stepDistance
	 *            the standard move distance.
	 */
	public ActionCorrector(double stepDistance, SensingParameters sp) {
		this.stepDistance = stepDistance;
	}

	@Override
	public TrackerAction divergeAction(Action action) {
		TrackerAction trackerAction = (TrackerAction) action;
		double distance = trackerAction.getDistance();
		if (trackerAction.isCameraAdjustment()) {
			double armLength = trackerAction.getResultingState().getCameraArmLength();
			if (armLength < sp.getMinLength()) {
				return new TrackerAction(trackerAction.getStartState(), sp.getMinLength());
			} else if (armLength > sp.getMaxLength()) {
				return new TrackerAction(trackerAction.getStartState(), sp.getMaxLength());
			}
		}
		
		if (!trackerAction.isMovement() || distance == 0
				|| distance == stepDistance) {
			return trackerAction;
		}
		// Fix the distance to to its correct value.
		return new TrackerAction(trackerAction.getStartState(),
				trackerAction.getHeading(), stepDistance);
	}
}

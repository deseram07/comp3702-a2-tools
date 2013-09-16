package game;

import geom.Vector2D;

import java.awt.geom.Point2D;

/**
 * Represents an action taken by one of the agents in the game.
 * 
 * @author lackofcheese
 *
 */
public class Action {
	/** True iff this action consists of calling HQ; this requires the agent
	 * to stand in place */
	private boolean callingHQ;
	
	/** The new heading for the agent, in radians. */
	private double heading;
	/** The distance to travel. */
	private double distance;
	/** The new camera arm length. */
	private double newCameraArmLength;
	
	/** The initial state. */
	private AgentState startState;
	/** The expected resulting state after this action. */
	private AgentState resultingState;
	
	/**
	 * The simplest action - stay still and do nothing.
	 * @param startState the starting state.
	 */
	public Action(AgentState startState) {
		this(startState, false);
	}
	
	/**
	 * A stationary action - either do nothing, or call HQ.
	 * @param startState the starting state.
	 * @param callingHQ true iff this is an HQ call.
	 */
	public Action(AgentState startState, boolean callingHQ) {
		this.startState = startState;
		this.callingHQ = callingHQ;
		this.heading = startState.getHeading();
		this.distance = 0;
		this.newCameraArmLength = startState.getCameraArmLength();
		
		this.resultingState = startState;
	}
	
	/**
	 * A heading change action - remain in the same place, but change heading.
	 * @param startState the starting state.
	 * @param heading the new heading.
	 */
	public Action(AgentState startState, double heading) {
		this(startState, heading, 0);
	}
	
	/**
	 * A movement with the given heading and distance.
	 * @param startState the starting state.
	 * @param heading the new heading.
	 * @param distance the distance to travel.
	 */
	public Action(AgentState startState, double heading, double distance) {
		this(startState, heading, distance, startState.getCameraArmLength());
	}
	
	/**
	 * A movement with the given heading and distance, in addition to a change of the camera arm
	 * length to the given value. 
	 * @param startState the starting state.
	 * @param heading the new heading.
	 * @param distance the distance to travel.
	 * @param newCameraArmLength the new camera arm length.
	 */
	public Action(AgentState startState, double heading, double distance, double newCameraArmLength) {
		this.startState = startState;
		this.callingHQ = false;
		this.heading = heading;
		this.distance = distance;
		this.newCameraArmLength = newCameraArmLength;
		
		Point2D startPos = startState.getPosition();
		Point2D endPos;
		if (distance == 0) {
			endPos = startPos;
		} else {
			endPos = new Vector2D(distance, heading).addedTo(startPos);
		}
		this.resultingState = new AgentState(endPos, heading, startState.hasCamera(), newCameraArmLength);
	}
	
	/**
	 * A movement towards the given desired position.
	 * @param startState the starting state.
	 * @param desiredPos the position to travel towards.
	 */
	public Action(AgentState startState, Point2D desiredPos) {
		this(startState, desiredPos, startState.getCameraArmLength());
	}
	
	/**
	 * A movement towards the given desired position, combined with a change of camera length to the given
	 * value.
	 * @param startState the starting state.
	 * @param desiredPos the position to travel towards.
	 * @param newCameraArmLength the camera arm length to .
	 */
	public Action(AgentState startState, Point2D desiredPos, double newCameraArmLength) {
		this.startState = startState;
		this.callingHQ = false;
		this.newCameraArmLength = newCameraArmLength;
		
		Point2D startPos = startState.getPosition();
		if (startPos.equals(desiredPos)) {
			this.heading = startState.getHeading();
			this.distance = 0;
		} else {
			Vector2D motion = new Vector2D(startPos, desiredPos);
			this.heading = motion.getDirection();
			this.distance = motion.getMagnitude();
		}
		this.resultingState = new AgentState(desiredPos, heading, startState.hasCamera(), newCameraArmLength);
	}

	
	/**
	 * Returns true iff this action is an HQ call.
	 * @return true iff this action is an HQ call.
	 */
	public boolean isCallingHQ() {
		return callingHQ;
	}

	/**
	 * Returns the heading taken for this action.
	 * @return the heading taken for this action.
	 */
	public double getHeading() {
		return heading;
	}

	/**
	 * Returns the distance travelled for this action.
	 * @return the distance travelled for this action.
	 */
	public double getDistance() {
		return distance;
	}
	
	/**
	 * Returns the new camera arm length after this action.
	 * @return the new camera arm length after this action.
	 */
	public double getNewCameraArmLength() {
		return newCameraArmLength;
	}

	/**
	 * Returns the initial state before this action.
	 * @return the initial state before this action.
	 */
	public AgentState getStartState() {
		return startState;
	}

	/**
	 * Returns the resulting state after this action.
	 * @return the resulting state after this action.
	 */
	public AgentState getResultingState() {
		return resultingState;
	}
}

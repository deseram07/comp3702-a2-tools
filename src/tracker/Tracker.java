package tracker;

import game.Action;
import game.ActionResult;
import game.Agent;
import game.AgentState;
import game.Percept;
import game.RectRegion;
import game.SensingParameters;
import game.TrackerAction;

import java.util.List;

import divergence.TargetMotionHistory;
import divergence.TrackerMotionHistory;
import target.TargetPolicy;

@SuppressWarnings("unused")
public class Tracker implements Agent {
	/** The number of targets. */
	private int numTargets;
	/** The policy of the target(s). */
	private TargetPolicy targetPolicy;
	/**
	 * The motion history of the target(s), or null if no history is available.
	 * */
	private TargetMotionHistory targetMotionHistory;
	/** The sensing parameters of the target(s). */
	private SensingParameters targetSensingParams;
	/** The initial state(s) of the target(s). */
	private List<AgentState> targetInitialStates;

	/** The motion history of this tracker. */
	private TrackerMotionHistory myMotionHistory;
	/** The sensing parameters of this tracker. */
	private SensingParameters mySensingParams;
	/** The initial state of this tracker. */
	private AgentState myInitialState;

	/** The obstacles. */
	private List<RectRegion> obstacles;
	/** The goal region. */
	private RectRegion goalRegion;

	/**
	 * Constructs a tracker with the given parameters.
	 * 
	 * This gives your tracker all of the information it has about the initial
	 * state of the game, including very important aspects such as the target's
	 * policy, which is known to the tracker.
	 * 
	 * @param numTargets
	 *            the number of targets.
	 * @param targetPolicy
	 *            the policy of the target(s).
	 * @param targetMotionHistory
	 *            the motion history of the target(s), or null if no history is
	 *            available.
	 * @param targetSensingParams
	 *            the sensing parameters of the target(s).
	 * @param targetInitialStates
	 *            the initial state(s) of the target(s).
	 * @param trackerMotionHistory
	 *            the motion history of this tracker.
	 * @param trackerSensingParams
	 *            the sensing parameters of this tracker.
	 * @param trackerInitialState
	 *            the initial state of this tracker.
	 * @param obstacles
	 *            the obstacles.
	 * @param goalRegion
	 *            the goal region.
	 */
	public Tracker(int numTargets, TargetPolicy targetPolicy,
			TargetMotionHistory targetMotionHistory,
			SensingParameters targetSensingParams,
			List<AgentState> targetInitialStates,

			TrackerMotionHistory trackerMotionHistory,
			SensingParameters trackerSensingParams,
			AgentState trackerInitialState,

			List<RectRegion> obstacles, RectRegion goalRegion) {
		this.numTargets = numTargets;
		this.targetPolicy = targetPolicy;
		this.targetMotionHistory = targetMotionHistory;
		this.targetSensingParams = targetSensingParams;
		this.targetInitialStates = targetInitialStates;

		this.myMotionHistory = trackerMotionHistory;
		this.mySensingParams = trackerSensingParams;
		this.myInitialState = trackerInitialState;

		this.obstacles = obstacles;
		this.goalRegion = goalRegion;
		initialise();
	}

	/**
	 * Initialises the tracker's policy.
	 * 
	 * This handles any setup your agent requires for its policy before the game
	 * actually starts. If you don't require any setup, leave this method blank.
	 */
	public void initialise() {
		// TODO Write this method!
	}

	@Override
	/**
	 * This method is used by the game to ask your tracker for an action
	 * when it is the tracker's turn.
	 * 
	 * It also passes your tracker all of the information it is allowed
	 * to have about the changing of the state of the game. In particular,
	 * this includes the following:
	 * 
	 * @param turnNo the current turn. This is the turn number within the game;
	 * this will be 0 for your very first turn, then 2, then 4, etc. 
	 * This is always even because odd-numbered turns are taken by the targets.
	 * 
	 * @param previousResult the result of the previous attempted action. This
	 * contains three components:
	 * previousResult.getAction() - the action that resulted after random
	 * 		divergence was applied to your desired action.
	 * previousResult.getNewState() - the state this resulted in, which is
	 * 		also the current state of your tracker.
	 * previousResult.getReward() - the reward obtained for the previous
	 * action.
	 * 
	 * @param scores the scores accrued by each individual player.
	 * Note that player #0 is the tracker, and players #1, #2, etc. are
	 * targets. 
	 * In order to win the game, your score will need to be higher than
	 * the total of all of the target scores.
	 * Normally numTargets = 1, so scores will only consist of two numbers.
	 * 
	 * @param newPercepts any percepts obtained by your tracker since
	 * its last turn.
	 * For example, if it's currently turn #2, this will contain any percepts
	 * from turns #0 and #1.
	 * The percept consists of three components:
	 * percept.getAgentNo() - the agent that was seen.
	 * percept.getTurnNo() - the turn after which it was seen.
	 * percept.getAgentState() - the state the agent was seen in.
	 */
	public TrackerAction getAction(int turnNo, ActionResult previousResult,
			double[] scores, List<Percept> newPercepts) {
		AgentState myState = previousResult.getNewState();
		// TODO Write this method!
		System.out.println(newPercepts);
		double heading = targetPolicy.getAction(myState).getHeading();
		return new TrackerAction(myState, heading, 100);
	}
}

package tracker;

import game.Action;
import game.ActionResult;
import game.Agent;
import game.AgentState;
import game.Percept;
import game.RectRegion;
import game.SensingParameters;

import java.util.List;

import target.TargetMotionHistory;
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
	 */
	public void initialise() {
		/*
		 * TODO Write this method! This handles any setup your agent requires
		 * for its policy before the game actually starts. If you don't have any
		 * setup, leave this method blank.
		 */
	}

	@Override
	public Action getAction(int turnNo, ActionResult previousResult,
			double[] scores, List<Percept> newPercepts) {
		/*
		 * TODO Write this method! This is the one that defines how your agent
		 * will respond to any given situation.
		 */
		return targetPolicy.getAction(previousResult.getNewState());
	}
}

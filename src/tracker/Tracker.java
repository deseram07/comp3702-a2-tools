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
	private int numTargets;
	private TargetPolicy targetPolicy;
	private TargetMotionHistory targetMotionHistory;
	private SensingParameters targetSensingParams;
	private List<AgentState> targetInitialStates;

	private TrackerMotionHistory myMotionHistory;
	private SensingParameters mySensingParams;
	private AgentState myInitialState;

	private List<RectRegion> obstacles;
	private RectRegion goalRegion;

	public Tracker(int numTargets, 
			TargetPolicy targetPolicy,
			TargetMotionHistory targetMotionHistory,
			SensingParameters targetSensingParams,
			List<AgentState> targetInitialStates,

			TrackerMotionHistory trackerMotionHistory,
			SensingParameters trackerSensingParams,
			AgentState trackerInitialState,

			List<RectRegion> obstacles, 
			RectRegion goalRegion) {
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

	public void initialise() {
		// TODO Write this method!
	}

	@Override
	public Action getAction(int turnNo, ActionResult previousResult, double[] scores,
			List<Percept> newPercepts) {
		// TODO Write this method!
		return targetPolicy.getAction(previousResult.getNewState());
	}
}

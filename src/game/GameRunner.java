package game;

import geom.GeomTools;
import geom.Vector2D;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import divergence.ActionDivergence;
import divergence.TargetDivergence;
import divergence.TargetMotionHistory;
import divergence.TrackerMotionHistory;
import divergence.ZeroDivergence;
import target.Target;
import target.TargetPolicy;
import tracker.Tracker;

/**
 * This class runs the "Game of Spy" for assignment 2. It contains a structured
 * representation of the specifications for a given game setup, and also
 * contains the code that executes the game loop.
 * 
 * @author lackofcheese
 */
public class GameRunner {
	private double MAX_SIGHT_DISTANCE_ERROR = 1e-5;
	private int NUM_CAMERA_ARM_STEPS = 1000;

	/* ------------------------ SETUP PARAMETERS -------------------------- */

	/** True iff a game setup is currently loaded */
	private boolean setupLoaded = false;

	/** The number of targets in the game. */
	private int numTargets;
	/** The (shared) policy of target(s) in the game. */
	private TargetPolicy targetPolicy;
	/** The motion history of the target(s), or null if no such history exists. */
	private TargetMotionHistory targetMotionHistory = null;
	/** The sensing parameters of the target(s). */
	private SensingParameters targetSensingParams;
	/** The initial state(s) of the target(s). */
	private List<AgentState> targetInitialStates;

	/**
	 * The motion history of the tracker, or null if the tracker's motion is
	 * deterministic.
	 */
	private TrackerMotionHistory trackerMotionHistory = null;
	/** The sensing parameters of the tracker. */
	private SensingParameters trackerSensingParams;
	/** The initial state of the tracker. */
	private AgentState trackerInitialState;
	/** The distance the tracker can move in one step. */
	private double trackerMoveDistance;

	/** The obstacles in the game space. */
	private List<RectRegion> obstacles;
	/** The goal region for the target(s). */
	private RectRegion goalRegion;
	
	/** 
	 * The obstacles in the game space, in addition to extra
	 * obstacles representing the workspace boundaries.
	 */
	private List<RectRegion> extendedObstacles;

	/**
	 * Loads the problem setup from a text file.
	 * 
	 * @param filename
	 *            the path of the text file to load.
	 * @throws IOException
	 *             if the text file doesn't exist or doesn't meet the assignment
	 *             specifications.
	 */
	public void loadSetup(String filename) throws IOException {
		Path baseFolder = Paths.get(filename).toAbsolutePath().getParent();
		setupLoaded = false;
		BufferedReader input = new BufferedReader(new FileReader(filename));
		String line;
		int lineNo = 0;
		Scanner s;
		try {
			line = input.readLine();
			lineNo++;
			s = new Scanner(line);
			numTargets = s.nextInt();
			s.close();

			line = input.readLine();
			lineNo++;
			s = new Scanner(line);
			boolean hasTargetHistory = (s.next().equals("A2"));
			s.close();

			line = input.readLine();
			lineNo++;
			s = new Scanner(line);
			String path = baseFolder.resolve(s.next()).toString();
			targetPolicy = new TargetPolicy(path);
			trackerMoveDistance = 1.0 / targetPolicy.getGridSize();
			if (hasTargetHistory) {
				path = baseFolder.resolve(s.next()).toString();
				targetMotionHistory = new TargetMotionHistory(path);
			}
			s.close();

			line = input.readLine();
			lineNo++;
			targetSensingParams = new SensingParameters(false, line);

			line = input.readLine();
			lineNo++;
			s = new Scanner(line);
			boolean hasTrackerHistory = (s.next().equals("B2"));
			s.close();

			line = input.readLine();
			lineNo++;
			if (hasTrackerHistory) {
				s = new Scanner(line);
				path = baseFolder.resolve(s.next()).toString();
				trackerMotionHistory = new TrackerMotionHistory(path);
				s.close();
			}

			line = input.readLine();
			lineNo++;
			s = new Scanner(line);
			boolean hasCamera = (s.next().equals("C2"));
			s.close();

			line = input.readLine();
			lineNo++;
			trackerSensingParams = new SensingParameters(hasCamera, line);

			line = input.readLine();
			lineNo++;
			trackerInitialState = new AgentState(hasCamera, line);

			targetInitialStates = new ArrayList<AgentState>();
			for (int i = 0; i < numTargets; i++) {
				line = input.readLine();
				lineNo++;
				targetInitialStates.add(new AgentState(false, line));
			}

			line = input.readLine();
			lineNo++;
			goalRegion = new RectRegion(line);

			line = input.readLine();
			lineNo++;
			s = new Scanner(line);
			int numObstacles = s.nextInt();
			s.close();

			obstacles = new ArrayList<RectRegion>();
			for (int i = 0; i < numObstacles; i++) {
				line = input.readLine();
				lineNo++;
				obstacles.add(new RectRegion(line));
			}
			
			extendedObstacles = new ArrayList<RectRegion>(obstacles);
			extendedObstacles.add(new RectRegion(-1, 0, 1, 1));
			extendedObstacles.add(new RectRegion(1, 0, 1, 1));
			extendedObstacles.add(new RectRegion(0, -1, 1, 1));
			extendedObstacles.add(new RectRegion(0, 1, 1, 1));

			setupLoaded = true;
			actionResultSequence = null;
			stateSequence = null;
			cs = null;
		} catch (InputMismatchException e) {
			throw new IOException(String.format(
					"Invalid number format on line %d: %s", lineNo,
					e.getMessage()));
		} catch (NoSuchElementException e) {
			throw new IOException(String.format("Not enough tokens on line %d",
					lineNo));
		} catch (NullPointerException e) {
			throw new IOException(String.format(
					"Line %d expected, but file ended.", lineNo));
		} finally {
			input.close();
		}
	}

	/**
	 * Returns whether a setup is currently loaded.
	 * 
	 * @return whether a setup is currently loaded.
	 */
	public boolean setupLoaded() {
		return setupLoaded;
	}

	/**
	 * Returns the number of targets.
	 * 
	 * @return the number of targets.
	 */
	public int getNumTargets() {
		return numTargets;
	}

	/**
	 * Returns the policy of the target.
	 * 
	 * @return the policy of the target.
	 */
	public TargetPolicy getTargetPolicy() {
		return targetPolicy;
	}

	/**
	 * Returns the motion history of the target(s), or null if no motion history
	 * is available.
	 * 
	 * @return the motion history of the target(s), or null if no motion history
	 *         is available.
	 */
	public TargetMotionHistory getTargetMotionHistory() {
		return targetMotionHistory;
	}

	/**
	 * Returns the sensing parameters of the target.
	 * 
	 * @return the sensing parameters of the target.
	 */
	public SensingParameters getTargetSensingParams() {
		return targetSensingParams;
	}

	/**
	 * Returns the initial state(s) of the target(s).
	 * 
	 * @return the initial state(s) of the target(s).
	 */
	public List<AgentState> getTargetInitialStates() {
		return targetInitialStates;
	}

	/**
	 * Returns the motion history of the tracker, or null if no motion history
	 * is available.
	 * 
	 * @return the motion history of the tracker, or null if no motion history
	 *         is available.
	 */
	public TrackerMotionHistory getTrackerMotionHistory() {
		return trackerMotionHistory;
	}

	/**
	 * Returns the sensing parameters of the tracker.
	 * 
	 * @return the sensing parameters of the tracker.
	 */
	public SensingParameters getTrackerSensingParams() {
		return trackerSensingParams;
	}

	/**
	 * Returns the initial state of the tracker.
	 * 
	 * @return the initial state of the tracker.
	 */
	public AgentState getTrackerInitialState() {
		return trackerInitialState;
	}

	/**
	 * Returns the list of obstacles.
	 * 
	 * @return the list of obstacles.
	 */
	public List<RectRegion> getObstacles() {
		return obstacles;
	}

	/**
	 * Returns the goal region.
	 * 
	 * @return the goal region.
	 */
	public RectRegion getGoalRegion() {
		return goalRegion;
	}

	/* ------------------------ RUNNING THE GAME -------------------------- */

	public class GameState {
		/** True iff the game is already over in this state. */
		private boolean gameComplete = false;
		/** The turn number of this game. */
		private int turnNo;
		/** The player whose turn it is to act. */
		private int currentPlayer;

		/** The players in the game. */
		private Agent[] players;
		/** The divergences for the players' actions. */
		private ActionDivergence[] playerDivs;
		/** The scores of the players. */
		private double[] playerScores;
		/** The states of the players. */
		private AgentState[] playerStates;

		/** The percepts acquired since the last tracker turn. */
		List<Percept> trackerPercepts;

		/**
		 * Constructs a game state representing the initial state of the game
		 * (i.e. just before the 0-th action).
		 */
		public GameState() {
			turnNo = 0;
			currentPlayer = 0;
			trackerPercepts = new ArrayList<Percept>();

			players = new Agent[numTargets + 1];
			playerDivs = new ActionDivergence[numTargets + 1];
			playerScores = new double[numTargets + 1];
			playerStates = new AgentState[numTargets + 1];
			for (int i = 1; i <= numTargets; i++) {
				players[i] = new Target(targetPolicy);
				playerDivs[i] = new TargetDivergence(targetPolicy.getGrid());
				playerScores[i] = 0;
				playerStates[i] = targetInitialStates.get(i - 1);
			}
			playerDivs[0] = new ZeroDivergence();
			playerScores[0] = 0;
			playerStates[0] = trackerInitialState;

			List<AgentState> targetInitialStatesCopy = new ArrayList<AgentState>();
			for (AgentState as : targetInitialStates) {
				targetInitialStatesCopy.add(new AgentState(as));
			}
			List<RectRegion> obstaclesCopy = new ArrayList<RectRegion>();
			for (RectRegion obstacle : obstacles) {
				obstaclesCopy.add(new RectRegion(obstacle));
			}
			players[0] = new Tracker(numTargets,
					new TargetPolicy(targetPolicy), targetMotionHistory,
					new SensingParameters(targetSensingParams),
					targetInitialStatesCopy,

					trackerMotionHistory, new SensingParameters(
							trackerSensingParams), new AgentState(
							trackerInitialState),

					obstaclesCopy, new RectRegion(goalRegion));
		}

		/**
		 * Duplicates another game state.
		 * 
		 * @param other
		 *            the state to duplicate.
		 */
		public GameState(GameState other) {
			this.gameComplete = other.gameComplete;
			this.turnNo = other.turnNo;
			this.currentPlayer = other.currentPlayer;

			this.players = other.players;
			this.playerDivs = other.playerDivs;
			this.playerScores = Arrays.copyOf(other.playerScores,
					numTargets + 1);
			this.playerStates = Arrays.copyOf(other.playerStates,
					numTargets + 1);

			this.trackerPercepts = new ArrayList<Percept>(other.trackerPercepts);
		}

		/**
		 * Returns whether this state represents a completed game.
		 * 
		 * @return whether this state represents a completed game.
		 */
		public boolean isGameComplete() {
			return gameComplete;
		}

		/**
		 * Returns the turn number of this game.
		 * 
		 * @return the turn number of this game.
		 */
		public int getTurnNo() {
			return turnNo;
		}

		/**
		 * Returns the number of the player to act.
		 * 
		 * @return the number of the player to act.
		 */
		public int getCurrentPlayer() {
			return currentPlayer;
		}

		/**
		 * Returns the scores of the players.
		 * 
		 * @return the scores of the players.
		 */
		public double[] getPlayerScores() {
			return Arrays.copyOf(playerScores, numTargets + 1);
		}

		/**
		 * Returns the score of the tracker.
		 * 
		 * @return the score of the tracker.
		 */
		public double getTrackerScore() {
			return playerScores[0];
		}

		/**
		 * Returns the total score of the target(s).
		 * 
		 * @return the total score of the target(s).
		 */
		public double getTargetScore() {
			double score = 0;
			for (int i = 1; i <= numTargets; i++) {
				score += playerScores[i];
			}
			return score;
		}

		/**
		 * Returns the states of the players.
		 * 
		 * @return the states of the players.
		 */
		public AgentState[] getPlayerStates() {
			return Arrays.copyOf(playerStates, numTargets + 1);
		}
	}

	/** The sequence of attempted actions with associated results */
	private List<ActionResult> actionResultSequence;
	/** The sequence of states of the game. */
	private List<GameState> stateSequence;
	/** The current state of the game. */
	private GameState cs = null;

	/** The line separator to use when writing to the output file. */
	private static String lineSep = System.getProperty("line.separator");

	/** Returns true iff a game is active and complete. */
	public boolean gameComplete() {
		return cs != null && cs.gameComplete;
	}

	/**
	 * Returns the sequence of actions with associated results.
	 * 
	 * @return the sequence of actions with associated results.
	 */
	public List<ActionResult> getActionResultSequence() {
		return actionResultSequence;
	}

	/**
	 * Returns the sequence of game states.
	 * 
	 * @return the sequence of game states.
	 */
	public List<GameState> getStateSequence() {
		return stateSequence;
	}

	/**
	 * Returns the current turn number.
	 * 
	 * @return the current turn number.
	 */
	public int getTurnNo() {
		return cs.turnNo;
	}

	/**
	 * Reinitialises the game (i.e. goes to turn 0).
	 */
	public void initialise() {
		cs = new GameState();
		actionResultSequence = new ArrayList<ActionResult>();
		stateSequence = new ArrayList<GameState>();
		stateSequence.add(cs);
	}

	/**
	 * Undoes all moves after the given turn number.
	 * 
	 * @param desiredTurnNo
	 *            the turn number to revert to.
	 */
	public void undoTo(int desiredTurnNo) {
		if (desiredTurnNo >= cs.turnNo) {
			return;
		}

		int turnNo = cs.turnNo;
		while (turnNo > desiredTurnNo) {
			stateSequence.remove(turnNo);
			turnNo -= 1;
			actionResultSequence.remove(turnNo);
		}
		cs = stateSequence.get(turnNo);
	}

	/**
	 * Runs the full game.
	 */
	public void runFull() {
		initialise();
		while (!gameComplete()) {
			simulateTurn();
		}
	}

	/**
	 * Simulates a single turn of the game.
	 */
	public void simulateTurn() {
		// Duplicate the state for modification.
		cs = new GameState(cs);

		// Retrieve the last action taken by this player.
		int previousTurn = cs.turnNo - numTargets - 1;
		ActionResult previousResult;
		if (previousTurn >= 0) {
			previousResult = actionResultSequence.get(previousTurn);
		} else {
			previousResult = new ActionResult(null, new AgentState(
					cs.playerStates[cs.currentPlayer]), 0);
		}

		// Query the player for an action.
		Action action;
		if (cs.currentPlayer == 0) {
			// Tracker to act.

			// Copy the game's scores to inform the tracker.
			double[] scores = Arrays.copyOf(cs.playerScores, numTargets + 1);
			action = cs.players[0].getAction(cs.turnNo, previousResult, scores,
					new ArrayList<Percept>(cs.trackerPercepts));
			cs.trackerPercepts.clear();
		} else {
			// Target to act.
			action = cs.players[cs.currentPlayer].getAction(cs.turnNo,
					previousResult, null, null);
		}

		// Diverge the action.
		action = cs.playerDivs[cs.currentPlayer].divergeAction(action);

		// Simulate the action.
		ActionResult result;
		result = simulateAction(cs.turnNo, cs.currentPlayer, action);
		cs.playerScores[cs.currentPlayer] += result.getReward();
		actionResultSequence.add(result);

		// If a target reached the goal, the game ends.
		if (cs.currentPlayer != 0
				&& isWithinGoal(cs.playerStates[cs.currentPlayer])) {
			cs.gameComplete = true;
		}

		cs.turnNo += 1;
		cs.currentPlayer = (cs.currentPlayer + 1) % (numTargets + 1);
		stateSequence.add(cs);
	}

	/**
	 * Simulates an action taken by a player; this consists of any movement and
	 * also testing for which other players can be seen.
	 * 
	 * @param turnNo
	 *            the turn number.
	 * @param playerNo
	 *            the acting player.
	 * @param action
	 *            the action taken.
	 * @return the result of the action.
	 */
	public ActionResult simulateAction(int turnNo, int playerNo, Action action) {
		boolean isHQCall = false;
		double reward = 0;
		
		// Execute any movement or camera adjustment.
		if (action.isMovement()) {
			simulateMovement(turnNo, playerNo, action);
		} else if (action.isCameraAdjustment()) {
			simulateCameraAdjustment(turnNo, playerNo, action);
		}
		
		// Check whether this is an HQ call, and update the reward if so.
		if (playerNo == 0) {
			TrackerAction trackerAction = (TrackerAction)action;
			if (trackerAction.isHQCall()) {
				isHQCall = true;
				reward -= 5;
			}
		}
		
		// Check whether the tracker and target see each other.
		if (playerNo == 0) {
			// Evaluate the tracker's scoring.
			for (int otherNo = 1; otherNo <= numTargets; otherNo++) {
				boolean canSee = GeomTools.canSee(cs.playerStates[playerNo], cs.playerStates[otherNo], trackerSensingParams, obstacles, MAX_SIGHT_DISTANCE_ERROR, NUM_CAMERA_ARM_STEPS);
				if (canSee) {
					// Reward for seeing the target.
					reward += 1;
				}
				// If we saw the target or called HQ, we obtain a percept of the target's state.
				if (canSee || isHQCall) {
					cs.trackerPercepts.add(new Percept(turnNo, otherNo,
							new AgentState(cs.playerStates[otherNo])));
				}
			}
		} else {
			// If the target sees the tracker, the target gets rewarded.
			if (GeomTools.canSee(cs.playerStates[playerNo], cs.playerStates[0], targetSensingParams, obstacles, MAX_SIGHT_DISTANCE_ERROR, NUM_CAMERA_ARM_STEPS)) {
				reward += 1;
			}
			// The tracker sees the target -> percept but no reward.
			if (GeomTools.canSee(cs.playerStates[0], cs.playerStates[playerNo], trackerSensingParams, obstacles, MAX_SIGHT_DISTANCE_ERROR, NUM_CAMERA_ARM_STEPS)) {
				cs.trackerPercepts.add(new Percept(turnNo, playerNo,
						new AgentState(cs.playerStates[playerNo])));
			}
		}
		return new ActionResult(action, cs.playerStates[playerNo], reward);
	}

	/**
	 * Simulates only the camera adjustment aspect of an action.
	 * 
	 * @param turnNo
	 *            the turn number.
	 * @param playerNo
	 *            the acting player.
	 * @param action
	 *            the action taken.
	 */
	public void simulateCameraAdjustment(int turnNo, int playerNo,
			Action action) {
		AgentState startState = action.getStartState();
		// If the action is invalid, ignore it.
		if (!startState.hasCamera() || !startState.equals(cs.playerStates[playerNo])) {
			return;
		}
		double armLength = action.getResultingState().getCameraArmLength();
		double minLength = trackerSensingParams.getMinLength();
		double maxLength = trackerSensingParams.getMaxLength();
		if (armLength < minLength) {
			armLength = minLength;
		} else if (armLength > maxLength) {
			armLength = maxLength;
		}
		Point2D playerPos = startState.getPosition();
		Point2D cameraPos = new Vector2D(armLength, startState.getHeading() - Math.PI/2).addedTo(playerPos);
		Line2D.Double playerLine = new Line2D.Double(playerPos, cameraPos);
		// If the new camera arm length causes collision, don't update the state.
		if (!GeomTools.isCollisionFree(playerLine, extendedObstacles)) {
			return;
		}
	
		// Adjustment successful - update the state.
		AgentState resultingState = new AgentState(startState.getPosition(),startState.getHeading(),true,armLength);
		cs.playerStates[playerNo] = resultingState;
	}

	/**
	 * Simulates only the movement aspect of an action.
	 * 
	 * @param turnNo
	 *            the turn number.
	 * @param playerNo
	 *            the acting player.
	 * @param action
	 *            the action taken.
	 */
	public void simulateMovement(int turnNo, int playerNo, Action action) {
		AgentState startState = action.getStartState();
		// If the start state doesn't match, we ignore the action.
		if (!startState.equals(cs.playerStates[playerNo])) {
			return;
		}

		Point2D startPos = startState.getPosition();
		double startHeading = startState.getHeading();

		AgentState endState = action.getResultingState();
		Point2D endPos = endState.getPosition();
		double endHeading = action.getHeading();
		double distance = action.getDistance();
		boolean hasCamera = endState.hasCamera();
		double armLength = endState.getCameraArmLength();
		
		// If the action includes an impossible turn, ignore the action.
		if (hasCamera && (startHeading != endHeading) && 
				!GeomTools.canTurn(startPos, startHeading, endHeading, armLength, extendedObstacles)) {
			return;
		}
		
		// If a linear component is involved, test it.
		if (distance != 0) {
			// Set the distance moved to be the tracker's actual movement distance.
			if (playerNo == 0 && distance != 0) {
				distance = trackerMoveDistance;
				endPos = new Vector2D(distance, endHeading).addedTo(startPos);
			}
			
			// If the movement is invalid, ignore the whole action.
			if (!GeomTools.canMove(startPos, endPos, hasCamera, armLength, extendedObstacles)) {
				return;
			}
		}

		endState = new AgentState(endPos, endHeading, hasCamera, armLength);
		cs.playerStates[playerNo] = endState;
	}

	/**
	 * Returns true iff the given state lies within the goal.
	 * 
	 * @param s
	 *            the state to test.
	 * @return true iff the given state lies within the goal.
	 */
	public boolean isWithinGoal(AgentState s) {
		return goalRegion.getRect().contains(s.getPosition());
	}

	/**
	 * Writes the current game results to an output file.
	 * 
	 * @param outputPath
	 *            the path to write to.
	 * @throws IOException
	 *             if the file cannot be written.
	 */
	public void writeResults(String outputPath) throws IOException {
		FileWriter writer = new FileWriter(outputPath);
		writer.write(actionResultSequence.size() + lineSep);
		writer.write(trackerInitialState + lineSep);
		for (AgentState as : targetInitialStates) {
			writer.write(as + lineSep);
		}
		for (ActionResult result : actionResultSequence) {
			if (result.getAction() == null) {
				writer.write("-" + lineSep);
			} else {
				writer.write(result.getNewState() + " " + result.getReward()
						+ lineSep);
			}
		}
		writer.close();
	}

	/* ---------------------- COMMAND LINE RUNNER ------------------------ */
	public int runVerbose(String outputPath, boolean verbose) {
		initialise();
		runFull();
		int trackerScore = (int)cs.getTrackerScore();
		int targetScore = (int)cs.getTargetScore();
		int winResult;
		String formatString;
		if (trackerScore > targetScore) {
			winResult = 1;
			formatString = "Tracker wins %d-%d";
		} else if (trackerScore == targetScore) {
			winResult = 0;
			formatString = "Tracker ties %d-%d";
		} else {
			winResult = -1;
			formatString = "Tracker loses %d-%d";
		}
		if (verbose) {
			System.out.println(String.format(formatString, trackerScore, targetScore));
		}
		/*
		double[] scores = cs.getPlayerScores();
		if (numTargets > 1) {
			System.out.print("Individual target scores: ");
			StringBuilder sb = new StringBuilder();
			for (int i = 1; i <= numTargets; i++) {
				double score = scores[i];
				sb.append(String.format("%d ", (int) score));
			}
			System.out.println(sb);
		}
		System.out.println();
		*/
		
		try {
			writeResults(outputPath);
		} catch (IOException e) {
			System.err.println("Failed to write output: " + e.getMessage());
		}
		return winResult;
	}

	/** The default file to load the game setup from. */
	private static final String DEFAULT_SETUP_FILE = "setup.txt";
	/** The default file to output the game sequence to. */
	private static final String DEFAULT_OUTPUT_FILE = "output.txt";

	/**
	 * Runs a game, with the problem setup file passed from the command line.
	 * 
	 * @param args
	 *            command line arguments; the first should be the setup file.
	 */
	public static void main(String[] args) {
		String setupFile;
		String outputFile;
		if (args.length >= 1) {
			setupFile = args[0];
			if (args.length >= 2) {
				outputFile = args[1];
			} else {
				outputFile = DEFAULT_OUTPUT_FILE;
			}
		} else {
			setupFile = DEFAULT_SETUP_FILE;
			outputFile = DEFAULT_OUTPUT_FILE;
		}
		GameRunner runner = new GameRunner();
		try {
			runner.loadSetup(setupFile);
		} catch (IOException e) {
			System.err.println("Failed to load setup file: " + e.getMessage());
			return;
		}
		int numGames = 1000;
		int numWins = 0;
		for (int i = 0; i < numGames; i++) {
			int result = runner.runVerbose(outputFile, false);
			if (result == 1) {
				numWins += 1;
			}
		}
		System.out.println(String.format("Tracker won %d of %d games.", numWins, numGames));
	}
}
package game;

import geom.GeomTools;
import geom.Vector2D;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Line2D.Double;
import java.awt.geom.Rectangle2D;
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

import target.TargetMotionHistory;
import target.TargetPolicy;
import target.TargetWithError;
import tracker.Tracker;
import tracker.TrackerMotionHistory;

/**
 * This class runs the "Game of Spy" for assignment 2. It contains a structured
 * representation of the specifications for a given game setup, and also
 * contains the code that executes the game loop.
 * 
 * @author lackofcheese
 */
public class GameRunner {
	private double MAX_ERROR = 1e-5;
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

	/** The obstacles in the game space. */
	private List<RectRegion> obstacles;
	/** The goal region for the target(s). */
	private RectRegion goalRegion;

	/** The maximum distance that can be moved by an agent in one step. */
	private double maxMovementDistance;

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
			maxMovementDistance = Math.sqrt(2) / targetPolicy.getGridSize();
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

	/**
	 * Returns the maximum distance an agent can move in one step.
	 * 
	 * @return the maximum distance an agent can move in one step.
	 */
	public double getMaxMovementDistance() {
		return maxMovementDistance;
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
		/** The scores of the players. */
		private double[] playerScores;
		/** The states of the players. */
		private AgentState[] playerStates;
		/** Whether each player has reached the goal. */
		private boolean[] goalReached;

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
			playerScores = new double[numTargets + 1];
			playerStates = new AgentState[numTargets + 1];
			goalReached = new boolean[numTargets + 1];
			for (int i = 1; i <= numTargets; i++) {
				players[i] = new TargetWithError(targetPolicy);
				playerScores[i] = 0;
				playerStates[i] = targetInitialStates.get(i - 1);
				goalReached[i] = isWithinGoal(playerStates[i]);
			}
			playerScores[0] = 0;
			playerStates[0] = trackerInitialState;
			goalReached[0] = false;

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
			this.playerScores = Arrays.copyOf(other.playerScores,
					numTargets + 1);
			this.playerStates = Arrays.copyOf(other.playerStates,
					numTargets + 1);
			this.goalReached = Arrays.copyOf(other.goalReached, numTargets + 1);

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
	public List<ActionResult> getActionResults() {
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

		while (cs.turnNo > desiredTurnNo) {
			stateSequence.remove(cs.turnNo);
			cs.turnNo -= 1;
			actionResultSequence.remove(cs.turnNo);
		}
	}

	/**
	 * Simulates a single turn of the game.
	 */
	public void simulateTurn() {
		cs = new GameState(cs);
		int previousTurn = cs.turnNo - numTargets - 1;
		ActionResult previousResult;
		if (previousTurn >= 0) {
			previousResult = actionResultSequence.get(previousTurn);
		} else {
			previousResult = new ActionResult(null, new AgentState(
					cs.playerStates[cs.currentPlayer]), 0);
		}
		double[] scores = Arrays.copyOf(cs.playerScores, numTargets + 1);
		Action action;
		if (cs.currentPlayer == 0) {
			action = cs.players[0].getAction(cs.turnNo, previousResult, scores,
					new ArrayList<Percept>(cs.trackerPercepts));
			cs.trackerPercepts.clear();
		} else {
			if (cs.goalReached[cs.currentPlayer]) {
				action = null;
			} else {
				action = cs.players[cs.currentPlayer].getAction(cs.turnNo,
						previousResult, scores, null);
			}
		}

		ActionResult result;
		if (action != null) {
			result = simulateAction(cs.turnNo, cs.currentPlayer, action);
			cs.playerScores[cs.currentPlayer] += result.getReward();
		} else {
			result = new ActionResult(null, previousResult.getNewState(), 0);
		}
		actionResultSequence.add(result);

		if (isWithinGoal(cs.playerStates[cs.currentPlayer])) {
			cs.goalReached[cs.currentPlayer] = true;
		}
		boolean allInGoal = true;
		for (int i = 1; i <= numTargets; i++) {
			if (!cs.goalReached[i]) {
				allInGoal = false;
			}
		}
		if (allInGoal) {
			cs.gameComplete = true;
		}

		cs.turnNo += 1;
		cs.currentPlayer = (cs.currentPlayer + 1) % (numTargets + 1);
		stateSequence.add(cs);
	}

	/**
	 * Simulates an action taken by a player; this consists of a movement and
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
		ActionResult result = simulateMovement(turnNo, playerNo, action);
		AgentState newState = result.getNewState();
		double reward = result.getReward();

		if (playerNo == 0) {
			for (int otherNo = 1; otherNo <= numTargets; otherNo++) {
				if (cs.goalReached[otherNo]) {
					continue;
				}
				if (action.isCallingHQ() || canSee(0, otherNo)) {
					reward += 1;
					cs.trackerPercepts.add(new Percept(turnNo, otherNo,
							new AgentState(cs.playerStates[otherNo])));
				}
			}
		} else {
			if (canSee(playerNo, 0)) {
				reward += 1;
			}
			if (canSee(0, playerNo)) {
				cs.trackerPercepts.add(new Percept(turnNo, playerNo,
						new AgentState(cs.playerStates[playerNo])));
			}
		}
		return new ActionResult(action, newState, reward);
	}

	/**
	 * Simulates only the movement / HQ call aspect of an action - doesn't add
	 * the rewards for seeing other players.
	 * 
	 * @param turnNo
	 * @param playerNo
	 * @param action
	 * @return the result of the movement / HQ call.
	 */
	public ActionResult simulateMovement(int turnNo, int playerNo, Action action) {
		AgentState startState = action.getStartState();
		if (action.isCallingHQ()) {
			return new ActionResult(action, startState, -5);
		}

		if (!startState.equals(cs.playerStates[playerNo])) {
			return new ActionResult(action, startState, 0);
		}

		Point2D startPos = startState.getPosition();
		double startHeading = startState.getHeading();

		AgentState endState = action.getResultingState();
		Point2D endPos = endState.getPosition();
		double heading = action.getHeading();
		boolean hasCamera = endState.hasCamera();
		double armLength = endState.getCameraArmLength();
		boolean canTurn = true;
		if (hasCamera) {
			double minLength = trackerSensingParams.getMinLength();
			double maxLength = trackerSensingParams.getMaxLength();
			if (armLength < minLength) {
				armLength = minLength;
			} else if (armLength > maxLength) {
				armLength = maxLength;
			}

			if (!canTurn(startPos, startHeading, heading, armLength)) {
				endPos = startPos;
				heading = startHeading;
				canTurn = false;
			}
		}

		if (canTurn) {
			double distance = action.getDistance();
			if (distance > maxMovementDistance + MAX_ERROR) {
				distance = maxMovementDistance;
				endPos = new Vector2D(distance, heading).addedTo(startPos);
			}

			if (!canMove(startPos, endPos, hasCamera, armLength)) {
				endPos = startPos;
			}
		}

		endState = new AgentState(endPos, heading, hasCamera, armLength);
		cs.playerStates[playerNo] = endState;
		return new ActionResult(action, endState, 0);
	}

	/**
	 * Return true if turning from the initial heading to the final heading at
	 * the given position is valid.
	 * 
	 * @param centre
	 *            the centre position.
	 * @param startHeading
	 *            the initial heading.
	 * @param endHeading
	 *            the final heading.
	 * @param armLength
	 *            the length of the camera arm.
	 * @return true
	 */
	public boolean canTurn(Point2D centre, double startHeading,
			double endHeading, double armLength) {
		Arc2D arc = new Arc2D.Double();
		double startDeg = -Math.toDegrees(startHeading - Math.PI / 2);
		double extentDeg = -Math.toDegrees(GeomTools.normaliseAngle(endHeading
				- startHeading));
		for (int i = 0; i < 2; i++) {
			arc.setArcByCenter(centre.getX(), centre.getY(), armLength,
					startDeg, extentDeg, Arc2D.PIE);
			if (isCollisionFree(arc)) {
				return true;
			}
			if (extentDeg <= 0) {
				extentDeg += 360;
			} else {
				extentDeg -= 360;
			}
		}
		return false;
	}

	/**
	 * Returns true iff moving from the start to the end with the given arm
	 * length is valid.
	 * 
	 * @param startPos
	 *            the start position.
	 * @param endPos
	 *            the end position.
	 * @param hasCamera
	 *            whether a camera arm is present.
	 * @param armLength
	 *            the length of the camera arm.
	 * @return true iff moving from the start to the end with the given arm
	 *         length is valid.
	 */
	public boolean canMove(Point2D startPos, Point2D endPos, boolean hasCamera,
			double armLength) {
		Line2D line = new Line2D.Double(startPos, endPos);
		if (!isCollisionFree(line)) {
			return false;
		}
		if (!hasCamera) {
			return true;
		}

		Vector2D disp = new Vector2D(startPos, endPos);
		double distance = disp.getMagnitude();
		double heading = disp.getDirection();
		Rectangle2D rec = new Rectangle2D.Double(startPos.getX(),
				startPos.getY(), armLength, distance);
		AffineTransform tf = AffineTransform.getRotateInstance(heading
				- Math.PI / 2, startPos.getX(), startPos.getY());
		return isCollisionFree(tf.createTransformedShape(rec));
	}

	/**
	 * Returns true iff the observer can see the observed.
	 * 
	 * @param observerNo
	 *            the player no. of the observer.
	 * @param observedNo
	 *            the player no. of the observed.
	 * @return true iff the observer can see the observed.
	 */
	public boolean canSee(int observerNo, int observedNo) {
		AgentState observedState = cs.playerStates[observedNo];
		Point2D observedPos = observedState.getPosition();
		if (canSee(observerNo, observedPos)) {
			return true;
		}
		if (observedNo != 0 || !cs.playerStates[0].hasCamera()) {
			return false;
		}
		double armDirection = observedState.getHeading() - Math.PI / 2;
		double armLength = observedState.getCameraArmLength();
		Point2D observedCameraPos = new Vector2D(armLength, armDirection)
				.addedTo(observedPos);
		int count = 0;
		for (int i = 1; i <= NUM_CAMERA_ARM_STEPS; i++) {
			double t = ((double) i) / NUM_CAMERA_ARM_STEPS;
			Point2D p = GeomTools.interpolatePoint(observedPos,
					observedCameraPos, t);
			if (canSee(observerNo, p)) {
				count += 1;
			}
		}
		return (count * 2 > NUM_CAMERA_ARM_STEPS + 1);
	}

	/**
	 * Returns true iff the given observer can see the given point.
	 * 
	 * @param observerNo
	 *            the player no. of the observer.
	 * @param point
	 *            the point.
	 * @return true iff the given observer can see the given point.
	 */
	public boolean canSee(int observerNo, Point2D point) {
		SensingParameters sp = (observerNo == 0 ? targetSensingParams
				: trackerSensingParams);
		AgentState observerState = cs.playerStates[observerNo];
		Point2D observerPos = observerState.getPosition();
		double observerHeading = observerState.getHeading();
		Point2D viewPos = observerPos;

		if (observerState.hasCamera()) {
			double armDirection = observerHeading - Math.PI / 2;
			double armLength = observerState.getCameraArmLength();
			viewPos = new Vector2D(armLength, armDirection)
					.addedTo(observerPos);
		}

		Vector2D viewVector = new Vector2D(viewPos, point);

		// Verify the viewing range.
		double distance = viewVector.getMagnitude();
		if (distance < MAX_ERROR) {
			return true;
		}
		if (distance > sp.getRange() + MAX_ERROR) {
			return false;
		}

		// Verify the viewing angle.
		double viewAngleDelta = GeomTools.normaliseAngle(viewVector
				.getDirection() - observerHeading);
		if (Math.abs(viewAngleDelta) > sp.getAngle() / 2 + MAX_ERROR) {
			return false;
		}

		return isCollisionFree(new Line2D.Double(viewPos, point));
	}

	/**
	 * Returns true iff the given line doesn't collide with any obstacles.
	 * 
	 * @param s
	 *            the line to test.
	 * @return true iff the given line doesn't collide with any obstacles.
	 */
	public boolean isCollisionFree(Shape s) {
		for (RectRegion obs : obstacles) {
			if (s.intersects(obs.getRect())) {
				return false;
			}
		}
		return true;
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

	/**
	 * Runs the full game.
	 */
	public void runFull() {
		initialise();
		while (!gameComplete()) {
			simulateTurn();
		}
	}

	/* ---------------------- COMMAND LINE RUNNER ------------------------ */

	/** The default file to load the game setup from. */
	private static final String DEFAULT_SETUP_FILE = "setup.txt";
	/** The default file to output the game sequence to. */
	private static final String DEFAULT_OUTPUT_FILE = "output.txt";

	/** The file to load the game setup from. */
	private static String setupFile = null;
	/** The file to load the output from. */
	private static String outputFile = null;

	/**
	 * Runs a game, with the problem setup file passed from the command line.
	 * 
	 * @param args
	 *            command line arguments; the first should be the setup file.
	 */
	public static void main(String[] args) {
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

		runner.initialise();
		runner.runFull();
		double[] scores = runner.cs.playerScores;
		double trackerScore = scores[0];
		System.out.println("Final Scores:");
		System.out.println("Tracker: " + (int) trackerScore);
		System.out.print("Target(s): ");
		StringBuilder sb = new StringBuilder();
		int winValue = 1;
		for (int i = 1; i <= runner.numTargets; i++) {
			double score = scores[i];
			sb.append(String.format("%d ", (int) score));
			if (score > trackerScore) {
				winValue = -1;
			} else if (score == trackerScore) {
				if (winValue == 1) {
					winValue = 0;
				}
			}
		}
		System.out.println(sb);
		System.out.println();
		if (winValue == 1) {
			System.out.println("Tracker wins!");
		} else if (winValue == 0) {
			System.out.println("Tracker ties!");
		} else {
			System.out.println("Tracker loses!");
		}
		try {
			runner.writeResults(outputFile);
		} catch (IOException e) {
			System.err.println("Failed to write output: " + e.getMessage());
		}
	}
}
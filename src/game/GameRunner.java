package game;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
	private double MAX_DISTANCE_ERROR = 1e-5;

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
		setupLoaded = false;
		gameComplete = false;
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
			targetPolicy = new TargetPolicy(s.next());
			maxMovementDistance = Math.sqrt(2) / targetPolicy.getGridSize();
			if (hasTargetHistory) {
				targetMotionHistory = new TargetMotionHistory(s.next());
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
				trackerMotionHistory = new TrackerMotionHistory(s.next());
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

	/* Parameters and methods for running the game. */

	/** The line separator to use when writing to the output file. */
	private static String lineSep = System.getProperty("line.separator");

	private boolean gameComplete = false;
	private int turnNo;
	private int currentPlayer;
	private double[] playerScores;
	private Agent[] agents;
	private AgentState[] agentStates;

	List<Percept> percepts;
	private List<ActionResult> actionResults;

	private boolean gameComplete() {
		return gameComplete;
	}

	private void initialise() {
		turnNo = 0;
		currentPlayer = 0;
		actionResults = new ArrayList<ActionResult>();
		percepts = new ArrayList<Percept>();

		playerScores = new double[numTargets + 1];
		agents = new Agent[numTargets + 1];
		agentStates = new AgentState[numTargets + 1];
		for (int i = 1; i <= numTargets; i++) {
			playerScores[i] = 0;
			agentStates[i] = targetInitialStates.get(i - 1);
			agents[i] = new TargetWithError(targetPolicy);
		}
		playerScores[0] = 0;
		agentStates[0] = trackerInitialState;

		List<AgentState> targetInitialStatesCopy = new ArrayList<AgentState>();
		for (AgentState as : targetInitialStates) {
			targetInitialStatesCopy.add(new AgentState(as));
		}
		List<RectRegion> obstaclesCopy = new ArrayList<RectRegion>();
		for (RectRegion obstacle : obstacles) {
			obstaclesCopy.add(new RectRegion(obstacle));
		}
		agents[0] = new Tracker(
				numTargets,
				new TargetPolicy(targetPolicy),
				targetMotionHistory,
				new SensingParameters(targetSensingParams),
				targetInitialStatesCopy,

				trackerMotionHistory,
				new SensingParameters(trackerSensingParams), 
				new AgentState(trackerInitialState),

				obstaclesCopy, 
				new RectRegion(goalRegion));
	}

	private void doStep() {
		int previousTurn = turnNo - numTargets - 1;
		ActionResult previousResult;
		if (previousTurn >= 0) {
			previousResult = actionResults.get(previousTurn);
		} else {
			previousResult = new ActionResult(null, new AgentState(
					agentStates[currentPlayer]), 0);
		}
		double[] scores = Arrays.copyOf(playerScores, numTargets + 1);
		Action action;
		if (currentPlayer == 0) {
			action = agents[0].getAction(turnNo, previousResult, scores,
					new ArrayList<Percept>(percepts));
			percepts.clear();
		} else {
			action = agents[currentPlayer].getAction(turnNo, previousResult,
					scores, null);
		}

		ActionResult result = doAction(turnNo, currentPlayer, action);
		scores[currentPlayer] += result.getReward();
		actionResults.add(result);

		turnNo += 1;
		currentPlayer = (currentPlayer + 1) % (numTargets + 1);
	}

	private ActionResult doAction(int turnNo, int playerNo, Action action) {
		ActionResult result = doMovement(turnNo, playerNo, action);
		AgentState newState = result.getNewState();
		double reward = result.getReward();

		if (playerNo == 0) {
			for (int otherNo = 1; otherNo <= numTargets; otherNo++) {
				if (action.isCallingHQ() || canSee(playerNo, otherNo)) {
					reward += 1;
					percepts.add(new Percept(turnNo, otherNo, new AgentState(
							agentStates[otherNo])));
				}
			}
		} else {
			if (canSee(playerNo, 0)) {
				reward += 1;
			}
			if (canSee(0, playerNo)) {
				percepts.add(new Percept(turnNo, playerNo, new AgentState(
						agentStates[playerNo])));
			}
		}
		return new ActionResult(action, newState, reward);
	}

	private ActionResult doMovement(int turnNo, int playerNo, Action action) {
		AgentState startState = action.getStartState();
		if (action.isCallingHQ()) {
			return new ActionResult(action, startState, -5);
		}

		if (!startState.equals(agentStates[playerNo])) {
			return new ActionResult(action, startState, 0);
		}

		AgentState endState = action.getResultingState();
		Point2D startPos = startState.getPosition();
		double heading = action.getHeading();
		double distance = action.getDistance();
		if (distance > maxMovementDistance + MAX_DISTANCE_ERROR) {
			distance = maxMovementDistance;
			Point2D endPos = new Point2D.Double(startPos.getX() + distance
					* Math.cos(heading), startPos.getY() + distance
					* Math.sin(heading));
			endState = new AgentState(endPos, heading, startState.hasCamera(),
					action.getNewCameraArmLength());
		}

		if (!canMove(startState, endState)) {
			endState = new AgentState(startPos, heading,
					startState.hasCamera(), action.getNewCameraArmLength());

		}
		return new ActionResult(action, endState, 0);
	}

	private boolean canMove(AgentState startState, AgentState endState) {
		return true;
	}

	private boolean canSee(int observerNo, int observedNo) {
		return false;
	}

	private void writeResults(String outputPath) throws IOException {
		FileWriter writer = new FileWriter(outputPath);
		writer.write(actionResults.size() + lineSep);
		writer.write(trackerInitialState + lineSep);
		for (AgentState as : targetInitialStates) {
			writer.write(as + lineSep);
		}
		for (ActionResult result : actionResults) {
			writer.write(result.getNewState() + " " + result.getReward()
					+ lineSep);
		}
		writer.close();
	}

	private void runFull() {
		initialise();
		while (!gameComplete) {
			doStep();
		}
	}

	/* Parameters and methods used when run from the command line. */

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
		System.out.println("Final Scores:");
		System.out.println("Tracker: " + (int) runner.playerScores[0]);
		System.out.println("Target(s):");
		StringBuilder sb = new StringBuilder();
		for (int i = 1; i < runner.numTargets; i++) {
			sb.append(String.format("%4d ", runner.playerScores[i]));
		}
		System.out.println(sb);
		try {
			runner.writeResults(outputFile);
		} catch (IOException e) {
			System.err.println("Failed to write output: " + e.getMessage());
		}
	}
}
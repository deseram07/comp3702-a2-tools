package divergence;

import game.Action;
import game.ActionResult;
import game.AgentState;
import geom.ActionEncoder;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Represents a sequence of actions and results made by an agent.
 * 
 * @author lackofcheese
 * 
 */
public class MotionHistory implements Iterable<MotionHistory.HistoryEntry> {
	/** The history */
	private List<HistoryEntry> history;

	/**
	 * Represents an entry in the history.
	 * 
	 * @author lackofcheese
	 */
	public static class HistoryEntry {
		/** The starting state. */
		private AgentState startState;
		/** The code for the desired action. */
		private int desiredActionCode;
		/** The code for the diverged action */
		private int divergedActionCode;
		/** The result code. */
		private int resultCode;

		/**
		 * Constructs an entry from the required values.
		 * 
		 * @param startState
		 *            the starting state.
		 * @param desiredActionCode
		 *            the desired action code.
		 * @param divergedActionCode
		 *            the diverged action code.
		 * @param resultCode
		 *            the result code.
		 */
		private HistoryEntry(AgentState startState, int desiredActionCode,
				int divergedActionCode, int resultCode) {
			this.startState = startState;
			this.desiredActionCode = desiredActionCode;
			this.divergedActionCode = divergedActionCode;
			this.resultCode = resultCode;
		}

		/**
		 * Constructs an entry from a String.
		 * 
		 * @param line
		 *            the String representation.
		 */
		private HistoryEntry(String line, boolean hasCamera) {
			Scanner s = new Scanner(line);
			Point2D pos = new Point2D.Double(s.nextDouble(), s.nextDouble());
			double heading = Math.toRadians(s.nextDouble());
			double cameraArmLength = 0;
			if (hasCamera) {
				cameraArmLength = s.nextDouble();
			}
			startState = new AgentState(pos, heading, hasCamera,
					cameraArmLength);
			desiredActionCode = s.nextInt();
			divergedActionCode = s.nextInt();
			resultCode = s.nextInt();
			s.close();
		}

		/**
		 * Returns the starting state.
		 * 
		 * @return the starting state.
		 */
		public AgentState getStartState() {
			return startState;
		}

		/**
		 * Returns the code for the desired action.
		 * 
		 * @return the code for the desired action.
		 */
		public int getDesiredActionCode() {
			return desiredActionCode;
		}

		/**
		 * Returns the code for the diverged action.
		 * 
		 * @return the code for the diverged action.
		 */
		public int getDivergedActionCode() {
			return divergedActionCode;
		}

		/**
		 * Returns the code for the result.
		 * 
		 * @return the code for the result.
		 */
		public int getResultCode() {
			return resultCode;
		}
	}

	/**
	 * Returns the entire history in a list.
	 * 
	 * @return the entire history in a list.
	 */
	public List<HistoryEntry> getHistory() {
		return new ArrayList<HistoryEntry>(history);
	}

	/**
	 * Returns the history entry at the given index.
	 * 
	 * @param entryNo
	 *            the index.
	 * @return the history entry at the given index.
	 */
	public HistoryEntry getEntry(int entryNo) {
		return history.get(entryNo);
	}

	/**
	 * Returns the number of history entries present.
	 * 
	 * @return the number of history entries present.
	 */
	public int getNumEntries() {
		return history.size();
	}

	/**
	 * Creates and adds a history entry from an ActionResult.
	 * 
	 * @param result
	 *            the action + result.
	 * @param encoder
	 *            the way to encode the actions into action codes.
	 */
	public void addEntry(ActionResult result, ActionEncoder encoder) {
		Action desiredAction = result.getDesiredAction();
		Action divergedAction = result.getDivergedAction();
		AgentState startState = desiredAction.getStartState();
		history.add(new HistoryEntry(startState,
				encoder.encodeAction(desiredAction),
				encoder.encodeAction(divergedAction),
				encoder.encodeAction(new Action(startState, 
						result.getResultingState().getPosition()))));
	}

	/**
	 * Creates an empty MotionHistory.
	 */
	public MotionHistory() {
		history = new ArrayList<HistoryEntry>();
	}

	/**
	 * Creates a MotionHistory from the given data file.
	 * 
	 * @param filename
	 *            the file to read from.
	 * @param hasCamera
	 *            whether the agent has a camera.
	 * @throws IOException
	 *             if there is an error reading the file.
	 */
	public MotionHistory(String filename, boolean hasCamera) throws IOException {
		this();
		BufferedReader input = new BufferedReader(new FileReader(filename));
		String line;
		int lineNo = 0;
		Scanner s;
		try {
			line = input.readLine();
			lineNo++;
			s = new Scanner(line);
			int numEntries = s.nextInt();
			s.close();

			for (int i = 0; i < numEntries; i++) {
				line = input.readLine();
				lineNo++;
				history.add(new HistoryEntry(line, hasCamera));
			}
		} catch (InputMismatchException e) {
			throw new IOException(String.format(
					"Invalid number format on line %d of %s: %s", lineNo,
					filename, e.getMessage()));
		} catch (NoSuchElementException e) {
			throw new IOException(String.format(
					"Not enough tokens on line %d of %s", lineNo, filename));
		} catch (NullPointerException e) {
			throw new IOException(String.format(
					"Line %d expected, but file %s ended.", lineNo, filename));
		} finally {
			input.close();
		}
	}

	/**
	 * Writes the history to a file.
	 * 
	 * @param filename
	 *            the path of the file to write to.
	 * @throws IOException
	 */
	public void writeToFile(String filename) throws IOException {
		FileWriter output = new FileWriter(filename);
		String lineSep = System.getProperty("line.separator");
		try {
			output.write(history.size() + lineSep);
			for (HistoryEntry entry : history) {
				output.write(String.format("%s   %2d %2d %2d%s",
						entry.getStartState(), entry.getDesiredActionCode(),
						entry.getDivergedActionCode(), entry.getResultCode(),
						lineSep));
			}
		} finally {
			output.close();
		}
	}

	@Override
	public Iterator<HistoryEntry> iterator() {
		return history.iterator();
	}
}
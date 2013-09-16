package target;

import game.Action;
import game.AgentState;
import geom.Grid;
import geom.Grid.GridCell;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Represents the policy of a target.
 * 
 * @author lackofcheese
 * 
 */
public class TargetPolicy {
	/** The grid over which the policy is defined. */
	private Grid grid;
	/** The policy itself, as a mapping of grid cells. */
	private Map<Grid.GridCell, Grid.GridCell> policyMap;

	/**
	 * Constructs a duplicate policy.
	 * 
	 * @param policy
	 *            the policy to duplicate.
	 */
	public TargetPolicy(TargetPolicy policy) {
		this.grid = new Grid(policy.grid.getGridSize());
		this.policyMap = new HashMap<Grid.GridCell, Grid.GridCell>(
				policy.policyMap);
	}

	/**
	 * Constructs a policy from a file.
	 * 
	 * @param path
	 *            the path of the file to read.
	 * @throws IOException
	 *             if the file is invalid.
	 */
	public TargetPolicy(String path) throws IOException {
		policyMap = new HashMap<Grid.GridCell, Grid.GridCell>();

		BufferedReader input = new BufferedReader(new FileReader(path));
		int lineNo = 0;
		String line;
		Scanner s;
		try {
			line = input.readLine();
			lineNo++;
			s = new Scanner(line);
			int numRows = s.nextInt();
			int numCols = s.nextInt();
			s.close();
			if (numRows != numCols) {
				throw new IOException(
						"Number of rows must equal number of columns.");
			}
			int gridSize = numRows;
			grid = new Grid(gridSize);

			for (int i = 0; i < gridSize; i++) {
				line = input.readLine();
				lineNo++;
				s = new Scanner(line);
				for (int j = 0; j < gridSize; j++) {
					GridCell current = grid.new GridCell(i, j);
					int actionCode = s.nextInt();
					GridCell target = grid.decodeAction(current, actionCode);
					policyMap.put(current, target);
				}
				s.close();
			}
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
	 * Returns the grid used by this policy.
	 * 
	 * @return the grid used by this policy.
	 */
	public Grid getGrid() {
		return grid;
	}

	/**
	 * Returns the size of the grid for this policy.
	 * 
	 * @return the size of the grid for this policy.
	 */
	public int getGridSize() {
		return grid.getGridSize();
	}

	/**
	 * Returns the proper next cell for the given cell.
	 * 
	 * @param index
	 *            the current cell.
	 * @return the proper next cell for the given cell.
	 */
	public GridCell getNextIndex(GridCell index) {
		return policyMap.get(index);
	}

	/**
	 * Returns the proper action for the given state.
	 * 
	 * @param currentState
	 *            the current state.
	 * @return the proper action for the given state.
	 */
	public Action getAction(AgentState currentState) {
		GridCell startIndex = grid.getIndex(currentState.getPosition());
		GridCell endIndex = policyMap.get(startIndex);
		if (endIndex.equals(startIndex)) {
			return new Action(currentState);
		}
		return new Action(currentState, grid.getCentre(endIndex));
	}
}

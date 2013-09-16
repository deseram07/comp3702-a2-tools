package target;

import game.Action;
import game.AgentState;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class TargetPolicy {
	private class CellIndex {
		private int row;
		private int col;
		
		private CellIndex(int row, int col) {
			this.row = row;
			this.col = col;
		}
		
		public int getRow() {
			return row;
		}
		
		public int getCol() {
			return col;
		}
		
		@Override
		public boolean equals(Object o) {
			if (!(o instanceof CellIndex)) {
				return false;
			}
			CellIndex other = (CellIndex)o;
			return this.row == other.row && this.col == other.col;
		}
		
		@Override
		public int hashCode() {
			return row * 3571 + col;
		}
	}
	
	private int gridSize;
	private Map<CellIndex, CellIndex> policyMap;
	
	public TargetPolicy(TargetPolicy policy) {
		this.gridSize = policy.gridSize;
		this.policyMap = new HashMap<CellIndex, CellIndex>(policy.policyMap);
	}
	
	public TargetPolicy(String path) throws IOException {
		policyMap = new HashMap<CellIndex, CellIndex>();
		
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
				throw new IOException("Number of rows must equal number of columns.");
			}
			gridSize = numRows;
			
			for (int i = 0; i < gridSize; i++) {
				line = input.readLine();
				lineNo++;
				s = new Scanner(line);
				for (int j = 0; j < gridSize; j++) {
					CellIndex current = new CellIndex(i, j);
					int actionCode = s.nextInt();
					CellIndex target = decodeAction(current, actionCode);
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
	
	public int getGridSize() {
		return gridSize;
	}
	
	public Map<CellIndex, CellIndex> getPolicyMap() {
		return new HashMap<CellIndex, CellIndex>(policyMap);
	}
	
	public int encodeAction(CellIndex start, CellIndex end) {
		int rowDelta = end.getRow() - start.getRow();
		int colDelta = end.getCol() - start.getCol();
		return rowDelta * 3 + colDelta + 4;
	}
	
	public CellIndex decodeAction(CellIndex start, int actionCode) {
		return new CellIndex(
				start.getRow() + actionCode / 3 - 1,
				start.getCol() + actionCode % 3 - 1);
	}
	
	public int get1DIndex(double coord) {
		int index = (int)(coord*gridSize);
		if (index < 0) {
			index = 0;
		}
		if (index >= gridSize) {
			index = gridSize - 1;
		}
		return index;
	}

	public double get1DCentreCoord(int index) {
		return (0.5 + index) / gridSize;
	}
	
	public Action getAction(AgentState currentState) {
		Point2D position = currentState.getPosition();
		CellIndex startIndex = new CellIndex(
				get1DIndex(position.getX()), get1DIndex(position.getY()));
		CellIndex endIndex = policyMap.get(startIndex);
		if (endIndex.equals(startIndex)) {
			return new Action(currentState);
		}
		
		Point2D endPosition = new Point2D.Double(
				get1DCentreCoord(endIndex.getRow()),
				get1DCentreCoord(endIndex.getCol()));
		return new Action(currentState, endPosition);
	}
}

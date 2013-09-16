package geom;

import java.awt.geom.Point2D;

/**
 * Represents a square grid over the workspace.
 * 
 * @author lackofcheese
 * 
 */
public class Grid {
	/** The number of rows and columns. */
	public int gridSize;

	/**
	 * Constructs a square grid with the given number of rows and columns.
	 * 
	 * @param gridSize
	 *            the number of rows and columns.
	 */
	public Grid(int gridSize) {
		this.gridSize = gridSize;
	}

	/**
	 * Represents a cell within the grid.
	 * 
	 * @author lackofcheese
	 * 
	 */
	public class GridCell {
		/** The row index of the cell. */
		private int row;
		/** The column index of the cell. */
		private int col;

		/**
		 * Constructs a grid cell from its row and column indices.
		 * 
		 * @param row
		 *            the row index of the cell.
		 * @param col
		 *            the column index of the cell.
		 */
		public GridCell(int row, int col) {
			this.row = bound(row);
			this.col = bound(col);
		}

		/**
		 * Returns the row index of the cell.
		 * 
		 * @return the row index of the cell.
		 */
		public int getRow() {
			return row;
		}

		/**
		 * Returns the column index of the cell.
		 * 
		 * @return the column index of the cell.
		 */
		public int getCol() {
			return col;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof GridCell)) {
				return false;
			}
			GridCell other = (GridCell) o;
			return this.row == other.row && this.col == other.col;
		}

		@Override
		public int hashCode() {
			return row * 3571 + col;
		}
	}

	/**
	 * Bounds the given 1-D index to the allowed range.
	 * 
	 * @param index
	 *            the index to bound.
	 * @return the closest allowable value to the given index.
	 */
	public int bound(int index) {
		if (index < 0) {
			index = 0;
		}
		if (index >= gridSize) {
			index = gridSize - 1;
		}
		return index;
	}

	/**
	 * Returns the number of rows and columns in the grid.
	 * 
	 * @return the number of rows and columns in the grid.
	 */
	public int getGridSize() {
		return gridSize;
	}

	/**
	 * Returns the action code corresponding to a movement between the given
	 * cells.
	 * 
	 * @param start
	 *            the start cell.
	 * @param end
	 *            the end cell.
	 * @return the action code for a movement from the start cell to the end
	 *         cell.
	 */
	public int encodeAction(GridCell start, GridCell end) {
		int rowDelta = end.getRow() - start.getRow();
		int colDelta = end.getCol() - start.getCol();
		return rowDelta * 3 + colDelta + 4;
	}

	/**
	 * Returns the end cell resulting from taking the action with the given
	 * action code from the starting cell.
	 * 
	 * @param start
	 *            the start cell.
	 * @param actionCode
	 *            the action to take.
	 * @return the end cell after taking the given action from the start cell.
	 */
	public GridCell decodeAction(GridCell start, int actionCode) {
		return new GridCell(start.getRow() + actionCode / 3 - 1, start.getCol()
				+ actionCode % 3 - 1);
	}

	/**
	 * Returns the heading corresponding to the given action.
	 * 
	 * @param actionCode
	 *            the action.
	 * @return the heading corresponding to the given action.
	 */
	public double getHeading(int actionCode) {
		switch (actionCode) {
		case 0:
			return 3 * Math.PI / 4;
		case 1:
			return Math.PI / 2;
		case 2:
			return Math.PI / 4;
		case 3:
			return Math.PI;
		case 4:
		case 5:
			return 0;
		case 6:
			return -3 * Math.PI / 4;
		case 7:
			return -Math.PI / 2;
		case 8:
			return -Math.PI / 4;
		default:
			return 0;
		}
	}

	/**
	 * Returns the action closest to the given heading.
	 * 
	 * @param heading
	 *            the heading to take.
	 * @return the action closest to the given heading.
	 */
	public int getCodeFromHeading(double heading) {
		int numEighths = (int) Math.round(heading * 4 / Math.PI);
		numEighths %= 8;
		if (numEighths <= -4) {
			numEighths += 8;
		} else if (numEighths > 4) {
			numEighths -= 8;
		}
		switch (numEighths) {
		case -3:
			return 6;
		case -2:
			return 7;
		case -1:
			return 8;
		case 0:
			return 5;
		case 1:
			return 2;
		case 2:
			return 1;
		case 3:
			return 0;
		case 4:
			return 3;
		default:
			return 0;
		}
	}

	/**
	 * Returns the grid cell containing the given point.
	 * 
	 * @param pos
	 *            the point to locate.
	 * @return the grid cell containing the given point.
	 */
	public GridCell getIndex(Point2D pos) {
		int row = (int) ((1 - pos.getY()) * gridSize);
		int col = (int) (pos.getX() * gridSize);
		return new GridCell(row, col);
	}

	/**
	 * Returns the centre point of the given cell.
	 * 
	 * @param cell
	 *            the cell.
	 * @return the centre point of the given cell.
	 */
	public Point2D getCentre(GridCell cell) {
		return new Point2D.Double((cell.getCol() + 0.5) / gridSize, 1
				- (cell.getRow() + 0.5) / gridSize);
	}
}
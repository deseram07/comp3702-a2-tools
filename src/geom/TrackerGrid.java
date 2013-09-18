package geom;

import java.awt.geom.Point2D;

/**
 * Represents a square grid over the workspace.
 * 
 * @author lackofcheese
 * 
 */
public class TrackerGrid {
	/** The width of each cell. */
	private double cellWidth;

	/**
	 * Constructs a grid for the tracker, with the given cell width.
	 * 
	 * @param cellWidth
	 *            the width of a cell in the tracker grid.
	 */
	public TrackerGrid(double cellWidth) {
		this.cellWidth = cellWidth;
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
			this.row = row;
			this.col = col;
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

		@Override
		public String toString() {
			return row + " " + col;
		}
	}

	/**
	 * Returns the cell width for this grid.
	 * 
	 * @return the cell width for this grid.
	 */
	public double getCellWidth() {
		return cellWidth;
	}

	/**
	 * Returns the action code corresponding to a movement from the centre to
	 * the given end cell.
	 * 
	 * @param end
	 *            the end cell.
	 * @return the action code for a movement from the centre cell to the end
	 *         cell.
	 */
	public int encodeAction(GridCell end) {
		int rowDelta = end.getRow() - 2;
		int colDelta = end.getCol() - 2;
		return rowDelta * 5 + colDelta + 12;
	}

	/**
	 * Returns the end cell resulting from taking the action with the given
	 * action code from the centre cell.
	 * 
	 * @param actionCode
	 *            the action to take.
	 * @return the end cell after taking the given action from the centre cell.
	 */
	public GridCell decodeAction(int actionCode) {
		return new GridCell(actionCode / 5 - 2, actionCode % 5 - 2);
	}

	/**
	 * Returns the grid cell containing the given point.
	 * 
	 * @param pos
	 *            the point to locate.
	 * @return the grid cell containing the given point.
	 */
	public GridCell getIndex(Point2D pos) {
		int col = (int) Math.floor(+pos.getX() / cellWidth + 0.5);
		int row = (int) Math.floor(-pos.getY() / cellWidth + 0.5);
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
		return new Point2D.Double(+cell.getCol() * cellWidth, -cell.getRow()
				* cellWidth);
	}

	/**
	 * Returns a random point in the given cell.
	 * 
	 * @param cell
	 *            the cell.
	 * @return a random point in the given cell.
	 */
	public Point2D getRandomPoint(GridCell cell) {
		double randX = Math.random() - 0.5;
		double randY = Math.random() - 0.5;
		return new Point2D.Double(+(cell.getCol() + randX) * cellWidth,
				-(cell.getRow() + randY) * cellWidth);
	}
}
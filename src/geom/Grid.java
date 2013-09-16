package geom;

import java.awt.geom.Point2D;

public class Grid {
	public int gridSize;

	public Grid(int gridSize) {
		this.gridSize = gridSize;
	}

	public class GridCell {
		private int row;
		private int col;

		public GridCell(int row, int col) {
			this.row = bound(row);
			this.col = bound(col);
		}

		public int getRow() {
			return row;
		}

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

	public int bound(int index) {
		if (index < 0) {
			index = 0;
		}
		if (index >= gridSize) {
			index = gridSize - 1;
		}
		return index;
	}

	public int getGridSize() {
		return gridSize;
	}

	public int encodeAction(GridCell start, GridCell end) {
		int rowDelta = end.getRow() - start.getRow();
		int colDelta = end.getCol() - start.getCol();
		return rowDelta * 3 + colDelta + 4;
	}

	public GridCell decodeAction(GridCell start, int actionCode) {
		return new GridCell(start.getRow() + actionCode / 3 - 1, start.getCol()
				+ actionCode % 3 - 1);
	}

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

	public GridCell getIndex(Point2D pos) {
		int row = (int) ((1 - pos.getY()) * gridSize);
		int col = (int) (pos.getX() * gridSize);
		return new GridCell(row, col);
	}

	public Point2D getCentre(GridCell cell) {
		return new Point2D.Double((cell.getCol() + 0.5) / gridSize, 1
				- (cell.getRow() + 0.5) / gridSize);
	}
}
package geom;

import java.awt.geom.Point2D;

/**
 * Assorted functions to help with handling the game's geometry.
 * 
 * @author lackofcheese
 * 
 */
public class GeomTools {
	/**
	 * Normalises an angle to the range (-pi, pi]
	 * 
	 * @param angle
	 *            the angle to normalise.
	 * @return the normalised angle.
	 */
	public static double normaliseAngle(double angle) {
		while (angle <= -Math.PI) {
			angle += 2 * Math.PI;
		}
		while (angle > Math.PI) {
			angle -= 2 * Math.PI;
		}
		return angle;
	}

	/**
	 * Linearly interpolates points.
	 * 
	 * @param p0
	 *            the first point <code>(t=0)</code>.
	 * @param p1
	 *            the second point <code>(t=1)</code>.
	 * @param t
	 *            the interpolation parameter.
	 * @return the point interpolated between <code>p0</code> and
	 *         <code>p1</code> for parameter value <code>t</code>.
	 */
	public static Point2D interpolatePoint(Point2D p0, Point2D p1, double t) {
		return new Point2D.Double(p0.getX() * (1 - t) + p1.getX() * t,
				p0.getY() * (1 - t) + p1.getY() * t);
	}

}

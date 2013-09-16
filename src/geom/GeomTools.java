package geom;

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
}

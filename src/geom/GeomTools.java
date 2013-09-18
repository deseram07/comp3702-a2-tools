package geom;

import game.AgentState;
import game.RectRegion;
import game.SensingParameters;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

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
	
	public static Point2D calculateViewPosition(AgentState state) {
		if (!state.hasCamera()) {
			return state.getPosition();
		}
		double heading = state.getHeading();
		double armLength = state.getCameraArmLength();
		return new Vector2D(heading - Math.PI/2, armLength).addedTo(state.getPosition());
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
	 * @param obstacles the list of obstacles to test against.
	 * @return true
	 */
	public static boolean canTurn(Point2D centre, double startHeading,
			double endHeading, double armLength, List<RectRegion> obstacles) {
		Arc2D arc = new Arc2D.Double();
		double startDeg = -Math.toDegrees(startHeading - Math.PI / 2);
		double extentDeg = -Math.toDegrees(GeomTools.normaliseAngle(endHeading
				- startHeading));
		for (int i = 0; i < 2; i++) {
			arc.setArcByCenter(centre.getX(), centre.getY(), armLength,
					startDeg, extentDeg, Arc2D.PIE);
			if (isCollisionFree(arc, obstacles)) {
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
	 *            @param obstacles the obstacles to test against.
	 * @return true iff moving from the start to the end with the given arm
	 *         length is valid.
	 */
	public static boolean canMove(Point2D startPos, Point2D endPos, boolean hasCamera,
			double armLength, List<RectRegion> obstacles) {
		Line2D line = new Line2D.Double(startPos, endPos);
		if (!isCollisionFree(line, obstacles)) {
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
		return isCollisionFree(tf.createTransformedShape(rec), obstacles);
	}

	/**
	 * Returns true iff the observer can see the potential observee.
	 * 
	 * @param observerState
	 *            the state of the observer.
	 * @param observeeState
	 *            the state of the potential observee.
	 * @param sp the sensing parameters of the observer.
	 * @param obstacles the view-obstructing obstacles. 
	 * @param maxDistanceError the maximum allowed error in distance.
	 * @param numCameraArmSteps the number of points to check on the camera arm.
	 * @return true iff the observer can see the potential observee.
	 */
	public static boolean canSee(AgentState observerState, AgentState observeeState,
			SensingParameters sp, List<RectRegion> obstacles,
			double maxDistanceError, int numCameraArmSteps) {
		Point2D observeePos = observeeState.getPosition();
		// Check if the observee can be directly seen.
		if (canSee(observerState, observeePos, sp, obstacles, maxDistanceError)) {
			return true;
		}
		
		// Check if the observee has a camera arm that can be seen.
		if (!observeeState.hasCamera()) {
			return false;
		}
		
		// Check if the observer can see the camera arm.
		Point2D observeeCameraPos = calculateViewPosition(observeeState);
		int count = 0;
		for (int i = 1; i <= numCameraArmSteps; i++) {
			double t = ((double) i) / numCameraArmSteps;
			Point2D p = GeomTools.interpolatePoint(observeePos,
					observeeCameraPos, t);
			if (canSee(observerState, p, sp, obstacles, maxDistanceError)) {
				count += 1;
			}
		}
		return (count * 2 > numCameraArmSteps + 1);
	}

	/**
	 * Returns true iff the given observer can see the given point.
	 *
	 * @param observerState
	 *            the state of the observer.
	 * @param point
	 *            the point.
	 * @param sp the sensing parameters of the observer.
	 * @param obstacles the view-obstructing obstacles. 
	 * @param maxDistanceError the maximum allowed error in distance.
	 * @return true iff the observer can see the potential observee.
	 */
	public static boolean canSee(AgentState observerState, Point2D point, 
			SensingParameters sp, List<RectRegion> obstacles,
			double maxDistanceError) {
		//Point2D observerPos = observerState.getPosition();
		double observerHeading = observerState.getHeading();
		
		Point2D viewPos = calculateViewPosition(observerState);
		Vector2D viewVector = new Vector2D(viewPos, point);

		// Verify the viewing range.
		double distance = viewVector.getMagnitude();
		if (distance < maxDistanceError) {
			return true;
		}
		if (distance > sp.getRange() + maxDistanceError) {
			return false;
		}

		// Verify the viewing angle.
		double viewAngleDelta = GeomTools.normaliseAngle(viewVector
				.getDirection() - observerHeading);
		if (Math.abs(viewAngleDelta) > sp.getAngle() / 2 + maxDistanceError) {
			return false;
		}

		return isCollisionFree(new Line2D.Double(viewPos, point), obstacles);
	}

	/**
	 * Returns true iff the given shape doesn't collide with any obstacles.
	 * 
	 * @param s
	 *            the shape to test.
	 * @param obstacles the obstacles to test against.
	 * @return true iff the given shape doesn't collide with any obstacles.
	 */
	public static boolean isCollisionFree(Shape s, List<RectRegion> obstacles) {
		for (RectRegion obs : obstacles) {
			if (s.intersects(obs.getRect())) {
				return false;
			}
		}
		return true;
	}
}

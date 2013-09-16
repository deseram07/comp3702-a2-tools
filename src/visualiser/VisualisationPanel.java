package visualiser;

import game.Action;
import game.ActionResult;
import game.AgentState;
import game.GameRunner;
import game.RectRegion;
import game.SensingParameters;
import geom.GeomTools;
import geom.Vector2D;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.Timer;

public class VisualisationPanel extends JComponent {
	private static int PIXEL_RADIUS = 5;

	/** UID, as required by Swing */
	private static final long serialVersionUID = -4286532773714402501L;

	private GameRunner gameRunner = new GameRunner();
	private Visualiser visualiser;
	private GameRunner.GameState currentState;

	private AffineTransform translation = AffineTransform.getTranslateInstance(
			0, -1);
	private AffineTransform transform = null;

	private Timer animationTimer;
	private int framePeriod = 200;
	private Integer frameNumber = null;
	private int maxFrameNumber;

	public VisualisationPanel(GameRunner gameRunner, Visualiser visualiser) {
		super();
		this.setBackground(Color.WHITE);
		this.setOpaque(true);
		this.gameRunner = gameRunner;
		this.visualiser = visualiser;
	}

	public void setPeriod(int period) {
		this.framePeriod = period;
		if (animationTimer != null) {
			animationTimer.setDelay(framePeriod);
		}
	}

	public void resetGame() {
		if (!gameRunner.setupLoaded()) {
			return;
		}
		gameRunner.initialise();
		if (animationTimer != null) {
			animationTimer.stop();
		}
		maxFrameNumber = gameRunner.getTurnNo();
		gotoFrame(0);
		animationTimer = new Timer(framePeriod, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int newFrameNumber = frameNumber + 1;
				if (newFrameNumber > maxFrameNumber) {
					stepGame();
				} else {
					gotoFrame(newFrameNumber);
					if (newFrameNumber == maxFrameNumber
							&& gameRunner.gameComplete()) {
						animationTimer.stop();
						visualiser.setPlaying(false);
					}
				}
			}
		});
		visualiser.setPlaying(false);
		visualiser.updateMaximum();
	}

	public void stepGame() {
		if (!gameRunner.setupLoaded()) {
			return;
		}
		gameRunner.undoTo(frameNumber);
		gameRunner.simulateTurn();
		if (gameRunner.gameComplete()) {
			animationTimer.stop();
			visualiser.setPlaying(false);
		}
		maxFrameNumber = gameRunner.getTurnNo();
		visualiser.updateMaximum();
		gotoFrame(frameNumber + 1);
	}

	public void gotoFrame(int frameNumber) {
		if (!gameRunner.setupLoaded()
				|| (this.frameNumber != null && this.frameNumber == frameNumber)) {
			return;
		}
		this.frameNumber = frameNumber;
		visualiser.setFrameNumber(frameNumber);
		currentState = gameRunner.getStateSequence().get(frameNumber);
		visualiser.updateInfoText();
		visualiser.updateTable();
		repaint();
	}

	public int getFrameNumber() {
		return frameNumber;
	}

	public GameRunner.GameState getCurrentState() {
		return currentState;
	}

	public void playPauseAnimation() {
		if (!gameRunner.setupLoaded()) {
			return;
		}
		if (animationTimer.isRunning()) {
			animationTimer.stop();
			visualiser.setPlaying(false);
		} else {
			if (gameRunner.gameComplete()) {
				gotoFrame(0);
			}
			animationTimer.start();
			visualiser.setPlaying(true);
		}
	}

	public void stopAnimation() {
		if (!gameRunner.setupLoaded()) {
			return;
		}
		if (animationTimer != null) {
			animationTimer.stop();
		}
		visualiser.setPlaying(false);
		frameNumber = null;
	}

	public GameRunner getRunner() {
		return gameRunner;
	}

	public void calculateTransform() {
		transform = AffineTransform.getScaleInstance(getWidth(), -getHeight());
		transform.concatenate(translation);
	}

	public void paintState(Graphics2D g2, AgentState state,
			SensingParameters sp, Color playerColor, Color viewColor) {
		if (state == null) {
			return;
		}
		Color c;

		Point2D agentPos = state.getPosition();
		Point2D viewPos = agentPos;
		if (sp.hasCamera()) {
			double armDirection = state.getHeading() - Math.PI / 2;
			double armLength = state.getCameraArmLength();
			viewPos = new Vector2D(armLength, armDirection).addedTo(agentPos);
			Line2D line = new Line2D.Double(agentPos, viewPos);
			c = g2.getColor();
			g2.setColor(playerColor);
			g2.setStroke(new BasicStroke(1));
			g2.draw(transform.createTransformedShape(line));
			g2.setColor(c);
		}
		c = g2.getColor();
		g2.setColor(playerColor);
		Point2D tp = transform.transform(agentPos, null);
		g2.fill(new Ellipse2D.Double(tp.getX() - PIXEL_RADIUS, tp.getY()
				- PIXEL_RADIUS, PIXEL_RADIUS * 2, PIXEL_RADIUS * 2));
		g2.setColor(c);

		double viewAngle = sp.getAngle();
		double startAngle = state.getHeading() - viewAngle / 2;
		double viewRange = sp.getRange();
		Arc2D arc = new Arc2D.Double();
		arc.setArcByCenter(viewPos.getX(), viewPos.getY(), viewRange,
				-Math.toDegrees(startAngle), -Math.toDegrees(viewAngle),
				Arc2D.PIE);
		c = g2.getColor();
		g2.setColor(viewColor);
		g2.fill(transform.createTransformedShape(arc));
		g2.setColor(c);
	}

	public void paintGrid(Graphics2D g2) {
		g2.setColor(new Color(1.0f, 0.0f, 0.0f, 0.1f));
		g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_MITER, 10.0f, new float[] { 5.0f }, 0.0f));
		int gridSize = gameRunner.getTargetPolicy().getGridSize();
		for (int i = 1; i < gridSize; i++) {
			double v = (double) i / gridSize;
			Line2D line = new Line2D.Double(v, 0, v, 1);
			g2.draw(transform.createTransformedShape(line));
			line = new Line2D.Double(0, v, 1, v);
			g2.draw(transform.createTransformedShape(line));
		}
	}

	public void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		if (!gameRunner.setupLoaded()) {
			return;
		}
		calculateTransform();
		Graphics2D g2 = (Graphics2D) graphics;
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, getWidth(), getHeight());

		paintGrid(g2);

		/*
		 * Color c = g2.getColor(); if (frameNumber > 0) { Action a =
		 * gameRunner.getActionResults().get(frameNumber-1).getAction(); double
		 * startHeading = a.getStartState().getHeading(); Point2D startPos =
		 * a.getStartState().getPosition(); double endHeading = a.getHeading();
		 * Point2D endPos = a.getResultingState().getPosition(); double
		 * armLength = a.getNewCameraArmLength();
		 * 
		 * double distance = startPos.distance(endPos);
		 * 
		 * Rectangle2D rec = new Rectangle2D.Double(startPos.getX(),
		 * startPos.getY(), armLength, distance); AffineTransform tf =
		 * AffineTransform.getRotateInstance( endHeading - Math.PI / 2,
		 * startPos.getX(), startPos.getY()); g2.setColor(new Color(1.0f, 0.0f,
		 * 1.0f, 0.2f)); Shape s = tf.createTransformedShape(rec);
		 * g2.fill(transform.createTransformedShape(s));
		 * 
		 * Arc2D arc = new Arc2D.Double(); double startDeg =
		 * -Math.toDegrees(startHeading - Math.PI/2); double extentDeg =
		 * -Math.toDegrees(GeomTools.normaliseAngle(endHeading - startHeading));
		 * System.out.println(startDeg + " " + extentDeg); g2.setColor(new
		 * Color(1.0f, 1.0f, 0.0f, 0.2f)); for (int i = 0; i < 2; i++) {
		 * arc.setArcByCenter(startPos.getX(), startPos.getY(), armLength,
		 * startDeg, extentDeg, Arc2D.PIE);
		 * g2.fill(transform.createTransformedShape(arc)); g2.setColor(new
		 * Color(0.0f, 1.0f, 1.0f, 0.2f)); if (extentDeg <= 0) { extentDeg +=
		 * 360; } else { extentDeg -= 360; } } } g2.setColor(c);
		 */

		List<RectRegion> obstacles = gameRunner.getObstacles();
		g2.setColor(Color.RED);
		for (RectRegion obs : obstacles) {
			Shape transformed = transform.createTransformedShape(obs.getRect());
			g2.fill(transformed);
		}

		g2.setColor(new Color(0.0f, 1.0f, 0.0f, 0.5f));
		Shape transformed = transform.createTransformedShape(gameRunner
				.getGoalRegion().getRect());
		g2.fill(transformed);

		AgentState[] states = currentState.getPlayerStates();
		SensingParameters sp = gameRunner.getTrackerSensingParams();
		paintState(g2, states[0], sp, Color.BLUE, new Color(0.0f, 0.0f, 1.0f,
				0.2f));

		sp = gameRunner.getTargetSensingParams();
		for (int i = 1; i < states.length; i++) {
			paintState(g2, states[i], sp, Color.BLACK, new Color(0.0f, 0.0f,
					0.0f, 0.2f));
		}
	}
}

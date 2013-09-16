package game;

import java.util.Scanner;

public class SensingParameters {
	private boolean hasCamera;
	private double range;
	private double angle;
	private double minLength;
	private double maxLength;
	
	public SensingParameters(SensingParameters otherParams) {
		this.hasCamera = otherParams.hasCamera;
		this.range = otherParams.range;
		this.angle = otherParams.angle;
		this.minLength = otherParams.minLength;
		this.maxLength = otherParams.maxLength;
	}
	
	public SensingParameters(boolean hasCamera, String line) {
		this.hasCamera = hasCamera;
		Scanner s = new Scanner(line);
		if (hasCamera) {
			minLength = s.nextDouble();
			maxLength = s.nextDouble();
		}
		angle = Math.toRadians(s.nextDouble());
		range = s.nextDouble();
		s.close();
	}
	
	public SensingParameters(double range, double angle) {
		this.hasCamera = false;
		this.range = range;
		this.angle = angle;
	}
	
	public SensingParameters(double range, double angle, double minLength, double maxLength) {
		this.hasCamera = true;
		this.range = range;
		this.angle = angle;
		this.minLength = minLength;
		this.maxLength = maxLength;
	}

	public boolean hasCamera() {
		return hasCamera;
	}

	public double getRange() {
		return range;
	}

	public double getAngle() {
		return angle;
	}

	public double getMinLength() {
		return minLength;
	}

	public double getMaxLength() {
		return maxLength;
	}
	
}

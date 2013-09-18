package game;

public class TrackerAction extends Action {
	private boolean isHQCall;
	
	public TrackerAction(AgentState startState, boolean isHQCall) {
		super(startState);
		this.isHQCall = isHQCall;
	}
	
	public TrackerAction(AgentState startState, double newCameraArmLength) {
		super(startState, newCameraArmLength);
		this.isHQCall = false;
	}
	
	public TrackerAction(AgentState startState, double heading, double distance) {
		super(startState, heading, distance);
		this.isHQCall = false;
	}
	
	public boolean isHQCall() {
		return isHQCall;
	}
}

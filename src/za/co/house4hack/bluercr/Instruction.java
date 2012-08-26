package za.co.house4hack.bluercr;

public class Instruction {

	public enum InstructionCommand {
	     LEFT, RIGHT, FORWARD, STOP
	}
	
    public InstructionCommand command;
    public double duration;
    public boolean active = false;
    
    public Instruction(InstructionCommand command, double duration){
    	this.command = command;
    	this.duration = duration;
    }
    
    @Override
    public Instruction clone(){
		return new Instruction(command,duration);
    	
    }
    

	String lookupCommandText(InstructionCommand command) {
		switch (command) {
		case FORWARD:
			return "Forward";
		case LEFT:
			return "Left";
		case RIGHT:
			return "Right";
		case STOP:
			return "Stop";
		}
		return "Unknown";
	}

	int lookupDrawable(InstructionCommand command) {
		switch (command) {
		case FORWARD:
			return R.drawable.forward;
		case LEFT:
			return R.drawable.left;
		case RIGHT:
			return R.drawable.right;
		case STOP:
			return R.drawable.stop;
		}
		return 0;
	}
	
	String lookupBT(InstructionCommand command) {
		switch (command) {
		case FORWARD:
			return "f";
		case LEFT:
			return "lf";
		case RIGHT:
			return "rf";
		case STOP:
			return "s";
		}
		return "Unknown";
	}
	
}

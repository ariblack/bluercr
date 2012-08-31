package za.co.house4hack.bluercr;

public class Instruction {

	public enum InstructionCommand {
	     LEFT, RIGHT, FORWARD, STOP, REVERSE
	}

	public static final String STOPCOMMAND = "vzsha";
	public static final double DEFAULT_DURATION = 0.5;
	
    public InstructionCommand command;
    public double duration;
    public boolean active = false;
    
    public Instruction(InstructionCommand command, double duration){
    	this.command = command;
    	this.duration = duration;
    }

    public Instruction(InstructionCommand command){
    	this.command = command;
    	this.duration = DEFAULT_DURATION;
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
		case REVERSE:
			return "Reverse";
			
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
		case REVERSE:	
			return R.drawable.back;
		}
		return 0;
	}
	
	String lookupBT(InstructionCommand command) {
		switch (command) {
		case FORWARD:
			return "vfg";
		case LEFT:
			return "lfleg";
		case RIGHT:
			return "rfreg";
		case REVERSE:
			return "vdg";			
		case STOP:
			return STOPCOMMAND;
		}
		return "Unknown";
	}
	
}

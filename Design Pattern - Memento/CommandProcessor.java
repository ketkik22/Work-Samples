package Bricks;
import java.util.*;

public class CommandProcessor {

	private Stack<Command> undoStack = new Stack<Command>();
	private Stack<Command> redoStack = new Stack<Command>();
	private static CommandProcessor theCommandProcessor = null;
	public static CommandProcessor makeCommandProcessor() {
		if (theCommandProcessor == null) {
			theCommandProcessor = new CommandProcessor();
		}
		return theCommandProcessor;
	}

	private CommandProcessor() { }

	public static void error(String gripe) {
		System.err.println(gripe);
	}


	public void execute(Command cmmd) {
		try {
			cmmd.execute();
			redoStack.clear();
			if (cmmd.isUndoable()) {
				undoStack.push(cmmd);
			}
		} catch(Exception e) {
			error(e.getMessage());
		}
	}

	public void undo() {
		try {
			if (undoStack.size() == 0) {
				error("Nothing to undo!");
				return;
			}
			Command c = undoStack.pop();
			c.undo();
			redoStack.push(c);
		} catch(Exception e) {
			error(e.getMessage());
		}
	}

	public void redo() {
		if (redoStack.size() == 0) {
			error("Nothing to redo!");
			return;
		}
		Command c = redoStack.pop();
		execute(c);
	}
}
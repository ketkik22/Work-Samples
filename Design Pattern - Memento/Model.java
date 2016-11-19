package Bricks;
import java.util.*;
import java.io.*;

abstract class Model extends Observable implements Serializable {
	protected String fileName = null; // file where this model is stored
	protected boolean unsavedChanges = false; // edited since last save?
	// getters and setters:
	public String getFileName() { return fileName; }
	public void setFileName(String fn) { fileName = fn; }
	public boolean hasUnsavedChanges() { return unsavedChanges; }
	public void setUnsavedChanges(boolean flag) { unsavedChanges = flag; }
	// overridables:
	public abstract Memento makeMemento();
	public abstract void accept(Memento m);
}
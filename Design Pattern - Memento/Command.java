package Bricks;
abstract class Command 
{
	protected Model model;
	protected boolean undoable = false;
	private Memento memento;

	public boolean isUndoable() 
	{ 
		return undoable; 
	}
	
	public Command(Model m) 
	{
		model = m;
	}
	
	public  void execute() throws Exception 
	{
		if (undoable) 
		{
			memento = model.makeMemento();
		}
	}
	
	public void undo() throws Exception 
	{
		if (undoable) 
		{
			model.accept(memento);
		}
	}
}

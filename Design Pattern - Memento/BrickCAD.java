package Bricks;
import java.util.*;

class SetHeight extends Command 
{
	private int newHeight;

	public SetHeight(Brick b, int newHeight) 
	{
		super(b);
		this.newHeight = newHeight;
		undoable = true; // added
	}

	public void execute() throws Exception 
	{
		// ???
		super.execute();
		((Brick)(model)).setHeight(newHeight);
	}
}

class Brick extends Model 
{
	private int height, width, length;

	public Brick(int h, int w, int l) 
	{
		height = h;
		width = w;
		length = l;
	}

	private class BrickMemento implements Memento 
	{
		int height, width, length;
		
		public BrickMemento(int height, int width, int length) 
		{
			// TODO Auto-generated constructor stub
			this.height = height;
			this.width = width;
			this.length = length;
		}
	}

	public int getHeight() 
	{
		return height;
	}

	public void setHeight(int h) 
	{
		// change height & notify observers (a little tricky)
		this.height = h;
		this.setChanged();
		this.notifyObservers();
	}

	public void accept(Memento m) 
	{
		// restore state & notify observers
		this.height = ((BrickMemento)(m)).height;
		this.length = ((BrickMemento)(m)).length;
		this.width = ((BrickMemento)(m)).width;
		setChanged();
		notifyObservers();
	}

	public Memento makeMemento() 
	{
		return new BrickMemento(height, width, length);
	}
}

class HeightView implements Observer 
{
	public void update(Observable subject, Object msg) 
	{
		if (subject instanceof Brick) 
		{
			System.out.println("height = " + ((Brick)subject).getHeight());
		}
	}
}

public class BrickCAD 
{
	public static void main(String args[]) 
	{
		try 
		{
			Brick brick = new Brick(10, 20, 30);
			HeightView view = new HeightView();
			brick.addObserver(view);
			Command cmmd = new SetHeight(brick, 15);
			cmmd.execute();
			cmmd.undo();
		} 
		catch (Exception e) 
		{
			System.out.println(e);
		}
	}
}


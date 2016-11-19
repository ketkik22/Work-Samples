package Bricks;
import java.util.*;

class AddCommand extends Command 
{
	double result;
	public AddCommand(Calculator calc, double result)
	{
		super(calc);
		this.result = result;
		undoable = true;
	}
	// ???

	@Override
	public void execute() throws Exception 
	{
		// TODO Auto-generated method stub
		super.execute();
		((Calculator)model).add(result);
	}
}

class ClearCommand extends Command 
{
	public ClearCommand(Calculator calc)
	{
		super(calc);
		undoable = true;
	}

	@Override
	public void execute() throws Exception 
	{
		// TODO Auto-generated method stub
		super.execute();
		((Calculator)model).clear();
	}
	// ???
}

class CalculatorView implements Observer 
{
	public CalculatorView(Calculator cal)
	{
		cal.addObserver(this);
	}

	public void update(Observable subject, Object msg) 
	{
		Calculator calc = (Calculator)subject;
		System.out.println("result = " + calc.getResult());
		System.out.println("# steps = " + calc.getNumSteps());
	}
}

class Calculator extends Model {

	private double result = 0.0; // accumulator
	private int numSteps = 0;    // # of arith ops performed

	public int getNumSteps() { return numSteps; }
	public double getResult() { return result; }


    // mul, sub, div would be similar
	public void add(double x) 
	{
		result += x;
		numSteps++;
		setChanged();
		notifyObservers();
	}
    
	// sets all fields back to 0
	public void clear() 
	{
		result = 0.0;
		numSteps = 0;
		setChanged();
		notifyObservers();
		
	}

	private class CalculatorAppMemento implements Memento
	{
		double result;
		int numSteps;
		
		public CalculatorAppMemento(double r, int n)
		{
			this.result = r;
			this.numSteps = n;
		}
	}
	
	public void accept(Memento m) 
	{
		// restore state & notify observers
		this.result = ((CalculatorAppMemento)(m)).result;
		//this.numSteps = ((CalculatorAppMemento)(m)).numSteps;
		this.numSteps++;
		setChanged();
		notifyObservers();
		
	}

	public Memento makeMemento() 
	{
		return new CalculatorAppMemento(result, numSteps);
	}
}



public class CalculatorApp 
{
   public static void main(String[] args) {
	   try {
		   Calculator calc = new Calculator();
		   CalculatorView view = new CalculatorView(calc);
		   Command cmmd1 = new AddCommand(calc, 3.14);
		   Command cmmd2 = new AddCommand(calc, 2.78);
		   Command cmmd3 = new ClearCommand(calc);
		   cmmd1.execute();
		   cmmd2.execute();
		   cmmd2.undo();
		   cmmd2.execute(); // redo cmmd2
		   cmmd3.execute();
		   cmmd3.undo();
		   cmmd2.undo();
		   cmmd1.undo();
	}  
	   catch (Exception e) 
	   {
		System.out.println(e);
	}
   }
}
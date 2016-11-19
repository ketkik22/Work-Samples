package Bricks;
import javax.swing.*;
import java.util.*;
import java.awt.event.*;



public abstract class View extends JPanel implements Observer, ActionListener 
{
	protected Model model;
	protected CommandProcessor cp;
	protected String title = "Model Viewer";
	public View(Model m) {
		model = m;
		model.addObserver(this);
		cp = CommandProcessor.makeCommandProcessor();
	}

	public void display() 
	{
	  JFrame frame = new JFrame();
      //frame.setSize(300, 500);
      frame.setTitle(title);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.add(this);
      frame.pack();
      frame.setVisible(true);
   }

}
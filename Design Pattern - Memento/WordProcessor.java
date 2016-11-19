package Bricks;import javax.swing.*;
import java.util.*;
import java.awt.event.*;

class InsertText extends Command {
	private String text;

		public InsertText(Document doc, String text) {
			super(doc);
			this.text = text;
			undoable = true;
		}

		public void execute() throws Exception {
			super.execute();
			((Document)model).insert(text);
	}

}

class SetCursor extends Command {
	private int pos;

		public SetCursor(Document doc, int pos) {
			super(doc);
			this.pos = pos;
			undoable = true;
		}

		public void execute() throws Exception {
			super.execute();
			((Document)model).setCursor(pos);
	}

}

class DocView implements Observer {
	public DocView(Document doc) {
		doc.addObserver(this);
	}
	public void update(Observable subject, Object msg) {
		Document doc = (Document)subject;
		int cursor = doc.getCursor();
	    //JOptionPane.showMessageDialog(null, "" + doc.getText());
	    System.out.println(doc.getText().substring(0, cursor) +  "^" + doc.getText().substring(cursor));
	}
}

class Document extends Model {
   private StringBuffer text = new StringBuffer(128);
   private int cursor = 0;

   public void insert(String word) {
	   text.insert(cursor, word);
	   cursor = cursor + word.length();
	   setChanged();
	   notifyObservers();
   }

   public void setCursor(int pos) {
	   cursor = pos;
	   setChanged();
	   notifyObservers();
   }

   public String getText() {
	   return text.toString();
   }

   public int getCursor() {
	   return cursor;
   }

   private class DocumentMemento implements Memento {
	   String text;
	   int cursor;
   }

   public Memento makeMemento() {
	   DocumentMemento result = new DocumentMemento();
	   result.text = this.text.toString();
	   result.cursor = this.cursor;
	   return result;
   }

   public void accept(Memento m) {
	   if (m instanceof DocumentMemento) {
		   DocumentMemento dm = (DocumentMemento)m;
		   this.text = new StringBuffer(dm.text);
		   this.cursor = dm.cursor;
		   setChanged();
		   notifyObservers();
	   }
   }
}

public class WordProcessor {
   public static void main(String[] args) {
	   try {
		Document doc = new Document();
	   	Command cmmd1 = new InsertText(doc, "A man, a plan, a canal, Panama!");
	   	DocView view = new DocView(doc);
	   	cmmd1.execute();
	   	Command cmmd2 = new SetCursor(doc, 14);
	   	cmmd2.execute();
	   	Command cmmd3 = new InsertText(doc, "ccc");
	   	cmmd3.execute();
	   	cmmd3.undo();
	   	cmmd2.undo();
	   	cmmd1.undo();
	}  catch (Exception e) {
		System.out.println(e);
	}
   }
}
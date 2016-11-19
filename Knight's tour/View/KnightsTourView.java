package KnightsTour.View;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Scanner;
import java.util.Vector;

import javax.imageio.ImageIO;
//import javax.swing.AbstractCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
//import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
//import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
//import javax.swing.table.TableCellEditor;
//import javax.swing.table.TableCellRenderer;

import KnightsTour.Action.*;

public class KnightsTourView 
{
	JFrame frame;
	JPanel contentPane;
	JTable table;
	JScrollPane scrollPane;
	
	Dimension dim;
	Toolkit toolkit;
	GridBagConstraints c = new GridBagConstraints();
	
	BufferedImage img1, img2, img3;
	ImageIcon imgIcon1, imgIcon2, imgIcon3;
	
	String columns[];
	Integer[][] knightsTour; 
	int n;
	
	public KnightsTourView(int n, Integer[][] tour)
	{
		this.knightsTour = new Integer[n][n];
		knightsTour = tour;
		this.n = n;
				
		toolkit = Toolkit.getDefaultToolkit();
		dim = toolkit.getScreenSize();
		
		try
		{
			File file = new File("image.jpg");
			img1 = ImageIO.read(file);
			
			file = new File("image1.jpg");
			img2 = ImageIO.read(file);
			
			file = new File("image2.jpg");
			img3 = ImageIO.read(file);
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
		
		imgIcon1 = new ImageIcon(img1);
		imgIcon2 = new ImageIcon(img2);
		imgIcon3 = new ImageIcon(img3);
		int height = img1.getHeight();
		int width = img1.getWidth() + 30;
		
		columns = new String[n];
		for(int i=0; i<n; i++)
			columns[i] = String.valueOf(i);
		
		//System.out.println(dim.width + " " + dim.height);
		
		frame = new JFrame("Knight's tour");
		frame.setLayout(new BorderLayout());
		frame.setSize(dim.width, dim.height);
		
		contentPane = new JPanel(new GridBagLayout());
		
		table = new JTable(new MyTableModel(n, columns));
		table.setVisible(true);
		table.setEnabled(false);
		table.setRowHeight(height);
			
		
		for(int column=0; column<n; column++)
		{
			table.getColumnModel().getColumn(column).setWidth(width);
		}
		
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		//c.insets = new Insets(dim.height*5/100, dim.width*5/100, dim.height*5/100, dim.width*5/100);
		c.insets = new Insets(0, 0, 0, 0);
		contentPane.add(table, c);
		
		scrollPane = new JScrollPane(contentPane);
		frame.add(scrollPane, BorderLayout.CENTER);
		frame.setVisible(true);
		
		displaySolution();
	}
	
	class MyTableModel extends DefaultTableModel
	{
		public MyTableModel(int n, String[] columns) 
		{
			// TODO Auto-generated constructor stub
			super(columns, n);
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) 
		{
			// TODO Auto-generated method stub
			return ImageIcon.class;
		}

		@Override
		public void setValueAt(Object value, int row, int column) 
		{
			// TODO Auto-generated method stub
			if(value instanceof ImageIcon)
			{
				//initialize();
				Vector rowData = (Vector)getDataVector().get(row);
	        	rowData.set(column, value);
	        	fireTableCellUpdated(row, column);
			}
		}

		@Override
		public boolean isCellEditable(int row, int column) 
		{
			// TODO Auto-generated method stub
			return false;
		}
	}
	
	public void displaySolution()
	{
		try
		{
			int count = 1;
			boolean flag = false;
			while(count<=n*n)
			{
				initialize();
				int row = 0, column = 0;
				flag = false;
				
				for(int i=0; i<n; i++)
				{
					for(int j=0; j<n; j++)
					{
						if(knightsTour[i][j] == count)
						{
							flag = true;
							row = i;
							column = j;
							break;
						}
					}
					if(flag)
						break;
				}
			
				table.setValueAt(imgIcon1, row, column);
				Thread.sleep(500);
				count++;
				
			}
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
	}
	
	public void initialize()
	{
		
		for(int column=0; column<n; column++)
		{
			for(int row=0; row<n; row++)
			{
				if(row%2 != 0 && column%2 == 0)
				{
					table.setValueAt(imgIcon2, row, column);
				}
				else if(row%2 == 0 && column%2 !=0)
				{
					table.setValueAt(imgIcon2, row, column);
				}
				else
				{
					table.setValueAt(imgIcon3, row, column);
				}
			}
		}
	}
		
	public static void main(String[] args) 
	{
		Scanner sc = new Scanner(System.in);
		System.out.print("Please enter the size of chessboard (nxn) = ");
		int flag=0;
		int n = sc.nextInt();
		Integer[][] knightsTour = new Integer[n][n];
		KnightsTour obj = new KnightsTour(n);
		
		if(n>=10)
		{
			obj.initialize(n/2);
			knightsTour=obj.buildSolution();
			obj.displaySol(knightsTour);
		}
		else
		{
			 for(int i=0;i<n&&flag!=1;i++)
				{
					for(int j=0;j<n;j++)
					{
						obj.possibleTour= new Integer[n][n];
				        obj.initialize(n);
				        if(obj.getPossibleMoves(i, j,0,n))
				        {
				        	if(obj.chkTour())
				        	 {
				        	    flag=1;
				        		break;
				        	 }
				        }
				        
					}
				}
		        if(obj.paths.size()==0)
		        	System.out.println(" No Path found: ");
		        else
		        {
		        if(obj.printMap(knightsTour))
		        	System.out.println("Closed Tour");
		        else
		        	System.out.println("Open Tour");
		        }
		}
	
		
		new KnightsTourView(n, knightsTour);
	}
}

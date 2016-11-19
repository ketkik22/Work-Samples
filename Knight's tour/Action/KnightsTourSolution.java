package KnightsTour.Action;
import java.util.Scanner;

public class KnightsTourSolution 
{

	int possibleTour[][];
	int currPath;
	int n;
	public KnightsTourSolution(int n)
	{
	  possibleTour= new int[n][n];
		for(int i=0; i<n; i++)
			for(int j=0; j<n; j++)
				possibleTour[i][j] = 0;
		
		this.n = n;
		
	}
	public void initialize()
	{
		
		for(int i=0; i<n; i++)
			for(int j=0; j<n; j++)
				possibleTour[i][j] = 0;
	}
	
    public void display()
    {
    	for (int i = 0; i < possibleTour.length; i++) {
			for (int j = 0; j < possibleTour.length; j++) 
			{
				if(possibleTour[i][j]<=9)
				System.out.print("   " +"0"+possibleTour[i][j]);
				else
					System.out.print("   " +possibleTour[i][j]);
			}
			System.out.print("\n");
    	}
    }
			
	public boolean getPossibleMoves(int i, int j,int cnt)
	{
		if(possibleTour[i][j]!=0)
			return false;
		
		else
		{
			
			possibleTour[i][j]=currPath++;
			if(cnt>=((n*n)-1))
					return true;
			 if(0 <= (i+1) && 0 <= (j+2) && (i+1) < n && (j+2) < n && getPossibleMoves((i+1),(j+2),(cnt+1)))
			 return true;
			 
			 if(0 <= (i+2) && 0 <= (j+1) && (i+2) < n && (j+1) < n && getPossibleMoves((i+2),(j+1),(cnt+1)))
				 return true;
			 if(0 <= (i-2) && 0 <= (j-1) && (i-2) < n && (j-1) < n && getPossibleMoves((i-2),(j-1),(cnt+1)))
			 {
				 return true;
			 }
			 if(0 <= (i-1) && 0 <= (j-2) && (i-1) < n && (j-2) < n && getPossibleMoves((i-1),(j-2),(cnt+1)))
			 {
				 return true;
			 }
			 if(0 <= (i-2) && 0 <= (j+1) && (i-2) < n && (j+1) < n && getPossibleMoves((i-2),(j+1),(cnt+1)))
			 {
				 return true;
			 }
			 if(0 <= (i+2) && 0 <= (j-1) && (i+2) < n && (j-1) < n && getPossibleMoves((i+2),(j-1),(cnt+1)))
				 return true;
			 if(0 <= (i-1) && 0 <= (j+2) && (i-1) < n && (j+2) < n  && getPossibleMoves((i-1),(j+2),(cnt+1)))
				 return true;
			 if(0 <= (i+1) && 0 <= (j-2) && (i+1) < n && (j-2) < n && getPossibleMoves((i+1),(j-2),(cnt+1)))
				 return true;
			 
			 possibleTour[i][j]=0;
			 currPath=currPath-1;
			 return false;
		}
	}
	public static void main(String args[])
	{
		Scanner sc = new Scanner(System.in);
		System.out.print("Please enter the size of chessboard (nxn) = ");
		int n = sc.nextInt();
		KnightsTourSolution obj = new KnightsTourSolution(n);
		obj.initialize();
        if(obj.getPossibleMoves(2, 1,0))
	    obj.display();
         else
	        System.out.println("No Path Found");
	/*	for(int i=0;i<n;i++)
		{
			for(int j=0;j<n;j++)
			{
		        obj.initialize();
		        if(obj.getPossibleMoves(i, j,0))
			     obj.display();
		         else
			        System.out.println("No Path Found");
			}
		}*/
	}
	
}

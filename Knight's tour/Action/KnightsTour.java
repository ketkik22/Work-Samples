package KnightsTour.Action;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class KnightsTour
{
	public Integer possibleTour[][];
	int currPath;
	int n;
	int chunkSize;
	HashMap<Integer, HashMap<String, Integer[][]>> tours;
	public HashMap<Integer[][],Boolean> paths=new HashMap<Integer[][],Boolean>();
	int[] visited= new int[4];
	public KnightsTour(int n)
	{
		this.n = n;
		this.currPath = 1;
		tours = new HashMap<Integer, HashMap<String, Integer[][]>>();
		possibleTour= new Integer[n][n];
		for(int i=0; i<n; i++)
			for(int j=0; j<n; j++)
				possibleTour[i][j] = 0;
				
	}
	
	public void initialize(int size)
	{
		currPath = 1;
		for(int i=0;i<4;i++)
			visited[i]=0;
		possibleTour = new Integer[size][size];
		for(int i=0; i<size; i++)
			for(int j=0; j<size; j++)
				possibleTour[i][j] = 0;
	}
	public void init(Integer[][] tour)
	{
		for(int i=0; i<n; i++)
			for(int j=0; j<n; j++)
				tour[i][j] = 0;
		
		for(int i=0;i<4;i++)
			visited[i]=0;
	}
	public void display(Integer[][] possibleTour)
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
    public boolean printMap(Integer[][] solution)
	{
    	boolean flag=false;
		for(Map.Entry<Integer[][],Boolean> entry : paths.entrySet())
		{
			if(flag)
				break;
			Integer sol[][]=entry.getKey();
			Boolean val=entry.getValue();
			if(val)
			{
				System.out.println("Closed tour: ");
				for(int i=0;i<n;i++)
				{
					for(int j=0;j<n;j++)
					{
						solution[i][j]=sol[i][j];
					}
				}
				flag=true;
				return true;
				
			}
			else
			{
				System.out.println("Open Tour: ");
				for(int i=0;i<n;i++)
				{
					for(int j=0;j<n;j++)
					{
						solution[i][j]=sol[i][j];
					}
				}
			}
			display(sol);
		}
		return flag;
	}
    public boolean chkTour()
	{
		
		int i,j,iStartIndex=0,jStartIndex=0,iLastIndex = 0,jLastIndex=0;
		for(i=0;i<possibleTour.length;i++)
			for(j=0;j<possibleTour.length;j++)
			{
				if(possibleTour[i][j]==(n*n))
				{
					iLastIndex=i;
					jLastIndex=j;
				}
				if(possibleTour[i][j]==1)
				{
					iStartIndex=i;
					jStartIndex=j;
				}
			}
		  if((iStartIndex+2)==iLastIndex&&(jStartIndex+1)==jLastIndex)
		  {
			  paths.put(possibleTour, true);
			  return true;
		  }
		  if((iStartIndex+1)==iLastIndex&&(jStartIndex+2)==jLastIndex)
		  {
			  paths.put(possibleTour, true);
			  return true;
		  }
		  if((iStartIndex-1)==iLastIndex&&(jStartIndex+2)==jLastIndex)
		  {
			  paths.put(possibleTour, true);
			  return true;
		  }
		  if((iStartIndex-2)==iLastIndex&&(jStartIndex+1)==jLastIndex)
		  {
			  paths.put(possibleTour, true);
			  return true;
		  }
		  if((iStartIndex-2)==iLastIndex&&(jStartIndex-1)==jLastIndex)
		  {
			  paths.put(possibleTour, true);
			  return true;
		  }
		  if((iStartIndex-1)==iLastIndex&&(jStartIndex-2)==jLastIndex)
		  {
			  paths.put(possibleTour, true);
			  return true;
		  }
		  if((iStartIndex+1)==iLastIndex&&(jStartIndex-2)==jLastIndex)
		  {
			  paths.put(possibleTour, true);
			  return true;
		  }
		  if((iStartIndex+2)==iLastIndex&&(jStartIndex-1)==jLastIndex)
		  {
			  paths.put(possibleTour, true);
			  return true;
		  }
		  
		  paths.put(possibleTour, false);
		  return false;
	}
			
	public boolean getPossibleMoves(int i, int j, int cnt,int size)
	{
		if(possibleTour[i][j] != 0)
		{
			return false;
		}

		possibleTour[i][j] = currPath++;
		
		if(cnt >= ((size*size)-1))
		{
			return true;
		}
		if(0 <= (i+2) && 0 <= (j+1) && (i+2) < size && (j+1) < size && getPossibleMoves((i+2),(j+1),(cnt+1),size))
		{
			return true;
		}
		if(0 <= (i+1) && 0 <= (j+2) && (i+1) < size && (j+2) < size && getPossibleMoves((i+1),(j+2),(cnt+1),size))
		{
			return true;
		}
		if(0 <= (i-1) && 0 <= (j+2) && (i-1) < size && (j+2) < size  && getPossibleMoves((i-1),(j+2),(cnt+1),size))
		{
			return true;
		}
		if(0 <= (i-2) && 0 <= (j+1) && (i-2) < size && (j+1) < size && getPossibleMoves((i-2),(j+1),(cnt+1),size))
		{
			return true;
		}
		if(0 <= (i+1) && 0 <= (j-2) && (i+1) < size && (j-2) < size && getPossibleMoves((i+1),(j-2),(cnt+1),size))
		{
			return true;
		}
		if(0 <= (i+2) && 0 <= (j-1) && (i+2) < size && (j-1) < size && getPossibleMoves((i+2),(j-1),(cnt+1),size))
		{
			return true;
		}
		if(0 <= (i-2) && 0 <= (j-1) && (i-2) < size && (j-1) < size && getPossibleMoves((i-2),(j-1),(cnt+1),size))
		{
			return true;
		}
		if(0 <= (i-1) && 0 <= (j-2) && (i-1) < size && (j-2) < size && getPossibleMoves((i-1),(j-2),(cnt+1),size))
		{
			return true;
		}
		 
		possibleTour[i][j] = 0;
		currPath--;
		
		//display();
		return false;
	
	}
	public Integer[][] getChunkTour(Integer[][] tour, int i,int j)
	{
		Integer[][] updatedTour= new Integer[n][n];
		int c=0,d=0,start,end;
		
		int k,l;
		for(k=0;k<n;k++)
			for(l=0;l<n;l++)
				updatedTour[k][l]=0;
		if(i<chunkSize)
		{
		k=chunkSize;
		start=0;
		}
		else
		{
			k=n;
			start=chunkSize;
		}
		if(j<chunkSize)
		{
			l=chunkSize;
			end=0;
		}
		else
		{
			l=n;
			end=chunkSize;
		}
		for(int a=start;a<(k);a++)
		{
			d=0;
			for(int b=end;b<(l);b++)
			{
				updatedTour[a][b]=tour[c][d];
				d++;
				
			}
		    c++;
	   }
		
		return updatedTour;
		
	}
	public Integer[][] buildSolution()
	{
		Integer[][] solution = new Integer[n][n];
		Integer[][] finalSol= new Integer[n][n];
		if(n%2 == 0)
		{
			chunkSize = n/2;
			initialize(chunkSize);
			HashMap<String, Integer[][]> firstTempTour = new HashMap<String, Integer[][]>();
			HashMap<String, Integer[][]> secondTempTour = new HashMap<String, Integer[][]>();
			HashMap<String, Integer[][]> fourthTempTour = new HashMap<String, Integer[][]>();
			HashMap<String, Integer[][]> thirdTempTour = new HashMap<String, Integer[][]>();
			for(int i=0;i<chunkSize;i++)
			{
				
				for(int j=0;j<chunkSize;j++)
				{
			        initialize(chunkSize);
			       // System.out.println("***** Start position is " + i + ", " + j);
			        if(getPossibleMoves(i, j,0,chunkSize))
				    {
			        	//display();
			        	firstTempTour.put(i + "_" + j, possibleTour);
			        	tours.put(0, firstTempTour);
			        	
			        	
			        	secondTempTour.put(i+"_"+(j+(chunkSize)),possibleTour);
			        	tours.put(1, secondTempTour);
			        	
			        	thirdTempTour.put((i+chunkSize)+"_"+j,possibleTour);
			        	tours.put(2, thirdTempTour);
			        	
			        	fourthTempTour.put((i+chunkSize)+"_"+(j+chunkSize),possibleTour);
			        	tours.put(3, fourthTempTour);
			        	
				    }
			         else
				        System.out.println("");
				}
			}
			
		}
		else
		{
			chunkSize = n/2;
			initialize(chunkSize);
			for(int i=0;i<chunkSize;i++)
			{
				for(int j=0;j<chunkSize;j++)
				{
			        initialize(chunkSize);
			        System.out.println("***** Start position is " + i + ", " + j);
			        if(getPossibleMoves(i, j,0,chunkSize))
				    {
			        	
			        	HashMap<String, Integer[][]> tempTour = new HashMap<String, Integer[][]>();
			        	tempTour.put(i + "_" + j, possibleTour);
			        	tours.put(1, tempTour);
			        	tours.put(2, tempTour);
				    }
			         else
				        System.out.println("");
				}
			}
			
			chunkSize = n/2 + 1;
			initialize(chunkSize);
			for(int i=0;i<chunkSize;i++)
			{
				for(int j=0;j<chunkSize;j++)
				{
			        initialize(chunkSize);
			        System.out.println("***** Start position is " + i + ", " + j);
			        if(getPossibleMoves(i, j,0,chunkSize))
				    {
			        	
			        	HashMap<String, Integer[][]> tempTour = new HashMap<String, Integer[][]>();
			        	tempTour.put(i + "_" + j, possibleTour);
			        	tours.put(0, tempTour);
			        	tours.put(3, tempTour);
				    }
			         else
				        System.out.println("");
				}
			}
		}
		//visited[3]=1;
		//solution = findKnightsTour(5,5,3,solution,0);
		Set<Integer> chunkNos= tours.keySet();
		for(int c: chunkNos)
		{
			HashMap<String,Integer[][]> curr= tours.get(c);
			int cnt=0;
			Set<String> startPos= curr.keySet();
			for(String s: startPos)
			{
				init(solution);
				cnt=0;
				int start= Integer.valueOf(s.substring(0,s.lastIndexOf("_")));
				int end= Integer.valueOf(s.substring(s.lastIndexOf("_")+1));
				solution=findKnightsTour(start,end,c,solution,0);
				visited[c]=1;
				for(int i=0;i<4;i++)
				{
					if(visited[i]==1)
						cnt++;
				}
				if(solution!=null && cnt==4)
				{
					for (int i = 0; i < solution.length; i++) 
			    	{
						for (int j = 0; j < solution.length; j++) 
						{
							finalSol[i][j]=solution[i][j];
						}
			    	}
				}
				displaySol(solution);
			}
		}
		return finalSol;
	}
	public void displaySol(Integer[][] solution)
	{
		int cnt=0;
		for(int i=0;i<4;i++)
		{
			if(visited[i]==1)
				cnt++;
		}
		if(solution!=null && cnt==4)
		{
			System.out.println("\n Knights Tour by divide conquer approach: \n ");
			for (int i = 0; i < solution.length; i++) 
	    	{
				for (int j = 0; j < solution.length; j++) 
				{
					if(solution[i][j] <= 9)
						System.out.print("   " + "0" + solution[i][j]);
					else
						System.out.print("   " + solution[i][j]);
				}
				System.out.print("\n");
	    	}
		}
	}
	public String getEndIndex(int a,int b,Integer[][] tour)
	{
		String endIdx="";
		int iLim=0,jLim=0,iStart=0,jStart=0;
		if(a<chunkSize)
		{
			iLim=chunkSize;
			iStart=0;
		}
			else
			{
				iLim=n;
				iStart=chunkSize;
			}
		if(b<chunkSize)
		{
			jLim=chunkSize;
			jStart=0;
		}
		else
		{
			jLim=n;
			jStart=chunkSize;
		}
		for(int i=iStart;i<iLim;i++)
			for(int j=jStart;j<jLim;j++)
			{
				
				if(tour[i][j]==(chunkSize*chunkSize))
				{
					endIdx=i+"_"+j;
					break;
				}
			}
		return endIdx;
	}
	public String getChunkNumber(int iEndIdx,int jEndIdx)
	{
		if((iEndIdx+2)<chunkSize && (jEndIdx+1)>=chunkSize)
		{
			if(visited[1]!=1)
			{
				visited[1]=1;
				HashMap<String,Integer[][]> startLoc=tours.get(1);
				if(startLoc.containsKey((iEndIdx+2)+"_"+(jEndIdx+1)))
					return 1+"_"+(iEndIdx+2)+"_"+(jEndIdx+1);
			}
		}
		else if(iEndIdx>=chunkSize&&(iEndIdx-2)<chunkSize && jEndIdx<chunkSize && (jEndIdx-1)<chunkSize)
		{
			
			if(visited[0]!=1)
			{
				visited[0]=1;
				HashMap<String,Integer[][]> startLoc=tours.get(0);
				if(startLoc.containsKey((iEndIdx-2)+"_"+(jEndIdx-1)))
					return 0+"_"+(iEndIdx-2)+"_"+(jEndIdx-1);
			}
		}
		else if(iEndIdx>=chunkSize&&(iEndIdx-1)<chunkSize && jEndIdx<chunkSize && (jEndIdx-2)<chunkSize)
		{
			
			if(visited[0]!=1)
			{
				visited[0]=1;
				HashMap<String,Integer[][]> startLoc=tours.get(0);
				if(startLoc.containsKey((iEndIdx-1)+"_"+(jEndIdx-2)))
					return 0+"_"+(iEndIdx-1)+"_"+(jEndIdx-2);
			}
		}
		else if(iEndIdx>=chunkSize&&(iEndIdx-2)>=chunkSize && jEndIdx>=chunkSize && (jEndIdx-1)<chunkSize)
		{
			if(visited[2]!=1)
			{
				visited[2]=1;
				HashMap<String,Integer[][]> startLoc=tours.get(2);
				if(startLoc.containsKey((iEndIdx-2)+"_"+(jEndIdx-1)))
					return 2+"_"+(iEndIdx-2)+"_"+(jEndIdx-1);
			}
		}
		else if((iEndIdx+2)>=chunkSize && (jEndIdx+1)>=chunkSize)
		{
			if(visited[3]!=1)
			{
				visited[3]=1;
				HashMap<String,Integer[][]> startLoc=tours.get(3);
				if(startLoc.containsKey((iEndIdx+2)+"_"+(jEndIdx+1)))
					return 3+"_"+(iEndIdx+2)+"_"+(jEndIdx+1);
			}
		}
		else if((iEndIdx+1)<chunkSize && (jEndIdx+2)>=chunkSize)
		{
			if(visited[1]!=1)
			{
				visited[1]=1;
				HashMap<String,Integer[][]> startLoc=tours.get(1);
				if(startLoc.containsKey((iEndIdx+2)+"_"+(jEndIdx+1)))
					return 1+"_"+(iEndIdx+1)+"_"+(jEndIdx+2);
			}
		}
		else if((iEndIdx+1)>=chunkSize && (jEndIdx+2)>=chunkSize)
		{
			if(visited[3]!=1)
			{
				visited[3]=1;
				HashMap<String,Integer[][]> startLoc=tours.get(3);
				if(startLoc.containsKey((iEndIdx+1)+"_"+(jEndIdx+2)))
					return 3+"_"+(iEndIdx+1)+"_"+(jEndIdx+2);
			}
		}
		else if((iEndIdx+1)>=chunkSize && (jEndIdx+2)<chunkSize)
		{
			
			if(visited[2]!=1)
			{
				
				visited[2]=1;
				HashMap<String,Integer[][]> startLoc=tours.get(2);
				
				if(startLoc.containsKey((iEndIdx+1)+"_"+(jEndIdx+2)))
					return 2+"_"+(iEndIdx+1)+"_"+(jEndIdx+2);
			}
		}
		else if(iEndIdx>=chunkSize && (iEndIdx-1)<chunkSize && (jEndIdx+2)<chunkSize)
		{
			if(visited[0]!=1)
			{
				visited[0]=1;
				HashMap<String,Integer[][]> startLoc=tours.get(0);
				if(startLoc.containsKey((iEndIdx-1)+"_"+(jEndIdx+2)))
					return 0+"_"+(iEndIdx-1)+"_"+(jEndIdx+2);
			}
		}
		
		else if(iEndIdx>=chunkSize && (iEndIdx-1)<chunkSize && (jEndIdx+2)>=chunkSize)
		{
			if(visited[1]!=1)
			{
				visited[1]=1;
				HashMap<String,Integer[][]> startLoc=tours.get(1);
				if(startLoc.containsKey((iEndIdx-1)+"_"+(jEndIdx+2)))
					return 1+"_"+(iEndIdx-1)+"_"+(jEndIdx+2);
			}
		}
		else if(iEndIdx>=chunkSize && (iEndIdx-1)>=chunkSize && jEndIdx<chunkSize &&(jEndIdx+2)>=chunkSize)
		{
			if(visited[3]!=1)
			{
				visited[3]=1;
				HashMap<String,Integer[][]> startLoc=tours.get(3);
				if(startLoc.containsKey((iEndIdx-1)+"_"+(jEndIdx+2)))
					return 3+"_"+(iEndIdx-1)+"_"+(jEndIdx+2);
			}
		}
		else if(iEndIdx>=chunkSize && (iEndIdx-2)>=chunkSize && jEndIdx<chunkSize &&(jEndIdx+1)>=chunkSize)
		{
			if(visited[3]!=1)
			{
				visited[3]=1;
				HashMap<String,Integer[][]> startLoc=tours.get(3);
				if(startLoc.containsKey((iEndIdx-2)+"_"+(jEndIdx+1)))
					return 3+"_"+(iEndIdx-2)+"_"+(jEndIdx+1);
			}
		}
		else if(iEndIdx>=chunkSize && (iEndIdx-2)<chunkSize && (jEndIdx+1)<chunkSize)
		{
			if(visited[0]!=1)
			{
				visited[0]=1;
				HashMap<String,Integer[][]> startLoc=tours.get(0);
				if(startLoc.containsKey((iEndIdx-2)+"_"+(jEndIdx+1)))
					return 0+"_"+(iEndIdx-2)+"_"+(jEndIdx+1);
			}
		}
		else if(iEndIdx>=chunkSize && (iEndIdx-2)<chunkSize && (jEndIdx+1)>=chunkSize)
		{
			if(visited[1]!=1)
			{
				visited[1]=1;
				HashMap<String,Integer[][]> startLoc=tours.get(1);
				if(startLoc.containsKey((iEndIdx-2)+"_"+(jEndIdx+1)))
					return 1+"_"+(iEndIdx-2)+"_"+(jEndIdx+1);
			}
		}
		else if((iEndIdx+1)<chunkSize && jEndIdx>=chunkSize && (jEndIdx-2)<chunkSize)
		{
			if(visited[0]!=1)
			{
				visited[0]=1;
				HashMap<String,Integer[][]> startLoc=tours.get(0);
				if(startLoc.containsKey((iEndIdx+1)+"_"+(jEndIdx-2)))
					return 0+"_"+(iEndIdx+1)+"_"+(jEndIdx-2);
			}
		}
		else if(iEndIdx>=chunkSize&&(iEndIdx+1)>chunkSize && jEndIdx>=chunkSize && (jEndIdx-2)<chunkSize)
		{
			if(visited[2]!=1)
			{
				visited[2]=1;
				HashMap<String,Integer[][]> startLoc=tours.get(2);
				if(startLoc.containsKey((iEndIdx+1)+"_"+(jEndIdx-2)))
					return 2+"_"+(iEndIdx+1)+"_"+(jEndIdx-2);
			}
		}
		else if((iEndIdx+2)<chunkSize && jEndIdx>=chunkSize && (jEndIdx-1)<chunkSize)
		{
			if(visited[0]!=1)
			{
				visited[0]=1;
				HashMap<String,Integer[][]> startLoc=tours.get(0);
				if(startLoc.containsKey((iEndIdx+2)+"_"+(jEndIdx-1)))
					return 0+"_"+(iEndIdx+2)+"_"+(jEndIdx-1);
			}
		}
		else if(iEndIdx>=chunkSize&&(iEndIdx+2)>chunkSize && jEndIdx>=chunkSize && (jEndIdx-1)<chunkSize)
		{
			if(visited[2]!=1)
			{
				visited[2]=1;
				HashMap<String,Integer[][]> startLoc=tours.get(2);
				if(startLoc.containsKey((iEndIdx+2)+"_"+(jEndIdx-1)))
					return 2+"_"+(iEndIdx+2)+"_"+(jEndIdx-1);
			}
		}
		else if(iEndIdx>=chunkSize&&(iEndIdx-2)<chunkSize && jEndIdx>=chunkSize && (jEndIdx-1)>chunkSize)
		{
			if(visited[1]!=1)
			{
				visited[1]=1;
				HashMap<String,Integer[][]> startLoc=tours.get(1);
				if(startLoc.containsKey((iEndIdx-2)+"_"+(jEndIdx-1)))
					return 1+"_"+(iEndIdx-2)+"_"+(jEndIdx-1);
			}
		}
		else if(iEndIdx>=chunkSize&&(iEndIdx-2)<chunkSize && jEndIdx>=chunkSize && (jEndIdx-1)<chunkSize)
		{
			if(visited[0]!=1)
			{
				visited[0]=1;
				HashMap<String,Integer[][]> startLoc=tours.get(0);
				if(startLoc.containsKey((iEndIdx-2)+"_"+(jEndIdx-1)))
					return 0+"_"+(iEndIdx-2)+"_"+(jEndIdx-1);
			}
		}
		
		else if(iEndIdx>=chunkSize&&(iEndIdx-2)<chunkSize && jEndIdx<chunkSize && (jEndIdx-1)<chunkSize)
		{
			if(visited[0]!=1)
			{
				visited[0]=1;
				HashMap<String,Integer[][]> startLoc=tours.get(0);
				if(startLoc.containsKey((iEndIdx-2)+"_"+(jEndIdx-1)))
					return 0+"_"+(iEndIdx-2)+"_"+(jEndIdx-1);
			}
		}
		
		else if(iEndIdx>=chunkSize&&(iEndIdx-1)<chunkSize && jEndIdx>=chunkSize && (jEndIdx-2)>chunkSize)
		{
			if(visited[1]!=1)
			{
				visited[1]=1;
				HashMap<String,Integer[][]> startLoc=tours.get(1);
				if(startLoc.containsKey((iEndIdx-1)+"_"+(jEndIdx-2)))
					return 1+"_"+(iEndIdx-1)+"_"+(jEndIdx-2);
			}
		}
		else if(iEndIdx>=chunkSize&&(iEndIdx-1)<chunkSize && jEndIdx>=chunkSize && (jEndIdx-2)<chunkSize)
		{
			if(visited[0]!=1)
			{
				visited[0]=1;
				HashMap<String,Integer[][]> startLoc=tours.get(0);
				if(startLoc.containsKey((iEndIdx-1)+"_"+(jEndIdx-2)))
					return 0+"_"+(iEndIdx-1)+"_"+(jEndIdx-2);
			}
		}
		else if(iEndIdx>=chunkSize&&(iEndIdx-1)>=chunkSize && jEndIdx>=chunkSize && (jEndIdx-2)<chunkSize)
		{
			if(visited[2]!=1)
			{
				visited[2]=1;
				HashMap<String,Integer[][]> startLoc=tours.get(2);
				if(startLoc.containsKey((iEndIdx-1)+"_"+(jEndIdx-2)))
					return 2+"_"+(iEndIdx-1)+"_"+(jEndIdx-2);
			}
		}
		return "-1";
	}
	/*public void displaySets()
	{
		HashMap<String,Integer[][]> fTour=tours.get(0);
		System.out.println("First "+ fTour.keySet());
		
		HashMap<String,Integer[][]> sTour=tours.get(1);
		System.out.println("Second "+ sTour.keySet());
		HashMap<String,Integer[][]> tTour=tours.get(2);
		System.out.println("Third "+ tTour.keySet());
		Integer sol[][]= tours.get(2).get("5_2");
		System.out.println("Display sets");
		for (int i = 0; i < sol.length; i++) 
    	{
			for (int j = 0; j < sol.length; j++) 
			{
				//if(solution[i][j] <= 9)
					//System.out.print("   " + "0" + solution[i][j]);
				//else
					System.out.print("   " + sol[i][j]);
			}
			System.out.print("\n");
    	}
		HashMap<String,Integer[][]> lTour=tours.get(3);
		System.out.println("Last "+ lTour.keySet());
		
	}*/
	public Integer[][] findKnightsTour(int iIdx, int jIdx, int chunkNo,Integer[][] solution,int cnt)
	{
	
		int iLim=0,jLim=0,iStart=0,jStart=0;
		HashMap<String,Integer[][]> currTours= tours.get(chunkNo);
		
		String ipPos=iIdx+"_"+jIdx;
				
		Integer[][] sol= currTours.get(ipPos);
		Integer[][] updatedSol= getChunkTour(sol,iIdx,jIdx);
	
		if(iIdx<chunkSize)
		{
			iLim=chunkSize;
			iStart=0;
		}
		else
		{
			iLim=n;
			iStart=chunkSize;
		}
		if(jIdx<chunkSize)
		{
			jLim=chunkSize;
			jStart=0;
		}
		else
		{
			jLim=n;
			jStart=chunkSize;
		}
		for(int i=iStart;i<iLim;i++)
			for(int j=jStart;j<jLim;j++)
			{
				solution[i][j]=updatedSol[i][j]+(cnt*(chunkSize*chunkSize));
			}
		
		cnt++;	
		String endIdx= getEndIndex(iIdx,jIdx,updatedSol);
		
		int endRowIdx=Integer.valueOf(endIdx.substring(0,endIdx.indexOf("_")));
		int endColIdx= Integer.valueOf(endIdx.substring(endIdx.indexOf("_")+1));
		String newChunk=getChunkNumber(endRowIdx,endColIdx);
		
		if(newChunk.contains("_"))
		{
			String[] tokens= newChunk.split("_");
			int chunkno=Integer.valueOf(tokens[0]);
			int newISt= Integer.valueOf(tokens[1]);
			int newJSt= Integer.valueOf(tokens[2]);
			findKnightsTour(newISt,newJSt,chunkno,solution,cnt);
		}
		
		
		return solution ;
	
	}
	public static void main(String args[])
	{
		int flag=0;
		Scanner sc = new Scanner(System.in);
		System.out.print("Please enter the size of chessboard (nxn) = ");
		int n = sc.nextInt();
		Integer[][] solution=new Integer[n][n];
		KnightsTour obj = new KnightsTour(n);
		if(n>=10)
		{
			obj.initialize(n/2);
		    solution=obj.buildSolution();
		}
		else
		{
			 for(int i=0;i<n;i++)
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
				if(flag == 1)
					break;
			}
	        if(obj.paths.size()==0)
	        	System.out.println(" No Path found: ");
	        //else
	        //obj.printMap();
		}
		
	}
}

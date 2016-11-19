
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class createMatrix {
  ArrayList<Dimensions> matrix=new ArrayList<Dimensions>();
  HashMap<String,Double> salesMap=new HashMap<String,Double>();
	public void create(File file)
	{
		
		double sales=0.0f;
		try
		{
			String currText;
			
			Scanner scan= new Scanner(file);
			while(scan.hasNextLine())
			{
				Dimensions d = new Dimensions();
				currText=scan.nextLine();
			    String[] tokens=currText.split("\\s+");
				String[] keyTokens= tokens[0].split("_");
			
				
				d.setStore(Integer.parseInt(keyTokens[0]));
				d.setMonth(Integer.parseInt(keyTokens[1]));
				d.setYear(Integer.parseInt(keyTokens[2]));
				
					
				String valueTokens[]=tokens[1].split("_");
				sales=Double.parseDouble(valueTokens[0]);
				salesMap.put(tokens[0], sales);

				d.setPromo((int)Float.parseFloat(valueTokens[1]));
				d.setHoliday((int)Float.parseFloat(valueTokens[2]));
				d.setCompetition((int)Float.parseFloat(valueTokens[3]));
				d.setPromo2((int)Float.parseFloat(valueTokens[4]));
				d.setPromo2Since((int)Float.parseFloat(valueTokens[5]));
				if(valueTokens[6].contains("-"))
					d.setPromoInterval("-");
				else
					d.setPromoInterval(valueTokens[6]+"_"+valueTokens[7]+"_"+valueTokens[8]+"_"+valueTokens[9]);
				matrix.add(d);
				
			}
			scan.close();
		}catch(Exception e)
		{
			
		}

	}
}

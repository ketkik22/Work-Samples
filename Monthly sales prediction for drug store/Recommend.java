import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;


public class Recommend {
	private static int k=3;
	public static void main(String[] args) throws Exception
	{
		if(args.length != 4)
		{
			System.err.printf("Number of input expected is 8 but entered is", args.length);
			System.exit(1);
		}
		File inputFile = new File(args[0]);
		if(!inputFile.exists())
		{
			System.exit(1);
		}
		createMatrix newMatrix = new createMatrix();
		String className = "";
		double salesPredicated=0;
		newMatrix.create(inputFile);
		className=knn(newMatrix.matrix,args[1]+" "+args[2]+" "+args[3]);
		for(Entry<String, Double> sales:newMatrix.salesMap.entrySet())
		{
			if(sales.getKey().equalsIgnoreCase(className))
			{
				salesPredicated=sales.getValue();
			}
		}
                System.out.println("\n\n----------------------------------------------------------------");
		System.out.println("The sales for the store " +args[1]+" for the period "+args[2]+"/"+args[3]+" is "+salesPredicated);
	}
	public static double distance(Dimensions a, Dimensions b){
		double distance = 0.0;
		distance = (float) (Math.pow(a.store-b.store,2)+Math.pow(a.month-b.month,2));
		return Math.sqrt(distance);
	}
	public static String knn(ArrayList<Dimensions> newMatrix,String input) throws Exception
	{
		String[] inputMatrix = input.split("\\s+");
		if(Integer.parseInt(inputMatrix[1])<1||Integer.parseInt(inputMatrix[1])>12)
		{
			throw new NoSuchElementException();
		}
		double dist=0;
		int temp=0;
		ArrayList<Dimensions> neighbours =new ArrayList<Dimensions>();
		double minimum =  Math.pow(100,5);
		TreeMap<String,Integer> holiday = new TreeMap<String,Integer>();
		TreeMap<String,Integer> promo = new TreeMap<String,Integer>();
		TreeMap<String,Integer> promo2 = new TreeMap<String,Integer>();
		TreeMap<String,Integer> promo2since = new TreeMap<String,Integer>();
		Dimensions inputStoreDetails = new Dimensions();
		inputStoreDetails.setStore(Integer.parseInt(inputMatrix[0]));
		inputStoreDetails.setMonth(Integer.parseInt(inputMatrix[1]));
		inputStoreDetails.setYear(Integer.parseInt(inputMatrix[2]));
		for(int i=0;i<k;i++)
		{	
			minimum =  Math.pow(100,5);
			for(int j=0;j<newMatrix.size();j++)
			{
				dist=distance(newMatrix.get(j),inputStoreDetails);
				if(dist<minimum)
				{
					minimum=dist;
					temp=j;
				}
			}
			neighbours.add(newMatrix.get(temp));
			newMatrix.remove(temp);
		}
		String month ="";
		String tmpNeighbours="";
		for(int i=0;i<3;i++)
		{
			if(neighbours.get(i).getMonth()<=9)
			{
				month="0"+neighbours.get(i).getMonth();
			}
			else
			{
				month=String.valueOf(neighbours.get(i).getMonth());
			}
			tmpNeighbours = neighbours.get(i).getStore()+"_"+month+"_"+neighbours.get(i).getYear();
			holiday.put(tmpNeighbours,neighbours.get(i).getHoliday());
			promo.put(tmpNeighbours, neighbours.get(i).getPromo());
			promo2.put(tmpNeighbours, neighbours.get(i).getPromo2());
			promo2since.put(tmpNeighbours, neighbours.get(i).getPromo2Since());
		}
		ArrayList<String> returnVal = new ArrayList<String>();

		returnVal.add(entriesSortedByValues(holiday).first().getKey());
		returnVal.add(entriesSortedByValues(promo).last().getKey());
		returnVal.add(entriesSortedByValues(promo2).last().getKey());
		returnVal.add(entriesSortedByValues(promo2since).last().getKey());
	    TreeMap<String,Integer> ret1 = new TreeMap<String,Integer>();
	    for(String tmp:returnVal)
	    {
	    	int count=1;
	    	if(!ret1.containsKey(tmp))
	    	{
	    		ret1.put(tmp, count);
	    	}
	    	else
	    	{
	    		ret1.put(tmp, ret1.get(tmp)+1);
	    	}
	    }
		return entriesSortedByValues(ret1).last().getKey();
	}
	static <K,V extends Comparable<? super V>> SortedSet<Map.Entry<K,V>> entriesSortedByValues(Map<K,V> map) {
        SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
            new Comparator<Map.Entry<K,V>>() {
                @Override public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
                    int res = e1.getValue().compareTo(e2.getValue());
                    return res != 0 ? res : 1; // Special fix to preserve items with equal values
                }
            }
        );
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }
}

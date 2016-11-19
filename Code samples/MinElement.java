import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MinElement 
{
	public int solution(int A[], int B[])
	{
		if(A.length<B.length)
		{
			List<Integer> smallArray = new ArrayList<Integer>();
			for(Integer a:A)
			{
				smallArray.add(a);
			}
			List<Integer> commonArray = new ArrayList<Integer>();
			
			for(Integer b:B)
			{
				if(smallArray.contains(b))
					commonArray.add(b);
			}
			
			if(commonArray.size()==0)
				return -1;
			
			Collections.sort(commonArray);
			Object[] o = commonArray.toArray();
			int minElement = Integer.parseInt(o[0].toString());
			return minElement;
		}
		else if(A.length>=B.length)
		{
			List<Integer> smallArray = new ArrayList<Integer>();
			for(Integer b:B)
			{
				smallArray.add(b);
			}
			List<Integer> commonArray = new ArrayList<Integer>();
			
			for(Integer a:A)
			{
				if(smallArray.contains(a))
					commonArray.add(a);
			}
			if(commonArray.size()==0)
				return -1;
			Collections.sort(commonArray);
			Object[] o = commonArray.toArray();
			
			return Integer.parseInt(o[0].toString());
		}
		
		return -1;
	}
	
	public static void main(String[] args) 
	{
		int A[] = {914,565,745,654,56451,879,4546};
		int B[] = {645,23132,914,654,3154,754,65465};
		MinElement m = new MinElement();
		System.out.println(m.solution(A,B));
	}
}

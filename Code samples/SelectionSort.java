
public class SelectionSort 
{
	public static void main(String[] args) 
	{
		int array[] = {12, 56, 2, 9, 36, 77, 40, 89, 100, 124, 58};
		array = sort(array);
	}
	
	public static int[] sort(int[] array)
	{
		int temp, min; 
		for(int i=0; i<array.length-1; i++)
		{
			min = i;
			for(int j=i+1; j<array.length; j++)
			{
				if(array[j]<array[min])
					min = j;
			}
			
			temp = array[min];
			array[min] = array[i];
			array[i] = temp;
			
			System.out.println();
			for(int n : array)
				System.out.print(n + "\t"); 
		}
		return array;
	}
}

public class BubbleSort 
{
	public static void main(String[] args) 
	{
		int array[] = {89, 45, 68, 90, 29, 34, 17};
		for(int n : array)
			System.out.print(n + "\t"); 
		System.out.println("\n Sorting");
		array = sort(array, array.length);
	}
	
	public static int[] sort(int[] array, int len)
	{
		for(int i=0; i<len-1; i++)
		{
			for(int j=0; j<len-1-i; j++)
			{
				if(array[j+1]<array[j])
				{
					int temp = array[j];
					array[j] = array[j+1];
					array[j+1] = temp;
				}
			}
			System.out.println();
			for(int n : array)
				System.out.print(n + "\t"); 
		}
		return array;
	}
}

public class InsertionSort 
{
	public static void main(String[] args) 
	{
		int array[] = {12, 56, 2, 9, 36, 77, 40, 89, 100, 124, 58};
		System.out.print("Before sorting : ");
		for(int n : array)
		{
			System.out.print(n + "\t");
		}
		array = sort(array, array.length);
		
	}
	
	public static int[] sort(int[] array, int l)
	{
		System.out.println("While soerting");
		for(int i=0; i<l; i++)
		{
			int value = array[i];
			int j = i-1;
			while(j>=0 && array[j]>value)
			{
				array[j+1] = array[j];
				j--;
			}
			array[j+1] = value;
			
			for(int n : array)
			{
				System.out.print(n + "\t");
			}
			System.out.println();
		}
		return array;
	}
}

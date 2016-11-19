public class MergeSort 
{
	public static void main(String[] args) 
	{
		int array[] = {8, 3, 2, 9, 7, 1, 5, 4};
		for(int n : array)
			System.out.print(n + "\t"); 
		System.out.println("\n Sorting");
		mergeSort(array);
	}
	
	public static void mergeSort(int[] a)
	{
		int n = a.length;
		if(n>1)
		{
			int b[] = new int[n/2];
			int c[] = new int[n/2];
			
			for(int i=0; i<n/2; i++)
			{
				b[i] = a[i];
			}
			int j = n/2;
			for(int i=0; i<n/2; i++)
			{
				c[i] = a[j];
				j++;
			}
			mergeSort(b);
			mergeSort(c);
			merge(b,c,a);
			for(int k : a)
				System.out.print(k + "\t");
			System.out.println();
		}
	}
	
	public static void merge(int[] b, int[] c, int[] a)
	{
		int i=0, j=0, k=0;
		int p = b.length;
		int q = c.length;
		int r = p + q;
		
		while(i<p && j<q)
		{
			if(b[i]<=c[j])
			{
				a[k] = b[i];
				i++;
			}
			else
			{
				a[k] = c[j];
				j++;
			}
			k++;
		}
		if (i==p)
		{
			for(int x=j; x<q; x++)
			{
				a[k] = c[x];
				k++;
			}
		}
		else
		{
			for(int x=i; x<p; x++)
			{
				a[k] = b[x];
				k++;
			}
		}
		
	}
}

import java.util.Stack;

/*
 * Equilibrium problem
 * 

A non-empty zero-indexed array A consisting of N integers is given. Array A represents numbers on a tape.

Any integer P, such that 0 < P < N, splits this tape into two non-empty parts: A[0], A[1], ..., A[P − 1] and A[P], A[P + 1], ..., A[N − 1].

The difference between the two parts is the value of: |(A[0] + A[1] + ... + A[P − 1]) − (A[P] + A[P + 1] + ... + A[N − 1])|

In other words, it is the absolute difference between the sum of the first part and the sum of the second part.

For example, consider array A such that:
  A[0] = 3
  A[1] = 1
  A[2] = 2
  A[3] = 4
  A[4] = 3

We can split this tape in four places:

        P = 1, difference = |3 − 10| = 7
        P = 2, difference = |4 − 9| = 5
        P = 3, difference = |6 − 7| = 1
        P = 4, difference = |10 − 3| = 7

Write a function:

    int solution(int A[], int N);

that, given a non-empty zero-indexed array A of N integers, returns the minimal difference that can be achieved.

For example, given:
  A[0] = 3
  A[1] = 1
  A[2] = 2
  A[3] = 4
  A[4] = 3

the function should return 1, as explained above.

Assume that:

        N is an integer within the range [2..100,000];
        each element of array A is an integer within the range [−1,000..1,000].

Complexity:

        expected worst-case time complexity is O(N);
        expected worst-case space complexity is O(N), beyond input storage (not counting the storage required for input arguments).

Elements of input arrays can be modified.

 */

class Solution 
{
    public int solution(int[] A) 
    {
        int sumOfHigherIndex = 0;
        int sumOfLowerIndex = 0;
        for(int p = 0; p < A.length; p++)
        {
        	sumOfHigherIndex = 0;
        	sumOfLowerIndex = 0;
        	if(p!=0 && p!=A.length-1)
        	{
        		for(int m = 0; m<p; m++)
        		{
        			sumOfLowerIndex += A[m];
        		}
                
               for(int m=p+1; m<A.length; m++)
               {
            	   sumOfHigherIndex += A[m]; 
               }
               
                if(sumOfHigherIndex==sumOfLowerIndex)
                 return p;
        	}
        	else
        	{
        		if(p==A.length-1)
        		{
	        		for(int n = 0; n<A.length; n++)
	        		{
	        			sumOfHigherIndex += A[n];
	        		}
	        		if(sumOfHigherIndex==0)
	        			return p;
        		}
        	}
        }
        
        return -1;
    }
    
    public static void main(String args[])
    {
    	Solution s = new Solution();
    	int A[] = {-1,3,-4,5,1,-6,2,1};
    	int a = s.solution(A);
    	System.out.println(a);
    	
    }
}
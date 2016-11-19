class Frog {
    public int solution(int X, int[] A) 
    {
        // write your code in Java SE 8
        int earliestTime = 0;
        int length = A.length;
        int count=0;
        boolean flag = false;
        
        for(int i=1; i<=X; i++)
        {
            count=0;
            for(int j=0; j<length; j++)
            {
                if(A[j]==i)
                    count++;
            }
            if(count==0)
                break;
        }
        if(count!=0)
        {
            for(int k=0; k<length; k++)
            {
                if(A[k]==X)
                {
                    if(earliestTime==0)
                        earliestTime = k;
                    else if(k<earliestTime)
                        earliestTime = k;
                    flag = true;
                }
            }
            if(!flag)
                earliestTime = -1;
        }
        else
            earliestTime=-1;
        
        return earliestTime;
    }
    
    public static void main(String[] args) {
		Frog f = new Frog();
		int[] A = {1,2,3,8,6,25,4,12,11,11,11,16,24,21,22,23,22,19,17,18,9,10,18,11,17,13,15,15,16,14,7,8,11,6,12,10,7,9,5};
		int a = f.solution(1, A);
		System.out.println("a="+a);
	}
}
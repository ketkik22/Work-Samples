
public class PrimeAndLCD 
{
	public static void main(String[] args) 
	{
		int n = 10255;
		int result = isPrime(n);
		
		if(result==0)
			System.out.println(n + " is a prime number");
		else
		{
			System.out.println(n + " is not a prime number and LCD is " + result);
		}
	}
	
	public static int isPrime(int num)
	{
		boolean flag = true;
		for(int i=2;i<=num/2;i++)
		{
	       int temp=num%i;
		   if(temp==0)
		   {
		      flag=false;
		      break;
		   }
		}
		
		if(flag)
			return 0;
		else
		{
			int i = 2;
			while(num % i != 0)
			{
				i++;
			}
			return i;
		}
	}
	
	
}

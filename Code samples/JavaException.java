class MyCalculator
{
	public double power(int e, int p) throws Exception
	{
		if(e<0 || p<0)
			throw new Exception("ABdfsfsdfC");
		return Math.pow(e, p);
	}	
}

public class JavaException 
{
	public static void main(String[] args) 
	{
		try
		{
			MyCalculator m = new MyCalculator();
			System.out.println(m.power(2, -3));
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
		
	
	}
}

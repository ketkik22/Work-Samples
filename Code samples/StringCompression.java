
public class StringCompression 
{
	public static void main(String[] args) 
	{
		StringCompression s = new StringCompression();
		
		String str = "aabbbbbbbbbbcdddeeeeeeeeeeea";
		System.out.println("The compressed string is " + s.compress(str));
	}
	
	public String compress(String str)
	{
		String str1 = "";
		int count = 1;
		char[] ch = str.toCharArray();
		for(int i=0; i<ch.length; i++ )
		{
			if((i+1)<ch.length && ch[i] == ch[i+1])
			{
				count++;
			}
			else
			{
				str1 = str1 + ch[i] + count;
				count = 1;
			}
		}
			
		if(str1.length()<str.length())
			return str1;
		else
			return str;
	}
}

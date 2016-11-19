
public class StringSpaceReplacementWithString 
{
	public static void main(String[] args) 
	{
		StringSpaceReplacementWithString s = new StringSpaceReplacementWithString();
		String str1 = "         dddv sdvs   h ";
		String str2 = "%20";
		
		System.out.println("The replaced string is " + s.replace(str1, str2));
		
	}
	
	public String replace(String str1, String str2)
	{
		String result = "";
		for(char ch : str1.toCharArray())
		{
			if(ch == ' ')
				result = result + str2;
			else
				result = result + ch;
		}
		return result;
		
		
	}
}

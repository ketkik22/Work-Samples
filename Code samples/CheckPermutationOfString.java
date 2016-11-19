
public class CheckPermutationOfString 
{
	public static void main(String[] args) 
	{
		String str1 = "shilpa";
		String str2 = "pailhh";
		
		boolean result = checkPermutationOfStrings(str1, str2);
		
		if(result)
			System.out.println("'" + str1 + "' is a permutation of string '" + str2 + "'.");
		else
			System.out.println("'" + str1 + "' is not a permutation of string '" + str2 + "'.");
	}
	
	public static boolean checkPermutationOfStrings(String str1, String str2)
	{
		if(str1.length() != str2.length())
			return false;
				
		return sort(str1).equalsIgnoreCase(sort(str2)); 
	}
	
	public static String sort(String str)
	{
		char[] array = str.toCharArray();
		java.util.Arrays.sort(array);
		return new String(array);
	}
}

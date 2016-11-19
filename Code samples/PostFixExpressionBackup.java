import java.util.Stack;


public class PostFixExpressionBackup 
{
	private static int range = (int)(Math.pow(2, 12));

	public int solution(String S)
	{
		if(S.length() >= 200000)
		{
			return -1;
		}
		
		int returnValue = -1;
		Stack<Integer> stack = new Stack<Integer>();
		char[] arr = S.toCharArray();
		boolean flag = false;
		
		for(char c : arr)
		{
			int value = (int)c;
			
			if(value >= 48 && value <= 57)
				stack.push(value-48);
			else if(value == 42)
			{
				returnValue = -1;
				if(stack.size() >= 2)
				{
					int a = stack.pop();
					int b = stack.pop();
					int result = a * b;
					
					if(result < range)
					{
						returnValue = result;
					}
				}
				
				if(returnValue == -1)
				{
					flag = true;
					break;
				}
				stack.push(returnValue);
			}
			else if(value == 43)
			{
				returnValue = -1;
				if(stack.size() >= 2)
				{
					int a = stack.pop();
					int b = stack.pop();
					int result = a + b;
					
					if(result < range)
					{
						returnValue = result;
					}
				}
				
				if(returnValue == -1)
				{
					flag = true;
					break;
				}
				stack.push(returnValue);
			}
			else
			{
				returnValue = -1;
				System.err.println("Invalid character : " + c);
				flag = true;
				break;
			}
		}
		
		if(!flag)
		{
			if(!stack.isEmpty())
				returnValue = stack.pop();
		}
		
		return returnValue;
	}
	
	public static void main(String[] args) {
		PostfixExpression p = new PostfixExpression();
		System.out.println(p.solution("22222*****"));
	}

}

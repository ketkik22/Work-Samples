import java.util.Stack;


public class PostfixExpression 
{
	public int solution(String S)
	{
		 int range = (int)(Math.pow(2, 12));
		 if(S.length()>=200000)
		 {
			 //System.err.println("Input too large. Size limit=200000");
			 return -1;
		 }
		 if(S.isEmpty())
			 return -1;
		 else
		 {
			 Stack<Integer> stack = new Stack<Integer>();
			 char[] stringArray = S.toCharArray();
			 boolean flag = false;
			 int returnValue = -1;
			 
			 for(char c : stringArray)
			 {
				 int character = (int)(c);
				 
				 if(character>=48 && character<=57)
				 {
					 stack.push((int)(character-48));
				 }
				 else if(character==42)
				 {
					 if(stack.size() >= 2)
					 {
						 int a = stack.pop();
						 int b = stack.pop();
						 int result = a*b;
						 
						 if(result<range)
							 stack.push(result);
					 }
				 }
				 else if(character==43)
				 {
					 if(stack.isEmpty() || stack.size()<=1)
						 return -1;
					 else
					 {
						 int a = stack.pop();
						 int b = stack.pop();
						 int result = a+b;
						 
						 if(result<range)
							 stack.push(result);
					 }
				 }
				 else
					 return -1;
			 }
			 if(stack.isEmpty())
				 return -1;
			 else
				 return stack.pop();
		 }
	}
	
	public static void main(String[] args) {
		PostfixExpression p = new PostfixExpression();
		System.out.println(p.solution("22222*****"));
	}
}

/*
 * 		Operation.java
 * 		Created on : 04/16/2016
 * 		Author : Ketki Kulkarni
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;

public class Operation 
{
	ArrayList<Float> operands;
	boolean targetAchieved;
	boolean isUsed[];
	int startPos;
	float target;
	Stack<String> stack;
	
	public Operation() 
	{
		operands = new ArrayList<Float>();
		targetAchieved = false;
		stack = new Stack<String>();
		startPos = 0;
		target = 0;
	}
	
	public static void main(String[] args) 
	{
		Scanner sc = new Scanner(System.in);
		Operation opr = new Operation();
		String result = "None";
		
		String temp[] = sc.nextLine().split(" ");
		opr.target = Float.parseFloat(temp[0]);
		
		for(int i=1; i<temp.length; i++)
		{
			opr.operands.add(Float.parseFloat(temp[i]));
		}
		
		opr.isUsed = new boolean[temp.length-1];
		
		if(opr.operands.size() >=2 && opr.operands.size() <= 5)
		{
			for(int i=0; i<opr.operands.size(); i++)
			{
				for(int j=0; j<opr.operands.size(); j++)
				{
					if(i != j)
					{
						for(int k =0; k<opr.isUsed.length; k++)
							opr.isUsed[k] = false;
						opr.startPos = i;
						opr.targetAchieved = false;
						
						opr.stack = new Stack<String>();
						opr.stack.push(String.valueOf(opr.operands.get(i)));
						
						opr.isUsed[i] = true;
						opr.isUsed[j] = true;
						
						//System.out.println("opr1 = " + opr.operands.get(i) + "\topr2 = " + opr.operands.get(j));
						
						opr.buildSolution(opr.operands.get(i), opr.operands.get(j));
						
						if(opr.targetAchieved)
						{
							//System.out.println(opr.stack);
							result = opr.formatResult();
							break;
						}
						else
							result = "None";
						//System.out.println(opr.stack);
					}
				}
				if(opr.targetAchieved)
					break;
			}
		}
		else
			result = "None";
		System.out.println("Output --> " +result);
	}
	
	public void buildSolution(float opr1, float opr2)
	{
		HashMap<String, Float> solutionMap = helper(opr1, opr2);
		//System.out.println("opr1 = " + opr1 + "\topr2 = " + opr2);
		//System.out.println(solutionMap);
		boolean flag = true;
		for(int i=0; i<isUsed.length; i++)
		{
			if(!isUsed[i])
				flag = false;
		}
		if(solutionMap.containsValue(target) && flag)
		{
			for(Object key : solutionMap.keySet().toArray())
			{
				//System.out.println(String.valueOf(key));
				if(target == solutionMap.get(key))
				{
					targetAchieved = true;
					
					if(String.valueOf(key).indexOf("+") != -1)
						stack.push("+");
					else if(String.valueOf(key).indexOf("*") != -1)
						stack.push("*");
					else if(String.valueOf(key).indexOf("/") != -1)
						stack.push("/");
					else if(String.valueOf(key).indexOf("-") != -1)
						stack.push("-");
						
					stack.push(String.valueOf(opr2));
					return;
				}
			}
		}
		else 
		{
			for(int i=0; i<isUsed.length; i++)
			{
				if(!isUsed[i])
				{
					for(Object key : solutionMap.keySet().toArray())
					{
						float opr3 = operands.get(i);
						//System.out.println("opr3 = " + opr3);
						isUsed[i] = true;
						
						if(String.valueOf(key).indexOf("+") != -1)
							stack.push("+");
						else if(String.valueOf(key).indexOf("*") != -1)
							stack.push("*");
						else if(String.valueOf(key).indexOf("/") != -1)
							stack.push("/");
						else if(String.valueOf(key).indexOf("-") != -1)
							stack.push("-");
							
						stack.push(String.valueOf(opr2));
						
						buildSolution(solutionMap.get(key), opr3);
									
						if(targetAchieved)
							return;
						else
						{
							isUsed[i] = false;
							if(stack.size() != 1)
							{
								stack.pop();
								stack.pop();
							}
						}
					}
				}
			}
		}
		return;
	}
	
	public HashMap<String, Float> helper(float opr1, float opr2)
	{
		HashMap<String, Float> solutionMap = new HashMap<String, Float>();
		
		solutionMap.put(opr1 + " + " + opr2, opr1+opr2);
		solutionMap.put(opr1 + " * " + opr2, opr1*opr2);
		
		solutionMap.put(opr1 + " - " + opr2, Math.abs(opr1-opr2));
		/*else
			solutionMap.put(opr2 + "-" + opr1, opr2-opr1);*/
		//System.out.println(opr1 + " - " + opr2 + " = " + (opr1-opr2));
		
		solutionMap.put(opr1 + " / " + opr2, opr1/(float)opr2);
		//System.out.println(opr1 + " / " + opr2 + " = " + opr1/(float)opr2);
		
		return solutionMap;
	}
	
	public String formatResult()
	{
		String result = "";
	
		int operand[] = new int[operands.size()];
		String operator[] = new String[operands.size()-1];
		
		int i=0, j=0;
		while(!stack.isEmpty())
		{
			if(i<operand.length)
			{
				float f = Float.parseFloat(stack.pop());
				operand[i] = (int)f;
			}
			if(j<operator.length)
				operator[j] = stack.pop();
			i++; j++;
		}
		
		if(operand.length == 2)
		{
			if(operator[0].equals("/"))
				result = operand[1] + " " + operator[0] + " " + operand[0];
			else
				result = operand[0] + " " + operator[0] + " " + operand[1];
				
		}
			
		else
		{
			i = operand.length-1;
			j = operator.length-1;
			
			if(operator[j].equals("-"))
			{
				if(operand[i-1]>operand[i])
					result = "(" + operand[i-1] + " " + operator[j--] + " " + operand[i] + ")";
				else
					result = "(" + operand[i] + " " + operator[j--] + " " + operand[i-1] + ")";
				i--;
				i--;
			}
			else if(operator[j].equals("/"))
			{
				result = "(" + operand[i] + " " + operator[j--] + " " + operand[i-1] + ")";
				i--;
				i--;
			}
			else
				result = "(" + operand[i--] + " " + operator[j--] + " " + operand[i--] + ")";
			
			while(j>=0)
			{
				if(operator[j].equals("/"))
					result = "(" + result + " " + operator[j--] + " " + operand[i--] + ")";
				else
					result  = "(" + operand[i--] + " " + operator[j--] + " " + result + ")";
			}
		}
		return result;
	}
	
	
}
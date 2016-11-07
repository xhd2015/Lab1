package fulton.byperiod.atuniversity.expression;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;


public class Expression {

	/**
	 * @param args
	 */
	
	public ExpressionNode head;
	protected ExpressionNode constNode;
	public static BufferedReader input=null;
	public Expression()
	{
		head=new ExpressionNode("", "0", "+", false);
		//包含一项常数项
		constNode=new ExpressionNode("*1", "0", "+", true);
	}
	
	public Expression(String s) throws ExpressionException
	{
		this();
		
		//提取单项式
		ArrayList<String> list=ExpressionNode.splitToSingleExpressions(s);
		for(String i:list)
		{
			//使用单项式构造表达式树
			ExpressionNode en=ExpressionNode.standardlizeExpression(i);
			if(en.isConst())//常数项另外处理
			{
				constNode.setCoefficient(constNode.getValueAsIsItem()+en.getValueAsIsItem());
			}
			else
			{
				head.addNode(en);
			}
		}
	}
	public void add(Expression e)
	{
		for(ExpressionNode n:e.head.siblings)
		{
			head.addNode(n);
		}
	}
	
	public void derive_ex(String variable)
	{
		constNode.setCoefficient(0);//常数项归0
		ArrayList<ExpressionNode> derived=new ArrayList<>();  

		for(int i=0;i<head.siblings.size();i++)
		{
			ExpressionNode son=head.siblings.get(i);
			derived.addAll(son.deriveon_ex(variable));
		}
		head.siblings.clear();
		for(ExpressionNode en:derived)
		{
			if(en.isConst())
			{
				double coeff=constNode.getValueAsIsItem();
				constNode.setCoefficient(coeff+en.getValueAsIsItem());
			}else{
				head.addNode(en);
			}
		}
	}
	public void eval_ex(HashMap<String, Double> table)
	{
		ArrayList<ExpressionNode> evaled=new ArrayList<>();

		for(int i=0;i<head.siblings.size();i++)
		{
			ExpressionNode son=head.siblings.get(i);
			evaled.addAll(son.eval_ex(table));
		}
		head.siblings.clear();
		for(ExpressionNode en:evaled)
		{
			if(en.isConst())
			{
				double coeff=constNode.getValueAsIsItem();
				constNode.setCoefficient(coeff+en.getValueAsIsItem());
			}else if(en.isItem && Math.abs(en.getValueAsIsItem())<=ExpressionNode.zeroRange){
				continue;//系数为0的项
			}else{
				head.addNode(en);
			}
		}
	}

	public static void print(Object...o)
	{
		printe("\n",o);
	}
	//@end default is "\n"
	public static void printe(String end,Object...o)
	{
		for(Object i:o)
		{
			System.out.print(i);
			System.out.print(" ");
		}
		System.out.print(end);
	}
	public static void printerr(Object...o)
	{
		for(Object i:o)
		{
			System.err.print(i);
			System.err.print(" ");
		}
		System.err.print("\n");
	}
	//@prompt default is ""
	public static String getInput(String prompt) throws IOException
	{
		if(input==null)
			input=new 	BufferedReader(new InputStreamReader(System.in));
		
		printe("",prompt);
		
		
		return input.readLine();
		
	}
	public static String getInput() throws IOException
	{
		return getInput("");
	}
	public String toString()
	{
		ArrayList<ArrayList<String>> list=new ArrayList<>();
		for(ExpressionNode e:head.siblings)
		{
			e.parseAsList(list, "");
		}
		StringBuilder sb=new StringBuilder();
		
		for(ArrayList<String> tuple:list)
		{
			sb.append(tuple.get(0));
			if(!tuple.get(1).equals("1.0") && !tuple.get(1).equals("1"))
				sb.append(tuple.get(1));
			sb.append(tuple.get(2));
		}
		if(!constNode.isZero())
		{
			sb.append(constNode.sign).append(constNode.coeff);
		}
		if(sb.length()>0 && sb.charAt(0)=='+')
			return "\""+sb.substring(1)+"\"";
		else
			return "\""+sb.toString()+"\"";
		
		
	}
	

}

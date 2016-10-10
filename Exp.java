//unb
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import fulton.byperiod.atuniversity.expression.Expression;
import fulton.byperiod.atuniversity.expression.ExpressionException;
import fulton.byperiod.atuniversity.expression.ExpressionNode;

public class Exp {
	
	public static ArrayList<String> expression(String expin) throws ExpressionException
	{
		//这里可以使用另一个parseCommand方法,参见ExpressionNode的静态方法.
		return ExpressionNode.parseCommand(expin);
	}
	public static void simpilfy(Expression e,HashMap<String,Double> table)
	{
		e.eval_ex(table);
	}
	public static void derivative(Expression e,String x)
	{
		e.derive_ex(x);
	}
	public static void main(String[] args) throws ExpressionException, IOException {
		// TODO Auto-generated method stub
		Expression curExp=null;
		String expin	= null;
		double start=0,end=0;
		while(true)
		{
			expin=Expression.getInput(">>>");
			start=System.currentTimeMillis();
			try{
				if(expin.startsWith("!"))
				{
					ArrayList<String> commargs=expression(expin);
					if(commargs.get(0).equals("derive"))
					{
						derivative(curExp, commargs.get(1));
					}
					else if(commargs.get(0).equals("simplify"))
					{
						HashMap<String,Double> table=new HashMap<String, Double>();
						for(int i=1;i<commargs.size();i+=2)
						{
							table.put(commargs.get(i), Double.parseDouble(commargs.get(i+1)));
						}
						simpilfy(curExp, table);
					}
				}else{
					curExp=new Expression(expin);
				}
				
				Expression.print(curExp);
			}catch(ExpressionException e)
			{
				Expression.print("[ ExpressionError ]",e.getMessage());
			}finally{
				end=System.currentTimeMillis();
				Expression.print("time:"+(end-start)+"ms");
			}
			
			
		}
		
	}

}

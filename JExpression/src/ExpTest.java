/**
*Nice
*/
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Assert;

import org.junit.Test;

import fulton.byperiod.atuniversity.expression.Expression;
import fulton.byperiod.atuniversity.expression.ExpressionException;

public class ExpTest {
	String expstr="2.7x*y*z";
	Expression exp;
	public ExpTest(){
		try {
			exp=new Expression(expstr);
		} catch (ExpressionException e) {
			// TODO Auto-generated catch block
			exp=null;
			e.printStackTrace();
		}
		
	}
	Object getArgsForSimplifyFunctionWheneverYouLikeToDoThisAndIWillNeverStopYouSoYouAreFree(String command)
	{
		ArrayList<String> commargs=null;
		Object rtn=null;
		try {
			commargs = Exp.expression(command);
			if(commargs.get(0).equals("simplify"))
			{
				HashMap<String,Double> table=new HashMap<String, Double>();
				for(int i=1;i<commargs.size();i+=2)
				{
					table.put(commargs.get(i), Double.parseDouble(commargs.get(i+1)));
				}
				rtn=table;
			}
			
		} catch (ExpressionException e) {
			// TODO Auto-generated catch block
			rtn=e;
		}
		return rtn;
		
	}
	@Test
	public void testSimpilfy() {
		String teststr;
		String expected;
		teststr="!simplify y=x";
		expected="[ error ]command has wrong format.";
		String output=null;
		
		Expression arg1=this.exp;
		Object arg2=this.getArgsForSimplifyFunctionWheneverYouLikeToDoThisAndIWillNeverStopYouSoYouAreFree(teststr);
		if (arg2 instanceof ExpressionException) {
			output=((ExpressionException) arg2).getMessage().toString();
		}else{
			Exp.simpilfy(arg1, (HashMap)arg2);
			output=arg1.toString();
		}
		Assert.assertEquals(expected, output);
	}

}

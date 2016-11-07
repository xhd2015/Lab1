import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import fulton.byperiod.atuniversity.expression.Expression;
import fulton.byperiod.atuniversity.expression.ExpressionException;

public class TestDerivate {
	Expression e;
	String variable;
	
	int curindex=0;
	String[] myexpstr=new String[]{"2.7","2.7x*y*z","2.7x*x","2.7x*y*z"};
	String[] myexpected=new String[]{"\"\"","\"2.7y * z\"","\"5.4x\"","\"2.7y * z\""};
	public TestDerivate(){
		variable="x";
	}
	
	@Test
	public void test() {
		while(curindex!=4)
		{
			try {
				e=new Expression(myexpstr[curindex]);
			} catch (ExpressionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.derive_ex(variable);
			System.out.println(e.toString());
			Assert.assertEquals(myexpected[curindex], e.toString());
			curindex++;
		}
	}

}

package fulton.byperiod.atuniversity.expression;

public class ExpressionException extends Exception {
	public String message;

	public ExpressionException(String string) {
		// TODO Auto-generated constructor stub
		message = string;
	}

	public String getMessage() {
		return this.message;
	}
}

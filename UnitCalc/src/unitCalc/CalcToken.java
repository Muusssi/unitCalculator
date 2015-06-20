package unitCalc;

public class CalcToken {
	
	public enum TokenType {
		SUM, SUB, MUL, DIV, POW, UMIN,
		BEGIN, END, EQUAL,
		ID, NUM,
		ERR, CONV,
	}
	
	public TokenType type;
	public String id;
	public int index;
	public boolean operator = false;
	
	/** For identifiers and numbers */
	public CalcToken(TokenType type, String id, int index) {
		this.type = type;
		this.id = id;
		this.index = index;
	}
	
	/** For operators, special characters and errors */
	public CalcToken(TokenType type, int index) {
		this.type = type;
		this.index = index;
		this.operator = true;
	}
}

package unitCalc;

import java.util.LinkedList;

public class Parser {
	
	/*
	 * S 	-> Expr
	 * Expr -> Num | ID | Expr op Expr | (Expr)
	 * Num 	-> d[.d[E[+/-]d]
	 * Id	-> L(0-9|a-z|A-Z)
	 */
	
	static String input;
	static LinkedList<CalcToken> tokens;
	static int i;
	
	static LinkedList<CalcToken> lex(String inputString) {
		input = inputString;
		tokens = new LinkedList<CalcToken>();
		i = 0;
		CalcToken tok = nextToken();
		while (tok != null) {
			if (tok.type == CalcToken.TokenType.ERR) {
				return null;
			}
			if (!tokens.isEmpty() && tok.type == CalcToken.TokenType.NUM
					&& (tokens.getLast().type == CalcToken.TokenType.NUM || tokens.getLast().type == CalcToken.TokenType.ID) ) {
				Calculator.inform("Syntax error");
				return null;
			}
			tokens.add(tok);
			tok = nextToken();
		}
		return tokens;
	}
	
	static CalcToken nextToken() {
		char c;
		CalcToken tok = null;
		String current = "";
		if (input.length() <= i) {
			return null;
		}
		
		
		while (input.length() > i ) {
			c = input.charAt(i);
			if (Character.isWhitespace(c)) {
				i++;
			}
			else {
				break;
			}
		}
		
		if (input.length() <= i) {
			return null;
		} else {
			c = input.charAt(i);
		}
		
		if (c == '+') {
			tok = new CalcToken(CalcToken.TokenType.SUM, i);
			i++;
		}
		else if (c == '-') {
			tok = new CalcToken(CalcToken.TokenType.SUB, i);
			i++;
		}
		else if (c == '*') {
			tok = new CalcToken(CalcToken.TokenType.MUL, i);
			i++;
		}
		else if (c == '/') {
			tok = new CalcToken(CalcToken.TokenType.DIV, i);
			i++;
		}
		else if (c == '^') {
			tok = new CalcToken(CalcToken.TokenType.POW, i);
			i++;
		}
		else if (c == '!') {
			tok = new CalcToken(CalcToken.TokenType.FACT, i);
			i++;
		}
		else if (c == ';') {
			tok = new CalcToken(CalcToken.TokenType.FSEP, i);
			i++;
		}
		else if (c == '(') {
			tok = new CalcToken(CalcToken.TokenType.BEGIN, i);
			i++;
		}
		else if (c == ')') {
			tok = new CalcToken(CalcToken.TokenType.END, i);
			i++;
		}
		else if (c == '=') {
			tok = new CalcToken(CalcToken.TokenType.EQUAL, i);
			i++;
		}
		else if (c == ':') {
			tok = new CalcToken(CalcToken.TokenType.CONV, i);
			i++;
		}
		else if (c == '?') {
			if (!Calculator.useMeasurementError) {
				Calculator.setMeasurementErrorOn();
			}
			tok = new CalcToken(CalcToken.TokenType.QMARK, i);
			i++;
		}
		else if (c == '%') {
			tok = new CalcToken(CalcToken.TokenType.ID, "%", i);
			i++;
		}
		else if (c == 'ä') {
			tok = new CalcToken(CalcToken.TokenType.ID, "ä", i);
			i++;
		}
		
		// IDs
		else if (Character.isLetter(c) || c == '|') {
			while (input.length() > i && (Character.isLetter(input.charAt(i)) || input.charAt(i) == '_' ||
					input.charAt(i) == '|' || Character.isDigit(input.charAt(i)))) {
				c = input.charAt(i);
				current += c;
				i++;
			}
			tok = new CalcToken(CalcToken.TokenType.ID, current, i-current.length());
		}
		// Number dd*[.dd*][(E|e)[+|-]dd*]
		else if (Character.isDigit(c)) {
			while (input.length() > i && Character.isDigit(input.charAt(i)) ) {
				c = input.charAt(i);
				current += c;
				i++;
			}
			// if decimals (both point and colon are accepted as decimal separators)
			if (input.length() > i+1 && (input.charAt(i) == '.' || input.charAt(i) == ',') && Character.isDigit(input.charAt(i+1))) {
				//c = input.charAt(i);
				c = '.';
				current += c;
				i++;
				c = input.charAt(i);
				current += c;
				i++;
			}
			while (input.length() > i && Character.isDigit(input.charAt(i)) ) {
				c = input.charAt(i);
				current += c;
				i++;
			}
			// if exponents
			if (input.length() > i+1 && (input.charAt(i) == 'E' || input.charAt(i) == 'e')) {
				if (input.length() > i+2 && (input.charAt(i+1) == '+' || input.charAt(i+1) == '-') &&
						Character.isDigit(input.charAt(i+2)) ) {
					c = input.charAt(i);
					current += c;
					i++;
					c = input.charAt(i);
					current += c;
					i++;
					c = input.charAt(i);
					current += c;
					i++;
				}
				else if (input.length() > i+1 && Character.isDigit(input.charAt(i+1)) ) {
					c = input.charAt(i);
					current += c;
					i++;
					c = input.charAt(i);
					current += c;
					i++;
				}
				while (input.length() > i && Character.isDigit(input.charAt(i)) ) {
					c = input.charAt(i);
					current += c;
					i++;
				}
			}
			
			tok = new CalcToken(CalcToken.TokenType.NUM, current, i-current.length());
		}
		
		else {
			Calculator.inform("Syntax error: unsupported character");
			System.out.println(c);
			Calculator.inform(input);
			String pointerLine = "";
			for (int j=0; j<i; j++) {
				pointerLine += " ";
			}
			pointerLine += "^";
			Calculator.inform(pointerLine);
			return new CalcToken(CalcToken.TokenType.ERR, ""+c, i);
		}
		
		return tok;
	}
	
	// For testing
	public static void main(String[] args) {
		LinkedList<CalcToken> tokens = lex("15m+22ft/2N*33.3m/s");
		if (tokens != null) {
			for (int i=0; i<tokens.size(); i++) {
				if (tokens.get(i).id != null) {
					Calculator.inform(tokens.get(i).type+" "+tokens.get(i).id);
				}
				else {
					Calculator.inform(tokens.get(i).type+" ");
				}
				
			}
		}
		
		
	}

}

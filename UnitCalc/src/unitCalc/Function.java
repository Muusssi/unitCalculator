package unitCalc;

import java.math.BigDecimal;
import java.util.HashMap;

public class Function {
	
	public static HashMap<String,Function> functionMap = new HashMap<String,Function>();
	
	public String name;
	public String identifier;
	public int argNum;
	
	public Function(String name, String identifier, int argNum) {
		this.name = name;
		this.identifier = identifier;
		this.argNum = argNum;
		functionMap.put(this.identifier, this);
	}
	
	
	public Variable evaluate(Variable[] arguments) {
		// Trigonometric functions
		if (this.identifier.equals("sin")) {
			if (arguments[0].value.remainder(Calculator.pi.value).compareTo(BigDecimal.ZERO) == 0) {
				return new Variable(BigDecimal.ZERO);
			}
			else {
				return new Variable(BigDecimal.valueOf(Math.sin(arguments[0].value.doubleValue())));
			}
		}
		else if (this.identifier.equals("cos")) {
			return new Variable(BigDecimal.valueOf(Math.cos(arguments[0].value.doubleValue())));
		}
		else if (this.identifier.equals("tan")) {
			if (arguments[0].value.remainder(Calculator.pi.value.multiply(new BigDecimal("0.5"))).compareTo(BigDecimal.ZERO) == 0) {
				if (arguments[0].value.remainder(Calculator.pi.value).compareTo(BigDecimal.ZERO) == 0) {
					return new Variable(BigDecimal.ZERO);
				}
				else {
					Calculator.inform("Math error: tan not defined at ±¹/2");
					return null;
				}
			}
			else {
				return new Variable(BigDecimal.valueOf(Math.tan(arguments[0].value.doubleValue())));
			}
		}
		
		//Exponents
		else if (this.identifier.equals("exp")) {
			if (arguments[0].value.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
				return new Variable(Calculator.e.value.pow(arguments[0].value.intValue()));
			}
			return new Variable(BigDecimal.valueOf(Math.exp(arguments[0].value.doubleValue())));
		}
		else if (this.identifier.equals("ln")) {
			if (arguments[0].value.compareTo(BigDecimal.ZERO) <= 0) {
				Calculator.inform("Math error - Logartihms are only defined for values greater than zero.");
				return null;
			}
			return new Variable(BigDecimal.valueOf(Math.log(arguments[0].value.doubleValue())));
		}
		else if (this.identifier.equals("lg")) {
			if (arguments[0].value.compareTo(BigDecimal.ZERO) <= 0) {
				Calculator.inform("Math error - Logartihms are only defined for values greater than zero.");
				return null;
			}
			return new Variable(BigDecimal.valueOf(Math.log10(arguments[0].value.doubleValue())));
		}
		
		
		else {
			return null;
		}
		
	}
	
	
	
	
	public static void initFunctionMap() {
		new Function("sin", "sin", 1);
		new Function("cos", "cos", 1);
		new Function("tan", "tan", 1);
		new Function("exponent", "exp", 1);
		new Function("natural logarithm", "ln", 1);
		new Function("logarithm with base 10", "lg", 1);
	}
}

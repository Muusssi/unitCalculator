package unitCalc;

import java.math.BigDecimal;
import java.util.HashMap;

public class Function {
	
	public static HashMap<String,Function> functionMap = new HashMap<String,Function>();
	
	public String name;
	public String identifier;
	public String explanation;
	public int argNum;
	
	
	public Function(String name, String identifier, int argNum, String explanation) {
		this.name = name;
		this.identifier = identifier;
		this.explanation = explanation;
		this.argNum = argNum;
		functionMap.put(this.identifier, this);
	}
	
	
	public Variable evaluate(Variable[] arguments) {
		// TODO functions: abs(), cbrt(), ...

		// Trigonometric functions
		if (this.identifier.equals("sin")) {
			if (arguments[0].value.remainder(Calculator.pi.value).compareTo(BigDecimal.ZERO) == 0) {
				return new Variable(BigDecimal.ZERO);
			}
			else {
				return new Variable(BigDecimal.valueOf(Math.sin(arguments[0].value.doubleValue())));
			}
		}
		else if (this.identifier.equals("asin")) {
			if ((arguments[0].value.compareTo(BigDecimal.ONE)) > 0 || (arguments[0].value.compareTo(new BigDecimal("-1")) < 0)) {
				Calculator.inform("Math error: arc sin is only defined for values between -1 and 1");
				return null;
			}
			else if (arguments[0].value.compareTo(BigDecimal.ONE) == 0) {
				return new Variable(new BigDecimal("0.5").multiply(Calculator.pi.value));
			}
			else if (arguments[0].value.compareTo(new BigDecimal("-1")) == 0) {
				return new Variable(new BigDecimal("-0.5").multiply(Calculator.pi.value));
			}
			else if (arguments[0].value.compareTo(BigDecimal.ZERO) == 0) {
				return new Variable(BigDecimal.ZERO);
			}
			return new Variable(BigDecimal.valueOf(Math.asin(arguments[0].value.doubleValue())));
		}
		else if (this.identifier.equals("cos")) {
			return new Variable(BigDecimal.valueOf(Math.cos(arguments[0].value.doubleValue())));
		}
		else if (this.identifier.equals("acos")) {
			if ((arguments[0].value.compareTo(BigDecimal.ONE)) > 0 || (arguments[0].value.compareTo(new BigDecimal("-1")) < 0)) {
				Calculator.inform("Math error: arc cos is only defined for values between -1 and 1");
				return null;
			}
			return new Variable(BigDecimal.valueOf(Math.acos(arguments[0].value.doubleValue())));
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
		else if (this.identifier.equals("atan")) {
			return new Variable(BigDecimal.valueOf(Math.atan(arguments[0].value.doubleValue())));
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
		
		else if (this.identifier.equals("sqrt")) {
			if (arguments[0].value.compareTo(BigDecimal.ZERO) < 0) {
				Calculator.inform("Math error - Square root is only supported for non-negative values.");
				return null;
			}
			return new Variable(BigDecimal.valueOf(Math.sqrt(arguments[0].value.doubleValue())));
		}
		
		else if (this.identifier.equals("cbrt")) {
			return new Variable(BigDecimal.valueOf(Math.cbrt(arguments[0].value.doubleValue())));
		}
		
		else {
			Calculator.inform("Error: Unimplemented function");
			return null;
		}
		
	}
	
	public static Variable flg(Variable arg) {
		return null;
	}
	
	public static Variable factorial(Variable arg) {
		if (!arg.isUnitless()) {
			Calculator.inform("Math error: factorial is only defined for unitless variables.");
			arg.show();
			return null;
		}
		if (arg.value.compareTo(BigDecimal.ZERO) <= 0 || arg.value.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) > 0) {
			Calculator.inform("Math error: factorial is only defined for positive integers.");
			arg.show();
			return null;
		}
		BigDecimal ans = new BigDecimal(1);
		BigDecimal multiplier = new BigDecimal(1);
		while (multiplier.compareTo(arg.value) < 0) {
			multiplier = multiplier.add(BigDecimal.ONE);
			ans = ans.multiply(multiplier);
		}
		return new Variable(ans);
	}
	
	
	public static Variable fsin(Variable arg) {
		// Exact from table
		// or Using expansions until error<1e-100
		//while ()
		return null;
	}
	
	
	
	
	public static void initFunctionMap() {
		new Function("sin", "sin", 1, "");
		new Function("arc sin", "asin", 1, "");
		new Function("cos", "cos", 1, "");
		new Function("arc cos", "acos", 1, "");
		new Function("tan", "tan", 1, "");
		new Function("arc tan", "atan", 1, "");
		new Function("exponent", "exp", 1, "");
		new Function("natural logarithm", "ln", 1, "");
		new Function("logarithm with base 10", "lg", 1, "");
		new Function("square root", "sqrt", 1, "");
		new Function("cubic root", "cbrt", 1, "");
		//new Function("date meaning time since 1970-01-01 for comparing dates", "date", 3, "");
	}
}

package unitCalc;

import java.math.BigDecimal;
import java.util.HashMap;

public class Function {
	
	public static HashMap<String,Function> functionMap = new HashMap<String,Function>();
	
	public String name;
	public String identifier;
	public String explanation;
	public int argNum;
	
	public FunctionEvaluators.FunctionEvaluator functionEvaluator;
	
	
	public Function(String name, String identifier, int argNum, String explanation, FunctionEvaluators.FunctionEvaluator functionEvaluator) {
		this.name = name;
		this.identifier = identifier;
		this.explanation = explanation;
		this.argNum = argNum;
		functionMap.put(this.identifier, this);
		this.functionEvaluator = functionEvaluator;
	}
	
	
	
	
	public Variable evaluate(Variable[] arguments) {
		if (this.functionEvaluator == null) {
			Calculator.inform("Error: Function '"+this.name+"' has not been implemented yet. But surely in next release.");
			return null;
		}
		else {
			for (int i=0; i<arguments.length; i++) {
				if (!arguments[i].isUnitless()) {
					Calculator.inform("Error: Function '"+this.name+"' only accepts unitless variables.");
					Calculator.inform("One argument has unit: "+arguments[i].measure.baseUnit.abr);
					return null;
				}
			}
			return this.functionEvaluator.evaluate(arguments);
		}
	}

	
	
	public static void initFunctionMap() {
		FunctionEvaluators funcEvaluators = new FunctionEvaluators();
		new Function("sin", "sin", 1, "", funcEvaluators.new SinEvaluator());
		new Function("arc sin", "asin", 1, "", funcEvaluators.new AsinEvaluator());
		new Function("cos", "cos", 1, "", funcEvaluators.new CosEvaluator());
		new Function("arc cos", "acos", 1, "", funcEvaluators.new AcosEvaluator());
		new Function("tan", "tan", 1, "", funcEvaluators.new TanEvaluator());
		new Function("arc tan", "atan", 1, "", funcEvaluators.new AtanEvaluator());
		new Function("exponent", "exp", 1, "", funcEvaluators.new ExpEvaluator());
		new Function("natural logarithm", "ln", 1, "", funcEvaluators.new LnEvaluator());
		new Function("logarithm with base 10", "lg", 1, "", funcEvaluators.new LgEvaluator());
		new Function("square root", "sqrt", 1, "", funcEvaluators.new SqrtEvaluator());
		new Function("cubic root", "cbrt", 1, "", funcEvaluators.new CbrtEvaluator());
		new Function("time now", "now", 0, "returns time since 1970-01-01 for comparing dates", funcEvaluators.new NowEvaluator());
		new Function("date", "date", 3, "date returns time between 1970-01-01 and specified date", funcEvaluators.new DateEvaluator());
		//new Function("time", "time", 6, "date returns time since 1970-01-01 for comparing dates", funcEvaluators.new TimeEvaluator());
		
		// TODO functions
	}
}

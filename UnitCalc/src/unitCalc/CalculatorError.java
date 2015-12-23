package unitCalc;

import java.util.LinkedList;

public class CalculatorError {
	
	public static LinkedList<CalculatorError> errors = new LinkedList<CalculatorError>();
	
	public String errorName;
	public Variable var1;
	public Variable var2;
	
	public Number num1;
	public Number num2;

	public CalculatorError(String errorName, String message) {
		this.errorName = errorName;
		errors.add(this);
	}
	
	public CalculatorError(String errorName, String message, Number num) {
		this.errorName = errorName;
		this.num1 = num;
		errors.add(this);
	}
	
	public CalculatorError(String errorName, String message, Number num1, Number num2) {
		this.errorName = errorName;
		this.num1 = num1;
		this.num2 = num2;
		errors.add(this);
	}

}

package unitCalc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * This class represents a object that is used to build calculations.
 * It can be a variable, constant, operation, ...
 * @author Tommi Oinonen
 *
 */
public class Variable {
	
	static HashMap<String,Variable> varMap = new HashMap<String,Variable>();
	static HashMap<String,Variable> constMap = new HashMap<String,Variable>();
	
	public String id = "var";
	public LinkedList<String> alternativeIds = new LinkedList<String>();
	public BigDecimal value = null;
	
	public BigDecimal accumulatedMaxValue = null;
	public BigDecimal accumulatedMinValue = null;
	
	
	
	public int[] siBase = new int[7];
	public Measure measure = null;
	
	//For constatnts
	public boolean isConstant = false;
	public String name;
	
	// For operators
	CalcToken.TokenType op = null;
	boolean isOperation = false;
	boolean isFunction = false;
	
	public Variable(BigDecimal value, String id, int[] siBase) {
		this.value = value;
		this.accumulatedMaxValue = this.value;
		this.accumulatedMinValue = this.value;
		if (siBase == null) {
			this.siBase = new int[7];
			this.measure = Calculator.unitlesMeasure;
		}
		else {
			this.siBase = siBase;
			this.measure = Measure.getMeasure(siBase[0], siBase[1], siBase[2], siBase[3], siBase[4], siBase[5], siBase[6]);
		}
		if (id != null) {
			this.id = id;
			this.measure = Measure.getMeasure(siBase[0], siBase[1], siBase[2], siBase[3], siBase[4], siBase[5], siBase[6]);
			varMap.put(this.id, this);
		}
	}
	
	
	/** For operators and parenthesis */
	public Variable(CalcToken.TokenType op, boolean isOperator) {
		this.op = op;
		this.id = "op";
		this.isOperation = isOperator;
	}
	
	/** For functions */
	public Variable(CalcToken.TokenType op, String funcName) {
		this.op = op;
		this.id = funcName;
		this.isFunction = true;
	}
	
	/** Checks if the variable is unitless. */
	public boolean isUnitless() {
		for (int i=0; i<7; i++) {
			if (this.siBase[i] != 0) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isInteger() {
		if (this.value.divideAndRemainder(BigDecimal.ONE)[1].compareTo(BigDecimal.ZERO) == 0) {
			return true;
		}
		else {
			return false;
		}
	}
	
	
	private int getNumberOfDecimalPlaces() {
	    String string = this.value.toPlainString();
	    int index = string.indexOf(".");
	    return index < 0 ? 0 : string.length() - index - 1;
	}
	
	public void setMeasurementError(BigDecimal measurementError) {
		if (measurementError == null) {
			int decimalPlaces = this.getNumberOfDecimalPlaces();
			int scale = this.value.scale();
			if (decimalPlaces != 0) {
				this.accumulatedMaxValue = this.value.add(new BigDecimal("5e-"+(decimalPlaces+1)));
				this.accumulatedMinValue = this.value.subtract(new BigDecimal("5e-"+(decimalPlaces+1)));
			}
			else {
				this.accumulatedMaxValue = this.value;
				this.accumulatedMinValue = this.value;
			}
		}
		else {
			this.accumulatedMaxValue = this.value.add(measurementError);
			this.accumulatedMinValue = this.value.subtract(measurementError);
		}
	}
	
	/** For constants */
	public static Variable makeConstant(BigDecimal value, String id, int[] siBase, String name, BigDecimal measurementError) {// TODO Constant error
		Variable constant = new Variable(value, id, siBase);
		constant.setMeasurementError(measurementError);
		constant.isConstant = true;
		constant.name = name;
		constMap.put(id, constant);
		return constant;
	}
	
	/**Adds an alternative id to the variableMap for the variable.*/
	public void addAlternativeId(String altId) {
		varMap.put(altId, this);
		this.alternativeIds.add(altId);
	}
	
	/** Sets the given unit for a unitless variable */
	public void setUnit(Unit unit) {// TODO Assuming that units are exact...
		this.value = this.value.multiply(unit.baseRelation);
		this.accumulatedMaxValue = this.accumulatedMaxValue.multiply(unit.baseRelation);
		this.accumulatedMinValue = this.accumulatedMinValue.multiply(unit.baseRelation);
		this.measure = unit.measure;
		this.siBase = new int[7];
		System.arraycopy(unit.measure.siBase, 0, this.siBase, 0, 7);
		if (this.isConstant) {
			this.id = "var";
			this.name = null;
		}
	}
	
	/** Calculates the given operation for the two varibles. */
	public static Variable calc(Variable var1, CalcToken.TokenType op, Variable var2) {
		Variable ans;
		int[] ansSIbase = new int[7];
		if (op == CalcToken.TokenType.UMIN) {
			System.arraycopy(var1.siBase, 0, ansSIbase, 0, 7);
			ans = new Variable(var1.value.negate(), null, ansSIbase);
			ans.accumulatedMaxValue = var1.accumulatedMinValue.negate();
			ans.accumulatedMinValue = var1.accumulatedMaxValue.negate();
			return ans;
		}
		
		else if (op == CalcToken.TokenType.FACT) {
			ans = Function.factorial(var1);
			return ans;
		}
		else if (op == CalcToken.TokenType.POW) { // TODO non integer (& negative) powers
			if (!var2.isUnitless()) {
				Calculator.inform("Math error: powers must be unitless non-negative integers.");
				var2.show();
				return null;
			}
			else if (var2.value.compareTo(new BigDecimal("100")) >= 0) {
				Calculator.inform("Math error: powers greater than 100 are not supported for performance reasons.");
				var2.show();
				return null;
			} 
			else if ( var2.value.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
				int pow = var2.value.intValue();
				if (pow < 0) {
					Calculator.inform("Math error: negative powers are not supported.");
					return null;
				}
				for (int i=0; i<7; i++) {
					ansSIbase[i] = var1.siBase[i]*pow;
				}
				ans = new Variable(var1.value.pow(pow), null, ansSIbase);
				ans.accumulatedMaxValue = var1.accumulatedMaxValue.pow(pow);
				ans.accumulatedMinValue = var1.accumulatedMinValue.pow(pow);
				return ans;
			}
			else {
				Calculator.inform("Math error: only non-negative integer powers are supported.");
				return null;
			}
		}
		
		else if (op == CalcToken.TokenType.MUL) {
			for (int i=0; i<7; i++) {
				ansSIbase[i] = var1.siBase[i] + var2.siBase[i];
			}
			ans = new Variable(var1.value.multiply(var2.value), null, ansSIbase);
			BigDecimal min1min2 = var1.accumulatedMinValue.multiply(var2.accumulatedMinValue);
			BigDecimal min1max2 = var1.accumulatedMinValue.multiply(var2.accumulatedMaxValue);
			BigDecimal max1min2 = var1.accumulatedMaxValue.multiply(var2.accumulatedMinValue);
			BigDecimal max1max2 = var1.accumulatedMaxValue.multiply(var2.accumulatedMaxValue);
			ans.accumulatedMaxValue = min1min2.max(min1max2);
			ans.accumulatedMaxValue = ans.accumulatedMaxValue.max(max1min2);
			ans.accumulatedMaxValue = ans.accumulatedMaxValue.max(max1max2);
			ans.accumulatedMinValue = min1min2.min(min1max2);
			ans.accumulatedMinValue = ans.accumulatedMinValue.min(max1min2);
			ans.accumulatedMinValue = ans.accumulatedMinValue.min(max1max2);
			return ans;
		}
		else if (op == CalcToken.TokenType.DIV) {//TODO acc error + div by possibly zero
			if (var2.value.compareTo(BigDecimal.ZERO) == 0) {
				Calculator.inform("Math error: division by zero");
				return null;
			}
			else if (var2.accumulatedMaxValue.signum() != var2.accumulatedMinValue.signum()) {
				Calculator.inform("Error: Zero is in the range of possible measurement error of the divisor.\nPossible divivison by zero.");
				var2.show();
				return null;
			}
			
			for (int i=0; i<7; i++) {
				ansSIbase[i] = var1.siBase[i] - var2.siBase[i];
			}
			if  (var1.value.compareTo(BigDecimal.ZERO) == 0) {
				ans = new Variable(BigDecimal.ZERO, null, ansSIbase);
			}
			else {
				try {
					ans = new Variable(var1.value.divide(var2.value), null, ansSIbase);
				}
				catch (ArithmeticException ae) {
					ans = new Variable(var1.value.divide(var2.value, 100, RoundingMode.HALF_UP), null, ansSIbase);
				}
			}
			BigDecimal min1min2 = var1.accumulatedMinValue.divide(var2.accumulatedMinValue, 100, RoundingMode.HALF_UP);
			BigDecimal min1max2 = var1.accumulatedMinValue.divide(var2.accumulatedMaxValue, 100, RoundingMode.HALF_UP);
			BigDecimal max1min2 = var1.accumulatedMaxValue.divide(var2.accumulatedMinValue, 100, RoundingMode.HALF_UP);
			BigDecimal max1max2 = var1.accumulatedMaxValue.divide(var2.accumulatedMaxValue, 100, RoundingMode.HALF_UP);
			ans.accumulatedMaxValue = min1min2.max(min1max2);
			ans.accumulatedMaxValue = ans.accumulatedMaxValue.max(max1min2);
			ans.accumulatedMaxValue = ans.accumulatedMaxValue.max(max1max2);
			ans.accumulatedMinValue = min1min2.min(min1max2);
			ans.accumulatedMinValue = ans.accumulatedMinValue.min(max1min2);
			ans.accumulatedMinValue = ans.accumulatedMinValue.min(max1max2);
			return ans;
		}
		else if (op == CalcToken.TokenType.SUM || op == CalcToken.TokenType.SUB) {
			boolean sameBase = true;
			for (int i=0; i<7; i++) {
				if (var1.siBase[i] != var2.siBase[i]) {
					sameBase = false;
					break;
				}
			}
			if (sameBase) {
				System.arraycopy(var1.siBase, 0, ansSIbase, 0, 7);
				if (op == CalcToken.TokenType.SUM) {
					ans = new Variable(var1.value.add(var2.value), null, ansSIbase);
					ans.accumulatedMaxValue = var1.accumulatedMaxValue.add(var2.accumulatedMaxValue);
					ans.accumulatedMinValue = var1.accumulatedMinValue.add(var2.accumulatedMinValue);
					return ans;
				}
				else if (op == CalcToken.TokenType.SUB) {
					ans = new Variable(var1.value.subtract(var2.value), null, ansSIbase);
					ans.accumulatedMaxValue = var1.accumulatedMaxValue.subtract(var2.accumulatedMinValue);
					ans.accumulatedMinValue = var2.accumulatedMinValue.subtract(var2.accumulatedMaxValue);
					return ans;
				}
			}
			else {
				Calculator.inform("Error: Can't subtract or add variables that have different measures.");
				var1.show();
				var2.show();
				return null;
			}
		}
		else if (op == CalcToken.TokenType.EQUAL) {
			Calculator.inform("Error: Illegal variable assignment.");
			return null;
		}
		else {
			Calculator.inform("Error: Something weird happened with this calculation.\n Please inform the developer if you can see this message.");
			return null;
		}
		Calculator.inform("Error: Something really weird happened with this calculation.\n Please inform the developer if you can see this message.");
		return null;
	}
	
	public static void listConstants() {
		Iterator<String> itr = varMap.keySet().iterator();
		Variable possibleConstant;
		while (itr.hasNext()) {
			possibleConstant = varMap.get(itr.next());
			if (possibleConstant.isConstant) {
				possibleConstant.show();
			}
			
		}
	}
	
	/** Prints the useful information for this variable or constant in given unit. If unit is null then show in all units of the relevant measure. */
	public void show(String unitAbr) {
		int resLess;
		int resGreater;
		if (this.measure != null && this.measure.name.equals("time")) {
			BigDecimal[] aAndRemainder = this.value.divideAndRemainder(new BigDecimal("31536000"));
			BigDecimal[] dAndRemainder = aAndRemainder[1].divideAndRemainder(new BigDecimal("86400"));
			BigDecimal[] hAndRemainder = dAndRemainder[1].divideAndRemainder(new BigDecimal("3600"));
			BigDecimal[] minAndRemainder = hAndRemainder[1].divideAndRemainder(new BigDecimal("60"));
			Calculator.inform("= "+aAndRemainder[0].stripTrailingZeros().toPlainString()+" years "
					+dAndRemainder[0].stripTrailingZeros().toPlainString()+" days "
					+hAndRemainder[0].stripTrailingZeros().toPlainString()+" hours "
					+minAndRemainder[0].stripTrailingZeros().toPlainString()+" mins "
					+minAndRemainder[1].stripTrailingZeros().toPlainString()+" s ");
		}
		if (unitAbr == null) {
			//Show all
			this.show();
			Iterator<Unit> itr = this.measure.units.iterator();
			while (itr.hasNext()) {
				Unit convUnit = itr.next();
				resLess = this.value.compareTo(new BigDecimal("0.000000001"));
				resGreater = this.value.compareTo(new BigDecimal("9999999"));
				if (this.value.compareTo(BigDecimal.ZERO) == 0) {
					Calculator.inform("= 0 "+convUnit.abr);
				}
				else if (resLess == -1 || resGreater == 1) {
					Calculator.inform("= "+this.value.divide(convUnit.baseRelation, 30, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toEngineeringString()+" "+convUnit.abr);
				}
				else {
					Calculator.inform("= "+this.value.divide(convUnit.baseRelation, 30, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString()+" "+convUnit.abr);
				}
			}
			return;
		}
		else if (!Unit.unitMap.containsKey(unitAbr)) {
			Calculator.inform("Unable to convert: Unknown unit '"+unitAbr+"'");
			return;
		}
		
		Unit resultUnit = Unit.unitMap.get(unitAbr);
		if (resultUnit.measure != this.measure) {
			Calculator.inform("Unable to convert: measures of the units do not match.");
			Calculator.inform(this.measure.baseUnit.abr+": "+this.measure.name+" -- "+resultUnit.abr+": "+resultUnit.measure.name);
			return;
		}
		else {
			Calculator.inform("= "+this.value.divide(resultUnit.baseRelation, 30, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toString()+" "+resultUnit.abr);
			return;
		}
	}
	
	/** Prints the useful information for this variable or constant. */
	public void show() {
		if (this.isConstant) {
			Calculator.inform("-- Constant: "+this.id+" - "+this.name);
		}
		else if ((this.id != null) && (!this.id.equals("var"))) {
			Calculator.inform("-- Variable: "+this.id);
		}
		
		int scale = this.value.scale();
		if (scale > 50) {
			scale = 30;
		}
		int resLess = this.value.compareTo(new BigDecimal("0.00001"));
		int resGreater = this.value.compareTo(new BigDecimal("9999999"));
		
		if (this.isUnitless()) {
			if (resLess == -1 || resGreater == 1) {
				Calculator.inform("= "+this.value.setScale(scale, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toEngineeringString());
			}
			else {
				Calculator.inform("= "+this.value.setScale(scale, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString());
			}
		}
		else {
			if (resLess == -1 || resGreater == 1) {
				Calculator.inform("= "+this.value.setScale(scale, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toEngineeringString()+" "+this.measure.baseUnit.abr);
			}
			else {
				Calculator.inform("= "+this.value.setScale(scale, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString()+" "+this.measure.baseUnit.abr);
			}
		}
		if (Calculator.useMeasurementError && this.accumulatedMaxValue != this.accumulatedMinValue) {
			if (Calculator.showErrorRange) {
				Calculator.inform("Error range = ["+this.accumulatedMinValue.setScale(30, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString()+", "+this.accumulatedMaxValue.setScale(30, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString()+"]");
			}
			BigDecimal maxError = this.accumulatedMaxValue.subtract(this.value).abs().max(this.accumulatedMinValue.subtract(this.value).abs());
			if (maxError.compareTo(new BigDecimal("1e-50")) > 0) {
				String errorLine;
				if (this.isUnitless()) {
					errorLine = "±"+maxError.setScale(30, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString();
				}
				else {
					errorLine = "±"+maxError.setScale(30, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString()+" "+this.measure.baseUnit.abr;
				}
				if (Calculator.showErrorPercentage && this.value.compareTo(BigDecimal.ZERO) != 0 && maxError.divide(this.value, 5, BigDecimal.ROUND_HALF_UP).compareTo(new BigDecimal("5e-5")) > 0) {
					Calculator.inform(errorLine+" Error Å "+maxError.multiply(new BigDecimal("100")).divide(this.value, 5, BigDecimal.ROUND_HALF_UP).setScale(5, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString()+" %");
				}
				else {
					if (Calculator.showErrorPercentage) {
						Calculator.inform(errorLine+" Error < 0.00005 %");
					}
					else {
						Calculator.inform(errorLine);
					}
					
				}
			}
		}
	}

}

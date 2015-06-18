package unitCalc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;

/**
 * This class represents a object that is used to build calculations.
 * It can be a variable, constant, operation, 
 * @author Tommi Oinonen
 *
 */
public class Variable {
	
	static HashMap<String,Variable> varMap = new HashMap<String,Variable>();
	
	public String id = "var";
	public BigDecimal value = null;
	
	public int[] siBase = new int[7];
	public Unit unit = null;
	public Measure measure = null;
	
	//For constatnts
	public boolean isConstant = false;
	public String name;
	
	// For operators
	CalcToken.TokenType op = null;
	boolean isOperation = false;
	
	/** For named variables with unit */
	public Variable(BigDecimal value, String id, int[] siBase) {
		this.value = value;
		this.id = id;
		this.siBase = siBase;
		varMap.put(this.id, this);
	}
	
	/** For unnamed variables */
	public Variable(BigDecimal value, int[] siBase) {
		this.value = value;
		this.siBase = siBase;
	}
	
	/** For unitless variables */
	public Variable(BigDecimal value) {
		this.value = value;
		this.siBase = new int[7];
	}
	
	/** For operators and parenthesis */
	public Variable(CalcToken.TokenType op, boolean isOperator) {
		this.op = op;
		this.id = "op";
		this.isOperation = isOperator;
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
	
	/** For constants */
	public static Variable makeConstant(BigDecimal value, String id, int[] siBase, String name) {
		Variable constant = new Variable(value, id, siBase);
		constant.isConstant = true;
		constant.name = name;
		return constant;
	}
	
	/** Sets the given unit for the variable */
	public void setUnit(Unit unit) {
		this.value = this.value.multiply(unit.baseRelation);
		this.unit = unit.measure.baseUnit;
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
		int[] ansSIbase = new int[7];
		
		if (op == CalcToken.TokenType.UMIN) {
			System.arraycopy(var1.siBase, 0, ansSIbase, 0, 7);
			return new Variable(var1.value.negate(), null, ansSIbase);
		}

		if (op == CalcToken.TokenType.POW) {
			
			
			if (!var2.isUnitless()) {
				Calculator.inform("Math error: powers must be unitless non-negative integers.");
				var2.show();
				return null;
			}
			int powOffLimit = var2.value.compareTo(new BigDecimal("100"));
			if (powOffLimit == 1) {
				Calculator.inform("Math error: powers greater than 100 are not supported for performance reasons.");
				var2.show();
				return null;
			} 
			
			boolean var2isInt;
			int pow = 0;
			//Check that the power is integer
			try {
				pow = var2.value.toBigIntegerExact().intValue();
		        var2isInt = true;
		    } catch (ArithmeticException ex) {
		    	var2isInt = false;
		    }
			 
			if ( var2isInt) {
				if (pow < 0) {
					Calculator.inform("Math error: negative powers are not supported.");
					return null;
				}
				for (int i=0; i<7; i++) {
					ansSIbase[i] = var1.siBase[i]*pow;
				}
				return new Variable(var1.value.pow(pow), null, ansSIbase);
			}
			else {
				Calculator.inform("Math error: only non-negative integer powers are supported.");
				return null;
			}
				
			
		}
		
		if (op == CalcToken.TokenType.MUL) {
			for (int i=0; i<7; i++) {
				ansSIbase[i] = var1.siBase[i] + var2.siBase[i];
			}
			return new Variable(var1.value.multiply(var2.value), null, ansSIbase);
		}
		else if (op == CalcToken.TokenType.DIV) {
			for (int i=0; i<7; i++) {
				ansSIbase[i] = var1.siBase[i] - var2.siBase[i];
			}
			try {
				return new Variable(var1.value.divide(var2.value), null, ansSIbase);
			}
			catch (ArithmeticException ae) {
				return new Variable(var1.value.divide(var2.value, 100, RoundingMode.HALF_UP), null, ansSIbase);
			}
		}
		
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
				return new Variable(var1.value.add(var2.value), null, ansSIbase);
			}
			else if (op == CalcToken.TokenType.SUB) {
				return new Variable(var1.value.subtract(var2.value), null, ansSIbase);
			}
		}
		else {
			Calculator.inform("Error: Can't subtract or add variables that have different measures.");
			var1.show();
			var2.show();
		}
		return null;
	}
	
	
	/** Prints the useful information for this variable or constant. */
	public void show() {
		
		if (this.isConstant) {
			Calculator.inform("Constant: "+this.id+" - "+this.name);
		}
		else if (this.id != null) {
			Calculator.inform("Variable: "+this.id);
		}
		
		int scale = this.value.scale();
		if (scale > 50) {
			scale = 80;
		}
		int resLess = this.value.compareTo(new BigDecimal("0.0001"));
		int resGreater = this.value.compareTo(new BigDecimal("9999999"));
		
		if (this.isUnitless()) {
			if (resLess == -1 || resGreater == 1) {
				Calculator.inform(this.value.setScale(scale, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toEngineeringString());
			}
			else {
				Calculator.inform(this.value.setScale(scale, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString());
			}
		}
		else {
			if (this.unit == null) {
				Measure m = Measure.getMeasure(this.siBase[0], this.siBase[1], this.siBase[2], this.siBase[3], this.siBase[4], this.siBase[5], this.siBase[6]);
				this.measure = m;
				this.unit = m.baseUnit;
			}
			
			if (resLess == -1 || resGreater == 1) {
				Calculator.inform("="+this.value.setScale(scale, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toEngineeringString()+" "+this.unit.measure.baseUnit.abr);
			}
			else {
				Calculator.inform("="+this.value.setScale(scale, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString()+" "+this.unit.measure.baseUnit.abr);
			}
		}
	}

}

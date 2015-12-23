package unitCalc;

import java.math.BigDecimal;

public class Number {
	
	public static final Number pi = new Number("3.141592653589793238462643383279502884197169399375105820"
			+ "974944592307816406286208998628034825342117067982148086513282306647093844609550582231"
			+ "725359408128481117450284102701938521105559644622948954930381964428810975665933446128"
			+ "475648233786783165271201909145648566923460348610454326648213393607260249141273724587"
			+ "0066063155881748815209209628292540917153643678925903600113305305488204665213841469519415116094");
	public static final Number e = new Number("2.71828182845904523536028747135266249775724709369995");
	
	public static int usedDecimals = 100;
	
	BigDecimal value = null;
	BigDecimal minValue = null;
	BigDecimal maxValue = null;
	
	String originalRepresentation = null;
	Number[] calculatedFrom = null;
	
	
	public Number(String stringValue) {
		this.value = new BigDecimal(stringValue);
		this.maxValue = this.value;
		this.minValue = this.value;
		this.originalRepresentation = stringValue;
	}
	
	public Number(BigDecimal value) {
		this.value = value;
		this.maxValue = this.value;
		this.minValue = this.value;
	}
	
	public void setError(String error) {
		if (error.equals("")) {
			//TODO decimal error
		}
		else {
			this.minValue = this.value.add(new BigDecimal(error));
			this.maxValue = this.value.subtract(new BigDecimal(error));
		}
	}
	
	public void setError(Number error) {
		this.minValue = this.value.add(error.value);
		this.maxValue = this.value.subtract(error.value);
	}
	
	
	public String toString() {
		return this.value.toString()+" ["+this.minValue.toString()+", "+this.maxValue.toString()+"]";
	}

	
	public Number add(Number otherNumber) {
		Number sum = new Number(this.value.add(otherNumber.value));
		sum.maxValue = this.maxValue.add(otherNumber.maxValue);
		sum.minValue = this.minValue.add(otherNumber.minValue);
		return sum;
	}
	
	public Number subtract(Number otherNumber) {
		Number ans = new Number(this.value.subtract(otherNumber.value));
		ans.maxValue = this.maxValue.subtract(otherNumber.minValue);
		ans.minValue = this.minValue.subtract(otherNumber.maxValue);
		return ans;
	}
	
	public Number multiply(Number otherNumber) {
		Number ans = new Number(this.value.multiply(otherNumber.value));
		ans.maxValue = this.maxValue.multiply(otherNumber.maxValue);
		ans.maxValue = ans.maxValue.max(this.maxValue.multiply(otherNumber.minValue));
		ans.maxValue = ans.maxValue.max(this.minValue.multiply(otherNumber.minValue));
		ans.maxValue = ans.maxValue.max(this.minValue.multiply(otherNumber.maxValue));
		ans.minValue = this.maxValue.multiply(otherNumber.maxValue);
		ans.minValue = ans.minValue.min(this.maxValue.multiply(otherNumber.minValue));
		ans.minValue = ans.minValue.min(this.minValue.multiply(otherNumber.minValue));
		ans.minValue = ans.minValue.min(this.minValue.multiply(otherNumber.maxValue));
		return ans;
	}
	
	public Number divide(Number otherNumber) {
		Number ans;
		try {
			ans = new Number(this.value.divide(otherNumber.value));
			ans.maxValue = this.maxValue.divide(otherNumber.maxValue);
			ans.maxValue = ans.maxValue.max(this.maxValue.divide(otherNumber.minValue));
			ans.maxValue = ans.maxValue.max(this.minValue.divide(otherNumber.minValue));
			ans.maxValue = ans.maxValue.max(this.minValue.divide(otherNumber.maxValue));
			ans.minValue = this.maxValue.divide(otherNumber.maxValue);
			ans.minValue = ans.minValue.min(this.maxValue.divide(otherNumber.minValue));
			ans.minValue = ans.minValue.min(this.minValue.divide(otherNumber.minValue));
			ans.minValue = ans.minValue.min(this.minValue.divide(otherNumber.maxValue));
			return ans;
		}
		catch (ArithmeticException ae) {
			ans = new Number(this.value.divide(otherNumber.value, Number.usedDecimals, BigDecimal.ROUND_HALF_UP));
			ans.maxValue = this.maxValue.divide(otherNumber.maxValue, Number.usedDecimals, BigDecimal.ROUND_HALF_UP);
			ans.maxValue = ans.maxValue.max(this.maxValue.divide(otherNumber.minValue, Number.usedDecimals, BigDecimal.ROUND_HALF_UP));
			ans.maxValue = ans.maxValue.max(this.minValue.divide(otherNumber.minValue, Number.usedDecimals, BigDecimal.ROUND_HALF_UP));
			ans.maxValue = ans.maxValue.max(this.minValue.divide(otherNumber.maxValue, Number.usedDecimals, BigDecimal.ROUND_HALF_UP));
			ans.minValue = this.maxValue.divide(otherNumber.maxValue, Number.usedDecimals, BigDecimal.ROUND_HALF_UP);
			ans.minValue = ans.minValue.min(this.maxValue.divide(otherNumber.minValue, Number.usedDecimals, BigDecimal.ROUND_HALF_UP));
			ans.minValue = ans.minValue.min(this.minValue.divide(otherNumber.minValue, Number.usedDecimals, BigDecimal.ROUND_HALF_UP));
			ans.minValue = ans.minValue.min(this.minValue.divide(otherNumber.maxValue, Number.usedDecimals, BigDecimal.ROUND_HALF_UP));
		}
		return ans;
	}
	
	public Number factorial() {
		if (this.value.compareTo(BigDecimal.ZERO) <= 0 || !this.hasIntegerValue()) {
			new CalculatorError("Math error", "Factorial is only defined for positive integers.", this);
			return null;
		}
		if (!this.hasNoError()) {
			new CalculatorError("Math error", "Factorial is only supported for exact integers.", this);
			return null;
		}
		BigDecimal ans = new BigDecimal(1);
		BigDecimal multiplier = new BigDecimal(1);
		while (multiplier.compareTo(this.value) < 0) {
			multiplier = multiplier.add(BigDecimal.ONE);
			ans = ans.multiply(multiplier);
		}
		Number factorial = new Number(ans);
		return factorial;
	}
	
	public Number negate() {
		Number ans = new Number(this.value.negate());
		ans.maxValue = this.minValue.negate();
		ans.minValue = this.maxValue.negate();
		return ans;
	}
	
	public Number pow(Number exponent) {
		// TODO: CRITICAL: power with numers
		return null;
	}

	
	public int compareTo(String valueString) {
		return this.value.compareTo(new BigDecimal(valueString));
	}
	
	public boolean isNegative() {
		return this.value.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0;
	}
	
	public boolean isZero() {
		if (this.maxValue.multiply(this.minValue).compareTo(BigDecimal.ZERO) < 0) {
			return true;
		}
		else {
			return this.value.compareTo(BigDecimal.ZERO) == 0;
		}
	}
	
	public boolean hasIntegerValue() {
		if (this.value.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
			return true;
		}
		return false;
	}
	
	public int intValue() {
		return this.value.intValue();
	}

	
	public boolean hasNoError() {
		if (this.maxValue.compareTo(this.minValue) == 0) {
			return true;
		}
		return false;
	}
	
	
	public static void main(String[] args) {
		Number a = new Number("4");
		System.out.println(a.factorial().toString());
		
	}
	
}

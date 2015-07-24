package unitCalc;

import java.math.BigDecimal;

public class FunctionEvaluators {
	
	public interface FunctionEvaluator {
        public Variable evaluate(Variable[] args);
    }
	
	public static void callCommand(FunctionEvaluator evaluator, Variable[] data) {
		evaluator.evaluate(data);
    }
	
	
	// Trigonometric functions
	public class SinEvaluator implements FunctionEvaluator {
        public Variable evaluate(Variable[] arguments) {
        	if (arguments[0].value.remainder(Calculator.pi.value).compareTo(BigDecimal.ZERO) == 0) {
				return new Variable(BigDecimal.ZERO);
			}
			else {
				return new Variable(BigDecimal.valueOf(Math.sin(arguments[0].value.doubleValue())));
			}
        }    
    }
	
	public class AsinEvaluator implements FunctionEvaluator {
		public Variable evaluate(Variable[] arguments) {
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
	}
	
	public class CosEvaluator implements FunctionEvaluator {
		public Variable evaluate(Variable[] arguments) {
			return new Variable(BigDecimal.valueOf(Math.cos(arguments[0].value.doubleValue())));
		}
	}
	
	public class AcosEvaluator implements FunctionEvaluator {
		public Variable evaluate(Variable[] arguments) {
			if ((arguments[0].value.compareTo(BigDecimal.ONE)) > 0 || (arguments[0].value.compareTo(new BigDecimal("-1")) < 0)) {
				Calculator.inform("Math error: arc cos is only defined for values between -1 and 1");
				return null;
			}
			return new Variable(BigDecimal.valueOf(Math.acos(arguments[0].value.doubleValue())));
		}
	}
	
	
	public class TanEvaluator implements FunctionEvaluator {
		public Variable evaluate(Variable[] arguments) {
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
	}
	
	public class AtanEvaluator implements FunctionEvaluator {
		public Variable evaluate(Variable[] arguments) {
			return new Variable(BigDecimal.valueOf(Math.atan(arguments[0].value.doubleValue())));
		}
	}
	
	
	
	// Exponents and logarithms
	public class ExpEvaluator implements FunctionEvaluator {
		public Variable evaluate(Variable[] arguments) {
			if (arguments[0].value.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
				return new Variable(Calculator.e.value.pow(arguments[0].value.intValue()));
			}
			return new Variable(BigDecimal.valueOf(Math.exp(arguments[0].value.doubleValue())));
		}
	}
	
	public class LnEvaluator implements FunctionEvaluator {
		public Variable evaluate(Variable[] arguments) {
			if (arguments[0].value.compareTo(BigDecimal.ZERO) <= 0) {
				Calculator.inform("Math error - Logartihms are only defined for values greater than zero.");
				return null;
			}
			return new Variable(BigDecimal.valueOf(Math.log(arguments[0].value.doubleValue())));
		}
	}
	
	public class LgEvaluator implements FunctionEvaluator {
		public Variable evaluate(Variable[] arguments) {
			if (arguments[0].value.compareTo(BigDecimal.ZERO) <= 0) {
				Calculator.inform("Math error - Logartihms are only defined for values greater than zero.");
				return null;
			}
			return new Variable(BigDecimal.valueOf(Math.log10(arguments[0].value.doubleValue())));
		}
	}
	
	
	
	// Roots
	public class SqrtEvaluator implements FunctionEvaluator {
		public Variable evaluate(Variable[] arguments) {
			if (arguments[0].value.compareTo(BigDecimal.ZERO) < 0) {
				Calculator.inform("Math error - Square root is only supported for non-negative values.");
				return null;
			}
			return new Variable(BigDecimal.valueOf(Math.sqrt(arguments[0].value.doubleValue())));
		}
	}
	
	public class CbrtEvaluator implements FunctionEvaluator {
		public Variable evaluate(Variable[] arguments) {
			return new Variable(BigDecimal.valueOf(Math.cbrt(arguments[0].value.doubleValue())));
		}
	}
	

	
}

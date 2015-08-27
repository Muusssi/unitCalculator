package unitCalc;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;

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
        	Variable ans;
        	if (arguments[0].value.remainder(Calculator.PI).compareTo(BigDecimal.ZERO) == 0) {
				ans = new Variable(BigDecimal.ZERO, null, null);
			}
			else {
				ans = new Variable(BigDecimal.valueOf(Math.sin(arguments[0].value.doubleValue())), null, null);
			}
        	// x - n*2pi -> [-2pi, 2pi] 
        	BigDecimal[] divideAndRemainder = arguments[0].value.divideAndRemainder(Calculator.PIx2);
			BigDecimal translatedMax = arguments[0].accumulatedMaxValue.subtract(Calculator.PIx2.multiply(divideAndRemainder[0]));
			BigDecimal translatedMin = arguments[0].accumulatedMinValue.subtract(Calculator.PIx2.multiply(divideAndRemainder[0]));
			
			if (translatedMin.compareTo(Calculator.PI.multiply(new BigDecimal("-3.5"))) <= 0) {
				ans.accumulatedMaxValue = BigDecimal.ONE;
			}
			if (translatedMin.compareTo(Calculator.PI.multiply(new BigDecimal("-2.5"))) <= 0) {
				ans.accumulatedMinValue = BigDecimal.ONE.negate();
			}
			if (translatedMin.compareTo(Calculator.PIx15.negate()) <= 0 && translatedMax.compareTo(Calculator.PIx15.negate()) >= 0) {
				ans.accumulatedMaxValue = BigDecimal.ONE;
			}
			if (translatedMin.compareTo(Calculator.PIx05.negate()) <= 0 && translatedMax.compareTo(Calculator.PIx05.negate()) >= 0) {
				ans.accumulatedMinValue = BigDecimal.ONE.negate();
			}
			if (translatedMin.compareTo(Calculator.PIx05) <= 0 && translatedMax.compareTo(Calculator.PIx05) >= 0) {
				ans.accumulatedMaxValue = BigDecimal.ONE;
			}
			if (translatedMin.compareTo(Calculator.PIx15) <= 0 && translatedMax.compareTo(Calculator.PIx15) >= 0) {
				ans.accumulatedMinValue = BigDecimal.ONE.negate();
			}
			if (translatedMax.compareTo(Calculator.PI.multiply(new BigDecimal("2.5"))) >= 0) {
				ans.accumulatedMaxValue = BigDecimal.ONE;
			}
			if (translatedMax.compareTo(Calculator.PI.multiply(new BigDecimal("3.5"))) >= 0) {
				ans.accumulatedMinValue = BigDecimal.ONE.negate();
			}
			BigDecimal maxPointValue = BigDecimal.valueOf(Math.sin(arguments[0].accumulatedMaxValue.doubleValue()));
			BigDecimal minPointValue = BigDecimal.valueOf(Math.sin(arguments[0].accumulatedMinValue.doubleValue()));
			ans.accumulatedMaxValue = ans.accumulatedMaxValue.max(maxPointValue);
			ans.accumulatedMaxValue = ans.accumulatedMaxValue.max(minPointValue);
			ans.accumulatedMinValue = ans.accumulatedMinValue.min(maxPointValue);
			ans.accumulatedMinValue = ans.accumulatedMinValue.min(minPointValue);
			
			return ans;
        }    
    }
	
	public class AsinEvaluator implements FunctionEvaluator {
		public Variable evaluate(Variable[] arguments) {
			if ((arguments[0].value.compareTo(BigDecimal.ONE)) > 0 || (arguments[0].value.compareTo(new BigDecimal("-1")) < 0)) {
				Calculator.inform("Math error: arc sin is only defined for values between -1 and 1");
				return null;
			}
			if ((arguments[0].accumulatedMaxValue.compareTo(BigDecimal.ONE)) > 0 || (arguments[0].accumulatedMinValue.compareTo(BigDecimal.ONE.negate()) < 0)) {
				Calculator.inform("Math error: arc sin is only defined for values between -1 and 1\nand because of the measurement error the value could be undefined.");
				return null;
			}
			Variable ans = new Variable(BigDecimal.valueOf(Math.asin(arguments[0].value.doubleValue())), null, null);
			ans.accumulatedMaxValue = BigDecimal.valueOf(Math.asin(arguments[0].accumulatedMaxValue.doubleValue()));
			ans.accumulatedMinValue = BigDecimal.valueOf(Math.asin(arguments[0].accumulatedMinValue.doubleValue()));
			return ans;
		}
	}
	
	public class CosEvaluator implements FunctionEvaluator {
		public Variable evaluate(Variable[] arguments) {
			Variable ans = new Variable(BigDecimal.valueOf(Math.cos(arguments[0].value.doubleValue())), null, null);
			
			BigDecimal[] divideAndRemainder = arguments[0].value.divideAndRemainder(Calculator.PIx2);
			BigDecimal translatedMax = arguments[0].accumulatedMaxValue.subtract(Calculator.PIx2.multiply(divideAndRemainder[0]));
			BigDecimal translatedMin = arguments[0].accumulatedMinValue.subtract(Calculator.PIx2.multiply(divideAndRemainder[0]));
			if (arguments[0].value.compareTo(BigDecimal.ZERO) < 0) {
				BigDecimal tmp = translatedMax;
				translatedMax = translatedMin.negate();
				translatedMin = tmp.negate();
			}
			
			if (translatedMin.compareTo(Calculator.PI.negate()) <= 0) {
				ans.accumulatedMinValue = BigDecimal.ONE.negate();
			}
			if (translatedMin.compareTo(BigDecimal.ZERO) <= 0) {
				ans.accumulatedMaxValue = BigDecimal.ONE;
			}
			if (translatedMin.compareTo(Calculator.PI) <= 0 && translatedMax.compareTo(Calculator.PI) >= 0) {
				ans.accumulatedMinValue = BigDecimal.ONE.negate();
			}
			if (translatedMax.compareTo(Calculator.PIx2) >= 0) {
				ans.accumulatedMaxValue = BigDecimal.ONE;
			}
			if (translatedMax.compareTo(Calculator.PI.multiply(new BigDecimal("3"))) >= 0) {
				ans.accumulatedMinValue = BigDecimal.ONE.negate();
			}
			BigDecimal maxPointValue = BigDecimal.valueOf(Math.cos(arguments[0].accumulatedMaxValue.doubleValue()));
			BigDecimal minPointValue = BigDecimal.valueOf(Math.cos(arguments[0].accumulatedMinValue.doubleValue()));
			ans.accumulatedMaxValue = ans.accumulatedMaxValue.max(maxPointValue);
			ans.accumulatedMaxValue = ans.accumulatedMaxValue.max(minPointValue);
			ans.accumulatedMinValue = ans.accumulatedMinValue.min(maxPointValue);
			ans.accumulatedMinValue = ans.accumulatedMinValue.min(minPointValue);
			
			return ans;
		}
	}
	
	public class AcosEvaluator implements FunctionEvaluator {
		public Variable evaluate(Variable[] arguments) {
			if ((arguments[0].value.compareTo(BigDecimal.ONE)) > 0 || (arguments[0].value.compareTo(BigDecimal.ONE.negate()) < 0)) {
				Calculator.inform("Math error: arc cos is only defined for values between -1 and 1.");
				return null;
			}
			if ((arguments[0].accumulatedMaxValue.compareTo(BigDecimal.ONE)) > 0 || (arguments[0].accumulatedMinValue.compareTo(BigDecimal.ONE.negate()) < 0)) {
				Calculator.inform("Math error: arc cos is only defined for values between -1 and 1\nand because of the measurement error the value could be undefined.");
				return null;
			}
			Variable ans = new Variable(BigDecimal.valueOf(Math.acos(arguments[0].value.doubleValue())), null, null);
			ans.accumulatedMaxValue = BigDecimal.valueOf(Math.acos(arguments[0].accumulatedMinValue.doubleValue()));
			ans.accumulatedMinValue = BigDecimal.valueOf(Math.acos(arguments[0].accumulatedMaxValue.doubleValue()));
			return ans;
		}
	}
	
	
	public class TanEvaluator implements FunctionEvaluator {
		public Variable evaluate(Variable[] arguments) {
			Variable ans = null;
			if (arguments[0].value.remainder(Calculator.PIx05).compareTo(BigDecimal.ZERO) == 0) {
				if (arguments[0].value.remainder(Calculator.PI).compareTo(BigDecimal.ZERO) == 0) {
					ans = new Variable(BigDecimal.ZERO, null, null);
				}
				else {
					Calculator.inform("Math error: tan not defined at ¹/2 ± n*¹");
					return null;
				}
			}
			else {
				ans = new Variable(BigDecimal.valueOf(Math.tan(arguments[0].value.doubleValue())), null, null);
			}
			BigDecimal[] divideAndRemainder = arguments[0].value.add(Calculator.PIx05).divideAndRemainder(Calculator.PI);
			BigDecimal translatedMax = arguments[0].accumulatedMaxValue.add(Calculator.PIx05).subtract(Calculator.PI.multiply(divideAndRemainder[0]));
			BigDecimal translatedMin = arguments[0].accumulatedMinValue.add(Calculator.PIx05).subtract(Calculator.PI.multiply(divideAndRemainder[0]));
			if (translatedMax.compareTo(Calculator.PI) >= 0 || translatedMin.compareTo(BigDecimal.ZERO) <= 0) {
				Calculator.inform("Math error: tan not defined at ¹/2 ± n*¹, these values are\ninside the possible measurement error range and therefore the value could be undefined.");
				return null;
			}
			ans.accumulatedMaxValue = BigDecimal.valueOf(Math.tan(arguments[0].accumulatedMaxValue.doubleValue()));
			ans.accumulatedMinValue = BigDecimal.valueOf(Math.tan(arguments[0].accumulatedMinValue.doubleValue()));
			return ans;
		}
	}
	
	public class AtanEvaluator implements FunctionEvaluator {
		public Variable evaluate(Variable[] arguments) {
			Variable ans = new Variable(BigDecimal.valueOf(Math.atan(arguments[0].value.doubleValue())), null, null);
			ans.accumulatedMaxValue = BigDecimal.valueOf(Math.atan(arguments[0].accumulatedMaxValue.doubleValue()));
			ans.accumulatedMinValue = BigDecimal.valueOf(Math.atan(arguments[0].accumulatedMinValue.doubleValue()));
			return ans;
		}
	}
	
	
	
	// Exponents and logarithms
	public class ExpEvaluator implements FunctionEvaluator {
		public Variable evaluate(Variable[] arguments) {
			Variable ans;
			if (arguments[0].value.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0 && arguments[0].value.compareTo(BigDecimal.ZERO) >= 0) {
				ans = new Variable(Calculator.e.value.pow(arguments[0].value.intValue()), null, null);
				ans.accumulatedMaxValue = Calculator.e.value.pow(arguments[0].value.intValue());
				ans.accumulatedMaxValue = Calculator.e.value.pow(arguments[0].value.intValue());
			}
			else {
				ans = new Variable(BigDecimal.valueOf(Math.exp(arguments[0].value.doubleValue())), null, null);
				ans.accumulatedMaxValue = BigDecimal.valueOf(Math.exp(arguments[0].accumulatedMaxValue.doubleValue()));
				ans.accumulatedMaxValue = BigDecimal.valueOf(Math.exp(arguments[0].accumulatedMinValue.doubleValue()));
			}
			return ans;
		}
	}
	
	public class LnEvaluator implements FunctionEvaluator {
		public Variable evaluate(Variable[] arguments) {
			if (arguments[0].value.compareTo(BigDecimal.ZERO) <= 0) {
				Calculator.inform("Math error - Logartihms are only defined for values greater than zero.");
				return null;
			}
			if (arguments[0].accumulatedMinValue.compareTo(BigDecimal.ZERO) <= 0) {
				Calculator.inform("Math error: Logartihms are only defined for values greater than zero and\nthe accumulated measuremnet error could be negative or zero.");
				return null;
			}
			Variable ans = new Variable(BigDecimal.valueOf(Math.log(arguments[0].value.doubleValue())), null, null);
			ans.accumulatedMaxValue = BigDecimal.valueOf(Math.log(arguments[0].accumulatedMaxValue.doubleValue()));
			ans.accumulatedMaxValue = BigDecimal.valueOf(Math.log(arguments[0].accumulatedMinValue.doubleValue()));
			return ans;
		}
	}
	
	public class LgEvaluator implements FunctionEvaluator {
		public Variable evaluate(Variable[] arguments) {
			if (arguments[0].value.compareTo(BigDecimal.ZERO) <= 0) {
				Calculator.inform("Math error - Logartihms are only defined for values greater than zero.");
				return null;
			}
			if (arguments[0].accumulatedMinValue.compareTo(BigDecimal.ZERO) <= 0) {
				Calculator.inform("Math error: Logartihms are only defined for values greater than zero and\nthe accumulated measuremnet error could be negative or zero.");
				return null;
			}
			Variable ans = new Variable(BigDecimal.valueOf(Math.log10(arguments[0].value.doubleValue())), null, null);
			ans.accumulatedMaxValue = BigDecimal.valueOf(Math.log10(arguments[0].accumulatedMaxValue.doubleValue()));
			ans.accumulatedMaxValue = BigDecimal.valueOf(Math.log10(arguments[0].accumulatedMinValue.doubleValue()));
			return ans;
		}
	}
	
	
	
	// Roots
	public class SqrtEvaluator implements FunctionEvaluator {
		public Variable evaluate(Variable[] arguments) {
			if (arguments[0].value.compareTo(BigDecimal.ZERO) < 0) {
				Calculator.inform("Math error - Square root is only supported for non-negative values.");
				return null;
			}
			if (arguments[0].accumulatedMinValue.compareTo(BigDecimal.ZERO) < 0) {
				Calculator.inform("Math error - Square root is only supported for non-negative values and\nthe accumulated measuremnet error cuold be negative.");
				return null;
			}
			Variable ans = new Variable(BigDecimal.valueOf(Math.sqrt(arguments[0].value.doubleValue())), null, null);
			ans.accumulatedMaxValue = BigDecimal.valueOf(Math.sqrt(arguments[0].accumulatedMaxValue.doubleValue()));
			ans.accumulatedMaxValue = BigDecimal.valueOf(Math.sqrt(arguments[0].accumulatedMinValue.doubleValue()));
			return ans;
		}
	}
	
	public class CbrtEvaluator implements FunctionEvaluator {
		public Variable evaluate(Variable[] arguments) {
			Variable ans = new Variable(BigDecimal.valueOf(Math.cbrt(arguments[0].value.doubleValue())), null, null);
			ans.accumulatedMaxValue = BigDecimal.valueOf(Math.cbrt(arguments[0].accumulatedMaxValue.doubleValue()));
			ans.accumulatedMaxValue = BigDecimal.valueOf(Math.cbrt(arguments[0].accumulatedMinValue.doubleValue()));
			return ans;
		}
	}
	
// Time functions
	public class DateEvaluator implements FunctionEvaluator {
		public Variable evaluate(Variable[] arguments) {
			if (!checkArgumentsAreUnitless("Date", arguments)) {
				return null;
			}
			if (arguments[0].value.compareTo(BigDecimal.ZERO) <= 0 || arguments[0].value.compareTo(new BigDecimal(31)) > 0) {
				Calculator.inform("Date evaluation error - The third argument of date() should be unitles integer between 1 and 31.");
				return null;
			}
			if (arguments[1].value.compareTo(BigDecimal.ZERO) <= 0 || arguments[1].value.compareTo(new BigDecimal(31)) > 0) {
				Calculator.inform("Date evaluation error - The second argument of date() should be unitles integer between 1 and 12.");
				return null;
			}
			if (arguments[2].value.compareTo(BigDecimal.ZERO) <= 0 || arguments[2].value.compareTo(new BigDecimal("9999")) > 0) {
				Calculator.inform("Date evaluation error - The year 0 did not actually exist and negative years not yet supported as well as years after 9999.");
				return null;
			}
			String day = arguments[0].value.toPlainString();
			if (arguments[0].value.compareTo(new BigDecimal("10")) < 0) {
				day = "0"+day;
			}
			String month = arguments[1].value.toPlainString();
			if (arguments[1].value.compareTo(new BigDecimal("10")) < 0) {
				month = "0"+month;
			}
			String year = arguments[2].value.toPlainString();
			if (arguments[2].value.compareTo(new BigDecimal("1000")) < 0) {
				year = "0"+year;
			}
			if (arguments[2].value.compareTo(new BigDecimal("100")) < 0) {
				year = "0"+year;
			}
			if (arguments[2].value.compareTo(new BigDecimal("10")) < 0) {
				year = "0"+year;
			}
			System.out.println(year+"-"+month+"-"+day);
			
		    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		    //df.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
		    Date date = null;
			try {
				date = df.parse(year+"-"+month+"-"+day);
			} catch (ParseException e) {
				Calculator.inform("Date evaluation error - Unable to parse the date.");
				return null;
			}
		    System.out.println(date.toString());
			Variable ans = new Variable(BigDecimal.valueOf(date.getTime()).multiply(new BigDecimal("0.001")), null, null);
			ans.setMeasurementError(new BigDecimal("43200"));//half day error
			ans.setUnit(Unit.unitMap.get("s"));
			return ans;
		}
	}
	
	public class NowEvaluator implements FunctionEvaluator {
		public Variable evaluate(Variable[] arguments) {
			Variable ans = new Variable(BigDecimal.valueOf(new Date().getTime()).multiply(new BigDecimal("0.001")), null, null);
			ans.setMeasurementError(null);
			ans.setUnit(Unit.unitMap.get("s"));
			return ans;
		}
	}
	
	
	private static boolean checkArgumentsAreUnitless(String functionName, Variable[] args) {
		for (int i=0; i< args.length; i++) {
			if (!args[i].isUnitless() || !args[i].isInteger()) {
				Calculator.inform(functionName+" evaluation error - The arguments of "+functionName+"() should be unitles integers.");
				return false;
			}
		}
		return true;
	}

	
}

package tests;

import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import unitCalc.Calculator;

public class CalculatorCalculateTests {

	@Before
	public void setUp() throws Exception {
		Calculator.initCalculator();
	}

	@After
	public void tearDown() throws Exception {
		
	}

	void assertCalculation(String calculation, String answer) {
		Calculator.inform(calculation);
		assertEquals(calculation+" should equal "+answer, new BigDecimal(answer).toPlainString(), Calculator.calculate(calculation).value.stripTrailingZeros().toPlainString());
	}
	
	void assertCalculation(String calculation, String answer, String measureBaseUnitAbr) {
		Calculator.inform(calculation);
		assertEquals(calculation+" should equal "+answer, new BigDecimal(answer).toPlainString(), Calculator.calculate(calculation).value.stripTrailingZeros().toPlainString());
		assertEquals(calculation+" should have unit "+measureBaseUnitAbr, measureBaseUnitAbr, Calculator.calculate(calculation).measure.baseUnit.abr);
	}
	
	void assertError(String calculation) {
		Calculator.inform(calculation);
		assertEquals(calculation+" should evaluate error ", null, Calculator.calculate(calculation));
	}

	@Test
	public void simple_arithmetic_should_work() {
		// Basics
		assertCalculation("4+5", "9");
		assertCalculation("4.0+5.0", "9");
		assertCalculation("4-5", "-1");
		assertCalculation("4.0-5.0", "-1");
		assertCalculation("4*5", "20");
		assertCalculation("4.0*5.0", "20");
		assertCalculation("4/5", "0.8");
		assertCalculation("4.0/5.0", "0.8");
		
		
		// order
		assertCalculation("2-(1+1)", "0");
		assertCalculation("2-1+1", "2");
		assertCalculation("1+2*3", "7");
		assertCalculation("2*2+1", "5");
		assertCalculation("(2*2+1)", "5");
		// Factorial
		assertCalculation("4!", "24");
		assertCalculation("1!", "1");
		
		assertCalculation("2^3", "8");
		assertCalculation("2.5^3", "15.625");
		
	}
	
	@Test
	public void basic_functions_should_work() {
		assertCalculation("sin(0)", "0");
		assertCalculation("sin(pi)", "0");
		assertCalculation("sin(0.5*pi)", "1");
		assertCalculation("sin(-0.5*pi)", "-1");
		
		assertCalculation("cos(0)", "1");
		assertCalculation("cos(pi)", "-1");
		//assertCalculation("cos(0.5*pi)", "0"); TODO needs fixing
		
	}
	
	@Test
	public void arithmetic_with_units_should_work() {
		assertCalculation("1m+2m", "3", "m");
		assertCalculation("1m+2ft", "1.6096", "m");
		assertCalculation("1m-2ft", "0.3904", "m");
		
		assertCalculation("1m*2ft", "0.6096", "m2");
		
		assertCalculation("1N/1m2", "1", "Pa");
		
		assertCalculation("1N^8", "1", "m8kg8|s16");
		
	}

}

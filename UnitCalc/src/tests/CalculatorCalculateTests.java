package tests;

import static org.junit.Assert.*;

import java.awt.TextArea;
import java.math.BigDecimal;

import javax.swing.JTextArea;

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

	@Test
	public void simple_arithmetic_should_work() {
		assertCalculation("4+5", "9");
		assertCalculation("4-5", "-1");
		assertCalculation("4*5", "20");
		assertCalculation("4/5", "0.8");
		
		
		assertCalculation("2-(1+1)", "0");
		assertCalculation("2-1+1", "2");
		assertCalculation("1+2*2", "5");
		assertCalculation("2*2+1", "5");
		assertCalculation("(2*2+1)", "5");
	}
	
	@Test
	public void basic_functions_shuold_work() {
		assertCalculation("sin(0)", "0");
		assertCalculation("sin(pi)", "0");
		assertCalculation("sin(0.5*pi)", "1");
		assertCalculation("sin(-0.5*pi)", "-1");
		
		assertCalculation("cos(0)", "1");
		assertCalculation("cos(pi)", "-1");
		//assertCalculation("cos(0.5*pi)", "0"); TODO needs fixing
	}

}

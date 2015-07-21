package unitCalc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;

import javax.swing.JTextArea;

public class Calculator {
	
	static String version = "1.5";
	
	static String lastCalculation = null;
	static JTextArea resultArea = null;
	
	static Variable pi;
	static Variable e;
	static Measure unitlesMeasure;

	public static void inform(String info) {
		if (resultArea == null) {
			System.out.println(info);
		}
		else {
			resultArea.append(info+"\n");
		}
	}

	public static void setResultArea(JTextArea area) {
		resultArea = area;
	}
	
	public static void showError(int index) {
		Calculator.inform(lastCalculation);
		String pointerLine = "";
		for (int j=0; j<index; j++) {
			pointerLine += " ";
		}
		pointerLine += "^";
		Calculator.inform(pointerLine);
	}
	
	
	public static Variable calculate(String calculation) {
		lastCalculation = calculation;
		if (calculation.equals("const")) {
			Variable.listConstants();
			return null;
		}
		LinkedList<CalcToken> lexing = Parser.lex(calculation);
		
		// Nothing to calculate
		if (lexing == null || lexing.size() == 0) {
			return null;
		}
		int lexingLenth = lexing.size();
		// id query
		if (lexingLenth == 1 && lexing.get(0).type == CalcToken.TokenType.ID) {
			String id = lexing.get(0).id;
			if (Variable.varMap.containsKey(id)) {
				Variable.varMap.get(id).show();
			}
			
			if (Unit.unitMap.containsKey(id)) {
				Unit.unitMap.get(id).show();
			}
			if (Function.functionMap.containsKey(id)) {
				Function f = Function.functionMap.get(id);
				// TODO function.show()
				inform("Function: "+f.name+" - "+f.identifier+"(x)");
			}
			else if (!Variable.varMap.containsKey(id) && !Unit.unitMap.containsKey(id)) {
				inform("Unknown identifier: '"+id+"'");
			}
			
			
		}

		
		// Assignement
		else if (lexingLenth > 2 && lexing.get(1).type == CalcToken.TokenType.EQUAL) {
			if (lexing.get(0).type != CalcToken.TokenType.ID) {
				inform("Error - illegal identifier: '"+lexing.get(0).id+"'");
				return null;
			}
			else if (Function.functionMap.containsKey(lexing.get(0).id)) {
				inform("Error - identifier '"+lexing.get(0).id+"' reserved for a function");
				return null;
			}
			String varName = lexing.poll().id;
			lexing.poll();
			LinkedList<Variable> postFix = toPostFix(lexing);
			Variable ans = evaluate(postFix);
			if (ans != null) {
				ans = new Variable(ans.value, varName, ans.siBase);
				ans.show();
				new Variable(ans.value, "ans", ans.siBase);
			}
		}
		//General expression
		else {
			boolean genTranslate = false;
			String translateTo = null;
			if (lexingLenth > 1 && lexing.getLast().type == CalcToken.TokenType.CONV) {
				lexing.pollLast();
				genTranslate = true; 
			}
			else if (lexingLenth > 2 && lexing.get(lexingLenth-2).type == CalcToken.TokenType.CONV) {
				translateTo = lexing.pollLast().id;
				lexing.pollLast();
			}
			
			LinkedList<Variable> postFix = toPostFix(lexing);
			
			/*
			System.out.println("--------");
			Iterator<Variable> itr = postFix.iterator();
			while (itr.hasNext()) {
				Variable var = itr.next();
				if (var.isOperation || var.isFunction) {
					System.out.print(var.op);
				}
				else {
					System.out.print(var.value.toString());
				}
				System.out.println();
			}
			*/
			
			Variable ans = evaluate(postFix);
			if (ans != null) {
				ans = new Variable(ans.value, "ans", ans.siBase);
				if (genTranslate) {
					ans.show(null);
				}
				else if (translateTo != null) {
					ans.show(translateTo);
				}
				else {
					ans.show();
				}
			}
		}
		Calculator.inform("-----------------------");
		return null;
	}
	
	/** Evaluates an expression which should be in postfix notation */
	public static Variable evaluate(LinkedList<Variable> postFix) {
		if (postFix == null) {
			return null;
		}
		LinkedList<Variable> stack = new LinkedList<Variable>();
		Variable var;
		Variable var1;
		Variable var2;
		while (!postFix.isEmpty()) {
			var = postFix.poll();
			if (var.isOperation) {
				if ((var.op == CalcToken.TokenType.UMIN || var.op == CalcToken.TokenType.FACT) && stack.size() >= 1) {
					// Unary minus
					var1 = stack.pollLast();
					stack.add(Variable.calc(var1, var.op, null));
				}
				else if (stack.size() > 1) {
					var2 = stack.pollLast();
					var1 = stack.pollLast();
					stack.add(Variable.calc(var1, var.op, var2));
				}
				else {
					inform("Syntax error");
					return null;
				}
			}
			else if (var.isFunction) {
				Function func = Function.functionMap.get(var.id);
				if (stack.size() >= func.argNum) {
					// TODO eval func
					Variable[] fargs = new Variable[func.argNum];
					for (int i=0; i<func.argNum; i++) {
						fargs[i] = stack.pollLast();
					}
					stack.add(func.evaluate(fargs));
				}
				else {
					Calculator.inform("Invalid arguments for function: "+func.name);
				}
			}
			else {
				stack.add(var);
			}
		}
		if (stack.size() != 1) {
			inform("Syntax error");
			return null;
		}
		else {
			return stack.poll();
		}
		
	}
	
	public static void print_list(LinkedList<Variable> list) {
		Iterator<Variable> itr = list.iterator();
		while (itr.hasNext()) {
			System.out.println(itr.next().id);
		}
	}
	
	public static LinkedList<Variable> toPostFix(LinkedList<CalcToken> inFix) {
		LinkedList<Variable> postFix = new LinkedList<Variable>();
		LinkedList<Variable> stack = new LinkedList<Variable>();
		if (inFix == null) {
			return null;
		}
		
		Iterator<CalcToken> itr = inFix.iterator();
		CalcToken tok;
		CalcToken previousTok = null;
		boolean expectingUnit = false;
		boolean expectingBegin = false;
		boolean notExpectingIdOrNum = false;
		while (itr.hasNext()) {
			tok = itr.next();
			
			// ID
			if (tok.type == CalcToken.TokenType.ID) {
				if (notExpectingIdOrNum) {
					inform("Syntax error");
					showError(tok.index);
					return null;
				}
				if (expectingUnit) {
					if (Unit.unitMap.containsKey(tok.id)) {
						Variable var = postFix.getLast();
						var.setUnit(Unit.unitMap.get(tok.id));
						expectingUnit = false;
					}
					else {
						inform("Unknown unit: '"+tok.id+"'");
						showError(tok.index);
						return null;
					}
				}
				else {
					if (Variable.varMap.containsKey(tok.id)) {
						postFix.add(Variable.varMap.get(tok.id));
						if (Variable.varMap.get(tok.id).isUnitless()) {
							expectingUnit = true;
						}
						else {
							expectingUnit = false;
						}
					}
					else if (Function.functionMap.containsKey(tok.id)) {
						Variable funcVar = new Variable(CalcToken.TokenType.FUNC, tok.id);
						stack.add(funcVar);
						expectingBegin = true;
					}
					else {
						if (Unit.unitMap.containsKey(tok.id)) {
							inform("Unable to set unit: '"+tok.id+"'");
						}
						else {
							inform("Unknown identifier: '"+tok.id+"'");
						}
						showError(tok.index);
						return null;
					}
				}
			}
			// Number
			else if (tok.type == CalcToken.TokenType.NUM) {
				if (notExpectingIdOrNum) {
					inform("Syntax error");
					showError(tok.index);
					return null;
				}
				BigDecimal a = new BigDecimal(tok.id);
				postFix.add(new Variable(a));
				expectingUnit = true;
			}
			// Starting parenthesis
			else if (tok.type == CalcToken.TokenType.BEGIN) {
				if (notExpectingIdOrNum) {
					inform("Syntax error");
					showError(tok.index);
					return null;
				}
				expectingUnit = false;
				expectingBegin = false;
				stack.add(new Variable(CalcToken.TokenType.BEGIN, false));
			}
			// Ending parenthesis
			else if (tok.type == CalcToken.TokenType.END) {
				notExpectingIdOrNum = false;
				expectingUnit = false;
				Variable var;
				while (true) {
					var = stack.pollLast();
					if (var == null) {
						inform("Syntax error: Parenthesis do not match! Left missing.");
						showError(tok.index);
						return null;
					}
					if (var.isOperation) {
						postFix.add(var);
					}
					else if (var.op == CalcToken.TokenType.BEGIN) {
						if (!stack.isEmpty() && stack.getLast().isFunction) {
							postFix.add(stack.pollLast());
						}
						break;
					}
					else if (var.op == CalcToken.TokenType.FUNC) {
						postFix.add(var);
					}
					else {
						Calculator.inform("ERR");
						return null;
					}
				}
			}
			else if (tok.type == CalcToken.TokenType.FSEP) {
				notExpectingIdOrNum = false;
				expectingUnit = false;
				while (!stack.isEmpty() && stack.getLast().op != CalcToken.TokenType.BEGIN) {
					postFix.add(stack.pollLast());
				}
				if (stack.isEmpty()) {
					Calculator.inform("Syntax error: Unnecessary function separator ';'");
					showError(tok.index);
					return null;
				}
			}
			else if (tok.operator) {
				notExpectingIdOrNum = false;
				if (tok.type == CalcToken.TokenType.FACT) {
					notExpectingIdOrNum = true;
				}
				if (tok.type == CalcToken.TokenType.SUB && (previousTok == null || previousTok.operator || previousTok.type == CalcToken.TokenType.BEGIN)) {
					stack.add(new Variable(CalcToken.TokenType.UMIN, true));
				}
				else {
					expectingUnit = false;
					while (!stack.isEmpty() && evaluatesFirst(stack.getLast().op, tok.type)) {
						postFix.add(stack.pollLast());
					}
					stack.add(new Variable(tok.type, true));
				}
			}
			
			else {
				inform("Syntax error.");
				showError(tok.index);
				return null;
			}
			if (expectingBegin && (!Function.functionMap.containsKey(tok.id) || !itr.hasNext())) {
				Calculator.inform("Syntax error: Expecting () after function "+tok.id);
				Calculator.showError(tok.index);
				return null;
			}
			previousTok = tok;
		}
		while (!stack.isEmpty()) {
			Variable var = stack.pollLast();
			if (var.op == CalcToken.TokenType.BEGIN) {
				inform("Syntax error: Parenthesis do not match! Right missing.");
				return null;
			}
			postFix.add(var);
		}
		//print_list(postFix);
		return postFix;
	}
	
	
	static boolean evaluatesFirst(CalcToken.TokenType fromStack, CalcToken.TokenType nextOp) {
		if (fromStack == CalcToken.TokenType.UMIN || fromStack == CalcToken.TokenType.FACT) {
			return true;
		}
		if (fromStack == CalcToken.TokenType.BEGIN || nextOp == CalcToken.TokenType.BEGIN) {
			return false;
		}
		else if (nextOp == fromStack) {
			return true;
		}
		else if (nextOp == CalcToken.TokenType.SUM || nextOp == CalcToken.TokenType.SUB) {
			return true;
		}
		else if (nextOp == CalcToken.TokenType.DIV || nextOp == CalcToken.TokenType.MUL) {
			if (fromStack != CalcToken.TokenType.SUM && fromStack != CalcToken.TokenType.SUB) {
				return true;
			}
			return false;
		}
		else if (nextOp == CalcToken.TokenType.POW && fromStack != CalcToken.TokenType.POW) {
			return false;
		}
		else {
			return false;
		}
	}

	
	public static void initCalculator() {

		//  [m, kg, s, A, K, Cd, mol ]
		
		// Base units
		Measure length = new Measure("length", 1,0,0,0,0,0,0);
		length.setBaseUnit("meter", "m");
		length.baseUnit.addSIScalers(1);
		length.addUnit("inch", "in", new BigDecimal("0.0254"));
		Unit unit = length.addUnit("thousandth of an inch", "thou", new BigDecimal("0.0000254"));
		unit.addAlternativeAbr("mil");
		unit = length.addUnit("feet", "ft", new BigDecimal("0.3048"));
		unit.addAlternativeAbr("foot");
		length.addUnit("yard", "yd", new BigDecimal("0.9144"));
		length.addUnit("fathom", "fathom", new BigDecimal("1.8288"));
		length.addUnit("mile", "mi", new BigDecimal("1609.344"));
		length.addUnit("nautical mile", "nmi", new BigDecimal("1852"));
		length.addUnit("light-year", "ly", new BigDecimal("9.46055E15"));
		length.addUnit("parsec", "pc", new BigDecimal("3.085678E16"));
		length.addUnit("Astronomical Unit", "AU", new BigDecimal("1.495979E11"));
		length.addUnit("Ångstrom", "Å", new BigDecimal("1E-10"));
		
		
		Measure mass = new Measure("mass", 0,1,0,0,0,0,0);
		mass.setBaseUnit("kilogram", "kg");
		unit = mass.addUnit("gram", "g", new BigDecimal("0.001"));
		unit.addSIScalers(1);
		unit = mass.addUnit("ton", "t", new BigDecimal("1000"));
		unit.addSIScalers(1);
		mass.addUnit("grain", "gr", new BigDecimal("6.479891E-5"));
		mass.addUnit("carat", "ka", new BigDecimal("200E-6"));
		mass.addUnit("stone", "st", new BigDecimal("6.350293"));
		mass.addUnit("pound", "lb", new BigDecimal("0.4535924"));
		mass.addUnit("ounce", "oz", new BigDecimal("0.0283495"));
		mass.addUnit("atomic mass", "u", new BigDecimal("1.6605402E-27"));
		mass.addUnit("slug", "slug", new BigDecimal("14.5939029999917"));
		
		
		Measure time = new Measure("time", 0,0,1,0,0,0,0);
		time.setBaseUnit("seconds", "s");
		time.baseUnit.addSIScalers(1);
		time.addUnit("minute", "min", new BigDecimal("60"));
		time.addUnit("hour", "h", new BigDecimal("3600"));
		time.addUnit("day", "d", new BigDecimal("86400"));
		time.addUnit("week", "wk", new BigDecimal("604800"));
		time.addUnit("month (30d)", "mo", new BigDecimal("2592000"));
		time.addUnit("year", "a", new BigDecimal("31536000"));
		
		Measure electricCurrent = new Measure("electricCurrent", 0,0,0,1,0,0,0);
		electricCurrent.setBaseUnit("ampere", "A");
		electricCurrent.baseUnit.addSIScalers(1);
		
		Measure temperature = new Measure("temperature", 0,0,0,0,1,0,0);
		temperature.setBaseUnit("kelvin", "K");
		temperature.baseUnit.addSIScalers(1);
		temperature.addUnit("celsius", "Cº", new BigDecimal("1"));
		temperature.addUnit("farenheit", "Fº", new BigDecimal("1").divide(new BigDecimal("1.8"), 100, RoundingMode.HALF_UP));
		
		Measure luminousIntencity = new Measure("luminousIntencity", 0,0,0,0,0,1,0);
		luminousIntencity.setBaseUnit("candela", "Cd");
		luminousIntencity.baseUnit.addSIScalers(1);
		
		Measure amountOfSubstance = new Measure("amountOfSubstance", 0,0,0,0,0,0,1);
		amountOfSubstance.setBaseUnit("mole", "mol");
		amountOfSubstance.baseUnit.addSIScalers(1);
		
		
		
		//Derived units with own names
		Measure frequency = new Measure("frequency", 0,0,-1,0,0,0,0);
		frequency.setBaseUnit("hertz", "Hz");
		frequency.baseUnit.addSIScalers(1);
		frequency.addUnit("revolution per minute", "rpm", new BigDecimal("0.01666666666666666666666666666666666666666666666666666666666666667"));

		
		Measure force = new Measure("force", 1,1,-2,0,0,0,0);
		force.setBaseUnit("newton", "N");
		force.baseUnit.addSIScalers(1);
		
		Measure pressure = new Measure("pressure or energy density", -1,1,-2,0,0,0,0);
		//Also energy density
		pressure.setBaseUnit("pascal", "Pa");
		pressure.baseUnit.addSIScalers(1);
		unit = pressure.addUnit("bar", "bar", new BigDecimal("100000"));
		unit.addSIScalers(1);
		unit = pressure.addUnit("joule per cubic meter", "J|m3", new BigDecimal("1"));
		unit.addSIScalers(1);
		pressure.addUnit("atmosferic pressure", "atm", new BigDecimal("101325"));
		pressure.addUnit("torr", "torr", new BigDecimal("101325").divide(new BigDecimal("760"), 100, BigDecimal.ROUND_UP));
		pressure.addUnit("millimeter of mercury", "mmHg", new BigDecimal("133.322387415"));
		pressure.addUnit("inch of mercury", "inHg", new BigDecimal("3386"));
		pressure.addUnit("pound per square inch", "psi", new BigDecimal("6894.757"));
		pressure.addUnit("pound per square foot", "pft", new BigDecimal("47.88026"));

		
		Measure energy = new Measure("energy", 2,1,-2,0,0,0,0);
		energy.setBaseUnit("joule", "J");
		energy.baseUnit.addSIScalers(1);
		unit = energy.addUnit("electron volt", "eV", new BigDecimal("1.6021773E-19"));
		unit.addSIScalers(1);
		unit = energy.addUnit("calories", "cal", new BigDecimal("4.1868"));
		unit.addSIScalers(1);
		unit = energy.addUnit("watt hour", "Wh", new BigDecimal("3.6E3"));
		unit.addSIScalers(1);
		unit = energy.addUnit("newton meter", "Nm", new BigDecimal("1"));
		unit.addSIScalers(1);
		unit = energy.addUnit("foot pound", "ftlb", new BigDecimal("1.3558179483314"));
		unit.addAlternativeAbr("ft_lb");
		energy.addUnit("british termal unit", "Btu", new BigDecimal("1055.05585257348"));
		energy.addUnit("Erg", "erg", new BigDecimal("0.0000001"));
		
		Measure power = new Measure("power", 2,1,-3,0,0,0,0);
		power.setBaseUnit("watt", "W");
		power.baseUnit.addSIScalers(1);
		unit = power.addUnit("joule per second", "J|s", new BigDecimal("1"));
		unit.addSIScalers(1);
		power.addUnit("horse power", "hp", new BigDecimal("735.5"));
		
		Measure electricCharge = new Measure("electricCharge", 0,0,1,1,0,0,0);
		electricCharge.setBaseUnit("coulomb", "C");
		electricCharge.baseUnit.addSIScalers(1);
		
		Measure voltage = new Measure("voltage", 2,1,-3,-1,0,0,0);
		voltage.setBaseUnit("volt", "V");
		voltage.baseUnit.addSIScalers(1);
		
		Measure capacitance = new Measure("capacitance", -2,-1,4,2,0,0,0);
		capacitance.setBaseUnit("farad", "F");
		capacitance.baseUnit.addSIScalers(1);
		
		Measure resistance = new Measure("resistance", 2,1,-3,-2,0,0,0);
		resistance.setBaseUnit("ohm", "Ω");
		resistance.baseUnit.addSIScalers(1);
		resistance.baseUnit.addAlternativeAbr("ohm");
		resistance.baseUnit.addSIScalers(1, "ohm");
		
		Measure conductance = new Measure("conductance", -2,-1,3,2,0,0,0);
		conductance.setBaseUnit("siemens", "S");
		conductance.baseUnit.addSIScalers(1);
		
		Measure magneticFlux = new Measure("magneticFlux", 2,1,-2,-1,0,0,0);
		magneticFlux.setBaseUnit("weber", "Wb");
		magneticFlux.baseUnit.addSIScalers(1);
		unit = magneticFlux.addUnit("maxwell", "Mx", new BigDecimal("10E-9"));
		unit.addSIScalers(1);
		
		Measure magneticFluxDensity = new Measure("magneticFluxDensity", 0,1,-2,-1,0,0,0);
		magneticFluxDensity.setBaseUnit("tesla", "T");
		magneticFluxDensity.baseUnit.addSIScalers(1);
		unit = magneticFluxDensity.addUnit("gauss", "G", new BigDecimal("0.0001"));
		unit.addSIScalers(1);
		
		Measure inductance = new Measure("inductance", 2,1,-2,-2,0,0,0);
		inductance.setBaseUnit("henry", "H");
		inductance.baseUnit.addSIScalers(1);
		
		Measure illuminance = new Measure("illuminance", -2,0,0,0,0,1,0);
		illuminance.setBaseUnit("lux", "lx");
		illuminance.baseUnit.addSIScalers(1);
		
		Measure catalyticActivity = new Measure("catalyticActivity", 0,0,-1,0,0,0,1);
		catalyticActivity.setBaseUnit("katal", "kat");
		catalyticActivity.baseUnit.addSIScalers(1);
		
		
		//Derived units
		Measure area = new Measure("area", 2,0,0,0,0,0,0);
		area.setBaseUnit("square meters", "m2");
		area.baseUnit.addSIScalers(2);
		area.addUnit("square kilometer", "km2", new BigDecimal("1000000"));
		area.addUnit("hectares", "ha", new BigDecimal("10000"));
		area.addUnit("acres", "acre", new BigDecimal("4046.856"));
		area.addUnit("barn", "b", new BigDecimal("1E-28"));
		area.addUnit("square yard", "yd2", new BigDecimal("0.83612736"));
		area.addUnit("square foot", "ft2", new BigDecimal("0.09290304"));
		area.addUnit("square mile", "mi2", new BigDecimal("2589988.110336"));
		area.addUnit("square inch", "in2", new BigDecimal("0.00064516"));
		

		Measure volume = new Measure("volume", 3,0,0,0,0,0,0);
		volume.setBaseUnit("cubic meters", "m3");
		volume.baseUnit.addSIScalers(3);
		volume.addUnit("litre", "l", new BigDecimal("0.001"));
		Unit.unitMap.get("l").addSIScalers(1);
		volume.addUnit("barrel", "bbl", new BigDecimal("0.1589873"));
		volume.addUnit("gallon (US)", "gal", new BigDecimal("0.003785411784"));
		Unit.unitMap.get("gal").addAlternativeAbr("gal_us");
		volume.addUnit("gallon (Imperial)", "gal_uk", new BigDecimal("4.546092E-3"));
		volume.addUnit("cubic feet", "cu_ft", new BigDecimal("0.028316846592"));
		Unit.unitMap.get("cu_ft").addAlternativeAbr("ft3");
		volume.addUnit("cubic inches", "cu_in", new BigDecimal("1.638706e-5"));
		Unit.unitMap.get("cu_in").addAlternativeAbr("in3");
		volume.addUnit("cubic yards", "cu_yd", new BigDecimal("0.7645549"));
		Unit.unitMap.get("cu_yd").addAlternativeAbr("yd3");
		volume.addUnit("cups (US)", "cup", new BigDecimal("0.0002365"));
		volume.addUnit("pint (Imperial)", "pint", new BigDecimal("0.0005682"));
		volume.addUnit("pint (US)", "pints_us", new BigDecimal("0.0004731"));
		volume.addUnit("fluid ounce", "fl_oz", new BigDecimal("0.0000295735296"));
		
		Measure velocity = new Measure("velocity", 1,0,-1,0,0,0,0);
		velocity.setBaseUnit("meter per second", "m|s");
		velocity.baseUnit.addSIScalers(1);
		unit = velocity.addUnit("meter per minute", "m|min", new BigDecimal("1").divide(new BigDecimal("60"), 100, RoundingMode.HALF_UP));
		unit.addSIScalers(1);
		unit = velocity.addUnit("meter per hour", "m|h", new BigDecimal("1").divide(new BigDecimal("3600"), 100, RoundingMode.HALF_UP));
		unit.addSIScalers(1);
		velocity.addUnit("knott", "kn", new BigDecimal("0.5144"));
		velocity.addUnit("mach", "M", new BigDecimal("331"));
		unit = velocity.addUnit("foott per second", "ft|s", new BigDecimal("0.3048"));
		velocity.addUnit("mile per second", "mi|s", new BigDecimal("1609.344"));
		velocity.addUnit("foot per minute", "ft|min", new BigDecimal("0.00508"));
		velocity.addUnit("mile per minute", "mi|min", new BigDecimal("26.8224"));
		velocity.addUnit("mile per hour", "mph", new BigDecimal("1609.344").divide(new BigDecimal("3600"), 100, RoundingMode.HALF_UP));

		Measure pace = new Measure("pace", -1,0,1,0,0,0,0);
		pace.setBaseUnit("second per meter", "s|m");
		pace.addUnit("minute per mile", "min|mi", new BigDecimal("60").divide(new BigDecimal("1609.344"), 100, RoundingMode.HALF_UP));
		pace.addUnit("minute per kilometer", "min|km", new BigDecimal("60").divide(new BigDecimal("1000"), 100, RoundingMode.HALF_UP));
		pace.addUnit("second per kilometer", "s|km", new BigDecimal("1").divide(new BigDecimal("1000"), 100, RoundingMode.HALF_UP));


		Measure volumetricFlow = new Measure("volumetricFlow", 3,0,-1,0,0,0,0);
		volumetricFlow.setBaseUnit("cubic meter per second", "m3|s");
		volumetricFlow.baseUnit.addSIScalers(3);
		
		Measure acceleration = new Measure("acceleration", 1,0,-2,0,0,0,0);
		acceleration.setBaseUnit("meter per second squared", "m|s2");
		acceleration.baseUnit.addSIScalers(1);
		
		Measure jerk = new Measure("jerk", 1,0,-3,0,0,0,0);
		jerk.setBaseUnit("meter per second cubed", "m|s3");
		jerk.baseUnit.addSIScalers(1);
		
		Measure snap = new Measure("snap", 1,0,-4,0,0,0,0);
		snap.setBaseUnit("meter per quartic second", "m|s4");
		snap.baseUnit.addSIScalers(1);
		
		Measure momentum = new Measure("momentum", 1,1,-1,0,0,0,0);
		momentum.setBaseUnit("newton second", "Ns");
		momentum.baseUnit.addSIScalers(1);
		
		Measure yank = new Measure("yank", 1,1,-3,0,0,0,0);
		yank.setBaseUnit("newton per meter", "N|m");
		yank.baseUnit.addSIScalers(1);
		
		Measure wawenumber = new Measure("wawenumber", -1,0,0,0,0,0,0);
		wawenumber.setBaseUnit("reciporal meter", "1|m");
		// TODO add reciporal units
		
		Measure areaDensity = new Measure("areaDensity", -2,1,0,0,0,0,0);
		areaDensity.setBaseUnit("kilogram per square meter", "kg|m2");
		
		Measure density = new Measure("density", -3,1,0,0,0,0,0);
		density.setBaseUnit("kilogram per cubic meter", "kg|m3");
		
		Measure specificVolume = new Measure("specificVolume", 3,-1,0,0,0,0,0);
		specificVolume.setBaseUnit("cubic meter per kilogram", "m3|kg");
		specificVolume.baseUnit.addSIScalers(3);
		
		Measure amounOfSubstanceConcentration = new Measure("amounOfSubstanceConcentration", -3,0,0,0,0,0,1);
		amounOfSubstanceConcentration.setBaseUnit("mole per cubic meter", "mol|m3");
		amounOfSubstanceConcentration.baseUnit.addSIScalers(1);
		
		Measure molarVolume = new Measure("molarVolume", 3,0,0,0,0,0,-1);
		molarVolume.setBaseUnit("cubic meter per mole", "m3|mol");
		molarVolume.baseUnit.addSIScalers(3);
		
		Measure action = new Measure("action or angularMomentum", 2,1,-1,0,0,0,0);
		action.setBaseUnit("joule second", "Js");
		action.baseUnit.addSIScalers(1);
		action.addUnit("newton meter second", "Nms", new BigDecimal("1") );
		Unit.unitMap.get("Nms").addSIScalers(1);
		
		Measure entropy = new Measure("entropy", 2,1,-2,0,-1,0,0);
		entropy.setBaseUnit("joule per kelvin", "J|K");
		entropy.baseUnit.addSIScalers(1);
		
		Measure molarEntropy = new Measure("molarEntropy", 2,1,-2,0,-1,0,-1);
		molarEntropy.setBaseUnit("joule per kelvin mole", "J|Kmol");
		molarEntropy.baseUnit.addSIScalers(1);
		
		Measure specificHeatCapacity = new Measure("specificHeatCapacity", 2,0,-2,0,-1,0,0);
		specificHeatCapacity.setBaseUnit("joule per kilogram kelvin", "J|kgK");
		specificHeatCapacity.baseUnit.addSIScalers(1);
		
		Measure molarEnergy = new Measure("molarEnergy", 2,1,-2,0,0,0,-1);
		molarEnergy.setBaseUnit("joule per mole", "J|mol");
		molarEnergy.baseUnit.addSIScalers(1);
		
		Measure specificEnergy = new Measure("specificEnergy", 2,0,-2,0,0,0,0);
		specificEnergy.setBaseUnit("joule per kilogram", "J|kg");
		specificEnergy.baseUnit.addSIScalers(1);
		
		
		Measure surfaceTension = new Measure("surfaceTension", 0,1,-2,0,0,0,0);
		surfaceTension.setBaseUnit("newton per meter", "N|m");
		surfaceTension.baseUnit.addSIScalers(1);
		surfaceTension.addUnit("joule per square meter", "J|m2", new BigDecimal("1"));
		
		Measure heatFluxDensity = new Measure("heatFluxDensity", 0,1,-3,0,0,0,0);
		heatFluxDensity.setBaseUnit("watt per square meter", "W|m2");
		heatFluxDensity.baseUnit.addSIScalers(1);
		
		Measure thermalConductivity = new Measure("thermalConductivity", 1,1,-3,0,-1,0,0);
		thermalConductivity.setBaseUnit("watt per meter kelvin", "W|mK");
		thermalConductivity.baseUnit.addSIScalers(1);
		
		Measure electricFieldStrength = new Measure("electricFieldStrength", 1,1,-3,-1,0,0,0);
		electricFieldStrength.setBaseUnit("volt per meter", "V|m");
		electricFieldStrength.baseUnit.addSIScalers(1);
		
		Measure kinematicViscosity = new Measure("kinematicViscosity", 2,0,-1,0,0,0,0);
		kinematicViscosity.setBaseUnit("square meter per second", "m2|s");
		kinematicViscosity.baseUnit.addSIScalers(2);
		
		Measure dynamicViscosity = new Measure("dynamicViscosity", -1,1,-1,0,0,0,0);
		dynamicViscosity.setBaseUnit("pascal second", "Pas");
		dynamicViscosity.baseUnit.addSIScalers(1);
		surfaceTension.addUnit("newton seconds per square meter", "Ns|m2", new BigDecimal("1"));
		
		Measure electricDisplacementField = new Measure("electricDisplacementField", -2,0,1,1,0,0,0);
		electricDisplacementField.setBaseUnit("coulomb per square meter", "C|m2");
		electricDisplacementField.baseUnit.addSIScalers(1);
		
		Measure electricChargeDensity = new Measure("electricChargeDensity", -3,0,1,1,0,0,0);
		electricChargeDensity.setBaseUnit("coulomb per cubic meter", "C|m3");
		electricChargeDensity.baseUnit.addSIScalers(1);
		
		Measure electricCurrentDensity = new Measure("electricCurrentDensity", -2,0,0,1,0,0,0);
		electricCurrentDensity.setBaseUnit("ampere per square meter", "A|m2");
		electricCurrentDensity.baseUnit.addSIScalers(1);
		
		Measure conductivity = new Measure("conductivity", -3,-1,3,2,0,0,0);
		conductivity.setBaseUnit("siemens per meter", "S|m");
		conductivity.baseUnit.addSIScalers(1);
		
		Measure molarConductivity = new Measure("molarConductivity", 0,-1,3,2,0,0,-1);
		molarConductivity.setBaseUnit("siemens square meter per mole", "Sm2|mol");
		molarConductivity.baseUnit.addSIScalers(1);
		
		Measure permittivity = new Measure("permittivity", -3,-1,4,2,0,0,0);
		permittivity.setBaseUnit("farad per meter", "F|m");
		permittivity.baseUnit.addSIScalers(1);
		
		Measure permeability = new Measure("permeability", 1,1,-2,-2,0,0,0);
		permeability.setBaseUnit("henry per meter", "H|m");
		permeability.baseUnit.addSIScalers(1);
		
		
		Measure magneticFieldStrength = new Measure("magneticFieldStrength", -1,0,0,1,0,0,0);
		magneticFieldStrength.setBaseUnit("ampere per meter", "A|m");
		magneticFieldStrength.baseUnit.addSIScalers(1);
		
		Measure resistivity = new Measure("resistivity", 3,1,-3,-2,0,0,0);
		resistivity.setBaseUnit("ohm meter", "Ωm");
		resistivity.baseUnit.addSIScalers(1);
		resistivity.addUnit("ohm meter", "ohmm", new BigDecimal("1"));
		Unit.unitMap.get("ohmm").addSIScalers(1);
		
		
		Measure resiporalTemperature = new Measure("resiporalTemperature", 0,0,0,0,-1,0,0);
		resiporalTemperature.setBaseUnit("resiporal kelvin", "1/K");



		//Still missing
		Measure gravitationalConstantMeasure = new Measure("gravitationalConstantMeasure", 3,-1,-2,0,0,0,0);
		gravitationalConstantMeasure.setBaseUnit("newton square meter per square kilogram", "Nm2|kg2");

		//Unitless
		unitlesMeasure = new Measure("unitless", 0,0,0,0,0,0,0);
		unitlesMeasure.setBaseUnit("", "");


		// Constants
		Variable.makeConstant(new BigDecimal("2.99792458E8"), "c", velocity.siBase, "speed of light");
		Variable.makeConstant(new BigDecimal("6.67259E-11"), "G", gravitationalConstantMeasure.siBase, "Newtonian constant of gravitation");
		Variable.makeConstant(new BigDecimal("9.80665"), "g", acceleration.siBase, "normal gravitational acceleration");
		Variable.makeConstant(new BigDecimal("273.15"), "T_0", temperature.siBase, "normal temperature");
		Variable.makeConstant(new BigDecimal("101325"), "p_0", pressure.siBase, "standard atmospheric pressure");
		Variable.makeConstant(new BigDecimal("22.41410E-3"), "V_m", molarVolume.siBase, "molar volume of ideal gas");
		Variable.makeConstant(new BigDecimal("8.314510"), "R", molarEntropy.siBase, "molar gas constant");
		Variable.makeConstant(new BigDecimal("6.0221367E23"), "N_A", Measure.unitlessBase, "Avogadro's constant");
		
		pi = Variable.makeConstant(new BigDecimal("3.14159265358979323846264338327950288419716939937510"), "π", Measure.unitlessBase, "pi");
		pi.addAlternativeId("pi");
		e = Variable.makeConstant(new BigDecimal("2.71828182845904523536028747135266249775724709369995"), "e", Measure.unitlessBase, "Napier's constant");
		
		Variable.makeConstant(new BigDecimal("9.1093897E-31"), "m_e", mass.siBase, "invariant mass of an electron");
		Variable.makeConstant(new BigDecimal("1.6726231E-27"), "m_p", mass.siBase, "invariant mass of a proton");
		Variable.makeConstant(new BigDecimal("1.6749286E-27"), "m_n", mass.siBase, "invariant mass of a neutron");
		Variable.makeConstant(new BigDecimal("3.3435860E-27"), "m_d", mass.siBase, "invariant mass of a deuteron");
		Variable.makeConstant(new BigDecimal("6.644663E-27"), "m_alpha", mass.siBase, "invariant mass of a alpha particle");

		Variable.makeConstant(new BigDecimal("8.85419"), "epsilon_0", permittivity.siBase, "permittivity of vacuum");
		Variable var = Variable.makeConstant(new BigDecimal("1.25664"), "µ_0", permeability.siBase, "permeability of vacuum");
		var.addAlternativeId("mu_0");

		Variable.makeConstant(new BigDecimal("6.6260755E-34"), "h", action.siBase, "Planck's constant");

		// Note frequencys:
		Variable.makeConstant(new BigDecimal("16.35"), "C0", frequency.siBase, "frequency of note C0");
		Variable consti = Variable.makeConstant(new BigDecimal("17.32"), "C#0", frequency.siBase, "frequency of note C#0");
		consti.addAlternativeId("Db0");
		Variable.makeConstant(new BigDecimal("18.35"), "D0", frequency.siBase, "frequency of note D0");
		consti = Variable.makeConstant(new BigDecimal("19.45"), "D#0", frequency.siBase, "frequency of note D#0");
		consti.addAlternativeId("Eb0");
		
		
		
		// units for angle
		
		unit = unitlesMeasure.addUnit("degrees", "º", Variable.varMap.get("pi").value.divide(new BigDecimal("180"), 100, RoundingMode.HALF_UP) );
		unit.addAlternativeAbr("deg");
		unit = unitlesMeasure.addUnit("pi", "π", Variable.varMap.get("pi").value);
		unit.addAlternativeAbr("pi");
		unitlesMeasure.addUnit("radians", "rad", new BigDecimal("1"));
		
	}
	
	public static void main(String[] args) {
		Calculator.initCalculator();
		Function.initFunctionMap();
		
		// the main loop
		Scanner reader = new Scanner(System.in);
		String calculation;
		Variable answer;
		
		while (true) {
			calculation = reader.nextLine();
			answer = calculate(calculation);
			if (answer != null) {
				answer.show();
			}
		}
		
	}

}

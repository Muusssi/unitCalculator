package unitCalc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;

public class Calculator {
	
	static String lastCalculation = null;

	public static void inform(String info) {
		System.out.println(info);
	}
	
	
	public static void showError(int index) {
		System.out.println(lastCalculation);
		String pointerLine = "";
		for (int j=0; j<index; j++) {
			pointerLine += " ";
		}
		pointerLine += "^";
		Calculator.inform(pointerLine);
	}
	
	
	public static Variable calculate(String calculation) {
		lastCalculation = calculation;
		LinkedList<CalcToken> lexing = Parser.lex(calculation);
		// Nothing to calculate
		if (lexing == null || lexing.size() == 0) {
			return null;
		}
		// id query
		else if (lexing.size() == 1 && lexing.get(0).type == CalcToken.TokenType.ID) {
			String id = lexing.get(0).id;
			if (Variable.varMap.containsKey(id)) {
				Variable.varMap.get(id).show();
			}
			if (Unit.unitMap.containsKey(id)) {
				Unit.unitMap.get(id).show();
			} else if (!Variable.varMap.containsKey(id) && !Unit.unitMap.containsKey(id)) {
				inform("Unknown identifier: '"+id+"'");
			}
			
		}
		/*
		// translation
		else if (lexing.size() == 2 && lexing.get(1).type == CalcToken.TokenType.ID) {
			// TODO translation
			System.out.println("Translations not implemented yet");
		}
		*/
		// Assignement
		else if (lexing.size() > 2 && lexing.get(1).type == CalcToken.TokenType.EQUAL) {
			if (lexing.get(0).type != CalcToken.TokenType.ID) {
				inform("Error - illegal identifier: '"+lexing.get(0).id+"'");
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
			LinkedList<Variable> postFix = toPostFix(lexing);
			/*
			System.out.println("--------");
			Iterator<Variable> itr = postFix.iterator();
			while (itr.hasNext()) {
				Variable var = itr.next();
				if (var.isOperation) {
					System.out.println(var.op);
				}
				else {
					System.out.println(var.value.toString());
				}
				
			}
			*/
			Variable ans = evaluate(postFix);
			if (ans != null) {
				ans.show();
				ans = new Variable(ans.value, "ans", ans.siBase);
			}
		}
		System.out.println("-------------");
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
				if (stack.size() > 1) {
					var2 = stack.pollLast();
					var1 = stack.pollLast();
					stack.add(Variable.calc(var1, var.op, var2));
				}
				else {
					inform("Syntax error");
					return null;
				}
			}
			else {
				stack.add(var);
			}
		}
		if (stack.size() != 1) {
			inform("Evaluation error");
			return null;
		}
		else {
			return stack.poll();
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
		boolean expectingUnit = false;
		while (itr.hasNext()) {
			tok = itr.next();
			
			// ID
			if (tok.type == CalcToken.TokenType.ID) {
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
					else {
						inform("Unknown identifier: '"+tok.id+"'");
						showError(tok.index);
						return null;
					}
				}
			}
			// Number
			else if (tok.type == CalcToken.TokenType.NUM) {
				BigDecimal a = new BigDecimal(tok.id);
				postFix.add(new Variable(a));
				expectingUnit = true;
			}
			// Starting parenthesis
			else if (tok.type == CalcToken.TokenType.BEGIN) {
				expectingUnit = false;
				stack.add(new Variable(CalcToken.TokenType.BEGIN, false));
			}
			// Ending parenthesis
			else if (tok.type == CalcToken.TokenType.END) {
				expectingUnit = false;
				Variable var;
				while (true) {
					var = stack.pollLast();
					if (var == null) {
						inform("Syntax error: Parenthesis do not match! Left missing.");
						return null;
					}
					if (var.isOperation) {
						postFix.add(var);
					}
					else if (var.op == CalcToken.TokenType.BEGIN) {
						break;
					}
					else {
						System.out.println("ERR");
						return null;
					}
				}
			}
			else if (tok.operator) {
				expectingUnit = false;
				while (!stack.isEmpty() && evaluatesFirst(stack.getLast().op, tok.type)) {
					postFix.add(stack.pollLast());
				}
				stack.add(new Variable(tok.type, true));
			}
			
			else {
				System.out.println("Unimplemented token in postfixing");
			}
		}
		while (!stack.isEmpty()) {
			Variable var = stack.pollLast();
			if (var.op == CalcToken.TokenType.END) {
				inform("Synatax error: Parenthesis do not match! Right missing.");
				return null;
			}
			postFix.add(var);
		}
		return postFix;
	}
	
	
	static boolean evaluatesFirst(CalcToken.TokenType fromStack, CalcToken.TokenType nextOp) {
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
	
	
	public static void main(String[] args) {
		
		//  [m, kg, s, A, K, Cd, mol ]
		
		// Base units
		Measure length = new Measure("length", 1,0,0,0,0,0,0);
		length.setBaseUnit("meters", "m");
		length.baseUnit.addSIScalers(1);
		length.addUnit("inches", "in", new BigDecimal("0.0254"));
		length.addUnit("feet", "ft", new BigDecimal("0.3048"));
		length.addUnit("yard", "yd", new BigDecimal("0.9144"));
		length.addUnit("miles", "mi", new BigDecimal("1609.344"));
		length.addUnit("nautical miles", "nmi", new BigDecimal("1852"));
		length.addUnit("light-years", "ly", new BigDecimal("9.46055E15"));
		length.addUnit("parsecs", "pc", new BigDecimal("3.085678E16"));
		length.addUnit("Astronomical Units", "AU", new BigDecimal("1.495979E11"));
		length.addUnit("�ngstroms", "�", new BigDecimal("1E-10"));
		
		
		Measure mass = new Measure("mass", 0,1,0,0,0,0,0);
		mass.setBaseUnit("kilograms", "kg");
		mass.addUnit("gram", "g", new BigDecimal("0.001"));
		Unit.unitMap.get("g").addSIScalers(1);
		mass.addUnit("ton", "t", new BigDecimal("1000"));
		Unit.unitMap.get("t").addSIScalers(1);
		mass.addUnit("grain", "gr", new BigDecimal("6.479891E-5"));
		mass.addUnit("carat", "ka", new BigDecimal("200E-6"));
		mass.addUnit("stone", "st", new BigDecimal("6.350293"));
		mass.addUnit("pounds", "lb", new BigDecimal("0.4535924"));
		mass.addUnit("ounces", "oz", new BigDecimal("0.0283495"));
		mass.addUnit("atomic mass", "u", new BigDecimal("1.6605402E-27"));
		
		
		Measure time = new Measure("time", 0,0,1,0,0,0,0);
		time.setBaseUnit("seconds", "s");
		time.baseUnit.addSIScalers(1);
		time.addUnit("minute", "min", new BigDecimal("60"));
		time.addUnit("hour", "h", new BigDecimal("3600"));
		time.addUnit("day", "d", new BigDecimal("86400"));
		time.addUnit("week", "wk", new BigDecimal("604800"));
		time.addUnit("year", "a", new BigDecimal("31536000"));
		
		Measure electricCurrent = new Measure("electricCurrent", 0,0,0,1,0,0,0);
		electricCurrent.setBaseUnit("amperes", "A");
		electricCurrent.baseUnit.addSIScalers(1);
		
		Measure temperature = new Measure("temperature", 0,0,0,0,1,0,0);
		temperature.setBaseUnit("kelvins", "K");
		temperature.baseUnit.addSIScalers(1);
		
		Measure luminousIntencity = new Measure("luminousIntencity", 0,0,0,0,0,1,0);
		luminousIntencity.setBaseUnit("candelas", "Cd");
		luminousIntencity.baseUnit.addSIScalers(1);
		
		Measure amountOfSubstance = new Measure("amountOfSubstance", 0,0,0,0,0,0,1);
		amountOfSubstance.setBaseUnit("moles", "mol");
		amountOfSubstance.baseUnit.addSIScalers(1);
		
		
		
		//Derived units with own names
		Measure frequency = new Measure("frequency", 0,0,-1,0,0,0,0);
		frequency.setBaseUnit("hertz", "Hz");
		frequency.baseUnit.addSIScalers(1);
		frequency.addUnit("revolutions per minute", "rpm", new BigDecimal("0.01666666666666666666666666666666666666666666666666666666666666667"));


		
		Measure force = new Measure("force", 1,1,-2,0,0,0,0);
		force.setBaseUnit("newtons", "N");
		force.baseUnit.addSIScalers(1);
		
		Measure pressure = new Measure("pressure", -1,1,-2,0,0,0,0);
		pressure.setBaseUnit("pascals", "Pa");
		pressure.baseUnit.addSIScalers(1);
		pressure.addUnit("bar", "bar", new BigDecimal("100000"));
		Unit.unitMap.get("bar").addSIScalers(1);
		pressure.addUnit("atmosferic pressure", "atm", new BigDecimal("101325"));
		pressure.addUnit("torr", "torr", new BigDecimal("133.322"));
		pressure.addUnit("atmospheres", "at", new BigDecimal("101.325"));
		pressure.addUnit("millimeter of mercury", "mmHg", new BigDecimal("133.322"));
		pressure.addUnit("pounds per square inch", "psi", new BigDecimal("6894.757"));
		pressure.addUnit("pounds per square foot", "pft", new BigDecimal("47.88026"));
		
		Measure energy = new Measure("energy", 2,1,-2,0,0,0,0);
		energy.setBaseUnit("joules", "J");
		energy.baseUnit.addSIScalers(1);
		energy.addUnit("electron volts", "eV", new BigDecimal("1.6021773E-19"));
		Unit.unitMap.get("eV").addSIScalers(1);
		energy.addUnit("calories", "cal", new BigDecimal("4.1868"));
		Unit.unitMap.get("cal").addSIScalers(1);
		energy.addUnit("watt hours", "Wh", new BigDecimal("3.6E3"));
		Unit.unitMap.get("Wh").addSIScalers(1);
		energy.addUnit("newton meters", "Nm", new BigDecimal("1"));
		Unit.unitMap.get("Nm").addSIScalers(1);
		
		Measure power = new Measure("power", 2,1,-3,0,0,0,0);
		power.setBaseUnit("watts", "W");
		power.baseUnit.addSIScalers(1);
		power.addUnit("horse power", "hp", new BigDecimal("735.5"));
		
		Measure electricCharge = new Measure("electricCharge", 0,0,1,1,0,0,0);
		electricCharge.setBaseUnit("coulombs", "C");
		electricCharge.baseUnit.addSIScalers(1);
		
		Measure voltage = new Measure("voltage", 2,1,-3,-1,0,0,0);
		voltage.setBaseUnit("volts", "V");
		voltage.baseUnit.addSIScalers(1);
		
		Measure capacitance = new Measure("capacitance", -2,-1,4,2,0,0,0);
		capacitance.setBaseUnit("farads", "F");
		capacitance.baseUnit.addSIScalers(1);
		
		Measure resistance = new Measure("resistance", 2,1,-3,-2,0,0,0);
		resistance.setBaseUnit("ohms", "ohm");
		resistance.baseUnit.addSIScalers(1);
		
		Measure conductance = new Measure("conductance", -2,-1,3,2,0,0,0);
		conductance.setBaseUnit("siemens", "S");
		conductance.baseUnit.addSIScalers(1);
		
		Measure magneticFlux = new Measure("magneticFlux", 2,1,-2,-1,0,0,0);
		magneticFlux.setBaseUnit("weber", "Wb");
		magneticFlux.baseUnit.addSIScalers(1);
		magneticFlux.addUnit("maxwell", "Mx", new BigDecimal("10E-9"));
		
		Measure magneticFluxDensity = new Measure("magneticFluxDensity", 0,1,-2,-1,0,0,0);
		magneticFluxDensity.setBaseUnit("teslas", "T");
		magneticFluxDensity.baseUnit.addSIScalers(1);
		magneticFluxDensity.addUnit("gauss", "G", new BigDecimal("0.0001"));
		Unit.unitMap.get("G").addSIScalers(1);
		
		Measure inductance = new Measure("inductance", 2,1,-2,-2,0,0,0);
		inductance.setBaseUnit("henrys", "H");
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
		
		
		
		Measure volume = new Measure("volume", 3,0,0,0,0,0,0);
		volume.setBaseUnit("cubic meters", "m3");
		volume.baseUnit.addSIScalers(3);
		volume.addUnit("litre", "l", new BigDecimal("0.001"));
		Unit.unitMap.get("l").addSIScalers(1);
		volume.addUnit("barrel", "bbl", new BigDecimal("0.1589873"));
		volume.addUnit("gallon (US)", "gal", new BigDecimal("3.785412E-3"));
		Unit.unitMap.get("gal").addAlternativeAbr("gal_us");
		volume.addUnit("gallon (Imperial)", "gal_uk", new BigDecimal("4.546092E-3"));
		volume.addUnit("cubic feet", "cu_ft", new BigDecimal("0.0283168"));
		Unit.unitMap.get("cu_ft").addAlternativeAbr("ft2");
		volume.addUnit("cubic inches", "cu_in", new BigDecimal("1.638706e-5"));
		Unit.unitMap.get("cu_in").addAlternativeAbr("in2");
		volume.addUnit("cubic yards", "cu_yd", new BigDecimal("0.7645549"));
		Unit.unitMap.get("cu_yd").addAlternativeAbr("yd2");
		volume.addUnit("cups (US)", "cup", new BigDecimal("0.0002365"));
		volume.addUnit("pints (Imperial)", "pints", new BigDecimal("0.0005682"));
		volume.addUnit("pints (US)", "pints_us", new BigDecimal("0.0004731"));
		
		Measure velocity = new Measure("velocity", 1,0,-1,0,0,0,0);
		velocity.setBaseUnit("meters per second", "m|s");
		velocity.baseUnit.addSIScalers(1);
		velocity.addUnit("meters per minute", "m|min", new BigDecimal("1").divide(new BigDecimal("60"), 100, RoundingMode.HALF_UP));
		Unit.unitMap.get("m|min").addSIScalers(1);
		velocity.addUnit("meters per hour", "m|h", new BigDecimal("1").divide(new BigDecimal("3600"), 100, RoundingMode.HALF_UP));
		Unit.unitMap.get("m|h").addSIScalers(1);
		velocity.addUnit("knotts", "kn", new BigDecimal("0.5144"));
		velocity.addUnit("mach", "M", new BigDecimal("331"));
		velocity.addUnit("feet per second", "ft|s", new BigDecimal("0.3048"));
		velocity.addUnit("feet per second", "ft|min", new BigDecimal("0.00508"));

		Measure pace = new Measure("pace", -1,0,1,0,0,0,0);
		pace.setBaseUnit("seconds per meter", "s|m");
		pace.addUnit("minutes per mile", "min|mi", new BigDecimal("60").divide(new BigDecimal("1609.344"), 100, RoundingMode.HALF_UP));
		pace.addUnit("minutes per kilometer", "min|km", new BigDecimal("60").divide(new BigDecimal("1000"), 100, RoundingMode.HALF_UP));
		pace.addUnit("seconds per kilometer", "s|km", new BigDecimal("1").divide(new BigDecimal("1000"), 100, RoundingMode.HALF_UP));
		pace.addUnit("seconds per kilometer", "s|km", new BigDecimal("1").divide(new BigDecimal("1000"), 100, RoundingMode.HALF_UP));


		Measure volumetricFlow = new Measure("volumetricFlow", 3,0,-1,0,0,0,0);
		volumetricFlow.setBaseUnit("cubic meters per second", "m3|s");
		volumetricFlow.baseUnit.addSIScalers(3);
		
		Measure acceleration = new Measure("acceleration", 1,0,-2,0,0,0,0);
		acceleration.setBaseUnit("meters per second squared", "m|s2");
		acceleration.baseUnit.addSIScalers(1);
		
		Measure jerk = new Measure("jerk", 1,0,-3,0,0,0,0);
		jerk.setBaseUnit("meters per second cubed", "m|s3");
		jerk.baseUnit.addSIScalers(1);
		
		Measure snap = new Measure("snap", 1,0,-4,0,0,0,0);
		snap.setBaseUnit("meters per quartic second", "m|s4");
		snap.baseUnit.addSIScalers(1);
		
		Measure momentum = new Measure("momentum", 1,1,-1,0,0,0,0);
		momentum.setBaseUnit("newton seconds", "Ns");
		momentum.baseUnit.addSIScalers(1);
		
		Measure angularMomentum = new Measure("angularMomentum", 2,1,-1,0,0,0,0);
		angularMomentum.setBaseUnit("newton meter seconds", "Nms");
		angularMomentum.baseUnit.addSIScalers(1);
		
		Measure yank = new Measure("yank", 1,1,-3,0,0,0,0);
		yank.setBaseUnit("newtons per second", "N|m");
		yank.baseUnit.addSIScalers(1);
		
		Measure wawenumber = new Measure("wawenumber", -1,0,0,0,0,0,0);
		wawenumber.setBaseUnit("reciporal metre", "1|m");
		
		Measure areaDensity = new Measure("areaDensity", -2,1,0,0,0,0,0);
		areaDensity.setBaseUnit("kilograms per square metre", "kg|m2");
		
		Measure density = new Measure("density", -3,1,0,0,0,0,0);
		density.setBaseUnit("kilograms per cubic metre", "kg|m3");
		
		Measure specificVolume = new Measure("specificVolume", 3,-1,0,0,0,0,0);
		specificVolume.setBaseUnit("cubic metres per kilogram", "m3|kg");
		specificVolume.baseUnit.addSIScalers(3);
		
		Measure amounOfSubstanceConcentration = new Measure("amounOfSubstanceConcentration", -3,0,0,0,0,0,1);
		amounOfSubstanceConcentration.setBaseUnit("moles per cubic metre", "mol|m3");
		amounOfSubstanceConcentration.baseUnit.addSIScalers(1);
		
		Measure molarVolume = new Measure("molarVolume", 3,0,0,0,0,0,-1);
		molarVolume.setBaseUnit("cubic metre per mole", "m3|mol");
		molarVolume.baseUnit.addSIScalers(3);
		
		Measure action = new Measure("action", 2,1,-1,0,0,0,0);
		action.setBaseUnit("joule seconds", "Js");
		action.baseUnit.addSIScalers(1);
		
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
		
		Measure energyDensity = new Measure("energyDensity", -1,1,-2,0,0,0,0);
		energyDensity.setBaseUnit("joule per cubic meter", "J|m3");
		energyDensity.baseUnit.addSIScalers(1);
		
		Measure surfaceTension = new Measure("surfaceTension", 0,1,-2,0,0,0,0);
		surfaceTension.setBaseUnit("newton per meter", "N|m");
		surfaceTension.baseUnit.addSIScalers(1);
		surfaceTension.addUnit("joule per square meter", "J|m2", new BigDecimal("1"));
		
		Measure heatFluxDensity = new Measure("heatFluxDensity", 0,1,-3,0,0,0,0);
		heatFluxDensity.setBaseUnit("watts per square meter", "W|m2");
		heatFluxDensity.baseUnit.addSIScalers(1);
		
		Measure thermalConductivity = new Measure("thermalConductivity", 1,1,-3,-1,0,0,0);
		thermalConductivity.setBaseUnit("watts per meter kelvin", "W|mK");
		thermalConductivity.baseUnit.addSIScalers(1);
		
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
		
		Measure electricFieldStrength = new Measure("electricFieldStrength", 1,1,-3,-1,0,0,0);
		electricFieldStrength.setBaseUnit("volt per meter", "V|m");
		electricFieldStrength.baseUnit.addSIScalers(1);
		
		Measure magneticFieldStrength = new Measure("magneticFieldStrength", -1,0,0,1,0,0,0);
		magneticFieldStrength.setBaseUnit("ampere per meter", "A|m");
		magneticFieldStrength.baseUnit.addSIScalers(1);
		
		Measure luminance = new Measure("luminance", -2,0,0,0,0,1,0);
		luminance.setBaseUnit("candela per square meter", "A|m");
		luminance.baseUnit.addSIScalers(1);
		
		Measure resistivity = new Measure("resistivity", 3,1,-3,-2,0,0,0);
		resistivity.setBaseUnit("ohm meter", "ohmm");
		resistivity.baseUnit.addSIScalers(1);
		
		Measure resiporalTemperature = new Measure("resiporalTemperature", 0,0,0,0,-1,0,0);
		resiporalTemperature.setBaseUnit("resiporal kelvin", "1/K");
		
		
		
		//Still missing
		Measure gravitationalConstantMeasure = new Measure("gravitationalConstantMeasure", 3,-1,-2,0,0,0,0);
		gravitationalConstantMeasure.setBaseUnit("newton square meter per square kilogram", "Nm2|kg2");
		
		
		
		
		
		// Constants
		Variable.makeConstant(new BigDecimal("2.99792458E8"), "c", velocity.siBase, "speed of light");
		Variable.makeConstant(new BigDecimal("6.67259E-11"), "G", gravitationalConstantMeasure.siBase, "gravitational constant");
		Variable.makeConstant(new BigDecimal("9.80665"), "g", acceleration.siBase, "normal gravitational acceleration");
		Variable.makeConstant(new BigDecimal("273.15"), "T_0", temperature.siBase, "normal temperature");
		Variable.makeConstant(new BigDecimal("101325"), "p_0", pressure.siBase, "normal pressure");
		Variable.makeConstant(new BigDecimal("22.41410E-3"), "V_m", molarVolume.siBase, "molar volume of ideal gas");
		Variable.makeConstant(new BigDecimal("8.314510"), "R", molarEntropy.siBase, "molar gas constant");
		Variable.makeConstant(new BigDecimal("6.0221367E23"), "N_A", Measure.unitlessBase, "Avogadro's constant");
		
		Variable.makeConstant(new BigDecimal("3.14159265358979323846264338327950288419716939937510"), "pi", Measure.unitlessBase, "pi");
		Variable.makeConstant(new BigDecimal("2.71828182845904523536028747135266249775724709369995"), "e", Measure.unitlessBase, "Napier's constant");
		
		Variable.makeConstant(new BigDecimal("9.1093897E-31"), "m_e", mass.siBase, "invariant mass of an electron");
		Variable.makeConstant(new BigDecimal("1.6726231E-27"), "m_p", mass.siBase, "invariant mass of a proton");
		Variable.makeConstant(new BigDecimal("1.6749286E-27"), "m_n", mass.siBase, "invariant mass of a neutron");
		Variable.makeConstant(new BigDecimal("3.3435860E-27"), "m_d", mass.siBase, "invariant mass of a deuteron");
		Variable.makeConstant(new BigDecimal("6.644663E-27"), "m_alpha", mass.siBase, "invariant mass of a alpha particle");
		
		Variable.makeConstant(new BigDecimal("8.85419"), "epsilon_0", permittivity.siBase, "permittivity of vacuum");
		Variable.makeConstant(new BigDecimal("1.25664"), "mu_0", permeability.siBase, "permeability of vacuum");
		
		Variable.makeConstant(new BigDecimal("6.6260755E-34"), "h", action.siBase, "Planck's constant");
		
		
		
		
		
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

package unitCalc;

import java.math.BigDecimal;
import java.util.HashMap;

public class Unit {
	
	static HashMap<String,Unit> unitMap = new HashMap<String,Unit>();
	
	public String name;
	public String abr;
	public BigDecimal baseRelation;
	public Measure measure;
	
	boolean isBaseUnit = false;
	boolean isAuxiliary = false;
	
	BigDecimal[] scalers = {new BigDecimal("1E24"), new BigDecimal("1E21"), new BigDecimal("1E18"), new BigDecimal("1E15"), new BigDecimal("1E12"),
			new BigDecimal("1E9"), new BigDecimal("1E6"), new BigDecimal("1E3"), new BigDecimal("1E2"), new BigDecimal("10"),
			new BigDecimal("0.1"), new BigDecimal("1E-2"), new BigDecimal("1E-3"), new BigDecimal("1E-6"), new BigDecimal("1E-9"),
			new BigDecimal("1E-12"), new BigDecimal("1E-15"), new BigDecimal("1E-18"), new BigDecimal("1E-21"), new BigDecimal("1E-24")
			};
	String[] preFixes = {"yotta ", "zetta ", "exa ", "peta ", "tera ", "giga ", "mega ", "kilo ", "hecto ", "deca ", "deci ", "centi ", "milli ", "micro ", "nano ", "pico ", "femto ", "atto ", "zepto ", "yocto "};
	String[] abrPreFixes = {"Y", "Z", "E", "P", "T", "G", "M", "k", "h", "da", "d", "c", "m", "�", "n", "p", "f", "a", "z", "y"};
	
	
	public Unit(String name, String abr, BigDecimal baseRelation, Measure measure, boolean isBaseUnit, boolean isAuxiliary) {
		this.name = name;
		this.abr = abr;
		this.baseRelation = baseRelation;
		this.measure = measure;
		this.isBaseUnit = isBaseUnit;
		unitMap.put(this.abr, this);
		this.addNameToUnitMap();
	}
	
	protected void addNameToUnitMap() {
		String slugifiedName = "";
		for (int i=0; i<this.name.length(); i++) {
			if (this.name.charAt(i) == ' ') {
				slugifiedName = slugifiedName + '_';
			}
			else {
				slugifiedName = slugifiedName + this.name.charAt(i);
			}
		}
		unitMap.put(slugifiedName, this);
	}
	
	public void addAlternativeAbr(String altAbr) {
		Unit.unitMap.put(altAbr, this);
	}
	
	/** Prints the useful information for this unit. */
	public void show() {
		if (this.isBaseUnit) {
			Calculator.inform("-- The base unit of ["+this.measure.name+"] = "+this.abr+" : ("+this.name+")");
		} else {
			Calculator.inform("-- Unit of ["+this.measure.name+"] = "+this.abr+" : ("+this.name+")");
			Calculator.inform("1 "+this.abr+" = "+this.baseRelation.toEngineeringString()+" "+this.measure.baseUnit.abr);
		}
	}
	
	public void addSIScalers(int power) {
		this.addSIScalers(power, this.abr);
	}
	
	/** Adds the unit scalers of the metric system for this unit. */
	public void addSIScalers(int power, String abr) {
		BigDecimal offsettMultiplier = this.baseRelation;

		for (int i=0; i<scalers.length; i++) {
			if (!unitMap.containsKey(abrPreFixes[i]+abr)) {
				Unit newUnit;
				if (power == -1) {
					newUnit = new Unit("resiporal "+preFixes[i]+this.name.substring(10), "1|"+abrPreFixes[i]+abr.substring(1), offsettMultiplier.divide(scalers[i], BigDecimal.ROUND_HALF_UP), this.measure, false, true);
					newUnit.addAlternativeAbr(newUnit.abr.substring(1));
				}
				else {
					newUnit = new Unit(preFixes[i]+this.name, abrPreFixes[i]+abr, offsettMultiplier.multiply(scalers[i].pow(power)), this.measure, false, true);
				}
				if (preFixes[i].equals("micro ")) {
					newUnit.addAlternativeAbr("micro"+abr);
					newUnit.addAlternativeAbr("u"+abr);
				}
			}
		}
		
	}
	
	
}

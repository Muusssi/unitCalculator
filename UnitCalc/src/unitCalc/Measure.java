package unitCalc;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;

public class Measure {
	
	static int[] unitlessBase = new int[7];
	
	static HashMap<String,Measure> measureMap = new HashMap<String,Measure>();
	
	public String name;
	public Unit baseUnit;
	public LinkedList<Unit> units = new LinkedList<Unit>();
	public int[] siBase;
	
	public Measure(String name, int m,int kg,int s,int A,int K,int Cd, int mol) {
		this.name = name;
		this.siBase = new int[7];
		siBase[0] = m;
		siBase[1] = kg;
		siBase[2] = s;
		siBase[3] = A;
		siBase[4] = K;
		siBase[5] = Cd;
		siBase[6] = mol;
		String baseMapping = Integer.toString(m)+"|"+Integer.toString(kg)+"|"+Integer.toString(s)+"|"+Integer.toString(A)
								+"|"+Integer.toString(K)+"|"+Integer.toString(Cd)+"|"+Integer.toString(mol);
		if (measureMap.containsKey(baseMapping)) {
			System.out.println("WARNING: duplicate measure "+this.name+" -- "+measureMap.get(baseMapping).name);
		}
		measureMap.put(this.name, this);
		measureMap.put(baseMapping, this);
	}
	
	
	public void setBaseUnit(String name, String abr) {
		this.baseUnit = new Unit(name, abr, new BigDecimal("1"), this, true, false);
	}
	
	public void addUnit(String name, String abr, BigDecimal baseRelation) {
		this.units.add( new Unit(name, abr, baseRelation, this, false, false));
	}
	
	
	
	public static Measure getMeasure(int m,int kg,int s,int A,int K,int Cd, int mol) {
		if (measureMap.containsKey(Integer.toString(m)+"|"+Integer.toString(kg)+"|"+Integer.toString(s)+"|"+Integer.toString(A)
							+"|"+Integer.toString(K)+"|"+Integer.toString(Cd)+"|"+Integer.toString(mol))) {
			return measureMap.get(Integer.toString(m)+"|"+Integer.toString(kg)+"|"+Integer.toString(s)+"|"+Integer.toString(A)
					+"|"+Integer.toString(K)+"|"+Integer.toString(Cd)+"|"+Integer.toString(mol));
		}
		// An uncommon measure -> generate unit and measure
		else {
			
			//Integer.toString(m)+"|"+Integer.toString(kg)+"|"+Integer.toString(s)+"|"+Integer.toString(A)
			//+"|"+Integer.toString(K)+"|"+Integer.toString(Cd)+"|"+Integer.toString(mol)
			Measure newMeasure = generateNewMeasure(m,kg,s,A,K,Cd,mol);
			return newMeasure;
		}
	}
	
	/**
	 * For the situation where the resulting measure is unknown and a new must be generated from the base.
	 */
	public static Measure generateNewMeasure(int m,int kg,int s,int A,int K,int Cd, int mol) {
		
		String newUnitName = "";
		String newUnitNameEnding = "";
		if (m>0) {newUnitName = newUnitName + "m"+Integer.toString(m);}
		if (kg>0) {newUnitName = newUnitName + "kg"+Integer.toString(kg);}
		if (s>0) {newUnitName = newUnitName + "s"+Integer.toString(s);}
		if (A>0) {newUnitName = newUnitName + "A"+Integer.toString(A);}
		if (K>0) {newUnitName = newUnitName + "K"+Integer.toString(K);}
		if (Cd>0) {newUnitName = newUnitName + "Cd"+Integer.toString(Cd);}
		if (mol>0) {newUnitName = newUnitName + "mol"+Integer.toString(mol);}
		if (newUnitName.equals("")) {
			newUnitName = newUnitName +"1";
		}
		if (m<0) {newUnitNameEnding = newUnitNameEnding + "m"+Integer.toString(-m);}
		if (kg<0) {newUnitNameEnding = newUnitNameEnding + "kg"+Integer.toString(-kg);}
		if (s<0) {newUnitNameEnding = newUnitNameEnding + "s"+Integer.toString(-s);}
		if (A<0) {newUnitNameEnding = newUnitNameEnding + "A"+Integer.toString(-A);}
		if (K<0) {newUnitNameEnding = newUnitNameEnding + "K"+Integer.toString(-K);}
		if (Cd<0) {newUnitNameEnding = newUnitNameEnding + "Cd"+Integer.toString(-Cd);}
		if (mol<0) {newUnitNameEnding = newUnitNameEnding + "mol"+Integer.toString(-mol);}
		if (!newUnitNameEnding.equals("")) {
			newUnitName = newUnitName +"|"+newUnitNameEnding;
		}
		
		Measure newMeasure = new Measure(newUnitName, m, kg, s, A, K, Cd, mol);
		newMeasure.setBaseUnit(newUnitName, newUnitName);
		return newMeasure;
		
	}
	
}

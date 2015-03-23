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
		measureMap.put(this.name, this);
		measureMap.put(Integer.toString(m)+"|"+Integer.toString(kg)+"|"+Integer.toString(s)+"|"+Integer.toString(A)
							+"|"+Integer.toString(K)+"|"+Integer.toString(Cd)+"|"+Integer.toString(mol), this);
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
		else {
			return null;
		}
	}
	
}

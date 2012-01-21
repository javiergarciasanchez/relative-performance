package relative_Performance;

import repast.simphony.context.Context;
import repast.simphony.essentials.RepastEssentials;
import repast.simphony.parameter.Parameter;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.*;
import repast.simphony.valueLayer.ValueLayer;

public abstract class Firm {

	public Grid<Object> dSpace;
	public ValueLayer[] perfSpaces;
	private double born;

	private static long firmIDCounter = 1;
	private String firmID = "Firm " + (firmIDCounter++);

	public abstract void decide();

	public Firm(Context<Object> context, Grid<Object> dSpace,
			ValueLayer[] perfSpaces) {

		this.dSpace = dSpace;
		this.perfSpaces = perfSpaces;

		born = RepastEssentials.GetTickCount();

		/*
		 * Add the firm to the context.
		 * 
		 * The adder to dSpace is simpleGridAdder, then the firm should be
		 * located manually
		 */
		context.add(this);
		setFirmLocation(this);

	}

	private void setFirmLocation(Firm firm) {

		GridDimensions dims = dSpace.getDimensions();
		int[] location = new int[dims.size()];
		int[] origin = dims.originToIntArray(null);
		for (int i = 0; i < location.length; i++) {
			location[i] = RandomHelper.getUniform().nextIntFromTo(0,
					dims.getDimension(i) - origin[i] - 1);
		}

		dSpace.moveTo(firm, location);

	}

	public int getPosX() {
		return dSpace.getLocation(this).getX();
	}

	public int getPosY() {
		return dSpace.getLocation(this).getY();
	}

	/*
	 * Creates a string with the performances of all the performance spaces
	 */
	public String getPerformance() {
		String tmpStr = firmID + ": ";
		for (ValueLayer vL : perfSpaces) {
			tmpStr += vL.get(Utils.toDoubleArray(dSpace.getLocation(this)
					.toIntArray(null))) + "; ";
		}
		return tmpStr;
	}

	/*
	 * Returns the performance in perf space given perfspace index
	 */
	public double getPerformance(int i) {
		return perfSpaces[i].get(Utils.toDoubleArray(dSpace.getLocation(this)
				.toIntArray(null)));
	}

	/*
	 * Returns the performance in perf space given the perfspace
	 */
	public double getPerformance(ValueLayer perfSpace) {
		return perfSpace.get(Utils.toDoubleArray(dSpace.getLocation(this)
				.toIntArray(null)));
	}

	public boolean checkEntry() {
		/*
		 * if firm should be able to enter to all performance spaces
		 */
		for (ValueLayer perfSpace : perfSpaces) {
			if (!((PerformanceSpace) perfSpace).checkEntry(this)) {
				return false;
			}
		}

		return true;

	}

	public boolean checkExit() {
		/*
		 * if firm is an exit on any performance space, it should exit
		 */
		for (ValueLayer perfSpace : perfSpaces) {
			if (((PerformanceSpace) perfSpace).checkExit(this)) {
				return true;
			}
		}

		return false;

	}

	public void updateDisplay() {

		for (ValueLayer perfSpace : perfSpaces) {
			((PerformanceSpace) perfSpace).updateDisplay(this);
		}

	}

	public String toString() {
		return firmID;
	}

	@Parameter(displayName = "Age", usageName = "age")
	public double getAge() {
		return RepastEssentials.GetTickCount() - born;
	}

	public double getBorn() {
		return born;
	}

	public String getType() {
		return this.getClass().getName().split("\\.")[1];
	}

	public double isNarrow() {
		if (this instanceof NarrowProfitMaximizer)
			return 1.0;
		else
			return 0.0;
	}

	public double isUtility() {
		if (this instanceof UtilityMaximizer)
			return 1.0;
		else
			return 0.0;
	}

	public double getProfit() {
		return getPerformance(0);
	}

	public double getPerf_I() {
		return getPerformance(1);
	}

	public double getNarrowProfit() {
		if (this instanceof NarrowProfitMaximizer)
			return getPerformance(0);
		else
			return 0.0;
	}

}

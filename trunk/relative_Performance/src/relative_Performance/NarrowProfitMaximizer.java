/**
 * 
 */
package relative_Performance;

import repast.simphony.context.Context;
import repast.simphony.space.grid.Grid;
import repast.simphony.valueLayer.ValueLayer;

/**
 * @author JGSanchez
 *
 */
public class NarrowProfitMaximizer extends Firm {

	/**
	 * 
	 * It maximizes profit without taking into account other dimensions
	 * 
	 * @param context
	 * @param dSpace
	 * @param perfSpaces
	 */
	public NarrowProfitMaximizer(Context<Object> context, Grid<Object> dSpace,
			ValueLayer[] perfSpaces) {
		super(context, dSpace, perfSpaces);
	}

	/* (non-Javadoc)
	 * @see relative_Performance.Firm#decide()
	 */
	@Override
	public void decide() {
		
		DecisionAlternatives alts = new DecisionAlternatives(this);
		
		int[] retDec = this.dSpace.getLocation(this).toIntArray(null);

		/*
		 * Assumes the profit maximizer space has index 0
		 */
		double maxPerf = perfSpaces[0].get(Utils.toDoubleArray(retDec));

		for (int[] alt : alts) {
			double u = perfSpaces[0].get(Utils.toDoubleArray(alt));
			if (u > maxPerf) {
				maxPerf = u;
				retDec = alt;
			}
		}

		dSpace.moveTo(this, retDec);

	}

}

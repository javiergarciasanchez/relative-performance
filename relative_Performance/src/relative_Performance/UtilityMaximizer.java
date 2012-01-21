package relative_Performance;

import repast.simphony.context.Context;
import repast.simphony.space.grid.Grid;
import repast.simphony.valueLayer.ValueLayer;

public class UtilityMaximizer extends Firm {

	public UtilityMaximizer(Context<Object> context, Grid<Object> dSpace,
			ValueLayer[] perfSpaces) {
		super(context, dSpace, perfSpaces);
	}

	public void decide() {

		DecisionAlternatives alts = new DecisionAlternatives(this);
		
		int[] retDec = this.dSpace.getLocation(this).toIntArray(null);

		double maxPerf = utility(retDec);

		for (int[] alt : alts) {
			double u = utility(alt);
			if (u > maxPerf) {
				maxPerf = u;
				retDec = alt;
			}
		}

		dSpace.moveTo(this, retDec);
	}


	private double utility(int... alt) {

		double[] weights = Utils.paramListToDouble("utilityWeights");

		double performance = 0;
		int i = 0;
		for (ValueLayer sp : this.perfSpaces) {
			performance = performance + sp.get(Utils.toDoubleArray(alt))
					* weights[i++];
		}

		return performance;

	}
}

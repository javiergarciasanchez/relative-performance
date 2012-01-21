package relative_Performance;

import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.essentials.RepastEssentials;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.SimpleCartesianAdder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;

public class LandscapeBuilder extends DefaultContext<Object> implements
		ContextBuilder<Object> {

	private static enum PerfSpType {
		FIXED, RELATIVE
	}

	@Override
	public Context<Object> build(Context<Object> context) {

		RunEnvironment instance = RunEnvironment.getInstance();

		/*
		 * Sets the Random Helper Seed
		 */
		Integer seed = (Integer) RepastEssentials.GetParameter("randomSeed");
		if (seed != null)
			RandomHelper.setSeed(seed);

		context.setId("relative_Performance");
		instance.endAt((Double) RepastEssentials.GetParameter("stopAt"));

		/*
		 * Creates the decision space as a Grid
		 */
		int[] dims = new int[2];
		dims[0] = (Integer) RepastEssentials.GetParameter("spaceSize");
		dims[1] = dims[0];

		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);

		Grid<Object> decisionSpace = gridFactory.createGrid("decisionSpace",
				context, new GridBuilderParameters<Object>(
						new repast.simphony.space.grid.StickyBorders(),
						new SimpleGridAdder<Object>(), true, dims));

		/*
		 * Creates the Performance Spaces as Value Layers
		 */

		String[] perfSpTypes = ((String) RepastEssentials
				.GetParameter("perfSpTypes")).split(";");
		int numOfPerformanceSp = perfSpTypes.length;

		PerformanceSpace[] perfSpaces = new PerformanceSpace[numOfPerformanceSp];

		for (int i = 0; i < numOfPerformanceSp; i++) {

			/*
			 * Minimum Performance Handler is assigned according to Performance
			 * Space type
			 */
			PerfSpType tmpPST = PerfSpType.valueOf(perfSpTypes[i]);
			MinPerfHandler minPerfHandler = null;
			switch (tmpPST) {
			case FIXED:
				minPerfHandler = new StaticMinPerf();
				break;
			case RELATIVE:
				minPerfHandler = new PercentileMinPerf();
				break;
			}

			/*
			 * Performance Space with index 0 is the profit maximizing space
			 * "Fourir_Space 1"
			 */
			perfSpaces[i] = new PerformanceSpace(i, context, decisionSpace,
					minPerfHandler, dims);

			context.addValueLayer(perfSpaces[i]);

			// if not a batch run it creates the corresponding 3D space
			if (!instance.isBatch()) {
				createSpace3D(perfSpaces[i], "space3D " + (i + 1), dims,
						context);
			}

		}

		new SpaceTimeManager(context, decisionSpace, perfSpaces, new DataCollector(context, perfSpaces));
		

		return context;
	}

	private void createSpace3D(PerformanceSpace perfSpaces, String name,
			int[] dims, Context<Object> context) {
		/*
		 * Creates the 3 dimensional continuous space to represent agents
		 * walking over landscape
		 */
		double[] dims3D = new double[3];
		double[] origin3D = new double[3];

		// x dimension - Width
		dims3D[0] = dims[0];
		origin3D[0] = 0.0;

		// z dimension - Depth
		dims3D[2] = dims[1];
		origin3D[2] = 0.0;

		// y dimension - Height
		dims3D[1] = (Double) RepastEssentials.GetParameter("dims3DHeight");
		origin3D[1] = dims3D[1] / 2.0;

		ContinuousSpaceFactory space3DFactory = ContinuousSpaceFactoryFinder
				.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space3D = space3DFactory.createContinuousSpace(
				name, context, new SimpleCartesianAdder<Object>(),
				new repast.simphony.space.continuous.StickyBorders(), dims3D,
				origin3D);

		perfSpaces.set3DSpace(space3D);

	}
}

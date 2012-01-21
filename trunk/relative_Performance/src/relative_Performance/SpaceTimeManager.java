package relative_Performance;

import java.util.ArrayList;
import java.util.List;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.essentials.RepastEssentials;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.collections.IndexedIterable;
import repast.simphony.valueLayer.ValueLayer;
import static repast.simphony.engine.schedule.ScheduleParameters.*;

public class SpaceTimeManager {

	private Context<Object> context;
	private Grid<Object> dSpace;
	private ValueLayer[] perfSpaces;
	private DataCollector dataCol;

	public SpaceTimeManager(Context<Object> context, Grid<Object> dSpace,
			ValueLayer[] perfSpaces, DataCollector dataCol) {

		this.context = context;
		this.dSpace = dSpace;
		this.perfSpaces = perfSpaces;
		this.dataCol = dataCol;

		context.add(this);
	}

	@ScheduledMethod(start = 1, interval = 1, priority = FIRST_PRIORITY)
	public void step() {



		manageEntry();

		/*
		 * Firms make their decision
		 */
		for (Object f : context.getObjects(Firm.class)) {
			((Firm) f).decide();
		}
		
		/*
		 * Random variation on every step
		 */
		for (ValueLayer perfSpace : perfSpaces) {
			((PerformanceSpace) perfSpace).introduceRandomness();
			((PerformanceSpace) perfSpace).updateMinPerf();
		}
		
		/*
		 * Exit is checked only after all firms have made their decisions
		 */
		manageExit();

		/*
		 * if not a batch run the 3D display is updated 
		 */
		if (!RunEnvironment.getInstance().isBatch()) {
			for (Object f : context.getObjects(Firm.class)) {
				((Firm) f).updateDisplay();
			}
		}
		
		dataCol.collect();
	}

	private void manageEntry() {
		int numberOfFirms = (Integer) RepastEssentials
				.GetParameter("potencialEntrants");

		Firm f;

		/*
		 * Utility maximizers entry
		 */
		for (int i = 1; i <= numberOfFirms; i++) {
			f = new UtilityMaximizer(context, dSpace, perfSpaces);
			if (!f.checkEntry())
				RepastEssentials.RemoveAgentFromModel(f);
		}
		
		/*
		 * Narrow profit maximizers entry
		 */
		for (int i = 1; i <= numberOfFirms; i++) {
			f = new NarrowProfitMaximizer(context, dSpace, perfSpaces);
			if (!f.checkEntry())
				RepastEssentials.RemoveAgentFromModel(f);
		}
		
	}

	private void manageExit() {

		IndexedIterable<Object> firms = context.getObjects(Firm.class);

		List<Firm> tmpList = new ArrayList<Firm>(firms.size());

		for (Object f : firms) {

			if (((Firm) f).checkExit()){
				tmpList.add((Firm) f);
			}
		}

		for (Firm f : tmpList) {
			RepastEssentials.RemoveAgentFromModel(f);
		}

	}
}

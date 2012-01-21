package relative_Performance;

import java.util.HashMap;
import repast.simphony.context.Context;

public class DataCollector {
	private Context<Object> context;
	private PerformanceSpace[] perfSpaces;
	private static final double NULL_PROFIT = -2;

	/*
	 * For each type of firms it contains the sum of performance for each perf
	 * Space
	 */
	HashMap<Class<? extends Firm>, double[]> sumPerf;
	HashMap<Class<? extends Firm>, Integer> countF;

	public DataCollector(Context<Object> context, PerformanceSpace[] perfSpaces) {
		this.context = context;
		this.perfSpaces = perfSpaces;
		context.add(this);
	}

	public void collect() {

		sumPerf = new HashMap<Class<? extends Firm>, double[]>(3);
		countF = new HashMap<Class<? extends Firm>, Integer>(3);

		double[] tmpArr = new double[perfSpaces.length];
		for (int i = 0; i < tmpArr.length; i++) {
			tmpArr[i] = 0.0;
		}

		for (Object f : context.getObjects(Firm.class)) {

			Class<? extends Firm> fClazz = ((Firm) f).getClass();

			for (PerformanceSpace perfSp : perfSpaces) {
				tmpArr[perfSp.getIndex()] += ((Firm) f).getPerformance(perfSp);
			}

			sumPerf.put(fClazz, tmpArr);

			Integer prev = countF.get(fClazz);
			countF.put(fClazz, (prev == null) ? 1 : prev + 1);

		}
	}

	public double getMeanNarrow(int perfSpIndex) {
		try {
			return getMean(perfSpIndex,
					Class.forName("relative_Performance.NarrowProfitMaximizer"));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return (Double) null;
	}

	public double getMeanUtility(int perfSpIndex) {
		try {
			return getMean(perfSpIndex,
					Class.forName("relative_Performance.UtilityMaximizer"));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return (Double) null;
	}

	private double getMean(int perfSpIndex, Class<?> firmType) {
		double[] sum;
		Integer num = 0;

		sum = sumPerf.get(firmType);
		num = countF.get(firmType);

		if (sum != null)
			return sum[perfSpIndex] / num;
		else
			return NULL_PROFIT;

	}

	public int getCountNarrow() {
		try {
			Integer ret = countF.get(Class
					.forName("relative_Performance.NarrowProfitMaximizer"));
			return (ret == null) ? 0 : ret;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return (Integer) null;
	}

	public int getCountUtility() {
		try {
			Integer ret = countF.get(Class
					.forName("relative_Performance.UtilityMaximizer"));
			return (ret == null) ? 0 : ret;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return (Integer) null;
	}

}

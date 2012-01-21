package relative_Performance;

import repast.simphony.essentials.RepastEssentials;

public final class Utils {

	public static double[] toDoubleArray(int[] coords) {
		double[] tmp = new double[coords.length];
		for (int i = 0; i < coords.length; i++) {
			tmp[i] = (double) coords[i];
		}
		return tmp;
	}

	public static double[] paramListToDouble(String param) {

		String[] tmp = ((String) RepastEssentials
				.GetParameter(param)).split(";");

		double[] ret = new double[tmp.length];

		for (int i = 0; i < ret.length; i++) {
			ret[i] = new Double(tmp[i]);
		}

		return ret;
	}

}

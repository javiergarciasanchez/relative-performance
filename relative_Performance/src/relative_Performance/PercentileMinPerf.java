package relative_Performance;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

public class PercentileMinPerf implements MinPerfHandler {

	@Override
	public void updateMinPerf(PerformanceSpace perfSpace) {
		int numOfFirms = perfSpace.getdSpace().size();

		if (numOfFirms > 1) {
			DescriptiveStatistics firmsPerf = new DescriptiveStatistics();

			for (Object f : perfSpace.getContext().getObjects(Firm.class)) {
				firmsPerf.addValue(((Firm) f).getPerformance(perfSpace));
			}

			double sd = firmsPerf.getStandardDeviation();
			/*
			 * if all the firms have the same performance (sd == 0) minPerf is
			 * not updated
			 */
			if (sd > 0) {

				int idx = perfSpace.getIndex();
				double[] tmp = Utils.paramListToDouble("exitPercentile");
				double z = normSInv(tmp[idx] / 100.0);
				perfSpace.setMinPerf(z*sd+firmsPerf.getMean());
				
				
/*
 * Has convergence problems
 * 
				NormalDistributionImpl norm = new NormalDistributionImpl(
						firmsPerf.getMean(), sd);
				try {
					perfSpace.setMinPerf(norm
							.inverseCumulativeProbability(tmp[idx] / 100.0));
				} catch (MathException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
*/				

			}
		}
	}
	
	//
	// Lower tail quantile for standard normal distribution function.
	//
	// This function returns an approximation of the inverse cumulative
	// standard normal distribution function.  I.e., given P, it returns
	// an approximation to the X satisfying P = Pr{Z <= X} where Z is a
	// random variable from the standard normal distribution.
	//
	// The algorithm uses a minimax approximation by rational functions
	// and the result has a relative error whose absolute value is less
	// than 1.15e-9.
	//
	// Author:      Peter John Acklam
	// (Javascript version by Alankar Misra @ Digital Sutras (alankar@digitalsutras.com))
	// Time-stamp:  2003-05-05 05:15:14
	// E-mail:      pjacklam@online.no
	// WWW URL:     http://home.online.no/~pjacklam
	// Source: http://home.online.no/~pjacklam/notes/invnorm/impl/misra/normsinv.html

	// An algorithm with a relative error less than 1.15*10-9 in the entire region.

	private double normSInv(double p)
	{
	    // Coefficients in rational approximations
	    double[] a = {-3.969683028665376e+01,  2.209460984245205e+02,
	                      -2.759285104469687e+02,  1.383577518672690e+02,
	                      -3.066479806614716e+01,  2.506628277459239e+00};

	    double[] b = {-5.447609879822406e+01,  1.615858368580409e+02,
	                      -1.556989798598866e+02,  6.680131188771972e+01,
	                      -1.328068155288572e+01 };

	    double[] c = {-7.784894002430293e-03, -3.223964580411365e-01,
	                      -2.400758277161838e+00, -2.549732539343734e+00,
	                      4.374664141464968e+00,  2.938163982698783e+00};

	    double[] d = {7.784695709041462e-03, 3.224671290700398e-01,
	                       2.445134137142996e+00,  3.754408661907416e+00};

	    // Define break-points.
	    double plow  = 0.02425;
	    double phigh = 1 - plow;

	    // Rational approximation for lower region:
	    if ( p < plow ) {
	             double q  = Math.sqrt(-2*Math.log(p));
	             return (((((c[0]*q+c[1])*q+c[2])*q+c[3])*q+c[4])*q+c[5]) /
	                                             ((((d[0]*q+d[1])*q+d[2])*q+d[3])*q+1);
	    }

	    // Rational approximation for upper region:
	    if ( phigh < p ) {
	             double q  = Math.sqrt(-2*Math.log(1-p));
	             return -(((((c[0]*q+c[1])*q+c[2])*q+c[3])*q+c[4])*q+c[5]) /
	                                                    ((((d[0]*q+d[1])*q+d[2])*q+d[3])*q+1);
	    }

	    // Rational approximation for central region:
	    double q = p - 0.5;
	    double r = q*q;
	    return (((((a[0]*r+a[1])*r+a[2])*r+a[3])*r+a[4])*r+a[5])*q /
	                             (((((b[0]*r+b[1])*r+b[2])*r+b[3])*r+b[4])*r+1);
	}
}

package relative_Performance;

import cern.jet.random.Binomial;
import cern.jet.random.Normal;
import repast.simphony.context.Context;
import repast.simphony.essentials.RepastEssentials;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.Dimensions;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.valueLayer.GridValueLayer;
import repast.simphony.valueLayer.ValueLayer;
import static java.lang.Math.*;

/*
 * Implements a Performance Landscape based on Fourier Synthesis. See Winter & Cattani 2007
 */

public class PerformanceSpace implements ValueLayer {
	private static final double MIN_COEF = 0.0;
	private static final double MAX_COEF = 1.0;
	private static final double MIN_COS = -PI / pow(2.0, 1.0 / 2.0);
	private static final double MAX_COS = PI / pow(2.0, 1.0 / 2.0);

	private enum CoefType {
		INIT_COEF, NOISE_COEF, SHOCK_COEF
	}

	private int index;
	private GridValueLayer grid;
	private Context<Object> context;
	private Grid<Object> dSpace;
	private ContinuousSpace<Object> space3D;

	int freqs = (Integer) RepastEssentials.GetParameter("freqs");
	int size;
	private double levelRange;

	// private boolean firmsMoved;
	private double minPerf;
	private MinPerfHandler minPerfHandler;

	private Normal noiseNormal = RandomHelper.createNormal(0.0,
			(Double) RepastEssentials.GetParameter("noiseStdDev"));
	private Normal shockNormal = RandomHelper.createNormal(1.0,
			(Double) RepastEssentials.GetParameter("shockStdDev"));
	private Binomial shockProb = RandomHelper.createBinomial(1,
			(Double) RepastEssentials.GetParameter("shockProbability"));
	private double ruggedness = (Double) RepastEssentials
			.GetParameter("ruggedness");

	public PerformanceSpace(int index, Context<Object> context,
			Grid<Object> dSpace, MinPerfHandler minPerfHandler, int... dims) {

		this.index = index;
		this.context = context;
		this.dSpace = dSpace;
		this.minPerfHandler = minPerfHandler;

		grid = new GridValueLayer(getName(), 0.0, true, dims);
		new GridValueLayer(getName(), 0.0, true, dims);

		/*
		 * The size of all dimensions is the same. The size is the number of
		 * steps: points - 1
		 */
		size = dims[0] - 1;

		/*
		 * The grid is filled by adding the different waves Each wave has its
		 * own coefficient and its own shift
		 */
		for (int nX = 0; nX <= freqs - 1; nX++) {
			for (int nZ = 0; nZ <= freqs - 1; nZ++) {

				double[] shift = new double[2];
				double coef;

				getRandomShift(shift, nX, nZ);
				coef = getRandomCoef(nX, nZ, CoefType.INIT_COEF);

				addWave(nX, nZ, shift, coef);

			}
		}

		/*
		 * Sets initial minPerformance
		 */
		setInitialMinPerf();

	}

	private void addWave(int nX, int nZ, double[] shift, double coef) {

		/*
		 * Apply the two frequencies to the whole layer
		 */
		for (int x = 0; x <= size; x++) {
			for (int z = 0; z <= size; z++) {

				double tmp = grid.get(x, z) + coef
						* cosWave(x, z, shift, nX, nZ);

				grid.set(tmp, x, z);
			}
		}
	}

	private double getRandomCoef(int nX, int nZ, CoefType type) {

		double freqX;
		double freqZ;
		double coef = 0;

		freqX = (nX == 0) ? 1.0 : nX * size / (freqs - 1);
		freqZ = (nZ == 0) ? 1.0 : nZ * size / (freqs - 1);

		switch (type) {
		case INIT_COEF:
			coef = RandomHelper.nextDoubleFromTo(MIN_COEF, MAX_COEF);
			break;
		case NOISE_COEF:
			coef = noiseNormal.nextDouble();
			break;
		case SHOCK_COEF:
			coef = shockNormal.nextDouble();
			break;

		}

		/*
		 * An adjustment is made for rugeddness (more or less weight to a
		 * specific coef depending on wave length).
		 */
		return coef
				* pow(pow(freqX, 2.0) + pow(freqZ, 2.0),
						-(ruggedness + 1.0) / 2.0);

	}

	private void getRandomShift(double[] shift, int nX, int nZ) {
		shift[0] = RandomHelper.nextDoubleFromTo(MIN_COS, MAX_COS);
		shift[1] = RandomHelper.nextDoubleFromTo(MIN_COS, MAX_COS);
	}

	private double cosWave(double x, double z, double[] shift, int nX, int nZ) {

		double freqX = (nX == 0) ? 1.0 : nX * size / (freqs - 1);
		double freqZ = (nZ == 0) ? 1.0 : nZ * size / (freqs - 1);

		double renVarX = MIN_COS + (MAX_COS - MIN_COS) / size * x;
		double renVarZ = MIN_COS + (MAX_COS - MIN_COS) / size * z;

		return cos(pow(
				pow((renVarX - shift[0]) * freqX, 2)
						+ pow((renVarZ - shift[1]) * freqZ, 2), (1.0 / 2.0)));
	}

	private void setInitialMinPerf() {

		double max = 0.0, min = 0.0;

		for (int x = 0; x <= size; x++) {
			for (int z = 0; z <= size; z++) {
				double value = this.get((double) x, (double) z);
				max = Math.max(max, value);
				min = Math.min(min, value);

			}
		}

		/*
		 * Sets initial minimum performance to average between max and min of
		 * valuelayer.
		 */
		double[] tmp = Utils.paramListToDouble("iniMinWeight");
		setMinPerf(min * tmp[index] + max * (1 - tmp[index]));

	}

	public void introduceRandomness() {

		/*
		 * Introducing shock
		 */
		if (shockProb.nextInt() == 1) {
			/*
			 * Adds just the maximum wave with random shift and coef
			 */
			double[] shift = new double[2];
			double coef;

			getRandomShift(shift, freqs, freqs);
			coef = getRandomCoef(freqs, freqs, CoefType.SHOCK_COEF);

			addWave(freqs, freqs, shift, coef);

		}

		// Introduce noise
		for (int freq = 0; freq <= freqs; freq++) {
			double[] shift = new double[2];
			double coef;

			getRandomShift(shift, freqs, freqs);
			coef = getRandomCoef(freq, freq, CoefType.NOISE_COEF);
			addWave(freq, freq, shift, coef);
		}

	}

	@Override
	public double get(double... coordinates) {
		return grid.get(coordinates);
	}

	@Override
	public Dimensions getDimensions() {
		return grid.getDimensions();
	}

	public boolean checkExit(Firm firm) {

		if (firm.getPerformance(this) < getMinPerf())
			return true;
		else
			return false;
	}

	public void updateMinPerf() {
		minPerfHandler.updateMinPerf(this);
	}

	public boolean checkEntry(Firm firm) {

		if (firm.getPerformance(this) < getMinPerf())
			return false;
		else
			return true;

	}

	public double getMinPerf() {
		return minPerf;
	}

	public void setMinPerf(double minPerf) {
		this.minPerf = minPerf;
	}

	public Context<Object> getContext() {
		return context;
	}

	public Grid<Object> getdSpace() {
		return dSpace;
	}

	/*
	 * Updates 3D display establishing decision coordinates and height as the
	 * value from value layer
	 */
	public void updateDisplay(Firm firm) {

		GridPoint pt = dSpace.getLocation(firm);
		double[] dec3D = new double[3];

		dec3D[0] = pt.getX();
		dec3D[2] = size - pt.getY();

		dec3D[1] = firm.getPerformance(this);

		// scale adjustment
		double scale = (Double) RepastEssentials.GetParameter("scale");
		double vertShift = (Double) RepastEssentials.GetParameter("vertShift");
		dec3D[1] = dec3D[1] * scale + vertShift;

		space3D.moveTo(firm, dec3D);

	}

	public void set3DSpace(ContinuousSpace<Object> space3D) {
		this.space3D = space3D;

	}

	@Override
	public String getName() {
		return "Fourier_Space " + (index + 1);
	}

	@Override
	public String toString() {
		return "Fourier_Space " + (index + 1);
	}

	public int getIndex() {
		return index;
	}

	public double getLevelRange() {
		return levelRange;
	}
}

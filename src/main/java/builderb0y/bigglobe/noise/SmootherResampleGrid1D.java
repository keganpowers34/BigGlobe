package builderb0y.bigglobe.noise;

import builderb0y.bigglobe.math.Interpolator;
import builderb0y.bigglobe.noise.Polynomial.SmootherPolynomial;

public class SmootherResampleGrid1D extends Resample2Grid1D {

	public SmootherResampleGrid1D(Grid1D source, int scaleX) {
		super(source, scaleX);
	}

	@Override
	public double getMaxOvershoot() {
		return 1.0D;
	}

	@Override
	public Polynomial polynomial(double value0, double value1) {
		return new SmootherPolynomial(value0, value1);
	}

	@Override
	public double interpolate(double value0, double value1, double fraction) {
		return Interpolator.mixSmoother(value0, value1, fraction);
	}
}
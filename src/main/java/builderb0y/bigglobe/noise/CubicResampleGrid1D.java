package builderb0y.bigglobe.noise;

import builderb0y.bigglobe.math.Interpolator;
import builderb0y.bigglobe.noise.Polynomial.CubicPolynomial;

public class CubicResampleGrid1D extends Resample4Grid1D {

	public CubicResampleGrid1D(Grid1D source, int scaleX) {
		super(source, scaleX);
	}

	@Override
	public double getMaxOvershoot() {
		return 1.125D;
	}

	@Override
	public Polynomial polynomial(double value0, double value1, double value2, double value3) {
		return new CubicPolynomial(value0, value1, value2, value3);
	}

	@Override
	public double interpolate(double value0, double value1, double value2, double value3, double fraction) {
		return Interpolator.mixCubic(value0, value1, value2, value3, fraction);
	}
}
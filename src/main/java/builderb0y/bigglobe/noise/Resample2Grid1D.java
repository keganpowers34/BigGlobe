package builderb0y.bigglobe.noise;

import builderb0y.bigglobe.math.BigGlobeMath;

/** a ResampleGrid1D which internally interpolates between 2 sample points. */
public abstract class Resample2Grid1D extends ResampleGrid1D {

	public Resample2Grid1D(Grid1D source, int scaleX) {
		super(source, scaleX);
	}

	@Override
	public double getValue(long seed, int x) {
		int fracX = BigGlobeMath.modulus_BP(x, this.scaleX);
		return this.interpolate(
			this.source.getValue(seed, x -= fracX),
			this.source.getValue(seed, x + this.scaleX),
			fracX * this.rcpX
		);
	}

	@Override
	public void getBulkX(long seed, int startX, NumberArray samples) {
		int sampleCount = samples.length();
		if (sampleCount <= 0) return;
		int scaleX    = this.scaleX;
		Grid1D source = this.source;
		int fracX     = BigGlobeMath.modulus_BP(startX, scaleX);
		int gridX     = startX - fracX;
		Polynomial polynomial = this.polynomial(
			source.getValue(seed, gridX),
			source.getValue(seed, gridX += scaleX)
		);
		for (int index = 0; true /* break in the middle of the loop */;) {
			samples.setD(index, polynomial.interpolate(fracX * this.rcpX));
			if (++index >= sampleCount) break;
			if (++fracX >= scaleX) {
				fracX  = 0;
				polynomial.push(source.getValue(seed, gridX += scaleX));
			}
		}
	}

	public abstract Polynomial polynomial(double value0, double value1);

	public abstract double interpolate(double value0, double value1, double fraction);
}
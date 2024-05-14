package builderb0y.bigglobe.noise;

import builderb0y.autocodec.annotations.VerifyIntRange;
import builderb0y.bigglobe.math.Interpolator;
import builderb0y.bigglobe.noise.polynomials.Polynomial.PolyForm;

public abstract class ResampleGrid2D implements Grid2D {

	public final Grid2D source;
	public final @VerifyIntRange(min = 0, minInclusive = false) int scaleX, scaleY;
	public final transient double rcpX, rcpY;
	public final transient double minValue, maxValue;

	public ResampleGrid2D(Grid2D source, int scaleX, int scaleY) {
		this.source = source;
		this.rcpX = 1.0D / (this.scaleX = scaleX);
		this.rcpY = 1.0D / (this.scaleY = scaleY);
		double overshoot = this.polyFormX().getMaxOvershoot() * this.polyFormY().getMaxOvershoot();
		this.minValue = Interpolator.mixLinear(source.maxValue(), source.minValue(), overshoot);
		this.maxValue = Interpolator.mixLinear(source.minValue(), source.maxValue(), overshoot);
	}

	@Override
	public double minValue() {
		return this.minValue;
	}

	@Override
	public double maxValue() {
		return this.maxValue;
	}

	public abstract PolyForm polyFormX();

	public abstract PolyForm polyFormY();
}
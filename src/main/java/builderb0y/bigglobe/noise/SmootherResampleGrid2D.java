package builderb0y.bigglobe.noise;

import builderb0y.bigglobe.math.Interpolator;
import builderb0y.bigglobe.noise.polynomials.OvershootConstants;
import builderb0y.bigglobe.noise.polynomials.Polynomial;
import builderb0y.bigglobe.noise.polynomials.Polynomial2.PolyForm2;
import builderb0y.bigglobe.noise.polynomials.SmootherPolynomial;

public class SmootherResampleGrid2D extends Resample4Grid2D {

	public SmootherResampleGrid2D(Grid2D source, int scaleX, int scaleY) {
		super(source, scaleX, scaleY);
	}

	@Override
	public PolyForm2 polyFormX() {
		return SmootherPolynomial.FORM;
	}

	@Override
	public PolyForm2 polyFormY() {
		return SmootherPolynomial.FORM;
	}
}
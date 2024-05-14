package builderb0y.bigglobe.noise;

import builderb0y.bigglobe.noise.polynomials.DerivativeSmootherPolynomial;
import builderb0y.bigglobe.noise.polynomials.Polynomial2.PolyForm2;
import builderb0y.bigglobe.noise.polynomials.SmootherPolynomial;

public class DySmootherResampleGrid2D extends Resample4Grid2D {

	public DySmootherResampleGrid2D(Grid2D source, int scaleX, int scaleY) {
		super(source, scaleX, scaleY);
	}

	@Override
	public PolyForm2 polyFormX() {
		return SmootherPolynomial.FORM;
	}

	@Override
	public PolyForm2 polyFormY() {
		return DerivativeSmootherPolynomial.FORM;
	}
}
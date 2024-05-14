package builderb0y.bigglobe.noise;

import builderb0y.bigglobe.noise.polynomials.DerivativeLinearPolynomial;
import builderb0y.bigglobe.noise.polynomials.LinearPolynomial;
import builderb0y.bigglobe.noise.polynomials.Polynomial2.PolyForm2;

public class DxLinearResampleGrid2D extends Resample4Grid2D {

	public DxLinearResampleGrid2D(Grid2D source, int scaleX, int scaleY) {
		super(source, scaleX, scaleY);
	}

	@Override
	public PolyForm2 polyFormX() {
		return DerivativeLinearPolynomial.FORM;
	}

	@Override
	public PolyForm2 polyFormY() {
		return LinearPolynomial.FORM;
	}
}
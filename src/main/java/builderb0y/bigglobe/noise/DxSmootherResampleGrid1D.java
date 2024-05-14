package builderb0y.bigglobe.noise;

import builderb0y.bigglobe.noise.polynomials.Polynomial2.PolyForm2;
import builderb0y.bigglobe.noise.polynomials.SmootherPolynomial;

public class DxSmootherResampleGrid1D extends Resample2Grid1D {

	public DxSmootherResampleGrid1D(Grid1D source, int scaleX) {
		super(source, scaleX);
	}

	@Override
	public PolyForm2 polyForm() {
		return SmootherPolynomial.FORM;
	}
}
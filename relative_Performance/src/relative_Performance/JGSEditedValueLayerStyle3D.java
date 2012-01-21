package relative_Performance;

import java.awt.Color;
import java.awt.Paint;

import repast.simphony.valueLayer.ValueLayer;
import repast.simphony.visualization.editedStyle.DefaultEditedValueLayerStyleData2D;
import repast.simphony.visualization.editedStyle.EditedStyleUtils;
import repast.simphony.visualization.editedStyle.EditedValueLayerStyleData;
import repast.simphony.visualization.visualization3D.style.ValueLayerStyle3D;

public class JGSEditedValueLayerStyle3D implements ValueLayerStyle3D {
	protected ValueLayer layer;
	protected EditedValueLayerStyleData innerStyle;

	public float getY(double... coordinates) {
		float y = innerStyle.getY();

		if (!innerStyle.isYValue())
			return y;

		y = (float) layer.get(coordinates);

		// scaling
		float yMax = innerStyle.getYMax();
		float yMin = innerStyle.getYMin();
		float yScale = innerStyle.getYScale();

		float div = yMax - yMin;

		if (div <= 0)
			div = 1;

		y = ((y - yMin) / div) * yScale;

		return y;
	}

	public void addValueLayer(ValueLayer layer) {
		String userStyleFile = null;

		this.layer = layer;

		switch (((PerformanceSpace) layer).getIndex()) {
		case 0:
			userStyleFile = "Fourier_Space 1.style_JGS.xml";
			break;
		case 1:
			userStyleFile = "Fourier_Space 2.style_JGS.xml";
		}

		innerStyle = EditedStyleUtils.getValueLayerStyle(userStyleFile);
		if (innerStyle == null)
			innerStyle = new DefaultEditedValueLayerStyleData2D();

	}

	public int getRed(double... coordinates) {
		return 0;
	}

	public int getGreen(double... coordinates) {
		return 0;
	}

	public int getBlue(double... coordinates) {
		return 0;
	}

	public Paint getPaint(double... coordinates) {
		/*
		 * Draws a black line at the height of minPerf otherwise the
		 * predetermined color
		 */
		double minPerf = ((PerformanceSpace) layer).getMinPerf();
		double height = layer.get(coordinates);

		if (height < minPerf) {
			return Color.CYAN;
		} else {
			return EditedStyleUtils.getValueLayerColor(innerStyle,
					layer.get(coordinates));
		}

	}

	public float getCellSize() {
		return innerStyle.getCellSize();
	}
}

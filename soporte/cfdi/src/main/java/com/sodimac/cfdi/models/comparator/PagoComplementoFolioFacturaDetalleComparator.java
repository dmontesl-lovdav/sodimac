package com.sodimac.cfdi.models.comparator;

import java.util.Comparator;

import com.sodimac.cfdi.models.PagoComplementoFolioFacturaDetalleModel;

public class PagoComplementoFolioFacturaDetalleComparator implements Comparator<PagoComplementoFolioFacturaDetalleModel>{

	@Override
	public int compare(PagoComplementoFolioFacturaDetalleModel o1, PagoComplementoFolioFacturaDetalleModel o2) {
		if (o1.getIdFactura().intValue() != o2.getIdFactura().intValue() ) {
			return (o1.getIdFactura().intValue() - o2.getIdFactura().intValue());
		}
		return o1.getParcialidad().intValue() - o2.getParcialidad().intValue();
	}

}

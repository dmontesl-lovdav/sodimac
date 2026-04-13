package com.sodimac.rebates.service;

import com.sodimac.rebates.model.ComentarioComparativoAprobacion;

public interface IComentarioComparativoAprobacionService {

	ComentarioComparativoAprobacion getById(Integer id);

	boolean save(ComentarioComparativoAprobacion comentarioComparativoAprobacion);
}

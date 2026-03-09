package com.sodimac.cfdi.repository.fiscal.documento;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.cfdi.entity.fiscal.DocumentoPublicadoEntity;

@Repository
public interface DocumentoPublicadoRepository extends JpaRepository<DocumentoPublicadoEntity, Integer> {

	@Query(value = "{call uspObtenerDocumentosCargados (:pfechaInicial, :pfechafinal, :pIdTipoDocumento)}", nativeQuery = true)	
	public List<Object[]> getDocumentosPublicados(
			  @Param("pfechaInicial") String pfechaInicial
			, @Param("pfechafinal") String pfechafinal
			, @Param("pIdTipoDocumento") Integer pIdTipoDocumento);
	
	public DocumentoPublicadoEntity findByIdDocumentoPublicado(Integer idDocumentoPublicado);

	@Modifying
	@Query(value = "{call uspBorrarArchivo(:pIdDocumentoPublicado)}", nativeQuery = true)	
	public void borrarArchivo( @Param("pIdDocumentoPublicado") Integer pIdDocumentoPublicado);
}

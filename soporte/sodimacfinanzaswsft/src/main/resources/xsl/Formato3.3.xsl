<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet version="1.1"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:cfdi="http://www.sat.gob.mx/cfd/3"
	xmlns:tfd="http://www.sat.gob.mx/TimbreFiscalDigital"
	xmlns:opamfact="http:www.itl-ac.mx"
	xmlns:fo="http://www.w3.org/1999/XSL/Format"
	exclude-result-prefixes="fo">
	
	<xsl:param name="usoCFDIDesc" select="/" />
	<xsl:param name="regimenFiscalDesc" select="/" />
	<xsl:param name="tipoComprobanteDesc" select="/" />
	<xsl:param name="tipoDeComprobanteDesc" select="/" />
	<xsl:param name="transaccion" select="/" />
	<xsl:param name="nombreObra" select="/" />
	<xsl:param name="responsableObra" select="/" />
	<xsl:param name="uuidRelacionado" select="/" />
	<xsl:param name="monedaDesc" select="/" />
    <xsl:param name="importeLetra" select="''"/>
    <xsl:param name="formaPagoLetter" select="''"/>
    <xsl:param name="metodoPagoLetter" select="''"/>
    <xsl:param name="ticketId" select="''" />
	
    <xsl:param name="pacEmail" select="''" />
    <xsl:param name="pacRZ" select="''" />
    <xsl:param name="pacRfc" select="''" />
    <xsl:param name="idPacExternal" select="''" />
    
    <xsl:param name="xsltfilePath" select="''" />
    <xsl:param name="xsltfilePathQR" select="''" />
    <xsl:param name="qrCodeFileName" select="''" />


	<xsl:template match="cfdi:Comprobante">
		<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
			<fo:layout-master-set>
				<fo:simple-page-master master-name="my-page"
					margin-top="1mm" margin-bottom="1cm" margin-left="6.5mm"
					margin-right="1cm">

					<fo:region-body region-name="Content"
						margin-top="7cm" margin-bottom="1cm" margin-left="6.5mm"
						margin-right="1cm" />
					<fo:region-before region-name="Header"
						display-align="after" extent="1cm" />
					<fo:region-after region-name="Footer"
						display-align="before" extent="1cm" precedence="true" />
				</fo:simple-page-master>
				<fo:page-sequence-master
					master-name="default-sequence">
					<fo:repeatable-page-master-reference
						master-reference="my-page" />
				</fo:page-sequence-master>
			</fo:layout-master-set>


			<fo:page-sequence master-reference="my-page">

				<fo:static-content flow-name="Header">
					<fo:block>
						<fo:table>
							<fo:table-column column-width="40.7mm" />
							<fo:table-column column-width="44.7mm" />
							<fo:table-column column-width="49.7mm" />
							<fo:table-column column-width="34.7mm" />
							<fo:table-column />
							<fo:table-body>
								<fo:table-row>
									<fo:table-cell display-align="center">
										<fo:block>
											<fo:external-graphic
												src="url(file:/{$xsltfilePath}/SodimacFactura.png)"
												content-height="130%" content-width="130%" />
										</fo:block>
									</fo:table-cell>

									<fo:table-cell display-align="before"
										text-align="left">
										<fo:block font-size="6pt" font-weight="bold"
											space-before="1mm" space-after="1mm">
											EMISOR
										</fo:block>
										<fo:block font-size="6pt" space-before="1.3mm">
											<xsl:value-of select="./cfdi:Emisor/@Rfc" />
										</fo:block>
										<fo:block font-size="6pt">
											<xsl:value-of select="./cfdi:Emisor/@Nombre" />
										</fo:block>
										<fo:block font-size="6pt" font-weight="bold"
											space-before="10mm">
											Régimen fiscal
										</fo:block>
										<fo:block font-size="6pt" space-before="1.3mm">
											<xsl:value-of select="./cfdi:Emisor/@RegimenFiscal" />
											<xsl:text> </xsl:text>
											<xsl:value-of select="$regimenFiscalDesc" />
										</fo:block>
									</fo:table-cell>

									<fo:table-cell display-align="before"
										text-align="left">
										<fo:block font-size="6pt" font-weight="bold"
											space-before="1mm" space-after="1mm">
											EXPEDIDO EN
										</fo:block>
										<fo:block font-size="6pt" space-before="1.3mm">
											<xsl:value-of select="./cfdi:Emisor/@Rfc" />
										</fo:block>
										<fo:block font-size="6pt">
											<xsl:value-of select="./cfdi:Emisor/@Nombre" />
										</fo:block>
									</fo:table-cell>

									<fo:table-cell display-align="before"
										text-align="left">
										<fo:table>
											<fo:table-column column-width="35mm" />
											<fo:table-column column-width="23mm" />
											<fo:table-body>
												<fo:table-row height="9px">
													<fo:table-cell display-align="center">
														<fo:block font-size="6pt" font-weight="bold">
															TIPO COMPROBANTE
														</fo:block>
													</fo:table-cell>
													<fo:table-cell display-align="center">
														<fo:block font-size="7pt" font-weight="bold">
															<fo:inline color="#FF0000">
																<xsl:value-of select="$tipoComprobanteDesc" />
															</fo:inline>
														</fo:block>
													</fo:table-cell>
												</fo:table-row>

												<fo:table-row height="9px">
													<fo:table-cell display-align="center">
														<fo:block font-size="6pt" font-weight="bold">
															SERIE / FOLIO
														</fo:block>
													</fo:table-cell>
													<fo:table-cell display-align="center">
														<fo:block font-size="6pt">
															<xsl:value-of select="@Serie" />
															-
															<xsl:value-of select="@Folio" />
														</fo:block>
													</fo:table-cell>
												</fo:table-row>

												<fo:table-row height="9px">
													<fo:table-cell display-align="center">
														<fo:block font-size="6pt" font-weight="bold">
															F. EMISIÓN COMPROBANTE
														</fo:block>
													</fo:table-cell>
													<fo:table-cell display-align="center">
														<fo:block font-size="6pt">
															<xsl:value-of select="@Fecha" />
														</fo:block>
													</fo:table-cell>
												</fo:table-row>

												<fo:table-row height="9px">
													<fo:table-cell display-align="center">
														<fo:block font-size="6pt" font-weight="bold">
															TIPO DE COMPROBANTE
														</fo:block>
													</fo:table-cell>
													<fo:table-cell display-align="center">
														<fo:block font-size="6pt">
															<xsl:value-of select="@TipoDeComprobante" />
															- <xsl:value-of select="$tipoDeComprobanteDesc" />
														</fo:block>
													</fo:table-cell>
												</fo:table-row>

												<fo:table-row height="9px">
													<fo:table-cell display-align="center">
														<fo:block font-size="6pt" font-weight="bold">
															VERSIÓN COMPROBANTE
														</fo:block>
													</fo:table-cell>
													<fo:table-cell display-align="center">
														<fo:block font-size="6pt">
															<xsl:value-of select="@Version" />
														</fo:block>
													</fo:table-cell>
												</fo:table-row>

												<fo:table-row height="9px">
													<fo:table-cell display-align="center">
														<fo:block font-size="6pt" font-weight="bold">
															NO. CERTIFICADO CFDI
														</fo:block>
													</fo:table-cell>
													<fo:table-cell display-align="center">
														<fo:block font-size="6pt">
															<xsl:value-of select="@NoCertificado" />
														</fo:block>
													</fo:table-cell>
												</fo:table-row>

												<fo:table-row height="9px">
													<fo:table-cell display-align="center">
														<fo:block font-size="6pt" font-weight="bold">
															NO. CERTIFICADO SAT
														</fo:block>
													</fo:table-cell>
													<fo:table-cell display-align="center">
														<fo:block font-size="6pt">
															<xsl:value-of
																select="./cfdi:Complemento/tfd:TimbreFiscalDigital/@NoCertificadoSAT" />
														</fo:block>
													</fo:table-cell>
												</fo:table-row>

												<fo:table-row height="9px">
													<fo:table-cell display-align="center">
														<fo:block font-size="6pt" font-weight="bold">
															F. CERTIFICACIÓN
														</fo:block>
													</fo:table-cell>
													<fo:table-cell display-align="center">
														<fo:block font-size="6pt">
															<xsl:value-of
																select="./cfdi:Complemento/tfd:TimbreFiscalDigital/@FechaTimbrado" />
														</fo:block>
													</fo:table-cell>
												</fo:table-row>

												<fo:table-row height="9px">
													<fo:table-cell display-align="center">
														<fo:block font-size="6pt" font-weight="bold">
															LUGAR DE EXPEDICIÓN
														</fo:block>
													</fo:table-cell>
													<fo:table-cell display-align="center">
														<fo:block font-size="6pt">
															<xsl:value-of select="@LugarExpedicion" />
														</fo:block>
													</fo:table-cell>
												</fo:table-row>

												<fo:table-row height="9px">
													<fo:table-cell display-align="center">
														<fo:block font-size="6pt" font-weight="bold">
															NO. TRANSACCIÓN
														</fo:block>
													</fo:table-cell>
													<fo:table-cell display-align="center">
														<fo:block font-size="6pt">
															<xsl:value-of select="$transaccion" />
														</fo:block>
													</fo:table-cell>
												</fo:table-row>
											</fo:table-body>
										</fo:table>
									</fo:table-cell>
								</fo:table-row>
							</fo:table-body>
						</fo:table>


						<fo:table>
							<fo:table-column column-width="8mm" />
							<fo:table-column column-width="127mm" />
							<fo:table-column column-width="64mm" />
							<fo:table-body>
								<fo:table-row>
									<fo:table-cell display-align="before">
										<fo:block font-size="6pt" font-weight="bold"
											space-before="1mm" space-after="1mm">

										</fo:block>
									</fo:table-cell>
									<fo:table-cell display-align="before">
										<fo:block font-size="6pt" font-weight="bold"
											space-before="1mm" space-after="1mm">
											RECEPTOR
										</fo:block>
										<fo:block font-size="6pt" space-before="1.3mm">
											<xsl:value-of select="./cfdi:Receptor/@Rfc" />
										</fo:block>
										<fo:block font-size="6pt">
											<xsl:value-of select="./cfdi:Receptor/@Nombre" />
										</fo:block>
										<fo:block font-size="6pt" space-before="7mm">
											<fo:inline font-weight="bold">Uso CFDI: </fo:inline>
											<xsl:value-of select="./cfdi:Receptor/@UsoCFDI" />
											<xsl:text> </xsl:text>
											<xsl:value-of select="$usoCFDIDesc" />
										</fo:block>
									</fo:table-cell>
									<fo:table-cell display-align="center">

										<fo:table>
											<fo:table-body>
												<fo:table-row height="15px">
													<fo:table-cell display-align="center"
														text-align="center">
														<fo:block font-size="6pt" font-weight="bold"></fo:block>
													</fo:table-cell>
												</fo:table-row>
											</fo:table-body>
										</fo:table>

										<fo:table border="1pt solid black">
											<fo:table-body border="inherit">
												<fo:table-row border="inherit" height="10px">
													<fo:table-cell border="inherit"
														background-color="#C0C0C0" display-align="center"
														text-align="center">
														<fo:block font-size="6pt" font-weight="bold">UUID:
														</fo:block>
													</fo:table-cell>
												</fo:table-row>
												<fo:table-row border="inherit" height="10px">
													<fo:table-cell border="inherit"
														display-align="center" text-align="center">
														<fo:block font-size="6pt">
															<xsl:value-of
																select="./cfdi:Complemento/tfd:TimbreFiscalDigital/@UUID" />
														</fo:block>
													</fo:table-cell>
												</fo:table-row>
											</fo:table-body>
										</fo:table>

										<fo:table>
											<fo:table-body>
												<fo:table-row height="10px">
													<fo:table-cell display-align="center"
														width="48px" text-align="right">
														<fo:block font-size="6pt" font-weight="bold">
														</fo:block>
													</fo:table-cell>
													<fo:table-cell display-align="center"
														width="48px" text-align="right">
														<fo:block font-size="6pt" font-weight="bold">
														</fo:block>
													</fo:table-cell>
													<fo:table-cell display-align="center"
														text-align="left">
														<fo:block font-size="6pt">
														</fo:block>
													</fo:table-cell>
												</fo:table-row>
												<fo:table-row height="10px">
													<fo:table-cell display-align="center"
														width="48px" text-align="right">
														<fo:block font-size="6pt" font-weight="bold">
														</fo:block>
													</fo:table-cell>
													<fo:table-cell display-align="center"
														width="48px" text-align="left">
														<fo:block font-size="6pt" font-weight="bold">
															Tipo de Cambio:
														</fo:block>
													</fo:table-cell>
													<fo:table-cell display-align="center"
														text-align="left">
														<fo:block font-size="6pt">
															<xsl:value-of select="format-number(@TipoCambio, '##.00')" />
														</fo:block>
													</fo:table-cell>
												</fo:table-row>
												<fo:table-row height="10px">
													<fo:table-cell display-align="center"
														width="48px" text-align="right">
														<fo:block font-size="6pt" font-weight="bold">
														</fo:block>
													</fo:table-cell>
													<fo:table-cell display-align="center"
														width="48px" text-align="left">
														<fo:block font-size="6pt" font-weight="bold">
															Moneda:
														</fo:block>
													</fo:table-cell>
													<fo:table-cell display-align="center"
														text-align="left">
														<fo:block font-size="6pt">
															<xsl:value-of select="@Moneda" />
															- <xsl:value-of select="$monedaDesc" />
														</fo:block>
													</fo:table-cell>
												</fo:table-row>
											</fo:table-body>
										</fo:table>

									</fo:table-cell>
								</fo:table-row>
							</fo:table-body>
						</fo:table>


					</fo:block>
				</fo:static-content>

                <fo:static-content flow-name="Footer"  >
                    <!-- BLOQUE 8 -->
                     
                    <fo:block space-before="5mm" >
                        <fo:table>
                            <fo:table-column/>
                            <fo:table-column/>
                            <fo:table-body>
                                <fo:table-row height="12px">
                                    <fo:table-cell display-align="center" number-columns-spanned="2" >
                                    	<fo:block border-bottom-width="2pt" border-bottom-style="solid"></fo:block>        
                                        <fo:block font-size="6pt" text-align="center" space-before=".5mm" space-after=".5mm" >
                                            ESTE DOCUMENTO ES UNA REPRESENTACIÓN IMPRESA DE UN CFDI 3.3 <fo:basic-link external-destination="url('https://www.sodimac.com.mx/')"
                                                                                                                   text-decoration="underline"
                                                                                                                   color="blue">http://www.sodimac.com.mx/</fo:basic-link>
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>
                                <fo:table-row height="6px">
                                    <fo:table-cell display-align="center">
                                        <fo:block font-size="6pt" text-align="left" space-before=".5mm" space-after=".5mm">
                                            RFC Prov. Cert.: <xsl:value-of select="./cfdi:Complemento/tfd:TimbreFiscalDigital/@RfcProvCertif"/>
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>
                                <fo:table-row height="6px">
                                    <fo:table-cell display-align="center">
                                        <fo:block font-size="6pt" text-align="left" space-before=".5mm" space-after=".5mm">
                                            CFDI emitido por <xsl:value-of select="$pacRZ" />
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>
                                <fo:table-row height="6px">
                                    <fo:table-cell display-align="center">
                                        <fo:block font-size="6pt" text-align="left" space-before=".5mm" space-after=".5mm">
                                            Proveedor Autorizado de Certificacíon (PAC) <xsl:value-of select="$idPacExternal" />
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>
                                <fo:table-row height="6px">
                                    <fo:table-cell display-align="center">
                                        <fo:block font-size="6pt" text-align="left" space-before=".5mm" space-after=".5mm">
                                            <xsl:value-of select="$pacEmail" />
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>
                            </fo:table-body>
                        </fo:table>
                    </fo:block>
                    <!--fin bloque 8-->
                </fo:static-content>
                
                
                <fo:flow flow-name="Content">
                    
                    <!--BLOQUE Detalle--> 
                    <fo:block space-before="2mm">
	                    <fo:table>
                                <fo:table-column column-width="12mm"/>
                                <fo:table-column column-width="12mm" />
                                <fo:table-column column-width="10mm" />
                                <fo:table-column column-width="10mm" />
                                <fo:table-column column-width="10mm" />
                                <fo:table-column />
                                <fo:table-column />
                                <fo:table-column />
                                <fo:table-column />
                                <fo:table-column />
                                <fo:table-column />
                                <fo:table-column column-width="20mm"/>
                                <fo:table-column column-width="15mm"/>
                                <fo:table-column column-width="15mm" />
                                
                                <fo:table-header>
                                    <fo:table-row height="12px">
                                                                             
                                        <fo:table-cell height="7px"  display-align="center" text-align="left"  font-weight="bold" number-columns-spanned="1"  >
                                            <fo:block  font-size="5pt"  space-before="1mm" space-after="1mm">
                                                Cve. Prod. Servicio
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell height="7px"  display-align="center" text-align="left"  font-weight="bold"  number-columns-spanned="1" >
                                            <fo:block font-size="5pt"  space-before="1mm" space-after="1mm">
                                                No. Identificación
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell height="7px" display-align="center" text-align="left"  font-weight="bold"  number-columns-spanned="1" >
                                            <fo:block font-size="5pt"  space-before="1mm" space-after="1mm">
                                                Cantidad
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell height="7px"  display-align="center" text-align="left"  font-weight="bold"  number-columns-spanned="1" >
                                            <fo:block font-size="5pt"  space-before="1mm" space-after="1mm">
                                                Unidad
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell height="7px"  display-align="center" text-align="left"  font-weight="bold"  number-columns-spanned="1" >
                                            <fo:block font-size="5pt"  space-before="1mm" space-after="1mm">
                                                Clave Unidad
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell height="7px"  display-align="center" text-align="left"  font-weight="bold"  number-columns-spanned="6" >
                                            <fo:block font-size="5pt"  space-before="1mm" space-after="1mm">
                                                Descripción
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell height="7px"  display-align="center" text-align="right"  font-weight="bold"  number-columns-spanned="1" >
                                            <fo:block font-size="5pt"  space-before="1mm" space-after="1mm">
                                               Valor Unitario
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell height="7px"  display-align="center" text-align="right"  font-weight="bold"  number-columns-spanned="1" >
                                            <fo:block font-size="5pt"  space-before="1mm" space-after="1mm">
                                                Descuento
                                            </fo:block>
                                        </fo:table-cell>
										<fo:table-cell height="7px"  display-align="center" text-align="right"  font-weight="bold"  number-columns-spanned="1" >
                                            <fo:block font-size="5pt"  space-before="1mm" space-after="1mm">
                                                Importe
                                            </fo:block>
                                        </fo:table-cell>                                        
                                    </fo:table-row>
                                    <fo:table-row>
                                    	<fo:table-cell height="1px"   font-weight="bold"  number-columns-spanned="14" >
                                    		<fo:block border-bottom-width="1pt" border-bottom-style="solid"></fo:block>
                                    	</fo:table-cell>  
                                     </fo:table-row>
                          
                                </fo:table-header>
                              <fo:table-body>

  								<xsl:for-each select="./cfdi:Conceptos/cfdi:Concepto" >
  								
									<fo:table-row>
	                                 	<fo:table-cell height="2px"   font-weight="bold"  number-columns-spanned="14" >
	                                 		<fo:block  ></fo:block>
	                                 	</fo:table-cell>  
	                                </fo:table-row>		                           	 
  								
	                                <fo:table-row>
							             <fo:table-cell  number-columns-spanned="1">
							                <fo:block font-size="6pt"  space-before="1mm" space-after="1mm">
                                                    <xsl:value-of select="@ClaveProdServ" />
                                            </fo:block>
							             </fo:table-cell>
							          	  <fo:table-cell number-columns-spanned="1">
							                <fo:block font-size="6pt"  space-before="1mm" space-after="1mm" >
                                                    <xsl:value-of select="@NoIdentificacion" />
                                            </fo:block>
							             </fo:table-cell>
							          	  <fo:table-cell number-columns-spanned="1">
							                <fo:block font-size="6pt"  space-before="1mm" space-after="1mm" >
                                            	<xsl:choose>
							                		<xsl:when test="not(number(@Cantidad))">
				            							<xsl:value-of select="@Cantidad"/>
				        							</xsl:when>
											        <xsl:when test="contains(@Cantidad, '.')">
											            <xsl:value-of select="concat ( substring-before( @Cantidad, '.' ) , '.' , substring ( substring-after ( @Cantidad, '.' ) , 1 , 2 ) )"/>
											        </xsl:when>
											        <xsl:otherwise>
											            <xsl:value-of select="format-number(@Cantidad, '#.##')"/>
											        </xsl:otherwise>
							    				</xsl:choose>
                                            </fo:block>
							             </fo:table-cell>
							          	  <fo:table-cell number-columns-spanned="1">
							                <fo:block font-size="6pt"  space-before="1mm" space-after="1mm" >
                                            	<xsl:choose>
							                		<xsl:when test="not(number(@Unidad))">
				            							<xsl:value-of select="@Unidad"/>
				        							</xsl:when>
											        <xsl:when test="contains(@Unidad, '.')">
											            <xsl:value-of select="concat ( substring-before( @Unidad, '.' ) , '.' , substring ( substring-after ( @Unidad, '.' ) , 1 , 2 ) )"/>
											        </xsl:when>
											        <xsl:otherwise>
											            <xsl:value-of select="format-number(@Unidad, '#.##')"/>
											        </xsl:otherwise>
							    				</xsl:choose>
                                            </fo:block>
							             </fo:table-cell>
							           	<fo:table-cell number-columns-spanned="1">
							                <fo:block font-size="6pt"  space-before="1mm" space-after="1mm" >
                                                    <xsl:value-of select="@ClaveUnidad" />
                                            </fo:block>
							             </fo:table-cell>
							           	<fo:table-cell number-columns-spanned="6">
							                <fo:block font-size="6pt"  space-before="1mm" space-after="1mm"  >
                                                    <xsl:value-of select="@Descripcion" />
                                            </fo:block>
							             </fo:table-cell>
							           	<fo:table-cell display-align="after" text-align="right">
							                <fo:block font-size="6pt"  space-before="1mm" space-after="1mm" >
                                                    $<xsl:value-of select="format-number(@ValorUnitario, '#,###.00')" />
                                            </fo:block>
							             </fo:table-cell>
							           	<fo:table-cell display-align="after" text-align="right">
							                <fo:block font-size="6pt"  space-before="1mm" space-after="1mm" >
                                                    $<xsl:value-of select="format-number(@Descuento, '#,##0.00')" />
                                            </fo:block>
							             </fo:table-cell>
							           	<fo:table-cell display-align="after" text-align="right">
							                <fo:block font-size="6pt"  space-before="1mm" space-after="1mm" >
                                                    $<xsl:value-of select="format-number(@Importe, '#,###.00')" />
                                            </fo:block>
							             </fo:table-cell>							             							             							             							             							             							             
	                            	</fo:table-row>
		                           	 <fo:table-row>
		                          		<fo:table-cell display-align="after" text-align="right" number-columns-spanned="5">
							                <fo:block font-size="6pt"  space-before="1mm" space-after="1mm">
                                                  
                                            </fo:block>
							             </fo:table-cell>
							             <fo:table-cell display-align="after" text-align="left" number-columns-spanned="1" font-weight="bold">
							                <fo:block font-size="5pt"  space-before="1mm" space-after="1mm">
                                                  Tipo
                                            </fo:block>
							             </fo:table-cell>
							             <fo:table-cell display-align="after" text-align="left" number-columns-spanned="1" font-weight="bold">
							                <fo:block font-size="5pt"  space-before="1mm" space-after="1mm">
                                                  Impuesto
                                            </fo:block>
							             </fo:table-cell>
							             <fo:table-cell display-align="after" text-align="left" number-columns-spanned="1" font-weight="bold">
							                <fo:block font-size="5pt"  space-before="1mm" space-after="1mm">
                                                  Base
                                            </fo:block>
							             </fo:table-cell>
							             <fo:table-cell display-align="after" text-align="left" number-columns-spanned="1" font-weight="bold">
							                <fo:block font-size="5pt"  space-before="1mm" space-after="1mm">
                                                  Tipo Factor
                                            </fo:block>
							             </fo:table-cell>
							             <fo:table-cell display-align="after" text-align="left" number-columns-spanned="1" font-weight="bold">
							                <fo:block font-size="5pt"  space-before="1mm" space-after="1mm">
                                                  Tasa o C.
                                            </fo:block>
							             </fo:table-cell>
							             <fo:table-cell display-align="after" text-align="right" number-columns-spanned="1" font-weight="bold">
							                <fo:block font-size="5pt"  space-before="1mm" space-after="1mm">
                                                  Importe
                                            </fo:block>
							             </fo:table-cell>							             							             							             
		                           	 </fo:table-row>
		                           	 <xsl:for-each select="./cfdi:Impuestos/cfdi:Traslados/cfdi:Traslado">
			                           	 <fo:table-row>
			                          		<fo:table-cell display-align="after" text-align="right" number-columns-spanned="5">
								                <fo:block font-size="5pt"  space-before="1mm" space-after="1mm">
	                                                  
	                                            </fo:block>
								             </fo:table-cell>
								             <fo:table-cell display-align="after" text-align="left" number-columns-spanned="1" >
								                <fo:block font-size="5pt"  space-before="1mm" space-after="1mm">
	                                                 <xsl:choose> 
							                           <xsl:when test="@Impuesto = '001'"> 
							                            
							                           </xsl:when> 	
							                           <xsl:otherwise> 
							                             Traslado
							                           </xsl:otherwise> 
							                        </xsl:choose>
	                                            </fo:block>
								             </fo:table-cell>
								             <fo:table-cell display-align="after" text-align="left" number-columns-spanned="1" >
								                <fo:block font-size="5pt"  space-before="1mm" space-after="1mm">
	                                                   <xsl:value-of select="@Impuesto" /> -&#160;
	                                               	 <xsl:choose> 
							                           <xsl:when test="@Impuesto = '001'"> 
							                              ISR
							                           </xsl:when>
							                           <xsl:when test="@Impuesto = '002'"> 
							                              IVA
							                           </xsl:when>  	
							                           <xsl:otherwise> 
							                             IEPS
							                           </xsl:otherwise> 
							                        </xsl:choose>
	                                            </fo:block>
								             </fo:table-cell>
								             <fo:table-cell display-align="after" text-align="left" number-columns-spanned="1" >
								                <fo:block font-size="5pt"  space-before="1mm" space-after="1mm">
	                                                  $<xsl:value-of select="format-number(@Base, '#,###.00')" />
	                                            </fo:block>
								             </fo:table-cell>
								             <fo:table-cell display-align="after" text-align="left" number-columns-spanned="1" >
								                <fo:block font-size="5pt"  space-before="1mm" space-after="1mm">
	                                                  <xsl:value-of select="@TipoFactor" />
	                                            </fo:block>
								             </fo:table-cell>
								             <fo:table-cell display-align="after" text-align="left" number-columns-spanned="1" >
								                <fo:block font-size="5pt"  space-before="1mm" space-after="1mm">
	                                                   <xsl:value-of select="@TasaOCuota" />
	                                            </fo:block>
								             </fo:table-cell>
								             <fo:table-cell display-align="after" text-align="right" number-columns-spanned="1" >
								                <fo:block font-size="5pt"  space-before="1mm" space-after="1mm">
	                                                  $<xsl:value-of select="format-number(@Importe, '#,###.00')" />
	                                            </fo:block>
								             </fo:table-cell>							             							             							             
			                           	 </fo:table-row>
		                           	 </xsl:for-each>
		                           	 
									<fo:table-row>
	                                 	<fo:table-cell height="2px"   font-weight="bold"  number-columns-spanned="14" >
	                                 		<fo:block  ></fo:block>
	                                 	</fo:table-cell>  
	                                </fo:table-row>		                           	 
		                           	 
                       			 </xsl:for-each>
                    			   <fo:table-row>
                                 	<fo:table-cell height="2px"   font-weight="bold"  number-columns-spanned="14" >
                                 		<fo:block border-top-width="1pt" border-top-style="solid"></fo:block>
                                 	</fo:table-cell>  
                                  </fo:table-row>
                                  
                                  <fo:table-row>
                                 	<fo:table-cell height="2px"     number-columns-spanned="11" >
                                 		<fo:block font-size="5pt"  space-before="1mm" space-after="1mm" >
                                 					<fo:table>
							                			<fo:table-column column-width="25mm"/>
							                			<fo:table-column/>
							                			<fo:table-body>
				                                          		<fo:table-row>
								                				    <fo:table-cell font-weight="bold" display-align="before" space-before="2mm" >
								                                        <fo:block  font-size="5pt" space-before="2mm" space-after="2mm">
								                                            Importe letra:
								                                        </fo:block>
								                                    </fo:table-cell>
								                                    <fo:table-cell  display-align="before" space-before="2mm" >
								                                        <fo:block  font-size="5pt" text-align="left" space-before="2mm" space-after="2mm">
								                                            <xsl:value-of select="$importeLetra" />
								                                        </fo:block>
								                                    </fo:table-cell>
								                				</fo:table-row>
								                				<fo:table-row>
								                				    <fo:table-cell font-weight="bold" display-align="before" space-before="2mm" >
								                                        <fo:block  font-size="5pt" text-align="left" space-before="2mm" space-after="2mm">
								                                            Forma de Pago:
								                                        </fo:block>
								                                    </fo:table-cell>
								                                    <fo:table-cell  display-align="before" space-before="2mm" >
								                                        <fo:block  font-size="5pt" text-align="left" space-before="2mm" space-after="2mm">
								                                            <xsl:value-of select="@FormaPago" /> &#160; <xsl:value-of select="$formaPagoLetter" />
								                                        </fo:block>
								                                    </fo:table-cell>
								                				</fo:table-row>
								                				<fo:table-row>
								                				    <fo:table-cell font-weight="bold" display-align="before" space-before="2mm" >
								                                        <fo:block  font-size="5pt" text-align="left" space-before="2mm" space-after="2mm">
								                                            Condiciones de Pago:
								                                        </fo:block>
								                                    </fo:table-cell>
								                                    <fo:table-cell  display-align="before" space-before="2mm" >
								                                        <fo:block  font-size="5pt" text-align="left" space-before="2mm" space-after="2mm">
								                                           <xsl:value-of select="@CondicionesDePago" />
								                                        </fo:block>
								                                    </fo:table-cell>
								                				</fo:table-row>
								                				  <fo:table-row>
								                				    <fo:table-cell font-weight="bold" display-align="before" space-before="2mm" >
								                                        <fo:block  font-size="5pt" text-align="left" space-before="2mm" space-after="2mm">
								                                            Método de Pago:
								                                        </fo:block>
								                                    </fo:table-cell>
								                                    <fo:table-cell  display-align="before" space-before="2mm" >
								                                        <fo:block  font-size="5pt" text-align="left" space-before="2mm" space-after="2mm">
								                                           <xsl:value-of select="@MetodoPago" /> &#160; <xsl:value-of select="$metodoPagoLetter" />
								                                        </fo:block>
								                                    </fo:table-cell>
								                				</fo:table-row>
							                			</fo:table-body>
							                		</fo:table>
                                 		</fo:block>
                                 	</fo:table-cell>
                                 	  <fo:table-cell height="2px"   font-weight="bold"  number-columns-spanned="3" >
                                 		<fo:block font-size="5pt"  space-before="1mm" space-after="1mm" >
                                 				<fo:table>
							                			<fo:table-column column-width="20mm"/>
							                			<fo:table-column/>
							                			<fo:table-body>
				                                          		<fo:table-row>
								                				    <fo:table-cell font-weight="bold" display-align="before" space-before="2mm" >
								                                        <fo:block  font-size="5pt" space-before="2mm" space-after="2mm">
								                                            Subtotal:
								                                        </fo:block>
								                                    </fo:table-cell>
								                                    <fo:table-cell  display-align="after" space-before="2mm" >
								                                        <fo:block  font-size="6pt" text-align="right" space-before="2mm" space-after="2mm">
								                                            $<xsl:value-of select="format-number(@SubTotal, '#,###.00')" />
								                                        </fo:block>
								                                    </fo:table-cell>
								                				</fo:table-row>
								                				<fo:table-row>
								                				    <fo:table-cell font-weight="bold" display-align="before" space-before="2mm" >
								                                        <fo:block  font-size="5pt" text-align="left" space-before="2mm" space-after="2mm">
								                                            Descuento:
								                                        </fo:block>
								                                    </fo:table-cell>
								                                    <fo:table-cell  display-align="after" space-before="2mm" >
								                                        <fo:block  font-size="6pt" text-align="right" space-before="2mm" space-after="2mm">
								                                            $<xsl:value-of select="format-number(@Descuento, '#,##0.00')" />
								                                        </fo:block>
								                                    </fo:table-cell>
								                				</fo:table-row>
								                			<xsl:for-each select="./cfdi:Impuestos/cfdi:Traslados/cfdi:Traslado" >
								                				<xsl:if test="@Importe != '0.00'">
					                                          		<fo:table-row>
									                				    <fo:table-cell display-align="before" space-before="2mm" >
									                                        <fo:block  font-size="5pt" space-before="2mm" space-after="2mm">
									                                        	<xsl:if test="@Impuesto = '001'">
									                                        	ISR <xsl:value-of select="format-number(format-number(@TasaOCuota,'##.00') * 100,'##')"/>%
									                                        	</xsl:if>
									                                       		<xsl:if test="@Impuesto = '002'">
									                                        	IVA <xsl:value-of select="format-number(format-number(@TasaOCuota,'##.00') * 100,'##')"/>% Trasladado
									                                        	
									                                        	</xsl:if>
									                                        	<xsl:if test="@Impuesto = '003'">
									                                        	IEPS <xsl:value-of select="format-number(format-number(@TasaOCuota,'##.00') * 100,'##')"/>% Trasladado
									                                        	</xsl:if> 
									                                            
									                                        </fo:block>
									                                    </fo:table-cell>
										                                <fo:table-cell display-align="after" >
									                                        <fo:block  font-size="6pt" text-align="right" space-before="1mm" space-after="1mm">
									                                            $<xsl:value-of select="format-number(@Importe, '#,###.00')" />
									                                        </fo:block>
								                                    	</fo:table-cell>
									                				</fo:table-row>
								                				</xsl:if> 
							                				 </xsl:for-each>
								                				  <fo:table-row>
								                				    <fo:table-cell font-weight="bold" display-align="before" space-before="2mm" >
								                                        <fo:block  font-size="5pt" text-align="left" space-before="2mm" space-after="2mm">
								                                            Total:
								                                        </fo:block>
								                                    </fo:table-cell>
								                                    <fo:table-cell  display-align="after" space-before="2mm" >
								                                        <fo:block  font-size="6pt" text-align="right" space-before="2mm" space-after="2mm">
								                                            $<xsl:value-of select="format-number(@Total, '#,###.00')" />
								                                        </fo:block>
								                                    </fo:table-cell>
								                				</fo:table-row>
							                			</fo:table-body>
							                		</fo:table>
                                 		</fo:block>
                                 	</fo:table-cell>  
                                  </fo:table-row>
                                  
                            </fo:table-body>
	                    </fo:table>    
                    </fo:block> 
                    <!--FIN BLOQUE Detalle-->
                    
                    <!--BLOQUE Ticket--> 
	                    <fo:block font-size="6pt" space-before="5mm" >
	                    	 <xsl:value-of select="$ticketId" />
	                    </fo:block>
                    <!--FIN BLOQUE Ticket--> 
                    
                    <!--BLOQUE Nombre de la Obra--> 
                    <fo:block space-before="3mm" >
	                    <fo:table>
	                    	<fo:table-column column-width="150.0mm"/>
                            <fo:table-column/>
                            <fo:table-body>
                                <fo:table-row>
                                   <fo:table-cell display-align="center"   >
                                        <fo:block font-weight="bold" font-size="6pt" space-before="5mm" space-after=".5mm">
                                            Nombre de la Obra:
                                        </fo:block>
                                        <fo:block font-size="6pt" space-before=".5mm" space-after="3mm">
                                            <xsl:value-of select="$nombreObra"/>
                                        </fo:block>
                                    </fo:table-cell>
                            	</fo:table-row>
                            	<fo:table-row>
                                   <fo:table-cell display-align="center"   >
                                        <fo:block font-weight="bold" font-size="6pt" space-before="5mm" space-after=".5mm">
                                            Responsable de la Obra:
                                        </fo:block>
                                        <fo:block font-size="6pt" space-before=".5mm" space-after="3mm">
                                            <xsl:value-of select="$responsableObra"/>
                                        </fo:block>
                                    </fo:table-cell>
                            	</fo:table-row>
                            </fo:table-body>
	                    </fo:table>
                    </fo:block>
                    <!-- FIN BLOQUE Nombre de la Obra-->

                    <!--BLOQUE Folio fiscal relacionado--> 
                    <fo:block space-before="3mm" >
	                    <fo:table>
	                    	<fo:table-column column-width="150.0mm"/>
                            <fo:table-column/>
                            <fo:table-body>
                                <fo:table-row>
                                   <fo:table-cell display-align="center"   >
                                        <fo:block font-weight="bold" font-size="6pt" space-before="5mm" space-after=".5mm">
                                            Folio Fiscal a Relacionar:
                                        </fo:block>
                                        <fo:block font-size="6pt" space-before=".5mm" space-after="3mm">
                                            <xsl:value-of select="$uuidRelacionado"/>
                                        </fo:block>
                                    </fo:table-cell>
                            	</fo:table-row>
                            </fo:table-body>
	                    </fo:table>
                    </fo:block>
                    <!-- FIN BLOQUE Folio fiscal relacionado-->

                       
                    <!--BLOQUE 6-->
                    <fo:block space-before="2mm" >
                        <fo:table>
                            <fo:table-column column-width="30.5mm"/>
                            <fo:table-column column-width="5mm"/>
                            <fo:table-column/>
                            <fo:table-column/>
                            <fo:table-column/>
                            <fo:table-column/>
                            <fo:table-column/>
                            <fo:table-column/>
                            <fo:table-column/>
                            
                            <fo:table-body>
                                <fo:table-row>
                                    <fo:table-cell display-align="before"  >
                                        <fo:block font-size="0" line-height="0">
                                            <!-- aquiva el codigomatricial -->
<!--                                              <fo:external-graphic src="url(file:/{$xsltfilePath}/{$qrCodeFileName})" alignment-baseline="auto" content-height="37mm" content-width="37mm" scaling="uniform" /> -->
											 <fo:external-graphic src="url(file:/{$xsltfilePathQR}/{$qrCodeFileName})" alignment-baseline="auto" content-height="37mm" content-width="37mm" scaling="uniform" />
                                        </fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell display-align="center" number-columns-spanned="1">
                                         <fo:block font-size="0" line-height="0">

                                        </fo:block>
                                    </fo:table-cell>
                                                                   
                                    <fo:table-cell display-align="center" number-columns-spanned="7">
                                        <!--aqui van datos adicionales-->
                                        <fo:table>
                                            <fo:table-column/>
                                            
                                            <fo:table-body>
                                               
                                                <fo:table-row height="12px" >                                             
				                                    <fo:table-cell display-align="center"   >
				                                        <fo:block font-weight="bold" font-size="6pt" space-before="5mm" space-after=".5mm">
				                                            Sello Digital del CFDI
				                                        </fo:block>
				                                        <fo:block font-size="5pt" space-before=".5mm" space-after="3mm" >
				                                            <xsl:value-of select="./cfdi:Complemento/tfd:TimbreFiscalDigital/@SelloCFD"/>
				                                        </fo:block>
				                                    </fo:table-cell>
				                                
                                                </fo:table-row>
                                                <fo:table-row height="6px">
                                                	<fo:table-cell display-align="center" >
                                               			<fo:block font-weight="bold" font-size="3pt" space-before="2.5mm" space-after=".5mm">
				                                           
				                                        </fo:block>
				                                    </fo:table-cell>
                                                </fo:table-row>
                                                <fo:table-row height="12px">
			                                        <fo:table-cell display-align="center" >
				                                        <fo:block font-weight="bold" font-size="6pt" space-before="5mm" space-after=".5mm">
				                                            Sello Digital del SAT
				                                        </fo:block>
				                                        <fo:block  font-size="5pt" space-before=".5mm" space-after="3mm">
				                                            <xsl:value-of select="./cfdi:Complemento/tfd:TimbreFiscalDigital/@SelloSAT"/>
				                                        </fo:block>
			                                    	</fo:table-cell>
                                                </fo:table-row>
                                                <fo:table-row height="6px">
                                                	<fo:table-cell display-align="center" >
                                               			<fo:block font-weight="bold" font-size="3pt" space-before="2.5mm" space-after=".5mm">
				                                           
				                                        </fo:block>
				                                    </fo:table-cell>
                                                </fo:table-row>
                                                <fo:table-row height="12px" >
				                                    <fo:table-cell display-align="center" >
				                                        <fo:block font-weight="bold" font-size="6pt" space-before="5mm" space-after=".5mm">
				                                            Cadena original del complemento de certificación digital del SAT
				                                        </fo:block>
				                                        <fo:block font-size="5pt" space-before=".5mm" space-after=".5mm">
				                                            ||<xsl:value-of select="./cfdi:Complemento/tfd:TimbreFiscalDigital/@Version"/>|<xsl:value-of select="./cfdi:Complemento/tfd:TimbreFiscalDigital/@UUID"/>|<xsl:value-of select="./cfdi:Complemento/tfd:TimbreFiscalDigital/@FechaTimbrado"/>|<xsl:value-of select="./cfdi:Complemento/tfd:TimbreFiscalDigital/@SelloSAT"/>|<xsl:value-of select="./cfdi:Complemento/tfd:TimbreFiscalDigital/@NoCertificadoSAT"/>||
				                                        </fo:block>
				                                    </fo:table-cell>
                                                </fo:table-row>
                                                            
                                            </fo:table-body>
                                        </fo:table>
                                    </fo:table-cell>
                                </fo:table-row>             
                            </fo:table-body>
                        </fo:table>    
                    </fo:block>
                    <!--FIN BLOQUE 6-->
                    <fo:block></fo:block>
   
                </fo:flow>
                
			</fo:page-sequence>

		</fo:root>
	</xsl:template>
</xsl:stylesheet>

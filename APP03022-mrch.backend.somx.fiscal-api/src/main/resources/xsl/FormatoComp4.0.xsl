<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:cfdi="http://www.sat.gob.mx/cfd/4"
	xmlns:tfd="http://www.sat.gob.mx/TimbreFiscalDigital"
	xmlns:pago20="http://www.sat.gob.mx/Pagos20"
	xmlns:opamfact="http:www.itl-ac.mx"
	xmlns:xalan="http://xml.apache.org/xslt"
    xmlns:util="com.sodimac.facturacion.util.UtilsXsl"
	xmlns:fo="http://www.w3.org/1999/XSL/Format"
	exclude-result-prefixes="fo">
	
	<xsl:param name="usoCFDIDesc" select="/" />
	<xsl:param name="regimenFiscalDesc" select="/" />
	<xsl:param name="regimenFiscalReceptorDesc" select="/" />
	<xsl:param name="tipoComprobanteDesc" select="/" />
	<xsl:param name="tipoDeComprobanteDesc" select="/" />
	<xsl:param name="transaccion" select="/" />
	<xsl:param name="nombreObra" select="/" />
	<xsl:param name="responsableObra" select="/" />
	<xsl:param name="uuidRelacionado" select="/" />
	<xsl:param name="moneda" select="/" />
	<xsl:param name="monedaDesc" select="/" />
    <xsl:param name="importeLetra" select="''"/>
    <xsl:param name="formaPagoLetter" select="''"/>
    <xsl:param name="metodoPagoLetter" select="''"/>
    <xsl:param name="ticketId" select="''" />
    <xsl:param name="periodicidad" select="''" />
    <xsl:param name="meses" select="''" />
    <xsl:param name="anio" select="''" />
	
    <xsl:param name="pacEmail" select="''" />
    <xsl:param name="pacRZ" select="''" />
    <xsl:param name="pacRfc" select="''" />
    <xsl:param name="idPacExternal" select="''" />
    
    <xsl:param name="xsltfilePath" select="''" />
	<xsl:param name="logoFileName" select="'no_logo.jpg'"/>
    <xsl:param name="xsltfilePathQR" select="''" />
    <xsl:param name="qrCodeFileName" select="''" />
    <xsl:param name="sFormaPagoComplemento" select="''"/>
    <xsl:param name="sFormaPagoComplementoDesc" select="''"/>
    
    <xsl:param name="impSaldoAntTotal" select="''"/>
    <xsl:param name="impPagadoTotal" select="''"/>
    <xsl:param name="impSaldoInsolutoTotal" select="''"/>

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
						display-align="after" extent="2cm" />
						
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
							<fo:table-column column-width="94.4mm" />
							<fo:table-column column-width="34.7mm" />
							
							<fo:table-body>
								<fo:table-row>
									<fo:table-cell display-align="center">
										<fo:block>
											<fo:external-graphic
												src="url({$xsltfilePath}{$logoFileName)})"
												content-height="100%" content-width="100%" />
										</fo:block>
									</fo:table-cell>
									
									<fo:table-cell display-align="before" text-align="left">
										<fo:table>
											<fo:table-column column-width="24.4mm" />
											<fo:table-column column-width="70mm" />
											<fo:table-body>
												<fo:table-row height="9px">
													<fo:table-cell number-columns-spanned="2">
														<fo:block font-size="6pt" font-weight="bold" space-before="1mm" space-after="1mm">
															Emisor
														</fo:block>
													</fo:table-cell>
												</fo:table-row>
												<fo:table-row height="9px">
													<fo:table-cell>
														<fo:block font-size="6pt" font-weight="bold">
															RFC:
														</fo:block>
													</fo:table-cell>
													<fo:table-cell>
														<fo:block font-size="6pt">
															<xsl:value-of select="./cfdi:Emisor/@Rfc" />
														</fo:block>
													</fo:table-cell>
												</fo:table-row>
												<fo:table-row height="9px">
													<fo:table-cell>
														<fo:block font-size="6pt" font-weight="bold">
															Razón Social:
														</fo:block>
													</fo:table-cell>
													<fo:table-cell>
														<fo:block font-size="6pt">
															<xsl:value-of select="./cfdi:Emisor/@Nombre" />
														</fo:block>
													</fo:table-cell>
												</fo:table-row>
												<fo:table-row height="9px">
													<fo:table-cell>
														<fo:block font-size="6pt" font-weight="bold">
															Régimen Fiscal:
														</fo:block>
													</fo:table-cell>
													<fo:table-cell>
														<fo:block font-size="6pt">
															<xsl:value-of select="./cfdi:Emisor/@RegimenFiscal" />
															<xsl:text> </xsl:text>
															<xsl:value-of select="$regimenFiscalDesc" />
														</fo:block>
													</fo:table-cell>
												</fo:table-row>
												<fo:table-row height="15px">
													<fo:table-cell number-columns-spanned="2">
														<fo:block />
													</fo:table-cell>
												</fo:table-row>
											</fo:table-body>
										</fo:table>
										
										<fo:table>
											<fo:table-column column-width="24.4mm" />
											<fo:table-column column-width="70mm" />
											<fo:table-body>
												<fo:table-row height="9px">
													<fo:table-cell number-columns-spanned="2">
														<fo:block font-size="6pt" font-weight="bold" space-before="2mm" space-after="2mm">
															Receptor
														</fo:block>
													</fo:table-cell>
												</fo:table-row>
												<fo:table-row height="9px">
													<fo:table-cell>
														<fo:block font-size="6pt" font-weight="bold">
															RFC:
														</fo:block>
													</fo:table-cell>
													<fo:table-cell>
														<fo:block font-size="6pt">
															<xsl:value-of select="./cfdi:Receptor/@Rfc" />
														</fo:block>
													</fo:table-cell>
												</fo:table-row>
												<fo:table-row height="9px">
													<fo:table-cell>
														<fo:block font-size="6pt" font-weight="bold">
															Razón Social:
														</fo:block>
													</fo:table-cell>
													<fo:table-cell>
														<fo:block font-size="6pt">
															<xsl:value-of select="./cfdi:Receptor/@Nombre" />
														</fo:block>
													</fo:table-cell>
												</fo:table-row>
												<fo:table-row height="9px">
													<fo:table-cell>
														<fo:block font-size="6pt" font-weight="bold">
															Régimen Fiscal:
														</fo:block>
													</fo:table-cell>
													<fo:table-cell>
														<fo:block font-size="6pt">
															<xsl:value-of select="./cfdi:Receptor/@RegimenFiscalReceptor" />
															<xsl:text> </xsl:text>
															<xsl:value-of select="$regimenFiscalReceptorDesc" />
														</fo:block>
													</fo:table-cell>
												</fo:table-row>
												<fo:table-row height="9px">
													<fo:table-cell>
														<fo:block font-size="6pt" font-weight="bold">
															Uso CFDI:
														</fo:block>
													</fo:table-cell>
													<fo:table-cell>
														<fo:block font-size="6pt">
															<xsl:value-of select="./cfdi:Receptor/@UsoCFDI" />
															<xsl:text> </xsl:text>
															<xsl:value-of select="$usoCFDIDesc" />
														</fo:block>
													</fo:table-cell>
												</fo:table-row>
												<fo:table-row height="9px">
													<fo:table-cell>
														<fo:block font-size="6pt" font-weight="bold">
															Código Postal:
														</fo:block>
													</fo:table-cell>
													<fo:table-cell>
														<fo:block font-size="6pt">
															<xsl:value-of select="./cfdi:Receptor/@DomicilioFiscalReceptor" />
														</fo:block>
													</fo:table-cell>
												</fo:table-row>
											</fo:table-body>
										</fo:table>
										
									</fo:table-cell>
									

									<fo:table-cell display-align="before" text-align="left">
										<fo:table>
											<fo:table-column column-width="35mm" />
											<fo:table-column column-width="23mm" />
											<fo:table-body>
												<fo:table-row height="9px">
													<fo:table-cell display-align="center">
														<fo:block font-size="6pt" font-weight="bold">
															Tipo Comprobante:
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
															Serie / Folio:
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
															F. Emisión Comprobante:
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
															Tipo De Comprobante:
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
															Versión Comprobante:
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
															No. Certificado CFDI:
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
															No. Certificado SAT:
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
															F. Certificación:
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
															Lugar De Expedición:
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
															No. Transacción:
														</fo:block>
													</fo:table-cell>
													<fo:table-cell display-align="center">
														<fo:block font-size="6pt">
															<xsl:value-of select="$transaccion" />
														</fo:block>
													</fo:table-cell>
												</fo:table-row>
												
												<!-- MONEDA -->
												<fo:table-row height="9px">
													<fo:table-cell display-align="center">
														<fo:block font-size="6pt" font-weight="bold">
															Moneda:
														</fo:block>
													</fo:table-cell>
													<fo:table-cell display-align="center">
														<fo:block font-size="6pt">
															Sin divisa
<!-- 															<xsl:value-of select="$moneda" /> - <xsl:value-of select="$monedaDesc" /> -->
														</fo:block>
													</fo:table-cell>
												</fo:table-row>
												
												<!-- Exportaci�n -->
												<fo:table-row height="9px">
													<fo:table-cell display-align="center">
														<fo:block font-size="6pt" font-weight="bold">
															Exportación:
														</fo:block>
													</fo:table-cell>
													<fo:table-cell display-align="center">
														<fo:block font-size="6pt">
															<xsl:value-of select="@Exportacion" /> -&#160;
			                                            	<xsl:choose> 
									                        	<xsl:when test="@Exportacion = '01'"> 
									                              No Aplica
									                           </xsl:when>
									                           <xsl:when test="@Exportacion = '02'"> 
									                              Definitiva con clave A1
									                           </xsl:when>  	
									                           <xsl:when test="@Exportacion = '03'"> 
									                              Temporal
									                           </xsl:when>
									                           <xsl:otherwise> 
									                             No Existe
									                           </xsl:otherwise> 
									                        </xsl:choose> 
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
							<fo:table-column column-width="40.7mm" />
							<fo:table-column column-width="94.4mm" />
							<fo:table-column column-width="34.7mm" />
							<fo:table-column />
							
							<fo:table-body>
								<fo:table-row>
									<fo:table-cell display-align="center">
										<fo:block />
									</fo:table-cell>
									<fo:table-cell display-align="center">
										<fo:block>
											<xsl:if test="$periodicidad != ''">
												<fo:table>
													<fo:table-column column-width="24.4mm" />
														<fo:table-column column-width="70mm" />
														<fo:table-body>
															<fo:table-row height="9px">
																<fo:table-cell number-columns-spanned="2">
																	<fo:block font-size="6pt" font-weight="bold" space-before="1mm" space-after="1mm">
																		Información Global
																	</fo:block>
																</fo:table-cell>
															</fo:table-row>
															<fo:table-row>
															<fo:table-cell font-weight="bold" display-align="before" space-before="2mm" >
									                        	<fo:block font-size="6pt" font-weight="bold">
									                            	Año:
									                            </fo:block>
									                        </fo:table-cell>
									                        <fo:table-cell display-align="before" space-before="2mm">
									                        	<fo:block font-size="6pt">
									                            	<xsl:value-of select="$anio" />
									                            </fo:block>
									                       	</fo:table-cell>
														</fo:table-row>
														<fo:table-row>
															<fo:table-cell font-weight="bold" display-align="before" space-before="2mm" >
									                        	<fo:block font-size="6pt" font-weight="bold">
									                            	Mes:
									                            </fo:block>
									                        </fo:table-cell>
									                        <fo:table-cell display-align="before" space-before="2mm">
									                        	<fo:block font-size="6pt">
									                            	<xsl:value-of select="$meses" />
									                            </fo:block>
									                       	</fo:table-cell>
														</fo:table-row>
														<fo:table-row>
															<fo:table-cell font-weight="bold" display-align="before" space-before="2mm" >
									                        	<fo:block font-size="6pt" font-weight="bold">
									                            	Periodicidad:
									                            </fo:block>
									                        </fo:table-cell>
									                        <fo:table-cell display-align="before" space-before="2mm">
									                        	<fo:block font-size="6pt">
									                            	<xsl:value-of select="$periodicidad" />
									                            </fo:block>
									                       	</fo:table-cell>
														</fo:table-row>
													</fo:table-body>
												</fo:table>
											</xsl:if>
										</fo:block>
									</fo:table-cell>
									<fo:table-cell display-align="center" number-columns-spanned="2">
										<fo:table>
											<fo:table-body>
													<fo:table-row height="5px">
														<fo:table-cell display-align="center" text-align="center">
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
                                            ESTE DOCUMENTO ES UNA REPRESENTACIÓN IMPRESA DE UN CFDI 4.0 <fo:basic-link external-destination="url('https://www.sodimac.com.mx/')"
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
                                            Proveedor Autorizado de Certificación (PAC) <xsl:value-of select="$idPacExternal" />
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
	                    	<fo:table-column column-width="25mm" /><!-- Cve.Prod. Servicio -->
                        	<fo:table-column column-width="15mm" /><!-- Cantidad -->
                        	<fo:table-column column-width="28mm" /><!-- Clave Unidad -->
                        	<fo:table-column column-width="2mm" />	<!-- Unidad -->
                        	<fo:table-column column-width="47mm" /><!-- Descripci�n -->
                        	<fo:table-column column-width="15mm" /><!-- Valor Unitario -->
                        	<fo:table-column column-width="15mm" /><!-- Descuento -->
                        	<fo:table-column column-width="2mm"  /><!-- Espacio -->
                        	<fo:table-column column-width="30mm" /><!-- Importe -->
                        	
                        	<fo:table-header>
                            	<fo:table-row height="12px">
                                                                          
									<fo:table-cell height="6px" display-align="center" text-align="center" font-weight="bold">
	                             		<fo:block  font-size="5pt" space-before="1mm" space-after="1mm">
	                                    	 Cve.Prod. Servicio
	                                 	</fo:block>
	                             	</fo:table-cell>
	                             	<fo:table-cell height="6px" display-align="center" text-align="center" font-weight="bold">
										<fo:block font-size="5pt" space-before="1mm" space-after="1mm">
	                                      	Cantidad
	                                  	</fo:block>
	                             	</fo:table-cell>
	                             	<fo:table-cell height="6px" display-align="center" text-align="center" font-weight="bold">
	                                  	<fo:block font-size="5pt" space-before="1mm" space-after="1mm">
	                                      	Clave Unidad
	                                  	</fo:block>
	                              	</fo:table-cell>
	                             	<fo:table-cell height="6px" display-align="center" text-align="center" font-weight="bold">
										<fo:block font-size="5pt" space-before="1mm" space-after="1mm">
	                                  	</fo:block>
	                              	</fo:table-cell>
	                              	<fo:table-cell height="6px" display-align="center" text-align="center" font-weight="bold">
										<fo:block font-size="5pt" space-before="1mm" space-after="1mm">
	                                      	Descripción
	                                  	</fo:block>
	                              	</fo:table-cell>
	                              	<fo:table-cell height="6px" display-align="center" text-align="center" font-weight="bold">
										<fo:block font-size="5pt" space-before="1mm" space-after="1mm">
	                                      	Valor Unitario  
	                                  	</fo:block>
	                              	</fo:table-cell>
	                              	<fo:table-cell height="6px" display-align="center" text-align="center" font-weight="bold">
										<fo:block font-size="5pt" space-before="1mm" space-after="1mm">
	                                      	Descuento 
	                                  	</fo:block>
	                              	</fo:table-cell>
	                              	<fo:table-cell height="6px" display-align="center" text-align="center" font-weight="bold">
										<fo:block />
	                              	</fo:table-cell>
	                              	<fo:table-cell height="6px" display-align="center" text-align="center" font-weight="bold">
										<fo:block font-size="5pt"  space-before="1mm" space-after="1mm">
	                                      	Importe
	                                  	</fo:block>
	                              	</fo:table-cell>
								</fo:table-row>
								<fo:table-row>
                               		<fo:table-cell height="1px" font-weight="bold" number-columns-spanned="9">
                                    	<fo:block border-bottom-width="1pt" border-bottom-style="solid"></fo:block>
                                    </fo:table-cell>
                                </fo:table-row>
							</fo:table-header>
                        	
                            <fo:table-body>
								
								<xsl:for-each select="./cfdi:Conceptos/cfdi:Concepto">
									<xsl:variable name="v_ClaveProdServ" select="@ClaveProdServ" />
									<xsl:variable name="v_Cantidad" select="@Cantidad" />
									<xsl:variable name="v_ClaveUnidad" select="@ClaveUnidad" />
									<xsl:variable name="v_Descripcion" select="util:toDescConcept(@Descripcion)" />
									<xsl:variable name="v_ValorUnitario" select="@ValorUnitario" />
									<xsl:variable name="v_Importe" select="@Importe" />
									<xsl:variable name="v_Descuento" select="@Descuento" />
									<xsl:variable name="v_ObjetoImp" select="@ObjetoImp" />
									
									<fo:table-row>
	                                 	<fo:table-cell height="2px" font-weight="bold" number-columns-spanned="9">
	                                 		<fo:block></fo:block>
	                                 	</fo:table-cell>
	                                </fo:table-row>
									
										<fo:table-row>
											<fo:table-cell>
												<fo:block font-size="5pt" space-before="1mm" space-after="1mm" text-align="center">
	                                            	<xsl:value-of select="$v_ClaveProdServ" />
	                                            </fo:block>
								            </fo:table-cell>
								          	<fo:table-cell>
								            	<fo:block font-size="5pt" space-before="1mm" space-after="1mm" text-align="center">
								                	<xsl:choose>
								                		<xsl:when test="not(number($v_Cantidad))">
					            							<xsl:value-of select="$v_Cantidad"/>
					        							</xsl:when>
												        <xsl:when test="contains($v_Cantidad, '.')">
												            <xsl:value-of select="concat ( substring-before( $v_Cantidad, '.' ) , '.' , substring ( substring-after ( $v_Cantidad, '.' ) , 1 , 2 ) )"/>
												        </xsl:when>
												        <xsl:otherwise>
												            <xsl:value-of select="format-number($v_Cantidad, '#.##')"/>
												        </xsl:otherwise>
								    				</xsl:choose>
	                                            </fo:block>
											</fo:table-cell>
											<fo:table-cell>
								               	<fo:block font-size="5pt" space-before="1mm" space-after="1mm" text-align="center">
	                                            	<xsl:value-of select="$v_ClaveUnidad" />
	                                          	</fo:block>
											</fo:table-cell>
											<fo:table-cell>
								            	<fo:block font-size="5pt" space-before="1mm" space-after="1mm" text-align="center">
								                	
	                                            </fo:block>
											</fo:table-cell>
								           	<fo:table-cell>
												<fo:block font-size="5pt" space-before="1mm" space-after="1mm" text-align="center">
	                                            	<xsl:value-of select="$v_Descripcion" />
	                                            </fo:block>
											</fo:table-cell>
											<fo:table-cell display-align="after" text-align="right">
							                	<fo:block font-size="5pt" space-before="1mm" space-after="1mm" text-align="right">
<!-- 	                                               	$<xsl:value-of select="format-number($v_ValorUnitario, '#,###.00')" /> -->
	                                           	    <xsl:choose>
								                		<xsl:when test="not(number($v_ValorUnitario))">
					            							$<xsl:value-of select="$v_ValorUnitario"/>
					        							</xsl:when>
												        <xsl:when test="contains($v_ValorUnitario, '.')">
												        	<xsl:variable name="v_ValorUnitarioFormat" select="concat ( substring-before( $v_ValorUnitario, '.' ) , '.' , substring ( substring-after ( $v_ValorUnitario, '.' ) , 1 , 2 ) )" />
												            $<xsl:value-of select="format-number($v_ValorUnitarioFormat, '#,###.00')"/>
												        </xsl:when>
												        <xsl:otherwise>
												            $<xsl:value-of select="format-number($v_ValorUnitario, '#,###.00')"/>
												        </xsl:otherwise>
								    				 </xsl:choose>                                  
	                                           	</fo:block>
							             	</fo:table-cell>
							             	<fo:table-cell display-align="after" text-align="right">
							                	<fo:block font-size="5pt" space-before="1mm" space-after="1mm" text-align="right">
<!-- 	                                               	$<xsl:value-of select="format-number($v_ImporteConceptoFormat, '#,###.00')" /> -->
	                                           	    <xsl:choose>
								                		<xsl:when test="not(number($v_Importe))">
					            							$<xsl:value-of select="$v_Importe"/>
					        							</xsl:when>
												        <xsl:when test="contains($v_Importe, '.')">
												        	<xsl:variable name="v_ImporteConceptoFormat" select="concat ( substring-before( $v_Importe, '.' ) , '.' , substring ( substring-after ( $v_Importe, '.' ) , 1 , 2 ) )" />
												            $<xsl:value-of select="format-number($v_ImporteConceptoFormat, '#,###.00')"/>
												        </xsl:when>
												        <xsl:otherwise>
												            $<xsl:value-of select="format-number($v_Importe, '#,###.00')"/>
												        </xsl:otherwise>
								    				 </xsl:choose>
	                                           	</fo:block>
							             	</fo:table-cell>
							             	<fo:table-cell display-align="after" text-align="right">
												<fo:block />
									        </fo:table-cell>
							             	<fo:table-cell display-align="after" text-align="right">
							                	<fo:block font-size="5pt" space-before="1mm" space-after="1mm" text-align="right">
<!-- 	                                               	$<xsl:value-of select="format-number($v_Descuento, '#,##0.00')" /> -->
	                                           	    <xsl:choose>
								                		<xsl:when test="not(number($v_Descuento))">
					            							$<xsl:value-of select="$v_Descuento"/>
					        							</xsl:when>
												        <xsl:when test="contains($v_Descuento, '.')">
												        	<xsl:variable name="v_DescuentoFormat" select="concat ( substring-before( $v_Descuento, '.' ) , '.' , substring ( substring-after ( $v_Descuento, '.' ) , 1 , 2 ) )" />
												            $<xsl:value-of select="format-number($v_DescuentoFormat, '#,##0.00')"/>
												        </xsl:when>
												        <xsl:otherwise>
												            $<xsl:value-of select="format-number($v_Descuento, '#,##0.00')"/>
												        </xsl:otherwise>
								    				 </xsl:choose>
	                                           	</fo:block>
							             	</fo:table-cell>
										</fo:table-row>
									
								</xsl:for-each><!-- ./cfdi:Conceptos/cfdi:Concepto -->
                                
                                <fo:table-row>
                                 	<fo:table-cell height="2px" number-columns-spanned="9">
                                 		<fo:block></fo:block>
                                 	</fo:table-cell>  
                                </fo:table-row>
	                                
                               	<fo:table-row>
									<fo:table-cell height="2px" font-weight="bold"  number-columns-spanned="9">
                                 		<fo:block border-top-width="1pt" border-top-style="solid"></fo:block>
                                 	</fo:table-cell>  
								</fo:table-row>
                                
                                <fo:table-row>
									<fo:table-cell height="2px" number-columns-spanned="6">
										<fo:block font-size="5pt" space-before="1mm" space-after="1mm">
											<fo:table>
												<fo:table-column column-width="25mm" />
							                	<fo:table-column />
							                	<fo:table-body>
							                		<fo:table-row>
														<fo:table-cell font-weight="bold" display-align="before" space-before="2mm" >
								                        	<fo:block  font-size="5pt" space-before="2mm" space-after="2mm">
								                            	Importe letra:
								                            </fo:block>
								                        </fo:table-cell>
								                        <fo:table-cell display-align="before" space-before="2mm">
								                        	<fo:block font-size="5pt" text-align="left" space-before="2mm" space-after="2mm">
								                            	<xsl:value-of select="util:toCamelCase($importeLetra)" />
								                            </fo:block>
								                       	</fo:table-cell>
													</fo:table-row>
							                	</fo:table-body>
											</fo:table>
										</fo:block>
									</fo:table-cell>
									
									<fo:table-cell height="2px" font-weight="bold" number-columns-spanned="3">
										<fo:block font-size="5pt" space-before="1mm" space-after="1mm">
											<fo:table>
												<fo:table-column column-width="20mm" />
												<fo:table-column />
							                	<fo:table-body>
							                		<fo:table-row>
														<fo:table-cell font-weight="bold" display-align="before" space-before="2mm">
															<fo:block font-size="5pt" space-before="2mm"
																space-after="2mm">
																Subtotal:
															</fo:block>
														</fo:table-cell>
														<fo:table-cell display-align="after" space-before="2mm">
															<fo:block font-size="6pt" text-align="right" space-before="2mm" space-after="2mm">
<!-- 																$<xsl:value-of select="format-number(@SubTotal, '#,###.00')" /> -->
		                                       	     			<xsl:choose>
								                					<xsl:when test="not(number(@SubTotal))">
					            										$<xsl:value-of select="@SubTotal"/>
					        										</xsl:when>
												       			 <xsl:when test="contains(@SubTotal, '.')">
												       			  	  <xsl:variable name="v_SubTotalResumen" select="concat ( substring-before( @SubTotal, '.' ) , '.' , substring ( substring-after ( @SubTotal, '.' ) , 1 , 2 ) )" />
												          			  $<xsl:value-of select="format-number($v_SubTotalResumen, '#,###.00')"/>
												       			 </xsl:when>
												        			<xsl:otherwise>
												            			$<xsl:value-of select="format-number(@SubTotal, '#,###.00')"/>
												        			</xsl:otherwise>
								    				 			</xsl:choose>															
															</fo:block>
														</fo:table-cell>
													</fo:table-row>
													<fo:table-row>
							                			<fo:table-cell font-weight="bold" display-align="before" space-before="2mm">
															<fo:block font-size="5pt" text-align="left" space-before="2mm" space-after="2mm">
																Descuento:
															</fo:block>
														</fo:table-cell>
														<fo:table-cell display-align="after" space-before="2mm">
															<fo:block font-size="6pt" text-align="right" space-before="2mm" space-after="2mm">
<!-- 																$<xsl:value-of select="format-number(@Descuento, '#,###.00')" /> -->
		                                       	     			<xsl:choose>
								                					<xsl:when test="not(number(@Descuento))">
					            										$0
					        										</xsl:when>
					        									<xsl:when test="contains(@Descuento, '.')">
												       			  	  <xsl:variable name="v_DescuentoResumen" select="concat ( substring-before( @Descuento, '.' ) , '.' , substring ( substring-after ( @Descuento, '.' ) , 1 , 2 ) )" />
												          			  $<xsl:value-of select="format-number($v_DescuentoResumen, '#,###.00')"/>
												       			</xsl:when>						
												        			<xsl:otherwise>
												            			$<xsl:value-of select="format-number(@Descuento, '#,###.00')"/>
												        			</xsl:otherwise>
								    				 			</xsl:choose>															
															</fo:block>
														</fo:table-cell>
							                		</fo:table-row>
													
													<xsl:for-each select="./cfdi:Impuestos/cfdi:Traslados/cfdi:Traslado">
						                				<xsl:if test="@Importe != '0.00'">
			                                          		<fo:table-row>
							                				    <fo:table-cell display-align="before" space-before="2mm">
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
<!-- 							                                            $<xsl:value-of select="format-number(@Importe, '#,###.00')" /> -->
		                                       	     			        <xsl:choose>
								                					        <xsl:when test="not(number(@Importe))">
					            										        $<xsl:value-of select="@Importe"/>
					        										        </xsl:when>
					        										        <xsl:when test="contains(@Importe, '.')">
												       			  	  			<xsl:variable name="v_ImporteResumen" select="concat ( substring-before( @Importe, '.' ) , '.' , substring ( substring-after ( @Importe, '.' ) , 1 , 2 ) )" />
												          			  			$<xsl:value-of select="format-number($v_ImporteResumen, '#,###.00')"/>
												       						</xsl:when>									       			         
												        			        <xsl:otherwise>
												            			        $<xsl:value-of select="format-number(@Importe, '#,###.00')"/>
												        			        </xsl:otherwise>
								    				 			        </xsl:choose>							                                        
							                                        </fo:block>
						                                    	</fo:table-cell>
							                				</fo:table-row>
						                				</xsl:if> 
													</xsl:for-each>
													<fo:table-row>
														<fo:table-cell font-weight="bold" display-align="before" space-before="2mm">
					                                        <fo:block  font-size="5pt" text-align="left" space-before="2mm" space-after="2mm">
					                                            Total:
					                                        </fo:block>
					                                    </fo:table-cell>
					                                    <fo:table-cell display-align="after" space-before="2mm" >
					                                        <fo:block font-size="6pt" text-align="right" space-before="2mm" space-after="2mm">
<!-- 					                                            $<xsl:value-of select="format-number(@Total, '#,###.00')" /> -->
				                               	     			<xsl:choose>
								                					<xsl:when test="not(number(@Total))">
					            										$<xsl:value-of select="@Total"/>
					        										</xsl:when>
												       			 <xsl:when test="contains(@Total, '.')">
												       			 	  <xsl:variable name="v_TotalResumen" select="concat ( substring-before( @Total, '.' ) , '.' , substring ( substring-after ( @Total, '.' ) , 1 , 2 ) )" />
												          			  $<xsl:value-of select="format-number($v_TotalResumen, '#,###.00')"/>
												       			 </xsl:when>
												        			<xsl:otherwise>
												            			$<xsl:value-of select="format-number(@Total, '#,###.00')"/>
												        			</xsl:otherwise>
								    				 			</xsl:choose>	                                        
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
                    
                    <!--BLOQUE Nombre de la Obra--> 
                    <fo:block space-before="3mm" >
	                    
	                    <fo:table>
							<fo:table-column column-width="150.mm" />
							<fo:table-column />
							
							<fo:table-body>
								<xsl:if test="$ticketId != ''" >
									<fo:table-row>
										<fo:table-cell display-align="center">
											<fo:table>
												<fo:table-column column-width="27.7mm" />
												<fo:table-column column-width="66.7mm" />
												<fo:table-body>
													<fo:table-row height="9px">
														<fo:table-cell>
															<fo:block font-size="6pt" font-weight="bold">
																Ticket:
															</fo:block>
														</fo:table-cell>
														<fo:table-cell>
															<fo:block font-size="6pt">
																<xsl:value-of select="$ticketId" />
															</fo:block>
														</fo:table-cell>
													</fo:table-row>
												</fo:table-body>
											</fo:table>
										</fo:table-cell>
										<fo:table-cell display-align="before" text-align="left">
											<fo:block></fo:block>
										</fo:table-cell>
									</fo:table-row>
								</xsl:if>
								<xsl:if test="$nombreObra != ''" >
									<fo:table-row>
										<fo:table-cell display-align="center">
											<fo:table>
												<fo:table-column column-width="27.7mm" />
												<fo:table-column column-width="66.7mm" />
												<fo:table-body>
													<fo:table-row height="9px">
														<fo:table-cell>
															<fo:block font-size="6pt" font-weight="bold">
																Nombre de la Obra:
															</fo:block>
														</fo:table-cell>
														<fo:table-cell>
															<fo:block font-size="6pt">
																<xsl:value-of select="$nombreObra"/>
															</fo:block>
														</fo:table-cell>
													</fo:table-row>
												</fo:table-body>
											</fo:table>
										</fo:table-cell>
										<fo:table-cell display-align="before" text-align="left">
											<fo:block></fo:block>
										</fo:table-cell>
									</fo:table-row>
								</xsl:if>
								<xsl:if test="$responsableObra != ''" >
									<fo:table-row>
										<fo:table-cell display-align="center">
											<fo:table>
												<fo:table-column column-width="27.7mm" />
												<fo:table-column column-width="66.7mm" />
												<fo:table-body>
													<fo:table-row height="9px">
														<fo:table-cell>
															<fo:block font-size="6pt" font-weight="bold">
																Responsable de la Obra:
															</fo:block>
														</fo:table-cell>
														<fo:table-cell>
															<fo:block font-size="6pt">
																<xsl:value-of select="$responsableObra"/>
															</fo:block>
														</fo:table-cell>
													</fo:table-row>
												</fo:table-body>
											</fo:table>
										</fo:table-cell>
										<fo:table-cell display-align="before" text-align="left">
											<fo:block></fo:block>
										</fo:table-cell>
									</fo:table-row>
								</xsl:if>
								<xsl:if test="$uuidRelacionado != ''" >
									<fo:table-row>
										<fo:table-cell display-align="center">
											<fo:table>
												<fo:table-column column-width="27.7mm" />
												<fo:table-column column-width="66.7mm" />
												<fo:table-body>
													<fo:table-row height="9px">
														<fo:table-cell>
															<fo:block font-size="6pt" font-weight="bold">
																Folio Fiscal a Relacionar:
															</fo:block>
														</fo:table-cell>
														<fo:table-cell>
															<fo:block font-size="6pt">
																<xsl:value-of select="$uuidRelacionado"/>
															</fo:block>
														</fo:table-cell>
													</fo:table-row>
												</fo:table-body>
											</fo:table>
										</fo:table-cell>
										<fo:table-cell display-align="before" text-align="left">
											<fo:block></fo:block>
										</fo:table-cell>
									</fo:table-row>
								</xsl:if>
								<fo:table-row>
									<fo:table-cell display-align="center">
										<fo:table>
											<fo:table-column column-width="27.7mm" />
											<fo:table-column column-width="66.7mm" />
											<fo:table-body>
												<fo:table-row height="9px">
													<fo:table-cell>
														<fo:block font-size="6pt" font-weight="bold" />
													</fo:table-cell>
													<fo:table-cell>
														<fo:block font-size="6pt" />
													</fo:table-cell>
												</fo:table-row>
											</fo:table-body>
										</fo:table>
									</fo:table-cell>
									<fo:table-cell display-align="before" text-align="left">
										<fo:block></fo:block>
									</fo:table-cell>
								</fo:table-row>
							</fo:table-body>
	                    </fo:table>
                    </fo:block>
                    <!-- FIN BLOQUE Nombre de la Obra-->     
                    
                    <!--BLOQUE INFORMACION DEL PAGO--> 
                    <fo:block space-before="2mm">
                    	
                    	
                    	<fo:table>
							<fo:table-body>
								<fo:table-row height="20px">
									<fo:table-cell>
										<fo:block></fo:block>
									</fo:table-cell>
								</fo:table-row>
								<fo:table-row border="inherit" height="20px">
									<fo:table-cell border="inherit" display-align="center" text-align="left" font-weight="bold">
										<fo:block font-size="10pt">Información de pago</fo:block>
									</fo:table-cell>
								</fo:table-row>
							</fo:table-body>
						</fo:table>
                    	
                    	<fo:table>
	                    	<fo:table-column column-width="25mm" /><!-- Forma Pago -->
                        	<fo:table-column column-width="25mm" /><!-- Cuenta Ordenante -->
                        	<fo:table-column column-width="20mm" /><!-- N�mero de operaci�n -->
                        	<fo:table-column column-width="20mm" /><!-- Nombre de Banco -->
                        	<fo:table-column column-width="34mm" /><!-- Cuenta Beneficiario -->
                        	<fo:table-column column-width="15mm" /><!-- Fecha Pago -->
                        	<fo:table-column column-width="10mm" /><!-- Moneda -->
                        	<fo:table-column column-width="30mm" /><!-- Monto -->
                        
	                        <fo:table-header>
	                           	<fo:table-row height="12px">
	                                                                         
									<fo:table-cell height="6px" display-align="center" text-align="center" font-weight="bold">
	                             		<fo:block  font-size="5pt" space-before="1mm" space-after="1mm">
	                                    	 Forma Pago
	                                 	</fo:block>
	                             	</fo:table-cell>
	                             	<fo:table-cell height="6px" display-align="center" text-align="center" font-weight="bold">
										<fo:block font-size="5pt" space-before="1mm" space-after="1mm">
	                                      	Cuenta Ordenante
	                                  	</fo:block>
	                             	</fo:table-cell>
	                             	<fo:table-cell height="6px" display-align="center" text-align="center" font-weight="bold">
	                                  	<fo:block font-size="5pt" space-before="1mm" space-after="1mm">
	                                      	Número de operación
	                                  	</fo:block>
	                              	</fo:table-cell>
	                             	<fo:table-cell height="6px" display-align="center" text-align="center" font-weight="bold">
										<fo:block font-size="5pt" space-before="1mm" space-after="1mm">
	                                      	Nombre de Banco
	                                  	</fo:block>
	                              	</fo:table-cell>
	                              	<fo:table-cell height="6px" display-align="center" text-align="center" font-weight="bold">
										<fo:block font-size="5pt" space-before="1mm" space-after="1mm">
	                                      	Cuenta Beneficiario
	                                  	</fo:block>
	                              	</fo:table-cell>
	                              	<fo:table-cell height="6px" display-align="center" text-align="center" font-weight="bold">
										<fo:block font-size="5pt" space-before="1mm" space-after="1mm">
	                                      	Fecha Pago
	                                  	</fo:block>
	                              	</fo:table-cell>
	                              	<fo:table-cell height="6px" display-align="center" text-align="center" font-weight="bold">
										<fo:block font-size="5pt" space-before="1mm" space-after="1mm">
	                                      	Moneda 
	                                  	</fo:block>
	                              	</fo:table-cell>
	                              	<fo:table-cell height="6px" display-align="center" text-align="center" font-weight="bold">
										<fo:block font-size="5pt"  space-before="1mm" space-after="1mm">
	                                      	Monto
	                                  	</fo:block>
	                              	</fo:table-cell>
								</fo:table-row>
								<fo:table-row>
	                              	<fo:table-cell height="1px" font-weight="bold" number-columns-spanned="8">
	                                   	<fo:block border-bottom-width="1pt" border-bottom-style="solid"></fo:block>
	                                </fo:table-cell>
	                           </fo:table-row>
							</fo:table-header>
							
							<fo:table-body>
										
								<fo:table-row>
	                            	<fo:table-cell height="2px" font-weight="bold" number-columns-spanned="8">
	                                	<fo:block></fo:block>
	                                </fo:table-cell>
								</fo:table-row>
								
								<fo:table-row>
									<fo:table-cell>
										<fo:block font-size="5pt" space-before="1mm" space-after="1mm" text-align="center">
	                                    	<xsl:value-of select="$sFormaPagoComplemento" /> - <xsl:value-of select="$sFormaPagoComplementoDesc" /> 
	                                   	</fo:block>
						            </fo:table-cell>
						            <fo:table-cell>
										<fo:block font-size="5pt" space-before="1mm" space-after="1mm" text-align="center">
	                                    	<xsl:value-of select="./cfdi:Complemento/pago20:Pagos/pago20:Pago/@CtaOrdenante" />
	                                   	</fo:block>
						            </fo:table-cell>
						            <fo:table-cell>
										<fo:block font-size="5pt" space-before="1mm" space-after="1mm" text-align="center">
	                                    	<xsl:value-of select="./cfdi:Complemento/pago20:Pagos/pago20:Pago/@NumOperacion" />
	                                   	</fo:block>
						            </fo:table-cell>
						            <fo:table-cell>
										<fo:block font-size="5pt" space-before="1mm" space-after="1mm" text-align="center">
											<xsl:value-of select="./cfdi:Complemento/pago20:Pagos/pago20:Pago/@NomBancoOrdExt" />
	                                   	</fo:block>
						            </fo:table-cell>
						            <fo:table-cell>
										<fo:block font-size="5pt" space-before="1mm" space-after="1mm" text-align="center">
	                                    	<xsl:value-of select="./cfdi:Complemento/pago20:Pagos/pago20:Pago/@CtaBeneficiario" />
	                                   	</fo:block>
						            </fo:table-cell>
						            <fo:table-cell>
										<fo:block font-size="5pt" space-before="1mm" space-after="1mm" text-align="center">
											<xsl:variable name="dt" select="./cfdi:Complemento/pago20:Pagos/pago20:Pago/@FechaPago" />
											<xsl:value-of select="concat(substring($dt,1,4),'-',substring($dt,6,2),'-',substring($dt,9,2))" />
	                                   	</fo:block>
						            </fo:table-cell>
						            <fo:table-cell>
										<fo:block font-size="5pt" space-before="1mm" space-after="1mm" text-align="center">
	                                    	<xsl:value-of select="./cfdi:Complemento/pago20:Pagos/pago20:Pago/@MonedaP" />
	                                   	</fo:block>
						            </fo:table-cell>
						            <fo:table-cell>
										<fo:block font-size="5pt" space-before="1mm" space-after="1mm" text-align="center">
											<xsl:variable name="v_monto" select="./cfdi:Complemento/pago20:Pagos/pago20:Pago/@Monto" />
	                              			<xsl:choose>
						                		<xsl:when test="not(number($v_monto))">
			            							$<xsl:value-of select="$v_monto"/>
			        							</xsl:when>
										        <xsl:when test="contains($v_monto, '.')">
										        	<xsl:variable name="v_montoFormat" select="concat ( substring-before( $v_monto, '.' ) , '.' , substring ( substring-after ( $v_monto, '.' ) , 1 , 2 ) )" />
										            $<xsl:value-of select="format-number($v_montoFormat, '#,###.00')"/>
										        </xsl:when>
										        <xsl:otherwise>
										            $<xsl:value-of select="format-number($v_monto, '#,###.00')"/>
										        </xsl:otherwise>
						    				</xsl:choose>
	                                   	</fo:block>
						            </fo:table-cell>
								</fo:table-row>
		                		
	                        </fo:table-body>
                        </fo:table>
                        
                        <fo:table>
							<fo:table-body>
								<fo:table-row height="20px">
									<fo:table-cell>
										<fo:block></fo:block>
									</fo:table-cell>
								</fo:table-row>
								<fo:table-row border="inherit" height="20px">
									<fo:table-cell border="inherit" display-align="center" text-align="left" font-weight="bold">
										<fo:block font-size="8pt">Documentos relacionados</fo:block>
									</fo:table-cell>
								</fo:table-row>
							</fo:table-body>
						</fo:table>
						
						<fo:table>
                        	<fo:table-column column-width="39mm" /><!-- Id documento -->
                        	<fo:table-column column-width="15mm" /><!-- Folio -->
                        	<fo:table-column column-width="15mm" /><!-- Serie -->
                        	<fo:table-column column-width="15mm" /><!-- Parcialidad -->
                        	<fo:table-column column-width="15mm" /><!-- Moneda del documento -->
                        	<fo:table-column column-width="20mm" /><!-- M�todo de pago -->
                        	<fo:table-column column-width="20mm" /><!-- Importe anterior -->
                        	<fo:table-column column-width="20mm" /><!-- Importe pagado -->
                        	<fo:table-column column-width="20mm" /><!-- Importe Saldo Insoluto -->
                        	
                        	<fo:table-header>
	                           	<fo:table-row height="12px">
	                                                                         
									<fo:table-cell height="6px" display-align="center" text-align="center" font-weight="bold">
	                             		<fo:block  font-size="5pt" space-before="1mm" space-after="1mm">
	                                    	 Id documento
	                                 	</fo:block>
	                             	</fo:table-cell>
	                             	<fo:table-cell height="6px" display-align="center" text-align="center" font-weight="bold">
										<fo:block font-size="5pt" space-before="1mm" space-after="1mm">
	                                      	Folio
	                                  	</fo:block>
	                             	</fo:table-cell>
	                             	<fo:table-cell height="6px" display-align="center" text-align="center" font-weight="bold">
	                                  	<fo:block font-size="5pt" space-before="1mm" space-after="1mm">
	                                      	Serie
	                                  	</fo:block>
	                              	</fo:table-cell>
	                             	<fo:table-cell height="6px" display-align="center" text-align="center" font-weight="bold">
										<fo:block font-size="5pt" space-before="1mm" space-after="1mm">
	                                      	Parcialidad
	                                  	</fo:block>
	                              	</fo:table-cell>
	                              	<fo:table-cell height="6px" display-align="center" text-align="center" font-weight="bold">
										<fo:block font-size="5pt" space-before="1mm" space-after="1mm">
	                                      	Moneda del documento
	                                  	</fo:block>
	                              	</fo:table-cell>
	                              	<fo:table-cell height="6px" display-align="center" text-align="center" font-weight="bold">
										<fo:block font-size="5pt" space-before="1mm" space-after="1mm">
	                                      	Método de pago
	                                  	</fo:block>
	                              	</fo:table-cell>
	                              	<fo:table-cell height="6px" display-align="center" text-align="center" font-weight="bold">
										<fo:block font-size="5pt" space-before="1mm" space-after="1mm">
	                                      	Importe anterior
	                                  	</fo:block>
	                              	</fo:table-cell>
	                              	<fo:table-cell height="6px" display-align="center" text-align="center" font-weight="bold">
										<fo:block font-size="5pt"  space-before="1mm" space-after="1mm">
	                                      	Importe pagado
	                                  	</fo:block>
	                              	</fo:table-cell>
	                              	<fo:table-cell height="6px" display-align="center" text-align="center" font-weight="bold">
										<fo:block font-size="5pt"  space-before="1mm" space-after="1mm">
	                                      	Importe saldo insoluto
	                                  	</fo:block>
	                              	</fo:table-cell>
								</fo:table-row>
								<fo:table-row>
	                              	<fo:table-cell height="1px" font-weight="bold" number-columns-spanned="9">
	                                   	<fo:block border-bottom-width="1pt" border-bottom-style="solid"></fo:block>
	                                </fo:table-cell>
	                           </fo:table-row>
							</fo:table-header>
							
							<fo:table-body>
								
								<xsl:for-each select="./cfdi:Complemento/pago20:Pagos/pago20:Pago/pago20:DoctoRelacionado">
									<xsl:variable name="v_ImpSaldoAnt" select="@ImpSaldoAnt" />
									<xsl:variable name="v_ImpPagado" select="@ImpPagado" />
									<xsl:variable name="v_ImpSaldoInsoluto" select="@ImpSaldoInsoluto" />
									
									<fo:table-row>
	                                 	<fo:table-cell height="2px" font-weight="bold" number-columns-spanned="9">
	                                 		<fo:block></fo:block>
	                                 	</fo:table-cell>
	                                </fo:table-row>
									
									<fo:table-row>
										<fo:table-cell>
											<fo:block font-size="5pt" space-before="1mm" space-after="1mm" text-align="left">
                                            	<xsl:value-of select="@IdDocumento" />
                                            </fo:block>
							            </fo:table-cell>
							          	<fo:table-cell>
							            	<fo:block font-size="5pt" space-before="1mm" space-after="1mm" text-align="center">
							                	<xsl:value-of select="@Folio" />
                                            </fo:block>
										</fo:table-cell>
										<fo:table-cell>
							               	<fo:block font-size="5pt" space-before="1mm" space-after="1mm" text-align="center">
                                            	<xsl:value-of select="@Serie" />
                                          	</fo:block>
										</fo:table-cell>
										<fo:table-cell>
							            	<fo:block font-size="5pt" space-before="1mm" space-after="1mm" text-align="center">
							                	<xsl:value-of select="@NumParcialidad" />
                                            </fo:block>
										</fo:table-cell>
							           	<fo:table-cell>
											<fo:block font-size="5pt" space-before="1mm" space-after="1mm" text-align="center">
                                            	<xsl:value-of select="@MonedaDR" />
                                            </fo:block>
										</fo:table-cell>
										<fo:table-cell>
											<fo:block font-size="5pt" space-before="1mm" space-after="1mm" text-align="center">
                                            	PPD
                                            </fo:block>
										</fo:table-cell>
										<fo:table-cell display-align="after" text-align="right">
						                	<fo:block font-size="5pt" space-before="1mm" space-after="1mm" text-align="right">
                                           	    <xsl:choose>
							                		<xsl:when test="not(number($v_ImpSaldoAnt))">
				            							$<xsl:value-of select="$v_ImpSaldoAnt"/>
				        							</xsl:when>
											        <xsl:when test="contains($v_ImpSaldoAnt, '.')">
											        	<xsl:variable name="v_ImpSaldoAntFormat" select="concat ( substring-before( $v_ImpSaldoAnt, '.' ) , '.' , substring ( substring-after ( $v_ImpSaldoAnt, '.' ) , 1 , 2 ) )" />
											            $<xsl:value-of select="format-number($v_ImpSaldoAntFormat, '#,###.00')"/>
											        </xsl:when>
											        <xsl:otherwise>
											            $<xsl:value-of select="format-number($v_ImpSaldoAnt, '#,###.00')"/>
											        </xsl:otherwise>
							    				 </xsl:choose>                                  
                                           	</fo:block>
						             	</fo:table-cell>
						             	<fo:table-cell display-align="after" text-align="right">
						                	<fo:block font-size="5pt" space-before="1mm" space-after="1mm" text-align="right">
                                           	    <xsl:choose>
							                		<xsl:when test="not(number($v_ImpPagado))">
				            							$<xsl:value-of select="$v_ImpPagado"/>
				        							</xsl:when>
											        <xsl:when test="contains($v_ImpPagado, '.')">
											        	<xsl:variable name="v_ImpPagadoFormat" select="concat ( substring-before( $v_ImpPagado, '.' ) , '.' , substring ( substring-after ( $v_ImpPagado, '.' ) , 1 , 2 ) )" />
											            $<xsl:value-of select="format-number($v_ImpPagadoFormat, '#,###.00')"/>
											        </xsl:when>
											        <xsl:otherwise>
											            $<xsl:value-of select="format-number($v_ImpPagado, '#,###.00')"/>
											        </xsl:otherwise>
							    				 </xsl:choose>
                                           	</fo:block>
						             	</fo:table-cell>
						             	<fo:table-cell display-align="after" text-align="right">
						                	<fo:block font-size="5pt" space-before="1mm" space-after="1mm" text-align="right">
                                           	    <xsl:choose>
							                		<xsl:when test="not(number($v_ImpSaldoInsoluto))">
				            							$<xsl:value-of select="$v_ImpSaldoInsoluto"/>
				        							</xsl:when>
											        <xsl:when test="contains($v_ImpSaldoInsoluto, '.')">
											        	<xsl:variable name="v_ImpSaldoInsolutoFormat" select="concat ( substring-before( $v_ImpSaldoInsoluto, '.' ) , '.' , substring ( substring-after ( $v_ImpSaldoInsoluto, '.' ) , 1 , 2 ) )" />
											            $<xsl:value-of select="format-number($v_ImpSaldoInsolutoFormat, '#,###.00')"/>
											        </xsl:when>
											        <xsl:otherwise>
											            $<xsl:value-of select="format-number($v_ImpSaldoInsoluto, '#,###.00')"/>
											        </xsl:otherwise>
							    				 </xsl:choose>
                                           	</fo:block>
						             	</fo:table-cell>
									</fo:table-row>
									
								</xsl:for-each> <!-- select="./cfdi:Complemento/pago20:Pagos/pago20:Pago/pago20:DoctoRelacionado" -->
								
								
								<fo:table-row>
                                 	<fo:table-cell height="2px" number-columns-spanned="9">
                                 		<fo:block></fo:block>
                                 	</fo:table-cell>  
                                </fo:table-row>
	                                
                               	<fo:table-row>
									<fo:table-cell height="2px" font-weight="bold" number-columns-spanned="9">
                                 		<fo:block border-top-width="1pt" border-top-style="solid"></fo:block>
                                 	</fo:table-cell>  
								</fo:table-row>
                                
                                <fo:table-row>
									<fo:table-cell number-columns-spanned="5">
										<fo:block font-size="5pt" space-before="1mm" space-after="1mm" text-align="left"></fo:block>
						            </fo:table-cell>
						          	<fo:table-cell>
										<fo:block font-size="5pt" space-before="1mm" space-after="1mm" text-align="center" font-weight="bold">Total</fo:block>
									</fo:table-cell>
									<fo:table-cell display-align="after" text-align="right" font-weight="bold">
					                	<fo:block font-size="5pt" space-before="1mm" space-after="1mm" text-align="right">
                                          	    <xsl:choose>
						                		<xsl:when test="not(number($impSaldoAntTotal))">
			            							$<xsl:value-of select="$impSaldoAntTotal"/>
			        							</xsl:when>
										        <xsl:when test="contains($impSaldoAntTotal, '.')">
										        	<xsl:variable name="impSaldoAntTotalFormat" select="concat ( substring-before( $impSaldoAntTotal, '.' ) , '.' , substring ( substring-after ( $impSaldoAntTotal, '.' ) , 1 , 2 ) )" />
										            $<xsl:value-of select="format-number($impSaldoAntTotalFormat, '#,###.00')"/>
										        </xsl:when>
										        <xsl:otherwise>
										            $<xsl:value-of select="format-number($impSaldoAntTotal, '#,###.00')"/>
										        </xsl:otherwise>
						    				 </xsl:choose>                                  
                                          	</fo:block>
					             	</fo:table-cell>
					             	<fo:table-cell display-align="after" text-align="right" font-weight="bold">
					                	<fo:block font-size="5pt" space-before="1mm" space-after="1mm" text-align="right">
                                          	    <xsl:choose>
						                		<xsl:when test="not(number($impPagadoTotal))">
			            							$<xsl:value-of select="$impPagadoTotal"/>
			        							</xsl:when>
										        <xsl:when test="contains($impPagadoTotal, '.')">
										        	<xsl:variable name="impPagadoTotalFormat" select="concat ( substring-before( $impPagadoTotal, '.' ) , '.' , substring ( substring-after ( $impPagadoTotal, '.' ) , 1 , 2 ) )" />
										            $<xsl:value-of select="format-number($impPagadoTotalFormat, '#,###.00')"/>
										        </xsl:when>
										        <xsl:otherwise>
										            $<xsl:value-of select="format-number($impPagadoTotal, '#,###.00')"/>
										        </xsl:otherwise>
						    				 </xsl:choose>
                                          	</fo:block>
					             	</fo:table-cell>
					             	<fo:table-cell display-align="after" text-align="right" font-weight="bold">
					                	<fo:block font-size="5pt" space-before="1mm" space-after="1mm" text-align="right">
                                          	    <xsl:choose>
						                		<xsl:when test="not(number($impSaldoInsolutoTotal))">
			            							$<xsl:value-of select="$impSaldoInsolutoTotal"/>
			        							</xsl:when>
										        <xsl:when test="contains($impSaldoInsolutoTotal, '.')">
										        	<xsl:variable name="impSaldoInsolutoTotalFormat" select="concat ( substring-before( $impSaldoInsolutoTotal, '.' ) , '.' , substring ( substring-after ( $impSaldoInsolutoTotal, '.' ) , 1 , 2 ) )" />
										            $<xsl:value-of select="format-number($impSaldoInsolutoTotalFormat, '#,###.00')"/>
										        </xsl:when>
										        <xsl:otherwise>
										            $<xsl:value-of select="format-number($impSaldoInsolutoTotal, '#,###.00')"/>
										        </xsl:otherwise>
						    				 </xsl:choose>
                                          	</fo:block>
					             	</fo:table-cell>
								</fo:table-row>
								
							</fo:table-body>
                        </fo:table>
                        
                    </fo:block>
                    <!--END INFORMACION DEL PAGO-->
                    
                    <!--BLOQUE 6-->
                    <fo:block>
                    	<fo:table space-before="2mm">
							<fo:table-column column-width="35.5mm" />
							<fo:table-column column-width="99.6mm" />
							<fo:table-column column-width="34.7mm" />
							<fo:table-column column-width="20.mm"/>
							
							<fo:table-body>
								<fo:table-row>
									<fo:table-cell display-align="before">
										<fo:block font-size="0" line-height="0">
                                            <!-- aquiva el codigomatricial -->
                                             <fo:external-graphic src="url({$xsltfilePathQR}{$qrCodeFileName})" alignment-baseline="auto" content-height="37mm" content-width="37mm" scaling="uniform" />
                                        </fo:block>
									</fo:table-cell>
									<fo:table-cell display-align="center" number-columns-spanned="3">
										<fo:table>
                                            <fo:table-column />
                                            <fo:table-body>
                                                <fo:table-row height="12px">                                      
				                                    <fo:table-cell display-align="center">
				                                        <fo:block font-weight="bold" font-size="6pt" space-before="5mm" space-after=".5mm">
				                                            Sello Digital del CFDI
				                                        </fo:block>
				                                        <fo:block-container overflow="hidden">
					                                        <fo:block font-size="5pt" space-before=".5mm" space-after="3mm" >
					                                            <xsl:value-of select="./cfdi:Complemento/tfd:TimbreFiscalDigital/@SelloCFD"/>
					                                        </fo:block>
					                                    </fo:block-container>
				                                    </fo:table-cell>
                                                </fo:table-row>     
                                                
                                                <fo:table-row height="6px">
                                                	<fo:table-cell display-align="center" >
                                               			<fo:block font-weight="bold" font-size="3pt" space-before="2.5mm" space-after=".5mm" />
				                                    </fo:table-cell>
                                                </fo:table-row>
                                                <fo:table-row height="12px">
			                                        <fo:table-cell display-align="center">
				                                        <fo:block font-weight="bold" font-size="6pt" space-before="5mm" space-after=".5mm">
				                                            Sello Digital del SAT
				                                        </fo:block>
				                                        
				                                        <fo:block-container overflow="hidden">
				                                        	<fo:block font-size="5pt" space-before=".5mm" space-after="3mm">
				                                            	<xsl:value-of select="./cfdi:Complemento/tfd:TimbreFiscalDigital/@SelloSAT"/>
				                                            </fo:block>
				                                        </fo:block-container>
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
				                                        <fo:block-container overflow="hidden">
					                                        <fo:block font-size="5pt" space-before=".5mm" space-after=".5mm">
					                                            ||<xsl:value-of select="./cfdi:Complemento/tfd:TimbreFiscalDigital/@Version"/>|<xsl:value-of select="./cfdi:Complemento/tfd:TimbreFiscalDigital/@UUID"/>|<xsl:value-of select="./cfdi:Complemento/tfd:TimbreFiscalDigital/@FechaTimbrado"/>|<xsl:value-of select="./cfdi:Complemento/tfd:TimbreFiscalDigital/@SelloSAT"/>|<xsl:value-of select="./cfdi:Complemento/tfd:TimbreFiscalDigital/@NoCertificadoSAT"/>||
					                                        </fo:block>
					                                    </fo:block-container>
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

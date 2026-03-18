<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="es-mx">
<head>
    <title> SODIMAC México - Facturación </title>
<%--       <meta http-equiv="refresh" content="<%=session.getMaxInactiveInterval()%>;url=GeneracionFactura"/> --%>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta content="width=device-width, initial-scale=1, maximum-scale=1" name="viewport">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">

<!--     <link type="text/css" rel="stylesheet" href="./resources/css/bootstrap.min.css" /> -->
    <link type="text/css" rel="stylesheet" href="./resources/css/sweetalert2.min.css" />
    <link type="text/css" rel="stylesheet" href="./resources/css/app.css" />
    <link type="text/css" rel="stylesheet" href="./resources/css/custom.css" />
    <link type="text/css" rel="stylesheet" href="./resources/css/all.css" />
    <link type="text/css" rel="stylesheet" href="./resources/vendors/ionicons/css/ionicons.min.css" />
    <link type="text/css" rel="stylesheet" href="./resources/css/spinner.css" />
    <link type="text/css" rel="stylesheet" href="./resources/js/datepicker/bootstrap-datepicker.standalone.min.css">
    
	<script type="text/javascript" src="./resources/js/jquery.min.js"></script>
    <script type="text/javascript" src="./resources/js/bootstrap.min.js"></script>
    <script type="text/javascript" src="./resources/js/sweetalert2.min.js"></script>
    <script type="text/javascript" charset="UTF-8" src="./resources/js/jquery.mask.min.js"></script>
    <script type="text/javascript" src="./resources/js/datepicker/bootstrap-datepicker.min.js"></script>
    <script type="text/javascript" src="./resources/js/datepicker/bootstrap-datepicker.es.min.js"></script>
    
    <link type="image/ico" rel="icon" href="resources/img/favicon.ico" />
    
</head>

<body>

<div id="spinner" class="hide loading">
</div>

    <header class="header">
        <nav class="navbar navbar-static-top">
            <div class="nav_header">
                <div class="container px-0">
                    <a href="" class="logo navbar-brand float-left text-white text-center">
                        <img src="resources/img/header.jpg" alt="logo" class="img-fluid navbar_brand_img">
                    </a>
                    <div class="top_right_nav">
                        <div class="float-right">
                            <!--start admin setting section-->
                            <h2 class="main-H">Servicio de facturación</h2>
                            <!--end admin setting section-->
                        </div>
                    </div>
                </div>
            </div>
        </nav>
    </header>

<!-- ↓MAIN tabs↓ -->
<div class="container encapsulate">
    <!-- ↓Nav↓ -->
    <ul class="nav nav-justified emulate-tabs">
        <li class="nav-item">
            <a class="nav-link" href="/facturacion">Generación de timbrado</a>
        </li>
        <li class="nav-item">
            <a class="nav-link" href="/facturacion/ConsultarFactura">Consulta de timbre</a>
        </li>
        <li class="nav-item">
            <a class="nav-link active" href="#">Consulta de múltiples timbres</a>
        </li>
    </ul>

    <!-- ↓Tercera Tab Principal = contenido↓ (consulta de múltiples timbres)↓ -->
	<div id="consultar-multiples-facturas">
	    <div class="container">
	        <div class="row">
	            <div class="col-12">
	                <div class="card">
	                    <div class="card-body">
	                        <div class="row m-b-0">
	                            <div class="col-12 col-sm-4 col-md-4">
	                                <div class="form-group m-b-0">
	                                    <input id="rfcInput_multiples_factura" value="" type="text" class="form-control text-uppercase" maxlength="13" onkeypress="onkeypressverifica(event);verificaTabRFC_consulta_factura_multiples(event);" autocomplete="off"  tabindex="1">

	                                    <label for="rfcInput" class="control-label">
	                                        <span class="text-danger">*</span>RFC:
	                                    </label><!-- ↓Begins validation↓ -->
	                                    <div>
	                                        <div class="col-12" id="rfcInput_multiples_factura_validate">
	                                        </div>
	                                    </div><!-- ↑ENDS validation↑ -->
	                                    <div class="txt-assist">
	                                        Ejemplo: EJEM880326 XXX
	                                    </div>
	                                </div><!-- /.form-group -->
	                            </div>

	                            <div class="col-12 col-sm-8 col-md-8 m-b-25" id="date-container">
	                                <div class="input-daterange input-group" id="datepicker">
	                                    <div class="form-group w-100 mr-3">
	                                        <input type="text" id="dateDesde" class="input-sm form-control w-100 full" autocomplete="off" name="start" 
	                                        placeholder="dd/mm/aaaa" data-date-format="dd/mm/yyyy" tabindex="2" onblur="isValidDate('dateDesde', 'date_desde_validate', 'dateHasta', 'date_hasta_validate', 1)">
	                                        <label for="·" class="control-label">
	                                            <span class="text-danger">*</span>Desde:
	                                        </label>
	                                        <div  id="date_desde_validate">
	                                        </div>
	                                    </div>
	                                    <div class="form-group w-100">
	                                        <input type="text" id="dateHasta" class="input-sm form-control w-100 full" autocomplete="off" name="end" data-date-format="dd/mm/yyyy"
	                                         tabindex="3" onblur="isValidDate('dateDesde', 'date_desde_validate','dateHasta', 'date_hasta_validate', 2)">
	                                        <label for="·" class="control-label">
	                                            <span class="text-danger">*</span>Hasta:
	                                        </label>
	                                        <div  id="date_hasta_validate">
	                                        </div>
	                                    </div>
	                                </div>
	                                <div class="txt-assist m-neutralize-form-group2">
	                                    Rango de fechas entre los que se buscarán sus facturas <strong>( dd/mm/yyyy )</strong>
	                                </div>
	                            </div>
	                        </div><!-- /.row -->

	                        <div class="row">
	                            <div class="col-12">
	                                <h4></h4>
	                            </div>
	                            <div class="col-12 col-sm-4 col-md-4 m-b-0 m-t-3">
	                            	<div class="form-group m-b-0">
	                                    <input id="email_multiples_factura" value="" type="text" maxlength="50"
	                                    class="form-control" autocomplete="off" tabindex="4"   onblur="releaseEventMailMultipleEmpty();">
	                                    <label for="email_multiples_factura" class="control-label">
	                                        <span class="text-danger">*</span>Correo Electrónico:
	                                    </label><!-- ↓Begins validation↓ -->
	                                    <div>
	                                        <div class="col-12" id="email_multiples_factura_validate">
	                                        </div>
	                                    </div><!-- ↑ENDS validation↑ -->
	                                    <div class="txt-assist">
	                                        Ingrese su correo con el cual facturó
	                                    </div>
	                                </div>
	                            </div>
	                            <div class="col-12 col-sm-4 col-md-4">
	                            	<button id="btnGenerarToken" onclick="btnGenerarToken()" type="button" class="btn btn-secondary btn-block">
	                                    Generar token
	                                </button>
	                                <div class="txt-assist">
	                                    Enviará un token a su correo
	                                </div>
	                            </div>
	                            <div class="col-12 col-sm-4 col-md-4 m-t-3">
	                            	<div class="form-group m-b-0">
	                                    <input id="token_multiples_factura" type="text" maxlength="50"
	                                    class="form-control" autocomplete="off" tabindex="4" onkeypress="tokenValidarExpReg(event)"  onblur="validaCamposVacios('token_multiples_factura', '#token_multiples_factura_validate', 'text')">
	                                    <label for="token_multiples_factura" class="control-label">
	                                        <span class="text-danger">*</span>Token:
	                                    </label><!-- ↓Begins validation↓ -->
	                                    <div>
	                                        <div class="col-12" id="token_multiples_factura_validate">
	                                        </div>
	                                    </div><!-- ↑ENDS validation↑ -->
	                                    <div class="txt-assist">
	                                        Ingrese el token que llegó a su correo después de "Generar token"
	                                    </div>
	                                </div>
	                            </div>
	                        </div><!-- /.row -->
	                        
                            <div class="row m-t-0 m-b-0 ">
                                <div class="col-10 text-left ">
                                	<span class="title-2 m-b-0 "><span class="text-danger" style="font-size: 1.5rem; vertical-align: middle;">*</span> Campos obligatorios</span>
                                </div>
                            </div>
	                        
	                        <div class="row text-center">
		                        <div class="col-12 col-sm-4 col-md-4 m-t-3">
		                        </div>
	                        	 <div class="col-12 col-sm-4 col-md-4 m-t-3">
	                                <button id="btnBuscarFacturas" type="button" class="btn btn-primary btn-block" onclick="validateSearchFactureMultiple()" tabindex="5">
	                                    Buscar timbres
	                                </button>
	                            </div>
		                        <div id="messages_consultas_multiples" style="margin-left: 20px;">
		                        </div>
	                        </div>
							<div class="tab-pane" id="tab34">

                                 <div class="text-center">
                                 	<div id="idSpinner" class="spinner-border d-none"></div>
                                 </div>
                                
                                <div class="tab-pane mt-5" id="tab34Content"></div>

                            </div><!-- /#tab34 -->

	                    </div><!-- /.card-body -->
	                </div><!-- /.card -->
	            </div><!-- /.col-12 -->
	        </div><!-- /.row -->
	    </div><!-- /.container -->
	</div><!-- ↑TERMINA - Tercera Tab Principal = contenido↓ (consulta de múltiples timbres)↑ -->
</div><!-- end MAIN tabs -->

<input type="hidden" id="hdnExpresionRegularRfcCaracteres" value=""/>
<input type="hidden" id="hdnExpresionRegularRfc" value=""/>
<input type="hidden" id="hdnExpresionRegularToken" value=""/>
<input type="hidden" id="hdnExpresionRegularEmail" value=""/>

<div id="divFooter">
    <jsp:include page="footer.jsp"/>
    
</div>

<script type="text/javascript" charset="UTF-8" src="./resources/js/ajax.js"></script>
<script type="text/javascript" charset="UTF-8" src="./resources/js/factura/factura-multiple.js"></script>
<script type="text/javascript" charset="UTF-8" src="./resources/js/variables.js"></script>

</body>
</html>

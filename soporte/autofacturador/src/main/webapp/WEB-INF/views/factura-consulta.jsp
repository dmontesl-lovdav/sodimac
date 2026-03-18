<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="es-mx">
<head>
    <title> SODIMAC México - Facturación </title>
<%--         <meta http-equiv="refresh" content="<%=session.getMaxInactiveInterval()%>;url=GeneracionFactura"/> --%>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">

    <link type="text/css" rel="stylesheet" href="./resources/css/bootstrap.min.css" />
    <link type="text/css" rel="stylesheet" href="./resources/css/sweetalert2.min.css" />
    <link type="text/css" rel="stylesheet" href="./resources/css/app.css" />
    <link type="text/css" rel="stylesheet" href="./resources/css/custom.css" />
    <link type="text/css" rel="stylesheet" href="./resources/css/all.css" />
    <link rel="stylesheet" type="text/css" href="./resources/vendors/ionicons/css/ionicons.min.css" />
    
	<script type="text/javascript" src="./resources/js/jquery.min.js"></script>
    <script type="text/javascript" src="./resources/js/bootstrap.min.js"></script>
    <script type="text/javascript" src="./resources/js/sweetalert2.min.js"></script>

	<link type="image/ico" rel="icon" href="resources/img/favicon.ico" />
</head>
<body>
    
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

<!-- âMAIN tabsâ -->
<div class="container encapsulate">
    <!-- âNavâ -->
    <ul class="nav nav-justified emulate-tabs">
        <li class="nav-item">
            <a class="nav-link" href="/facturacion">Generación de timbrado</a>
        </li>
        <li class="nav-item">
            <a class="nav-link active" href="#">Consulta de timbre</a>
        </li>
        <li class="nav-item">
            <a class="nav-link" href="/facturacion/ConsultaMultiple">Consulta de múltiples timbres</a>
        </li>
    </ul>

    <!-- âSegunda Tab Principal = contenidoâ (consulta de una sola factura)â -->
	<div id="consultar-1factura">
	    <div class="container">
	        <div class="row">
	            <div class="col-12">
	                <div class="card">
	                    <div class="card-body">
	                        <form class="row">
	                            <div class="col-12 col-sm-3 col-md-3">
	                                <div class="form-group">
	                                    <input id="rfcInput_consulta_factura" type="text" class="form-control text-uppercase" maxlength="13" onkeypress="onkeypressverifica(event);verificaTabRFC_consulta_factura(event);" autocomplete="off" tabindex="1">

	                                    <label for="rfcInput_consulta_factura" class="control-label">
	                                        <span class="text-danger">*</span>RFC:
	                                    </label><!-- âBegins validationâ -->
	                                    <div>
	                                        <div class="col-12" id="rfc_validation_consulta_factura">
	                                        </div>
	                                    </div><!-- âENDS validationâ -->
	                                    <div class="txt-assist">
	                                        El formato correcto es: EJEM880326 XXX
	                                    </div>
	                                </div><!-- /.form-group -->
	                            </div>

	                            <div class="col-12 col-sm-3 col-md-3">
	                                <div class="form-group">
	                                    <input id="ticketHolder_consulta_factura" type="text" class="form-control" onkeypress="onkeypressverifica(event);" onkeydown="verificaTabTicket(event, 'ticketHolder_consulta_factura', 'messajeTicketCompra_consulta_factura', '1')" autocomplete="off" maxlength="20" tabindex="2">

	                                    <label for="ticketHolder_consulta_factura" class="control-label">
	                                        <span class="text-danger">*</span>Ticket u Orden de compra:
	                                    </label><!-- âBegins validationâ -->
	                                    <div>
	                                        <div class="col-12" id="messajeTicketCompra_consulta_factura">
	                                        </div>
	                                    </div><!-- âENDS validationâ -->
	                                    <div class="txt-assist">
	                                        19 caracteres para ticket y 10 para orden de compra
	                                    </div>
	                                </div><!-- /form-group -->
	                            </div>

	                            <!-- â Comienza Captcha â -->
	                            <div id="captcha" class="col-12 col-sm-6 col-md-6">
	                                <div class="captcha-from row">
	                                    <div class="col-3 pr-2">
	                                        <div class="form-group">
	                                            <!-- input captcha -->
	                                            <div class="captcha-code">
	                                                <div class="code">
	                                                    <div class="dynamic-code"></div>
	                                                </div>
	                                            </div>
	                                        </div>
	                                    </div>
	                                    <div class="col-1 px-0">
	                                        <div class="captcha-reload" data-toggle="tooltip" title="" data-original-title="Recargar texto Captcha">
	                                            <i class="ti-reload"></i>
	                                        </div>
	                                    </div>
	                                    <div class="col-4">
	                                        <div class="captcha-input form-group">
	                                            <input type="text" class="form-control" id="captcha-input" required autocomplete="off" tabindex="3">
	                                            <label for="captcha-input" class="control-label">
	                                                <span class="text-danger">*</span>Ingrese captcha:
	                                            </label>
	                                            <span id="errCaptcha"></span>
	                                        </div>
	                                    </div>
	                                    <div class="col-4">
	                                        <button id="btnBuscarFactura" type="button" class="btn btn-primary btn-block" onclick="buscarFactura()" disabled="disabled">Buscar timbre</button>
	                                    </div>
	                                    <div class="txt-assist m-neutralize-form-group">
	                                        Por favor ingrese el código captcha
	                                    </div>
	                                </div>
	                            </div><!-- #/main -->
	                            <!-- â Termina Captcha â -->

	                        </form>
	                        
                            <div class="tab-pane mt-5" id="tab33">

                                 <div class="text-center">
                                 	<div id="idSpinner" class="spinner-border d-none"></div>
                                 </div>
                                
                                	<div class="tab-pane mt-5" id="tab33Content"></div>

                            </div><!-- /#tab33 -->
	                        
	                    </div><!-- /.card-body -->
	                </div><!-- /.card -->
	            </div><!-- /.col-12 -->
	        </div><!-- /.row -->
	    </div><!-- /.container -->
	</div>
	<!-- âTERMINA - Segunda Tab Principal = contenidoâ (consulta de una sola factura)â -->

 
</div>
<!-- end MAIN tabs -->

<input type="hidden" id="hdnExpresionRegularRfcCaracteres" value=""/>
<input type="hidden" id="hdnExpresionRegularRfc" value=""/>
<input type="hidden" id="hdnExpresionRegularEmail" value=""/>

<div id="divFooter">
    <jsp:include page="footer.jsp"/>
</div>

<script type="text/javascript" charset="UTF-8" src="./resources/js/ajax.js"></script>
<script type="text/javascript" charset="UTF-8" src="./resources/js/factura-consulta.js"></script>
<script type="text/javascript" charset="UTF-8" src="./resources/js/variables.js"></script>
<script type="text/javascript" charset="UTF-8" src="./resources/js/captcha.js"></script>

</body>
</html>

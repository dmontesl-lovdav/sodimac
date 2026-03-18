<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es-mx">
<head>
    <title> SODIMAC México - Facturación </title>
<%--     <meta http-equiv="refresh" content="<%=session.getMaxInactiveInterval()%>;url=GeneracionFactura"/> --%>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    
    <link type="text/css" rel="stylesheet" href="./resources/css/bootstrap.min.css" />
    <link type="text/css" rel="stylesheet" href="./resources/css/sweetalert2.min.css" />
    <link type="text/css" rel="stylesheet" href="./resources/css/app.css" />
    <link type="text/css" rel="stylesheet" href="./resources/css/custom.css" />
    <link type="text/css" rel="stylesheet" href="./resources/css/all.css" />
    <link type="text/css" rel="stylesheet" href="./resources/vendors/ionicons/css/ionicons.min.css" />
    <link type="text/css" rel="stylesheet" href="./resources/css/spinner.css" />
    <link type="text/css" rel="stylesheet" href="./resources/css/fontawesome/css/all.css" />
    
    <script type="text/javascript" src="./resources/js/jquery.min.js"></script>
    <script type="text/javascript" src="./resources/js/popper.min.js"></script>
    <script type="text/javascript" src="./resources/js/bootstrap.min.js"></script>
    <script type="text/javascript" src="./resources/js/sweetalert2.min.js"></script>
    <script type="text/javascript" src="./resources/js/jquery.maskMoney.min.js"></script>
        
    <link type="image/ico" rel="icon" href="resources/img/favicon.ico">
</head>

<body onload="document.getElementById('ticketHolder').focus();">

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
            <a class="nav-link active" href="#">Generación de timbrado</a>
        </li>
        <li class="nav-item">
            <a class="nav-link" href="/facturacion/ConsultarFactura">Consulta de timbre</a>
        </li>
        <li class="nav-item">
            <a class="nav-link" href="/facturacion/ConsultaMultiple">Consulta de múltiples timbres</a>
        </li>
    </ul>

    <!-- ↓Primera Tab Principal = contenido (generación de timbrado)↓ -->
    <div id="generar_factura">
        <div class="container">
            <div class="row">
                <div class="col-12">
                    <div class="card">
                        <div class="card-body">
                            <!-- BEGIN FORM WIZARD WITH VALIDATION -->
                            <form id="commentForm" method="post" action="#" class="validate">
                                <div id="rootwizard">
                                    <div class="row rootwizard---">
                                        <div class="col-8 stepper-line position-absolute">
                                            <hr size="60" style="display:none;">
                                        </div>
                                        <div class="col-12">
                                            <ul class="row nav nav-pills">
                                                <li class="col-12 col-sm-4 col-lg-4 nav-item">
                                                    <a class="nav-link" href="#tab31" data-toggle="tab">
														<div class="text-center">
															<span class="userprofile_tab1 text-c    enter">1</span>
															<p id="subticket" class="rootwizard-subt">Datos de su compra</p>
														</div>
                                                    </a>
                                                </li>
                                                <li class="col-12 col-sm-4 col-lg-4 nav-item">
                                                    <a class="nav-link text-info" href="#tab32" data-toggle="tab">
                                                        <div class="text-center">
                                                            <span class="userprofile_tab2">2</span>
                                                            <p id="subticket" class="rootwizard-subt">Sus datos Fiscales</p>
                                                        </div>
                                                    </a>
                                                </li>
                                                <li class="col-12 col-sm-4 col-lg-4 nav-item">
                                                    <a class="nav-link text-info" href="#tab33" data-toggle="tab">
                                                        <div class="text-center">
                                                            <span class="userprofile_tab3">3</span>
                                                            <p id="subticket" class="rootwizard-subt">Su documento</p>
                                                        </div>
                                                    </a>
                                                </li>
                                            </ul>
                                        </div>
                                    </div><!-- /.row rootwizard--- m-b-25 -->

                                    <div class="tab-content">
                                        <div class="tab-pane" id="tab31">
                                            <div class="row" id="messagesOrdenTicket">
                                                                                        
                                                <div class="col-md-7 m-t-10">

                                                    <div class="row">
                                                        <div class="col-2 text-right">
                                                        	<img src="resources/img/scaner.jpg" height="42" width="42" alt="Escaner" class="img-fluid navbar_brand_img">
                                                        </div>
                                                        <div class="col-10 text-left" style="padding: 0px;">
                                                            <span class="title-1">Favor de digitar el número y monto total del ticket o escanea el código de barras</span>
                                                            <div id=divMaximoTickets></div>
                                                        </div>
                                                    </div>
                                                         
                                                    <br/>                                     
                                                
                                                    <div class="row form-group m-b-0">
														<div class="col-sm-5">
	                                                        <input id="ticketHolder" value="2494370530" type="text" class="form-control" onkeypress="onkeypressverifica(event);" onkeydown="verificaTabTicket(event,  'ticketHolder', 'messajeTicketCompra', '1')" autocomplete="off" maxlength="20" tabindex="1">
	
	                                                        <label for="ticketHolder" class="control-label" style="left: 1rem;">
	                                                            <span class="text-danger"> *</span>Ticket u orden de compra:
	                                                        </label>
	                                                        <div><!-- Begins validation -->
	                                                            <div class="col-12" id="messajeTicketCompra">
	                                                            </div>
	                                                        </div><!-- ENDS validation -->
	                                                        <div class="txt-assist">
	                                                            19 caracteres para ticket y 10 para orden de compra
	                                                        </div>
                                                         </div>
                                                     
                                                         <div class="col-sm-4">
													      	<input id="ticketAmount" value="" type="text" class="form-control" onkeydown="verificaAmountTicket(event,  'ticketHolder', 'messajeTicketCompra', '1')" autocomplete="off" maxlength="11" tabindex="2">
	
	                                                        <label for="ticketAmount" class="control-label" style="left: 1rem; font-size: 0.8rem;">
	                                                            <span class="text-danger" style="font-size: 1.5rem; ">*</span>Total ($)
	                                                        </label>
	                                                        <div><!-- Begins validation -->
	                                                            <div class="col-12" id="messajeAmount" style="width:150%">
	                                                            </div>
	                                                        </div><!-- ENDS validation -->
	                                                        <div class="txt-assist">
	                                                            Importe total del ticket
	                                                        </div>
													    </div>
													    <div class="col-sm-3 text-center">                                                       	

															<div id="divBotonAgregar"></div>
	                                                        
													    </div>
                                                    </div><!-- /form-group -->
                                                  
                                                    <div class="row m-b-0 ">
                                                        <div class="col-10 text-left ">
                                                        	<span class="title-2 m-b-0 "><span class="text-danger" style="font-size: 1.5rem; vertical-align: middle;">*</span> Campos obligatorios</span>
                                                        </div>
                                                    </div>
                                                    
		                                            <div id="divSpinnerTicker" class="text-center" style="display:none">
		                                            	<div id="idSpinnerTicker" class="spinner-border"></div>
		                                            </div>
		                                            <div class="row form-group m-b-0">
		                                            	<div class="col-sm-6"></div>
		                                            	<div class="col-sm-3"></div>
		                                            	<div class="col-sm-3 text-center">
			                                            	<ul class="pager wizard pager_a_cursor_pointer">
	                                                            <li class="next invalidar" id="invalidar">
	                                                                <a onclick="eliminacionClases(event)" class="btn btn-primary  disabled" id="btnFirstNext" style="float:left;" tabindex="4">
	                                                                   &nbsp; &nbsp;  Siguiente  &nbsp; &nbsp;
	                                                                </a>
	                                                            </li>
	                                                        </ul>
		                                            	</div>
		                                            </div>

                                                     <div id="newItemsTicket"></div>
                                                     <div id="newItemsTickettmp">
                                                     </div>
                                                    <div id="itemsTicket"></div>
                                                    <br>
                                                </div><!-- /col-md-7 m-t-68 -->

                                                <!-- TicketTabs -->
                                                <div class="col-sm-12 col-md-5 m-t-10">
                                                    <div class="img-sample">
                                                        <div class="row text-center">
                                                            <div class="col-12 mb-2">
                                                                <span class="link-ticket">Ticket</span>
                                                                <span class="link-purchase">Orden de compra</span>
                                                            </div>
                                                        </div>

                                                        <div class="ticket">
                                                            <div id="ticket_graph" class="container text-center">
                                                                <h4 class="mb-3 fnt-wn">Localice su Número de ticket</h4>
                                                                <div class="text-center m-b-30">
                                                                    <div class="ticket-bg position-relative mx-auto">
                                                                        <div class="ticket-bg-base mx-auto">
                                                                            <img src="resources/img/fqoiyMZ.png" class="img-fluid img-ticket" alt="info">
                                                                        </div>
                                                                        <div class="yellow" data-toggle="tooltip" title="No. de ticket, 19 dígitos"></div>
                                                                    
                                                                        	<div class="monto-pulse d-none d-lg-block " data-toggle="tooltip" title="Monto"></div>
                                                                       
                                                                        
                                                                    </div>
                                                                </div>
                                                            </div>
                                                        </div><!-- .ticket -->

                                                        <div class="purchase">
                                                            <div id="ticket_graph" class="container text-center">
                                                                <h4 class="mb-3 fnt-wn">Localice la orden de compra</h4>
                                                                <div class="text-center m-b-30 copy__">
                                                                    <div class="ticket-bg position-relative mx-auto">
                                                                        <div class="ticket-bg-base mx-auto">
                                                                            <img src="resources/img/fqoiyMZ1.png" class="img-fluid img-ticket" alt="info">
                                                                        </div>
                                                                        <div class="yellow y-two" data-toggle="tooltip" title="Número de orden de compra"></div>
                                                                        <div class="monto-pulse monto-pulse-order d-none d-lg-block" data-toggle="tooltip" title="Monto"></div>
                                                                    </div>
                                                                </div>
                                                            </div>
                                                        </div><!-- .purchase -->

                                                        <div class="d-none_">
                                                            <div class="ticket-control-prev">
                                                                <span class="ticket-control-prev-icon">
                                                                    <i class="ti-angle-left"></i>
                                                                </span>
                                                            </div>
                                                            <div class="ticket-control-next">
                                                                <span class="ticket-control-prev-icon">
                                                                    <i class="ti-angle-right"></i>
                                                                </span>
                                                            </div>
                                                        </div>
                                                    </div><!-- img-sample -->
                                                </div><!-- TicketTabs -->
                                            </div><!-- /.row #messagesOrdenTicket -->
                                        </div><!-- /#tab31 -->

										
                                        <div class="tab-pane" id="tab32">
                                            <div class="col-12 col-md-10 col-lg-8 mx-auto m-t-40">
                                            	<div id="divMensajeVersion40" class="row">
                                                    
                                                    	<div class="col-12 text-left" style="padding: 0px;">
                                                            <span class="title-3">Importante:</span>
                                                            <span class="title-3-1">Por disposición fiscal se ha migrado a la versión de timbrado 4.0, es necesario validar sus datos conforme a la constancia de situación fiscal</span>
                                                            
                                                        </div>
                                                </div>
                                                <div id="divMensajeNC33" class="row">
                                                    
                                                    	<div class="col-12 text-left" style="padding: 0px;">
                                                            <span class="title-4">Importante:</span>
                                                            <span class="title-4-1">La factura origen fue timbrada en la versión 3.3, por disposición fiscal ya no es posible generar la Nota de Crédito</span>
                                                            
                                                        </div>
                                                </div>
                                            
                                                <div class="row">
                                                    <div class="col-12 col-sm-8 col-md-9">
                                                    	
                                                    	<div class="col-12 text-left" style="padding: 0px;">
                                                            <span class="title-1">Capture su RFC y oprima el botón Buscar RFC</span>
                                                        </div>
                                                        <br><br>
                                                    
                                                    
                                                        <div class="form-group">
                                                            <input id="rfcInput" name="" placeholder="" type="text" class="form-control text-uppercase" maxlength="13" value="PRO070928KG0">
                                                            <label for="rfcInput" class="control-label">
                                                                <span class="text-danger">*</span>RFC:</label>
                                                            <div><!-- Begins validation -->
                                                                <div class="col-12" id="rfc_validation">
                                                                </div>
                                                            </div><!-- ENDS validation -->
                                                            <div class="txt-assist">
                                                                El formato correcto es: EJEM880326 XXX
                                                            </div>
                                                        </div><!-- /.form-group -->
                                                    </div><!-- /.col-12 .col-sm-8 .col-md-10 -->

                                                    <div class="col-12 col-sm-4 col-md-3 m-t-50">
                                                        <button id="btnBuscarRFC" type="button" class="btn btn-primary btn-block btn-rfc mt-0" onclick="validaRFC()">                                                       											
                                                            Buscar RFC
                                                        </button>
                                                    </div>
                                                </div>
                                            </div>
                                            
                                            <div class="col-12 col-md-10 col-lg-8 mx-auto">
                                             <div class="form-group" id="datosEmpresa">
				    					    </div>
                                            
                                            </div><!-- /.col-12 .col-md-10 .col-lg-8 .mx-auto -->

                                            <div class="col-md-8 mx-auto">
                                                <div class="form-group" id="datosEmpresa">
                                                </div>
                                            </div>
                                            <div id="btnActualizar">
                                            </div>
                                            <div class="col-md-2">
                                            </div><!-- ↓Botones final de tarjeta↓ -->

                                            <div class="col-12 col-md-10 col-lg-8 mx-auto">
                                            	
                                                <ul class="pager wizard pager_a_cursor_pointer ">
                                                	<button id="btnGenerarFactura" class="btn btn-primary float-right" disabled onclick="btnGenerarFactura_onClick()">Generar timbre</button>
                                                    <li class="previous" id="previoFactura">
                                                        <a class="btn btn-link float-right mr-5">
                                                            Atrás
                                                        </a>
                                                    </li>
                                                </ul>
                                            </div>

<div class="modal fade" tabindex="-1" role="dialog" id="divModalConfirm">
    <div class="modal-dialog modal-dialog-centered" role="document" style="width:350px">
        <div class="modal-content">
<!--             <div class="modal-header"> -->
<!--                 <h5 class="titulo-left-modal">Facturación Sodimac</h5> -->
<!--             </div> -->
            <div class="modal-body">
            	<div id="divTickets" class="text-center"></div>
            	<br>
            	<div class="row">
           			<div class="col-md-1">
           				
           			</div>
           			<div class="col-md-5">
			            <ul class="pager wizard pager_a_cursor_pointer ">
			            <li class="next" id="invalidarFactura">
	                        <a id="btnConfirmarSi" onclick="btnConfirmarSi_onClick()" class="swal2-confirm swal2-styled" style="display: inline-block; background-color: rgb(48, 133, 214); border-left-color: rgb(48, 133, 214); border-right-color: rgb(48, 133, 214); "><font color="white">Generar</font></a>
			            </li>
			            </ul>
           			</div>
           			<div class="col-md-5">
           				<button id="btnConfirmarNo" onclick="btnConfirmarNo_onClick()" class="swal2-cancel swal2-styled" style="display: inline-block; background-color: rgb(221, 51, 51);">Cancelar</button>
           			</div>
            	</div>
            </div>
        </div>
    </div>
</div>
                                        </div><!-- /#tab32 -->

                                        <div class="tab-pane mt-5" id="tab33">
                                            
                                            	<div class="tab-pane mt-5" id="tab33Content"></div>
                                            	
												<div class="row text-right mt-4 float-right" >
												    <div class="col-12">
												        <button id="btnFinalizar" style="display:none" type="button" class="btn btn-primary" onClick="javascript: window.location.pathname = '/facturacion'">Finalizar</button>
												    </div>
												</div>

                                        </div><!-- /#tab33 -->
                                    </div><!-- /.tab-content.m-t-20 -->

                                </div><!-- /#rootwizard -->
                            </form><!-- ENDS FORM WIZARD WITH VALIDATION -->
                        </div><!-- /.card-body -->
                    </div><!-- /.card -->
                </div><!-- /.col-12 -->
            </div><!-- /.row -->
        </div><!-- /.container -->
    </div><!-- ↑TERMINA - Primera Tab Principal = contenido (generación de timbrado)↑ -->
    
        
    
</div>
<!-- end MAIN tabs -->

<input type="hidden" id="hdnExpresionRegularRfcCaracteres" value=""/>
<input type="hidden" id="hdnExpresionRegularRfc" value=""/>
<input type="hidden" id="hdnExpresionRegularEmail" value=""/>
<input type="hidden" id="hdnExpresionRegularRZ" value=""/>
<input type="hidden" id="hdnExpresionRegularObra" value=""/>
<input type="hidden" id="hdnExpresionRegularResponsableObra" value=""/>
<input type="hidden" id="hdnRFCPublicoGeneral" value=""/>
<input type="hidden" id="hdnRFCPublicoGeneralMensaje" value=""/>

<div id="divFooter">
    <jsp:include page="footer.jsp"/>
</div>

<script type="text/javascript" charset="UTF-8" src="./resources/js/ajax.js"></script>
<script type="text/javascript" charset="UTF-8" src="./resources/js/factura-form.js"></script>
<script type="text/javascript" charset="UTF-8" src="./resources/js/variables.js"></script>
<script type="text/javascript" charset="UTF-8" src="./resources/js/all.js"></script>
<script type="text/javascript" charset="UTF-8" src="./resources/js/validacion.js"></script>
<script type="text/javascript" charset="UTF-8" src="./resources/js/steps.min.js"></script>
<script type="text/javascript" charset="UTF-8" src="./resources/js/valida.min.js"></script>
<script type="text/javascript" charset="UTF-8" src="./resources/js/forms.js"></script>
<script type="text/javascript" charset="UTF-8" src="./resources/js/form.js"></script>

</body>
</html>

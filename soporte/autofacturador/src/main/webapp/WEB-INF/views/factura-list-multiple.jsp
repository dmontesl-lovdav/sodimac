<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"  %>
<!DOCTYPE html>
<html lang="es-mx">
<head>
    <title> Sodimac </title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">

<!--     <link type="text/css" rel="stylesheet" href="./resources/css/bootstrap.min.css" /> -->
    <link type="text/css" rel="stylesheet" href="./resources/css/app.css" />
    <link type="text/css" rel="stylesheet" href="./resources/css/custom.css" />
    <link type="text/css" rel="stylesheet" href="./resources/css/all.css" />
    <link rel="stylesheet" type="text/css" href="./resources/vendors/ionicons/css/ionicons.min.css" />
    
    <script type="text/javascript" src="./resources/js/jquery.min.js"></script>
    <script type="text/javascript" src="./resources/js/popper.min.js"></script>
    <script type="text/javascript" src="./resources/js/bootstrap.min.js"></script>

	<link type="image/ico" rel="icon" href="resources/img/favicon.ico" />
</head>
<body>
<div class="row mt-3">
    <div class="col-12">
		<h4 id="responselabel"></h4>  
    </div>
</div>
	<c:choose>
		<c:when test="${fn:length(dClientesMultiple) gt 0}">
			<script>   $('#responselabel').text("Resultado de su búsqueda"); </script>
		</c:when>
		<c:otherwise>
			<script> $('#responselabel').text("Los datos proporcionados no arrojan resultados, favor de verificar e intentar nuevamente");</script>
		</c:otherwise>
	</c:choose>


<div class="table-responsive">
	<c:set var="clearMultipleObj" value="${fn:substringBefore(param.clearMultipleObj, '.jsp')}"/>
<%-- 		<c:out value="${clearMultipleObj}"/>  --%>
    <c:if test="${fn:length(dClientesMultiple) gt 0}">
		    <script type="text/javascript">
				if (${clearMultipleObj}) {
					clearMultipleObj();
				}
		    </script>
	    <c:forEach var="item" items="${dClientesMultiple}">
	    	<script type="text/javascript">
		    	if (${clearMultipleObj}) {
		    		createClientesMultipleParent('${item.uuid}', '${item.nombreArchivo}');
		    	}
	    	</script>
	    	
	    </c:forEach>
    </c:if>
    <!--  <p>The length of the companies collection is : ${fn:length(dClientesMultiple)}</p> -->
	<c:if test="${fn:length(dClientesMultiple) gt 0}">
	  	<table id="data" class="table table-striped" >
		    <thead class="table-header">
		        <tr>
		            <th style="width: 4%;">
		            	<c:if test="${fn:length(dClientesMultiple) gt 1}">
			                <label class="custom-control custom-checkbox mb-0">
			                    <input id="selectall" name="selectall" ${dSelectAll} type="checkbox" onClick="SetCheckUncheck('All')"  class="custom-control-input" name="default_checkbox">
			                    <span class="custom-control-indicator custom_checkbox_primary"></span>
			                    <span class="custom-control-description text-primary"></span>
			                </label>
		                </c:if>
		            </th>
		            <th style="width: 14%;">Documento</th>
		            <th style="width: 14%;">Ticket u Orden</th>
		            <th style="width: 14%;">Razón Social</th>
		            <th style="width: 14%;">Fecha timbrado</th>
		            <th style="width: 10%;">Estatus</th>
		            <th style="width: 30%;" class="text-center">Acciones</th>
		        </tr>
		    </thead>
	   		 <tbody>
			<!-- loop over and print our customers -->
			<c:forEach var="tempCustomer" items="${dClientesMultiple}">
	    
	        <tr>
	            <td>
	            <c:if test="${fn:length(dClientesMultiple) gt 1}">
	                <label class="custom-control custom-checkbox mb-0">
	                    <input id="${tempCustomer.nombreArchivo}" ${tempCustomer.checked} type="checkbox" onClick="SetCheckUncheck('${tempCustomer.uuid}')" class="case custom-control-input" name="default_checkbox[]">
	                    <span class="custom-control-indicator custom_checkbox_primary"></span>
	                    <span class="custom-control-description text-primary"></span>
	                </label>
	            </c:if>
	            </td>
	            <td>${tempCustomer.uuid}</td>
	            <td>${tempCustomer.ticket}</td>
	            <td>${tempCustomer.razonSocial}</td>
	            <td>${tempCustomer.fechaTimbrado}</td>
	            <td>${tempCustomer.nombreEstatus}</td>
	            <td class="py-0 pr-0" style="padding: 0px;">
	                <div id="actions" style="display: ${tempCustomer.uuid}none" class="container-fluid">
	                    <button type="button" onClick="obtenerCorreo ('${tempCustomer.uuid}', '${tempCustomer.rfc}')"
	                            class="btn btn-secondary no-shadow no-line float-right p-0" data-toggle="modal" data-target="responsive2">
	                        <div class="d-block-btn" data-toggle="tooltip" title="Reenviar">
	                        	<i class="ti-email"></i>
	                        </div>
	                    </button>
	                    
	                    <c:if test="${dDescargar == 'true'}">
		                    <spring:url value="/DescargarArchivo/${tempCustomer.nombreArchivo}" var="downloadUrl"></spring:url>
		                    <a href="${downloadUrl}" 
		                       class="btn btn-secondary no-shadow no-line float-right p-0" id="icons">
		                       <div class="d-block-btn" data-toggle="tooltip" title="Descargar">
		                       		<i class="ti-download"></i>
		                       </div>
		                    </a>
	                    </c:if>
	                                        
	                    <button type="button" onClick="imprimirFactura('${tempCustomer.nombreArchivo}')"
	                            class="btn btn-secondary no-shadow no-line float-right p-0" data-dismiss="modal">
	                        <div class="d-block-btn" data-toggle="tooltip" title="Imprimir">
	                        	<i class="ti-printer"></i>
	                        </div>
	                    </button>
	                    <button type="button" onClick="visualizarPDF('${tempCustomer.nombreArchivo}')"
	                            class="btn btn-secondary no-shadow no-line float-right p-0" data-toggle="modal" data-target="divModalPDF">
	                        <div class="d-block-btn" data-toggle="tooltip" title="Ver documento">
	                        	<i class="ti-eye"></i>
	                        </div>
	                    </button>
	                </div>
	                <div style="display:none;margin-left:10px;" class="alert p-0 alert-dismissible alert_warning_border fade show m-t-10" role="alert" id="messageStatus">
	                    <button type="button" class="close" data-dismiss="alert" aria-label="Close">
	                        <span aria-hidden="true">&times;</span>
	                    </button>
	                    <div class="alert_icon2 bg-success">
	                        <i class="ti-check"></i>
	                    </div>
	                    <div class="alert_data p-2">
	                        Se ha solicitado el documento del ticket, le será enviado en un lapso de N-horas.
	                    </div>
	                </div>
	
	
	            </td>
	        </tr>
	        </c:forEach>
	        
	    </tbody>
	</table>

<c:if test="${fn:length(dClientesMultiple) gt 1}">
	<div class="row" style="display:contents;">
	    <div class="d-inline-flex p-2 text">
	        <button id="reenviar" disabled type="button" onClick="obtenerCorreoMultiple()"
	                class="btn btn-secondary no-shadow no-line float-right" data-toggle="modal" data-target="modalReenvioMultiple">
	            <i class="ti-email"></i>
	            <span class="d-none d-md-block">Reenviar</span>
	        </button>
	        <c:if test="${dDescargar == 'true'}">
		        <button id="btnDescargaMultiple" disabled type="button" onclick="btnDescargaMultiple_onClick()"
		                class="btn btn-secondary no-shadow no-line float-right" data-dismiss="modal">
		            <i class="ti-download"></i>
		            <span class="d-none d-md-block">Descargar</span>
		        </button>
	        </c:if>
	    </div>
	</div>
</c:if>

	<div>
		<c:set var="CountFacturas" value="${dClientesMultipleCount}" /> 
	 	<c:set var="rowsPerPage" value="${dRowsPerPage}" /> 
	 	 	<c:set var="pageNumber" value="${param.pageNumber}"/>
	<%--  	<c:set var="pageNumber" value="${fn:substringBefore(param.pageNumber, '.jsp')}"/> --%>
	<%--  	<c:out value="PAGINA"/> --%>
	<%--  	<c:out value="${param.pageNumber}"/> --%>
	<%--   	<c:out value="${pageNumber}"/> --%>
		<fmt:formatNumber var="_a" value="${CountFacturas/rowsPerPage}" maxFractionDigits="0"/>
		<fmt:parseNumber var="a" type="number" value = "${fn:trim(_a)}" />
	<%-- 	<c:out value="${a}"/> --%>
		<c:set var="b" value="${CountFacturas/rowsPerPage}" /> 
	<%-- 	<c:out value="${b}"/> --%>
			<c:choose>
		    <c:when test="${a==0}">
		        <c:set var="numberOfPages" value="1" scope="session"/>   
		    </c:when>
		 
		    <c:when test="${b>a}">
		        <c:set var="xxx" value="${b%a}"/>
		        <c:if test="${xxx>0}">
		            <c:set var="numberOfPages" value="${b-xxx+1}" scope="session"/>   
		        </c:if>
		    </c:when>
		 
		    <c:when test="${a>=b}">
		        <c:set var="numberOfPages" value="${a}" scope="session"/>    
		    </c:when>
		</c:choose>
	
	
		<c:set var="start" value="${pageNumber*rowsPerPage-rowsPerPage}"/>
		<c:set var="stop" value="${pageNumber*rowsPerPage-1}"/>
	<%-- 	<c:out value="${start}"/> --%>
	<%-- 	<c:out value="${stop}"/> --%>
		
			<nav aria-label="Page navigation example2">
					<ul class="pagination">
						<%--For displaying Previous link --%>
						<li class="page-item">
							<c:if test="${pageNumber gt 1}">
				    			<a class="page-link" href="#" onClick="SearchFactureMultipleOnLinks(${pageNumber - 1},${start}, ${rowsPerPage});" >Anterior</a>
				    			</c:if>
				    		</li>
						

	 					<c:if test="${(pageNumber-5) < 0}">	 
				    			<c:forEach begin="1" end="10" var="i">
				    				<c:if test="${i gt 0 and i lt numberOfPages+1}">
							    		<c:choose>
									           <c:when test="${i!=pageNumber}">
									           <li class="page-item">
									               <a class="page-link" href="#" onClick="SearchFactureMultipleOnLinks(${i}, ${start}, ${rowsPerPage});"><c:out value="${i}"/></a>
									            </li> 
									           </c:when>
									           <c:otherwise>
									           	<li class="page-item">
									               <a class="page-link" style="background-color:lightgray;"  ><c:out value="${i}"/></a>
									             </li> 
									           </c:otherwise>
								            
								       </c:choose> 
				    				</c:if>
				    			</c:forEach>
				    		</c:if>
				    		<c:if test="${(pageNumber-5) >= 0}">	 
				    			<c:forEach begin="${pageNumber-4}" end="${pageNumber+6}" var="i">
				    				<c:if test="${i gt 0 and i lt numberOfPages+1}">
							    		<c:choose>
									           <c:when test="${i!=pageNumber}">
									           <li class="page-item">
									               <a class="page-link" href="#" onClick="SearchFactureMultipleOnLinks(${i}, ${start}, ${rowsPerPage});"><c:out value="${i}"/></a>
									            </li> 
									           </c:when>
									           <c:otherwise>
									           	<li class="page-item">
									               <a class="page-link" style="background-color:lightgray;"  ><c:out value="${i}"/></a>
									             </li> 
									           </c:otherwise>
								            
								       </c:choose> 
				    				</c:if>
				    			</c:forEach>
				    		</c:if>	 

	 			    	
			  			<%--For displaying Next link --%>
					    <c:if test="${pageNumber lt numberOfPages}">
						    <li class="page-item">
						    	<a class="page-link" href="#" onClick="SearchFactureMultipleOnLinks(${pageNumber + 1},${start}, ${rowsPerPage});">Siguiente</a>
						    </li>
					    </c:if>
				    </ul>
				</nav>
	</div>

    </c:if>
</div>

<div class="form-group">
    <ul class="pager wizard pager_a_cursor_pointer"></ul>
</div>

<div class="modal pullDown" id="responsive2" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="responsivemodal">Reenvío de correo</h5>
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <i class="ti-close"></i>
                </button>
            </div>
            <div class="modal-body">
                <div class="row">
                    <div class="col-12">
                        <form id="reenvioMail">
                            <div class="form-group">
                            	<input type="hidden" id="uuid" class="uuid" value=""/>
                            	<input type="hidden" id="rfcReenvio" value=""/>
                                <input type="text" class="form-control" id="correo_electronico_anterior" name="correo_electronico_anterior" readonly autocomplete="off">
                                <label for="correo_electronico_anterior" class="">
                                    <span class="text-danger">*</span>Correo electrónico:
                                </label>
                                <div>
                                    <div id="correo_electronico_anterior_validation"></div>
                                </div>
                            </div>
                            <div class="form-group">
                                <input type="text" class="form-control " id="correo_electronico_nuevo" name="correo_electronico_nuevo" onblur="validaCamposVacios('correo_electronico_nuevo', '#correo_electronico_nuevo_validation', 'full has-success', 'full has-danger','email')" maxlength="50" autocomplete="off">
                                <label for="correo_electronico_nuevo" class="">
                                    <span class="text-danger">*</span>Con copia para:
                                </label>
                                <div>
                                    <div id="correo_electronico_nuevo_validation"></div>
                                </div>
                            </div>
                            <div class="">
                                <div class="col text-right px-0">
                                    <button type="button" id="enviarUserDate" onClick="reenviarFactura()" class="btn btn-primary">Enviar</button>
                                </div>
                            </div>

                        </form>
                    </div>
                </div>
            </div>
            <!-- /.modal-body-->
        </div>
        <!-- /.modal-content-->
    </div>
    <!-- /.modal-dialog-->
</div>

<div class="modal pullDown" id="modalReenvioMultiple" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Reenvío de correo</h5>
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <i class="ti-close"></i>
                </button>
            </div>
            <div class="modal-body">
                <div class="row">
                    <div class="col-12">
                        <form id="frmReenvioMultiple">
                            <div class="form-group">
                                <input type="text" class="form-control" id="correo_electronico_anterior_mult" readonly autocomplete="off">
                                <label for="correo_electronico_anterior_mult" class="">
                                    <span class="text-danger">*</span>Correo electrónico:
                                </label>
                                <div>
                                    <div id="correo_electronico_anterior_mult_validation"></div>
                                </div>
                            </div>
                            <div class="form-group">
                                <input type="text" class="form-control " id="correo_electronico_nuevo_mult" onblur="validaCamposVacios('correo_electronico_nuevo_mult', '#correo_electronico_nuevo_mult_validation', 'full has-success', 'full has-danger','email')" maxlength="50" autocomplete="off">
                                <label for="correo_electronico_nuevo_mult" class="">
                                    <span class="text-danger">*</span>Con copia para:
                                </label>
                                <div>
                                    <div id="correo_electronico_nuevo_mult_validation"></div>
                                </div>
                            </div>
                            <div class="">
                                <div class="col text-right px-0">
                                    <button type="button" id="enviarUserDateMultiple" onClick="reenviarFacturaMultiple()" class="btn btn-primary">Enviar</button>
                                </div>
                            </div>

                        </form>
                    </div>
                </div>
            </div>
            <!-- /.modal-body-->
        </div>
        <!-- /.modal-content-->
    </div>
    <!-- /.modal-dialog-->
</div>

<div class="modal fade modal-pdf-viewer" tabindex="-1" role="dialog" id="divModalPDF">
    <div class="modal-dialog modal-lg modal-xl mt-2" role="document" >
        <div class="modal-content mt-0">
            <div class="modal-header p-0">
                <h5 class="titulo-left-modal">Ver documento <span id="uuid-pdf"></span></h5>
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body p-0">
                <div id="divAjaxPDF">
                    <iframe id="FramePDF" src="" style="border: 1px solid black; width:100%; height:500px"></iframe>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    $(document).ready(function(){
      $('[data-toggle="tooltip"]').tooltip();   
    });
</script>

<script type="text/javascript" charset="UTF-8" src="./resources/js/variables.js"></script>
<script type="text/javascript" charset="UTF-8" src="./resources/js/all.js"></script>
<script type="text/javascript" charset="UTF-8" src="./resources/js/ajax.js"></script>

</body>

</html>

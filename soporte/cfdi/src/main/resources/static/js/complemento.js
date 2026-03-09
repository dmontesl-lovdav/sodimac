var MODAL_DATOS_EXCEL = "#modalDescargarExcelComplemento";
var MODAL_FOLIO_FACTURA = "#modalFolioFactura";
var MODAL_VALIDAR = "#modalValidar";
var MODAL_CONFIRMAR = "#modalConfirmarVincular";
var MODAL_CONFIRMAR_DESVINCULAR = "#modalConfirmarDesvincular";
var MODAL_CONFIRMAR_TIMBRAR = "#modalConfirmarTimbrar";

var periodo = 15;
var intervalWariningMsg = "";
var rowsPerPage =10;
var table;
var archivosPermitidosDescargar = 10;

Mensajes_js = {
		MENSAJE_EXCEL_SIN_DATOS : 'No existe información para descargar',
		MENSAJE_ADVERTENCIA_ASOCIAR_COMPLEMENTO_FOLIO : '¿Esta seguro que desea asociar el complemento de pago al folio?',
		MENSAJE_COMPLEMENTO_VINCULADO_EXITO : 'Se ha vinculado el complemento al folio con &eacute;xito',
		MENSAJE_COMPLEMENTO_VINCULADO_ERROR : 'Ha ocurrido un error al vincular el complemento al folio',
		MENSAJE_ADVERTENCIA_DESVINCULAR_COMPLEMENTO_FOLIO : '¿Esta seguro que desea desvincular el complemento al folio?',
		MENSAJE_COMPLEMENTO_DESVINCULADO_EXITO : 'Se ha desvinculado el complemento al folio con &eacute;xito',
		MENSAJE_COMPLEMENTO_DESVINCULADO_ERROR : 'Ha ocurrido un error al desvincular el complemento al folio',
		
		MENSAJE_ADVERTENCIA_TIMBRAR_COMPLEMENTO_PAGO : '¿Esta seguro que desea timbrar el complemento de pago?',
		MENSAJE_COMPLEMENTO_TIMBRAR_EXITO : 'Se ha timbrado el complemento de pago con &eacute;xito',
		mensage_mail_invalido : "El correo no es válido",
		campos_vacios_message : "El campo está vacío"
	};


$(document).ready(function(){
	
	showSpinner();
    var url = '/cfdi/consultar/getStartDate';

    $.ajax({
    	url: url,
    	type: "get", async: true, cache: false, crossDomain: false,
    	success: function(data){
    	    $('#date-container .input-daterange').datepicker({
    	        format: "dd/mm/yyyy",
    	        language: "es",
    	        todayBtn: "true",
    	        daysOfWeekHighlighted: "0,6",
    	        clearBtn: true,
    	        weekStart: 1,
    	        startDate: data,
    	        endDate: "today",
    	        todayHighlight: true
    	    });
    		
    	}
    });
	            
    var url = '/cfdi/consultar/getConfiguracion';
    $.ajax({
    	url: url,
    	data: {NombreCampo:"Multiple.cfdi.date.interval"},
    	type: "post", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		periodo = data;
    	}
    });
    
    var url = '/cfdi/consultar/getConfiguracion';
    $.ajax({
    	url: url,
    	data: {NombreCampo:"Multiple.cfdi.date.interval.warning.message"},
    	type: "post", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		intervalWariningMsg = data;
    	}
    });

    var url = '/cfdi/consultar/getConfiguracion';
    $.ajax({
    	url: url,
    	data: {NombreCampo:"Multiple.cfdi.data.grid.rowsPerPage"},
    	type: "post", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		rowsPerPage = data;
    	}
    });
    
    var url = '/cfdi/pagos/obtenerStatusPagos?tipoPago=CP';
    $.ajax({
    	url: url,
    	type: "post", dataType: "json", async: false, cache: false, crossDomain: false,
    	success: function(data){
    		var selector = document.getElementById("listEstatusComple")
    		$('#listEstatusComple').append(new Option("Seleccione estatus de complemento", ""));
    		var nCont = 0;
    		for(let item of data)
    			{
    			nCont++;
    			selector.options[nCont] = new Option(item.descripcionEstatus,item.idEstatus);
    			}
    		result = data;
        }
    });    
      
    releaseEventEmpty("rfcInput");
    releaseEventEmpty("montoInput");
    
    $("#dateHasta").mask("00/00/0000");
    $("#dateDesde").mask("00/00/0000");
    
    const current = new Date();
	const year = current.getFullYear();
	let month = current.getMonth() + 1;
	let day = current.getDate();
	current.setDate(current.getDate() - 1);
	let day1 = current.getDate();
	
	month = month.toString().padStart(2, "0");
	day = day.toString().padStart(2, "0");
	day1 = day1.toString().padStart(2, "0");
	
	$("#dateDesde").val( day1 + '/' + month + '/' + year );
	$("#dateHasta").val( day + '/' + month + '/' + year );
    hideSpinner();
});

function isValidDate(idDateDesde, validationFieldDesde, idDateHasta, validationFieldHasta) {
	
	setTimeout(function(){
		$("#" + validationFieldDesde).empty();
		$("#" + validationFieldHasta).empty();
		addSuccess(idDateDesde);
		addSuccess(idDateHasta);

		if ($("#" + idDateDesde).val().trim() == "") {
	        addDanger(idDateDesde);
		} else {
			if  (!validatefechaInput($("#" + idDateDesde).val())) {
		  		$("#" + idDateDesde).val("");
		  	}			
		}
		if ($("#" + idDateHasta).val().trim() == "") {
	        addDanger(idDateHasta);
		} else {
		  	if  (!validatefechaInput($("#" + idDateHasta).val())) {
		  		$("#" + idDateHasta).val("");
		  	}
		}
		if ($("#" + idDateDesde).val().trim() == "" || $("#" + idDateHasta).val().trim() == "") {
	        return;
		}

  		if (validarRangoInvalidoFechas (idDateDesde, idDateHasta)) {
	        messageDanger("#" + validationFieldDesde, "La fecha Desde no puede ser mayor a Hasta");
	        addDanger(idDateDesde);	  				
  			return;
  		}

  		if (validarRangoMaximoFechas (idDateDesde, idDateHasta)) {
  			$("#" + idDateDesde).val("");
  			$("#" + idDateHasta).val("");
  			$("#" + idDateDesde).datepicker('clearDates');
  			$("#" + idDateHasta).datepicker('clearDates');

    		Swal.fire(
    				  'Facturación Sodimac',
    				  intervalWariningMsg.replace("{dias}", periodo),
    				  'warning'
    				)
  		}
  		
	}
	
	, 250);
}

function consultarOnClick() {
	
	$("#date_desde_validation").empty();
	$("#date_hasta_validation").empty();
	$("#rfcInput_validation").empty();
	$("#montoInput_validation").empty();

	var dateDesdeValue = $("#dateDesde").val().trim();
	var dateHastaValue = $("#dateHasta").val().trim();
	var rfc = $("#rfcInput").val().toUpperCase().trim();
	var cTipo = $("#listEstatusComple option:selected").val();
	var monto = $("#montoInput").val().trim();

	if (dateDesdeValue==""){
        messageDanger("#date_desde_validation", campos_vacios_message);
        addDanger("dateDesde");
        return;
	}
	if (dateHastaValue==""){
        messageDanger("#date_hasta_validation", campos_vacios_message);
        addDanger("dateHasta");
        return;
	}
	
	if (!validatefechaInput(dateDesdeValue)) {
        messageDanger("#date_desde_validation", "Fecha no válida");
        addDanger("dateDesde");
        return;
	}
	if (!validatefechaInput(dateHastaValue)) {
        messageDanger("#date_hasta_validation", "Fecha no válida");
        addDanger("dateHasta");
        return;
	}
	
	if (validarRangoInvalidoFechas ('dateDesde', 'dateHasta')) {
        messageDanger("#date_desde_validation", "La fecha Desde no puede ser mayor a Hasta");
        addDanger("dateDesde");	  				
        return;				
	}
		
	if (rfc != "" && !rfcValidationBD(rfc)){
        messageDanger("#rfcInput_validation", "RFC no esta en la base de datos");
        addDanger("rfcInput");
        return;
	}
	
	if (monto != "" && !validateMontoInput(monto)) {
        messageDanger("#montoInput_validation", "Monto inválido");
        addDanger("montoInput");
        return;
    }
	
	showSpinner();
	$('#btnBuscarComplementos').prop("disabled", true);

	var url = './listarComplementos?dateDesde='+dateDesdeValue+'&dateHasta='+dateHastaValue+'&rfc='+rfc+'&pageNumber=0'+'&start=0' + '&rowsPerPage=' + rowsPerPage + '&tipocomplemento=' + cTipo + '&pmonto=' + monto;
	const fragmentDiv = document.getElementById("fragmentDiv");
	$.ajax({
		type: "GET",
		//async: false,
		contentType: "application/json",
		url: url,
		//data: objData,
		timeout: 120000,
		success: function(response) {
			if (response.indexOf("Inicio de Ses") != -1) {
				window.location.href = './';
			} else {
				fragmentDiv.innerHTML = response;
				load_table_style();
				hideSpinner();
				$('#btnBuscarComplementos').prop("disabled", false);
			}
		},
		error: function(e) {
			console.log("ERROR: ", e);
			hideSpinner();
			$('#btnBuscarComplementos').prop("disabled", false);
		},
		done: function(e) {
			console.log("DONE");
			hideSpinner();
		}
	});
}


function descargaxlsxOnClick() {
	
	var existenDatos = false;
	$('#data tr').each(function(index, tr) {
		existenDatos = true;
	});
	if (!existenDatos) {
		$(MODAL_DATOS_EXCEL).modal("show");
		$("#divWarningExcelComplemento").html(Mensajes_js.MENSAJE_EXCEL_SIN_DATOS);
		return;
	}
	
	$("#date_desde_validation").empty();
	$("#date_hasta_validation").empty();
	$("#rfcInput_validation").empty();
	
	var dateDesdeValue = $("#dateDesde").val().trim();
	var dateHastaValue = $("#dateHasta").val().trim();
	var rfc = $("#rfcInput").val().toUpperCase().trim();
	var cTipo = $("#listEstatusComple option:selected").val();
	var monto = $("#montoInput").val().trim();

	if (dateDesdeValue==""){
        messageDanger("#date_desde_validation", campos_vacios_message);
        addDanger("dateDesde");
        return;
	}
	if (dateHastaValue==""){
        messageDanger("#date_hasta_validation", campos_vacios_message);
        addDanger("dateHasta");
        return;
	}
	
	if (!validatefechaInput(dateDesdeValue)) {
        messageDanger("#date_desde_validation", "Fecha no válida");
        addDanger("dateDesde");
        return;
	}
	if (!validatefechaInput(dateHastaValue)) {
        messageDanger("#date_hasta_validation", "Fecha no válida");
        addDanger("dateHasta");
        return;
	}
	
	if (validarRangoInvalidoFechas ('dateDesde', 'dateHasta')) {
        messageDanger("#date_desde_validation", "La fecha Desde no puede ser mayor a Hasta");
        addDanger("dateDesde");	  				
        return;				
	}
		
	if (rfc != "" && !rfcValidationBD(rfc)){
        messageDanger("#rfcInput_validation", "RFC no esta en la base de datos");
        addDanger("rfcInput");
        return;
	}
	
	if (monto != "" && !validateMontoInput(monto)) {
        messageDanger("#montoInput_validation", "Monto inválido");
        addDanger("montoInput");
        return;
    }
	
	$('#btnDescargaxlsx').prop("disabled", true);
	showSpinner();

	var url = './listarComplementos/descargaXlsx?dateDesde='+dateDesdeValue+'&dateHasta='+dateHastaValue+'&rfc='+rfc+'&tipocomplemento=' + cTipo +'&pmonto=' + monto;
	let a = document.createElement('a');
	a.href = url;
	document.body.appendChild(a);
	a.click();
	document.body.removeChild(a);
	hideSpinner();
	$('#btnDescargaxlsx').prop("disabled", false);
	return; 
}


function montoInput_onkeypress() {
	var x = event.keyCode;
	var w = event.which;
	//32=space , 46=Suprimir key, w=17 y x=86 = ctrl-V
	  if( x==undefined || x==32 || x==46 || (w==17 && x==86)){
	  } else {
		  	var re = new RegExp("[0-9]+([.])?([0-9]+)?$");
			var isValid = re.test(String.fromCharCode(x));
		    if (!isValid) {
		    	event.preventDefault();
		    }
	  }
}

function releaseEventMontoInputEmpty() {
	
	$("#montoInput_validation").empty();
    var valor = $("#montoInput").val().trim();
    
    if (valor != null && valor != "") {
	    var result = validateMontoInput(valor);
	    if (!result) {
	        messageDanger("#montoInput_validation", "Monto inválido");
	        addDanger("montoInput");
	        return false;
	    }
	}
    
    addSuccess("montoInput");
    return true;

}

function validateMontoInput(textoValidar) {

	var re = new RegExp("^[0-9]+([.])?([0-9]+)?$");
    return re.test(textoValidar);
}

function load_table_style() {
	
	table = $('#data').DataTable({
		"info": true,
		"paging": true,
		"ordering": false,
		"lengthChange" 	: false,		//Oculta la posibilidad de cambiar el numero de resultados
		"searching": false,
		"pagingType": "simple_numbers",
		"pageLength": 10,
		"language": {
			"info": "Mostrando _START_ de _END_ de un total: _TOTAL_ registros",
			"lengthMenu": "Mostrando _MENU_ registros",
			"loadingRecords": "Cargando...",
			"processing": "Procesando...",
			"emptyTable": "Sin registros en la tabla",
			"infoEmpty": "Mostrando 0 de 0 de un total de: 0 registros",
			"infoFiltered": "(filtrado de _MAX_ entradas totales)",
			"search": "Buscar:",
			"zeroRecords": "Sin registros encontrados",
			"paginate": {
				"first": "Primero",
				"last": "Último",
				"previous": "Anterior",
				"next": "Siguiente",
			}
		}
	});
	
	$('.dataTables_length').addClass('bs-select');
}

function rfcInput_onkeypress() {
	var x = event.keyCode;
	var w = event.which;
	//32=space , 46=Suprimir key, w=17 y x=86 = ctrl-V
	  if( x==undefined || x==32 || x==46 || (w==17 && x==86)){
	  } else {
		  	var re = new RegExp($("#hdnExpresionRegularRfcCaracteres").val());
			var isValid = re.test(String.fromCharCode(x));
		    if (!isValid) {
		    	event.preventDefault();
		    }
	  }
}

function releaseEventrfcInputEmpty() {
	
	$("#rfcInput_validation").empty();
    var valor = $("#rfcInput").val().trim().toUpperCase();
    
    var result = validateRfcInput(valor);
    if (!result) {
        messageDanger("#rfcInput_validation", "RFC inválido");
        addDanger("rfcInput");
        return false;
    }
    addSuccess("rfcInput");
    return true;
}

function rfcValidation(rfc){
    if (!validateRfcInput(rfc)){
        return false;
    } else {
        var result = false
        var url = './consultar/validarRfc';
        $.ajax({
        	url: url,
        	data: {rfc: rfc},
        	type: "post", async: false, cache: false, crossDomain: false,
        	success: function(data){
                if (data == "OK")
              	  result = true;
            }
        }); 
	    return result;
    }
}

function validateRfcInput(valor) {

	if (valor == "") return true;
	var re = new RegExp($("#hdnExpresionRegularRfc").val());
    return re.test(valor);
}

function rfcValidationBD(rfc) {
	var result = false;

	if (validateRfcInput(rfc)) {
        var url = '/cfdi/consultar/validarRfcMultiple';
        $.ajax({
        	url: url,
        	data: {rfc: rfc},
        	type: "post", async: false, cache: false, crossDomain: false,
        	success: function(data){
                if (data == "OK"){
                	result = true;
                }
            }
        });	        
	}
    return result;
}

function asociarFolioFacturaOnclick(idPagoComplemento, estatus) {
	if (idPagoComplemento=="") {return};	
	var realizaBusqueda=false;
	var cleanFolio = true;
	
	$("#hdnIdPagoComplemento").val(idPagoComplemento);
	$("#hdEstatusComplemento").val(estatus);
	consultarFolios(idPagoComplemento, 0, estatus, realizaBusqueda, cleanFolio);
}

function consultarFoliosOnClick() {
	var idPagoComplemento = $("#hdnIdPagoComplemento").val().trim();
	var estatusComplemento = $("#hdEstatusComplemento").val().trim();
	var folioFactura = $("#txtFolioFactura").val().trim();
	
	var realizaBusqueda = true;
	var cleanFolio = false;
	
	$("#folioFacturaInput_validation").empty();

	if (folioFactura==""){
        messageDanger("#folioFacturaInput_validation", campos_vacios_message);
        addDanger("txtFolioFactura");
        return;
	}
	
	consultarFolios(idPagoComplemento, folioFactura, estatusComplemento, realizaBusqueda, cleanFolio);
}

function consultarFolios(idPagoComplemento, folioFactura, estatusComplemento, realizaBusqueda, cleanFolio) {
	showSpinner();
	$('#btnBuscarFolioFacturasPago').prop("disabled", true);

	var url = './listarComplementos/folio';
	$.ajax({
    	url: url,
    	data: {idPagoComplemento: idPagoComplemento,
    		   folioFactura: folioFactura,
    		   realizaBusqueda: realizaBusqueda,
    		   estatusComplemento: estatusComplemento},
    	type: "get", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		//var data = data.replace(/\?/g, '•');
    		$("#modalFolioFactura").modal();
    		loadDataUsers(data, cleanFolio);
    		hideSpinner();
    		$('#btnBuscarFolioFacturasPago').prop("disabled", false);
    	},
    	error: function(e) {
			console.log("ERROR: ", e);
			hideSpinner();
			$('#btnBuscarFolioFacturasPago').prop("disabled", false);
		},
		done: function(e) {
			console.log("DONE");
			hideSpinner();
		}
    });

}

function loadDataUsers(dataModel, cleanFolio) {
	var dataJson = dataModel.listPagosFoliosFacturaView;
	$("#spanRFC").html(dataModel.rfc);
	$("#spanNombre").html(dataModel.razonSocial);
	$("#spanFolioFactura").html(dataModel.folioFactura);
	$("#spanImportePago").html(dataModel.importePagoComplementoStr);
	$("#spanTotalRemanente").html(dataModel.totalRemanenteFolioFacturaStr);
	$("#spanTotalFolioFactura").html(dataModel.totalFolioFacturaStr);
	$("#spanTotalOtrosPagos").html(dataModel.totalOtrosPagosStr);
	
	console.log("btnVincularPago: " + dataModel.btnVincularPago);
	console.log("btnAsignarComplemento: " + dataModel.btnAsignarComplemento);
	console.log("validaComplemento: " + dataModel.validaComplemento);
	
	if (dataModel.btnVincularPago) { $("#btnDesvincular").show(); } else { $("#btnDesvincular").hide(); }
	if (dataModel.btnAsignarComplemento) { $("#btnGuardarPagoComp").show(); } else { $("#btnGuardarPagoComp").hide(); }
	
	if (dataModel.validaComplemento) { 
		$("#spanValidacion").hide();
	} else {
		$("#spanValidacion").html(dataModel.msgValidacion);
		$("#spanValidacion").show();
	}
	
	if (dataModel.existePagoComplemento) {
		$("#divBusquedaFolio").hide();
	} else {
		$("#divBusquedaFolio").show();
		
		if (cleanFolio) {
			$("#txtFolioFactura").html("");
			$("#txtFolioFactura").val("");
		} 
	}
	
	/*
	if (dataModel.existePagoComplemento) {
		$("#divBusquedaFolio").hide();
		$("#btnDesvincular").show();
		$("#btnGuardarPagoComp").hide();
		
		if (ocultaDesvincular == 1) {
			$("#btnDesvincular").hide();
		}
	} else {
		$("#divBusquedaFolio").show();
		if (cleanFolio) {
			$("#txtFolioFactura").html("");
			$("#txtFolioFactura").val("");
			$("#btnGuardarPagoComp").hide();
			$("#spanValidacion").hide();
		} else {
			$("#btnGuardarPagoComp").show();	
		}
		$("#btnDesvincular").hide();
		
		if (ocultaDesvincular == 1) {
			$("#btnGuardarPagoComp").hide();
		}
	}
	
	if (dataModel.msgValidacion != null && dataModel.msgValidacion != "") {
		$("#spanValidacion").html(dataModel.msgValidacion);
		$("#spanValidacion").show();
		$("#btnDesvincular").hide();
		$("#btnGuardarPagoComp").hide();
		
		if (cleanFolio) {
			$("#spanValidacion").hide();
		}
	} else {
		$("#spanValidacion").hide();
	}*/
	
	table = $('#folio-table').DataTable({
		"info": true,
		"bDestroy":true,
		"paging": true,
		"ordering": false,
		"lengthChange" 	: false,		//Oculta la posibilidad de cambiar el numero de resultados
		"searching": false,
		"pagingType": "simple_numbers",
		"pageLength": 10,
		"language": {
			"info": "Mostrando _START_ de _END_ de un total: _TOTAL_ registros",
			"lengthMenu": "Mostrando _MENU_ registros",
			"loadingRecords": "Cargando...",
			"processing": "Procesando...",
			"emptyTable": "Sin registros en la tabla",
			"infoEmpty": "Mostrando 0 de 0 de un total de: 0 registros",
			"infoFiltered": "(filtrado de _MAX_ entradas totales)",
			"search": "Buscar:",
			"zeroRecords": "Sin registros encontrados",
			"paginate": {
				"first": "Primero",
				"last": "Último",
				"previous": "Anterior",
				"next": "Siguiente",
			}
		},
		columns:[
			{name:"registro", data:"registro"},
			{name:"orden", data:"orden"},
			{name:"complementoPago", data:"idPagoComplemento"},
			{name:"pago", data:"pagoComplementoStr"},
			{name:"factura", data:"idFactura"},
			{name:"montoRealFactura", data:"montoRealFacturaStr"},
			{name:"montoTotalNC", data:"montoTotalNCStr"},
			{name:"montoPorPagar", data:"montoPorPagarStr"},
			{name:"importeSaldoAnterior", data:"importeSaldoAnteriorStr"},
			{name:"importePagado", data:"importePagadoStr"},
			{name:"importePosterior", data:"importePosteriorStr"},
			{name:"parcialidad", data:"parcialidad"},
			{name:"moneda", data:"monedaPago"},
			{name:"equivalencia", data:"equivalencia"}
		],
		data:dataJson,
		createdRow: function( row, data, dataIndex){
                if( data.colorPagoComplemento){
                    $(row).css("background-color", " #abebc6");
                }
            }
	});
	$('.dataTables_length').addClass('bs-select');
}

function folioInput_onkeypress() {
	var x = event.keyCode || event.which;
	if( x==undefined || x==32){
	  } else {
		  	var re = new RegExp("^[0-9]*$");
			var isValid = re.test(String.fromCharCode(x));
		    if (!isValid) {
		    	event.preventDefault();
		    }
	  }
}

function asignarComplementoFolioOnClick() {
	$(MODAL_CONFIRMAR).modal("show");
	$("#divWarningConfirmarVincular").html(Mensajes_js.MENSAJE_ADVERTENCIA_ASOCIAR_COMPLEMENTO_FOLIO);
}

function desvincularComplementoFolioOnClick() {
	$(MODAL_CONFIRMAR_DESVINCULAR).modal("show");
	$("#divWarningConfirmarDesVincular").html(Mensajes_js.MENSAJE_ADVERTENCIA_DESVINCULAR_COMPLEMENTO_FOLIO);
}

function timbrarComplementoPagoOnClick(idPagoComplemento) {
	if (idPagoComplemento=="") {return};	
	
	$("#hdnIdPagoComplemento").val(idPagoComplemento);
	
	$(MODAL_CONFIRMAR_TIMBRAR).modal("show");
	$("#divWarningConfirmarTimbrar").html(Mensajes_js.MENSAJE_ADVERTENCIA_TIMBRAR_COMPLEMENTO_PAGO);
	
}

function asignarComplementoFolioConfirmOnClick() {
	$(MODAL_CONFIRMAR).modal("hide");
	$(MODAL_FOLIO_FACTURA).modal("hide");
	
	var idPagoComplemento = $("#hdnIdPagoComplemento").val().trim();
	var estatusComplemento = $("#hdEstatusComplemento").val().trim();
	var folioFactura = $("#txtFolioFactura").val().trim();
	
	var url = './asignarComplementosFolio';
	$.ajax({
    	url: url,
    	data: {idPagoComplemento: idPagoComplemento,
    		   folioFactura: folioFactura,
    		   estatusComplemento: estatusComplemento},
    	type: "post", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		console.log(data);
    		var json = JSON.parse(data);
			if(json.STATUS == "OK") {
    			Swal.fire('Facturación Sodimac',Mensajes_js.MENSAJE_COMPLEMENTO_VINCULADO_EXITO,'success');	
			} else {
				Swal.fire('Facturación Sodimac',Mensajes_js.MENSAJE_COMPLEMENTO_VINCULADO_ERROR,'warning');	
			}
    		
    		consultarOnClick();
    	}
    });
}

function desvincularComplementoFolioConfirmOnClick() {
	$(MODAL_CONFIRMAR_DESVINCULAR).modal("hide");
	$(MODAL_FOLIO_FACTURA).modal("hide");
	
	var idPagoComplemento = $("#hdnIdPagoComplemento").val().trim();
	
	var url = './desvincularComplementosFolio';
	$.ajax({
    	url: url,
    	data: {idPagoComplemento: idPagoComplemento}, 
    	type: "post", async: false, cache: false, crossDomain: false,
    	success: function(data){
			console.log(data);
			var json = JSON.parse(data);
			if(json.STATUS == "OK") {
    			Swal.fire('Facturación Sodimac',Mensajes_js.MENSAJE_COMPLEMENTO_DESVINCULADO_EXITO,'success');	
			} else {
				Swal.fire('Facturación Sodimac',Mensajes_js.MENSAJE_COMPLEMENTO_DESVINCULADO_ERROR,'warning');	
			}
    		consultarOnClick();
    	}
    });
    
    //Swal.fire('Facturación Sodimac',Mensajes_js.MENSAJE_COMPLEMENTO_DESVINCULADO_EXITO,'success');
    //consultarOnClick();
}

function timbrarComplementoPagoConfirmOnClick() {
	$(MODAL_CONFIRMAR_TIMBRAR).modal("hide");
	$(MODAL_FOLIO_FACTURA).modal("hide");
	
	var idPagoComplemento = $("#hdnIdPagoComplemento").val().trim();
	showSpinner();
	
	var url = './timbrarComplementoPago';
	$.ajax({
    	url: url,
    	data: {idPagoComplemento: idPagoComplemento}, 
    	type: "post", async: false, cache: false, crossDomain: false,
    	success: function(data){
			console.log(data);
			var json = JSON.parse(data);
			if(json.STATUS == "OK") {
    			Swal.fire('Facturación Sodimac',Mensajes_js.MENSAJE_COMPLEMENTO_TIMBRAR_EXITO,'success');	
			} else {
				Swal.fire('Facturación Sodimac',json.descripcion,'warning');	
			}
    		consultarOnClick();
    	}
    });
    hideSpinner();
}

function obtenerCorreo(uuid, rfc, idComplemento)
{
	if (uuid=="" || rfc=="") return;
	$("#uuid").val(uuid);
	$("#idComplemento").val(idComplemento);
	$("#rfcReenvio").val(rfc);
	$('#enviarUserDate').prop("disabled", false);
	
    /*var url = './consultar/obtenerCorreo';

    $.ajax({
    	url: url,
    	data: {idComplemento: idComplemento},
    	type: "get", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		var data = data.replace(/\?/g, '•');
    		$("#correo_electronico_anterior").val(data);
    		addSuccess("correo_electronico_anterior");
    		eventosReenvio();
    		$("#responsive2").modal();
    	}
    });*/
    
    var data = "";
	$("#correo_electronico_anterior").val(data);
	addSuccess("correo_electronico_anterior");
	eventosReenvio();
	$("#responsive2").modal();
}

function eventosReenvio(){
	releaseEventEmpty("correo_electronico_nuevo");
	releaseEventEmpty("correo_electronico_anterior");
}

function reenviarFactura()
{
	var idComplemento = $("#idComplemento").val().trim();
	var rfc = $("#rfcReenvio").val().trim();
	var email = $("#correo_electronico_anterior").val().toLowerCase().trim();
	var emailCC = $("#correo_electronico_nuevo").val().toLowerCase().trim();

	if (idComplemento=="") return;
	
	if (email == ""){
        messageDanger("#correo_electronico_anterior_validation", Mensajes_js.campos_vacios_message);
        addDanger("correo_electronico_nuevo");
        return;
    }

	if (!validateEmail(email)){
        messageDanger("#correo_electronico_anterior_validation", Mensajes_js.mensage_mail_invalido);
        addDanger("correo_electronico_anterior");
        return;
    }

	$('#enviarUserDate').prop("disabled", true);
	var url = './reenvioFactura';

    $.ajax({
    	url: url,
    	data: {rfc:rfc
    	    , idComplemento:idComplemento
    	    , eMailCC: emailCC
    	    , email: email},
    	type: "get", async: true, cache: false, crossDomain: false,
    	success: function(data){

    		if (data == "success"){
    			$("#responsive2").modal("hide");
    			$('.modal-backdrop').remove();
        		Swal.fire(
        				  'Facturación Sodimac',
        				  'Se ha reenviado el documento correctamente',
        				  'success'
        				)
    		}

    		if (data == "error")
        		Swal.fire(
      				  'Facturación Sodimac',
      				  'Ocurrió un problema al reenviar el documento, intente mas tarde',
      				  'warning'
      				)    		
    	}
    });

}

function validateEmail(email) {

	var re = new RegExp($("#hdnExpresionRegularEmail").val());
    return re.test(email);
}

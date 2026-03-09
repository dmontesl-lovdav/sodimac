var MODAL_DATOS_EXCEL = "#modalDescargarExcelComplemento";
var MODAL_CARGAR_ARCHIVO = "#modalCargaArchivo";
var MODAL_VALIDAR = "#modalValidar";
var MODAL_CONFIRMAR = "#modalConfirmarGuardar";
var MODAL_CONFIRMAR_DESVINCULAR = "#modalConfirmarDesvincular";
var MODAL_CONFIRMAR_BORRAR = "#modalConfirmarBorrar";

var periodo = 15;
var intervalWariningMsg = "";
var rowsPerPage =10;
var table;
var archivosPermitidosDescargar = 10;

Mensajes_js = {
		MENSAJE_ADVERTENCIA_CARGAR_ARCHIVO : '¿Esta seguro que desea cargar el archivo seleccionado?',
		MENSAJE_ARCHIVO_CARGADO_EXITO : 'Se ha cargado el documento con &eacute;xito',
		MENSAJE_ARCHIVO_CARGADO_ERROR : 'Ha ocurrido un error al cargar el archivo',
		MENSAJE_ADVERTENCIA_DESVINCULAR_COMPLEMENTO_FOLIO : '¿Esta seguro que desea desvincular el complemento al folio?',
		MENSAJE_BORRAR_ARCHIVO_EXITO : 'Se ha borrado el archivo con &eacute;xito',
		MENSAJE_BORRAR_ARCHIVO_ERROR : 'Ha ocurrido un error al borrar el archivo',
		MENSAJE_ADVERTENCIA_BORRAR_ARCHIVO : '¿Esta seguro que desea borrar el archivo?',
		
		VALIDAR_SELECCIONAR_TIPO_ARCHIVO : "Seleccionar un tipo de documento",
		VALIDAR_SELECCIONAR_ARCHIVO : "Seleccionar un archivo"
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
    
    var url = '/cfdi/publicar/tiposDocumento';
    $.ajax({
    	url: url,
    	type: "post", dataType: "json", async: false, cache: false, crossDomain: false,
    	success: function(data){
    		var selector = document.getElementById("listTipoDocumento")
    		$('#listTipoDocumento').append(new Option("Seleccione tipo de documento", ""));
    		var nCont = 0;
    		for(let item of data)
    			{
    			nCont++;
    			selector.options[nCont] = new Option(item.descripcion, item.id);
    			}
    		result = data;
        }
    });    
      
    releaseEventEmpty("rfcInput");
    
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
	
	var dateDesdeValue = $("#dateDesde").val().trim();
	var dateHastaValue = $("#dateHasta").val().trim();
	var cTipo = $("#listTipoDocumento option:selected").val();

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
	
	showSpinner();
	$('#btnBuscarDocumento').prop("disabled", true);
	
	var url = './listar/documentos?dateDesde='+dateDesdeValue+'&dateHasta='+dateHastaValue+'&pageNumber=0'+'&start=0' + '&rowsPerPage=' + rowsPerPage + '&tipoDocumento=' + cTipo;
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
				$('#btnBuscarDocumento').prop("disabled", false);
			}
		},
		error: function(e) {
			console.log("ERROR: ", e);
			hideSpinner();
			$('#btnBuscarDocumento').prop("disabled", false);
		},
		done: function(e) {
			console.log("DONE");
			hideSpinner();
		}
	});
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

function publicarOnClick() {
	$("#spanValidacion").hide();
	showSpinner();
	
	var url = '/cfdi/publicar/tiposDocumento';
    $.ajax({
    	url: url,
    	type: "post", dataType: "json", async: false, cache: false, crossDomain: false,
    	success: function(data){
			$("#modalCargaArchivo").modal();
			$('#listTipoDocumentoModal').empty();
			$("#archivo").val(null);
    		var selector = document.getElementById("listTipoDocumentoModal")
    		$('#listTipoDocumentoModal').append(new Option("Seleccione tipo de documento", ""));
    		var nCont = 0;
    		for(let item of data) {
    			nCont++;
    			selector.options[nCont] = new Option(item.descripcion, item.id);
    		}
    		hideSpinner();
        }, error: function(e) {
			console.log("ERROR: ", e);
			hideSpinner();
		},
		done: function(e) {
			console.log("DONE");
			hideSpinner();
		}
    });
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
    		$("#modalCargaArchivo").modal();
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

function cargarArchivoOnClick() {
	$("#spanValidacion").hide();
	
	var vArchivo = $("#archivo").val().trim();
	var vListTipoDocumentoModal = $("#listTipoDocumentoModal").val().trim();
	

	if (vListTipoDocumentoModal == ""){
		addDanger("listTipoDocumentoModal");
        $("#spanValidacion").html(Mensajes_js.VALIDAR_SELECCIONAR_TIPO_ARCHIVO);
		$("#spanValidacion").show();
        return;
    }
    
    if (vArchivo == ""){
        addDanger("archivo");
        $("#spanValidacion").html(Mensajes_js.VALIDAR_SELECCIONAR_ARCHIVO);
		$("#spanValidacion").show();
        return;
    }
	
	$(MODAL_CONFIRMAR).modal("show");
	$("#divWarningCargarArchivo").html(Mensajes_js.MENSAJE_ADVERTENCIA_CARGAR_ARCHIVO);
}

function desvincularComplementoFolioOnClick() {
	$(MODAL_CONFIRMAR_DESVINCULAR).modal("show");
	$("#divWarningConfirmarDesVincular").html(Mensajes_js.MENSAJE_ADVERTENCIA_DESVINCULAR_COMPLEMENTO_FOLIO);
}

function borrarArchivoOnClick(idDocumentoPublicado) {
	if (idDocumentoPublicado=="") {return};	
	
	$("#hdnIdDocumento").val(idDocumentoPublicado);
	$(MODAL_CONFIRMAR_BORRAR).modal("show");
	$("#divWarningConfirmarBorrar").html(Mensajes_js.MENSAJE_ADVERTENCIA_BORRAR_ARCHIVO);
	
}

function guardarArchivoConfirmOnClick() {
	showSpinner();
	$(MODAL_CONFIRMAR).modal("hide");
	$(MODAL_CARGAR_ARCHIVO).modal("hide");
	
	var form = $('#create-documento')[0];
    var data = new FormData(form);
    
    $.ajax({
		type: "POST",
		enctype: "multipart/form-data",
		url: "../publicar/create",
		data: data,
		processData: false,
		contentType: false,
		cache: false,
		success: function(data) {
			console.log(data);
			
			var json = JSON.parse(data);
			if(json.STATUS == "OK") {
    			Swal.fire('Publicación de Documentos',Mensajes_js.MENSAJE_ARCHIVO_CARGADO_EXITO,'success');	
			} else {
				Swal.fire('Publicación de Documentos',json.MSG_ERROR,'warning');	
			}
    		consultarOnClick();
			hideSpinner();
		},
		error : function(e) {
			console.log("ERROR: ", e);
			hideSpinner();
		}
	});
}

function borrarArchivoConfirmOnClick() {
	$(MODAL_CONFIRMAR_BORRAR).modal("hide");
	
	var idDocumentoPublicado = $("#hdnIdDocumento").val().trim();
	showSpinner();
	
	var url = './borrarArchivo';
	$.ajax({
    	url: url,
    	data: {idDocumentoPublicado: idDocumentoPublicado}, 
    	type: "post", async: false, cache: false, crossDomain: false,
    	success: function(data){
			console.log(data);
			var json = JSON.parse(data);
			if(json.STATUS == "OK") {
    			Swal.fire('Publicación de Documentos',Mensajes_js.MENSAJE_BORRAR_ARCHIVO_EXITO,'success');	
			} else {
				Swal.fire('Publicación de Documentos',Mensajes_js.MENSAJE_BORRAR_ARCHIVO_ERROR,'warning');	
			}
    		consultarOnClick();
    	}
    });
    hideSpinner();
}
var MODAL_DATOS_EXCEL = "#modalDescargarExcelClientes";

var periodo = 15;
var intervalWariningMsg = "";
var rowsPerPage =10;
var table;
var archivosPermitidosDescargar = 10;

Mensajes_js = {
		MENSAJE_EXCEL_SIN_DATOS : 'No existe información para descargar',
	};

$(document).ready(function(){
	
    var url = './consultar/getStartDate';

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
	            
    var url = './consultar/getConfiguracion';
    $.ajax({
    	url: url,
    	data: {NombreCampo:"Multiple.cfdi.date.interval"},
    	type: "post", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		periodo = data;
    	}
    });
    
    var url = './consultar/getConfiguracion';
    $.ajax({
    	url: url,
    	data: {NombreCampo:"Multiple.cfdi.date.interval.warning.message"},
    	type: "post", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		intervalWariningMsg = data;
    	}
    });
    
    var url = './consultar/getConfiguracion';
    $.ajax({
    	url: url,
    	data: {NombreCampo:"Multiple.cfdi.data.archivosDescargar"},
    	type: "post", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		archivosPermitidosDescargar = data * 1;
    	}
    });

    var url = './consultar/getConfiguracion';
    $.ajax({
    	url: url,
    	data: {NombreCampo:"Multiple.cfdi.data.grid.rowsPerPage"},
    	type: "post", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		rowsPerPage = data;
    	}
    });
    
    var url = './consultar/ObtenerTipoComprobante';
    $.ajax({
    	url: url,
    	type: "post", dataType: "json", async: false, cache: false, crossDomain: false,
    	success: function(data){
    		var selector = document.getElementById("listTipTicket");
    		$('#listTipTicket').append(new Option("Seleccione tipo de comprobante", ""));
    		var nCont = 0;
    		for(let item of data)
    			{
    			nCont++;
    			selector.options[nCont] = new Option(item.descripcionTimbrado,item.idComprobante);
    			}
    		result = data;
        }
    });
    
    var url = './consultar/ObtenerTipoOrigen';
    $.ajax({
    	url: url,
    	type: "post", dataType: "json", async: false, cache: false, crossDomain: false,
    	success: function(data){
    		var selector = document.getElementById("listOrigen");
    		$('#listOrigen').append(new Option("Seleccione origen", "0"));
    		var nCont = 0;
    		for(let item of data)
    			{
    			nCont++;
    			selector.options[nCont] = new Option(item.descripcion,item.idOrigen);
    			//console.log(item.descripcion + "- " + item.idOrigen);
    			}
    		result = data;
        }
    });    
      
    releaseEventEmpty("rfcInput");
    releaseEventEmpty("uuidInput");
    releaseEventEmpty("ticketVentaInput");
    releaseEventEmpty("montoInput");
    
    $("#dateHasta").mask("00/00/0000");
    $("#dateDesde").mask("00/00/0000");
    $("#uuidInput").mask("AAAAAAAA-AAAA-AAAA-AAAA-AAAAAAAAAAAA");
    
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
	$("#uuidInput_validation").empty();
	$("#ticketVentaInput_validation").empty();
	$("#montoInput_validation").empty();
    $("#messages_consultas_multiples").empty();

	var dateDesdeValue = $("#dateDesde").val().trim();
	var dateHastaValue = $("#dateHasta").val().trim();
	var rfc = $("#rfcInput").val().toUpperCase().trim();
	var uuid = $("#uuidInput").val().toUpperCase().trim();
	var ticket = $("#ticketVentaInput").val().trim();
	var cTipo = $("#listTipTicket option:selected").val();
	var idOrigen = $("#listOrigen option:selected").val();
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
	
	if (uuid != "" && uuid.length < 36){
        messageDanger("#uuidInput_validation", "Solo has ingresado " + uuid.length + " dígitos, por favor completa a 36 caracteres");
        addDanger("uuidInput");
        return;
	}
	
	if (ticket != "" && ticket.length < 19){
        messageDanger("#ticketVentaInput_validation", "Solo has ingresado " + ticket.length + " dígitos, por favor completa a 19 dígitos");
        addDanger("ticketVentaInput");
        return;
	}
	
	if (monto != "" && !validateMontoInput(monto)) {
        messageDanger("#montoInput_validation", "Monto inválido");
        addDanger("montoInput");
        return;
    }
    		
	showSpinner();
	$('#btnBuscarFacturas').prop("disabled", true);
	
	var url = './consultar/listarFacturaMultiple?dateDesde='+dateDesdeValue+'&dateHasta='+dateHastaValue+'&rfc='+rfc+'&uuid='+uuid+'&ticket='+ticket+'&pageNumber=0'+'&start=0' + '&rowsPerPage=' + rowsPerPage + '&tipocomprobante=' + cTipo + '&pidOrigen=' + idOrigen + '&pmonto=' + monto;
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
				$('#btnBuscarFacturas').prop("disabled", false);
			}
		},
		error: function(e) {
			console.log("ERROR: ", e);
			hideSpinner();
			$('#btnBuscarFacturas').prop("disabled", false);
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
		$("#divWarningExcelClientes").html(Mensajes_js.MENSAJE_EXCEL_SIN_DATOS);
		return;
	}
	
	$("#date_desde_validation").empty();
	$("#date_hasta_validation").empty();
	$("#rfcInput_validation").empty();
	$("#uuidInput_validation").empty();
	$("#ticketVentaInput_validation").empty();
    $("#messages_consultas_multiples").empty();

	var dateDesdeValue = $("#dateDesde").val().trim();
	var dateHastaValue = $("#dateHasta").val().trim();
	var rfc = $("#rfcInput").val().toUpperCase().trim();
	var uuid = $("#uuidInput").val().toUpperCase().trim();
	var ticket = $("#ticketVentaInput").val().trim();
	var cTipo = $("#listTipTicket option:selected").val();
	var idOrigen = $("#listOrigen option:selected").val();
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
	
	if (uuid != "" && uuid.length < 36){
        messageDanger("#uuidInput_validation", "Solo has ingresado " + uuid.length + " dígitos, por favor completa a 36 caracteres");
        addDanger("uuidInput");
        return;
	}
	
	if (ticket != "" && ticket.length < 19){
        messageDanger("#ticketVentaInput_validation", "Solo has ingresado " + ticket.length + " dígitos, por favor completa a 19 dígitos");
        addDanger("ticketVentaInput");
        return;
	}
	
	if (monto != "" && !validateMontoInput(monto)) {
        messageDanger("#montoInput_validation", "Monto inválido");
        addDanger("montoInput");
        return;
    }

	$('#btnDescargaxlsx').prop("disabled", true);
	showSpinner();

	var url = './consultar/descargaXlsx?dateDesde='+dateDesdeValue+'&dateHasta='+dateHastaValue+'&rfc='+rfc+'&uuid='+uuid+'&ticket='+ticket+'&pageNumber=0'+'&start=0' + '&rowsPerPage=' + rowsPerPage + '&tipocomprobante=' + cTipo + '&pidOrigen=' + idOrigen + '&pmonto=' + monto;
	let a = document.createElement('a');
	a.href = url;
	document.body.appendChild(a);
	a.click();
	document.body.removeChild(a);
	$('#btnDescargaxlsx').prop("disabled", false);
	hideSpinner();
	return; 
}

function rfcValidationBD(rfc) {
	var result = false;

	if (validateRfcInput(rfc)) {
        var url = './consultar/validarRfcMultiple';
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

function obtenerCorreoMultiple(){
    
	var archivos = nombresArchivosList();

	if (archivos=="" || archivos.indexOf(",") == -1) {
		Swal.fire(
				  'Facturación Sodimac',
				  'Favor de seleccionar al menos dos documentos',
				  'warning'
				)
		return;
	}
	
    $("#correo_electronico_anterior_mult").val($("#email_multiples_factura").val());
    addSuccess("correo_electronico_anterior_mult");
	eventosReenvioMultiple();
	$("#modalReenvioMultiple").modal();
}

function reenviarFacturaMultiple(){

	var rfc = $("#rfcInput").val().toUpperCase().trim();

	var emailCC = $("#correo_electronico_nuevo_mult").val().toLowerCase().trim();
	if (uuid=="") return;

	if (emailCC == ""){
	    messageDanger("#correo_electronico_nuevo_mult_validation", campos_vacios_message);
	    addDanger("correo_electronico_nuevo_mult");
	    return;
	}

	if (!validateEmail(emailCC)){
	    messageDanger("#correo_electronico_nuevo_mult_validation", mensage_mail_invalido);
	    addDanger("correo_electronico_nuevo_mult");
	    return;
	}

	var archivos = nombresArchivosList();
	
	if (archivos=="" || archivos.indexOf(",") == -1){
		Swal.fire(
				  'Facturación Sodimac',
				  'Favor de seleccionar al menos dos documentos',
				  'warning'
				)
		return;
	}
	
	$('#enviarUserDateMultiple').prop("disabled", true);
	var url = './consultar/reenvioFacturaMultiple';

	$.ajax({
		url: url,
		data: {rfc:rfc, id:archivos, eMailCC: emailCC},
		type: "get", async: true, cache: false, crossDomain: false,
		success: function(data){

			if (data == "success"){
				$("#modalReenvioMultiple").modal("hide");
				$('.modal-backdrop').remove();
	    		Swal.fire(
	    				  'Facturación Sodimac',
	    				  'Se han reenviado los documentos seleccionados',
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

function eventosReenvioMultiple(){
	releaseEventEmpty("correo_electronico_nuevo_mult");
	releaseEventEmpty("correo_electronico_anterior_mult");
	
    releaseEventCopiPaste("#correo_electronico_anterior_mult", "paste", "#correo_electronico_anterior_mult_validation", "El sistema no permite pegar, ¡Gracias!");
    releaseEventCopiPaste("#correo_electronico_anterior_mult", "copy", "#correo_electronico_anterior_mult_validation", "El sistema no permite copiar, ¡Gracias!");
    releaseEventCopiPaste("#correo_electronico_anterior_mult", "dragover", "#correo_electronico_anterior_mult_validation", "El sistema no permite drag & drop, ¡Gracias!");
    releaseEventCopiPaste("#correo_electronico_nuevo_mult", "paste", "#correo_electronico_nuevo_mult_validation", "El sistema no permite pegar, ¡Gracias!");
    releaseEventCopiPaste("#correo_electronico_nuevo_mult", "copy", "#correo_electronico_nuevo_mult_validation", "El sistema no permite copiar, ¡Gracias!");
    releaseEventCopiPaste("#correo_electronico_nuevo_mult", "dragover", "#correo_electronico_nuevo_mult_validation", "El sistema no permite drag & drop, ¡Gracias!");
}

function nombresArchivosList() {
	var nombreList="";
	
	dTotClientesMultipleObj.filter(function (_dClientesMultipleObj) {
		if (_dClientesMultipleObj.checked) {
			nombreList += (nombreList=="" ? _dClientesMultipleObj.nombreArchivo : "," + _dClientesMultipleObj.nombreArchivo);
		}
	});
	
	return nombreList;
}

function load_table_style() {
	//Config dataTable devices.
	table = $('#data').DataTable({
		"order": [[4, "desc"]],
		"aaSorting": [],
		"columnDefs": [{
			targets: 0,
			orderable: false
		},{
			targets: 10,
			orderable: false,
            render: function (data, type, row, meta) {
                if (type === 'display') {
                	var posComprobante = row[11].indexOf("value=") + 7;
                	var idComprobante = row[11].substr(posComprobante, 1);
                	
                	var periodo = 60;
                	
                	var doc = new DOMParser().parseFromString(row[5], "text/xml");
                	var elem = doc.getElementsByTagName("span")[0];
                	var fechaTimbrado = elem.innerHTML;
                	
                	var idDateDesdeArr = fechaTimbrado.substr(0,10).split("-");
                	var idDateDesdeArrHoras = fechaTimbrado.substr(11,8).split(":");
                	var dateD = new Date ( (+idDateDesdeArr[0]), (+idDateDesdeArr[1])-1, (+idDateDesdeArr[2]), idDateDesdeArrHoras[0], idDateDesdeArrHoras[1], idDateDesdeArrHoras[2] );
                	var dateH = new Date ();
                	
                	//86400000=1000*60*60*24 mileseconds = 1 dia
                	var result = (dateH.getTime() - dateD.getTime()) > (86400000 * (periodo-1));
                	
                	//Deshabilitar refacturación en caso de no ser I=Ingreso o la fecha de tembrado sea mayor a 60 dias
                    if (idComprobante != "I" || result) {
                        //data = data.replace("@@@", "");
                    }
                }
                return data;
            }
		},{
			targets: 0,
			checkboxes: {
               selectRow: true
            }
		}],
		"select": {
		    style: "multi"
		},		
		"searching": true,
		"paging": true,
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

function ticketVentaInput_onkeypress() {
	var x = event.keyCode;
	var w = event.which;
	//32=space , 46=Suprimir key, w=17 y x=86 = ctrl-V
	  if( x==undefined || x==32 || x==46 || (w==17 && x==86)){
	  } else {
		  	var re = new RegExp("^[0-9]*$");
			var isValid = re.test(String.fromCharCode(x));
		    if (!isValid) {
		    	event.preventDefault();
		    }
	  }

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


function releaseEventticketVentaInputEmpty() {
	
	$("#ticketVentaInput_validation").empty();
    var valor = $("#ticketVentaInput").val().trim();
    
    var result = validateTicketVentaInput(valor);
    if (!result) {
        messageDanger("#ticketVentaInput_validation", "Ticket de venta inválido");
        addDanger("ticketVentaInput");
        return false;
    }
    
    addSuccess("ticketVentaInput");
    return true;

}

function validateTicketVentaInput(textoValidar) {

	var re = new RegExp("^[0-9]*$");
    return re.test(textoValidar);
}


function uuidInput_onkeypress() {
	var x = event.keyCode;
	var w = event.which;
	//32=space , 46=Suprimir key, w=17 y x=86 = ctrl-V
	  if( x==undefined || x==32 || x==46 || (w==17 && x==86)){
	  } else {
		  	var re = new RegExp("^[0-9a-zA-Z-]*$");
			var isValid = re.test(String.fromCharCode(x));
		    if (!isValid) {
		    	event.preventDefault();
		    }
	  }

}

function releaseEventuuidInputEmpty() {
	
	$("#uuidInput_validation").empty();
    var valor = $("#uuidInput").val().trim();
    
    var result = validateUuidInput(valor);
    if (!result) {
        messageDanger("#uuidInput_validation", "UUID inválido");
        addDanger("uuidInput");
        return false;
    }
    
    addSuccess("uuidInput");
    return true;

}

function validateUuidInput(textoValidar) {

	var re = new RegExp("^[0-9a-zA-Z-]*$");
    return re.test(textoValidar);
}

function btnDescargaMultiple_onClick() {

	var archivos = table.column(0).checkboxes.selected();
	
	if (archivos == null || archivos.length < 2) {
		Swal.fire(
				  'Facturación Sodimac',
				  'Favor de seleccionar al menos dos documentos',
				  'warning'
				)
		return;
	}
	
	if (archivos.length > archivosPermitidosDescargar) {
		Swal.fire(
				  'Facturación Sodimac',
				  'El número de archivos a descargar es mayor al permitido por el sistema',
				  'warning'
				)
		return;		
	}
	
	var url = './consultar/descargarMultiple/'+archivos.join().replaceAll('</span>','').replaceAll('<span>','');
	
	let a = document.createElement('a');
	a.href = url;
	document.body.appendChild(a);
	a.click();
	document.body.removeChild(a);
}

function rfcRefactOnblur() {
	
    var valor = $("#rfcRefact").val().trim().toUpperCase();
    if (valor == "") {
        addDanger("rfcRefact");
        return false;    	
    }
    
    var result = validateRfcInput(valor);
    if (!result) {
        messageDanger("#rfcRefact_validation", rfc_no_valido);
        addDanger("rfcRefact");
        return false;
    }
    
    
    var cliente = existeRFC(valor);
    
    $('#subCfdiDown').empty();
    $('#subCfdiDown').append(new Option("Seleccione uso de CFDI", ""));
    for (var i = 0; i < cliente.usosCfdi.length; i++) {
        $('#subCfdiDown').append(new Option(cliente.usosCfdi[i].id + "-" + cliente.usosCfdi[i].descripcion, cliente.usosCfdi[i].id));
    }
    
    $('#selMetodoPago').empty();
    $('#selMetodoPago').append(new Option("Seleccione método de pago", ""));
    for (var i = 0; i < cliente.metodosPago.length; i++) {
        $('#selMetodoPago').append(new Option(cliente.metodosPago[i].id + "-" + cliente.metodosPago[i].descripcion, cliente.metodosPago[i].id));
    }

    addSuccess("rfcRefact");
    return true;

}

function rfcRefactOnkeypress(event){
	var keyCode = event.keyCode || event.which;
    var re = new RegExp($("#hdnExpresionRegularRfcCaracteres").val());
    var isValid = re.test(String.fromCharCode(keyCode));
    if (!isValid) {
    	event.preventDefault();
    }
            
}

function razonSocialRefactOnblur() {
	
    var valor = $("#razonSocialRefact").val().trim();
    if (valor == "") {
        addDanger("razonSocialRefact");
        return false;    	
    }
    
    var result = validateRazonSocial(valor);
    if (!result) {
        messageDanger("#razonSocialRefact_validation", mensaje_razon_social_no_valido);
        addDanger("razonSocialRefact");
        return false;
    }
    
    addSuccess("razonSocialRefact");
    return true;

}

function razonSocialRefactOnkeypress(event){
	var keyCode = event.keyCode || event.which;
    var re = new RegExp($("#hdnExpresionRegularRZ").val());
    var isValid = re.test(String.fromCharCode(keyCode));
    if (!isValid) {
    	event.preventDefault();
    }
            
}

function correoRefactOnblur() {
	
    var valor = $("#correoRefact").val().trim().toLowerCase();
    if (valor == "") {
        addDanger("correoRefact");
        return false;    	
    }
    
    var result = validateEmail(valor);
    if (!result) {
        messageDanger("#correoRefact_validation", mensage_mail_invalido);
        addDanger("correoRefact");
        return false;
    }
    
    addSuccess("correoRefact");
    return true;

}



function obraRefactOnblur() {
	
    var valor = $("#obraRefact").val().trim();
    if (valor != "") {
        var result = validateObra(valor);
        if (!result) {
            messageDanger("#obraRefact_validation", mensaje_nombre_obra_no_valido);
            addDanger("obraRefact");
            return false;
        }
    }
        
    addSuccess("obraRefact");
    return true;

}

function obraRefactOnkeypress(event){
	var keyCode = event.keyCode || event.which;
    var re = new RegExp($("#hdnExpresionRegularObra").val());
    var isValid = re.test(String.fromCharCode(keyCode));
    if (!isValid) {
    	event.preventDefault();
    }
            
}

function responsableRefactOnblur() {
	
    var valor = $("#responsableRefact").val().trim();
    if (valor != "") {
        var result = validateResponsableObra(valor);
        if (!result) {
            messageDanger("#responsableRefact_validation", mensaje_responsable_obra_no_valido);
            addDanger("responsableRefact");
            return false;
        }
    }
        
    addSuccess("responsableRefact");
    return true;

}

function responsableRefactOnkeypress(event){
	var keyCode = event.keyCode || event.which;
    var re = new RegExp($("#hdnExpresionRegularResponsableObra").val());
    var isValid = re.test(String.fromCharCode(keyCode));
    if (!isValid) {
    	event.preventDefault();
    }
            
}

function refacturarOnClick() {
	
	$("#rfcRefact_validation").empty();
	$("#razonSocialRefact_validation").empty();
	$("#cfdi_validation").empty();
	$("#selMetodoPago_validation").empty();
	$("#correoRefact_validation").empty();
	$("#obraRefact_validation").empty();
	$("#responsableRefact_validation").empty();
	
	var idFacturaPac = $("#hdnIdFacturaPac").val();
	var rfc = $("#rfcRefact").val().toUpperCase().trim();
	var razonSocial = $("#razonSocialRefact").val().toUpperCase().trim();
	var usoCfdi = $("#subCfdiDown").val();
	var metodoPago = $("#selMetodoPago").val();
	var correo = $("#correoRefact").val().trim();
	var obra = $("#obraRefact").val().trim();
	var responsable = $("#responsableRefact").val().trim();
	
	if (rfc==""){
        messageDanger("#rfcRefact_validation", rfc_vacío);
        addDanger("rfcRefact");
        return;
	}

	if (!validateRfcInput(rfc)){
        messageDanger("#rfcRefact_validation", rfc_no_valido);
        addDanger("rfcRefact");
        return;
    }

	if (!rfcValidationBD(rfc)){
        messageDanger("#rfcRefact_validation", "RFC no esta en la base de datos");
        addDanger("rfcRefact");
        return;
	}

	if (razonSocial==""){
        messageDanger("#razonSocialRefact_validation", mensaje_razon_social_vacio);
        addDanger("razonSocialRefact");
        return;
	}

	if (!validateRazonSocial(razonSocial)){
        messageDanger("#razonSocialRefact_validation", mensaje_razon_social_no_valido);
        addDanger("razonSocialRefact");
        return;
    }

	if (usoCfdi == ""){
		messageDanger("#cfdi_validation", cfdi_no_valido);
		return;
	}
		
	if (metodoPago == ""){
		messageDanger("#selMetodoPago_validation", metodoPago_no_valido);
		return;
	}

	if (correo==""){
        messageDanger("#correoRefact_validation", email_vacio);
        addDanger("correoRefact");
        return;
	}

    if (!validateEmail(correo)) {
        messageDanger("#correoRefact_validation", mensage_mail_invalido);
        addDanger("correoRefact");
        return;
    }

    if (obra != "" && !validateObra(obra)) {
        messageDanger("#obraRefact_validation", mensaje_nombre_obra_no_valido);
        addDanger("obraRefact");
        return;
    }

    if (responsable != "" && !validateResponsableObra(responsable)) {
        messageDanger("#responsableRefact_validation", mensaje_responsable_obra_no_valido);
        addDanger("responsableRefact");
        return;
    }

    showSpinner();
	$('#btnRefacturar').prop("disabled", true);
	
	var url = './consultar/refacturar';
	const fragmentDiv = document.getElementById("fragmentDiv");

	$.ajax({
		type: "get",
		async: true,
		cache: false,
		contentType: "application/json",
		url: url,
		data: {idFactura:idFacturaPac, rfc:rfc, usoCfdi:usoCfdi, metodoPago:metodoPago, razonSocial:razonSocial, email:correo, nombreObra:obra, responsableObra:responsable},
		timeout: 150000,
		success: function(response) {
			if (response.indexOf("Inicio de Ses") != -1) {
				window.location.href = './';
			} else {

	    		if (response == "success"){
	    			$("#refacturarModal").modal("hide");
	    			$('.modal-backdrop').remove();
	        		Swal.fire(
	        				  'Facturación Sodimac',
	        				  'Se ha retimbrado la factura correctamente.',
	        				  'success'
	        				)
	    		}
	    		if (response != "success"){
	        		Swal.fire(
	        				  'Facturación Sodimac',
	        				  'No se retimbró la factura, ' + response,
	        				  'warning'
	        				)
	    		}

			}
			$('#btnRefacturar').prop("disabled", false);
			hideSpinner();
		},
		error: function(e) {
			console.log("ERROR: ", e);
			$('#btnRefacturar').prop("disabled", false);
			hideSpinner();
		},
		done: function(e) {
			console.log("DONE");
		}
	});        
}

function listaTipoComprobante(){
	var result;
    var url = '/consultar/ObtenerTipoComprobante';
    $.ajax({
    	url: url,
    	//data: {rfc: rfc},
    	type: "post", dataType: "json", async: false, cache: false, crossDomain: false,
    	success: function(data){
    		result = data;
    		console.log(result);
        }
    });
    
    return result;
}

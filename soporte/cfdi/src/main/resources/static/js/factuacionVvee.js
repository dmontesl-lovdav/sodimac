var MODAL_DATOS_EXCEL = "#modalDescargarExcelComplemento";

var periodo = 15;
var intervalWariningMsg = "";
var rowsPerPage =10;
var table;
var archivosPermitidosDescargar = 10;

Mensajes_js = {
		MENSAJE_EXCEL_SIN_DATOS : 'No existe información para descargar',
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
    	data: {NombreCampo:"date.interval.facturas.vvee"},
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
    
    var url = '/cfdi/facturacionVvee/obtenerTiendas';
    $.ajax({
    	url: url,
    	type: "post", dataType: "json", async: false, cache: false, crossDomain: false,
    	success: function(data){
    		var selector = document.getElementById("listTiendas")
    		$('#listTiendas').append(new Option("Seleccione Tienda", ""));
    		var nCont = 0;
    		for(let item of data)
    			{
    			nCont++;
    			selector.options[nCont] = new Option(item.id + "-" + item.nombre,item.id);
    			}
    		result = data;
        }
    });    
      
    releaseEventEmpty("TicketInput");
    
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
	$("#ticketInput_validation").empty();

	var dateDesdeValue = $("#dateDesde").val().trim();
	var dateHastaValue = $("#dateHasta").val().trim();
	var cTienda = $("#listTiendas option:selected").val();
	var ticket = $("#TicketInput").val().trim();

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
		
	if (ticket != "" && !validateTicketInput(ticket)) {
        messageDanger("#ticketInput_validation", "Ticket inválido");
        addDanger("TicketInput");
        return;
    }
	
	showSpinner();
	$('#btnBuscarComisiones').prop("disabled", true);
	
	var url = './listarFacturacionVvee?dateDesde='+dateDesdeValue+'&dateHasta='+dateHastaValue+'&pageNumber=0'+'&start=0' + '&rowsPerPage=' + rowsPerPage + '&pTienda=' + cTienda + '&pTicket=' + ticket;
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
				$('#btnBuscarComisiones').prop("disabled", false);
			}
		},
		error: function(e) {
			console.log("ERROR: ", e);
			hideSpinner();
			$('#btnBuscarComisiones').prop("disabled", false);
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
	
	var dateDesdeValue = $("#dateDesde").val().trim();
	var dateHastaValue = $("#dateHasta").val().trim();
	var cTienda = $("#listTiendas option:selected").val();
	var ticket = $("#TicketInput").val().trim();

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
		
	if (ticket != "" && !validateTicketInput(ticket)) {
        messageDanger("#ticketInput_validation", "Ticket inválido");
        addDanger("TicketInput");
        return;
    }
	
	$('#btnDescargaxlsx').prop("disabled", true);
	showSpinner();

	var url = './listarFacturacionVvee/descargaXlsx?dateDesde='+dateDesdeValue+'&dateHasta='+dateHastaValue+'&pTienda=' + cTienda +'&pTicket=' + ticket;
	let a = document.createElement('a');
	a.href = url;
	document.body.appendChild(a);
	a.click();
	document.body.removeChild(a);
	hideSpinner();
	$('#btnDescargaxlsx').prop("disabled", false);
	return; 
}


function ticketInput_onkeypress() {
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

function releaseEventTicketInputEmpty() {
	
	$("#ticketInput_validation").empty();
    var valor = $("#TicketInput").val().trim();
    
    if (valor != null && valor != "") {
	    var result = validateTicketInput(valor);
	    if (!result) {
	        messageDanger("#ticketInput_validation", "Ticket inválido");
	        addDanger("TicketInput");
	        return false;
	    }
	}
    
    addSuccess("TicketInput");
    return true;

}

function validateTicketInput(textoValidar) {

	var re = new RegExp("^[0-9]*$");
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

function obtenerCorreo(uuid, email)
{
	if (uuid=="") return;
	$("#uuid").val(uuid);
	$('#enviarUserDate').prop("disabled", false);
	
    $("#correo_electronico_anterior").val(email);
	addSuccess("correo_electronico_anterior");
	eventosReenvio();
	$("#responsive2").modal();
}

function eventosReenvio(){
	releaseEventEmpty("correo_electronico_nuevo");
	releaseEventEmpty("correo_electronico_anterior");
}

function validateEmail(email) {

	var re = new RegExp($("#hdnExpresionRegularEmail").val());
    return re.test(email);
}

function reenviarFactura()
{
	var uuid = $("#uuid").val().trim();
	var email = $("#correo_electronico_anterior").val().toLowerCase().trim();
	var emailCC = $("#correo_electronico_nuevo").val().toLowerCase().trim();

	if (uuid=="") return;
	
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
    
    if (emailCC == ""){
        messageDanger("#correo_electronico_nuevo_validation", campos_vacios_message);
        addDanger("correo_electronico_nuevo");
        return;
    }

	if (!validateEmail(emailCC)){
        messageDanger("#correo_electronico_nuevo_validation", mensage_mail_invalido);
        addDanger("correo_electronico_nuevo");
        return;
    }

	$('#enviarUserDate').prop("disabled", true);
	var url = './reenvioFactura';

    $.ajax({
    	url: url,
    	data: {uuid:uuid, eMailCC: emailCC, email: email},
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
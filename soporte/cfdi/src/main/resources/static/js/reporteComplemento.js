var periodo = 15;
var intervalWariningMsg = "";
var rowsPerPage =10;
var table;
var archivosPermitidosDescargar = 10;

Mensajes_js = {
		
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
      
    releaseEventEmpty("rfcInput");
    
    $("#dateHasta").mask("00/00/0000");
    $("#dateDesde").mask("00/00/0000");
    
 /*   const current = new Date();
	const year = current.getFullYear();
	let month = current.getMonth() + 1;
	let day = current.getDate();
	current.setDate(current.getDate() - 1);
	let day1 = current.getDate();
	
	month = month.toString().padStart(2, "0");
	day = day.toString().padStart(2, "0");
	day1 = day1.toString().padStart(2, "0");
	
	$("#dateDesde").val( day1 + '/' + month + '/' + year );
	$("#dateHasta").val( day + '/' + month + '/' + year );*/
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

	var dateDesdeValue = $("#dateDesde").val().trim();
	var dateHastaValue = $("#dateHasta").val().trim();
	var rfc = $("#rfcInput").val().toUpperCase().trim();
	var ticket = $("#ticketVentaInput").val().trim();
	var uuid = $("#uuidInput").val().trim();
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
	
	showSpinner();
	$('#btnBuscarComplementos').prop("disabled", true);

	var url = './listarReporteComplementos?dateDesde='+dateDesdeValue+'&dateHasta='+dateHastaValue+'&rfc='+rfc+'&pageNumber=0'+'&start=0' + '&rowsPerPage=' + rowsPerPage + '&ticket=' + ticket + '&uuid=' + uuid + '&monto=' + monto;
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
	
	$("#date_desde_validation").empty();
	$("#date_hasta_validation").empty();
	$("#rfcInput_validation").empty();
	
	var dateDesdeValue = $("#dateDesde").val().trim();
	var dateHastaValue = $("#dateHasta").val().trim();
	var rfc = $("#rfcInput").val().toUpperCase().trim();
	var ticket = $("#ticketVentaInput").val().trim();
	var uuid = $("#uuidInput").val().trim();
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
	
	$('#btnDescargaxlsx').prop("disabled", true);
	showSpinner();

	var url = './listarReporteComplementos/descargaXlsx?dateDesde='+dateDesdeValue+'&dateHasta='+dateHastaValue+'&rfc='+rfc+'&ticket=' + ticket + '&uuid=' + uuid + '&monto=' + monto;
	let a = document.createElement('a');
	a.href = url;
	document.body.appendChild(a);
	a.click();
	document.body.removeChild(a);
	hideSpinner();
	$('#btnDescargaxlsx').prop("disabled", false);
	return; 
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
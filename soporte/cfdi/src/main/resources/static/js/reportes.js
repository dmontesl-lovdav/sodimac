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
    
    var url = './obtenerCanales';
    $.ajax({
    	url: url,
    	type: "post", dataType: "json", async: false, cache: false, crossDomain: false,
    	success: function(data){
    		var selector = document.getElementById("listCanales")
    		$('#listCanales').append(new Option("Seleccione un canal", ""));
    		var nCont = 0;
    		for(let item of data)
    			{
    			nCont++;
    			selector.options[nCont] = new Option(item.canal,item.canal);
    			}
    		result = data;
        }
    });

	var url = './obtenerTiendas';
    $.ajax({
    	url: url,
    	type: "post", dataType: "json", async: false, cache: false, crossDomain: false,
    	success: function(data){
    		var selector = document.getElementById("listTiendas")
    		$('#listTiendas').append(new Option("Seleccione una tienda", ""));
    		var nCont = 0;
    		for(let item of data)
    			{
    			nCont++;
    			selector.options[nCont] = new Option(item.tienda,item.tienda);
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
	
	const current1 = new Date();
	current1.setMonth(current1.getMonth() - 1);
	const year1 = current1.getFullYear();
	let month1 = current1.getMonth() + 1;
	let day1 = current1.getDate();
	
	month = month.toString().padStart(2, "0");
	day = day.toString().padStart(2, "0");
	
	month1 = month1.toString().padStart(2, "0");
	day1 = day1.toString().padStart(2, "0");
	
	$("#dateDesde").val( day1 + '/' + month1 + '/' + year1 );
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
	var ticket = $("#ticketVentaInput").val().trim();
	var tienda = $("#listTiendas option:selected").val();
	var canal = $("#listCanales option:selected").val();

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
	$('#btnBuscarTablero').prop("disabled", true);
	$('#btnDescargaxlsx').prop("disabled", true);
	$('#btnDescargaDetallexlsx').prop("disabled", true);
	
	var url = './listarTablero?dateDesde='+dateDesdeValue+'&dateHasta='+dateHastaValue+'&ticket='+ticket+'&pageNumber=0'+'&start=0' + '&rowsPerPage=' + rowsPerPage + '&canal=' + canal + '&tienda=' + tienda;
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
				$('#btnBuscarTablero').prop("disabled", false);
				$('#btnDescargaxlsx').prop("disabled", false);
				$('#btnDescargaDetallexlsx').prop("disabled", false);
			}
		},
		error: function(e) {
			console.log("ERROR: ", e);
			hideSpinner();
			$('#btnBuscarTablero').prop("disabled", false);
			$('#btnDescargaxlsx').prop("disabled", false);
			$('#btnDescargaDetallexlsx').prop("disabled", false);
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
	
	var dateDesdeValue = $("#dateDesde").val().trim();
	var dateHastaValue = $("#dateHasta").val().trim();
	var ticket = $("#ticketVentaInput").val().trim();
	var tienda = $("#listTiendas option:selected").val();
	var canal = $("#listCanales option:selected").val();

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
	$('#btnBuscarTablero').prop("disabled", true);
	$('#btnDescargaxlsx').prop("disabled", true);
	$('#btnDescargaDetallexlsx').prop("disabled", true);

	let url = './listarTablero/descargaXlsx?dateDesde='+dateDesdeValue+'&dateHasta='+dateHastaValue+'&ticket='+ticket+'&canal=' + canal + '&tienda=' + tienda;
	let a = document.createElement('a');
	a.href = url;
	document.body.appendChild(a);
	a.click();
	document.body.removeChild(a);
	
	$('#btnBuscarTablero').prop("disabled", false);
	$('#btnDescargaxlsx').prop("disabled", false);
	$('#btnDescargaDetallexlsx').prop("disabled", false);
	hideSpinner();
	
	/*ejecutarDescarga(url, lastStep);*/
	
	return; 
}

/*async function ejecutarDescarga(url) {
	let myPromise = new Promise(function(resolve) {
    	let a = document.createElement('a');
		a.href = url;
		document.body.appendChild(a);
		a.click();
		document.body.removeChild(a);
			
		$('#btnBuscarTablero').prop("disabled", false);
		$('#btnDescargaxlsx').prop("disabled", false);
		$('#btnDescargaDetallexlsx').prop("disabled", false);
		hideSpinner();
		resolve('OK');
  	});
  	console.log(await myPromise);
}*/

function ejecutarDescarga(url, callback) {
	
    let a = document.createElement('a');
	a.href = url;
	document.body.appendChild(a);
	a.click();
	document.body.removeChild(a);
			
	callback('OK');
}

function lastStep(text) {
	$('#btnBuscarTablero').prop("disabled", false);
	$('#btnDescargaxlsx').prop("disabled", false);
	$('#btnDescargaDetallexlsx').prop("disabled", false);
	hideSpinner();
	
	console.log(text);
}

function descargaDetallexlsxOnClick() {
	
	$("#date_desde_validation").empty();
	$("#date_hasta_validation").empty();
	
	var dateDesdeValue = $("#dateDesde").val().trim();
	var dateHastaValue = $("#dateHasta").val().trim();
	var ticket = $("#ticketVentaInput").val().trim();
	var tienda = $("#listTiendas option:selected").val();
	var canal = $("#listCanales option:selected").val();

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
	$('#btnBuscarTablero').prop("disabled", true);
	$('#btnDescargaxlsx').prop("disabled", true);
	$('#btnDescargaDetallexlsx').prop("disabled", true);	
	
	var url = './listarTablero/procesarDetalleXlsx?dateDesde='+dateDesdeValue+'&dateHasta='+dateHastaValue+'&ticket='+ticket+'&canal=' + canal + '&tienda=' + tienda;
	$.ajax({
		type: "GET",
		//async: false,
		contentType: "application/json",
		url: url,
		//data: objData,
		timeout: 30000,
		success: function(response) {
			if (response.indexOf("Inicio de Ses") != -1) {
				window.location.href = './';
			} else {
				Swal.fire(
			        'Procesando reporte',
			       	'Su reporte se agrego a la lista de procesamiento correctamente. <br/> Utilice el siguiente id de ejecucion para consultarlo: <br/><b>'+response+'</b>',
			        'success'
			    );
				hideSpinner();	
			}
		},
		error: function(e) {
			console.log("ERROR: ", e);
			Swal.fire(
		        'Procesando reporte',
		       	'Ocurrio un problema al intentar registrar el procesamiento de su reporte',
		        'warning'
		    );
			hideSpinner();
		},
		done: function(e) {
			console.log("DONE");
			hideSpinner();
		}
	}); 	
    $('#btnBuscarTablero').prop("disabled", false);
	$('#btnDescargaxlsx').prop("disabled", false);
	$('#btnDescargaDetallexlsx').prop("disabled", false);

	/*var url = './listarTablero/descargaDetalleXlsx?dateDesde='+dateDesdeValue+'&dateHasta='+dateHastaValue+'&ticket='+ticket+'&canal=' + canal + '&tienda=' + tienda;
	let a = document.createElement('a');
	a.href = url;
	document.body.appendChild(a);
	a.click();
	document.body.removeChild(a);*/
 
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

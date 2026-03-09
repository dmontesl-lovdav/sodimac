var MODAL_VALIDAR = "#modalValidar";
var MODAL_CONFIRMAR = "#modalConfirmar";
var MODAL_DATOS_EXCEL = "#modalDescargarExcelPagos";

var periodo = 15;
var intervalWariningMsg = "";
var rowsPerPage =10;
var table;
var archivosPermitidosDescargar = 10;

Mensajes_js = {
		MENSAJE_EXCEL_SIN_DATOS : 'No existe información para descargar',
		MENSAJE_FOLIO_CLIENTE_INCORRECTO : 'Favor de capturar folio de cliente correcto',
		MENSAJE_FOLIO_CLIENTE_INCORRECTO_NO_CUENTA : 'Favor de capturar folio de cliente correcto: 18 o 11 caracteres',
		MENSAJE_ADVERTENCIA_FOLIO_CLIENTE : '¿Esta seguro que desea modificar el folio del cliente?',
		MENSAJE_FOLIO_CLIENTE_EXITO : 'Se ha modificado el folio de cliente con &eacute;xito'
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
    
    var url = './obtenerStatusPagos?tipoPago=P';
    $.ajax({
    	url: url,
    	type: "post", dataType: "json", async: false, cache: false, crossDomain: false,
    	success: function(data){
    		var selector = document.getElementById("listEstatusPagos")
    		$('#listEstatusPagos').append(new Option("Seleccione estatus de pago", ""));
    		var nCont = 0;
    		for(let item of data)
    		{
    			nCont++;
    			if(item.idEstatus == 'PL') {
	    			selector.options[nCont] = new Option(item.descripcionEstatus,item.idEstatus, true, true);
    			} else {
    				selector.options[nCont] = new Option(item.descripcionEstatus,item.idEstatus);
    			}
    		}
    		result = data;
        }
    });    
    
    releaseEventEmpty("montoInput");
    
    $("#dateHasta").mask("00/00/0000");
    $("#dateDesde").mask("00/00/0000");
    $("#listEstatusPagos option[value='PL']").attr("selected", true);
    $("#listEstatusPagos").val("PL");
		
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
	
	console.log('Consulta pagos');
	
	$("#date_desde_validation").empty();
	$("#date_hasta_validation").empty();
	$("#montoInput_validation").empty();

	var dateDesdeValue = $("#dateDesde").val().trim();
	var dateHastaValue = $("#dateHasta").val().trim();
	var cTipo = $("#listEstatusPagos option:selected").val();
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
	
	if (monto != "" && !validateMontoInput(monto)) {
        messageDanger("#montoInput_validation", "Monto inválido");
        addDanger("montoInput");
        return;
    }
		   		
	showSpinner();
	$('#btnBuscarPagos').prop("disabled", true);

	var url = './listarPagos?dateDesde='+dateDesdeValue+'&dateHasta='+dateHastaValue+'&pageNumber=0'+'&start=0' + '&rowsPerPage=' + rowsPerPage + '&tipopago=' + cTipo + '&pmonto=' + monto;
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
				$('#btnBuscarPagos').prop("disabled", false);
			}
		},
		error: function(e) {
			console.log("ERROR: ", e);
			hideSpinner();
			$('#btnBuscarPagos').prop("disabled", false);
		},
		done: function(e) {
			console.log("DONE");
			hideSpinner();
		}
	});        
}

function editarFolioCliente(idPago, edit) {
	var txtFolioCliente = "#txtFolioCliente_" + idPago;
	var spanFolioCliente = "#spanFolioCliente_" + idPago;
	var btnEditfolioCliente = "#btnEditfolioCliente_" + idPago;
	var btnGuardarfolioCliente = "#btnGuardarfolioCliente_" + idPago;
	
	$(txtFolioCliente).prop("hidden", !edit);
	$(spanFolioCliente).prop("hidden", edit);
	
	$(btnEditfolioCliente).prop("hidden", edit);
	$(btnGuardarfolioCliente).prop("hidden", !edit);
	
	if (edit) {
		var folioClienteOriginal = $(spanFolioCliente).text();
		$(txtFolioCliente).val(folioClienteOriginal);
		$(txtFolioCliente).focus();
	}
}

function cancelarEditNumeroCuenta() {
	var idPago = $("#hdIdPago").val();
	editarFolioCliente(idPago, false);
}

function modificarFolioCliente(idPago) {
	if (idPago=="") return;
	
	var txtName = "#txtFolioCliente_" + idPago;
	var folioCliente = $(txtName).val();
	var validaFolio = validaFolioCliente(folioCliente);
	if(validaFolio) {
		
		var lenghtText = folioCliente.length;
	    if(lenghtText == 18 || lenghtText == 11) {
			$(MODAL_CONFIRMAR).modal("show");
			$("#divWarningConfirmar").html(Mensajes_js.MENSAJE_ADVERTENCIA_FOLIO_CLIENTE);
			$("#hdIdPago").val(idPago);
		} else {
			$(MODAL_VALIDAR).modal("show");
			$("#divWarning").html(Mensajes_js.MENSAJE_FOLIO_CLIENTE_INCORRECTO_NO_CUENTA);
			$("#hdIdPago").val(null);
		}
	} else {
		$(MODAL_VALIDAR).modal("show");
		$("#divWarning").html(Mensajes_js.MENSAJE_FOLIO_CLIENTE_INCORRECTO);
		$("#hdIdPago").val(null);
	}
}

function modificarfolioClienteConfirm() {
	$(MODAL_CONFIRMAR).modal("hide");
	var idPago = $("#hdIdPago").val();
	console.log('Confirmar folio de cliente: ' + idPago);

	showSpinner();
	$('#btnBuscarPagos').prop("disabled", true);
	var txtFolioCliente = "#txtFolioCliente_" + idPago;
	var folioCliente = $(txtFolioCliente).val();

	var url = './cambiarFolioCliente';
	$.ajax({
		url: url,
        data: {idPago: parseInt(idPago), folioCliente: folioCliente},
        type: "post", async: false, cache: false, crossDomain: false,
        success: function(data){
			if (data != ""){
                Swal.fire(
    				'Facturación Sodimac',
    				data,
    				'warning'
    			)
            } else {
            	Swal.fire(
        			'Facturación Sodimac',
        			Mensajes_js.MENSAJE_FOLIO_CLIENTE_EXITO,
        			'success'
        		)
        		consultarOnClick();
            }
			$('#btnBuscarPagos').prop("disabled", false);
			hideSpinner();
        },
		error: function(e) {
			console.log("ERROR: ", e);
			$('#btnBuscarPagos').prop("disabled", false);
			hideSpinner();
		},
		done: function(e) {
			console.log("DONE");
		}
	});
}

function validaFolioCliente(folioCliente) {
	if (folioCliente == null || folioCliente=="") {
		return false;
	}
	
	//if (parseInt(folioCliente) == 0) {
	//	return false;
	//}
	
  	var re = new RegExp("^[0-9]*$");
	var isValid = re.test(folioCliente);
    if (!isValid) {
    	return false;
    }
//    var lenghtText = folioCliente.length;
//    if(lenghtText == 18 || lenghtText == 11) {
//		console.log("Número de cuenta bancario correcto");
//	} else {
//		return false;
//	}
    return true;
}

function folioClienteInput_onkeypress() {
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


function cambiarEstatusPago(idPago, estatusPago) {
	console.log('Liberar pago: ' + idPago);

	showSpinner();
	$('#btnBuscarPagos').prop("disabled", true);

	var url = './cambiarEstatusPago';
	$.ajax({
		url: url,
        data: {idPago: parseInt(idPago), estatusPago: estatusPago},
        type: "post", async: false, cache: false, crossDomain: false,
        success: function(data){
			if (data != ""){
                Swal.fire(
    				'Facturación Sodimac',
    				data,
    				'warning'
    			)
            } else {
            	Swal.fire(
        			'Facturación Sodimac',
        			'Se ha cambiado el estatus correctamente',
        			'success'
        		)
        		consultarOnClick();
            }
			$('#btnBuscarPagos').prop("disabled", false);
			hideSpinner();
        },
		error: function(e) {
			console.log("ERROR: ", e);
			$('#btnBuscarPagos').prop("disabled", false);
			hideSpinner();
		},
		done: function(e) {
			console.log("DONE");
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
		$("#divWarningExcelPagos").html(Mensajes_js.MENSAJE_EXCEL_SIN_DATOS);
		return;
	}
	
	$("#date_desde_validation").empty();
	$("#date_hasta_validation").empty();

	var dateDesdeValue = $("#dateDesde").val().trim();
	var dateHastaValue = $("#dateHasta").val().trim();
	var cTipo = $("#listEstatusPagos option:selected").val();
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
	
	if (monto != "" && !validateMontoInput(monto)) {
        messageDanger("#montoInput_validation", "Monto inválido");
        addDanger("montoInput");
        return;
    }

	$('#btnDescargaxlsx').prop("disabled", true);
	showSpinner();
	
	//var url = './listarPagos?dateDesde='+dateDesdeValue+'&dateHasta='+dateHastaValue+'&pageNumber=0'+'&start=0' + '&rowsPerPage=' + rowsPerPage + '&tipopago=' + cTipo;
	var url = './listarPagos/descargaXlsx?dateDesde='+dateDesdeValue+'&dateHasta='+dateHastaValue+'&tipopago=' + cTipo+'&pmonto=' + monto;
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
		}],	
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


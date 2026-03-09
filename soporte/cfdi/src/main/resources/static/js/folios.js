var MODAL_VALIDAR = "#modalValidar";
var MODAL_DATOS_EXCEL = "#modalDescargarExcelFolios";
var MODAL_CONFIRMAR = "#modalConfirmar";
var MODAL_RESULTADO = "#modalResultado";
var MODAL_CONFIRMAR_DESVINCULAR = "#modalConfirmarDesvincular";
	
var periodo = 15;
var intervalWariningMsg = "";
var rowsPerPage = 10;
var table;
var archivosPermitidosDescargar = 10;
var ordenGlobal = 1;

// LOGS DE EVIDENCIA - INICIO
console.log("[FOLIOS] Inicializando variables - periodo:", periodo, "rowsPerPage:", rowsPerPage);
// LOGS DE EVIDENCIA - FIN

Mensajes_js = {
		MENSAJE_SELECCIONA_FACTURA : 'Seleccionar al menos una factura',
		MENSAJE_EXCEL_SIN_DATOS : 'No existe información para descargar',
		MENSAJE_EXCEL_SIN_DATOS_FOLIO : 'No existe información para generar folios',
		MENSAJE_FOLIO_DIFERENTE : 'No es posible generar folio factura con diferentes RFC',
		MENSAJE_ADVERTENCIA_GENERAR_FOLIO : '¿Esta seguro que desea generar el folio?',
		MENSAJE_FOLIO_DESVINCULADO_EXITO : 'Se ha desvinculado el folio correctamente',
		MENSAJE_ADVERTENCIA_DESVINCULAR_FOLIO : '¿Esta seguro que desea desvincular la factura?',
		MENSAJE_FOLIO_GENERADO_EXITO:'Se ha generado el folio _FOLIO_ con &eacute;xito'
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
    		// LOGS DE EVIDENCIA - INICIO
    		console.log("[FOLIOS] Configuración cargada - Multiple.cfdi.date.interval:", data);
    		console.log("[FOLIOS] Variable periodo actualizada a:", periodo);
    		// LOGS DE EVIDENCIA - FIN
    	}
    });
    
    var url = '/cfdi/consultar/getConfiguracion';
    $.ajax({
    	url: url,
    	data: {NombreCampo:"Multiple.cfdi.date.interval.warning.message"},
    	type: "post", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		intervalWariningMsg = data;
    		// LOGS DE EVIDENCIA - INICIO
    		console.log("[FOLIOS] Configuración cargada - Multiple.cfdi.date.interval.warning.message:", data);
    		console.log("[FOLIOS] Variable intervalWariningMsg actualizada a:", intervalWariningMsg);
    		// LOGS DE EVIDENCIA - FIN
    	}
    });

    var url = '/cfdi/consultar/getConfiguracion';
    $.ajax({
    	url: url,
    	data: {NombreCampo:"Multiple.cfdi.data.grid.rowsPerPage"},
    	type: "post", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		rowsPerPage = data;
    		// LOGS DE EVIDENCIA - INICIO
    		console.log("[FOLIOS] Configuración cargada - Multiple.cfdi.data.grid.rowsPerPage:", data);
    		console.log("[FOLIOS] Variable rowsPerPage actualizada a:", rowsPerPage);
    		// LOGS DE EVIDENCIA - FIN
    	}
    });
    
    
    releaseEventEmpty("rfcInput");
    releaseEventEmpty("folioInput");
    
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
  			// LOGS DE EVIDENCIA - INICIO
  			console.log("[FOLIOS - VALIDACIÓN] Rango de fechas excedido!");
  			console.log("[FOLIOS - VALIDACIÓN] - Período máximo permitido:", periodo, "días");
  			console.log("[FOLIOS - VALIDACIÓN] - Mensaje:", intervalWariningMsg.replace("{dias}", periodo));
  			// LOGS DE EVIDENCIA - FIN

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
	ordenGlobal = 1;
	$("#date_desde_validation").empty();
	$("#date_hasta_validation").empty();
	$("#rfcInput_validation").empty();
	$("#folioInput_validation").empty();

	var dateDesdeValue = $("#dateDesde").val().trim();
	var dateHastaValue = $("#dateHasta").val().trim();
	var rfc = $("#rfcInput").val().toUpperCase().trim();
	var folio = $("#folioInput").val().trim();

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
	$('#btnBuscarFolioFacturas').prop("disabled", true);

	// LOGS DE EVIDENCIA - INICIO
	console.log("[FOLIOS - CONSULTAR] Iniciando consulta con parámetros:");
	console.log("[FOLIOS - CONSULTAR] - dateDesde:", dateDesdeValue);
	console.log("[FOLIOS - CONSULTAR] - dateHasta:", dateHastaValue);
	console.log("[FOLIOS - CONSULTAR] - rfc:", rfc);
	console.log("[FOLIOS - CONSULTAR] - folio:", folio);
	console.log("[FOLIOS - CONSULTAR] - rowsPerPage:", rowsPerPage);
	// LOGS DE EVIDENCIA - FIN

	var url = './listarFoliosFacturas?dateDesde='+dateDesdeValue+'&dateHasta='+dateHastaValue+'&rfc='+rfc+'&pageNumber=0'+'&start=0' + '&rowsPerPage=' + rowsPerPage + '&folio=' + folio;

	// LOGS DE EVIDENCIA - INICIO
	console.log("[FOLIOS - CONSULTAR] URL completa:", url);
	console.log("[FOLIOS - CONSULTAR] Timestamp inicio request:", new Date().getTime());
	// LOGS DE EVIDENCIA - FIN
	const fragmentDiv = document.getElementById("fragmentDiv");
	$.ajax({
		type: "GET",
		//async: false,
		contentType: "application/json",
		url: url,
		//data: objData,
		timeout: 180000, // Aumentado a 180 segundos (3 minutos) para consultas pesadas
		success: function(response) {
			console.log("[FOLIOS - SUCCESS] Timestamp llegada response:", new Date().getTime());
			console.log("[FOLIOS - SUCCESS] Tamaño del response:", response.length, "caracteres");

			if (response.indexOf("Inicio de Ses") != -1) {
				console.log("[FOLIOS - SUCCESS] Redirigiendo a login");
				window.location.href = './';
			} else {
				console.log("[FOLIOS - SUCCESS] Asignando HTML al fragmentDiv");
				fragmentDiv.innerHTML = response;

				console.log("[FOLIOS - SUCCESS] HTML asignado, iniciando load_table_style()");
				console.log("[FOLIOS - SUCCESS] Contenido de #data existe:", $('#data').length > 0);

				load_table_style();

				console.log("[FOLIOS - SUCCESS] load_table_style() completado");
				console.log("[FOLIOS - SUCCESS] Ocultando spinner y habilitando botón");

				hideSpinner();
				$('#btnBuscarFolioFacturas').prop("disabled", false);

				console.log("[FOLIOS - SUCCESS] Proceso completado. Timestamp fin:", new Date().getTime());
			}
		},
		error: function(e) {
			console.log("[FOLIOS - ERROR] Timestamp error:", new Date().getTime());
			console.log("[FOLIOS - ERROR] Detalle:", e);
			hideSpinner();
			$('#btnBuscarFolioFacturas').prop("disabled", false);
		},
		done: function(e) {
			console.log("[FOLIOS - DONE] Callback done ejecutado");
			hideSpinner();
		}
	});
}


function descargaxlsxOnClick() {
	//return;
	
	var existenDatos = false;
	$('#data tr').each(function(index, tr) {
		existenDatos = true;
	});
	if (!existenDatos) {
		$(MODAL_DATOS_EXCEL).modal("show");
		$("#divWarningExcelFolios").html(Mensajes_js.MENSAJE_EXCEL_SIN_DATOS);
		return;
	}
	
	ordenGlobal = 1;
	$("#date_desde_validation").empty();
	$("#date_hasta_validation").empty();
	$("#rfcInput_validation").empty();
	$("#folioInput_validation").empty();

	var dateDesdeValue = $("#dateDesde").val().trim();
	var dateHastaValue = $("#dateHasta").val().trim();
	var rfc = $("#rfcInput").val().toUpperCase().trim();
	var folio = $("#folioInput").val().trim();

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

	var url = './listarFoliosFactura/descargaXlsx?dateDesde='+dateDesdeValue+'&dateHasta='+dateHastaValue+'&rfc='+rfc+'&folio=' + folio;
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
	// LOGS DE EVIDENCIA - INICIO
	console.log("[FOLIOS - DATATABLE] Inicializando DataTable con pageLength:", rowsPerPage);
	// LOGS DE EVIDENCIA - FIN

	table = $('#data').DataTable({
		"info": true,
		"paging": true,
		"ordering": false,
		"lengthChange" 	: false,		//Oculta la posibilidad de cambiar el numero de resultados
		"searching": false,
		"pagingType": "simple_numbers",
		"pageLength": parseInt(rowsPerPage),  // CORREGIDO: Ahora usa rowsPerPage de configuración en lugar de hardcodeado
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

	// LOGS DE EVIDENCIA - INICIO
	console.log("[FOLIOS - DATATABLE] DataTable inicializado correctamente");
	// LOGS DE EVIDENCIA - FIN
	
	$('.dataTables_length').addClass('bs-select');
	$('#data').on('draw.dt', function() {
    	disabledCheck();
	});
	
	disabledCheck();
}

function disabledCheck() {
	$('#data tr').each(function(index, tr) {
		var estatus = 0;
		
		$(tr).find("input[type='hidden']").each(function() {
			estatus = $(this).val();
		});
		
		//console.log(estatus);
		$(tr).find("input[name='chkIdFactura']").each(function() {
			if (estatus == 1) {
				$(this).attr("disabled", false);
			} else {
				$(this).attr("disabled", true);
			}
		});
		
	});
}

/*function load_table_style() {
	//Config dataTable devices.
	table = $('#data').DataTable({
		"order": [[1, "desc"]],
		"aaSorting": [],
		"columnDefs": [{
			targets: 0,
			orderable: false
		},{
			targets: 3,
			orderable: false,
            render: function (data, type, row, meta) {
                if (type === 'display') {
                	/*var posComprobante = row[11].indexOf("value=") + 7;
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
                    }*/
                /*}
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
}*/

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

function getListFoliosFactura() {
	var _CERO = '0';
	var listFacturas = [];
	$('#data tr').each(function(index, tr) {
		var selectedChk = false;
		var disabledChk = false;
		var idFactura = 0;
		
		$(tr).find("input[name='chkIdFactura']").each(function() {
			if (this.checked) {
				selectedChk = true;
				idFactura = $(this).val();
            }
            if (this.disabled) {
				disabledChk = true;
			}
		});
		
		if (selectedChk && !disabledChk) {
			//console.log("Checked [" + index + "]: " + idFactura);
			var self = $(this);
			//var idFolioFactura = isEmptyDefault(self.find("td:eq(3)").text().trim(),_CERO);
			var idFactura = isEmptyDefault(self.find("td:eq(2)").text().trim(),_CERO);
			var txtOrden = "#txtOrden_" + idFactura;
			var orden = $(txtOrden).val();
            var folioFactura = isEmptyDefault(self.find("td:eq(3)").text().trim(),"");
            var rfc = isEmptyDefault(self.find("td:eq(5)").text().trim(),_CERO);
            
            var dtoFactura = {};
				dtoFactura['idFactura']=parseInt(idFactura);
				dtoFactura['orden']=orden;
				dtoFactura['folioFactura']=folioFactura;
				dtoFactura['rfc']=rfc;
				
			listFacturas.push(dtoFactura);
		}
	});
	return listFacturas;
}

function generarFolioOnClick() {
	var existenDatos = false;
	$('#data tr').each(function(index, tr) {
		existenDatos = true;
	});
	if (!existenDatos) {
		$(MODAL_DATOS_EXCEL).modal("show");
		$("#divWarningExcelFolios").html(Mensajes_js.MENSAJE_EXCEL_SIN_DATOS_FOLIO);
		return;
	}
	
	var listFacturas = getListFoliosFactura();
	if(listFacturas.length == 0) {
		$(MODAL_VALIDAR).modal("show");
		$("#divWarning").html(Mensajes_js.MENSAJE_SELECCIONA_FACTURA);
		return;
	}
	
	var validaFolioFactura = validaFolioRFC(listFacturas);
	if(validaFolioFactura) {
		$(MODAL_CONFIRMAR).modal("show");
		$("#divWarningConfirmar").html(Mensajes_js.MENSAJE_ADVERTENCIA_GENERAR_FOLIO);
	} else {
		$(MODAL_VALIDAR).modal("show");
		$("#divWarning").html(Mensajes_js.MENSAJE_FOLIO_DIFERENTE);
	}
	
	var folioFactura = {};
	console.log(validaFolioFactura);
	
	folioFactura['listFacturas'] = listFacturas;
    var dataJson=JSON.stringify(folioFactura);
    console.log(dataJson);
}

function isEmpty(value) {
  return typeof value == 'string' && !value.trim() || typeof value == 'undefined' || value === null;
}

function isEmptyDefault(value, valueDefault) {
	if(isEmpty(value)) {
		return valueDefault;	
	}
	return value.trim();
}

function validaFolioRFC(listFacturas) {
	var auxRFC = null;
	if (listFacturas != null && listFacturas.length > 0) {
		var dtoFactura = listFacturas[0];
		auxRFC = dtoFactura['rfc'];
		
		for(var i=0; i<listFacturas.length; i++){
			var data = listFacturas[i];
			console.log("auxRFC: " + auxRFC + " - RFC: " + data.rfc);
			if(auxRFC != data.rfc) {
				return false;
			}
	    };
	}
	return true;
}

function generarFolioConfirmOnClick() {
	$(MODAL_CONFIRMAR).modal("hide");
	var listFacturas = getListFoliosFactura();
	var folioFactura = {};
	folioFactura['listFacturas'] = listFacturas;
    var dataJson=JSON.stringify(folioFactura);
    
  	showSpinner();
	$('#btnGenerarFolio').prop("disabled", true);
	
	var url = './generarFolio';
	$.ajax({
		type: "POST",
		async: false,
		contentType: "application/json",
		url: url,
		data: dataJson,
		timeout: 30000,
		success: function(response) {
			if (response.indexOf("Inicio de Ses") != -1) {
				window.location.href = './';
			} else {
				var data = response;
				Swal.fire('Facturación Sodimac',Mensajes_js.MENSAJE_FOLIO_GENERADO_EXITO.replace("_FOLIO_",data),'success');
				//$(MODAL_RESULTADO).modal("show"); 
				//$("#divResultado").html(Mensajes_js.MENSAJE_FOLIO_GENERADO_EXITO.replace("_FOLIO_",data));
				consultarOnClick();
			}
		},
		error: function(e) {
			console.log("ERROR: ", e);
		},
		done: function(e) {
			console.log("DONE");
		}
	});
	    	
    $('#btnGenerarFolio').prop("disabled", false);
    hideSpinner();
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

function desvincularOnClick(idFactura)
{
	if (idFactura=="") {return};
	
	$("#hdIdFactura").val(idFactura);
	$(MODAL_CONFIRMAR_DESVINCULAR).modal("show");
	$("#divWarningConfirmarDesvincular").html(Mensajes_js.MENSAJE_ADVERTENCIA_DESVINCULAR_FOLIO);		
}

function desvincularConfirmOnClick() {
	
	$(MODAL_CONFIRMAR_DESVINCULAR).modal("hide");
	var idFactura = $("#hdIdFactura").val();
	var dtoFactura = {};
	dtoFactura['idFactura']=parseInt(idFactura);
	
	var dataJson=JSON.stringify(dtoFactura);
    
  	showSpinner();
	$('#btnGenerarFolio').prop("disabled", true);
	
	var url = './desvincularFolio';
	$.ajax({
		type: "POST",
		async: false,
		contentType: "application/json",
		url: url,
		data: dataJson,
		timeout: 30000,
		success: function(response) {
			if (response.indexOf("Inicio de Ses") != -1) {
				window.location.href = './';
			} else {
				var data = response;
				//$(MODAL_RESULTADO).modal("show");
				//$("#divResultado").html(Mensajes_js.MENSAJE_FOLIO_DESVINCULADO_EXITO);
				Swal.fire('Facturación Sodimac',Mensajes_js.MENSAJE_FOLIO_DESVINCULADO_EXITO,'success');
				consultarOnClick();
			}
		},
		error: function(e) {
			console.log("ERROR: ", e);
		},
		done: function(e) {
			console.log("DONE");
		}
	});
	    	
    $('#btnGenerarFolio').prop("disabled", false);
    hideSpinner ();
}

function facturasRelacionadasOnclick(uuidRelacionado) {
	if (uuidRelacionado=="") {return};
	consultarUuidsRelacionados(uuidRelacionado);
}

function consultarUuidsRelacionados(uuidRelacionado) {
	showSpinner();
	
	var url = './listarFacturasRelacionadas';
	$.ajax({
    	url: url,
    	data: {uuidRelacionado: uuidRelacionado},
    	type: "get", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		//var data = data.replace(/\?/g, '•');
    		$("#modalFacturaRelacionada").modal();
    		loadDataUuids(data);
    		hideSpinner();
    	},
    	error: function(e) {
			console.log("ERROR: ", e);
			hideSpinner();
		},
		done: function(e) {
			console.log("DONE");
			hideSpinner();
		}
    });
}

function onlyNumberKey(evt) {
             
    // Only ASCII character in that range allowed
    var ASCIICode = (evt.which) ? evt.which : evt.keyCode
    if (ASCIICode > 31 && (ASCIICode < 48 || ASCIICode > 57))
        return false;
    return true;
}

function habilitarFolioCliente(idFactura) {
	var txtOrden = "#txtOrden_" + idFactura;
	var chekFactura = "#chekFactura_" + idFactura;
	var edit=$(chekFactura).is(":checked");
	
	$(txtOrden).prop("disabled", !edit);
	if (edit) {
		$(txtOrden).val( ordenGlobal );
		$(txtOrden).focus();
		
		ordenGlobal ++;
	}
}


function loadDataUuids(dataList) {
	var dataJson = dataList;
	
	table = $('#factura-rel-table').DataTable({
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
			{name:"ticket", data:"ticket"},
			{name:"monto", data:"montoStr"},
			{name:"uuidRelacionado", data:"uuidRelacionado"}
		],
		data:dataJson
	});
	$('.dataTables_length').addClass('bs-select');
}

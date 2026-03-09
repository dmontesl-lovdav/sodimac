var indices_tabla = {
	PARAM_BUTTON_EDIT: 0
	, PARAM_BUTTON_DELETE: 1
	, IDCONFIG: 2
	, ID_MODULO: 3
	, EMPRESA: 4
	, CABECERA: 5
	, POSICION: 6
	, CUENTA_CONTABLE: 7
	, DEBITO_CREDITO: 8, DESCRIPCION: 9
	, MONEDA: 10
	, TIPO_CAMBIO: 11
	, SISTEMA_ORIGEN: 12
	, ORIGEN_ETL: 13
	, TIPO_USO: 14
	, INDICADOR_IMPUESTO: 15	
	, TIPO_TRANSACCION: 16
	, CLASE_DOC: 17
	, SUCURSAL: 18, IMPUESTO: 19
	, TIPO_IMPUESTO: 20
	, TASA_IMPUESTO: 21
	, TIPO_TRANSACCION_CONTABLE: 22
	, ESTATUS: 23
	, USUARIO: 24
	, FECHA_REGISTRO: 25
	, FECHA_ACTUALIZACION: 26
}

$(document).ready(function() {

	releaseEventEmpty("nombreBusqueda");

	//Submit asincrono del formulario de creacion/actualizacion de una Poliza
	$("#formPoliza").submit(function(event) {
		showSpinner();
		$('#btnGuardarPoliza').prop("disabled", true);
		$('#polizaModalForm #footerMsg').removeAttr('hidden');
		$('#polizaModalForm #footerMsg').addClass('alert-success').removeClass('alert-danger');
		$('#polizaModalForm #footerMsg').html('Guardando cambios...');

		event.preventDefault();

		const formData = {
			cuentaContable: $('#formPoliza #cuentaContable').val(),
			descripcion: $('#formPoliza #descripcion').val(),
			idModulo: $('#formPoliza #idModulo').val(),
			empresa: $('#formPoliza #empresa').val(),
			cabecera: $('#formPoliza #cabecera').val(),
			posicion: $('#formPoliza #posicion').val(),
			debitoCredito: $('#formPoliza #debitoCredito').val(),
			moneda: $('#formPoliza #moneda').val(),
			tipoCambio: $('#formPoliza #tipoCambio').val(),
			sistemaOrigen: $('#formPoliza #sistemaOrigen').val(),
			origenEtl: $('#formPoliza #origenEtl').val(),
			tipoUso: $('#formPoliza #tipoUso').val(),
			indicadorImpuesto: $('#formPoliza #indicadorImpuesto').val(),
			tipoTransaccion: $('#formPoliza #tipoTransaccion').val(),
			claseDoc: $('#formPoliza #claseDoc').val(),
			sucursal: $('#formPoliza #sucursal').val(),
			impuesto: $('#formPoliza #impuesto').val(),
			tipoTransaccionContable: $('#formPoliza #tipoImpuesto').val(),
			tipoImpuesto: $('#formPoliza #tasaImpuesto').val(),
			tasaImpuesto: $('#formPoliza #tipoTransaccionContable').val(),
			estatus: $('#formPoliza #estatus').val()
		};
		var action = $('#action').val();
		
		$.ajax({
			type: "POST",
			url: "./guardarPoliza?action=" + action,
			contentType: "application/json",
			data: formData,
			success: function(response) {
				hideSpinner();
				$('#polizaModalForm #footerMsg').addClass('alert-success').removeClass('alert-danger');
				$('#polizaModalForm #footerMsg').html('Cambios guardados exitosamente');
				listarPolizas();
				setTimeout(function() {
					$('#polizaModalForm').modal('hide');
				}, 3000);
			},
			error: function(jqXHR, textStatus, errorThrown) {
				hideSpinner();			
				$('#polizaModalForm #footerMsg').addClass('alert-danger').removeClass('alert-success');
				
				let responseContentType = jqXHR.getResponseHeader("Content-Type");

				if (responseContentType && responseContentType.indexOf("application/json") !== -1) {
				    // La respuesta es JSON
				    try {
				        let jsonResponse = JSON.parse(jqXHR.responseText);
				        console.error('Error JSON:', jsonResponse);
				        
						$('#polizaModalForm #footerMsg').html(jsonResponse.error);
				    } catch (e) {
				        console.error('Error al analizar JSON:', e);
						$('#polizaModalForm #footerMsg').html('Ocurrio un error al revisar la respuesta');
				    }
				} else if (responseContentType && responseContentType.indexOf("text/html") !== -1) {
				    // La respuesta es HTML
				    let htmlResponse = jqXHR.responseText;
				    console.error('Error HTML:', htmlResponse);
					
					var message = $(jqXHR.responseText).filter('p:eq(1)').text();
					message = message.substr(7);
					message = message.trim();
					$('#polizaModalForm #footerMsg').html(message);
				} else {
				    // Otro tipo de error
				    $('#polizaModalForm #footerMsg').html('Ocurrio un error inesperado, consulte al administrador del sistema');
				}
			}
		});

	});


	$.fn.dataTable.render.ellipsis = function(limite) {
		return function(data, type, row, meta) {
			return type === 'display' && data.length > limite ? data.substr(0, limite) + '…' : data;
		}
	};

	$('#divVisible').hide();
	
	$("#descripcionBusqueda").on('keyup', function(e) {
		if ($("#descripcionBusqueda").val() != "") {
			$("#descripcionBusqueda").addClass('full')
		} else {
			$("#descripcionBusqueda").removeClass('full')
		}
	});
});

function listarPolizas() {

	console.log('Consulta polizas');

	showSpinner();
	$('#btnBuscarPolizas').prop("disabled", true);

	var formData = {
		cuentaContable: $('#nombreBusqueda').val(),
		descripcion: $('#descripcionBusqueda').val()
	};
	
	var url = './listarPolizas';
	const fragmentDiv = document.getElementById("fragmentDiv");
	$.ajax({
		type: "GET",
		//async: false,
		contentType: "application/json",
		url: url,
		data: formData,
		timeout: 30000,
		success: function(response) {
			hideSpinner(); //TODO: validar

			if (response.indexOf("Inicio de Ses") != -1) {
				window.location.href = './';
			} else {
				fragmentDiv.innerHTML = response;
				load_table_style();
				
				var control = 0;
							
				$('#data TBODY TR').each(function() {
					control ++;
				});
					
				if (control == 1) {
					var columns = 0;
					$('#data TBODY TR TD').each(function() {
						columns ++;
					});
					
					if(columns > 1) {
						$('#btnDescargaxlsx').prop('disabled', false);
						$('#divVisible').show();
					} else {
						$('#btnDescargaxlsx').prop('disabled', true);
						$('#divVisible').hide();
					}
				}		
				else if(control > 1){
					$('#btnDescargaxlsx').prop('disabled', false);
					$('#divVisible').show();
				}else{
					$('#btnDescargaxlsx').prop('disabled', true);
					$('#divVisible').hide();
				}
				
				hideSpinner(); //TODO: validar
			}
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

	$('#btnBuscarPolizas').prop("disabled", false);

}

function eliminarPoliza() {

	console.log('Eliminando poliza');

	showSpinner();
	$('#btnEliminarPoliza').prop("disabled", true);

	$('#polizaEliminarModalForm #footerMsg').removeAttr('hidden');
	$('#polizaEliminarModalForm #footerMsg').addClass('alert-success').removeClass('alert-danger');
	$('#polizaEliminarModalForm #footerMsg').html('Eliminando póliza...');

	var url = './eliminarPoliza?idConfigContable=' + $('#polizaEliminarModalForm #idConfigContable').val();
	$.ajax({
		type: "POST",
		//async: false,
		contentType: "application/json",
		url: url,
		//data: objData,
		timeout: 30000,
		success: function(response) {
			$('#polizaEliminarModalForm #footerMsg').addClass('alert-success').removeClass('alert-danger');
			$('#polizaEliminarModalForm #footerMsg').html('Póliza eliminada exitosamente');
			listarPolizas();
			setTimeout(function() {
				$('#polizaEliminarModalForm').modal('hide');
			}, 3000);
		},
		error: function(xhr, status, error) {
			hideSpinner();
			$('#polizaEliminarModalForm #footerMsg').addClass('alert-danger').removeClass('alert-success');
			$('#polizaEliminarModalForm #footerMsg').html('Error al eliminar');
		}

	});

}


/**
 * Muestra el dialogo para crear un nuevo Parametro
 */
function showNewPolizaModalForm() {

	$('#action').val('newPoliza');

	resetarInputs("polizaModalForm");
	$('#polizaModalForm #tituloFormulario').html('Nueva Póliza Contable');

	//Se restean los colores del alert
	$('#polizaModalForm #footerMsg').addClass('alert-success').removeClass('alert-danger');
	$('#polizaModalForm #footerMsg').attr('hidden', 'hidden');

	$('#btnGuardarPoliza').prop("disabled", false);
	
	//Se muestra el formulario
	$('#polizaModalForm').modal('show');
}

/**
 * Muestra el dialogo para modificar un Parametro
 */
function showEditPolizaModalForm(rowId) {

	$('#action').val('updatePoliza');

	resetarInputs("polizaModalForm");

	//Se obtienen desde la tabla los datos del Parametro seleccionado
	poblarFormulario(table.row(rowId).data(), '#polizaModalForm', true);

	$('#polizaModalForm #tituloFormulario').html('Editar Póliza Contable');
	$('#polizaModalForm #footerMsg').addClass('alert-success').removeClass('alert-danger');
	$('#polizaModalForm #footerMsg').attr('hidden', 'hidden');

	$('#btnGuardarPoliza').prop("disabled", false);
	
	//Se muestra el formulario
	$('#polizaModalForm').modal('show');

}

function showViewPolizaModalForm(rowId) {

	resetarInputs("readOnlyPolizasContablesModalForm");
	
	//Se obtienen desde la tabla los datos del Parametro seleccionado
	poblarFormulario(table.row(rowId).data(), '#readOnlyPolizasContablesModalForm', false);
	$('#readOnlyPolizasContablesModalForm #tituloFormulario').html('Póliza Contable');
	//Se muestra el formulario
	$('#readOnlyPolizasContablesModalForm').modal('show');

}

function showDeletePolizaModalForm(rowId) {

	//Se obtienen desde la tabla los datos del Parametro seleccionado
	//$('#polizaEliminarModalForm').find('#cuentaContable').val(table.row(rowId).data()[indices_tabla.CUENTA_CONTABLE]);
	$('#polizaEliminarModalForm').find('#idConfigContable').val(table.row(rowId).data()[indices_tabla.IDCONFIG]);
	$('#polizaEliminarModalForm').find('#cuentaContableEliminar').html(table.row(rowId).data()[indices_tabla.CUENTA_CONTABLE]);

	$('#polizaEliminarModalForm #tituloFormulario').html('Eliminar Póliza Contable');

	$('#polizaEliminarModalForm #footerMsg').addClass('alert-success').removeClass('alert-danger');
	$('#polizaEliminarModalForm #footerMsg').attr('hidden', 'hidden');

	$('#btnEliminarPoliza').prop("disabled", false);

	//Se muestra el formulario
	$('#polizaEliminarModalForm').modal('show');
}

function resetarInputs(inputName) {
	//Se resetean los input de formulario
	$('#' + inputName + ' #idConfigContable').val('');
	$('#' + inputName + ' #cuentaContable').val('');
	$('#' + inputName + ' #descripcion').val('');
	$('#' + inputName + ' #idModulo').val('');
	$('#' + inputName + ' #empresa').val('');
	$('#' + inputName + ' #cabecera').val('');
	$('#' + inputName + ' #posicion').val('');
		
	$('#' + inputName + ' #debitoCredito').val('');
	$('#' + inputName + ' #moneda').val('');
	$('#' + inputName + ' #tipoCambio').val('');
	$('#' + inputName + ' #sistemaOrigen').val('');
	$('#' + inputName + ' #origenEtl').val('');
	

	$('#' + inputName + ' #tipoUso').val('');
	$('#' + inputName + ' #indicadorImpuesto').val('');
		
	$('#' + inputName + ' #tipoTransaccion').val('');
	$('#' + inputName + ' #claseDoc').val('');
	$('#' + inputName + ' #sucursal').val('');
	$('#' + inputName + ' #impuesto').val('');
	$('#' + inputName + ' #tipoImpuesto').val('');
	$('#' + inputName + ' #tasaImpuesto').val('');
	$('#' + inputName + ' #tipoTransaccionContable').val('');
	$('#' + inputName + ' #estatus').val('');
}

function poblarFormulario(data, idFormulario, editar) {
	$(idFormulario).find('#idConfigContable').val(data[indices_tabla.IDCONFIG]);
	$(idFormulario).find('#cuentaContable').val(data[indices_tabla.CUENTA_CONTABLE]);
	$(idFormulario).find('#descripcion').val(data[indices_tabla.DESCRIPCION]);
	$(idFormulario).find('#cabecera').val(data[indices_tabla.CABECERA]);
	$(idFormulario).find('#posicion').val(data[indices_tabla.POSICION]);
	$(idFormulario).find('#tipoCambio').val(data[indices_tabla.TIPO_CAMBIO]);
	$(idFormulario).find('#indicadorImpuesto').val(data[indices_tabla.INDICADOR_IMPUESTO]);
	$(idFormulario).find('#impuesto').val(data[indices_tabla.IMPUESTO]);
	$(idFormulario).find('#usuario').val(data[indices_tabla.USUARIO]);
	$(idFormulario).find('#fechaRegistro').val(data[indices_tabla.FECHA_REGISTRO]);
	$(idFormulario).find('#fechaActualizacion').val(data[indices_tabla.FECHA_ACTUALIZACION]);
	
	if(editar) {
		$(idFormulario).find('#idModulo option').filter(function() { return $(this).text() == data[indices_tabla.ID_MODULO]; }).prop('selected', true);
		$(idFormulario).find('#empresa option').filter(function() { return $(this).text() == data[indices_tabla.EMPRESA]; }).prop('selected', true);
		$(idFormulario).find('#debitoCredito option').filter(function() { return $(this).text() == data[indices_tabla.DEBITO_CREDITO]; }).prop('selected', true);
		$(idFormulario).find('#moneda option').filter(function() { return $(this).text() == data[indices_tabla.MONEDA]; }).prop('selected', true);
		$(idFormulario).find('#claseDoc option').filter(function() { return $(this).text() == data[indices_tabla.CLASE_DOC]; }).prop('selected', true);
		$(idFormulario).find('#sistemaOrigen option').filter(function() { return $(this).text() == data[indices_tabla.SISTEMA_ORIGEN]; }).prop('selected', true);
		$(idFormulario).find('#origenEtl option').filter(function() { return $(this).text() == data[indices_tabla.ORIGEN_ETL]; }).prop('selected', true);
		$(idFormulario).find('#tipoUso option').filter(function() { return $(this).text() == data[indices_tabla.TIPO_USO]; }).prop('selected', true);
		$(idFormulario).find('#tipoTransaccion option').filter(function() { return $(this).text() == data[indices_tabla.TIPO_TRANSACCION]; }).prop('selected', true);
		$(idFormulario).find('#tipoImpuesto option').filter(function() { return $(this).text() == data[indices_tabla.TIPO_IMPUESTO]; }).prop('selected', true);
		$(idFormulario).find('#tasaImpuesto option').filter(function() { return $(this).text() == data[indices_tabla.TASA_IMPUESTO]; }).prop('selected', true);
		$(idFormulario).find('#tipoTransaccionContable option').filter(function() { return $(this).text() == data[indices_tabla.TIPO_TRANSACCION_CONTABLE]; }).prop('selected', true);
		$(idFormulario).find('#sucursal option').filter(function() { return $(this).text() == data[indices_tabla.SUCURSAL]; }).prop('selected', true);
		$(idFormulario).find('#estatus').val(data[indices_tabla.ESTATUS]);
	} else {
		$(idFormulario).find('#idModulo').val(data[indices_tabla.ID_MODULO]);
		$(idFormulario).find('#empresa').val(data[indices_tabla.EMPRESA]);
		$(idFormulario).find('#debitoCredito').val(data[indices_tabla.DEBITO_CREDITO]);
		$(idFormulario).find('#moneda').val(data[indices_tabla.MONEDA]);
		$(idFormulario).find('#claseDoc').val(data[indices_tabla.CLASE_DOC]);
		$(idFormulario).find('#sistemaOrigen').val(data[indices_tabla.SISTEMA_ORIGEN]);
		$(idFormulario).find('#origenEtl').val(data[indices_tabla.ORIGEN_ETL]);
		$(idFormulario).find('#tipoUso').val(data[indices_tabla.TIPO_USO]);	
		$(idFormulario).find('#tipoTransaccion').val(data[indices_tabla.TIPO_TRANSACCION]);
		$(idFormulario).find('#tipoImpuesto').val(data[indices_tabla.TIPO_IMPUESTO]);
		$(idFormulario).find('#tasaImpuesto').val(data[indices_tabla.TASA_IMPUESTO]);
		$(idFormulario).find('#tipoTransaccionContable').val(data[indices_tabla.TIPO_TRANSACCION_CONTABLE]);
		
		$(idFormulario).find('#sucursal').val(data[indices_tabla.SUCURSAL]);
		$(idFormulario).find('#estatus').val(data[indices_tabla.ESTATUS] == "1" ? 'Activo' : 'Inactivo');
	}
}

function load_table_style() {
	//Config dataTable devices.
	table = $('#data').DataTable({
		"order": [[indices_tabla.FECHA_REGISTRO, "desc"]],
		"aaSorting": [],
		"columnDefs": [
			{
				targets: indices_tabla.PARAM_BUTTON_EDIT,
				render: function(data, type, row, meta) {
					var $editButton = $(data);

					$editButton.attr('onclick', 'showEditPolizaModalForm(' + meta.row + ')');

					return $editButton.prop('outerHTML');
				}
			},
			{
				targets: indices_tabla.PARAM_BUTTON_DELETE,
				render: function(data, type, row, meta) {
					var $deleteButton = $(data);

					$deleteButton.attr('onclick', 'showDeletePolizaModalForm(' + meta.row + ')');

					return $deleteButton.prop('outerHTML');
				}
			},
			{
				targets: indices_tabla.CUENTA_CONTABLE,
				render: function(data, type, row, meta) {
					return "<a href='javascript:void(0)' onclick='showViewPolizaModalForm(" + meta.row + ")'>" + data + "</a>";
				}
			},
			{
				targets: indices_tabla.ESTATUS,
				render: function(data, type, row, meta) {
					return data == "1" ? 'Activo' : 'Inactivo';
				}
			}
		],
		"info": true,
		"paging": true,
		"ordering": false,
		"lengthChange": false,		//Oculta la posibilidad de cambiar el numero de resultados
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

function descargaxlsxOnClick() {

	$('#btnDescargaxlsx').prop("disabled", true);
	showSpinner();
	
	var url = './listarParametros/toExcel';
	let a = document.createElement('a');
	a.href = url;
	document.body.appendChild(a);
	a.click();
	document.body.removeChild(a);
	hideSpinner();
	$('#btnDescargaxlsx').prop("disabled", false);
	return; 
}

function showSpinner() {
	document.getElementById("spinner").classList.remove("hide");
	document.getElementById("spinner").classList.add("show");
}

function hideSpinner() {
	document.getElementById("spinner").classList.remove("show");
	document.getElementById("spinner").classList.add("hide");
}

function releaseEventEmpty(id) {
	var id_new = "#" + id;
	$(id_new)
		.blur(function() {
			if (document.getElementById(id).value.length > 0) {
				document.getElementById(id).classList.add("full");
			} else {
				document.getElementById(id).classList.remove("full");
			}
		});
}


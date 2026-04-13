/*------------------------------------------------*
 *   Copyright (c) SODIMAC. December 2020.        *
 *   Portal web: www.sodimac.com.mx.              *
 *   Contributors:                                *
 *   Vantis S. de RL. de CV. Web: vantis.mx       *
 *   Fabian Alvarez <fabian.alvarez@vantis.mx>    *
--------------------------------------------------*/
//CONSTANTS

/***
 ** GET styles, functions that start during page load.
*/
$(document).ready(function() {


});

function ajaxSubmitFormConsult() {

	var fInicio = new Date(Date.parse($("#fechaInicio").val()));
	var fFinal = new Date(Date.parse($("#fechaFinal").val()));

	fInicio.setDate(fInicio.getDate());
	fFinal.setDate(fFinal.getDate() + 1);

	// construct consult object.
	var objData = JSON.stringify({
		idperiodo: $("#periodo-idCatPeriodo").val(),
		descripcionPeriodo: $.trim($("#descripcion-periodo").val()),
		idCatProgramaPago: $("#programaPago-idCatProgramaPago").val(),
		tipodeRebate: $("#autorizacion-tipoRebate").val(),
		fechaInicio: fInicio,
		fechaFinal: fFinal
	});

	$("#btn-consultar").prop("disabled", true);

	// const div: where information will be displayed.
	const fragmentDiv = document.getElementById("fragmentDiv");

	// ajax request.
	$.ajax({
		type: "POST",
		contentType: "application/json",
		url: "../authorization/consult",
		data: objData,
		timeout: 30000,
		beforeSend: function() {
			swal.fire({
				html: '<h5>Cargando...</h5>',
				showConfirmButton: false,
				allowOutsideClick: false,
				didOpen: () => {
					Swal.showLoading()
				},
			});
		},
		success: function(response) {
			$("#fragmentDiv").html(response);
			$("#btn-consultar").prop("disabled", false);

			//config dataTable fillrate//
			$('#dtAutorizacion').DataTable({
				"lengthMenu": [[5, 10, 20, 50, -1], [5, 10, 20, 50, "Todos"]],
				"order": [9, "desc"],
				"aaSorting": [],
				'columnDefs': [

					{
						orderable: false,
						targets: [0, 1, 2, 3, 5, 6, 7, 11, 12, 13, 14, 15],
					}
				],
				select: {
					select: true,
					style: 'multi',
					// Restricting Selection
					selector: 'tr>td:nth-child(1)'
				},
				"searching": true,
				"paging": true,
				"pagingType": "simple_numbers",
				"pageLength": 10,
				"language": {
					"info": "Mostrando _START_ de _END_ de un total de: _TOTAL_ registros",
					"lengthMenu": "Mostrando _MENU_ registros",
					"loadingRecords": "Cargando...",
					"processing": "Procesando...",
					"emptyTable": "Sin registros en la tabla",
					"infoEmpty": "Mostrando 0 de 0 de un total: 0 registros",
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

			var control = 0;

			$('#dtAutorizacion TBODY TR').each(function() {

				control++;

			})

			if (control >= 2) {

				$('#btn-confirm-all').prop('disabled', false);
				$('#btn-negate-all').prop('disabled', false);
				$('#divAutorizar').show();
				$('#divRechazar').show();

			} else {

				$('#btn-confirm-all').prop('disabled', true);
				$('#btn-negate-all').prop('disabled', true);
				$('#divAutorizar').hide();
				$('#divRechazar').hide();
			}

			swal.close();

		},
		error: function(e) {

			console.log("ERROR: ", e);
			toastr.error("No se pudo conectar con el servicio", "Error de conexión");
			$("#btn-consultar").prop("disabled", false);
			swal.close();
		}
	});

}

function check_togle(source) {

	checkboxes = document.getElementsByName('checkbox-select');

	for (var i = 0, n = checkboxes.length; i < n; i++) {

		checkboxes[i].checked = source.checked;
	}
}

function confirm_action(id, idPeriodo, _tipodeRebate, idProveedor, rowNum, tipoRebate) {

	var nameRd = "rd" + rowNum;
	var inp = "input[name='" + nameRd + "']:checked";

	var type = $(inp).attr("tipo");

	if (type != 0 && type != 1) {

		var tit = "Seleccionar acción";
		var mesg = "Favor de elegir Autorizar o Rechazar";

		toastr.warning(mesg, tit);

		return false;
	}

	if (type == 1) {

		confirm_process(id, idPeriodo, _tipodeRebate, idProveedor, tipoRebate);


	} else {

		confirm_cancel(id, idPeriodo, _tipodeRebate, idProveedor, tipoRebate);

	}

}


function confirm_process(id, idPeriodo, _tipodeRebate, idProveedor, tipoRebate) {

	const swalSodimac = Swal.mixin({
		customClass: {
			confirmButton: 'btn btn-sodimac',
			cancelButton: 'btn btn-sodimac-red',
			html: true
		},
		buttonsStyling: false
	})

	// construct consult object.
	var objData = JSON.stringify({
		iDderegistro: id,
		idperiodo: parseInt(idPeriodo, 10),
		tipodeRebate: parseInt(_tipodeRebate, 10),
		cuenta: idProveedor
	});

	var _title = "Autorizar";
	var _text = "Período: " + idPeriodo + " / Proveedor: " + idProveedor + " / " + tipoRebate + "</br>¿Desea autorizar el descuento del rebate?";

	swalSodimac.fire({
		title: _title,
		html: _text,
		icon: 'question',
		showCancelButton: true,
		confirmButtonText: '¡Autorizar!',
		cancelButtonText: '¡Cancelar!',
		allowOutsideClick: false,
		reverseButtons: false
	}).then((result) => {
		if (result.isConfirmed) {

			// ajax request.
			$.ajax({
				type: "POST",
				contentType: "application/json",
				url: "../authorization/process",
				data: objData,
				timeout: 50000,
				beforeSend: function() {
					swal.fire({
						html: '<h5>Cargando...</h5>',
						showConfirmButton: false,
						allowOutsideClick: false,
						didOpen: () => {
							Swal.showLoading()
						},
					});
				},
				success: function(response) {

					var titulo = response.title;
					var message = response.message;
					var tipo = response.typeMessage;
					var strTipo = "";

					if (tipo == 1) {

						strTipo = 'success';
					} else if (tipo == 2) {

						strTipo = 'warning';
					} else if (tipo == 3) {

						strTipo = 'error';
					}

					if (!response.code) {

						swalSodimac.fire({
							title: titulo,
							html: message,
							icon: strTipo,
							allowOutsideClick: false
						}).then((result) => {
							return false;
						});

					}

					swalSodimac.fire({
						title: titulo,
						html: message,
						icon: strTipo,
						allowOutsideClick: false
					}).then((result) => {
						ajaxSubmitFormConsult();
					});

				},
				error: function(e) {

					console.log("ERROR: ", e);
					// toastr.error("No se pudo conectar con el servicio", "Error de conexión");
					swalSodimac.fire(
						'Error de conexión',
						'No se pudo conectar con el servicio',
						'error'
					);

				}
			});

		}

	});
}

function confirm_cancel(id, idPeriodo, _tipodeRebate, idProveedor, tipoRebate) {

	const swalSodimac = Swal.mixin({
		customClass: {
			confirmButton: 'btn btn-sodimac',
			cancelButton: 'btn btn-sodimac-red'
		},
		buttonsStyling: false
	})

	var _title = "Rechazar";
	var _text = "Período: " + idPeriodo + " / Proveedor: " + idProveedor + " / " + tipoRebate + "</br>Por favor ingrese motivo del rechazo";
	var value = "";

	swalSodimac.fire({
		title: _title,
		html: _text,
		input: 'textarea',
		inputAttributes: {
			autocapitalize: 'off'
		},
		icon: 'question',
		showCancelButton: true,
		confirmButtonText: '¡SI!',
		cancelButtonText: '¡NO!',
		allowOutsideClick: false,
		reverseButtons: false,
		preConfirm: () => {
			var id = document.getElementsByClassName("swal2-textarea");
			value = id[0].value;
			if (value.length != 0) {

			} else { Swal.showValidationMessage('Debe ingresa un motivo, favor de validar') }
		}

	}).then((result) => {

		// construct consult object.
		var objData = JSON.stringify({
			iDderegistro: id,
			idperiodo: parseInt(idPeriodo, 10),
			tipodeRebate: parseInt(_tipodeRebate, 10),
			cuenta: idProveedor,
			message: result.value
		});

		if (result.isConfirmed) {

			// ajax request.
			$.ajax({
				type: "POST",
				contentType: "application/json",
				url: "../authorization/cancel",
				data: objData,
				timeout: 50000,
				beforeSend: function() {
					swal.fire({
						html: '<h5>Cargando...</h5>',
						showConfirmButton: false,
						allowOutsideClick: false,
						didOpen: () => {
							Swal.showLoading()
						},
					});
				},
				success: function(response) {

					var titulo = response.title;
					var message = response.message;
					var tipo = response.typeMessage;
					var strTipo = "";

					if (tipo == 1) {

						strTipo = 'success';
					} else if (tipo == 2) {

						strTipo = 'warning';
					} else if (tipo == 3) {

						strTipo = 'error';
					}

					if (!response.code) {

						swalSodimac.fire({
							title: titulo,
							text: message,
							icon: strTipo,
							allowOutsideClick: false
						}).then((result) => {
							return false;
						});

					}

					swalSodimac.fire({
						title: titulo,
						text: message,
						icon: strTipo,
						allowOutsideClick: false
					}).then((result) => {
						ajaxSubmitFormConsult();
					});

				},
				error: function(e) {

					console.log("ERROR: ", e);
					// toastr.error("No se pudo conectar con el servicio", "Error de conexión");
					swalSodimac.fire(
						'Error de conexión',
						'No se pudo conectar con el servicio',
						'error'
					);

				}
			});

		}

	});
}

function multiple_confirm() {

	const swalSodimac = Swal.mixin({
		customClass: {
			confirmButton: 'btn btn-sodimac',
			cancelButton: 'btn btn-sodimac-red',
			html: true
		},
		buttonsStyling: false
	})

	var _listComparativoAprobacion = new Array();
	var control = 0;
	//get all the checked checboxex
	$('#dtAutorizacion input.checkbox-control:checkbox:checked').each(function() {

		var row = $(this).parent().parent();
		var ComparativoAprobacion = {};
		var _id = row.find("TD").eq(1).text();
		var _idPeriodo = row.find("TD").eq(2).text();
		var _tipodeRebate = row.find("TD").eq(3).text();
		var _cuenta = row.find("TD").eq(8).text();

		ComparativoAprobacion.iDderegistro = _id;
		ComparativoAprobacion.idperiodo = parseInt(_idPeriodo, 10);
		ComparativoAprobacion.tipodeRebate = parseInt(_tipodeRebate, 10);
		ComparativoAprobacion.cuenta = _cuenta;

		_listComparativoAprobacion.push(ComparativoAprobacion);

		control += 1;
	})

	if (control <= 1) {

		toastr.warning("Debe seleccionar al menos 2 registros", "Cantidad");

		return false;
	}

	var _title = "Autorizar";
	var _text = "Número de rebates: " + control + ".</br>¿Desea autorizar los descuentos de los rebates?";

	// construct consult object.
	var objData = JSON.stringify({
		comparativoAprobacionList: _listComparativoAprobacion,
		message: ""
	});

	swalSodimac.fire({
		title: _title,
		html: _text,
		icon: 'question',
		showCancelButton: true,
		confirmButtonText: '¡Autorizar!',
		cancelButtonText: '¡Cancelar!',
		allowOutsideClick: false,
		reverseButtons: false
	}).then((result) => {
		if (result.isConfirmed) {

			// ajax request.
			$.ajax({
				type: "POST",
				contentType: "application/json",
				url: "../authorization/multipleProcess",
				data: objData,
				timeout: 80000,
				beforeSend: function() {
					swal.fire({
						html: '<h5>Cargando...</h5>',
						showConfirmButton: false,
						allowOutsideClick: false,
						didOpen: () => {
							Swal.showLoading()
						},
					});
				},
				success: function(response) {

					var titulo = response.title;
					var message = response.message;
					var tipo = response.typeMessage;
					var strTipo = "";

					if (tipo == 1) {

						strTipo = 'success';
					} else if (tipo == 2) {

						strTipo = 'warning';
					} else if (tipo == 3) {

						strTipo = 'error';
					}

					if (!response.code) {

						swalSodimac.fire({
							title: titulo,
							html: message,
							icon: strTipo,
							allowOutsideClick: false
						}).then((result) => {
							return false;
						});

					}

					swalSodimac.fire({
						title: titulo,
						html: message,
						icon: strTipo,
						allowOutsideClick: false
					}).then((result) => {
						ajaxSubmitFormConsult();
					});

				},
				error: function(e) {

					console.log("ERROR: ", e);
					swalSodimac.fire(
						'Error de conexión',
						'No se pudo conectar con el servicio',
						'error'
					);

				}
			});

		}

	});

}

function multiple_negate() {

	const swalSodimac = Swal.mixin({
		customClass: {
			confirmButton: 'btn btn-sodimac',
			cancelButton: 'btn btn-sodimac-red',
			html: true
		},
		buttonsStyling: false
	})

	var _listComparativoAprobacion = new Array();
	var control = 0;
	//get all the checked checboxex
	$('#dtAutorizacion input.checkbox-control:checkbox:checked').each(function() {

		var row = $(this).parent().parent();
		var ComparativoAprobacion = {};
		var _id = row.find("TD").eq(1).text();
		var _idPeriodo = row.find("TD").eq(2).text();
		var _tipodeRebate = row.find("TD").eq(3).text();
		var _cuenta = row.find("TD").eq(8).text();

		ComparativoAprobacion.iDderegistro = _id;
		ComparativoAprobacion.idperiodo = parseInt(_idPeriodo, 10);
		ComparativoAprobacion.tipodeRebate = parseInt(_tipodeRebate, 10);
		ComparativoAprobacion.cuenta = _cuenta;

		_listComparativoAprobacion.push(ComparativoAprobacion);

		control += 1;
	})

	if (control <= 1) {

		toastr.warning("Debe seleccionar al menos 2 registros", "Cantidad");

		return false;
	}

	var _title = "Rechazar";
	var _text = "Número de rebates: " + control + ".</br>Por favor ingrese motivo del rechazo multiple";

	swalSodimac.fire({
		title: _title,
		html: _text,
		input: 'textarea',
		inputAttributes: {
			autocapitalize: 'off'
		},
		icon: 'question',
		showCancelButton: true,
		confirmButtonText: '¡SI!',
		cancelButtonText: '¡NO!',
		allowOutsideClick: false,
		reverseButtons: false,
		preConfirm: () => {
			var id = document.getElementsByClassName("swal2-textarea");
			value = id[0].value;
			if (value.length != 0) {

			} else { Swal.showValidationMessage('Debe ingresa un motivo, favor de validar') }
		}

	}).then((result) => {

		// construct consult object.
		var objData = JSON.stringify({
			comparativoAprobacionList: _listComparativoAprobacion,
			message: result.value
		});

		if (result.isConfirmed) {

			// ajax request.
			$.ajax({
				type: "POST",
				contentType: "application/json",
				url: "../authorization/multipleNegate",
				data: objData,
				timeout: 100000,
				beforeSend: function() {
					swal.fire({
						html: '<h5>Cargando...</h5>',
						showConfirmButton: false,
						allowOutsideClick: false,
						didOpen: () => {
							Swal.showLoading()
						},
					});
				},
				success: function(response) {

					var titulo = response.title;
					var message = response.message;
					var tipo = response.typeMessage;
					var strTipo = "";

					if (tipo == 1) {

						strTipo = 'success';
					} else if (tipo == 2) {

						strTipo = 'warning';
					} else if (tipo == 3) {

						strTipo = 'error';
					}

					if (!response.code) {

						swalSodimac.fire({
							title: titulo,
							text: message,
							icon: strTipo,
							allowOutsideClick: false
						}).then((result) => {
							return false;
						});

					}

					swalSodimac.fire({
						title: titulo,
						text: message,
						icon: strTipo,
						allowOutsideClick: false
					}).then((result) => {
						ajaxSubmitFormConsult();
					});

				},
				error: function(e) {

					console.log("ERROR: ", e);
					// toastr.error("No se pudo conectar con el servicio", "Error de conexión");
					swalSodimac.fire(
						'Error de conexión',
						'No se pudo conectar con el servicio',
						'error'
					);

				}
			});

		}

	});

}
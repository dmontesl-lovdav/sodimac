$(document).ready(function() {


});

function ajaxSubmitFormConsult() {

	// construct consult object.
	var objData = JSON.stringify({
		idCatalogo: $("#id-catalogo").val(),
		nombre: $("#nombre-catalogo").val(),
		descripcion: $("#descripcion-catalogo").val()
	});

	$("#btn-consultar").prop("disabled", true);

	// const div: where information will be displayed.
	const fragmentDiv = document.getElementById("fragmentDiv");

	// ajax request.
	$.ajax({
		type: "POST",
		contentType: "application/json",
		url: "../catalogos/consult",
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
			$('#dtCatalogos').DataTable({
				"lengthMenu": [[5, 10, 20, 50, -1], [5, 10, 20, 50, "Todos"]],
				"order": [0, "asc"],
				"aaSorting": [],
				'columnDefs': [
					{
						orderable: false,
						targets: [3, 6, 7],
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


function ajaxSubmitForm() {

	var objData = JSON.stringify({
		activo: $("#create-select-status").val(),
		nombre: $("#create-nombre").val(),
		descripcion: $("#create-descripcion").val()
	});


	$("#btn-createCatalogo").prop("disabled", true);

	$.ajax({
		type: "POST",
		contentType: "application/json",
		url: "../catalogos/create",
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

			var titulo = response.title;
			var message = response.message;
			var tipo = response.typeMessage;

			if (!response.code) {

				if (tipo == 2) {

					toastr.warning(message, titulo);

				} else if (tipo == 3) {

					toastr.error(message, titulo);
				}

				$("#btn-createCatalogo").prop("disabled", false);
				swal.close();

				return false;
			}


			$("#create-select-status").materialSelect('destroy');
			$("#create-select-status").val("").change();
			$("#create-select-status").materialSelect();
			$("#create-nombre").val("");
			$("#create-descripcion").val("");
			$('#responsiveAddCatalogo').modal('toggle');

			if (tipo == 1) {

				toastr.success(message, titulo);

			} else if (tipo == 4) {

				toastr.info(message, titulo);
			}

			$("#btn-createDocumento").prop("disabled", false);
			swal.close();

			setTimeout(() => { ajaxSubmitFormConsult(); }, 1700);

		},
		error: function(e) {

			console.log("ERROR: ", e);
			toastr.error("No se pudo conectar con el servicio", "Error de conexión");
			$("#btn-createDocumento").prop("disabled", false);
			swal.close();

		}
	});

}

function ajaxSubmitFormEdit() {

	var objData = JSON.stringify({
		idCatalogo: $("#edit-id").val(),
		activo: $("#edit-select-status").val(),
		nombre: $("#edit-nombre").val(),
		descripcion: $("#edit-descripcion").val()
	});


	$("#btn-editCatalogo").prop("disabled", true);

	$.ajax({
		type: "POST",
		contentType: "application/json",
		url: "../catalogos/edit",
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

			var titulo = response.title;
			var message = response.message;
			var tipo = response.typeMessage;

			if (!response.code) {

				if (tipo == 2) {

					toastr.warning(message, titulo);

				} else if (tipo == 3) {

					toastr.error(message, titulo);
				}

				$("#btn-createCatalogo").prop("disabled", false);
				swal.close();

				return false;
			}

			$("#edit-id").val("");
			$("#edit-select-status").materialSelect('destroy');
			$("#edit-select-status").val("").change();
			$("#edit-select-status").materialSelect();
			$("#edit-nombre").val("");
			$("#edit-descripcion").val("");
			$('#responsiveEditCatalogo').modal('toggle');

			if (tipo == 1) {

				toastr.success(message, titulo);

			} else if (tipo == 4) {

				toastr.info(message, titulo);
			}

			$("#btn-editDocumento").prop("disabled", false);
			swal.close();

			setTimeout(() => { ajaxSubmitFormConsult(); }, 1700);

		},
		error: function(e) {

			console.log("ERROR: ", e);
			toastr.error("No se pudo conectar con el servicio", "Error de conexión");
			$("#btn-editDocumento").prop("disabled", false);
			swal.close();

		}
	});

}


function edit(id, nombre, desc, status) {
	$("#edit-id").val(id);
	$("#edit-select-status").materialSelect('destroy');
	$("#edit-select-status").val(status).change();
	$("#edit-select-status").materialSelect();
	$("#edit-nombre").val(nombre);
	$("#edit-descripcion").val(desc);
	$('#responsiveEditCatalogo').modal('toggle');
	$('#edit-catalogo .label-gcis').addClass('active');
}

function confirm_delete(id) {

	const swalSodimac = Swal.mixin({
		customClass: {
			cancelButton: 'btn btn-sodimac-red',
			confirmButton: 'btn btn-sodimac'
		},
		buttonsStyling: false
	})

	swalSodimac.fire({
		title: '¿Está seguro de eliminar el registro?',
		text: "Está acción no podrá revertirse",
		icon: 'warning',
		showCancelButton: true,
		confirmButtonText: '¡eliminar!',
		cancelButtonText: '¡cancelar!',
		allowOutsideClick: false,
		reverseButtons: false
	}).then((result) => {
		if (result.isConfirmed) {

			$.ajax({
				type: "GET",
				contentType: "application/json",
				url: "../catalogos/delete/" + id,
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
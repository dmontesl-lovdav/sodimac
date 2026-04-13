$(document).ready(function() {


});

function ajaxSubmitFormConsult() {
	
	// construct consult object.
	var objData = JSON.stringify({
		"proveedor": $("#proveedor").val(),
		"razonSocial": $("#razon-social").val(),
		"tipoAcuerdo": $("#tipoAcuerdo-idCatTipoAcuerdo").val(),
		"programaPago": $("#programaPago-idCatProgramaPago").val()
	});

	$("#btn-consultar").prop("disabled", true);

	// const div: where information will be displayed.
	const fragmentDiv = document.getElementById("fragmentDiv");

	// ajax request.
	$.ajax({
		type: "POST",
		contentType: "application/json",
		url: "../acuerdos/consult",
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
			$('#dtAcuerdos').DataTable({
				"lengthMenu": [[5, 10, 20, 50, -1], [5, 10, 20, 50, "Todos"]],
				"order": [0, "asc"],
				"aaSorting": [],
				'columnDefs': [
					{
						orderable: false,
						targets: [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15],
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
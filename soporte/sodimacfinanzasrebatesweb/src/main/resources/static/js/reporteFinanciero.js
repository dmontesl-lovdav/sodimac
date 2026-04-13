$(document).ready(function() {


});

function ajaxSubmitFormConsult() {

	var fInicio = getDate( $("#fechaInicio").val() );
	var fFinal = getDate( $("#fechaFinal").val() );

	// construct consult object.
	var objData = JSON.stringify({
		start: 0,
		rowsPerPage:500,
		idPeriodo: $("#idCatPeriodo").val(),
		idProveedor: $("#idProveedor").val() == '' ? 0 : $("#idProveedor").val(),
		tipoPeriodo: $("#idCatProgramaPago").val(),
		tipoRebate: $("#tipoRebate").val(),
		fechaIni: fInicio,
		fechaFin: fFinal
	});

	$("#btn-consultar").prop("disabled", true);

	// ajax request.
	$.ajax({
		type: "POST",
		contentType: "application/json",
		url: "../reporteFinanciero/consult",
		data: objData,
		timeout: 300000,
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
			$('#dtReporteFinanciero').DataTable({
				"lengthMenu": [[5, 10, 25, 50, -1], [5, 10, 25, 50, "Todos"]],
				"order": [0, "asc"],
				"aaSorting": [],
				columnDefs: [{
					orderable: false,
					targets: [0, 1, 2, 3, 4, 5, 6, 7, 8, 9],
				}],
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

			var control = 0;

			$('#dtReporteFinanciero TBODY TR').each(function() {

				control++;

			})

			if (control >= 2) {

				$('#btn-download').prop('disabled', false);
				$('#divVisible').show();

			} else {

				$('#btn-download').prop('disabled', true);
				$('#divVisible').hide();
			}

		},
		error: function(e) {

			console.log("ERROR: ", e);
			toastr.error("No se pudo conectar con el servicio", "Error de conexión");
			$("#btn-consultar").prop("disabled", false);
			swal.close();
		}
	});
	
	function getDate(component) {
		if (component != null && component.length > 0) {
			var objFecha = new Date(Number(component.substring(0, 4))
								  , Number(component.substring(5, 7))-1
								  , Number(component.substring(8, 10))
								  , 0, 0, 0, 0);
			return objFecha;
		}
		return null;
	}

}
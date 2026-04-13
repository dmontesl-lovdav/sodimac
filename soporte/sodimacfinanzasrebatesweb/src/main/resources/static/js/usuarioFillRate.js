$(document).ready(function() {


});

function ajaxSubmitFormConsult() {

	var fInicio = getDate( $("#fechaInicio").val() );
	var fFinal = getDate( $("#fechaFinal").val() );
	var existeFiltro = false;
	
	if (fInicio != null && fFinal ) {

		existeFiltro = true;
		
		if (fInicio > fFinal) {
			toastr.warning("La fecha final no puede ser menor a la fecha inicio, favor de validar", "Validación de Fecha");
			return;
		}
		
		if(Math.round((fFinal.getTime() - fInicio.getTime()) / (1000 * 3600 * 24)) > (30 * 6)) {
			toastr.warning("El periodo de fechas no debe ser mayor a 6 meses, favor de validar", "Validación de Fecha");
			return;
		}
	} 
	
	if ( $("#idCatPeriodo").val() > 0) {
		existeFiltro = true;
	}
	
	if (!existeFiltro) {
		toastr.warning("Seleccionar periodo o fechas de consulta", "Validación de Fecha");
		return;
	}
	
	
	// construct consult object.
	var objData = JSON.stringify({
		start: 0,
		rowsPerPage:500,
		idPeriodo: $("#idCatPeriodo").val(),
		idProveedor: $("#idProveedor").val() == '' ? 0 : $("#idProveedor").val(),
		tipoPeriodo: $("#idCatProgramaPago").val(),
		fechaIni: fInicio,
		fechaFin: fFinal
	});

	$("#btn-consultar").prop("disabled", true);

	// const div: where information will be displayed.
	const fragmentDiv = document.getElementById("fragmentDiv");

	// ajax request.
	$.ajax({
		type: "POST",
		contentType: "application/json",
		url: "../usuarioFillRate/consult",
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
			$('#dtUsuarioFillRate').DataTable({
				"lengthMenu": [[5, 10, 25, 50, -1], [5, 10, 25, 50, "Todos"]],
				"order": [0, "asc"],
				"aaSorting": [],
				columnDefs: [{
					orderable: false,
					targets: [0, 1, 2, 3, 4, 5, 6, 7, 8, 9],
				}],
				"searching": false,
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

			$('#dtUsuarioFillRate TBODY TR').each(function() {

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

}

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
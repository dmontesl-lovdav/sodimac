$(document).ready(function() {


});
// format number to US dollar
let USDollar = new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
});
function ajaxSubmitFormConsult() {

	var fInicio = getDate( $("#fechaIni").val() );
	var fFinal = getDate( $("#fechaFin").val() );

	// construct consult object.
	var objData = JSON.stringify({
		"idPeriodo": $("#periodo-idCatPeriodo").val(),
		"idProveedor": $("#proveedor").val(),
		"tipoRebate": $("#tipoRebate").val(),
		"fechaCargaIni": fInicio,
		"fechaCargaFin": fFinal
	});

	$("#btn-consultar").prop("disabled", true);

	// const div: where information will be displayed.
	const fragmentDiv = document.getElementById("fragmentDiv");

	// ajax request.
	$.ajax({
		type: "POST",
		contentType: "application/json",
		url: "../polizas/consult",
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
			$('#dtPolizas').DataTable({
				"lengthMenu": [[5, 10, 20, 50, -1], [5, 10, 20, 50, "Todos"]],
				"order": [0, "asc"],
				"aaSorting": [],
				'columnDefs': [
					{
						orderable: false,
						targets: [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10],
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

			var control = 0;

			$('#dtPolizas TBODY TR').each(function() {
				control++;
			})

			if (control >= 1) {
				$('#btn-download').prop('disabled', false);
				$('#divVisible').show();
				
				google.charts.load('current', { 'packages': ['corechart'] });
				google.charts.setOnLoadCallback(drawChart);
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

function drawChart() {
	var data = new google.visualization.DataTable();
	data.addColumn('string', 'Amount');
	data.addColumn('number', 'Total amount');

	var inputs = $('#rebatesGrafica div input');
	for(var i = 0, len = inputs.length; i < len; i++) {
		let value = inputs[i].value;
		let name = inputs[i].id.split('-')[2] + ": " + USDollar.format(value);
		data.addRow([name, Number(parseFloat(value))]);
	}

	var total = google.visualization.data.group(data, [{
		type: 'boolean',
		column: 0,
		modifier: function() { return true; }
	}], [{
		type: 'number',
		column: 1,
		aggregation: google.visualization.data.sum
	}]);

	data.addRow(['Total: ' + USDollar.format(total.getValue(0, 1)), 0]);

	var options = {
		sliceVisibilityThreshold: 0,
		title: 'Rebates (Cantidades en Miles)',
		pieSliceText: 'value',
		titleTextStyle: {
			color: 'black',    // any HTML string color ('red', '#cc00cc')
			fontName: 'Arial', // i.e. 'Times New Roman'
			fontSize: 14, // 12, 18 whatever you want (don't specify px)
			bold: true,    // true or false
			italic: false   // true of false
		},
		legend: {
			textStyle: {
				fontSize: 13,
			},
			alignment: 'center'
		},
		tooltip: {
			textStyle: {
				fontSize: 12,
			}
		},
		chartArea: { left: '10', top: 'auto', width: '100%', height: '80%' }
	};

	var chart = new google.visualization.PieChart(document.querySelector('#piechart'));
	chart.draw(data, options);
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

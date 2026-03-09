/**
 * 
 */
 
 function buscarCodigoPostalOnClick(){
	
	var cp = $("#idCatCodigoPostal").val() * 1;
	
	if(typeof(cp) !== 'number' || cp == 0){
		alert("El Codigo Postal no es número valido");
		return;
	}
	var url = './findByCP/' + cp;
	showSpinner();
	$.ajax({
		type: "GET",
		//async: false,
		contentType: "application/json",
		url: url,
		//data: objData,
		timeout: 30000,
		success: function(response) {
			if(response.respuesta.codigo == 1){
				$("#idCatCodigoPostal").val(response.data.codigopostal)
				$("#estado").val(response.data.c_estado);
				$("#localidad").val(response.data.c_localidad);
				$("#municipio").val(response.data.c_municipio);
			} else {
				$("#idCatCodigoPostal").val(0)
				$("#estado").val('');
				$("#localidad").val('');
				$("#municipio").val('');
				alert("No se encontro el codigo postal: " + cp);
			}
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
 
 
function emisorTiendaCreateOnClick(id){
	showSpinner();
	var url = '../wsadministracion/findById/' + id + '/CREATE';
	const fragmentDiv = document.getElementById("divEmisorTiendaEdit");
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
				$("#modalEmisorTiendaEdit").modal("show");
				fragmentDiv.innerHTML = response;
				//load_table_style();			
				hideSpinner();	
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
} 
 
function emisorTiendaEditOnClick(id){
	showSpinner();
	var url = '../wsadministracion/findById/' + id + '/UPDATE';
	const fragmentDiv = document.getElementById("divEmisorTiendaEdit");
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
				$("#modalEmisorTiendaEdit").modal("show");
				fragmentDiv.innerHTML = response;
				//load_table_style();			
				hideSpinner();	
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
}

function runMyFunction() {
  var msg = "";
  if ($("#idCatCodigoPostal").val() == null || $("#idCatCodigoPostal").val().trim() == "" || $("#idCatCodigoPostal").val().trim() == 0){
	msg = msg +  "Codigo Postal Invalido.\n";
  } 
  if ($("#idTienda").val() == null || $("#idTienda").val().trim() == ""){
	msg = msg +  "IdTienda Invalido.\n";
  }
  if ($("#idConfDatosEmisor").val() == 0){
	msg = msg +  "Seleccione un Emisor valido.\n";
  }
  if ($("#idCatTipoTienda").val() == 0){
	msg = msg +  "Seleccione un Tipo de Tienda valido.\n";
  }
  if ($("#calle").val() == null || $("#calle").val().trim() == ""){
	msg = msg +  "Espécifique la calle.\n";
  }
  if ($("#noExterior").val() == null || $("#noExterior").val().trim() == ""){
	msg = msg +  "Espécifique el número exterior.\n";
  }
  if ($("#noInterior").val() == null){
	$("#noInterior").val("");
  }
  if ($("#colonia").val() == null || $("#colonia").val().trim() == ""){
	msg = msg +  "Espécifique la colonia.\n";
  }
  if ($("#referencia").val() == null){
	$("#referencia").val("");
  }
  
  if(msg != ""){
	alert(msg);
	event.preventDefault();
  } else {
	  showSpinner();
	  $("#modalEmisorTiendaEdit").modal("hide");
	  document.getElementById("myForm").submit();
  }

}

function showSpinner () {
  document.getElementById("spinner").classList.remove("hide");
  document.getElementById("spinner").classList.add("show");
}

function hideSpinner () {
  document.getElementById("spinner").classList.remove("show");
  document.getElementById("spinner").classList.add("hide");
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



$(document).ready(function(){
	load_table_style();
	hideSpinner();
	const queryString = window.location.search;
	const urlParams = new URLSearchParams(queryString);
	const accionPrevia = urlParams.get('accionPrevia');
	//.log(accionPrevia);
	if(accionPrevia == 'update' || accionPrevia == 'create'){
			setTimeout(function(){
		$('#modalmsg').modal("show");
		}, 500);
	}
	
});


/**
 * 
 */

function consultarCatalogos() {
	
	var url = './catalogos/obtenerCatalogos';
	const fragmentDiv = document.getElementById("fragmentDivPerfilList");
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

$(document).ready(function(){
	//consultarCatalogos();
});


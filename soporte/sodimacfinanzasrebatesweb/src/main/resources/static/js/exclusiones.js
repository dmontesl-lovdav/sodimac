var ID_ORDEN_COMPRA = 1;
var ID_SKU 			= 2;
var ID_FAMILIA 		= 3;
var ID_PROVEEDORES 	= 4;

$(document).ready(function() {

});

function ajaxSubmitFormConsult() {
	
	var idPeriodo = $("#periodo-idCatPeriodo").val();
	if (!(idPeriodo != null && idPeriodo.length > 0)) {
		toastr.warning("Validación", "Seleccionar un periodo de consulta");
		return;
	}

	
	// construct consult object.
	var objData = JSON.stringify({
		start: 0,
		rowsPerPage:500,
		folio: $("#txt-folio").val(),
		comentario: $("#txt-exclusion").val(),
		idPeriodo : $("#periodo-idCatPeriodo").val(),
		idTipoExclusion: $("#exclusion-idTipoExclusion").val(),
		numProveedor: $("#txt-proveedor").val(),
		ordenCompra: $("#txt-ordenCompra").val() 
	});

	$("#btn-consultar").prop("disabled", true);
	
	// const div: where information will be displayed.
	//const fragmentDiv = document.getElementById("fragmentDiv");
	
	// ajax request.
	$.ajax({
		type: "POST",
		contentType: "application/json",
		url: "../exclusiones/consult",
		data: objData,
		timeout: 30000,
		success: function(response) {
			$("#fragmentDiv").html(response);
			$("#btn-consultar").prop("disabled", false);
			
			//config dataTable dtexclusiones//
			var tableExclusiones = $('#dtExclusiones').DataTable({
				"info": true,
				"paging": true,
				"ordering": false,
				"lengthChange" 	: false,		//Oculta la posibilidad de cambiar el numero de resultados
				"searching": false,
				"pagingType": "simple_numbers",
				"pageLength": 5,
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
			swal.close();
			
			var control = tableExclusiones.rows().count();
			if(control >= 1){
				$('#btn-download').prop('disabled', false);
				$('#divVisible').show();
			}else{
				$('#btn-download').prop('disabled', true);
				$('#divVisible').hide();
			}
		},
		error : function(e) {
			console.log("ERROR: ", e);
			toastr.error("No se pudo conectar con el servicio", "Error de conexión");
			$("#btn-consultar").prop("disabled", true);
		}
	});

}

//BEGIN CREATE EXCLUSION
function ajaxSubmitForm() {

	// Get form
	var form = $('#create-exclusion')[0];
	var data = new FormData(form);
	var tipoExclusion = document.getElementById('create-select-tipo-exclusion');
	
	if( (tipoExclusion.value == ID_SKU) || (tipoExclusion.value == ID_FAMILIA)) {
		var numProveedor = $("#txt-edit-numProveedor").val().trim();
		var exclusion = $("#txt-edit-exclusion").val();
		
		if (exclusion != null && exclusion.length > 0 ) {
			if (numProveedor.length == 0) {
				toastr.warning("Capturar proveedor", "Advertencia");
				return false;
			}
		}
	}

	$("#btn-createExclusion").prop("disabled", true);

	$.ajax({
		type: "POST",
		enctype: "multipart/form-data",
		url: "../exclusiones/create",
		data: data,
		processData: false,
		contentType: false,
		cache: false,
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
		
			if(!response.code){
				
				if(tipo == 2){
	
					toastr.warning(message, titulo);
		
				}else if(tipo == 3){
		
					toastr.error(message, titulo);
				}
				
				$("#btn-createExclusion").prop("disabled", false);
				swal.close();
				
				return false;
			}
			
			$("#txt-edit-exclusion").val("");
			$("#txt-edit-numProveedor").val("");
			$("#txt-comentario").val("");
			$("#archivo").val("");
			$("#layout").val("");
			$("#create-select-periodo").materialSelect('destroy');
			$("#create-select-periodo").val("").change();
			$("#create-select-periodo").materialSelect();
			$("#create-select-tipo-rebate").materialSelect('destroy');
			$("#create-select-tipo-rebate").val("").change();
			$("#create-select-tipo-rebate").materialSelect();
			$("#create-select-tipo-exclusion").materialSelect('destroy');
			$("#create-select-tipo-exclusion").val("").change();
			$("#create-select-tipo-exclusion").materialSelect();
			$('#responsiveAddExclusion').modal('toggle');
			$("#linkVisible").css("display", "none");
			
			if(tipo == 1){
	
				toastr.success(message, titulo);
		
			}else if(tipo == 4){
		
				toastr.info(message, titulo);
			}
		
			$("#btn-createExclusion").prop("disabled", false);
			swal.close();
			
			setTimeout(() => {  ajaxSubmitFormConsult(); }, 1700);
		
		},
		error : function(e) {
		
			console.log("ERROR: ", e);
			toastr.error("No se pudo conectar con el servicio", "Error de conexión");
			$("#btn-createExclusion").prop("disabled", false);
			swal.close();
			
		}
	});

}

function validFile() {

	// Obtener nombre de archivo
	let archivo = document.getElementById('archivo'),
	// Obtener extensión del archivo
	extension = archivo.value.substring(archivo.value.lastIndexOf('.'), archivo.value.length);
	
	// Si la extensión obtenida no está incluida en la lista de valores
	// del atributo "accept", mostrar un error.
	if(archivo.getAttribute('accept').split(',').indexOf(extension) < 0) {
		toastr.warning("Archivo inválido. No se permite la extensión: " + extension);
		archivo.value = "";
	}
	
	// Validando tamaño de archivo
	if (archivo.files.length == 1) { // length > 0 Si hay varios archivos
		if (archivo.files.item(0)) {
		//for (let i = 0; i <= archivo.files.length - 1; i++) { // Solo si hay varios archivos se itera uno por uno
			const fsize = archivo.files.item(0).size; // Tamaño del archivo
			console.log(fsize);
			const file = Math.ceil((fsize / 1024)); // entre bites
			if (file == 0) {
				toastr.warning("El archivo esta vacío. Seleccione otro archivo.");
				archivo.value = "";
			} /* else if (file >= 4096) {
				toastr.warning("Archivo demasiado grande. Tamaño máximo: 4MB");
				archivo.value = "";
			}*/ else {
				//toastr.success("OK: " + file);
			}
		//}
		}
	}
}

function validFileLayout() {

	// Obtener nombre de archivo
	let layout = document.getElementById('layout'),
	// Obtener extensión del archivo
	extension = layout.value.substring(layout.value.lastIndexOf('.'), layout.value.length);
	
	// Si la extensión obtenida no está incluida en la lista de valores
	// del atributo "accept", mostrar un error.
	if(layout.getAttribute('accept').split(',').indexOf(extension) < 0) {
		toastr.warning("Layout inválido. No se permite la extensión: " + extension);
		layout.value = "";
	}
	
	// Validando tamaño de archivo
	if (layout.files.length == 1) { // length > 0 Si hay varios archivos
		if (layout.files.item(0)) {
		//for (let i = 0; i <= archivo.files.length - 1; i++) { // Solo si hay varios archivos se itera uno por uno
			const fsize = layout.files.item(0).size; // Tamaño del archivo
			console.log(fsize);
			const file = Math.ceil((fsize / 1024)); // entre bites
			if (file == 0) {
				toastr.warning("El Layout esta vacío. Seleccione otro archivo.");
				layout.value = "";
			} /* else if (file >= 4096) {
				toastr.warning("Archivo demasiado grande. Tamaño máximo: 4MB");
				archivo.value = "";
			}*/ else {
				//toastr.success("OK: " + file);
			}
		//}
		}
	
	}
}


function edit(idCatTipoExclusion, idExclusion, pProveedor, pdeshabilitarModificarComentario) {
	$("#hdIdCatTipoExclusionParent").val(idCatTipoExclusion);
	$("#hdIdExclusionParent").val(idExclusion);
	
	$("#btn-comentario-exclusion").prop("disabled", pdeshabilitarModificarComentario);
	
	$("#hdIdCatTipoExclusion").val(idCatTipoExclusion);
	$("#hdIdExclusion").val(idExclusion);
	
	if (idCatTipoExclusion == ID_ORDEN_COMPRA) {
		lf_consultarOrdenCompraDet(idExclusion);
	}
	else if (idCatTipoExclusion == ID_SKU) {
		lf_consultarSkuDetProveedor(idExclusion, pProveedor);
	}
	else if (idCatTipoExclusion == ID_FAMILIA) {
		lf_consultarFamiliaDet(idExclusion);
	}
	else if (idCatTipoExclusion == ID_PROVEEDORES) {
		lf_consultarProveedorDet(idExclusion);
	}
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
				type: "PUT",
				contentType: "application/json",
				url: "./delete/" + id,
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
					
					if (tipo == 2) {
						toastr.warning(message, titulo);
					} else if (tipo == 3) {
						toastr.error(message, titulo);
					}

					if (tipo == 1) {
						toastr.success(message, titulo);
					} else if (tipo == 4) {
						toastr.info(message, titulo);
					}
					
					swal.close();
					setTimeout(() => { ajaxSubmitFormConsult(); }, 1700);

				},
				error: function(e) {
					console.log("ERROR: ", e);
					toastr.error("No se pudo conectar con el servicio", "Error de conexión");
					swal.close();
				}
			});

		}

	});
}

function confirm_inactivar(id) {
    const swalSodimac = Swal.mixin({
		customClass: {
			cancelButton: 'btn btn-sodimac-red',
			confirmButton: 'btn btn-sodimac'
		},
		buttonsStyling: false
	})

	swalSodimac.fire({
		title: '¿Está seguro de inactivar la exclusión?',
		text: "Está acción no podrá revertirse",
		icon: 'warning',
		showCancelButton: true,
		confirmButtonText: '¡inactivar!',
		cancelButtonText: '¡cancelar!',
		allowOutsideClick: false,
		reverseButtons: false
	}).then((result) => {
		if (result.isConfirmed) {

			$.ajax({
				type: "PUT",
				contentType: "application/json",
				url: "./inactivar/" + id,
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
					
					if (tipo == 2) {
						toastr.warning(message, titulo);
					} else if (tipo == 3) {
						toastr.error(message, titulo);
					}

					if (tipo == 1) {
						toastr.success(message, titulo);
					} else if (tipo == 4) {
						toastr.info(message, titulo);
					}
					
					swal.close();
					setTimeout(() => { ajaxSubmitFormConsult(); }, 1700);

				},
				error: function(e) {
					console.log("ERROR: ", e);
					toastr.error("No se pudo conectar con el servicio", "Error de conexión");
					swal.close();
				}
			});

		}

	});
}

function confirm_autorizacion(id) {
    
    const swalSodimac = Swal.mixin({
		customClass: {
			cancelButton: 'btn btn-sodimac-red',
			confirmButton: 'btn btn-sodimac'
		},
		buttonsStyling: false
	})

	swalSodimac.fire({
		title: '¿Está seguro de autorizar el registro?',
		text: "Está acción no podrá revertirse",
		icon: 'warning',
		showCancelButton: true,
		confirmButtonText: '¡autorizar!',
		cancelButtonText: '¡cancelar!',
		allowOutsideClick: false,
		reverseButtons: false
	}).then((result) => {
		if (result.isConfirmed) {

			$.ajax({
				type: "PUT",
				contentType: "application/json",
				url: "./autorizar/" + id,
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
					
					if (tipo == 2) {
						toastr.warning(message, titulo);
					} else if (tipo == 3) {
						toastr.error(message, titulo);
					}

					if (tipo == 1) {
						toastr.success(message, titulo);
					} else if (tipo == 4) {
						toastr.info(message, titulo);
					}
					
					swal.close();
					setTimeout(() => { ajaxSubmitFormConsult(); }, 1700);

				},
				error: function(e) {
					console.log("ERROR: ", e);
					toastr.error("No se pudo conectar con el servicio", "Error de conexión");
					swal.close();
				}
			});

		}

	});
}

function confirm_rechazo(id) {
    
    const swalSodimac = Swal.mixin({
		customClass: {
			cancelButton: 'btn btn-sodimac-red',
			confirmButton: 'btn btn-sodimac'
		},
		buttonsStyling: false
	})

	swalSodimac.fire({
		title: '¿Está seguro de rechazar el registro?',
		text: "Está acción no podrá revertirse",
		icon: 'warning',
		showCancelButton: true,
		confirmButtonText: '¡Rechazar!',
		cancelButtonText: '¡Cancelar!',
		allowOutsideClick: false,
		reverseButtons: false
	}).then((result) => {
		if (result.isConfirmed) {

			$.ajax({
				type: "PUT",
				contentType: "application/json",
				url: "./rechazar/" + id,
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
					
					if (tipo == 2) {
						toastr.warning(message, titulo);
					} else if (tipo == 3) {
						toastr.error(message, titulo);
					}

					if (tipo == 1) {
						toastr.success(message, titulo);
					} else if (tipo == 4) {
						toastr.info(message, titulo);
					}
					
					swal.close();
					setTimeout(() => { ajaxSubmitFormConsult(); }, 1700);

				},
				error: function(e) {
					console.log("ERROR: ", e);
					toastr.error("No se pudo conectar con el servicio", "Error de conexión");
					swal.close();
				}
			});

		}

	});
}

function eliminarExclusionCarga(id) {
	var idExclusion = $("#hdIdExclusion").val();
	
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
				type: "PUT",
				contentType: "application/json",
				url: "./delete/det/" + id,
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

					if (tipo == 2) {
						toastr.warning(message, titulo);
					} else if (tipo == 3) {
						toastr.error(message, titulo);
					}

					if (tipo == 1) {
						toastr.success(message, titulo);
					} else if (tipo == 4) {
						toastr.info(message, titulo);
					}

					$("#btn-create-exclusion-oc").prop("disabled", false);
					swal.close();
					setTimeout(() => { lf_consultarOrdenCompraDet(idExclusion); }, 1700);

				},
				error: function(e) {

					console.log("ERROR: ", e);
					toastr.error("No se pudo conectar con el servicio", "Error de conexión");
					swal.close();

				}
			});

		}

	});
}

function changeTipoExclusion() {
	let tipoExclusion = document.getElementById('create-select-tipo-exclusion');
	
	if( (tipoExclusion.value == ID_ORDEN_COMPRA) || (tipoExclusion.value == ID_PROVEEDORES)) {
		$("#txt-edit-numProveedor").val("");
		$("#txt-edit-numProveedor").prop("disabled", true);
	} else {
		$("#txt-edit-numProveedor").val("");
		$("#txt-edit-numProveedor").prop("disabled", false);
	}
	
	//$('#linkVisible').show();
	$("#linkVisible").css("display", "block");
	var href = $('#btn-download-layout').attr('href');
	$('#btn-download-layout').attr('href', href.substring(0, href.indexOf("=") + 1) + tipoExclusion.value);
	/*$('#create-select-periodo').find('option').each(function(index,element){
		if( (tipoExclusion.value == ID_ORDEN_COMPRA) && index == 1) {
			element.disabled=true;
		} else {
			element.disabled=false;
		}
	});*/
}

function obtenerPeriodos(pIdTipoExclusion){
	var result;
    var url = '/exclusiones/periodos';
    $.ajax({
    	url: url,
    	data: {idTipoExclusion: pIdTipoExclusion},
    	type: "post", dataType: "json", async: false, cache: false, crossDomain: false,
    	success: function(data){
    		result = data;
        }
    });
    return result;
}

function btnLimpiar(){

	$('#txt-folio').val('');
	$('#txt-proveedor').val('');
	$('#txt-exclusion').val('');
	$('#txt-ordenCompra').val('');

	$("#periodo-idCatPeriodo").materialSelect('destroy');
	$("#periodo-idCatPeriodo").val("0").change();
	$("#periodo-idCatPeriodo").materialSelect();

	$("#exclusion-idTipoExclusion").materialSelect('destroy');
	$("#exclusion-idTipoExclusion").val("0").change();
	$("#exclusion-idTipoExclusion").materialSelect();
}

function modificarComentarioExclusion () {
	var idExclusion = $("#hdIdExclusion").val();
	var comentario = $("#edit-txt-comentario").val();
	var objData = JSON.stringify({
		idExclusion: idExclusion,
		comentario: comentario
	});
 	
	const swalSodimac = Swal.mixin({
		customClass: {
			cancelButton: 'btn btn-sodimac-red',
			confirmButton: 'btn btn-sodimac'
		},
		buttonsStyling: false
	})

	swalSodimac.fire({
		title: '¿Está seguro de modificar el comentario?',
		text: "",
		icon: 'warning',
		showCancelButton: true,
		confirmButtonText: '¡modificar!',
		cancelButtonText: '¡cancelar!',
		allowOutsideClick: false,
		reverseButtons: false
	}).then((result) => {
		if (result.isConfirmed) {

			$.ajax({
				type: "POST",
				contentType: "application/json",
				url: "../exclusiones/updateComentario",
				data: objData,
				timeout: 30000,
				success: function(response) {

					var titulo = response.title;
					var message = response.message;
					var tipo = response.typeMessage;

					if (tipo == 2) {
						toastr.warning(message, titulo);
					} else if (tipo == 3) {
						toastr.error(message, titulo);
					}

					if (tipo == 1) {
						toastr.success(message, titulo);
					} else if (tipo == 4) {
						toastr.info(message, titulo);
					}

					swal.close();
				},
				error: function(e) {

					console.log("ERROR: ", e);
					toastr.error("No se pudo conectar con el servicio", "Error de conexión");
					swal.close();

				}
			});

		}

	});
}
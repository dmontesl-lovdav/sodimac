var ID_SKU 			= 2;
$(document).ready(function() {

});

function ajaxSubmitSkuDetForm(pJsonId) {
	var idExclusion = $("#hdIdExclusionSku").val();
	var objData = JSON.stringify({
		idExclusion: idExclusion,
		motivo: "Motivo", //$("#create-txt-motivo-det").val()
		jsonId: pJsonId
	});
	
	$("#btn-sku-create").prop("disabled", true);
	
	$.ajax({
		type: "POST",
		contentType: "application/json",
		url: "./add/sku",
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

				$("#btn-sku-create").prop("disabled", false);
				swal.close();

				return false;
			}
			
			if (tipo == 1) {
				toastr.success(message, titulo);
			} else if (tipo == 4) {
				toastr.info(message, titulo);
			}

			$("#btn-sku-create").prop("disabled", false);
			swal.close();
			setTimeout(() => { lf_consultarSkuDet(idExclusion); }, 1700);

		},
		error: function(e) {

			console.log("ERROR: ", e);
			toastr.error("No se pudo conectar con el servicio", "Error de conexión");
			$("#btn-sku-create").prop("disabled", false);
			swal.close();

		}
	});
}

function lf_listarSkuDisponible(pIdExclusion) {
	$("#hdIdExclusionSku").val(pIdExclusion);
	
	var url = './list/disponible/sku/sesion';
	$.ajax({
    	url: url,
    	data: {idExclusion: pIdExclusion},
    	type: "get", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		lf_loadDataSku(data); 
    	},
    	error: function(e) {
			console.log("ERROR: ", e);
		},
		done: function(e) {
			console.log("DONE");
		}
    }); 	
}

function lf_consultarSkuDet(idExclusion) {
	
	var url = './detalle';
	$.ajax({
    	url: url,
    	data: {idExclusion: idExclusion},
    	type: "get", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		lf_loadDataSKUPrincipalDet(data);
    	},
    	error: function(e) {
			console.log("ERROR: ", e);
		},
		done: function(e) {
			console.log("DONE");
		}
    });
}

function lf_consultarSkuDetProveedor(idExclusion, pProveedor) {
	
	var url = './detalleproveedor';
	$.ajax({
    	url: url,
    	data: {idExclusion: idExclusion, proveedor: pProveedor},
    	type: "get", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		lf_loadDataSKUPrincipalDet(data);
    	},
    	error: function(e) {
			console.log("ERROR: ", e);
		},
		done: function(e) {
			console.log("DONE");
		}
    });
}

function lf_consultarSkuDetByIdExclusionCarga(idExclusion, idExclusionCarga, pProveedor, pSku) {
	
	var url = './detalle/exclusion/sku';
	$.ajax({
    	url: url,
    	data: {idExclusion: idExclusion
			 , idExclusionCarga: idExclusionCarga
			 , proveedor: pProveedor
			 , sku: pSku},
    	type: "get", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		lf_loadDataSkuProveedor(data);
    	},
    	error: function(e) {
			console.log("ERROR: ", e);
		},
		done: function(e) {
			console.log("DONE");
		}
    }); 	
    //$('#btnBuscarFolioFacturasPago').prop("disabled", false);
}

function lf_consultarSkuDetByFamilia(idExclusion, idExclusionCarga, pProveedor, pFamilia) {
	
	var url = './detalle/exclusion/sku/familia';
	$.ajax({
    	url: url,
    	data: {idExclusion: idExclusion
			 , idExclusionCarga: idExclusionCarga
			 , proveedor: pProveedor
			 , clacom: pFamilia},
    	type: "get", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		lf_loadDataSkuDetByFamilia(data);
    	},
    	error: function(e) {
			console.log("ERROR: ", e);
		},
		done: function(e) {
			console.log("DONE");
		}
    }); 	
    //$('#btnBuscarFolioFacturasPago').prop("disabled", false);
}

function lf_loadDataSku(dataModel) {
	var dataJson = dataModel.cicmxOcDtos;
	
	$('#responsiveSku').modal('show');
	$("#divEditSku").show();
	$("#divEditSkuFamilia").hide();
	$("#divEditSkuProveedor").hide();
	
	$("#txt-sku").val(dataModel.sku);
	$('#lbl-sku').addClass('active');
	
	$("#txt-sku-descripcion").val(dataModel.skuDescripcion);
	$('#lbl-sku-descripcion').addClass('active');
	
	$('#exclusion-sku-table').DataTable({
		"info": true,
		"bDestroy":true,
		"paging": true,
		"ordering": false,
		"lengthChange" 	: false,		//Oculta la posibilidad de cambiar el numero de resultados
		"searching": true,
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
		},
		columns:[
            {name:"jsonId", data:"jsonId"},
            {name:"numProveedor", data:"numProveedor"},
            {name:"nomProveedor", data:"nomProveedor"},
            {name:"numOc", data:"numOc"},
			{name:"clacom", data:"clacom"},
			{name:"sku", data:"sku"},
			{name:"skuDescripcion", data:"skuDescripcion"}
		],
		columnDefs: [
			{
				'targets': 0,
				'checkboxes': {
				   'selectRow': true
				}
			}, 
		    {
		        "targets": 1, 
		        "className": "text-left"
		   },
		   {
		        "targets": 2,
		        "className": "text-left",
		   },
		   {
		        "targets": 3,
		        "className": "text-left",
		   },
		   {
		        "targets": 4,
		        "className": "text-left",
		   },
		   {
		        "targets": 5,
		        "className": "text-left",
		   },
		   {
		        "targets": 6,
		        "className": "text-left",
		   }
		],
		select: {
			'style': 'multi'
		},
		data:dataJson
	});
	
	/*var data = tableSKU.rows().data();
 	data.each(function (value, index) {
    	//console.log(`For index ${index}, data value is ${value}`);
    	var tr = tableSKU.row(index).node();
    	$(tr).find("input[type='checkbox']").each(function() {
			if (value.checked == 1) {
				$(this).attr("checked", true);
			} else {
				$(this).attr("checked", false);
			}
		});
 	});
	$('.dataTables_length').addClass('bs-select');*/
}

function lf_loadDataSkuDetByFamilia(dataModel) {
	var dataJson = dataModel.listExclusionCargaDet;
	
	$('#responsiveSku').modal('show');
	$("#divEditSku").hide();
	$("#divEditSkuFamilia").show();
	$("#divEditSkuProveedor").hide();
	
	$("#txt-sku-familia-num-proveedor").val(dataModel.proveedor.numProveedor);
	$('#lbl-sku-familia-num-proveedor').addClass('active');
	
	$("#txt-sku-familia-proveedor").val(dataModel.proveedor.nomProveedor);
	$('#lbl-sku-familia-proveedor').addClass('active');
	
	$("#txt-sku-familia-familia").val(dataModel.exclusionCarga.carga);
	$('#lbl-sku-familia-familia').addClass('active');
	
	//$("#txt-sku-familia-motivo").val(dataModel.exclusionCarga.motivo);
	//$('#lbl-sku-familia-motivo').addClass('active');
	
	$('#exclusion-sku-familia-table').DataTable({
		"info": true,
		"bDestroy":true,
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
		},
		columns:[
            {name:"sku", data:"sku"},
			{name:"ordenCompra", data:"ordenCompra"},
			{name:"sku", data:"sku"},
			{name:"skuDescripcion", data:"skuDescripcion"}
		],
		columnDefs: [
			{
				"targets": 0,
				"visible": false
			}, 
		    {
		        "targets": 1, 
		        "visible": false
		   },
		   {
		        "targets": 2,
		        "className": "text-left",
		   },
		   {
		        "targets": 3,
		        "className": "text-left",
		   }
		],
		data:dataJson
	});
	
	$('.dataTables_length').addClass('bs-select');
}

function lf_loadDataSkuProveedor(dataModel) {
	var dataJson = dataModel.listExclusionCargaDet;
	
	$('#responsiveSku').modal('show');
	$("#divEditSku").hide();
	$("#divEditSkuFamilia").hide();
	$("#divEditSkuProveedor").show();
	
	$("#txt-sku-proveedor-num-proveedor").val(dataModel.proveedor.numProveedor);
	$('#lbl-sku-proveedor-num-proveedor').addClass('active');
	
	$("#txt-sku-proveedor-proveedor").val(dataModel.proveedor.nomProveedor);
	$('#lbl-sku-proveedor-proveedor').addClass('active');
	
	$('#exclusion-sku-proveedor-table').DataTable({
		"info": true,
		"bDestroy":true,
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
		},
		columns:[
            {name:"ordenCompra", data:"ordenCompra"},
			{name:"clacom", data:"clacom"},
			{name:"sku", data:"sku"},
			{name:"skuDescripcion", data:"skuDescripcion"}
		],
		columnDefs: [
			{
				"targets": 0,
				"className": "text-left"
			}, 
		    {
		        "targets": 1, 
		        "className": "text-left"
		   },
		   {
		        "targets": 2,
		        "className": "text-left",
		   },
		   {
		        "targets": 3,
		        "className": "text-left",
		   }
		],
		data:dataJson
	});
	
	$('.dataTables_length').addClass('bs-select');
}

function eliminarSku(id) {
	var idExclusion = $("#hdIdExclusionSkuDet").val();
	
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
				url: "./delete/carga/det/" + id,
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
					setTimeout(() => { lf_consultarSkuDet(idExclusion); }, 1700);

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

function lf_listarSku(pIdExclusion) {
	$("#hdIdExclusionSku").val(pIdExclusion);
	var url = './list/disponible/sku/unico';
	$.ajax({
    	url: url,
    	data: {idExclusion: pIdExclusion
			 , familia: ""},
    	type: "get", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		lf_loadDataConsultaSkuFamilia(data);
    	},
    	error: function(e) {
			console.log("ERROR: ", e);
		},
		done: function(e) {
			console.log("DONE");
		}
    });
}

function lf_consultarSkuByOc(pIdExclusion, pProveedor, pOrdenCompra) {
	var url = './list/disponible/sku/oc';
	$.ajax({
    	url: url,
    	data: {idExclusion: pIdExclusion
    	     , proveedor: pProveedor
			 , ordenCompra: pOrdenCompra},
    	type: "get", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		lf_loadDataConsultaSkuDet(data);
    	},
    	error: function(e) {
			console.log("ERROR: ", e);
		},
		done: function(e) {
			console.log("DONE");
		}
    });
}

function lf_consultarSkuByFamilia(pIdExclusion, pProveedor, pOrdenCompra, pClacom) {
	console.log("pProveedor: " + pProveedor);
	console.log("pOrdenCompra: " + pOrdenCompra);
	
	var url = './list/disponible/sku/unico';
	$.ajax({
    	url: url,
    	data: {idExclusion: pIdExclusion
			 , familia: pClacom},
    	type: "get", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		lf_loadDataConsultaSkuFamilia(data);
    	},
    	error: function(e) {
			console.log("ERROR: ", e);
		},
		done: function(e) {
			console.log("DONE");
		}
    });
}

function lf_loadDataConsultaSkuDet(dataModel) {
	var dataJson = dataModel.cicmxOcDtos;
	
	$('#responsiveSkuDet').modal('show');
	$('#responsiveSkuDet').css("z-index", "15000");
	$("#divConsultaSkuFamilia").hide();
	$("#divConsultaSkuArbol").show();
	
	$("#txt-sku-consulta-arbol-num-proveedor").val(dataModel.proveedor.numProveedor);
	$('#lbl-sku-consulta-arbol-num-proveedor').addClass('active');
	
	$("#txt-sku-consulta-arbol-proveedor").val(dataModel.proveedor.nomProveedor);
	$('#lbl-sku-consulta-arbol-proveedor').addClass('active');
	
	$("#txt-sku-consulta-arbol-oc").val(dataModel.ordenCompra);
	$('#lbl-sku-consulta-arbol-oc').addClass('active');
	
	$('#exclusion-sku-consulta-arbol-table').DataTable({
		"info": true,
		"bDestroy":true,
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
		},
		columns:[
			{name:"clacom", data:"clacom"},
            {name:"sku", data:"sku"},
			{name:"skuDescripcion", data:"skuDescripcion"}
		],
		columnDefs: [
			{
				'targets': 0,
				"className": "text-left"
			}, 
			{
				'targets': 1,
				"className": "text-left"
			}, 
		    {
		        "targets": 2, 
		        "className": "text-left"
		   }
		],
		data:dataJson
	});
	
	$('.dataTables_length').addClass('bs-select');
}

function lf_loadDataConsultaSkuFamilia(dataModel) {
	var dataJson = dataModel.cicmxOcDtos;
	
	$('#responsiveSkuDet').modal('show');
	$("#divConsultaSkuFamilia").show();
	$("#divConsultaSkuArbol").hide();
	
	$("#txt-sku-consulta-familia-num-proveedor").val(dataModel.proveedor.numProveedor);
	$('#lbl-sku-consulta-familia-num-proveedor').addClass('active');
	
	$("#txt-sku-consulta-familia-proveedor").val(dataModel.proveedor.nomProveedor);
	$('#lbl-sku-consulta-familia-proveedor').addClass('active');
	
	$("#txt-sku-consulta-familia-familia").val(dataModel.clacom);
	$('#lbl-sku-consulta-familia-familia').addClass('active');
	
	$('#exclusion-sku-consulta-familia-table').DataTable({
		"info": true,
		"bDestroy":true,
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
		},
		columns:[
            {name:"sku", data:"sku"},
			{name:"skuDescripcion", data:"skuDescripcion"}
		],
		columnDefs: [
			{
				'targets': 0,
				"className": "text-left"
			}, 
		    {
		        "targets": 1, 
		        "className": "text-left"
		   }
		],
		data:dataJson
	});
	
	$('.dataTables_length').addClass('bs-select');
}

function lf_loadDataSKUPrincipalDet(dataModel) {
	$('#responsiveEditExclusion').modal('show');
	
	var contabilizado = dataModel.contabilizado;
	var dataJson = dataModel.listExclusiones;
	var selectPeriodo = $('#edit-select-periodo');
	var selectTipoRebate = $('#edit-select-tipo-rebate');
	var selectExclusion = $('#edit-select-tipo-exclusion');
	
	set_select(selectPeriodo, dataModel.periodo.idCatPeriodo);
	set_select(selectTipoRebate, dataModel.catTipoRebate.idCatTipoRebate);
	set_select(selectExclusion, dataModel.catTipoExclusion.idCatTipoExclusion);
	
	$("#edit-txt-comentario").val(dataModel.comentario);
	$('#lbl-comentario').addClass('active');
	
	$("#thTipoExclusion").html("SKU");
	$("#thDetalleExclusion").html("Detalle");
	
	if (contabilizado == 1) {
		$("#btn-create-exclusion").prop("disabled", true);
	} else {
		$("#btn-create-exclusion").prop("disabled", false);	
	}
	
	table = $('#exclusion-edit-table').DataTable({
		"info": true,
		"bDestroy":true,
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
		},
		columns:[
			{name:"numProveedor", data:"numProveedor"},
			{name:"nomProveedor", data:"nomProveedor"},
			{name:"carga", data:"carga"}, 
			{name:"motivo", data:"motivo"},
			{
               "data": "carga", "render": function (data, type, row, meta) {
				   var v_sku = "'" + data + "'";
				   return '<button type="button" title="Detalle SKU" class="blck btn btn-sm btn-link glow-on-hover" style="cursor: pointer" onclick="lf_consultarSkuDetByIdExclusionCarga(' + row.idExclusion + ', '+ row.idExclusionCarga + ',' + row.numProveedor + ',' + v_sku + ')"> <span class="fa-stack fa-lg"> <i class="fas fas-button fa-eye" aria-hidden="true"></i></span></button>';
				}
			},
			{
               "data": "idExclusionCarga", "render": function (data) {
				   if (contabilizado) {
				   		return '<button type="button" title="No es posible eliminar" class="blck btn btn-sm btn-link glow-on-hover" style="cursor: pointer"> <span class="fa-stack fa-lg icon-eraser"> <i class="fas fas-button fa-eraser" aria-hidden="true" style="color: darkgray;"></i></span></button>';
				   } else {
						return '<button type="button" title="Eliminar registro" class="blck btn btn-sm btn-link glow-on-hover" style="cursor: pointer" onclick="eliminarExclusionCarga(' + data + ')"> <span class="fa-stack fa-lg icon-eraser"> <i class="fas fas-button fa-eraser" aria-hidden="true"></i></span></button>';   
				   }
				}
			}
		],
		columnDefs: [
		    {
		        "targets": 0, 
		        "className": "text-center"
		   },
		   {
		        "targets": 1,
		        "className": "text-left",
		   },
		   {
		        "targets": 2,
		        "className": "text-left",
		   },
		   {
		        "targets": 3,
		        "className": "text-left",
		   },
		   {
		        "targets": 4,
		        "className": "text-center"
		   },
		   {
		        "targets": 5,
		        "className": "text-center"
		   }
		],
		data:dataJson
	});
	$('.dataTables_length').addClass('bs-select');
}

function lf_listarSkuByOc(pIdExclusion, pIdExclusionCarga, pProveedor, pOrdenCompra, mostrarCantidades) {

	var url = './detalle/exclusion/sku/oc';
	$.ajax({
    	url: url,
    	data: {idExclusion: pIdExclusion
			 , idExclusionCarga: pIdExclusionCarga
			 , proveedor: pProveedor
			 , ordenCompra:  pOrdenCompra},
    	type: "get", async: true, cache: false, crossDomain: false,
    	success: function(data){
			data.mostrarCantidades=mostrarCantidades
    		lf_loadDataConsultaSkuArbol(data);
    	},
    	error: function(e) {
			console.log("ERROR: ", e);
		},
		done: function(e) {
			console.log("DONE");
		}
    });
	
}

function lf_loadDataConsultaSkuArbol(dataModel) {
	var dataJson = dataModel.listExclusionCargaDet;
	var mostrarCantidades = dataModel.mostrarCantidades;
	$('#responsiveSkuDet').modal('show');
	$("#divConsultaSkuFamilia").hide();
	$("#divConsultaSkuArbol").show();
	
	$("#txt-sku-consulta-arbol-num-proveedor").val(dataModel.proveedor.numProveedor);
	$('#lbl-sku-consulta-arbol-num-proveedor').addClass('active');
	
	$("#txt-sku-consulta-arbol-proveedor").val(dataModel.proveedor.nomProveedor);
	$('#lbl-sku-consulta-arbol-proveedor').addClass('active');
	
	$("#txt-sku-consulta-arbol-oc").val(dataModel.ordenCompra);
	$('#lbl-sku-consulta-arbol-oc').addClass('active');
	
	$('#exclusion-sku-consulta-arbol-table').DataTable({
		"info": true,
		"bDestroy":true,
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
		},
		columns:[
			{name:"clacom", data:"clacom"},
            {name:"sku", data:"sku"},
			{name:"skuDescripcion", data:"skuDescripcion"},
			{name:"cantidadOrdenada", data:"cantidadOrdenada", visible: mostrarCantidades},
			{name:"cantidadRecibida", data:"cantidadRecibida", visible: mostrarCantidades}
		],
		columnDefs: [
			{
				'targets': 0,
				"className": "text-left"
			}, 
			{
				'targets': 1,
				"className": "text-left"
			}, 
		    {
		        "targets": 2, 
		        "className": "text-left"
		    },
			{
				"targets": 3, 
				"className": "text-left"
			},
			{
				"targets": 4, 
				"className": "text-left"
			}
		],
		data:dataJson
	});
	
	$('.dataTables_length').addClass('bs-select');
}
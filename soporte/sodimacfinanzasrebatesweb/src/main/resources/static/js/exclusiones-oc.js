$(document).ready(function() {

});

function ajaxSubmitOCDetForm(pJsonId) {
	var idExclusion = $("#hdIdExclusion").val();
	var objData = JSON.stringify({
		idExclusion: idExclusion,
		motivo: $("#txt-oc-motivo").val(),
		jsonId: pJsonId
	});
	
	$("#btn-create-exclusion-oc").prop("disabled", true);
	
	$.ajax({
		type: "POST",
		contentType: "application/json",
		url: "./add/det",
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

				$("#btn-create-exclusion-oc").prop("disabled", false);
				swal.close();

				return false;
			}
			
			if (tipo == 1) {
				toastr.success(message, titulo);
			} else if (tipo == 4) {
				toastr.info(message, titulo);
			}

			$("#btn-create-exclusion-oc").prop("disabled", false);
			$('#responsiveOC').modal('hide');
			swal.close();
			setTimeout(() => { lf_consultarOrdenCompraDet(idExclusion); }, 1700);

		},
		error: function(e) {

			console.log("ERROR: ", e);
			toastr.error("No se pudo conectar con el servicio", "Error de conexión");
			$("#btn-create-exclusion-oc").prop("disabled", false);
			swal.close();

		}
	});
}


function lf_consultarOrdenCompraDet(idExclusion) {
	var url = './detalle';
	$.ajax({
    	url: url,
    	data: {idExclusion: idExclusion},
    	type: "get", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		lf_loadDataOrdenCompraDet(data);
    	},
    	error: function(e) {
			console.log("ERROR: ", e);
		},
		done: function(e) {
			console.log("DONE");
		}
    });
}

function lf_loadDataOrdenCompraDet(dataModel) {
	$('#responsiveEditExclusion').modal('show');
	
	var contabilizado = dataModel.contabilizado;
	var idUser = $('#hdIdUserParent').val();
	var idEstatusExclusion = dataModel.catEstatusExclusion.idCatEstatusExclusion;
	var idUsuarioSolicitud = dataModel.usuarioSolicitud.id + '';
	var dataJson = dataModel.listExclusiones;
	var selectPeriodo = $('#edit-select-periodo');
	var selectTipoRebate = $('#edit-select-tipo-rebate');
	var selectExclusion = $('#edit-select-tipo-exclusion');
	
	set_select(selectPeriodo, dataModel.periodo.idCatPeriodo);
	set_select(selectTipoRebate, dataModel.catTipoRebate.idCatTipoRebate);
	set_select(selectExclusion, dataModel.catTipoExclusion.idCatTipoExclusion);
	
	$("#edit-txt-comentario").val(dataModel.comentario);
	$('#lbl-comentario').addClass('active');
	
	$("#thTipoExclusion").html("Orden&nbsp;de Compra");
	$("#thDetalleExclusion").html("Skus");

	if (contabilizado == 1 || idEstatusExclusion == 7 || idUser != idUsuarioSolicitud
	 || (dataModel.usuarioAutorizacion.id != null && dataModel.usuarioAutorizacion.id != dataModel.usuarioSolicitud.id)) {
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
			{name:"periodoVigente", data:"periodoVigente", "render": function (data) {
				   if (data == 0) {
					  	return 'No';
				   } else {
						return 'Si';   
				   }
				}
			},
			{name:"tieneAcuerdo", data:"tieneAcuerdo", "render": function (data) {
				   if (data) {
					  	return 'Si';
				   } else {
						return 'No';   
				   }
				}
			},
			{
               "data": "fechaRecepcion", "render": function (data) {
				   return data.substring(0, 10);
				}
			},
			{name:"motivo", data:"motivo"},
			{
               "data": "carga", "render": function (data, type, row, meta) {
				   return '<button type="button" title="Skus" class="blck btn btn-sm btn-link glow-on-hover" style="cursor: pointer" onclick="lf_listarSkuByOc(' + row.idExclusion + ', '+ row.idExclusionCarga + ',' + row.numProveedor + ',' + data + ',' + true + ')"> <span class="fa-stack fa-lg"> <i class="fas fas-button fa-eye" aria-hidden="true"></i></span></button>';
				}
			},
			{
               "data": "idExclusionCarga", "render": function (data) {
				   if (contabilizado || idEstatusExclusion == 7 || idUser != idUsuarioSolicitud) {
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
		        "className": "text-left"
		   },
		   {
		        "targets": 2,
		        "className": "text-center"
		   },
		   {
		        "targets": 3,
		        "className": "text-center"
		   },
		   {
		        "targets": 4,
		        "className": "text-center"
		   },
		   {
		        "targets": 5,
		        "className": "text-center"
		   },
		   {
		        "targets": 6,
		        "className": "text-left"
		   },
		   {
		        "targets": 7,
		        "className": "text-center"
		   },
		    {
		        "targets": 8,
		        "className": "text-center"
		   }
		],
		data:dataJson
	});
	$('.dataTables_length').addClass('bs-select');
}

function lf_listarOrdenCompraDisponible(pIdExclusion) {
	$("#hdIdExclusionSku").val(pIdExclusion);
	
	var url = './list/disponible/oc/sesion';
	$.ajax({
    	url: url,
    	data: {idExclusion: pIdExclusion},
    	type: "get", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		lf_loadDataConsultaOcDisponible(data, pIdExclusion);
    	},
    	error: function(e) {
			console.log("ERROR: ", e);
		},
		done: function(e) {
			console.log("DONE");
		}
    }); 	
}

function lf_listarOrdenCompraByProveedorDisponible(pIdExclusion, pProveedor) {
	$("#hdIdExclusionSku").val(pIdExclusion);
	
	var url = './list/disponible/oc';
	$.ajax({
    	url: url,
    	data: {idExclusion: pIdExclusion
    		, proveedor: pProveedor},
    	type: "get", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		lf_loadDataConsultaOcByProveedorDisponible(data, pIdExclusion, pProveedor);
    	},
    	error: function(e) {
			console.log("ERROR: ", e);
		},
		done: function(e) {
			console.log("DONE");
		}
    }); 	
}

function lf_loadDataConsultaOcByProveedorDisponible(dataModel, idExclusion, pProveedor) {
	var dataJson = dataModel.cicmxOcDtos;
	
	$('#responsiveOcConsulta').modal('show');
	$("#responsiveOcConsulta").css("z-index", "1500");
	$("#txt-oc-consulta-num-proveedor").val(dataModel.proveedor.numProveedor);
	$('#lbl-oc-consulta-num-proveedor').addClass('active');
	
	$("#txt-oc-consulta-proveedor").val(dataModel.proveedor.nomProveedor);
	$('#lbl-oc-consulta-proveedor').addClass('active');
	
	$('#exclusion-oc-consulta-table').DataTable({
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
            {name:"numOc", data:"numOc"},
			{
               "data": "numOc", "render": function (data) {
				   return '<button type="button" title="Art&iacute;culos" class="blck btn btn-sm btn-link glow-on-hover" style="cursor: pointer" onclick="lf_consultarSkuByOc(' + idExclusion + ',' + pProveedor + ',' + data + ')"> <span class="fa-stack fa-lg"> <i class="fas fas-button fa-eye" aria-hidden="true"></i></span></button>';
				}
			}
		],
		columnDefs: [
			{
				'targets': 0,
				"className": "text-left"
			}, 
		    {
		        "targets": 1, 
		        "className": "text-center"
		   }
		],
		data:dataJson
	});
}

function lf_loadDataConsultaOcDisponible(dataModel, pIdExclusion) {

	var idExclusion = pIdExclusion;
	var dataJson = dataModel.cicmxOcDtos;
	
	$('#responsiveOC').modal('show');
	$("#txt-oc-motivo").val("");
	
	$('#exclusion-oc-table').DataTable({
		"info": true,
		"bDestroy":true,
		"paging": true,
		"ordering": false,
		"lengthChange": false,		//Oculta la posibilidad de cambiar el numero de resultados
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
			{
               "data": "fecRecepcion", "render": function (data) {
				   return data.substring(0, 10);
				}
			},
			{
               "data": "numOc", "render": function (data, type, row, meta) {
				   return '<button type="button" title="Art&iacute;culos" class="blck btn btn-sm btn-link glow-on-hover" style="cursor: pointer" onclick="lf_consultarSkuByOc(' + idExclusion + ',' + row.numProveedor + ',' + data +  ')"> <span class="fa-stack fa-lg"> <i class="fas fas-button fa-eye" aria-hidden="true"></i></span></button>';
				}
			}
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
		        "className": "text-left"
		   },
		   {
		        "targets": 3, 
		        "className": "text-center"
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
		select: {
			'style': 'multi'
		},
		data:dataJson
	});
	
	/*$(".dt-checkboxes-select-all").attr("checked", true); // by classname
	var data = tableOC.rows().data();
 	data.each(function (value, index) {
    	//console.log(`For index ${index}, data value is ${value}`);
    	var tr = tableOC.row(index).node();
    	$(tr).find("input[type='checkbox']").each(function() {
			$(this).attr("checked", true);
		});
 	});
	$('.dataTables_length').addClass('bs-select');*/	
}


function lf_consultarOcDetByProveedor(idExclusion, idExclusionCarga, pProveedor) {
	
	$("#hdIdExclusionSkuDet").val(idExclusionCarga);
	var url = './detalle/exclusion/oc/proveedor';
	$.ajax({
    	url: url,
    	data: {idExclusion: idExclusion
			 , idExclusionCarga: idExclusionCarga
			 , proveedor: pProveedor},
    	type: "get", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		lf_loadDataConsultaOcByProveedor(data, idExclusion, idExclusionCarga, pProveedor);
    	},
    	error: function(e) {
			console.log("ERROR: ", e);
		},
		done: function(e) {
			console.log("DONE");
		}
    });
}

function lf_loadDataConsultaOcByProveedor(dataModel, idExclusion, idExclusionCarga, pProveedor) {
	var dataJson = dataModel.listExclusionCargaDet;
	
	$('#responsiveOcConsulta').modal('show');
	$("#txt-oc-consulta-num-proveedor").val(dataModel.proveedor.numProveedor);
	$('#lbl-oc-consulta-num-proveedor').addClass('active');
	
	$("#txt-oc-consulta-proveedor").val(dataModel.proveedor.nomProveedor);
	$('#lbl-oc-consulta-proveedor').addClass('active');
	
	$('#exclusion-oc-consulta-table').DataTable({
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
			{
               "data": "ordenCompra", "render": function (data) {
				   return '<button type="button" title="Art&iacute;culos" class="blck btn btn-sm btn-link glow-on-hover" style="cursor: pointer" onclick="lf_listarSkuByOc(' + idExclusion + ',' + idExclusionCarga + ',' + pProveedor + ',' + data + ',' + false + ')"> <span class="fa-stack fa-lg"> <i class="fas fas-button fa-eye" aria-hidden="true"></i></span></button>';
				}
			}
		],
		columnDefs: [
			{
				'targets': 0,
				"className": "text-left"
			}, 
		    {
		        "targets": 1, 
		        "className": "text-center"
		   }
		],
		data:dataJson
	});
}


function set_select(selectObj, id) {
	selectObj.materialSelect('destroy');
	selectObj.val(id).change();;
	selectObj.materialSelect();
}


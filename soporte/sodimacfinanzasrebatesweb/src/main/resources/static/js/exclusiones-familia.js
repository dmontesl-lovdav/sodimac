$(document).ready(function() {

});

function ajaxSubmitFamiliaDetForm(pJsonId) {
	var idExclusion = $("#hdIdExclusion").val();
	var objData = JSON.stringify({
		idExclusion: idExclusion,
		motivo: $("#txt-familia-motivo").val(),
		jsonId: pJsonId
	});
	
	$("#btn-create-exclusion-familia").prop("disabled", true);
	
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

				$("#btn-create-exclusion-familia").prop("disabled", false);
				swal.close();

				return false;
			}
			
			if (tipo == 1) {
				toastr.success(message, titulo);
			} else if (tipo == 4) {
				toastr.info(message, titulo);
			}

			$("#btn-create-exclusion-familia").prop("disabled", false);
			$('#responsiveFamilia').modal('hide');
			swal.close();
			setTimeout(() => { lf_consultarFamiliaDet(idExclusion); }, 1700);

		},
		error: function(e) {

			console.log("ERROR: ", e);
			toastr.error("No se pudo conectar con el servicio", "Error de conexión");
			$("#btn-create-exclusion-oc").prop("disabled", false);
			swal.close();

		}
	});
}


function lf_consultarFamiliaDet(idExclusion) {
	var url = './detalle';
	$.ajax({
    	url: url,
    	data: {idExclusion: idExclusion},
    	type: "get", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		lf_loadDataFamiliaDet(data);
    	},
    	error: function(e) {
			console.log("ERROR: ", e);
		},
		done: function(e) {
			console.log("DONE");
		}
    });
}

function lf_loadDataFamiliaDet(dataModel) {
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
	
	$("#thTipoExclusion").html("Familia");
	$("#thDetalleExclusion").html("Skus");
	
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
				   var v_familia = "'" + data + "'";
				   return '<button type="button" title="Art&iacute;culos" class="blck btn btn-sm btn-link glow-on-hover" style="cursor: pointer" onclick="lf_consultarSkuDetByFamilia(' + row.idExclusion + ', '+ row.idExclusionCarga + ',' + row.numProveedor + ',' + v_familia + ')"> <span class="fa-stack fa-lg"> <i class="fas fas-button fa-eye" aria-hidden="true"></i></span></button>';
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
		        "className": "text-left"
		   },
		   {
		        "targets": 2,
		        "className": "text-center"
		   },
		   {
		        "targets": 3,
		        "className": "text-left"
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

function lf_listarFamiliaDisponible(pIdExclusion) {
	$("#hdIdExclusionSku").val(pIdExclusion);
	
	var url = './list/disponible/familia/sesion';
	$.ajax({
    	url: url,
    	data: {idExclusion: pIdExclusion},
    	type: "get", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		lf_loadDataFamilia(data, pIdExclusion);
    	},
    	error: function(e) {
			console.log("ERROR: ", e);
		},
		done: function(e) {
			console.log("DONE");
		}
    }); 	
}

function lf_loadDataFamilia(dataModel, pIdExclusion) {
	var idExclusion = pIdExclusion;//$("#hdIdExclusionSku").val();
	var dataJson = dataModel.cicmxOcDtos;
	
	$('#responsiveFamilia').modal('show');
	$("#txt-familia-motivo").val("");
	
	$('#exclusion-familia-table').DataTable({
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
            {name:"clacom", data:"clacom"},
			{
               "data": "clacom", "render": function (data, type, row, meta) {
				   var v_familia = "'" + data + "'";
				   return '<button type="button" title="Art&iacute;culos" class="blck btn btn-sm btn-link glow-on-hover" style="cursor: pointer" onclick="lf_consultarSkuByFamilia(' + idExclusion + ',' + row.numProveedor + ',' + row.numOc + ',' + v_familia +  ')"> <span class="fa-stack fa-lg"> <i class="fas fas-button fa-eye" aria-hidden="true"></i></span></button>';
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
		        "className": "text-left"
		   },
		   {
		        "targets": 4, 
		        "className": "text-left"
		   }
		],
		select: {
			'style': 'multi'
		},
		data:dataJson
	});
	
	/*$(".dt-checkboxes-select-all").attr("checked", true); // by classname
	var data = tableFamilia.rows().data();
 	data.each(function (value, index) {
    	//console.log(`For index ${index}, data value is ${value}`);
    	var tr = tableFamilia.row(index).node();
    	$(tr).find("input[type='checkbox']").each(function() {
			$(this).attr("checked", true);
		});
 	});
	
	$('.dataTables_length').addClass('bs-select');*/
}


function set_select(selectObj, id) {
	selectObj.materialSelect('destroy');
	selectObj.val(id).change();;
	selectObj.materialSelect();
}
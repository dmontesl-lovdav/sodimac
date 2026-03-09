var indices_tabla = {
	 PARAM_BUTTON:0
	,NOMBRE_PARAMETRO:1
	,VALOR:2,APLICACION:3
	,DESCRIPCION:4
	,FECHA_CREACION:5
	,FECHA_MODIFICACION:6
	,USUARIO_MODIFICACION:7
	,ID_TIPO_PARAMETRO:8
	,DESC_TIPO_PARAMETRO:9
	,ESTATUS:10
	,VALOR_INACTIVO:11
}

$(document).ready(function(){
	
	releaseEventEmpty("nombreBusqueda");
	
	//Submit asincrono del formulario de creacion/actualizacion de un Parametro
	$("#formParametro").submit(function(event) {
	
		$('#footerMsg').removeAttr('hidden');
		$('#footerMsg').addClass('alert-success').removeClass('alert-danger');
		$('#footerMsg').html('Guardando cambios...');
		
	    event.preventDefault();
	    
	    var formData = $(this).serialize();
	    var action = $('#action').val();
	    
	    $.ajax({
	      type: "POST",
	      url: "./guardarParametro?action=" + action,
	      data: formData,
	      success: function(response) {
			   $('#footerMsg').addClass('alert-success').removeClass('alert-danger');
			   $('#footerMsg').html('Cambios guardados exitosamente');
			   listarParametros();
	      },
	      error: function(xhr, status, error) {
			   $('#footerMsg').addClass('alert-danger').removeClass('alert-success');
	           $('#footerMsg').html('Error al guardar');
	      }
	    });
	    
 	});
 	
 	$.fn.dataTable.render.ellipsis = function (limite) {
	    return function ( data, type, row, meta ) {
	        return type === 'display' && data.length > limite ? data.substr( 0, limite ) + '…' : data;
	    }
	};
	
});

function descargaxlsxOnClick() {

	$('#btnDescargaxlsx').prop("disabled", true);
	showSpinner();
	
	var url = './listarParametros/toExcel';
	let a = document.createElement('a');
	a.href = url;
	document.body.appendChild(a);
	a.click();
	document.body.removeChild(a);
	hideSpinner();
	$('#btnDescargaxlsx').prop("disabled", false);
	return; 
}

function listarParametros() {
	
	console.log('Consulta parametros');
	
	showSpinner();	
	$('#btnBuscarParametros').prop("disabled", true);
	
	var url = './listarParametros?sparam=' + $('#nombreBusqueda').val();
	const fragmentDiv = document.getElementById("fragmentDiv");
	$.ajax({
		type: "GET",
		//async: false,
		contentType: "application/json",
		url: url,
		//data: objData,
		timeout: 30000,
		success: function(response) {
			hideSpinner(); //TODO: validar
			
			if (response.indexOf("Inicio de Ses") != -1) {
				window.location.href = './';
			} else {
				fragmentDiv.innerHTML = response;
				load_table_style();
				hideSpinner(); //TODO: validar
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
	    	
    $('#btnBuscarParametros').prop("disabled", false);
    
}



/**
 * Muestra el dialogo para crear un nuevo Parametro
 */
function showNewParameterModalForm(){
	
	$('#action').val('newParameter');
	$('#sysParameterModalForm #nombreParametro').prop("readonly", false);
	
	//Se resetean los input de formulario
	$('#sysParameterModalForm #nombreParametro').val('');
	$('#sysParameterModalForm #valor').val('');
	$('#sysParameterModalForm #aplicacion').val('Facturacion');
	$('#sysParameterModalForm #descripcion').val('');
	$('#sysParameterModalForm #tipoParametro').val(1);
	$('#sysParameterModalForm #rol').val(1);
	$('#sysParameterModalForm #estatus').val(1);
	$('#sysParameterModalForm #valorInactivo').val('');
	
	$('#sysParameterModalForm #tituloFormulario').html('Nuevo Parametro');
	
	//Se restean los colores del alert
	$('#footerMsg').addClass('alert-success').removeClass('alert-danger');
	$('#footerMsg').attr('hidden', 'hidden');
	
	//Se muestra el formulario
	$('#sysParameterModalForm').modal('show');
}

/**
 * Muestra el dialogo para modificar un Parametro
 */
function showEditParameterModalForm(rowId){
	
	$('#action').val('updParameter');
	$('#sysParameterModalForm #nombreParametro').prop("readonly", true);
	
	//Se obtienen desde la tabla los datos del Parametro seleccionado
	poblarFormulario(table.row(rowId).data(), '#sysParameterModalForm');
	
	$('#sysParameterModalForm #tituloFormulario').html('Parametro');	
    $('#footerMsg').addClass('alert-success').removeClass('alert-danger');
	$('#footerMsg').attr('hidden', 'hidden');
	
	//Se muestra el formulario
	$('#sysParameterModalForm').modal('show');
	
}

function showViewParameterModalForm(rowId){
	
	//Se obtienen desde la tabla los datos del Parametro seleccionado
	poblarFormulario(table.row(rowId).data(), '#readOnlySysParameterModalForm');
	$('#readOnlySysParameterModalForm #tituloFormulario').html('Parametro');	
	//Se muestra el formulario
	$('#readOnlySysParameterModalForm').modal('show');
	
}

function poblarFormulario(data, idFormulario){
	$(idFormulario).find('#nombreParametro').val(data[indices_tabla.NOMBRE_PARAMETRO]);
	$(idFormulario).find('#valor').val(data[indices_tabla.VALOR]);
	$(idFormulario).find('#aplicacion').val(data[indices_tabla.APLICACION]);
	$(idFormulario).find('#descripcion').val(data[indices_tabla.DESCRIPCION]);
	$(idFormulario).find('#tipoParametro').val(data[indices_tabla.ID_TIPO_PARAMETRO]);
	$(idFormulario).find('#estatus').val(data[indices_tabla.ESTATUS]);
	$(idFormulario).find('#fechaCreacion').val(data[indices_tabla.FECHA_CREACION]);
	$(idFormulario).find('#valorInactivo').val(data[indices_tabla.VALOR_INACTIVO]);
}

function load_table_style() {
	//Config dataTable devices.
	table = $('#data').DataTable({
		"order": [[indices_tabla.FECHA_MODIFICACION, "desc"]],
		"aaSorting": [],
		"columnDefs": [
		{
			targets: indices_tabla.PARAM_BUTTON,
			orderable: false,
            render: function(data, type, row, meta){
				var $editButton = $(data);
				if($editButton.val() == 'update'){
					$editButton.attr('onclick','showEditParameterModalForm('+meta.row+')');
				}else{
					$editButton.attr('onclick','showViewParameterModalForm('+meta.row+')');
				}
				
				return $editButton.prop('outerHTML');				
			}
		},
		{
			targets: indices_tabla.NOMBRE_PARAMETRO,
			orderable: false,
            render: function(data, type, row, meta){			
				return "<a href='javascript:void(0)' onclick='showEditParameterModalForm("+meta.row+")'>"+data+"</a>";
			}
		},
		{
			targets: indices_tabla.VALOR,
			orderable: false,
            render: $.fn.dataTable.render.ellipsis(15)
		},
		{
			targets: indices_tabla.APLICACION,
			orderable: false
		},	
		{
			targets: indices_tabla.DESCRIPCION,
			orderable: false
		},
		{
			targets: indices_tabla.FECHA_CREACION,
			orderable: false
		},
		{
			targets: indices_tabla.FECHA_MODIFICACION,
			orderable: true
		},
		{
			targets: indices_tabla.FECHA_MODIFICACION,
			orderable: false
		},
		{
			targets: indices_tabla.ID_TIPO_PARAMETRO,
			visible: false
		},
		{
			targets: indices_tabla.DESC_TIPO_PARAMETRO,
			visible: true
		},
		{
			targets: indices_tabla.ESTATUS,
			orderable: false,
			render: function(data, type, row, meta){
				return data == 1 ? 'Activo' : 'Inactivo';			
			}
		},
		{
			targets: indices_tabla.VALOR_INACTIVO,
			orderable: false,
            render: $.fn.dataTable.render.ellipsis(15)
		}
		],	
		"info": true,
		"paging": true,
		"ordering": true,
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

function showSpinner () {
  document.getElementById("spinner").classList.remove("hide");
  document.getElementById("spinner").classList.add("show");
}

function hideSpinner () {	
  document.getElementById("spinner").classList.remove("show");
  document.getElementById("spinner").classList.add("hide");
}

function releaseEventEmpty(id){
    var id_new ="#"+id;
    $( id_new )
    .blur(function() {
        if (document.getElementById(id).value.length > 0){
            document.getElementById(id).classList.add("full");
        } else {
            document.getElementById(id).classList.remove("full");            
        }
    });
}

function txtnombreCampo_onkeypress() {
	var x = event.keyCode;
	//32=space
	  if(x==32){
		 event.preventDefault();
	  }
}

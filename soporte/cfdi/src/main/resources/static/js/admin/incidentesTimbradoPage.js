
var MODAL_CONFIRMAR_ELIMINAR_TICKET = "#modalConfirmar";
var MODAL_CONFIRMAR_ELIMINAR_INTENTOS = "#modalConfirmarIntentos";

Mensajes_js = {
		MENSAJE_ADVERTENCIA_ELIMINAR_TICKET : '¿Esta seguro que desea eliminar el ticket en proceso?',
		MENSAJE_TICKET_ELIMINADO_EXITO:'Se ha eliminado el ticket _TICKET_ con &eacute;xito',
		MENSAJE_ADVERTENCIA_ELIMINAR_INTENTOS : '¿Esta seguro que desea eliminar los intentos?',
		MENSAJE_INTENTOS_ELIMINADO_EXITO:'Se han eliminado los intentos del ticket _TICKET_ con &eacute;xito'
	};

var campos_vacios_message = "El campo está vacío";
var mensage_mail_invalido = "El correo no es válido";
const emailRegex = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;

$(document).ready(function(){
	
	releaseEventEmpty("ticketBusqueda");
	 	
 	$.fn.dataTable.render.ellipsis = function (limite) {
	    return function ( data, type, row, meta ) {
	        return type === 'display' && data.length > limite ? data.substr( 0, limite ) + '…' : data;
	    }
	};
	
});


function consultarTicket() {
	
	if ($('#ticketBusqueda').val().trim() == "" ) {
		return;
	}
	
	showSpinner();	
	$('#btnConsultarTicket').prop("disabled", true);
	
	var url = './listarIncidentesTimbrado?sparam=' + $('#ticketBusqueda').val();
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
				if (response.indexOf("Mensaje de error") != -1){
					$('#btnEliminarTicket').prop("disabled", false);
					$('#btnEliminarIntentos').prop("disabled", false);
				} else {
					$('#btnEliminarTicket').prop("disabled", true);
					$('#btnEliminarIntentos').prop("disabled", true);
				}
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
	    	
    $('#btnConsultarTicket').prop("disabled", false);
    
}


function showDeleteTicketModalForm(){
		$(MODAL_CONFIRMAR_ELIMINAR_TICKET).modal("show");
		$("#divWarningConfirmar").html(Mensajes_js.MENSAJE_ADVERTENCIA_ELIMINAR_TICKET);
}

function showDeleteIntentosModalForm(){
		$(MODAL_CONFIRMAR_ELIMINAR_INTENTOS).modal("show");
		$("#divWarningConfirmarIntentos").html(Mensajes_js.MENSAJE_ADVERTENCIA_ELIMINAR_INTENTOS);
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

function deleteTicketConfirmOnClick() {
	$(MODAL_CONFIRMAR_ELIMINAR_TICKET).modal("hide");
	var ticket = $('#ticketBusqueda').val();
    
  	showSpinner();
	$('#btnEliminarTicket').prop("disabled", true);
	
	var url = './eliminarTicket';
	$.ajax({
		type: "POST",
		async: false,
		contentType: "application/json",
		url: url,
		data: ticket,
		timeout: 30000,
		success: function(response) {
			if (response.indexOf("Inicio de Ses") != -1) {
				window.location.href = './';
			} else {
				var data = response;
				Swal.fire('Facturación Sodimac',Mensajes_js.MENSAJE_TICKET_ELIMINADO_EXITO.replace("_TICKET_",data),'success');
				consultarTicket();
			}
		},
		error: function(e) {
			console.log("ERROR: ", e);
		},
		done: function(e) {
			console.log("DONE");
		}
	});
	    	
    $('#btnEliminarTicket').prop("disabled", false);
    hideSpinner();
}

function deleteIntentosConfirmOnClick() {
	$(MODAL_CONFIRMAR_ELIMINAR_INTENTOS).modal("hide");
	var ticket = $('#ticketBusqueda').val();
    
  	showSpinner();
	$('#btnEliminarIntentos').prop("disabled", true);
	
	var url = './eliminarIntentos';
	$.ajax({
		type: "POST",
		async: false,
		contentType: "application/json",
		url: url,
		data: ticket,
		timeout: 30000,
		success: function(response) {
			if (response.indexOf("Inicio de Ses") != -1) {
				window.location.href = './';
			} else {
				var data = response;
				Swal.fire('Facturación Sodimac',Mensajes_js.MENSAJE_INTENTOS_ELIMINADO_EXITO.replace("_TICKET_",data),'success');
				consultarTicket();
			}
		},
		error: function(e) {
			console.log("ERROR: ", e);
		},
		done: function(e) {
			console.log("DONE");
		}
	});
	    	
    $('#btnEliminarIntentos').prop("disabled", false);
    hideSpinner();
}

function ticketInput_onkeypress() {
	var x = event.keyCode;
	var w = event.which;
	//32=space , 46=Suprimir key, w=17 y x=86 = ctrl-V
	  if( x==undefined || x==32 || x==46 || (w==17 && x==86)){
	  } else {
		  	var re = new RegExp("^[0-9]*$");
			var isValid = re.test(String.fromCharCode(x));
		    if (!isValid) {
		    	event.preventDefault();
		    }
	  }
}

function obtenerCorreo(uuid, email)
{
	if (uuid=="") return;
	$("#uuid").val(uuid);
	$('#enviarUserDate').prop("disabled", false);

	$("#correo_electronico_anterior").val(email);
	addSuccess("correo_electronico_anterior");

	releaseEventEmpty("correo_electronico_nuevo");
	releaseEventEmpty("correo_electronico_anterior");

	$("#responsive2").modal();

}

function reenviarFactura()
{
	var uuid = $("#uuid").val().trim();
	var emailCC = $("#correo_electronico_nuevo").val().toLowerCase().trim();

	if (uuid=="") return;
	
	if (emailCC == ""){
        messageDanger("#correo_electronico_nuevo_validation", campos_vacios_message);
        addDanger("correo_electronico_nuevo");
        return;
    }

	if (!validateEmail(emailCC)){
        messageDanger("#correo_electronico_nuevo_validation", mensage_mail_invalido);
        addDanger("correo_electronico_nuevo");
        return;
    }

	$('#enviarUserDate').prop("disabled", true);
	var url = './ReenvioFactura';

    $.ajax({
    	url: url,
    	data: {id:uuid, eMailCC: emailCC},
    	type: "get", async: true, cache: false, crossDomain: false,
    	success: function(data){

    		if (data == "success"){
    			$("#responsive2").modal("hide");
    			$('.modal-backdrop').remove();
        		Swal.fire(
        				  'Facturación Sodimac',
        				  'Se ha reenviado el documento correctamente',
        				  'success'
        				)
    		}

    		if (data == "error")
        		Swal.fire(
      				  'Facturación Sodimac',
      				  'Ocurrió un problema al reenviar el documento, intente mas tarde',
      				  'warning'
      				)    		
    	}
    });

}

function validaCamposVacios(id,idAnswer,classAddSuccess, classAddError, method){
    field = document.getElementById(id).value;
    if (field.length == 0) {
        messageDanger(idAnswer,campos_vacios_message);
        addDanger(id);
    } else {
        $(idAnswer).empty();
        addSuccess(id);
        if (method == "email"){
            var mailValidate = $("#"+id).val();   
            if (!validateEmail(mailValidate)){
                messageDanger(idAnswer,mensage_mail_invalido);
                addDanger(id);  
            }
        }
    }
}

function addSuccess(id){
    var element = document.getElementById(id);
    element.classList.remove("full", "has-danger");                    
    var success = "full has-success";                       
    var space = success.indexOf(" ");
    var cadenaOne = success.substring(0, space);
    var cadenaTwo = success.substring(space+1, success.length);
    element.classList.add(cadenaOne, cadenaTwo);                    
}

function validateEmail(email) {

	var re = new RegExp(emailRegex);
    return re.test(email);
}

function messageDanger(id,descriptionMessage){
	$(id).empty();
    $(id).append('<div class="alert alert-danger alert-dismissible fade show m-t-10" role="alert" ><button type="button" class="close" data-dismiss="alert" aria-label="Close"><i class="ti-close"></i></button><div class="alert_icon"><i class="ti-na"></i></div><div class="alert_data">'+descriptionMessage+'</div></div>');
}

function addDanger(id){
    var element = document.getElementById(id);
    element.classList.remove("full", "has-success");                    
    var error = "full has-danger";
    var space = error.indexOf(" ");
    var cadenaOne = error.substring(0, space);
    var cadenaTwo = error.substring(space+1, error.length);
    element.classList.add(cadenaOne, cadenaTwo);                    
}

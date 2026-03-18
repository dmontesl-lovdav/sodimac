eventosConsulta();

$("#rfcInput_consulta_factura").focus();

var dTotClientesListObj = [];

var url = '/facturacion/getExpresionRegular';

$.ajax({
	url: url,
	type: "post", async: true, cache: false, crossDomain: false,
	success: function(data){
		var datos=data.split("@");
		$("#hdnExpresionRegularRfcCaracteres").val(datos[0]);
		$("#hdnExpresionRegularRfc").val(datos[1]);
	}
});

function createClientesList (uuid, nombreArchivo) {

	if (uuid != "") {
		dTotClientesListObj.push({
			uuid: uuid, 
			checked: false,
			nombreArchivo: nombreArchivo
			});
	}
	
}

function verificaTabRFC_consulta_factura(event){
    if (event.keyCode==9 || event.keyCode==13) {
    	validaRFC_consulta_factura();
        return;
    }
  
	var keyCode = event.keyCode || event.which;
    var re = new RegExp($("#hdnExpresionRegularRfcCaracteres").val());
    var isValid = re.test(String.fromCharCode(keyCode));
    if (!isValid) {
    	event.preventDefault();
    }
        
}

function validaRFC_consulta_factura(){
    $("#rfc_validation_consulta_factura").empty();        
    var rfc = $("#rfcInput_consulta_factura")[0].value.toUpperCase();
    var valrfc = rfcValidation(rfc);
    if (valrfc != true){
        $("#rfc_validation_consulta_factura").empty();
        lonRfc = $("#rfcInput_consulta_factura").val().length;
        if (lonRfc == 0){
            addDanger("rfcInput_consulta_factura");  
            messageDanger("#rfc_validation_consulta_factura", rfc_vacío);
        } else {
            addDanger("rfcInput_consulta_factura");  
            messageDanger("#rfc_validation_consulta_factura", rfc_no_valido);
        }

    }   else  {
        $("#rfc_validation_consulta_factura").empty();        
        var rfcValidate = existeRFC(rfc);
        if ( rfcValidate === 0){
            addDanger("rfcInput_consulta_factura");  
            messageDanger("#rfc_validation_consulta_factura", rfc_no_valido);
        } else {
            $("#rfc_validation_consulta_factura").empty();        
        }
        $("#rfc_validation_consulta_factura").empty();                
        addSuccess("rfcInput_consulta_factura");  
    }
}

function eventosConsulta(){
    releaseEventEmpty("rfcInput_consulta_factura");
    releaseEventEmpty("ticketHolder_consulta_factura");
    releaseEventEmpty("captcha-input");

    releaseEventCopiPaste("#rfcInput_consulta_factura", "copy", "#rfc_validation_consulta_factura", "El sistema no permite copiar, ¡Gracias!");
    releaseEventCopiPaste("#rfcInput_consulta_factura", "paste", "#rfc_validation_consulta_factura", "El sistema no permite pegar, ¡Gracias!");
    releaseEventCopiPaste("#rfcInput_consulta_factura", "dragover", "#rfc_validation_consulta_factura", "El sistema no permite drag & drop, ¡Gracias!");
    releaseEventCopiPaste("#ticketHolder_consulta_factura", "copy", "#messajeTicketCompra_consulta_factura", "El sistema no permite copiar, ¡Gracias!");
    releaseEventCopiPaste("#ticketHolder_consulta_factura", "paste", "#messajeTicketCompra_consulta_factura", "El sistema no permite pegar, ¡Gracias!");
    releaseEventCopiPaste("#ticketHolder_consulta_factura", "dragover", "#messajeTicketCompra_consulta_factura", "El sistema no permite drag & drop, ¡Gracias!");
}

function buscarFactura(){
	
	$("#rfc_validation_consulta_factura").empty();
	$("#messajeTicketCompra_consulta_factura").empty();
	
	var rfc = $("#rfcInput_consulta_factura").val().trim().toUpperCase();
	var ticket = $("#ticketHolder_consulta_factura").val().trim();

	if (rfc == ""){
        messageDanger("#rfc_validation_consulta_factura", rfc_vacío);
        addDanger("rfcInput_consulta_factura");
        return;
    }

	if (!rfcValidarExpReg(rfc)){
        messageDanger("#rfc_validation_consulta_factura", rfc_no_valido);
        addDanger("rfcInput_consulta_factura");
        return;
    }

	if (ticket == ""){
        messageDanger("#messajeTicketCompra_consulta_factura", mensaje_leyenda_ticket_orden_vacio);
        addDanger("ticketHolder_consulta_factura");
        return;
    }

	if (ticket.length > 10 && ticket.length < 19) {
        messageDanger("#messajeTicketCompra_consulta_factura", mensaje_leyenda_numero_ticket_genera_factura);
        addDanger("ticketHolder_consulta_factura");
        return;		
	}

	var fechaTicket;
	var fechaTmp;
	var fechaTmp1;
	var fechaTick; 
	var dateRegex = /^(?=\d)(?:(?:31(?!.(?:0?[2469]|11))|(?:30|29)(?!.0?2)|29(?=.0?2.(?:(?:(?:1[6-9]|[2-9]\d)?(?:0[48]|[2468][048]|[13579][26])|(?:(?:16|[2468][048]|[3579][26])00)))(?:\x20|$))|(?:2[0-8]|1\d|0?[1-9]))([-.\/])(?:1[012]|0?[1-9])\1(?:1[6-9]|[2-9]\d)?\d\d(?:(?=\x20\d)\x20|$))?(((0?[1-9]|1[012])(:[0-5]\d){0,2}(\x20[AP]M))|([01]\d|2[0-3])(:[0-5]\d){1,2})?$/;
	
	if (ticket.length >= 19){
		if (ticket.length === 20)
			fechaTicket = ticket.substr(1,8);	

		if (ticket.length === 19)
			fechaTicket = ticket.substr(0,8);

		fechaTemp1 = + fechaTicket.substr(4,2) + '/' + fechaTicket.substr(6,2) + '/' + fechaTicket.substr(0,4);
		fechaTmp = fechaTicket.substr(6,2) + '/' + fechaTicket.substr(4,2) + '/' + fechaTicket.substr(0,4);
		
		fechaTick = new Date(fechaTemp1);
		
		if (!dateRegex.test(fechaTmp)){
	        messageDanger("#messajeTicketCompra_consulta_factura", "No es un ticket con fecha válida");
	        addDanger("ticketHolder_consulta_factura");
			return;
		}
	    
		var fechaActual = new Date();
		
		if (fechaTick > fechaActual){
	        messageDanger("#messajeTicketCompra_consulta_factura", "No es un ticket con fecha válida");
	        addDanger("ticketHolder_consulta_factura");
			return;
		}			
	}

	
	var urlLista = '/facturacion/ListarFactura/' + rfc + '?ticket=' + ticket;
    $("#tab33Content").load(urlLista);

}

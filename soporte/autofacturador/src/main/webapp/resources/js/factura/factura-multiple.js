function rfcValidationMultiple(){
    return (!rfcValidarExpReg($("#rfcInput_multiples_factura").val().toUpperCase()) ? false : true);
}


function isValidDate(idDateDesde, validationFieldDesde, idDateHasta, validationFieldHasta, typeDate) {
	
	setTimeout(function(){ 
		if ($("#" + idDateDesde).val() == "") {
			validaCamposVacios(idDateDesde, '#' + validationFieldDesde, 'text');
			validaCamposVacios(idDateHasta, '#' + validationFieldHasta, 'text');
			
			if ($("#" + idDateDesde).val() == "" && $("#" + idDateHasta).val() == "" ) {
	  			validaCamposVacios(idDateDesde, '#' + validationFieldDesde, 'text');
				validaCamposVacios(idDateHasta, '#' + validationFieldHasta, 'text');
				
	  	        var element = document.getElementById("#" + idDateDesde);
			}

		} else {
	
					//regex for dd/mm/yyyy, dd-mm-yyyy or dd.mm.yyyy
				  	  var regex2 = /^(?:(?:31(\/|-|\.)(?:0?[13578]|1[02]))\1|(?:(?:29|30)(\/|-|\.)(?:0?[13-9]|1[0-2])\2))(?:(?:1[6-9]|[2-9]\d)?\d{2})$|^(?:29(\/|-|\.)0?2\3(?:(?:(?:1[6-9]|[2-9]\d)?(?:0[48]|[2468][048]|[13579][26])|(?:(?:16|[2468][048]|[3579][26])00))))$|^(?:0?[1-9]|1\d|2[0-8])(\/|-|\.)(?:(?:0?[1-9])|(?:1[0-2]))\4(?:(?:1[6-9]|[2-9]\d)?\d{2})$/,
				      dateDesdeValid = regex2.test($("#" + idDateDesde).val());
				  	  dateHastaValid = regex2.test($("#" + idDateHasta).val());
				  
				  	  if (dateDesdeValid && dateHastaValid) {
				  		  var idDateDesdeArr = $("#" + idDateDesde).val().split("/"),
				  		  	  idDateHastaArr = $("#" + idDateHasta).val().split("/");
				  		  
				  		var dateD = new Date ( (+idDateDesdeArr[2]), (+idDateDesdeArr[1])-1, (+idDateDesdeArr[0]), "00", "00", "00" );
				  		var dateH = new Date ( (+idDateHastaArr[2]), (+idDateHastaArr[1])-1, (+idDateHastaArr[0]), "00", "00", "00" );
				  		
				  		var invalidRange = dateH.getTime() < dateD.getTime();
				  		//2592000000 mileseconds = 1 Monts
				  		var rangeHigh = (dateH.getTime() - dateD.getTime()) > (2592000000 * periodo);
				  		
				  		
				  		if (invalidRange || rangeHigh) {
				  			$("#" + idDateDesde).val("");
				  			$("#" + idDateHasta).val("");
				  			$("#" + idDateDesde).datepicker('clearDates');
				  			$("#" + idDateHasta).datepicker('clearDates');
				  		}
				  		
				  		if (rangeHigh) {
			        		Swal.fire(
			        				  'Facturación Sodimac',
			        				  intervalWariningMsg.replace("{meses}", periodo),
			        				  'warning'
			        				)
				  		}
				  	  }
				  	  
				  	if  (!dateDesdeValid) {
				  		$("#" + idDateDesde).val("");
				  	}
				  	if  (!dateHastaValid) {
				  		$("#" + idDateHasta).val("");
				  	}
				  	validaCamposVacios(idDateDesde, '#' + validationFieldDesde, 'text');
				  	validaCamposVacios(idDateHasta, '#' + validationFieldHasta, 'text');
				  	  
				  	  
	
	
		}

	}, 250);
}

var dTotClientesMultipleObj = [];

function clearMultipleObj() {
	dTotClientesMultipleObj = [];
}

function createClientesMultipleParent (uuid, nombreArchivo) {

	dTotClientesMultipleObj.push({
								uuid: uuid, 
								checked: false,
								nombreArchivo: nombreArchivo
								});
	
}

function SetCheckUncheck(uuid){

	if (uuid == "All") {
		var selectAll = $('#selectall').prop('checked');
		dTotClientesMultipleObj.filter(function (_dClientesMultipleObj) {
			_dClientesMultipleObj.checked = selectAll ? true : false;
		});

	} else {
		dTotClientesMultipleObj.filter(function (_dClientesMultipleObj) {
			if (_dClientesMultipleObj.uuid === uuid) {
				_dClientesMultipleObj.checked = !_dClientesMultipleObj.checked;
			}
		});

	}
	
	checkBoxesChecked();
	
	var selectedCount = dTotClientesMultipleObj.reduce(function (accumulator, _dClientesMultipleObj) {
		if (_dClientesMultipleObj.checked) {
			accumulator = accumulator + 1;
		}
		  return accumulator;
		}, 0);

	if (selectedCount < 2) {
		$('#reenviar').prop('disabled', true);
		$('#btnDescargaMultiple').prop('disabled', true);
	} else {
		$('#reenviar').removeProp('disabled');
		$('#btnDescargaMultiple').removeProp('disabled');
	}

}


function SearchFactureMultipleOnLinks(pageNumber, start, rowsPerPage) {
	showSpinner();
	var rfc = $("#rfcInput_multiples_factura").val().toUpperCase().trim();
	var dateDesdeValue = $("#dateDesde").val().trim();
	var dateHastaValue = $("#dateHasta").val().trim();
	var email = $("#email_multiples_factura").val().trim();
	
	var uuidList = "";
	
	dTotClientesMultipleObj.filter(function (_dClientesMultipleObj) {
		if (_dClientesMultipleObj.checked === true) {
			uuidList += (uuidList=="" ? _dClientesMultipleObj.uuid : "," + _dClientesMultipleObj.uuid);
		}
		
	});
	
	  var urlListaMultiple =   '/facturacion/ListarFacturaMultiple?rfc='+rfc+'&dateDesde='+dateDesdeValue+'&dateHasta='+dateHastaValue+'&pageNumber='+ pageNumber + '&start=' + start + '&rowsPerPage=' + rowsPerPage + '&email=' + email + '&uuidList=';                                        
    $("#tab34Content").load(urlListaMultiple);
	
	
	
}

function checkBoxesChecked() {
	var sList = "";
	$('input[type=checkbox]').each(function () {
		if (this.id != "selectall") {
		    var sThisVal = (this.checked ? "1" : "0");
		    if (sThisVal == "1") {
		    	sList += (sList=="" ? this.id : "," + this.id);
		    }    
		}

	});

}

var periodo = 6;
var intervalWariningMsg = "";
var rowsPerPage =5;

$(document).ready(function(){
	
    var url = '/facturacion/multiple/getStartDate';

    $.ajax({
    	url: url,
    	type: "get", async: true, cache: false, crossDomain: false,
    	success: function(data){
    	    $('#date-container .input-daterange').datepicker({
    	        format: "dd/mm/yyyy",
    	        language: "es",
    	        todayBtn: "true",
    	        daysOfWeekHighlighted: "0,6",
    	        clearBtn: true,
    	        weekStart: 1,
    	        startDate: data,
    	        endDate: "today",
    	        todayHighlight: true
    	    });
    		
    	}
    });
	    
    var url = '/facturacion/multiple/getLongitudToken';

    $.ajax({
    	url: url,
    	type: "post", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		var datos=data.split("@");
    		$("#token_multiples_factura").attr("maxlength",datos[0]);
    		$("#hdnExpresionRegularToken").val(datos[1]);
    	}
    });
        
    var url = '/facturacion/getConfiguracion';
    $.ajax({
    	url: url,
    	data: {NombreCampo:"Multiple.date.interval"},
    	type: "post", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		periodo = data;
    	}
    });
    
    var url = '/facturacion/getConfiguracion';
    $.ajax({
    	url: url,
    	data: {NombreCampo:"Multiple.date.interval.warning.message"},
    	type: "post", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		intervalWariningMsg = data;
    	}
    });
    
    var url = '/facturacion/getConfiguracion';
    $.ajax({
    	url: url,
    	data: {NombreCampo:"Multiple.data.grid.rowsPerPage"},
    	type: "post", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		rowsPerPage = data;
    	}
    });
      
    
    $("body").on('DOMSubtreeModified', "#responselabel", function() {
        var selected = $('#responselabel');
        $(document).scrollTop(selected[0].offsetParent.offsetTop);
        hideSpinner();
    });

    releaseEventEmpty("rfcInput_multiples_factura");
    releaseEventEmpty("token_multiples_factura");
    releaseEventCopiPaste("#rfcInput_multiples_factura", "paste", "#rfcInput_multiples_factura_validate", "El sistema no permite pegar, ¡Gracias!");
    releaseEventCopiPaste("#rfcInput_multiples_factura", "copy", "#rfcInput_multiples_factura_validate", "El sistema no permite copiar, ¡Gracias!");
    releaseEventCopiPaste("#rfcInput_multiples_factura", "dragover", "#rfcInput_multiples_factura_validate", "El sistema no permite drag & drop, ¡Gracias!");
        
    review_mouseout_rfc_genera_factura_multiple("rfcInput_multiples_factura");
    
    $("#dateHasta").on('blur', function(e){
        var x = event.keyCode;       
      
            event.preventDefault();
        
    })
    
    $("#dateHasta").mask("00/00/0000");
    $("#dateDesde").mask("00/00/0000");
});

function tokenValidarExpReg(e){
	var keyCode = e.keyCode || e.which;
    var re = new RegExp($("#hdnExpresionRegularToken").val());
    var isValid = re.test(String.fromCharCode(keyCode));
    if (isValid) {
    	return isValid;
    } else {
    	e.preventDefault();
    }
    
}

function btnGenerarToken() {

	var rfc = $("#rfcInput_multiples_factura").val().toUpperCase().trim();
	var dateDesdeValue = $("#dateDesde").val().trim();
	var dateHastaValue = $("#dateHasta").val().trim();
	var email = $("#email_multiples_factura").val().trim();

	if (rfc==""){
        messageDanger("#rfcInput_multiples_factura_validate", rfc_vacío);
        addDanger("rfcInput_multiples_factura");
        return;
	}
	
	if (!rfcValidationMultipleBD(rfc)){
        messageDanger("#rfcInput_multiples_factura_validate", rfc_no_valido);
        addDanger("rfcInput_multiples_factura");
        return;
	}
	
	if (dateDesdeValue==""){
        messageDanger("#date_desde_validate", campos_vacios_message);
        addDanger("dateDesde");
        return;
	}
	if (dateHastaValue==""){
        messageDanger("#date_hasta_validate", campos_vacios_message);
        addDanger("dateHasta");
        return;
	}
	
	if (email==""){
        messageDanger("#email_multiples_factura_validate", mensaje_mail_vacio_seccion_datos_fiscales);
        addDanger("email_multiples_factura");
        return;
	}
	
	if (!releaseEventMailMultipleEmpty()) {
		return;
	}

	var url = '/facturacion/GenerarToken';
	showSpinner();
    $.ajax({
    	url: url,
    	data: {rfc:rfc, email:email},
    	type: "get", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		hideSpinner();
    		if (data.success ) {
        		Swal.fire(
      				  'Facturación Sodimac',
      				   data.message,
      				  'success'
      				)
    		}else {
        		Swal.fire(
        				  'Facturación Sodimac',
        				  data.message,
        				  'warning'
        				)
    		}
    		

    	}
    });
	
}


function releaseEventMailMultipleEmpty(){
    email  =$("#email_multiples_factura").val();
    var longitud = ($("#email_multiples_factura").val().length);
    if (longitud != 0){        
        var element = document.getElementById("email_multiples_factura");
        element.classList.add("full");
        var validaRS = validateRS($("#email_multiples_factura").val());
        if  (validateRS){
            var valemail = validateEmail(email);
            if (valemail){
                addSuccess("email_multiples_factura");
                return true;
            } else {
                messageDanger("#email_multiples_factura_validate", mensaje_mail_invalido_seccion_datos_fiscales);
                addDanger("email_multiples_factura");
                return false;
            }
        } else {
            messageDanger("#email_multiples_factura_validate", mensaje_mail_invalido_seccion_datos_fiscales);
            addDanger("email_multiples_factura");
            return false;
        }
    } else {
            messageDanger("#email_multiples_factura_validate", mensaje_mail_vacio_seccion_datos_fiscales);
            addDanger("email_multiples_factura");
            return false;
    }
}

function validateSearchFactureMultiple() {
	
	$("#rfcInput_multiples_factura_validate").empty();
	$("#date_desde_validate").empty();
	$("#date_hasta_validate").empty();
	$("#email_multiples_factura_validate").empty();
	$("#token_multiples_factura_validate").empty();
    $("#messages_consultas_multiples").empty();

	var rfc = $("#rfcInput_multiples_factura").val().toUpperCase().trim();
	var dateDesdeValue = $("#dateDesde").val().trim();
	var dateHastaValue = $("#dateHasta").val().trim();
	var token = $("#token_multiples_factura").val().trim();
	var email = $("#email_multiples_factura").val().trim();

	if (rfc==""){
        messageDanger("#rfcInput_multiples_factura_validate", rfc_vacío);
        addDanger("rfcInput_multiples_factura");
        return;
	}

	if (!rfcValidationMultipleBD(rfc)){
        messageDanger("#rfcInput_multiples_factura_validate", rfc_no_valido);
        addDanger("rfcInput_multiples_factura");
        return;
	}
    
	if (dateDesdeValue==""){
        messageDanger("#date_desde_validate", campos_vacios_message);
        addDanger("dateDesde");
        return;
	}
	if (dateHastaValue==""){
        messageDanger("#date_hasta_validate", campos_vacios_message);
        addDanger("dateHasta");
        return;
	}
	
	if (email==""){
        messageDanger("#email_multiples_factura_validate", mensaje_mail_vacio_seccion_datos_fiscales);
        addDanger("email_multiples_factura");
        return;
	}
	
	if (!releaseEventMailMultipleEmpty()) {
		return;
	}
	
	if (token==""){
        messageDanger("#token_multiples_factura_validate", token_vacio);
        addDanger("token_multiples_factura");
        return;
	}
	if (!tokenValidationMultipleBD(token, rfc, email)){
        messageDanger("#token_multiples_factura_validate", token_no_valido);
        addDanger("token_multiples_factura");
        
        return;
	}

	var rfcInput_multiples_factura = $("#rfcInput_multiples_factura").hasClass("has-success");
    var dateHasta = $("#dateHasta").hasClass("has-success");
    var dateDesde = $("#dateDesde").hasClass("has-success");
    var token_multiples_factura = $("#token_multiples_factura").hasClass("has-success");
    var email_multiples_factura = $("#email_multiples_factura").hasClass("has-success");
    if (rfcInput_multiples_factura && dateHasta && dateDesde && token_multiples_factura && email_multiples_factura){
    	showSpinner();
    	$('#btnBuscarFacturas').prop("disabled", true);
    	var urlListaMultiple = '/facturacion/ListarFacturaMultiple?rfc='+rfc+'&dateDesde='+dateDesdeValue+'&dateHasta='+dateHastaValue+'&pageNumber=1' + '&start=0' + '&rowsPerPage=' + rowsPerPage + '&email=' + email;
        $("#tab34Content").load(urlListaMultiple);
        $('#btnBuscarFacturas').prop("disabled", false);
    } else {
        $("#messages_consultas_multiples").append(mensaje_error_formulario_envio_datos);
        document.getElementById("messages_consultas_multiples").classList.add("text-danger");
    }
}

function review_mouseout_rfc_genera_factura_multiple(id){
    var id_new ="#"+id;
    $( id_new )
    .blur(function() {
        validaRFC_consulta_factura_multiple();
    });
}

function validaRFC_consulta_factura_multiple(){
    $("#rfcInput_multiples_factura_validate").empty();        
    var rfc = $("#rfcInput_multiples_factura")[0].value;
    var valrfc = rfcValidationMultiple(rfc);
    if (valrfc != true){
        $("#rfcInput_multiples_factura_validate").empty();
        lonRfc = $("#rfcInput_multiples_factura").val().length;
        if (lonRfc == 0){
            addDanger("rfcInput_multiples_factura");  
            messageDanger("#rfcInput_multiples_factura_validate", rfc_vacío);
        } else {
            addDanger("rfcInput_multiples_factura");  
            messageDanger("#rfcInput_multiples_factura_validate", rfc_no_valido);
        }
    }   else  {
         addSuccess("rfcInput_multiples_factura");  
        
    }
}

function verificaTabRFC_consulta_factura_multiples(event){
    if (event.keyCode==9 || event.keyCode==13) {
    	validaRFC_consulta_factura_multiple();
        return;
    }
  
	var keyCode = event.keyCode || event.which;
    var re = new RegExp($("#hdnExpresionRegularRfcCaracteres").val());
    var isValid = re.test(String.fromCharCode(keyCode));
    if (!isValid) {
    	event.preventDefault();
    }	
        
}

function rfcValidationMultipleBD(rfc) {
	var result = false;

	if (rfcValidarExpReg(rfc)) {
        var url = '/facturacion/ValidarRfcMultiple';
        $.ajax({
        	url: url,
        	data: {rfc: rfc},
        	type: "post", async: false, cache: false, crossDomain: false,
        	success: function(data){
                if (data == "OK"){
                	result = true;
                }
            }
        });	        
	}
    return result;
}

function tokenValidationMultipleBD(token, rfc, email) {
	var result = false;

    var url = '/facturacion/ValidarTokenConsulta';
    $.ajax({
    	url: url,
    	data: {token: token, rfc: rfc, email: email},
    	type: "post", async: false, cache: false, crossDomain: false,
    	success: function(data){
            if (data == "OK"){
            	result = true;
            }
        }
    });	        
    return result;	
}

function obtenerCorreoMultiple(){
    
	var archivos = nombresArchivosList();

	if (archivos=="" || archivos.indexOf(",") == -1) {
		Swal.fire(
				  'Facturación Sodimac',
				  'Favor de seleccionar al menos dos documentos',
				  'warning'
				)
		return;
	}
	
    $("#correo_electronico_anterior_mult").val($("#email_multiples_factura").val());
    addSuccess("correo_electronico_anterior_mult");
	eventosReenvioMultiple();
	$("#modalReenvioMultiple").modal();
}

function reenviarFacturaMultiple(){

	var rfc = $("#rfcInput_multiples_factura").val().toUpperCase().trim();

	var emailCC = $("#correo_electronico_nuevo_mult").val().toLowerCase().trim();
	if (uuid=="") return;

	if (emailCC == ""){
	    messageDanger("#correo_electronico_nuevo_mult_validation", campos_vacios_message);
	    addDanger("correo_electronico_nuevo_mult");
	    return;
	}

	if (!validateEmail(emailCC)){
	    messageDanger("#correo_electronico_nuevo_mult_validation", mensage_mail_invalido);
	    addDanger("correo_electronico_nuevo_mult");
	    return;
	}

	var archivos = nombresArchivosList();
	
	if (archivos=="" || archivos.indexOf(",") == -1){
		Swal.fire(
				  'Facturación Sodimac',
				  'Favor de seleccionar al menos dos documentos',
				  'warning'
				)
		return;
	}
	
	$('#enviarUserDateMultiple').prop("disabled", true);
	var url = '/facturacion/ReenvioFacturaMultiple';

	$.ajax({
		url: url,
		data: {rfc:rfc, id:archivos, eMailCC: emailCC},
		type: "get", async: true, cache: false, crossDomain: false,
		success: function(data){

			if (data == "success"){
				$("#modalReenvioMultiple").modal("hide");
				$('.modal-backdrop').remove();
	    		Swal.fire(
	    				  'Facturación Sodimac',
	    				  'Se han reenviado los documentos seleccionados',
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

function eventosReenvioMultiple(){
	releaseEventEmpty("correo_electronico_nuevo_mult");
	releaseEventEmpty("correo_electronico_anterior_mult");
	
    releaseEventCopiPaste("#correo_electronico_anterior_mult", "paste", "#correo_electronico_anterior_mult_validation", "El sistema no permite pegar, ¡Gracias!");
    releaseEventCopiPaste("#correo_electronico_anterior_mult", "copy", "#correo_electronico_anterior_mult_validation", "El sistema no permite copiar, ¡Gracias!");
    releaseEventCopiPaste("#correo_electronico_anterior_mult", "dragover", "#correo_electronico_anterior_mult_validation", "El sistema no permite drag & drop, ¡Gracias!");
    releaseEventCopiPaste("#correo_electronico_nuevo_mult", "paste", "#correo_electronico_nuevo_mult_validation", "El sistema no permite pegar, ¡Gracias!");
    releaseEventCopiPaste("#correo_electronico_nuevo_mult", "copy", "#correo_electronico_nuevo_mult_validation", "El sistema no permite copiar, ¡Gracias!");
    releaseEventCopiPaste("#correo_electronico_nuevo_mult", "dragover", "#correo_electronico_nuevo_mult_validation", "El sistema no permite drag & drop, ¡Gracias!");
}

function nombresArchivosList() {
	var nombreList="";
	
	dTotClientesMultipleObj.filter(function (_dClientesMultipleObj) {
		if (_dClientesMultipleObj.checked) {
			nombreList += (nombreList=="" ? _dClientesMultipleObj.nombreArchivo : "," + _dClientesMultipleObj.nombreArchivo);
		}
	});
	
	return nombreList;
}

function releaseEventtoken_multiples_facturaEmpty() {
	
    var valor = $("#token_multiples_factura").val().trim();
    if (valor == "") {
        messageDanger("#token_multiples_factura_validate", token_vacio);
        addDanger("token_multiples_factura");
        return false;    	
    }
        
    addSuccess("token_multiples_factura");
    return true;

}

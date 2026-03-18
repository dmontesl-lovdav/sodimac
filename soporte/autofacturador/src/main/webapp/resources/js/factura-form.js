$("#rfcInput").focus();

const ID_APLICACION_AUTOFACTURADOR = 1;
const ID_APLICACION_AUTOFACTURADOR_VVEE = 11;
const CFDI_40 = "4.0";

var url_string = window.location.href;
var url = new URL(url_string);
var vdescargar = url.searchParams.get("7g8dqd89h");
var numeroTicketsPermitidos = 1;
var blnNotaCredito = false;
var blnDescargar = false;
var gVersionCFDI = "4.0";
var gVVEE = "NO";
 var gDatosCfdiNC = null;

if (vdescargar=="vvee") {
	blnDescargar = true;
	gVVEE = "SI";
	numeroTicketsPermitidos = 1;
	document.getElementById("divBotonAgregar").innerHTML = '<button id="btnAgregar" onclick="verificaAmountTicket(event,  \'ticketHolder\', \'messajeTicketCompra\', \'1\')" class="btn btn-primary">Agregar Ticket</button>';
	document.getElementById("divMaximoTickets").innerHTML = '<span class="title-1 text-center" style="font-size:0.9rem">(Máximo 5 tickets)</span>';
	
	/*var cfdiClient = versionCFDI(ID_APLICACION_AUTOFACTURADOR_VVEE);
    if (cfdiClient.versionCFDIEstatus == "1") {
		gVersionCFDI = cfdiClient.versionCFDI;
	}*/
	
} else {
	document.getElementById("divBotonAgregar").innerHTML = '' +
	'<ul class="pager wizard pager_a_cursor_pointer">' +
    '	<li class="next" id="invalidar2">' +
    '    	<a onclick="" class="btn btn-primary" id="btnAgregar" style="float:left;" tabindex="4">' +
    '   		Siguiente' +
    '       	</a>' +
    '       </li>' +
    '</ul>'
    
    $("#btnFirstNext").addClass("btn-Agregavisible");

	var url = '/facturacion/getIsDescargar';
    $.ajax({
    	url: url,
    	type: "post", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		if (data == "true") {
    			blnDescargar = true;
    		}
    	}
    });
    
    /*var cfdiClient = versionCFDI(ID_APLICACION_AUTOFACTURADOR);
    if (cfdiClient.versionCFDIEstatus == "1") {
		gVersionCFDI = cfdiClient.versionCFDI;
	}*/
}
$("#divMensajeVersion40").hide();
$("#divMensajeNC33").hide();

$(function() {

	eventos();
	
    $(".purchase").hide();
    $(".link-ticket, .link-purchase").bind("click", function() {
        $(".ticket, .purchase").hide();
        if ($(this).attr("class") == "link-ticket") {
            $(".ticket").fadeIn(250);
        } else {
            $(".purchase").fadeIn(250);
        }
    });
    //////////
    $(".purchase").hide();
    $(".ticket-control-prev, .ticket-control-next").bind("click", function() {
        $(".ticket, .purchase").hide();
        if ($(this).attr("class") == "ticket-control-prev") {
            $(".ticket").fadeIn(250);
        } else {
            $(".purchase").fadeIn(250);
        }
    });    
    
});

$('#ticketAmount').maskMoney();

var dTotClientesListObj = [];

function clearListObj() {
	dTotClientesListObj = [];
}

function createClientesList (uuid, nombreArchivo) {

	if (uuid != "") {
		dTotClientesListObj.push({
			uuid: uuid, 
			checked: false,
			nombreArchivo: nombreArchivo
			});
	}
	
}

function SetCheckUncheck(uuid){

	if (uuid == "All") {
		var selectAll = $('#selectall').prop('checked');
		dTotClientesListObj.filter(function (_dClientesListObj) {
			_dClientesListObj.checked = selectAll ? true : false;
		});

	} else {
		dTotClientesListObj.filter(function (_dClientesListObj) {
			if (_dClientesListObj.uuid === uuid) {
				_dClientesListObj.checked = !_dClientesListObj.checked;
			}
		});

	}

	var selectedCount = dTotClientesListObj.reduce(function (accumulator, _dTotClientesListObj) {
		if (_dTotClientesListObj.checked) {
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


function nombresArchivosList() {
	var nombreList="";
	
	dTotClientesListObj.filter(function (_dClientesListObj) {
		if (_dClientesListObj.checked) {
			nombreList += (nombreList=="" ? _dClientesListObj.nombreArchivo : "," + _dClientesListObj.nombreArchivo);
		}
	});
	
	return nombreList;
}

function obtenerCorreoMultiple(){

	var archivos = nombresArchivosList();

	if (archivos==""){
		Swal.fire(
				  'Facturación Sodimac',
				  'Favor de seleccionar al menos un documento',
				  'warning'
				)
		return;
	}

	var file = archivos.split(",");
	var uuid = file[0].substring(17, 53)
		
	$('#enviarUserDateMultiple').prop("disabled", false);

    var url = '/facturacion/ObtenerCorreo';

    $.ajax({
    	url: url,
    	data: {uuid:uuid},
    	type: "get", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		var data = data.replace(/\?/g, '•');
    		$("#correo_electronico_anterior_mult").val(data);
    		addSuccess("correo_electronico_anterior_mult");
    		eventosReenvioMultiple();
    		$("#modalReenvioMultiple").modal();
    	}
    });	
}

function eventosReenvioMultiple(){
	releaseEventEmpty("correo_electronico_nuevo_mult");
	
    releaseEventCopiPaste("#correo_electronico_anterior_mult", "paste", "#correo_electronico_anterior_mult_validation", "El sistema no permite pegar, ¡Gracias!");
    releaseEventCopiPaste("#correo_electronico_anterior_mult", "copy", "#correo_electronico_anterior_mult_validation", "El sistema no permite copiar, ¡Gracias!");
    releaseEventCopiPaste("#correo_electronico_anterior_mult", "dragover", "#correo_electronico_anterior_mult_validation", "El sistema no permite drag & drop, ¡Gracias!");
    releaseEventCopiPaste("#correo_electronico_nuevo_mult", "paste", "#correo_electronico_nuevo_mult_validation", "El sistema no permite pegar, ¡Gracias!");
    releaseEventCopiPaste("#correo_electronico_nuevo_mult", "copy", "#correo_electronico_nuevo_mult_validation", "El sistema no permite copiar, ¡Gracias!");
    releaseEventCopiPaste("#correo_electronico_nuevo_mult", "dragover", "#correo_electronico_nuevo_mult_validation", "El sistema no permite drag & drop, ¡Gracias!");
}

function reenviarFacturaMultiple(){

	var rfc = $("#rfcInput").val().toUpperCase().trim();

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
	
	if (archivos==""){
		Swal.fire(
				  'Facturación Sodimac',
				  'Favor de seleccionar al menos un documento',
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

function showTerminos() {				
	var htmlStr = '<div class="container" style="width: 95%; height: 600px; overflow-y: scroll;">' +
	'  <div class="row">' +
	'    <div class="col-xs-12 col-ms-12 col-md-12 col-lg-12 no-pad" id="space">' +
	'      <div class="service-content">' +
	'        <div class="col-xs-12">' +
	'          <div class="sector1 mt40 text-justify">' +
	'            <br>' +
	'            <h4>Condiciones Generales </h4>' +
	'            <br>' +
	'            <h4>General</h4>' +
	'            <br>' +
	'            <p>Bienvenido a <b>Comercializadora SDMHC S.A. de C.V.,</b> Estos Condiciones Generales  regulan el acceso en México a nuestro Portal <a href="https://www.sodimac.com.mx/sodimac-mx/" target="_blank">www.sodimac.com.mx </a>y su uso por todo usuario o consumidor. En este Portal  podrás usar, sin costo alguno, nuestro software y nuestras aplicaciones para equipos móviles para visitar, comparar y adquirir, si lo deseas los productos y servicios que se exhiben aquí. </p>' +
	'            <br>' +
	'            <p> Recomendamos a todos nuestros Clientes leer atentamente estos Condiciones Generales . Estos se aplicarán y se entenderán incorporados en todas las compras y a todos los servicios que contrates con <b>Comercializadora SDMHC S.A. de C.V.</b> (en adelante "SODIMAC" o "SODIMAC.com"), mediante los sistemas de comercialización comprendidos en este sitio web. Podrás también acceder, en este mismo Portal , a los hipervínculos <a href="https://secure.sodimac.com.mx/sodimac-mx/myaccount/login" target="_blank">"Mi Cuenta"</a>, "Servicio al Cliente", "Más SODIMAC.com" y otros, cuyo objeto es facilitarte el acceso e incrementar los beneficios de uso del Portal , mismos que pueden variar periódicamente. Considera que al acceder a la página web de SODIMAC y realizar tu compra, se entenderá que expresamente has leído, entendido y por lo tanto aceptas los mismos en su totalidad, en caso contrario, te pedimos abstenerte de entrar, registrarte o comprar en el sitio web, y eliminar toda información que hubiera quedado almacenada en él. Si el usuario o consumidor decide continuar con el uso de <a href="https://www.sodimac.com.mx/sodimac-mx/" target="_blank">www.sodimac.com.mx </a>, dicha acción se considerará como su absoluta aceptación a los Condiciones Generales  aquí establecidos. </p>' +
	'            <br>' +
	'            <p> <b>Comercializadora SDMHC S.A. DE C.V.</b> (SODIMAC), quien provee el sitio web de compras online <a href="https://www.sodimac.com.mx/sodimac-mx/" target="_blank">www.sodimac.com.mx </a> es una empresa debidamente acreditada por las leyes mexicanas su domicilio Fiscal es el ubicado en Av. Adolfo López Mateos No. 201, Col. Santa Cruz Acatlán, CP 53150, Naucalpan de Juárez, Estado de México, con número telefónico en la ciudad de México: 01 800 0625 222 y se cuenta con R.F.C CSD161207R2A. </p>' +
	'            <br>' +
	'            <p> El Consumidor (Cliente), acepta que sus datos personales que consten en la Orden de Compra en cada caso serán los datos que lo acrediten como Cliente. </p>' +
	'            <br>' +
	'            <br>' +
	'            <h4>Objeto</h4>' +
	'            <br>' +
	'            <ul class="ul-deci">' +
	'              <li>El objeto del presente contrato es la compraventa de los bienes que el Cliente seleccione en la orden de compra que se genera en el portal, a los precios y cargos que por la forma de entrega elegida apliquen y de acuerdo a la forma de pago que él elija de las que se establecen en el inciso b) de la cláusula séptima de las Condiciones Generales. </li>' +
	'              <li>Cuando así sea optado por el Cliente, SODIMAC entregará a su riesgo, los bienes objeto del presente contrato en el domicilio indicado por el Cliente en la caratula, con los cargos que procedan por concepto de entrega a domicilio, mismos que les serán comunicados. </li>' +
	'              <li>Los bienes objeto del presente contrato tendrán la garantía propia que otorguen sus fabricantes conforme a la vigencia que a cada bien corresponda, contada a partir de su entrega, que podrá ser exigible indistintamente en las instalaciones de SODIMAC a ésta, siempre y cuando no haya transcurrido un plazo mayor a 30 días contados a partir de la compra, o en las instalaciones del fabricante directamente a él conforme a los términos establecidos por los artículos 82, 92 y 93 de la Ley Federal de Protección al Consumidor, obligándose invariablemente el Cliente a presentar el comprobante de compra respectivo para hacer válida dicha garantía.</li>' +
	'              <li>El Cliente se obliga a recoger los bienes objeto de la contratación en el domicilio de SODIMAC, de haberlo señalado así, en cuyo caso exime a SODIMAC de responsabilidad en caso de que sufrieran pérdida o deterioro durante el trayecto. </li>' +
	'              <li>Los contratantes están de acuerdo en que el incumplimiento de cualquiera de las obligaciones contenidas en el presente contrato originará a cargo del responsable una pena convencional equivalente al quince por ciento del importe total de la operación. </li>' +
	'              <li>SODIMAC expedirá en cada compra el comprobante, recibo o factura correspondiente dentro de los tres días siguientes al pago detallando los conceptos e importes retribuidos. </li>' +
	'              <li>Los contratantes declaran conocer el alcance y fuerza legal de todas y cada una de las cláusulas de este contrato, sometiéndose en primera instancia a la jurisdicción y competencia de la Procuraduría Federal del Consumidor, y en caso de subsistir la misma, ante los tribunales judiciales del Estado de México. </li>' +
	'              <li>Previa obtención de la pre-autorización del verificador bancario correspondiente respecto de la transacción específica y la obtención de la autorización definitiva en el punto de venta, el Cliente podrá adquirir productos de los que expenda SODIMAC utilizando como forma de pago cualquiera de las siguientes: ' +
	'                <span class="espacios"> o	Tarjeta de Crédito Visa o MasterCard; o	Tarjeta de Débito Visa; o Tarjeta de Puntos SODIMAC, siempre y cuando hubieren sido expedidas en el territorio mexicano. </span></li>' +
	'            </ul>' +
	'            <br>' +
	'            <br>' +
	'            <h4>Condiciones generales para el uso de la sección compras en línea</h4>' +
	'            <br>' +
	'            <h4>Definiciones</h4>' +
	'            <br>' +
	'            <br>' +
	'            <ul class="ul-deci">' +
	'              <li><b>SODIMAC</b>: como concepto de negocio, es un portal dedicado al comercio electrónico del denominado "empresa-consumidor" (business to consumer). COMERCIALIZADORA SDMHC S.A. DE C.V., como empresa, es una sociedad constituida bajo las leyes mexicanas dedicada a realizar transacciones electrónicas entre vendedores, compradores y consumidores. </li>' +
	'              <li><b>"Portal"</b>: Página electrónica o Portal de Internet llamada SODIMAC (<a href="https://www.sodimac.com.mx/sodimac-mx/" target="_blank">www.sodimac.com.mx</a>) administrado por SODIMAC, cuya función principal es poner a disposición del público artículos de consumo. </li>' +
	'              <li><b>"Cliente"</b>: Persona física o moral registrada o visitante del Portal de SODIMAC, la cual podrá actuar como comprador o consumidor para realizar operaciones comerciales o simplemente para realizar una búsqueda a través de las herramientas proporcionadas por SODIMAC. </li>' +
	'              <li>"Condiciones Generales": Las Condiciones Generales establecidas en este documento que rigen el uso del Portal, las cuales obligan a SODIMAC y a los Clientes. Estas Condiciones Generales establecen el marco jurídico mediante el cual se desarrollarán las relaciones comerciales y contractuales con los Clientes. </li>' +
	'            </ul>' +
	'            <br>' +
	'            <br>' +
	'            <h4>Declaraciones del cliente</h4>' +
	'            <br>' +
	'            <br>' +
	'            <p> El Cliente declara bajo protesta de decir verdad que: </p>' +
	'            <br>' +
	'            <ul class="ul-deci">' +
	'              <li>Tiene plena capacidad jurídica y aptitud comercial para negociar, contratar y obligarse en los términos que su relación jurídico-comercial con SODIMAC así lo amerite.</li>' +
	'              <li>Reconoce que conforme al Código Civil Federal, en su artículo 1796, los contratos obligan "no sólo al cumplimiento de lo expresamente pactado, sino también a las consecuencias que, según su naturaleza, son conforme a la buena fe, al uso o a la ley. </li>' +
	'              <li>Reconoce que por virtud de las reformas al Código Civil Federal, Código Federal de Procedimientos Civiles y al Código de Comercio, publicadas el 29 de Mayo de 2000 en el Diario Oficial de la Federación Mexicano, orientadas a regular el comercio electrónico, es legalmente válido contratar y manifestar el consentimiento por cualquier medio electrónico, y que ante cualquier tribunal o autoridad judicial será válido como medio probatorio cualquier documento o archivo electrónico. </li>' +
	'              <li>La utilización del Portal SODIMAC atribuye la condición de Cliente de SODIMAC y expresa la adhesión plena y sin reservas del Cliente a todas y cada una de las Condiciones Generales en la versión publicada por SODIMAC en el momento mismo en que el Cliente acceda al Portal. En consecuencia, el Cliente debe leer atentamente las Condiciones en cada una de las ocasiones en que se proponga utilizar el Portal.</li>' +
	'              <li>El Cliente conoce que el acceso y/o utilización de ciertos servicios y contenidos ofrecidos a los Clientes y/o a través del Portal se encuentra sometida a ciertas condiciones particulares propias que, complementan con la aceptación del Cliente las Condiciones Generales (en adelante, las "Condiciones particulares"). Con anterioridad al acceso y/o la utilización de dichos servicios y contenidos, por tanto, el Cliente ha de leer atentamente también las correspondientes Condiciones Particulares.</li>' +
	'            </ul>' +
	'            <br>' +
	'            <br>' +
	'            <h4>Actitud comercial y capacidad jurídica</h4>' +
	'            <br>' +
	'            <br>' +
	'            <ul class="ul-deci">' +
	'              <li>Los servicios ofrecidos por SODIMAC son dirigidos sólo a personas que son legalmente capaces para contratar y tienen la aptitud comercial suficiente para entrar en cualquier clase de negocios que sean lícitos. De conformidad con lo anterior, los servicios no se dirigen a, y no puede usarse por, entre otros, menores de edad, personas físicas o morales quebradas, insolventes, sujetas a concurso, suspensión de pagos o régimen similar, y en general cualquier persona o entidad no apta para ejercer actos de comercio. </li>' +
	'              <li>SODIMAC vende productos para niños, los cuales solo pueden ser comprados por mayores de 18 años (adultos), mediante cualquiera de los medios de pago electrónicos disponibles en el Portal. Los menores de edad pueden usar este Portal a través de la supervisión sus padres o de un representante legal. Se debe tener 18 años para usar cualquiera de las funcionalidades del Portal relacionadas con materiales peligrosos, productos flamables, etc. </li>' +
	'              <li>Igualmente, los servicios no pueden usarse por individuos que pierdan su capacidad de Cliente, ya sea porque el Cliente ha dado por terminado su registro de Cliente, o porque SODIMAC haya suspendido o terminado (indefinida o temporalmente) la calidad de Cliente así como su acceso a los servicios ofrecidos en el Portal. </li>' +
	'            </ul>' +
	'            <br>' +
	'            <br>' +
	'            <h4>Objeto</h4>' +
	'            <br>' +
	'            <br>' +
	'            <p> SODIMAC permitirá a los clientes a través del portal de <a href="https://www.sodimac.com.mx/sodimac-mx/" target="_blank">www.sodimac.com.mx</a>, el acceso y la utilización de diversos servicios y contenidos puestos a su disposición por SODIMAC o por terceros clientes del portal y/o terceros proveedores de servicios y contenidos (en adelante, los "Servicios").SODIMAC podrá en cualquier momento y sin aviso previo modificar o eliminar unilateralmente (i) la presentación y configuración del Portal de SODIMAC y/o (ii), los Servicios o las condiciones requeridas para acceder y/o utilizar el Portal de SODIMAC. </p>' +
	'            <br>' +
	'            <br>' +
	'            <h4>Del registro y cuenta del cliente</h4>' +
	'            <br>' +
	'            <ul class="ul-deci">' +
	'              <li>Si el Cliente desea contratar, solicitar información, acceder a promociones especiales o efectuar pagos en este Portal, podrá registrar una contraseña o clave secreta, para lo cual, el Cliente deberá completar el formulario de registro que se encuentra en el Portal. Esta contraseña no es requisito para contratar en este Portal, pero permite un acceso personalizado, confidencial y seguro. El Cliente es el responsable de mantener la confidencialidad de su clave secreta registrada en este Portal y de restringir el acceso a su computador o equipo móvil o cualquier otro medio de acceso. Si el Cliente así lo desea, podrá cambiar su clave siguiendo el procedimiento establecido en el Portal. La sola visita de este Portal no te impone obligación alguna, a menos que se haya expresado en forma inequívoca y mediante actos positivos la voluntad de adquirir determinados bienes o servicios, en la forma indicada en estas Condiciones Generales. </li>' +
	'              <li>Para que puedan suministrarse los Productos y Servicios, el Cliente debe llenar la solicitud de registro, proporcionando y manteniendo actualizada la información veraz, exacta y completa que pida el sistema (por ejemplo, domicilio citando el nombre de la calle, número, colonia, ciudad, Estado y código postal (la "Información "). Los Productos y Servicios sólo serán suministrables en la cobertura que SODIMAC tenga disponible. En ningún caso SODIMAC será responsable por daños y perjuicios por Información no actualizada.</li>' +
	'              <li>El Cliente tecleará su dirección de correo electrónico que será su "Cuenta Personal" y elegirá a su libre discreción una clave para que SODIMAC lo identifique como "Cliente" ("Clave de Acceso"). </li>' +
	'              <li>SODIMAC se reserva la facultad para, en cualquier tiempo posterior, verificar, validar o volver a comprobar por cualquier medio, la información enviada por el Cliente, y determinar a su sola discreción si el Cliente es elegible para participar en el Portal o bien, darlo de baja o cancelar su acceso al Portal en caso de encontrar información falsa, inexacta o no actualizada. </li>' +
	'              <li>Para llevar a cabo transacciones electrónicas y tener acceso a zonas privadas del Portal, el Cliente deberá: (i) tener membresía vigente de SODIMAC y (ii) acceder a su Cuenta Personal tecleando su dirección de correo electrónico y Clave de Acceso en la página de SODIMAC.</li>' +
	'              <li>Considerando el aviso de privacidad que implementará SODIMAC, cada Cliente será responsable de todas las actividades y cargos en relación con el uso de la Cuenta Personal por cualquier persona o entidad que tenga acceso a dicha cuenta, y será responsable también de asegurarse del cumplimiento de todas estas Condiciones Generales por parte de las personas que tengan acceso a su Cuenta Personal. La Cuenta Personal con SODIMAC no puede transferirse sin la previa aprobación por escrito de SODIMAC y estará sujeta a cualquier límite y/o restricciones en su uso que SODIMAC, de tiempo en tiempo podrá establecer. </li>' +
	'              <li>El Cliente tomará todas las precauciones razonables para asegurar la confidencialidad de la Cuenta Personal y Clave de Acceso que el mismo asignará, y sólo él será responsable por todos los costos, gastos, daños y perjuicios, obligaciones, multas u otros daños económicos, incluyendo los honorarios razonables de abogado y los gastos derivados de cualquier litigio, arbitraje o apelación que proceda, que sea resultado de revelar, o permitir la revelación de cualquier Cuenta Personal o Clave de Acceso. En caso de cualquier revelación u otra violación de seguridad, el Cliente será responsable de cualquier uso no autorizado del Portal hasta que SODIMAC reciba el aviso por escrito de tal situación, de conformidad a los canales referidos en el aviso de privacidad. </li>' +
	'              <li>SODIMAC tratará la "Información del Registro" de manera automatizada y confidencial, no obstante ante el evento de existir algún requerimiento judicial, podrá ser transmitida, registrada o dado de alta ante la autoridad competente, con las finalidades que en cada caso corresponda. Todas estas circunstancias serán previa y debidamente advertidas por SODIMAC a los Clientes, en los casos y en la forma en que ello resulta legalmente exigible. El Cliente reconoce que, al proporcionar la información de carácter personal requerida, otorga a SODIMAC la autorización a que hace mención el Artículo 109 de la Ley Federal del Derechos de Autor publicada el 24 de diciembre de 1996 en el Diario Oficial de la Federación de los Estados Unidos Mexicanos. La prestación del servicio de Portal por parte de SODIMAC exige el registro del Cliente para tener acceso a la compra de mercancías a través de internet o vía telefónica, con apego a lo siguiente: </li>' +
	'              <li>o	El Cliente deberá actualizar en el apartado correspondiente del portal de SODIMAC cualquier cambio que sufra la información ingresada al momento de su registro, dentro de un plazo que no excederá de los diez días naturales siguientes a que haya tenido lugar dicho cambio.' +
	'				  </li>' +
	'               <li>o	El Cliente se compromete a: (a) utilizar el Portal y los Servicios conforme con las leyes o reglamentos aplicables (estatales, federales, locales o internacionales), con lo dispuesto en estas Condiciones Generales, la moral u las buenas costumbres generalmente aceptadas y el orden público, (b) No violar ni afectar en ninguna forma derechos de terceros (enunciativa más no limitativamente: derechos de consumidor, derechos de propiedad intelectual, derechos de autor, marcas o patentes; derechos civiles o comerciales; derechos de privacidad y confidencialidad); afectar la reputación, honor o buen nombre comercial o personal de los otros Clientes, (c) abstenerse de utilizar el Portal y los servicios con fines o efectos ilícitos, contrarios a lo establecido en las presente Condiciones Generales, lesivos de los derechos e intereses de terceros o que de cualquier forma puedan dañar, inutilizar, sobrecargar o deteriorar el Portal y los Servicios o impedir la normal utilización o disfrute del Portal y de los servicios por parte de otros Clientes.</li>' +
	'            </ul>' +
	'            <br>' +
	'            <br>' +
	'            <h4>Políticas de seguridad </h4>' +
	'            <br>' +
	'            <br>' +
	'            <p> SODIMAC adoptará las medidas necesarias y prudentes para resguardar la seguridad de los datos y de la contraseña del Portal. En caso de detectarse cambios en la información que has registrado en el Portal, o bien, ante cualquier irregularidad en las transacciones, o simplemente como medida de protección de la identidad de nuestros Clientes, nuestros ejecutivos podrán contactarlos vía telefónica a fin de corroborar tus datos e intentar evitar posibles fraudes. En caso de no poder establecer el contacto en un plazo de 48 horas, por tu propia seguridad, tu orden de compra efectuada en nuestro Portal no podrá ser confirmada. Los comprobantes de las gestiones realizadas para contactarte y poder confirmar la operación estarán disponibles en nuestras oficinas durante 30 días naturales. </p>' +
	'            <br>' +
	'            <br>' +
	'            <h4>Alcance de los precios informados en este Portal</h4>' +
	'            <br>' +
	'            <br>' +
	'            <p> SODIMAC no modificará las condiciones bajo las cuales haya contratado con los clientes en este Portal. Mientras aparezcan en este Portal, los precios informados estarán a la disposición de los clientes, aunque no sean los mismos que se ofrezcan en otros canales de venta de SODIMAC, como tiendas físicas, catálogos, televisión, radio, u otros. Con todo, los precios son aplicables para la ciudad de entrega o de despacho. Si una vez ingresado un producto al carrito de compras, el cliente cambia la dirección de entrega o de despacho que previamente registró, (a una ciudad diferente), cambiará el precio del producto al precio de la ciudad con la tienda SODIMAC más cercana. </p>' +
	'            <br>' +
	'            <br>' +
	'            <h4>Promociones</h4>' +
	'            <br>' +
	'            <br>' +
	'            <p> Las promociones que se ofrezcan en este Portal no serán necesariamente las mismas que ofrezcan otros canales de venta de SODIMAC. En las promociones que consistan en la entrega gratuita o con precio rebajado de un producto por la compra de otro, el despacho del bien que se entregue gratuitamente o a precio rebajado, se hará en el mismo lugar al cual se despacha el producto comprado, salvo que el cliente solicite, al aceptar la oferta, que los productos se remitan a direcciones distintas, en cuyo caso deberá pagar el valor del despacho de ambos productos. No se podrá participar en estas promociones sin adquirir conjuntamente todos los productos comprendidos en ellas. Es responsabilidad del Cliente revisar los Condiciones Particulares aplicables a cada promoción que ofrezca SODIMAC, ya sea mediante la página web, en tiendas SODIMAC, o a través de atención a clientes. </p>' +
	'            <br>' +
	'            <br>' +
	'            <h4>Compras</h4>' +
	'            <br>' +
	'            <br>' +
	'            <p> Los Clientes podrán adquirir productos de los que comercialice SODIMAC conforme a lo siguiente: </p>' +
	'            <br>' +
	'            <ul class="ul-deci"> ' +
	'				<li>Límites de Compra: Para ser atendible un servicio de entrega a domicilio deberá encontrarse el pedido dentro de los rangos de compra establecidos en el Portal.</li>' +
	'              <li>Formas de pago: Previa obtención de la pre-autorización del verificador bancario correspondiente respecto de la transacción específica y la obtención de la autorización definitiva en el punto de venta, el Cliente podrá adquirir productos de los que expenda SODIMAC utilizando como forma de pago cualquiera de las siguientes: (1) Tarjeta de Crédito Visa o MasterCard; (2) Tarjeta de Débito; o (3) Tarjeta de puntos siempre y cuando hubieren sido expedidas en el territorio mexicano. El pago con tarjetas de débito se realizará a través de WebPay, sistema de pago electrónico que se encarga de hacer el cargo automático a la cuenta bancaria del usuario. El uso de estas tarjetas se sujetará a lo establecido en estos Condiciones Generales y, en relación con su emisor, a lo pactado en los respectivos Contratos de Apertura y Reglamento de Uso, que predominarán en caso de haber contradicción. Todos los aspectos relativos al uso de estas tarjetas bancarias, como su fecha de emisión, caducidad, cupo, bloqueos, etc., se sujetarán, en relación con su emisor, a lo pactado en los respectivos Contratos de Apertura y Reglamento de Uso. De haber contradicción, predominará lo expresado en ese último instrumento. Si por cualquier motivo se dejare sin efecto una transacción cuyo pago se hubiera realizado con alguno de estos medios, entonces los efectos de la devolución del precio se regirán por las reglas que al efecto el cliente haya pactado con su banco y a las instrucciones que establezca la CNBV. En el Portal  se podrá hacer ofertas especiales asociadas a uno o más medios de pago. </li>' +
	'              <li>Reclamaciones, Devoluciones, Reembolsos y Cambios: En caso de que el artículo entregado presente defectos de fabricación o no sea el artículo solicitado, el Cliente podrá efectuar la reclamación correspondiente en las tiendas SODIMAC participantes dentro de la República Mexicana y en el horario de servicio de la tienda, regresarlo siempre y cuando se encuentre en su empaque original con todos sus contenidos y su, ticket de compra ó Factura conforme a lo siguiente: (i) dentro de los 30 (treinta) días siguientes a la fecha en que lo recibió; en Línea Blanca aplica hasta 7 días, en Computo y Electrónica aplica de 7 hasta 15 días (ii) SODIMAC cambiará el producto o reembolsará el importe mediante nota de crédito depositando el monto correspondiente de la compra en dinero electrónico directamente en su membresía. Lo anterior no aplica en productos perecederos vinos, licores, ropa interior, cartuchos y toners para impresora, cintas y rellenadores (o consumibles similares), así como en muebles (armados por clientes), Cd´s, libros, joyería, recargas de Tiempo Aire. </li>' +
	'              <li>Políticas de entrega Para cualquier entrega de pedidos será necesaria la previa presentación de la identificación del Cliente o su representante y sólo aplicarán entregas a domicilios que se encuentren dentro de la cobertura vigente en el Portal de SODIMAC con liquidación simultánea del cargo por el servicio y el precio de la mercancía adquirida.</li> </ul>' +
	'            <br>' +
	'            <br>' +
	'            <h4>Despacho de los productos</h4>' +
	'            <br>' +
	'            <br>' +
	'            <p> El Cliente podrá elegir las condiciones de despacho y entrega de los productos adquiridos de entre las que se encuentren informadas en el Portal. Al indicar los datos de la orden de despacho, se recomienda revisar los datos de la compra, como son: nombre, dirección, teléfonos de contacto. Esta información es fundamental para una correcta y oportuna entrega de los productos en el domicilio o lugar indicado para la entrega. <br>' +
	'              <br>' +
	'              SODIMAC atenderá todas las consultas acerca de la orden en la línea 600 600 40 20 opción 2; o ingresar una solicitud en la opción Contáctenos vía mail disponible en este Portal  web. Se recomienda tener a la mano los comprobantes de la compra (ticket, factura, orden de compra internet o venta telefónica). Se recomienda que previamente a la compra, verificar las dimensiones del producto y las dimensiones físicas del lugar donde se realizará la instalación, lo anterior para evitar problemas al momento de la entrega. Los productos podrán ser entregados de lunes a sábado entre 9:00 y 21:00 horas, y de acuerdo a las condiciones pactadas al momento de la compra. El Cliente deberá verificar que el producto corresponda a lo que se adquirió y que se encuentra en perfectas condiciones, antes de firmar la aceptación de entrega. El despacho no considera el armado o instalación de productos, ni el uso de cualquier equipo o material para levantar o ingresar los productos a pisos superiores. El personal de transporte no está autorizado ni capacitado para instalar, armar, intervenir o alterar los productos en el domicilio. El producto debe ser recibido por un mayor de edad, quien deberá firmar de recibido en la guía de despacho para acreditar la recepción. <br>' +
	'              <br>' +
	'              En caso que el Cliente no esté conforme con el producto al momento de la entrega, éste deberá ser rechazado y registrado, anotando en la guía de despacho o documento de entrega el detalle identificado, junto con el nombre y firma de quien recibe. Exige y conserva la copia de la guía de despacho. En este caso el transportista se llevará el producto para realizar el respectivo cambio. Comunícate con nosotros al 600 600 40 20, opción 2, con la finalidad de respaldar esta información, o ingresa tu solicitud en opción Contáctenos vía mail disponible en este Portal. Si lo prefieres, escríbenos a <a href="mailto:Atencioncc@sodimac.com.mx">Atencioncc@sodimac.com.mx</a>. <br>' +
	'              <br>' +
	'              En caso de no contar con existencias del producto adquirido, procede el Reembolso Total del valor pagado por el producto excluyendo los gastos de envío en caso de haberse incurrido. </p>' +
	'            <br>' +
	'            <br>' +
	'            <h4>Condiciones de la entrega</h4>' +
	'            <br>' +
	'            <br>' +
	'            <p> El Cliente deberá garantizar que el lugar físico donde se realiza la entrega del despacho cuenta con las condiciones mínimas para el acceso del medio de transporte, esto es, disponibilidad de estacionamiento y horario de acceso. El despacho de productos en pisos superiores se sujeta a la condición de que sea posible desplazar los productos con embalaje en forma razonablemente segura (por ejemplo: caja escala adecuada para maniobrar según las dimensiones del producto, disponibilidad de ascensores para carga o con las dimensiones adecuadas para el traslado del producto). Asimismo, los productos que superen los 250 Kg. (total de la orden de compra o reserva), se entregan solo en el primer piso. Si el Cliente eligió la modalidad "Compra Online Retira en Tienda", el producto adquirido a través de este Portal debe ser retirado en la tienda que se haya seleccionado, dentro de 5 días naturales siguientes, en horario de tienda, desde que se le informe al Cliente por e-mail que su producto está disponible. Si el producto no fuera retirado dentro del plazo de 5 días naturales, SODIMAC procederá a dejar sin efecto la compra el Cliente y le será abonado al Cliente el precio pagado, en el mismo medio de pago usado para la compra. </p>' +
	'            <br>' +
	'            <br>' +
	'            <h4>Como adquirir servicios en este Portal</h4>' +
	'            <br>' +
	'            <br>' +
	'            <p> Para contratar cualquiera de los servicios complementarios y/o adicionales que se informan en este Portal, el Cliente deberá seguir los siguientes pasos: </p>' +
	'            <br>' +
	'            <ul class="ul-deci">' +
	'			   <li>Seleccionar el servicio de su interés, de los que estén disponibles en el domicilio donde se desee contratar los servicios online, y hacer click en el campo "contratar". Para consulta de disponibilidad de servicios por zona, verificar en www.sodimac.cl/servicios/cobertura. </li>' +
	'              <li>Ingresar la dirección de e-mail y la clave. Si no se cuenta con registro, ingresar al link "regístrate". </li>' +
	'              <li>Seleccionar el domicilio donde se debe ejecutar el servicio, y uno de los medios de pago disponibles en el Portal. </li>' +
	'              <li>Una vez colocada la orden de servicio, se desplegará en la pantalla una descripción del servicio, su precio (según la ciudad en que se deba prestar el servicio), el medio de pago, el valor total de la operación y las demás condiciones de la orden, mismas que podrán imprimirse y almacenarse; así como un número único de la orden, con el que se podrá hacer el seguimiento de la misma. </li>' +
	'              <li>La orden pasará automáticamente a un proceso de confirmación de identidad, así como de la vigencia, disponibilidad y cupo del medio de pago. 6. Verificada la información anterior exitosamente, se efectuará el cargo en el medio de pago, y se enviará el comprobante de compra con el respectivo ticket o factura, en formato electrónico, al correo registrado del Cliente.</li>' +
	'			</ul>' +
	'            <br>' +
	'            <br>' +
	'            <h4>Alcances y limitaciones a la responsabilidad de SODIMAC</h4>' +
	'            <br>' +
	'            <br>' +
	'            <ul class="ul-deci">' +
	'				<li><b>SODIMAC NO TENDRÁ NINGUNA RESPONSABILIDAD, SEÑALANDO EN FORMA ENUNCIATIVA MAS NO LIMITATIVA, EN CASO QUE:</b>' +
	'            <br>' +
	'            <p class="espacios"> o	Investigar la veracidad de la información capturada en la forma de registro del Portal y enviada por el Cliente; <br>' +
	'              <br>' +
	'              o	Investigar ni garantizar la calidad ni capacidad jurídica o aptitud comercial de los Clientes; <br>' +
	'              <br>' +
	'              o	Existan fallas técnicas en el sistema, en el Portal o del Internet, de cualquier naturaleza análoga, que estén fuera de su alcance y control; <br>' +
	'              <br>' +
	'              o	Información, ofertas, promociones o comunicaciones provenientes de Portal s o páginas electrónicas ajenas a SODIMAC (particularmente en los casos de links o ligas externas, así como publicidad o banners que aparezca(n) en el Portal que haga referencia o conduzca a Portal s externos, ajenos o de terceras personas); <br>' +
	'              <br>' +
	'              o	Los Clientes violen algún derecho de terceros, ya sea por la información que los Clientes proporcionen al registrarse en el Portal, o bien por su conducta y/o actividades realizadas en línea (violaciones a derechos de autor, marcas o propiedad intelectual en general; publicar o comunicar materiales ofensivos, engañosos, tendenciosos, difamatorios o violatorios de alguna ley; no cumplir con contratos o transacciones realizadas en línea o a través de cualesquiera de los servicios proporcionados por SODIMAC, etc.). </p>' +
	'            </li>' +
	'            <br>' +
	'            <li>SODIMAC estará, en todo caso, obligada a cumplir con sus compromisos y obligaciones aquí asumidas, siempre y cuando, los Clientes cumplan con todos los compromisos y obligaciones aquí adquiridos por ellos, en particular los referentes a pagos. Cualquier incumplimiento de alguna de las partes al contrato o Condiciones Generales traerá por consecuencia, sin necesidad de que medie resolución judicial al efecto, la rescisión inmediata de la relación contractual, sin perjuicio de las acciones legales que la parte afectada instaure contra la parte que incumple. </li>' +
	'            <li>En ningún caso SODIMAC será responsable de daños, pérdidas o gastos directos, indirectos, inherentes o consecuentes, que surjan en relación con este Portal o su uso o imposibilidad de uso por alguna de las partes, o en relación con cualquier falla en el rendimiento, error, omisión, interrupción, defecto, demora en la operación o transmisión de virus de computadora o falla de sistema o línea, aún en el caso de que SODIMAC, o sus representantes fueran informados sobre la posibilidad de dichos daños, pérdidas o gastos. El uso de ligas o links con otras páginas electrónicas de Internet será bajo el propio riesgo del Cliente; SODIMAC no investiga, verifica, controla ni respalda el contenido, la exactitud, las opiniones expresadas y otras conexiones suministradas por estos medios.' +
	'            En cualquiera de los casos la Responsabilidad máxima por parte de <b>SODIMAC</b> será hasta por la cantidad pagada por el Cliente por los servicios otorgados. </li>' +
	'            <li>En cualquiera de los casos la Responsabilidad máxima por parte de SODIMAC será hasta por la cantidad pagada por el Cliente por los servicios otorgados.</li>' +
	'            <p></p>' +
	'            <br>' +
	'            <br>' +
	'            <h4>Propiedad industrial</h4>' +
	'            <br>' +
	'            <br>' +
	'            <p> Los contenidos de este portal, incluyendo en forma enunciativa más no limitativa: las pantallas relativas a los servicios de <b>SODIMAC</b>, los programas, bases de datos, redes, archivos, gráficas, logos, íconos, imágenes, audio, clips y software que permiten al Cliente acceder, son propiedad de SODIMAC y están protegidas por las leyes y tratados internacionales en materia de derecho de autor, marcas, patentes, modelos y diseños industriales. El contenido y software sólo pueden ser usados como un recurso de compra, cualquier otro uso, incluyendo la reproducción, modificación, distribución, transmisión, re-publicación, exhibición o ejecución del contenido de esta página se encuentran estrictamente prohibidas. El uso indebido y la reproducción total o parcial de dichos contenidos serán objeto de todas las acciones judiciales pertinentes que apliquen según las legislaciones aplicables. </p>' +
	'            <br>' +
	'            <br>' +
	'            <h4>Competencia</h4>' +
	'            <br>' +
	'            <br>' +
	'            <p> Las operaciones comerciales que se realicen entre SODIMAC y sus Clientes estarán regidas tanto por la Ley Federal de Protección al Consumidor de los Estados Unidos Mexicanos y la legislación común, y por lo tanto, cualquier controversia que derive de la aplicación de la misma se ventilará ante las autoridades administrativas y/o judiciales competentes de la ciudad de los domicilios de los contratantes, renunciando expresamente a cualquiera otra jurisdicción que les pudiera corresponder por razón de su domicilio presente o futuro. Con relación a la información comercial y disposiciones generales para los productos que ofrece SODIMAC se observa lo dispuesto en las Normas Oficiales Mexicanas aplicables a los mismos. </p>' +
	'            <br>' +
	'            <br>' +
	'            <h4>Aceptación, Modificaciones y no renuncia de los Condiciones Generales y de los Derechos y Obligaciones derivados de los mismos.</h4>' +
	'            <br>' +
	'            <br>' +
	'            <ul class="ul-deci">' +
	'				<li>En caso de que SODIMAC no siga o no adopte de tiempo en tiempo alguna de las políticas aquí establecidas, no se entenderá que renuncia a ellas, o que este convenio se haya modificado o renovado. La relación entre los Clientes y SODIMAC se regirá siempre por los Condiciones Generales aquí establecidos y aceptados por las partes. </li>' +
	'              <li>Estos Condiciones Generales podrán ser modificados por SODIMAC de tiempo en tiempo previo aviso al Cliente mediante la publicación respectiva en la sección denominada Aviso Legal del Portal SODIMAC. Es obligación del Cliente visitar estos Condiciones Generales con frecuencia razonable para estar enterado de los cambios a los mismos. </li>' +
	'              <li>El Cliente acepta estos Condiciones Generales, enunciativa mas no limitativamente, ya sea pulsando (haciendo click en) el botón en la página del registro titulado "Acepto" o accediendo, usando, visitando, listando bienes o servicios, subastando, vendiendo o comprando en el Portal. </li>' +
	'			</ul>' +
	            '' +
	'            Políticas de privacidad' +
	            '' +
	'            <br>' +
	'            <br>' +
	'            <p> Las partes serán responsables cada cual de cumplir lo dispuesto por la Ley Federal de Protección de Datos Personales en Posesión de los Particulares en relación con la información que al amparo de este contrato intercambien. Así mismo, la información que SODIMAC reciba del Cliente quedará regulada por el Aviso de Privacidad correspondiente disponible en el Portal, ratificando el Cliente su conformidad al haber leído y entendido sus alcances y aceptado sus modificaciones por cambios de ley. </p>' +
	'            <br>' +
	'            <br>' +
	'            <h4>Conducta del Usuario.</h4>' +
	'            <br>' +
	'            <br>' +
	'            <p> Tú como usuario del Portal aceptas que está prohibido proporcionar a través del mismo, cualquier información que: </p>' +
	'            <br>' +
	'		  <ul class="ul-point">' +
	'            <li>No sea de tu propiedad, o no tengas autorización de uso del titular. </li>' +
	'            <li>Promueva realizar algún ilícito. </li>' +
	'            <li>Indique algún tipo de difamación. </li>' +
	'            <li>Implique discriminación. </li>' +
	'            <li>Sea falsa y/o desactualizada. </li>' +
	'            <li>Indique alguna idea y/o sentimiento en contra de las buenas costumbres y usos de la sociedad.</li>' +
	'            <li>Invite a la violencia.</li>' +
	'            <li>Sea Obscena. </li>' +
	'             <li>Contenga virus informáticos o programas que alteren el buen funcionamiento del SITIO. </li>' +
	'		  </ul>' +
	'            <br>' +
	'            <p> Tú como Cliente o usuario del Portal deberás ABSTENERTE de utilizar como medio de pago tarjetas de crédito o debido, cuentas y claves de acceso de cualquier medio de pago que hayan sido robadas o clonadas, aun cuando estas no hayan sido reportadas por el titular de la cuenta ante las autoridades correspondientes, o instituciones financieras que las otorgó; en el entendido que SODIMAC se reserva las acciones legales que correspondan. </p>' +
	'            <br>' +
	'            <p> Además aceptas que está prohibido utilizar o intentar utilizar cualquier otra máquina, software, herramienta, agente u otro dispositivo o mecanismo (incluyendo sin limitación navegadores, spiders, robots, agentes inteligentes) para navegar o buscar en este Portal, cuando la finalidad sea ilegitima o no haya motivo para su uso, utiliza para ello agentes de búsqueda disponibles de SODIMAC en este Portal y otros navegadores de terceros que generalmente están disponibles (ejemplo, Google Chrome o Microsoft Explorer). <br>' +
	'              <br>' +
	'              Tienes prohibido violar la seguridad del Portal; utilizar dispositivos o procesos que interfieran con el funcionamiento del Portal; explorar, realizar, o intentar realizar minería de datos, para escanear o demostrar la vulnerabilidad de un Sistema informático y/o algún apartado con información en el Portal. Cualquier trasgresión al Portal dará lugar a acciones civiles y penales en tu contra y el hecho de que SODIMAC no actúe en consecuencia no implica una renuncia o limitación a sus derechos. <br>' +
	'              <br>' +
	'              SODIMAC tratará la información que registres de manera automatizada y confidencial, de conformidad a los lineamientos del Aviso de Privacidad disponibles en el Portal. No obstante, ante el evento de existir algún requerimiento de Autoridad, la información podrá ser revelada o transmitida a la autoridad competente, con las finalidades que en cada caso corresponda. Todas estas circunstancias serán previa y debidamente advertidas por SODIMAC para tu conocimiento. <br>' +
	'              <br>' +
	'              Podrás actualizar tu información personal o eliminarla en las secciones donde la proporcionaste. Aclarando que proporcionar cualquier información falsa o inexacta constituye el incumplimiento de estas Condiciones Generales. Al confirmar tu compra y finalizar el proceso de pago, estás de acuerdo en aceptar y pagar por los artículos solicitados, otros gastos asociados como servicio de envío, así como en los datos de facturación proporcionados a SODIMAC. </p>' +
	'            <br>' +
	'            <br>' +
	'            <h4>Leyes y Jurisdicción</h4>' +
	'            <br>' +
	'            <br>' +
	'            <p> Las operaciones comerciales que realicen SODIMAC y sus Clientes usuarios estarán regidas por las leyes y normas mexicanas; y cualquier controversia que surja derivada del uso de los Productos y/o Servicio se someterá a los tribunales de Naucalpan de Juárez, Estado de México. <br>' +
	'              <br>' +
	'              Los Condiciones Generales se encuentran vigentes desde la fecha de su publicación en el Portal  y permanecerán vigentes hasta su terminación; SODIMAC puede terminarlos discrecionalmente en cualquier momento sin previo aviso. El Cliente podrá terminar este contrato dejando de usar el Portal y terminando cualquier SERVICIO ofrecido por SODIMAC, en cuyo caso deberá eliminar  de sus archivos físicos o electrónicos cualquier contenido que haya copiado, o descargado de este Portal. <br>' +
	'              <br>' +
	'              La impresión del Aviso Legal del Portal, o de cualquiera de sus secciones, que incluyen las Condiciones Generales, y aviso de privacidad; así como cualquier comunicación que se tenga por medios electrónicos servirán como prueba plena en cualquier procedimiento administrativo y/o judicial. <br>' +
	'              <br>' +
	'              <b>SI USTED NO ACEPTA ESTOS CONDICIONES GENERALES, FAVOR DE ABSTENERSE DE REGISTRARSE.</b> <br>' +
	'              <br>' +
	'              <span style="size:10px">Última Actualización: 02 de septiembre 2019 <br>' +
	'              <br>' +
	'              Sodimac © Comercializadora SDMHC S.A. de C.V., Av. Adolfo López Mateos 201, Col. Santa Cruz Acatlán, Naucalpan de Juárez, Estado de México (2018). </span> </p>' +
	'          </ul></div>' +
	'        </div>' +
	'      </div>' +
	'    </div>' +
	'  </div>' +
	'</div>' +
	'</div>'
	
	
	
	Swal.fire({
		  title: 'Términos y Condiciones',
		  text: "Mensaje",
		  width: '90VW',
		  html: htmlStr,
		  customClass: 'swal-wide',
		  showCancelButton: false,
		  confirmButtonColor: '#3085d6',
		  cancelButtonColor: '#d33',
		  cancelButtonText: 'Cancelar',
		  confirmButtonText: 'Aceptar'
		}).then((result) => {

		})
	
}

function releaseEventtxtemailEditEmpty(){
	
    var valor = $("#txtemailEdit").val().trim();
    if (valor == "") {
        messageDanger("#txtemailEdit_validation", mensaje_mail_vacio_seccion_datos_fiscales);
        addDanger("txtemailEdit");
        return false;    	
    }
    
    var element = document.getElementById("txtemailEdit");
    //element.classList.add("full");
    var result = validateEmail(valor);
    if (!result) {
        messageDanger("#txtemailEdit_validation", mensaje_mail_invalido_seccion_datos_fiscales);
        addDanger("txtemailEdit");
        return false;
    }
    
    addSuccess("txtemailEdit");
    return true;

}

function releaseEventtxtrazonSocialEditEmpty() {
	
    var valor = $("#txtrazonSocialEdit").val().trim();
    if (valor == "") {
        messageDanger("#txtrazonSocialEdit_validation", mensaje_razon_social_vacio);
        addDanger("txtrazonSocialEdit");
        return false;    	
    }
    
    var element = document.getElementById("txtrazonSocialEdit");
    //element.classList.add("full");
    var result = validateRS(valor);
    if (!result) {
        messageDanger("#txtrazonSocialEdit_validation", mensaje_razon_social_no_valido);
        addDanger("txtrazonSocialEdit");
        return false;
    }
    
    //if (gVersionCFDI == CFDI_40) {
	    var regimenCapitalClient = validarRegimenCapital(valor);
	    if (regimenCapitalClient.regimenCapitalEstatus != "1") {
			var mensaje =  regimenCapitalClient.regimenCapitalInvalidoMsg;
	        messageDanger("#txtrazonSocialEdit_validation", mensaje);
	        addDanger("txtrazonSocialEdit");
	        return false;
	    }
	//}
    
    addSuccess("txtrazonSocialEdit");
    return true;
}

function releaseEventtxtnombreObraEditEmpty() {
	
    var valor = $("#txtnombreObraEdit").val().trim();

    var result = validateObra(valor);
    if (!result) {
        messageDanger("#txtnombreObraEdit_validation", mensaje_nombre_obra_no_valido);
        addDanger("txtnombreObraEdit");
        return false;
    }
    
    addSuccess("txtnombreObraEdit");
    return true;

}

function releaseEventtxtresponsableObraEditEmpty() {
	
    var valor = $("#txtresponsableObraEdit").val().trim();

    var result = validateResponsableObra(valor);
    if (!result) {
        messageDanger("#txtresponsableObraEdit_validation", mensaje_responsable_obra_no_valido);
        addDanger("txtresponsableObraEdit");
        return false;
    }
    
    addSuccess("txtresponsableObraEdit");
    return true;

}

function eventos(){
    releaseEventEmpty("ticketHolder");
    releaseEventEmpty("rfcInput");
    releaseEventEmpty("ticketAmount");

    /*releaseEventCopiPaste("#ticketHolder", "paste", "#messajeTicketCompra", "El sistema no permite pegar, ¡Gracias!");
    releaseEventCopiPaste("#ticketHolder", "copy", "#messajeTicketCompra", "El sistema no permite copiar, ¡Gracias!");
    releaseEventCopiPaste("#ticketHolder", "dragover", "#messajeTicketCompra", "El sistema no permite drag & drop, ¡Gracias!");
    releaseEventCopiPaste("#ticketAmount", "paste", "#messajeAmount", "El sistema no permite pegar, ¡Gracias!");
    releaseEventCopiPaste("#ticketAmount", "copy", "#messajeAmount", "El sistema no permite copiar, ¡Gracias!");
    releaseEventCopiPaste("#ticketAmount", "dragover", "#messajeAmount", "El sistema no permite drag & drop, ¡Gracias!");
    releaseEventCopiPaste("#rfcInput", "paste", "#rfc_validation", "El sistema no permite pegar, ¡Gracias!");
    releaseEventCopiPaste("#rfcInput", "copy", "#rfc_validation", "El sistema no permite copiar, ¡Gracias!");
    releaseEventCopiPaste("#rfcInput", "dragover", "#rfc_validation", "El sistema no permite drag & drop, ¡Gracias!");*/
}

function btnGenerarFactura_onClick(){

	$("#rfc_validation").empty();
	$("#cfdi_validation").empty();
	
	//if (gVersionCFDI == CFDI_40) {
		$("#regimenFiscal_validation").empty();
		$("#txtcodigoPostalEdit_validation").empty();
	//}
	
	$("#txtrazonSocialEdit_validation").empty();
	$("#txtemailEdit_validation").empty();
	$("#txtnombreObraEdit_validation").empty();
	$("#txtresponsableObraEdit_validation").empty();
	
    var result = validarDatosCliente();
	
    if (!result){
    	return false;
    }

    var url = '/facturacion/ObtenerTickets';
    $.ajax({
    	url: url,
    	type: "get", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		
    		if (data != "") {
        		var dataArray =	data.split(",");
        		var htmlStr = '<div class="swal2-icon swal2-question swal2-icon-show" style="display: flex;"></div><div id="swal2-content" style="display: block;"><font size="4">¿Desea generar el timbrado de los siguientes tickets?</font></div>';  
        		htmlStr = htmlStr + '<table class="table table-striped">';
        		htmlStr = htmlStr + '<thead>';
        		htmlStr = htmlStr + '<tr>';
        		htmlStr = htmlStr + '  <th scope="col" style="text-align: center;">Ticket u Orden</th>';
        		htmlStr = htmlStr + '</tr>';
        		htmlStr = htmlStr + '</thead>';
        		htmlStr = htmlStr + '<tbody style="font-size: 15px; text-align: left;">';
        		for (i = 0; i < dataArray.length; i++) {

        			htmlStr = htmlStr + '<tr> <th scope="row" style="text-align: center;">' + dataArray[i] + '</th> <tr>';
        			}
        		htmlStr = htmlStr + '</tbody>';
        		htmlStr = htmlStr + '</table>';
    		
        		$("#divTickets").html(htmlStr);
        		$("#divModalConfirm").modal();
    		} else {
    			window.location.replace("/facturacion/");
    		}

        }
    });
    
}

function validarDatosCliente(){
	var result = true;
	var rfc = $("#rfcInput").val().toUpperCase().trim();
	var usoCfdi = $("#subCfdiDown").val().trim();
	var regimenFiscal = "";
	var blnRazonSocial = false;
	
	var blnEmail = false;

	//if (gVersionCFDI == CFDI_40) {
		regimenFiscal = $("#subRegimenFiscalDown").val().trim();
	//}

	if (rfc == ""){
        messageDanger("#rfc_validation", rfc_vacío);
        addDanger("rfcInput");
        result = false;
    }

	if (!rfcValidarExpReg(rfc)){
        messageDanger("#rfc_validation", rfc_no_valido);
        addDanger("rfcInput");
        result = false;
    }
	
	var razonSocial = "";
	var codigoPostal = "";
	var email = "";
	
	if (nuevoCliente) {
		
		var resultDato = releaseEventtxtrazonSocialEditEmpty();
		if (resultDato) {
			razonSocial = $("#txtrazonSocialEdit").val().toUpperCase().trim();
		} else {
			result = false;
		}
		
		//if (gVersionCFDI == CFDI_40) {
			resultDato = releaseEventtxtcodigoPostalEditEmpty();
			if (resultDato) {
				codigoPostal = $("#txtcodigoPostalEdit").val().trim();
			} else {
				result = false;
			}
		//}
		
		resultDato = releaseEventtxtemailEditEmpty();
		if (resultDato) {
			email = $("#txtemailEdit").val().toLowerCase().trim();
		} else {
			result = false;
		}
		
	} else {
		validarRazonSocial = true;
		if ( $("#txtrazonSocialEdit").val()==undefined || $("#txtrazonSocialEdit").hasClass("has-success") || $("#txtrazonSocialEdit").hasClass("has-danger") || validarRazonSocial ) {
			var resultDato = releaseEventtxtrazonSocialEditEmpty();
			if (resultDato) {
				razonSocial = $("#txtrazonSocialEdit").val().toUpperCase().trim();
			} else {
				result = false;
			}
		}
		
		//if (gVersionCFDI == CFDI_40) {
			if ( $("#txtcodigoPostalEdit").val()==undefined || $("#txtcodigoPostalEdit").hasClass("has-success") || $("#txtcodigoPostalEdit").hasClass("has-danger") || validarCodigoPostal ) {
				var resultDato = releaseEventtxtcodigoPostalEditEmpty();
				if (resultDato) {
					codigoPostal = $("#txtcodigoPostalEdit").val().trim();
				} else {
					result = false;
				}
			}
		//}
		

		if ($("#txtemailEdit").val()==undefined || $("#txtemailEdit").hasClass("has-success") || $("#txtemailEdit").hasClass("has-danger") || validarCorreo ) {
			var resultDato = releaseEventtxtemailEditEmpty();
			if (resultDato) {
				email = $("#txtemailEdit").val().toLowerCase().trim();
			} else {
				result = false;
			}
		}
	}
		
	if (usoCfdi == ""){
		messageDanger("#cfdi_validation", cfdi_no_valido);
		result = false;
	}
	
	//if (gVersionCFDI == CFDI_40) {
		if (regimenFiscal == ""){
			messageDanger("#regimenFiscal_validation", regimen_fiscal_no_valido);
			result = false;
		}
	//}
	
	return result;
}

function agregaInputHtml(message){
	 var numItems = 0;
	
   var value = $("#ticketHolder").val();
   
  /////////////////////////////////////////////////////////////////////////////////////// 
   var divParent = document.createElement("DIV");
   divParent.setAttribute("class", "row form-group m-b-0 form-inline");
   divParent.setAttribute("id", value);
   
   var divChild1 = document.createElement("DIV");
   divChild1.setAttribute("class", "col-sm-3 align-middle");
   var span1 = document.createElement("SPAN");
   span1.setAttribute("class", "align-middle");
   span1.innerHTML = message;
   divChild1.appendChild(span1);
   
   var divChild2 = document.createElement("DIV");
   divChild2.setAttribute("class", "col-sm-6");
   var input = document.createElement("INPUT");
   input.setAttribute("class", "form-control count");
   input.setAttribute("type", "text");
   input.setAttribute("size", "22");
   input.setAttribute("value", value);   
   input.setAttribute("readonly","");
   numItems = $('.count').length;
   divChild2.appendChild(input);
   
   var divChild3 = document.createElement("DIV");
   divChild3.setAttribute("class", "itemCont col-sm-3 text-center");
   var btn = document.createElement("button");
   btn.setAttribute("class", "btn btn-warning mb-2");
   btn.setAttribute("id", "b"+value);
   btn.setAttribute("type", "button");
   btn.setAttribute("onClick","eliminarTicket('" +  value  + "');");
   btn.innerHTML = "&nbsp; &nbsp; Eliminar &nbsp; &nbsp; &nbsp; ";
   divChild3.appendChild(btn);
   
   
   divParent.appendChild(divChild1);
   divParent.appendChild(divChild2);
   divParent.appendChild(divChild3);
   document.getElementById("newItemsTicket").appendChild(divParent);
      
   
}

var gEmpresa = "";
var gCp = null;
var gRegimenFiscal = null;
var gMail = "";
var nuevoCliente = false;
var validarCorreo = false;
var validarRazonSocial = false;
var validarCodigoPostal = false;

function validaRFC(){
   $("#datosEmpresa").empty();
   $('#btnActualizar').empty();
   $('#subCfdiDown').empty();
   $('#txtemailEdit').empty();
   $('#txtnombreObraEdit').empty();
   $('#txtresponsableObraEdit').empty();
   $('#txtrazonSocialEdit2').empty();
   $('#txtemailEdit2').empty();
   
   $('#btnBuscarRFC').prop("disabled", false);
   $('#subCfdiDown').prop("disabled", false);
   $('#txtnombreObraEdit').prop("disabled", false);
   $('#txtresponsableObraEdit').prop("disabled", false);
   $('#txtrazonSocialEdit2').prop("disabled", false);
   
   //if (gVersionCFDI == CFDI_40) {
	  $('#subRegimenFiscalDown').empty();
	  $('#subRegimenFiscalDown').prop("disabled", false);
	  
	  //$("#txtcodigoPostalEdit").empty();
	  $("#txtcodigoPostalEdit2").empty();
	  $('#txtcodigoPostalEdit2').prop("disabled", false);
   //}
   
   var rfc = $("#rfcInput").val().toUpperCase();

   if (rfc == $("#hdnRFCPublicoGeneral").val()) {
       addDanger("rfcInput");  
       messageDanger("#rfc_validation", $("#hdnRFCPublicoGeneralMensaje").val());
	   return;
   }

   var valrfc = rfcValidation(rfc);
   if (valrfc != true){
       $("#rfc_validation").empty();
       lonRfc = $("#rfcInput").val().length;
       if (lonRfc == 0){
           addDanger("rfcInput");  
           messageDanger("#rfc_validation", rfc_vacío);
       } else {
           addDanger("rfcInput");  
           messageDanger("#rfc_validation", rfc_no_valido);
       }
   } else {
       $("#rfc_validation").empty();        
       //variable que devuelve llamada de rfc no existente en DB
       
       var cliente = existeRFCVersion(rfc,gVersionCFDI);
       
       if (!cliente.existe){
	       $("#datosEmpresa").empty();
	       $('#btnActualizar').empty();
	       emailInputEmpresa();
	       gCp = "";
           gRegimenFiscal = "";
	       
	       nuevoCliente = true;
	       $("#datosEmpresa").append('<div class="form-group"><label class="custom-control custom-checkbox wizard_label_block ml-1"><input type="checkbox" id="guardarDatos" name="guardarDatos" class="custom-control-input"> <span class="custom-control-indicator"></span><span class="custom-control-description custom_control_description_color"> Guardar datos y aceptar</span></label><span class="pl-2"><a href="#" onclick="showTerminos();" style="text-decoration:underline;">Terminos y Condiciones</a></span></div>');
       } else {
           $("#rfc_validation").empty();        
           $("#datosEmpresa").empty();
           $('#btnActualizar').empty();
           var empresa = cliente.razonSocial;
           var cp = null;
           var regimenFiscal = null;
           gCp = "";
           gRegimenFiscal = "";
           
           if (cliente.codigoPostal != undefined) {
			  cp = cliente.codigoPostal;
		   }
		   if (cliente.regimenFiscal != undefined) {
			  regimenFiscal = cliente.regimenFiscal;
		   }
           
           gEmpresa = empresa;
           gCp = cp;
           gRegimenFiscal = regimenFiscal;
            
           var mail=cliente.email;
           ///mail=mail.split("?").join("*");
           mail = mail.replace(/\?/g, '•');
           nuevoCliente = false;
           emailMask(empresa,mail, cp);           
       }//if (!cliente.existe)
       
       
//       $('#cfdi_validation').empty();
//       $('#subCfdiDown').empty();
//       $('#subCfdiDown').append(new Option("Seleccione uso de CFDI", ""));
//       for (var i = 0; i < cliente.usosCfdi.length; i++) {
//           $('#subCfdiDown').append(new Option(cliente.usosCfdi[i].id + "-" + cliente.usosCfdi[i].descripcion, cliente.usosCfdi[i].id));
//       }
//     
	   if (!blnNotaCredito) {
	   	 setUsoDeCfdi(rfc, gVersionCFDI, gRegimenFiscal);
	   }
  
       if ( (!blnNotaCredito) && cliente.existe)  {
			$('#subCfdiDown option[value="' + cliente.claveUsoCfdi + '"]').prop('selected', true);
	   }
       
       if (blnNotaCredito) {
		   $('#btnBuscarRFC').prop("disabled", true);
		   var claveUsoCfdiNC = gDatosCfdiNC.usoCfdi;
		   var descUsoCfdiNC = gDatosCfdiNC.usoCfdiDescripcion;
		   var razonSocialNC = gDatosCfdiNC.razonSocial;
		   var cpNC = gDatosCfdiNC.codigoPostal;
		   var mailNC = gDatosCfdiNC.correo;
		   mailNC = mailNC.replace(/\?/g, '•');
		   
		   var indexArroba = mailNC.indexOf("@");
		   var posfinCorreo1Parte = 3;
		   if (posfinCorreo1Parte > indexArroba) posfinCorreo1Parte = indexArroba;
		   var uno = mailNC.substring(0, posfinCorreo1Parte);
		   var dos = "";
		   if (posfinCorreo1Parte < indexArroba) dos = mailNC.substring(posfinCorreo1Parte, indexArroba);
	       var posFinal = mailNC.length;
	       var indexPunto = mailNC.lastIndexOf(".");
	       var posfinDominio1Parte = indexArroba + 4; 
	       if (posfinDominio1Parte > indexPunto) posfinDominio1Parte = indexPunto;
	       var tres = mailNC.substring(indexArroba+1, posfinDominio1Parte);
	       var cuatro = "";
	       if (posfinDominio1Parte < indexPunto) cuatro = mailNC.substring(posfinDominio1Parte, indexPunto);
	       var finalMail= mailNC.substring(indexPunto, posFinal);
	       //var gMailNC = uno+dos+tres+cuatro+finalMail;
		   
		   $('#cfdi_validation').empty();
		   $('#subCfdiDown').empty();
    	   $('#subCfdiDown').append(new Option(claveUsoCfdiNC + "-" + descUsoCfdiNC, claveUsoCfdiNC));
    	   $('#subCfdiDown option[value="' + claveUsoCfdiNC + '"]').prop('selected', true);
    	   $('#subCfdiDown').prop("disabled", true);
    	   $('#txtnombreObraEdit').prop("disabled", true);
    	   $('#txtresponsableObraEdit').prop("disabled", true);
    	   
    	   $('#txtrazonSocialEdit').val(razonSocialNC);
    	   $('#txtrazonSocialEdit2').val(razonSocialNC);
    	   $('#txtrazonSocialEdit2').prop("disabled", true);
    	   
    	   $("#txtemailEdit").val("");
    	   $("#txtemailEdit2").val(uno+dos+'@'+tres+cuatro+finalMail);
    	   
    	   addSuccess("txtrazonSocialEdit2");
    	   
    	   //if (gVersionCFDI == CFDI_40) {
	          //$("#txtcodigoPostalEdit").val(cpNC);
	          $("#txtcodigoPostalEdit").val(cpNC);
			  $("#txtcodigoPostalEdit2").val(cpNC);
			  //$('#txtcodigoPostalEdit').prop("disabled", true);
			  $('#txtcodigoPostalEdit2').prop("disabled", true);
			  addSuccess("txtcodigoPostalEdit2");
		   //}
       }
       
       //if (gVersionCFDI == CFDI_40) {
	       
	       if (blnNotaCredito) {
				gRegimenFiscal = gDatosCfdiNC.regimenFiscal;
				
				var claveRegimenFiscalNC = gDatosCfdiNC.regimenFiscal;
		        var descRegimenFiscalNC = gDatosCfdiNC.regimenFiscalDescripcion;
				
				$('#subRegimenFiscalDown').empty();
                $('#subRegimenFiscalDown').append(new Option(claveRegimenFiscalNC + "-" + descRegimenFiscalNC, claveRegimenFiscalNC));
				$('#subRegimenFiscalDown').prop("disabled", true);
		   } else {
			   $('#subRegimenFiscalDown').empty();
               $('#subRegimenFiscalDown').append(new Option("Seleccione el régimen fiscal", ""));
               for (var i = 0; i < cliente.listRegimenfiscal.length; i++) {
                   $('#subRegimenFiscalDown').append(new Option(cliente.listRegimenfiscal[i].id + "-" + cliente.listRegimenfiscal[i].descripcion, cliente.listRegimenfiscal[i].id));
               }
		   }
		   
		   if (gRegimenFiscal != null) {
	       		$('#subRegimenFiscalDown option[value="' + gRegimenFiscal + '"]').prop('selected', true);
	       }
	   //}
       
       addSuccess("rfcInput");
       $('#btnGenerarFactura').prop("disabled", false);
       $('#btnBuscarRFC').removeClass("btn-primary").addClass("btn-secondary");
   }   
           /*************** DATOS QUE MANDARAN A TRAER EN SU BACK, ESTOS SON SOLO REFERENCIALES *****************************/
}

function setUsoDeCfdi(rfc, version, regimenFiscal) {
	var usoCfdiVersion = consultarUsoCfdiRegimenFiscal(rfc, version, regimenFiscal);
	var usosCfdi = usoCfdiVersion.usosCfdi;
	$('#cfdi_validation').empty();
	$('#subCfdiDown').empty();
	$('#subCfdiDown').append(new Option("Seleccione uso de CFDI", ""));
	for (var i = 0; i < usosCfdi.length; i++) {
		$('#subCfdiDown').append(new Option(usosCfdi[i].id + "-" + usosCfdi[i].descripcion, usosCfdi[i].id));
	}
}

function changeRegimenFiscal() {
	var rfc = $("#rfcInput").val().toUpperCase();
	var regimenFiscal = $("#subRegimenFiscalDown").val().trim();
	console.log("regimenFiscal: " + regimenFiscal);
	setUsoDeCfdi(rfc, gVersionCFDI, regimenFiscal);
}

function emailInputEmpresa() {
	var htmlForm =           '<div class="form-group">' +
   								'<input id="txtrazonSocialEdit" name="txtrazonSocialEdit" placeholder="" type="text" class="form-control text-uppercase" onblur="releaseEventtxtrazonSocialEditEmpty()" onkeypress="txtrazonSocialEdit_onkeypress();" maxlength="254" autocomplete="off">' +
   								'<label class="control-label">' +
	    							 '<span class="text-danger">*</span>' +
		    							  'Denominación o Razón Social:' +
   							    '</label>' +
	    						'<div>' +
	    						 	'<div class="col-12" id="txtrazonSocialEdit_validation"></div>' +
	    						'</div>';
	//if (gVersionCFDI == CFDI_40) {
	    htmlForm = htmlForm + '<div class="txt-assist">' +
	                                'SODIMAC S.A. de C.V. <i class="fas fa-times-circle"></i> <br />' + 
									'SODIMAC <i class="fas fa-check-circle green-color"></i>' +
	                            '</div>';
	//}
	   htmlForm = htmlForm +  '</div>';
	
	$("#datosEmpresa").append(htmlForm);
	//if (gVersionCFDI == CFDI_40) {
							 //Régimen fiscal
	 $("#datosEmpresa").append('<div class="form-group">' + 
                                '<label for="subRegimenFiscalDown" class="control-label">' +
									'<span class="text-danger">*</span>Régimen Fiscal:' +
                                '</label>' +
                                '<select id="subRegimenFiscalDown" onchange="changeRegimenFiscal()" class="btn btn-light text-left w-100">' +
                                  	'<option value="">Seleccione el régimen fiscal</option>' +
                                '</select>' +
                                '<div>' +
                                    '<div class="col-12" id="regimenFiscal_validation"></div>' +
                                '</div>' +
                            '</div>');
                             //termina régimen fiscal
    //}
    						//uso de CFDI 
	 $("#datosEmpresa").append('<div class="form-group">' + 
                                '<label for="subCfdiDown" class="control-label">' +
									'<span class="text-danger">*</span>Uso de CFDI:' +
                                '</label>' +
                                '<select id="subCfdiDown" class="btn btn-light text-left w-100">' +
                                  	'<option value="">Seleccione uso de CFDI</option>' +
                                '</select>' +
                                '<div>' +
                                    '<div class="col-12" id="cfdi_validation"></div>' +
                                '</div>' +
                            '</div>');
	    					 //Termina uso de CFDI
	//if (gVersionCFDI == CFDI_40) {
	    					 //codigo postal 1
	 $("#datosEmpresa").append('<div class="form-group">' +
   								'<input id="txtcodigoPostalEdit" name="txtcodigoPostalEdit" placeholder="" type="text" class="form-control" onblur="releaseEventtxtcodigoPostalEditEmpty()" onkeypress="txtcodigoPostalEdit_onkeypress(event);" maxlength="5" autocomplete="off">' +
   								'<label class="control-label">' +
	    							'<span class="text-danger">*</span>' +
	    								'Código postal:' +
   						    	'</label>' +
	    					 	'<div>' +
	    					 		'<div class="col-12" id="txtcodigoPostalEdit_validation"></div>' +
	    						'</div>' +
	    					 '</div>');
	    					 //termina codigo postal 1
	//}
	$("#datosEmpresa").append('<div class="form-group">' +
	    					 	'<input id="txtemailEdit" name="txtemailEdit" placeholder="" type="text" class="form-control" onblur="releaseEventtxtemailEditEmpty()" maxlength="50" autocomplete="off">' +
	    					 	'<label class="control-label">' +
	    					 		'<span class="text-danger">*</span>' +
	    					 		'Correo electrónico:' +
	    					 	'</label>' +
	    					 	'<div>' +
	    					 		'<div class="col-12" id="txtemailEdit_validation"></div>' +
	    					 	'</div>' +
	    					 '</div>');
	
	if (blnDescargar) {
		$("#datosEmpresa").append('' + 
	    					 '<div class="form-group">' +
	   							'<input id="txtnombreObraEdit" name="txtnombreObraEdit" placeholder="" type="text" class="form-control" onblur="releaseEventtxtnombreObraEditEmpty()" onkeypress="txtnombreObraEdit_onkeypress();" maxlength="50" autocomplete="off">' +
	   							'<label class="control-label">' +
		    							  'Nombre de obra:' +
	   						    '</label>' +
		    							 '<div>' +
		    							 	'<div class="col-12" id="txtnombreObraEdit_validation"></div>' +
		    							 '</div>' +
		    					 '</div>'
		);
		$("#datosEmpresa").append('' + 
				 '<div class="form-group">' +
						'<input id="txtresponsableObraEdit" name="txtresponsableObraEdit" placeholder="" type="text" class="form-control" onblur="releaseEventtxtresponsableObraEditEmpty()" onkeypress="txtresponsableObraEdit_onkeypress();" maxlength="50" autocomplete="off">' +
						'<label class="control-label">' +
							  'Responsable de obra:' +
					    '</label>' +
							 '<div>' +
							 	'<div class="col-12" id="txtresponsableObraEdit_validation"></div>' +
							 '</div>' +
					 '</div>'
		);
	}
	
	eventosRegistro();
}

function emailMask(empresa, mail, cp){
   
	var indexArroba = mail.indexOf("@");
	var posfinCorreo1Parte = 3;
	if (posfinCorreo1Parte > indexArroba) posfinCorreo1Parte = indexArroba;
	var uno = mail.substring(0, posfinCorreo1Parte);
	var dos = "";
	if (posfinCorreo1Parte < indexArroba) dos = mail.substring(posfinCorreo1Parte, indexArroba);
   var posFinal = mail.length;
   var indexPunto = mail.lastIndexOf(".");
   var posfinDominio1Parte = indexArroba + 4; 
   if (posfinDominio1Parte > indexPunto) posfinDominio1Parte = indexPunto;
   var tres = mail.substring(indexArroba+1, posfinDominio1Parte);
   var cuatro = "";
   if (posfinDominio1Parte < indexPunto) cuatro = mail.substring(posfinDominio1Parte, indexPunto);
   var finalMail= mail.substring(indexPunto, posFinal);
   gMail = uno+dos+tres+cuatro+finalMail;
   
   var htmlForm =             '<div class="form-group">' +
								'<div id="razonSocialMask" class="row">' +
									'<div class="col-md-11" id="rsfijo">' +
	   									'<input id="txtrazonSocialEdit2" name="txtrazonSocialEdit2" disabled  placeholder="" type="text" class="form-control text-uppercase" onblur="releaseEventtxtrazonSocialEditEmpty();" onkeypress="txtrazonSocialEdit_onkeypress();" maxlength="254" autocomplete="off">' +
	   		   							'<label for="txtrazonSocialEdit2" style="left: 1.5rem !important;" class="control-label">' +
			    							 '<span class="text-danger">*</span>' +
			    							  'Denominación o Razón Social:' +
		    							  '</label>' +
	   								'</div>' +
									'<div class="col-md-11 hide-email" id="rsEditable">' +
	   									'<input id="txtrazonSocialEdit" name="txtrazonSocialEdit" placeholder="" type="text" class="form-control text-uppercase" onblur="releaseEventtxtrazonSocialEditEmpty();" onkeypress="txtrazonSocialEdit_onkeypress();" maxlength="254" autocomplete="off">' +
	   		   							'<label for="txtrazonSocialEdit" style="left: 1.5rem !important;" class="control-label">' +
			    							 '<span class="text-danger">*</span>' +
			    							  'Denominación o Razón Social:' +
		    							 '</label>' +
	    							'</div>' +
	   								'<div id="iconEditRazonSocial" class="col-md-1">' +
	   									'<i class="far fa-edit fa-2x" style="cursor: pointer; color:rgba(33,150,243,1);" onClick="editarRazonSocial()"></i>' +
	   								'</div>' +
	   								'<div id="iconCancelRazonSocial" class="col-md-1 hide-email">' +
   										'<i class="far fa-window-close fa-2x" style="cursor: pointer; color:rgba(33,150,243,1);" onClick="cancelarEditarRazonSocial()"></i>' +
   									'</div>' +
	   							'</div>' +
		    				    '<div  class="row">' +
		    				    	'<div class="col-11" id="txtrazonSocialEdit_validation"></div>' +
	    				    	'</div>';
	//if (gVersionCFDI == CFDI_40) {    				    	
	 htmlForm = htmlForm +      '<div class="txt-assist">' +
	                                'SODIMAC S.A. de C.V. <i class="fas fa-times-circle"></i> <br />' + 
									'SODIMAC <i class="fas fa-check-circle green-color"></i>' +
	                            '</div>';
	//}
	                            
	 htmlForm = htmlForm + '</div>';
	
	$("#datosEmpresa").append(htmlForm);
		    				
	//if (gVersionCFDI == CFDI_40) {
		    				 //Régimen fiscal
     $("#datosEmpresa").append('<div class="form-group">' + 
                                '<label for="subRegimenFiscalDown" class="control-label">' +
									'<span class="text-danger">*</span>Régimen Fiscal:' +
                                '</label>' +
                                '<select id="subRegimenFiscalDown" onchange="changeRegimenFiscal()" class="btn btn-light text-left w-100">' +
                                  	'<option value="">Seleccione el régimen fiscal</option>' +
                                '</select>' +
                                '<div>' +
                                    '<div class="col-11" id="regimenFiscal_validation"></div>' +
                                '</div>' +
                            '</div>');
                             //termina régimen fiscal
	//}
							//uso de CFDI 
	$("#datosEmpresa").append('<div class="form-group">' + 
                                '<label for="subCfdiDown" class="control-label">' +
									'<span class="text-danger">*</span>Uso de CFDI:' +
                                '</label>' +
                                '<select id="subCfdiDown" class="btn btn-light text-left w-100">' +
                                  	'<option value="">Seleccione uso de CFDI</option>' +
                                '</select>' +
                                '<div>' +
                                    '<div class="col-11" id="cfdi_validation"></div>' +
                                '</div>' +
                            '</div>');
	    					//Termina uso de CFDI);
	//if (gVersionCFDI == CFDI_40) {
		    				//Codigo postal 2
	$("#datosEmpresa").append('<div class="form-group">' +
		    					'<div id="codigoPostalMask" class="row">' +
									'<div class="col-md-11" id="rsfijoCodigoPostal">' +
	   									'<input id="txtcodigoPostalEdit2" name="txtcodigoPostalEdit2" disabled  placeholder="" type="text" class="form-control" onblur="releaseEventtxtcodigoPostalEditEmpty();" onkeypress="txtcodigoPostal_onkeypress();" maxlength="5" autocomplete="off">' +
	   		   							'<label for="txtcodigoPostalEdit2" style="left: 1.5rem !important;" class="control-label">' +
			    							 '<span class="text-danger">*</span>' +
			    							  'Código postal:' +
		    							  '</label>' +
	   								'</div>' +
									'<div class="col-md-11 hide-email" id="rsEditableCodigoPostal" >' +
	   									'<input id="txtcodigoPostalEdit" name="txtcodigoPostalEdit"   placeholder="" type="text" class="form-control" onblur="releaseEventtxtcodigoPostalEditEmpty();" onkeypress="txtcodigoPostalEdit_onkeypress(event);" maxlength="5" autocomplete="off">' +
	   		   							'<label for="txtcodigoPostalEdit" style="left: 1.5rem !important;" class="control-label">' +
			    							 '<span class="text-danger">*</span>' +
			    							  'Código postal:' +
		    							 '</label>' +
	    							'</div>' +
	   								
	   								
	   								'<div id="iconEditCodigoPostal" class="col-md-1">' +
	   									'<i class="far fa-edit fa-2x" style="cursor: pointer; color:rgba(33,150,243,1);" onClick="editarCodigoPostal()"></i>' +
	   								'</div>' +
	   								'<div id="iconCancelCodigoPostal" class="col-md-1 hide-email">' +
   										'<i class="far fa-window-close fa-2x" style="cursor: pointer; color:rgba(33,150,243,1);" onClick="cancelarEditarCodigoPostal()"></i>' +
   									'</div>' +
	   							'</div>' +
		    				    '<div  class="row">' +
		    				    	'<div class="col-11" id="txtcodigoPostalEdit_validation"></div>' +
	    				    	'</div>' +

		    				'</div>');
		    				//termina codigo postal 2
	//}  				
	$("#datosEmpresa").append('<div class="form-group">' +
		    					'<div id="mailMask" class="row">' +
			    					 '<div class="col-md-11" id="emailfijo">' +
			    					 	'<input id="txtemailEdit2" name="txtemailEdit2" disabled  placeholder="" type="text" class="form-control"  maxlength="50" autocomplete="off">' +
			    					 	'<label for="txtemailEdit2" style="left: 1.5rem !important;" class="control-label">' +
			    					 		'<span class="text-danger">*</span>' +
			    					 		'Correo Electrónico:' +
			    					 	'</label>' +
			    					 '</div>' +
			    					 '<div class="col-md-11 hide-email" id="emailEditable">' +
			    					 	'<input id="txtemailEdit" name="txtemailEdit"   value="" placeholder="" type="text" class="form-control" onblur="releaseEventtxtemailEditEmpty()" maxlength="50" autocomplete="off">' +
			    					 	'<label for="txtemailEdit" style="left: 1.5rem !important;" class="control-label">' +
			    					 		'<span class="text-danger">*</span>' +
			    					 		'Correo Electrónico:' +
			    					 	'</label>' +
			    					 '</div>' +
			    					 
		   							'<div id="iconEditMail" class="col-md-1">' +
										'<i class="far fa-edit fa-2x" style="cursor: pointer; color:rgba(33,150,243,1);" onClick="editarCorreo()"></i>' +
									'</div>' +
									'<div id="iconCancelMail" class="col-md-1 hide-email">' +
										'<i class="far fa-window-close fa-2x" style="cursor: pointer; color:rgba(33,150,243,1);" onClick="cancelarEditarCorreo()"></i>' +
									'</div>' +
		    				    '</div>' +
		    				    '<div  class="row">' +
		    				    	'<div class="col-11" id="txtemailEdit_validation"></div>' +
		    				    '</div>' +
	    					 '</div>');
   if (blnDescargar) {
	   $("#datosEmpresa").append('' + 
				'<div class="form-group">' +
				'<div class="row">' +
					'<div class="col-md-11">' +
							'<input id="txtnombreObraEdit" name="txtnombreObraEdit" placeholder="" type="text" class="form-control" onblur="releaseEventtxtnombreObraEditEmpty();" onkeypress="txtnombreObraEdit_onkeypress();" maxlength="50" autocomplete="off">' +
  							'<label for="txtnombreObraEdit" style="left: 1.5rem !important;" class="control-label">' +
							  'Nombre Obra:' +
						  '</label>' +
						'</div>' +
					'</div>' +
			    '<div  class="row">' +
			    	'<div class="col-11" id="txtnombreObraEdit_validation"></div>' +
		    	'</div>' +
			'</div>'
	   );	   
	   $("#datosEmpresa").append('' + 
				'<div class="form-group">' +
				'<div class="row">' +
					'<div class="col-md-11">' +
							'<input id="txtresponsableObraEdit" name="txtresponsableObraEdit" placeholder="" type="text" class="form-control" onblur="releaseEventtxtresponsableObraEditEmpty();" onkeypress="txtresponsableObraEdit_onkeypress();" maxlength="50" autocomplete="off">' +
 							'<label for="txtresponsableObraEdit" style="left: 1.5rem !important;" class="control-label">' +
							  'Responsable Obra:' +
						  '</label>' +
						'</div>' +
					'</div>' +
			    '<div  class="row">' +
			    	'<div class="col-11" id="txtresponsableObraEdit_validation"></div>' +
		    	'</div>' +
			'</div>'
	   );	   
   }
   
   
	$("#txtemailEdit2").val(uno+dos+'@'+tres+cuatro+finalMail);
	$("#txtrazonSocialEdit").val(empresa);
	$("#txtrazonSocialEdit2").val(empresa);
	
	if (blnNotaCredito) {
		$('#iconEditRazonSocial').addClass("hide-email");
		$('#txtrazonSocialEdit2').prop("disabled", true);
	}
	
	//if (gVersionCFDI == CFDI_40) {
		$("#txtcodigoPostalEdit").val(cp);
		$("#txtcodigoPostalEdit2").val(cp);
			
		if (cp == "") {
			validarCodigoPostal = true;
		} else {
			addSuccess("txtcodigoPostalEdit2");
		}
		
		if (blnNotaCredito) {
			$('#iconEditCodigoPostal').addClass("hide-email");
			$('#txtcodigoPostalEdit2').prop("disabled", true);
		}
	//}
	
	addSuccess("txtrazonSocialEdit2");
	addSuccess("txtemailEdit2");
	eventosRegistro();

	
	
	$('#txtemailEdit2').removeClass("has-success");
	$('#txtrazonSocialEdit2').removeClass("has-success");
    $('#txtcodigoPostalEdit2').removeClass("has-success");
}


function editarCorreo() {
	
	$("#txtemailEdit").css("background-color", "white");
	
	$('#emailfijo').removeClass("show-email");
	$('#emailfijo').addClass("hide-email");
	
	$('#emailEditable').removeClass("hide-email");
	$('#emailEditable').addClass("show-email");
	
	$('#iconEditMail').removeClass("show-email");
	$('#iconEditMail').addClass("hide-email");
	
	$('#iconCancelMail').removeClass("hide-email");
	$('#iconCancelMail').addClass("show-email");
	
	$('#txtemailEdit').val('');
	$("#txtemailEdit").focus();
	
	validarCorreo = true;
	
}

function cancelarEditarCorreo() {
	
	//$("#txtemailEdit").css("background-color", "#e9ecef");
	$('#txtemailEdit').val('');
	
	$('#iconEditMail').removeClass("hide-email");
	$('#iconEditMail').addClass("show-email");
	
	$('#iconCancelMail').removeClass("show-email");
	$('#iconCancelMail').addClass("hide-email");
	
	$('#emailfijo').removeClass("hide-email");
	$('#emailfijo').addClass("show-email");
	
	$('#emailEditable').removeClass("show-email");
	$('#emailEditable').addClass("hide-email");
	
	$('#txtemailEdit').removeClass("has-success");
	$('#txtemailEdit').removeClass("has-danger");
	
	$('#txtemailEdit_validation').empty();
	
	validarCorreo = false;
}


function editarRazonSocial() {
	
	$("#txtrazonSocialEdit").css("background-color", "white");
	
	$('#rsfijo').removeClass("show-email");
	$('#rsfijo').addClass("hide-email");
	
	$('#rsEditable').removeClass("hide-email");
	$('#rsEditable').addClass("show-email");
	
	$('#txtrazonSocialEdit').val(gEmpresa);
	
	$('#iconEditRazonSocial').removeClass("show-email");
	$('#iconEditRazonSocial').addClass("hide-email");
	
	$('#iconCancelRazonSocial').removeClass("hide-email");
	$('#iconCancelRazonSocial').addClass("show-email");
	
	$('#txtrazonSocialEdit').removeClass("has-danger");
	$('#txtrazonSocialEdit').removeClass("has-success");

	$("#txtrazonSocialEdit").focus();
	
	validarRazonSocial = true;
}


function cancelarEditarRazonSocial() {
	
	//$("#txtrazonSocialEdit").css("background-color", "#e9ecef");
	$('#txtrazonSocialEdit').val('');
	
	$('#iconEditRazonSocial').removeClass("hide-email");
	$('#iconEditRazonSocial').addClass("show-email");
	
	$('#iconCancelRazonSocial').removeClass("show-email");
	$('#iconCancelRazonSocial').addClass("hide-email");
	
	$('#txtrazonSocialEdit').val('');
	
	$('#rsfijo').removeClass("hide-email");
	$('#rsfijo').addClass("show-email");
	
	$('#rsEditable').removeClass("show-email");
	$('#rsEditable').addClass("hide-email");
	
	$('#txtrazonSocialEdit').removeClass("has-danger");
	$('#txtrazonSocialEdit').removeClass("has-success")

	
	$('#txtrazonSocialEdit_validation').empty();
	
	validarRazonSocial = false;
		
}

function txtrazonSocialEdit_onkeypress() {
	var x = event.keyCode;
	//32=space , 46=Suprimir key
	  if( x==undefined || x==32 || x==46){
	  } else {
		  	var re = new RegExp($("#hdnExpresionRegularRZ").val());
			var isValid = re.test(String.fromCharCode(x));
		    if (!isValid) {
		    	event.preventDefault();
		    }
	  }

}

//Codigo Postal
function editarCodigoPostal() {
	
	$("#txtcodigoPostalEdit").css("background-color", "white");
	
	$('#rsfijoCodigoPostal').removeClass("show-email");
	$('#rsfijoCodigoPostal').addClass("hide-email");
	
	$('#rsEditableCodigoPostal').removeClass("hide-email");
	$('#rsEditableCodigoPostal').addClass("show-email");
	
	$('#txtcodigoPostalEdit').val(gCp);
	
	$('#iconEditCodigoPostal').removeClass("show-email");
	$('#iconEditCodigoPostal').addClass("hide-email");
	
	$('#iconCancelCodigoPostal').removeClass("hide-email");
	$('#iconCancelCodigoPostal').addClass("show-email");
	
	$('#txtcodigoPostalEdit').removeClass("has-danger");
	$('#txtcodigoPostalEdit').removeClass("has-success")

	$("#txtcodigoPostalEdit").focus();
	
	validarCodigoPostal = true;
}

function cancelarEditarCodigoPostal() {
	
	$('#txtcodigoPostalEdit').val('');
	
	$('#iconEditCodigoPostal').removeClass("hide-email");
	$('#iconEditCodigoPostal').addClass("show-email");
	
	$('#iconCancelCodigoPostal').removeClass("show-email");
	$('#iconCancelCodigoPostal').addClass("hide-email");
	
	$('#txtcodigoPostalEdit').val('');
	
	$('#rsfijoCodigoPostal').removeClass("hide-email");
	$('#rsfijoCodigoPostal').addClass("show-email");
	
	$('#rsEditableCodigoPostal').removeClass("show-email");
	$('#rsEditableCodigoPostal').addClass("hide-email");
	
	$('#txtcodigoPostalEdit').removeClass("has-danger");
	$('#txtcodigoPostalEdit').removeClass("has-success")

	
	$('#txtcodigoPostalEdit_validation').empty();
	
	validarCodigoPostal = false;
		
}

function releaseEventtxtcodigoPostalEditEmpty() {
	
    var valor = $("#txtcodigoPostalEdit").val().trim();
    if (valor == "") {
        messageDanger("#txtcodigoPostalEdit_validation", mensaje_codigo_postal_vacio);
        addDanger("txtcodigoPostalEdit");
        return false;    	
    }
    
    var element = document.getElementById("txtcodigoPostalEdit");
    //element.classList.add("full");
    var result = validateRS(valor);
    if (!result) {
        messageDanger("#txtcodigoPostalEdit_validation", mensaje_codigo_postal_no_valido);
        addDanger("txtcodigoPostalEdit");
        return false;
    }
    
    var codigoPostalClient = validarCP(valor);
    
    $('#txtcodigoPostalEdit_validation').empty();
    releaseEventEmpty("txtcodigoPostalEdit");
    
    if (codigoPostalClient.codigoPostalEstatus != "1") {
		var mensaje =  codigoPostalClient.codigoPostalInvalidoMsg;
        messageDanger("#txtcodigoPostalEdit_validation", mensaje);
        addDanger("txtcodigoPostalEdit");
        return false;
    }
    
    addSuccess("txtcodigoPostalEdit");
    return true;

}

function txtcodigoPostalEdit_onkeypress(event) {
	var x = event.keyCode || event.which;
	if( x==undefined || x==32){
	  } else {
		  	var re = new RegExp("^[0-9]*$");
			var isValid = re.test(String.fromCharCode(x));
		    if (!isValid) {
		    	event.preventDefault();
		    }
	  }
}

//Termina codigo postal

function txtnombreObraEdit_onkeypress() {
	var x = event.keyCode;
	//32=space , 46=Suprimir key
	  if( x==undefined || x==32 || x==46){
	  } else {
		  	var re = new RegExp($("#hdnExpresionRegularObra").val());
			var isValid = re.test(String.fromCharCode(x));
		    if (!isValid) {
		    	event.preventDefault();
		    }
	  }

}

function txtresponsableObraEdit_onkeypress() {
	var x = event.keyCode;
	//32=space , 46=Suprimir key
	  if( x==undefined || x==32 || x==46){
	  } else {
		  	var re = new RegExp($("#hdnExpresionRegularResponsableObra").val());
			var isValid = re.test(String.fromCharCode(x));
		    if (!isValid) {
		    	event.preventDefault();
		    }
	  }

}

function validameTicket(id, subId, validateId, number){
	
	$("#messajeTicketCompra").empty();
	$("#messajeAmount").empty();
	
    var subValidateId = "#"+validateId;
// 2024-12-05 rmt se elimina validacion por puntosCES
//    if ($('#ticketAmount').val() == "") {
//    	messageDangerWithStyle("#messajeAmount", "Monto invalido", "width:150%");
//    	return false;
//    }
    z = $(subId).val();
    if (z.length === 10 || z.length === 19 || z.length === 20 ){
		if (z.length != 10){
			
	    	var fechaTicket;
	    	var fechaTmp;
	    	var fechaTmp1;
	    	var fechaTick; 
	    	var dateRegex = /^(?=\d)(?:(?:31(?!.(?:0?[2469]|11))|(?:30|29)(?!.0?2)|29(?=.0?2.(?:(?:(?:1[6-9]|[2-9]\d)?(?:0[48]|[2468][048]|[13579][26])|(?:(?:16|[2468][048]|[3579][26])00)))(?:\x20|$))|(?:2[0-8]|1\d|0?[1-9]))([-.\/])(?:1[012]|0?[1-9])\1(?:1[6-9]|[2-9]\d)?\d\d(?:(?=\x20\d)\x20|$))?(((0?[1-9]|1[012])(:[0-5]\d){0,2}(\x20[AP]M))|([01]\d|2[0-3])(:[0-5]\d){1,2})?$/;
	
			if (z.length === 20)
				fechaTicket = $(subId).val().substr(1,8);	
	
			if (z.length === 19)
				fechaTicket = $(subId).val().substr(0,8);
		
			fechaTemp1 = + fechaTicket.substr(4,2) + '/' + fechaTicket.substr(6,2) + '/' + fechaTicket.substr(0,4);
			fechaTmp = fechaTicket.substr(6,2) + '/' + fechaTicket.substr(4,2) + '/' + fechaTicket.substr(0,4);
			
			fechaTick = new Date(fechaTemp1);
			
			if (!dateRegex.test(fechaTmp)){
	    		messageDanger(subValidateId, "No es un ticket con fecha válida");
	    		addDanger(id);
	    	    return false;
	    	}
	        
	    	var fechaActual = new Date();
	    	
	    	if (fechaTick > fechaActual){
	    		messageDanger(subValidateId, "No es un ticket con fecha válida");
	    		addDanger(id);
	    	  return false;
	    	}
			
		}
		
		validaNumeroDeTickets(1);
		return validarAgregarTicket();
				
    } else {
        var value = document.getElementById(id).value.length;
        $(subValidateId).empty();
        if (value == 0){
            messageDanger(subValidateId, mensaje_leyenda_ticket_orden_vacio);
            addDanger(id);
            return false;
        }

        if ( value>=1 && value < 19) {
            messageDanger(subValidateId, "Solo has ingresado "+value+" dígitos, por favor completa a 10 o 19 dígitos");
            addDanger(id);
            return false;
        } 
        if (value >20){
            messageDanger(subValidateId, mensaje_leyenda_ticket_orden_fuera_de_rango);
            addDanger(id);
            return false;
        }

    }
}

function validaNumeroDeTickets(inicial) {
	var  numItems = $('.count').length;
	if (numItems + inicial >= numeroTicketsPermitidos) {
		$('#ticketHolder').prop("disabled", true);
		$('#ticketAmount').prop("disabled", true);
		$('#btnAgregar').prop("disabled", true);
		$('#btnAgregar').addClass("disabled");
	} else {
		habilitarTicketBtnAgregar();
	}
	
}

function habilitarTicketBtnAgregar(){
	
	$('#ticketHolder').prop("disabled", false);
	$('#ticketAmount').prop("disabled", false);
	$('#btnAgregar').prop("disabled", false);
	$('#btnAgregar').removeClass("disabled");	
}

function verificaAmountTicket(event, id, validateId, number){
    var x = event.keyCode || event.which;
    var subId = "#"+id;
    if((x >= 48 && x<= 57) || (x >= 96 && x<= 105) ||   (x==13) ||  (x==9) || (x==32) ||  (x==8) ||  (x==46) || (37 <= x && x <= 40) ||  (x==190) ||  (x==110) ||  (x==1)){
        if (x==9 || x==13 || x==1) {
            event.preventDefault();
            validameTicket(id, subId, validateId, number);
            document.getElementById(id).removeAttribute("autofocus","");
            $(subId).blur();
        } else {
        	validarAmontticket(event);
        }
        
    } else {
        event.preventDefault();
    }
}

function validarAmontticket(e) {
	var isValid = false;
	var keyCode = e.keyCode || e.which;
	var re = new RegExp("^[0-9]*$");
	if (96 <= keyCode && keyCode <= 105) {
		isValid =  true;
	} else {
		isValid = re.test(String.fromCharCode(keyCode));
	}
	if ((keyCode == 8) ||  (keyCode == 46) || (37 <= keyCode && keyCode <= 40) || (keyCode==190) || (keyCode==110) || isValid && !e.shiftKey) {
		return isValid;
	} else {
		e.preventDefault();
	}
	
}

function eventosRegistro(){
	releaseEventEmpty("txtrazonSocialEdit");
	//if (gVersionCFDI == CFDI_40) {
		releaseEventEmpty("txtcodigoPostalEdit");
	//}
	releaseEventEmpty("txtemailEdit");
	releaseEventEmpty("txtemailEdit2");
	releaseEventEmpty("txtnombreObraEdit");
	releaseEventEmpty("txtresponsableObraEdit");
	
    releaseEventCopiPaste("#txtrazonSocialEdit", "paste", "#txtrazonSocialEdit_validation", "El sistema no permite pegar, ¡Gracias!");
    releaseEventCopiPaste("#txtrazonSocialEdit", "copy", "#txtrazonSocialEdit_validation", "El sistema no permite copiar, ¡Gracias!");
    releaseEventCopiPaste("#txtrazonSocialEdit", "dragover", "#txtrazonSocialEdit_validation", "El sistema no permite drag & drop, ¡Gracias!");
    //if (gVersionCFDI == CFDI_40) {
	    releaseEventCopiPaste("#txtcodigoPostalEdit", "paste", "#txtcodigoPostalEdit_validation", "El sistema no permite pegar, ¡Gracias!");
	    releaseEventCopiPaste("#txtcodigoPostalEdit", "copy", "#txtcodigoPostalEdit_validation", "El sistema no permite copiar, ¡Gracias!");
	    releaseEventCopiPaste("#txtcodigoPostalEdit", "dragover", "#txtcodigoPostalEdit_validation", "El sistema no permite drag & drop, ¡Gracias!");
	//}
    releaseEventCopiPaste("#txtemailEdit", "paste", "#txtemailEdit_validation", "El sistema no permite pegar, ¡Gracias!");
    releaseEventCopiPaste("#txtemailEdit", "copy", "#txtemailEdit_validation", "El sistema no permite copiar, ¡Gracias!");
    releaseEventCopiPaste("#txtemailEdit", "dragover", "#txtemailEdit_validation", "El sistema no permite drag & drop, ¡Gracias!");
    releaseEventCopiPaste("#txtnombreObraEdit", "paste", "#txtnombreObraEdit_validation", "El sistema no permite pegar, ¡Gracias!");
    releaseEventCopiPaste("#txtnombreObraEdit", "copy", "#txtnombreObraEdit_validation", "El sistema no permite copiar, ¡Gracias!");
    releaseEventCopiPaste("#txtnombreObraEdit", "dragover", "#txtnombreObraEdit_validation", "El sistema no permite drag & drop, ¡Gracias!");
    releaseEventCopiPaste("#txtresponsableObraEdit", "paste", "#txtresponsableObraEdit_validation", "El sistema no permite pegar, ¡Gracias!");
    releaseEventCopiPaste("#txtresponsableObraEdit", "copy", "#txtresponsableObraEdit_validation", "El sistema no permite copiar, ¡Gracias!");
    releaseEventCopiPaste("#txtresponsableObraEdit", "dragover", "#txtresponsableObraEdit_validation", "El sistema no permite drag & drop, ¡Gracias!");
}

function validarAgregarTicket(){

	var result = false;
	var ticket = $("#ticketHolder").val();
	var monto = $("#ticketAmount").val();
	if (monto == '') monto='0';
	blnNotaCredito = false;
	//$("#divSpinnerTicker").css("display", "");
	showSpinner ();
	
	console.log(ticket);
	if(ticket != null && ticket.startsWith('0')) {
		ticket = ticket.substr(1);
		console.log("Quitando " + ticket);
	}
	
    var url = '/facturacion/GuardarTicket';
    $.ajax({
    	url: url,
    	data: {ticket: ticket, monto: monto},
    	type: "post", async: false, cache: false, crossDomain: false,
    	success: function(data){
    	   //$("#divSpinnerTicker").css("display", "none");
    	   
  		   $("#rfcInput").val("");
  		   $("#rfcInput").prop("readonly",false);
  		   $('#btnBuscarRFC').prop("disabled", false);
  		   
 		   if (data == "OK" || data == "OKNC") {
 			   if (data == "OKNC") {
  				  $('#btnAgregar').addClass("disabled");
  				  data = "OK";
  				  blnNotaCredito = true;
  				  
  				  gDatosCfdiNC = obtenerDatosCfdiNotaCredito();
  				  //Cuando es nota de credito la factura origen determina la versión
  				  //gVersionCFDI = gDatosCfdiNC.versionCfdi;
  				  var rfcNc = gDatosCfdiNC.rfc.toUpperCase().trim();
  				  var versionVigenteNC = gDatosCfdiNC.versionVigenteNC;
  				  $("#rfcInput").val(rfcNc);
  				  $("#rfcInput").prop("readonly",true);
  				  addSuccess("rfcInput");
  				  console.log(gDatosCfdiNC);
  				  
                  if(versionVigenteNC == "OK") { 
	                  $("#divMensajeNC33").hide();
                  } else {
                      $("#divMensajeNC33").show();
                      $('#btnBuscarRFC').prop("disabled", true);
                  }
  			   }
 			   agregarTicket();
 			   
 			   //Version especifica del CFDI
 			   //if (gVersionCFDI == CFDI_40) {
					/*var cfdiClientEsp = versionEspecificaCFDI(ticket,gVersionCFDI, gVVEE);
			    	if (cfdiClientEsp.versionCFDIEstatus == "1") {
						gVersionCFDI = cfdiClientEsp.versionEspecificaCFDI;
					}*/
					
					gVersionCFDI == CFDI_40;
					//if (gVersionCFDI == CFDI_40) {
						$("#divMensajeVersion40").show();
					/*} else {
						$("#divMensajeVersion40").hide();
					}*/
				//}
 			   result = true;
		   } else if (data == "redirect") {
			   window.location.replace("/facturacion/");
			   habilitarTicketBtnAgregar();
		   } else {
			   $('#ticketHolder').prop("disabled", false);
			   messageDanger("#messajeTicketCompra", data);
			   addDanger("ticketHolder");
			   $('#ticketHolder').select();
			   habilitarTicketBtnAgregar();
		   }
		   
		   hideSpinner();
        }
    });
    
    return result;
}

function agregarTicket(){
	var ticket = $("#ticketHolder").val();
    if (ticket.length === 10){
        var message = "Número de Orden";
    } else {
        var message = "Número de Ticket";                        
    }
    $("#messajeTicketCompra").empty();                    
    agregaInputHtml(message);
    $('#ticketHolder').val('');
    $('#ticketAmount').val('');
    //cambiar estilos para que el BTN se active
    addSuccess("ticketHolder"); 
    var botonSiguienteTicket = document.getElementById("btnFirstNext");
    botonSiguienteTicket.classList.remove("disabled");
    var btnsiguiente = document.getElementById("invalidar");
    btnsiguiente.classList.remove("invalidar");
    //$('#ticketHolder').prop("disabled", true);
    $('#ticketHolder').focus();
	
}


function eliminarTicket(ticket) {
	var url = new URL(url_string);
	var vdescargar = url.searchParams.get("7g8dqd89h");

	Swal.fire({
		  title: 'Eliminar Ticket',
		  text: '¿Está seguro de eliminar este ticket para facturación?',
		  type: 'question',
		  showCancelButton: true,
		  confirmButtonColor: '#3085d6',
		  cancelButtonColor: '#d33',
		  cancelButtonText: 'Cancelar',
		  confirmButtonText: 'Aceptar'
		}).then((result) => {
		  if (result.value) {
			  
				if (vdescargar == null || vdescargar !="vvee") 
				{
					var nCont = 0;
				    $(".itemCont").each(function(){
				    	nCont++;
				      });
				    
				    if (nCont == 1)
					    {
				    	$("#btnFirstNext").addClass("btn-Agregavisible"); 
				    	$("#btnAgregar").removeClass("btn-Agregavisible");
				    	}
				}	
				
			  eliminarTicketRequest(ticket);
			
		  }
		}) 
}

function eliminarTicketRequest(myTicket) {
	
    var url = '/facturacion/EliminarTicket';
    $.ajax({
    	url: url,
    	data: {ticket:myTicket},
    	type: "post", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		if (data=="OK") {
    			eliminardelDOM(myTicket);
    			if (document.getElementsByClassName('form-inline').length == 0) {
    				var botonSiguienteTicket = document.getElementById("btnFirstNext");
    				botonSiguienteTicket.classList.add("disabled");
    			}
    			validaNumeroDeTickets(0);
        		Swal.fire(
      				  'Facturación Sodimac',
      				  'Su ticket ha sido eliminado para facturación',
      				  'success'
      				).then((result) => {
      					$('#ticketHolder').focus();
      				});
    		}

        }
    });
    
    blnNotaCredito = false;
    $('#btnGenerarFactura').prop("disabled", true);
    /*if (vdescargar=="vvee") {
	
		var cfdiClient = versionCFDI(ID_APLICACION_AUTOFACTURADOR_VVEE);
    	if (cfdiClient.versionCFDIEstatus == "1") {
			gVersionCFDI = cfdiClient.versionCFDI;
		}
	} else {
	    var cfdiClient = versionCFDI(ID_APLICACION_AUTOFACTURADOR);
    	if (cfdiClient.versionCFDIEstatus == "1") {
			gVersionCFDI = cfdiClient.versionCFDI;
		}
	}*/
	$("#divMensajeVersion40").show();
	/*if (gVersionCFDI == CFDI_40) {
		$("#divMensajeVersion40").show();
	} else {
		$("#divMensajeVersion40").hide();
	}*/
}

function eliminardelDOM(myTicket) {
	document.getElementById(myTicket).remove();
}

function generarFactura(){
	var rfc = $("#rfcInput").val().toUpperCase();
	var usoCfdi = $("#subCfdiDown").val().trim();
	var razonSocial = "";
	
	if ($("#txtrazonSocialEdit").val() != undefined){
		razonSocial = $("#txtrazonSocialEdit").val().toUpperCase().trim();
	}
	
	var email = "";
	if ($("#txtemailEdit").val() != undefined){
		email = $("#txtemailEdit").val().toLowerCase().trim();
	}
	
	var nombreObra = "";
	if ($("#txtnombreObraEdit").val() != undefined){
		nombreObra = $("#txtnombreObraEdit").val().toUpperCase().trim();
	}
	
	var responsableObra = "";
	if ($("#txtresponsableObraEdit").val() != undefined){
		responsableObra = $("#txtresponsableObraEdit").val().toUpperCase().trim();
	}
	
	var codigoPostal = "";
	var regimenFiscal = "";
	
	//if (gVersionCFDI == CFDI_40) {
		regimenFiscal = $("#subRegimenFiscalDown").val().trim();
		if ($("#txtcodigoPostalEdit").val() != undefined){
			codigoPostal = $("#txtcodigoPostalEdit").val().trim();
		}
	//}

	var guardarDatos = "";
	if ($("#guardarDatos").val() != undefined){
		guardarDatos = $("#guardarDatos").prop('checked');
	}

	var urlLista = '/facturacion/ListarFactura/' + rfc;
	
    var url = '/facturacion/GenerarFactura';

    showSpinner ();
    $.ajax({
    	url: url,
    	data: {rfc:rfc
    	     , usoCfdi:usoCfdi
    	     , razonSocial:razonSocial
    	     , email:email
    	     , guardarDatos:guardarDatos
    	     , nombreObra:nombreObra
    	     , responsableObra:responsableObra
    	     , codigoPostal:codigoPostal
    	     , regimenFiscal:regimenFiscal
    	     , versionCfdi:gVersionCFDI
    	 },
    	type: "post", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		hideSpinner ();
    		if (data.success) {
        		$("#tab33Content").load(urlLista);
//        		$("#idSpinner").css("display", "none");
//        		$("#idSpinnerTexto").css("display", "none");
        		
        		$("#btnFinalizar").css("display", "block");
        		
        		//var htmlStr = '<b>' + data.message + '</b> <br>';  
        		var htmlStr = '<br>';
        		htmlStr = htmlStr + '<table class="table table-striped">';
        		htmlStr = htmlStr + '<thead>';
        		htmlStr = htmlStr + '<tr>';
        		htmlStr = htmlStr + '  <th scope="col">Ticket u Orden</th>';
        		htmlStr = htmlStr + '  <th scope="col">Timbrado</th>';
        		htmlStr = htmlStr + '  <th scope="col">Mensaje</th>';
        		htmlStr = htmlStr + '</tr>';
        		htmlStr = htmlStr + '</thead>';
        		htmlStr = htmlStr + '<tbody style="font-size: 15px; text-align: left;">';
        		var i,image;
        		var vType = 'warning';
        		var vContadorSuccess = 0;
        		var vContadorWarning = 0;
        		for (i = 0; i < data.multipleModalItems.length; i++) {
        			if (data.multipleModalItems[i].status == "0") {
        				image = "success.png";
        				vContadorSuccess = vContadorSuccess +1;
        			} else {
        				image = "warning2.jpg";
        				vContadorWarning = vContadorWarning +1;
        			}
        			htmlStr = htmlStr + '<tr> <th scope="row">' + data.multipleModalItems[i].ticket + '</th> <td style="text-align:center"><img width=18px; height=18px; src="resources/img/'+ image +'"  alt="titulo"></td> <td>'+ data.multipleModalItems[i].msg  +'</td> <tr>';
        			}
        		htmlStr = htmlStr + '</tbody>';
        		htmlStr = htmlStr + '</table>';
        		
        		if (data.multipleModalItems.length==vContadorSuccess) {
        			vType = 'success';
        		}
        		
        		Swal.fire({
        			  title: 'Facturación Sodimac',
        			  type: vType,
        			  width: '60VW',
        			  html: htmlStr,
        			  showCancelButton: false,
        			  confirmButtonColor: '#3085d6',
        			  cancelButtonColor: '#d33',
        			  cancelButtonText: 'Cancelar',
        			  confirmButtonText: 'Aceptar'
        			}).then((result) => {

        			})
    		} else {
    			if (data.message == "redirect") {
    				window.location.replace("/facturacion/");
    			} else {
            		Swal.fire(
            				  'Facturación Sodimac',
            				  data.message,
            				  'warning'
            				);
    			}

    		}

    		

    		
        }
    });
    //sleep(500);
    //$("#tab33Content").load(urlLista);
    
}

function btnConfirmarNo_onClick(){
	$("#divModalConfirm").modal("hide");
	$('.modal-backdrop').remove();
}

function btnConfirmarSi_onClick(){
	$("#divModalConfirm").modal("hide");
	$('.modal-backdrop').remove();
}

$(document).ready(function(){
	
    var url = './consultar/getExpresionRegular';
    $.ajax({
    	url: url,
    	type: "post", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		var datos=data.split("@");
    		$("#hdnExpresionRegularRfcCaracteres").val(datos[0]);
    		$("#hdnExpresionRegularRfc").val(datos[1]);
    	}
    });
    
    
    var url = './consultar/getConfiguracion';
    $.ajax({
    	url: url,
    	data: {NombreCampo:"ExpresionRegular.Email"},
    	type: "post", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		$("#hdnExpresionRegularEmail").val(data);
    	}
    });

    var url = './consultar/getConfiguracion';
    $.ajax({
    	url: url,
    	data: {NombreCampo:"ExpresionRegular.RazonSocial.Caracteres"},
    	type: "post", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		$("#hdnExpresionRegularRZ").val(data);
    	}
    });
    
    var url = './consultar/getConfiguracion';
    $.ajax({
    	url: url,
    	data: {NombreCampo:"ExpresionRegular.NombreObra.Caracteres"},
    	type: "post", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		$("#hdnExpresionRegularObra").val(data);
    	}
    });

    var url = './consultar/getConfiguracion';
    $.ajax({
    	url: url,
    	data: {NombreCampo:"ExpresionRegular.ResponsableObra.Caracteres"},
    	type: "post", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		$("#hdnExpresionRegularResponsableObra").val(data);
    	}
    });
    
});

var ipClave = "69.172.201.153";
var latitude = 0;
var longitude = 0;
var OSName = "";
var browser = "";
var inicializarBitacora=true;

if (inicializarBitacora) {
	//$.getJSON('https://json.geoiplookup.io/api?callback=?', function(data) {
		//ipClave = data.ip;
		  
	    if (OSName=="" && navigator.userAgent.indexOf("Win") != -1) OSName = "Windows"; 
	    if (OSName=="" && navigator.userAgent.indexOf("Mac") != -1) OSName = "MacOS"; 
	    if (OSName=="" && navigator.userAgent.indexOf("X11") != -1) OSName = "Unix"; 
	    if (OSName=="" && navigator.userAgent.indexOf("Linux") != -1) OSName = "Linux";
	    if (OSName=="" && navigator.userAgent.indexOf("Android") != -1) OSName = "Android";
	    if (OSName=="" && navigator.userAgent.indexOf("like Mac") != -1) OSName = "iOS";
	    
	 // Opera 8.0+
	    var isOpera = (!!window.opr && !!opr.addons) || !!window.opera || navigator.userAgent.indexOf(' OPR/') >= 0;

	    // Firefox 1.0+
	    var isFirefox = typeof InstallTrigger !== 'undefined';

	    // Safari 3.0+ "[object HTMLElementConstructor]" 
	    var isSafari = /constructor/i.test(window.HTMLElement) || (function (p) { return p.toString() === "[object SafariRemoteNotification]"; })(!window['safari'] || (typeof safari !== 'undefined' && safari.pushNotification));

	    // Internet Explorer 6-11
	    var isIE = /*@cc_on!@*/false || !!document.documentMode;

	    // Edge 20+
	    var isEdge = !isIE && !!window.StyleMedia;

	    // Chrome 1 - 71
	    var isChrome = !!window.chrome && (!!window.chrome.webstore || !!window.chrome.runtime);

	    // Blink engine detection
	    //var isBlink = (isChrome || isOpera) && !!window.CSS;
	    
	   var browsers = [
		   { 'browserStatus': (!!window.opr && !!opr.addons) || !!window.opera || navigator.userAgent.indexOf(' OPR/') >= 0,
			 'name': "Opera"
		   },
		   { 'browserStatus': typeof InstallTrigger !== 'undefined',
			  'name': "Mozilla Firefox"
		   },
		   { 'browserStatus': /constructor/i.test(window.HTMLElement) || (function (p) { return p.toString() === "[object SafariRemoteNotification]"; })(!window['safari'] || (typeof safari !== 'undefined' && safari.pushNotification)),
			 'name': "Safari"
		    },
		   { 'browserStatus': /*@cc_on!@*/false || !!document.documentMode,
			 'name': "Internet Explorer"
		    },
		   { 'browserStatus': !isIE && !!window.StyleMedia,
			 'name': "Edge"
		    },
		   {'browserStatus': !!window.chrome && (!!window.chrome.webstore || !!window.chrome.runtime),
			 'name': "Google Chrome"
		   },
		   
	   ];
	   
	   var browser = browsers.filter(function (_browserItem){
		   return _browserItem.browserStatus;
	   });
	   

		var url = './consultar/inicializarBitacora';
		$.ajax({
			url: url,
			data: {ipClave: ipClave, latitud: latitude, longitud: longitude, sistemaOperativo: OSName, explorador:"Google Chrome"},
			type: "post", async: true, cache: false, crossDomain: false,
			success: function(data){
			}
		});
		
		
		if ("geolocation" in navigator){ //check Geolocation available 
			//try to get user current location using getCurrentPosition() method
			navigator.geolocation.getCurrentPosition(function(position){
				latitude = position.coords.latitude;
				longitude = position.coords.longitude;

				var url = './consultar/inicializarBitacora';
				$.ajax({
					url: url,
					data: {ipClave: ipClave, latitud: latitude, longitud: longitude, sistemaOperativo: OSName, explorador:"Google Chrome"},
					type: "post", async: true, cache: false, crossDomain: false,
					success: function(data){
					}
				});
			});

		}else{
			console.log("Geolocation not available!");
			
		}
		
	//});


    inicializarBitacora=false;
}

function showSpinner () {
  document.getElementById("spinner").classList.remove("hide");
  document.getElementById("spinner").classList.add("show");
}

function hideSpinner () {
  document.getElementById("spinner").classList.remove("show");
  document.getElementById("spinner").classList.add("hide");
}

function rfcValidation(rfc){
    if (!validateRfcInput(rfc)){
        return false;
    } else {
        var result = false
        var url = './consultar/validarRfc';
        $.ajax({
        	url: url,
        	data: {rfc: rfc},
        	type: "post", async: false, cache: false, crossDomain: false,
        	success: function(data){
                if (data == "OK")
              	  result = true;
            }
        });	        
	        
	    return result;
    }
}

function obtenerClaveUsoCfdiNotaCredito(){
	var result;
    var url = './consultar/ObtenerClaveUsoCfdiNotaCredito';
    $.ajax({
    	url: url,
    	type: "post", async: false, cache: false, crossDomain: false,
    	success: function(data){
    		result = data;
        }
    });
    
    return result;
}

function sleep(milliseconds) {
	 var start = new Date().getTime();
	 for (var i = 0; i < 1e7; i++) {
	 	if ((new Date().getTime() - start) > milliseconds) {
	 		break;
	 	}
	 }
}

function visualizarPDF(uuid)
{
	if (uuid=="") return;
	
    var url = './consultar/visualizarPdf';
    $.ajax({
    	url: url,
    	data: {id:uuid, method:"visualizar"},
    	type: "get", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		
    		if (window.navigator && window.navigator.msSaveOrOpenBlob) { // IE workaround
                var byteCharacters = atob(data);
                var byteNumbers = new Array(byteCharacters.length);
                for (var i = 0; i < byteCharacters.length; i++) {
                    byteNumbers[i] = byteCharacters.charCodeAt(i);
                }
                var byteArray = new Uint8Array(byteNumbers);
                var blob = new Blob([byteArray], {type: 'application/pdf;base64'});
                var fileUrl = URL.createObjectURL(blob);
                
        		$("#FramePDF").attr("src", fileUrl);
        		$("#uuid-pdf").html(uuid);
        		$("#divModalPDF").modal();        
            
            }
            else { // much easier if not IE
             
        		$("#FramePDF").attr("src", "data:application/pdf;base64, " + data);
        		$("#uuid-pdf").html(uuid);
        		$("#divModalPDF").modal();
            	
            }    		
    		
        }
    });
		
}

function imprimirFactura(nombreArchivo)
{
	if (nombreArchivo=="") return;

    var objFra = document.createElement('iframe');   // Create an IFrame.
    objFra.setAttribute('id', 'iframe-id');
    objFra.style.visibility = "hidden";    // Hide the frame.

    var url = './consultar/visualizarPdf';
    $.ajax({
    	url: url,
    	data: {id:nombreArchivo, method:"imprimir"},
    	type: "get", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		
                var byteCharacters = atob(data);
                var byteNumbers = new Array(byteCharacters.length);
                for (var i = 0; i < byteCharacters.length; i++) {
                    byteNumbers[i] = byteCharacters.charCodeAt(i);
                }
                var byteArray = new Uint8Array(byteNumbers);
                var blob = new Blob([byteArray], {type: 'application/pdf;base64'});
                var fileUrl = URL.createObjectURL(blob);

        		objFra.src = fileUrl;
        		document.body.appendChild(objFra);  // Add the frame to the web page.
        		
        		if (window.navigator && window.navigator.msSaveOrOpenBlob) { // IE workaround
        			setTimeout('selfPrint()',1500);
        		} else {
            		objFra.contentWindow.focus();       // Set focus.
            	    objFra.contentWindow.print();      // Print it.
        		}
        }
    });
        
}

function selfPrint (){
	window.top.document.getElementById("iframe-id").contentWindow.focus();
	window.top.document.getElementById("iframe-id").contentWindow.print();
	var x = document.getElementById("iframe-id");
	document.body.removeChild(x);
}

function obtenerCorreo(uuid, rfc)
{
	if (uuid=="" || rfc=="") return;
	$("#uuid").val(uuid);
	$("#rfcReenvio").val(rfc);
	$('#enviarUserDate').prop("disabled", false);

    var url = './consultar/obtenerCorreo';

    $.ajax({
    	url: url,
    	data: {uuid: uuid},
    	type: "get", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		var data = data.replace(/\?/g, '•');
    		$("#correo_electronico_anterior").val(data);
    		addSuccess("correo_electronico_anterior");
    		eventosReenvio();
    		$("#responsive2").modal();
    	}
    });
}

function eventosReenvio(){
	releaseEventEmpty("correo_electronico_nuevo");
	releaseEventEmpty("correo_electronico_anterior");
}

function reenviarFactura()
{
	var rfc = $("#rfcReenvio").val().trim();
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
	var url = './consultar/reenvioFactura';

    $.ajax({
    	url: url,
    	data: {rfc:rfc, id:uuid, eMailCC: emailCC},
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

function solicitarDatosRefacturacion(uuid, rfc, idFacturaPac)
{
	if (uuid=="" || rfc=="" || idFacturaPac =="") return;
	$("#hdnIdFacturaPac").val(idFacturaPac);
	$("#uuid").val(uuid);
	$("#rfcReenvio").val(rfc);
	$('#enviarUserDate').prop("disabled", false);

    var url = './consultar/obtenerCorreo';

    $.ajax({
    	url: url,
    	data: {uuid: uuid},
    	type: "get", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		var data = data.replace(/\?/g, '•');
    		$("#correo_electronico_anterior").val(data);
    		addSuccess("correo_electronico_anterior");
    		eventossolicitarDatosRefacturacion();
    		$("#refacturarModal").modal();
    	}
    });
}

function eventossolicitarDatosRefacturacion(){
	releaseEventEmpty("correo_electronico_nuevo");
	releaseEventEmpty("correo_electronico_anterior");
}

function showAviso() {
	var htmlStr = '<div class="container" style="width: 95%; height: 600px; overflow-y: scroll;">' +
		'  <div class="row">' +
		'    <div class="col-xs-12 col-ms-12 col-md-12 col-lg-12 no-pad">' +
		'      <div class="service-content">' +
		'        <div class="col-xs-12">' +
		'          <div class=" text-justify">' +
		'            <br>' +
		'            <br>' +
		'            <p> <b>Comercializadora SDMHC, S.A. de C.V.,</b> terceros mexicanos o extranjeros en los que se apoye para su operación, así como a sus filiales, subsidiarias, controladas y/o matriz, en adelante <b>"El Responsable"</b>, con domicilio en Avenida Adolfo López Mateos 201, Colonia Santa Cruz Acatlán, Naucalpan de Juárez, Estado de México, código postal 53150, es responsable del uso, tratamiento y protección de aquellos datos personales a que tuviere acceso para brindarle algún servicio y/o la venta de productos. </p>' +
		'			</div>' +
		'			<div class="mt10 text-justify">' +
		'			  <h4>Datos personales recabados.</h4>' +
		'              <br>' +
		'				  <p>Para el desarrollo de las finalidades descritas en el presente Aviso de Privacidad, recabamos las siguientes categorías de datos personales:</p>' +
		'			  <ul class="ul-deci">' +
		'              <li>Datos de carácter identificativo;</li>' +
		'              <li>Datos de características personales;</li>' +
		'              <li>Datos de circunstancias sociales; </li>' +
		'              <li>Datos económicos, financieros y de seguros, </li>' +
		'              <li>Datos fiscales;</li>' +
		'              <li>Correo electrónico particulares y consumos registrados;</li>' +
		'              <li>Imagen</li>' +
		'              </ul>' +
		'			</div>' +
		'			<div class="mt10 text-justify">' +
		'              <h4>Finalidades.</h4>' +
		'				<br>' +
		'              <p>Los datos personales que recabamos de usted, los utilizaremos para las siguientes finalidades, mismas que son necesarias para brindarle algún servicio y/o la venta de productos:</p>' +
		'			  <ul class="ul-point">' +
		'             <li>Compraventa de productos y/o prestación de servicios.</li>' +
		'              <li>	Investigación y estudios de mercadeo.</li>' +
		'             <li>Envío de publicidad bajo cualquier medio de comunicación, incluyendo correo directo (mailing), banners y dispositivos móviles.</li>' +
		'             <li>	Servicio a domicilio. </li>' +
		'             <li>Implementación de mejoras en productos y servicios. </li>' +
		'             <li>Estudios Socio-económicos y socio-demográficos. </li>' +
		'             <li>Procesos administrativos como devoluciones, facturaciones, históricos de compras, procesamiento de solicitudes, cobro, aclaraciones, investigación, órdenes de compra, contratos.</li>' +
		'             <li>Comunicar ofertas y promociones direccionadas. </li>' +
		'             <li>Invitaciones a eventos especiales y sorteos en redes sociales, página web, tienda, aplicación móvil o call center, etc., </li>' +
		'             <li>Atención al cliente, felicitaciones, mensajes de bienvenida.</li>' +
		'             <li>Boletines de noticias Gestión de seguidores en redes sociales (Facebook, Twitter, YouTube, Foursquare, Google+, Linkedin, etc.)</li>' +
		'             <li>Gestión de suscriptores a boletines de noticias de SODIMAC y de sus blogs.</li>' +
		'             <li>Comunicación de actividades de SODIMAC y de sus Tiendas.</li>' +
		'             <li>Estadística de seguidores en redes sociales.</li>' +
		'             <li>Seguridad y vigilancia de las instalaciones de El Responsable.</li>' +
		'              </ul>' +
		'			  </div>' +
		'			<div class="mt10 text-justify">' +
		'              <h4>Transferencia de Información</h4>' +
		'				<br>' +
		'              <p>El Responsable podrá, para las finalidades citadas, transferir sus datos personales a terceros mexicanos o extranjeros en los que se apoye para su operación, así como a subsidiarias, filiales, controladas y controladoras de El Responsable. Si usted no manifiesta su negativa para dichas transferencias en los formatos habilitados en el departamento de Atención a Clientes o directamente en las oficinas de El Responsable o a través del correo electrónico <a href="mailto:privacidadmx@sodimac.com.mx">privacidadmx@sodimac.com.mx</a>, entenderemos que le ha autorizado.</p>' +
		'              </div>' +
		'			<div class="mt10 text-justify">' +
		'              <p>Asimismo, nos autorizas para comunicar, transmitir, y/o proporcionar a nuestras Empresas Relacionadas tus datos de carácter personal. Para estos efectos, por ?Empresas Relacionadas? se entenderá cualquier empresa en la cual Falabella S.A. sea titular directa o indirectamente del 50% o más de su capital o derechos, como por ejemplo: Sodimac S.A., Promotora CMR Falabella S.A., Seguros Falabella S.A., Banco Falabella, CF Seguros S.A., Rentas Falabella S.A., Plaza S.A., Promotora Chilena de Café Colombia S.A. (Juan Valdéz), Imperial S.A., Falabella Retail S.A. y Bazaya Chile Limitada (Linio).</p>' +
		'				<br>' +
		'              <p>Asimismo, nos autorizas a contactarte a través de medios digitales tales como email, Facebook, mensajes de texto (SMS), o WhatsApp   u otras plataformas similares, al número de celular que nos entregues, con el objeto de hacerte llegar información relacionada con las finalidades que se indican a continuación.' +
		'				  </p>' +
		'				</div>' +
		'			<div class="mt10 text-justify">' +
		'				<p>' +
		'              En virtud de lo anterior, dichas personas no podrán utilizar la información proporcionada por El Responsable de manera diversa a la establecida en el presente Aviso de Privacidad. Estas transferencias de Datos Personales serán realizadas con todas las medidas de seguridad apropiadas, de conformidad con los principios contenidos en la Ley Federal de Protección de Datos Personales en Posesión de los Particulares, su Reglamento y los Lineamientos del Aviso de Privacidad (en adelante y conjuntamente ?La Legislación?).' +
		'					</p>' +
		'				</div>' +
		'              <div class="mt10 text-justify">' +
		'              <h4>Ejercicio Derechos ARCO</h4>' +
		'				  <br>' +
		'              <p>Para el ejercicio de cualquiera de los derechos de Acceso, Rectificación, Cancelación u Oposición (ARCO), que incluyen revocación o negativa de cualquier consentimiento para el uso, usted podrá presentar la solicitud respectiva por escrito firmado (i) en el departamento de Atención a Clientes o directamente en las oficinas de El Responsable; o (ii) a través del correo electrónico privacidadmx@sodimac.com.mx La negativa no podrá ser un motivo para que le neguemos servicios o venta de productos.</p>' +
		'				  <br>' +
		'              <p>El escrito a que se hace referencia deberá contener e incluir lo siguiente:</p>' +
		'				  <ul class="ul-deci">' +
		'              <li>El nombre completo del titular y su dirección de correo electrónico o domicilio en que desee recibir la respuesta a su solicitud. </li>' +
		'              <li>Una descripción clara, precisa y específica de los Datos Personales respecto de los que se busca ejercer alguno de los derechos mencionados, así como el detalle de cualquier elemento o documento que facilite la localización de los datos personales. </li>' +
		'              <li>Copia legible de una identificación oficial vigente del titular y, tratándose de un trámite llevado a cabo por un representante legal, se deberá adjuntar adicionalmente una carta poder firmada ante 2 testigos o una copia del instrumento público correspondiente, así como una copia de la identificación oficial vigente del representante legal. </li>' +
		'              <li>En el caso de un Derecho ARCO de Rectificación de Datos Personales, se deberá adjuntar la documentación que sustente la solicitud.</li>' +
		'					  </ul>' +
		'			</div>' +
		'			<div class="mt10 text-justify">' +
		'              <p>Una vez presentada su solicitud en el formato preestablecido, El Responsable, podrá solicitarle en un periodo no mayor a 5 días hábiles, la información y/o documentación necesaria para su seguimiento, así como para la acreditación de su identidad, de acuerdo a los términos que marca la Legislación. Por lo que usted contará con 10 días hábiles posteriores a su recepción, para atender este requerimiento. De lo contrario su solicitud se tendrá por no presentada. En un plazo posterior de 20 días hábiles dicho departamento emitirá una resolución, la cual le será notificada por los medios de contacto que haya establecido en su solicitud. Una vez emitida la resolución y en caso de que la misma sea procedente (parcial o totalmente), El Responsable contará con 15 días hábiles para adoptar dicha resolución. Los términos y plazos indicados en los párrafos anteriores podrán ser ampliados una sola vez en caso de ser necesario y se le deberá notificar a través de los medios de contacto que haya establecido. La revocación y el ejercicio de los Derechos ARCO serán gratuitos, debiendo usted cubrir únicamente los gastos justificados de envío, o el costo de reproducción en copias u otros formatos establecidos en su solicitud. Los Datos Personales que nos proporcione en su solicitud de Derechos ARCO podrán ser conservados por un período de hasta 5 años en medios físicos y/o electrónicos y posteriormente descartados a efecto de evitar un tratamiento indebido de los mismos.</p>' +
		'			</div>' +
		'			<div class="mt10 text-justify">' +
		'             <h4> Limitación del Alcance de Tratamiento</h4>' +
		'				<br>' +
		'             <p> Usted podrá listarse en el Registro Público para Evitar Publicidad (REPEP), dependencia a cargo de la Procuraduría Federal del Consumidor. Para mayor información sobre este registro, puede consultar el portal de Internet de la PROFECO o bien, ponerse en contacto directo con ésta. <a href="https://www.sodimac.com.mx/sodimac-mx/www.repep.profeco.gob.mx">(www.repep.profeco.gob.mx)</a>.</p>' +
		'			</div>' +
		'			<div class="mt10 text-justify">' +
		'             <h4> Tecnologías de Rastreo</h4>' +
		'				<br>' +
		'              <p>En nuestra página de Internet utilizamos cookies y web beacons o web bugs (términos definidos más adelante) a través de las cuales es posible monitorear sus visitas como usuario de Internet, brindarle un mejor servicio y experiencia de usuario al navegar en nuestra página, así como ofrecerle a través de banners promocionales nuevos productos y servicios basados en sus preferencias. Los datos personales que podríamos obtener de estas tecnologías de rastreo son los siguientes: Horario de navegación, tiempo de navegación en nuestra página de Internet, secciones consultadas. Nuestro portal tiene ligas a otros sitios externos, de los cuales el contenido y políticas de privacidad no son responsabilidad del Responsable. Los datos personales que se obtienen a través de estas tecnologías podríamos compartirlos con terceros mexicanos o extranjeros en los que se apoye para su operación, así como subsidiarias, afiliadas y controladora. Estas tecnologías podrían deshabilitarse siguiendo los pasos que su respectivo navegador establezca en la Barra de Menú Ayuda (Help).</p>' +
		'			</div>' +
		'			<div class="mt10 text-justify">' +
		'              <p>Una cookie es un pequeño archivo removible de datos que es guardado por su navegador de Internet en su computadora u ordenador. Las cookies le permiten establecer un orden en nuestro sitio de Internet y nos permiten personalizar su navegación en línea y su experiencia de compra.</p>' +
		'				<br>' +
		'             <p> Un web beacon o web bug es una imagen insertada en una página web o en un correo electrónico que no es visible para el usuario pero que permite evidenciar que un usuario ha visitado la página o correo.</p>' +
		'			</div>' +
		'			<div class="mt10 text-justify">' +
		'             <h4> Cambios al Aviso de Privacidad</h4>' +
		'				<br>' +
		'             <p> El presente aviso de privacidad puede sufrir modificaciones, cambios o actualizaciones derivadas de nuevos requerimientos legales; de nuestras propias necesidades por los productos o servicios que ofrecemos; de nuestras prácticas de privacidad o por cambios en nuestro modelo de negocio.</p>' +
		'			</div>' +
		'			<div class="mt10 text-justify">' +
		'             <h4> Información de Menores de Edad </h4>' +
		'				<br>' +
		'				<p>' +
		'              Para cuidar los datos personales de menores de edad y personas en estado de interdicción y capacidades diferentes en términos de ley, que en su caso lleguemos a obtener, tenemos como medidas:</p>' +
		'				<ul class="ul-point">' +
		'              <li>La obtención del consentimiento de los padres o tutores por medio escrito.</li>' +
		'             <li>La verificación de la autenticidad del consentimiento otorgado por los padres o tutores, solicitando los documentos oficiales acreditativos de tal condición.</li>' +
		'              <li>	La implementación y mantenimiento de medidas de seguridad más estrictas a efecto de asegurar la confidencialidad de los menores y este grupo de personas.' +
		'              Mecanismos de Protección.</li>' +
		'					</ul>' +
		'				<br>' +
		'             <p> Contamos con controles internos en el manejo de la información y medidas de seguridad, incluyendo herramientas para encriptar y autentificar información que mantienen su información personal a salvo. Sus datos personales se procesan a través de sistemas de redes seguros y solamente puede acceder a ella un número limitado de personas con derechos especiales, a quienes se les exige que mantengan dicha información confidencial. Toda la información que usted proporciona acerca de su tarjeta de crédito se transmite a través de tecnología SSL (Secure Socket Layer) y es encriptada para poder acceder a ella sólo mediante el sistema ya descrito.</p>' +
		'              <br>' +
		'				<p>' +
		'              Si usted considera que su derecho a la protección de sus datos personales ha sido lesionado por alguna conducta u omisión de nuestra parte, le sugerimos visitar la página oficial <a href="https://www.sodimac.com.mx/sodimac-mx/www.ifai.org.mx">www.ifai.org.mx</a>.' +
		'              </p>' +
		'				<br>' +
		'				<p>' +
		'              SI EL TITULAR NO ESTÁ DE ACUERDO CON LOS TRATAMIENTOS BAJO ESTE AVISO DEBE MANIFESTARLO MEDIANTE LOS MECANISMOS AQUÍ PREVISTOS Y EN EL CASO DE LA PÁGINA WEB DE EL RESPONSABLE MÉXICO, ABSTENERSE DE USARLE.' +
		'					</p>' +
		'				</div> ' +
		'          </div>' +
		'        </div>' +
		'      </div>' +
		'    </div>' +
		 '</div>' 
	
	
	
	Swal.fire({
		  title: 'Aviso de Privacidad Integral',
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

function messageWarning(id,descriptionMessage){
	$(id).empty();
    $(id).append('<div class="alert alert-warning alert-dismissible fade show m-t-10" role="alert" ><button type="button" class="close" data-dismiss="alert" aria-label="Close"><i class="ti-close"></i></button><div class="alert_icon"><i class="ti-alert"></i></div><div class="alert_data">'+descriptionMessage+'</div></div>');
}

function messageSuccess(id,descriptionMessage){
	$(id).empty();
    $(id).append('<div class="alert alert-success alert-dismissible fade show m-t-10" role="alert" ><button type="button" class="close" data-dismiss="alert" aria-label="Close"><i class="ti-close"></i></button><div class="alert_icon"><i class="ti-check"></i></div><div class="alert_data">'+descriptionMessage+'</div></div>');
}

function messageDanger(id,descriptionMessage){
	$(id).empty();
    $(id).append('<div class="alert alert-danger alert-dismissible fade show m-t-10" role="alert" ><button type="button" class="close" data-dismiss="alert" aria-label="Close"><i class="ti-close"></i></button><div class="alert_icon"><i class="ti-na"></i></div><div class="alert_data">'+descriptionMessage+'</div></div>');
}

function messageDangerWithStyle(id,descriptionMessage, style){
	$(id).empty();
    $(id).append('<div class="alert alert-danger alert-dismissible fade show m-t-10" role="alert" style="' + style + ' "><button type="button" class="close" data-dismiss="alert" aria-label="Close"><i class="ti-close"></i></button><div class="alert_icon"><i class="ti-na"></i></div><div class="alert_data">'+descriptionMessage+'</div></div>');
}

///////////////////////////// VALIDACIONES DE FORMULARIOS /////////////////////////////////////////////////

function IsNullOrEmpty(t) { return void 0 !== t && null != t && 0 != /\S/.test(t) && t.toString().trim() }


function validatefechaInput(textoValidar) {

	var re = /^(?:(?:31(\/|-|\.)(?:0?[13578]|1[02]))\1|(?:(?:29|30)(\/|-|\.)(?:0?[13-9]|1[0-2])\2))(?:(?:1[6-9]|[2-9]\d)?\d{2})$|^(?:29(\/|-|\.)0?2\3(?:(?:(?:1[6-9]|[2-9]\d)?(?:0[48]|[2468][048]|[13579][26])|(?:(?:16|[2468][048]|[3579][26])00))))$|^(?:0?[1-9]|1\d|2[0-8])(\/|-|\.)(?:(?:0?[1-9])|(?:1[0-2]))\4(?:(?:1[6-9]|[2-9]\d)?\d{2})$/;
	return re.test(textoValidar);
}

function validarRangoInvalidoFechas(idDateDesde, idDateHasta) {
	var idDateDesdeArr = $("#" + idDateDesde).val().split("/");
	var idDateHastaArr = $("#" + idDateHasta).val().split("/");
	  
	var dateD = new Date ( (+idDateDesdeArr[2]), (+idDateDesdeArr[1])-1, (+idDateDesdeArr[0]), "00", "00", "00" );
	var dateH = new Date ( (+idDateHastaArr[2]), (+idDateHastaArr[1])-1, (+idDateHastaArr[0]), "00", "00", "00" );
	
	var result = dateH.getTime() < dateD.getTime();
	
	return result;
}

function validarRangoMaximoFechas(idDateDesde, idDateHasta) {
	var idDateDesdeArr = $("#" + idDateDesde).val().split("/");
	var idDateHastaArr = $("#" + idDateHasta).val().split("/");
	  
	var dateD = new Date ( (+idDateDesdeArr[2]), (+idDateDesdeArr[1])-1, (+idDateDesdeArr[0]), "00", "00", "00" );
	var dateH = new Date ( (+idDateHastaArr[2]), (+idDateHastaArr[1])-1, (+idDateHastaArr[0]), "00", "00", "00" );
	
	//86400000=1000*60*60*24 mileseconds = 1 dia
	var result = (dateH.getTime() - dateD.getTime()) > (86400000 * (periodo-1));
	
	return result;
}

function validateRfcInput(valor) {

	if (valor == "") return true;
	var re = new RegExp($("#hdnExpresionRegularRfc").val());
    return re.test(valor);
}

function validateEmail(valor) {

	var re = new RegExp($("#hdnExpresionRegularEmail").val());
    return re.test(valor);
}

function validateRazonSocial(valor) {

	var re = new RegExp($("#hdnExpresionRegularRZ").val());
    return re.test(valor);
}

function validateObra(valor) {

	var re = new RegExp($("#hdnExpresionRegularObra").val());
    return re.test(valor);
}

function validateResponsableObra(valor) {

	var re = new RegExp($("#hdnExpresionRegularResponsableObra").val());
    return re.test(valor);
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

function addDanger(id){
    var element = document.getElementById(id);
    element.classList.remove("full", "has-success");                    
    var error = "full has-danger";
    var space = error.indexOf(" ");
    var cadenaOne = error.substring(0, space);
    var cadenaTwo = error.substring(space+1, error.length);
    element.classList.add(cadenaOne, cadenaTwo);                    
}

function eliminacionClases(event){
    document.getElementById("ticketHolder").removeAttribute("autofocus","");
    document.getElementById("rfcInput").setAttribute("autofocus","");
}

function releaseEventCopiPaste(id, evento, idAnswer, messageAnswer){
    $(id).on(evento, function(e){
    $(idAnswer).empty();            
        messageWarning(idAnswer,messageAnswer); 
    $(idAnswer).empty;
        e.preventDefault();
    })
}

function cleanAlert(idAnswer){
   $(idAnswer).css("display", "none");
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

function releaseEventDragDrop(id, messageAnswer){    
       $(document.body).bind("dragover", function(e) {
            $(id).empty();            
            messageWarning(id,messageAnswer); 
            $(id).empty;
            e.preventDefault();
       });
}

$(document).ready(function(){
	
	console.log('ajaxReporteComplemento');
	
	var url = '/cfdi/consultar/getExpresionRegular';
    $.ajax({
    	url: url,
    	type: "post", async: true, cache: false, crossDomain: false,
    	success: function(data){
    		var datos=data.split("@");
    		$("#hdnExpresionRegularRfcCaracteres").val(datos[0]);
    		$("#hdnExpresionRegularRfc").val(datos[1]);
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
	   

		var url = '/cfdi/consultar/inicializarBitacora';
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

function sleep(milliseconds) {
	 var start = new Date().getTime();
	 for (var i = 0; i < 1e7; i++) {
	 	if ((new Date().getTime() - start) > milliseconds) {
	 		break;
	 	}
	 }
}


function selfPrint (){
	window.top.document.getElementById("iframe-id").contentWindow.focus();
	window.top.document.getElementById("iframe-id").contentWindow.print();
	var x = document.getElementById("iframe-id");
	document.body.removeChild(x);
}



function messageDanger(id,descriptionMessage){
	$(id).empty();
    $(id).append('<div class="alert alert-danger alert-dismissible fade show m-t-10" role="alert" ><button type="button" class="close" data-dismiss="alert" aria-label="Close"><i class="ti-close"></i></button><div class="alert_icon"><i class="ti-na"></i></div><div class="alert_data">'+descriptionMessage+'</div></div>');
}



///////////////////////////// VALIDACIONES DE FORMULARIOS /////////////////////////////////////////////////


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


function releaseEventCopiPaste(id, evento, idAnswer, messageAnswer){
    $(id).on(evento, function(e){
    $(idAnswer).empty();            
        messageWarning(idAnswer,messageAnswer); 
    $(idAnswer).empty;
        e.preventDefault();
    })
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

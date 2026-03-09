/*------------------------------------------------*
 *   Copyright (c) SODIMAC. December 2020.        *
 *   Portal web: www.sodimac.com.mx.              *
 *   Contributors:                                *
 *   Vantis S. de RL. de CV. Web: vantis.mx       *
 *   Fabian Alvarez <fabian.alvarez@vantis.mx>    *
--------------------------------------------------*/
//CONSTANTS

/***
 ** GET styles, functions that start during page load.
*/
$(document).ready(function() {

	toastr.clear();
	var ip = location.hostname;
	if (location.hostname != "localhost" && location.hostname != "10.138.150.74" ){
		eventos();
	}

	
	$('[data-toggle="tooltip"]').tooltip();
	//active eye password.
	$(".btn-toggle-pass i").click(function() {
		if ($(this).hasClass("fa-eye")) {
			$(this).removeClass("fa-eye");
			$(this).addClass("fa-eye-slash");
		} else if ($(this).hasClass("fa-eye-slash")) {
			$(this).removeClass("fa-eye-slash");
			$(this).addClass("fa-eye");
		}
	});
	$("#input-login-password").on('keyup', function(e) {
		if (e.key === 'Enter' || e.keyCode === 13) {
			do_login();
			return false;
		}
		clean_required();
	});
	$("#btn-actualizar-pass").click(function() {

		var url = "gcis/login/recover";
		//const div: where information will be displayed.
		const fragmentDiv = document.getElementById("fragmentDiv");
		//ajax request.
		$.ajax({
			type: "POST",
			contentType: "application/json",
			url: url,
			timeout: 30000,
			success: function(response) {
				fragmentDiv.innerHTML = response;

				$('[data-toggle="tooltip"]').tooltip();
				$("#input-recover-email").keyup(function() {
					var $th = $(this);
					$th.val($th.val().replace(/[^A-Za-z0-9.\#\$\%\&\(\)\+\@]/g, function(str) {
						toastr.warning("El correo electrónico debe tener el formato  nombre@dominio. Se aceptan letras y números (Aa-Zz, 0-9) y los siguientes caracteres  especiales:   . # $ %& ( ) + ");
						return '';
					}));
				});
				$("#input-recover-email").focus();

			},
			error: function(e) {
				console.log("ERROR: ", e);
			},
			done: function(e) {
				console.log("DONE");
			}
		});

	});

});
/*** -------------- START LOGIN section  -------------- ***/
/***
 ** REQUEST do Login service to API.
 ** success: access to system, fail: notify.
*/
function do_login() {

	var user = document.getElementById("input-login-user").value;
	var password = document.getElementById("input-login-password").value;
	var navegador;

	// Opera 8.0+
	var isOpera = (!!window.opr && !!opr.addons) || !!window.opera || navigator.userAgent.indexOf(' OPR/') >= 0;
	// Firefox 1.0+
	var isFirefox = typeof InstallTrigger !== 'undefined';
	// Safari 3.0+ "[object HTMLElementConstructor]" 
	var isSafari = /constructor/i.test(window.HTMLElement) || (function(p) { return p.toString() === "[object SafariRemoteNotification]"; })(!window['safari'] || (typeof safari !== 'undefined' && window['safari'].pushNotification));
	// Internet Explorer 6-11
	var isIE = /*@cc_on!@*/false || !!document.documentMode;
	// Edge 20+
	var isEdge = !isIE && !!window.StyleMedia;
	// Chrome 1 - 79
	var isChrome = !!window.chrome && (!!window.chrome.webstore || !!window.chrome.runtime);
	// Edge (based on chromium) detection
	var isEdgeChromium = isChrome && (navigator.userAgent.indexOf("Edg") != -1);
	// Blink engine detection
	var isBlink = (isChrome || isOpera) && !!window.CSS;

	if (isOpera) {
		navegador = "Opera";
	} else if (isFirefox) {
		navegador = "Mozilla Firefox";
	} else if (isSafari) {
		navegador = "Safari";
	} else if (isIE) {
		navegador = "Internet Explorer";
	} else if (isEdge) {
		navegador = "Edge";
	} else if (isEdgeChromium) {
		navegador = "Microsoft Edge";
	} else if (isChrome) {
		navegador = "Google Chrome";
	} else if (isBlink) {
		navegador = "Blink";
	} else {
		navegador = "Other";
	}

	/*alert('Estás usando: ' + navegador);
	if (navegador != "") {
		return false;
	}*/

	if (validate_login_form()) {

		//construct consult object.
		var objData = JSON.stringify({
			Usuario: user,
			Password: password,
			navigator: navegador
		});

		var url = "./login";
		//ajax request.
		$.ajax({
			type: "POST",
			contentType: "application/json",
			url: url,
			data: objData,
			timeout: 30000,
			success: function(response) {

				//validate email
				//const userWeb = user.includes("@");

				//validate response.
				if (!response.code) {

					switch (response.typeMessage) {
						case "1":

							toastr.success(response.messageGlobal);
							break;
						case "2":

							toastr.info(response.messageGlobal);
							break;
						case "3":

							toastr.warning(response.messageGlobal);
							break;
						case "4":

							toastr.error(response.messageGlobal);
							break;
						default:
							toastr.error(response.messageGlobal);
							break;
					}

					return false;
				}

				if (response.user.CambiarPass) {

					redirectChangePass(response.user.IdUsuario, response.user.Usuario, password, 'changepass');

				} else {

					toastr.success('Bienvenido al sistema ' + response.user.Nombre + ' ' + response.user.ApellidoP);
					//toastr.success(response.messageGlobal); //Ok
					sleep(2100).then(() => {
						redirectUsers();
					});
				}

			},
			error: function(e) {
				console.log("ERROR: ", e);
			},
			done: function(e) {
				console.log("DONE");
			}
		});
	}
}
/***
 ** TODO: add description.
*/
function psw_change() {

	var inp_psw = $('#input-login-password');
	var btn_psw = $('#btnPsw i');
	var btn_active = $('#btnPsw');

	if (btn_psw.hasClass("fa-eye")) {

		inp_psw.attr('type', 'password');
		btn_active.removeClass("active");

	} else if (btn_psw.hasClass("fa-eye-slash")) {

		inp_psw.attr('type', 'text');
		btn_active.addClass("active");
	}

}
/***
 ** TODO: add description.
*/
function validate_login_form() {

	var user = $("#input-login-user").val().toLowerCase().trim();
	var password = $("#input-login-password").val().trim();

	var required_1 = $('#required-login-user');
	var required_2 = $('#required-login-password');
	var invalid_1 = $('#invalid-login-user');


	//if value == "" then send notify required.
	if (user.length <= 0) {

		required_1.addClass('visible');
		return false;
	} else if (password.length <= 0) {

		required_2.addClass('visible');
		return false;
	}

	//TODO: logic validate fields

	if (user.length < 3) {
		invalid_1.addClass('visible');
		return false;
	}
		
	if (!validateEmail(user)){
		
		invalid_1.addClass('visible');
		return false;
    }
	
	return true;
}
/***
 ** TODO: add description.
*/
function redirectUsers() {

	window.location.href = './inicio';

}
/***
 ** TODO: add description.
*/
function redirectChangePass(idUser, email, password, fragment) {

	//construct consult object.
	var objData = JSON.stringify({
		password: password,
		idUser: idUser,
		email: email
	});
	//url controller.
	//var url = "/login/" + fragment;
	var url = "gcis/login/" + fragment;
	//const div: where information will be displayed.
	const fragmentDiv = document.getElementById("fragmentDiv");

	$.ajax({
		type: "POST",
		contentType: "application/json",
		url: url,
		data: objData,
		timeout: 30000,
		success: function(response) {

			fragmentDiv.innerHTML = response;
			// Simulate an HTTP redirect:
			$('#fragmentDiv').removeClass("col-md-4  offset-md-4");
			$('#fragmentDiv').addClass("col-md-10 offset-md-1");
			$('[data-toggle="tooltip"]').tooltip();
			$(".btn-toggle-pass i").click(function() {
				if ($(this).hasClass("fa-eye")) {
					$(this).removeClass("fa-eye");
					$(this).addClass("fa-eye-slash");
				} else if ($(this).hasClass("fa-eye-slash")) {
					$(this).removeClass("fa-eye-slash");
					$(this).addClass("fa-eye");
				}
			});
			$("#input-changepass-newpass").keyup(function() {
				var $th = $(this);
				$th.val($th.val().replace(/[^A-Za-zñÑ0-9ÁáÉéÍíÓóÚú\#\$\%\&\(\)\+\.\_\;\@]/g, function(str) {
					toastr.warning("Se aceptan letras, números, acentos (Aa-Zz, 0-9) y los siguientes caracteres  especiales:  _ ; . # $ % & ( ) + @");
					return '';
				}));
			});
			$("#input-changepass-newpassconf").keyup(function() {
				var $th = $(this);
				$th.val($th.val().replace(/[^A-Za-zñÑ0-9ÁáÉéÍíÓóÚú\#\$\%\&\(\)\+\.\_\;\@]/g, function(str) {
					toastr.warning("Se aceptan letras, números, acentos (Aa-ZzÑñ, 0-9, ´) y los siguientes caracteres  especiales:  _ ; . # $ % & ( ) + @");
					return '';
				}));
			});
			$("#input-changepass-passtemp").focus();

			var conf = document.getElementById("input-changepass-newpass");
			var myInput = document.getElementById("input-changepass-newpassconf");
			var email = document.getElementById("hidden-input-email").value;
			var letter = document.getElementById("valid-minus");
			var capital = document.getElementById("valid-mayus");
			var special = document.getElementById("valid-special");
			var number = document.getElementById("valid-number");
			var length = document.getElementById("valid-legth");
			var confirm = document.getElementById("valid-conf");
			var correo = document.getElementById("valid-email");
			var system = document.getElementById("valid-system");
			var combo = document.getElementById("valid-combo");
			var store = document.getElementById("valid-store");
			var indice = email.indexOf("@");
			email = email.substring(0, indice);

			// When the user starts to type something inside the password field
			myInput.onkeyup = function() {
				// Validate lowercase letters
				var lowerCaseLetters = /^(?=.*[a-z])(?!.*((abc)|(bcd)|(cde)|(def)|(efg)|(fgh)|(ghi)|(hij)|(ijk)|(jkl)|(lmn)|(mno)|(opq)|(pqr)|(qrs)|(rst)|(stu)|(tuv)|(uvw)|(vwx)|(wxy)|(xyz))).{3,25}/g;
				if (myInput.value.match(lowerCaseLetters)) {
					letter.classList.remove("invalid");
					letter.classList.add("valid");
				} else {
					letter.classList.remove("valid");
					letter.classList.add("invalid");
				}

				// Validate capital letters
				var upperCaseLetters = /^(?=.*[A-Z])(?!.*((ABC)|(BCD)|(CDE)|(DEF)|(EFG)|(FGH)|(GHI)|(HIJ)|(IJK)|(JKL)|(LMN)|(MNO)|(OPQ)|(PQR)|(QRS)|(RST)|(STU)|(TUV)|(UVW)|(VWX)|(WXY)|(XYZ))).{3,25}/g;
				if (myInput.value.match(upperCaseLetters)) {
					capital.classList.remove("invalid");
					capital.classList.add("valid");
				} else {
					capital.classList.remove("valid");
					capital.classList.add("invalid");
				}

				// Validate specials
				var specials = /[#$%()/=¿?*+-.#@_;]/g;
				if (myInput.value.match(specials)) {
					special.classList.remove("invalid");
					special.classList.add("valid");
				} else {
					special.classList.remove("valid");
					special.classList.add("invalid");
				}

				// Validate numbers
				var numbers = /^(?=.*\d)(?!.*((12)|(23)|(34)|(45)|(56)|(67)|(78)|(89)|(01))).{1,25}/g;
				// /^(?=.*\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[#$%()/=¿?*+-&.#@_;])(?!.*["'¡!])(?!.*((12)|(23)|(34)|(45)|(56)|(67)|(78)|(89)|(01))).{11,25}/;
				if (myInput.value.match(numbers)) {
					number.classList.remove("invalid");
					number.classList.add("valid");
				} else {
					number.classList.remove("valid");
					number.classList.add("invalid");
				}

				// Validate length
				if (myInput.value.length >= 11 && myInput.value.length <= 25) {
					length.classList.remove("invalid");
					length.classList.add("valid");
				} else {
					length.classList.remove("valid");
					length.classList.add("invalid");
				}

				// Validate match
				if (myInput.value == conf.value) {
					confirm.classList.remove("invalid");
					confirm.classList.add("valid");
				} else {
					confirm.classList.remove("valid");
					confirm.classList.add("invalid");
				}

				// Validate email
				if (!myInput.value.toLowerCase().includes(email.toLowerCase())) {
					correo.classList.remove("invalid");
					correo.classList.add("valid");
				} else {
					correo.classList.remove("valid");
					correo.classList.add("invalid");
				}

				// Validate system
				var regex_system = /^(?=(?=.*(sodi|sodimac|gci|totem|tótem|gcis))).{3,25}/g;
				// /^(?=(?=.*(sodi|sodimac|gci|totem|tótem|gcis|izcalli|arboledas|sanmateo|jacarandas|paseosdesanluis|bocadelrio|sanluis))).{5,25}/g
				if (!myInput.value.toLowerCase().match(regex_system)) {
					system.classList.remove("invalid");
					system.classList.add("valid");
				} else {
					system.classList.remove("valid");
					system.classList.add("invalid");
				}

				// Validate sodimac name
				var regex_combo = /^(?=(?=.*((abc)|(bcd)|(cde)|(def)|(efg)|(fgh)|(ghi)|(hij)|(ijk)|(jkl)|(lmn)|(mno)|(opq)|(pqr)|(qrs)|(rst)|(stu)|(tuv)|(uvw)|(vwx)|(wxy)|(xyz)))).{3,25}/g;
				if (!myInput.value.toLowerCase().match(regex_combo)) {
					combo.classList.remove("invalid");
					combo.classList.add("valid");
				} else {
					combo.classList.remove("valid");
					combo.classList.add("invalid");
				}

				// Validate stores names
				var regex_store = /^(?=(?=.*(izcalli|arboledas|sanmateo|jacarandas|paseosdesanluis|bocadelrio|sanluis))).{7,25}/g;
				if (!myInput.value.toLowerCase().match(regex_store)) {
					store.classList.remove("invalid");
					store.classList.add("valid");
				} else {
					store.classList.remove("valid");
					store.classList.add("invalid");
				}

			}

			conf.onkeyup = function() {

				// Validate match
				if (myInput.value == conf.value) {
					confirm.classList.remove("invalid");
					confirm.classList.add("valid");
				} else {
					confirm.classList.remove("valid");
					confirm.classList.add("invalid");
				}
			}

		},
		error: function(e) {
			console.log("ERROR: ", e);
		},
		done: function(e) {
			console.log("DONE");
		}
	});

}
/***
 ** TODO: add description.
*/
function psw_temp(type) {

	switch (type) {
		case 1:

			if ($('#btnPswTemp i').hasClass("fa-eye")) {

				$('#input-changepass-passtemp').attr('type', 'password');
				$('#btnPswTemp').removeClass("active");

			} else if ($('#btnPswTemp i').hasClass("fa-eye-slash")) {

				$('#input-changepass-passtemp').attr('type', 'text');
				$('#btnPswTemp').addClass("active");
			}

			break;
		case 2:

			if ($('#btnPswNew i').hasClass("fa-eye")) {

				$('#input-changepass-newpass').attr('type', 'password');
				$('#btnPswNew').removeClass("active");

			} else if ($('#btnPswNew i').hasClass("fa-eye-slash")) {

				$('#input-changepass-newpass').attr('type', 'text');
				$('#btnPswNew').addClass("active");
			}
			break;
		case 3:

			if ($('#btnPswConfirm i').hasClass("fa-eye")) {

				$('#input-changepass-newpassconf').attr('type', 'password');
				$('#btnPswConfirm').removeClass("active");

			} else if ($('#btnPswConfirm i').hasClass("fa-eye-slash")) {

				$('#input-changepass-newpassconf').attr('type', 'text');
				$('#btnPswConfirm').addClass("active");
			}
			break;
		default:

			break;
	}
}
/***
 ** TODO: add description.
*/
function clean_required() {

	var required_1 = $('#required-login-user');
	var required_2 = $('#required-login-password');
	var invalid_1 = $('#invalid-login-user');

	required_1.removeClass('visible');
	required_2.removeClass('visible');
	invalid_1.removeClass('visible');

	return false;

}
/***
 ** TODO: add description.
*/
function update_password(fragment) {

	var idUser = document.getElementById("hidden-input-idUser").value;
	var oldPassword = document.getElementById("input-changepass-passtemp").value;
	var password = document.getElementById("input-changepass-newpassconf").value;

	if (validate_temporal_form()) {
		//construct consult object.
		var objData = JSON.stringify({
			OldPassword: oldPassword,
			Password: password,
			IdUsuario: idUser
		});
		//url controller.
		//var url = "/login/" + fragment;
		var url = "gcis/login/" + fragment;

		$.ajax({
			type: "POST",
			contentType: "application/json",
			url: url,
			data: objData,
			timeout: 30000,
			success: function(response) {

				//validate response.
				if (!response.code) {

					switch (response.typeMessage) {
						case "1":

							toastr.success(response.messageGlobal);
							break;
						case "2":

							toastr.info(response.messageGlobal);
							break;
						case "3":

							toastr.warning(response.messageGlobal);
							break;
						case "4":

							toastr.error(response.messageGlobal);
							break;
						default:
							toastr.error(response.messageGlobal);
							//toastr.error("Error desconocido");
							break;
					}

					return false;
				}

				toastr.success("El cambio de contraseña se realizó correctamente");
				//toastr.success(response.messageGlobal);
				sleep(2100).then(() => {
					//redirectUsers();
					redirectLogin();
				});
			},
			error: function(e) {
				console.log("ERROR: ", e);
			},
			done: function(e) {
				console.log("DONE");
			}
		});
	}

}
/***
 ** TODO: add description.
*/
function validate_temporal_form() {

	var pass1 = document.getElementById("input-changepass-passtemp");
	var pass2 = document.getElementById("input-changepass-newpass");
	var pass3 = document.getElementById("input-changepass-newpassconf");
	var email = document.getElementById("hidden-input-email").value;
	// var email = "fabian.vantis";
	var indice = email.indexOf("@");
	email = email.substring(0, indice);

	var required_1 = $('#required-changepass-passtemp');
	var required_2 = $('#required-changepass-newpass');
	var required_3 = $('#required-changepass-newpassconf');


	//if value == "" then send notify required.
	if (pass1.value == "" || pass1.value.trim().length <= 0) {

		required_1.addClass('visible');
		return false;
	} else if (pass2.value == "" || pass2.value.trim().length <= 0) {

		required_2.addClass('visible');
		return false;
	} else if (pass3.value == "" || pass3.value.trim().length <= 0) {

		required_3.addClass('visible');
		return false;
	}

	if (pass2.value != pass3.value) {

		toastr.warning("La nueva contraseña y su confirmación no coinciden");
		return false
	}

	const regex_min = /^(?=.*\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[#$%()/=¿?*+-.#@_;])(?!.*["'¡!])(?!.*((12)|(23)|(34)|(45)|(56)|(67)|(78)|(89)|(01))).{11,25}/;

	var newpassValid = regex_min.test(pass2.value);
	var newpassconfValid = regex_min.test(pass3.value);

	if (!newpassValid || !newpassconfValid) {
		toastr.warning("Formato de contraseña incorrecta");
		return false;
	}

	const regex_char = new RegExp("^(?!.*((abc)|(bcd)|(cde)|(def)|(efg)|(fgh)|(ghi)|(hij)|(ijk)|(jkl)|(lmn)|(mno)|(opq)|(pqr)|(qrs)|(rst)|(stu)|(tuv)|(uvw)|(vwx)|(wxy)|(xyz)))(?!(?=.*(" + email + "|sodi|sodimac|gci|totem|tótem|gcis|izcalli|arboledas|sanmateo|jacarandas|paseosdesanluis|bocadelrio|sanluis))).{11,25}");

	var newpassFinal = regex_char.test(pass2.value.toLowerCase());
	var newpassconfFinal = regex_char.test(pass3.value.toLowerCase());

	if (!newpassFinal || !newpassconfFinal) {
		toastr.warning("Formato de contraseña incorrecta");
		return false;
	}

	return true;
}
/***
 ** TODO: add description.
*/
function clean_temporal() {

	$('#required-changepass-passtemp').removeClass('visible');
	$('#required-changepass-newpass').removeClass('visible');
	$('#required-changepass-newpassconf').removeClass('visible');

	return false;

}
/***
 ** TODO: add description.
*/
function redirectLogin() {

	window.location.href = './';

}
/***
 ** REQUEST get data table CONSULT service to API.
 ** GET fragment page: fragments/devices :: tableDevices.
*/
function recover_password(fragment) {

	var user = document.getElementById("input-recover-email").value;
	var navegador;

	// Opera 8.0+
	var isOpera = (!!window.opr && !!opr.addons) || !!window.opera || navigator.userAgent.indexOf(' OPR/') >= 0;
	// Firefox 1.0+
	var isFirefox = typeof InstallTrigger !== 'undefined';
	// Safari 3.0+ "[object HTMLElementConstructor]" 
	var isSafari = /constructor/i.test(window.HTMLElement) || (function(p) { return p.toString() === "[object SafariRemoteNotification]"; })(!window['safari'] || (typeof safari !== 'undefined' && window['safari'].pushNotification));
	// Internet Explorer 6-11
	var isIE = /*@cc_on!@*/false || !!document.documentMode;
	// Edge 20+
	var isEdge = !isIE && !!window.StyleMedia;
	// Chrome 1 - 79
	var isChrome = !!window.chrome && (!!window.chrome.webstore || !!window.chrome.runtime);
	// Edge (based on chromium) detection
	var isEdgeChromium = isChrome && (navigator.userAgent.indexOf("Edg") != -1);
	// Blink engine detection
	var isBlink = (isChrome || isOpera) && !!window.CSS;

	if (isOpera) {
		navegador = "Opera";
	} else if (isFirefox) {
		navegador = "Mozilla Firefox";
	} else if (isSafari) {
		navegador = "Safari";
	} else if (isIE) {
		navegador = "Internet Explorer";
	} else if (isEdge) {
		navegador = "Edge";
	} else if (isEdgeChromium) {
		navegador = "Microsoft Edge";
	} else if (isChrome) {
		navegador = "Google Chrome";
	} else if (isBlink) {
		navegador = "Blink";
	} else {
		navegador = "Other";
	}

	if (validate_recover_form()) {

		//construct consult object.
		var objData = JSON.stringify({
			Usuario: user,
			navigator: navegador
		});
		//url controller.
		//var url = "/login/" + sendemail;
		var url = "gcis/login/" + fragment;
		//ajax request.
		$.ajax({
			type: "POST",
			contentType: "application/json",
			url: url,
			data: objData,
			timeout: 30000,
			success: function(response) {

				//validate response.
				if (!response.code) {

					switch (response.typeMessage) {
						case "1":

							toastr.success(response.messageGlobal);
							break;
						case "2":

							toastr.info(response.messageGlobal);
							break;
						case "3":

							toastr.warning(response.messageGlobal);
							break;
						case "4":
							toastr.error("Correo electrónico incorrecto");
							//toastr.error(response.messageGlobal);
							break;
						default:
							toastr.error(response.messageGlobal);
							//toastr.error("Error desconocido");
							break;
					}

					return false;
				}

				toastr.info("Te hemos enviado un correo electrónico con la nueva contraseña temporal. Revisa tu correo y sigue las instrucciones");
				//toastr.info(response.messageGlobal); //Usuario guardado correctamente
				sleep(2500).then(() => {
					// redirectChangePass(response.user.IdUsuario, response.user.Password, 'changepass');
					redirectChangePass(response.user.IdUsuario, response.user.Usuario, "", 'changepass');
				});

			},
			error: function(e) {
				console.log("ERROR: ", e);
			},
			done: function(e) {
				console.log("DONE");
			}
		});
	}
}
/***
 ** TODO: add description.
*/
function validate_recover_form() {

	var email = document.getElementById("input-recover-email");

	var required = $('#required-recover-email');


	//if value == "" then send notify required.
	if (email.value == "" || email.value.trim().length <= 0) {

		required.addClass('visible');
		return false;
	}

	if (email.value.trim().length < 3) {

		toastr.warning("Ingrese al menos 3 caracteres en 'Correo electrónico'");
		return false;
	}

	const regex = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
	var r = regex.test(email.value);

	if (!r) {

		toastr.warning("El correo electrónico debe tener el formato  nombre@dominio. Se aceptan letras y números (Aa-Zz, 0-9) y los siguientes caracteres  especiales: . # $ %& ( ) +");
		return false;
	}

	return true;

}
/***
 ** TODO: add description.
*/
function clean_recover() {

	$('#required-recover-email').removeClass('visible');

	return false;

}
/***
 ** TODO: add description.
*/
function sleep(ms) {

	return new Promise(resolve => setTimeout(resolve, ms));

}

function validateEmail(email) {

	const regex = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
	return regex.test(email);

}

function eventos(){

    releaseEventCopiPaste("#input-login-user", "paste");
    releaseEventCopiPaste("#input-login-user", "copy");
    releaseEventCopiPaste("#input-login-user", "dragover");
    releaseEventCopiPaste("#input-login-password", "paste");
    releaseEventCopiPaste("#input-login-password", "copy");
    releaseEventCopiPaste("#input-login-password", "dragover");
}

function releaseEventCopiPaste(id, evento){
    $(id).on(evento, function(e){
        e.preventDefault();
    })
}


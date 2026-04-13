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

});
/*** -------------- START LOGIN section  -------------- ***/
/***
 ** TODO: add description.
*/
function redirectChangePass(idUser, email, password) {

	//construct consult object.
	var objData = JSON.stringify({
		pass: password,
		id: idUser,
		usuario: email
	});

	$.ajax({
		type: "POST",
		contentType: "application/json",
		url: "./change",
		data: objData,
		timeout: 30000,
		success: function(response) {

			$("#fragmentDiv").html(response);
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
			// Focus
			$("#input-changepass-passtemp").focus();
			// Submit form recover
			$("form#form-change").submit(function(e) {
				// Stop default form Submit.
				e.preventDefault();

				// Call Ajax Submit.
				ajaxSubmitFormChange();
			});
			//Disable cut copy paste
			$('#input-changepass-newpass').bind('cut copy paste', function(e) {
				e.preventDefault();
			});
			$('#input-changepass-newpassconf').bind('cut copy paste', function(e) {
				e.preventDefault();
			});
			//Disable mouse right click
			$('#input-changepass-newpass').on("contextmenu", function(e) {
				return false;
			});
			$('#input-changepass-newpassconf').on("contextmenu", function(e) {
				return false;
			});
			// Validate
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
				// Validate lowercase letters /^(?=.*[a-z])(?!.*((abc)|(bcd)|(cde)|(def)|(efg)|(fgh)|(ghi)|(hij)|(ijk)|(jkl)|(lmn)|(mno)|(opq)|(pqr)|(qrs)|(rst)|(stu)|(tuv)|(uvw)|(vwx)|(wxy)|(xyz))).{3,25}/g
				var lowerCaseLetters = /^(?=.*[a-z]).{1,25}/g;
				if (myInput.value.match(lowerCaseLetters)) {
					letter.classList.remove("invalid");
					letter.classList.add("valid");
				} else {
					letter.classList.remove("valid");
					letter.classList.add("invalid");
				}

				// Validate capital letters /^(?=.*[A-Z])(?!.*((ABC)|(BCD)|(CDE)|(DEF)|(EFG)|(FGH)|(GHI)|(HIJ)|(IJK)|(JKL)|(LMN)|(MNO)|(OPQ)|(PQR)|(QRS)|(RST)|(STU)|(TUV)|(UVW)|(VWX)|(WXY)|(XYZ))).{3,25}/g
				var upperCaseLetters = /^(?=.*[A-Z]).{1,25}/g;
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

				// Validate numbers /^(?=.*\d)(?!.*((12)|(23)|(34)|(45)|(56)|(67)|(78)|(89)|(01))).{1,25}/g
				var numbers = /^(?=.*\d).{1,25}/g;
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
				var regex_system = /^(?=(?=.*(sodi|sodimac|reba|rebates|rebts))).{3,25}/g;
				if (!myInput.value.toLowerCase().match(regex_system)) {
					system.classList.remove("invalid");
					system.classList.add("valid");
				} else {
					system.classList.remove("valid");
					system.classList.add("invalid");
				}

				// Validate sodimac name
				var regex_combo = /^(?=(?=.*((abc)|(bcd)|(cde)|(def)|(efg)|(fgh)|(ghi)|(hij)|(ijk)|(jkl)|(lmn)|(mno)|(opq)|(pqr)|(qrs)|(rst)|(stu)|(tuv)|(uvw)|(vwx)|(wxy)|(xyz)|(012)|(123)|(234)|(345)|(456)|(567)|(678)|(789)|(987)|(876)|(765)|(654)|(543)|(432)|(321)|(210)))).{3,25}/g;
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
function ajaxSubmitFormChange() {

	var idUser = document.getElementById("hidden-input-idUser").value;
	var oldPassword = document.getElementById("input-changepass-passtemp").value.trim();
	var password = document.getElementById("input-changepass-newpassconf").value.trim();

	if (validate_temporal_form()) {
		//construct consult object.
		var objData = JSON.stringify({
			pass: oldPassword,
			newPassword: password,
			id: idUser
		});

		$("#btn-actualizarNueva").prop("disabled", true);

		$.ajax({
			type: "POST",
			contentType: "application/json",
			url: "./update",
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

					$("#btn-actualizarNueva").prop("disabled", false);
					swal.close();

					return false;
				}

				$("#btn-actualizarNueva").prop("disabled", false);
				swal.close();

				if (tipo == 1) {

					toastr.success(message, titulo);

				} else if (tipo == 4) {

					toastr.info(message, titulo);
				}

				setTimeout(() => { redirectLogin(); }, 1900);

			},
			error: function(e) {

				console.log("ERROR: ", e);
				toastr.error("No se pudo conectar con el servicio", "Error de conexión");
				$("#btn-actualizarNueva").prop("disabled", false);
				swal.close();
			}

		});
	}

}
/***
 ** TODO: add description.
*/
function validate_temporal_form() {

	var pass2 = document.getElementById("input-changepass-newpass");
	var pass3 = document.getElementById("input-changepass-newpassconf");
	var email = document.getElementById("hidden-input-email").value;
	var indice = email.indexOf("@");
	email = email.substring(0, indice);

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
function redirectLogin() {

	window.location.href = './';

}

function ajaxSubmitFormRecover() {

	var user = document.getElementById("input-recover-email").value;

	if (validate_recover_form()) {

		// construct create object.
		var objData = JSON.stringify({
			usuario: user,
			pass: ""
		});
		//ajax request.
		$.ajax({
			type: "POST",
			contentType: "application/json",
			url: "./recover",
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

				//validate response.
				var titulo = response.title;
				var message = response.message;
				var tipo = response.typeMessage;

				if (!response.code) {

					if (tipo == 2) {

						toastr.warning(message, titulo);

					} else if (tipo == 3) {

						toastr.error(message, titulo);
					}

					swal.close();

					return false;
				}

				swal.close();

				if (tipo == 1) {

					toastr.success(message, titulo);

				} else if (tipo == 4) {

					toastr.info(message, titulo);
				}

				setTimeout(() => { redirectChangePass(response.id, response.usuario, ""); }, 2500);

			},
			error: function(e) {

				console.log("ERROR: ", e);
				toastr.error("No se pudo conectar con el servicio", "Error de conexión");
				swal.close();
			}
		});
	}
}
/***
 ** TODO: add description.
*/
function validate_recover_form() {

	var email = document.getElementById("input-recover-email");

	if (email.value.trim().length < 5) {

		toastr.warning("Ingrese al menos 5 caracteres en 'Correo electrónico'");
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
function sleep(ms) {

	return new Promise(resolve => setTimeout(resolve, ms));

}

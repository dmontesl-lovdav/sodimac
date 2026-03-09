/*------------------------------------------------*
 *   Copyright (c) SODIMAC. December 2020.        *
 *   Portal web: www.sodimac.com.mx.              *
 *   Contributors:                                *
 *   Vantis S. de RL. de CV. Web: vantis.mx       *
 *   Fabian Alvarez <fabian.alvarez@vantis.mx>    *
--------------------------------------------------*/
//CONSTANTS
//element principal select.
const search_user = document.getElementById("search-user-dev");
/***
 ** GET styles, functions that start during page load.
*/
$(document).ready(function() {

	//clean notify message
	toastr.clear();
	// jsShowWindowLoad();
	//styles for navigation bar.
	$(".nav-item").removeClass("active");
	$(".dropdown-item").removeClass("active");
	$("#navUsuarios").addClass("active");
	$("#usuariosPDATotem").addClass("active");

	$('body').on('click', function(e) {
		$('[data-toggle=popover-pass]').each(function() {
			// hide any open popovers when the anywhere else in the body is clicked
			if (!$(this).is(e.target) && $(this).has(e.target).length === 0 && $('.popover').has(e.target).length === 0) {
				$(this).popover('hide');
			}
		});
	});
	$("#search-user-dev").keyup(function() {
		var $th = $(this);
		$th.val($th.val().replace(/[^A-Za-zÁáÉéÍíÓóÚúÑñ ]/g, function(str) {
			toastr.warning("Puedes ingresar sólo letras y acentos (Aa-Zz, Ññ, ´)");
			return '';
		}));
	});

});

/*** -------------- START CONSULT section  -------------- ***/

/***
 ** REQUEST get data table CONSULT service to API.
 ** GET fragment page: fragments/devices :: tableDevices.
*/
function consult_userdev(fragment) {

	$('[data-toggle]').popover('dispose');
	$('.blck').prop('disabled', false);
	$('#fragmentDiv, li, select, #dtUsuariosPdaTotem_filter').removeClass('prevent-click');

	var search = document.getElementById("search-user-dev");
	//if value == "" then send notify required.
	if (search.value.trim().length < 2 && search.value.trim() != "") {

		toastr.warning("Ingrese al menos 2 caracteres");
		return false;
	}
	//construct consult object.
	var objData = JSON.stringify({
		filter: document.getElementById("search-user-dev").value.trim()
	});
	//url controller.
	var url = "cfdi/consultarCfdi/" + fragment;
	//const div: where information will be displayed.
	const fragmentDiv = document.getElementById("fragmentDiv");
	//ajax request.
	$.ajax({
		type: "POST",
		contentType: "application/json",
		url: url,
		data: objData,
		timeout: 30000,
		success: function(response) {
			fragmentDiv.innerHTML = response;
			load_table_style();
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
 ** GET style and config data table devices.
*/
function load_table_style() {

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

	//config dataTable users device
	$('#dtUsuariosPdaTotem').DataTable({
		"order": [[0, "asc"]],
		"aaSorting": [],
		columnDefs: [{
			orderable: false,
			targets: [3, 4, 5],

		}],
		"searching": true,
		"paging": true,
		"pagingType": "simple_numbers",
		"pageLength": 10,
		"language": {
			"info": "Mostrando _START_ de _END_ de un total: _TOTAL_ registros",
			"lengthMenu": "Mostrando _MENU_ registros",
			"loadingRecords": "Cargando...",
			"processing": "Procesando...",
			"emptyTable": "Sin registros en la tabla",
			"infoEmpty": "Mostrando 0 de 0 de un total: 0 registros",
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
/***
 ** TODO: add description.
*/
function psw_change(id) {

	var inp_psw = $('#passUserPda-' + id.toString());
	var btn_psw = $('#btnPsw-' + id.toString() + " i");
	var btn_active = $('#btnPsw-' + id.toString());

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
function psw_edit() {

	var inp_psw = $('#edit-input-password');
	var btn_psw = $('#btn-edit-pass i');
	var btn_active = $('#btn-edit-pass');

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
function psw_create() {

	var inp_psw = $('#create-read-password');
	var btn_psw = $('#btn-create-pass i');
	var btn_active = $('#btn-create-pass');

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
function clean_required(type) {

	if (type == 'create') {

		$('#required-create-name').removeClass('visible');
		$('#required-create-lastnameP').removeClass('visible');
		$('#required-create-lastnameM').removeClass('visible');

		return false;

	} else if (type == 'edit') {

		$('#required-edit-name').removeClass('visible');
		$('#required-edit-lastnameP').removeClass('visible');
		$('#required-edit-lastnameM').removeClass('visible');

		return false;
	}

	return false;

}
/***
 ** TODO: add description.
*/
function edit_userDev(id, user, nameUser, lastnameFather, lastnameMother, password) {

	if (password == null || password == "") {

		password = "";
	}

	//pass name user in tittle card body.
	$('.blck').prop('disabled', true);
	$('#dtUsuariosPdaTotem_paginate li, #dtUsuariosPdaTotem_length, #dtUsuariosPdaTotem_filter').addClass('prevent-click');
	var name = document.getElementById("name_userDev");
	//var gen_pwd = document.getElementById("read-generate-psw");
	name.innerHTML = "";
	name.innerHTML = user;
	//gen_pwd.innerHTML = password;
	//pass values from user specific for edit.
	$('#hidden-input-id').val(id);
	$('#edit-input-name').val(nameUser);
	$('#edit-input-lastnameP').val(lastnameFather);
	$('#edit-input-lastnameM').val(lastnameMother);
	$('#edit-input-user').val(user);
	$('#edit-input-password').val(password);
	//add class active to label form.
	$('.edicionUsuarios label').addClass("active");
	//reset input eye.
	$('#edit-input-password').attr('type', 'password');
	$('#btn-edit-pass').removeClass('active');
	$('#btn-edit-pass i').removeClass("fa-eye-slash");
	$('#btn-edit-pass i').addClass("fa-eye");
	//activate copy button.
	$("#copyUser-0").click(function() {

		if ($("#edit-input-user").val() == "") {

			return false;
		}
		$("#edit-input-user").select();
		document.execCommand("copy");
		toastr.success('Usuario copiado');
	});
	$("#copyPass-0").click(function() {

		var attr = $("#edit-input-password").is("[type='password']");

		if (typeof attr !== typeof undefined && attr !== false || $("#edit-input-password").val() == "") {

			return false;
		}
		$("#edit-input-password").select();
		document.execCommand("copy");
		toastr.success('Contraseña copiada');
	});
	//Popover cambio contrasaeña//
	$('[data-toggle="popover-pass"]').popover({
		html: true,
		trigger: 'click',
		placement: 'top',
		content: $('#popover_content_password').html()
	});
	//btn Switch
	$('#reciclePass-0').on('click', function() {
		$('[data-toggle="popover-pass"]').popover("show");
	});
	//btns confirm
	$(document).on('click', '#confirmPass-0', function() {
		//send generate password function.
		generate_password("resetpsw");
		//if function succes then activate device.
		sleep(1000).then(() => {

			//consult_userdev("consult");
			return false;
		});
	});
	//btns deny
	$(document).on('click', '#denyPass-0', function() {
		//toastr.info('Generación cancelada');
		$('[data-toggle="popover-pass"]').popover("hide");

	});
	//validate fields
	$("#edit-input-name").keyup(function() {
		var $th = $(this);
		$th.val($th.val().replace(/[^A-Za-zñÑÁáÉéÍíÓóÚú ]/g, function(str) {
			toastr.warning("Puedes ingresar sólo letras y acentos (Aa-Zz, Ññ, ´)");
			return '';
		}));
	});
	$("#edit-input-lastnameP").keyup(function() {
		var $th = $(this);
		$th.val($th.val().replace(/[^A-Za-zñÑÁáÉéÍíÓóÚú ]/g, function(str) {
			toastr.warning("Puedes ingresar sólo letras y acentos (Aa-Zz, Ññ, ´)");
			return '';
		}));
	});
	$("#edit-input-lastnameM").keyup(function() {
		var $th = $(this);
		$th.val($th.val().replace(/[^A-Za-zñÑÁáÉéÍíÓóÚú ]/g, function(str) {
			toastr.warning("Puedes ingresar sólo letras y acentos (Aa-Zz, Ññ, ´)");
			return '';
		}));
	});
	//responsive design for for the width of the screen.
	if ($('header').width() <= 767) {

		$('.edicionUsuarios').animate({
			opacity: 1,
			top: "0px",
		}, 300).show();

	}
	else if ($('header').width() >= 768) {

		$('.edicionUsuarios').animate({
			opacity: 1,
			top: "108px",
		}, 300).show();

	}
}
/***
 ** POST service: generate new password.
*/
function generate_password(fragment) {

	var pwd = document.getElementById("edit-input-password");
	//var pwd_generate = document.getElementById("read-generate-psw");
	var hidden_id = document.getElementById("hidden-input-id");
	var passUser = document.getElementById('passUserPda-' + hidden_id.value);

	//construct update object.
	var objData = JSON.stringify({
		IdUsuario: parseInt(hidden_id.value, 10)
	});
	//url controller.
	var url = "cfdi/consultarCfdi/" + fragment;
	//ajax request.
	$.ajax({
		type: "POST",
		contentType: "application/json",
		url: url,
		data: objData,
		timeout: 20000,
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

			toastr.success('Edición exitosa');
			//toastr.success(response.messageGlobal);
			pwd.value = response.dtoUserDevice.Password;
			passUser.value = response.dtoUserDevice.Password;
			$('[data-toggle="popover-pass"]').popover("hide");

			return true;
		},
		error: function(e) {
			toastr.error("Error de conexión");
			return false;
		},
		done: function(e) {
			console.log("DONE");
		}
	});

	return false;
}
/***
 ** TODO: add description.
*/
function edit_service(fragment) {

	var hidden_id = document.getElementById("hidden-input-id");
	var input_4 = document.getElementById("edit-input-name");
	var input_5 = document.getElementById("edit-input-lastnameP");
	var input_6 = document.getElementById("edit-input-lastnameM");
	var user = document.getElementById("edit-input-user");
	var pwd = document.getElementById("edit-input-password");

	var name = input_4.value.trim();
	name = name.replace(/^\s+|\s+$|\s+(?=\s)/g, "");

	var lastname1 = input_5.value.trim();
	lastname1 = lastname1.replace(/^\s+|\s+$|\s+(?=\s)/g, "");

	var lastname2 = input_6.value.trim();
	lastname2 = lastname2.replace(/^\s+|\s+$|\s+(?=\s)/g, "");

	if (validate_edit_form()) {

		//construct update object.
		var objData = JSON.stringify({
			IdUsuario: parseInt(hidden_id.value, 10),
			Nombre: name,
			ApellidoP: lastname1,
			ApellidoM: lastname2,
			Usuario: user.value,
			Password: pwd.value
		});
		//url controller.
		var url = "cfdi/consultarCfdi/" + fragment;
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

							toastr.error(response.messageGlobal);
							break;
						default:

							toastr.error(response.messageGlobal);
							//toastr.error("Error desconocido");
							break;
					}
					return false;
				}

				toastr.success('Edición exitosa');
				//toastr.success(response.messageGlobal);
				consult_userdev('consult');

			},
			error: function(e) {
				toastr.error("Error de conexión");
				return false;
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
function validate_edit_form() {

	const input_4 = document.getElementById("edit-input-name");
	const input_5 = document.getElementById("edit-input-lastnameP");
	const input_6 = document.getElementById("edit-input-lastnameM");

	const required_4 = $('#required-edit-name');
	const required_5 = $('#required-edit-lastnameP');
	const required_6 = $('#required-edit-lastnameM');

	//if value == "" then send notify required.
	if (input_4.value == "") {

		required_4.addClass('visible');
		return false;
	} else if (input_5.value == "") {

		required_5.addClass('visible');
		return false;
	} else if (input_6.value == "") {

		required_6.addClass('visible');
		return false;
	}

	//if value == "" then send notify required.
	if (input_4.value.trim().length < 2) {

		toastr.warning("Ingrese al menos 2 caracteres en 'Nombre'");
		return false;
	} else if (input_5.value.trim().length < 2) {

		toastr.warning("Ingrese al menos 2 caracteres en 'Apellido paterno'");
		return false;
	} else if (input_6.value.trim().length < 2) {

		toastr.warning("Ingrese al menos 2 caracteres en 'Apellido materno'");
		return false;
	}

	return true;

}
/***
 ** TODO: add description.
*/
function cancel_edit() {

	$('[data-toggle]').popover('dispose');
	$('.blck').prop('disabled', false);
	$('#dtUsuariosPdaTotem_paginate li, #dtUsuariosPdaTotem_length, #dtUsuariosPdaTotem_filter').removeClass('prevent-click');
	$('.edicionUsuarios').animate({
		opacity: 0,
		top: "100px",
	}, 300).hide();
	//toastr.info('Edición cancelada');

	//TODO: no send consult
	consult_userdev("consult");

}
/***
 ** GET fragment page: fragments/devices :: createDevice.
*/
function create_userdev(fragment) {

	$('[data-toggle]').popover('dispose');
	$('.blck').prop('disabled', false);
	$('#fragmentDiv, #dtUsuariosPdaTotem_paginate li, #dtUsuariosPdaTotem_length, #dtUsuariosPdaTotem_filter').removeClass('prevent-click');
	//url controller.
	var url = "cfdi/consultarCfdi/" + fragment;
	$("#fragmentDiv").load(url);
	sleep(600).then(() => {
		document.getElementById("create-input-name").focus();
		//active eye password.
		/*$(".btn-toggle-pass i").click(function() {
			if ($(this).hasClass("fa-eye")) {
				$(this).removeClass("fa-eye");
				$(this).addClass("fa-eye-slash");
			} else if ($(this).hasClass("fa-eye-slash")) {
				$(this).removeClass("fa-eye-slash");
				$(this).addClass("fa-eye");
			}
		});*/
		//activate copy button.
		$("#copyUser-1").click(function() {

			if ($("#create-read-user").val() == "") {

				return false;
			}
			$("#create-read-user").select();
			document.execCommand("copy");
			toastr.success('Usuario copiado');
		});
		$("#copyPass-1").click(function() {

			var attr = $("#create-read-password").is("[type='password']");

			if (typeof attr !== typeof undefined && attr !== false || $("#create-read-password").val() == "") {

				return false;
			}
			$("#create-read-password").select();
			document.execCommand("copy");
			toastr.success('Contraseña copiada');
		});
	});

}
/***
 ** TODO: add description.
*/
function save_create(fragment) {

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
	//activate copy button.
	$("#copyUser-1").click(function() {

		if ($("#create-read-user").val() == "") {

			return false;
		}
		$("#create-read-user").select();
		document.execCommand("copy");
		toastr.success('Usuario copiado');
	});
	$("#copyPass-1").click(function() {

		var attr = $("#create-read-password").is("[type='password']");

		if (typeof attr !== typeof undefined && attr !== false || $("#create-read-password").val() == "") {

			return false;
		}
		$("#create-read-password").select();
		document.execCommand("copy");
		toastr.success('Contraseña copiada');
	});
	$('#btn-AltaUsuario').prop('disabled', true);

	let form = document.getElementById("formCreateUserDev");
	var user = document.getElementById("create-read-user");
	var pwd = document.getElementById("create-read-password");
	var input_1 = document.getElementById("create-input-name").value;
	var input_2 = document.getElementById("create-input-lastnameP").value;
	var input_3 = document.getElementById("create-input-lastnameM").value;

	var name = input_1.trim();
	name = name.replace(/^\s+|\s+$|\s+(?=\s)/g, "");

	var lastname1 = input_2.trim();
	lastname1 = lastname1.replace(/^\s+|\s+$|\s+(?=\s)/g, "");

	var lastname2 = input_3.trim();
	lastname2 = lastname2.replace(/^\s+|\s+$|\s+(?=\s)/g, "");

	if (validate_create_form()) {

		//construct update object.
		var objData = JSON.stringify({
			Nombre: name,
			ApellidoP: lastname1,
			ApellidoM: lastname2,
			SegundoNombre: ""
		});
		//url controller.
		var url = "cfdi/consultarCfdi/" + fragment;
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
							$('#btn-AltaUsuario').prop('disabled', false);
							break;
						case "2":

							toastr.info(response.messageGlobal);
							$('#btn-AltaUsuario').prop('disabled', false);
							break;
						case "3":

							toastr.warning(response.messageGlobal);
							$('#btn-AltaUsuario').prop('disabled', false);
							break;
						case "4":

							toastr.error(response.messageGlobal);
							$('#btn-AltaUsuario').prop('disabled', false);
							break;
						default:

							toastr.error(response.messageGlobal);
							$('#btn-AltaUsuario').prop('disabled', false);
							//toastr.error("Error desconocido");
							break;
					}
					return false;
				}
				//disable fields form.
				$('#create-input-name').prop('disabled', true);
				$('#create-input-lastnameP').prop('disabled', true);
				$('#create-input-lastnameM').prop('disabled', true);
				//paint user and pswd.
				$('#create-label-user').addClass("active");
				$('#create-label-password').addClass("active");
				user.value = response.dtoUserDevice.Usuario;
				pwd.value = response.dtoUserDevice.Password;
				//notify success message.
				toastr.success('Alta exitosa');
				//toastr.success(response.messageGlobal);
				$('#btn-NuevoUsuario').prop('hidden', false);

				return true;
			},
			error: function(e) {
				$('#btn-AltaUsuario').prop('disabled', false);
				toastr.error("Error de conexión");
				return false;
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
function validate_create_form() {

	const input_1 = document.getElementById("create-input-name");
	const input_2 = document.getElementById("create-input-lastnameP");
	const input_3 = document.getElementById("create-input-lastnameM");
	const user = document.getElementById("create-read-user");
	const pwd = document.getElementById("create-read-password");

	const required_1 = $('#required-create-name');
	const required_2 = $('#required-create-lastnameP');
	const required_3 = $('#required-create-lastnameM');

	//if value == "" then send notify required.
	if (input_1.value == "") {

		//jsRemoveWindowLoad();
		required_1.addClass('visible');
		$('#btn-AltaUsuario').prop('disabled', false);
		return false;
	} else if (input_2.value == "") {

		//jsRemoveWindowLoad();
		required_2.addClass('visible');
		$('#btn-AltaUsuario').prop('disabled', false);
		return false;
	} else if (input_3.value == "") {

		//jsRemoveWindowLoad();
		required_3.addClass('visible');
		$('#btn-AltaUsuario').prop('disabled', false);
		return false;
	}

	//if value == "" then send notify required.
	if (input_1.value.trim().length < 2) {

		//jsRemoveWindowLoad();
		toastr.warning("Ingrese al menos 2 caracteres en 'Nombre'");
		$('#btn-AltaUsuario').prop('disabled', false);
		return false;
	} else if (input_2.value.trim().length < 2) {

		//jsRemoveWindowLoad();
		toastr.warning("Ingrese al menos 2 caracteres en 'Apellido paterno'");
		$('#btn-AltaUsuario').prop('disabled', false);
		return false;
	} else if (input_3.value.trim().length < 2) {

		//jsRemoveWindowLoad();
		toastr.warning("Ingrese al menos 2 caracteres en 'Apellido materno'");
		$('#btn-AltaUsuario').prop('disabled', false);
		return false;
	}

	return true;
}
/***
 ** TODO: add description.
*/
function cancel_create() {

	//toastr.info('Usuario cancelado');

	if (search_user.value == '') {

		sleep(500).then(() => {

			window.location.reload();
		});

	} else {

		consult_userdev("consult");

	}

	return false;
}
/***
 ** GET styles and effects popover, buttons for activate / de activate device.
*/
function active_userdev(id, user, type) {

	//button switch.
	if (type == 1) {

		clean_popover();

		$('[data-toggle="' + id + '"]').popover({
			html: true,
			//trigger: 'click',
			placement: 'top',
			content: function() { return $('#popover_content_wrapper').html(); }
		});

		//get value switch button: true or false.
		var switchButton = $("#" + id).is(":checked");

		if (switchButton) {
			//load data to popover.
			create_popover_content();
			$("#mensajeActivacion").html("<span style='color: #006AB4!important;'>¿Deseas activar este usuario?</span>");
			$("#idUsuario").html(user);
			$("#botonesActivacion").html("<div class='btn btn-default btn-sm text-center' id='confirmActive-" + id + "'>Activar</div><div class='btn btn-outline-primary btn-sm text-center' id='denyActive-" + id + "'>Cancelar</div>");
			$('[data-toggle="' + id + '"]').popover("show");
			$('.blck').prop('disabled', true);
			$('#fragmentDiv').addClass('prevent-click');
		} else {
			//load data to popover.
			create_popover_content();
			$("#mensajeActivacion").html("¿Deseas desactivar este usuario?");
			$("#idUsuario").html(user);
			$("#botonesActivacion").html("<div class='btn btn-outline-primary btn-sm text-center' id='confirmDesactive-" + id + "'>Desactivar</div><div class='btn btn-default btn-sm text-center' id='denyDesactive-" + id + "'>Cancelar</div>");
			$('[data-toggle="' + id + '"]').popover("show");
			$('.blck').prop('disabled', true);
			$('#fragmentDiv').addClass('prevent-click');
		}
	}
	//buttons confirm.
	//button "Activar".
	$(document).on('click', '#confirmActive-' + id, function() {

		//send switchStatus function.
		switch_status(id, true, "update");
		//if function succes then activate device.
		sleep(800).then(() => {
			if (switch_status) {

				toastr.success("Usuario activado");
				$(this).prop('checked', true);
				$('.blck').prop('disabled', false);
				$('#fragmentDiv').removeClass('prevent-click');
				$('[data-toggle="' + id + '"]').popover('hide');
				clean_popover();

				return false;

			} else {
				//else then show error and cancel activation.
				toastr.error("Error");
				$("#" + id).prop('checked', false);
				$('.blck').prop('disabled', false);
				$('#fragmentDiv').removeClass('prevent-click');
				$('[data-toggle="' + id + '"]').popover('hide');
				clean_popover();

				return false;
			}
		});
	});
	//button "Desactivar".
	$(document).on('click', '#confirmDesactive-' + id, function() {

		//send switchStatus function.
		switch_status(id, false, "update");
		//if function succes then de activate device.
		sleep(800).then(() => {
			if (switch_status) {

				toastr.info("Usuario desactivado");
				$(this).prop('checked', false);
				$('.blck').prop('disabled', false);
				$('#fragmentDiv').removeClass('prevent-click');
				$('[data-toggle="' + id + '"]').popover('hide');
				clean_popover();

				return false;

			} else {

				toastr.error("Error");
				$("#" + id).prop('checked', true);
				$('.blck').prop('disabled', false);
				$('#fragmentDiv').removeClass('prevent-click');
				$('[data-toggle="' + id + '"]').popover('hide');
				clean_popover();

				return false;

			}
		});
	});

	//buttons cancel.
	//button "Cancelar" of activate.
	$(document).on('click', '#denyActive-' + id, function() {

		$("#" + id).prop('checked', false);
		$('.blck').prop('disabled', false);
		$('#fragmentDiv').removeClass('prevent-click');
		$('[data-toggle="' + id + '"]').popover('hide');
		clean_popover();

		return false;

	});
	//button "Cancelar" of de activate.
	$(document).on('click', '#denyDesactive-' + id, function() {

		$("#" + id).prop('checked', true);
		$('.blck').prop('disabled', false);
		$('#fragmentDiv').removeClass('prevent-click');
		$('[data-toggle="' + id + '"]').popover('hide');
		clean_popover();

		return false;

	});

	function create_popover_content() {

		$('#popover_content_wrapper').html(`
			<p id="mensajeActivacion">
			</p>
			<h5 id="idUsuario">
			</h5>
			<br>
			<div class="row">
				<div id="botonesActivacion" class="col-lg-12">
				</div>
			</div>`);
	}
}
/***
 ** REQUEST update status DISABLE service to API.
*/
function switch_status(id, status, fragment) {

	//construct update object.
	var objData = JSON.stringify({
		IdUsuario: id,
		Activo: status
	});
	//url controller.
	var url = "cfdi/consultarCfdi/" + fragment;
	//ajax request.
	$.ajax({
		type: "POST",
		contentType: "application/json",
		url: url,
		data: objData,
		timeout: 10000,
		success: function(response) {

			//validate response.
			if (!response.code) {
				return false;
			}

			return true;
		},
		error: function(e) {
			console.log("ERROR: ", e);
			return false;
		},
		done: function(e) {
			console.log("DONE");
		}
	});

	return false;

}
/***
 ** CLEAN pop over, auxiliary function for button switch.
*/
function clean_popover() {

	$("#popover_content_wrapper").html("");

}
/***
 ** TODO: add description.
*/
function sleep(ms) {

	return new Promise(resolve => setTimeout(resolve, ms));

}
//función para quitar "pre-loader"
function jsRemoveWindowLoad() {
	// eliminar el div que bloquea la pantalla
	$("#WindowLoad").remove();

}
//función "pre-loader"
function jsShowWindowLoad() {

	jsRemoveWindowLoad();

	//centrar imagen gif
	height = 20;
	var ancho = 0;
	var alto = 0;

	//obtenemos el ancho y alto de la ventana de nuestro navegador, compatible con todos los navegadores
	if (window.innerWidth == undefined) ancho = window.screen.width;
	else ancho = window.innerWidth;
	if (window.innerHeight == undefined) alto = window.screen.height;
	else alto = window.innerHeight;

	//operación necesaria para centrar el div que muestra el mensaje
	var heightdivsito = alto / 2 - parseInt(height) / 2;//Se utiliza en el margen superior, para centrar

	//imagen que aparece mientras nuestro div es mostrado y da apariencia de cargando
	imgCentro = "<div style='text-align:center;height:" + alto + "px;'><div  style='color:#000;margin-top:" + heightdivsito + "px;'><img src='/resources/img/loader-Rolling.svg'></div>";  //<div class='preloader'></div>  <img src='img/loader-Rolling.gif'>

	//creamos el div que bloquea grande
	div = document.createElement("div");
	div.id = "WindowLoad"
	div.style.width = ancho + "px";
	div.style.height = alto + "px";
	$("body").append(div);

	//creamos un input text para que el foco se plasme en este y el usuario no pueda escribir en nada de atras
	input = document.createElement("input");
	input.id = "focusInput";
	input.type = "text"

	//asignamos el div que bloquea
	$("#WindowLoad").append(input);

	//asignamos el foco y ocultamos el input text
	$("#focusInput").focus();
	$("#focusInput").hide();

	//centramos el div del texto
	$("#WindowLoad").html(imgCentro);

}
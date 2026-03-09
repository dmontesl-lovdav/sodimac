/*------------------------------------------------*
 *   Copyright (c) SODIMAC. December 2020.        *
 *   Portal web: www.sodimac.com.mx.              *
 *   Contributors:                                *
 *   Vantis S. de RL. de CV. Web: vantis.mx       *
 *   Fabian Alvarez <fabian.alvarez@vantis.mx>    *
--------------------------------------------------*/
//CONSTANTS
//element principal select.
const select_1 = document.getElementById("idScursal");
const select_2 = document.getElementById("idTipo");
const select_3 = document.getElementById("idEstatus");
//element principal required.
const required_1 = $('#required-idSucursal');
const required_2 = $('#required-idTipo');
const required_3 = $('#required-idEstatus');
/***
 ** GET styles, functions that start during page load.
*/
$(document).ready(function() {

	//clean notify message
	toastr.clear();
	//styles for navigation bar.
	$(".nav-item").removeClass("active");
	$(".dropdown-item").removeClass("active");
	$("#navDispositivos").addClass("active");
	$('.mdb-select').materialSelect();
	$('.select-wrapper.md-form.md-outline input.select-dropdown').bind('focus blur', function() {
		$(this).closest('.select-outline').find('label').toggleClass('active');
		$(this).closest('.select-outline').find('.caret').toggleClass('active');
	});

});

jQuery(function() {

	$('input').on('click', function() {

		required_1.removeClass('visible');
		required_2.removeClass('visible');
		required_3.removeClass('visible');
	});

	$('#fragmentDiv').on('click', function() {

		required_1.removeClass('visible');
		required_2.removeClass('visible');
		required_3.removeClass('visible');
	});

	$("#edit-input-folio").keyup(function() {
		var $th = $(this);
		$th.val($th.val().replace(/[^A-Za-zñÑ0-9 ]/g, function() {
			toastr.warning("Puedes ingresar sólo números y letras (0-9)(Aa-Zz, ñ)");
			return '';
		}));
	});
	$("#filter").keyup(function() {
		var $th = $(this);
		$th.val($th.val().replace(/[^A-Za-zñÑ0-9 ]/g, function() {
			toastr.warning("Puedes ingresar sólo números y letras (0-9)(Aa-Zz, ñ)");
			return '';
		}));
	});

});

/*** -------------- START CONSULT section  -------------- ***/

/***
 ** REQUEST get data table CONSULT service to API.
 ** GET fragment page: fragments/devices :: tableDevices.
*/
function consult_device(fragment) {

	$('[data-toggle]').popover('dispose');
	$('.blck').prop('disabled', false);
	$('#fragmentDiv, li, select, #dtPdaTotem_filter').removeClass('prevent-click');

	//if value == "" then send notify required.
	if (select_1.value == "") {

		required_1.addClass('visible');
		return false;
	} else if (select_2.value == "") {

		required_2.addClass('visible');
		return false;
	} else if (select_3.value == "") {

		required_3.addClass('visible');
		return false;
	}
	//construct consult object.
	var objData = JSON.stringify({
		selectedIdBranchOffice: document.getElementById("idScursal").value,
		selectedIdTypeOfDevice: document.getElementById("idTipo").value,
		selectedIdStatus: document.getElementById("idEstatus").value,
		filter: document.getElementById("filter").value.trim()
	});
	//url controller.
	//var url = "/device/" + fragment;
	var url = "gcis/device/" + fragment;
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
	//Config dataTable devices.
	$('#dtPdaTotem').DataTable({
		"order": [[0, "asc"]],
		"aaSorting": [],
		columnDefs: [{
			orderable: false,
			targets: [6, 7],

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
/***
 ** GET styles and effects popover, buttons for activate / de activate device.
*/
function active_device(id, name, type) {

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
			create_popover_content();
			$("#mensajeActivacion").html("<span style='color: #006AB4!important;'>¿Deseas activar este dispositivo?</span>");
			$("#idUsuario").html(name);
			$("#botonesActivacion").html("<div class='btn btn-default btn-sm text-center' id='confirmActive-" + id + "'>Activar</div><div class='btn btn-outline-primary btn-sm text-center' id='denyActive-" + id + "'>Cancelar</div>");
			$('[data-toggle="' + id + '"]').popover("show");
			$('.blck').prop('disabled', true);
			$('#fragmentDiv').addClass('prevent-click');
		} else {
			create_popover_content();
			$("#mensajeActivacion").html("¿Deseas desactivar este dispositivo?");
			$("#idUsuario").html(name);
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
		switch_status(id, 1, "update");
		//if function succes then activate device.
		sleep(500).then(() => {
			if (switch_status) {

				toastr.success("Dispositivo activado");
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
		switch_status(id, 0, "update");
		//if function succes then de activate device.
		sleep(500).then(() => {
			if (switch_status) {

				toastr.info("Dispositivo desactivado");
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
		idDevice: id,
		idStatus: status
	});
	//url controller.
	//var url = "/device/" + fragment;
	var url = "gcis/device/" + fragment;
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
 ** CLEAN pop over, auxiliary function for button switch.
*/
function clean_popover() {

	$("#popover_content_wrapper").html("");

}
/***
 ** CLEAN notify element required.
*/
function clean_consult() {

	required_1.removeClass('visible');
	required_2.removeClass('visible');
	required_3.removeClass('visible');
}
/*** -------------- END CONSULT section -------------- ***/

/*** -------------- START CREATE section  -------------- ***/
/***
 ** GET fragment page: fragments/devices :: createDevice.
*/
function create_device(fragment) {

	$('[data-toggle]').popover('dispose');
	$('.blck').prop('disabled', false);
	$('#fragmentDiv, li, #dtPdaTotem_length, #dtPdaTotem_filter').removeClass('prevent-click');
	required_1.removeClass('visible');
	required_2.removeClass('visible');
	required_3.removeClass('visible');
	//url controller.
	//var url = "/device/" + fragment;
	var url = "gcis/device/" + fragment;
	$("#fragmentDiv").load(url);
	sleep(400).then(() => {
		document.getElementById("folio").focus();
	});

}
/***
 ** TODO: add description.
*/
function save_create(fragment) {

	$('#btn-AltaUsuarioDispositivo').prop('disabled', true);

	let form = document.getElementById("formCreateDevice");
	var select_4 = document.getElementById("selectedIdTipo");
	var select_5 = document.getElementById("selectedIdSucursal");
	var select_6 = document.getElementById("selectedIdZona");
	var input_1 = document.getElementById("folio");
	var input_2 = document.getElementById("nameDevice");
	var input_3 = document.getElementById("macAddress");
	var input_4 = document.getElementById("macAddressConf");

	var nDevice = input_2.value.trim();
	var nameDevice = nDevice.replace(/^\s+|\s+$|\s+(?=\s)/g, "");

	if (validate_create_form()) {

		//construct update object.
		var objData = JSON.stringify({
			TipoDispositivo: parseInt(select_4.value, 10),
			Sucursal: select_5.value,
			Zona: parseInt(select_6.value, 10),
			ClaveEquipo: input_1.value,
			NombreEquipo: nameDevice,
			MacAddress: input_4.value.toUpperCase()
		});
		//url controller.
		//var url = "/device/" + fragment;
		var url = "gcis/device/" + fragment;
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
							$('#btn-AltaUsuarioDispositivo').prop('disabled', false);
							break;
						case "2":

							toastr.info(response.messageGlobal);
							$('#btn-AltaUsuarioDispositivo').prop('disabled', false);
							break;
						case "3":

							toastr.warning(response.messageGlobal);
							$('#btn-AltaUsuarioDispositivo').prop('disabled', false);
							break;
						case "4":

							toastr.error(response.messageGlobal);
							$('#btn-AltaUsuarioDispositivo').prop('disabled', false);
							break;
						default:

							toastr.error(response.messageGlobal);
							$('#btn-AltaUsuarioDispositivo').prop('disabled', false);
							//toastr.error("Error desconocido");
							break;
					}
					return false;
				}

				toastr.success('Alta exitosa');
				//toastr.success(response.messageGlobal);
				$('#macAddress').removeClass("success-mac");
				$('#macAddressConf').removeClass("success-mac");
				create_device("create");

			},
			error: function(e) {
				toastr.error("Error de conexión");
				$('#btn-AltaUsuarioDispositivo').prop('disabled', false);
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

	const select_4 = document.getElementById("selectedIdTipo");
	const select_5 = document.getElementById("selectedIdSucursal");
	const select_6 = document.getElementById("selectedIdZona");
	const input_1 = document.getElementById("folio");
	const input_2 = document.getElementById("nameDevice");
	var input_3 = document.getElementById("macAddress");
	var input_4 = document.getElementById("macAddressConf");

	const required_4 = $('#required-create-idTipo');
	const required_5 = $('#required-create-idSucursal');
	const required_6 = $('#required-create-idZona');
	const required_7 = $('#required-create-folio');
	const required_8 = $('#required-create-nameDevice');
	const required_9 = $('#required-create-macAddress');
	const required_10 = $('#required-create-macAddressConf');

	var macOne = input_3.value.replace(/-/g, "");
	var macTwo = input_4.value.replace(/-/g, "");

	//if value == "" then send notify required.
	if (select_4.value == "") {

		required_4.addClass('visible');
		$('#btn-AltaUsuarioDispositivo').prop('disabled', false);
		return false;
	} else if (select_5.value == "") {

		required_5.addClass('visible');
		$('#btn-AltaUsuarioDispositivo').prop('disabled', false);
		return false;
	} else if (select_6.value == "") {

		required_6.addClass('visible');
		$('#btn-AltaUsuarioDispositivo').prop('disabled', false);
		return false;
	} else if (input_1.value == "") {

		required_7.addClass('visible');
		$('#btn-AltaUsuarioDispositivo').prop('disabled', false);
		return false;
	} else if (input_2.value == "") {

		required_8.addClass('visible');
		$('#btn-AltaUsuarioDispositivo').prop('disabled', false);
		return false;
	} else if (input_3.value == "") {

		required_9.addClass('visible');
		$('#btn-AltaUsuarioDispositivo').prop('disabled', false);
		return false;
	} else if (input_4.value == "") {

		required_10.addClass('visible');
		$('#btn-AltaUsuarioDispositivo').prop('disabled', false);
		return false;
	}

	//TODO: logic validate fields
	if (input_2.value.trim().length <= 0) {

		toastr.warning("No se permite el ingreso de únicamente espacios en blanco en: 'Nombre del dispositivo'");
		$('#btn-AltaUsuarioDispositivo').prop('disabled', false);
		return false
	}

	if (input_3.value != input_4.value) {

		toastr.warning("La MAC address  y su confirmación  no coinciden");
		$('#btn-AltaUsuarioDispositivo').prop('disabled', false);
		return false
	}

	const regex = /([a-fA-F0-9]{2}[:-]){5}[a-fA-F0-9]{2}/s;

	var macValid = regex.test(input_3.value);
	var macValid2 = regex.test(input_4.value);

	if (!macValid || !macValid2) {

		toastr.warning("El formato de la MAC address es incorrecta");
		$('#btn-AltaUsuarioDispositivo').prop('disabled', false);
		return false;
	}

	return true;
}
/***
 ** TODO: add description.
*/
function clean_required(type) {

	if (type == 'create') {

		var required_4 = $('#required-create-idTipo');
		var required_5 = $('#required-create-idSucursal');
		var required_6 = $('#required-create-idZona');
		var required_7 = $('#required-create-folio');
		var required_8 = $('#required-create-nameDevice');
		var required_9 = $('#required-create-macAddress');
		var required_10 = $('#required-create-macAddressConf');

		required_4.removeClass('visible');
		required_5.removeClass('visible');
		required_6.removeClass('visible');
		required_7.removeClass('visible');
		required_8.removeClass('visible');
		required_9.removeClass('visible');
		required_10.removeClass('visible');

		return false;

	} else if (type == 'edit') {

		var required_11 = $('#required-edit-idTipo');
		var required_12 = $('#required-edit-idSucursal');
		var required_13 = $('#required-edit-idZone');
		var required_14 = $('#required-edit-folio');
		var required_15 = $('#required-edit-name');
		var required_16 = $('#required-edit-mac');
		var required_17 = $('#required-edit-mactwo');

		required_11.removeClass('visible');
		required_12.removeClass('visible');
		required_13.removeClass('visible');
		required_14.removeClass('visible');
		required_15.removeClass('visible');
		required_16.removeClass('visible');
		required_17.removeClass('visible');

		return false;
	}

	return false;

}
/***
 ** TODO: add description.
*/
function cancel_create() {

	//toastr.info('Dispositivo cancelado');
	if (select_1.value == '' && select_2.value == '' && select_3.value == '') {

		window.location.reload();

		return false;
	}

	consult_device("consult");

}
/***
 ** TODO: add description.
*/
function validate_addmac(event, inp) {

	var mac_1 = $('#macAddress');
	var mac_2 = $('#macAddressConf');

	var key = window.Event ? event.which : event.keyCode;
	var v = inp.value;
	var l = v.length;
	var maxLen = 17 // Length of mac string including '-'

	if (mac_2.val().length >= 5) {

		if (mac_1.val() == mac_2.val()) {

			mac_1.removeClass("error-mac");
			mac_2.removeClass("error-mac");
			mac_1.addClass("success-mac");
			mac_2.addClass("success-mac");

		} else {

			mac_1.removeClass("success-mac");
			mac_2.removeClass("success-mac");
			mac_1.addClass("error-mac");
			mac_2.addClass("error-mac");
		}
	}

	//(key >= 65 && key <= 70) || (key >= 97 && key <= 102)
	//Backspace = 8, Enter = 13, Supr = 46.
	if (key == 8 || key == 13 || key == 46) {
		return true;
	}

	var x = inp.value;

	if (l >= 2 && l < maxLen) {
		var v1;
		v1 = v;

		while (!(v1.indexOf("-") < 0)) { // Better use RegEx
			v1 = v1.replace("-", "")
		}

		var ultimoCaracter = v.charAt(v.length - 2);
		var ultimoCaracter2 = v.charAt(v.length - 4);

		if (ultimoCaracter == "-") {
			inp.value = v;
		} else {
			if (ultimoCaracter2 == "-") {
				var cadenaCorregida = v.substring(0, v.length - 1);
				inp.value = cadenaCorregida + "-" + x.charAt(x.length - 1);
			} else {
				inp.value = v + "-";
			}
		}
	}

	var $th = $(inp);
	$th.val($th.val().replace(/[^A-Fa-f0-9\-]/g, function(str) {
		return '';
	}));
}
/***
 ** TODO: add description.
*/
function validate_editmac(event, inp) {

	var mac_1 = $('#edit-input-mac');
	var mac_2 = $('#edit-input-mactwo');

	var key = window.Event ? event.which : event.keyCode;
	var v = inp.value;
	var l = v.length;
	var maxLen = 17 // Length of mac string including '-'

	if (mac_2.val().length >= 5) {

		if (mac_1.val() == mac_2.val()) {

			mac_1.removeClass("error-mac");
			mac_2.removeClass("error-mac");
			mac_1.addClass("success-mac");
			mac_2.addClass("success-mac");

		} else {

			mac_1.removeClass("success-mac");
			mac_2.removeClass("success-mac");
			mac_1.addClass("error-mac");
			mac_2.addClass("error-mac");
		}
	}


	//Backspace = 8, Enter = 13, Supr = 46.
	if (key == 8 || key == 13 || key == 46) {
		return true;
	}

	var x = inp.value;

	if (l >= 2 && l < maxLen) {
		var v1;
		v1 = v;

		while (!(v1.indexOf("-") < 0)) { // Better use RegEx
			v1 = v1.replace("-", "")
		}

		var ultimoCaracter = v.charAt(v.length - 2);
		var ultimoCaracter2 = v.charAt(v.length - 4);

		if (ultimoCaracter == "-") {
			inp.value = v;
		} else {
			if (ultimoCaracter2 == "-") {
				var cadenaCorregida = v.substring(0, v.length - 1);
				inp.value = cadenaCorregida + "-" + x.charAt(x.length - 1);
			} else {
				inp.value = v + "-";
			}
		}
	}

	var $th = $(inp);
	$th.val($th.val().replace(/[^A-Fa-f0-9\-]/g, function(str) {
		return '';
	}));

}
/*** -------------- END CREATE section -------------- ***/


/***
 ** TODO: add description.
*/
function edit_device(id, type, store, zone, namedevice, mac) {

	//pass name user in tittle card body.
	$('.blck').prop('disabled', true);
	$('li, #dtPdaTotem_length, #dtPdaTotem_filter').addClass('prevent-click');
	var name = document.getElementById("name_device");
	name.innerHTML = "";
	name.innerHTML = id;
	//construct wrap list.
	$('.select-edit').materialSelect({
		destroy: true
	});
	$('.select-edit').materialSelect();
	$('.select-wrapper.md-form.md-outline input.select-dropdown').bind('focus blur', function() {
		$(this).closest('.select-outline').find('label').toggleClass('active');
		$(this).closest('.select-outline').find('.caret').toggleClass('active');
	});
	//pass values from user specific for edit.
	$('#edit-select-tipo').val(type);
	$('#edit-select-sucursal').val(store);
	$('#edit-select-zona').val(zone);
	$('#edit-input-folio').val(id);
	$('#hidden-input-folio').val(id);
	$('#edit-input-name').val(namedevice);
	$('#edit-input-mac').val(mac);
	$('#edit-input-mactwo').val(mac);
	//add class active to label form.
	$('.edicionDispositivos label').addClass("active");
	//validate fields
	$("#edit-input-folio").keyup(function() {
		var $th = $(this);
		$th.val($th.val().replace(/[^A-Za-z0-9ñÑ]/g, function(str) {
			toastr.warning("Puedes ingresar sólo números y letras (0-9)(Aa-Zz, ñ)");
			return '';
		}));
	});
	$("#edit-input-name").keyup(function() {
		var $th = $(this);
		$th.val($th.val().replace(/[^A-Za-z0-9ñÑ ]/g, function(str) {
			toastr.warning("Puedes ingresar sólo números y letras (0-9)(Aa-Zz, ñ)");
			return '';
		}));
	});
	//Disable cut copy paste
	$('#edit-input-mac').bind('cut copy paste', function(e) {
		e.preventDefault();
	});
	$('#edit-input-mactwo').bind('cut copy paste', function(e) {
		e.preventDefault();
	});
	//Disable mouse right click
	$('#edit-input-mac').on("contextmenu", function(e) {
		return false;
	});
	$('#edit-input-mactwo').on("contextmenu", function(e) {
		return false;
	});
	//responsive design for for the width of the screen.
	if ($('header').width() <= 767) {

		$('.edicionDispositivos').animate({
			opacity: 1,
			top: "0px",
		}, 300).show();

	}
	else if ($('header').width() >= 768) {

		$('.edicionDispositivos').animate({
			opacity: 1,
			top: "108px",
		}, 300).show();

	}

}
/***
 ** TODO: add description.
*/
function validate_mac(type) {

	if (type == 'create') {

		var mac_1 = $('#macAddress');
		var mac_2 = $('#macAddressConf');

		if (mac_2.val().length >= 5) {

			if (mac_1.val() == mac_2.val()) {

				mac_1.removeClass("error-mac");
				mac_2.removeClass("error-mac");
				mac_1.addClass("success-mac");
				mac_2.addClass("success-mac");

			} else {

				mac_1.removeClass("success-mac");
				mac_2.removeClass("success-mac");
				mac_1.addClass("error-mac");
				mac_2.addClass("error-mac");
			}

		}

		return false;

	} else if (type == 'edit') {

		var mac_1 = $('#edit-input-mac');
		var mac_2 = $('#edit-input-mactwo');

		if (mac_2.val().length >= 5) {

			if (mac_1.val() == mac_2.val()) {

				mac_1.removeClass("error-mac");
				mac_2.removeClass("error-mac");
				mac_1.addClass("success-mac");
				mac_2.addClass("success-mac");

			} else {

				mac_1.removeClass("success-mac");
				mac_2.removeClass("success-mac");
				mac_1.addClass("error-mac");
				mac_2.addClass("error-mac");
			}

		}

		return false;
	}

	return false;

}
/***
 ** TODO: add description.
*/
function edit_service(fragment) {

	var select_7 = document.getElementById("edit-select-tipo");
	var select_8 = document.getElementById("edit-select-sucursal");
	var select_9 = document.getElementById("edit-select-zona");
	var input_5 = document.getElementById("edit-input-folio");
	var input_6 = document.getElementById("edit-input-name");
	var input_7 = document.getElementById("hidden-input-folio");
	var input_8 = document.getElementById("edit-input-mactwo");

	var nDevice = input_6.value.trim();
	var nameDevice = nDevice.replace(/^\s+|\s+$|\s+(?=\s)/g, "");

	if (validate_edit_form()) {

		//construct update object.
		var objData = JSON.stringify({
			TipoDispositivo: parseInt(select_7.value, 10),
			Sucursal: select_8.value,
			Zona: parseInt(select_9.value, 10),
			ClaveChange: input_5.value,
			ClaveEquipo: input_7.value,
			NombreEquipo: nameDevice,
			MacAddress: input_8.value.toUpperCase()
		});
		//url controller.
		//var url = "/device/" + fragment;
		var url = "gcis/device/" + fragment;
		//ajax request.
		$.ajax({
			type: "PUT",
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

				$('.blck').prop('disabled', false);
				toastr.success('Edición exitosa');
				//toastr.success(response.messageGlobal);
				consult_device('consult');

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

	const select_7 = document.getElementById("edit-select-tipo");
	const select_8 = document.getElementById("edit-select-sucursal");
	const select_9 = document.getElementById("edit-select-zona");
	const input_5 = document.getElementById("edit-input-folio");
	const input_6 = document.getElementById("edit-input-name");
	var input_7 = document.getElementById("edit-input-mac");
	var input_8 = document.getElementById("edit-input-mactwo");

	const required_11 = $('#required-edit-idTipo');
	const required_12 = $('#required-edit-idSucursal');
	const required_13 = $('#required-edit-idZone');
	const required_14 = $('#required-edit-folio');
	const required_15 = $('#required-edit-name');
	const required_16 = $('#required-edit-mac');
	const required_17 = $('#required-edit-mactwo');

	//if value == "" then send notify required.
	if (select_7.value == "") {

		required_11.addClass('visible');
		return false;
	} else if (select_8.value == "") {

		required_12.addClass('visible');
		return false;
	} else if (select_9.value == "") {

		required_13.addClass('visible');
		return false;
	} else if (input_5.value == "") {

		required_14.addClass('visible');
		return false;
	} else if (input_6.value == "") {

		required_15.addClass('visible');
		return false;
	} else if (input_7.value == "") {

		required_16.addClass('visible');
		return false;
	} else if (input_8.value == "") {

		required_17.addClass('visible');
		return false;
	}

	//TODO: logic validate fields
	if (input_6.value.trim().length <= 0) {

		toastr.warning("No se permite el ingreso de únicamente espacios en blanco en: 'Nombre del dispositivo'");
		return false
	}

	if (input_7.value != input_8.value) {

		toastr.warning("La MAC address  y su confirmación  no coinciden");
		return false
	}

	const regex = /([a-fA-F0-9]{2}[:-]){5}[a-fA-F0-9]{2}/s;

	var macValid = regex.test(input_7.value);
	var macValid2 = regex.test(input_8.value);

	if (!macValid || !macValid2) {

		toastr.warning("El formato de la MAC address es incorrecta");
		return false;
	}

	return true;

}
/***
 ** TODO: add description.
*/
function cancel_edit() {

	var mac_1 = $('#edit-input-mac');
	var mac_2 = $('#edit-input-mactwo');

	mac_1.removeClass("error-mac");
	mac_2.removeClass("error-mac");
	mac_1.removeClass("success-mac");
	mac_2.removeClass("success-mac");

	$('.blck').prop('disabled', false);
	$('li, #dtPdaTotem_length, #dtPdaTotem_filter').removeClass('prevent-click');
	$('.edicionDispositivos').animate({
		opacity: 0,
		top: "100px",
	}, 300).hide();
	//toastr.info('Edición cancelada');

}
/***
 ** TODO: add description.
*/
function sleep(ms) {

	return new Promise(resolve => setTimeout(resolve, ms));

}
/***
 ** TODO: add description.
*/
function vDroPas(control) {

	control.preventDefault();
	return;
}
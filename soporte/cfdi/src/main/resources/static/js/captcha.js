/**-------------------------------------------------
 * Simple Captcha System
 * @package Code Snippets
 * @link http://rhythmshahriar.com/codes/
 * @author Rhythm Shahriar <rhy@rhythmshahriar.com>
 * @link http://rhythmshahriar.com
 * @copyright Copyright © 2017, Rhythm Shahriar
 ---------------------------------------------------*/
 
//generate captcha
function generateCaptcha(length, chars) {
    var result = '';
    for (var i = length; i > 0; --i) result += chars[Math.round(Math.random() * (chars.length - 1))];
    return result;
}

//default captcha
$('.dynamic-code').text(generateCaptcha(4, '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ'));

$('.captcha-reload').on('click', function () {
    $('.dynamic-code').text(generateCaptcha(4, '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ'));
});

//check captcha
$('#captcha-input').on('change', function () {
    if($(this).val() != $('.dynamic-code').text()){
        $('#errCaptcha').html('<span style="color: red;"><i class="ion-close"></i> Captcha no válido</span>');
        $(this).val('');
    }else {
        $('#errCaptcha').html('<span style="color: green;">Captcha válido</span>');
        document.getElementById("captcha-input").classList.add("full");
        $('#btnBuscarFactura').prop("disabled", false);
//        document.getElementById("btnBuscarFactura_genera_factura").disabled = false;
        var rfc = $("#rfcInput_consulta_factura").hasClass("has-success");
        var ticket_orden = $("#ticketHolder_consulta_factura").hasClass("has-success");

     /*   if (rfc && ticket_orden ){
            document.getElementById("btnBuscarFactura_genera_factura").disabled = false;            
        } else {
            alert("no se van");
        }*/
    }
});

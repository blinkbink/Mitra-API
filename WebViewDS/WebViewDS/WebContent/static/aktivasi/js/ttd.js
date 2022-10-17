$("#formSukses").hide();
var canvas = document.getElementById('signature-pad');
var clearButton = document.getElementById('clear');

var yes = 0;
var sgn = document.getElementById('sgn');
var esgn = document.getElementById('e_sgn');
var user = null;
$("#disabledbtn").hide();

var scoreuserdb = 1;

function usernameCheck(link, refTrx)
{
    var upperCase= new RegExp('[^a-z][^0-9]');
    var usernameData = new FormData();
    usernameData.append('username', $("#username").val());
    usernameData.append('refTrx', refTrx);
    $( "#username" ).val($( "#username" ).val().toLowerCase());

    var format = /[!@#$%^&*()+\-=\[\]{};':"\\|,<>\/?]+/;

    $.ajax({
        type: "POST",
        url: link+"/CheckUsername.html",
        contentType: false,
        processData: false,
        data: usernameData,
        success: function(data, status, jqXHR){

            if(data == 100 && $( "#username" ).val().length > 5 && $('#username').val().indexOf(' ')<0 && !format.test($( "#username" ).val()) && $( "#username" ).val().slice(-1) != "." && $( "#username" ).val().slice(-1) != "_" && $( "#username" ).val().charAt(0) != "." && $( "#username" ).val().charAt(0) != "_")
            {
                user = 0;
                $("#same_name").text("");
                $("#username").css("background-color","white");
                $("#notsame_name").closest('.form-group').removeClass('has-error has-feedback').addClass('has-success has-feedback');
                document.getElementById("notsame_name").innerHTML="<span class=\"glyphicon glyphicon-ok form-control-feedback\"></span>";

            }
            else if(data == 200 && $( "#username" ).val().length > 5)
            {
                user = 1;
                $("#same_name").text("Username sudah digunakan");
                $("#username").css("background-color","white");
                $("#notsame_name").closest('.form-group').removeClass('has-success has-feedback').addClass('has-error has-feedback');
                document.getElementById("notsame_name").innerHTML="<span class=\"glyphicon glyphicon-remove form-control-feedback\"></span>";
            }


        },
        error: function(jqXHR, textStatus, errorThrown){
            user = 1;

            $("#same_name").text("Error : " + jqXHR.status);
        }
    });

    if($( "#username" ).val() == "")
    {
        user = 1;
        $("#null_name").text("Harus diisi");
        $("#same_name").text("");
        $("#notsame_name").text("");
    }
    else if($( "#username" ).val().length < 6)
    {
        user = 1;
        $("#null_name").text("Panjang karakter min 6, maks 15 karakter.");
        $("#same_name").text("");
        $("#username").css("background-color","white");
        $("#notsame_name").closest('.form-group').removeClass('has-success has-feedback').addClass('has-error has-feedback');
        document.getElementById("notsame_name").innerHTML="<span class=\"glyphicon glyphicon-remove form-control-feedback\"></span>";
    }
    else if($('#username').val().indexOf(' ')>=0)
    {
    	user = 1;
        $("#null_name").text("Tidak boleh ada spasi");
        $("#same_name").text("");
        $("#notsame_name").closest('.form-group').removeClass('has-success has-feedback').addClass('has-error has-feedback');
        document.getElementById("notsame_name").innerHTML="<span class=\"glyphicon glyphicon-remove form-control-feedback\"></span>";
    }
    else if(format.test($( "#username" ).val())){
    	user = 1;
        $("#null_name").text("Karakter tidak valid");
        $("#same_name").text("");
        $("#notsame_name").closest('.form-group').removeClass('has-success has-feedback').addClass('has-error has-feedback');
        document.getElementById("notsame_name").innerHTML="<span class=\"glyphicon glyphicon-remove form-control-feedback\"></span>";
    }
    else if($( "#username" ).val().slice(-1) == "." || $( "#username" ).val().slice(-1) == "_"){
    	user = 1;
        $("#null_name").text("Username tidak valid");
        $("#same_name").text("");
        $("#notsame_name").closest('.form-group').removeClass('has-success has-feedback').addClass('has-error has-feedback');
        document.getElementById("notsame_name").innerHTML="<span class=\"glyphicon glyphicon-remove form-control-feedback\"></span>";
    }
    else if($( "#username" ).val().charAt(0) == "." || $( "#username" ).val().charAt(0) == "_"){
    	user = 1;
        $("#null_name").text("Username tidak valid");
        $("#same_name").text("");
        $("#notsame_name").closest('.form-group').removeClass('has-success has-feedback').addClass('has-error has-feedback');
        document.getElementById("notsame_name").innerHTML="<span class=\"glyphicon glyphicon-remove form-control-feedback\"></span>";
    }
    else
    {
        user = 0;
        $("#null_name").text("");
    }
}

$( "#password" ).keyup(function() {
    if($( "#password" ).val() == "")
    {
        scorePassword = 1;
        $("#null_password").text("Harus diisi");
    }
    else if($( "#password" ).val().length < 6)
    {
        scorePassword = 1;
    }
    else
    {
        scorePassword = 0;
        $("#null_password").text("");
    }
});

$( "#password2" ).keyup(function() {
    if($( "#password2" ).val() == "")
    {
        scorePassword2 = 1;
        $("#null_password2").text("Harus diisi");
    }
    else
    {
        scorePassword2 = 0;
        $("#null_password2").text("");
    }


    if($("#password").val() != $("#password2").val())
    {
        scoreConfirm = 1;
        $("#null_confirm").text("Kata sandi tidak sama");
    }
    else
    {
        scoreConfirm = 0;
        $("#null_confirm").text("");
    }
});


function prosesaktivasi(link)
{

    var scoreUsername = null;
    var scorePassword = null;
    var scorePassword2 = null;
    var scoreTtd = null;
    var scoreSgn = null;
    var upperCase= new RegExp('[^a-z][^0-9]');
    var format = /[!@#$%^&*()+\-=\[\]{};':"\\|,<>\/?]+/;

    if($("#i_ttd").val() != "")
    {
        scoreTtd = 0;
        scoreSgn = 0;
    }
    else
    {
        if($("#i_ttd").val() == "")
        {
            if($("#sgn").val() == "")
            {
                scoreTtd = 1;
                scoreSgn = 1;
                $("#null_sgn").text("");
            }
            else
            {
                scoreTtd = 0;
                scoreSgn = 0;
                $("#null_sgn").text("");
            }
        }
    }

    if($("#username").val() == "")
    {
        scoreUsername = 1;
        $("#null_name").text("Harus diisi");
    }
    else if ($("#username").val().length < 6)
    {
        scoreUsername = 1;
        $("#null_name").text("Panjang karakter min 6, maks 15 karakter.");
    }
    else if($('#username').val().indexOf(' ')>=0)
    {
        scoreUsername = 1;
        $("#null_name").text("Username can't contain space");
    }
    else if(format.test($( "#username" ).val())){
        scoreUsername = 1;
        $("#null_name").text("Karakter tidak valid");
    }
    else
    {
        scoreUsername = 0;
        $("#null_name").text("");
    }

    if($("#password").val() == "")
    {
        scorePassword = 1;
        $("#null_password").text("Harus diisi");
    }
    else
    {
        scorePassword = 0;
        $("#null_password").text("");
    }

    if($("#password2").val() == "")
    {
        scorePassword2 = 1;
        $("#null_password2").text("Harus diisi");
    }
    else
    {
        scorePassword2 = 0;
        $("#null_password2").text("");
    }

    if($("#password").val() != $("#password2").val())
    {
        scoreConfirm = 1;
        $("#null_confirm").text("Kata sandi tidak sama");
    }
    else
    {
        scoreConfirm = 0;
        $("#null_confirm").text("");
    }

    if(user == 0 && scoreUsername == 0 && scorePassword == 0 && scorePassword2 == 0 && scoreConfirm == 0 && scoreTtd == 0 && scoreSgn == 0 && str >= 34)
    {
        $("#otpModal").modal('show');

        if (!$("#btnotp").attr('disabled'))
        {
            $("#btnotp").click();
        }
    }
}

$( "#prosesOTP" ).click(function() {
    document.getElementById("btnotp").disabled = true;
});

function submitOTP(link, refTrx)
{
    $("#e_proses").text("");
    $("#prosesOTP").button('loading');
    var dataOTP = new FormData();
    
    dataOTP.append('nohp', $("#nohp").val());
    dataOTP.append('otpcode', $("#otp").val());
    dataOTP.append('refTrx', refTrx);

    $.ajax({
        type: "POST",
        url: link+"/CheckOTP.html",
        contentType: false,
        processData: false,
        data: dataOTP,
        success: function(data, status, jqXHR){
        	var result = JSON.parse(data);
			
			if(result.rc == "00")
			{
				$("#e_otp").text("");
	            $("#disabledbtn").show();
	            $("#prosesOTP").button("reset");
	            document.getElementById("btnotp").disabled = true;
	            document.getElementById("prosesOTP").disabled = true;

	            $("#prosesaktivasi").hide();
	            $("#reset").hide();
	            setTimeout(function() {
	                $("#otpModal").modal('hide');
	            }, 700);

	            setTimeout(function() {
	                $("#loadingModal").modal('show');
	            }, 1000);

	            var formData = new FormData();

	            var canvas = document.getElementById('signature-pad');
	            var context = canvas.getContext('2d');
	            var dataURL = canvas.toDataURL();

	            var blobBin = atob(dataURL.split(',')[1]);
	            var array = [];
	            for(var i = 0; i < blobBin.length; i++) {
	                array.push(blobBin.charCodeAt(i));
	            }
	            var file=new Blob([new Uint8Array(array)], {type: 'image/png'});

	            formData.append('nohp', $("#nohp").val());
	            formData.append('username', $("#username").val());
	            formData.append('password', hex_md5($("#username").val()+$("#password").val()));
	            
	            var sk = document.getElementById('sk');
	            var se = document.getElementById('se');

	            formData.append('sk', sk.checked);
	            formData.append('se', se.checked);
	            formData.append('fttd', file);
	            formData.append('refTrx', refTrx);

	            $.ajax({
	                type: "POST",
	                url: link+"/AktivasiMitra.html",
	                contentType: false,
	                processData: false,
	                data: formData,
	                success: function(data, status, jqXHR){

	                    var result = JSON.parse(data);

	                    if(result.rc == "00")
	                    {
	                        setTimeout(function()
	                        {
	                            $("#loadingModal").modal('hide');
	                        }, 2100);

	                        $("#disabledbtn").val("Aktivasi Berhasil");

	                        setTimeout(function() {
	                            $("#formAktivasi").hide();
	                            $("#formSukses").show();
	                        }, 2500);

	                        $("#prosesaktivasi").hide();
	                        $("#reset").hide();

	                        window.parent.postMessage(data, '*');
	                    }
	                    else
	                    {
	                        document.getElementById("btnotp").disabled = false;
	                        document.getElementById("prosesOTP").disabled = false;

	                        $("#otpModal").modal('hide');
	                        $("#prosesmessage").text(result.notif);
	                        $("#loadingModal").modal('hide');
	                        setTimeout(function() {
	                            $('#notifproses').modal('show');
	                            $("#loadingModal").modal('hide');
	                        }, 1600);
	                        $("#formAktivasi").show();
	                        $("#formSukses").hide();
	                        $("#prosesaktivasi").show();
	                        $("#reset").show();
	                        $("#disabledbtn").hide();
	                        
	                        if(result.rc == "06")
	                        {
	                            //alert("Username sudah digunakan");
	                            user = 1;
	                            $("#same_name").text("Username sudah digunakan");
	                            $("#username").css("background-color","white");
	                            $("#notsame_name").closest('.form-group').removeClass('has-success has-feedback').addClass('has-error has-feedback');
	                            document.getElementById("notsame_name").innerHTML="<span class=\"glyphicon glyphicon-remove form-control-feedback\"></span>";
	                        }
	                    }

	                },

	                error: function(jqXHR, textStatus, errorThrown){

	                    document.getElementById("btnotp").disabled = false;
	                    document.getElementById("prosesOTP").disabled = false;

	                    $("#otpModal").modal('hide');
	                    $("#prosesmessage").text(jqXHR.status);
	                    $("#loadingModal").modal('hide');
	                    setTimeout(function() {
	                        $('#notifproses').modal('show');
	                        $("#loadingModal").modal('hide');
	                    }, 1600);
	                    $("#formAktivasi").show();
	                    $("#formSukses").hide();
	                    $("#prosesaktivasi").show();
	                    $("#reset").show();
	                    $("#disabledbtn").hide();
	                }
	            });
			}
			else
			{
				document.getElementById("btnotp").disabled = false;
	            $("#prosesOTP").button("reset");
	            $("#e_otp").text("OTP Salah");	
			}
        },
        error: function(jqXHR, textStatus, errorThrown){

            document.getElementById("btnotp").disabled = false;
            $("#prosesOTP").button("reset");
            $("#e_otp").text("OTP Salah");
        }
    });
}


function submitOTPEmail(link, refTrx)
{
    var dataOTP = new FormData();
    
    dataOTP.append('nohp', $("#nohp").val());
    dataOTP.append('otpcode', $("#verEmail").val());
    dataOTP.append('type', "email");
    dataOTP.append('refTrx', refTrx);

    $.ajax({
        type: "POST",
        url: link+"/CheckOTP.html",
        contentType: false,
        processData: false,
        data: dataOTP,
        success: function(data, status, jqXHR){
        	var result = JSON.parse(data);
			
			if(result.rc == "00")
			{
	            document.getElementById("btnotpEmail").disabled = true;
	            document.getElementById("prosesOTPEmail").disabled = true;
	            
	            
	        	$("#verifEmailModal").modal('hide');
	        	$("#verifEmail").removeClass("btn btn-warning");
	        	$("#verifEmail").addClass("btn btn-info");
	        	document.getElementById("verifEmail").innerText = "Terverifikasi"
	        	document.getElementById("verifEmail").disabled = true;
	        		
	        	Swal.fire({
	        		  position: 'center',
	        		  icon: 'success',
	        		  title: 'Email Berhasil Diverifikasi',
	        		  showConfirmButton: true,
	        		  confirmButtonColor: '#3085d6'
	        		})
        		
        		verifikasiemail = true;
      
    			if(prosessekeotp.checked && proseskeotp.checked && verifikasiemail)
    			{
    				proseskeotpbtn.disabled = false;
    			}
    			else
    			{
    				proseskeotpbtn.disabled = true;
    			}
			}
			else
			{
				document.getElementById("btnotpEmail").disabled = false;
	            document.getElementById("prosesOTPEmail").disabled = false;
	            $("#e_verEmail").text("OTP Salah");
	            $("#notif_verEmail").text("");	
	            
			}
        },
        error: function(jqXHR, textStatus, errorThrown){

            document.getElementById("btnotp").disabled = false;
            document.getElementById("prosesOTPEmail").disabled = false;
            $("#e_verEmail").text("Koneksi error, silahkan coba kembali");
            $("#notif_verEmail").text("");
        }
    });
}


function dataURItoBlob(dataURI) {
// convert base64/URLEncoded data component to raw binary data held in a string
    var byteString;
    if (dataURI.split(',')[0].indexOf('base64') >= 0)
        byteString = atob(dataURI.split(',')[1]);
    else
        byteString = unescape(dataURI.split(',')[1]);
// separate out the mime component
    var mimeString = dataURI.split(',')[0].split(':')[1].split(';')[0];
// write the bytes of the string to a typed array
    var ia = new Uint8Array(byteString.length);
    for (var i = 0; i < byteString.length; i++) {
        ia[i] = byteString.charCodeAt(i);
    }
    return new Blob([ia], {type:mimeString});
}

var signaturePad = new SignaturePad(canvas, {
// It's Necessary to use an opaque color when saving image as JPEG;
// this option can be omitted if only saving as PNG or SVG
// backgroundColor: 'rgb(0, 0, 0)'
});

// Adjust canvas coordinate space taking into account pixel ratio,
// to make it look crisp on mobile devices.
// This also causes canvas to be cleared.
function resizeCanvas() {
    // When zoomed out to less than 100%, for some very strange reason,
    // some browsers report devicePixelRatio as less than 1aa
    // and only part of the canvas is cleared then.



    var ratio = Math.max(window.devicePixelRatio || 1, 1);



    // This part causes the canvas to be cleared
    canvas.width = canvas.offsetWidth * 1;
    canvas.height = canvas.offsetHeight * 1;
    canvas.getContext("2d").scale(1, 1);

    // This library does not listen for canvas changes, so after the canvas is automatically
    // cleared by the browser, SignaturePad#isEmpty might still return false, even though the
    // canvas looks empty, because the internal data of this library wasn't cleared. To make sure
    // that the state of this library is consistent with visual state of the canvas, you
    // have to clear it manually.
    signaturePad.clear();
}

// On mobile devices it might make more sense to listen to orientation change,
// rather than window resize events.
//window.onresize = resizeCanvas;
resizeCanvas();

function getBase64Image(img) {
    var canvas = document.createElement("canvas");
    canvas.width = img.width;
    canvas.height = img.height;
    var ctx = canvas.getContext("2d");
    ctx.drawImage(img, 0, 0, img.width, img.height);
    var dataURL = canvas.toDataURL("image/png");
    return dataURL;
}


$('#signModal').on('shown.bs.modal', function () {
    if (yes == 0){
        canvas.width = canvas.offsetWidth * 1;
        canvas.height = canvas.offsetHeight * 1;
        yes = 1 ;

    }
})


$('#signModal').on('hidden.bs.modal', function () {

    if (signaturePad.isEmpty()) {
        $("#sgn").closest('.form-group').removeClass('has-success has-feedback').addClass('has-error has-feedback');
        error = "<span class=\"glyphicon glyphicon-remove form-control-feedback\"></span>Harus diisi\n";
        esgn.innerHTML=error;
        sgn.value = "" ;

    }else {
        sgn.value = "success" ;
        $("#sgn").closest('.form-group').removeClass('has-error has-feedback').addClass('has-success has-feedback');
        esgn.innerHTML="<span class=\"glyphicon glyphicon-ok form-control-feedback\"></span>";

    }


})


function updateTtd(email) {

    var dataURL = signaturePad.toDataURL();
    var blob = dataURLToBlob(dataURL);

    var formData = new FormData();
    formData.append('email', email);
    formData.append("updatettd", blob, "signature.png");

    $.ajax({
        type : "POST",
        url : "/process/sourceTtd.html",
        data : formData,
        processData : false,
        contentType : false,
        cache : false,
        success : function(result) {
            if (result) {
                var obj = jQuery.parseJSON(result);
                if (obj.status == "OK") {
                    $("#sts_mdl").text("Ubah Tanda Tangan");
                    $('#modal-header').attr('class',
                        'modal-header modal-primary');
                    $("#text_mdl").html("Tanda Tangan Berhasil di Perbarui");
                    $("#cModal").attr('onclick',
                        "window.location=\"changeTtd.html\"");
                    $("#bModal").attr('onclick',
                        "window.location=\"changeTtd.html\"");
                    $('#myPleaseWait').modal('hide');
                    $('#myModal').modal('show');
                } else {
                    $("#sts_mdl").text("Gagal");
                    $('#modal-header').attr('class','modal-header modal-danger');
                    $("#text_mdl").html("Tanda Tangan Berhasil Gagal di Perbarui");
                    $('#myPleaseWait').modal('hide');
                    $('#myModal').modal('show');
                }
            }
        }
    });

}

function saveEktp(email) {

    var base64 = getBase64Image(document.getElementById("loadektp"));
    var blob = dataURLToBlob(base64);

    var formData = new FormData();
    formData.append('frmProcess', 'saveEktp');
    formData.append("blob", blob, "signature.png");
    formData.append('email', email);

    $.ajax({
        type : "POST",
        url : "/doc/ttd.html",
        data : formData,
        processData : false,
        contentType : false,
        cache : false,
        success : function(result) {
            if (result) {
                var obj = jQuery.parseJSON(result);
                if (obj.status == "OK") {
                    $("#sts_mdl").text("Verikasi No HP");
                    $('#modal-header').attr('class',
                        'modal-header modal-primary');
                    $("#text_mdl").html(
                        "No. HP berhasil diverifikasi. Terima Kasih");
                    $("#cModal").attr('onclick',
                        "window.location=\"verification.html\"");
                    $("#bModal").attr('onclick',
                        "window.location=\"verification.html\"");
                    $('#myPleaseWait').modal('hide');
                    $('#myModal').modal('show');
                } else {
                    $("#sts_mdl").text("Gagal");
                    $('#modal-header').attr('class',
                        'modal-header modal-danger');
                    $("#text_mdl").html(obj.status);
                    $('#myModal').modal('show');
                }
            }
        }
    });

}

function saveNpwp(email) {

    var base64 = getBase64Image(document.getElementById("loadnpwp"));
    var blob = dataURLToBlob(base64);

    var formData = new FormData();
    formData.append('frmProcess', 'saveNpwp');
    formData.append("blob", blob, "signature.png");
    formData.append('email', email);

    $.ajax({
        type : "POST",
        url : "/doc/ttd.html",
        data : formData,
        processData : false,
        contentType : false,
        cache : false,
        success : function(result) {
            if (result) {
                var obj = jQuery.parseJSON(result);
                if (obj.status == "OK") {
                    $("#sts_mdl").text("Verikasi No HP");
                    $('#modal-header').attr('class',
                        'modal-header modal-primary');
                    $("#text_mdl").html(
                        "No. HP berhasil diverifikasi. Terima Kasih");
                    $("#cModal").attr('onclick',
                        "window.location=\"verification.html\"");
                    $("#bModal").attr('onclick',
                        "window.location=\"verification.html\"");
                    $('#myPleaseWait').modal('hide');
                    $('#myModal').modal('show');
                } else {
                    $("#sts_mdl").text("Gagal");
                    $('#modal-header').attr('class',
                        'modal-header modal-danger');
                    $("#text_mdl").html(obj.status);
                    $('#myModal').modal('show');
                }
            }
        }
    });

}

function saveSelfie(email) {

    var base64 = getBase64Image(document.getElementById("loadselfie"));
    var blob = dataURLToBlob(base64);
    var formData = new FormData();
    formData.append('frmProcess', 'saveSelfie');
    formData.append("blob", blob, "signature.png");
    formData.append('email', email);

    $.ajax({
        type : "POST",
        url : "/doc/ttd.html",
        data : formData,
        processData : false,
        contentType : false,
        cache : false,
        success : function(result) {
            if (result) {
                var obj = jQuery.parseJSON(result);
                if (obj.status == "OK") {
                    $("#sts_mdl").text("Verikasi No HP");
                    $('#modal-header').attr('class',
                        'modal-header modal-primary');
                    $("#text_mdl").html(
                        "No. HP berhasil diverifikasi. Terima Kasih");
                    $("#cModal").attr('onclick',
                        "window.location=\"verification.html\"");
                    $("#bModal").attr('onclick',
                        "window.location=\"verification.html\"");
                    $('#myPleaseWait').modal('hide');
                    $('#myModal').modal('show');
                } else {
                    $("#sts_mdl").text("Gagal");
                    $('#modal-header').attr('class',
                        'modal-header modal-danger');
                    $("#text_mdl").html(obj.status);
                    $('#myModal').modal('show');
                }
            }
        }
    });

}
// One could simply use Canvas#toBlob method instead, but it's just to show
// that it can be done using result of SignaturePad#toDataURL.
function dataURLToBlob(dataURL) {
    // Code taken from https://github.com/ebidel/filer.js
    var parts = dataURL.split(';base64,');
    var contentType = parts[0].split(":")[1];
    var raw = window.atob(parts[1]);
    var rawLength = raw.length;
    var uInt8Array = new Uint8Array(rawLength);

    for (var i = 0; i < rawLength; ++i) {
        uInt8Array[i] = raw.charCodeAt(i);
    }

    return new Blob([ uInt8Array ], {
        type : contentType
    });
}

clearButton.addEventListener("click", function(event) {
    signaturePad.clear();
});




//savePNGButton.addEventListener("click", function (event) {
//  if (signaturePad.isEmpty()) {
//    alert("Please provide a signature first.");
//  } else {
//    var dataURL = signaturePad.toDataURL();
//  download(dataURL, "signature.png");
//  }
//});



// $("#formSukses").hide();
// var canvas = document.getElementById('signature-pad');
// var clearButton = document.getElementById('clear');
//
// var yes = 0;
// var sgn = document.getElementById('sgn');
// var esgn = document.getElementById('e_sgn');
// $("#disabledbtn").hide();
//
// var scoreuserdb = 1; //<!----------------------------------->>>>> update
//
// $( "#username" ).keyup(function() {
//     var upperCase= new RegExp('[^a-z][^0-9]');    //<!----------------------------------->>>>> update
//     var usernameData = new FormData();
//     usernameData.append('username', $("#username").val());
//     this.value = this.value.toLowerCase();    //<!----------------------------------->>>>> update
//
//     var format = /[!@#$%^&*()+\-=\[\]{};':"\\|,<>\/?]+/;  //<!----------------------------------->>>>> update
//
//     $.ajax({
//         type: "POST",
//         url: "https://api.tandatanganku.com/CheckUsername.html",
//         contentType: false,
//         processData: false,
//         data: usernameData,
//         success: function(data, status, jqXHR){
//
//             if(data == 100 && $( "#username" ).val().length >= 6 && $('#username').val().indexOf(' ')<0 && !format.test($( "#username" ).val()))  //<!----------------------------------->>>>> update
//             {
//                 scoreuserdb = 0; //<!----------------------------------->>>>> update
//                 scoreUsername = 0;
//                 $("#same_name").text("");
//                 $("#notsame_name").text("OK");
//             }
//             else if(data == 200 && $( "#username" ).val().length >= 6)
//             {
//                 scoreUsername = 1;
//                 $("#same_name").text("Username sudah digunakan");
//                 $("#notsame_name").text("");
//             }
//
//         },
//         error: function(jqXHR, textStatus, errorThrown){
//             $("#same_name").text("Error : " + jqXHR.status);
//         }
//     });
//
//
//     if($( "#username" ).val() == "")
//     {
//         scoreUsername = 1;
//         $("#null_name").text("Harus diisi");
//         $("#same_name").text("");
//         $("#notsame_name").text("");
//     }
//     else if($( "#username" ).val().length < 6)
//     {
//         scoreUsername = 1;
//         $("#null_name").text("Panjang karakter min 6, maks 15 karakter.");
//         $("#same_name").text("");
//         $("#notsame_name").text("");
//     }
//     else if($('#username').val().indexOf(' ')>=0) //<!----------------------------------->>>>> update
//     {
//         scoreUsername = 1; //<!----------------------------------->>>>> update
//         $("#null_name").text("Username can't contain space"); //<!----------------------------------->>>>> update
//         $("#same_name").text(""); //<!----------------------------------->>>>> update
//         $("#notsame_name").text(""); //<!----------------------------------->>>>> update
//     }
//     else if(format.test($( "#username" ).val())){//<!----------------------------------->>>>> update
//         scoreUsername = 1;  //<!----------------------------------->>>>> update
//         $("#null_name").text("Karakter tidak valid"); //<!----------------------------------->>>>> update
//         $("#same_name").text(""); //<!----------------------------------->>>>> update
//         $("#notsame_name").text(""); //<!----------------------------------->>>>> update
//     }
//     else
//     {
//         scoreUsername = 0;
//         $("#null_name").text("");
//     }
// });
//
// $( "#password" ).keyup(function() {
//     if($( "#password" ).val() == "")
//     {
//         scorePassword = 1;
//         $("#null_password").text("Harus diisi");
//     }
//     else if($( "#password" ).val().length < 6)
//     {
//         scorePassword = 1;
//     }
//     else
//     {
//         scorePassword = 0;
//         $("#null_password").text("");
//     }
// });
//
// $( "#password2" ).keyup(function() {
//     if($( "#password2" ).val() == "")
//     {
//         scorePassword2 = 1;
//         $("#null_password2").text("Harus diisi");
//     }
//     else
//     {
//         scorePassword2 = 0;
//         $("#null_password2").text("");
//     }
//
//     if($("#password").val() != $("#password2").val())
//     {
//         scoreConfirm = 1;
//         $("#null_confirm").text("Password not match");
//     }
//     else
//     {
//         scoreConfirm = 0;
//         $("#null_confirm").text("");
//     }
// });
//
//
// function prosesaktivasi()
// {
//     var scoreUsername = null;
//     var scorePassword = null;
//     var scorePassword2 = null;
//     var scoreTtd = null;
//     var scoreSgn = null;
//     var upperCase= new RegExp('[^a-z][^0-9]');
//     var format = /[!@#$%^&*()+\-=\[\]{};':"\\|,<>\/?]+/;
//
//     if($("#i_ttd").val() != "")
//     {
//         scoreTtd = 0;
//         scoreSgn = 0;
//     }
//     else
//     {
//         if($("#i_ttd").val() == "")
//         {
//             if($("#sgn").val() == "")
//             {
//                 scoreTtd = 1;
//                 scoreSgn = 1;
//                 $("#null_sgn").text("");
//             }
//             else
//             {
//                 scoreTtd = 0;
//                 scoreSgn = 0;
//                 $("#null_sgn").text("");
//             }
//         }
//     }
//
//     if($("#username").val() == "")
//     {
//         scoreUsername = 1;
//         $("#null_name").text("Harus diisi");
//     }
//     else if ($("#username").val().length < 6) //<!----------------------------------->>>>> update
//     {
//         scoreUsername = 1;
//         $("#null_name").text("Panjang karakter min 6, maks 15 karakter."); //<!----------------------------------->>>>> update
//     }
//     else if($('#username').val().indexOf(' ')>=0) //<!----------------------------------->>>>> update
//     {
//         scoreUsername = 1; //<!----------------------------------->>>>> update
//         $("#null_name").text("Username can't contain space"); //<! ----------------------------------->>>>> update
//     }
//     else if(format.test($( "#username" ).val())){ //<!----------------------------------->>>>> update
//         scoreUsername = 1; //<!----------------------------------->>>>> update
//         $("#null_name").text("Karakter tidak valid"); //<!----------------------------------->>>>> update
//         $("#same_name").text(""); //<!----------------------------------->>>>> update
//         $("#notsame_name").text(""); //<!----------------------------------->>>>> update
//     }
//     else
//     {
//         scoreUsername = 0;
//         $("#null_name").text("");
//     }
//
//     if($("#password").val() == "")
//     {
//         scorePassword = 1;
//         $("#null_password").text("Harus diisi");
//     }
//     else
//     {
//         scorePassword = 0;
//         $("#null_password").text("");
//     }
//
//     if($("#password2").val() == "")
//     {
//         scorePassword2 = 1;
//         $("#null_password2").text("Harus diisi");
//     }
//     else
//     {
//         scorePassword2 = 0;
//         $("#null_password2").text("");
//     }
//
//     if($("#password").val() != $("#password2").val())
//     {
//         scoreConfirm = 1;
//         $("#null_confirm").text("Password not match");
//     }
//     else
//     {
//         scoreConfirm = 0;
//         $("#null_confirm").text("");
//     }
//
//     if(scoreUsername == 0 && scorePassword == 0 && scorePassword2 == 0 && scoreConfirm == 0 && scoreTtd == 0 && scoreSgn == 0 && str > 36 && scoreuserdb == 0)
//     {
//         $("#otpModal").modal('show');
//     }
// }
//
// $( "#prosesOTP" ).click(function() {
//     document.getElementById("btnotp").disabled = true;
// });
//
// function submitOTP()
// {
//     $("#e_proses").text("");
//     $("#prosesOTP").button('loading');
//     var dataOTP = new FormData();
//
//     dataOTP.append('nohp', $("#handphone").val());
//     dataOTP.append('otpcode', $("#otp").val());
//
//     $.ajax({
//         type: "POST",
//         url: "https://api.tandatanganku.com/CheckOTP.html",
//         contentType: false,
//         processData: false,
//         data: dataOTP,
//         success: function(data, status, jqXHR){
//             $("#disabledbtn").val("Memproses...");
//             $("#disabledbtn").show();
//             //if otp success do activation
//             $("#prosesOTP").button("reset");
//             document.getElementById("btnotp").disabled = true;
//
//             setTimeout(function() {
//                 $("#otpModal").modal('hide');
//             }, 1100);
//
//             setTimeout(function() {
//                 $("#loadingModal").modal('show');
//             }, 1700);
//
//             var formData = new FormData();
//
//             var canvas = document.getElementById('signature-pad');
//             var context = canvas.getContext('2d');
//             var dataURL = canvas.toDataURL();
//
//             var blobBin = atob(dataURL.split(',')[1]);
//             var array = [];
//             for(var i = 0; i < blobBin.length; i++) {
//                 array.push(blobBin.charCodeAt(i));
//             }
//             var file=new Blob([new Uint8Array(array)], {type: 'image/png'});
//
//             formData.append('preid', $("#preid").val());
//             formData.append('email', $("#email").val());
//             formData.append('username', $("#username").val());
//             formData.append('password', $("#password").val());
//             formData.append('fttd', file);
//
//             $.ajax({
//                 type: "POST",
//                 url: "https://api.tandatanganku.com/AktivasiMitra.html",
//                 contentType: false,
//                 processData: false,
//                 data: formData,
//                 success: function(data, status, jqXHR){
//                     //console.log(data);
//
//                     $("#prosesmessage").text("Sukses: " + data);
//
//                     setTimeout(function() {
//                         $("#loadingModal").modal('hide');
//                     }, 2500);
//
//                     setTimeout(function() {
//                         $('#notifproses').modal({
//                             show: 'true'
//                         });
//                     }, 3300);
//
//                     $("#prosesOTP").hide();
//                     $("#reset").hide();
//                     $("#disabledbtn").val("Aktivasi Berhasil");
//
//                     setTimeout(function() {
//                         $("#formAktivasi").hide();
//                         //$("#formSukses").show();
//                     }, 3300);
//
//                     $("#prosesaktivasi").hide();
//                     parent.Return(data);
//                 },
//
//                 error: function(jqXHR, textStatus, errorThrown){
//                     //console.log(jqXHR.status);
//                     var err = eval("(" + jqXHR.responseText + ")");
//
//                     document.getElementById("btnotp").disabled = false;
//
//                     $("#otpModal").modal('hide');
//                     $("#prosesmessage").text("Error: " + jqXHR.status + " - " + err.Message);
//                     $("#loadingModal").modal('hide');
//                     $('#notifproses').modal('show');
//                     $("#formAktivasi").show();
//                     $("#formSukses").hide();
//                 }
//             });
//
//         },
//         error: function(jqXHR, textStatus, errorThrown){
//             document.getElementById("btnotp").disabled = false;
//             $("#prosesOTP").button("reset");
//             $("#e_otp").text("OTP Salah");
//         }
//     });
// }
//
//
// function dataURItoBlob(dataURI) {
// // convert base64/URLEncoded data component to raw binary data held in a string
//     var byteString;
//     if (dataURI.split(',')[0].indexOf('base64') >= 0)
//         byteString = atob(dataURI.split(',')[1]);
//     else
//         byteString = unescape(dataURI.split(',')[1]);
// // separate out the mime component
//     var mimeString = dataURI.split(',')[0].split(':')[1].split(';')[0];
// // write the bytes of the string to a typed array
//     var ia = new Uint8Array(byteString.length);
//     for (var i = 0; i < byteString.length; i++) {
//         ia[i] = byteString.charCodeAt(i);
//     }
//     return new Blob([ia], {type:mimeString});
// }
//
// var signaturePad = new SignaturePad(canvas, {
// // It's Necessary to use an opaque color when saving image as JPEG;
// // this option can be omitted if only saving as PNG or SVG
// // backgroundColor: 'rgb(0, 0, 0)'
// });
//
// // Adjust canvas coordinate space taking into account pixel ratio,
// // to make it look crisp on mobile devices.
// // This also causes canvas to be cleared.
// function resizeCanvas() {
//     // When zoomed out to less than 100%, for some very strange reason,
//     // some browsers report devicePixelRatio as less than 1aa
//     // and only part of the canvas is cleared then.
//
//
//
//     var ratio = Math.max(window.devicePixelRatio || 1, 1);
//
//
//
//     // This part causes the canvas to be cleared
//     canvas.width = canvas.offsetWidth * 1;
//     canvas.height = canvas.offsetHeight * 1;
//     canvas.getContext("2d").scale(1, 1);
//
//     // This library does not listen for canvas changes, so after the canvas is automatically
//     // cleared by the browser, SignaturePad#isEmpty might still return false, even though the
//     // canvas looks empty, because the internal data of this library wasn't cleared. To make sure
//     // that the state of this library is consistent with visual state of the canvas, you
//     // have to clear it manually.
//     signaturePad.clear();
// }
//
// // On mobile devices it might make more sense to listen to orientation change,
// // rather than window resize events.
// //window.onresize = resizeCanvas;
// resizeCanvas();
//
// function getBase64Image(img) {
//     var canvas = document.createElement("canvas");
//     canvas.width = img.width;
//     canvas.height = img.height;
//     var ctx = canvas.getContext("2d");
//     ctx.drawImage(img, 0, 0, img.width, img.height);
//     var dataURL = canvas.toDataURL("image/png");
//     return dataURL;
// }
//
//
// $('#signModal').on('shown.bs.modal', function () {
//     if (yes == 0){
//         canvas.width = canvas.offsetWidth * 1;
//         canvas.height = canvas.offsetHeight * 1;
//         yes = 1 ;
//
//     }
// })
//
//
// $('#signModal').on('hidden.bs.modal', function () {
//
//     if (signaturePad.isEmpty()) {
//         $("#sgn").closest('.form-group').removeClass('has-success has-feedback').addClass('has-error has-feedback');
//         error = "<span class=\"glyphicon glyphicon-remove form-control-feedback\"></span>Harus diisi\n";
//         esgn.innerHTML=error;
//         sgn.value = "" ;
//
//     }else {
//         sgn.value = "success" ;
//         $("#sgn").closest('.form-group').removeClass('has-error has-feedback').addClass('has-success has-feedback');
//         esgn.innerHTML="<span class=\"glyphicon glyphicon-ok form-control-feedback\"></span>";
//
//     }
//
//
// })
//
//
// function updateTtd(email) {
//
//     var dataURL = signaturePad.toDataURL();
//     var blob = dataURLToBlob(dataURL);
//
//     var formData = new FormData();
//     formData.append('email', email);
//     formData.append("updatettd", blob, "signature.png");
//
//     $.ajax({
//         type : "POST",
//         url : "/process/sourceTtd.html",
//         data : formData,
//         processData : false,
//         contentType : false,
//         cache : false,
//         success : function(result) {
//             if (result) {
//                 var obj = jQuery.parseJSON(result);
//                 if (obj.status == "OK") {
//                     $("#sts_mdl").text("Ubah Tanda Tangan");
//                     $('#modal-header').attr('class',
//                         'modal-header modal-primary');
//                     $("#text_mdl").html("Tanda Tangan Berhasil di Perbarui");
//                     $("#cModal").attr('onclick',
//                         "window.location=\"changeTtd.html\"");
//                     $("#bModal").attr('onclick',
//                         "window.location=\"changeTtd.html\"");
//                     $('#myPleaseWait').modal('hide');
//                     $('#myModal').modal('show');
//                 } else {
//                     $("#sts_mdl").text("Gagal");
//                     $('#modal-header').attr('class','modal-header modal-danger');
//                     $("#text_mdl").html("Tanda Tangan Berhasil Gagal di Perbarui");
//                     $('#myPleaseWait').modal('hide');
//                     $('#myModal').modal('show');
//                 }
//             }
//         }
//     });
//
// }
//
// function saveEktp(email) {
//
//     var base64 = getBase64Image(document.getElementById("loadektp"));
//     var blob = dataURLToBlob(base64);
//
//     var formData = new FormData();
//     formData.append('frmProcess', 'saveEktp');
//     formData.append("blob", blob, "signature.png");
//     formData.append('email', email);
//
//     $.ajax({
//         type : "POST",
//         url : "/doc/ttd.html",
//         data : formData,
//         processData : false,
//         contentType : false,
//         cache : false,
//         success : function(result) {
//             if (result) {
//                 var obj = jQuery.parseJSON(result);
//                 if (obj.status == "OK") {
//                     $("#sts_mdl").text("Verikasi No HP");
//                     $('#modal-header').attr('class',
//                         'modal-header modal-primary');
//                     $("#text_mdl").html(
//                         "No. HP berhasil diverifikasi. Terima Kasih");
//                     $("#cModal").attr('onclick',
//                         "window.location=\"verification.html\"");
//                     $("#bModal").attr('onclick',
//                         "window.location=\"verification.html\"");
//                     $('#myPleaseWait').modal('hide');
//                     $('#myModal').modal('show');
//                 } else {
//                     $("#sts_mdl").text("Gagal");
//                     $('#modal-header').attr('class',
//                         'modal-header modal-danger');
//                     $("#text_mdl").html(obj.status);
//                     $('#myModal').modal('show');
//                 }
//             }
//         }
//     });
//
// }
//
// function saveNpwp(email) {
//
//     var base64 = getBase64Image(document.getElementById("loadnpwp"));
//     var blob = dataURLToBlob(base64);
//
//     var formData = new FormData();
//     formData.append('frmProcess', 'saveNpwp');
//     formData.append("blob", blob, "signature.png");
//     formData.append('email', email);
//
//     $.ajax({
//         type : "POST",
//         url : "/doc/ttd.html",
//         data : formData,
//         processData : false,
//         contentType : false,
//         cache : false,
//         success : function(result) {
//             if (result) {
//                 var obj = jQuery.parseJSON(result);
//                 if (obj.status == "OK") {
//                     $("#sts_mdl").text("Verikasi No HP");
//                     $('#modal-header').attr('class',
//                         'modal-header modal-primary');
//                     $("#text_mdl").html(
//                         "No. HP berhasil diverifikasi. Terima Kasih");
//                     $("#cModal").attr('onclick',
//                         "window.location=\"verification.html\"");
//                     $("#bModal").attr('onclick',
//                         "window.location=\"verification.html\"");
//                     $('#myPleaseWait').modal('hide');
//                     $('#myModal').modal('show');
//                 } else {
//                     $("#sts_mdl").text("Gagal");
//                     $('#modal-header').attr('class',
//                         'modal-header modal-danger');
//                     $("#text_mdl").html(obj.status);
//                     $('#myModal').modal('show');
//                 }
//             }
//         }
//     });
//
// }
//
// function saveSelfie(email) {
//
//     var base64 = getBase64Image(document.getElementById("loadselfie"));
//     var blob = dataURLToBlob(base64);
//     var formData = new FormData();
//     formData.append('frmProcess', 'saveSelfie');
//     formData.append("blob", blob, "signature.png");
//     formData.append('email', email);
//
//     $.ajax({
//         type : "POST",
//         url : "/doc/ttd.html",
//         data : formData,
//         processData : false,
//         contentType : false,
//         cache : false,
//         success : function(result) {
//             if (result) {
//                 var obj = jQuery.parseJSON(result);
//                 if (obj.status == "OK") {
//                     $("#sts_mdl").text("Verikasi No HP");
//                     $('#modal-header').attr('class',
//                         'modal-header modal-primary');
//                     $("#text_mdl").html(
//                         "No. HP berhasil diverifikasi. Terima Kasih");
//                     $("#cModal").attr('onclick',
//                         "window.location=\"verification.html\"");
//                     $("#bModal").attr('onclick',
//                         "window.location=\"verification.html\"");
//                     $('#myPleaseWait').modal('hide');
//                     $('#myModal').modal('show');
//                 } else {
//                     $("#sts_mdl").text("Gagal");
//                     $('#modal-header').attr('class',
//                         'modal-header modal-danger');
//                     $("#text_mdl").html(obj.status);
//                     $('#myModal').modal('show');
//                 }
//             }
//         }
//     });
//
// }
// // One could simply use Canvas#toBlob method instead, but it's just to show
// // that it can be done using result of SignaturePad#toDataURL.
// function dataURLToBlob(dataURL) {
//     // Code taken from https://github.com/ebidel/filer.js
//     var parts = dataURL.split(';base64,');
//     var contentType = parts[0].split(":")[1];
//     var raw = window.atob(parts[1]);
//     var rawLength = raw.length;
//     var uInt8Array = new Uint8Array(rawLength);
//
//     for (var i = 0; i < rawLength; ++i) {
//         uInt8Array[i] = raw.charCodeAt(i);
//     }
//
//     return new Blob([ uInt8Array ], {
//         type : contentType
//     });
// }
//
// clearButton.addEventListener("click", function(event) {
//     signaturePad.clear();
// });
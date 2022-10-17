$("#formSukses").hide();
var canvas = document.getElementById('signature-pad');
var clearButton = document.getElementById('clear');

var yes = 0;
var sgn = document.getElementById('sgn');
var esgn = document.getElementById('e_sgn');
$("#disabledbtn").hide();


$( "#username" ).keyup(function() {
	var usernameData = new FormData();
	usernameData.append('username', $("#username").val());
	
	$.ajax({
        type: "POST",
        url: "https://api.tandatanganku.com/CheckUsername.html",            
        contentType: false,
        processData: false,
        data: usernameData,
        success: function(data, status, jqXHR){
        	//console.log(data);
        	
        	if(data == 100 && $( "#username" ).val().length >= 5)
    		{
        		scoreUsername = 0;
        		$("#same_name").text("");
        		$("#notsame_name").text("OK");
    		}
        	else if(data == 200 && $( "#username" ).val().length >= 5)
    		{
        		scoreUsername = 1;
        		$("#same_name").text("Username sudah digunakan");
        		$("#notsame_name").text("");
    		}
        	
        },
        error: function(jqXHR, textStatus, errorThrown){
        	$("#same_name").text("Error : " + jqXHR.status);
        }
   });
	
	  if($( "#username" ).val() == "")
		  {
		  		scoreUsername = 1;
		  		$("#null_name").text("Can't Empty");
		  		$("#same_name").text("");
		  		$("#notsame_name").text("");
		  }
	  else if($( "#username" ).val().length < 5)
		  {
		  		scoreUsername = 1;
		  		$("#null_name").text("Can't less than 5 characters");
		  		$("#same_name").text("");
		  }
	  else
		  {
		  		scoreUsername = 0;
		  		$("#null_name").text("");
		  }
	});

$( "#password" ).keyup(function() {
	  if($( "#password" ).val() == "")
	  {
	  		scorePassword = 1;
	  		$("#null_password").text("Can't Empty");
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
		  		$("#null_password2").text("Can't Empty");
		  }
	  else
		  {
		  		scorePassword2 = 0;
		  		$("#null_password2").text("");
		  }
	  
	  
	  if($("#password").val() != $("#password2").val())
		{
			scoreConfirm = 1;
			$("#null_confirm").text("Password not match");
		}
	  else
		{
			scoreConfirm = 0;
			$("#null_confirm").text("");
		}
	});


function prosesaktivasi()
{
	
	var scoreUsername = null;
	var scorePassword = null;
	var scorePassword2 = null;
	var scoreTtd = null;
	var scoreSgn = null;
	
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
		$("#null_name").text("Can't Empty");
	}
	else if ($("#username").val().length < 5)
	{
		scoreUsername = 1;
		$("#null_name").text("Can't less than 5 characters");
	}
	else
	{
		scoreUsername = 0;
		$("#null_name").text("");
	}

	if($("#password").val() == "")
	{
		scorePassword = 1;
		$("#null_password").text("Can't Empty");
	}
	else
	{
		scorePassword = 0;
		$("#null_password").text("");
	}
	
	if($("#password2").val() == "")
	{
		scorePassword2 = 1;
		$("#null_password2").text("Can't Empty");
	}
	else
	{
		scorePassword2 = 0;
		$("#null_password2").text("");
	}
	
	if($("#password").val() != $("#password2").val())
	{
		scoreConfirm = 1;
		$("#null_confirm").text("Password not match");
	}
	else
	{
		scoreConfirm = 0;
		$("#null_confirm").text("");
	}
	
	if(scoreUsername == 0 && scorePassword == 0 && scorePassword2 == 0 && scoreConfirm == 0 && scoreTtd == 0 && scoreSgn == 0 && str > 36)
	{
		$("#otpModal").modal('show');
	}
}

$( "#prosesOTP" ).click(function() {
		document.getElementById("btnotp").disabled = true;
	});

function submitOTP()
{
	$("#e_proses").text("");
	$("#prosesOTP").button('loading');
	var dataOTP = new FormData();
	
	dataOTP.append('nohp', $("#handphone").val());
	dataOTP.append('otpcode', $("#otp").val());
	
	$.ajax({
        type: "POST",
        url: "https://api.tandatanganku.com/CheckOTP.html",            
        contentType: false,
        processData: false,
        data: dataOTP,
        success: function(data, status, jqXHR){
        	$("#disabledbtn").val("Memproses...");
        	$("#disabledbtn").show();
        	//if otp success do activation
        	$("#prosesOTP").button("reset");
        	document.getElementById("btnotp").disabled = true;
        	
        	setTimeout(function() {
        		$("#otpModal").modal('hide');
        	}, 1100);
        	  	
        	setTimeout(function() {
        		$("#loadingModal").modal('show');
        	}, 1700);
        	  	
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
            
            formData.append('preid', $("#preid").val());
        	formData.append('email', $("#email").val());
        	formData.append('username', $("#username").val());
        	formData.append('password', $("#password").val());
        	formData.append('fttd', file);

        	$.ajax({
                type: "POST",
                url: "https://api.tandatanganku.com/AktivasiMitra.html",            
                contentType: false,
                processData: false,
                data: formData,
                success: function(data, status, jqXHR){
                	//console.log(data);
                	
                	$("#prosesmessage").text("Sukses: " + data);
                	
                	setTimeout(function() {
                		$("#loadingModal").modal('hide');
                	}, 2500);
            	
                	setTimeout(function() {
	            		$('#notifproses').modal({
	            	        show: 'true'
	            	    });
                	}, 3300);
                	
                	$("#prosesOTP").hide();
                	$("#reset").hide();
                	$("#disabledbtn").val("Aktivasi Berhasil");
                	
                	setTimeout(function() {
                    	$("#formAktivasi").hide();
                    	//$("#formSukses").show();
                	}, 3300);

                	$("#prosesaktivasi").hide();
                	parent.Return(data);
                },
                
                error: function(jqXHR, textStatus, errorThrown){
                	//console.log(jqXHR.status);
                	 var err = eval("(" + jqXHR.responseText + ")");

                	document.getElementById("btnotp").disabled = false;
                	
                	$("#otpModal").modal('hide');
                	$("#prosesmessage").text("Error: " + jqXHR.status + " - " + err.Message);
                	$("#loadingModal").modal('hide');
                	$('#notifproses').modal('show');
                	$("#formAktivasi").show();
                	$("#formSukses").hide();
                }
           });
        	
        },
        error: function(jqXHR, textStatus, errorThrown){
        	document.getElementById("btnotp").disabled = false;
        	$("#prosesOTP").button("reset");
        	$("#e_otp").text("OTP Salah");
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
		error = "<span class=\"glyphicon glyphicon-remove form-control-feedback\"></span>Can't Empty\n";
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


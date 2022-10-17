var canvas = document.getElementById('signature-pad');
var clearButton = document.getElementById('clear');

var yes = 0;
var sgn = document.getElementById('sgn');
var esgn = document.getElementById('e_sgn');



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
		error = "<span class=\"glyphicon glyphicon-remove form-control-feedback\"></span>Must be filled out\n";
    	esgn.innerHTML=error;
    	sgn.value = "" ;

	}else {
		sgn.value = "success" ;
		$("#sgn").closest('.form-group').removeClass('has-error has-feedback').addClass('has-success has-feedback');
		esgn.innerHTML="<span class=\"glyphicon glyphicon-ok form-control-feedback\"></span>";
		
	}
	

})

function saveTtd(email) {

	var i_ktp = document.getElementById("i_ktp").value;
	var i_npwp = document.getElementById("i_npwp").value;
	var i_wajah = document.getElementById("i_wajah").value;
	var i_ttd = document.getElementById("i_ttd").value;


	var formData = new FormData();
	
	formData.append('email', email);

	if(i_ttd == ""){
		var dataURL = signaturePad.toDataURL();
		var blob = dataURLToBlob(dataURL);
		formData.append("fttd", blob, "signature.png");
	}else{
		formData.append("lttd", i_ttd);
	}
	
	if (i_ktp == ""){
		var base64ktp = getBase64Image(document.getElementById("loadektp"));
		var fektp = dataURLToBlob(base64ktp);
		formData.append("fektp", fektp, "ektp.png");
	}
	else
		formData.append("lektp", i_ktp);

	if (i_npwp == "")
	{
		if ($('#loadnpwp').attr('src')) {
			var base64npwp = getBase64Image(document.getElementById("loadnpwp"));
			var fnpwp = dataURLToBlob(base64npwp);
			formData.append("fnpwp", fnpwp, "npwp.png");
		}
	}
	else
		formData.append("lnpwp", i_npwp);

	if (i_wajah == ""){
		var base64selfie = getBase64Image(document.getElementById("loadselfie"));
		var fselfie = dataURLToBlob(base64selfie);
		formData.append("fselfie", fselfie, "selfie.png");
	}
	else
		formData.append("lselfie", i_wajah);

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


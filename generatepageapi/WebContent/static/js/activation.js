jQuery(document).ready(function($) {
       // Showing the progress bar since the first moment.
      
       $('#password').password();
       $('#password').on('password.text', (e, text, score) => {
   		str=score
       })
		
});

var str=0;
	

var usernext = 0;



function validateUserName(id) {

	var username = $("#username").val();

	if (username.length < 6) {
		usernext = 1;
		error = "Username length must be between <strong>6</strong> and <strong>15</strong> characters.";
		setError(id, error);

	} else {

		var dataString = {
			'frmProcess' : 'username',
			'username' : username,
		}

		var error = "";
		var regex = /^[a-zA-Z0-9_.-]*$/;
		if (!regex.test(username)) {
			usernext = 1;
			error = "Invalid Username";
			setError(id, error);
		} else {
			check(id);
			$.ajax({
				type : "POST",
				url : "/process/check.html",
				data : dataString,
				success : function(result) {
					if (result) {
						var obj = jQuery.parseJSON(result);
						if (obj.msg == "OK") {
							usernext = 0;
							setSuccess(id);
						} else {
							usernext = 1;
							error = obj.msg;
							setError(id, error);
						}
					}
				},

			});
		}
	}

	return error;
}

function isNumberKey(evt) {
	var charCode = (evt.which) ? evt.which : event.keyCode
	if (charCode > 31 && (charCode < 48 || charCode > 57))
		return false;
	return true;
}

function validatePwd() {
	var fld = $("#password");
	var fld2 = $("#password2");
	if (fld2.val().length > 0) {
		if (fld2.val() == fld.val()) {
			fld2.css("background-color", "white");
			$('#e_password2').closest('.form-group').removeClass(
					'has-error has-feedback').addClass(
					'has-success has-feedback');
			document.getElementById("e_password2").innerHTML = "<span class=\"glyphicon glyphicon-ok form-control-feedback\"></span>";

		} else {

			fld2.css("background-color", "white");
			$('#e_password2').closest('.form-group').removeClass(
					'has-success has-feedback').addClass(
					'has-error has-feedback');
			document.getElementById("e_password2").innerHTML = "<span class=\"glyphicon glyphicon-remove form-control-feedback\"></span>";

		}
	} else {
		fld2.css("background-color", "white");
		$('#e_password2').closest('.form-group').removeClass(
				'has-success has-feedback').removeClass(
				'has-error has-feedback');
		document.getElementById("e_password2").innerHTML = "";

	}

}

function proOtp() {

	var pro = 0;

	if (usernext == 1) {
		pro = 1;
		swal("", "Username salah", "error");
	} else

	if ($("#username").val().length == 0) {
		pro = 1;
		swal("", "Username tidak boleh kosong!", "error");
	} else

	if ($("#password").val().length == 0) {
		pro = 1;
		swal("", "Password tidak boleh kosong!", "error");
	} else

	if ($("#password").val() != $("#password2").val()) {
		pro = 1;
		swal("", "Password tidak sama!", "error");
	} else if (str < 34) {
		pro = 1;
		swal("", "Password lemah!", "error");
	}

	var is_ttd = document.getElementById("i_ttd").value;
	
	
	
	if(tipe=='2'){
		if (!$('#loadselfie').attr('src')) {
			swal("", "Photo Wajah tidak boleh kosong !", "error");
			return false;
		}
	}

	if (is_ttd == "") {
		if (signaturePad.isEmpty()) {
			pro = 1;
			swal("", "Please provide a signature first!", "error");

		}
	}
	
	if (pro == 0) {
	
		if(tipe=='1'){
		$("#otp").val('');
		$("#otpModal").modal('show');
		}
		if(tipe=='2'){
		checkLive();
		}
	}
	
	
}

function checkLive(){
	
	$('#myPleaseWait').modal('show');
	
	var form = new FormData();

	var base64selfies = getBase64Image(document.getElementById("loadselfie"));
	var fselfies = dataURLToBlob(base64selfies);

	var base64ktps = getBase64Image(document.getElementById("loadektp"));
	var fektps = dataURLToBlob(base64ktps);

	form.append("fektp", fektps, "ektp.png");
	form.append("fselfie", fselfies, "selfie.png");

	$.ajax({
		type : "POST",
		url : "/process/frmlive.html",
		data : form,
		processData : false,
		contentType : false,
		cache : false,
		success : function(result) {
			if (result) {
				var obj = jQuery.parseJSON(result);

				if (obj.status == "OK") {
					$('#myPleaseWait').modal('hide');
					
					$("#otp").val('');
					$("#otpModal").modal('show');
					
				} else {
					$('#myPleaseWait').modal('hide');
					$("#sts_mdl").text("Registrasi Gagal");
					$('#modal-header').attr('class',
							'modal-header modal-danger');
					$("#text_mdl").html(obj.info);
					$('#myPleaseWait').modal('hide');
					$('#myModal').modal('show');
					return false;
				}

			} else {
				$('#myPleaseWait').modal('hide');
				$("#sts_mdl").text("Registrasi Gagal");
				$('#modal-header').attr('class', 'modal-header modal-danger');
				$("#text_mdl")
						.html('Simpan data gagal ! Silahkan Hubungi Kami');
				$('#myPleaseWait').modal('hide');
				$('#myModal').modal('show');
				return false;
			}
		}
	});
	return false;

	
}

function proActive() {
	verOTP();
}

function otp() {

	var preid = document.getElementById("preid").value;

	var i = [], play = [];
	var dataString = {
		'frmProcess' : 'otp',
		'msisdn' : preid
	};
	$
			.ajax({
				type : "POST",
				url : "/process/frmprc.html",
				data : dataString,
				success : function(result) {
					if (result) {
						var obj = jQuery.parseJSON(result);
						if (obj.status == "OK") {
							var selector = $(document.getElementById("btnotp")), inDex = $(
									document.getElementById("btnotp")).index(), prevText = $(
									document.getElementById("btnotp")).val();
							i[inDex] = 0;
							var inSeconds = 1 * 60;

							$(selector).prop("disabled", "disabled");

							play[inDex] = setInterval(function() {
								if (inSeconds > 1) {
									inSeconds = inSeconds - 1;
									if (inSeconds.toString().length > 1) {
										$(selector).val(
												"Tunggu " + inSeconds
														+ " detik");
									} else {
										$(selector).val(
												"Tunggu " + inSeconds
														+ " detik");
									}
								} else {
									$(selector).prop("disabled", "");
									clearInterval(play[inDex]);
									$(selector).val(prevText);
								}
							}, 1000);

						} else {
							$('#otpModal').modal('hide');
							$("#sts_mdl").text("Gagal");
							$('#modal-header').attr('class',
									'modal-header modal-danger');
							$("#text_mdl").html("SMS Tidak Bisa Dikirim");
							$('#myModal').modal('show');
						}
					} else {
						$('#otpModal').modal('hide');
						$("#sts_mdl").text("Gagal");
						$('#modal-header').attr('class',
								'modal-header modal-danger');
						$("#text_mdl").html("SMS Tidak Bisa Dikirim");
						$('#myModal').modal('show');

					}
				}
			});

}

function verOTP() {

	var code = document.getElementById("otp").value;

	var preid = document.getElementById("preid").value;

	if (code.length == 0) {
		swal("", "Kode OTP tidak boleh kosong!", "error");
		return false;
	}

	var code = $("#otp").val();
	var dataString = {
		'frmProcess' : 'verCodePre',
		'code' : code,
		'msisdn' : preid
	};
	$.ajax({
		type : "POST",
		url : "/process/frmprc.html",
		data : dataString,
		success : function(result) {
			if (result) {
				var obj = jQuery.parseJSON(result);
				if (obj.status == "OK") {
					preregister();
				} else {
					$('#otpModal').modal('hide');
					$("#sts_mdl").text("Gagal");
					$('#modal-header').attr('class',
							'modal-header modal-danger');
					$("#text_mdl").html(obj.status);
					$('#myModal').modal('show');
				}
			}
		}
	});
	return false;
}

function preregister() {

	$('#otpModal').modal('hide');
	$('#myPleaseWait').modal('show');
	$("#bModal").html('Close');

	var i_ttd = document.getElementById("i_ttd").value;
	

	var formData = new FormData();

	var preid = document.getElementById("preid").value;
	var email = document.getElementById("email").value;
	var password = document.getElementById("password").value;
	var username = document.getElementById("username").value;

	formData.append("method", "preregister");
	formData.append("password", password);
	formData.append("username", username);
	formData.append("preid", preid);
	
	if(i_ttd == ""){
		var dataURL = signaturePad.toDataURL();
		var blob = dataURLToBlob(dataURL);
		formData.append("fttd", blob, "signature.png");
	}
	
	if(tipe=='2'){
		var i_wajah = document.getElementById("i_wajah").value;
		if (i_wajah == ""){
			var base64selfie = getBase64Image(document.getElementById("loadselfie"));
			var fselfie = dataURLToBlob(base64selfie);
			formData.append("fselfie", fselfie, "selfie.png");
		}	
	}
	formData.append('email', email);
	

	$.ajax({
		type : "POST",
		url : "/process/activationprocess.html",
		data : formData,
		processData : false,
		contentType : false,
		cache : false,
		success : function(result) {
			if (result) {
				var obj = jQuery.parseJSON(result);
				if (obj.status == "OK") {

					$("#sts_mdl").text("Registrasi");
					$('#modal-header').attr('class',
							'modal-header modal-primary');
					$("#text_mdl").html(obj.notif);
					$("#cModal").attr('onclick',
							"window.location=\"index.html\"");
					$("#bModal").attr('onclick',
							"window.location=\"index.html\"");
					$('#myPleaseWait').modal('hide');
					$('#myModal').modal('show');
					$(window).click(function() {
						window.location.href = 'index.html?';
					});
				} else {
					$('#otpModal').modal('hide');
					$("#sts_mdl").text("Registrasi");
					$('#modal-header').attr('class',
							'modal-header modal-danger');
					$("#text_mdl").html(obj.notif);
					$('#myPleaseWait').modal('hide');
					$('#myModal').modal('show');
				}
			}
		},
		error : function() {
			$('#otpModal').modal('hide');

			$("#sts_mdl").text("Error");
			$('#modal-header').attr('class', 'modal-header modal-danger');
			$("#text_mdl").html("<b>System Error</b>");
			$('#myPleaseWait').modal('hide');
			$('#myModal').modal('show');
		}
	});
	
	return false;
}



function setError(id, error) {
	$("#" + id).closest('.form-group').removeClass('has-success has-feedback')
			.addClass('has-error has-feedback');
	$("#" + id).html(
			"<span class=\"glyphicon glyphicon-remove form-control-feedback\"></span>"
					+ error);
}
function check(id) {
	$("#" + id)
			.html(
					'<img src="/images/loader.gif" align="absmiddle">&nbsp;Checking availability...');
}
function setSuccess(id) {
	$("#" + id).closest('.form-group').removeClass('has-error has-feedback')
			.addClass('has-success has-feedback');
	$("#" + id)
			.html(
					"<span class=\"glyphicon glyphicon-ok form-control-feedback\"></span>");

}


function readselfie(input) {
    if (input.files && input.files[0]) {
        var reader = new FileReader();
        
        reader.onload = function (e) {
            $('#loadselfie').attr('src', e.target.result);
        }
        
        reader.readAsDataURL(input.files[0]);
    }
}

$("#imgselfie").change(function(){
	readselfie(this);
});

function detectmob() { 
	 if( navigator.userAgent.match(/Android/i)
	 || navigator.userAgent.match(/webOS/i)
	 || navigator.userAgent.match(/iPhone/i)
	 || navigator.userAgent.match(/iPad/i)
	 || navigator.userAgent.match(/iPod/i)
	 || navigator.userAgent.match(/BlackBerry/i)
	 || navigator.userAgent.match(/Windows Phone/i)
	 ){
		 document.getElementById('wb').style.display = "none";
		 document.getElementById('a_cam').style.display = "none";
		 document.getElementById("wbf").style.display = "inline";
	  }
	 else {
		 document.getElementById('wb').style.display = "inline";
		 document.getElementById('a_cam').style.display = "inline";
		 document.getElementById("wbf").style.display = "none";
	  }
}

if(document.getElementById('wb')){
detectmob();
}

function on_camera() {
	
	$("#camsts_mdl").text("Web Cam");
  	$('#cammodal-header').attr('class','modal-header modal-primary');
	$("#camtext_mdl").html("<div id='load_selfie' width='320' height='240' ></div>");
	$("#cambModal").html('Ambil Photo');
	$('#camModal').modal('show');
	
	
	Webcam.set({
		width: 480,
		height: 360,
		crop_width: 480,
		crop_height: 360,
		image_format: 'jpeg',
		jpeg_quality: 100
	});
	Webcam.attach( '#load_selfie' );
}	
	

	function take_snapshot() {
		// take snapshot and get image data
		Webcam.snap( function(data_uri) {
			// display results in page
			document.getElementById('loadselfie').src = data_uri;
			Webcam.reset();
		});
		$("#cambModal").html('Close');
		
	}
	
	$('#camModal').on('hidden.bs.modal', function () {
		// take snapshot and get image data
		Webcam.snap( function(data_uri) {
			// display results in page
			document.getElementById('loadselfie').src = data_uri;
			Webcam.reset();
		});
		
		var ecam = document.getElementById('e_cam');
		var cam = document.getElementById('cam');
		
		if (!$('#loadselfie').attr('src')) {
			$("#cam").closest('.form-group').removeClass('has-success has-feedback').addClass('has-error has-feedback');
			error = "<span class=\"glyphicon glyphicon-remove form-control-feedback\"></span>Must be filled out\n";
	    	ecam.innerHTML=error;
	    	cam.value = "" ;

		}else {
			cam.value = "success" ;
			$("#cam").closest('.form-group').removeClass('has-error has-feedback').addClass('has-success has-feedback');
			ecam.innerHTML="<span class=\"glyphicon glyphicon-ok form-control-feedback\"></span>";
		}	
		
		$("#cambModal").html('Close');
	})

	function cekSk() {
      var checkBox = document.getElementById("sk");
      if (checkBox.checked == true) {
        document.getElementById("nextotp").disabled = false;
      } else {
        document.getElementById("nextotp").disabled = true;
      }
    }

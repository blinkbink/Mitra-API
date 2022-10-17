function generateKey() {
	var pass = document.getElementById("password").value;

	$('#myPleaseWait').modal('show');
	var dataString = {
		'frmProcess' : 'genKey',
		'password' : pass
	};
	// AJAX Code To Submit Form.
	$.ajax({
		type : "POST",
		url : "/process/frmprocess.html",
		data : dataString,
		cache : false,
		success : function(result) {
			if (result) {
				var obj = jQuery.parseJSON(result);
				if (obj.status == "OK") {
					$("#sts_mdl").text("Berhasil");
					$('#modal-header').attr('class',
							'modal-header modal-primary');
					$("#text_mdl").html(
							"Berikut adalah signed public key anda :<br>"
									+ obj.key);
					$("#cModal").attr('onclick',
							"window.location=\"infokey.html\"");
					$("#bModal").attr('onclick',
							"window.location=\"infokey.html\"");
					$('#myPleaseWait').modal('hide');
					$('#myModal').modal('show');
				} else {
					$("#sts_mdl").text("Gagal");
					$('#modal-header').attr('class',
							'modal-header modal-danger');
					$("#text_mdl").html(obj.status);
					$('#myPleaseWait').modal('hide');
					$('#myModal').modal('show');
				}
			}
		},
		error : function() {
			$("#sts_mdl").text("Error");
			$('#modal-header').attr('class', 'modal-header modal-danger');
			$("#text_mdl").html("<b>System Error</b>");
			$('#myPleaseWait').modal('hide');
			$('#myModal').modal('show');
		}
	});
	return false;
}

function signPublicKey() {
	$('#myPleaseWait').modal('show');
	var pub = document.getElementById("pub").value;

	var dataString = {
		'frmProcess' : 'signPub',
		'pub' : pub
	};
	// AJAX Code To Submit Form.
	$.ajax({
		type : "POST",
		url : "/process/frmprocess.html",
		data : dataString,
		cache : false,
		success : function(result) {
			if (result) {
				var obj = jQuery.parseJSON(result);
				if (obj.status == "OK") {
					$("#sts_mdl").text("Berhasil");
					$('#modal-header').attr('class',
							'modal-header modal-primary');
					$("#text_mdl").html(
							"Berikut adalah signed public key anda :<br>"
									+ obj.key);
					$("#cModal").attr('onclick',
							"window.location=\"infokey.html\"");
					$("#bModal").attr('onclick',
							"window.location=\"infokey.html\"");
					$('#myPleaseWait').modal('hide');
					$('#myModal').modal('show');

				} else {
					$("#sts_mdl").text("Gagal");
					$('#modal-header').attr('class',
							'modal-header modal-danger');
					$("#text_mdl").html(obj.status);
					$('#myPleaseWait').modal('hide');
					$('#myModal').modal('show');

				}
			}
		},
		error : function() {
			$("#sts_mdl").text("Error");
			$('#modal-header').attr('class', 'modal-header modal-danger');
			$("#text_mdl").html("<b>System Error</b>");
			$('#myPleaseWait').modal('hide');
			$('#myModal').modal('show');
		}
	});
	return false;
}

function resetPassword() {

	var fld = $("#password");
	var fld2 = $("#password2");
	var pass = $("#password").val();
	var email = $("#email").val();
	var username = $("#username").val();
	if (fld2.val() == fld.val()) {
		if (str < 34) {

		} else {

			$('#myPleaseWait').modal('show');

			var dataString = {
				'frmProcess' : 'resetpassword',
				'password' : pass,
				'email' : email,
				'username' : username
			};

			// AJAX Code To Submit Form.
			$.ajax({

				type : "POST",
				url : "/process/frmprc.html",
				data : dataString,
				cache : false,
				success : function(result) {
					if (result) {
						var obj = jQuery.parseJSON(result);
						if (obj.status == "OK") {

							$("#sts_mdl").text("Reset Password");
							$('#modal-header').attr('class',
									'modal-header modal-primary');
							$("#text_mdl").html('Reset Password Berhasil');
							$("#cModal").attr('onclick',
									"window.location=\"index.html\"");
							$("#bModal").attr('onclick',
									"window.location=\"index.html\"");
							$('#myPleaseWait').modal('hide');
							$('#myModal').modal('show');
						} else {
							$("#sts_mdl").text("Reset Password");
							$('#modal-header').attr('class',
									'modal-header modal-danger');
							$("#text_mdl").html('Reset Password Gagal');
							$('#myPleaseWait').modal('hide');
							$('#myModal').modal('show');
						}
					}
				},
				error : function() {
					$("#sts_mdl").text("Error");
					$('#modal-header').attr('class',
							'modal-header modal-danger');
					$("#text_mdl").html("<b>System Error</b>");
					$('#myPleaseWait').modal('hide');
					$('#myModal').modal('show');
				}
			});
			return false;
		}
	} else {

	}

	return false;
}

function resetHandler() {

	$('#loadektp').attr('src', '');
	$('#loadnpwp').attr('src', '');
	$('#loadselfie').attr('src', '');
	signaturePad.clear();
}


function register(){
	
	if ($("#password").val() != $("#password2").val()) {
		swal("", "Password tidak sama", "error");
		return false;
	}

	if (!$('#loadektp').attr('src')) {
		swal("", "Photo E-KTP tidak boleh kosong !", "error");
		return false;
	}

	if (!$('#loadselfie').attr('src')) {
		swal("", "Photo Wajah tidak boleh kosong !", "error");
		return false;
	}

	if (signaturePad.isEmpty()) {
		swal("", "Please provide a signature first.", "error");
		return false;
	}
	
	document.getElementById("png").disabled = true;
	$('#myPleaseWait').modal('show');
	$("#bModal").html('Close');

	
	var dataString = {
			'frmProcess' : 'tesCom',
			'versi' : '1.1'
			};
	$.ajax({
        type: "POST",
		url: "/process/frmprc.html",
		data: dataString,
        success: function (msg) {
            if(msg){
            	var obj = jQuery.parseJSON(msg);
				if(obj.status=="ok"){
					registerAjax();
				} 
				else {
				$("#sts_mdl").text("Registrasi");
				$('#modal-header').attr('class','modal-header modal-danger');
				$("#text_mdl").html('Simpan data gagal ! Silahkan Hubungi Kami');
				$('#myPleaseWait').modal('hide');
				document.getElementById("png").disabled = false;
				$('#myModal').modal('show');
			}

		} else {
			$('#myPleaseWait').modal('hide');
			$("#sts_mdl").text("Registrasi Gagal");
			$('#modal-header').attr('class', 'modal-header modal-danger');
			$("#text_mdl").html('Simpan data gagal ! Silahkan Hubungi Kami');
			$('#myPleaseWait').modal('hide');
			document.getElementById("png").disabled = false;
			$('#myModal').modal('show');

		}
        }
    })
	
	
}

function registerAjax() {

	var form = new FormData();

	var base64selfies = getBase64Image(document.getElementById("loadselfie"));
	var fselfies = dataURLToBlob(base64selfies);

	var base64ktps = getBase64Image(document.getElementById("loadektp"));
	var fektps = dataURLToBlob(base64ktps);
	
	var dataURL = signaturePad.toDataURL();
	var blob = dataURLToBlob(dataURL);
	
	if ($('#loadnpwp').attr('src')) {
		var base64npwp = getBase64Image(document.getElementById("loadnpwp"));
		var fnpwp = dataURLToBlob(base64npwp);
		form.append("fnpwp", fnpwp, "npwp.png");
	}
	var y = document.getElementById("Year").value;
	var m = document.getElementById("Month").value;
	var d = document.getElementById("Day").value;
	if (m.length == 1) {
		m = "0" + m;
	}
	if (d.length == 1) {
		d = "0" + d
	}
	var datebirth = y + "-" + m + "-" + d;

	var idcard = document.getElementById("idcard").value;
	var name = document.getElementById("name").value;
	var jk = document.getElementById("jk").value;
	var lbrith = document.getElementById("lbrith").value;
	var address = document.getElementById("address").value;
	var kodepos = document.getElementById("kodepos").value;
	var kelurahan = document.getElementById("kelurahan").value;
	var kecamatan = document.getElementById("kecamatan").value;
	var kota = document.getElementById("kota").value;
	var propinsi = document.getElementById("propinsi").value;
	var handphone = document.getElementById("handphone").value;
	var email = document.getElementById("email").value;
	var password = document.getElementById("password").value;
	var npwp = document.getElementById("npwp").value;
	var username = document.getElementById("username").value;
	
	form.append("method", "register");
	form.append("fttd", blob, "signature.png");
	form.append("fektp", fektps, "ektp.png");
	form.append("fselfie", fselfies, "selfie.png");
	form.append("idktp", idcard);
	form.append("nama", name);		
	form.append("alamat", address);
	form.append("jenis_kelamin", jk);
	form.append("tmp_lahir", lbrith);
	form.append("tgl_lahir", datebirth);
	form.append("kelurahan", kelurahan);
	form.append("kecamatan",kecamatan);
	form.append("kota",kota);
	form.append("provinsi",propinsi);
	form.append("tlp",handphone);
	form.append("password",password);
	form.append("kode_pos",kodepos);
	form.append("email",email);
	form.append("npwp",npwp);
	form.append("username", username);

	
	$.ajax({
		type : "POST",
		url : "/process/frmRegist.html",
		data : form,
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
					document.getElementById("png").disabled = false;
					$('#myModal').modal('show');
				} else {
					$("#sts_mdl").text("Registrasi");
					$('#modal-header').attr('class',
							'modal-header modal-danger');
					$("#text_mdl").html(obj.notif);
					$('#myPleaseWait').modal('hide');
					document.getElementById("png").disabled = false;
					$('#myModal').modal('show');
//					return false;
				}

			} else {
				$('#myPleaseWait').modal('hide');
				$("#sts_mdl").text("Registrasi Gagal");
				$('#modal-header').attr('class', 'modal-header modal-danger');
				$("#text_mdl")
						.html('Simpan data gagal ! Silahkan Hubungi Kami');
				$('#myPleaseWait').modal('hide');
				document.getElementById("png").disabled = false;
				$('#myModal').modal('show');
//				return false;
			}
		}
	});
//	return false;

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
			.html("<span class=\"glyphicon glyphicon-ok form-control-feedback\"></span>");

}

function validateEmail(id) {

	var email = $("#email").val();

	var dataString = {
		'frmProcess' : 'email',
		'email' : email,
	}

	var error = "";
	var regex = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
	if (!regex.test(email)) {
		emailnext = 1;
		error = "Invalid Email Address";
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
						emailnext = 0;
						setSuccess(id);
					} else {
						emailnext = 1;
						error = obj.msg;
						setError(id, error);
					}
				}
			},

		});
	}

	return error;
}

function validateNik(id) {

	var error ="";
	var ktp = $("#idcard").val();
	var dataString = {
		'frmProcess' : 'ktp',
		'ktp' : ktp,
	}
	if (ktp.length < 16) {
		idcard = 1;
		error = "Must <strong>16</strong> Digit.";
		setError("e_idcard", error);

	} else {
		check("e_idcard");
		$.ajax({
			type : "POST",
			url : "/process/check.html",
			data : dataString,
			success : function(result) {
				if (result) {
					var obj = jQuery.parseJSON(result);
					if (obj.msg == "OK") {
						idcard = 0;
						setSuccess("e_idcard");
					} else {
						idcard = 1;
						error = obj.msg;
						setError("e_idcard", error);
					}
				}
			},

		});
	}

	return error;
}

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

function validateHP(id) {
	var error ="";
	var hp = $("#handphone").val();

	if (hp.length > 7) {
		hpnext = 0;
		setSuccess(id);
	} 
	else {
		hpnext = 1;
		error = "Nomor HP Minial 8 digit";
		setError(id, error);
	}	
	
	return error;
}

function validatebrith(id,fld) {
    var error = "";
    if (fld.value.length < 3) {
    	brithnext = 1;
    	error = "Must be filled out";
  		setError(id, error);
    } else {
    	brithnext = 0;
  		setSuccess(id);
    }
    return error;
  }

function validateAlamat(id, fld) {
    var error = "";
    if (fld.value.length < 3) {
    	alamatnext = 1;
  		error = "Must be filled out";
		setError(id, error);
    } else {
    	alamatnext = 0;
		setSuccess(id);
    }
    return error;
  }

function validatePos(id, fld) {
    var error = "";
    if (fld.value.length < 3) {
    	posnext = 1;
  		error = "Must be filled out";
  		setError(id, error);

    } else {
    	posnext = 0;
  		setSuccess(id);
    }
    var adr = document.getElementById('address');
    validateAlamat('e_address', adr);
    return error;
  }

function validateName(id, fld) {
    var error = "";
    if (fld.value.length < 3) {
    	namenext = 1;
  		error = "Must be filled out";
  		setError(id, error);

    } else {
    	namenext = 0;
  		setSuccess(id);
    }
    return error;
  }

  
 


    function validatePwd() {
      var fld = $("#password");
      var fld2 = $("#password2");
      if (fld2.val().length > 0) {
        if (fld2.val() == fld.val()) {
          passnext = 0;
          fld2.css("background-color", "white");
          $('#e_password2').closest('.form-group').removeClass('has-error has-feedback').addClass('has-success has-feedback');
          document.getElementById("e_password2").innerHTML = "<span class=\"glyphicon glyphicon-ok form-control-feedback\"></span>";
        } else {
          passnext = 1;
          fld2.css("background-color", "white");
          $('#e_password2').closest('.form-group').removeClass('has-success has-feedback').addClass('has-error has-feedback');
          document.getElementById("e_password2").innerHTML = "<span class=\"glyphicon glyphicon-remove form-control-feedback\"></span>";
        }
      } else {
        passnext = 1;
        fld2.css("background-color", "white");
        $('#e_password2').closest('.form-group').removeClass('has-success has-feedback').removeClass('has-error has-feedback');
        document.getElementById("e_password2").innerHTML = "";
      }
    }

    function isNumberKey(evt) {
      var charCode = (evt.which) ? evt.which : event.keyCode
      if (charCode > 31 && (charCode < 48 || charCode > 57)) return false;
      return true;
    }

$(document)
		.ready(
				function() {

					$("#npwp")
							.change(
									function() {
										var npwp = $("#npwp").val();

										if (npwp.length == 20) {
											npwpnext = 0;
											$("#npwp")
													.closest('.form-group')
													.removeClass(
															'has-error has-feedback')
													.addClass(
															'has-success has-feedback');
											document.getElementById("e_npwp").innerHTML = "<span class=\"glyphicon glyphicon-ok form-control-feedback\"></span>";
										} else {
											npwpnext = 1;
											$("#npwp")
													.closest('.form-group')
													.removeClass(
															'has-success has-feedback')
													.addClass(
															'has-error has-feedback');
											error = "<span class=\"glyphicon glyphicon-remove form-control-feedback\"></span> Must <strong>15</strong> Digit.";
											document.getElementById("e_npwp").innerHTML = error;
										}

										if (npwp.length == 0) {
											npwpnext = 0;
											$("#npwp")
													.closest('.form-group')
													.removeClass(
															'has-error has-feedback')
													.removeClass(
															'has-success has-feedback');
											document.getElementById("e_npwp").innerHTML = "";
										}

									});

				});


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
	if (fld2.val() == fld.val()) {
		if (str < 34) {

		} else {

			$('#myPleaseWait').modal('show');

			var dataString = {
				'frmProcess' : 'resetpassword',
				'password' : pass,
				'email' : email
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

function resetHandler(){
	
	 $('#loadektp').attr('src', '');
	 $('#loadnpwp').attr('src', '');
	 $('#loadselfie').attr('src', '');
	 signaturePad.clear();
}




function register() {
	
	
	if ($("#password").val() != $("#password2").val()){
		alert("Password tidak sama");
		return false;
	}
	

	
	if (!$('#loadektp').attr('src')) {
		alert("Photo E-KTP tidak boleh kosong !");
		return false;
	}
	
	if (!$('#loadselfie').attr('src')) {
		alert("Photo Wajah tidak boleh kosong !");
		return false;
	}

	if (signaturePad.isEmpty()) {
		alert("Please provide a signature first.");
		return false;
	}

	$('#myPleaseWait').modal('show');	
	$("#bModal").html('Close');
	
	
	
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
	
	
	var dataString = {
		'frmProcess' : 'register',
		'idktp' : idcard,
		'nama' : name,
		'alamat' : address,
		'jenis_kelamin' : jk,
		'tmp_lahir' : lbrith,
		'tgl_lahir' : datebirth,
		'kelurahan' : kelurahan,
		'kecamatan' : kecamatan,
		'kota' : kota,
		'provinsi' : propinsi,
		'tlp' : handphone,
		'password' : password,
		'kode_pos' : kodepos,
		'email' : email,
		'npwp' : npwp,
		
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

					saveTtd(email);

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
				} else {
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
			$("#sts_mdl").text("Error");
			$('#modal-header').attr('class', 'modal-header modal-danger');
			$("#text_mdl").html("<b>System Error</b>");
			$('#myPleaseWait').modal('hide');
			$('#myModal').modal('show');
		}
	});


		
	return false;
}


function sendregis(dataString){

}

function otp(){

	var preid = document.getElementById("preid").value;
	
	var i = [],
    play = [];
		var dataString = {
				'frmProcess' : 'otp',
				'msisdn' : preid
		};
		$.ajax({	
			type: "POST",
			url: "/process/frmprc.html",		
			data: dataString,
			success: function(result){
				if(result){
					var obj = jQuery.parseJSON(result);
					if(obj.status=="OK"){
						var selector = $(document.getElementById("btnotp")),
				        inDex = $(document.getElementById("btnotp")).index(),
				        prevText = $(document.getElementById("btnotp")).val();
				        i[inDex] = 0;
				        var inSeconds = 1 * 60 ;

				        $(selector).prop("disabled", "disabled");
				        
				        play[inDex] = setInterval(function() {
				                if(inSeconds > 1){
				                    inSeconds = inSeconds - 1;
				                    if(inSeconds.toString().length > 1){
				                        $(selector).val("Tunggu "+inSeconds+" detik");
				                    }else{
				                        $(selector).val("Tunggu " + inSeconds+" detik");
				                    }
				                }else{
				                    $(selector).prop("disabled", "");
				                    clearInterval(play[inDex]);
				                    $(selector).val(prevText);
				                }                              	            
				        }, 1000);
						
					} else {
						$("#sts_mdl").text("Gagal");
					  	$('#modal-header').attr('class','modal-header modal-danger');
						$("#text_mdl").html("SMS Tidak Bisa Dikirim");
						$('#myModal').modal('show');
					}
				}else {
					$("#sts_mdl").text("Gagal");
				  	$('#modal-header').attr('class','modal-header modal-danger');
					$("#text_mdl").html("SMS Tidak Bisa Dikirim");
					$('#myModal').modal('show');

				}
			}
		});  

}

function verOTP(){
	
	
	var code = document.getElementById("otp").value;
	
	var preid = document.getElementById("preid").value;
	
	if(code.length == 0){
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
				type: "POST",
				url: "/process/frmprc.html",		
				data: dataString,
				success: function(result){
					if(result){
						var obj = jQuery.parseJSON(result);
						if(obj.status=="OK"){
							preregister();
						} 
						else {
							$('#otpModal').modal('hide');
							$("#sts_mdl").text("Gagal");
						  	$('#modal-header').attr('class','modal-header modal-danger');
							$("#text_mdl").html(obj.status);
							$('#myModal').modal('show');
						}
					}
				}
			});  
			return false;
}


function preregister() {
	
	

	$('#myPleaseWait').modal('show');
	$("#bModal").html('Close');
	var preid = document.getElementById("preid").value;
	var mitraid = document.getElementById("mitraid").value;
	var idcard = document.getElementById("idcard").value;
	var name = document.getElementById("name").value;
	var jk = document.getElementById("jk").value;
	var lbrith = document.getElementById("lbrith").value;
	var datebirth = document.getElementById("datebirth").value;
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

	var dataString = {
		'frmProcess' : 'preregister',
		'idktp' : idcard,
		'nama' : name,
		'alamat' : address,
		'jenis_kelamin' : jk,
		'tmp_lahir' : lbrith,
		'tgl_lahir' : datebirth,
		'kelurahan' : kelurahan,
		'kecamatan' : kecamatan,
		'kota' : kota,
		'provinsi' : propinsi,
		'tlp' : handphone,
		'password' : password,
		'kode_pos' : kodepos,
		'email' : email,
		'npwp' : npwp,
		'preid' : preid,
		'mitraid' : mitraid
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

					saveTtd(email);

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
				} else {
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
			$("#sts_mdl").text("Error");
			$('#modal-header').attr('class', 'modal-header modal-danger');
			$("#text_mdl").html("<b>System Error</b>");
			$('#myPleaseWait').modal('hide');
			$('#myModal').modal('show');
		}
	});
	return false;
}

pic1 = new Image(16, 16);
pic1.src = "/images/loader.gif";

$(document)
		.ready(
				function() {
					$("#email")
							.change(
									function() {
										var usr = $("#email").val();
										var dataString = {
											'frmProcess' : 'email',
											'email' : usr,
										}
										if (usr.length >= 4) {
											$("#e_email")
													.html(
															'<img src="/images/loader.gif" align="absmiddle">&nbsp;Checking availability...');
											$
													.ajax({
														type : "POST",
														url : "/process/check.html",
														data : dataString,
														success : function(
																result) {
															if (result) {
																var obj = jQuery
																		.parseJSON(result);
																if (obj.msg == "OK") {
																	$("#email")
																			.closest(
																					'.form-group')
																			.removeClass(
																					'has-error has-feedback')
																			.addClass(
																					'has-success has-feedback');
																	document
																			.getElementById("e_email").innerHTML = "<span class=\"glyphicon glyphicon-ok form-control-feedback\"></span>";
																} else {
																	$("#email")
																			.closest(
																					'.form-group')
																			.removeClass(
																					'has-success has-feedback')
																			.addClass(
																					'has-error has-feedback');
																	error = "<span class=\"glyphicon glyphicon-remove form-control-feedback\"></span>"
																			+ obj.msg;
																	document
																			.getElementById("e_email").innerHTML = error;
																}
															}
														},

													});

										} else {
											$("#email")
													.closest('.form-group')
													.removeClass(
															'has-success has-feedback')
													.addClass(
															'has-error has-feedback');
											error = "<span class=\"glyphicon glyphicon-remove form-control-feedback\"></span> The username should have at least <strong>4</strong> characters.";
											document.getElementById("e_email").innerHTML = error;

										}

									});

					$("#idcard")
							.change(
									function() {
										var ktp = $("#idcard").val();
										var dataString = {
											'frmProcess' : 'ktp',
											'ktp' : ktp,
										}
										if (ktp.length == 16) {
											$("#e_idcard")
													.html(
															'<img src="/images/loader.gif" align="absmiddle">&nbsp;Checking availability...');
											$
													.ajax({
														type : "POST",
														url : "/process/check.html",
														data : dataString,
														success : function(
																result) {
															if (result) {
																var obj = jQuery
																		.parseJSON(result);
																if (obj.msg == "OK") {
																	$("#idcard")
																			.closest(
																					'.form-group')
																			.removeClass(
																					'has-error has-feedback')
																			.addClass(
																					'has-success has-feedback');
																	document
																			.getElementById("e_idcard").innerHTML = "<span class=\"glyphicon glyphicon-ok form-control-feedback\"></span>";
																} else {
																	$("#idcard")
																			.closest(
																					'.form-group')
																			.removeClass(
																					'has-success has-feedback')
																			.addClass(
																					'has-error has-feedback');
																	error = "<span class=\"glyphicon glyphicon-remove form-control-feedback\"></span>"
																			+ obj.msg;
																	document
																			.getElementById("e_idcard").innerHTML = error;
																}
															}
														},

													});

										} else {
											$("#idcard")
													.closest('.form-group')
													.removeClass(
															'has-success has-feedback')
													.addClass(
															'has-error has-feedback');
											error = "<span class=\"glyphicon glyphicon-remove form-control-feedback\"></span> Must <strong>16</strong> Character.";
											document.getElementById("e_idcard").innerHTML = error;

										}

									});
					
					
					$("#handphone")
					.change(
							function() {
								var hp = $("#handphone").val();
								var dataString = {
									'frmProcess' : 'hp',
									'hp' : hp
								}
									$("#e_handphone").html('<img src="/images/loader.gif" align="absmiddle">&nbsp;Checking availability...');
									$
											.ajax({
												type : "POST",
												url : "/process/check.html",
												data : dataString,
												success : function(
														result) {
													if (result) {
														var obj = jQuery
																.parseJSON(result);
														if (obj.msg == "OK") {
															$("#handphone")
																	.closest(
																			'.form-group')
																	.removeClass(
																			'has-error has-feedback')
																	.addClass(
																			'has-success has-feedback');
															document
																	.getElementById("e_handphone").innerHTML = "<span class=\"glyphicon glyphicon-ok form-control-feedback\"></span>";
														} else {
															$("#handphone")
																	.closest(
																			'.form-group')
																	.removeClass(
																			'has-success has-feedback')
																	.addClass(
																			'has-error has-feedback');
															error = "<span class=\"glyphicon glyphicon-remove form-control-feedback\"></span>"
																	+ obj.msg;
															document
																	.getElementById("e_handphone").innerHTML = error;
														}
													}
												},

											});
							});
					

					$("#npwp")
							.change(
									function() {
										var ktp = $("#npwp").val();

										if (ktp.length == 20) {
											$("#npwp")
													.closest('.form-group')
													.removeClass(
															'has-error has-feedback')
													.addClass(
															'has-success has-feedback');
											document.getElementById("e_npwp").innerHTML = "<span class=\"glyphicon glyphicon-ok form-control-feedback\"></span>";
										} else {
											$("#npwp")
													.closest('.form-group')
													.removeClass(
															'has-success has-feedback')
													.addClass(
															'has-error has-feedback');
											error = "<span class=\"glyphicon glyphicon-remove form-control-feedback\"></span> Must <strong>15</strong> Character.";
											document.getElementById("e_npwp").innerHTML = error;
										}
									});

				});

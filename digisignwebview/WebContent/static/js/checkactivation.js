var pbK;
var usernext = 1;
function parseData(data) {
	pbK = data;
}

$.get("/js/km/public.pem", parseData);

function cekSk() {
    var checkBox = document.getElementById("sk");
    if (checkBox.checked == true) {
      document.getElementById("nextotp").disabled = false;
    } else {
      document.getElementById("nextotp").disabled = true;
    }
  }

function cekPwd() {
	// Encrypt with the public key...
	var encrypt = new JSEncrypt();
	encrypt.setPublicKey(pbK);

	
	var username = $("#uname").val();
	var password = $("#pwd").val();

	
	if (password.length > 3) {
	var pass = hex_md5(username + password);
	var encrypted = encrypt.encrypt(pass);
	var preid = document.getElementById("preid").value;

	var dataString = {
			'frmProcess' : 'checkActive',
			'username' : username,
			'password' : encrypted,
			'preid' : preid
	}

	
	$.ajax({
		type : "POST",
		url : "/process/check.html",
		data : dataString,
		success : function(result) {
			if (result) {
				var obj = jQuery.parseJSON(result);
				if (obj.msg == "OK") {
					usernext=0;
				} else {
					usernext=1;
					$("#e_username").closest('.form-group').removeClass(
							'has-success has-feedback').addClass(
							'has-error has-feedback');
					$("#e_password").closest('.form-group').removeClass(
					'has-success has-feedback').addClass(
					'has-error has-feedback');
					
				}
			}
		},

	});
	}
	return false;
}

function proOtp() {

	var pro = 0;

	if (usernext == 1) {
		pro = 1;
		swal("", "Username atau Password Salah", "error");
	} else

	if ($("#uname").val().length == 0) {
		pro = 1;
		swal("", "Username tidak boleh kosong!", "error");
	} else

	if ($("#pwd").val().length == 0) {
		pro = 1;
		swal("", "Password tidak boleh kosong!", "error");
	} else

	
	if (pro == 0) {
	
		$("#otp").val('');
		$("#otpModal").modal('show');
	}
	
	
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


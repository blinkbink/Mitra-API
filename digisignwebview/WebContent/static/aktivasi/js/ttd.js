$("#formSukses").hide();
var canvas = document.getElementById('signature-pad');
var clearButton = document.getElementById('clear');

var yes = 0;
var sgn = document.getElementById('sgn');
var esgn = document.getElementById('e_sgn');
var user = null;
$("#disabledbtn").hide();

function usernameCheck(link, refTrx)
{
	var usernameData = new FormData();
	usernameData.append('username', $("#username").val());
	usernameData.append('refTrx', refTrx);
    
	$.ajax({
        type: "POST",
        url: link+"/CheckUsername.html",            
        contentType: false,
        processData: false,
        data: usernameData,
        success: function(data, status, jqXHR){
        	//console.log(data);
        	
        	if(data == 100 && $( "#username" ).val().length > 5)
             {
        		if(/[^0-9^a-z^A-Z^.^_]/g.test($('#username').val()))
        		{
        			$("#null_name").text("Karakter tidak valid");
	        		$("#same_name").text("");
	        		$("#username").css("background-color","white");
	        		$("#notsame_name").closest('.form-group').removeClass('has-success has-feedback').addClass('has-error has-feedback');
	        		document.getElementById("notsame_name").innerHTML="<span class=\"glyphicon glyphicon-remove form-control-feedback\"></span>";
	                 
        		}else{
        			user = 0;
	                $("#same_name").text("");
	                $("#username").css("background-color","white");
	                $("#notsame_name").closest('.form-group').removeClass('has-error has-feedback').addClass('has-success has-feedback');
	                document.getElementById("notsame_name").innerHTML="<span class=\"glyphicon glyphicon-ok form-control-feedback\"></span>";
        		}
             }
        	else if(data == 200 && $( "#username" ).val().length > 5 )
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
        	$("#username").css("background-color","white");
            $("#notsame_name").closest('.form-group').removeClass('has-success has-feedback').addClass('has-error has-feedback');
            document.getElementById("notsame_name").innerHTML="<span class=\"glyphicon glyphicon-remove form-control-feedback\"></span>";
//            $("#same_name").text("Username sudah digunakan");
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
	  else if(/[^0-9^a-z^A-Z^.^_]/g.test($('#username').val()))
	    {
		  $("#null_name").text("Karakter tidak valid");
	      $("#same_name").text("");
	      $("#username").css("background-color","white");
	      $("#notsame_name").closest('.form-group').removeClass('has-success has-feedback').addClass('has-error has-feedback');
	      document.getElementById("notsame_name").innerHTML="<span class=\"glyphicon glyphicon-remove form-control-feedback\"></span>";
	    }
	  else 
		  {
		  		user = 0;
		  		$("#null_name").text("");
		  }
	}

//$( "#username" ).keyup(function() {
//	var usernameData = new FormData();
//	usernameData.append('username', $("#username").val());
//	
//	$.ajax({
//        type: "POST",
//        url: "https://wvapi.tandatanganku.com/CheckUsername.html",            
//        contentType: false,
//        processData: false,
//        data: usernameData,
//        success: function(data, status, jqXHR){
//        	//console.log(data);
//        	
//        	if(data == 100 && $( "#username" ).val().length > 5)
//             {
//                 user = 0;
//                 $("#same_name").text("");
//                 $("#username").css("background-color","white");
//                 $("#notsame_name").closest('.form-group').removeClass('has-error has-feedback').addClass('has-success has-feedback');
//                 document.getElementById("notsame_name").innerHTML="<span class=\"glyphicon glyphicon-ok form-control-feedback\"></span>";
//
//             }
//        	else if(data == 200 && $( "#username" ).val().length > 5)
//             {
//                 user = 1;
//                 $("#same_name").text("Username sudah digunakan");
//                 $("#username").css("background-color","white");
//                 $("#notsame_name").closest('.form-group').removeClass('has-success has-feedback').addClass('has-error has-feedback');
//                 document.getElementById("notsame_name").innerHTML="<span class=\"glyphicon glyphicon-remove form-control-feedback\"></span>";
//
//             }
//
//        	
//        },
//        error: function(jqXHR, textStatus, errorThrown){
//            user = 1;
//
//        	$("#same_name").text("Error : " + jqXHR.status);
//        }
//   });
//	
//	  if($( "#username" ).val() == "")
//		  {
//		  		user = 1;
//		  		$("#null_name").text("Harus diisi");
//		  		$("#same_name").text("");
//		  		$("#notsame_name").text("");
//		  }
//	  else if($( "#username" ).val().length < 6)
//	    {
//	        user = 1;
//	        $("#null_name").text("Panjang karakter min 6, maks 15 karakter.");
//	        $("#same_name").text("");
//	        $("#username").css("background-color","white");
//	        $("#notsame_name").closest('.form-group').removeClass('has-success has-feedback').addClass('has-error has-feedback');
//	        document.getElementById("notsame_name").innerHTML="<span class=\"glyphicon glyphicon-remove form-control-feedback\"></span>";
//	    }
//	  else
//		  {
//		  		user = 0;
//		  		$("#null_name").text("");
//		  }
//	});

$( "#inputVer" ).keyup(function() {
	  if($( "#inputVer" ).val() == "")
	  {
		    scoreverifikasi=1;
	  		$("#null_verifikasi").text("Harus diisi");
	  }
	  else
	  {		
		  	scoreverifikasi=0;
	  		$("#null_verifikasi").text("");
	  		$("#e_verifikasi").text("");
	  }
	});
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


function prosesaktivasi_session(link)
{
	
	var scoreUsername = null;
	var scorePassword = null;
	var scorePassword2 = null;
	var scoreTtd = null;
	var scoreSgn = null;
	var scoreSK = null;
	
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
				$("#null_sgn").text("Harus diisi");
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
	else if(/[^0-9^a-z^A-Z^.^_]/g.test($('#username').val()))
	{
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
	if ($('#sk').is(":checked") && $('#se').is(":checked"))
	{
	  // it is checked
		scoreSK = 0;
		$("#null_sk").text("");
		$("#null_se").text("");
	}
	else{
		scoreSK = 1;
		if(!$('#sk').is(":checked"))
		{
			$("#null_sk").text("Setujui Syarat dan Ketentuan terlebih dahulu!");
		}
		if(!$('#se').is(":checked"))
		{
			$("#null_se").text("Setujui penerbitan sertifikat elektronik dahulu!!");
		}

		if($('#sk').is(":checked"))
		{
			$("#null_sk").text("");
		}
		if($('#se').is(":checked"))
		{
			$("#null_se").text("");
		}
	}
	if(scoreSK==0 && user == 0 && scoreUsername == 0 && scorePassword == 0 && scorePassword2 == 0 && scoreConfirm == 0 && scoreTtd == 0 && scoreSgn == 0 && str >= 34)
	{
			$("#otpModal").modal('show');
	  		if (!$("#btnotp").attr('disabled'))
	        {
	            $("#btnotp").click();
	        }
	}
}

function prosesaktivasi_session_ver(link)
{
	
	var scoreUsername = null;
	var scorePassword = null;
	var scorePassword2 = null;
	var scoreTtd = null;
	var scoreSgn = null;
	var scoreverifikasi = null;
	var scoreSK = null;
	
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
				$("#null_sgn").text("Harus diisi");
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
	else if(/[^0-9^a-z^A-Z^.^_]/g.test($('#username').val()))
	{
		scoreUsername = 1;
		$("#null_name").text("Karakter tidak valid");
	}
	else
	{
		scoreUsername = 0;
		$("#null_name").text("");
	}
	if($( "#inputVer" ).val() == "")
	{
			scoreverifikasi=1;
	  		$("#null_verifikasi").text("Harus diisi");
	}
	  else
	{		
		  	scoreverifikasi=0;
	  		$("#null_verifikasi").text("");
	  		$("#e_verifikasi").text("");
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
	if ($('#sk').is(":checked"))
	{
	  // it is checked
		scoreSK = 0;
		$("#null_sk").text("");
	}
	else{
		scoreSK = 1;
		$("#null_sk").text("Setujui Syarat dan Ketentuan terlebih dahulu!");
	}
	
	if(scoreSK==0 && scoreverifikasi == 0 && user == 0 && scoreUsername == 0 && scorePassword == 0 && scorePassword2 == 0 && scoreConfirm == 0 && scoreTtd == 0 && scoreSgn == 0 && str >= 34)
	{
		$("#e_proses").text("");
//		$("#prosesOTP").button('loading');
		$("#prosesaktivasi").hide();
    	$("#disabledbtn").show();
    	$("#e_verifikasi").text("");
		var dataVer = new FormData();
		
		dataVer.append('nohp', $("#email").val());
		dataVer.append('otpcode', $("#inputVer").val());
		dataVer.append('sessionid', $("#sessionid").val());
		dataVer.append('refTrx', $("#sessionid").val());
		
		$.ajax({
	        type: "POST",
	        url: link+"/CheckOTP.html",   
	        contentType: false,
	        processData: false,
	        data: dataVer,
	        success: function(data, status, jqXHR){
	        	
	        	if(data == 408 ||data == 401)
	        	{
	        		Swal.fire({
		        		  position: 'center',
		        		  icon: 'error',
		        		  title: 'Tidak dapat melanjutkan aktivasi, sesi habis',
		        		  showConfirmButton: true,
		        		  confirmButtonColor: '#3085d6'
		        		}).then((result) => {
	    			  location.reload();
	    			})
	        	}
	        	else
	        	{

		        	$("#e_otp").text("");
		        	$("#prosesaktivasi").show();
		        	$("#disabledbtn").hide();
//		        	$("#prosesOTP").button("reset");
//		        	document.getElementById("btnotp").disabled = true;
//		        	document.getElementById("prosesOTP").disabled = true;
		        	
//		        	$("#prosesaktivasi").hide();
//		        	$("#reset").hide();
//		        	setTimeout(function() {
//		        		$("#otpModal").modal('hide');
//		        	}, 700);
//		        	  	
//		        	setTimeout(function() {
//		        		$("#loadingModal").modal('show');
//		        	}, 1000);
		        	if(data == 200)
		            {
		        		$("#otpModal").modal('show');
					  		if (!$("#btnotp").attr('disabled'))
					        {
					            $("#btnotp").click();
					        }
				  	}else if(data == 404){
				  		$("#prosesaktivasi").show();
			        	$("#disabledbtn").hide();
			        	$("#e_verifikasi").text("Kode Verifikasi Salah");
				  	}else{
				  		$("#prosesaktivasi").show();
			        	$("#disabledbtn").hide();
			        	$("#e_verifikasi").text("Pengecekan Gagal, Coba Lagi");
				  	}
	        	}
	        	
	        	
	        	},
	        	error: function(jqXHR, textStatus, errorThrown){
	        	
//	        	document.getElementById("btnotp").disabled = false;
	//	        	$("#prosesOTP").button("reset");
		        	$("#prosesaktivasi").show();
		        	$("#disabledbtn").hide();
		        	$("#e_verifikasi").text("Kode Verifikasi Salah");
	        }
		 });
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
	
	dataOTP.append('nohp', $("#handphone").val());
	dataOTP.append('otpcode', $("#otp").val());
	dataOTP.append('refTrx', refTrx);
	dataOTP.append('sessionid', $("#sessionid").val());
	
	$.ajax({
        type: "POST",
        url: link+"/CheckOTP.html",   
        contentType: false,
        processData: false,
        data: dataOTP,
        success: function(data, status, jqXHR){
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
            
            formData.append('preid', $("#preid").val());
        	formData.append('email', $("#email").val());
        	formData.append('username', $("#username").val());
        	formData.append('password', $("#password").val());
        	formData.append('sessionid', $("#sessionid").val());
        	formData.append('fttd', file);
        	formData.append('refTrx', refTrx);
        	var sk = document.getElementById('sk');
            var se = document.getElementById('se');

            formData.append('sk', sk.checked);
            formData.append('se', se.checked);

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
        	
        },
        error: function(jqXHR, textStatus, errorThrown){
        	
        	document.getElementById("btnotp").disabled = false;
        	$("#prosesOTP").button("reset");
        	$("#e_otp").text("OTP Salah");
        }
	 });
}

function submitOTP_session(link, refTrx)
{
	$("#e_proses").text("");
	$("#prosesOTP").button('loading');
	var dataOTP = new FormData();
	
	dataOTP.append('nohp', $("#handphone").val());
	dataOTP.append('otpcode', $("#otp").val());
	dataOTP.append('refTrx', refTrx);
	dataOTP.append('sessionid', $("#sessionid").val());
	
	$.ajax({
        type: "POST",
        url: link+"/CheckOTP.html",   
        contentType: false,
        processData: false,
        data: dataOTP,
        success: function(data, status, jqXHR){
        	
        	if(data == 408 ||data == 401)
        	{
        		Swal.fire({
	        		  position: 'center',
	        		  icon: 'error',
	        		  title: 'Tidak dapat melanjutkan aktivasi, sesi habis',
	        		  showConfirmButton: true,
	        		  confirmButtonColor: '#3085d6'
	        		}).then((result) => {
    			  location.reload();
    			})
        	}
        	else
        	{
        		if(data == 200){
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
    	            
    	            formData.append('preid', $("#preid").val());
    	            formData.append('sessionid', $("#sessionid").val());
    	            formData.append('sessionkey', $("#sessionkey").val());
    	        	formData.append('email', $("#email").val());
    	        	formData.append('username', $("#username").val());
    	        	formData.append('password', $("#password").val());
    	        	formData.append('fttd', file);
    	        	formData.append('refTrx', refTrx);
    	        	var sk = document.getElementById('sk');
    	            var se = document.getElementById('se');

    	            formData.append('sk', sk.checked);
    	            formData.append('se', se.checked);
    	
    	        	$.ajax({
    	                type: "POST",
    	                url: link+"/AktivasiMitra_session.html",     
    	                contentType: false,
    	                processData: false,
    	                data: formData,
    	                success: function(data, status, jqXHR){
    	                	var result = JSON.parse(data);
    	                	jo = result;
    	                	if(result.rc == "408" ||result.rc == "401")
    	                	{
    	                		Swal.fire({
    	  		        		  position: 'center',
    	  		        		  icon: 'error',
    	  		        		  title: 'Tidak dapat melanjutkan aktivasi, sesi habis',
    	  		        		  showConfirmButton: true,
    	  		        		  confirmButtonColor: '#3085d6'
    	  		        		}).then((result) => {
    		        			  location.reload();
    		        			})
    	                	}
    	                	else
    	                	{
    	                		if(jo.link != null){
//    		                		window.location.href =  jo.link;
    		                		console.log(data);
    		                    	
    		                		setTimeout(function()
    		                		{
    		                			$("#loadingModal").modal('hide');
    		                		}, 2100);
    		            		
    			                	$("#disabledbtn").val("Aktivasi Berhasil");
    			                	
    			                	setTimeout(function() {
    			                    	$("#formAktivasi").hide();
    			                    	$("#formSukses").show();
    			                    	$("#formSukses-footer").show();
    			                	}, 2500);
    			                	$link_red =  jo.link;
    								$("#prosesaktivasi").hide();
    								$("#reset").hide();
    		
    		                	}
    		                	if(result.rc == "00")
    		                	{   
    		                		console.log(data);
    		                    	
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
    		
//    								window.parent.postMessage(data, '*');
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
            	}else if(data==404){
            		document.getElementById("btnotp").disabled = false;
                	$("#prosesOTP").button("reset");
                	$("#e_otp").text("OTP Salah");
            	}else{
            		document.getElementById("btnotp").disabled = false;
                	$("#prosesOTP").button("reset");
                	$("#e_otp").text("Koneksi Error, Coba Lagi");
            	}
        	}
        },
        error: function(jqXHR, textStatus, errorThrown){
        	
        	document.getElementById("btnotp").disabled = false;
        	$("#prosesOTP").button("reset");
        	$("#e_otp").text("OTP Salah");
        }
	 });
}



function submitOTPEmail_session(link, refTrx)
{

	var dataOTP = new FormData();
	
	dataOTP.append('data', usertype.rtype);
	dataOTP.append('verEmail', $("#verEmail").val());
	dataOTP.append('refTrx', refTrx);
	dataOTP.append('sessionid', $("#sessionid").val());
	
	$.ajax({
        type: "POST",
        url: link+"/CheckOTP.html",   
        contentType: false,
        processData: false,
        data: dataOTP,
        success: function(data, status, jqXHR){
        	
        	if(data == 408 ||data == 401)
        	{
        		Swal.fire({
	        		  position: 'center',
	        		  icon: 'error',
	        		  title: 'Tidak dapat melanjutkan aktivasi, sesi habis',
	        		  showConfirmButton: true,
	        		  confirmButtonColor: '#3085d6'
	        		}).then((result) => {
    			  location.reload();
    			})
        	}
        	else
        	{
        		if(data == 200){
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
    	
            	}else if(data==404){
            		document.getElementById("btnotpEmail").disabled = false;
            		document.getElementById("prosesOTPEmail").disabled = false;
            		$("#e_verEmail").text("Kode verifikasi salah");
            		$("#notif_verEmail").text("");
            	}else{
            		document.getElementById("btnotpEmail").disabled = false;
            		document.getElementById("prosesOTPEmail").disabled = false;
            		$("#e_verEmail").text("Koneksi error, silahkan coba kembali");
            		$("#notif_verEmail").text("");
            	}
        	}
        	
        },
        error: function(jqXHR, textStatus, errorThrown){
        	
        	document.getElementById("btnotp").disabled = false;
        	$("#prosesOTPEmail").button("reset");
        	$("#e_verifEmail").text("OTP Salah");
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

function otp(link) {
	document.getElementById("reqOtp").disabled = true;
	
	var counter = 60;

	var interval = setInterval(function() {
	    counter--;
	    
	    if (counter <= 0) 
	    {
	     	clearInterval(interval);
	     	$("#sukses_otp").text("");
	     	$("#gagal_otp").text("");
	        return;
	    }
	    else
	    {
	    	document.getElementById("reqOtp").disabled = true;
	    	document.getElementById("reqOtp").value=counter + " S";
	    }
	}, 1000);
	
	setTimeout(function() {
        document.getElementById("reqOtp").disabled = false;
        document.getElementById("reqOtp").value="Kirim OTP lagi";
      }, 60000);
		
	$.ajax({
		url: link+'/COTP.html',
		//url: 'https://wvapi.digisign.id/COTP.html',
		dataType: 'json',
		type: 'post',
		contentType: 'application/json',
		data: JSON.stringify(usersign),
		processData: false,
		success: function( data, textStatus, jQxhr ){
		
			if(data.rc == "408" || data.rc == "401")
			{
				alertDanger(data.info,0);
				
				$('#alertModal').on('hidden.bs.modal', function () {
					location.reload();
				});
			}
			else
			{
				if(data.rc == "00")
				{
					$("#sukses_otp").text(data.notif);	
				}
				else if(data.rc == "05")
				{
					$("#gagal_otp").text(data.notif);	
				}
				else if(data.result=="E1")
				{
					alertDanger(data.info,0);
				}

				else{
					$("#certEmpty").modal('hide');
					$("#alertKonfirmasi").modal('hide');
					$("#alertModal").modal('hide');
					$("#loadingPage").modal('hide');
				
					//check kolom doc link has
					if(data.doc_link != null)
					{
						showPDF(data.doc_link);
						
						$("#cancelLocation").hide();
						$("#prosesSign").hide();
						$("#location").hide();
						$("#sgn-widget").hide();
						$("#alertModal").modal('hide');
						alertDanger("Dokumen Sudah Pernah ditandatangan ",0);
					}
					else
					{
						alertDanger(data.info, 0);
						
						if(data.result  == '408' || data.result  == '401')
						{
							$('#alertModal').on('hidden.bs.modal', function () {
								location.reload();
							});
						}
					}
				}
			}
			
			
		},
		error: function( jqXhr, textStatus, errorThrown ){
//			$("#alertKonfirmasi").modal('hide');
//			alertDanger("Data gagal diproses",0);
			$("#gagal_otp").text("Gagal kirim OTP");

		}
	});
	return false;
}

function otpNewCert2(link) {
	document.getElementById("reqOtpNew").disabled = true;
	
	var counter = 60;
	var interval = setInterval(function() {
	    counter--;
	    
	    if (counter <= 0) 
	    {
	     	clearInterval(interval);
	     	$("#sukses_otpNew").text("");
	     	$("#gagal_otpNew").text("");
	        return;
	    }
	    else
	    {
	    	document.getElementById("reqOtpNew").disabled = true;
	    	document.getElementById("reqOtpNew").value=counter + " S";
	    }
	}, 1000);
	
	setTimeout(function() {
        document.getElementById("reqOtpNew").disabled = false;
        document.getElementById("reqOtpNew").value="Kirim OTP lagi";
      }, 60000);
	var form = new FormData();
    //form.append("jsonfield", "{\"JSONFile\":"+useruser+"}");
    form.append("type", useruser.type);
    form.append("ptype", useruser.ptype);
    form.append("utype", useruser.utype);
    form.append("refTrx", refTrx);
    form.append("setype", useruser.sessionid);
	var settings = {
	        "async": true,
	        "crossDomain": true,
	        "url": link+'/GOTP.html',
	        "method": "POST",
	        "headers": {
	          "cache-control": "no-cache",
	         },
	        "processData": false,
	        "contentType": false,
	        "mimeType": "multipart/form-data",
	        "data": form
	      }

    $.ajax(settings).done(function (response) {
    	var JsonObject = JSON.parse(response);
    	if(JsonObject.rc == "00")
		{
			$("#sukses_otpNew").text(JsonObject.notif);	
		}
		else if(JsonObject.rc == "05")
		{
			$("#gagal_otpNew").text(JsonObject.notif);	
		}
		else if(JsonObject.rc=="E1")
		{
			alertDanger(JsonObject.notif,0);
		}

		else{
			$("#alertKonfirmasi").modal('hide');
			$("#certEmpty").modal('hide');
			alertDanger(JsonObject.notif, 0);
			
			if(JsonObject.rc  == '408' || JsonObject.rc  == '401')
			{
				$('#alertModal').on('hidden.bs.modal', function () {
					location.reload();
				});
			}
		}
		

    });
 	return false;
}


function otp2(link) {
	document.getElementById("reqOtp").disabled = true;
	
	var counter = 60;
	var interval = setInterval(function() {
	    counter--;
	    
	    if (counter <= 0) 
	    {
	     	clearInterval(interval);
	     	$("#sukses_otp").text("");
	     	$("#gagal_otp").text("");
	        return;
	    }
	    else
	    {
	    	document.getElementById("reqOtp").disabled = true;
	    	document.getElementById("reqOtp").value=counter + " S";
	    }
	}, 1000);
	
	setTimeout(function() {
        document.getElementById("reqOtp").disabled = false;
        document.getElementById("reqOtp").value="Kirim OTP lagi";
      }, 60000);
	var form = new FormData();
    //form.append("jsonfield", "{\"JSONFile\":"+useruser+"}");
    form.append("type", useruser.type);
    form.append("ptype", useruser.ptype);
    form.append("utype", useruser.utype);
    form.append("refTrx", refTrx);
    form.append("setype", useruser.sessionid);
	var settings = {
	        "async": true,
	        "crossDomain": true,
	        "url": link+'/GOTP.html',
	        "method": "POST",
	        "headers": {
	          "cache-control": "no-cache",
	         },
	        "processData": false,
	        "contentType": false,
	        "mimeType": "multipart/form-data",
	        "data": form
	      }

    $.ajax(settings).done(function (response) {
    	var JsonObject = JSON.parse(response);
    	if(JsonObject.rc == "00")
		{
			$("#sukses_otp").text(JsonObject.notif);	
		}
		else if(JsonObject.rc == "05")
		{
			$("#gagal_otp").text(JsonObject.notif);	
		}
		else if(JsonObject.rc=="E1")
		{
			alertDanger(JsonObject.notif,0);
		}
		else{
			$("#alertKonfirmasi").modal('hide');
			
			alertDanger(JsonObject.notif, 0);
			
			if(JsonObject.rc  == '408' || JsonObject.rc  == '401')
			{
				$('#alertModal').on('hidden.bs.modal', function () {
					location.reload();
				});
			}
		}
		

    });
 	return false;
}

function otp_email_activsign(link) {
	document.getElementById("reqVer").disabled = true;
	
	var formData = new FormData();
	var counter = 60;
	
	var interval = setInterval(function() {
	    counter--;
	    
	    if (counter <= 0) 
	    {
	     	clearInterval(interval);
	     	$("#sukses_otp_ver").text("");
	     	$("#gagal_otp_ver").text("");
	        return;
	    }
	    else
	    {
	    	document.getElementById("reqVer").disabled = true;
	    	$('#reqVer').val(counter + " S");
	    }
	}, 1000);
	
	setTimeout(function() {
        document.getElementById("reqVer").disabled = false;
        document.getElementById("reqVer").value="Kirim ulang Kode Verifikasi";
      }, 60000);
		
//	formData.append('email', $("#inputemails").val());
//	formData.append('idmitra', $("#idmitra").val());
	formData.append("type", usertype.type);
	formData.append('etype', usertype.etype);
    formData.append("utype", usertype.utype);
	formData.append('stype', true);
	$.ajax({
		url: link+'/GOTP.html',
		type: 'POST',
		contentType: false,
		data: formData,
		processData: false,
		success: function( data, textStatus, jQxhr ){
			var result = JSON.parse(data);
			
			if(result.rc == "00")
			{
				$("#sukses_otp_ver").text(result.notif);	
			}
			else
			{
				$("#gagal_otp_ver").text(result.notif);	
			}

		},
		error: function( jqXhr, textStatus, errorThrown ){
			$("#gagal_otp_ver").text("Gagal Mengirim Kode Verifikasi");
		}
	});
	return false;
}

function otpNewCert(link) {
	document.getElementById("reqOtpNew").disabled = true;
	
	var counter = 60;

	var interval = setInterval(function() {
	    counter--;
	    
	    if (counter <= 0) 
	    {
	     	clearInterval(interval);
	     	$("#sukses_otpNew").text("");
	     	$("#gagal_otpNew").text("");
	        return;
	    }
	    else
	    {
	    	document.getElementById("reqOtpNew").disabled = true;
	    	document.getElementById("reqOtpNew").value=counter + " S";
	    }
	}, 1000);
	
	setTimeout(function() {
        document.getElementById("reqOtpNew").disabled = false;
        document.getElementById("reqOtpNew").value="Kirim OTP lagi";
      }, 60000);
		
	$.ajax({
		url: link+'/COTP.html',
		//url: 'https://wvapi.digisign.id/COTP.html',
		dataType: 'json',
		type: 'post',
		contentType: 'application/json',
		data: JSON.stringify(usersign),
		processData: false,
		success: function( data, textStatus, jQxhr ){
		
			if(data.rc == "00")
			{
				$("#sukses_otpNew").text(data.notif);	
			}
			else if(data.rc == "05")
			{
				$("#gagal_otpNew").text(data.notif);	
			}
			else if(data.rc=="E1")
			{
				alertDanger(data.info,0);
			}
			else{
				
				$("#alertKonfirmasi").modal('hide');
				$("#certEmpty").modal('hide');
				$("#alertModal").modal('hide');
				$("#loadingPage").modal('hide');
			
				//check kolom doc link has
				if(data.doc_link != null)
				{
					showPDF(data.doc_link);
					
					$("#cancelLocation").hide();
					$("#prosesSign").hide();
					$("#location").hide();
					$("#sgn-widget").hide();
					$("#alertModal").modal('hide');
					alertDanger("Dokumen Sudah Pernah ditandatangan ",0);
				}
				else
				{
					alertDanger(data.info, 0);
					
					if(data.result  == '408' || data.result  == '401')
					{
						$('#alertModal').on('hidden.bs.modal', function () {
							location.reload();
						});
					}
				}
			}
			
		},
		error: function( jqXhr, textStatus, errorThrown ){
			$("#alertKonfirmasi").modal('hide');
			alertDanger("Data gagal diproses",0);

		}
	});
	return false;
}

function proses_verifikasi(link)
{
	
		$("#e_proses").text("");
//		$("#prosesOTP").button('loading');
		$("#cekverif").hide();
    	$("#disabledbtn").show();
    	$("#e_verifikasi").text("");
		var dataVer = new FormData();
		
		dataVer.append('nohp', $("#inputemails").val());
		dataVer.append('otpcode', $("#inputVer").val());
		$.ajax({
	        type: "POST",
	        url: link+"/CheckOTP.html",   
	        contentType: false,
	        processData: false,
	        data: dataVer,
	        success: function(data, status, jqXHR){
	        	$("#e_otp").text("");
	        	$("#cekverif").show();
	        	$("#disabledbtn").hide();
//	        	$("#prosesOTP").button("reset");
//	        	document.getElementById("btnotp").disabled = true;
//	        	document.getElementById("prosesOTP").disabled = true;
	        	
//	        	$("#prosesaktivasi").hide();
//	        	$("#reset").hide();
//	        	setTimeout(function() {
//	        		$("#otpModal").modal('hide');
//	        	}, 700);
//	        	  	
//	        	setTimeout(function() {
//	        		$("#loadingModal").modal('show');
//	        	}, 1000);
	        	if(data == 200)
	            {
//	        		$("#modalverif").modal('hide');
//	        		$("#modalActive").modal('show');
	        		$("#modalVerif").fadeOut();
	        		$("#modalActive").fadeIn();
			  	}else if(data == 404){
			  		$("#cekverif").show();
		        	$("#disabledbtn").hide();
		        	$("#e_verifikasi").text("Kode Verifikasi Salah");
			  	}else{
			  		$("#cekverif").show();
		        	$("#disabledbtn").hide();
		        	$("#e_verifikasi").text("Pengecekan Gagal, Coba Lagi");
			  	}
	        	
	        	},
	        	error: function(jqXHR, textStatus, errorThrown){
	        	
//	        	document.getElementById("btnotp").disabled = false;
	//	        	$("#prosesOTP").button("reset");
		        	$("#cekverif").show();
		        	$("#disabledbtn").hide();
		        	$("#e_verifikasi").text("Kode Verifikasi Salah");
	        }
		 });
	
}
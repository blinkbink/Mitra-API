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
			else if(data.result=="E1")
			{
				alertDanger(data.info,0);
			}

			else{
				
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
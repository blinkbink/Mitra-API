function verifEmail()
{
	$("#verifEmailModal").modal('show');
}

function otpEmail(link, refTrx) {
	document.getElementById("btnotpEmail").disabled = true;
	
	var formData = new FormData();
	var counter = 60;
	
	var interval = setInterval(function() {
	    counter--;
	    
	    if (counter <= 0) 
	    {
	     	clearInterval(interval);
	     	$("#notif_verEmail").text("");
	     	$("#e_verEmail").text("");
	        return;
	    }
	    else
	    {
	    	document.getElementById("btnotpEmail").disabled = true;
	    	$('#btnotpEmail').val(counter + " S");
	    }
	}, 1000);
	
	setTimeout(function() {
        document.getElementById("btnotpEmail").disabled = false;
        document.getElementById("btnotpEmail").value="Kirim OTP lagi";
      }, 60000);
		

	formData.append("type", usertype.type);
	formData.append("etype", usertype.etype);
	formData.append("rtype", usertype.rtype);
	formData.append("refTrx", refTrx);
	formData.append("seacttype", $("#sessionid").val());
	
	
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
				$("#notif_verEmail").text(result.notif);
				$("#e_verEmail").text("");
			}
			else
			{
				if(result.rc == "408" || result.rc == "401")
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
					$("#e_verEmail").text(result.notif);	
					$("#notif_verEmail").text("");
				}
			}

		},
		error: function( jqXhr, textStatus, errorThrown ){
			$("#e_verEmail").text("Gagal Mengirim OTP");
		}
	});
	return false;
}

function otp(link, refTrx) {
	
	document.getElementById("btnotp").disabled = true;
	$("#e_otp").text("");
	
	var formData = new FormData();
	var counter = 60;
	
	var interval = setInterval(function() {
	    counter--;
	    
	    if (counter <= 0) 
	    {
	     	clearInterval(interval);
	     	$("#notif_handphone").text("");
	     	$("#e_handphone").text("");
	        return;
	    }
	    else
	    {
	    	document.getElementById("btnotp").disabled = true;
	    	$('#btnotp').val(counter + " S");
	    }
	}, 1000);
	
	setTimeout(function() {
        document.getElementById("btnotp").disabled = false;
        document.getElementById("btnotp").value="Kirim OTP lagi";
      }, 60000);
		
//	formData.append('nohp', $("#handphone").val());
//	formData.append('idmitra', $("#nomer").val());
//	formData.append('preid', $("#nomnom").val());
	formData.append("type", usertype.type);
	formData.append("ptype", usertype.ptype);
	formData.append("rtype", usertype.rtype);
	formData.append("refTrx", refTrx);
	formData.append("seacttype", $("#sessionid").val());
	
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
				$("#notif_handphone").text(result.notif);	
			}
			else
			{
				if(result.rc == "408" || result.rc == "401")
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
					$("#e_handphone").text(result.notif);
				}
					
			}

		},
		error: function( jqXhr, textStatus, errorThrown ){
			$("#e_handphone").text("Gagal Mengirim OTP");
		}
	});
	return false;
}

function otp_email(link) {
	document.getElementById("reqVer").disabled = true;
	
	var formData = new FormData();
	var counter = 60;
	
	var interval = setInterval(function() {
	    counter--;
	    
	    if (counter <= 0) 
	    {
	     	clearInterval(interval);
	     	$("#notif_handphone").text("");
	     	$("#e_handphone").text("");
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
		
//	formData.append('email', $("#email").val());
////	formData.append('idmitra', $("#idmitra").val());
//	formData.append('idmitra', $("#nomer").val());
//	formData.append('preid', $("#nomnom").val());
	formData.append("type", usertype.type);
	formData.append("etype", usertype.etype);
	formData.append("rtype", usertype.rtype);
	formData.append("seacttype", $("#sessionid").val());
	
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
				$("#notif_handphone").text(result.notif);	
			}
			else
			{
				if(result.rc == "408" || result.rc == "401")
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
					$("#e_handphone").text(result.notif);	
				}
				
				
			}

		},
		error: function( jqXhr, textStatus, errorThrown ){
			$("#e_handphone").text("Gagal Mengirim Kode Verifikasi");
		}
	});
	return false;
}

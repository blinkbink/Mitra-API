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
		

	formData.append('nohp', $("#nohp").val());
	formData.append('type', "email");
	formData.append('refTrx', refTrx);
	
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
				$("#e_verEmail").text(result.notif);	
				$("#notif_verEmail").text("");
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
		
	formData.append('nohp', $("#nohp").val());
	formData.append('refTrx', refTrx);
	
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
				$("#e_handphone").text(result.notif);	
			}

		},
		error: function( jqXhr, textStatus, errorThrown ){
			$("#e_handphone").text("Gagal Mengirim OTP");
		}
	});
	return false;
}
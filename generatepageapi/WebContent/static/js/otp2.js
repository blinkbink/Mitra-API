function otp() {
	document.getElementById("reqOtp").disabled = true;

	var counter = 60;
	
	var interval = setInterval(function() {
	    counter--;
	    
	    if (counter <= 0) 
	    {
	     	clearInterval(interval);
	        return;
	    }
	    else
	    {
	    	document.getElementById("reqOtp").disabled = true;
	    	$('#reqOtp').val(counter + " S");
	    }
	}, 1000);
	
	setTimeout(function() {
        document.getElementById("reqOtp").disabled = false;
        document.getElementById("reqOtp").value="Kirim OTP lagi";
      }, 60000);
	
	
	$.ajax({
		url: 'https://wvapi.tandatanganku.com/COTP.html',
		//url: 'https://wvapi.digisign.id/COTP.html',
		dataType: 'json',
		type: 'post',
		contentType: 'application/json',
		data: JSON.stringify(usersign),
		processData: false,
		success: function( data, textStatus, jQxhr ){
			
			var res = data;

			if(res.result=="00"){
				//DS.ResultSignDoc(1,res.status);
				return false;
			}else if(res.result=="E1"){
				alertDanger(res.info,0);
			}else{
				$("#alertKonfirmasi").modal('hide');
				alertDanger(res.info,0);
			}
		},
		error: function( jqXhr, textStatus, errorThrown ){
			$("#alertKonfirmasi").modal('hide');
			alertDanger("Data gagal diproses",0);

		}
	});
	return false;
}
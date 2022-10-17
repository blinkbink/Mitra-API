function saveLoc(){
	

	if(cekComplete()){
		$.ajax({
			url: 'prc_reqsign.html',
			dataType: 'json',
			type: 'post',
			contentType: 'application/json',
			data: JSON.stringify(dataUser),
			processData: false,
			success: function( data, textStatus, jQxhr ){
				
				var res = data;

				if(res.result=="00"){
					DS.ResultLocSign(1);
				}else{
					alertDanger("Data gagal diproses",0);
				}
			},
			error: function( jqXhr, textStatus, errorThrown ){
				alertDanger("Data gagal diproses",0);

			}
		});
		

	}
	else{
	
		alertDanger("Lokasi tandatangan belum lengkap. Silakan lengkapi terlebih dahulu",0);
	
	}
}
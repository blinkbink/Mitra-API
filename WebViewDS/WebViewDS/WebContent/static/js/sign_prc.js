function saveSign(){
	if(cekComplete()){
		$.ajax({
			url: 'prc_sign.html',
			dataType: 'json',
			type: 'post',
			contentType: 'application/json',
			data: JSON.stringify(dataUser),
			processData: false,
			success: function( data, textStatus, jQxhr ){
				
				var res = data;

				if(res.result=="00"){
					DS.ResultSignDoc(1,res.status);
				}else if(res.result=="E1"){
					alertDanger(res.info,0);
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
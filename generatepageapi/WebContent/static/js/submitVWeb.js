function prosesSignMitraVWeb(){
	
	if(cekComplete()){
		$.ajax({
			url: 'https://wvapi.tandatanganku.com/prc_signMitraV.html',
			//url: 'https://wvapi.digisign.id/prc_signMitraV.html',
			dataType: 'json',
			type: 'post',
			contentType: 'application/json',
			data: JSON.stringify(dataUser),
			processData: false,
			success: function( data, textStatus, jQxhr ){
				
				var res = data;
				$("#alertKonfirmasi").modal('hide');
				
				if(res.result=="00"){
					$("#alertKonfirmasi").modal('hide');
					$("#alertModal").modal('hide');
					$("#loadingPage").modal('hide');
					
					window.parent.postMessage(res, '*');
				}else if(res.result=="E1"){
					$("#alertKonfirmasi").modal('hide');
					
					alertDanger(res.notif,0);
				}else{
					$("#alertKonfirmasi").modal('hide');
					
					alertDanger(res.notif,0);
				}
			},
			error: function( jqXhr, textStatus, errorThrown ){
				$("#alertKonfirmasi").modal('hide');
				
				alertDanger(res.notif,0);
			}
		});
		

	}
	else{
	
		alertDanger("Lokasi tandatangan belum lengkap. Silakan lengkapi terlebih dahulu",0);
	
	}
}
function savepwd(){
	//alert("masuk save pwd");
	//if(savepassword()){
		//alert("masuk save pwd kedua");
		$.ajax({
			url: 'https://wvapi.tandatanganku.com/savePWD.html',
			dataType: 'json',
			type: 'post',
			contentType: 'application/json',
			data: JSON.stringify(dataUser),
			processData: false,
			success: function( data, textStatus, jQxhr ){
				
				var res = data;

				if(res.result=="00"){
					//DS.ResultSignDoc(1,res.status);
					$("#modalActive").hide();
					statususer=res.status;
					alertSucess(res.info,0);
				}else if(res.result=="E1"){
					alertDanger(res.info,0);
					
				}else if(res.result=="12"){
					statususer=res.status;
					alertDanger(res.info,0);
					active(res.status);
				}
				else{
					//alertDanger("Data gagal diproses. silahkan ulangi kembali.",0);
					alertDanger(res.info,0);
				}
			},
			error: function( jqXhr, textStatus, errorThrown ){
				alertDanger("Data gagal diproses",0);
			}
		});
	//}
}
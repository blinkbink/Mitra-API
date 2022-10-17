function savepwd(link){

		$.ajax({
			url: link+'/savePWD.html',
			dataType: 'json',
			type: 'post',
			contentType: 'application/json',
			data: JSON.stringify(dataUser),
			processData: false,
			success: function( data, textStatus, jQxhr ){
				
				var res = data;

				if(res.result=="00"){
					//DS.ResultSignDoc(1,res.status);
					//$("#modalActive").hide();
					$("#modalActive").modal('hide');
					statususer=res.status;
					alertSucess(res.info,0);
				}else if(res.result=="E1"){
					alertDanger(res.info,0);
				}else if(res.result=="12"){
					statususer=res.status;
					alertDanger(res.info,0);
				}
				
				
				//Ditambah 1/11/2019 10:35 AM
				else if(res.result=="16"){
					statususer=res.status;
					alertDanger(res.info,0);
					$("#modalActive").hide();

				}
				else if(res.result == "13")
				{
					
						showPDF(res.doc_link);
						$("#prosesSign").hide();
						$("#cancelLocation").hide();
						$("#location").hide();
						$("#sgn-widget").hide();
						$("#alertModal").modal('hide');
						$("#modalActive").hide();

						alertDanger(res.info,0);
										
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
function showPDF2(path)
{
	$("#pdf-loader").show();

	PDFJS.getDocument({ url: path }).then(function(pdf_doc) {
		__PDF_DOC = pdf_doc;
		__TOTAL_PAGES = __PDF_DOC.numPages;
		
		// Hide the pdf loader and show pdf container in HTML
		$("#pdf-loader").hide();
		$("#pdf-contents").show();
		$("#pdf-total-pages").text(__TOTAL_PAGES);

		// Show the first page
		showPage(1);
		//Android.setPage(__TOTAL_PAGES);

	}).catch(function(error) {
		// If error re-show the upload button
		$("#pdf-loader").hide();
		$("#upload-button").show();
		
		alert(error.message);
	});
}

function prosesSignMitraV(link){
	
	if(cekComplete()){
		$.ajax({
			url: link+'/prc_signMitraV.html',
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
					
					
					if(res.doc_link != null)
						{
							showPDF(res.doc_link);
							$("#prosesSign").hide();
							$("#cancelLocation").hide();
							$("#location").hide();
							$("#sgn-widget").hide();
							$("#alertModal").modal('hide');
							alertDanger("Dokumen Sudah Pernah ditandatangan",0);
						}
					else
						{
							parent.Return(JSON.stringify(res));
						}
				
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


function checkDocSign(link, cert){
	
	if(cekComplete()){
		$.ajax({
			url: link+'/prc_checkdocsign.html',
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
					
					if(res.doc_link != null)
					{
						//check kolom doc link has
						showPDF(res.doc_link);
						
						$("#cancelLocation").hide();
						$("#prosesSign").hide();
						$("#location").hide();
						$("#sgn-widget").hide();
						
						alertDanger("Dokumen Sudah Pernah ditandatangan",0);
					}
				}else{
					if(res.result == "06" || res.result == "07" || res.result == "08" || res.result == "09")
					{
						alertDanger(res.info, 0);
					}
					else
					{
						var email = null;
						if(!res.hasOwnProperty("signing") )
						{
							if(res.result  == '05' || res.result  == 'G1')
							{
								email = $("#inputUserNewCert").val();	
								usersign.usersign = $("#inputUserNewCert").val();
								
								$('#infoNotif').text(res.infoNotif);
								$('#signNotif').text(res.signNotif);
								
								dataUser.cert=res.result;
								
								var alertRenewal = document.getElementById("alertRenewal");
								var checkrenewal = document.getElementById("checkrenewal");
								if(alertRenewal)
								{
									document.getElementById('alertRenewal').style.display = 'none';
								}
								if(checkrenewal)
								{
									document.getElementById('checkrenewal').style.display = 'none';
								}
								
								dataUser.renewal = false;
								
								$('#certEmpty').modal('show');
							}
							else
							{
								dataUser.cert=res.result;
								email = $("#inputUser").val();
								
								if(res.result == '03' || res.result == '04')
								{
									$('#alertRenewalInfoBox').text(res.alertRenewalInfoBox);
									$('#alertRenewalInfo').text(res.alertRenewalInfo);
								}
								else
								{
									dataUser.renewal = false;
								}
								
								usersign.usersign = $("#inputUser").val();
								
								$("#alertKonfirmasi").modal('show');
								
							}
						}
						else
						{
							dataUser.cert=res.result;
							email = $("#inputUser").val();	
							if(res.result == '03' || res.result == '04')
							{
								$('#alertRenewalInfoBox').text(res.alertRenewalInfoBox);
								$('#alertRenewalInfo').text(res.alertRenewalInfo);
							}
							else
							{
								var alertRenewal = document.getElementById("alertRenewal");
								var checkrenewal = document.getElementById("checkrenewal");
								if(alertRenewal)
								{
									document.getElementById('alertRenewal').style.display = 'none';
								}
								
								if(checkrenewal)
								{
									document.getElementById('checkrenewal').style.display = 'none';
								}
								dataUser.renewal = false;
							}
							
							usersign.usersign = $("#inputUser").val();
							$("#alertKonfirmasi").modal('show');
						}
						dataUser.usersign=email;
					}
				}
			},
			error: function( jqXhr, textStatus, errorThrown ){
				alertDanger("Tidak dapat melanjutkan proses tandatangan",0);
			}
		});

	}
	else{
		alertDanger("Lokasi tandatangan belum lengkap. Silakan lengkapi terlebih dahulu",0);
	}
}
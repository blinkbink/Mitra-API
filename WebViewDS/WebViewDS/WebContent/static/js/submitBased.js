function showPDF2()
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

function prosesSignMitraBased(link){
	

	if(cekComplete()){
		$.ajax({
			url: link+'/prc_signMitra.html',
			//url: 'https://wvapi.digisign.id/prc_signMitra.html',
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
							window.parent.postMessage(res, '*');
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

function checkDocSign(link){
	
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
					}else{
						window.parent.postMessage(res, '*');
					}
				
				
				}else{
					$("#alertKonfirmasi").modal('show');
					
				}
			},
			error: function( jqXhr, textStatus, errorThrown ){
				$("#alertKonfirmasi").modal('hide');
				
				//alertDanger(res.notif,0);
			}
		});

	}
	else{
		alertDanger("Lokasi tandatangan belum lengkap. Silakan lengkapi terlebih dahulu",0);
	}
}
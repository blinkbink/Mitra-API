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

function prosesSignMitraV_session(link){
	
	if(cekComplete()){
		$.ajax({
			url: link+'/prc_signMitraV_session.html',
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
					$("page-loader").modal('hide');
					
					if(res.link!=null || res.link!=undefined ){
						alertSucessredirect(res.notif+" ",res.link,0);
//						window.location.href =  res.link;
					}else{
						alertSucess(res.notif,0);
					}
//					if(res.link!=null || res.link!=undefined ){
//						window.location.href =  res.link;
//					}
//					if(res.doc_link != null)
//						{
//							showPDF(res.doc_link);
//							$("#prosesSign").hide();
//							$("#cancelLocation").hide();
//							$("#location").hide();
//							$("#sgn-widget").hide();
//							$("#alertModal").modal('hide');
//							alertDanger("Dokumen Sudah Pernah ditandatangan",0);
//						}
//					else
//						{
//							parent.Return(JSON.stringify(res));
//						}
//					alertDanger(res.notif,0);///////////ALERT
//					alert(res.notif);
				}else if(res.result=="E1"){
					$("#alertKonfirmasi").modal('hide');
					$("page-loader").modal('hide');
					
					alertDanger(res.notif,0);
				}else{
					$("#alertKonfirmasi").modal('hide');
					$("page-loader").modal('hide');
					
					alertDanger(res.notif,0);
					

					if(res.result  == "408" || res.result  == "401")
					{
						$('#alertModal').on('hidden.bs.modal', function () {
							location.reload();
						});
					}
				}
			},
			error: function( jqXhr, textStatus, errorThrown ){
				$("#alertKonfirmasi").modal('hide');
				$("page-loader").modal('hide');
				
				alertDanger("gagal proses tandatangan",0);
			}
		});
		
	}
	else{
		alertDanger("Lokasi tandatangan belum lengkap. Silakan lengkapi terlebih dahulu",0);
	}
}
function signinBulkPdf(iddc,per,domain,id,crypted) {

    
    $("#otpModal").modal('hide');
    var dataString = {
            'frmProcess' : 'signPdf',
            'original' : '&doc='+iddc,
            'xxid': id,
            'crpww': crypted
            
    }
    
    
    $.ajax({
        type : "POST",
        url : domain +"/puc/BulkSignPdfProcess.html",
        data : dataString,
        cache : false,
        //async : false,
        success : function(result) {
            if (result) {
                
                st=st+per;
                if(st>99){
                    window.onbeforeunload = null;
                    $('.modal-header h3').html('Selesai.');
                    $("#done").css('visibility', 'visible');
                    
                }
                $("#probar").css("width", st+"%");
                
                var obj = jQuery.parseJSON(result);
                if (obj.status == "OK") {
                    sc++;
                    $("#sc").html(sc);
                } else {
                    fl++;
                    $("#fl").html(fl);
                    
                }
            }
        }
    });

    return false;

}

function callRe(apps,link){
    
	   var obj = new Object();
       obj.sukses = sc;
       obj.gagal  = fl;
       obj.total = dc.length;
       obj.refTrx = refTrx;
       var jsonString= JSON.stringify(obj);
 
       
       $.ajax({
			url: link+'/prc_backEndBulkMonitor.html',
			dataType: 'json',
			type: 'post',
			contentType: 'application/json',
			data: JSON.stringify(obj),
			processData: false,
			success: function( data, textStatus, jQxhr ){
				var res = data;
				console.log("Sukses >"+res.sukses+" + "+res.gagal+"< Gagal = "+res.total+" Total");

				try
			       {
				       	if(relink==0 || relink == '0')
				   		{
				       		if(sc!=dc.length){
				    			window.location.reload(); 
				    		}
				       		else
				    		{
				    				window.location= apps + "/finished.html";
				    		}
				   		}
				       	else
				   		{
				       		if(sc!=dc.length){
				    			window.location.reload(); 
				    		}
				       		else
				    		{
				       			window.location = relink;
				    		}
				       	}
			       	}
			       	catch(err)
			       	{
			       		window.location= apps + "/finished.html";
			       	}
			},
			error: function( jqXhr, textStatus, errorThrown ){
				
				try
			       {
				       	if(relink==0 || relink == '0')
				   		{
				       		if(sc!=dc.length){
				    			window.location.reload(); 
				    		}
				       		else
				    		{
				    				window.location= apps + "/finished.html";
				    		}
				   		}
				       	else
				   		{
				       		if(sc!=dc.length){
				    			window.location.reload(); 
				    		}
				       		else
				    		{
				       			window.location = relink;
				    		}
				       	}
			       	}
			       	catch(err)
			       	{
			       		window.location= apps + "/finished.html";
			       	}
			       	
			}
			
		});    
       
       

}

function prosesCekBulkMitraV_session(link,domain, cert){
	
		dataUser.refTrx=refTrx;
		dataUser.doc = dc;
		
		$.ajax({
			url: link+'/prc_bulkMitraV_session.html',
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
					$("#proModal").modal('show');
                    $('.modal-header h3').html('Processing...')
					var per = 100/dc.length;
                    for (var i=0; i < dc.length; i++) {
                        signinBulkPdf(dc[i],per,domain,useruser.tstmp,dataUser.userpwd);
                        if(dc.length - 1 === i) {
                            break;
                        }
                    }
					
				}else if(res.result=="E1"){
					$("#alertKonfirmasi").modal('hide');
					
					alertDanger(res.notif,0);
				}else{
					$("#alertKonfirmasi").modal('hide');
					alertDanger(res.notif,0);
					
					if(res.result == "408" || res.result == "401")
					{
						$('#alertModal').on('hidden.bs.modal', function () {
							location.reload();
						});
					}
				}
			},
			error: function( jqXhr, textStatus, errorThrown ){
				$("#alertKonfirmasi").modal('hide');
				
				alertDanger(res.notif,0);
			}
		});
		
//		
	
}
function prosesViewMitraV_session(link){
	
	if(cekComplete()){
		$.ajax({
			url: link+'/prc_viewMitraV_session.html',
			//url: 'https://wvapi.digisign.id/prc_signMitraV.html',
			dataType: 'json',
			type: 'post',
			contentType: 'application/json',
			data: JSON.stringify(dataUser),
			processData: false,
			success: function( data, textStatus, jQxhr ){
				
				var res = data;
				$("#alertKonfirmasi").modal('hide');
				$("#loadingPage").modal('hide');
				$("#alertModal").modal('hide');
				
				if(res.result=="00"){
					$("#alertKonfirmasi").modal('hide');
					$("#alertModal").modal('hide');
					$("#loadingPage").modal('hide');
					if(res.link!=null || res.link!=undefined ){
						alertSucessredirect(res.notif+" ",res.link,0);
//						window.location.href =  res.link;
					}else{
						alertSucess(res.notif,0);
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

function onTimer() 
{
	var minute = 1;
	var sec = 59;
	const interval = setInterval(function() 
	{
		document.getElementById("prosesSign").disabled = true;
	    document.getElementById("prosesSign").setAttribute('title', "Menunggu proses pendaftaran ulang selesai. Belum menerima email ? Kirim ulang dalam ("+minute + " : " + sec+")");
	    $('[data-toggle="tooltip"]').tooltip('show');
        sec--;
	    if (sec == -1) 
	    {
	    	
	        minute --;
	        sec = 59;
	        if (minute == -1) 
	        {
	        	document.getElementById("prosesSign").disabled = false;
	        	document.getElementById("prosesSign").innerHTML = "Proses";
	        	clearInterval(interval);
	        	$('[data-toggle="tooltip"]').tooltip('hide');
	        }
	    }
	  }, 1000);
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
					if(res.result == "06" || res.result == "07" || res.result == "08" || res.result == "09" || res.result  == "408" || res.result  == "401")
					{
						alertDanger(res.info, 0);
						
						if(res.result  == "408" || res.result  == "401")
						{
							$('#alertModal').on('hidden.bs.modal', function () {
								location.reload();
							});
						}
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
							
								dataUser.cert = res.result;
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
								
								dataUser.cert = res.result;
								email = $("#inputUser").val();
								usersign.usersign = $("#inputUser").val();
								if(res.result == '03' || res.result == '04')
								{
									$('#alertRenewalInfoBox').text(res.alertRenewalInfoBox);
									$('#alertRenewalInfo').text(res.alertRenewalInfo);
								}
								else
								{
									dataUser.renewal = false;
								}
								$("#alertKonfirmasi").modal('show');
							}
						}
						else
						{
							dataUser.cert = res.result;
							
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

function checkDocSignBulk(link, cert){

		$.ajax({
			url: link+'/prc_checkusersign.html',
			dataType: 'json',
			type: 'post',
			contentType: 'application/json',
			data: JSON.stringify(dataUser),
			processData: false,
			success: function( data, textStatus, jQxhr ){
			
			var res = data;
			if(res.result == "06" || res.result == "07" || res.result == "08" || res.result == "09" || res.result  == "408" || res.result  == "401")
			{
				alertDanger(res.info, 0);
				
				if(res.result  == "408" || res.result  == "401")
				{
					$('#alertModal').on('hidden.bs.modal', function () {
						location.reload();
					});
				}
			}
			else
			{
				if(res.result == "05" || res.result == "G1")
				{
					
					$('#infoNotif').text(res.infoNotif);
					$('#signNotif').text(res.signNotif);
					
					dataUser.cert = res.result;
					$('#certEmpty').modal('show');
					
					document.getElementById('alertRenewal').style.display = 'none';
					document.getElementById('checkrenewal').style.display = 'none';
					dataUser.renewal = false;
				}
				else
				{
					dataUser.cert = res.result;
					if(res.result == '03' || res.result == '04')
					{
						$('#alertRenewalInfoBox').text(res.alertRenewalInfoBox);
						$('#alertRenewalInfo').text(res.alertRenewalInfo);
					}
					else
					{
						dataUser.renewal = false;
					}
					$("#alertKonfirmasi").modal('show');
				}
			}
				
			},
			error: function( jqXhr, textStatus, errorThrown ){
//				
				alertDanger("Tidak dapat melanjutkan proses tandatangan",0);
			}
		});
		
		
}
function prosesaktivasi(link)
{         
	console.log("hit");
        $.ajax({
            type: "POST",
            url: link+"/AktivasiMitra.html",            
            data: {
                email: $("#email").val(),
                usernamer: $("#username").val(),
                password: $("#password").val(),
                sgn: $("#sgn").val()
            },
            processData: true,
            contentType: "application/x-www-form-urlencoded",
            success: function(data, status, jqXHR){
               //alert("success..."+data);
               //console.log("Sukses");
            },
            error: function(xhr){
               //console.log("error" + xhr.responseText);
               alert("error"+xhr.responseText);
            }
       });      
} 

function prosesaktivasi_session(link)
{         
	console.log("hit");
        $.ajax({
            type: "POST",
            url: link+"/AktivasiMitra_session.html",            
            data: {
                email: $("#email").val(),
                usernamer: $("#username").val(),
                password: $("#password").val(),
                sgn: $("#sgn").val()
            },
            processData: true,
            contentType: "application/x-www-form-urlencoded",
            success: function(data, status, jqXHR){
            	res = data;
            	jo = data;
            	alert("LINK RES SB :"+res.link);
            	alert("LINK JO SB :"+jo.link);
            	alert("LINK DATA SB :"+jo.link);

            	alert("DATA SB :"+data);
            	window.location.href =  jo.link;
            	//ow.location.href =  data.link;
            	console.log(data);
               //alert("success..."+data);
               //console.log("Sukses");
            },
            error: function(xhr){
               //console.log("error" + xhr.responseText);
               alert("error"+xhr.responseText);
            }
       });      
} 

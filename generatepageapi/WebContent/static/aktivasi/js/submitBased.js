function prosesaktivasi()
{         
	console.log("hit");
        $.ajax({
            type: "POST",
            url: "https://wvapi.tandatanganku.com/AktivasiMitra.html",            
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

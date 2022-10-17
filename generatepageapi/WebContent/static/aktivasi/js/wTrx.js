$( document ).ready(function() {
    function readKTP(input) {
        if (input.files && input.files[0]) {
            var reader = new FileReader();
            
            reader.onload = function (e) {
                $('#loadektp').attr('src', e.target.result);
            }
            
            reader.readAsDataURL(input.files[0]);
        }
    }
    
    $("#imgektp").change(function(){
        readKTP(this);
    });
    

    
    function readNPWP(input) {
        if (input.files && input.files[0]) {
            var reader = new FileReader();
            
            reader.onload = function (e) {
                $('#loadnpwp').attr('src', e.target.result);
            }
            
            reader.readAsDataURL(input.files[0]);
        }
    }
    
    $("#imgnpwp").change(function(){
    	readNPWP(this);
    });
    
    function readselfie(input) {
        if (input.files && input.files[0]) {
            var reader = new FileReader();
            
            reader.onload = function (e) {
                $('#loadselfie').attr('src', e.target.result);
            }
            
            reader.readAsDataURL(input.files[0]);
        }
    }
    
    $("#imgselfie").change(function(){
    	readselfie(this);
    });
    
    
	
	var propinsi;
	
	
	$("#kota").select2({
	    placeholder: "--Pilih--"
	});


	
	$("#kecamatan").select2({
	    placeholder: "--Pilih--"
	});
	$("#kelurahan").select2({
	    placeholder: "--Pilih--"
	});
	
	$("#propinsi").select2({
			placeholder: "--Pilih--"
	},
			$.ajax({
		        type: "POST",
				url: "/process/frmprc.html?frmProcess=getPropinsi",
		        data: "{}",
		        success: function (msg) {
		            if(msg){
		            	var obj = jQuery.parseJSON(msg);
						if(obj.status=="ok"){
							var res="<option> --Pilih-- </option>";
							var list = obj.props;
							for (var i=0; i<list.length; i++) {
								 res+="<option value='"+list[i].data+"' data-id='"+list[i].value+"'>"+list[i].data+"</option>";
								 
							}
							document.getElementById("propinsi").innerHTML=res;
						}
					
					}
		        }
		    })
	);

	$("#propinsi").change(function() {
		$("#kota").select2("val", "");
		var res="";
		var prop=$('#propinsi option:selected').data('id');
		var dataString = {
				'frmProcess' : 'getRegenci',
				'prop' : prop
				};
		$.ajax({
	        type: "POST",
			url: "/process/frmprc.html",
			data: dataString,
	        success: function (msg) {
	            if(msg){
	            	var obj = jQuery.parseJSON(msg);
					if(obj.status=="ok"){
						var res="<option> --Pilih-- </option>";
						var list = obj.regencies;
						for (var i=0; i<list.length; i++) {
							 res+="<option value='"+list[i].data+"' data-id='"+list[i].value+"'>"+list[i].data+"</option>";
							 
						}
						document.getElementById("kota").innerHTML=res;
					}
				
				}
	        }
	    })
	})
	
	$("#kota").change(function() {
		$("#kecamatan").select2("val", "");
		
		var reg=$('#kota option:selected').data('id');
		var dataString = {
				'frmProcess' : 'getDistrict',
				'reg' : reg
				};
		$.ajax({
	        type: "POST",
			url: "/process/frmprc.html",
			data: dataString,
	        success: function (msg) {
	            if(msg){
	            	var obj = jQuery.parseJSON(msg);
					if(obj.status=="ok"){
						var res="<option> --Pilih-- </option>";
						var list = obj.districts;
						for (var i=0; i<list.length; i++) {
							 res+="<option value='"+list[i].data+"' data-id='"+list[i].value+"'>"+list[i].data+"</option>";
							 
						}
						document.getElementById("kecamatan").innerHTML=res;
					}
				
				}
	        }
	    })
	}) 
	
	$("#kecamatan").change(function() {
		$("#kelurahan").select2("val", "");
		
		var dis=$('#kecamatan option:selected').data('id');
		var dataString = {
				'frmProcess' : 'getVillage',
				'dis' : dis
				};
		$.ajax({
	        type: "POST",
			url: "/process/frmprc.html",
			data: dataString,
	        success: function (msg) {
	            if(msg){
	            	var obj = jQuery.parseJSON(msg);
					if(obj.status=="ok"){
						var res="<option> --Pilih-- </option>";
						var list = obj.villages;
						for (var i=0; i<list.length; i++) {
							 res+="<option value='"+list[i].data+"' data-id='"+list[i].value+"'>"+list[i].data+"</option>";
							 
						}
						document.getElementById("kelurahan").innerHTML=res;
					}
				
				}
	        }
	    })
	}) 
	
	$("#kelurahan").change(function() {
		document.getElementById("kodepos").value="";
		var res="";
		var dis=$('#kelurahan option:selected').data('id');
		var dataString = {
				'frmProcess' : 'getKodepos',
				'dis' : dis
				};
		$.ajax({
	        type: "POST",
			url: "/process/frmprc.html",
			data: dataString,
	        success: function (msg) {
	            if(msg){
	            	var obj = jQuery.parseJSON(msg);
					if(obj.status=="ok"){
						document.getElementById("kodepos").value=obj.kodepos;
					}
				
				}
	        }
	    })
	}) 
	
	
   
	
	});


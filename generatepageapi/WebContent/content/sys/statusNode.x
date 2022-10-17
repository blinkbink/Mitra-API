#set ($images = "$request.contextPath/images")

#if ($errors)
   <ul>
   #foreach ($error in $errors)
     <li class="red">$error</li>
   #end
   </ul>
#end

#foreach($s in $status)
 #if ($s.status.expired) 
  #set ($exp = "expired") 
 #else 
  #set ($exp = "not expired") 
 #end
  <li class="dhtmlgoodies_sheet.gif">
   <a href="statusshow.html?id=$s.status.id">
    <img border="0" src="${images}/tree_${s.status.iconName}"></img>
    $s.status.id - ${exp} - ${s.status.elapsedAsString}
   </a>
  </li>
#end

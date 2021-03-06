function go_search_order(kind){
	if(document.frm.key.value=="")
		return;
		
	var url = "adminOrderList.do?page=1&kind=" + kind;
	// 보던 페이지가 어떤 페이지이던간에 검색 결과의 1페이지로 가기위해 파라미터 page를 1로 전송
	document.frm.action = url;
	document.frm.submit();
}

function go_total_order(kind){
	document.frm.key.value="";
	document.frm.action = "adminOrderList.do?page=1&kind=" + kind;
	document.frm.submit();
}

function go_order_save(kind){
	var count = 0;
	if(document.frm.result.length == undefined){
		if(document.frm.result.checked == true)
			count++;
	}else{
		for(var i = 0; i < document.frm.result.length; i++){
			if(document.frm.result[i].checked == true)
				count++;
		}
	}
	
	if(count == 0){
		alert("주문처리할 항목을 선택해 주세요.")
	}else{
		document.frm.action = "adminOrderSave.do?kind="+kind;
		document.frm.submit();
	}
}
function del_order(kind){
	var count = 0;  //  체크된 체크박스의 갯수를 카운트 하기위한 변수
	if(document.frm.delete.length==undefined){   // 장바구니에 물건이 하나일때, 체크박스가 하나일때
		if( document.frm.delete.checked == true)   // 그 체크박스만 체크되어 있는지 확인
			count++;	 
	}else{
		for( var i=0; i<document.frm.delete.length; i++){
			if( document.frm.delete[i].checked==true)
				count++;
		}
	}
	// 지금의 스크립트 명령은 체크박스가 하나도 체크되지 않았다면 원래로 되돌아 가기위한 코드들입니다
	if( count == 0 ){
		alert("삭제할 항목을 선택해주세요");
	} else{
		document.frm.action = "adminOrderDelete.do?kind="+kind;
	    document.frm.submit();
	}
}

function memberKindChange(kind){
	if(kind=="1"){
		document.frm.action = "adminOrderList.do?kind="+2;
		document.frm.submit();
	}else if(kind=="2"){
		document.frm.action = "adminOrderList.do?kind="+1;
		document.frm.submit();
	}
}

function deleteSpo(sposeq, result, kind, odseq){
	if(result == 3 || result == 4){
		alert("주문이 배달 중이거나 배달완료해 취소처리 할 수 없습니다.");
	}else{
		document.location.href = "adminOrderMDelete.do?sposeq=" + sposeq + "&kind=" + kind
		 + "&odseq=" + odseq;
	}
}

function go_order_mov(){
	document.location.href = "adminOrderList.do?kind="+1;
}

function resultAllCheck(resultAllCheck){
	const checkboxes = document.getElementsByName('result');
  	checkboxes.forEach((checkbox) => {
    	checkbox.checked = resultAllCheck.checked;
	})
}

function deleteAllCheck(deleteAllCheck){
	const checkboxes = document.getElementsByName('delete');
  	checkboxes.forEach((checkbox) => {
    	checkbox.checked = deleteAllCheck.checked;
	})
}
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="../include/header.jsp"%>

<article style="background-color: #f2ebe6;">
	
	<div class="wrap">
		<ul id="terms_wrap_ul">
			<li><span><a href="Mterms.do">이용약관</a></span></li>
			<li><span><a href="Mprivacy.do" style="color: red; text-decoration: underline;">개인정보취급방침</a></span></li>
			<li><span><a href="Mlegal.do">법적고지</a></span></li>
		</ul>
		<h1 class="sbig_h1">이용약관 및 정책</h1>
		
		<div class="btnarea">
			<div class="newbtn">
				<input class="btn_c1" type="button" style="background-color: #292929;" 
				onclick="location.href='Mprivacy.do'" value="개인정보처리방침">
			</div>
			<div class="newbtn">
				<input class="btn_c1" type="button" onclick="location.href='MvideoPolicy.do'" value="영상정보처리기기운영관리방침">
			</div>
			 
		</div>
		
		<div class="textarea">
			<%@ include file="text2.jsp" %>
		</div>
	</div>
	
</article>
<div class="clear"></div>

<%@ include file="../include/footer.jsp" %>
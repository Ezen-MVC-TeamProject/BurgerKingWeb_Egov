<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="../include/Delivery/deli_header.jsp"%>

<article style="background-color: #f2ebe6;">
	<div class="contentsBox01">
	<div class="web_container03">
		<h1 class="tit01">아이디/비밀번호 찾기</h1>
		<ul id="terms_wrap_ul" style="left: 0px; position:relative;">
			<li>
				<span>
					<a href="MfindIdForm.do" style="color: red; text-decoration: underline; font-size: 1.45em;">아이디 찾기</a>
				</span>
			</li>
			<li><span><a href="MfindPwdForm.do?id=${memberVO.ID}&name=${memberVO.NAME}" style="font-size: 1.45em;">비밀번호 찾기</a></span></li>
		</ul>
		<div class="btnarea" style="height:0px; margin-top: 80px;"></div>
		<div class="textarea" style="height: 450px; width: 90%;">
			<div class="msgBox">
				<span class="showid_text">아이디 찾기 완료</span><br>
				<span class="showid_text"><small><b>회원님</b>의 아이디는 다음과 같습니다.</small></span>
			</div>
			<div class="inpbox">
				<c:choose>
					<c:when test="${empty memberVO}">
						
					</c:when>
					<c:otherwise>
						<div id="findid_loadtext">${memberVO.ID}으로 회원가입하셨습니다.</div>
					</c:otherwise>
				</c:choose>
			</div>
		</div>
		<div class="c_btn item2 findidalign" style="height: 150px;">
			<input type="button" class="btn01 m" value="비밀번호 찾기" onclick="location.href='MfindPwdForm.do?id=${memberVO.ID}&name=${memberVO.NAME}'"/>
			<input type="button" class="btn01 m red" value="로그인" onclick="location.href='MloginForm.do'"/>
		</div>
	</div>
	</div>
</article>

<%@ include file="../include/footer.jsp" %>
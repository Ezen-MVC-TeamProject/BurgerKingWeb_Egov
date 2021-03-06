<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<title>딜리버리</title>
		<link href="<c:url value='css/mobile.css'/>" rel="stylesheet">
		<script src="http://code.jquery.com/jquery-latest.js"></script> 
		<script src="<c:url value='script/mburger.js'/>" type="text/javascript"></script>
		<link rel="icon" href="<c:url value='image/main/favicon.ico'/>">
		<link rel="preconnect" href="https://fonts.googleapis.com">
		<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
		<link href="https://fonts.googleapis.com/css2?family=Do+Hyeon&family=Nanum+Myeongjo:wght@800&family=Noto+Sans+KR&display=swap" rel="stylesheet">
	</head> 
	<body>
	<header id=h_header> 
		<div id="header_container" style="background-color: #e22219;">
			<div class="web_container">
			 
				<c:choose>
					<c:when test="${empty loginUser}">
						<h1 class="WEB_logo" onclick="location.href='mobilemain.do'">
							<span>버거킹</span> 
						</h1> 
					</c:when>
					<c:otherwise> 
						<h1 class="WEB_logo" onclick="location.href='MdeliveryForm.do?kind1=1'">
							<span>딜리버리</span>
						</h1>
					</c:otherwise>
				</c:choose>
				
				<c:choose>
					<c:when test="${empty loginUser}">
						<div class="WEB utilWrap">
							<a style="display: none;"><span>로그아웃</span></a>
							<a href="MloginForm.do"><span>로그인</span></a>
							<a href="MfaqListForm.do?fnum=1"><span>고객센터</span></a>
							<a style="display: none;"><span>MY킹</span></a>
							<a href="mobilemain.do"><span>브랜드홈</span></a>
						</div> 
					</c:when>
					<c:when test="${memberkind == 2}">
						<div class="WEB utilWrap">
							<a href="mobilemain.do"><span>브랜드홈</span></a>
							<a href="MdeliveryOrderList.do"><span>주문 내역 확인</span></a>
							<a href="Mlogout.do"><span>비회원 로그아웃</span></a>
						</div>
						<div class="WEB user">
							<p><span>${loginUser.NAME}</span>님 안녕하세요</p>
						</div>
					</c:when>
					<c:otherwise>
						<div class="WEB utilWrap">
							<a href="Mlogout.do"><span>로그아웃</span></a>
							<a style="display: none;"><span>로그인</span></a>
							<a href="MfaqListForm.do?fnum=1"><span>고객센터</span></a>
							<a href="MdeliveryMypageForm.do"><span>MY킹</span></a>
							<a href="mobilemain.do"><span>브랜드홈</span></a>
						</div>
						<div class="WEB user">
							<p><span>${loginUser.NAME}</span>님 안녕하세요</p>
						</div>
					</c:otherwise>
				</c:choose>
			</div>
		</div>
		</header>
		<c:if test="${!empty loginUser && memberkind == 1}">
			<%@ include file="../../include/Delivery/deli_orderCart.jsp"%>		
		</c:if>

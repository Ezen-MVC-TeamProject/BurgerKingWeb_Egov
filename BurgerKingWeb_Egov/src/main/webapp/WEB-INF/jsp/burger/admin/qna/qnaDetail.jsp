<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="../../admin/header.jsp" %>
<%@ include file="../../admin/sub_menu.jsp"%>

<article>
	<h1>Q&amp;A 게시판</h1>
	<form name="frm" method="post">
		<input type="hidden" name="qseq" value="${qnaVO.QSEQ}">
		<table id="orderList">
			<tr>
				<th width="20%">제목</th>
				<td align="left">${qnaVO.SUBJECT} ${qnaVO.REP}</td>
				<th>비밀번호</th>
				<td align="left">${qnaVO.PASS}</td>
			</tr>
			<tr>
				<th>등록일</th>
				<td align="left"><fmt:formatDate value="${qnaVO.INDATE}"/></td>
				<th>작성자</th>
				<td align="left">${qnaVO.ID}</td>
			</tr>
			<tr>
				<th>내용</th>
				<td align="left">${qnaVO.CONTENT}</td>
			</tr>
		</table>
		<!-- 관리자가 쓴 답글 또는 답글 쓰는 입력란 표시 -->
		<c:choose>
			<c:when test='${qnaVO.REP=="1"}'>
				<table id="orderList">
					<tr>
						<td colspan="2"><img src="<c:url value='admin/images/opinionimg01.gif'/>"></td>
					</tr>
					<tr>
						<td colspan="2">
							<textarea name="reply" rows="3" cols="50"></textarea><br><br>
							<input type="button" class="btn" value="저장" onclick="go_qna_rep();">
						</td>
					</tr>
				</table>
			</c:when>
			<c:otherwise>
				<table id="orderList">
					<tr>
						<th>댓글</th>
						<td>${qnaVO.REPLY}</td>
					</tr>
				</table>
			</c:otherwise>
		</c:choose>
		<input type="button" class="btn" value="목록" onclick="location.href='adminQnaList.do'">
	</form>
</article>

<%@ include file="../../admin/footer.jsp"%>
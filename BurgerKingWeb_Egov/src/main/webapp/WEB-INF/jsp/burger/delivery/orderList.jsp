<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="../include/Delivery/deli_header.jsp"%>
<div class="clear"></div>
<form name="order" method="post" action="burger.do" style="background: #f2ebe6;">
<input type="hidden" name="order" value="">
<article>

</article>
 
<article>
<div class="location">
<div class="web_container1">
	<ul>
		<li><a href="deliveryForm.do?kind1=1">딜리버리</a>&nbsp;>&nbsp;</li>
		<li>주문내역</li>
	</ul>
</div>
</div>
<div class="contentsBox01">
	<div class="web_container1">
		<div class="subtitWrap m_bg_basic">
			<h2 class="page_tit">주문내역</h2>
			<h2 style="color:red; font-weight: bold;">${message}</h2>	
		</div>
		<c:choose>
		<c:when test="${empty ovo}">
			<div class="tab_cont">
				<div class="nodata"><p>주문내역이 없습니다.</p></div>
			</div>
		</c:when>
		<c:otherwise>
					<div class="container01 orderWrap">
			<h2 class="tit01 tit_ico delivery"><span>배달정보</span></h2>
		</div>
		<div class="container02 deli_info01">
			<div class="addrWrap01">
				<p class="txt_addr">
					<c:if test="${!empty mkind}">
						<div style="color: red;">화면의 주문번호는 비회원 주문내역 확인에 필요합니다.(주문번호-주문세부번호)</div>
					</c:if>
					<span>${Myaddress.ADDRESS}</span>	
				</p>
				<!-- <button type="button" class="btn04 h02 rbtn"><span>변경</span></button> -->
			</div>
			<div class="info_list">
				<dl><dt>연락처</dt><dd>${userPhone}</dd></dl>
			</div>
		</div>
		<div class="tit01 tit_ico burger tit_ordermenu">
			<h2><span>주문정보</span></h2>
		</div>
		<div class="container02 order_accWrap open">		
		<ul class="cart_list01">
			<c:forEach var="orderList"  items="${ovo}">
			<li>
				<div class="cont">
					<div class="menu_titWrap">
						<div class="menu_name">
							<p class="tit"><strong><span>${orderList.OSEQ}-${orderList.ODSEQ} : ${orderList.PNAME}</span></strong></p>
							<p class="price">
								<strong><span>
									<c:if test="${orderList.RESULT == 1}">
										주문 처리 전
									</c:if>
									<c:if test="${orderList.RESULT == 2}">
										주문 처리 중
									</c:if>
									<c:if test="${orderList.RESULT == 3}">
										배달 중
									</c:if>
								</span></strong>
							</p>
						</div>
					</div>
					<div class="quantity"><strong class="tit">수량</strong>
						<div class="num_set">
							<div class="result">${orderList.QUANTITY}</div>
						</div>
					</div>
					<button type="button" name="submit" class="btn_del02" onclick="go_order_delete('${orderList.ODSEQ}')"><span>Delete menu</span></button>
				</div>
				<div class="sumWrap">
				<dl>
					<dt>상품금액</dt>
					<dd>
						<strong>
							<em><span></span><span class="unit">${orderList.PRICE1}원</span></em>
						</strong>
					</dd>
				</dl>
				</div><br><br><br>
			</li>
			<c:choose>
				<c:when test="${empty spseqAm}">
					<hr><br>
				</c:when>
				<c:otherwise>
					<ul class="cart_list01">
					<li>
					<div class="cont" style="padding: 32px 64px; font-size:2rem;font-weight: bold;">
						<c:forEach items="${spseqAm}" var="spseqAm">
							<c:if test="${spseqAm.ODSEQ == orderList.ODSEQ}">
								<div style="width:100%;">
									${spseqAm.ODSEQ} : ${spseqAm.SNAME}
									<div style="color:red; float:right;">${spseqAm.ADDPRICE}원</div>
								</div>
							</c:if>
						</c:forEach>
					</div>
					</li>
					</ul>
				</c:otherwise>
			</c:choose>
			<hr><br>
			</c:forEach>
		</ul>
		</div>
		<h2 class="tit01 tit_ico money"><span>최종 결제금액</span></h2>
		<div class="container02">
			<div class="order_payment_list">
				<dl class="tot">
					<dt>총 주문금액</dt>
					<dd>
						<strong>
							<em><span></span><span class="unit">${totalPrice}원</span></em>
						</strong>
					</dd>
				</dl>
				<dl>
					<dt>제품 금액</dt>
					<dd>
						<strong>
							<em><span></span><span class="unit">${totalPrice}원</span></em>
						</strong>
					</dd>
				</dl>
				<dl>
					<dt>배달팁</dt>
					<dd>
						<strong>
							<em><span></span><span class="unit">0원</span></em>
						</strong>
					</dd>
				</dl>
			</div>
		</div>
		<div class="totamountWrap">
			<div class="c_btn m_item2">
				<c:if test="${!empty loginUser}">
					<button type="button" class="btn01 m red" onclick="location.href='deliveryForm.do?kind1=1'">
					<span>추가주문하기</span></button>
				</c:if>
			</div>
		</div>
		</c:otherwise>
		</c:choose>
	</div>
</div>
</article>
</form>
<div class="clear"></div>
<%@ include file="../include/footer.jsp" %>
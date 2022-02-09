package com.ezen.burger.web;

import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.ezen.burger.service.MemberService;
import com.ezen.burger.service.QnaService;

@Controller
public class QnaController {
	@Resource(name="QnaService")
	QnaService qs;
	
	@Resource(name="MemberService")
	MemberService ms;
	
	
	
	// 고객센터 문의
			@RequestMapping(value="/qnaForm.do")
			public String qna_list(Model model, HttpServletRequest request) {
				HttpSession session = request.getSession();
				HashMap<String, Object> loginUser = (HashMap<String, Object>)session.getAttribute("loginUser");
				if( loginUser == null ) {
						return "ServiceCenter/qnaList";
					}else {
						HashMap<String, Object> paramMap = new HashMap<String, Object>();
						paramMap.put("id", loginUser.get("ID").toString() );
						paramMap.put("ref_curser", null);
						if(session.getAttribute("memberkind") != null) {
							int memberKind = Integer.parseInt(session.getAttribute("memberkind").toString());
							/* int memberKind = (int)session.getAttribute("memberkind"); */
							// 회원 종류 검사 (1:회원, 2:비회원)
							if(memberKind == 1) {	// 회원
									qs.listQna( paramMap );
									ArrayList<HashMap<String, Object>> list 
									= (ArrayList<HashMap<String, Object>>)paramMap.get("ref_cursor");
									model.addAttribute("qnaList", list);	
									return "ServiceCenter/qnaList";
							}else if(memberKind == 2 ) {
									model.addAttribute("message", "Qna문의를 하려면 로그인을 하셔야합니다.");
									return "member/loginForm";	
								}
						}
				}
				return " ServiceCenter/qnaList";
			}
	
	
		// 고객센터 문의작성
		@RequestMapping(value="qnaWriteForm.do")
		public String qnaWriteForm(Model model, HttpServletRequest request) {
			return "ServiceCenter/qnaWrite";
		}
		
		
		// 고객센터 문의내용전송
		@RequestMapping(value="/qnaWrite.do")
		public String qnaWrite(Model model, HttpServletRequest request,
				@RequestParam("subject") String subject, @RequestParam("content") String content) {
			HttpSession session = request.getSession();
			HashMap<String, Object> loginUser = (HashMap<String, Object>)session.getAttribute("loginUser");
			if( loginUser == null ) {
					return "ServiceCenter/qnaList";
				}else {
					HashMap<String, Object> paramMap = new HashMap<String, Object>();
					paramMap.put("id", loginUser.get("ID").toString() );
					paramMap.put("ref_curser", null);
					if(session.getAttribute("memberkind") != null) {
						int memberKind = Integer.parseInt(session.getAttribute("memberkind").toString());
						/* int memberKind = (int)session.getAttribute("memberkind"); */
						// 회원 종류 검사 (1:회원, 2:비회원)
						if(memberKind == 1) {	// 회원
							paramMap.put("id", loginUser.get("ID").toString() );
							paramMap.put("subject", subject );
							paramMap.put("content", content);
							qs.b_insertQna( paramMap );	
								return "redirect:/ServiceCenter/qnaList";
						}else if(memberKind == 2 ) {
								model.addAttribute("message", "Qna문의를 하려면 로그인을 하셔야합니다.");
								return "member/loginForm";	
							}
					}
			}
			return " ServiceCenter/qnaList";
		}
		
		
		
		/*
		
		// 고객센터 문의내용전송
		@RequestMapping(value="qnaWrite" , method=RequestMethod.POST)
		public ModelAndView qna_write( @ModelAttribute("dto") @Valid QnaVO qnavo,
				BindingResult result, Model model, HttpServletRequest request){
			ModelAndView mav = new ModelAndView();
			if(result.getFieldError("subject") !=null) {	// 제목 미입력
				mav.addObject("message", "제목을 입력하세요"); 
				mav.setViewName("ServiceCenter/qnaWrite");
				return mav;
			}else if(result.getFieldError("content") !=null) {	// 내용 미입력
				mav.addObject("message", "내용을 입력하세요");
				mav.setViewName("ServiceCenter/qnaWrite");
				return mav;
			}else if(result.getFieldError("pass") !=null) {	// 비밀번호 미입력
				mav.addObject("message", "비밀번호를 입력하세요");
				mav.setViewName("ServiceCenter/qnaWrite");
				return mav;
			}  
			HttpSession session = request.getSession();
			MemberVO mvo = (MemberVO) session.getAttribute("loginUser");
			  if (mvo == null) mav.setViewName("member/loginform");	// 비로그인상태
			    else {
			    	qnavo.setId(mvo.getId());
			    	qs.insertQna(qnavo);
			    }   
			    mav.setViewName("redirect:/qnaForm");
				return mav;
			}
		
		
		// 고객센터 qna passform
		@RequestMapping(value="/passCheckForm")
		 public ModelAndView passCheckForm(@RequestParam("qseq")int qseq,
				 @RequestParam(value="message", required = false)String message,
				 HttpServletRequest request) {
			ModelAndView mav=new ModelAndView();
			HttpSession session = request.getSession();
			
			if(session.getAttribute("loginUser") == null) {	// 비로그인 상태
				mav.setViewName("redirect:/loginForm");
			}else {
				if(message != null) {
					mav.addObject("message", message);	// qna 게시물의 pass가 틀린경우 passChk의 message를 출력 
				}
				mav.addObject("qseq", qseq);
			    mav.setViewName("ServiceCenter/passChk");
			}
		    return mav;
		 }
		

		// 고객센터 qna pass검사
			@RequestMapping(value="/passChk", method=RequestMethod.POST)
			public ModelAndView passChk (HttpServletRequest request , Model model) {
				ModelAndView mav = new ModelAndView();
				HttpSession session = request.getSession();
				if(session.getAttribute("loginUser") == null) {	// 비로그인 상태
					mav.setViewName("redirect:/loginForm");
				}else {
					String pass = request.getParameter("pass");
					int qseq = Integer.parseInt(request.getParameter("qseq"));
					QnaVO qvo = qs.getpassChk(qseq); 
					if(!qvo.getPass().equals(pass)) {	// 비밀번호 불일치
						mav.addObject("message", "비밀번호가 일치하지 않습니다"); 
						mav.setViewName("redirect:/passCheckForm?qseq=" + qseq);
					}else {	// 비밀번호 일치
						mav.addObject("qseq", qseq);
						mav.setViewName("redirect:/qnaView");
					}
				}
				return mav;
			}
			
		
		// 고객센터 문의내용
		@RequestMapping(value="/qnaView")
		public ModelAndView qna_view(Model model, HttpServletRequest request) {
			ModelAndView mav = new ModelAndView();
			int qseq = Integer.parseInt(request.getParameter("qseq"));
			mav.addObject("qnaVO", qs.getQna(qseq));	//qseq로 해당게시물 보기
			mav.setViewName("ServiceCenter/qnaView");
			return mav;
		}
		 
		
		// 고객센터 qna 삭제
		@RequestMapping(value="qnaDelete" )
		public String qnaDelete( @RequestParam("delete") int [] qseqArr ) {
			for( int qseq : qseqArr)
				qs.deleteQna(qseq);
			return "redirect:/qnaForm";
		}*/
	
}

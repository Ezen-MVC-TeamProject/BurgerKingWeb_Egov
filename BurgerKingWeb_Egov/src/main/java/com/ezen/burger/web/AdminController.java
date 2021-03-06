package com.ezen.burger.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.ezen.burger.dto.Paging;
import com.ezen.burger.service.AdminService;
import com.ezen.burger.service.EventService;
import com.ezen.burger.service.MemberService;
import com.ezen.burger.service.OrderService;
import com.ezen.burger.service.ProductService;
import com.ezen.burger.service.QnaService;
import com.oreilly.servlet.MultipartRequest;
import com.oreilly.servlet.multipart.DefaultFileRenamePolicy;



@Controller
public class AdminController {
	@Resource(name="AdminService")
	AdminService as;

	@Resource(name="MemberService")
	MemberService ms;

	@Resource(name="QnaService")
	QnaService qs;

	@Resource(name="EventService")
	EventService es;
	
	@Resource(name="ProductService")
	ProductService ps;
	
	@Resource(name="OrderService")
	OrderService os;

	@Autowired
	ServletContext context;

	
	@RequestMapping(value = "/admin")
	public String admin() {
		return "/admin/main";
	}
	
	
	
	@RequestMapping(value = "/adminLogin")
	public String adminLogin(HttpServletRequest request, Model model) {
		HashMap<String, Object> paramMap = new HashMap<String, Object>();
		String id = request.getParameter("id");
		String pwd = request.getParameter("pwd");
		if(id==null || pwd == null) {
			return "/admin/adminLogin";
		}
		paramMap.put("id", id);
		paramMap.put("ref_cursor", null);
		
		as.b_adminCheck(paramMap);
		
		ArrayList< HashMap<String,Object> > list 
		= (ArrayList<HashMap<String, Object>>) paramMap.get("ref_cursor");
		if(list.size() == 0) {  // 입력한 아이디 없다면
			model.addAttribute("message" , "아이디가 없어요");
			return "admin/adminLogin";
		}
		HashMap<String, Object> resultMap = list.get(0); 
		if(resultMap.get("PWD")==null) {
			model.addAttribute("message" , "관리자에게 문의하세요");
			return "admin/adminLogin";
		}else if( pwd.equals( (String)resultMap.get("PWD") ) ) {
			HttpSession session = request.getSession();
			session.setAttribute("loginAdmin", resultMap);
			return "admin/main";
		}else {
			model.addAttribute("message" , "비번이 안맞아요");
			return "admin/adminLogin";
		}
	}
	
	@RequestMapping("/adminLogout")
	public String logout(HttpServletRequest request) {
		HttpSession session = request.getSession();
		session.removeAttribute("loginAdmin");
		return "redirect:/admin";
	}
	
	@RequestMapping("adminMemberList")
	public String adminMemberList(HttpServletRequest request, Model model) {
		HttpSession session = request.getSession();
		if (session.getAttribute("loginAdmin") == null) {
			return "admin/adminLogin";
		} else {
			int page = 1;
			if (request.getParameter("page") != null) {
				page = Integer.parseInt(request.getParameter("page"));
				session.setAttribute("page", page);
			} else if (session.getAttribute("page") != null) {
				page = (int) session.getAttribute("page");
			} else {
				page = 1;
				session.removeAttribute("page");
			}

			String key = "";
			if (request.getParameter("key") != null) {
				key = request.getParameter("key");
				session.setAttribute("key", key);
			} else if (session.getAttribute("key") != null) {
				key = (String) session.getAttribute("key");
			} else {
				session.removeAttribute("key");
				key = "";
			}

			Paging paging = new Paging();
			paging.setPage(page);
			
			HashMap<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("cnt", 0);	//게시물의 갯수를 담아올 공간 생성
			paramMap.put("key", key);
			
			as.b_getAllCountMem(paramMap);
			System.out.println(paramMap);
			int cnt = Integer.parseInt( paramMap.get("cnt").toString() );
			paging.setTotalCount( cnt );
			
			paramMap.put("startNum" , paging.getStartNum() );
			paramMap.put("endNum", paging.getEndNum() );
			paramMap.put("ref_cursor", null);
			as.b_listMember(paramMap);

			ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>) paramMap.get("ref_cursor");
			
			model.addAttribute("memberList", list);
			model.addAttribute("paging", paging);
			model.addAttribute("key", key);
		}
		return "admin/member/memberList";
	}
	
	@RequestMapping(value = "/adminMemberDelete", method = RequestMethod.POST)
	public String adminMemberDelete(HttpServletRequest request,
			@RequestParam("delete") int[] mseqArr, Model model) {
		String id = request.getParameter("id");
		
		HttpSession session = request.getSession();
		HashMap<String, Object> loginAdmin = (HashMap<String, Object>)session.getAttribute("loginAdmin");
		
		// 해당 아이디의 result가 2,3인 주문을 가지고 온
		HashMap<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("ref_cursor" , null);
		paramMap.put("id" , id);
		os.b_getOrderListResult2(paramMap);

		ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>) paramMap.get("ref_cursor");
		// 해당 리스트가 1개라도 있으면 주문 처리중인 주문이 하나 이상 있는 것이므로
		// 삭제를 하지 않고 메시지만 보내고 중지한다.
		if(list.size() > 0) {
			model.addAttribute("message", "진행중인 주문이 있어서 회원탈퇴가 불가능합니다.");
			return "redirect:/adminMemberList.do?page=1&key=";
		}
		
		
		HashMap<String, Object> paramMap1 = new HashMap<String, Object>();
		for (int mseq : mseqArr) {
			paramMap1.put("mseq", mseq);
			as.b_deleteMember(paramMap1);
		}
		return "redirect:/adminMemberList.do";
		 
	}

//event
	@RequestMapping("/adminEventList")
	public String adminEventList(HttpServletRequest request, Model model) {
		HttpSession session = request.getSession();
		HashMap<String, Object> loginAdmin = (HashMap<String, Object>)session.getAttribute("loginAdmin");
		if (loginAdmin == null) {
			return "admin/adminLogin";
		} else {
			int page = 1;
			if (request.getParameter("page") != null) {
				page = Integer.parseInt(request.getParameter("page"));
				session.setAttribute("page", page);
			} else if (session.getAttribute("page") != null) {
				page = (int) session.getAttribute("page");
			} else {
				page = 1;
				session.removeAttribute("page");
			}

			String key = "";
			if (request.getParameter("key") != null) {
				key = request.getParameter("key");
				session.setAttribute("key", key);
			} else if (session.getAttribute("key") != null) {
				key = (String) session.getAttribute("key");
			} else {
				session.removeAttribute("key");
				key = "";
			}

			Paging paging = new Paging();
			paging.setPage(page);
			
			HashMap<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("cnt", 0);	//게시물의 갯수를 담아올 공간 생성
			paramMap.put("key", key);
			
			as.b_getAllCountEvent(paramMap);
			System.out.println(paramMap);
			int cnt = Integer.parseInt( paramMap.get("cnt").toString() );
			paging.setTotalCount( cnt );
			
			paramMap.put("startNum" , paging.getStartNum() );
			paramMap.put("endNum", paging.getEndNum() );
			paramMap.put("ref_cursor", null);
			as.b_listEvent(paramMap);

			ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>) paramMap.get("ref_cursor");
			for(int i = 0; i < list.size(); i++) {
				String start = list.get(i).get("STARTDATE").toString();
				String end = list.get(i).get("ENDDATE").toString();
				//substring
				start = start.substring(2,10);
				end = end.substring(2,10);
				list.get(i).put("STARTDATE", start);
				list.get(i).put("ENDDATE", end);
			}
			model.addAttribute("eventList", list);
			model.addAttribute("paging", paging);
			model.addAttribute("key", key);
			return "admin/event/eventList";
		}
	}
	
	
//이벤트상세보기
	@RequestMapping("/adminEventDetail")
	public String adminEventDetail(HttpServletRequest request, Model model, @RequestParam("eseq") int eseq) {
		HttpSession session = request.getSession();
		if (session.getAttribute("loginAdmin") == null) {
			return "admin/adminLogin";
		} else {
			HashMap<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("eseq", eseq);
			paramMap.put("ref_cursor", null);
			es.b_getEvent(paramMap);
			
			ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>) paramMap.get("ref_cursor");
			for(int i = 0; i < list.size(); i++) {
				String start = list.get(i).get("STARTDATE").toString();
				String end = list.get(i).get("ENDDATE").toString();
				//substring
				start = start.substring(2,10);
				end = end.substring(2,10);
				list.get(i).put("STARTDATE", start);
				list.get(i).put("ENDDATE", end);
			}
			model.addAttribute("eventVO", list.get(0));
			return "admin/event/eventDetail";
		}
	}

	@RequestMapping("/adminEventWriteForm")
	public String adminEventWriteForm(HttpServletRequest request, Model model) {
		HttpSession session = request.getSession();
		HashMap<String, Object> loginAdmin = (HashMap<String, Object>)session.getAttribute("loginAdmin");
		if (loginAdmin == null)
			return "admin/adminLogin";

		return "admin/event/eventWrite";
	}
	
//이벤트등록
	@RequestMapping(value = "/adminEventWrite", method = RequestMethod.POST) 
	public String adminEventWrite(Model model, HttpServletRequest request) {
		HashMap<String, Object> paramMap = new HashMap<String, Object>();
		
		String savePath = context.getRealPath("image/main/event");
		System.out.println(savePath);
		
		
		String image = "";
		String thumbnail = "";
		
		try {
			MultipartRequest multi = new MultipartRequest(request, savePath, 5 * 1024 * 1024, "UTF-8",
					new DefaultFileRenamePolicy());
			String subject = multi.getParameter("subject");
			String content = multi.getParameter("content");
			String enddate = multi.getParameter("enddate");
			enddate = enddate.substring(2,10);
			image = multi.getFilesystemName("image");
			thumbnail = multi.getFilesystemName("thumbnail");
			
			paramMap.put("subject", subject);
			paramMap.put("content", content);
			paramMap.put("enddate", enddate);
			paramMap.put("image", image);
			paramMap.put("thumbnail", thumbnail);
			paramMap.put("state", '1');
			
			if (multi.getParameter("subject") == null) {
				System.out.println("이벤트명을 입력하세요");
				return "admin/event/eventWrite.jsp";
			}
			System.out.println(paramMap);
			as.b_insertEvent(paramMap);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "redirect:/adminEventList.do";
	}
	
	
//이벤트삭제
	@RequestMapping(value = "/adminEventDelete")
	public String adminEventDelete(@RequestParam("delete") String[] eseqArr, HttpServletRequest request) {
		
		HttpSession session = request.getSession();
		HashMap<String, Object> loginAdmin = (HashMap<String, Object>)session.getAttribute("loginAdmin");
		if (loginAdmin == null) {
			return "admin/adminLogin";
		} else {
			HashMap<String, Object> paramMap = new HashMap<String, Object>();
			for (String eseq : eseqArr) {
				paramMap.put("eseq", eseq);
				es.b_deleteEvent(paramMap);
			}				
			return "redirect:/adminEventList.do";
		}
	}
			
	@RequestMapping(value = "/adminEventUpdateForm")
	public String adminEventUpdateForm(HttpServletRequest request, Model model, @RequestParam("eseq")int eseq) {
		HttpSession session = request.getSession();
		HashMap<String, Object> loginAdmin = (HashMap<String, Object>)session.getAttribute("loginAdmin");
		if (loginAdmin == null) {
			return "admin/adminLogin";
		} else {
			HashMap<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("eseq", eseq);
			paramMap.put("ref_cursor", null);
			es.b_getEvent(paramMap);
			
			ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>) paramMap.get("ref_cursor");
			for(int i = 0; i < list.size(); i++) {
				String end = list.get(i).get("ENDDATE").toString();
				//substring
				end = end.substring(0,10);
				list.get(i).put("ENDDATE", end);
			}
			model.addAttribute("eventVO", list.get(0));

			return "admin/event/eventUpdate";
		}
	}
	
	//이벤트수정
	@RequestMapping(value="/adminEventUpdate" , method=RequestMethod.POST) 
	  public String adminEventUpdate( Model model, HttpServletRequest request) { 
		HashMap<String, Object> paramMap = new HashMap<String, Object>();
		
		String savePath = context.getRealPath("image/main/event");
		System.out.println(savePath);
		
		
		String image = "";
		String thumbnail = "";
		try {
			MultipartRequest multi=new MultipartRequest(request, savePath,
					5*1024*1024, "UTF-8", new DefaultFileRenamePolicy());
			
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
	        SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd hh:mm:ss");

			String enddate = multi.getParameter("enddate");
			int state = 1;
			// 현재시간과 이벤트 종료 시간을 비교하여 현재시간이 이벤트 종료시간보다 커지면
			// state를 0으로 변경하여 종료된 이벤트로 이동
	        if(sdf.format(timestamp).compareTo(enddate) > 0) {
	        	state = 0;
	        }
	        
	        String subject = multi.getParameter("subject");
			String content = multi.getParameter("content");
			image = multi.getFilesystemName("image");
			thumbnail = multi.getFilesystemName("thumbnail");
			int eseq = Integer.parseInt(multi.getParameter("eseq"));
			
			paramMap.put("eseq", eseq);
			paramMap.put("subject", subject);
			paramMap.put("content", content);
			paramMap.put("enddate", enddate);
			paramMap.put("state", state);
			
			
			if(multi.getFilesystemName("image") == null)
				paramMap.put("image",multi.getParameter("oldImage"));
			else
				paramMap.put("image", image);
			if(multi.getFilesystemName("thumbnail") == null)
				paramMap.put("thumbnail",multi.getParameter("oldthumbnail"));
			else
				paramMap.put("thumbnail", thumbnail);
			
			as.b_updateEvent(paramMap);
		} catch (IOException e) {		e.printStackTrace();	}
		return "redirect:/adminEventList.do";
	  }
	
	@RequestMapping(value = "/adminMemberUpdateForm")
	public String adminMemberUpdateForm(@RequestParam("mseq") int mseq, HttpServletRequest request, Model model) {
		HttpSession session = request.getSession();
		if (session.getAttribute("loginAdmin") == null) {
			return "admin/adminLogin";
		} else {
			HashMap<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("mseq", mseq);
			paramMap.put("ref_cursor", null);
			ms.b_getMember2(paramMap);
			
			ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>) paramMap.get("ref_cursor");
			model.addAttribute("memberVO", list.get(0));

			return "admin/member/memberUpdate";
		}
	}

	@RequestMapping(value = "/adminMemberUpdate", method = RequestMethod.POST)
	public String adminMemberUpdateForm(HttpServletRequest request, Model model,
			@RequestParam(value = "pwd_chk", required = false) String pwd_chk) {
		String id = request.getParameter("id");
		String name = request.getParameter("name");
		String pwd = request.getParameter("pwd");
		String phone = request.getParameter("phone");
		
		if(pwd.equals(null)) {
			model.addAttribute("message", "암호를 입력하세요");
			return "admin/member/memberUpdate";
		} else if (name.equals("")) {
			model.addAttribute("message", "이름을 입력하세요");
			return "admin/member/memberUpdate";
		} else if (pwd_chk == null || (pwd_chk != null && !pwd_chk.equals(pwd))) {
			model.addAttribute("message", "비밀번호 확인이 일치하지 않습니다.");
			return "admin/member/memberUpdate";
		}
		if (phone.equals("")) {
			model.addAttribute("message", "전화번호를 입력하세요");
			return "admin/member/memberUpdate";
		} else {
			HashMap<String, Object> mvo = new HashMap<String, Object>();
			mvo.put("id",id);
			mvo.put("pwd",pwd);
			mvo.put("name",name);
			mvo.put("phone",phone);
			
			System.out.println(mvo);
			ms.b_adminUpdateMember(mvo);
			return "redirect:/adminMemberList.do";
		}
	}
	
//qna
	@RequestMapping(value = "/adminQnaList")
	public String adminQnaList(HttpServletRequest request, Model model) {
		HttpSession session = request.getSession();
		if (session.getAttribute("loginAdmin") == null) {
			return "admin/adminLogin";
		} else {
			int page = 1;
			if (request.getParameter("page") != null) {
				page = Integer.parseInt(request.getParameter("page"));
				session.setAttribute("page", page);
			} else if (session.getAttribute("page") != null) {
				page = (int) session.getAttribute("page");
			} else {
				page = 1;
				session.removeAttribute("page");
			}

			String key = "";
			if (request.getParameter("key") != null) {
				key = request.getParameter("key");
				session.setAttribute("key", key);
			} else if (session.getAttribute("key") != null) {
				key = (String) session.getAttribute("key");
			} else {
				session.removeAttribute("key");
				key = "";
			}

			Paging paging = new Paging();
			paging.setPage(page);
			
			HashMap<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("cnt", 0);	//게시물의 갯수를 담아올 공간 생성
			paramMap.put("key", key);
			
			as.b_getAllCountQna(paramMap);
			System.out.println(paramMap);
			int cnt = Integer.parseInt( paramMap.get("cnt").toString() );
			paging.setTotalCount( cnt );
			
			paramMap.put("startNum" , paging.getStartNum() );
			paramMap.put("endNum", paging.getEndNum() );
			paramMap.put("ref_cursor", null);
			as.b_adminListQna(paramMap);

			ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>) paramMap.get("ref_cursor");

			model.addAttribute("qnaList", list);
			model.addAttribute("paging", paging);
			model.addAttribute("key", key);
		}
		return "admin/qna/qnaList";
	}
	
	@RequestMapping(value = "/adminQnaDelete", method = RequestMethod.POST)
	public String adminQnaDelete(@RequestParam("delete") int[] qseqArr, HttpServletRequest request) {
		HttpSession session = request.getSession();
		HashMap<String, Object> loginAdmin = (HashMap<String, Object>)session.getAttribute("loginAdmin");
		if (loginAdmin == null) {
			return "admin/adminLogin";
		} else {
			HashMap<String, Object> paramMap = new HashMap<String, Object>();
			for (int qseq : qseqArr) {
				paramMap.put("qseq", qseq);
				qs.b_deleteQna(paramMap);
			}				
			return "redirect:/adminQnaList.do";
		}
	}
	
	@RequestMapping("/adminQnaDetail")
	public String adminQnaDetail(@RequestParam("qseq") int qseq, HttpServletRequest request, Model model) {
		HttpSession session = request.getSession();
		HashMap<String, Object> loginAdmin = (HashMap<String, Object>)session.getAttribute("loginAdmin");
		if (loginAdmin == null) {
			return "admin/adminLogin";
		} else {
			HashMap<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("qseq", qseq);
			paramMap.put("ref_cursor", null);
			qs.b_getQna(paramMap);
			ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>) paramMap.get("ref_cursor");
			model.addAttribute("qnaVO", list.get(0));
			return "admin/qna/qnaDetail";
		}

	}
	
// QnA 답글달기
	@RequestMapping("/adminQnaRepsave")
	public String adminQnaRepsave(HttpServletRequest request, Model model, @RequestParam("qseq") int qseq,
			@RequestParam("reply") String reply) {
		HttpSession session = request.getSession();
		HashMap<String, Object> loginAdmin = (HashMap<String, Object>)session.getAttribute("loginAdmin");
		if (loginAdmin == null) {
			return "admin/adminLogin";
		} else {
			HashMap<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("qseq", qseq);
			paramMap.put("reply", reply);
			paramMap.put("ref_cursor", null);
			qs.b_updateQna(paramMap);
			return "redirect:/adminQnaDetail.do?qseq=" + qseq;
		}

	}
	
// shortproduct는 썸네일을 위한 작업
	@RequestMapping(value="/adminShortProductList.do")
	public String adminShortProductList(HttpServletRequest request, Model model) {		
			HttpSession session = request.getSession();
			
			HashMap<String, Object> loginAdmin = (HashMap<String, Object>)session.getAttribute("loginAdmin");
			if (loginAdmin == null) {
				return "admin/adminLogin";
			} else {
				int page = 1;
				if (request.getParameter("page") != null) {
					page = Integer.parseInt(request.getParameter("page"));
					session.setAttribute("page", page);
				} else if (session.getAttribute("page") != null) {
					page = (int) session.getAttribute("page");
				} else {
					page = 1;
					session.removeAttribute("page");
				}

				String key = "";
				if (request.getParameter("key") != null) {
					key = request.getParameter("key");
					session.setAttribute("key", key);
				} else if (session.getAttribute("key") != null) {
					key = (String) session.getAttribute("key");
				} else {
					session.removeAttribute("key");
					key = "";
				}

				Paging paging = new Paging();
				paging.setPage(page);
				
				HashMap<String, Object> paramMap = new HashMap<String, Object>();
				paramMap.put("cnt", 0);	//게시물의 갯수를 담아올 공간 생성
				paramMap.put("key", key);
				
				as.b_getShortProductAllCount(paramMap);
				System.out.println(paramMap);
				int cnt = Integer.parseInt( paramMap.get("cnt").toString() );
				paging.setTotalCount( cnt );
				
				paramMap.put("startNum" , paging.getStartNum() );
				paramMap.put("endNum", paging.getEndNum() );
				paramMap.put("ref_cursor", null);
				as.b_listShortProduct(paramMap);

				ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>) paramMap.get("ref_cursor");

				model.addAttribute("shortproductList", list);
				model.addAttribute("paging", paging);
				model.addAttribute("key", key);
		}
		return "admin/product/shortproductList";
	}

	@RequestMapping(value="/adminProductList.do")
	public String adminProductList(HttpServletRequest request, Model model) {
		HttpSession session = request.getSession();
		
		HashMap<String, Object> loginAdmin = (HashMap<String, Object>)session.getAttribute("loginAdmin");
		if (loginAdmin == null) {
			return "admin/adminLogin";
		} else {
			int page = 1;
			if (request.getParameter("page") != null) {
				page = Integer.parseInt(request.getParameter("page"));
				session.setAttribute("page", page);
			} else if (session.getAttribute("page") != null) {
				page = (int) session.getAttribute("page");
			} else {
				page = 1;
				session.removeAttribute("page");
			}

			String key = "";
			if (request.getParameter("key") != null) {
				key = request.getParameter("key");
				session.setAttribute("key", key);
			} else if (session.getAttribute("key") != null) {
				key = (String) session.getAttribute("key");
			} else {
				session.removeAttribute("key");
				key = "";
			}

			Paging paging = new Paging();
			paging.setPage(page);
			
			HashMap<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("cnt", 0);	//게시물의 갯수를 담아올 공간 생성
			paramMap.put("key", key);
			
			as.b_getProductAllCount(paramMap);
			System.out.println(paramMap);
			int cnt = Integer.parseInt( paramMap.get("cnt").toString() );
			paging.setTotalCount( cnt );
			
			paramMap.put("startNum" , paging.getStartNum() );
			paramMap.put("endNum", paging.getEndNum() );
			paramMap.put("ref_cursor", null);
			as.b_listProduct(paramMap);

			ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>) paramMap.get("ref_cursor");

			model.addAttribute("productList", list);
			model.addAttribute("paging", paging);
			model.addAttribute("key", key);
		}
		return "admin/product/productList";
	}
	
	@RequestMapping(value = "/adminProductDelete.do", method = RequestMethod.POST)
	public String adminProductDelete(HttpServletRequest request, @RequestParam("delete") int[] pseqArr, Model model) {
		HttpSession session = request.getSession();
		HashMap<String, Object> loginAdmin = (HashMap<String, Object>)session.getAttribute("loginAdmin");
		if (loginAdmin == null) {
			return "admin/adminLogin";
		} else {		
			HashMap<String, Object> paramMap = new HashMap<String, Object>();		
			for (int pseq : pseqArr) {
				paramMap.put("pseq", pseq);
				as.b_deleteProduct(paramMap);
		}		
		return "redirect:/adminShortProductList.do";
	}
	}
	
	@RequestMapping(value="/adminProductWriteForm.do")
	public String adminProductWriteForm(HttpServletRequest request, Model model) {
		HttpSession session = request.getSession();
		HashMap<String, Object> loginAdmin = (HashMap<String, Object>)session.getAttribute("loginAdmin");
		if (loginAdmin == null) {
			return "admin/adminLogin";
		} else {
			String kindList[] = { "스페셜&할인팩", "프리미엄", "와퍼", "주니어&버거", "올데이킹&치킨버거", "사이드", "음료&디저트", "독퍼" };
			model.addAttribute("kindList", kindList);
			return "admin/product/productWrite";
		}
	}
	
	@RequestMapping(value="/adminShortProductWriteForm.do")
	public String adminShortProductWriteForm(HttpServletRequest request, Model model) {
		HttpSession session = request.getSession();
		HashMap<String, Object> loginAdmin = (HashMap<String, Object>)session.getAttribute("loginAdmin");
		if (loginAdmin == null) {
			return "admin/adminLogin";
		} else {
			String kindList[] = { "스페셜&할인팩", "프리미엄", "와퍼", "주니어&버거", "올데이킹&치킨버거", "사이드", "음료&디저트", "독퍼" };
			model.addAttribute("kindList", kindList);
			return "admin/product/shortproductWrite";
		}
	}
	
	@RequestMapping(value = "adminProductWrite.do", method = RequestMethod.POST)
	public String adminProductWrite(Model model, HttpServletRequest request,HttpServletResponse response){
		String savePath = context.getRealPath("/image/menu/product");
		System.out.println(savePath);
		
		try {
			MultipartRequest multi = new MultipartRequest(request, savePath, 5 * 1024 * 1024, "UTF-8",
					new DefaultFileRenamePolicy());
			String k1 = multi.getParameter("kind1");
			String k2 = multi.getParameter("kind2");
			String k3 = multi.getParameter("kind3");
			
			HashMap<String, Object>paramMap1=new HashMap<String, Object>();
			paramMap1.put("kind1", k1);
			paramMap1.put("ref_cursor", null);
			
			as.b_selectProduct1(paramMap1);
			ArrayList<HashMap<String, Object>> list1 = (ArrayList<HashMap<String, Object>>) paramMap1.get("ref_cursor");
			
			if(list1.size()==0) {
				response.setContentType("text/html; charset=UTF-8");
				PrintWriter writer = response.getWriter();
				writer.println("<script>alert('해당하는 종류분류 값이 없습니다.'); location.href='adminProductWriteForm.do';</script>");
				writer.close();
			}
			
			HashMap<String, Object>paramMap2=new HashMap<String, Object>();
			paramMap2.put("kind1", k1);
			paramMap2.put("kind2", k2);
			paramMap2.put("ref_cursor", null);
			as.b_selectProduct2(paramMap2);
			ArrayList<HashMap<String, Object>> list2 = (ArrayList<HashMap<String, Object>>) paramMap2.get("ref_cursor");
			
			if(list2.size()==0) {
				response.setContentType("text/html; charset=UTF-8");
				PrintWriter writer = response.getWriter();
				writer.println("<script>alert('해당하는 분류번호의 상품 썸네일이 없습니다.'); location.href='adminProductWriteForm.do';</script>");
				writer.close();
			}
			if(k3.equals("4")) {
				response.setContentType("text/html; charset=UTF-8");
				PrintWriter writer = response.getWriter();
				writer.println("<script>alert('입력할 수 없는 세부 값입니다.'); location.href='adminProductWriteForm.do';</script>");
				writer.close();	
			}else {
				HashMap<String, Object>pvo=new HashMap<String, Object>();
				pvo.put("kind1", multi.getParameter("kind1"));
				pvo.put("kind2", multi.getParameter("kind2"));
				pvo.put("kind3", multi.getParameter("kind3"));
						
				pvo.put("pname", multi.getParameter("pname"));
				pvo.put("price1", Integer.parseInt(multi.getParameter("price1")));
				pvo.put("price2", Integer.parseInt("0"));
				pvo.put("price3", Integer.parseInt("0"));
				pvo.put("content", multi.getParameter("content"));
				pvo.put("image", multi.getFilesystemName("image"));
				pvo.put("useyn", multi.getParameter("useyn"));
			    
			    as.b_insertProduct(pvo);
			}
			
		} catch (IOException e) {e.printStackTrace();	}
		return "redirect:/adminProductList.do";
	}
	
	@RequestMapping(value = "/adminShortProductWrite.do", method = RequestMethod.POST)
	public String adminShortProductWrite(Model model, HttpServletRequest request,
			HttpServletResponse response) {
		String savePath = context.getRealPath("/image/menu/product");
		System.out.println(savePath);

		try {
			MultipartRequest multi = new MultipartRequest(request, savePath, 5 * 1024 * 1024, "UTF-8",
					new DefaultFileRenamePolicy());
			String k1 = multi.getParameter("kind1");
			String k2 = multi.getParameter("kind2");
			HashMap<String, Object>paramMap=new HashMap<String, Object>();
			paramMap.put("kind1", k1);
			paramMap.put("kind2", k2);
			paramMap.put("ref_cursor", null);
			as.b_selectProduct2(paramMap);

			ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>) paramMap.get("ref_cursor");
			
			if(list.size()!=0) {
				response.setContentType("text/html; charset=UTF-8");
				PrintWriter writer = response.getWriter();
				writer.println("<script>alert('해당하는 분류번호 값이 이미 있습니다.'); location.href='adminShortProductWriteForm';</script>");
				writer.close();
			
			}else {
				HashMap<String, Object>pvo=new HashMap<String, Object>();
				pvo.put("kind1", multi.getParameter("kind1"));
				pvo.put("kind2", multi.getParameter("kind2"));
				pvo.put("kind3", "4" );
				pvo.put("pname", multi.getParameter("pname"));
				pvo.put("price1", Integer.parseInt(multi.getParameter("price1")));
			    pvo.put("price2", Integer.parseInt("0"));
				pvo.put("price3", Integer.parseInt("0"));
				pvo.put("content", " ");
				pvo.put("image", multi.getFilesystemName("image"));
				pvo.put("useyn", multi.getParameter("useyn"));
			
			    as.b_insertProduct(pvo);
			}
			
		} catch (IOException e) {e.printStackTrace();	}
		return "redirect:/adminShortProductList.do";
	}
	
	@RequestMapping(value="/adminProductDetail.do")
	public String productDetail(@RequestParam("pseq") int pseq, HttpServletRequest request, Model model) {
		HttpSession session = request.getSession();
		HashMap<String, Object> loginAdmin = (HashMap<String, Object>)session.getAttribute("loginAdmin");
		if (loginAdmin == null) {
			return "admin/adminLogin";
		} else {
			HashMap<String, Object> paramMap=new HashMap<String, Object>();
			paramMap.put("pseq",pseq);
			paramMap.put("ref_cursor",null);
			as.b_productDetail(paramMap);
			ArrayList< HashMap<String,Object> > list 
			= (ArrayList<HashMap<String, Object>>) paramMap.get("ref_cursor");

			HashMap<String,Object>resultMap=list.get(0);
			// 카테고리 별 타이틀을 배열에 저장 
			String kindList1[] = {"0", "스페셜&할인팩", "프리미엄", "와퍼", "주니어&버거", "올데이킹&치킨버거", "사이드", "음료&디저트", "독퍼"};
			int index = Integer.parseInt(resultMap.get("KIND1").toString());
			String kindList3[] = {"0", "Single", "Set", "LargeSet", "Menu list"};
			int index2 = Integer.parseInt(resultMap.get("KIND3").toString());
			// 추출한 kind 번호로 배열에서 해당 타이틀 추출 & 리퀘스트에 저장 
			model.addAttribute("productVO", resultMap);
			model.addAttribute("kind1", kindList1[index]);
			model.addAttribute("kind3", kindList3[index2]);
			return "admin/product/productDetail";
		}
	}
	
	  	@RequestMapping(value="/adminShortProductDetail.do")
	public String shortProductDetail(@RequestParam("pseq") int pseq, HttpServletRequest request, Model model) {
		HttpSession session = request.getSession();
		HashMap<String, Object> loginAdmin = (HashMap<String, Object>)session.getAttribute("loginAdmin");
		if (loginAdmin == null) {
			return "admin/adminLogin";
		} else {
			HashMap<String, Object> paramMap=new HashMap<String, Object>();
			paramMap.put("pseq",pseq);
			paramMap.put("ref_cursor",null);
			as.b_productDetail(paramMap);

			ArrayList< HashMap<String,Object> > list 
			= (ArrayList<HashMap<String, Object>>) paramMap.get("ref_cursor");

			HashMap<String,Object>resultMap=list.get(0);
			// 카테고리 별 타이틀을 배열에 저장 
			String kindList1[] = {"0", "스페셜&할인팩", "프리미엄", "와퍼", "주니어&버거", "올데이킹&치킨버거", "사이드", "음료&디저트", "독퍼"};
			int index = Integer.parseInt(resultMap.get("KIND1").toString());
			String kindList3[] = {"0", "Single", "Set", "LargeSet", "Menu list"};
			int index2 = Integer.parseInt(resultMap.get("KIND3").toString());
			String useynList[] = {"0", "사용", "미사용"};
			int index3 = Integer.parseInt(resultMap.get("USEYN").toString());
			// 추출한 kind 번호로 배열에서 해당 타이틀 추출 & 리퀘스트에 저장 
			model.addAttribute("kind1", kindList1[index]);
			model.addAttribute("kind3", kindList3[index2]);
			model.addAttribute("useyn", useynList[index3]);
			model.addAttribute("productVO", resultMap); 
			model.addAttribute("k1", resultMap.get("Kind1"));
			model.addAttribute("pseq", pseq);
			return "admin/product/shortproductDetail";
		}
	}
	
	@RequestMapping(value="/adminProductUpdateForm.do")
	public String adminProductUpdateForm(@RequestParam("pseq") int pseq, HttpServletRequest request, Model model) {
		HashMap<String, Object> paramMap=new HashMap<String, Object>();
		
		HttpSession session = request.getSession();
		HashMap<String, Object> loginAdmin = (HashMap<String, Object>)session.getAttribute("loginAdmin");
		if (loginAdmin == null) {
			return "admin/adminLogin";
		} else {
		paramMap.put("pseq",pseq); 
		paramMap.put("ref_cursor",null);
		as.b_productDetail(paramMap);
		ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>) paramMap.get("ref_cursor");
		HashMap<String,Object>resultMap=list.get(0);
		
		String kindList1[] = {"스페셜&할인팩", "프리미엄", "와퍼", "주니어&버거", "올데이킹&치킨버거", "사이드", "음료&디저트", "독퍼"};
		String kindList3[] = {"Single", "Set", "LargeSet"};
		model.addAttribute("kindList1", kindList1);
		model.addAttribute("kindList3", kindList3);
		int index = Integer.parseInt(resultMap.get("KIND1").toString());
		int index2 = Integer.parseInt(resultMap.get("KIND3").toString());
		
		model.addAttribute("productVO",resultMap);
		model.addAttribute("kind", kindList1[index-1]);
		model.addAttribute("kind3", kindList3[index2-1]);
		return "admin/product/productUpdate";
	}
   }
	
	@RequestMapping(value="adminShortProductUpdateForm.do", method = RequestMethod.POST)
	public String adminShortProductUpdateForm(HttpServletRequest request, Model model) {
		HashMap<String, Object> paramMap=new HashMap<String, Object>();
		int pseq = Integer.parseInt(request.getParameter("pseq"));
		
		HttpSession session = request.getSession();
		HashMap<String, Object> loginAdmin = (HashMap<String, Object>)session.getAttribute("loginAdmin");
		if (loginAdmin == null) {
			return "admin/adminLogin";
		} else {
		paramMap.put("pseq",pseq); 
		paramMap.put("ref_cursor",null);	
		as.b_productDetail(paramMap);
		ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>) paramMap.get("ref_cursor");
		HashMap<String,Object>resultMap=list.get(0);
		
		String k1 = request.getParameter("k1");
		String kindList1[] = {"스페셜&할인팩", "프리미엄", "와퍼", "주니어&버거", "올데이킹&치킨버거", "사이드", "음료&디저트", "독퍼"};
		int index = Integer.parseInt(resultMap.get("KIND1").toString());
		
		model.addAttribute("productVO", resultMap);
		model.addAttribute("kindList1", kindList1);
		model.addAttribute("kind", kindList1[index-1]);
		model.addAttribute("k1", k1);
		return "admin/product/shortproductUpdate";
	}
	}	

	@RequestMapping(value="/adminShortProductUpdate.do", method = RequestMethod.POST)
	public String adminShortProductUpdate(HttpServletRequest request, Model model) {				
		HashMap<String, Object> paramMap = new HashMap<String, Object>();
		int pseq=0;
		String savePath = context.getRealPath("/image/menu/product");
		System.out.println(savePath);
		try {
			MultipartRequest multi = new MultipartRequest(
					request, savePath , 5*1024*1024,  "UTF-8", new DefaultFileRenamePolicy() );
			paramMap.put("pseq",Integer.parseInt(multi.getParameter("pseq")));
			pseq=Integer.parseInt(multi.getParameter("pseq"));
			paramMap.put("kind1", multi.getParameter("kind1"));
			paramMap.put("kind2", multi.getParameter("kind2"));
			paramMap.put("kind3", multi.getParameter("kind3"));
			paramMap.put("pname", multi.getParameter("pname"));
			paramMap.put("price1",0);
			paramMap.put("price2",0);
			paramMap.put("price3",0);
			paramMap.put("content", " ");
			System.out.println(multi.getParameter("useyn") + "//////");
			if(multi.getParameter("useyn") == null) {
				paramMap.put("useyn","2");
			}else {
				paramMap.put("useyn","1");
			}
			if(multi.getFilesystemName("image") == null)
			 paramMap.put("image", multi.getParameter("oldImage"));
			else
				paramMap.put("image", multi.getFilesystemName("image"));
		} catch (IOException e) {e.printStackTrace();}
		as.b_updateProduct(paramMap);
		return "redirect:/adminShortProductDetail.do?pseq="+pseq;
	}	
	
	@RequestMapping(value="/adminProductUpdate.do", method = RequestMethod.POST)
	public String adminProductUpdate(HttpServletRequest request, Model model) {				
		HashMap<String, Object> paramMap = new HashMap<String, Object>();
		
		String savePath = context.getRealPath("/image/menu/product");
		System.out.println(savePath);
		int pseq=0;
		try {
			MultipartRequest multi= new MultipartRequest(
					request, savePath , 5*1024*1024,  "UTF-8", new DefaultFileRenamePolicy() );
			paramMap.put("pseq",Integer.parseInt(multi.getParameter("pseq")));
			pseq=Integer.parseInt(multi.getParameter("pseq"));
			paramMap.put("kind1", multi.getParameter("kind1"));
			paramMap.put("kind2", multi.getParameter("kind2"));
			paramMap.put("kind3", multi.getParameter("kind3"));
			paramMap.put("pname", multi.getParameter("pname"));
			paramMap.put("price1", Integer.parseInt(multi.getParameter("price1")));
			paramMap.put("price2", Integer.parseInt(multi.getParameter("price2")));
			paramMap.put("price3", Integer.parseInt(multi.getParameter("price3")));
			paramMap.put("content",multi.getParameter("content"));
			System.out.println(multi.getParameter("useyn") + "//////");
			if(multi.getParameter("useyn") == null) {
				paramMap.put("useyn",2);
			}else {
				paramMap.put("useyn",1);
			}
			if(multi.getFilesystemName("image") == null)
			 paramMap.put("image", multi.getParameter("oldImage"));
			else
				paramMap.put("image", multi.getFilesystemName("image").toString());
			as.b_updateProduct(paramMap);
		} catch (IOException e) {e.printStackTrace();}
		return "redirect:/adminProductDetail.do?pseq="+pseq;
	}
	

	// 관리자 주문 리스트
	@RequestMapping(value="/adminOrderList")
	public String adminOrderList(HttpServletRequest request, @RequestParam("kind")String kind, Model model) {
		// 위의 param kind는 회원:1, 비회원:2의 값을 가지고 들어온다.
		HttpSession session = request.getSession();
		HashMap<String, Object> loginAdmin = (HashMap<String, Object>)session.getAttribute("loginAdmin");
		if (loginAdmin == null) {
			return "admin/adminLogin";
		} else {
			int page = 1;
			if (request.getParameter("page") != null) {
				page = Integer.parseInt(request.getParameter("page"));
				session.setAttribute("page", page);
			} else if (session.getAttribute("page") != null) {
				page = (int) session.getAttribute("page");
			} else {
				page = 1;
				session.removeAttribute("page");
			}

			String key = "";
			if (request.getParameter("key") != null) {
				key = request.getParameter("key");
				session.setAttribute("key", key);
			} else if (session.getAttribute("key") != null) {
				key = (String) session.getAttribute("key");
			} else {
				session.removeAttribute("key");
				key = "";
			}

			Paging paging = new Paging();
			paging.setPage(page);
			
			
			HashMap<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("cnt", 0);
			paramMap.put("key", key);
			// kind값으로 회원 혹은 비회원의 count값을 구하여 paging을 설정한다.
			if(kind.equals("1")) {
				as.b_getAllCountOrderMem(paramMap); //order_view1
			}else {
				as.b_getAllCountOrderNonmem(paramMap); //order_view2
			}

			System.out.println(paramMap);
			int cnt = Integer.parseInt( paramMap.get("cnt").toString() );
			paging.setTotalCount( cnt );

			// kind값의 해당하는 order_view의 리스트를 불러온다.
			paramMap.put("startNum" , paging.getStartNum() );
			paramMap.put("endNum", paging.getEndNum() );
			paramMap.put("ref_cursor", null);
			System.out.println(paramMap);
			if(kind.equals("1")) {
				as.b_adminListOrder(paramMap); //order_view1
			}else {
				as.b_adminListOrder2(paramMap); //order_view2
			}

			ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>) paramMap.get("ref_cursor");
			
			model.addAttribute("kind", kind);
			model.addAttribute("orderList", list);
			model.addAttribute("paging", paging);
			model.addAttribute("key", key);
		}
		return "admin/order/orderList";
	}
	
	// 주문 상태 처리 (1, 2, 3)
	@RequestMapping(value="/adminOrderSave")
	public String adminOrderSave(HttpServletRequest request, @RequestParam("kind")String kind, Model model) {
		
		
		HttpSession session = request.getSession();
		HashMap<String, Object> loginAdmin = (HashMap<String, Object>)session.getAttribute("loginAdmin");
		if (loginAdmin == null) {
			return "admin/adminLogin";
		} else {
			// 체크된 데이터들의 odseq값을 가져온다.
			
			String[] result = request.getParameterValues("result");
			
			for(int i = 0; i < result.length; i++) {
				// odseq값을 이용해 해당 주문상세의 result값을 가져온다.
				HashMap<String, Object> paramMap1 = new HashMap<String, Object>();

				paramMap1.put("odseq", result[i]);
				paramMap1.put("ref_cursor", null);
				
				as.b_adminGetResult(paramMap1);
				
				// result값을 list에 저장
				ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>) paramMap1.get("ref_cursor");
				HashMap<String, Object>resultMap = list.get(0);
				
				// result값을 +1하여 변경한다.
				HashMap<String, Object> paramMap2 = new HashMap<String, Object>();
				paramMap2.put("odseq", result[i]);
				paramMap2.put("result", Integer.parseInt( resultMap.get("RESULT").toString()) + 1);
				System.out.println(paramMap2);
				as.b_updateOrderResult(paramMap2); 
			}
			
		}
		return "redirect:/adminOrderList.do?kind="+kind;
	}
	
	// 관리자 주문 리스트 삭제
	@RequestMapping(value="/adminOrderDelete")
	public String adminOrderDelete(HttpServletRequest request, @RequestParam("kind")String kind, Model model) {
		HttpSession session = request.getSession();
		HashMap<String, Object> loginAdmin = (HashMap<String, Object>)session.getAttribute("loginAdmin");
		if (loginAdmin == null) {
			return "admin/adminLogin";
		} else {
			String[] oseqArr = request.getParameterValues("delete");
			
			for(String odseq : oseqArr) { 
				// odseq 값으로 해당 번호 값을 가진 주문의 oseq 값 추출
				HashMap<String, Object> paramMap1 = new HashMap<String, Object>();
				paramMap1.put("odseq", odseq);
				paramMap1.put("ref_cursor", null);
				os.b_getOseq(paramMap1);
				
				ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>) paramMap1.get("ref_cursor");
				HashMap<String, Object>resultMap1 = list.get(0);// oseq값 = resultMap1
				System.out.println("resultMap1 :" + resultMap1);
				// odseq 값을 담은 paramMap1을 이용하여 orderDetail 삭제
				os.b_deleteOrderDetail(paramMap1);
				// 같은 값의 추가메뉴도 삭제
				os.b_deleteSpo(paramMap1);
				
				// oseq 값을 기반으로 남은 odseq 즉 주문상세가 남아있는지 확인을 한다.
				HashMap<String, Object> paramMap2 = new HashMap<String, Object>();
				paramMap2.put("oseq", resultMap1.get("OSEQ"));
				paramMap2.put("ref_cursor", null);
				System.out.println(paramMap2);
				os.b_getOrderDetailByOseq(paramMap2);
				ArrayList<HashMap<String, Object>> list2 = (ArrayList<HashMap<String, Object>>) paramMap2.get("ref_cursor");				
				
				// 해당 oseq값에 해당하는 detail이 없으면 orders 테이블의 데이터도 삭제
				if(list2.size() == 0) {
					os.b_deleteOrders(paramMap2);
				}
			}
		}
		return "redirect:/adminOrderList.do?kind="+kind;
	}
	
	// 관리자 주문 상세페이지
	@RequestMapping(value="/adminOrderDetailForm")
	public String adminOrderDetailForm(HttpServletRequest request,
			@RequestParam("kind")String kind, @RequestParam("seq")String odseq, Model model) {
		HttpSession session = request.getSession();
		HashMap<String, Object> loginAdmin = (HashMap<String, Object>)session.getAttribute("loginAdmin");
		if (loginAdmin == null) {
			return "admin/adminLogin";
		} else {
			// kind 값에 따라 order_view1,2에서 odseq값을 가진 orderVO를 조회한다.
			if(kind.equals("1")) {
				HashMap<String, Object> paramMap1 = new HashMap<String, Object>();
				paramMap1.put("odseq", odseq);
				paramMap1.put("ref_cursor", null);
				os.b_getOrder_view(paramMap1);
				
				ArrayList<HashMap<String, Object>> list1 = (ArrayList<HashMap<String, Object>>) paramMap1.get("ref_cursor");
				HashMap<String, Object>resultMap1 = list1.get(0);
				System.out.println("resultMap1 :" + resultMap1);
				int totalPrice = Integer.parseInt( resultMap1.get("PRICE1").toString()) * Integer.parseInt( resultMap1.get("QUANTITY").toString());
				// odseq값을 가진 추가메뉴를 조회한다.
				ps.b_selectSubProductOrder6(paramMap1);
				ArrayList<HashMap<String, Object>> list2 = (ArrayList<HashMap<String, Object>>) paramMap1.get("ref_cursor");
				
				
				for(HashMap<String, Object>sovo : list2) {
					totalPrice += Integer.parseInt( sovo.get("ADDPRICE").toString());
				}
				
				// 조회한 값들을 전송한다.
				model.addAttribute("totalPrice", totalPrice);
				model.addAttribute("kind", kind);
				model.addAttribute("ovo", resultMap1);
				model.addAttribute("spseqAm", list2);
			}else if(kind.equals("2")) {
				HashMap<String, Object> paramMap1 = new HashMap<String, Object>();
				paramMap1.put("odseq", odseq);
				paramMap1.put("ref_cursor", null);
				os.b_getOrder_view2(paramMap1);
				
				ArrayList<HashMap<String, Object>> list1 = (ArrayList<HashMap<String, Object>>) paramMap1.get("ref_cursor");
				HashMap<String, Object>resultMap1 = list1.get(0);
				System.out.println("resultMap1 :" + resultMap1);
				int totalPrice = Integer.parseInt( resultMap1.get("PRICE1").toString()) * Integer.parseInt( resultMap1.get("QUANTITY").toString());
				// odseq값을 가진 추가메뉴를 조회한다.
				ps.b_selectSubProductOrder6(paramMap1);
				ArrayList<HashMap<String, Object>> list2 = (ArrayList<HashMap<String, Object>>) paramMap1.get("ref_cursor");
				
				
				for(HashMap<String, Object>sovo : list2) {
					totalPrice += Integer.parseInt( sovo.get("ADDPRICE").toString());
				}
				
				// 조회한 값들을 전송한다.
				model.addAttribute("totalPrice", totalPrice);
				model.addAttribute("kind", kind);
				model.addAttribute("ovo", resultMap1);
				model.addAttribute("spseqAm", list2);
			}
		}
		return "admin/order/orderDetail";
	}
	
	// 관리자 페이지에서 주문의 추가 쟤료 삭제
	@RequestMapping(value="/adminOrderMDelete")
	public String adminOrderMDelete(HttpServletRequest request,
			@RequestParam("kind")String kind, @RequestParam("sposeq")String sposeq,
			@RequestParam("odseq")String odseq) {

		HttpSession session = request.getSession();
		HashMap<String, Object> loginAdmin = (HashMap<String, Object>)session.getAttribute("loginAdmin");
		if (loginAdmin == null) {
			return "admin/adminLogin";
		} else {
			HashMap<String, Object> paramMap = new HashMap<String, Object>(); 
			paramMap.put("sposeq", sposeq);
			ps.b_deleteSpo(paramMap);
			
		}
		return "redirect:/adminOrderDetailForm.do?kind="+kind+"&seq="+odseq;
	}
}

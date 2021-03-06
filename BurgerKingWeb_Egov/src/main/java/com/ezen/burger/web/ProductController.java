package com.ezen.burger.web;

import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.ezen.burger.service.CartService;
import com.ezen.burger.service.ProductService;

@Controller
public class ProductController {
	@Resource(name="ProductService")
	ProductService ps;
	
	@Resource(name="CartService")
	CartService cs;
	
	
	@RequestMapping(value="menuListForm.do")
	public String menuListForm(Model model, @RequestParam("kind1") String kind1) {
		
		HashMap<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("kind1", kind1);
		paramMap.put("ref_cursor", null);
		
		ps.b_getProduct(paramMap);
		ArrayList<HashMap<String, Object>> list
		= (ArrayList<HashMap<String, Object>>)paramMap.get("ref_cursor");
		
		model.addAttribute("kind1", kind1);
		model.addAttribute("productList",list);
		return "product/menuList";
	}
	
	
	@RequestMapping(value="menudetailForm.do")
	public String menudetailForm(Model model, @RequestParam("pseq") int pseq) {
		HashMap<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("pseq", pseq);
		paramMap.put("ref_cursor", null);
		
		ps.getDeliverydetail(paramMap);
		ArrayList<HashMap<String, Object>> list 
		= (ArrayList<HashMap<String, Object>>)paramMap.get("ref_cursor");
		HashMap<String, Object> resultMap = list.get(0);
		
		HashMap<String, Object> paramMap2 = new HashMap<String, Object>();
		// 불러온 목록들을 전송
		paramMap2.put("kind1", list.get(0).get("KIND1"));
		paramMap2.put("kind2", list.get(0).get("KIND2"));
		ps.getProductkind(paramMap2);
		ArrayList<HashMap<String, Object>> list2 = (ArrayList<HashMap<String, Object>>)paramMap2.get("ref_cursor");
		
		model.addAttribute("productVO", list.get(0));
		model.addAttribute("list2", list2);
		
		return "product/productDetail";
	}
	
	@RequestMapping(value="/deliveryDetail.do")
	public String deliveryDetail(HttpServletRequest request, Model model,
			@RequestParam("pseq") int pseq) {
		ModelAndView mav = new ModelAndView();
		HttpSession session = request.getSession();
		if(session.getAttribute("memberkind") == null) {
			return "redirect:/loginForm.do";
		}
		// 해당 물품번호의 상위 목록을 불러오기 (해당 pseq의 kind3=4인 값을 불러온다.)
		HashMap<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("pseq", pseq);
		paramMap.put("ref_cursor", null);
		
		ps.getDeliverydetail(paramMap);
		
		ArrayList<HashMap<String, Object>> pvo = (ArrayList<HashMap<String, Object>>)paramMap.get("ref_cursor");
		// 이후 해당 상위 목록의 세부 품목들(kind3=1~3)을 불러온다.
		HashMap<String, Object> paramMap2 = new HashMap<String, Object>();
		paramMap2.put("kind1", pvo.get(0).get("KIND1"));
		paramMap2.put("kind2", pvo.get(0).get("KIND2"));
		paramMap2.put("ref_cursor", null);
		
		ps.getProductkind(paramMap2);
		
		ArrayList<HashMap<String, Object>> list1 = (ArrayList<HashMap<String, Object>>)paramMap2.get("ref_cursor");
		// 불러온 목록들을 전송
		model.addAttribute("list1", list1);
		model.addAttribute("productVO", pvo.get(0));
		return "delivery/deliveryDetail";
	}
	
	@RequestMapping(value="/deliveryAddMaterial")
	public String deliveryAddMaterial(HttpServletRequest request, Model model,
			@RequestParam("pseq") int pseq) {
		HttpSession session = request.getSession();
		
		if(session.getAttribute("loginUser") == null) {
			return "redirect:/loginForm.do";
		}
		
		// 추가할 재료 목록
		HashMap<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("ref_cursor", null);
		
		ps.getSubProduct(paramMap);
		
		ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>)paramMap.get("ref_cursor");
		
		
		model.addAttribute("subProductVO", list);
		model.addAttribute("pseq", pseq);
		return "delivery/addMeterial";
	}
}

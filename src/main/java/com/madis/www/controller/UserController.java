package com.madis.www.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.madis.www.model.dao.ForStaticDao;
import com.madis.www.model.dao.impl.CusumImpl;
import com.madis.www.model.dao.impl.MenuImpl;
import com.madis.www.model.dao.impl.ReverImpl;
import com.madis.www.model.dao.impl.UserDaoImpl;
import com.madis.www.model.dto.Cusum;
import com.madis.www.model.dto.Menu;
import com.madis.www.model.dto.UserInfo;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private	MenuImpl menuImpl;
	
	@Autowired
	private UserDaoImpl userImpl;

	@Autowired
	private ReverImpl reverImpl;
	
	@Autowired
	private	CusumImpl cusumImpl;

	@RequestMapping(value = { "/reservation/reserveInfo" })
	public ModelAndView reserveInfo() {
		System.out.println("reserveInfo");

		ModelAndView mav = new ModelAndView();

		SecurityContext context = SecurityContextHolder.getContext();
		Authentication authentication = context.getAuthentication();

		System.out.println("authentication.getName():" + authentication.getName() + "----");
		String email = authentication.getName();
		UserInfo user = userImpl.getUser(email);
		System.out.println("user.getNo(): " + user.getNo());

		Cusum cusum = new Cusum();
		cusum.setIndex(user.getNo());
		// cusum.setUser_id(user.getNo());

		List<Cusum> info = reverImpl.getReverListByUser(cusum);

		mav.addObject("product", info);

		return mav;
	}

	@RequestMapping(value = { "/statistic/statisticInfo" })
	public String statisticInfo(String month, Model model) {
		System.out.println("statisticInfo");
		return "user/statistic/statisticInfo";
	}
	
	// 월별정보 가져오기
	@RequestMapping(value = { "/statistic/getMonth" })
	public @ResponseBody Map<String, Object> genMonth(Cusum cusum, String month) {
		System.out.println("/statistic/statisticInfo/getMonth");
		
		List<ForStaticDao> static_list = cusumImpl.getMonthByUser(cusum,month);
		List<Menu> menu_list = new ArrayList<Menu>();
		System.out.println(static_list.size());
		for(int i=0; i<static_list.size(); i++) {
			Menu menu = new Menu();
			menu.setIndex(static_list.get(i).getMenu_id());
			menu = menuImpl.getMenu(menu);
			menu_list.add(menu);
		}
		System.out.println("/statistic/statisticInfo/getMonth");
		
		int isdata = 0;
		if (static_list.size() != 0) {
			isdata = 1;
		}
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("static_list", static_list);
		resultMap.put("menu_list", menu_list);
		resultMap.put("isdata", isdata);
		
		return resultMap;
	}
	
	@RequestMapping(value = { "/reservation/insert" })
	public @ResponseBody Map<String, Object> reservation(Cusum cusum) {
		System.out.println("/reservation/insert");
		
		System.out.println(cusum.getUser_id());
		System.out.println(cusum.getMenu_id());
		
		reverImpl.insertRever(cusum);
		
		System.out.println("success");
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("result", 1);
		
		return resultMap;
	}
	
	@RequestMapping(value = { "/reservation/deleteReady" })
	public @ResponseBody Map<String, Object> deletesReady(Cusum cusum) {

		System.out.println("dddde");


		reverImpl.deleteRever(cusum);

		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("result", 1);

		return resultMap;
	}
}
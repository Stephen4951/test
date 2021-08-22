package com.spring.mvc.user.controller;

import java.util.Date;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;

import com.spring.mvc.user.model.UserVO;
import com.spring.mvc.user.service.IUserService;

@RestController
@RequestMapping("/user")
public class UserController {
	
	public UserController() {
		System.out.println(System.getProperties());
	}

	@Autowired
	private IUserService service;
	
	//회원 가입 요청 처리
	//Rest-api에서 Insert -> Post
	@PostMapping("/")
	public String register(@RequestBody UserVO user) {
		System.out.println("/user/ POST 요청 발생!");
		System.out.println("param: " + user);
		
		service.register(user);
		return "joinSuccess";
	}
	
	//아이디 중복 확인 요청 처리
	@PostMapping("/checkId")
	public String checkId(@RequestBody String account) {
		
		System.out.println("/user/checkId: POST 요청 발생!");
		System.out.println("parameter: " + account);
		String result = null;
		
		int checkNum = service.checkId(account);
		if(checkNum == 1) {
			System.out.println("아이디가 중복됨!");
			result = "NO";
		} else {
			System.out.println("아이디 사용 가능!");
			result = "OK";
		}

		return result;
	}
	
	//로그인 요청 처리
	@PostMapping("/loginCheck")
	public String loginCheck(@RequestBody UserVO inputData
										, HttpSession session
										/*HttpServletRequest request*/
										, HttpServletResponse response) {
		
		//서버에서 세션객체를 얻는 방법.
		//1. HttpServletRequest객체 사용
//		HttpSession session = request.getSession();
		
		
		
		/*
		 # 클라이언트가 전송한 id값과 pw값을 가지고 DB에서 회원의 정보를
		  조회해서 불러온 다음 값 비교를 통해
		  1. 아이디가 없을 경우 클라이언트 쪽으로 문자열 "idFail" 전송.
		  2. 비밀번호가 틀렸을 경우 문자열 "pwFail" 전송.
		  3. 로그인 성공 시 문자열 "loginSuccess" 전송.
		 */
		
		String result = null;
		
		System.out.println("/user/loginCheck요청!: POST");
		System.out.println("Parameter: " + inputData);
		
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		UserVO dbData = service.selectOne(inputData.getAccount());
		
		if(dbData != null) {
			if(encoder.matches(inputData.getPassword(), dbData.getPassword())) {
				session.setAttribute("login", dbData);
				result = "loginSuccess";
				
				long limitTime = 60 * 60 * 24 * 90;
				
				//자동 로그인 체크시 처리해야 할 내용.
				if(inputData.isAutoLogin()) {
					System.out.println("자동 로그인 쿠키 생성 중...");
					Cookie loginCookie = new Cookie("loginCookie", session.getId());
					loginCookie.setPath("/");
					loginCookie.setMaxAge((int)limitTime);
					response.addCookie(loginCookie);
					
					//자동로그인 유지시간을 날짜 객체로 변환
					long expiredDate = System.currentTimeMillis() + (limitTime * 1000);
					//Date객체의 생성자에 매개값으로 밀리초의 시간을 전달하면 날짜로 변환해 줍니다.
					Date limitDate = new Date(expiredDate);
					
					service.keepLogin(session.getId(), limitDate, inputData.getAccount());
					
				}
				
				
			} else {
				result = "pwFail";
			}
		} else {
			result = "idFail";
		}
		return result;
	}
	
	//로그아웃 요청 처리
	@GetMapping("/logout")
	public ModelAndView logout(HttpSession session
								, HttpServletRequest request
								, HttpServletResponse response) {
		
		System.out.println("/user/logout 요청!");
		
		UserVO user = (UserVO) session.getAttribute("login");
		
		if(user != null) {
			session.removeAttribute("login");
			session.invalidate();
			
			Cookie loginCookie = WebUtils.getCookie(request, "loginCookie");
			if(loginCookie != null) {
				loginCookie.setMaxAge(0);
				response.addCookie(loginCookie);
				service.keepLogin("none", new Date(), user.getAccount());
			}
			
			
		}
		
		return new ModelAndView("redirect:/");
	}
	
	
	
	
	
	//회원 탈퇴 요청 처리
//	@RequestMapping(value="/", method=RequestMethod.DELETE)
	@DeleteMapping("/{account}")
	public String delete(@PathVariable String account) {
		System.out.println("/user/" + account + ": DELETE 요청 발생!");
		
		service.delete(account);
		return "delSuccess";
	}
	
	//회원 정보 조회 요청 처리
	@GetMapping("/{account}")
	public UserVO selectOne(@PathVariable String account) {
		System.out.println("/user/" + account + ": GET요청 발생!");
		
		return service.selectOne(account);
	}
	
	
	//회원정보 전체조회 요청 처리 
	@GetMapping("/")
	public List<UserVO> selectOne() {
		System.out.println("/user/ : GET요청 발생!");
		return service.selectAll();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}

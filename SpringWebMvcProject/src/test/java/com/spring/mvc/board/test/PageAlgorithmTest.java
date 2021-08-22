package com.spring.mvc.board.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.spring.mvc.board.repository.IBoardMapper;
import com.spring.mvc.commons.PageVO;
import com.spring.mvc.commons.SearchVO;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations= {"file:src/main/webapp/WEB-INF/spring/mvc-config.xml"})
public class PageAlgorithmTest {
	
	/*
	  *** 페이징 알고리즘 만들기 ***
	  
	  # 1. 사용자가 보게 될 페이지 화면
	  - 한 화면에 페이지를 10개씩 끊어서 보여준다면?
	  ex) 1 2 3 4 .... 9 10 [다음] // [이전] 31 32 33 34... 39 40 [다음]
	  
	  - 만약에 총 게시물의 갯수가 67개라면??
	  1 2 3 4 5 6 7 
	  
	  - 총 게시물 수가 142개이고 현재 12페이지에 사용자가 머물러 있다면???
	  [이전] 11 12 13 14 15 
	  
	  # 2. 우선 총 게시물의 수를 조회해야 합니다.
	  - 총 게시물 수는 DB로부터 수를 조회하는 SQL문을 작성.
	  
	  # 3. 사용자가 현재 위치한 페이지를 기준으로
	   끝 페이지 번호를 계산하는 로직 작성.
	   
	  - 만약 현재 사용자가 보고 있는 페이지가 3페이지고, 
	   한 화면에 보여줄 페이지가 10페이지씩이라면???
	   -> 끝 페이지 번호?? 10페이지
	  - 만약 현재 페이지가 36페이지고 한 화면에 보여줄 페이지 수가
	   20 페이지라면???
	   -> 끝 페이지 번호: 40페이지
	   
	   공식: Math.ceil(현재 위치한 페이지 번호 / 한 화면 당 보여질 페이지 수) 
	   			* 한 화면당 보여질 페이지 수
	   
	   # 4. 시작 페이지 번호 계산하기 
	   - 현재 위치한 페이지가 15페이지고, 한 화면에 보여줄 페이지가 10페이지씩이라면???
	    -> 시작 페이지 번호는: 11번
	    
	   - 현재 위치한 페이지가 73페이지고, 한 화면에 20페이지씩 보여준다면??
	   -> 시작페이지 번호: 61번
	   
	    공식: (끝 페이지 번호 - 한 화면에 보여줄 페이지의 수) + 1
	   
	   #5. 끝 페이지 보정 
	   
	   - 총 게시물 수가 324개이고 한 페이지당 10개의 게시물을 보여준다.
	   - 그리고 이사람은 31페이지를 현재 보고 있다.
	   - 그리고 한 화면에 게시물은 10개가 배치된다.
	   - 그렇다면 위 공식에 의한 끝 페이지는 몇 번으로 계산되는가 -> 40번
	   - 하지만 실제 끝 페이지는 몇 번에서 끝나는가? 33번
	   
	   # 5-1. 이전 버튼 활성 여부 설정.
	   - 언제 이전버튼을 비활성화 할 것인가 -> 시작페이지가 1인 부분에서 비활성.
	     공식: 시작페이지 번호가 1로 구해진 시점에서 비활성 처리. 나머지는 활성.
	     
	   # 5-2. 다음 버튼 활성 여부 설정.
	   - 언제 다음 버튼을 비활성화 할 것인가??
	   - 공식: 보정 전 끝 페이지 번호 x 한 페이지에 들어갈 게시물 수 >= 총 게시물 수 -> 비활성.
	   
	   # 5-3. 끝 페이지 값 보정
	   - 다음 버튼이 비활성화 되었을 때 총 게시물 수에 맞춰 끝 페이지 번호를
	    재 보정 한다.
	   - 공식: Math.ceil(총 게시물 수 / 한 페이지에 보여줄 게시물 수)
	  
	 */

	
	
	@Autowired
	private IBoardMapper mapper;
	
	@Test
	public void pagingAlgorithmTest() {
		
		//총 게시물 구하는 수 테스트
		System.out.println("---------------------------");
		//System.out.println("# 총 게시물 수: " + mapper.countArticles());
		System.out.println("---------------------------");
		
		//끝 페이지 번호 계산 테스트
		PageVO paging = new PageVO();
		paging.setPage(26);
		int displayPage = 10;
		
		int endPage = (int) Math.ceil(paging.getPage() / (double) displayPage) 
													* displayPage;
		
		System.out.println("끝 페이지 번호: " + endPage + "번");
		
		//시작페이지 번호 계산 테스트
		int beginPage = (endPage - displayPage) + 1;
		System.out.println("시작 페이지 번호: " + beginPage + "번");
		
		//이전 버튼 활성, 비활성 여부.
		boolean isPrev = (beginPage == 1) ? false : true;
		System.out.println("이전 버튼 활성화 여부: " + isPrev);
		
		//다음 버튼 활성, 비활성 여부.
		//boolean isNext = (mapper.countArticles() <= (endPage * paging.getCountPerPage())) ? false : true;
		//System.out.println("다음 버튼 활성화 여부: " + isNext);
		
		//끝 페이지 보정.
//		if(!isNext) {
//			endPage = (int) Math.ceil(mapper.countArticles() / (double) paging.getCountPerPage());
//		}
//		System.out.println("보정 후 끝 페이지 번호: " + endPage + "번");
		
		System.out.println("------------------------------------------------------");
		
	}
	
	@Test
	public void searchTest() {
		
		SearchVO search = new SearchVO();
		search.setPage(4);
		search.setKeyword("9");
		
		System.out.println("---------------------------");
//		mapper.getArticleListByTitle(search)
//			.forEach(vo -> System.out.println(vo));
		System.out.println("---------------------------");
		
	}
	
	
	
	
	
	
	
	
	
	

}

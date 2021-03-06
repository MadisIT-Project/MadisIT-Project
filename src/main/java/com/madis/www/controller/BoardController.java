package com.madis.www.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.madis.www.model.dao.impl.BoardImpl;
import com.madis.www.model.dao.impl.CommentImpl;
import com.madis.www.model.dao.impl.UserDaoImpl;
import com.madis.www.model.dto.Board;
import com.madis.www.model.dto.Comment;
import com.madis.www.model.dto.UserInfo;

@Controller
@RequestMapping("/board")
public class BoardController {
	
	@Autowired
	private	UserDaoImpl userImpl;
	
	@Autowired
	private BoardImpl boardImpl;
	
	@Autowired
	private CommentImpl commentImpl;
	
	// 글 쓰기 페이지 출력 (.jsp 출력)
	@RequestMapping(value="/write")
	public String writeBoard(Board board, Model model) {
		System.out.println("write");
		if (board.getIndex() != 0) {
			System.out.println("writeBoard1");
			model.addAttribute("board", boardImpl.getBoard(board.getIndex()));
		}
		else {
			System.out.println("writeBoard2");
			model.addAttribute("board", board);
		}
		// Model 정보 저장
		return "board/writeBoard";
	}
	
	// 글 등록
	@RequestMapping(value="/insert")
	public String insertBoard(Board board) throws IOException {
		System.out.println("insertBoard");
		System.out.println(board.getTitle());
		
		// 시큐리티 컨텍스트 객체를 얻습니다.
		SecurityContext context = SecurityContextHolder.getContext();

		// 인증 객체를 얻습니다.
		Authentication authentication = context.getAuthentication();
		
		if (!(authentication instanceof AnonymousAuthenticationToken)) {
			UserInfo user = userImpl.getUser(authentication.getName());
			board.setU_index(user.getNo());
		}
		
		/*
		// 파일 업로드 처리
		MultipartFile uploadFile = vo.getUploadFile();
		if(!uploadFile.isEmpty()) {
			String fileName = uploadFile.getOriginalFilename();
			uploadFile.transferTo(new File("/Users/harrimkim/" + fileName));
		}
		*/
		boardImpl.insertBoard(board);
		return "redirect:/board";
	}
	
	// 글 수정
	@RequestMapping("/{index}/update")
	public String updateBoard(@PathVariable int index, Board board) {
		System.out.println("updateBoard");
		boardImpl.updateBoard(board);
		return "redirect:/board";
	}
	
	// 글 삭제
	@RequestMapping("/{index}/delete")
	public @ResponseBody Map<String, Object> deleteBoard(@PathVariable int index) {
		System.out.println("board/{index}/delete");

		// 글 삭제 이전에 comment 삭제
		commentImpl.deleteAllComment(index);
		
		boardImpl.deleteBoard(index);
		System.out.println("success");
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("result", 1);
		
		return resultMap;
	}

	// 글 상세 조회 (.jsp 출력)
	@RequestMapping("/{index}")
	public String getBoard(@PathVariable int index, Model model) {
		
		Board board = boardImpl.getBoard(index);
		boolean isWriter = false;
		
		// 시큐리티 컨텍스트 객체를 얻습니다.
		SecurityContext context = SecurityContextHolder.getContext();

		// 인증 객체를 얻습니다.
		Authentication authentication = context.getAuthentication();
		
		if (!(authentication instanceof AnonymousAuthenticationToken)) {
			UserInfo user = userImpl.getUser(authentication.getName());
			if (board.getU_index() == user.getNo()) {
				isWriter = true;
			}
		}
		
		List<Comment> commentList = commentImpl.getCommentList(index);
		List<String> commentUserList = new ArrayList<String>();
		List<Boolean> isWriterListForCom = new ArrayList<Boolean>();
		for(int i=0; i<commentList.size();i++) {
			System.out.println(commentList.get(i).getU_id());
			// 댓글 작성자 이름 정보 저장
			UserInfo user = userImpl.getUser2(commentList.get(i).getU_id());
			System.out.println("a3");
			String name = user.getName();
			System.out.println("a3");
			commentUserList.add(name);
			
			isWriter = false;
			System.out.println("a3");
			if (!(authentication instanceof AnonymousAuthenticationToken)) {
				// 댓글 작성자가 현재 로그인 된 사람이 맞는지 저장
				user = userImpl.getUser(authentication.getName());
				if (commentList.get(i).getU_id() == user.getNo()) {
					isWriter = true;
				}
			}
			isWriterListForCom.add(isWriter);
			System.out.println("a4");
		}
		System.out.println("aa2");
		
		// Model 정보 저장
		model.addAttribute("board", boardImpl.getBoard(index));
		model.addAttribute("isWriter",isWriter);
		model.addAttribute("CommentList", commentList);
		model.addAttribute("CommentUserList", commentUserList);
		model.addAttribute("CommentIsWriterList", isWriterListForCom);
		return "board/getBoard";
	}
	
	// 글 목록 검색 (.jsp 출력)
	@RequestMapping("")
	public String getBoardList(Board board, Model model){
		System.out.println("getBoardList");
		
		List<Board> boardList = boardImpl.getBoardList(board);
		List<Integer> commentCount = new ArrayList<Integer>();
		for(int i=0; i<boardList.size();i++) {
			Integer count = commentImpl.getCommentList(boardList.get(i).getIndex()).size();
			commentCount.add(count);
		}
		
		List<String> userList = new ArrayList<String>();
		for(int i=0; i<boardList.size();i++) {
			UserInfo user = userImpl.getUser2(boardList.get(i).getU_index());
			String name = user.getName();
			userList.add(name);
		}
		
		// Model 정보 저장
		model.addAttribute("boardList", boardImpl.getBoardList(board));
		model.addAttribute("CommentCountList", commentCount);
		model.addAttribute("UserList", userList);
		return "board/getBoardList";
	}
}

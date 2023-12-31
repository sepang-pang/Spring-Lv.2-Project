package com.sparta.learnspring.service;

import com.sparta.learnspring.dto.MsgDto;
import com.sparta.learnspring.dto.RequestDto;
import com.sparta.learnspring.dto.ResponseDto;
import com.sparta.learnspring.entity.Post;
import com.sparta.learnspring.jwt.JwtUtil;
import com.sparta.learnspring.repoistory.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;
@Slf4j(topic = "PostService 로그")
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final JwtUtil jwtUtil;

    // 입력
    public ResponseDto createPost(RequestDto requestDto, Principal principal) {

            // 제이슨 - > 엔티티화
            Post post = new Post(requestDto, principal);

            // DB 저장
            postRepository.save(post);

            // postResponse 로 저장
            ResponseDto responseDto = new ResponseDto(post);
            log.info("게시글 작성 성공");
            return responseDto;


    }

    // 조회
    public List<ResponseDto> displayPost() {
        log.info("전체 글 조회");
        return postRepository.findAllByOrderByModifiedAtDesc().stream().map(ResponseDto::new).toList();
    }

    // 선택 조회
    public List<ResponseDto> selectDisplayPost(String username) {
        log.info(username + " 의 글을 조회");
        return postRepository.findAllByUsername(username).stream().map(ResponseDto::new).toList();
    }


    @Transactional
    public List<ResponseDto> updatePost(Long id, RequestDto requestDto, Principal principal) {
        // 해당 메모가 존재하는지 확인 // Optional
        Post post = findPost(id);

        // 사용자 확인
        if (post.getUsername().equals(principal.getName())) {
            post.update(requestDto);
            log.info("게시글 수정 성공");
            return postRepository.findById(id).stream().map(ResponseDto::new).toList();
        } else {
            log.info("게시글 수정 실패 : 작성자가 아닙니다.");
            return null;
        }

    }

    public MsgDto deletePost(Long id, RequestDto requestDto, Principal principal) {
        // 해당 게시글이 존재하지는지 확인
        Post post = findPost(id);

        // 사용자 확인
        if (post.getUsername().equals(principal.getName())) {
            // 게시글 삭제
            postRepository.delete(post);
            log.info("게시글 삭제 성공");
            return new MsgDto("게시글 삭제 성공", HttpStatus.OK.value());
        } else {
            log.info("게시글 삭제 실패");
            return new MsgDto("게시글 삭제 실패", HttpStatus.BAD_REQUEST.value());
        }

    }

    private Post findPost(Long id) {
        return postRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("선택한 메모는 존재하지 않습니다.")
        );
    }
}

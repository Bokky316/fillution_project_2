package com.javalab.student.controller.board;

import com.javalab.student.dto.PageRequestDTO;
import com.javalab.student.dto.PageResponseDTO;
import com.javalab.student.dto.board.PostDto;
import com.javalab.student.dto.board.PostNeighborsDTO;
import com.javalab.student.service.board.PostService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@Slf4j
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    // 게시글 등록
    @PostMapping
    public ResponseEntity<PostDto> createPost(@RequestBody @Valid PostDto postDto) {
        return ResponseEntity.ok(postService.createPost(postDto));
    }

    // 게시글 수정
    @PutMapping("/{id}")
    public ResponseEntity<PostDto> updatePost(@PathVariable("id") Long id, @RequestBody @Valid PostDto postDto) {
        return ResponseEntity.ok(postService.updatePost(id, postDto));
    }

    // 게시글 정보 조회
    @GetMapping("/{id}")
    public ResponseEntity<PostDto> getPostById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    // 페이징 처리된 게시글 목록 조회 (게시판별)
    @GetMapping("/board/{boardId}")
    public ResponseEntity<PageResponseDTO<PostDto>> getPostsByBoard(
            @PathVariable("boardId") Long boardId,
            @ModelAttribute PageRequestDTO pageRequestDTO) {

        PageResponseDTO<PostDto> responseDTO = postService.getPostsByBoard(boardId, pageRequestDTO);
        return ResponseEntity.ok(responseDTO);
    }

    // 게시글 검색
    @GetMapping("/search")
    public ResponseEntity<PageResponseDTO<PostDto>> searchPosts(@ModelAttribute PageRequestDTO pageRequestDTO) {
        log.info("검색 키워드 : " + pageRequestDTO.getKeyword() + ", 페이지 : " + pageRequestDTO.getPage() + " " + pageRequestDTO.getSize());

        PageResponseDTO<PostDto> responseDTO = postService.searchPosts(pageRequestDTO);
        return ResponseEntity.ok(responseDTO);
    }

    // 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable("id") Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
    // 게시글 삭제 (FAQ 게시글)
    @DeleteMapping("/faq/{id}")
    public ResponseEntity<Void> deleteFaqPost(@PathVariable("id") Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    // FAQ 게시글 조회 (board_id = 2인 게시글만 조회)
    @GetMapping("/faq")
    public ResponseEntity<List<PostDto>> getFAQPosts() {
        Long faqBoardId = 2L;  // FAQ 게시판 ID
        List<PostDto> faqPosts = postService.getPostsByBoardId(faqBoardId);
        return ResponseEntity.ok(faqPosts);
    }

    @GetMapping("/{id}/neighbors")
    public ResponseEntity<PostNeighborsDTO> getPostNeighbors(@PathVariable("id") Long id) {
        PostNeighborsDTO neighbors = postService.getPostNeighbors(id);
        return ResponseEntity.ok(neighbors);
    }
}

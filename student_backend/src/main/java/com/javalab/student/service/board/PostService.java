package com.javalab.student.service.board;

import com.javalab.student.dto.PageRequestDTO;
import com.javalab.student.dto.PageResponseDTO;
import com.javalab.student.dto.board.PostDto;
import com.javalab.student.dto.board.PostNeighborsDTO;
import com.javalab.student.entity.board.Board;
import com.javalab.student.entity.board.Post;
import com.javalab.student.exception.UnauthorizedException;
import com.javalab.student.repository.board.BoardRepository;
import com.javalab.student.repository.board.PostRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final BoardRepository boardRepository;

    public PostDto createPost(PostDto postDto) {
        Board board = boardRepository.findById(postDto.getBoardId())
                .orElseThrow(() -> new EntityNotFoundException("해당 게시판이 존재하지 않습니다."));

        Post post = new Post();
        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        post.setBoard(board);
        post.setAuthorId(postDto.getAuthorId());

        // 수정된 부분 -> 프론트엔드에서 받은 카테고리로 설정
        post.setCategory(postDto.getCategory());

        validatePost(post);

        Post savedPost = postRepository.save(post);
        return PostDto.fromEntity(savedPost);
    }

    public PostDto updatePost(Long postId, PostDto postDto) {
        Post existingPost = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("해당 게시글이 존재하지 않습니다."));

        if (!existingPost.getAuthorId().equals(postDto.getAuthorId())) {
            throw new UnauthorizedException("게시글 수정 권한이 없습니다.");
        }

        existingPost.setTitle(postDto.getTitle());
        existingPost.setContent(postDto.getContent());
        existingPost.setCategory(postDto.getCategory());

        validatePost(existingPost);

        Post updatedPost = postRepository.save(existingPost);
        return PostDto.fromEntity(updatedPost);
    }

    public PostDto getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 게시글이 존재하지 않습니다."));
        PostDto dto = PostDto.fromEntity(post);
        System.out.println("Category: " + dto.getCategory()); // category 값을 출력해 봅니다.
        return dto;
    }

    public PageResponseDTO<PostDto> getPostsByBoard(Long boardId, PageRequestDTO pageRequestDTO) {
        Pageable pageable = pageRequestDTO.getPageable("id");
        Page<Post> result = postRepository.findByBoardId(boardId, pageable);
        List<PostDto> dtoList = result.getContent().stream()
                .map(PostDto::fromEntity)
                .collect(Collectors.toList());

        return PageResponseDTO.<PostDto>builder()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total((int) result.getTotalElements())
                .build();
    }

    public PageResponseDTO<PostDto> searchPosts(PageRequestDTO pageRequestDTO) {
        Pageable pageable = pageRequestDTO.getPageable("id");

        // String[] 타입의 검색 조건(type)을 String으로 변환
        String types = pageRequestDTO.getTypes() != null ? String.join("", pageRequestDTO.getTypes()) : null;

        Page<Post> result = postRepository.searchByMultipleFields(types, pageRequestDTO.getKeyword(), pageable);

        List<PostDto> dtoList = result.getContent().stream()
                .map(PostDto::fromEntity)
                .collect(Collectors.toList());

        return PageResponseDTO.<PostDto>builder()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total((int) result.getTotalElements())
                .build();
    }

    public void deletePost(Long id) {
        if (!postRepository.existsById(id)) {
            throw new EntityNotFoundException("해당 게시글이 존재하지 않습니다.");
        }
        postRepository.deleteById(id);
    }

    private void validatePost(Post post) {
        if (StringUtils.isEmpty(post.getTitle())) {
            throw new IllegalArgumentException("제목은 필수 입력 항목입니다.");
        }

        if (post.getTitle().length() > 255) {
            throw new IllegalArgumentException("제목은 255자를 초과할 수 없습니다.");
        }

        post.setTitle(sanitizeInput(post.getTitle()));
        post.setContent(sanitizeInput(post.getContent()));
    }

    private String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("<[^>]*>", "");
    }

    public List<PostDto> getPostsByBoardId(Long boardId) {
        List<Post> posts = postRepository.findByBoardId(boardId);
        return posts.stream()
                .map(PostDto::fromEntity)
                .collect(Collectors.toList());
    }

    //
    public PostNeighborsDTO getPostNeighbors(Long postId) {
        PostNeighborsDTO neighbors = new PostNeighborsDTO();

        // 현재 게시물의 이전/다음 게시물 ID를 DB에서 조회하여 설정
        List<Post> posts = postRepository.findAllByOrderByCreatedAtAsc();
        int currentIndex = posts.indexOf(posts.stream().filter(p -> p.getId().equals(postId)).findFirst().orElse(null));

        if (currentIndex > 0) {
            neighbors.setPrevPostId(posts.get(currentIndex - 1).getId());
        }
        if (currentIndex < posts.size() - 1) {
            neighbors.setNextPostId(posts.get(currentIndex + 1).getId());
        }

        return neighbors;
    }
}

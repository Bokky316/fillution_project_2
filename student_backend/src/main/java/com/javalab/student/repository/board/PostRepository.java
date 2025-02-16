package com.javalab.student.repository.board;

import com.javalab.student.entity.board.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByBoardId(Long boardId, Pageable pageable);
    List<Post> findByBoardId(Long boardId);
    List<Post> findAllByOrderByCreatedAtAsc();

    @Query("SELECT p FROM Post p WHERE " +
            "(:types IS NULL OR " +
            "(:types LIKE '%t%' AND p.title LIKE %:keyword%) OR " +
            "(:types LIKE '%c%' AND p.content LIKE %:keyword%) OR " +
            "(:types LIKE '%w%' AND CAST(p.authorId AS string) LIKE %:keyword%))")
    Page<Post> searchByMultipleFields(@Param("types") String types,
                                      @Param("keyword") String keyword,
                                      Pageable pageable);
}

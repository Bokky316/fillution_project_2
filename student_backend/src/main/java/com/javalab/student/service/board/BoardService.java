package com.javalab.student.service.board;

import com.javalab.student.dto.board.BoardDto;
import com.javalab.student.entity.board.Board;
import com.javalab.student.exception.EntityNotFoundException;
import com.javalab.student.repository.board.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;

    public List<BoardDto> getAllBoards() {
        return boardRepository.findAll().stream()
                .map(board -> {
                    BoardDto dto = new BoardDto();
                    dto.setId(board.getId());
                    dto.setName(board.getName());
                    dto.setDescription(board.getDescription());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public BoardDto getBoard(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("게시판을 찾을 수 없습니다."));

        BoardDto dto = new BoardDto();
        dto.setId(board.getId());
        dto.setName(board.getName());
        dto.setDescription(board.getDescription());
        return dto;
    }
}

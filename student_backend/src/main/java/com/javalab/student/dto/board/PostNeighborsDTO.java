package com.javalab.student.dto.board;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostNeighborsDTO {
    private Long prevPostId;
    private Long nextPostId;
}

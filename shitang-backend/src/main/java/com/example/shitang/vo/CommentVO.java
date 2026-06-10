package com.example.shitang.vo;

import com.example.shitang.entity.Comment;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CommentVO {
    private Long id;
    private Long userId;
    private String username;
    private String realName;
    private Long dishId;
    private String dishName;
    private String content;
    private BigDecimal rating;
    private String reply;
    private Integer status;
    private LocalDateTime createTime;

    public static CommentVO from(Comment comment) {
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setUserId(comment.getUserId());
        vo.setDishId(comment.getDishId());
        vo.setContent(comment.getContent());
        vo.setRating(comment.getRating());
        vo.setReply(comment.getReply());
        vo.setStatus(comment.getStatus());
        vo.setCreateTime(comment.getCreateTime());
        return vo;
    }
}

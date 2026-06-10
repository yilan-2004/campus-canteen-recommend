package com.example.shitang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.shitang.dto.CommentRequest;
import com.example.shitang.entity.Comment;
import com.example.shitang.vo.CommentVO;

import java.util.List;

public interface CommentService extends IService<Comment> {
    CommentVO createComment(CommentRequest request);
    List<CommentVO> listByDish(Long dishId);
    List<CommentVO> listMine();
    List<CommentVO> listAll(String keyword, Integer status);
    CommentVO replyToComment(Long commentId, String reply);
}

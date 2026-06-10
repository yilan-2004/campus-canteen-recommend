package com.example.shitang.controller;

import com.example.shitang.common.Result;
import com.example.shitang.dto.CommentRequest;
import com.example.shitang.service.CommentService;
import com.example.shitang.vo.CommentVO;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public Result<CommentVO> create(@Valid @RequestBody CommentRequest request) {
        return Result.success(commentService.createComment(request));
    }

    @GetMapping("/dish/{dishId}")
    public Result<List<CommentVO>> listByDish(@PathVariable Long dishId) {
        return Result.success(commentService.listByDish(dishId));
    }

    @GetMapping("/my")
    public Result<List<CommentVO>> my() {
        return Result.success(commentService.listMine());
    }

    @GetMapping("/admin")
    public Result<List<CommentVO>> adminList(@RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) Integer status) {
        return Result.success(commentService.listAll(keyword, status));
    }

    @PutMapping("/{id}/reply")
    public Result<CommentVO> reply(@PathVariable Long id, @RequestBody ReplyRequest request) {
        return Result.success(commentService.replyToComment(id, request.getReply()));
    }

    @Data
    public static class ReplyRequest {
        private String reply;
    }
}

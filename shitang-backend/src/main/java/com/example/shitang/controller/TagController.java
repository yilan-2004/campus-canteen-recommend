package com.example.shitang.controller;

import com.example.shitang.common.Result;
import com.example.shitang.dto.TagRequest;
import com.example.shitang.entity.Tag;
import com.example.shitang.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {
    private final TagService tagService;

    @GetMapping
    public Result<List<Tag>> list(@RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) String type) {
        return Result.success(tagService.listTags(keyword, type));
    }

    @GetMapping("/{id}")
    public Result<Tag> detail(@PathVariable Long id) {
        return Result.success(tagService.getById(id));
    }

    @PostMapping
    public Result<Tag> create(@Valid @RequestBody TagRequest request) {
        return Result.success(tagService.createTag(request));
    }

    @PutMapping("/{id}")
    public Result<Tag> update(@PathVariable Long id, @Valid @RequestBody TagRequest request) {
        return Result.success(tagService.updateTag(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        tagService.deleteTag(id);
        return Result.success();
    }
}

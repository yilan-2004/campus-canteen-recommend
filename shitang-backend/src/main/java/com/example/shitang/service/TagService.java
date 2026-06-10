package com.example.shitang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.shitang.dto.TagRequest;
import com.example.shitang.entity.Tag;

import java.util.List;

public interface TagService extends IService<Tag> {
    List<Tag> listTags(String keyword, String type);
    Tag createTag(TagRequest request);
    Tag updateTag(Long id, TagRequest request);
    void deleteTag(Long id);
}

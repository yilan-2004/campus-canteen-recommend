package com.example.shitang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.shitang.common.BizException;
import com.example.shitang.dto.TagRequest;
import com.example.shitang.entity.DishTag;
import com.example.shitang.entity.Tag;
import com.example.shitang.mapper.DishTagMapper;
import com.example.shitang.mapper.TagMapper;
import com.example.shitang.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {
    private final DishTagMapper dishTagMapper;

    @Override
    public List<Tag> listTags(String keyword, String type) {
        return list(new LambdaQueryWrapper<Tag>()
                .like(StringUtils.hasText(keyword), Tag::getName, keyword)
                .eq(StringUtils.hasText(type), Tag::getType, type)
                .orderByDesc(Tag::getCreateTime));
    }

    @Override
    public Tag createTag(TagRequest request) {
        checkDuplicate(null, request.getName(), request.getType());
        Tag tag = new Tag();
        tag.setName(request.getName());
        tag.setType(request.getType());
        tag.setCreateTime(LocalDateTime.now());
        save(tag);
        return tag;
    }

    @Override
    public Tag updateTag(Long id, TagRequest request) {
        Tag tag = requireTag(id);
        checkDuplicate(id, request.getName(), request.getType());
        tag.setName(request.getName());
        tag.setType(request.getType());
        updateById(tag);
        return tag;
    }

    @Override
    public void deleteTag(Long id) {
        requireTag(id);
        Long usedCount = dishTagMapper.selectCount(new LambdaQueryWrapper<DishTag>().eq(DishTag::getTagId, id));
        if (usedCount != null && usedCount > 0) {
            throw new BizException("标签已被菜品使用，不能删除");
        }
        removeById(id);
    }

    private Tag requireTag(Long id) {
        Tag tag = getById(id);
        if (tag == null) {
            throw new BizException("标签不存在");
        }
        return tag;
    }

    private void checkDuplicate(Long id, String name, String type) {
        Long count = count(new LambdaQueryWrapper<Tag>()
                .eq(Tag::getName, name)
                .eq(Tag::getType, type)
                .ne(id != null, Tag::getId, id));
        if (count != null && count > 0) {
            throw new BizException("同类型标签名称已存在");
        }
    }
}

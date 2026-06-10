package com.example.shitang.vo;

import com.example.shitang.entity.Tag;
import lombok.Data;

@Data
public class TagVO {
    private Long id;
    private String name;
    private String type;

    public static TagVO from(Tag tag) {
        TagVO vo = new TagVO();
        vo.setId(tag.getId());
        vo.setName(tag.getName());
        vo.setType(tag.getType());
        return vo;
    }
}

package com.example.shitang.vo;

import com.example.shitang.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserInfoVO {
    private Long id;
    private String username;
    private String role;
    private String studentNo;
    private String realName;
    private String college;
    private String grade;
    private String tastePreference;
    private Integer status;
    private LocalDateTime createTime;

    public static UserInfoVO from(User user) {
        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRole(user.getRole());
        vo.setStudentNo(user.getStudentNo());
        vo.setRealName(user.getRealName());
        vo.setCollege(user.getCollege());
        vo.setGrade(user.getGrade());
        vo.setTastePreference(user.getTastePreference());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }
}

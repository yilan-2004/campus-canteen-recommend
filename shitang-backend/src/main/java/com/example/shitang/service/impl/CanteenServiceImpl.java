package com.example.shitang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.shitang.common.BizException;
import com.example.shitang.dto.CanteenRequest;
import com.example.shitang.entity.Canteen;
import com.example.shitang.mapper.CanteenMapper;
import com.example.shitang.service.CanteenService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CanteenServiceImpl extends ServiceImpl<CanteenMapper, Canteen> implements CanteenService {

    @Override
    public List<Canteen> listCanteens(String keyword, Integer status) {
        return list(new LambdaQueryWrapper<Canteen>()
                .like(StringUtils.hasText(keyword), Canteen::getName, keyword)
                .eq(status != null, Canteen::getStatus, status)
                .orderByDesc(Canteen::getCreateTime));
    }

    @Override
    public Canteen createCanteen(CanteenRequest request) {
        LocalDateTime now = LocalDateTime.now();
        Canteen canteen = new Canteen();
        canteen.setName(request.getName());
        canteen.setLocation(request.getLocation());
        canteen.setOpenTime(request.getOpenTime());
        canteen.setDescription(request.getDescription());
        canteen.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        canteen.setCreateTime(now);
        canteen.setUpdateTime(now);
        save(canteen);
        return canteen;
    }

    @Override
    public Canteen updateCanteen(Long id, CanteenRequest request) {
        Canteen canteen = requireCanteen(id);
        canteen.setName(request.getName());
        canteen.setLocation(request.getLocation());
        canteen.setOpenTime(request.getOpenTime());
        canteen.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            canteen.setStatus(request.getStatus());
        }
        canteen.setUpdateTime(LocalDateTime.now());
        updateById(canteen);
        return canteen;
    }

    @Override
    public void disableCanteen(Long id) {
        Canteen canteen = requireCanteen(id);
        canteen.setStatus(0);
        canteen.setUpdateTime(LocalDateTime.now());
        updateById(canteen);
    }

    private Canteen requireCanteen(Long id) {
        Canteen canteen = getById(id);
        if (canteen == null) {
            throw new BizException("食堂不存在");
        }
        return canteen;
    }
}

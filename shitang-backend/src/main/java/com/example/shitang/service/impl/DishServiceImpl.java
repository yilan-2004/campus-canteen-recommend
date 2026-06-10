package com.example.shitang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.shitang.common.BizException;
import com.example.shitang.dto.DishRequest;
import com.example.shitang.entity.Canteen;
import com.example.shitang.entity.CanteenWindow;
import com.example.shitang.entity.Dish;
import com.example.shitang.entity.DishCategory;
import com.example.shitang.entity.DishTag;
import com.example.shitang.entity.Tag;
import com.example.shitang.entity.User;
import com.example.shitang.entity.Favorite;
import com.example.shitang.mapper.DishMapper;
import com.example.shitang.mapper.DishTagMapper;
import com.example.shitang.mapper.FavoriteMapper;
import com.example.shitang.service.BehaviorService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.example.shitang.service.CanteenService;
import com.example.shitang.service.CanteenWindowService;
import com.example.shitang.service.DishCategoryService;
import com.example.shitang.service.DishService;
import com.example.shitang.service.TagService;
import com.example.shitang.service.UserService;
import com.example.shitang.vo.DishVO;
import com.example.shitang.vo.TagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    private final CanteenWindowService windowService;
    private final CanteenService canteenService;
    private final DishCategoryService categoryService;
    private final TagService tagService;
    private final DishTagMapper dishTagMapper;
    private final FavoriteMapper favoriteMapper;
    private final BehaviorService behaviorService;
    private final UserService userService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public List<DishVO> listDishes(String keyword, Long canteenId, Long windowId, Long categoryId, Long tagId,
                                   String taste, Integer status, BigDecimal minPrice, BigDecimal maxPrice) {
        // 商户只能看自己窗口的菜品
        Set<Long> merchantWindowIds = getMerchantWindowIds();

        Set<Long> windowIds = null;
        if (merchantWindowIds != null) {
            // 商户模式:限制为自己的窗口
            windowIds = merchantWindowIds;
            if (windowId != null) {
                if (!windowIds.contains(windowId)) return Collections.emptyList();
                windowIds = Set.of(windowId);
            }
        } else if (canteenId != null) {
            windowIds = windowService.list(new LambdaQueryWrapper<CanteenWindow>()
                            .eq(CanteenWindow::getCanteenId, canteenId))
                    .stream().map(CanteenWindow::getId).collect(Collectors.toSet());
            if (windowIds.isEmpty()) {
                return Collections.emptyList();
            }
        }

        Set<Long> dishIdsByTag = null;
        if (tagId != null) {
            dishIdsByTag = dishTagMapper.selectList(new LambdaQueryWrapper<DishTag>().eq(DishTag::getTagId, tagId))
                    .stream().map(DishTag::getDishId).collect(Collectors.toSet());
            if (dishIdsByTag.isEmpty()) {
                return Collections.emptyList();
            }
        }

        List<Dish> dishes = list(new LambdaQueryWrapper<Dish>()
                .like(StringUtils.hasText(keyword), Dish::getName, keyword)
                .in(windowIds != null, Dish::getWindowId, windowIds)
                .eq(windowId != null, Dish::getWindowId, windowId)
                .eq(categoryId != null, Dish::getCategoryId, categoryId)
                .in(dishIdsByTag != null, Dish::getId, dishIdsByTag)
                .like(StringUtils.hasText(taste), Dish::getTaste, taste)
                .eq(status != null, Dish::getStatus, status)
                .ge(minPrice != null, Dish::getPrice, minPrice)
                .le(maxPrice != null, Dish::getPrice, maxPrice)
                .orderByDesc(Dish::getCreateTime));
        return dishes.stream().map(this::buildDishVO).toList();
    }

    @Override
    public DishVO getDishDetail(Long id) {
        return buildDishVO(requireDish(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DishVO viewDishDetail(Long id) {
        Dish dish = requireDish(id);
        // 浏览量防刷:同一IP同一菜品每天只计一次
        String clientIp = getClientIp();
        String viewKey = "view:dish:" + id + ":" + java.time.LocalDate.now();
        Long added = redisTemplate.opsForSet().add(viewKey, clientIp);
        if (added != null && added > 0) {
            redisTemplate.expire(viewKey, java.time.Duration.ofDays(2));
            dish.setViewCount((dish.getViewCount() == null ? 0 : dish.getViewCount()) + 1);
            dish.setUpdateTime(LocalDateTime.now());
            updateById(dish);
        }
        User user = userService.currentUserEntity();
        behaviorService.record(user.getId(), id, "VIEW", BigDecimal.ONE);
        DishVO vo = buildDishVO(dish);
        vo.setLiked(behaviorService.hasBehavior(user.getId(), id, "LIKE"));
        vo.setFavorited(favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, user.getId())
                .eq(Favorite::getDishId, id)) > 0);
        return vo;
    }

    /**
     * 如果当前用户是商户,返回其窗口 ID 集合;否则返回 null
     */
    private Set<Long> getMerchantWindowIds() {
        try {
            User user = userService.currentUserEntity();
            if (user != null && "MERCHANT".equals(user.getRole())) {
                return windowService.list(new LambdaQueryWrapper<CanteenWindow>()
                                .eq(CanteenWindow::getMerchantId, user.getId()))
                        .stream().map(CanteenWindow::getId).collect(Collectors.toSet());
            }
        } catch (Exception e) {
            // 未登录或非商户,不限制
        }
        return null;
    }

    private String getClientIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return "unknown";
            HttpServletRequest request = attrs.getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            if (ip != null && !ip.isEmpty()) {
                ip = ip.split(",")[0].trim();
            }
            if (ip == null || ip.isEmpty()) {
                ip = request.getHeader("X-Real-IP");
            }
            if (ip == null || ip.isEmpty()) {
                ip = request.getRemoteAddr();
            }
            return ip;
        } catch (Exception e) {
            return "unknown";
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> likeDish(Long id) {
        Dish dish = requireDish(id);
        ensureAvailable(dish);
        User user = userService.currentUserEntity();
        boolean alreadyLiked = behaviorService.hasBehavior(user.getId(), id, "LIKE");
        if (alreadyLiked) {
            // 取消点赞
            dish.setLikeCount(Math.max(0, (dish.getLikeCount() == null ? 0 : dish.getLikeCount()) - 1));
            dish.setUpdateTime(LocalDateTime.now());
            updateById(dish);
            behaviorService.removeBehavior(user.getId(), id, "LIKE");
            Map<String, Object> result = new HashMap<>();
            result.put("dish", buildDishVO(dish));
            result.put("liked", false);
            return result;
        } else {
            // 点赞
            dish.setLikeCount((dish.getLikeCount() == null ? 0 : dish.getLikeCount()) + 1);
            dish.setUpdateTime(LocalDateTime.now());
            updateById(dish);
            behaviorService.record(user.getId(), id, "LIKE", BigDecimal.valueOf(2.00));
            Map<String, Object> result = new HashMap<>();
            result.put("dish", buildDishVO(dish));
            result.put("liked", true);
            return result;
        }
    }

    @Override
    public DishVO createDish(DishRequest request) {
        validateRefs(request.getWindowId(), request.getCategoryId());
        LocalDateTime now = LocalDateTime.now();
        Dish dish = new Dish();
        fillDish(dish, request);
        dish.setAvgRating(BigDecimal.ZERO);
        dish.setSalesCount(0);
        dish.setViewCount(0);
        dish.setFavoriteCount(0);
        dish.setLikeCount(0);
        dish.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        dish.setCreateTime(now);
        dish.setUpdateTime(now);
        save(dish);
        return buildDishVO(dish);
    }

    @Override
    public DishVO updateDish(Long id, DishRequest request) {
        Dish dish = requireDish(id);
        validateRefs(request.getWindowId(), request.getCategoryId());
        fillDish(dish, request);
        if (request.getStatus() != null) {
            dish.setStatus(request.getStatus());
        }
        dish.setUpdateTime(LocalDateTime.now());
        updateById(dish);
        return buildDishVO(dish);
    }

    @Override
    public void disableDish(Long id) {
        Dish dish = requireDish(id);
        dish.setStatus(0);
        dish.setUpdateTime(LocalDateTime.now());
        updateById(dish);
    }

    @Override
    public DishVO updateStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1 && status != 2)) {
            throw new BizException("菜品状态只能是0下架、1上架或2待审核");
        }
        Dish dish = requireDish(id);
        dish.setStatus(status);
        dish.setUpdateTime(LocalDateTime.now());
        updateById(dish);
        return buildDishVO(dish);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DishVO updateTags(Long id, List<Long> tagIds) {
        Dish dish = requireDish(id);
        dishTagMapper.delete(new LambdaQueryWrapper<DishTag>().eq(DishTag::getDishId, id));
        if (!CollectionUtils.isEmpty(tagIds)) {
            for (Long tagId : tagIds) {
                if (tagService.getById(tagId) == null) {
                    throw new BizException("标签不存在：" + tagId);
                }
                DishTag dishTag = new DishTag();
                dishTag.setDishId(id);
                dishTag.setTagId(tagId);
                dishTagMapper.insert(dishTag);
            }
        }
        return buildDishVO(dish);
    }

    private void fillDish(Dish dish, DishRequest request) {
        dish.setWindowId(request.getWindowId());
        dish.setCategoryId(request.getCategoryId());
        dish.setName(request.getName());
        dish.setImage(request.getImage());
        dish.setPrice(request.getPrice());
        dish.setDescription(request.getDescription());
        dish.setTaste(request.getTaste());
        dish.setCalories(request.getCalories());
    }

    private void validateRefs(Long windowId, Long categoryId) {
        if (windowService.getById(windowId) == null) {
            throw new BizException("所属窗口不存在");
        }
        if (categoryService.getById(categoryId) == null) {
            throw new BizException("菜品分类不存在");
        }
    }

    private Dish requireDish(Long id) {
        Dish dish = getById(id);
        if (dish == null) {
            throw new BizException("菜品不存在");
        }
        return dish;
    }

    private void ensureAvailable(Dish dish) {
        if (dish.getStatus() == null || dish.getStatus() != 1) {
            throw new BizException("菜品未上架");
        }
    }

    private DishVO buildDishVO(Dish dish) {
        DishVO vo = DishVO.from(dish);
        CanteenWindow window = windowService.getById(dish.getWindowId());
        if (window != null) {
            vo.setWindowName(window.getName());
            vo.setCanteenId(window.getCanteenId());
            Canteen canteen = canteenService.getById(window.getCanteenId());
            if (canteen != null) {
                vo.setCanteenName(canteen.getName());
            }
        }
        DishCategory category = categoryService.getById(dish.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }
        vo.setTags(loadTags(dish.getId()));
        return vo;
    }

    private List<TagVO> loadTags(Long dishId) {
        List<DishTag> dishTags = dishTagMapper.selectList(new LambdaQueryWrapper<DishTag>().eq(DishTag::getDishId, dishId));
        if (CollectionUtils.isEmpty(dishTags)) {
            return Collections.emptyList();
        }
        List<Long> tagIds = dishTags.stream().map(DishTag::getTagId).toList();
        List<Tag> tags = tagService.listByIds(tagIds);
        return tags.stream().map(TagVO::from).toList();
    }
}

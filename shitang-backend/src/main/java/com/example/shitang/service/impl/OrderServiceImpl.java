package com.example.shitang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.shitang.common.BizException;
import com.example.shitang.dto.OrderCreateRequest;
import com.example.shitang.dto.OrderItemRequest;
import com.example.shitang.entity.CanteenWindow;
import com.example.shitang.entity.Dish;
import com.example.shitang.entity.OrderItem;
import com.example.shitang.entity.Orders;
import com.example.shitang.entity.User;
import com.example.shitang.mapper.OrderItemMapper;
import com.example.shitang.mapper.OrdersMapper;
import com.example.shitang.service.CanteenWindowService;
import com.example.shitang.recommend.RecommendCacheInvalidator;
import com.example.shitang.service.BehaviorService;
import com.example.shitang.service.DishService;
import com.example.shitang.service.NotificationService;
import com.example.shitang.service.OrderService;
import com.example.shitang.service.UserService;
import com.example.shitang.vo.OrderItemVO;
import com.example.shitang.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrderService {
    private final OrderItemMapper orderItemMapper;
    private final UserService userService;
    private final DishService dishService;
    private final BehaviorService behaviorService;
    private final CanteenWindowService windowService;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO createOrder(OrderCreateRequest request) {
        User user = userService.currentUserEntity();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemRequest item : request.getItems()) {
            Dish dish = requireAvailableDish(item.getDishId());
            totalAmount = totalAmount.add(dish.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        Orders order = new Orders();
        order.setUserId(user.getId());
        order.setTotalAmount(totalAmount);
        order.setStatus("PAID");
        order.setCreateTime(LocalDateTime.now());
        save(order);

        for (OrderItemRequest item : request.getItems()) {
            Dish dish = requireAvailableDish(item.getDishId());
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setDishId(dish.getId());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(dish.getPrice());
            orderItemMapper.insert(orderItem);

            dish.setSalesCount((dish.getSalesCount() == null ? 0 : dish.getSalesCount()) + item.getQuantity());
            dish.setUpdateTime(LocalDateTime.now());
            dishService.updateById(dish);
            behaviorService.record(user.getId(), dish.getId(), "ORDER", BigDecimal.valueOf(5.00));
        }
        eventPublisher.publishEvent(new RecommendCacheInvalidator.UserBehaviorEvent(user.getId(), "ORDER"));
        return buildOrderVO(order);
    }

    @Override
    public List<OrderVO> listMine() {
        Long userId = userService.currentUserEntity().getId();
        return list(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getUserId, userId)
                .orderByDesc(Orders::getCreateTime))
                .stream().map(this::buildOrderVO).toList();
    }

    @Override
    public List<OrderVO> listAdmin() {
        List<Orders> orders = list(new LambdaQueryWrapper<Orders>().orderByDesc(Orders::getCreateTime));
        // 商户只能看包含自己窗口菜品的订单
        try {
            User user = userService.currentUserEntity();
            if (user != null && "MERCHANT".equals(user.getRole())) {
                Set<Long> merchantDishIds = getMerchantDishIds(user.getId());
                orders = orders.stream()
                        .filter(o -> {
                            List<OrderItem> items = orderItemMapper.selectList(
                                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, o.getId()));
                            return items.stream().anyMatch(i -> merchantDishIds.contains(i.getDishId()));
                        })
                        .toList();
            }
        } catch (Exception e) {
            // 未登录,返回全部
        }
        return orders.stream().map(this::buildOrderVO).toList();
    }

    private Set<Long> getMerchantDishIds(Long merchantId) {
        List<CanteenWindow> windows = windowService.list(new LambdaQueryWrapper<CanteenWindow>()
                .eq(CanteenWindow::getMerchantId, merchantId));
        if (windows.isEmpty()) return Collections.emptySet();
        Set<Long> windowIds = windows.stream().map(CanteenWindow::getId).collect(Collectors.toSet());
        return dishService.list(new LambdaQueryWrapper<Dish>().in(Dish::getWindowId, windowIds))
                .stream().map(Dish::getId).collect(Collectors.toSet());
    }

    @Override
    public OrderVO updateStatus(Long id, String status) {
        if (!"CREATED".equals(status) && !"PAID".equals(status) && !"CANCELLED".equals(status)) {
            throw new BizException("订单状态只能是 CREATED、PAID 或 CANCELLED");
        }
        Orders order = getById(id);
        if (order == null) {
            throw new BizException("订单不存在");
        }
        order.setStatus(status);
        updateById(order);
        // 发送通知
        String statusLabel = "PAID".equals(status) ? "已支付" : "CANCELLED".equals(status) ? "已取消" : "待支付";
        notificationService.send(order.getUserId(), "ORDER_STATUS",
                "订单状态变更", "订单 #" + id + " 状态已更新为: " + statusLabel, id);
        return buildOrderVO(order);
    }

    private OrderVO buildOrderVO(Orders order) {
        OrderVO vo = OrderVO.from(order);
        List<OrderItemVO> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, order.getId()))
                .stream().map(this::buildOrderItemVO).toList();
        vo.setItems(items);
        return vo;
    }

    private OrderItemVO buildOrderItemVO(OrderItem item) {
        OrderItemVO vo = OrderItemVO.from(item);
        Dish dish = dishService.getById(item.getDishId());
        if (dish != null) {
            vo.setDishName(dish.getName());
            vo.setDishImage(dish.getImage());
        }
        return vo;
    }

    private Dish requireAvailableDish(Long dishId) {
        Dish dish = dishService.getById(dishId);
        if (dish == null) {
            throw new BizException("菜品不存在");
        }
        if (dish.getStatus() == null || dish.getStatus() != 1) {
            throw new BizException("菜品未上架");
        }
        return dish;
    }
}

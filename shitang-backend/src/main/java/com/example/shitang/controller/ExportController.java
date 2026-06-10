package com.example.shitang.controller;

import com.example.shitang.entity.Dish;
import com.example.shitang.entity.Orders;
import com.example.shitang.entity.User;
import com.example.shitang.service.DishService;
import com.example.shitang.service.OrderService;
import com.example.shitang.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {
    private final DishService dishService;
    private final OrderService orderService;
    private final UserService userService;

    @GetMapping("/dishes")
    public void exportDishes(HttpServletResponse response) throws IOException {
        List<Dish> dishes = dishService.list();
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("菜品列表");
        String[] headers = {"ID", "名称", "价格", "口味", "热量", "评分", "销量", "浏览", "收藏", "点赞", "状态"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) headerRow.createCell(i).setCellValue(headers[i]);

        int rowIdx = 1;
        for (Dish d : dishes) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(d.getId());
            row.createCell(1).setCellValue(d.getName());
            row.createCell(2).setCellValue(d.getPrice() != null ? d.getPrice().doubleValue() : 0);
            row.createCell(3).setCellValue(d.getTaste() != null ? d.getTaste() : "");
            row.createCell(4).setCellValue(d.getCalories() != null ? d.getCalories() : 0);
            row.createCell(5).setCellValue(d.getAvgRating() != null ? d.getAvgRating().doubleValue() : 0);
            row.createCell(6).setCellValue(d.getSalesCount() != null ? d.getSalesCount() : 0);
            row.createCell(7).setCellValue(d.getViewCount() != null ? d.getViewCount() : 0);
            row.createCell(8).setCellValue(d.getFavoriteCount() != null ? d.getFavoriteCount() : 0);
            row.createCell(9).setCellValue(d.getLikeCount() != null ? d.getLikeCount() : 0);
            row.createCell(10).setCellValue(d.getStatus() != null ? d.getStatus() : 0);
        }
        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
        writeResponse(response, wb, "菜品列表");
    }

    @GetMapping("/orders")
    public void exportOrders(HttpServletResponse response) throws IOException {
        List<Orders> orders = orderService.list();
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("订单列表");
        String[] headers = {"ID", "用户ID", "总金额", "状态", "创建时间"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) headerRow.createCell(i).setCellValue(headers[i]);

        int rowIdx = 1;
        for (Orders o : orders) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(o.getId());
            row.createCell(1).setCellValue(o.getUserId());
            row.createCell(2).setCellValue(o.getTotalAmount() != null ? o.getTotalAmount().doubleValue() : 0);
            row.createCell(3).setCellValue(o.getStatus() != null ? o.getStatus() : "");
            row.createCell(4).setCellValue(o.getCreateTime() != null ? o.getCreateTime().toString() : "");
        }
        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
        writeResponse(response, wb, "订单列表");
    }

    @GetMapping("/users")
    public void exportUsers(HttpServletResponse response) throws IOException {
        List<User> users = userService.list();
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("用户列表");
        String[] headers = {"ID", "用户名", "姓名", "学号", "学院", "年级", "角色", "状态", "注册时间"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) headerRow.createCell(i).setCellValue(headers[i]);

        int rowIdx = 1;
        for (User u : users) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(u.getId());
            row.createCell(1).setCellValue(u.getUsername() != null ? u.getUsername() : "");
            row.createCell(2).setCellValue(u.getRealName() != null ? u.getRealName() : "");
            row.createCell(3).setCellValue(u.getStudentNo() != null ? u.getStudentNo() : "");
            row.createCell(4).setCellValue(u.getCollege() != null ? u.getCollege() : "");
            row.createCell(5).setCellValue(u.getGrade() != null ? u.getGrade() : "");
            row.createCell(6).setCellValue(u.getRole() != null ? u.getRole() : "");
            row.createCell(7).setCellValue(u.getStatus() != null ? u.getStatus() : 0);
            row.createCell(8).setCellValue(u.getCreateTime() != null ? u.getCreateTime().toString() : "");
        }
        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
        writeResponse(response, wb, "用户列表");
    }

    private void writeResponse(HttpServletResponse response, Workbook wb, String filename) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8) + ".xlsx");
        wb.write(response.getOutputStream());
        wb.close();
    }
}

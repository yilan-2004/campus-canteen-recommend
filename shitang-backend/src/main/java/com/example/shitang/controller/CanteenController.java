package com.example.shitang.controller;

import com.example.shitang.common.Result;
import com.example.shitang.dto.CanteenRequest;
import com.example.shitang.entity.Canteen;
import com.example.shitang.service.CanteenService;
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
@RequestMapping("/api/canteens")
@RequiredArgsConstructor
public class CanteenController {
    private final CanteenService canteenService;

    @GetMapping
    public Result<List<Canteen>> list(@RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) Integer status) {
        return Result.success(canteenService.listCanteens(keyword, status));
    }

    @GetMapping("/{id}")
    public Result<Canteen> detail(@PathVariable Long id) {
        return Result.success(canteenService.getById(id));
    }

    @PostMapping
    public Result<Canteen> create(@Valid @RequestBody CanteenRequest request) {
        return Result.success(canteenService.createCanteen(request));
    }

    @PutMapping("/{id}")
    public Result<Canteen> update(@PathVariable Long id, @Valid @RequestBody CanteenRequest request) {
        return Result.success(canteenService.updateCanteen(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        canteenService.disableCanteen(id);
        return Result.success();
    }
}

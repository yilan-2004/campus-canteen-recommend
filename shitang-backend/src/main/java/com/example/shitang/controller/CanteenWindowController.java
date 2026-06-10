package com.example.shitang.controller;

import com.example.shitang.common.Result;
import com.example.shitang.dto.WindowRequest;
import com.example.shitang.entity.CanteenWindow;
import com.example.shitang.service.CanteenWindowService;
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
@RequestMapping("/api/windows")
@RequiredArgsConstructor
public class CanteenWindowController {
    private final CanteenWindowService windowService;

    @GetMapping
    public Result<List<CanteenWindow>> list(@RequestParam(required = false) Long canteenId,
                                            @RequestParam(required = false) Long merchantId,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) Integer status) {
        return Result.success(windowService.listWindows(canteenId, merchantId, keyword, status));
    }

    @GetMapping("/{id}")
    public Result<CanteenWindow> detail(@PathVariable Long id) {
        return Result.success(windowService.getById(id));
    }

    @PostMapping
    public Result<CanteenWindow> create(@Valid @RequestBody WindowRequest request) {
        return Result.success(windowService.createWindow(request));
    }

    @PutMapping("/{id}")
    public Result<CanteenWindow> update(@PathVariable Long id, @Valid @RequestBody WindowRequest request) {
        return Result.success(windowService.updateWindow(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        windowService.disableWindow(id);
        return Result.success();
    }
}

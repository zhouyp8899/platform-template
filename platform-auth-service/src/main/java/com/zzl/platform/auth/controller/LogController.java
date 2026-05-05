package com.zzl.platform.auth.controller;

import com.zzl.platform.common.core.res.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/log/")
public class LogController {

    @PostMapping("operate/page")
    public Result<List<Long>> getLogList() {
        return Result.success(Collections.emptyList());
    }
}

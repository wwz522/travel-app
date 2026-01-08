package com.tripmate.controller;

import com.tripmate.annotation.RequireAuth;
import com.tripmate.common.Result;
import com.tripmate.dto.AIRoutePlanRequest;
import com.tripmate.dto.AIRoutePlanResponse;
import com.tripmate.service.AIRoutePlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/ai")
public class AIRoutePlanController {

    @Autowired
    private AIRoutePlanService aiRoutePlanService;

    @PostMapping("/plan-route")
    @RequireAuth
    public Result<AIRoutePlanResponse> planRoute(
            @RequestBody AIRoutePlanRequest request,
            HttpServletRequest httpRequest) {
        AIRoutePlanResponse response = aiRoutePlanService.generateRoutePlan(request);
        return Result.success(response);
    }
}


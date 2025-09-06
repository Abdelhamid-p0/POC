package com.service.payement.dashboardApi.controller;

import com.service.payement.dashboardApi.service.DashboardService;
import com.service.payement.model.BatchStatistique;
import com.service.payement.model.Log;
import com.service.payement.model.SingleStatistique;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService modeService) {
        this.dashboardService = modeService;
    }

    @PostMapping("mode")
    @ResponseBody
    public void setMode(@RequestParam String value) {
        dashboardService.switchMode(value);
    }

    @PostMapping("exception")
    @ResponseBody
    public void leverException() {
        dashboardService.leverException();
    }

    @GetMapping()
    public String getDashboard(Model model) {

        model.addAttribute("batchStats", dashboardService.getBatchStatistiqueList());
        model.addAttribute("singleStats", dashboardService.getSingleStatistiqueList());
        model.addAttribute("logs", dashboardService.getLogList());
        model.addAttribute("factureDLQs", dashboardService.getFactureDLQ());
        model.addAttribute("batchListnnerStatus",dashboardService.getBatchListnnerStatus());
        model.addAttribute("singleListnnerStatus",dashboardService.getSingleListnnerStatus());
        model.addAttribute("throwExceptionStatus",dashboardService.getThrownExceptionStatus());

        return "dashboard";
    }


}
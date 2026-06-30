package com.giavico.planning.api;

import com.giavico.planning.api.PlanningManagementResponses.ImportStep;
import com.giavico.planning.api.PlanningManagementResponses.NdActionItem;
import com.giavico.planning.api.PlanningManagementResponses.NdManagementOverview;
import com.giavico.planning.api.PlanningManagementResponses.NdOrder;
import com.giavico.planning.api.PlanningManagementResponses.NdRequirement;
import com.giavico.planning.api.PlanningManagementResponses.NdStockItem;
import com.giavico.planning.service.NdPlanningManagementService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/planning/nd")
public class PlanningManagementController {
    private final NdPlanningManagementService ndPlanningManagementService;

    public PlanningManagementController(NdPlanningManagementService ndPlanningManagementService) {
        this.ndPlanningManagementService = ndPlanningManagementService;
    }

    @GetMapping("/management")
    public NdManagementOverview overview() {
        return ndPlanningManagementService.overview();
    }

    @GetMapping("/orders")
    public List<NdOrder> orders() {
        return ndPlanningManagementService.orders();
    }

    @GetMapping("/requirements")
    public List<NdRequirement> requirements(@RequestParam(required = false) String orderId) {
        return ndPlanningManagementService.requirements(orderId);
    }

    @GetMapping("/schedule")
    public List<NdRequirement> schedule(@RequestParam(required = false) String orderId) {
        return ndPlanningManagementService.requirements(orderId);
    }

    @GetMapping("/stock")
    public List<NdStockItem> stock() {
        return ndPlanningManagementService.stock();
    }

    @GetMapping("/actions")
    public List<NdActionItem> actionQueue(@RequestParam(required = false) String orderId) {
        return ndPlanningManagementService.actionQueue(orderId);
    }

    @GetMapping("/imports/workflow")
    public List<ImportStep> importWorkflow() {
        return ndPlanningManagementService.importWorkflow();
    }
}

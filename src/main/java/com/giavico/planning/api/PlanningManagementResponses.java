package com.giavico.planning.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class PlanningManagementResponses {
    private PlanningManagementResponses() {
    }

    public record NdManagementOverview(
            String productLine,
            List<NdOrder> orders,
            List<NdRequirement> requirements,
            List<NdStockItem> stock,
            List<NdActionItem> actions,
            List<ImportStep> importWorkflow,
            NdStats stats
    ) {
    }

    public record NdStats(
            int activeOrders,
            int urgentOrders,
            int shortageOrders,
            int lateStartOrders,
            BigDecimal openQuantityKg,
            BigDecimal rawShortageKg
    ) {
    }

    public record NdOrder(
            String id,
            String orderCode,
            String customer,
            String region,
            String productCode,
            String productNameVi,
            String productNameZh,
            BigDecimal quantityKg,
            BigDecimal producedKg,
            BigDecimal shippedKg,
            BigDecimal remainingKg,
            LocalDate deliveryDate,
            String status,
            String priority,
            String sourceDocument
    ) {
    }

    public record NdProductRule(
            String code,
            String codeTw,
            String nameVi,
            String nameZh,
            String specification,
            BigDecimal yieldRate,
            int cultivationDays,
            int harvestDays,
            int heatTreatmentDays,
            int cuttingDays,
            int qaDays,
            BigDecimal capacityKgPerDay,
            String rawMaterialCode,
            String packagingCode
    ) {
    }

    public record NdStockItem(
            String code,
            String nameVi,
            String nameZh,
            String type,
            String unit,
            BigDecimal warehouseQty,
            BigDecimal sx1Qty,
            BigDecimal sx2Qty,
            BigDecimal totalQty,
            LocalDate expirationDate,
            int leadTimeDays,
            String supplier
    ) {
    }

    public record NdRequirement(
            String orderId,
            String orderCode,
            NdProductRule product,
            BigDecimal rawNeedKg,
            BigDecimal rawAvailableKg,
            BigDecimal rawShortageKg,
            BigDecimal packagingNeed,
            BigDecimal packagingAvailable,
            BigDecimal packagingShortage,
            int productionDays,
            LocalDate latestStartDate,
            String risk,
            List<NdScheduleStage> stages
    ) {
    }

    public record NdScheduleStage(
            String key,
            String labelEn,
            String labelVi,
            String labelZh,
            LocalDate date,
            String owner
    ) {
    }

    public record NdActionItem(
            String orderId,
            String orderCode,
            String level,
            String titleEn,
            String titleVi,
            String titleZh,
            String detailEn,
            String detailVi,
            String detailZh
    ) {
    }

    public record ImportStep(
            int sequence,
            String titleEn,
            String titleVi,
            String titleZh,
            String detailEn,
            String detailVi,
            String detailZh
    ) {
    }
}

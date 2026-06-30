package com.giavico.planning.service;

import com.giavico.planning.api.PlanningManagementResponses.ImportStep;
import com.giavico.planning.api.PlanningManagementResponses.NdActionItem;
import com.giavico.planning.api.PlanningManagementResponses.NdManagementOverview;
import com.giavico.planning.api.PlanningManagementResponses.NdOrder;
import com.giavico.planning.api.PlanningManagementResponses.NdProductRule;
import com.giavico.planning.api.PlanningManagementResponses.NdRequirement;
import com.giavico.planning.api.PlanningManagementResponses.NdScheduleStage;
import com.giavico.planning.api.PlanningManagementResponses.NdStats;
import com.giavico.planning.api.PlanningManagementResponses.NdStockItem;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class NdPlanningManagementService {
    private final Clock clock;

    public NdPlanningManagementService() {
        this(Clock.systemDefaultZone());
    }

    NdPlanningManagementService(Clock clock) {
        this.clock = clock;
    }

    public NdManagementOverview overview() {
        List<NdOrder> orders = orders();
        List<NdRequirement> requirements = requirements(null);
        List<NdStockItem> stock = stock();
        List<NdActionItem> actions = actionQueue(null);
        return new NdManagementOverview("ND", orders, requirements, stock, actions, importWorkflow(), stats(orders, requirements));
    }

    public List<NdOrder> orders() {
        LocalDate today = today();
        return List.of(
                order("nd-mgmt-1", "PTE-26020032-002", "AA081 / Kolkata", "SEA", "VND-NC001-HAA-20",
                        bd("98000"), bd("24000"), bd("0"), today.plusDays(28), "IN_PRODUCTION", "URGENT",
                        "歐美-東南亞-韓國-日本區 訂單"),
                order("nd-mgmt-2", "TW002-26-025", "TW002 Đài Loan", "TW002", "VND-NC001-HAA-20",
                        bd("70000"), BigDecimal.ZERO, BigDecimal.ZERO, today.plusDays(45), "MATERIAL_RISK", "NORMAL",
                        "2026 年訂單管制表"),
                order("nd-mgmt-3", "KR-26030005", "Korean Foods Ltd.", "KR/JP", "VND-NC002-HAA-50",
                        bd("42000"), BigDecimal.ZERO, BigDecimal.ZERO, today.plusDays(68), "CONFIRMED", "NORMAL",
                        "ND 3月生產排程"),
                order("nd-mgmt-4", "RPO-22100006-5", "Internal surplus control", "VN", "VND-AI002-BAA-52",
                        bd("18000"), bd("4500"), BigDecimal.ZERO, today.plusDays(20), "SCHEDULED", "NORMAL",
                        "KHỐNG CHẾ SỐ DƯ")
        );
    }

    public List<NdRequirement> requirements(String orderId) {
        return orders().stream()
                .filter(order -> orderId == null || order.id().equals(orderId))
                .map(this::requirementFor)
                .sorted(Comparator.comparing(NdRequirement::latestStartDate))
                .toList();
    }

    public List<NdStockItem> stock() {
        LocalDate today = today();
        return List.of(
                stockItem("ND-RAW-CULTURE", "Thạch dừa thô sau nuôi", "培養後生椰果", "RAW", "kg",
                        bd("36000"), bd("8500"), bd("18000"), today.plusDays(13), 18, "SX1 / SX2 cultivation"),
                stockItem("ND-RAW-SHEET", "Bán thành phẩm thạch dừa tấm", "椰果薄片半成品", "SEMI", "kg",
                        bd("15000"), bd("5000"), bd("7000"), today.plusDays(35), 20, "SX1 sheet group"),
                stockItem("ND-BOX-10KG", "Thùng giấy chống nước 10kg", "10kg防水紙箱", "AUXILIARY", "box",
                        bd("7200"), bd("1500"), bd("1100"), today.plusDays(365), 12, "Bao bì nội địa"),
                stockItem("ND-DRUM-50KG", "Thùng phuy 50kg", "50kg桶", "AUXILIARY", "drum",
                        bd("820"), bd("120"), bd("80"), today.plusDays(540), 16, "Kho bao bì")
        );
    }

    public List<NdActionItem> actionQueue(String orderId) {
        List<NdActionItem> actions = requirements(orderId).stream()
                .flatMap(requirement -> actionItemsFor(requirement).stream())
                .toList();

        if (!actions.isEmpty()) {
            return actions;
        }

        return List.of(new NdActionItem(
                null,
                null,
                "INFO",
                "No critical ND blockers",
                "Không có điểm nghẽn ND nghiêm trọng",
                "目前無ND重大阻礙",
                "Continue tracking daily production and stock reports.",
                "Tiếp tục theo dõi báo cáo sản xuất và tồn kho hằng ngày.",
                "持續追蹤每日生產與庫存報表。"
        ));
    }

    public List<ImportStep> importWorkflow() {
        return List.of(
                new ImportStep(1, "Upload ND workbook", "Tải file ND", "上傳ND表",
                        "ND schedule, raw stock, cutting plan", "Lịch ND, tồn nguyên liệu, kế hoạch cắt", "ND排程、原料庫存、切割計劃"),
                new ImportStep(2, "Preview mapping", "Xem mapping", "預覽對應",
                        "Confirm product, quantity, dates, and status", "Xác nhận sản phẩm, số lượng, ngày, trạng thái", "確認產品、數量、日期、狀態"),
                new ImportStep(3, "Validate risk", "Kiểm tra rủi ro", "檢查風險",
                        "Shortage, expiry, and late inoculation checks", "Thiếu hàng, hết hạn, trễ ngày cấy", "缺料、效期、接種逾期"),
                new ImportStep(4, "Commit records", "Lưu dữ liệu", "保存資料",
                        "Save normalized planning records", "Lưu dữ liệu kế hoạch chuẩn hóa", "保存標準計劃資料")
        );
    }

    private NdRequirement requirementFor(NdOrder order) {
        NdProductRule product = productFor(order.productCode())
                .orElseThrow(() -> new IllegalStateException("Missing ND product rule: " + order.productCode()));
        BigDecimal openQty = order.remainingKg().max(BigDecimal.ZERO);
        BigDecimal rawNeedKg = divideUp(openQty, product.yieldRate());
        BigDecimal packagingNeed = product.packagingCode().contains("DRUM")
                ? divideUp(openQty, bd("50"))
                : divideUp(openQty, bd("10"));
        BigDecimal rawAvailableKg = stockAvailable(product.rawMaterialCode());
        BigDecimal packagingAvailable = stockAvailable(product.packagingCode());
        BigDecimal rawShortageKg = rawNeedKg.subtract(rawAvailableKg).max(BigDecimal.ZERO);
        BigDecimal packagingShortage = packagingNeed.subtract(packagingAvailable).max(BigDecimal.ZERO);
        int productionDays = Math.max(1, divideUp(openQty, product.capacityKgPerDay()).intValue());

        LocalDate packDate = order.deliveryDate().minusDays(product.qaDays() + 1L);
        LocalDate cutDate = packDate.minusDays(product.cuttingDays());
        LocalDate heatDate = cutDate.minusDays(product.heatTreatmentDays());
        LocalDate harvestDate = heatDate.minusDays(product.harvestDays());
        LocalDate inoculationDate = harvestDate.minusDays(product.cultivationDays());
        String risk = risk(inoculationDate, order.deliveryDate(), rawShortageKg, packagingShortage);

        return new NdRequirement(
                order.id(),
                order.orderCode(),
                product,
                rawNeedKg,
                rawAvailableKg,
                rawShortageKg,
                packagingNeed,
                packagingAvailable,
                packagingShortage,
                productionDays,
                inoculationDate,
                risk,
                List.of(
                        stage("inoculation", "Inoculation", "Cấy giống", "接種", inoculationDate, "SX1 / SX2"),
                        stage("cultivation", "Cultivation", "Nuôi thạch", "培養", inoculationDate.plusDays(product.cultivationDays()), product.cultivationDays() + " days"),
                        stage("harvest", "Harvest", "Thu hoạch", "採收", harvestDate, "Production"),
                        stage("heat", "Heat treatment", "Gia nhiệt", "加熱", heatDate, "Production / QC"),
                        stage("cut", "Cutting", "Cắt hạt/tấm", "切割", cutDate, "SX2 cutting"),
                        stage("pack", "Pack", "Đóng gói", "包裝", packDate, "Packing"),
                        stage("qa", "QA release", "QA duyệt", "QA放行", packDate.plusDays(product.qaDays()), "QA / Warehouse")
                )
        );
    }

    private List<NdActionItem> actionItemsFor(NdRequirement requirement) {
        java.util.ArrayList<NdActionItem> actions = new java.util.ArrayList<>();
        if ("LATE".equals(requirement.risk())) {
            actions.add(new NdActionItem(
                    requirement.orderId(),
                    requirement.orderCode(),
                    "ERROR",
                    "Late inoculation window",
                    "Trễ cửa sổ cấy",
                    "接種窗口逾期",
                    "Latest start date was " + requirement.latestStartDate() + ". Reconfirm delivery or reallocate existing stock.",
                    "Ngày cấy muộn nhất là " + requirement.latestStartDate() + ". Cần xác nhận lại ngày giao hoặc điều phối tồn hiện có.",
                    "最晚開始日為 " + requirement.latestStartDate() + "。需重確交期或調撥現有庫存。"
            ));
        }
        if (requirement.rawShortageKg().signum() > 0) {
            actions.add(new NdActionItem(
                    requirement.orderId(),
                    requirement.orderCode(),
                    "WARNING",
                    "Raw material shortage",
                    "Thiếu nguyên liệu",
                    "原料不足",
                    requirement.rawShortageKg().toPlainString() + " kg must be cultivated or reassigned from SX stock.",
                    "Cần cấy bổ sung hoặc điều phối " + requirement.rawShortageKg().toPlainString() + " kg từ tồn SX.",
                    "需補培養或調撥 " + requirement.rawShortageKg().toPlainString() + " kg。"
            ));
        }
        if (requirement.packagingShortage().signum() > 0) {
            actions.add(new NdActionItem(
                    requirement.orderId(),
                    requirement.orderCode(),
                    "WARNING",
                    "Packaging shortage",
                    "Thiếu bao bì",
                    "包材不足",
                    requirement.packagingShortage().toPlainString() + " units required before pack date.",
                    "Cần thêm " + requirement.packagingShortage().toPlainString() + " đơn vị trước ngày đóng gói.",
                    "包裝日前需補 " + requirement.packagingShortage().toPlainString() + " 個。"
            ));
        }
        return actions;
    }

    private NdStats stats(List<NdOrder> orders, List<NdRequirement> requirements) {
        BigDecimal openQuantity = orders.stream()
                .map(NdOrder::remainingKg)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal rawShortage = requirements.stream()
                .map(NdRequirement::rawShortageKg)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new NdStats(
                orders.size(),
                (int) orders.stream().filter(order -> "URGENT".equals(order.priority())).count(),
                (int) requirements.stream().filter(req -> req.rawShortageKg().signum() > 0 || req.packagingShortage().signum() > 0).count(),
                (int) requirements.stream().filter(req -> "LATE".equals(req.risk())).count(),
                openQuantity,
                rawShortage
        );
    }

    private String risk(LocalDate latestStartDate, LocalDate deliveryDate, BigDecimal rawShortageKg, BigDecimal packagingShortage) {
        LocalDate today = today();
        if (latestStartDate.isBefore(today)) {
            return "LATE";
        }
        if (rawShortageKg.signum() > 0 || packagingShortage.signum() > 0) {
            return "SHORTAGE";
        }
        if (!latestStartDate.isAfter(today.plusDays(5)) || !deliveryDate.isAfter(today.plusDays(21))) {
            return "WATCH";
        }
        return "OK";
    }

    private Optional<NdProductRule> productFor(String productCode) {
        return products().stream().filter(product -> product.code().equals(productCode)).findFirst();
    }

    private List<NdProductRule> products() {
        return List.of(
                new NdProductRule("VND-NC001-HAA-20", "ND-6E163-HA-25", "Thạch dừa hạt lựu 5-8mm", "不蜜糖椰果丁 5-8mm",
                        "10kg carton / 5-8mm dice", bd("0.84"), 18, 2, 1, 2, 2, bd("12000"), "ND-RAW-CULTURE", "ND-BOX-10KG"),
                new NdProductRule("VND-NC002-HAA-50", "VNC-AO222-HAA-50", "Thạch dừa tấm mỏng", "薄片椰果",
                        "50kg drum / sheet", bd("0.82"), 20, 2, 1, 3, 2, bd("8000"), "ND-RAW-SHEET", "ND-DRUM-50KG"),
                new NdProductRule("VND-AI002-BAA-52", "ND-AI002-BAA-52", "Thạch dừa phối cải trang", "椰果改裝品",
                        "mixed spec / controlled surplus", bd("0.80"), 18, 2, 1, 2, 2, bd("7000"), "ND-RAW-CULTURE", "ND-BOX-10KG")
        );
    }

    private BigDecimal stockAvailable(String materialCode) {
        return stock().stream()
                .filter(item -> item.code().equals(materialCode))
                .map(NdStockItem::totalQty)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

    private NdOrder order(
            String id,
            String orderCode,
            String customer,
            String region,
            String productCode,
            BigDecimal quantityKg,
            BigDecimal producedKg,
            BigDecimal shippedKg,
            LocalDate deliveryDate,
            String status,
            String priority,
            String sourceDocument
    ) {
        NdProductRule product = productFor(productCode)
                .orElseThrow(() -> new IllegalStateException("Missing ND product rule: " + productCode));
        BigDecimal remainingKg = quantityKg.subtract(producedKg).subtract(shippedKg).max(BigDecimal.ZERO);
        return new NdOrder(id, orderCode, customer, region, productCode, product.nameVi(), product.nameZh(),
                quantityKg, producedKg, shippedKg, remainingKg, deliveryDate, status, priority, sourceDocument);
    }

    private NdStockItem stockItem(
            String code,
            String nameVi,
            String nameZh,
            String type,
            String unit,
            BigDecimal warehouseQty,
            BigDecimal sx1Qty,
            BigDecimal sx2Qty,
            LocalDate expirationDate,
            int leadTimeDays,
            String supplier
    ) {
        BigDecimal total = warehouseQty.add(sx1Qty).add(sx2Qty);
        return new NdStockItem(code, nameVi, nameZh, type, unit, warehouseQty, sx1Qty, sx2Qty, total, expirationDate, leadTimeDays, supplier);
    }

    private NdScheduleStage stage(String key, String labelEn, String labelVi, String labelZh, LocalDate date, String owner) {
        return new NdScheduleStage(key, labelEn, labelVi, labelZh, date, owner);
    }

    private BigDecimal divideUp(BigDecimal dividend, BigDecimal divisor) {
        if (dividend.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return dividend.divide(divisor, 0, RoundingMode.CEILING);
    }

    private BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }
}

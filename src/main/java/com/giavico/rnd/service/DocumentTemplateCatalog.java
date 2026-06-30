package com.giavico.rnd.service;

import com.giavico.rnd.api.DocumentTemplateResponses;
import com.giavico.rnd.api.DocumentTemplateResponses.Field;
import com.giavico.rnd.api.DocumentTemplateResponses.Template;
import com.giavico.rnd.api.DocumentTemplateResponses.Text;
import com.giavico.rnd.domain.DocumentType;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class DocumentTemplateCatalog {
    private static final String GENERAL = "general";
    private static final String SPECIFICATION = "specification";
    private static final String PROCESS = "process";
    private static final String APPROVAL = "approval";
    private static final String TRACKING = "tracking";

    private final List<Template> templates = List.of(
            template(DocumentType.SAMPLE_REPORT, "research", name("Sample Report", "Báo cáo mẫu", "樣品報告"), true,
                    List.of(
                            field("sampleCode", "Sample / experiment code", "Mã mẫu / thí nghiệm", "樣品／實驗代碼", "text", true, GENERAL),
                            field("productCode", "Product code", "Mã sản phẩm", "產品代碼", "text", true, GENERAL),
                            field("manufacturingDate", "Manufacturing date", "Ngày sản xuất", "製造日期", "date", false, GENERAL),
                            field("storageCondition", "Storage condition", "Điều kiện bảo quản", "儲存條件", "text", false, GENERAL),
                            field("productName", "Product name", "Tên sản phẩm", "產品名稱", "text", true, GENERAL),
                            field("quantity", "Quantity", "Số lượng", "數量", "number", false, GENERAL),
                            field("packaging", "Packaging", "Bao bì", "包裝", "text", false, GENERAL),
                            table("processMeasurements", "Process measurements", "Kết quả đo theo công đoạn", "製程測量結果", SPECIFICATION,
                                    column("item", "Item", "Chỉ tiêu", "項目"), column("standard", "Standard", "Tiêu chuẩn", "標準"),
                                    column("sample", "Sample", "Mẫu", "樣品"), column("rawMaterial", "Raw material", "Nguyên liệu", "原料"),
                                    column("peeling", "Peeling", "Gọt vỏ", "去皮"), column("crushing", "Crushing", "Nghiền", "破碎"),
                                    column("milling", "Milling", "Xay", "研磨"), column("pulper", "Pulper", "Chà", "打漿"),
                                    column("pulperFinisher", "Pulper finisher", "Chà tinh", "精製"), column("dkDs", "DK / DS", "DK / DS", "DK／DS"),
                                    column("cleaning", "Cleaning", "Làm sạch", "清洗"), column("concentration", "Concentration", "Cô đặc", "濃縮")),
                            table("formula", "Formula", "Công thức", "配方", PROCESS,
                                    column("material", "Material / additive", "Nguyên liệu / phụ gia", "原料／添加物"),
                                    column("brix", "Brix", "Brix", "糖度"), column("brixPercent", "Brix %", "Brix %", "糖度百分比"),
                                    column("weightPercent", "Weight %", "Khối lượng %", "重量百分比")),
                            field("flowChart", "Flow chart", "Sơ đồ quy trình", "流程圖", "textarea", true, PROCESS),
                            table("sampleDispatches", "Sample dispatch", "Gửi mẫu", "樣品寄送", TRACKING,
                                    column("orderNumber", "Order no.", "Số đơn", "訂單號"), column("locationCustomer", "Location / customer", "Nơi nhận / khách hàng", "地點／客戶"),
                                    column("sendingDate", "Sending date", "Ngày gửi", "寄送日期"), column("quantity", "Quantity", "Số lượng", "數量"),
                                    column("condition", "Condition", "Tình trạng", "狀態"), column("producer", "Producer", "Người thực hiện", "製作者"),
                                    column("authorizedBy", "Authorized by", "Người phê duyệt", "核准者")),
                            field("notes", "Notes", "Ghi chú", "備註", "textarea", false, GENERAL)),
                    approvals(
                            role("R&D department manager", "Trưởng bộ phận R&D", "研發部主管"),
                            role("Research supervisor", "Giám sát nghiên cứu", "研究主管"),
                            role("Reporter", "Người lập báo cáo", "報告人"))),

            template(DocumentType.ENGINEERING_CHANGE_NOTICE, "change", name("Engineering Change Notice", "Thông báo thay đổi quy trình và phương thức", "工程變更通知單"), true,
                    List.of(
                            field("date", "Date", "Ngày", "日期", "date", true, GENERAL),
                            field("productName", "Product name", "Tên sản phẩm", "品名", "text", true, GENERAL),
                            field("changeItem", "Change item", "Hạng mục sửa đổi", "修改項目", "text", true, GENERAL),
                            field("beforeChange", "Before change", "Trước sửa đổi", "修改前", "textarea", true, PROCESS),
                            field("afterChange", "After change", "Sau sửa đổi", "修改後", "textarea", true, PROCESS),
                            field("process", "Process", "Quy trình", "製程", "textarea", false, PROCESS),
                            field("notes", "Notes", "Ghi chú", "備註", "textarea", false, PROCESS),
                            field("implementationDate", "Expected implementation date", "Ngày dự kiến thực hiện", "預計實施日期", "date", true, GENERAL),
                            select("changeMethod", "Change method", "Phương thức thay đổi", "變更方式", true, GENERAL,
                                    option("Normal", "Bình thường", "正常"), option("Phased", "Theo giai đoạn", "階段"), option("Temporary", "Tạm thời", "臨時")),
                            checkbox("changeFactors", "Change factors", "Yếu tố thay đổi", "更改因素", GENERAL,
                                    option("Safety", "Tính an toàn", "安全性"), option("Raw material", "Nguyên liệu", "原料"), option("Additive", "Phụ liệu", "輔料"),
                                    option("Customer requirement", "Yêu cầu khách hàng", "客戶需求"), option("Inventory consumption", "Xử lý hàng tồn", "消化庫存"),
                                    option("Specification / process change", "Sửa đổi quy cách / quy trình", "規格／製程更改"), option("Other", "Khác", "其他")),
                            checkbox("limitSamples", "Limit samples", "Mẫu giới hạn", "限度樣品", GENERAL,
                                    option("Semi-finished product", "Bán thành phẩm", "半成品"), option("Before sterilization", "Trước sát khuẩn", "殺菌前"), option("Finished product", "Thành phẩm", "成品"))),
                    changeApprovals()),

            template(DocumentType.CHANGE_PROPOSAL, "change", name("Process, Formula and Specification Change Proposal", "Phiếu đề xuất thay đổi quy trình, công thức và quy cách", "製程、配方、規格提議更改單"), true,
                    List.of(
                            field("date", "Date", "Ngày", "日期", "date", true, GENERAL),
                            field("productCode", "Product code", "Mã số sản phẩm", "產品代號", "text", true, GENERAL),
                            checkbox("changeItems", "Change items", "Hạng mục sửa đổi", "修改項目", GENERAL,
                                    option("Specification", "Quy cách", "規格"), option("Formula", "Công thức", "配方"), option("Process", "Quy trình", "製程")),
                            field("currentStandard", "Current standard", "Nội dung văn bản tiêu chuẩn hiện tại", "標準書內容", "textarea", true, PROCESS),
                            field("proposedChange", "Proposed change", "Nội dung đề xuất sửa đổi", "提議更改內容", "textarea", true, PROCESS),
                            field("startDate", "Change start date", "Thời điểm bắt đầu", "更改開始時間", "date", true, GENERAL),
                            field("reason", "Reason for change", "Lý do sửa đổi", "更改原因", "textarea", true, PROCESS),
                            field("notes", "Notes", "Ghi chú", "備註", "textarea", false, PROCESS)),
                    approvals(role("General manager", "Tổng giám đốc", "總經理"), role("Executive vice president", "Phó tổng điều hành", "執行副總"),
                            role("Technical manager", "Trưởng bộ phận kỹ thuật", "技術部主管"), role("QA & Production manager", "Trưởng bộ phận QA & Sản xuất", "品保與生產部主管"),
                            role("Unit manager", "Chủ quản đơn vị", "單位主管"), role("Prepared by", "Người lập biểu", "製表人"))),

            template(DocumentType.ENGINEERING_CHANGE_REQUEST, "change", name("Engineering Change Request", "Phiếu đề xuất thay đổi công trình", "工程變更申請單"), true,
                    List.of(
                            field("documentNumber", "Document number", "Mã số văn kiện", "文件編號", "text", true, GENERAL),
                            field("notificationDate", "Notification date", "Ngày thông báo", "通知日期", "date", true, GENERAL),
                            field("receivedDate", "Received date", "Ngày nhận", "收件日期", "date", false, GENERAL),
                            field("productCode", "Product code", "Mã số sản phẩm", "產品代號", "text", true, GENERAL),
                            select("urgency", "Urgency", "Mức độ khẩn cấp", "完成急迫度", true, GENERAL, option("Urgent", "Gấp", "急件"), option("Normal", "Bình thường", "一般件")),
                            field("packaging", "Packaging", "Bao bì", "包裝", "text", false, GENERAL),
                            select("importance", "Importance", "Mức độ quan trọng", "完成重要度", true, GENERAL, option("A", "A", "A"), option("B", "B", "B")),
                            field("quantity", "Quantity", "Số lượng", "數量", "number", false, GENERAL),
                            field("referenceCost", "Reference cost", "Giá thành tham khảo", "參考成本", "number", false, GENERAL),
                            field("estimatedAnnualQuantity", "Estimated annual quantity", "Sản lượng dự tính năm", "年預估量", "number", false, GENERAL),
                            field("completionDate", "Completion date", "Ngày hoàn thành", "完成日期", "date", false, GENERAL),
                            specification("brix", "Brix", "Brix", "糖度"), specification("acid", "Acid", "Acid", "酸度"), specification("ph", "pH", "pH", "酸鹼值"),
                            checkbox("businessReasons", "Business reasons", "Nguyên nhân kinh doanh", "業務原因", PROCESS,
                                    option("Shortage", "Thiếu hụt", "短缺"), option("Purchase price increase", "Giá mua tăng", "收購價上揚"), option("New order", "Đơn đặt hàng mới", "新訂單"),
                                    option("Insufficient customer forecast", "Dự báo khách hàng không đủ", "客戶預估量不足")),
                            checkbox("planningReasons", "Planning reasons", "Nguyên nhân kế hoạch", "生管原因", PROCESS,
                                    option("Seasonal production shortage", "Sản lượng mùa vụ không đủ", "產季生產量不足"), option("Semi-finished shortage", "Thiếu bán thành phẩm", "半成品銜接不足"), option("Inventory consumption", "Tiêu hóa tồn kho", "去化庫存")),
                            field("otherReason", "Other reason", "Nguyên nhân khác", "其他原因", "textarea", false, PROCESS),
                            field("existingAnalysis", "Existing finished product and raw-material analysis", "Phân tích thành phẩm và nguyên liệu hiện có", "現有成品與原料分析", "textarea", false, PROCESS),
                            field("recommendedMaterials", "Recommended raw / semi-finished materials", "Nguyên liệu / bán thành phẩm kiến nghị", "建議使用原料與半成品", "textarea", false, PROCESS),
                            checkbox("requiredResults", "Required results", "Kết quả yêu cầu", "要求結果", PROCESS,
                                    option("Review", "Xét duyệt", "審核"), option("Trial and component analysis", "Làm thử và phân tích thành phần", "試作、成分分析表"), option("Create BOM", "Lập BOM", "BOM表製作"),
                                    option("Create limit sample", "Làm mẫu giới hạn", "限度樣品製作"), option("QC engineering diagram", "Sơ đồ công trình QC", "QC工程圖"), option("Production operation standard", "Tiêu chuẩn thao tác sản xuất", "生產作業標準"),
                                    option("Experimental process notice", "Thông báo quy trình thí nghiệm", "實驗製程通知單"), option("Trial-to-production notice", "Thông báo chuyển thử nghiệm sang sản xuất", "實驗品轉現場品通知單")),
                            field("notes", "Notes", "Ghi chú", "備註", "textarea", false, PROCESS)),
                    approvals(role("Responsible unit manager", "Chủ quản đơn vị phụ trách", "負責單位主管"), role("Requesting unit manager", "Chủ quản đơn vị đề xuất", "申請單位主管"), role("Applicant", "Người lập biểu", "申請人"))),

            receipt(DocumentType.SEMI_FINISHED_STANDARD_RECEIPT, "Semi-finished Acceptance Standard Receipt and Recall Register", "Bảng ký nhận và thu hồi quy cách nghiệm thu bán thành phẩm", "半成品允收規格表回收簽收單", true, true),
            receipt(DocumentType.PRODUCT_SPECIFICATION_RECEIPT, "New Product Specification Receipt and Recall Register", "Bảng ký nhận và thu hồi phiếu quy cách sản phẩm mới", "新產品規格說明單回收簽收單", false, true),
            receipt(DocumentType.PRODUCT_CHANGE_NOTICE_RECEIPT, "Product Process, Formula and Specification Change Notice Receipt Register", "Bảng ký nhận và thu hồi thông báo sửa đổi quy trình, công thức và quy cách", "產品製程、配方、規格更改通知單回收簽收單", true, true),
            receipt(DocumentType.MANUFACTURING_NOTICE_RECEIPT, "New Product Manufacturing Notice Receipt and Recall Register", "Bảng ký nhận và thu hồi thông báo chế biến sản phẩm mới", "新產品製造通知單回收簽收單", true, true),

            template(DocumentType.PRODUCT_CHANGE_NOTIFICATION, "change", name("Product Process, Formula and Specification Change Notice", "Thông báo sửa đổi quy trình, công thức và quy cách sản phẩm", "產品製程、配方、規格更改通知單"), true,
                    List.of(field("revision", "Revision", "Phiên bản", "版次", "text", true, GENERAL), field("date", "Date", "Ngày", "日期", "date", true, GENERAL),
                            field("productCode", "Product code", "Mã số sản phẩm", "品名代號", "text", true, GENERAL),
                            checkbox("changeItems", "Change items", "Hạng mục sửa đổi", "修改項目", GENERAL, option("Specification", "Quy cách", "規格"), option("Formula", "Công thức", "配方"), option("Process", "Quy trình", "製程")),
                            field("beforeChange", "Before change", "Trước khi sửa đổi", "修改前", "textarea", true, PROCESS),
                            field("afterChange", "After change", "Sau khi sửa đổi", "修改後", "textarea", true, PROCESS),
                            field("notes", "Notes", "Ghi chú", "備註", "textarea", false, PROCESS)), changeApprovals()),

            template(DocumentType.MANUFACTURING_NOTICE, "product", name("New Product Manufacturing Notice", "Thông báo chế biến sản phẩm mới", "新產品製造通知單"), true,
                    newProductFields(true), productApprovals()),
            template(DocumentType.PRODUCT_SPECIFICATION, "product", name("New Product Specification Sheet", "Phiếu giải thích quy cách sản phẩm mới", "新產品規格說明單"), true,
                    newProductFields(false), productApprovals()),

            template(DocumentType.FINISHED_PRODUCT_ACCEPTANCE, "standard", name("Finished Product Acceptance Specification", "Bảng quy cách nghiệm thu thành phẩm", "成品允收規格表"), true,
                    acceptanceFields(false, false), approvals(role("Technical manager", "Chủ quản bộ phận kỹ thuật", "技術部主管"), role("Unit manager", "Chủ quản đơn vị", "單位主管"), role("Prepared by", "Người lập biểu", "製表人"))),
            template(DocumentType.SEMI_FINISHED_ACCEPTANCE, "standard", name("Semi-finished Product Acceptance Specification", "Bảng quy cách nghiệm thu bán thành phẩm", "半成品允收規格表"), true,
                    acceptanceFields(true, false), approvals(role("Executive vice president", "Phó tổng điều hành", "執行副總"), role("QA manager", "Chủ quản QA", "品保主管"), role("Technical manager", "Chủ quản kỹ thuật", "技術部主管"), role("Prepared by", "Người lập biểu", "製表人"))),
            template(DocumentType.RAW_MATERIAL_ACCEPTANCE, "standard", name("Raw Material Acceptance Specification", "Bảng quy cách nghiệm thu nguyên liệu", "原料允收規格表"), true,
                    acceptanceFields(true, true), approvals(role("General manager", "Tổng giám đốc", "總經理"), role("Raw material manager", "Chủ quản nguyên liệu", "原料部主管"), role("Technical manager", "Chủ quản kỹ thuật", "技術部主管"), role("Prepared by", "Người lập biểu", "製表人"))),

            template(DocumentType.PRODUCT_CONFIRMATION, "product", name("Coordination Meeting Product Confirmation", "Phiếu xác nhận sản phẩm họp thỏa thuận", "協調會產品確認單"), false,
                    List.of(field("lockedSourceNotice", "An unlocked source PDF is required before this template can be configured.", "Cần bản PDF đã mở khóa để cấu hình chính xác biểu mẫu này.", "需要未加密的來源 PDF 才能準確設定此表單。", "notice", false, GENERAL)), List.of()),

            template(DocumentType.FOREIGN_RETURN_SAMPLE_CONTACT, "sample",
                    name("Foreign Returned Sample Contact Form", "Phiếu liên hệ mẫu nước ngoài hồi xưởng", "國外回廠樣品聯繫表"), true,
                    foreignReturnSampleFields(), approvals(role("Department manager", "Chủ quản bộ phận", "主管簽核"), role("Prepared by", "Người lập biểu", "填表人"))),

            template(DocumentType.NEW_PRODUCT_PRODUCTION_CONTACT, "sample",
                    name("New Product Production Contact Form", "Phiếu liên hệ chế biến sản phẩm mới", "新產品製作連絡單"), true,
                    newProductContactFields(false), approvals(role("Product technology supervisor", "Chủ quản kỹ thuật sản phẩm", "產技課主管"),
                    role("Product technology", "Kỹ thuật sản phẩm", "產技課"), role("Unit manager", "Chủ quản đơn vị", "單位主管"), role("Prepared by", "Người lập biểu", "填表"))),

            template(DocumentType.NEW_PRODUCT_SAMPLE_PROCESSING_CONTACT, "sample",
                    name("New Product Sample Processing Contact Form", "Đơn liên hệ chế biến hàng mẫu", "新產品製作連絡單"), true,
                    newProductContactFields(true), approvals(role("R&D manager", "Chủ quản Nghiên cứu Phát triển", "研發主管"),
                    role("Research department", "Phòng Nghiên cứu", "研究課"), role("Business manager", "Chủ quản Kinh doanh", "業務主管"), role("Prepared by", "Người lập biểu", "填表"))),

            template(DocumentType.SAMPLE_PRODUCTION_CONTACT, "sample",
                    name("Sample Production Contact Form", "Phiếu liên hệ chế biến mẫu", "樣品製作連絡單"), true,
                    sampleProductionContactFields(), approvals(role("General manager", "Tổng giám đốc", "總經理"), role("R&D manager", "Giám đốc R&D", "研發經理"),
                    role("Business manager", "Chủ quản kinh doanh", "業務主管"), role("Department manager", "Chủ quản bộ phận", "部門主管"), role("Prepared by", "Người lập biểu", "填表人"))),

            template(DocumentType.SAMPLE_SHIPMENT_NOTICE, "sample",
                    name("Sample Shipment Notice", "Bảng thông báo gửi mẫu", "寄樣通知單"), true,
                    sampleShipmentNoticeFields(), approvals(role("Unit manager", "Chủ quản đơn vị", "單位主管"), role("Prepared by", "Người lập biểu", "製表者"))),

            template(DocumentType.SAMPLE_EXPORT_INVOICE, "sample",
                    name("Sample Export Invoice", "Hóa đơn gửi mẫu", "樣品出口發票"), true,
                    sampleExportInvoiceFields(), List.of())
    );

    public List<Template> findAll() { return templates; }

    public Template find(DocumentType type) {
        return templates.stream().filter(template -> template.type() == type).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Document template not found: " + type));
    }

    private static Template receipt(DocumentType type, String en, String vi, String zh, boolean includesName, boolean sourceAvailable) {
        List<Field> columns = includesName
                ? List.of(column("productCode", "Product code", "Mã sản phẩm", "產品代號"), column("productName", "Product / material name", "Tên sản phẩm / nguyên liệu", "產品／原料名稱"),
                column("createdDate", "Created date", "Ngày lập", "製定日期"), column("deliveredDate", "Delivered date", "Ngày giao", "傳遞日期"), column("receiver", "Receiver", "Người nhận", "收件者"),
                column("returnDate", "Recall date", "Ngày thu hồi", "回收日期"), column("deliveredBy", "Delivered by", "Người giao", "傳遞者"), column("notes", "Notes", "Ghi chú", "備註"))
                : List.of(column("productCode", "Product code", "Mã sản phẩm", "產品代號"), column("createdDate", "Created date", "Ngày lập", "製定日期"),
                column("deliveredDate", "Delivered date", "Ngày giao", "傳遞日期"), column("receiver", "Receiver", "Người nhận", "收件者"), column("returnDate", "Recall date", "Ngày thu hồi", "回收日期"),
                column("deliveredBy", "Delivered by", "Người giao", "傳遞者"), column("notes", "Notes", "Ghi chú", "備註"));
        return template(type, "tracking", name(en, vi, zh), sourceAvailable,
                List.of(new Field("entries", name("Receipt entries", "Danh sách ký nhận", "簽收明細"), "table", true, TRACKING, List.of(), columns)), List.of());
    }

    private static List<Field> newProductFields(boolean manufacturingNotice) {
        List<Field> fields = new java.util.ArrayList<>(List.of(
                field("revision", "Revision", "Phiên bản", "版次", "text", true, GENERAL), field("date", "Date", "Ngày", "日期", "date", true, GENERAL),
                field("vnProductCode", "Vietnam product code", "Mã sản phẩm Việt Nam", "越南代號", "text", true, GENERAL),
                field("taiwanProductCode", "Taiwan product code", "Mã sản phẩm Đài Loan", "台灣代號", "text", false, GENERAL),
                field("productName", "Product name", "Tên sản phẩm", "產品名稱", "text", true, GENERAL), field("packaging", "Packaging", "Bao bì", "包裝", "text", false, GENERAL),
                field("storageAndShelfLife", "Storage condition / shelf life", "Điều kiện bảo quản / hạn sử dụng", "保管條件／有效期限", "text", false, GENERAL),
                field("weight", "Weight", "Trọng lượng", "重量", "text", false, GENERAL)));
        for (String[] item : List.of(new String[]{"brix", "Brix", "Brix", "糖度"}, new String[]{"acid", "Acid", "Acid", "酸度"}, new String[]{"ph", "pH", "pH", "酸鹼值"},
                new String[]{"an", "AN", "AN", "甲醛態氮"}, new String[]{"solid", "Solid", "Chất rắn", "固形物"}, new String[]{"cps", "CPS", "CPS", "黏度"},
                new String[]{"ash", "Ash", "Tro", "灰份"}, new String[]{"brixAcidRatio", "Brix / Acid", "Tỷ lệ Brix / Acid", "糖酸比"},
                new String[]{"tpc", "TPC", "Tổng số vi sinh", "總生菌數"}, new String[]{"yeastMold", "Yeast / Mold", "Nấm men / nấm mốc", "酵母／黴菌"},
                new String[]{"coliform", "Coliform", "Coliform", "大腸桿菌群"}, new String[]{"eColi", "E. coli", "E. coli", "大腸桿菌"})) {
            fields.add(comparison(item[0], item[1], item[2], item[3]));
        }
        fields.add(field("rawMaterials", "Raw materials", "Nguyên liệu", "原料", "textarea", true, PROCESS));
        if (manufacturingNotice) {
            fields.add(field("additives", "Additives", "Phụ gia", "添加物", "textarea", false, PROCESS));
            fields.add(field("formula", "Formula", "Công thức", "配方", "textarea", true, PROCESS));
        }
        fields.add(field("process", "Process", "Quy trình", "製程", "textarea", true, PROCESS));
        fields.add(field("notes", "Notes", "Ghi chú", "備註", "textarea", false, PROCESS));
        return List.copyOf(fields);
    }

    private static List<Field> acceptanceFields(boolean material, boolean rawMaterial) {
        List<Field> fields = new java.util.ArrayList<>(List.of(field("revision", "Revision", "Phiên bản", "版次", "text", true, GENERAL), field("date", "Date", "Ngày", "日期", "date", true, GENERAL)));
        if (material) {
            fields.add(field("materialName", rawMaterial ? "Raw material name" : "Semi-finished product name", rawMaterial ? "Tên nguyên liệu" : "Tên bán thành phẩm", rawMaterial ? "原料名稱" : "半成品名稱", "text", true, GENERAL));
            fields.add(field("variety", "Variety", "Chủng loại", "品種", "text", true, GENERAL));
            fields.add(field("materialCode", "Material code", "Mã nguyên liệu", "原料代號", "text", true, GENERAL));
            if (!rawMaterial) fields.add(select("origin", "Origin", "Nguồn gốc", "來源", true, GENERAL, option("Domestic", "Trong nước", "國內"), option("Imported", "Ngoài nước", "國外")));
            fields.add(table("acceptanceRequirements", "Acceptance requirements", "Yêu cầu nghiệm thu", "驗收要求", SPECIFICATION,
                    column("item", "Item", "Hạng mục", "項目"), column("requirement", "Requirement", "Yêu cầu", "要求"), column("notes", "Notes", "Ghi chú", "備註"), column("inspectionMethod", "Inspection method", "Phương pháp kiểm nghiệm", "檢驗方式")));
            fields.add(field("heavyMetalLimits", "Heavy metal limits", "Giới hạn kim loại nặng", "重金屬標準", "textarea", true, SPECIFICATION));
            fields.add(field("usageSpecification", "Usage specification", "Quy cách sử dụng", "使用規格", "textarea", true, SPECIFICATION));
            fields.add(field("pesticideResidue", "Pesticide residue", "Dư lượng thuốc trừ sâu", "農藥標準", "textarea", true, SPECIFICATION));
        } else {
            fields.add(field("productCode", "Product code", "Mã sản phẩm", "產品代號", "text", true, GENERAL));
            fields.add(field("productName", "Product name", "Tên sản phẩm", "產品名稱", "text", true, GENERAL));
            for (String[] item : List.of(new String[]{"brix", "Brix", "Brix", "糖度"}, new String[]{"acid", "Acid", "Acid", "酸度"}, new String[]{"ph", "pH", "pH", "酸鹼值"}, new String[]{"an", "AN", "AN", "甲醛態氮"},
                    new String[]{"solid", "Solid", "Chất rắn", "固形物"}, new String[]{"brixAcidRatio", "Brix / Acid", "Tỷ lệ Brix / Acid", "糖酸比"}, new String[]{"ash", "Ash", "Tro", "灰份"},
                    new String[]{"cps", "CPS", "CPS", "黏度"}, new String[]{"impurities", "Impurities", "Tạp chất", "雜質"}, new String[]{"tpc", "TPC", "Tổng số vi sinh", "總生菌數"},
                    new String[]{"yeastMold", "Yeast / Mold", "Nấm men / nấm mốc", "酵母／黴菌"}, new String[]{"coliform", "Coliform", "Coliform", "大腸桿菌群"})) fields.add(specification(item[0], item[1], item[2], item[3]));
            fields.add(field("storageCondition", "Storage condition", "Điều kiện bảo quản", "存放條件", "text", true, GENERAL));
            fields.add(field("shelfLife", "Shelf life", "Hạn sử dụng", "有效日期", "text", true, GENERAL));
            fields.add(field("weightPackaging", "Weight / packaging", "Trọng lượng / bao bì", "重量／包裝", "text", true, GENERAL));
        }
        return List.copyOf(fields);
    }

    private static Template template(DocumentType type, String category, Text name, boolean sourceAvailable, List<Field> fields, List<Text> approvals) {
        return new Template(type, type.formNumber(), name, category, sourceAvailable,
                "/api/rnd-documents/templates/" + type.name() + "/source", fields, approvals);
    }

    public String sourceFile(DocumentType type) {
        return switch (type) {
            case SAMPLE_REPORT -> "sample-report.pdf";
            case ENGINEERING_CHANGE_NOTICE -> "engineering-change-notice.pdf";
            case CHANGE_PROPOSAL -> "change-proposal.pdf";
            case ENGINEERING_CHANGE_REQUEST -> "engineering-change-request.pdf";
            case SEMI_FINISHED_STANDARD_RECEIPT -> "semi-finished-standard-receipt.pdf";
            case PRODUCT_SPECIFICATION_RECEIPT -> "product-specification-receipt.pdf";
            case PRODUCT_CHANGE_NOTICE_RECEIPT -> "product-change-notice-receipt.pdf";
            case MANUFACTURING_NOTICE_RECEIPT -> "manufacturing-notice-receipt.pdf";
            case PRODUCT_CHANGE_NOTIFICATION -> "product-change-notification.pdf";
            case MANUFACTURING_NOTICE -> "manufacturing-notice.pdf";
            case PRODUCT_SPECIFICATION -> "product-specification.pdf";
            case FINISHED_PRODUCT_ACCEPTANCE -> "finished-product-acceptance.pdf";
            case SEMI_FINISHED_ACCEPTANCE -> "semi-finished-acceptance.pdf";
            case RAW_MATERIAL_ACCEPTANCE -> "raw-material-acceptance.pdf";
            case PRODUCT_CONFIRMATION -> "product-confirmation-locked.pdf";
            case FOREIGN_RETURN_SAMPLE_CONTACT -> "foreign-return-sample-contact.doc";
            case NEW_PRODUCT_PRODUCTION_CONTACT -> "new-product-production-contact.doc";
            case NEW_PRODUCT_SAMPLE_PROCESSING_CONTACT -> "new-product-sample-processing-contact.doc";
            case SAMPLE_PRODUCTION_CONTACT -> "sample-production-contact.doc";
            case SAMPLE_SHIPMENT_NOTICE -> "sample-shipment-notice.xlsx";
            case SAMPLE_EXPORT_INVOICE -> "sample-export-invoice.xlsx";
        };
    }

    private static List<Field> foreignReturnSampleFields() {
        return List.of(
                field("documentNumber", "Number", "Số phiếu", "NO", "text", false, GENERAL),
                field("date", "Date", "Ngày", "日期", "date", true, GENERAL),
                field("requester", "Requester", "Người giao việc", "交辦人", "text", true, GENERAL),
                select("returnGrade", "Returned sample grade", "Cấp mẫu hồi xưởng", "樣品回廠級數", false, GENERAL, option("A", "A", "A"), option("B", "B", "B"), option("C", "C", "C")),
                field("productName", "Product name", "Tên sản phẩm", "品名", "text", true, GENERAL),
                field("productCode", "Product code", "Mã sản phẩm", "代號", "text", true, GENERAL),
                select("developmentImportance", "Sample development importance", "Mức độ quan trọng phát triển mẫu", "樣品開發重要性", false, GENERAL, option("A", "A", "A"), option("B", "B", "B"), option("C", "C", "C")),
                field("quantity", "Quantity", "Số lượng", "數量", "text", false, GENERAL),
                field("completionDeadline", "Completion deadline", "Thời hạn hoàn thành", "完成期限", "date", false, GENERAL),
                field("packaging", "Packaging", "Bao bì", "包裝", "text", false, GENERAL),
                field("storageType", "Storage type", "Trạng thái lưu giữ", "儲存型態", "text", false, GENERAL),
                field("purpose", "Purpose", "Mục đích", "目的", "textarea", true, PROCESS),
                field("instruction", "Instructions", "Chỉ thị", "指示", "textarea", false, PROCESS),
                specification("brix", "Brix", "Brix", "糖度"), specification("acid", "Acid", "Acid", "酸度"), specification("ph", "pH", "pH", "酸鹼值"),
                specification("solid", "Solid", "Chất rắn", "固形量"), specification("hardness", "Hardness", "Độ cứng", "硬度"), specification("size", "Size", "Kích cỡ", "SIZE"),
                specification("pulpOver12mm", "Pulp size > 12 mm", "Kích thước thịt quả > 12 mm", "果肉大小(>12mm)"),
                specification("pulp8To12mm", "Pulp size 8-12 mm", "Kích thước thịt quả 8-12 mm", "果肉大小(8-12mm)"),
                specification("pulpUnder8mm", "Pulp size < 8 mm", "Kích thước thịt quả < 8 mm", "果肉大小(<8mm)"),
                field("variety", "Variety", "Giống", "品種", "text", false, SPECIFICATION),
                field("maturity", "Maturity", "Độ chín", "成熟度", "text", false, SPECIFICATION),
                field("origin", "Origin", "Xuất xứ", "產地", "text", false, SPECIFICATION),
                field("source", "Source", "Nguồn", "來源", "text", false, SPECIFICATION),
                field("preparationStatus", "Preparation status", "Tình trạng xử lý", "調理狀況", "textarea", false, PROCESS),
                field("processDescription", "Process description", "Mô tả quy trình", "製程說明", "textarea", false, PROCESS),
                field("cautions", "Cautions", "Lưu ý", "注意事項", "textarea", false, PROCESS),
                field("notes", "Notes", "Ghi chú", "備註", "textarea", false, GENERAL),
                field("resultAnalysis", "Result analysis", "Phân tích kết quả", "結果分析", "textarea", false, PROCESS),
                field("reply", "Reply", "Phản hồi", "回覆", "textarea", false, PROCESS));
    }

    private static List<Field> newProductContactFields(boolean bilingualVersion) {
        List<Field> fields = new java.util.ArrayList<>(List.of(
                select("sampleType", "Sample type", "Loại mẫu", "樣品類型", true, GENERAL,
                        option("New sample", "Hàng mẫu mới", "新樣品"), option("Changed sample", "Hàng mẫu cải tiến / thay đổi", "新樣品變更")),
                field("submissionRound", "Submission round", "Lần gửi mẫu", "送樣次數", "number", false, GENERAL),
                field("date", "Date", "Ngày", "日期", "date", true, GENERAL),
                field("documentNumber", "Number", "Số phiếu", "NO", "text", false, GENERAL),
                field("customerCode", "Customer code", "Mã khách hàng", "客戶代號", "text", true, GENERAL),
                field("salesRegionOrFinalBuyer", "Sales region or final buyer", "Khu vực tiêu thụ hoặc người mua cuối", "銷售地區或最終買方", "text", true, GENERAL),
                field("productName", "Product name", "Tên hàng", "品名", "text", true, GENERAL),
                select("urgency", "Sample urgency", "Mẫu khẩn", "樣品急迫性", true, GENERAL, option("Urgent", "Khẩn", "急件"), option("Normal", "Thông thường", "一般件")),
                field("sampleQuantity", "Sample quantity", "Số lượng mẫu", "樣品數量", "text", false, GENERAL),
                select("importance", "Sample importance", "Mức độ quan trọng", "樣品重要性", false, GENERAL, option("A", "A", "A"), option("B", "B", "B")),
                field("specialRequirement", "Special requirement", "Yêu cầu đặc biệt", "特殊要求", "textarea", false, GENERAL),
                field("desiredCompletionDate", "Desired completion date", "Ngày mong muốn hoàn thành", "希望完成日期", "date", false, GENERAL),
                field("comparisonSample", "Comparison sample / competitor / price / annual demand", "Mẫu đối chứng / đối thủ / giá / nhu cầu năm", "比對品／競爭廠商／價格／年度需求量", "textarea", false, GENERAL),
                checkbox("usageType", "Usage type", "Trạng thái sử dụng", "使用型態", PROCESS, option("Juice", "Nước ép", "果汁"), option("Mixed juice", "Nước ép tổng hợp", "綜合果汁"),
                        option("Single product", "Đơn phẩm", "單品"), option("Syrup", "Nước đường cô đặc", "濃糖"), option("Jelly", "Thạch", "果凍"),
                        option("Alcohol", "Rượu", "酒類"), option("Dairy", "Sữa", "乳製品"), option("Other", "Khác", "其他")),
                field("juiceRatio", "Original juice ratio %", "Tỷ lệ nguyên chất %", "原汁率", "number", false, PROCESS),
                field("otherIngredients", "Other content ingredients", "Nguyên liệu nội dung khác", "其他內容物原料", "textarea", false, PROCESS),
                checkbox("packagingType", "Packaging type", "Trạng thái bao bì", "包裝型態", PROCESS, option("Can", "Lon", "Can"), option("PET", "PET", "PET"),
                        option("Foil pack", "Bao bạc", "鋁箔包"), option("Glass bottle", "Hũ thủy tinh", "玻璃瓶"), option("Cup", "Ly", "杯裝"),
                        option("Fresh house", "Fresh house", "新鮮屋"), option("Tetra Pak", "Tetra Pak", "利樂包")),
                checkbox("salesMethod", "Sales method", "Phương thức tiêu thụ", "銷售方式", PROCESS, option("Ambient", "Nhiệt độ thường", "常溫"), option("Chilled", "Hàng mát", "冷藏"), option("Frozen", "Hàng lạnh", "冷凍")),
                field("productionCondition", "Production conditions", "Điều kiện làm mẫu", "製作條件", "textarea", false, PROCESS),
                select("formulaData", "Formula data", "Tài liệu phối thức", "配方資料", false, PROCESS, option("Required", "Cần cung cấp", "須提供"), option("Not required", "Không cần cung cấp", "不須提供")),
                field("rawMaterialCostControl", "Raw material cost control", "Khống chế giá thành nguyên liệu", "原物料成本控制", "text", false, PROCESS),
                checkbox("productType", "Product type", "Đặc tính sản phẩm", "產品型態", PROCESS, option("Clear", "Trong", "澄清"), option("Cloudy", "Đục", "混濁"),
                        option("Concentrate", "Nước cô đặc", "濃縮汁"), option("Juice", "Nước ép", "原汁"), option("High syrup", "Đường cao độ", "濃糖汁"), option("Other", "Khác", "其他")),
                field("productSolid", "Product solid %", "Chất rắn sản phẩm %", "固形物", "number", false, SPECIFICATION),
                checkbox("storageType", "Storage type", "Trạng thái lưu giữ", "儲存型態", PROCESS, option("Frozen", "Hàng lạnh", "冷凍"), option("Chilled", "Hàng mát", "冷藏"), option("Ambient", "Nhiệt độ thường", "常溫")),
                select("customerSpecificationRequired", "Customer specification requirement", "Yêu cầu quy cách khách hàng", "客戶規格", false, SPECIFICATION,
                        option("Required", "Có yêu cầu quy cách", "有規格要求"), option("Not required", "Không yêu cầu quy cách", "無規格要求"))));
        for (String[] item : List.of(new String[]{"brix", "Brix", "Brix", "糖度"}, new String[]{"tpc", "TPC", "Tổng số vi sinh", "總生菌數"}, new String[]{"acid", "Acid", "Acid", "酸度"},
                new String[]{"yeastMold", "Yeast / Mold", "Nấm men / nấm mốc", "黴菌／酵母菌"}, new String[]{"ph", "pH", "pH", "酸鹼值"}, new String[]{"tab", "TAB", "TAB", "TAB"},
                new String[]{"an", "AN", "AN", "甲醛態氮"}, new String[]{"transmittanceAbsorbance", "Transmittance / absorbance", "Độ truyền quang / hấp thụ", "透光度／吸光度"},
                new String[]{"solid", "Solid", "Chất rắn", "固形物"}, new String[]{"otherSpecification", "Other", "Khác", "其他"})) fields.add(specification(item[0], item[1], item[2], item[3]));
        fields.add(field("notes", "Notes", "Ghi chú", "備註", "textarea", false, GENERAL));
        fields.add(field(bilingualVersion ? "businessManagerOpinion" : "sampleSubmissionAndCustomerOpinion",
                bilingualVersion ? "Business manager opinion" : "Sample submission record and customer opinion",
                bilingualVersion ? "Ý kiến chủ quản kinh doanh" : "Ghi nhận gửi mẫu và ý kiến khách hàng",
                bilingualVersion ? "業務主管意見" : "送樣記錄及客戶意見", "textarea", false, APPROVAL));
        fields.add(field(bilingualVersion ? "researchReply" : "unitManagerOpinion",
                bilingualVersion ? "Research reply" : "Unit manager opinion",
                bilingualVersion ? "Phản hồi phòng kỹ thuật" : "Ý kiến chủ quản đơn vị",
                bilingualVersion ? "研究回覆" : "單位主管意見", "textarea", false, APPROVAL));
        fields.add(field("receivedDate", "Received date", "Ngày nhận đơn", "收單日期", "date", false, APPROVAL));
        fields.add(field("completedDate", "Completed date", "Ngày hoàn thành", "完成日期", "date", false, APPROVAL));
        fields.add(field("sampleCode", "Sample code", "Mã số sản phẩm", "樣品代號", "text", false, APPROVAL));
        fields.add(field("sampleLot", "Sample lot", "Mã số hàng mẫu", "樣品批號", "text", false, APPROVAL));
        fields.add(field("packagingQuantity", "Packaging quantity", "Số lượng bao bì", "包裝數量", "text", false, APPROVAL));
        fields.add(field("resultAnalysis", "Result analysis", "Kết quả phân tích", "結果分析", "textarea", false, APPROVAL));
        return List.copyOf(fields);
    }

    private static List<Field> sampleProductionContactFields() {
        return List.of(
                checkbox("sampleOptions", "Sample options", "Tùy chọn mẫu", "樣品選項", GENERAL, option("No sample reply required", "Không cần trả lời mẫu", "不須回覆樣品"),
                        option("General sample / new product", "Mẫu thông thường / sản phẩm mới", "一般樣品／新產品"), option("Advance shipment sample", "Mẫu hàng trước", "先貨樣品")),
                field("date", "Date", "Ngày", "日期", "date", true, GENERAL), field("documentNumber", "Number", "Số phiếu", "NO", "text", false, GENERAL),
                field("customer", "Customer", "Khách hàng", "客戶", "text", true, GENERAL), field("completionDeadline", "Completion deadline", "Thời hạn hoàn thành", "完成期限", "date", false, GENERAL),
                field("regionOrFinalBuyer", "Region or final buyer", "Khu vực hoặc người mua cuối", "地區或最終買方", "text", false, GENERAL),
                field("sampleIssueDate", "Sample issue date", "Ngày phát mẫu", "發樣日期", "date", false, GENERAL), field("productNameAndCode", "Product name and code", "Tên và mã sản phẩm", "品名、代號", "text", true, GENERAL),
                field("quantity", "Quantity", "Số lượng", "數量", "text", false, GENERAL), field("sampleLot", "Sample lot", "Số lô mẫu", "樣品批號", "text", false, GENERAL),
                field("packaging", "Packaging", "Bao bì", "包裝", "text", false, GENERAL),
                checkbox("productType", "Product type", "Đặc tính sản phẩm", "產品型態", PROCESS, option("Clear", "Trong", "澄清"), option("Cloudy", "Đục", "混濁"),
                        option("Concentrate", "Nước cô đặc", "濃縮汁"), option("Juice", "Nước ép", "原汁"), option("High syrup", "Đường cao độ", "濃糖汁")),
                specification("brix", "Brix", "Brix", "糖度"), specification("acid", "Acid", "Acid", "酸度"), specification("brixAcidRatio", "Brix / acid ratio", "Tỷ lệ Brix / Acid", "糖酸比"),
                specification("an", "AN", "AN", "甲醛態氮"), specification("ph", "pH", "pH", "酸鹼值"), specification("solid", "Solid", "Chất rắn", "固形物"),
                specification("ash", "Ash", "Tro", "灰份"), specification("abs", "ABS", "ABS", "吸光度"), specification("transmittance", "Transmittance", "Độ truyền quang", "透光度"),
                specification("otherSpecification", "Other", "Khác", "其他"),
                field("mainIngredients", "Main content ingredients", "Nguyên liệu chính", "主要內容物原料", "textarea", false, PROCESS),
                field("defaultJuiceRatio", "Default juice ratio", "Tỷ lệ nguyên chất dự kiến", "預設原汁率", "number", false, PROCESS),
                checkbox("manufacturingType", "Manufacturing type", "Hình thức sản xuất", "製造型態", PROCESS, option("Juice", "Nước ép", "果汁"), option("Jelly", "Thạch", "果凍"), option("Alcohol", "Rượu", "酒類"), option("Other", "Khác", "其他")),
                checkbox("salesMethod", "Sales method", "Phương thức tiêu thụ", "銷售方式", PROCESS, option("Chilled", "Hàng mát", "冷藏"), option("Ambient", "Nhiệt độ thường", "常溫"), option("Can", "Dạng lon", "罐型")),
                field("productionCondition", "Production conditions", "Điều kiện làm mẫu", "製作條件", "textarea", false, PROCESS),
                field("formulaCostControl", "Formula raw material cost control", "Khống chế chi phí nguyên liệu công thức", "配方原物料之成本控制", "text", false, PROCESS),
                select("formulaData", "Formula data", "Tài liệu phối thức", "配方資料", false, PROCESS, option("Required", "Cần cung cấp", "須提供"), option("Not required", "Không cần cung cấp", "不須提供")),
                field("resultAnalysis", "Result analysis", "Phân tích kết quả", "結果分析", "textarea", false, APPROVAL),
                field("notes", "Notes", "Ghi chú", "備註", "textarea", false, GENERAL));
    }

    private static List<Field> sampleShipmentNoticeFields() {
        return List.of(
                field("date", "Date", "Ngày", "日期", "date", true, GENERAL),
                table("shipments", "Shipment rows", "Danh sách gửi mẫu", "寄樣明細", TRACKING,
                        column("orderNumber", "Order number", "Đơn hàng", "單號"), column("customerCode", "Customer code", "Mã khách hàng", "客戶代號"),
                        column("preparedBy", "Prepared by", "Người lập đơn", "填表人"), column("sampleSerial", "Sample serial", "Mã số hàng mẫu", "流水號"),
                        column("productCode", "Product code", "Mã sản phẩm", "產品代號"), column("productType", "Type", "Trạng thái", "型態"),
                        column("packaging", "Packaging", "Bao bì", "包裝"), column("storage", "Storage", "Lưu giữ", "儲存"),
                        column("quantity", "Quantity", "Số lượng", "數量"), column("recipient", "Recipient", "Người nhận mẫu", "收件者")),
                field("notes", "Notes", "Ghi chú", "GHI CHÚ", "textarea", false, GENERAL),
                table("recipientDirectory", "Recipient directory", "Danh bạ người nhận", "收件者通訊錄", TRACKING,
                        column("region", "Region", "Khu vực", "地區"), column("attention", "Attention / recipient", "Người nhận", "收件者"),
                        column("companyAddress", "Company and address", "Công ty và địa chỉ", "公司與地址"), column("telephone", "Telephone", "Điện thoại", "電話")));
    }

    private static List<Field> sampleExportInvoiceFields() {
        return List.of(
                field("transporter", "Transporter", "Đơn vị vận chuyển", "運輸者", "text", false, GENERAL),
                field("date", "Date", "Ngày", "日期", "date", true, GENERAL),
                table("invoiceItems", "Invoice items", "Dòng hóa đơn", "發票明細", TRACKING,
                        column("number", "No.", "STT", "No"), column("product", "Product", "Sản phẩm", "產品"),
                        column("quality", "Quality", "Chất lượng", "品質"), column("package", "Package", "Bao bì", "包裝"),
                        column("storage", "Storage", "Lưu giữ", "儲存"), column("from", "From", "Từ", "從"),
                        column("to", "To", "Đến", "到"), column("price", "Price", "Giá", "價格"), column("note", "Note", "Ghi chú", "備註")),
                field("totalWeight", "Total weight", "Tổng khối lượng", "總重量", "text", false, GENERAL),
                field("customsNote", "Customs note", "Ghi chú hải quan", "海關備註", "textarea", false, GENERAL));
    }
    private static Field field(String key, String en, String vi, String zh, String type, boolean required, String section) { return new Field(key, name(en, vi, zh), type, required, section, List.of(), List.of()); }
    private static Field specification(String key, String en, String vi, String zh) { return field(key, en, vi, zh, "specification", false, SPECIFICATION); }
    private static Field comparison(String key, String en, String vi, String zh) { return field(key, en, vi, zh, "comparison", false, SPECIFICATION); }
    private static Field column(String key, String en, String vi, String zh) { return field(key, en, vi, zh, "text", false, TRACKING); }
    private static Field table(String key, String en, String vi, String zh, String section, Field... columns) { return new Field(key, name(en, vi, zh), "table", false, section, List.of(), Arrays.asList(columns)); }
    private static Field select(String key, String en, String vi, String zh, boolean required, String section, Text... options) { return new Field(key, name(en, vi, zh), "select", required, section, Arrays.asList(options), List.of()); }
    private static Field checkbox(String key, String en, String vi, String zh, String section, Text... options) { return new Field(key, name(en, vi, zh), "checkbox-group", false, section, Arrays.asList(options), List.of()); }
    private static Text name(String en, String vi, String zh) { return new Text(en, vi, zh); }
    private static Text option(String en, String vi, String zh) { return name(en, vi, zh); }
    private static Text role(String en, String vi, String zh) { return name(en, vi, zh); }
    private static List<Text> approvals(Text... roles) { return Arrays.asList(roles); }
    private static List<Text> productApprovals() { return approvals(role("Technical manager", "Chủ quản kỹ thuật", "技術部主管"), role("Unit manager", "Chủ quản đơn vị", "單位主管"), role("Prepared by", "Người lập biểu", "製表人")); }
    private static List<Text> changeApprovals() { return approvals(role("General manager", "Tổng giám đốc", "總經理"), role("Executive vice president", "Phó tổng điều hành", "執行副總"), role("Technical manager", "Chủ quản kỹ thuật", "技術部主管"), role("Unit manager", "Chủ quản đơn vị", "單位主管"), role("Prepared by", "Người lập biểu", "製表人")); }
}

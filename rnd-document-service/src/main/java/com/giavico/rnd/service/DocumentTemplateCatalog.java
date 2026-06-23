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
                    List.of(field("lockedSourceNotice", "An unlocked source PDF is required before this template can be configured.", "Cần bản PDF đã mở khóa để cấu hình chính xác biểu mẫu này.", "需要未加密的來源 PDF 才能準確設定此表單。", "notice", false, GENERAL)), List.of())
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
        };
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

package com.giavico.rnd.domain;

public enum DocumentType {
    SAMPLE_REPORT("P-RS1 003-01.03"),
    MANUFACTURING_NOTICE("P-RS1 001-01.02"),
    PRODUCT_SPECIFICATION("P-RS1 001-03.02"),
    FINISHED_PRODUCT_ACCEPTANCE("P-RS1 001-02.02");

    private final String formNumber;
    DocumentType(String formNumber) { this.formNumber = formNumber; }
    public String formNumber() { return formNumber; }
}

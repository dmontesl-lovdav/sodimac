// src/main/java/com/sodimac/fiscal/api/pdf/PaymentPdfService.java
package com.sodimac.fiscal.api.pdf;

public interface PaymentPdfService {
    byte[] renderFromXml(String xml);
}

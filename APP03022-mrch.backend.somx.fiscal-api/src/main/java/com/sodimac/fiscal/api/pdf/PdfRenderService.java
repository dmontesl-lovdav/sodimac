package com.sodimac.fiscal.api.pdf;

public interface PdfRenderService {
    byte[] renderFromXml(String xml);
}

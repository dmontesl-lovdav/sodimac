package com.sodimac.fiscal.api.pdf;

import java.nio.file.Path;

public interface PdfRenderService {
    byte[] renderFromXml(String xml);
}

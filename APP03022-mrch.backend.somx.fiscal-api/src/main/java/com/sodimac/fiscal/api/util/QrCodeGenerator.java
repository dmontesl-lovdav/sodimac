package com.sodimac.fiscal.api.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class QrCodeGenerator {

    private static final String QR_CODE_IMAGE_PREFIX = "QRCode";

    /**
     * @param text      contenido del QR (URL SAT)
     * @param width     ancho en píxeles
     * @param height    alto en píxeles
     * @param dirPath   ruta física de la carpeta donde se guardará (ej. /var/tmp/xsl-fiscal-XXXX/)
     * @param sufijo    normalmente RFC del receptor
     * @param uuid      UUID del CFDI
     */
    public String generateQRCodeImage(String text,
                                      int width,
                                      int height,
                                      String dirPath,
                                      String sufijo,
                                      String uuid)
            throws WriterException, IOException {

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix =
                qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        String fileName = QR_CODE_IMAGE_PREFIX + sufijo + "_" + uuid + ".png";
        Path path = FileSystems.getDefault().getPath(dirPath + fileName);
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);

        return fileName; // por si quieres usarlo directamente
    }
}

package com.sodimac.wsconfiguracion.util;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;


public class QrCodeGenerator {

	private String QR_CODE_IMAGE_PATH = "QRCode";
	
    @SuppressWarnings("unused")
	public String generateQRCodeImage(String text, int width, int height, String filePath, String sufijo)
            throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        Path path = FileSystems.getDefault().getPath(filePath + QR_CODE_IMAGE_PATH + sufijo + ".png" );
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
        
        return QR_CODE_IMAGE_PATH;
    }
	
}

package com.lab.face.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class ImageUtils {

    public static byte[] convertToJpeg(byte[] imageBytes) {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(imageBytes);
            BufferedImage image = ImageIO.read(in);

            if (image == null) {
                throw new RuntimeException("无法解析图片格式");
            }

            if (image.getType() != BufferedImage.TYPE_INT_RGB) {
                BufferedImage rgbImage = new BufferedImage(
                    image.getWidth(),
                    image.getHeight(),
                    BufferedImage.TYPE_INT_RGB
                );
                Graphics2D g = rgbImage.createGraphics();
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, image.getWidth(), image.getHeight());
                g.drawImage(image, 0, 0, null);
                g.dispose();
                image = rgbImage;
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("图片格式转换失败: " + e.getMessage());
        }
    }
}
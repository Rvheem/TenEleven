/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package image;

import java.awt.image.BufferedImage;

/**
 *
 * @author abder
 */
import java.awt.image.BufferedImage;

public class JPEGInterpreter implements ImageInterpreter {
    @Override
    public BufferedImage interpret(BufferedImage image) {
        // Interpret JPEG image (conversion logic)
        BufferedImage interpretedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        interpretedImage.createGraphics().drawImage(image, 0, 0, null);
        return interpretedImage;
    }
}

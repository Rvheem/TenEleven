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
// Step 2: Implement concrete interpreter classes for each image format

public class PNGInterpreter implements ImageInterpreter {
    @Override
    public BufferedImage interpret(BufferedImage image) {
        // Check if the image is already in PNG format
        if (image.getType() == BufferedImage.TYPE_INT_ARGB) {
            // Image is already in PNG format, return it as is
            return image;
        } else {
            // Convert image to PNG format
            BufferedImage pngImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            pngImage.createGraphics().drawImage(image, 0, 0, null);
            return pngImage;
        }
    }
}
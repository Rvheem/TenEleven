package image;

import java.awt.image.BufferedImage;

public class JPGInterpreter implements ImageInterpreter {
    @Override
    public BufferedImage interpret(BufferedImage image) {
        // Check if the image is already in JPG format
        if (image.getType() == BufferedImage.TYPE_INT_RGB) {
            // Image is already in JPG format, return it as is
            return image;
        } else {
            // Convert image to JPG format
            BufferedImage jpgImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            jpgImage.createGraphics().drawImage(image, 0, 0, null);
            return jpgImage;
        }
    }
}

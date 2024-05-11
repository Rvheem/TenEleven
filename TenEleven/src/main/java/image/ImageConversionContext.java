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
// Step 3: Create a context class to hold the input image and manage interpretation
public class ImageConversionContext {
    private BufferedImage image;
    private ImageInterpreter interpreter;

    public ImageConversionContext(BufferedImage image, ImageInterpreter interpreter) {
        this.image = image;
        this.interpreter = interpreter;
    }

    public BufferedImage interpretImage() {
        return interpreter.interpret(image);
    }
}

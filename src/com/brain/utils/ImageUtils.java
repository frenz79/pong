package com.brain.utils;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class ImageUtils {

	public static BufferedImage scale2(BufferedImage before, double scale) {
	    int w = before.getWidth();
	    int h = before.getHeight();
	    // Create a new image of the proper size
	    int w2 = (int) (w * scale);
	    int h2 = (int) (h * scale);
	    BufferedImage after = new BufferedImage(w2, h2, BufferedImage.TYPE_INT_RGB);
	    AffineTransform scaleInstance = AffineTransform.getScaleInstance(scale, scale);
	    AffineTransformOp scaleOp 
	        = new AffineTransformOp(scaleInstance, AffineTransformOp.TYPE_BILINEAR);

	    Graphics2D g2 = (Graphics2D) after.getGraphics();
	    // Here, you may draw anything you want into the new image, but we're
	    // drawing a scaled version of the original image.
	    g2.drawImage(before, scaleOp, 0, 0);
	    g2.dispose();
	    return after;
	}
	
}

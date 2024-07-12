package com.brain.neuralnet;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class NeuralNetworkViewer extends JFrame {

	private Image image;
	private Image layerImages[] = new Image[4];

	private CustomPaintComponent imageComponent;
	private CustomPaintComponentArray layerImageComponent;
	
    public NeuralNetworkViewer( ) {    
    	setTitle("Neural Network Viewer");
    	setSize(800,300);
    	setLocation(800,0);
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        imageComponent = new CustomPaintComponent();
        imageComponent.setPreferredSize( new Dimension(400,300) );
        layerImageComponent = new CustomPaintComponentArray(); 
        layerImageComponent.setPreferredSize( new Dimension(400,300) );
        
        setLayout( new FlowLayout(FlowLayout.LEFT));
        add( imageComponent );
        add( layerImageComponent );
        
        pack();
        setVisible(true);
    }
    
    class CustomPaintComponent extends JPanel {
    	@Override
    	public void paint(Graphics g) {    	 
    	  Graphics2D g2d = (Graphics2D)g;
    	  if ( image!=null ) {
    		  g2d.drawImage(image, 0, 0, 400, 300, this);
    	  }    	 
    	}
    }
    
    class CustomPaintComponentArray extends JPanel {    	
    	@Override
    	public void paint(Graphics g) {    	 
    	  Graphics2D g2d = (Graphics2D)g;
    	  if ( layerImages!=null ) {
    		  g2d.drawImage(layerImages[0],   0, 0, 200, 150, this);
    		  g2d.drawImage(layerImages[1], 200, 0, 200, 150, this);
    		  g2d.drawImage(layerImages[2], 0,  150, 200, 150, this);
    		  g2d.drawImage(layerImages[3], 200, 150, 200, 150, this);
    	  }    	 
    	}
    }
    
	public void setImage(Image image) {
		this.image = image;
		repaint();
	}

	public void setLayerImage(int i, BufferedImage layerImage) {
		this.layerImages[i] = layerImage;
		repaint();
	}
}
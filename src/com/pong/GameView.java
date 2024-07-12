package com.pong;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.brain.models.Direction;
import com.brain.models.Event;
import com.brain.neuralnet.NeuralEnv;
import com.brain.neuralnet.NeuralEnvEventListener;

public class GameView extends JFrame implements NeuralEnv, KeyListener {

	public final KeyEvent UP_KEY_EVENT = new KeyEvent(this, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_W, 'w');
	public final KeyEvent DOWN_KEY_EVENT = new KeyEvent(this, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_S, 's');
	
    private static final long serialVersionUID = -5782301423436L;

    int screenW = 800;
    int screenH = 600;
    JPanel panel;
    Paddle paddle;
    Ball   ball;

    boolean ballOutOfField = false;
    
    private BallEventListener ballEvtListner;
    
    public GameView( NeuralEnvEventListener neuralListener ) throws AWTException {
        super("Pong");
        
        panel = new JPanel();
        this.add(panel);
        
        setPreferredSize(new Dimension(screenW, screenH));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        addKeyListener(this);

        bufferedImage = new BufferedImage(screenW, screenH, BufferedImage.TYPE_INT_BGR);
        
        paddle = new Paddle(100, 300, screenW, screenH);
        
        ballEvtListner = new BallEventListener() {

			@Override
			public void onBallLost() {
				neuralListener.onEvent(Event.BAD);
			}

			@Override
			public void onBallHit() {
				neuralListener.onEvent(Event.GOOD);
			}	
        };
        
        ball = new Ball(300, 300, screenW, screenH, ballEvtListner);
                
        new Thread(() -> {
        	long lastMove = 0L;
        	while (true) {
        		long now = System.nanoTime();
        		
        		if ( now-lastMove > TimeUnit.MILLISECONDS.toNanos(10) ) {
        			lastMove = now;         			
        			ball.handlePaddleCollision(paddle.x, paddle.y, paddle.width, paddle.len);
        			ball.move();
        			ball.draw(getGraphics());
        			repaint();
        		}
        	}
        }).start();
        
        setVisible(true);
    }

    long lastPaint = 0L;
    long ballOutOfFieldTime = 0L;  
      
    private BufferedImage bufferedImage;
   
    
    @Override
    public void paint(Graphics g) {  
    	 Graphics2D graphic2D = bufferedImage.createGraphics();
    	 long now  = System.nanoTime();
    	 
    	 graphic2D.setColor(Color.BLACK);
    	 graphic2D.fillRect(0, 0, 800, 600);

    	 graphic2D.setColor(Color.WHITE);
         this.paddle.draw(graphic2D);
         this.ball.draw(graphic2D);
         
         if ( ballOutOfField ) {
        	 graphic2D.setColor(Color.RED);
        	 graphic2D.drawString("ARRRGHHHHH!!",350,250);
 			
 			if ( now-ballOutOfFieldTime > TimeUnit.SECONDS.toNanos(2)) {
 				ballOutOfField = false;
 			}
         }        
         lastPaint = now;
         
        g.drawImage(bufferedImage,0,0, this);
        graphic2D.dispose();
    }
    
    public void keyTyped(KeyEvent e) {}

    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        int key = keyEvent.getKeyCode();
        if (key == KeyEvent.VK_ESCAPE) {
            this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        } else if (key == KeyEvent.VK_W) {
            this.paddle.up();
            this.paddle.draw(getGraphics());
            repaint();
        } else if (key == KeyEvent.VK_S) {
            this.paddle.down();
            this.paddle.draw(getGraphics());
            repaint();
        }
    }

    @Override
	public BufferedImage getEnvImage() {
		return bufferedImage;
	}

	public void move( Direction dir) {
		switch(dir) {
		case DOWN:
			keyPressed(DOWN_KEY_EVENT);
			break;
		case UP:
			keyPressed(UP_KEY_EVENT);
			break;
		default:
			break;
		
		}
		
	}
}
package com.pong;

import java.awt.Graphics;

public class Ball {

    int x;
    int y;
    int size = 20;
    
    int screenWidth;
    int screenHeight;
    
    int speed = 4;
    int dirX = speed;
    int dirY = -speed;

    private final BallEventListener listener;
    
    public Ball(int _x, int _y, int width, int height, BallEventListener listener) {
        this.x = _x;
        this.y = _y;
        this.screenWidth = width;
        this.screenHeight = height;
        this.listener = listener;
    }
    
    public void draw(Graphics g) {
    	g.fillOval(x,y,size,size);
    }

	public void move() {
        this.x += dirX;
        this.y += dirY;
        
        // Field borders
        if ( y<size*2 || y>=screenHeight-size) {
        	dirY = -dirY;
        	this.y += dirY;
        }      
        if ( x<=size/2  || x>screenWidth-size ) {
        	dirX = -dirX;
        	
        	if ( x<=size/2 ) {
        		listener.onBallLost();
        	}
        	this.x += dirX;
        }
	}

	private boolean wasColliding = false;
	
	public boolean handlePaddleCollision(int paddleX, int paddleY, int paddleWidth, int paddleLen) {
		boolean collided =
				x>=paddleX && x<=paddleX+paddleWidth
			&&  y>=paddleY && y<=paddleY+paddleLen;
		
		if ( collided ) {
			if ( !wasColliding ) {
				if ( dirX<0 ) {		
					listener.onBallHit();
					dirX = -dirX;
				}
			}
			wasColliding = true;
		} else {
			wasColliding = false;
		}
		return collided && !wasColliding;
	}
}

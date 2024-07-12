package com.pong;

import java.awt.Graphics;

public class Paddle {

    // Position of the paddle's center
    int x;
    int y;
    int len = 80;
    int width = 20;
    
    int screenWidth;
    int screenHeight;

    public Paddle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.screenWidth = width;
        this.screenHeight = height;
    }

    public void draw(Graphics g) {
        g.fillRect(this.x, this.y, width, len);
    }

    public void up() {
        this.y -= len / 4;
        if (this.y <=len / 4 ) {
        	this.y = 40;
        }
    }

    public void down() {
        this.y += len / 4;
        if (this.y>=screenHeight - (len+10)) {
        	this.y=screenHeight - (len+10);
        }
    }

}
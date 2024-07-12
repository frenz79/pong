package com.brain.neuralnet;

import java.awt.image.BufferedImage;

import com.brain.models.Direction;

public interface NeuralEnv {

	public BufferedImage getEnvImage();

	public void move(Direction up);

}

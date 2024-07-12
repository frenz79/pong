package com.brain.models;

public class Spike {
	
	public long time;
	public float intensity = 1.0f;
	public boolean isFeedback = false;
	
	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public float getIntensity() {
		return intensity;
	}

	public void setIntensity(float intensity) {
		this.intensity = intensity;
	}

	public Spike setFeedback(boolean isFeedback) {
		this.isFeedback = isFeedback;
		return this;
	}
}

package com.brain.factories;

import com.brain.models.Axon;
import com.brain.models.Neuron;
import com.brain.models.Spike;

public class CommonFactory {

	private static Spike lastSpike = null;
	
	public static Spike buildSpike( long time ) {
		if ( lastSpike!=null && lastSpike.time==time ) {
			return lastSpike;
		}
		lastSpike = new Spike();
		lastSpike.setTime(time);
		return lastSpike;
	}
	
	public static Spike buildSpike( long time, float intensity ) {
		Spike s = new Spike();
		s.setTime(time);
		s.setIntensity(intensity);
		return s;
	}
	
	public static Neuron buildNeuron( long id, int layerId, float xPos, float yPos, float zPos ) {
		Neuron a = new Neuron(id, layerId);
		a.setxPos(xPos);
		a.setyPos(yPos);
		a.setzPos(zPos);
		return a;
	}

	public static Axon buildAxon( Neuron src, Neuron dst ) {
		Axon a = new Axon( src,dst );
		src.addOutput(a);
		dst.addInput(a);
		return a;
	}
	
	public static Axon buildFeedbackAxon( Neuron dst ) {
		Axon a = new Axon(null,dst);
		dst.addInput(a);
		return a;
	}

	public static Axon buildActionAxon(Neuron src) {
		Axon a = new Axon(src,null);
		src.addOutput(a);
		return a;
	}
}

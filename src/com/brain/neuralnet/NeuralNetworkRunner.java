package com.brain.neuralnet;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import com.brain.models.Neuron;

public class NeuralNetworkRunner {
	
	private NeuralNetwork neuralNet;

	long time = 0L;
	public long getTime() {
		return time; 
	}
	
	long averageStepTime = 0L;
	long iterCounter     = 0L;
	
	long moveOnTime() {		
		long oldTime = time;
		time = System.nanoTime();
		
		averageStepTime += time-oldTime;
		iterCounter++;
		
		if ( iterCounter==1000 ) {
			System.out.println("Tick micros:"+TimeUnit.NANOSECONDS.toMicros(averageStepTime/iterCounter));
			iterCounter = 0L;
			averageStepTime = 0L;
		}
		return time;
	}
	
	public NeuralNetworkRunner start( NeuralNetwork n ) {
		this.neuralNet = n;
		final CyclicBarrier cyclicBarrier = new CyclicBarrier( neuralNet.getNeuronsLayer().size(), new Runnable() {
			@Override
			public void run() {
				moveOnTime();
			}			
		});
				
		moveOnTime();
		for (  Entry<Integer, List<Neuron>> entry : neuralNet.getNeuronsLayer().entrySet() ) {			
			new Thread(() -> {
				try {
					final List<Neuron> neurons = entry.getValue();
					int len = neurons.size();
					System.out.println("Starting layer["+entry.getKey()+"] thread -> "+neurons.size()+" neurons");
					
					while (true) {
						long now = getTime();
						for ( int i=0; i<len; i++ ) {
							neurons.get(i).process( now );
						}						
						cyclicBarrier.await();
					}
				} catch ( Exception ex ) {
					ex.printStackTrace();
				}
			}, "Runner[L="+entry.getValue().get(0).getLayerId()+"]").start();
		}
		return this;
	}
}

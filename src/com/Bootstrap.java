package com;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import com.brain.factories.CommonFactory;
import com.brain.models.Axon;
import com.brain.models.BrainRegion;
import com.brain.models.Direction;
import com.brain.models.Event;
import com.brain.models.Neuron;
import com.brain.models.Spike;
import com.brain.models.Neuron.NeuronStatus;
import com.brain.neuralnet.NeuralEnv;
import com.brain.neuralnet.NeuralEnvEventListener;
import com.brain.neuralnet.NeuralNetwork;
import com.brain.neuralnet.NeuralNetworkConfig;
import com.brain.neuralnet.NeuralNetworkRunner;
import com.brain.neuralnet.NeuralNetworkViewer;
import com.brain.neuralnet.SimpleNeuralNet;
import com.brain.utils.ImageUtils;
import com.pong.GameView;

public class Bootstrap {
	
	static int 
			FOV_X_SIZE = 40, 
			FOV_Y_SIZE = 30,
			LAYERS_SIZE[] = {40, 80, 60, 4},
			LAYERS_COUNT = 4,
			VIEW_SAMPLING_TIME_MS = 10
	;
	static float
			LAYER_NEURONS_DIST = 10.0f,
			LAYERS_DIST = 1000.0f	
	;	
	
	public static Spike SPIKE_WHITE = CommonFactory.buildSpike(0L, 2.00f);
	public static Spike SPIKE_BLACK = CommonFactory.buildSpike(0L, 0.01f);
	
	public static Spike SPIKE_FEEDBACK_GOOD = CommonFactory.buildSpike(0L,  10.0f).setFeedback( true );
	public static Spike SPIKE_FEEDBACK_BAD  = CommonFactory.buildSpike(0L, -100.0f).setFeedback( true );
	
	public static final void spikeFeeling( NeuralNetwork n, NeuralNetworkRunner r, boolean isBad ) {
	
		Spike spike = (isBad)?SPIKE_FEEDBACK_BAD:SPIKE_FEEDBACK_GOOD;
		
		List<Axon> regionUp = n.getOutputRegion( BrainRegion.MOTOR_REGION_UP );
		for ( Axon a : regionUp ) {
			Neuron motorN = a.getSrc();
			for ( Axon i : motorN.getIn() ) {
				if ( i.getSrc()!=null && NeuronStatus.RESTING.equals(i.getSrc().getStatus()) ) {
					i.strengthen( isBad );
					i.addSpike( spike );
				}
			}
		}
		
		List<Axon> regionDown = n.getOutputRegion( BrainRegion.MOTOR_REGION_DOWN );
		for ( Axon a : regionDown ) {
			Neuron motorN = a.getSrc();
			for ( Axon i : motorN.getIn() ) {
				if ( i.getSrc()!=null && NeuronStatus.RESTING.equals(i.getSrc().getStatus()) ) {
					i.strengthen( isBad );
					i.addSpike( spike );
				}
			}
		}
	}
	
	public static void main( String[] args ) throws InterruptedException, AWTException {		
		NeuralNetworkConfig config = new NeuralNetworkConfig();
		config.layersSize = LAYERS_SIZE;		
		config.numLayers = LAYERS_COUNT;
		config.layersDistance = LAYERS_DIST;
		config.neuronsDist = LAYER_NEURONS_DIST;		
		config.inLayerConn = new int[][] {{2,8},{10,2},{6,2}, {0,0}}; 		
		config.outLayerConn = new int[][] {{0,1,4,5}, {1,2,4,5}, {2,3,8,1}};
		
		NeuralNetwork network = new SimpleNeuralNet( config )
				.createInputRegion( BrainRegion.VIEW_REGION, FOV_X_SIZE, FOV_Y_SIZE )
				.createOutputRegion( BrainRegion.MOTOR_REGION_UP, 2, 2 )
				.createOutputRegion( BrainRegion.MOTOR_REGION_DOWN, 2, 2 )
		;
		
		NeuralNetworkRunner r = new NeuralNetworkRunner();
		r.start(network);
		
		NeuralEnv pongEnv = new GameView( new NeuralEnvEventListener() {			
			@Override
			public void onEvent( Event evt ) {
				spikeFeeling(network, r, evt.equals(Event.BAD));			
			}
		});
		
		startViewSamplingThread( network, pongEnv, r );
		startCollectActionsThread( network, pongEnv );	
		Thread.currentThread().join();
	}
	public static void startDisplayBrainThread( NeuralNetwork network, NeuralEnv pongEnv, NeuralNetworkRunner r) {		
		
	}
	
	public static void startViewSamplingThread( NeuralNetwork network, NeuralEnv pongEnv, NeuralNetworkRunner r) {		
		new Thread(() -> {
			NeuralNetworkViewer neuroView = new NeuralNetworkViewer();
			
			BufferedImage[] layerImages = new BufferedImage[LAYERS_COUNT];
			for ( int i=0; i<layerImages.length; i++ ) {
				layerImages[i] = new BufferedImage(LAYERS_SIZE[i], LAYERS_SIZE[i], BufferedImage.TYPE_INT_RGB);
			}	
			
			int idleColor    = Color.GRAY.getRGB();
			int restingColor = Color.RED.getRGB();
			int spikingColor = Color.YELLOW.getRGB();
			
			int idleColorV    = Color.BLACK.getRGB();
			int restingColorV = Color.ORANGE.getRGB();
			int spikingColorV = new Color(255, 200, 0).getRGB();
			
			int idleColorF    = Color.DARK_GRAY.getRGB();
			int restingColorF = Color.PINK.getRGB();
			int spikingColorF = new Color(255, 220, 0).getRGB();
			
			long lastSensorSpikeTime = 0L;
			while ( true ) {
				try {
					LockSupport.parkNanos( 1000L ); // 10micros
					long now = System.nanoTime();
					
					if ( (now-lastSensorSpikeTime) >  TimeUnit.MILLISECONDS.toNanos(VIEW_SAMPLING_TIME_MS) ) {						
						BufferedImage envImage = pongEnv.getEnvImage();
						if ( envImage!=null ) {
							BufferedImage scaledEnvImage = ImageUtils.scale2( envImage, FOV_X_SIZE/(double)envImage.getWidth(null) );
							neuroView.setImage(scaledEnvImage);
					        
							int[] pixels = ((DataBufferInt) scaledEnvImage.getRaster().getDataBuffer()).getData();						
							spikeViewSignal( pixels,  network, r ); 
						}
						lastSensorSpikeTime = now;
					}
					
					for ( int l=0; l<layerImages.length; l++ ) {
						int[] pixels = ((DataBufferInt) layerImages[l].getRaster().getDataBuffer()).getData();
						List<Neuron> neurons = network.getNeurons(l);
						if (neurons!=null) {
							for ( int i=0; i<neurons.size(); i++ ) {
								if ( neurons.get(i).getBrainRegion()==BrainRegion.VIEW_REGION ) {
									switch( neurons.get(i).getStatus() ) {
										case IDLE   : pixels[i] = idleColorV; break;
										case RESTING: pixels[i] = restingColorV; break;
										case SPIKING: pixels[i] = spikingColorV; break;
									}
								} /*else if ( neurons.get(i).getBrainRegion()==BrainRegion.FEEDBACK_REGION ) {
									switch( neurons.get(i).getStatus() ) {
										case IDLE   : pixels[i] = idleColorF; break;
										case RESTING: pixels[i] = restingColorF; break;
										case SPIKING: pixels[i] = spikingColorF; break;
									}
								} */else {
										switch( neurons.get(i).getStatus() ) {
										case IDLE   : pixels[i] = idleColor; break;
										case RESTING: pixels[i] = restingColor; break;
										case SPIKING: pixels[i] = spikingColor; break;
									}
								}
							}
							neuroView.setLayerImage(l, layerImages[l] );
						}
					}
					
				} catch ( Exception ex ) {
					ex.printStackTrace();
				}
			}			
		}).start();
	}
	
	public static void spikeViewSignal( int[] pixels, NeuralNetwork n, NeuralNetworkRunner r) {
		List<Axon> viewRegionAxons = n.getInputRegion( BrainRegion.VIEW_REGION );
		for ( int i=0; i<pixels.length; i++ ) {
			if ( pixels[i]!=0 ) {
				viewRegionAxons.get(i).addSpike(SPIKE_WHITE);
			} 
			/*else {
				viewRegionAxons.get(i).addSpike(SPIKE_BLACK);
			}*/
		}
	}	
	
	public static void startCollectActionsThread( NeuralNetwork network, NeuralEnv pongEnv ) {	
		new Thread(() -> {
			List<Axon> axonsUp   = network.getOutputRegion( BrainRegion.MOTOR_REGION_UP );
			List<Axon> axonsDown = network.getOutputRegion( BrainRegion.MOTOR_REGION_DOWN );
						
			float threshold = axonsUp.size()/5;
			
			while ( true ) {
				try {
					double direction = 0;					
					
					for( int i=0; i<axonsUp.size(); i++ ) {	
						Axon a = axonsUp.get(i);						
						direction += a.sumSpikes( );
					}
					for( int i=0; i<axonsDown.size(); i++ ) {	
						Axon a = axonsDown.get(i);						
						direction -= a.sumSpikes( );
					}
					
					if ( Math.abs(direction)>threshold) {
						if ( direction>0.0 ) {
						//	System.out.println("Net -> upKey ["+direction+"]>"+threshold);
							pongEnv.move( Direction.UP );
						}
						else if ( direction<0.0 ) {
						//	System.out.println("Net -> downKey ["+direction+"]>"+threshold);
							pongEnv.move( Direction.DOWN );
						}
					}
					
					Thread.sleep(10);
				} catch ( Exception ex ) {
					ex.printStackTrace();
				}
			}			
		}).start();
		
		System.out.println("Neurons: " + network.getNeurons().size() + " Axons:"+ network.getAxons().size() );
	}

}

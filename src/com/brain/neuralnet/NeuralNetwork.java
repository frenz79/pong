package com.brain.neuralnet;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.brain.factories.CommonFactory;
import com.brain.models.Axon;
import com.brain.models.BrainRegion;
import com.brain.models.Neuron;

public abstract class NeuralNetwork {

	final Random randomGen = new Random( System.currentTimeMillis() );
	
	private final NeuralNetworkConfig configuration;	
	private long  neuronCounter = -1;
	
	private final List<Neuron> neurons = new ArrayList<>();
	private final List<Axon>   axons   = new ArrayList<>();
	
	private final Map<BrainRegion,List<Axon>> inputAxons  = new EnumMap<>(BrainRegion.class);
	private final Map<BrainRegion,List<Axon>> outputAxons = new EnumMap<>(BrainRegion.class);
	
	private final Map<Integer,List<Neuron>> neuronsLayer = new HashMap<>();
	private final Map<Integer,Integer> neuronsLayerFirstFree = new HashMap<>();
		
	public abstract void buildInLayerAxons( List<Neuron> _neurons, int axonsGrowRange, int axonsGrowProbability );
	
	public abstract void buildOutLayerAxons( List<Neuron> _srcNeurons, List<Neuron> _dstNeurons, int axonsGrowRange, int axonsGrowProbability );

	public abstract void buildAxons( Neuron[] srcNeurons, Neuron[] dstneurons, int srcSize, int dstSize, int axonsGrowRange, int axonsGrowProbability  );

	
	public NeuralNetwork( NeuralNetworkConfig configuration ) {
		this.configuration = configuration;
		build();
	}
	
	private List<Neuron> buildLayer( int id, int size, float neuronsDist, float zPos ) {
		List<Neuron> ret = new ArrayList<>( size );				
		for (int x=0; x<size; x++ ) {
			for (int y=0; y<size; y++ ) {
				ret.add( CommonFactory.buildNeuron(++neuronCounter, id, x*neuronsDist, y*neuronsDist, zPos) );
			}
		}
		this.neuronsLayerFirstFree.put(id, 0);
		return ret;
	}	
		
	protected NeuralNetwork addNeuron( Neuron n ) {
		this.neurons.add( n );
		return this;
	}

	protected NeuralNetwork addNeurons( int layer, List<Neuron> n ) {
		List<Neuron> l = this.neuronsLayer.get(layer);
		if ( l==null ) {
			l = new ArrayList<Neuron>(n.size());
			this.neuronsLayer.put(layer, l);
		}
		l.addAll( n );		
		addNeurons( n );
		return this;
	}
	
	protected NeuralNetwork addNeurons( List<Neuron> n ) {
		this.neurons.addAll( n );
		return this;
	}
	
	protected NeuralNetwork addAxon( Axon a ) {
		this.axons.add( a );
		return this;
	}
	
	public List<Neuron> getNeurons() {
		return neurons;
	}
	
	public List<Neuron> getNeurons(int layer) {
		return neuronsLayer.get(layer);
	}

	public List<Axon> getAxons() {
		return axons;
	}
	/*
	public String dumpActionsResult( BrainRegion region ) {
		boolean noActions = true;		
		StringBuilder str = new StringBuilder();
		
		for ( Axon a : outputAxons.get(region) ) {			
			StringBuilder tmp = new StringBuilder();
			for ( Spike s : a.getSpikes() ) {
				tmp.append(s.getIntensity()).append(" ");
				noActions = false;
			}			
			str.append("OUT: ")
			   .append( a.getSrc().getId() )
			   .append(" -> ")
			   .append( (tmp.length()==0)?"0 ":tmp );
		}
		
		if ( noActions ) {
			return null;
		}
		return str.toString();
	}
*/
	public int getLayerSize( int l ) {
		return configuration.layersSize[l];
	}

	private NeuralNetwork build( ) {			
		List<List<Neuron>> layers = new ArrayList<List<Neuron>>( configuration.numLayers );
		for ( int i=0; i<configuration.numLayers; i++ ) {
			List<Neuron> layer = buildLayer(i, configuration.layersSize[i], configuration.neuronsDist, i * configuration.layersDistance );
			buildInLayerAxons( layer, configuration.inLayerConn[i][0], configuration.inLayerConn[i][1] );			
			layers.add(i, layer );
			addNeurons(i, layer );
		}
		
		for ( int i=0; i<configuration.outLayerConn.length; i++ ) {
			buildOutLayerAxons( 
				layers.get(configuration.outLayerConn[i][0]), 
				layers.get(configuration.outLayerConn[i][1]),
				configuration.outLayerConn[i][2],
				configuration.outLayerConn[i][3]
			);	
		}
		return this;
	}
	
	public NeuralNetwork createInputRegion( BrainRegion region, int w, int h ) {
		int layerId =  0 ;
		int startId = neuronsLayerFirstFree.get(layerId);
		int endId   = startId+w*h;
				
		List<Neuron> n = getNeurons(layerId);
		List<Axon> a = inputAxons.get(region);
		
		if ( a==null ) {
			a = new ArrayList<Axon>(w*h);
			inputAxons.put(region, a);
		}
		for ( int i=startId; i<endId; i++ ) {
			a.add( CommonFactory.buildFeedbackAxon( n.get(i) ));
			n.get(i).setBrainRegion(region);
		}
		
		this.neuronsLayerFirstFree.put(layerId,endId);		
		return this;
	}
	
	public NeuralNetwork createOutputRegion( BrainRegion region, int w, int h ) {
		int layerId =  configuration.numLayers-1 ;
		int startId = neuronsLayerFirstFree.get(layerId);
		int endId   = startId+w*h;
		
		List<Neuron> n = getNeurons(layerId);
		List<Axon> a = outputAxons.get(region);
		
		if ( a==null ) {
			a = new ArrayList<Axon>();
			outputAxons.put(region, a);
		}		

		for ( int i=startId; i<endId; i++ ) {
			a.add( CommonFactory.buildActionAxon( n.get(i) ));
		}
		this.neuronsLayerFirstFree.put(layerId,endId);
		return this;
	}
	
	public List<Axon> getInputRegion( BrainRegion region ) {
		return inputAxons.get(region);
	}
	
	public List<Axon> getOutputRegion( BrainRegion region ) {
		return outputAxons.get(region);
	}

	public Map<Integer, List<Neuron>> getNeuronsLayer() {
		return neuronsLayer;
	}
}

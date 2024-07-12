package com.brain.neuralnet;

import java.util.List;

import com.brain.factories.CommonFactory;
import com.brain.models.Neuron;

public class SimpleNeuralNet extends NeuralNetwork {
	
	public  SimpleNeuralNet( NeuralNetworkConfig configuration ) {
		super( configuration );
	}

	@Override
	public void buildInLayerAxons(List<Neuron> _neurons, int axonsGrowRange, int axonsGrowProbability) {
		Neuron[] neurons = _neurons.toArray(new Neuron[] {});		
		buildAxons( neurons, neurons, getLayerSize(neurons[0].getLayerId()), getLayerSize(neurons[0].getLayerId()), axonsGrowRange, axonsGrowProbability );
	}

	@Override
	public void buildOutLayerAxons(List<Neuron> _srcNeurons, List<Neuron> _dstNeurons, int axonsGrowRange, int axonsGrowProbability) {
		Neuron[] srcNeurons = _srcNeurons.toArray(new Neuron[] {});
		Neuron[] dstNeurons = _dstNeurons.toArray(new Neuron[] {});	
		buildAxons( srcNeurons, dstNeurons, getLayerSize(srcNeurons[0].getLayerId()), getLayerSize(dstNeurons[0].getLayerId()), axonsGrowRange, axonsGrowProbability );
	}	
	
	private int scale( float scaleFact, int coordinate ) {
		if ( scaleFact==1.0f ) {
			return coordinate;
		}
		if ( coordinate==0 ) {
			return 0;
		}
		return (int) (scaleFact*coordinate);
	}
	
	private void addAxon( Neuron srcNeuron, Neuron[] dstneurons, int dstCoordinate, int axonsGrowProbability, float scaleFact ) {
		int scaledCoord = scale(scaleFact, dstCoordinate);
		if (randomGen.nextInt(100)<axonsGrowProbability && (scaledCoord) < dstneurons.length && (scaledCoord) >= 0 ) { 
			addAxon( CommonFactory.buildAxon( srcNeuron, dstneurons[scaledCoord] ) );
		}
	}	
	
	@Override
	public void buildAxons( Neuron[] srcNeurons, Neuron[] dstneurons, int srcSize, int dstSize, int axonsGrowRange, int axonsGrowProbability ) {
		float scaleFact = (float)dstSize / (float)srcSize;
		
		for ( int i=0; i<srcNeurons.length-axonsGrowRange; i++) {			
			for ( int x=1; x<axonsGrowRange; x++ ) {
				Neuron srcNeuron = srcNeurons[i];
				addAxon( srcNeuron, dstneurons,  (i + x ), axonsGrowProbability, scaleFact );
				addAxon( srcNeuron, dstneurons,  (i + (dstSize+1)*x), axonsGrowProbability, scaleFact );
				addAxon( srcNeuron, dstneurons,  (i + dstSize*x    ), axonsGrowProbability, scaleFact );
				addAxon( srcNeuron, dstneurons,  (i + (dstSize-1)*x), axonsGrowProbability, scaleFact );
				addAxon( srcNeuron, dstneurons,  (i - (dstSize+1)*x), axonsGrowProbability, scaleFact );
				addAxon( srcNeuron, dstneurons,  (i - dstSize*x), axonsGrowProbability, scaleFact );
				addAxon( srcNeuron, dstneurons,  (i - (dstSize-1)*x), axonsGrowProbability, scaleFact );
				addAxon( srcNeuron, dstneurons,  (i - x ), axonsGrowProbability, scaleFact );
				/*
				if (rnd.nextInt(100)<axonsGrowProbability && (i + x         ) < dstneurons.length ) 
					addAxon( factory.buildAxon( srcNeuron, dstneurons[i + x] ) );
				if (rnd.nextInt(100)<axonsGrowProbability && (i + (dstSize+1)*x) < dstneurons.length ) 
					addAxon( factory.buildAxon( srcNeuron, dstneurons[i + (dstSize+1)*x] ) );
				if (rnd.nextInt(100)<axonsGrowProbability && (i + dstSize*x    ) < dstneurons.length ) 
					addAxon( factory.buildAxon( srcNeuron, dstneurons[i + dstSize*x] ) );
				if (rnd.nextInt(100)<axonsGrowProbability && (i + (dstSize-1)*x) < dstneurons.length ) 
					addAxon( factory.buildAxon( srcNeuron, dstneurons[i + (dstSize-1)*x] ) );
				if (rnd.nextInt(100)<axonsGrowProbability && (i - (dstSize+1)*x) >= 0 ) 
					addAxon( factory.buildAxon( srcNeuron, dstneurons[i - (dstSize+1)*x] ) );
				if (rnd.nextInt(100)<axonsGrowProbability && (i - dstSize*x    ) >= 0 ) 
					addAxon( factory.buildAxon( srcNeuron, dstneurons[i - dstSize*x] ) );
				if (rnd.nextInt(100)<axonsGrowProbability && (i - (dstSize-1)*x) >= 0 ) 
					addAxon( factory.buildAxon( srcNeuron, dstneurons[i - (dstSize-1)*x] ) );
				if (rnd.nextInt(100)<axonsGrowProbability && (i - x         ) >= 0 ) 
					addAxon( factory.buildAxon( srcNeuron, dstneurons[i - x] ) );
				*/				
			}
		}
	}	

}

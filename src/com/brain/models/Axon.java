package com.brain.models;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Axon {

	public static int diedAxons = 0;
	
	public static long travelSpeed = 1L;
	
	private static long sequence = 0L;
	
	private final long id;
	private final Neuron src;
	private final Neuron dst;
	private final double length;
	private final long latency;
	
	private float strengthCap    = 2.0f;
	private float strengthFactor = 1.0f;
	private final float incStrengthFactorStep = 0.01f;
	private final float decStrengthFactorStep = 0.01f;
	
	private final List<Spike> spikes = new LinkedList<>( );
	
	@Override
	public String toString() {
		return  ((src!=null)?src.toString():"NULL")
				+" -> "+
				((dst!=null)?dst.toString():"NULL")
		;
	}
	
	public void strengthen( boolean increase ) {
		if (increase==true && strengthFactor<strengthCap) {
			strengthFactor += incStrengthFactorStep;
		}
		else if (increase==false && strengthFactor>-strengthCap){
			strengthFactor -= decStrengthFactorStep;
		}		
		//if ( strengthFactor<=0.0f ) {
		//	diedAxons++;
		//	System.out.println("Died Axons = "+diedAxons);
		//}
	}
	
	public boolean isDead() {
		return strengthFactor<=0.0f;
	}
	
	public Axon( Neuron src, Neuron dst ) {
		this.id = sequence++;
		this.src = src;
		this.dst = dst;
	
		if ( this.src!=null && this.dst!=null ) {
			this.length = Math.sqrt( 
				  Math.pow(src.getxPos()-dst.getxPos(), 2) 
				+ Math.pow(src.getyPos()-dst.getyPos(), 2) 
				+ Math.pow(src.getzPos()-dst.getzPos(), 2) 
			);	
			this.latency = 1L + (long)this.length / travelSpeed;
		} else {
			this.length = 0L;
			this.latency = 0L;
		}
	}

	private int feedbackSpikesCounter = 0;
	
	public Axon addSpike(Spike s) {
		wLock.lock();
		try {
			this.spikes.add(s);
			if ( s.isFeedback ) {
				feedbackSpikesCounter++;
			}
			return this;
		} finally {
			wLock.unlock();
		}
	}
	
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock rLock = lock.readLock();
	private final Lock wLock = lock.writeLock();
	
	public float sumSpikes( ) {
		float ret = 0.0f;
		if ( strengthFactor == 0.0f ) {
			return ret;
		}		
		wLock.lock();
		try {			
			for ( int i=spikes.size()-1; i>=0; i-- ) {
				ret += spikes.get(i).intensity * strengthFactor;
				spikes.remove(i);
			}			
		} finally {
			wLock.unlock();
		}
		return ret;
	}
	
	private static final List<Spike>  EMPTY_SPIKE_LIST = new ArrayList<>();	
	
	public List<Spike> getFeebackSpikes() {
		rLock.lock();
		try {
			if ( spikes.isEmpty() || feedbackSpikesCounter==0 ) {
				return EMPTY_SPIKE_LIST;
			}
			List<Spike> ret = new ArrayList<>( feedbackSpikesCounter );			
			for ( int i=0; i<spikes.size(); i++ ) {
				Spike s = spikes.get(i);
				if ( s.isFeedback ) {				
					ret.add(s);
					feedbackSpikesCounter--;
					if ( feedbackSpikesCounter==0 ) {
						break;
					}
				}
			}
			return ret;
		} finally {
			rLock.unlock();
		}
	}
	
	public List<Spike> pullSpikes( long timeThres ) {
		wLock.lock();
		try {
			List<Spike> ret = new ArrayList<>( spikes.size() );			
			for ( int i=spikes.size()-1; i>=0; i-- ) {
				Spike s = spikes.get(i);
				if ( s.time < timeThres ) {
					ret.add(s);
					spikes.remove( i );
				}
			}
			return ret;
		} finally {
			wLock.unlock();
		}
	}
	
	public long getTravelTime() {
		return latency;
	}

	public double getLength() {
		return length;
	}

	public Neuron getSrc() {
		return src;
	}

	public Neuron getDst() {
		return dst;
	}

	public float getStrengthFactor() {
		return strengthFactor;
	}

	public void clearSpikes() {
		wLock.lock();
		try {
			this.spikes.clear();
		} finally {
			wLock.unlock();
		}
	}

	public long getId() {
		return id;
	}

	public boolean hasSpikes() {
		rLock.lock();
		try {
			return !this.spikes.isEmpty();
		} finally {
			rLock.unlock();
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Axon other = (Axon) obj;
		return id == other.id;
	}

}

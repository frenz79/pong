package com.brain.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.brain.factories.CommonFactory;

public class Neuron {

	public static CommonFactory commonFactory;
	
	private final long id;
	private final int layerId;
	private BrainRegion region;
	
	private float xPos, yPos, zPos;
	private List<Axon> out = new ArrayList<>();
	private List<Axon> in  = new ArrayList<>();
	
	private long lastFireTime = 0L;
	
	private long refractoryPeriod = TimeUnit.MILLISECONDS.toNanos(14);
	private float accumulator;
	
	private float accumulatorThreshold = 1.0f;
	
	@Override
	public String toString() {
		return "L:"+layerId+";ID:"+id+";POS:"+xPos+","+yPos+","+zPos;
	}
	
	public enum NeuronStatus {
		RESTING,
		IDLE,
		SPIKING
	}
	
	private NeuronStatus status = NeuronStatus.IDLE;
	
	private void handleResting() {
		if ( !BrainRegion.MOTOR_REGION_UP.equals( getBrainRegion() ) 
		  && !BrainRegion.MOTOR_REGION_DOWN.equals( getBrainRegion() )) {
			for ( int i=0; i<out.size(); i++ ) {
				Axon a = out.get(i);
				if ( a.hasSpikes() ) {
					Neuron src = a.getSrc();
					if ( src!=null && NeuronStatus.RESTING.equals(src.getStatus()) ) {
						for ( Spike s : a.getFeebackSpikes() ) {
							for ( Axon a1 : src.getOut() ) {
								if ( this.equals(a1.getSrc()) ) {
									a1.addSpike(s);
									a1.strengthen( false );
								}
							}
						}
					}
				}
			}
		}
		
		for ( int i=0; i<in.size(); i++ ) {
			Axon a = in.get(i);
			if ( !a.isDead() ) {
		//		a.strengthen( false );
				a.clearSpikes();
			}
		}		
		return;
	}
	
	private void handleIdle( Spike outSpike, long now ) {		
		for ( int i=0; i<in.size(); i++ ) {
			Axon a = in.get(i);
			if ( !a.isDead() && a.hasSpikes() ) {
				long timeThres = now-a.getTravelTime();
				List<Spike> spikes = a.pullSpikes( timeThres );
				for ( int j=0; j<spikes.size(); j++ ) {
					if ( accumulateInput( outSpike, now, spikes.get(j).intensity*a.getStrengthFactor() ) ) {
						a.strengthen( true );
						break;
					}
				}
			}
		}
	}
	
	public void process( long now ) {
		//outSpike.setTime(now);		
		if ( lastFireTime>0L && now - lastFireTime < refractoryPeriod ) {
			this.status = NeuronStatus.RESTING;
			handleResting();
		} else {
			Spike outSpike = commonFactory.buildSpike( now );
			this.status = NeuronStatus.IDLE;
			handleIdle( outSpike, now );
		}		
	}
	
	private float getAccumulatorThreshold() {
		return this.accumulatorThreshold;
	}
	
	private boolean accumulateInput( Spike outSpike, long now, float intensity) {
		this.accumulator += intensity;

		if ( this.accumulator >= getAccumulatorThreshold()) {
			//	StringBuilder outSpike = new StringBuilder();
			
			List<Axon> ax = getOut();
			for ( int i=0; i<ax.size(); i++ ) {
				Axon a = ax.get(i);
	
				if ( !a.isDead() ) {
					a.addSpike( outSpike );
				}

				//		if ( a.getDst()!=null ) {
				//			outSpike.append( a.getDst().id ).append(",");
				//		}
			}

			//	if ( outSpike.length()>0 ) {
			//	System.out.println("Spiking ["+id+"] -> ["+outSpike.toString()+"]");
			//	}
			this.lastFireTime = now;
			this.status = NeuronStatus.SPIKING;
			return true;
		}

		return false;
	}
	
	public void addOutput( Axon a ) {
		this.out.add(a);
	}

	public void addInput( Axon a ) {
		this.in.add(a);
	}
	
	public Neuron( long id, int layerId ) {
		this.id = id;
		this.layerId = layerId;
	}

	public float getxPos() {
		return xPos;
	}

	public void setxPos(float xPos) {
		this.xPos = xPos;
	}

	public float getyPos() {
		return yPos;
	}

	public void setyPos(float yPos) {
		this.yPos = yPos;
	}

	public float getzPos() {
		return zPos;
	}

	public void setzPos(float zPos) {
		this.zPos = zPos;
	}

	public List<Axon> getOut() {
		return out;
	}

	public void setOut(List<Axon> out) {
		this.out = out;
	}

	public List<Axon> getIn() {
		return in;
	}

	public void setIn(List<Axon> in) {
		this.in = in;
	}

	public long getId() {
		return id;
	}

	public NeuronStatus getStatus() {
		return status;
	}

	public void setStatus(NeuronStatus status) {
		this.status = status;
	}

	public int getLayerId() {
		return layerId;
	}

	public BrainRegion getBrainRegion() {
		return region;
	}

	public void setBrainRegion(BrainRegion region) {
		this.region = region;
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
		Neuron other = (Neuron) obj;
		return id == other.id;
	}
}

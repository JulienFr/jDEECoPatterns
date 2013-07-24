package cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.highload;

import java.util.Random;

import cz.cuni.mff.d3s.spl.core.Data;
import cz.cuni.mff.d3s.spl.core.StatisticSnapshot;

public class ScpHSComponentData implements Data{

	public static final ScpHSComponentData INSTANCE = new ScpHSComponentData();
	
	Random rand = new Random();
	
	public ScpHSComponentData() {
	}
	
	public Long generate() {
		return ((Integer) rand.nextInt(100)).longValue();
	}

	@Override
	public StatisticSnapshot getStatisticSnapshot() {
		return new LoadStatistics(generate());
	}

	@Override
	public void addValue(long when, long value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void __clear() {
		/* Do nothing. */
	}
	
	private class LoadStatistics implements StatisticSnapshot {
		private double mean;
		
		public LoadStatistics(Long load) {
			mean = load;
		}

		@Override
		public double getArithmeticMean() {
			return mean;
		}

		@Override
		public long getSampleCount() {
			return 1;
		}

		@Override
		public long[] getSamples() {
			throw new UnsupportedOperationException("Original samples not available.");
		}
		
	}
}

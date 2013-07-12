package cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.deployment3;

import java.util.Comparator;

public class LinkComparator implements Comparator<Link> {

	@Override
	public int compare(Link ml1, Link ml2) {
		Object d1 = ml1.getDistance();
		Object d2 = ml2.getDistance();
		if (d1.getClass().equals(Integer.class) && d2.getClass().equals(Integer.class)){
			return Integer.compare((Integer)d1, (Integer)d2);
		}
		return 0;
	}
}

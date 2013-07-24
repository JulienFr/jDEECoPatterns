package cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.highload;

import java.util.List;
import java.util.Map;

import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.KnowledgeExchange;
import cz.cuni.mff.d3s.deeco.annotations.Membership;
import cz.cuni.mff.d3s.deeco.annotations.Out;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.annotations.Selector;
import cz.cuni.mff.d3s.deeco.ensemble.Ensemble;
import cz.cuni.mff.d3s.deeco.knowledge.Component;
import cz.cuni.mff.d3s.deeco.knowledge.OutWrapper;
import cz.cuni.mff.d3s.spl.core.Data;
import cz.cuni.mff.d3s.spl.core.Formula;
import cz.cuni.mff.d3s.spl.core.Result;
import cz.cuni.mff.d3s.spl.core.impl.SimpleFormulas;
import cz.cuni.mff.d3s.spl.sources.SystemLoad;
import cz.cuni.mff.spl.SPL;

/**
 * 
 * @author Julien Malvot
 *
 */
public class BalanceHSEnsemble extends Ensemble {
	
	private static final long serialVersionUID = 1L;
	
	@PerformanceEvaluation
	public static Boolean migrateApp(){
		// instantiate a scp component data source
		Data scpLoad = ScpHSComponentData.INSTANCE;
		// high load for 50%
		Formula belowHighLoad = SimpleFormulas.createSmallerThanConst("load", 50);
		// bind the scp data source with the load variable
		belowHighLoad.bind("load", scpLoad);
		// evaluate the formula
		if (belowHighLoad.evaluate() == Result.TRUE){
			// the load is ok, no need of anything
			return false;
		}
		// the load is too high, need to ask the Zimory Platform to start a new VM with more power
		return true;
	}
	
	
	@Membership
	public static Boolean membership(
			// AppComponent coordinator
			@In("coord.id") String cId,
			@In("coord.scpId") String cScpId,
			@In("coord.isDeployed") Boolean cIsDeployed,
			// ScpComponent member
			@In("member.id") String mId,
			@In("member.load") Long mLoad
			) { 
		if (cIsDeployed && cScpId.equals(mId)){
			// ...
			return true;
		}
		return false;
	}
	
	@KnowledgeExchange
	@PeriodicScheduling(3000)
	public static void map(
			// AppComponent coordinator
			@In("coord.id") String cId,
			// AppComponent member
			@In("member..id") String mId) {
		// do something
	}
}

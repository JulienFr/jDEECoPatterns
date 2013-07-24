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
	
	@PerformanceRequirement
	public static Boolean loads(@In("coord.id") String cId, @In("member.id") String mId){
		// retrieve the component from the id, the user can not modify the data as
		// we would not allow any inout or out parameters here
		Component c = getComponentFromId(cId);
		Component m = getComponentFromId(mId);
		AppHSComponent ahc = null;
		ScpHSComponent shc = null;
		// get the application component
		if (c.getClass().isAssignableFrom(AppHSComponent.class))
			ahc = (AppHSComponent) c;
		else return false;
		// get the scp component
		if (m.getClass().isAssignableFrom(ScpHSComponent.class))
			shc = (ScpHSComponent) m;
		else return false;
		
		Data cLoad = ...;
		Data mLoad = ...;
		Formula machineIdle = SimpleFormulas.createSmallerThanConst("load", 0.2);
		machineIdle.bind("load", cLoad);
		Formula smallLoad = SimpleFormulas.createSmallerThanConst("load", 0.6);
		smallLoad.bind("load", mLoad);
		
		// launch spl with the previous init
		
		// get results and see if it's satisfactory
		
		// return true if so
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

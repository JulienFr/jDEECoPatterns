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
		// retrieve the component from the id, the user could in anyway modify the data as
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
			// only after some preconditions, we can come to the selector computations
			// scp selection
			/*scpSelector(msScpSelectors.value, msScpIds,  scpLatencies);
			// app selection
			for (int i = 0; i < msAppIds.size(); i++){
				msAppSelectors.value.set(i, appSelector(cAppId, cAppRunningOn, msAppIds.get(i), msAppRunningOn.get(i))); 
			}*/
			// here we go
			return true;
		}
		return false;
	}

	// to expand to different cdScpInstanceIds in case of high candidate range
	@KnowledgeExchange
	@PeriodicScheduling(3000)
	public static void map(
			// AppComponent coordinator (1)
			@In("coord.id") String cAppId,
			@InOut("coord.scpId") OutWrapper<String> cAppScpId,
			@Out("coord.isDeployed") OutWrapper<Boolean> cAppIsDeployed,
			// AppComponent members (n-1)
			@In("members.App.id") List<String> msAppIds,
			@InOut("members.App.scpId") OutWrapper<List<String>> msAppScpIds,
			@InOut("members.App.isDeployed") OutWrapper<List<Boolean>> msAppIsDeployed,
			// ScpComponent members (n)
			@In("members.Scp.id") List<String> msScpIds,
			@InOut("members.Scp.appIds") OutWrapper<List<List<String>>> msScpAppIds) {
		String appComponentIds = cAppId;
		String scpComponentIds = msScpIds.get(0);
		// all AppComponents are now deployed by ScpComponents
		cAppScpId.value = msScpIds.get(0);
		cAppIsDeployed.value = Boolean.TRUE;
		msScpAppIds.value.get(0).add(cAppId);
		// linkage
		for (int i = 0; i < msAppIsDeployed.value.size(); i++){
			msAppIsDeployed.value.set(i,Boolean.TRUE);
			msAppScpIds.value.set(i, msScpIds.get(i+1));
			// app component id registration into the scp component
			// regarding of the first assigned id for the coordinator
			msScpAppIds.value.get(i+1).add(msAppIds.get(i));
			
			appComponentIds += " " + msAppIds.get(i);
			scpComponentIds += " " + msScpIds.get(i+1);
		}
		System.out.println("coordinator="+cAppId+ 
							"   AppComponents=" + appComponentIds + 
							"   ScpComponents=" +scpComponentIds);
	}
}

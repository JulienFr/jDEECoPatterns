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
import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.ENetworkId;
import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.LatencyGenerator;
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
	
	@Membership
	public static Boolean membership(
			// AppComponent coordinator
			@In("coord.id") String cId,
			@In("coord.scpId") String cScpId,
			@In("coord.isDeployed") Boolean cIsDeployed,
			// ScpComponent member
			@In("member.id") String mId
			) { 
		if (cIsDeployed && cScpId.equals(mId)){
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
		return false;
	}
	
	@KnowledgeExchange
	@PeriodicScheduling(3000)
	public static void map(
			// AppComponent coordinator
			@In("coord.id") String cId,
			@Out("coord.scpId") OutWrapper<String> cScpId,
			// AppComponent member
			@In("member.id") String mId,
			@In("member.machineId") String machineId,
			@In("member.networkId") ENetworkId networkId,
			@InOut("member.onAppIds") OutWrapper<List<String>> onAppIds) {
		// id of the new spawned ScpComponent
		String newScpId = "IMTZimory";
		// attach the new ScpComponent to the AppComponent
		cScpId.value = newScpId;
		// detach the ScpComponent from the AppComponent
		onAppIds.value.remove(cId);
		// spawn a new component into the runtime
		ScpHSComponent newScpComponent = new ScpHSComponent(newScpId, networkId);
		newScpComponent.machineId = machineId;
		newScpComponent.onAppIds.add(cId);
		// latencies generated between the node and the others
		// but this would mean altering the knowledge of all scp components to add a new latency with this node
		// to be implemented..
		//LatencyGenerator.generate(newScpComponent, LocalLauncherHSNoJPF.scpComponents, 80, false);
		// the spawn includes 
		LocalLauncherHSNoJPF.registerComponent(newScpComponent);
	}
}

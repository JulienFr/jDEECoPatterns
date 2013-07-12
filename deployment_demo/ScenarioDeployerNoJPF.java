package cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.deployment3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.cuni.mff.d3s.deeco.knowledge.Component;
import cz.cuni.mff.d3s.deeco.knowledge.KnowledgeManager;
import cz.cuni.mff.d3s.deeco.knowledge.RepositoryKnowledgeManager;
import cz.cuni.mff.d3s.deeco.knowledge.local.LocalKnowledgeRepository;
import cz.cuni.mff.d3s.deeco.provider.AbstractDEECoObjectProvider;
import cz.cuni.mff.d3s.deeco.provider.ClassDEECoObjectProvider;
import cz.cuni.mff.d3s.deeco.provider.InitializedDEECoObjectProvider;
import cz.cuni.mff.d3s.deeco.runtime.Runtime;
import cz.cuni.mff.d3s.deeco.runtime.middleware.LinkedMiddlewareEntry;
import cz.cuni.mff.d3s.deeco.runtime.middleware.network.ENetworkId;
import cz.cuni.mff.d3s.deeco.scheduling.MultithreadedScheduler;
import cz.cuni.mff.d3s.deeco.scheduling.Scheduler;

/**
 * The scenario
 * Science Cloud Platform Instances SCPis
 * 
 * @author Julien Malvot
 *
 */
public class ScenarioDeployerNoJPF {
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// no non-initialized components
		List<Class<?>> components = Arrays
				.asList(new Class<?>[] {});
		List<Class<?>> ensembles = Arrays
				.asList(new Class<?>[] { LinkEnsemble.class });
		KnowledgeManager km = new RepositoryKnowledgeManager(
				new LocalKnowledgeRepository());
		Scheduler scheduler = new MultithreadedScheduler();
		AbstractDEECoObjectProvider dop = new ClassDEECoObjectProvider(
				components, ensembles);
		Runtime rt = new Runtime(km, scheduler);
		rt.registerComponentsAndEnsembles(dop);
	
		List<Component> scpComponents = new ArrayList<Component>( 
			Arrays.asList(
				// 3 SCPis at the LMU Munich 
				new ScpComponent("LMU1", ENetworkId.LMU_MUNICH),
				new ScpComponent("LMU2", ENetworkId.LMU_MUNICH),
				new ScpComponent("LMU3", ENetworkId.LMU_MUNICH),
				// 3 SCPis at the IMT Lucca
				new ScpComponent("IMT1", ENetworkId.IMT_LUCCA),
				new ScpComponent("IMT2", ENetworkId.IMT_LUCCA),
				new ScpComponent("IMT3", ENetworkId.IMT_LUCCA),
				new ScpComponent("EGM1", ENetworkId.EN_GARDEN))
		);
		// list of all components which are part of the system
		List<Component> cloudComponents = new ArrayList<Component>(scpComponents);	
		// 2 Application Instances to be deployed in the cloud
		cloudComponents.add(new AppComponent("APP1"));
		cloudComponents.add(new AppComponent("APP2"));
		
		// initialize the singleton of the RandomIntegerDistanceMiddlewareEntry to generate the latency distances
		// these will be dynamically provided by the communication middleware if jDEECo gets connected to it
		LinkedMiddlewareEntry middlewareEntry = LinkedMiddlewareEntry.getMiddlewareEntrySingleton();
		middlewareEntry.getSla().maxLinkLatency = 50;
		middlewareEntry.updateDistanceTopology(scpComponents, 80);
		// print the matrix
		String[] matrix = middlewareEntry.distanceLinksToString();
		for (int i = 0; i < matrix.length; i++)
			System.out.println(matrix[i]);
		
		matrix = middlewareEntry.distanceLinksToStringWithSLA();
		for (int i = 0; i < matrix.length; i++)
			System.out.println(matrix[i]);
				
		// initialize the DEECo with input initialized components
		dop = new InitializedDEECoObjectProvider(cloudComponents, null);
		rt.registerComponentsAndEnsembles(dop);
	
		rt.startRuntime();
	}
}

package cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.deployment3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cz.cuni.mff.d3s.deeco.annotations.Process;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.Out;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.knowledge.Component;
import cz.cuni.mff.d3s.deeco.knowledge.OutWrapper;

/**
 * 
 * @author Julien Malvot
 *
 */
public class AppComponent extends Component {
	
	public final static long serialVersionUID = 1L;

	/**
	 * id of the SCP instance which the application component is processed by
	 */
	public String scpId;
	
	/**
	 * flag for deployment
	 */
	public Boolean isDeployed;
	
	/**
	 * constructor for the Application Component
	 * @param id
	 */
	public AppComponent(String id) {
		this.id = id;
		this.scpId = null;
	}
	
	/*@Process
	@PeriodicScheduling(5000)
	public static void process(@In("id") String id, @Out("isDeployed") OutWrapper<Boolean> isDeployed) {
		isDeployed.value = Boolean.FALSE;
		System.out.println(id + " has been undeployed");
	}*/
}

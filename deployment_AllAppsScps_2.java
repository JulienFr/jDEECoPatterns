public class LinkEnsemble extends Ensemble {
	
	private static final long serialVersionUID = 1L;

	
	private static final int appGroupSize = 5;
	private static final int scpGroupSize = 5;
	// The filters apply prior to the membership, that allows the user for more restrictions on the input ids
	// instead of letting the framework implementing it. (this would weaken the core pretty much, as well as the user abilities)
	// They follow the same pattern structure as the membership, with the constraints below :
	// - they can use (or not) the coordinator
	// - they are no other outputs than a list of strings, no inOut and out parameters
	// - they can reference only one group member (this explains why we do not need an identifier after the members ensemble party for the paths), 
	// 		the reference is seen in the member group annotation of the membership
	// 		so then it guarantees the filter to be applied for different input atomic group members
	/**
	 * Filter for the application component
	 */
	@Filter
	public static List<String> appFilter(
			@In("coord.id") String cId,
			@In("coord.runningOn") String cRunningOn,
			// the size of these two arrays is taken from the member group annotation of the membership
			// so then the filter can be applied on different group members with different sizes
			@In("members.id") List<String> mIds,
			@In("members.runningOn") List<String> runningOns){
		// apply filtering however the user wants on the different latency parameters,
		// this way, it gives a high flexibility for selecting the desired id
		// the filters can be strongly constrained by the framework, by disallowing some operations
		// it only applies to the knowledge of a single member type as the paths are local to a node
		List<String> filteredIds = new ArrayList<String> ();
		// filter the input mIds, and returning appGroupSize number of ids
		// the coordinator is running on machine X,
		// all the other members must be also running on machine X, chekc this out in some condition
		// if the output size does not match the size in the annotation, an exception is thrown
		return filteredIds;
	}
	/**
	 * Filter for the scp component
	 */
	@Filter
	public static List<String> scpFilter(
			@In("members.id") List<String> scpIds,
			@In("members.latency") List<Map<String, Object>> scpLatencies){
		List<String> filteredIds = new ArrayList<String> ();
		// can use intermediate user-defined functions to manipulate the latencies
		// until getting a strict and limited set of filtered scpIds
		// the input and output are declared
		return filteredIds;
	}
	/**
	 * Membership function
	 */
	@Membership
	@Members({
		// we state which filters are to be considered
		// this way, we can have the possibility to use the same filter for different member groups
		@MemberGroup(identifier="App", filter="appFilter", size=appGroupSize),
		@MemberGroup(identifier="Scp", filter="scpFilter", size=scpGroupSize)
	})
	public static Boolean membership(
			// AppComponent coordinator
			@In("coord.id") String cAppId,
			@In("coord.scpId") String cAppScpId,
			@In("coord.isDeployed") Boolean cAppIsDeployed,
			// AppComponent members
			@In("members.App.id") List<String> msAppIds,
			@In("members.App.scpId") List<String> msAppScpIds,
			@In("members.App.isDeployed") List<Boolean> msAppIsDeployed,
			// ScpComponent members
			@In("members.Scp.id") List<String> msScpIds,
			@In("members.Scp.appIds") List<List<String>> msScpAppIds
			) {
		// the condition would be here only about the coordinator, as it is the only triggerer here
		// and all the other members are filtered, if the filters fail, the membership is not called, and
		// another case can be estimated
		if (!cAppIsDeployed){
			return true;
		}
		return false;
	}

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
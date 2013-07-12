package cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.deployment3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.KnowledgeExchange;
import cz.cuni.mff.d3s.deeco.annotations.MemberGroup;
import cz.cuni.mff.d3s.deeco.annotations.Members;
import cz.cuni.mff.d3s.deeco.annotations.Membership;
import cz.cuni.mff.d3s.deeco.annotations.Out;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.ensemble.Ensemble;
import cz.cuni.mff.d3s.deeco.invokable.types.IdentifiedIdListType;
import cz.cuni.mff.d3s.deeco.knowledge.OutWrapper;

/**
 * 
 * @author Julien Malvot
 *
 */
public class LinkEnsemble extends Ensemble {
	
	private static final long serialVersionUID = 1L;

	// filter on the application components : analog to a pre-processed membership
	// no need of selection for them as we do not have any relation between AppComponents subsets
	@Filter
	public static boolean appFilter(
			@In("coord.id") String cId,
			@In("coord.runningOn") String cRunningOn,
			@In("member.id") String mId,
			@In("member.runningOn") String mRunningOn){
		return (!mId.equals(cId) && cRunningOn.equals(mRunningOn));
	}
	
	// selection for the scp component
	@Selection
	public static List<String> scpSelection(
			@In("members.id") List<String> scpIds,
			@In("members.sla.maxLinkLatency") List<Long> scpMaxLatencies,
			@In("members.latencies") List<Map<String, Long>> scpLatencies){
		List<String> mLinkedIds = new ArrayList<String> ();
		int range = 4;
		// transforming the List<Map> data structure into a List data structure
		List<Link> mLinks = new ArrayList<Link> ();
		for (int i = 0; i < scpLatencies.size(); i++){
			Map<String,Long> map = scpLatencies.get(i);
			Object[] toIdSet = scpLatencies.get(i).keySet().toArray();
			// iterate over all the link destinations
			for (int j = 0; j < toIdSet.length; j++){
				Long latency = map.get((Object)scpIds.get(i));
				// if the latency respects the Service Level Agreement max latency of the source
				if (latency <= scpMaxLatencies.get(i)){
					// add the link to the data structure
					Link link = new Link(scpIds.get(i), (String)toIdSet[j], latency);
					mLinks.add(link);
				}
			}
		}
		// sort all the list of links by order of latency
		Collections.sort(mLinks, new LinkComparator());
		// reuse the initial implemented algorithm
		Integer indexer = -1; // the first required id to be explored for starting the exploration
		// while the linkedIds set is not well-sized or the algorithm runs out of possibilities
		while (mLinkedIds.size() < range && (range+indexer) <= mLinks.size()){
			mLinkedIds.clear();
			Integer firstAddIndex = 0;
			for (int i = 0; i < mLinks.size(); i++){
				Link link = mLinks.get(i);
				// if the link respects the maximum sla latency
				if (i > indexer){
					// first add into the linkage group
					if (mLinkedIds.size() == 0){
						// the starting index of the add is remembered as a bottom limit to be reached for a new search
						firstAddIndex = i;
						// add the two ids of the link
						mLinkedIds.add(link.getFromId());
						mLinkedIds.add(link.getToId());
					}else if (mLinkedIds.size() < range){
						String fId = link.getFromId();
						String tId = link.getToId();
						// if exclusively one or the other ids is part of the covered link
						if ((mLinkedIds.contains(fId) && !mLinkedIds.contains(tId))
								|| (!mLinkedIds.contains(fId) && mLinkedIds.contains(tId))){
							// we add the uncovered to the linkage group
							if (!mLinkedIds.contains(fId))
								mLinkedIds.add(fId);
							else
								mLinkedIds.add(tId);
						}
					}else{
						break;
					}
				}
			}
			// if we did not get enough ids into the interconnection, 
			// then we start the new iteration from the first found index
			if (mLinkedIds.size() < range)
				indexer = firstAddIndex;
		}
		return mLinkedIds;
	}
	
	@Membership
	@Members({
		// we state which filters are to be considered
		// this way, we can have the possibility to use the same filter for different member groups
		@MemberGroup(identifier="App", filter="appFilter"),
		@MemberGroup(identifier="Scp", selection="scpSelection")
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
		// if all AppComponents have not been deployed yet
		// we suppose the input members arrays are not empty as being injected before the call
		if (!cAppIsDeployed &&
			!msAppIsDeployed.contains(false)){
			return true;
		}
		return null;
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

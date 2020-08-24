package Demo.GenerationFiles;

/*
 *  *********************************************************************** *
 *  * project: org.matsim.*
 *  * DefaultControlerModules.java
 *  *                                                                         *
 *  * *********************************************************************** *
 *  *                                                                         *
 *  * copyright       : (C) 2014 by the members listed in the COPYING, *
 *  *                   LICENSE and WARRANTY file.                            *
 *  * email           : info at matsim dot org                                *
 *  *                                                                         *
 *  * *********************************************************************** *
 *  *                                                                         *
 *  *   This program is free software; you can redistribute it and/or modify  *
 *  *   it under the terms of the GNU General Public License as published by  *
 *  *   the Free Software Foundation; either version 2 of the License, or     *
 *  *   (at your option) any later version.                                   *
 *  *   See also COPYING, LICENSE and WARRANTY file                           *
 *  *                                                                         *
 *  * ***********************************************************************
 */

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.NetworkWriter;
import org.matsim.lanes.Lane;
import org.matsim.lanes.Lanes;
import org.matsim.lanes.LanesFactory;
import org.matsim.lanes.LanesToLinkAssignment;
import org.matsim.lanes.LanesUtils;
import org.matsim.lanes.LanesWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Class to create network and lanes for the breass scenario.
 * 
 * You may choose between simulating inflow capacity or not. Whereby simulating
 * inflow capacity means that one additional short link is added in front of
 * link 2-3, 2-4 and 4-5. (Inflow capacity at 3-4 and 3-5 is not necessary (i.e.
 * flow capacity can not be exceeded at this links) because they only have one
 * incoming link with the same flow capacity.)
 * 
 * @author tthunig
 * 
 */
public class NetworkGeneration {
	
	private static final Logger log = Logger
			.getLogger(NetworkGeneration.class);
	
	private static Scenario scenario;
	
	//private static final int NUMBER_OF_PERSONS = 3600; // per hour
	//private static final int SIMULATION_PERIOD = 1; // in hours
	//private static final double SIMULATION_START_TIME = 0.0; // seconds from midnight
	
	public static boolean simulateInflowCap = false;
	public static boolean middleLinkExists = true;
	public static boolean byPassExists = false;
	//private LaneType laneType = LaneType.NONE; 
	public static boolean btuRun = false;
	public static int numberOfPersons=10;
	
	// capacity of the links that all agents have to use
	public static double capFirstLast; // [veh/h]
	// capacity of middle link (use the default capacity if it is 0.0)
	public static double capZ; // [veh/h]
	// capacity of slow links (24, 35)
	public static double capSlow; // [veh/h]
	// capacity of fast links (23, 45)
	public static double capFast; // [veh/h]
	// link length for the inflow links
	public static double inflowLinkLength; // [m]
	// link length for all links with big travel time
	public static int linkLengthBig; // [m]
	// link length for all other links
	public static long linkLengthSmall; // [m]
	// travel time for the middle link
	public static double linkTTMid;
	// travel time for the middle route links
	public static double linkTTSmall; // [s]
	// travel time for the two remaining outer route links (choose at least 3*LINK_TT_SMALL!)
	public static double linkTTBig; // [s]
	// travel time for inflow links and links that all agents have to use
	public static double minimalLinkTT; // [s]
	// travel time for the by-pass route
	public static double byPassTT = 1200; // [s]


	public NetworkGeneration(Scenario scenario) {		
		this.scenario = scenario;
	}
	
	public enum LaneType{
		NONE, TRIVIAL, REALISTIC
	}

	/**
	 * Creates the network for the Breass scenario.
	 */
	//public void createNetworkAndLanes(){
	public static void main(String[] args) throws IOException {
		
	    initNetworkParams();
		
		createNetwork();
		

		
	
		//if (laneType.equals(LaneType.TRIVIAL))
		//	createTrivialLanes();
		//if (laneType.equals(LaneType.REALISTIC))
		//	createRealisticLanes();
		//if (laneType.equals(LaneType.NONE))
		//	log.info("No lanes are used");
	}

	private static void initNetworkParams() {
		if (btuRun){
			/* use defaults if capacities are not set yet */
			if (capFirstLast == 0.0) capFirstLast = numberOfPersons;
			if (capSlow == 0.0) capSlow = numberOfPersons;
			if (capFast == 0.0) capFast = numberOfPersons;
			if (capZ == 0.0) capZ = capSlow;
			
			inflowLinkLength = 7.5 * 1;
			linkLengthSmall = 200;
			linkTTMid = 1;
			linkTTSmall = 10;
			linkTTBig = 20;
			
			minimalLinkTT = 1;
		} else {
			/* use defaults if capacities are not set yet */
			if (capFirstLast == 0.0) capFirstLast = numberOfPersons;
			if (capSlow == 0.0) capSlow = numberOfPersons / 2 ;
			if (capFast == 0.0) capFast = numberOfPersons / 2 ;
			if (capZ == 0.0) capZ = capSlow;
			
			inflowLinkLength = 7.5 * 1;
			linkLengthSmall = 1000;
			linkLengthBig = 10000;
			
			linkTTMid = 1 * 60;
			linkTTSmall = 1 * 60;
			linkTTBig = 10 * 60;
			minimalLinkTT = 1;
		}
	}

	private static void createNetwork() throws IOException {
		//Network net = this.scenario.getNetwork();
		//Network net = this.scenario.getNetwork();
		Network net = NetworkUtils.createNetwork();
		NetworkFactory fac = net.getFactory();
		
		//NetworkFactory fac = net.getFactory();

		// create nodes
		net.addNode(fac.createNode(Id.createNodeId(0),
				new Coord(-200, 200)));
		net.addNode(fac.createNode(Id.createNodeId(1),
				new Coord(0, 200)));
		net.addNode(fac.createNode(Id.createNodeId(2),
				new Coord(200, 200)));
		net.addNode(fac.createNode(Id.createNodeId(3),
				new Coord(400, 400)));
		net.addNode(fac.createNode(Id.createNodeId(4),
				new Coord(400, 0)));
		net.addNode(fac.createNode(Id.createNodeId(5),
				new Coord(600, 200)));
		net.addNode(fac.createNode(Id.createNodeId(6),
				new Coord(800, 200)));
		//if (byPassExists) {
		//	net.addNode(fac.createNode(Id.createNodeId(7), new Coord(0, 600)));
		//	net.addNode(fac.createNode(Id.createNodeId(8), new Coord(600, 600)));
		//}
		
		//if (simulateInflowCap){
		//	net.addNode(fac.createNode(Id.createNodeId(23),
		//			new Coord(250, 250)));
		//	net.addNode(fac.createNode(Id.createNodeId(24),
		//			new Coord(250, 150)));
		//	net.addNode(fac.createNode(Id.createNodeId(45),
		//			new Coord(450, 50)));
		//}
		
		// create links
		Link l = fac.createLink(Id.createLinkId("0_1"),
				net.getNodes().get(Id.createNodeId(0)),
				net.getNodes().get(Id.createNodeId(1)));
		setLinkAttributes(l, capFirstLast, linkLengthSmall, minimalLinkTT);
		net.addLink(l);
		
		l = fac.createLink(Id.createLinkId("1_2"),
				net.getNodes().get(Id.createNodeId(1)),
				net.getNodes().get(Id.createNodeId(2)));
		// use a big link length here such that no spill back occurs on the first link and vehicles can use the by-pass without congestion
		setLinkAttributes(l, capFirstLast, 5*linkLengthBig, minimalLinkTT);	
		net.addLink(l);
		
		if (simulateInflowCap){
			l = fac.createLink(Id.createLinkId("2_23"),
					net.getNodes().get(Id.createNodeId(2)),
					net.getNodes().get(Id.createNodeId(23)));
			setLinkAttributes(l, capFast, inflowLinkLength, minimalLinkTT);
			net.addLink(l);
			
			l = fac.createLink(Id.createLinkId("23_3"),
					net.getNodes().get(Id.createNodeId(23)),
					net.getNodes().get(Id.createNodeId(3)));
			setLinkAttributes(l, capFast, linkLengthSmall, linkTTSmall - minimalLinkTT);
			net.addLink(l);
		} else {
			l = fac.createLink(Id.createLinkId("2_3"),
					net.getNodes().get(Id.createNodeId(2)),
					net.getNodes().get(Id.createNodeId(3)));
			setLinkAttributes(l, capFast, linkLengthSmall, linkTTSmall);
			net.addLink(l);
		}
		
		if (simulateInflowCap){
			l = fac.createLink(Id.createLinkId("2_24"),
					net.getNodes().get(Id.createNodeId(2)),
					net.getNodes().get(Id.createNodeId(24)));
			setLinkAttributes(l, capSlow, inflowLinkLength, minimalLinkTT);
			net.addLink(l);
			
			l = fac.createLink(Id.createLinkId("24_4"),
					net.getNodes().get(Id.createNodeId(24)),
					net.getNodes().get(Id.createNodeId(4)));
			setLinkAttributes(l, capSlow, linkLengthBig, linkTTBig - minimalLinkTT);
			net.addLink(l);
		} else {
			l = fac.createLink(Id.createLinkId("2_4"),
					net.getNodes().get(Id.createNodeId(2)),
					net.getNodes().get(Id.createNodeId(4)));
			setLinkAttributes(l, capSlow, linkLengthBig, linkTTBig);
			net.addLink(l);
		}
		
		if (middleLinkExists){
			l = fac.createLink(Id.createLinkId("3_4"),
				net.getNodes().get(Id.createNodeId(3)),
				net.getNodes().get(Id.createNodeId(4)));
			setLinkAttributes(l, capZ, linkLengthSmall, linkTTMid);
			net.addLink(l);
		}
	
		l = fac.createLink(Id.createLinkId("3_5"),
				net.getNodes().get(Id.createNodeId(3)),
				net.getNodes().get(Id.createNodeId(5)));
		setLinkAttributes(l, capSlow, linkLengthBig, linkTTBig);
		net.addLink(l);
		
		if (simulateInflowCap){
			l = fac.createLink(Id.createLinkId("4_45"),
					net.getNodes().get(Id.createNodeId(4)),
					net.getNodes().get(Id.createNodeId(45)));
			setLinkAttributes(l, capFast, inflowLinkLength, minimalLinkTT);
			net.addLink(l);
			
			l = fac.createLink(Id.createLinkId("45_5"),
					net.getNodes().get(Id.createNodeId(45)),
					net.getNodes().get(Id.createNodeId(5)));
			setLinkAttributes(l, capFast, linkLengthSmall, linkTTSmall - minimalLinkTT);
			net.addLink(l);
		}
		else{
			l = fac.createLink(Id.createLinkId("4_5"),
					net.getNodes().get(Id.createNodeId(4)),
					net.getNodes().get(Id.createNodeId(5)));
			setLinkAttributes(l, capFast, linkLengthSmall, linkTTSmall);
			net.addLink(l);
		}
		
		l = fac.createLink(Id.createLinkId("5_6"),
				net.getNodes().get(Id.createNodeId(5)),
				net.getNodes().get(Id.createNodeId(6)));
		setLinkAttributes(l, capFirstLast, linkLengthSmall, minimalLinkTT);
		net.addLink(l);
		
		/*if (byPassExists) {
			l = fac.createLink(Id.createLinkId("1_7"), net.getNodes().get(Id.createNodeId(1)), net.getNodes().get(Id.createNodeId(7)));
			setLinkAttributes(l, capFirstLast, linkLengthBig, byPassTT/3);
			net.addLink(l);
			l = fac.createLink(Id.createLinkId("7_8"), net.getNodes().get(Id.createNodeId(7)), net.getNodes().get(Id.createNodeId(8)));
			setLinkAttributes(l, capFirstLast, linkLengthBig, byPassTT/3);
			net.addLink(l);
			l = fac.createLink(Id.createLinkId("8_5"), net.getNodes().get(Id.createNodeId(8)), net.getNodes().get(Id.createNodeId(5)));
			setLinkAttributes(l, capFirstLast, linkLengthBig, byPassTT/3);
			net.addLink(l);
		}*/
		
		// Create Return links
		l = fac.createLink(Id.createLinkId("6_5"),
				net.getNodes().get(Id.createNodeId(6)),
				net.getNodes().get(Id.createNodeId(5)));
		setLinkAttributes(l, capFirstLast, linkLengthSmall, minimalLinkTT);
		net.addLink(l);
		
		l = fac.createLink(Id.createLinkId("5_4"),
				net.getNodes().get(Id.createNodeId(5)),
				net.getNodes().get(Id.createNodeId(4)));
		setLinkAttributes(l, capFast, linkLengthSmall, linkTTSmall);
		net.addLink(l);
		
		l = fac.createLink(Id.createLinkId("5_3"),
				net.getNodes().get(Id.createNodeId(5)),
				net.getNodes().get(Id.createNodeId(3)));
		setLinkAttributes(l, capSlow, linkLengthBig, linkTTBig);
		net.addLink(l);
		
		l = fac.createLink(Id.createLinkId("4_2"),
				net.getNodes().get(Id.createNodeId(4)),
				net.getNodes().get(Id.createNodeId(2)));
		setLinkAttributes(l, capSlow, linkLengthBig, linkTTBig);
		net.addLink(l);
		
		l = fac.createLink(Id.createLinkId("3_2"),
				net.getNodes().get(Id.createNodeId(3)),
				net.getNodes().get(Id.createNodeId(2)));
		setLinkAttributes(l, capFast, linkLengthSmall, linkTTSmall);
		net.addLink(l);
		
		l = fac.createLink(Id.createLinkId("2_1"),
				net.getNodes().get(Id.createNodeId(2)),
				net.getNodes().get(Id.createNodeId(1)));
		setLinkAttributes(l, capFirstLast, 5*linkLengthBig, minimalLinkTT);
		net.addLink(l);
		
		l = fac.createLink(Id.createLinkId("1_0"),
				net.getNodes().get(Id.createNodeId(1)),
				net.getNodes().get(Id.createNodeId(0)));
		// use a big link length here such that no spill back occurs on the first link and vehicles can use the by-pass without congestion
		setLinkAttributes(l, capFirstLast, linkLengthSmall, minimalLinkTT);	
		net.addLink(l);
		
		// create output folder if necessary
		//Path outputFolder = Files.createDirectories(Paths.get("input"));
		Path outputFolder = Files.createDirectories(Paths.get("src"));

		// write network
		//new NetworkWriter(net).write(outputFolder.resolve("./input/network.xml").toString());	
		new NetworkWriter(net).write(outputFolder.resolve("./test/network.xml").toString());
		
		
	}

	private static void setLinkAttributes(Link link, double capacity,
			double length, double travelTime) {
		
		link.setCapacity(capacity);
		link.setLength(length);
		// agents have to reach the end of the link before the time step ends to
		// be able to travel forward in the next time step (matsim time step logic)
		link.setFreespeed(link.getLength() / (travelTime - 0.1));
	}

	/**
	 * creates a trivial lane for every link
	 */
	private static void createTrivialLanes() {
		
		Lanes laneDef20 = scenario.getLanes();
		LanesFactory fac = laneDef20.getFactory();
		
		for (Link link: scenario.getNetwork().getLinks().values()){
			// create a trivial lane for every link that has outgoing links
			if (link.getToNode().getOutLinks() != null && !link.getToNode().getOutLinks().isEmpty()) {
				LanesToLinkAssignment linkAssignment = fac.createLanesToLinkAssignment(link.getId());
				// add all outgoing links as toLinks
				List<Id<Link>> toLinkList = new ArrayList<>(link.getToNode().getOutLinks().keySet());
				LanesUtils.createAndAddLane(linkAssignment, fac,
						Id.create(link.getId() + ".ol", Lane.class), link.getCapacity(),
						link.getLength(), 0, 1, toLinkList, null);
				laneDef20.addLanesToLinkAssignment(linkAssignment);
			}
		}
	}

	/**
	 * creates a lane for every turning direction, i.e. for every signal at a link
	 */
	private static void createRealisticLanes() {
		
		Lanes laneDef20 = scenario.getLanes();
		LanesFactory fac = laneDef20.getFactory();

		// create link assignment of link 1_2
		LanesToLinkAssignment linkAssignment = fac
				.createLanesToLinkAssignment(Id.createLinkId("1_2"));

		LanesUtils.createAndAddLane(linkAssignment, fac,
				Id.create("1_2.ol", Lane.class), capFirstLast,
				linkLengthSmall, 0, 1, null, 
				Arrays.asList(Id.create("1_2.l", Lane.class),
				Id.create("1_2.r", Lane.class)));
		
		if (simulateInflowCap) {
			LanesUtils.createAndAddLane(linkAssignment, fac,
					Id.create("1_2.l", Lane.class), capFirstLast,
					linkLengthSmall / 2, -1, 1,
					Collections.singletonList(Id.createLinkId("2_23")),	null);
			LanesUtils.createAndAddLane(linkAssignment, fac,
					Id.create("1_2.r", Lane.class), capFirstLast,
					linkLengthSmall / 2, 1, 1,
					Collections.singletonList(Id.createLinkId("2_24")), null);
		} else {
			LanesUtils.createAndAddLane(linkAssignment, fac,
					Id.create("1_2.l", Lane.class), capFirstLast,
					linkLengthSmall / 2, -1, 1,
					Collections.singletonList(Id.createLinkId("2_3")), null);
			LanesUtils.createAndAddLane(linkAssignment, fac,
					Id.create("1_2.r", Lane.class), capFirstLast,
					linkLengthSmall / 2, 1, 1,
					Collections.singletonList(Id.createLinkId("2_4")), null);
		}	
		
		laneDef20.addLanesToLinkAssignment(linkAssignment);
		
		// no lanes on 2_3 (or 23_3) are needed if 3_4 doesn't exist
		if (middleLinkExists) {
			// create link assignment of link 2_3 (or 23_3 if inflow capacity is
			// simulated)
			if (simulateInflowCap) {
				linkAssignment = fac.createLanesToLinkAssignment(Id
						.createLinkId("23_3"));

				LanesUtils.createAndAddLane(linkAssignment, fac,
						Id.create("23_3.ol", Lane.class), capFast,
						linkLengthSmall, 0,	1, null,
						Arrays.asList(Id.create("23_3.f", Lane.class),
								Id.create("23_3.r", Lane.class)));

				LanesUtils.createAndAddLane(linkAssignment, fac,
						Id.create("23_3.f", Lane.class), capFast,
						linkLengthSmall / 2, 0, 1,
						Collections.singletonList(Id.createLinkId("3_5")), null);

				LanesUtils.createAndAddLane(linkAssignment, fac,
						Id.create("23_3.r", Lane.class), capFast,
						linkLengthSmall / 2, 1, 1,
						Collections.singletonList(Id.createLinkId("3_4")), null);

				laneDef20.addLanesToLinkAssignment(linkAssignment);
			} else {
				linkAssignment = fac.createLanesToLinkAssignment(Id
						.createLinkId("2_3"));

				LanesUtils.createAndAddLane(linkAssignment, fac,
						Id.create("2_3.ol", Lane.class), capFast,
						linkLengthSmall, 0,	1, null,
						Arrays.asList(Id.create("2_3.f", Lane.class),
								Id.create("2_3.r", Lane.class)));

				LanesUtils.createAndAddLane(linkAssignment, fac,
						Id.create("2_3.f", Lane.class), capFast,
						linkLengthSmall / 2, 0, 1,
						Collections.singletonList(Id.createLinkId("3_5")), null);

				LanesUtils.createAndAddLane(linkAssignment, fac,
						Id.create("2_3.r", Lane.class), capFast,
						linkLengthSmall / 2, 1, 1,
						Collections.singletonList(Id.createLinkId("3_4")), null);

				laneDef20.addLanesToLinkAssignment(linkAssignment);
			}
		}
	}

}

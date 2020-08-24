package Demo.GenerationFiles;

import java.io.IOException;

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

import java.util.ArrayList;
import java.util.List;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.api.core.v01.population.Route;
import org.matsim.core.api.internal.MatsimWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.population.routes.RouteUtils;
import org.matsim.core.scenario.ScenarioUtils;


/**
 * Class to create a population for the braess scenario.
 * 
 * Choose the number of persons you like to simulate, 
 * their starting times and their initial routes 
 * before calling the method createPersons(...)
 * 
 * @author tthunig
 */
public class PopulationGeneration {
	
	
	/** ALL means: initialize all 3 routes, select the outer ones */
	public enum InitRoutes{
		ALL, ONLY_MIDDLE, ONLY_OUTER, NONE, EVERY_FOURTH_Z_REST_BYPASS, EVERY_SECOND_OUTER_REST_BYPASS
	}
	
	private static Population population;
	private static Network network;
	
	private static int numberOfPersons=10; // per hour
	private static int simulationPeriod = 1; // in hours. default is one hour.
	private static double simulationStartTime = 0.0; // seconds from midnight. default is midnight 
	
	//private static boolean simulateInflowCap23 = false;
	//private static boolean simulateInflowCap24 = false;
	//private static boolean simulateInflowCap45 = false;
	//private static boolean middleLinkExists = true;
	
//	private boolean writePopFile = false;
//	private String pathToPopFile = "C:/Users/Theresa/Desktop/braess/plans3600.xml";
	

	/**
	 * Checks several properties of the network.
	 */
	
	/*
	private void checkNetworkProperties() {
		
		// check whether the network simulates inflow capacity
		if (this.network.getNodes().containsKey(Id.createNodeId(23)))
			this.simulateInflowCap23 = true;
		if (this.network.getNodes().containsKey(Id.createNodeId(24)))
			this.simulateInflowCap24 = true;
		if (this.network.getNodes().containsKey(Id.createNodeId(45)))
			this.simulateInflowCap45 = true;
		
		// check whether the network contains the middle link
		if (!this.network.getLinks().containsKey(Id.createLinkId("3_4")))
			this.middleLinkExists = false;
	}
	*/
	
	/**
	 * Fills a population container with the given number of persons. All
	 * persons travel from the left to the right through the network as in
	 * Braess's original paradox.
	 * 
	 * All agents start uniformly distributed between simulationStartTime and 
	 * (simulationStartTime + simulationPeriod) am.
	 * 
	 * If initRouteSpecification is NONE, all agents are initialized with no initial
	 * routes. 
	 * If it is ONLY_MIDDLE, all agents are initialized with the middle route.
	 * If it is ALL they are initialized with all three routes in this
	 * scenario, whereby every second agent gets the upper and every other agent
	 * the lower route as initial selected route. 
	 * If it is ONLY_OUTER, all agents are initialized with both outer routes, 
	 * whereby they are again alternately selected.
	 * 
	 * @param initRouteSpecification
	 *            specification which routes should be used as initial routes
	 *            (see enum RouteInitialization for the possibilities)
	 * @param initPlanScore
	 *            initial score for all plans the persons will get. Use null for
	 *            no scores.
	 */
	
	public static void main(String[] args) throws IOException {
		
		/*
		 * First, create a new Config and a new Scenario.
		 */
		Config config = ConfigUtils.createConfig();
		Scenario sc = ScenarioUtils.createScenario(config);

		/*
		 * Pick the Network and the Population out of the Scenario for convenience. 
		 */
		Network network = sc.getNetwork();
		Population population = sc.getPopulation();

		/*
		 * Pick the PopulationFactory out of the Population for convenience.
		 * It contains methods to create new Population items.
		 */
		PopulationFactory populationFactory = population.getFactory();

		
		final InitRoutes initRouteSpecification = InitRoutes.ONLY_OUTER;
		// initial score for all initial plans (if to low, to many agents switch to outer routes simultaneously)
		Double initPlanScore= null;
		
		
		for (int i = 0; i < numberOfPersons * simulationPeriod; i++) {

			// create a person
			Person person = populationFactory.createPerson(Id.create(i, Person.class));
			population.addPerson(person);
			
			
			//Person person = population.getFactory().createPerson(
			//		Id.createPersonId(i));

			// create a start activity at link 0_1
			Activity startAct = population.getFactory()
					.createActivityFromLinkId("dummy", Id.createLinkId("0_1"));
			// distribute agents uniformly between simulationStartTime and (simulationStartTime + simulationPeriod) am.
			startAct.setEndTime(simulationStartTime + (double)(i)/(numberOfPersons * simulationPeriod) * simulationPeriod * 3600);
		
			// create a drain activity at link 5_6
			Activity drainAct = population.getFactory().createActivityFromLinkId(
					"dummy", Id.createLinkId("5_6"));
			
			// create a dummy leg
			//Leg leg1 = population.getFactory().createLeg(TransportMode.car);
			//Leg leg1 = population.getFactory().createLeg(TransportMode.walk);
			// fill the leg if necessary
			// switch (initRouteSpecification){
			// case ONLY_MIDDLE:
			//ncase EVERY_FOURTH_Z_REST_BYPASS:
			//case ALL:
			//	leg1 = createMiddleLeg();
			//	break;
			//case ONLY_OUTER:
			//case EVERY_SECOND_OUTER_REST_BYPASS:
			//	leg1 = createUpperLeg();
			//	break;
			//default:
			//	break;
			//}
			
			/*
			 * Create a Plan for the first Person
			 */
			Plan plan = populationFactory.createPlan();
			
			/*
			 * Create a "home" Activity for the Person. In order to have the Person end its day at the same location,
			 * we keep the home coordinates for later use (see below).
			 * Note that we use the CoordinateTransformation created above.
			 */
			
			//Coord homeCoordinates = new Coord(14.31377, 51.76948);
			Coord homeCoordinates = new Coord(-200,200);
			
		    //Activity activity1 = populationFactory.createActivityFromCoord("home", ct.transform(homeCoordinates));
			Activity activity1 = populationFactory.createActivityFromCoord("h", homeCoordinates);
			activity1.setEndTime(21600); // leave at 6 o'clock
			plan.addActivity(activity1); // add the Activity to the Plan
			
			/*
			 * Create a Leg. A Leg initially hasn't got many attributes. It just says that a car will be used.
			 */
			plan.addLeg(populationFactory.createLeg("car"));
			//plan.addLeg(populationFactory.createLeg("walk"));
			//plan.addLeg(populationFactory.createLeg("twoway_vehicles"));
			
				
			/*
			 * Create a "work" Activity, at a different location.
			 */
			// Activity activity2 = populationFactory.createActivityFromCoord("work", ct.transform(new Coord(14.34024, 51.75649)));
			Activity activity2 = populationFactory.createActivityFromCoord("w", new Coord(800, 200));
			activity2.setEndTime(57600); // leave at 4 p.m.
			plan.addActivity(activity2);
			
			/*
			 * Create another car Leg.
			 */
			//plan.addLeg(populationFactory.createLeg("walk"));
			plan.addLeg(populationFactory.createLeg("car"));
			
			
			/*
			 * End the day with another Activity at home. Note that it gets the same coordinates as the first activity.
			 */
			Activity activity3 = populationFactory.createActivityFromCoord("h", homeCoordinates);
			plan.addActivity(activity3);
			
			
			person.addPlan(plan);
			
		MatsimWriter popWriter = new PopulationWriter(population, network);
		// popWriter.write("./input/population.xml");
		popWriter.write("C:\\Users\\nismi87\\git\\GenerationFiles\\src\\test\\population.xml");
		


		/*
		 * Pick the Network and the Population out of the Scenario for convenience. 
		 */
		//Network network = scenario.getNetwork();
		//Population population = scenario.getPopulation();



	}
	/*public static void createPersons(InitRoutes initRouteSpecification, Double initPlanScore) {
		
		//if (!this.middleLinkExists && 
		//		(initRouteSpecification.equals(InitRoutes.ONLY_MIDDLE) || initRouteSpecification.equals(InitRoutes.EVERY_FOURTH_Z_REST_BYPASS))){
		//	throw new IllegalArgumentException("You are trying to create agents with an initial middle route, although no middle link exists.");
		//}
				
		// write population file if flag is enabled
		//if (this.writePopFile){
		//	PopulationWriter popWriter = new PopulationWriter(population);
		//	popWriter.write(this.pathToPopFile);
		//}
	}*/

	/*private static Plan createPlan(Activity startAct, Leg leg, Activity drainAct,
			Double initPlanScore) {
		
		Plan plan = population.getFactory().createPlan();

		plan.addActivity(startAct);
		plan.addLeg(leg);
		plan.addActivity(drainAct);
		plan.setScore(initPlanScore);
		
		return plan;
	}*/

	/**
	 * Creates a leg with the middle path.
	 */
	/*private Leg createMiddleLeg() {
		Leg legZ = population.getFactory()
				.createLeg(TransportMode.car);
		
		List<Id<Link>> pathZ = new ArrayList<>();
		pathZ.add(Id.createLinkId("1_2"));
		if (!this.simulateInflowCap23){
			pathZ.add(Id.createLinkId("2_3"));
		}
		else{
			pathZ.add(Id.createLinkId("2_23"));
			pathZ.add(Id.createLinkId("23_3"));
		}
		pathZ.add(Id.createLinkId("3_4"));
		if (!this.simulateInflowCap45){
			pathZ.add(Id.createLinkId("4_5"));
		}
		else{
			pathZ.add(Id.createLinkId("4_45"));
			pathZ.add(Id.createLinkId("45_5"));
		}
		
		Route routeZ = RouteUtils.createLinkNetworkRouteImpl(Id.createLinkId("0_1"), pathZ, Id.createLinkId("5_6"));
		
		legZ.setRoute(routeZ);
		return legZ;
	}
	*/

	/**
	 * Creates a leg with the upper path.
	 */
		/*
	private static Leg createUpperLeg() {
		Leg legUp = population.getFactory()
				.createLeg(TransportMode.car);
		
		List<Id<Link>> pathUp = new ArrayList<>();
		pathUp.add(Id.createLinkId("1_2"));
		if (!simulateInflowCap23){
			pathUp.add(Id.createLinkId("2_3"));
		}
		else{
			pathUp.add(Id.createLinkId("2_23"));
			pathUp.add(Id.createLinkId("23_3"));
		}
		pathUp.add(Id.createLinkId("3_5"));
		
		Route routeUp = RouteUtils.createLinkNetworkRouteImpl(Id.createLinkId("0_1"), pathUp, Id.createLinkId("5_6"));
		
		legUp.setRoute(routeUp);
		return legUp;
	}
	*/
	/**
	 * Creates a leg with the lower path. 
	 */
		
		/*public void setNumberOfPersons(int numberOfPersons) {
		this.numberOfPersons = numberOfPersons;
	}

	public void writePopulation(String file) {
		new PopulationWriter(population).write(file);
	}

	public void setSimulationPeriod(int simulationPeriod) {
		this.simulationPeriod = simulationPeriod;
	}

	public void setSimulationStartTime(double simulationStartTime) {
		this.simulationStartTime = simulationStartTime;
	}*/

}
}


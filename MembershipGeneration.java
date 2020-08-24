package Demo.GenerationFiles;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.carsharing.manager.demand.membership.MembershipContainer;
import org.matsim.contrib.carsharing.manager.demand.membership.PersonMembership;
import org.matsim.core.utils.io.MatsimXmlParser;
import org.xml.sax.Attributes;

public class MembershipGeneration {

	
	//Creates a membership type
	public class PersonMembership {
		
		private Map<String, Set<String>> membershipsPerCompany;
		private Map<String, Set<String>> membershipsPerCSType;
		
		public PersonMembership(Map<String, Set<String>> membershipsPerCompany, 
				Map<String, Set<String>> membershipsPerCSType) {
					
			this.membershipsPerCompany = membershipsPerCompany;
			this.membershipsPerCSType = membershipsPerCSType;
		}

		public Map<String, Set<String>> getMembershipsPerCSType() {
			return membershipsPerCSType;
		}
		
		public Map<String, Set<String>> getMembershipsPerCompany() {
			return membershipsPerCompany;
		}
	}
	
	
	//Creates membership of each person
	public class MembershipContainer {
		
		private Map<Id<Person>, PersonMembership> perPersonMemberships = new HashMap<Id<Person>, PersonMembership>();
		
		public void addPerson(String personId, PersonMembership personMembership) {
			
			Id<Person> personId2 = Id.createPersonId(personId);
			this.perPersonMemberships.put(personId2, personMembership);
		}
		
		public Map<Id<Person>, PersonMembership> getPerPersonMemberships() {
			return perPersonMemberships;
		}
	
	}
	
	//Reads membership of each person
	public class MembershipReader extends MatsimXmlParser{

		private MembershipContainer membershipContainer = new MembershipContainer();
		private Map<String, Set<String>> memberships;
		private Set<String> carsharingTypes;
		private Map<String, Set<String>> membershipPerCSType;
		private String personId;
		private String companyId;
		private HashMap<String, String> stringCache = new HashMap<>();

		@Override
		public void startTag(String name, Attributes atts, Stack<String> context) {

			if (name.equals("person")) {
				
				personId = atts.getValue("id");
				memberships = new HashMap<>();
				membershipPerCSType = new HashMap<>();
			}
			else if (name.equals("company")) {
				
				companyId = atts.getValue("id");
				companyId = stringCache.computeIfAbsent(companyId, id -> id);
				carsharingTypes = new TreeSet<>();
			}
			else if (name.equals("carsharing")) {
				
				String csType = atts.getValue("name");
				csType = stringCache.computeIfAbsent(csType, type -> type);
				if (this.membershipPerCSType.containsKey(csType)) {
					
					Set<String> companies = this.membershipPerCSType.get(csType);
					companies.add(companyId);
					this.membershipPerCSType.put(csType, companies);
					
				}
				else {
					Set<String> companies = new TreeSet<>();
					companies.add(companyId);
					this.membershipPerCSType.put(csType, companies);				
				}
				carsharingTypes.add(csType);
			}		
		}

		@Override
		public void endTag(String name, String content, Stack<String> context) {
			
			if (name.equals("person")) {
				PersonMembership personMembership = new PersonMembership(memberships, membershipPerCSType);
				membershipContainer.addPerson(personId, personMembership);
			}
			else if (name.equals("company")) {
				memberships.put(companyId, carsharingTypes);

			}		
		}
		public MembershipContainer getMembershipContainer() {
			return membershipContainer;
		}
	}


}

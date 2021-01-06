package ca.sheridancollege.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.security.core.Authentication;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;

import ca.sheridancollege.bean.ResearchStudy;

public class Functions {
	
	public static int getCollectionCount(String collectionName) throws InterruptedException, ExecutionException {

		//Initialize firestore instance
		Firestore firestore = FirestoreClient.getFirestore();
		
		ApiFuture<QuerySnapshot> userCollection = firestore.collection(collectionName).get();
		
		List<QueryDocumentSnapshot> documents = userCollection.get().getDocuments();
		
		return documents.size();
		
	}
	
	//Fetch the documents
	//Convert the documents to the given class objects
	public static List<Object> getDocuments(ApiFuture<QuerySnapshot> snapshot, Class<?> classObject) 
			throws InterruptedException, ExecutionException{

		List<QueryDocumentSnapshot> documents = snapshot.get().getDocuments();

		List<Object> collection = new ArrayList<Object>();
		
		
		for(DocumentSnapshot document : documents) {
			collection.add(document.toObject(classObject));
		}
		
		return collection;
		
	}
	
	public static List<ResearchStudy> searchResearch(List<Object> objects, String criteria, String searchString) {
		
		List<ResearchStudy> list = new ArrayList<ResearchStudy>();
		
		for (Object object : objects) {
			//Type cast to ResearchStudy
			//The list contains ResearchStudy class objects
			ResearchStudy researchStudy = (ResearchStudy) object;
			
			if(criteria.equals("Research Area")){
				if(researchStudy.getResearchArea().toLowerCase().contains(searchString.toLowerCase())) {
					list.add(researchStudy);
				}
			} else if(criteria.equals("Research Institution")) {
				if(researchStudy.getResearchInstitution().toLowerCase().contains(searchString.toLowerCase())) {
					list.add(researchStudy);
				}
			} else if(criteria.equals("Research Duration")) {
				if(researchStudy.getResearchDuration().toLowerCase().contains(searchString.toLowerCase())) {
					list.add(researchStudy);
				}
			} else if(criteria.equals("Researcher")) {
				if(researchStudy.getPostedBy().toLowerCase().contains(searchString.toLowerCase())) {
					list.add(researchStudy);
				}
			} else if(criteria.equals("Posted Date")) {
				if(researchStudy.getPostedDate().toLowerCase().contains(searchString.toLowerCase())) {
					list.add(researchStudy);
				}
			} else if(criteria.equals("Research Title")) {
				if(researchStudy.getResearchTitle().toLowerCase().contains(searchString.toLowerCase())) {
					list.add(researchStudy);
				}
			} else if(criteria.equals("Research Detail")) {
			
				if(researchStudy.getResearchDetail().toLowerCase().contains(searchString.toLowerCase())) {
					list.add(researchStudy);
				}
			} else {
				
				int searchNum = Integer.parseInt(searchString);

				if (criteria.equals("Minimum Number of Participants")) {
					if (researchStudy.getNumParticipants() >= searchNum) {
						list.add(researchStudy);
					}
				} else {
					//Maximum number of participants
					if (researchStudy.getNumParticipants() <= searchNum) {
						list.add(researchStudy);
					}
				}
			}
		}
		
		return list;
	}

	// Search criterias
	public static ArrayList<String> getCriterias() {

		ArrayList<String> criterias = new ArrayList<>();

		criterias.add("Research Title");
		criterias.add("Research Area");
		criterias.add("Research Institution");
		criterias.add("Research Duration");
		criterias.add("Researcher");
		criterias.add("Posted Date");
		criterias.add("Research Detail");
		criterias.add("Minimum Number of Participants");
		criterias.add("Maximum Number of Participants");

		return criterias;
	}
	
	//Check the collection and see if the id already exists
	//Return the next available ID
	public static int documentID(int id, String collection) throws InterruptedException, ExecutionException {
		
		Firestore firestore = FirestoreClient.getFirestore();
		
		boolean exists = true;
		String strID = Integer.toString(id);
		
		do {
			
			DocumentSnapshot document = firestore.collection(collection).document(strID).get()
					.get();
			
			if(document.exists()) {
				
				id = id + 1;
				strID = Integer.toString(id);
				
			} else {
				
				exists = false;
				
			}
			
		} while(exists);
		
		return id;
	}
	
	public static String getUserType(Authentication auth) {
		
		if(auth.isAuthenticated()) {
			//If the user is logged in
			if(isAdministrator(auth)) {
				return "ADMINISTRATOR";
			} else {
				return "USER";
			}
			
		} else {
			//If the user is not logged in
			return "USER";
		}
	}
	
	//Generate a 20 digit string
	//Reference: 
	//https://stackoverflow.com/questions/41107/how-to-generate-a-random-alpha-numeric-string
	public static String getCode(int num) {
		
		final String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		final String lower = upper.toLowerCase();
		final String digits = "0123456789";
		
		//Total of 62 characters
		final String alphanum = upper + lower + digits;
		
		String code = "";
		
		for(int i = 0; i < num; i++) {
			
			int random = (int) (Math.random() * 62);
			
			code += alphanum.charAt(random);
		}
		
		return code;
		
	}
	
	private static boolean isAdministrator(Authentication auth) {
		
		//Reference: https://stackoverflow.com/questions/10092882/how-can-i-get-the-current-user-roles-from-spring-security-3-1
		return auth.getAuthorities().stream()
				.anyMatch(role -> role.getAuthority().equals("ROLE_ADMINISTRATOR"));
	}
}

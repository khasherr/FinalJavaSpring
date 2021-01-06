package ca.sheridancollege.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;

import ca.sheridancollege.bean.ResearchStudy;
import ca.sheridancollege.email.EmailServiceImpl;
import ca.sheridancollege.util.Functions;

@Controller
public class ResearchController {
	
	//Variables

	@Autowired
	private EmailServiceImpl esi;
	
	private Firestore firestore = null;
	
	//List researches
	@GetMapping("/studies")
	public String listResearch(Model model) throws InterruptedException, ExecutionException {
		
		firestore = FirestoreClient.getFirestore();

		ApiFuture<QuerySnapshot> snapshot = firestore.collection("researchstudy").get();
		
		model.addAttribute("researches", Functions.getDocuments(snapshot, ResearchStudy.class));
		
		model.addAttribute("criterias", Functions.getCriterias());
		
		return "viewResearch.html";
	}
	
	//View details of a research
	@GetMapping("/viewDetails/{researchid}")
	public String viewDetails(Model model, @PathVariable int researchid) 
			throws InterruptedException, ExecutionException {

		firestore = FirestoreClient.getFirestore();

		ResearchStudy research = firestore.collection("researchstudy")
				.whereEqualTo("researchStudyId", researchid)
				.get()
				.get()
				.getDocuments().get(0).toObject(ResearchStudy.class);

		model.addAttribute("research", research);
		
		return "viewDetails.html";
		
	}

	// Search features for Administrators
	@GetMapping("/searchResearch")
	public String search(Model model, @RequestParam String search, @RequestParam String criteria)
			throws InterruptedException, ExecutionException {

		firestore = FirestoreClient.getFirestore();
		
		//Validations
		boolean errorFound = false;
		ArrayList<String> errors = new ArrayList<String>();
		
		if (criteria.equals("Researcher")) {
			
			errorFound = !Pattern.compile("^[a-zA-Z0-9,;\\-. ]*$").matcher(search).matches();
			errors.add("Alphanumeric characters, \",\", ; , and the whitespace are allowed");
			
		} else if (criteria.equals("Minimum Number of Participants") 
				|| criteria.equals("Maximum Number of Participants")) {
			try {
				int validation = Integer.parseInt(search);
				
				if (validation < 1) {
					errors.add("The minimum number of participants is 1.");
				}
				errorFound = true;
			} catch (Exception e) {
				errorFound = true;
				errors.add("Please type a number");
			}
		}
		
		//Process the errors
		//Display the error messages
		if(errorFound) {
			
			ApiFuture<QuerySnapshot> snapshot = firestore.collection("researchstudy").get();
			
			model.addAttribute("researches", Functions.getDocuments(snapshot, ResearchStudy.class));
			
			model.addAttribute("criterias", Functions.getCriterias());
			
			model.addAttribute("errors", errors);
			
			return "viewResearch.html";
		}
		
		//If there are no errors
		ApiFuture<QuerySnapshot> snapshot = firestore.collection("researchstudy").get();

		List<Object> researches = Functions.getDocuments(snapshot, ResearchStudy.class);

		model.addAttribute("researches", Functions.searchResearch(researches, criteria, search));

		model.addAttribute("criterias", Functions.getCriterias());

		return "viewResearch.html";
	}
}

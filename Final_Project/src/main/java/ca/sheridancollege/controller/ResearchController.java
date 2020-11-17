package ca.sheridancollege.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;

import ca.sheridancollege.bean.Application;
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

	//Apply to a research
	@GetMapping("/apply/{researchid}")
	public String apply(Model model, @PathVariable int researchid) throws InterruptedException, ExecutionException {
		
		firestore = FirestoreClient.getFirestore();
		
		ResearchStudy research = firestore.collection("researchstudy")
				.whereEqualTo("researchStudyId", researchid)
				.get()
				.get()
				.getDocuments().get(0).toObject(ResearchStudy.class);
		
		Application application = new Application();
		
		//Set research id of the application
		application.setResearchID(research.getResearchStudyId());
		
		model.addAttribute("application", application);
		
		return "apply.html";
		
	}
	
	@GetMapping("/saveApplication")
	public String saveApplication(Model model, @ModelAttribute Application application) 
			throws InterruptedException, ExecutionException {
		
		//Set applied date
		SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy");
		
		firestore = FirestoreClient.getFirestore();
		
		Date date = new Date();
		
		application.setAppliedDate(formatter.format(date));
		
		//Set state

		application.setState("Not Decided");
		
		//Set ID
		int numApplications = Functions.getCollectionCount("application");
		
		application.setId(Functions.documentID(numApplications + 1, "application"));

		ResearchStudy research = firestore.collection("researchstudy").whereEqualTo("researchStudyId", 
				application.getResearchID()).get()
				.get().getDocuments().get(0).toObject(ResearchStudy.class);
		
		//Add application to the application collection
		firestore.collection("application").document(Integer.toString(application.getId())).set(application);
		
		firestore.collection("researchstudy").document(Integer.toString(research.getResearchStudyId())).set(research);
		
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

		ApiFuture<QuerySnapshot> snapshot = firestore.collection("researchstudy").get();

		List<Object> researches = Functions.getDocuments(snapshot, ResearchStudy.class);

		model.addAttribute("researches", Functions.searchResearch(researches, criteria, search));

		model.addAttribute("criterias", Functions.getCriterias());

		return "viewResearch.html";
	}
}

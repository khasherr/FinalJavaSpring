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
public class ResearcherController {

	//Variables
	
	@Autowired
	private EmailServiceImpl esi;
	
	private int applicationid = 0;
	private int researchid = 0;
	private Firestore firestore = null;
	
	//Researcher Home
	@GetMapping("/researchers")
	public String goResearcherHome() {
		
		return "ResearcherHome.html";
	}
	
	//Register research
	@GetMapping("/registerResearch")
	public String registerResearch(Model model) {
		
		model.addAttribute("research", new ResearchStudy());
		
		
		return "registerResearch.html";
	}
	
	@GetMapping("/saveResearch")
	public String saveResearch(Model model, @ModelAttribute ResearchStudy research, Authentication authentication) 
			throws InterruptedException, ExecutionException {
		
		firestore = FirestoreClient.getFirestore();
				
		SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy");
		
		Date date = new Date();
		
		research.setPostedDate(formatter.format(date));
		
		research.setUsername(authentication.getName());
		
		int numResearches = 0;
		
		try {
			numResearches = ca.sheridancollege.util.Functions.getCollectionCount("researchstudy");
		} catch (Exception e) {
			System.out.println("The collection \"researchstudy\" does not exist");
		}
		
		research.setResearchStudyId(Functions.documentID(numResearches + 1, "researchstudy"));
		
		// Save the new user details as a document
		try {
			firestore.collection("researchstudy").document(Long.toString(research.getResearchStudyId())).set(research);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		model.addAttribute("research", new ResearchStudy());
		
		return "registerResearch.html";
	}
	
	//Manage registered research
	//Users can only view researches they registered
	@GetMapping("/manageResearch")
	public String manageResearch(Model model, Authentication authentication) throws InterruptedException, ExecutionException {
		
		//Initialize firestore instance
		firestore = FirestoreClient.getFirestore();

		ApiFuture<QuerySnapshot> snapshot = firestore.collection("researchstudy")
				.whereEqualTo("username", authentication.getName())
				.get();
		
		model.addAttribute("researches", Functions.getDocuments(snapshot, ResearchStudy.class));
		
		model.addAttribute("criterias", Functions.getCriterias());
		
		return "manageResearch.html";
	}
	
	//Search registered researches
	@GetMapping("/searchResearchManage")
	public String searchManage(Model model, @RequestParam String search, @RequestParam String criteria,
			Authentication authentication) throws InterruptedException, ExecutionException {
		
		firestore = FirestoreClient.getFirestore();
		
		ApiFuture<QuerySnapshot> snapshot = firestore.collection("researchstudy")
				.whereEqualTo("username", authentication.getName())
				.get();
		
		List<Object> researches = Functions.getDocuments(snapshot, ResearchStudy.class);
		
		model.addAttribute("researches", Functions.searchResearch(researches, criteria, search));
		
		model.addAttribute("criterias", Functions.getCriterias());
		
		return "manageResearch.html";
	}
	
	//Edit research
	@GetMapping("/editResearch/{researchid}")
	public String editResearch(Model model, @PathVariable int researchid) throws InterruptedException, ExecutionException {
		
		firestore = FirestoreClient.getFirestore();
		
		ResearchStudy research = firestore.collection("researchstudy")
				.whereEqualTo("researchStudyId", researchid)
				.get()
				.get()
				.getDocuments().get(0).toObject(ResearchStudy.class);

		model.addAttribute("research", research);
		
		return "editResearch.html";
		
	}
	
	//Delete research
	@GetMapping("/deleteResearch/{researchid}")
	public String deleteResearch(Model model, @PathVariable int researchid, Authentication authentication) 
			throws InterruptedException, ExecutionException {

		firestore = FirestoreClient.getFirestore();
		
		ResearchStudy research = firestore.collection("researchstudy")
				.whereEqualTo("researchStudyId", researchid).get()
				.get()
				.getDocuments().get(0).toObject(ResearchStudy.class);

		// Retrieve the applications
		List<Application> applications = new ArrayList<Application>();

		ApiFuture<QuerySnapshot> snapshot = firestore.collection("application")
				.whereEqualTo("researchID", research.getResearchStudyId())
				.whereEqualTo("state", "Not Decided")
				.get();

		for (Object object : Functions.getDocuments(snapshot, Application.class)) {

			// All objects in the array are Application class objects
			Application application = (Application) object;

			applications.add(application);
		}

		// Send cancelation emails to the applicants
		for (Application application : applications) {
			String msg = "Research Canceled\n" + "\nResearch Title: " + research.getResearchTitle()
					+ "\nApplicant name: " + application.getName() + "\nAppication Title: " + application.getTitle();
			try {
				esi.sendMailWithInline(application.getEmail(), research.getResearchTitle() + " has been canceled",
						"Researva", msg, "Team Guacamole");
			} catch (MessagingException e) {
				System.out.println(e);
			}

			firestore.collection("application").document(Integer.toString(application.getId())).delete();
		}
		
		firestore.collection("researchstudy").document(Integer.toString(researchid)).delete();
		
		return "redirect:/manageResearch";
		
	}
	
	//Update research
	@GetMapping("/updateResearch")
	public String updateResearch(Model model, @ModelAttribute ResearchStudy research, Authentication authentication) 
			throws InterruptedException, ExecutionException {
		
		firestore = FirestoreClient.getFirestore();
		
		research.setUsername(authentication.getName());
		
		firestore.collection("researchstudy").document(research.getResearchStudyId().toString()).set(research);
		
		return "redirect:/manageResearch";
	}
	
	//View applications of a selected research
	@GetMapping("/viewApplications/{researchid}")
	public String viewApplications(Model model, @PathVariable int researchid) 
			throws InterruptedException, ExecutionException {
		
		firestore = FirestoreClient.getFirestore();
		
		ResearchStudy research = firestore.collection("researchstudy")
				.whereEqualTo("researchStudyId", researchid)
				.get()
				.get()
				.getDocuments().get(0).toObject(ResearchStudy.class);
		
		ApiFuture<QuerySnapshot> snapshot = firestore.collection("application")
				.whereEqualTo("researchID", researchid)
				.whereEqualTo("state", "Not Decided")
				.get();
		
		model.addAttribute("applications", Functions.getDocuments(snapshot, Application.class));
		
		model.addAttribute("research", research);
		
		this.researchid = research.getResearchStudyId();
		
		return "viewApplications.html";
	}
	
	//View selected application
	@GetMapping("/viewApplication/{id}")
	public String viewApplication(Model model, @PathVariable int id) 
			throws InterruptedException, ExecutionException {
		
		firestore = FirestoreClient.getFirestore();
		
		applicationid = id;
		
		Application application = firestore.collection("application").document(Integer.toString(id)).get()
				.get().toObject(Application.class);
		
		model.addAttribute("app", application);
		
		model.addAttribute("researchid", researchid);
		
		return "viewApplication.html";
		
		
	}
	
	//Reject application
	@GetMapping("/rejectApplication")
	public String rejectApplication(Model model) throws InterruptedException, ExecutionException {

		firestore = FirestoreClient.getFirestore();
		
		Application application = firestore.collection("application").document(Integer.toString(applicationid)).get()
				.get().toObject(Application.class);

		String email = application.getEmail();

		int researchid = application.getResearchID();

		ResearchStudy research = firestore.collection("researchstudy")
				.whereEqualTo("researchStudyId", researchid)
				.get()
				.get()
				.getDocuments().get(0).toObject(ResearchStudy.class);
		
		//Send reject email
		String msg = "Your application has been rejected.\n"
				+ "\nResearch Title: " + research.getResearchTitle()
				+ "\nApplicant name: " + application.getName()
				+ "\nAppication Title: " + application.getTitle();
		try {
			esi.sendMailWithInline(email, "Your application has been rejected", "Researva", msg, "Team Guacamole");
		} catch(MessagingException e) {
			System.out.println(e);
		}
		
		application.setState("Rejected");
		
		firestore.collection("application").document(Integer.toString(application.getId())).set(application);
		
		ApiFuture<QuerySnapshot> snapshot = firestore.collection("application")
				.whereEqualTo("researchID", researchid)
				.whereEqualTo("state", "Not Decided")
				.get();
		
		model.addAttribute("applications",  Functions.getDocuments(snapshot, Application.class));
		
		model.addAttribute("research", research);
		
		return "viewApplications.html";
	}
	
	//Accept application
	@GetMapping("/acceptApplication")
	public String acceptApplication(Model model) throws InterruptedException, ExecutionException {

		Application application = firestore.collection("application").document(Integer.toString(applicationid)).get()
				.get().toObject(Application.class);
		
		String email = application.getEmail();

		int researchid = application.getResearchID();
		
		ResearchStudy research = firestore.collection("researchstudy")
				.whereEqualTo("researchStudyId", researchid)
				.get()
				.get()
				.getDocuments().get(0).toObject(ResearchStudy.class);
		
		// Send Acceptance email
		String msg = "Your application has been accepted!\n" + 
		"\nApplicant name: " + application.getName() + 
		"\nAppication Title: " + application.getTitle() +
		"\nResearch Title: " + research.getResearchTitle() + 
		"\nResearch Institution: " + research.getResearchInstitution() +
		"\nResearch Area: " + research.getResearchArea() + 
		"\nResearch Details: " + research.getResearchDetail();
		
		try {
			esi.sendMailWithInline(email, "Your application has been accepted!", "Researva", msg, "Team Guacamole");
		} catch (MessagingException e) {
			System.out.println(e);
		}
		

		application.setState("Accepted");
		
		firestore.collection("application").document(Integer.toString(application.getId())).set(application);
		
		ApiFuture<QuerySnapshot> snapshot = firestore.collection("application")
				.whereEqualTo("researchID", researchid)
				.whereEqualTo("state", "Not Decided")
				.get();

		model.addAttribute("applications", Functions.getDocuments(snapshot, Application.class));
		
		model.addAttribute("research", research);
		
		return "viewApplications.html";
	}
	
	
}

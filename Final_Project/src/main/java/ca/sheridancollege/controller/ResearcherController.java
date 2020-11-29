package ca.sheridancollege.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.mail.MessagingException;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

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
		model.addAttribute("error", false);
		
		
		return "registerResearch.html";
	}
	
	private ArrayList<ArrayList<String>> researchValidation(Set<ConstraintViolation<ResearchStudy>> validationErrors){
		
		//Validation String lists
		List<String> errorTitle = new ArrayList<String>();
		List<String> errorArea = new ArrayList<String>();
		List<String> errorInstitution = new ArrayList<String>();
		List<String> errorDuration = new ArrayList<String>();
		List<String> errorName = new ArrayList<String>();
		List<String> errorDetails = new ArrayList<String>();
		List<String> errorNumParticipants = new ArrayList<String>();
		
		//Add the error messages
		for (ConstraintViolation<ResearchStudy> e : validationErrors) {
			if(e.getPropertyPath().toString().equals("researchTitle")) {
				errorTitle.add(e.getMessage());
			} else if(e.getPropertyPath().toString().equals("researchArea")) {
				errorArea.add(e.getMessage());
			} else if(e.getPropertyPath().toString().equals("researchInstitution")) {
				errorInstitution.add(e.getMessage());
			} else if(e.getPropertyPath().toString().equals("researchDuration")) {
				errorDuration.add(e.getMessage());
			} else if(e.getPropertyPath().toString().equals("postedBy")) {
				errorName.add(e.getMessage());
			} else if(e.getPropertyPath().toString().equals("researchDetail")) {
				errorDetails.add(e.getMessage());
			} else {
				errorNumParticipants.add(e.getMessage());
			}
		}
		
		ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
		
		//Add them in another arraylist
		data.add((ArrayList<String>) errorTitle);
		data.add((ArrayList<String>) errorArea);
		data.add((ArrayList<String>) errorInstitution);
		data.add((ArrayList<String>) errorDuration);
		data.add((ArrayList<String>) errorName);
		data.add((ArrayList<String>) errorDetails);
		data.add((ArrayList<String>) errorNumParticipants);
		
		return data;
	}
	
	//Save Researches
	@GetMapping("/saveResearch")
	public String saveResearch(Model model, @ModelAttribute ResearchStudy research, Authentication authentication) 
			throws InterruptedException, ExecutionException {
		
		//Validation
		Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
		Set<ConstraintViolation<ResearchStudy>> validationErrors = validator.validate(research);

		if (!validationErrors.isEmpty()) {
			
			ArrayList<ArrayList<String>> errors = researchValidation(validationErrors);
			
			model.addAttribute("errorTitle", errors.get(0));
			model.addAttribute("errorArea", errors.get(1));
			model.addAttribute("errorInstitution", errors.get(2));
			model.addAttribute("errorDuration", errors.get(3));
			model.addAttribute("errorName", errors.get(4));
			model.addAttribute("errorDetails", errors.get(5));
			model.addAttribute("errorNumParticipants", errors.get(6));

			model.addAttribute("research", research);
			model.addAttribute("error", true);

			return "registerResearch.html";
		}
		
		
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
		
		research.setApplyCount(0);
		
		// Save the new user details as a document
		try {
			firestore.collection("researchstudy").document(Long.toString(research.getResearchStudyId())).set(research);
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		
		return "redirect:/registerResearch?postresearch";
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
					errorFound = true;
				}
			} catch (Exception e) {
				errorFound = true;
				errors.add("Please type a number");
			}
		}
		
		ApiFuture<QuerySnapshot> snapshot = firestore.collection("researchstudy")
				.whereEqualTo("username", authentication.getName())
				.get();
		
		List<Object> researches = Functions.getDocuments(snapshot, ResearchStudy.class);
		
		model.addAttribute("criterias", Functions.getCriterias());
		
		if (errorFound) {

			model.addAttribute("researches", researches);

			model.addAttribute("errors", errors);

			return "manageResearch.html";
		}
		
		model.addAttribute("researches", Functions.searchResearch(researches, criteria, search));
		
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
	
	//Delete research - researcher
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
		
		//Sleep for 1 second
		//Wait for the update
		TimeUnit.MILLISECONDS.sleep(1000);
		
		if(ca.sheridancollege.util.Functions.getUserType(authentication).equals("ADMINISTRATOR")) {
			return "redirect:/studies";
		} else {
			return "redirect:/manageResearch";
		}
	}
	
	//Update research
	@GetMapping("/updateResearch")
	public String updateResearch(Model model, @ModelAttribute ResearchStudy research, Authentication authentication) 
			throws InterruptedException, ExecutionException {

		// Validation
		Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
		Set<ConstraintViolation<ResearchStudy>> validationErrors = validator.validate(research);

		if (!validationErrors.isEmpty()) {

			ArrayList<ArrayList<String>> errors = researchValidation(validationErrors);

			model.addAttribute("errorTitle", errors.get(0));
			model.addAttribute("errorArea", errors.get(1));
			model.addAttribute("errorInstitution", errors.get(2));
			model.addAttribute("errorDuration", errors.get(3));
			model.addAttribute("errorName", errors.get(4));
			model.addAttribute("errorDetails", errors.get(5));
			model.addAttribute("errorNumParticipants", errors.get(6));

			model.addAttribute("research", research);
			model.addAttribute("error", true);

			return "editResearch.html";
		}

		firestore = FirestoreClient.getFirestore();

		research.setUsername(authentication.getName());

		firestore.collection("researchstudy").document(research.getResearchStudyId().toString()).set(research);

		return "redirect:/editResearch/"+ research.getResearchStudyId() +"?update";
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

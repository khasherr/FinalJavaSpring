package ca.sheridancollege.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import ca.sheridancollege.bean.Application;
import ca.sheridancollege.bean.ResearchStudy;
import ca.sheridancollege.email.EmailServiceImpl;
import ca.sheridancollege.repository.ApplicationRepository;
import ca.sheridancollege.repository.ResearchStudyRepository;

@Controller 
public class ResearcherController {

	@Autowired
	private ResearchStudyRepository researchRepository;

	@Autowired
	private ApplicationRepository applicationRepository;
	
	@Autowired
	private EmailServiceImpl esi;
	
	private int applicationid = 0;
	private int researchid = 0;
	
	@GetMapping("/researchers")
	public String goResearcherHome() {
		
		return "ResearcherHome.html";
	}
	
	@GetMapping("/registerResearch")
	public String registerResearch(Model model) {
		
		model.addAttribute("research", new ResearchStudy());
		
		
		return "registerResearch.html";
	}
	
	@GetMapping("/saveResearch")
	public String saveResearch(Model model, @ModelAttribute ResearchStudy research, Authentication authentication) {
		
		SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy");
		
		Date date = new Date();
		
		research.setPostedDate(formatter.format(date));
		
		research.setUsername(authentication.getName());
		
		researchRepository.save(research);
		
		model.addAttribute("research", new ResearchStudy());
		
		return "registerResearch.html";
	}
	
	@GetMapping("/manageResearch")
	public String manageResearch(Model model, Authentication authentication) {
		
		model.addAttribute("researches", researchRepository.findByUsername(authentication.getName()));
		
		model.addAttribute("criterias", getCriterias());
		
		return "manageResearch.html";
	}
	
	private ArrayList<String> getCriterias() {

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
	
	@GetMapping("/searchResearchManage")
	public String searchManage(Model model, @RequestParam String search, @RequestParam String criteria,
			Authentication authentication) {
		
		if(criteria.equals("Research Title")) {

			model.addAttribute("researches", researchRepository.
					findByResearchTitleContainingAndUsername(search, authentication.getName()));
		} else if(criteria.equals("Research Area")) {

			model.addAttribute("researches", researchRepository.
					findByResearchAreaContainingAndUsername(search, authentication.getName()));
		}else if(criteria.equals("Research Institution")) {

			model.addAttribute("researches", researchRepository.
					findByResearchInstitutionContainingAndUsername(search, authentication.getName()));
		}else if(criteria.equals("Research Duration")) {

			model.addAttribute("researches", researchRepository.
					findByResearchDurationContainingAndUsername(search, authentication.getName()));
		}else if(criteria.equals("Researcher")) {

			model.addAttribute("researches", researchRepository.
					findByPostedByContainingAndUsername(search, authentication.getName()));
		}else if(criteria.equals("Posted Date")) {

			model.addAttribute("researches", researchRepository.
					findByPostedDateContainingAndUsername(search, authentication.getName()));
		}else if(criteria.equals("Research Detail")) {

			model.addAttribute("researches", researchRepository.
					findByResearchDetailContainingAndUsername(search, authentication.getName()));
		}else if(criteria.equals("Minimum Number of Participants")) {
			try {
				model.addAttribute("researches", researchRepository
						.findByNumParticipantsGreaterThanEqualAndUsername(Integer.parseInt(search), authentication.getName()));
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
		}else {
			
			//Maximum number of participants

			try {
				model.addAttribute("researches", researchRepository
						.findByNumParticipantsLessThanEqualAndUsername(Integer.parseInt(search), authentication.getName()));
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
			
			
		}

		model.addAttribute("criterias", getCriterias());

		return "manageResearch.html";
	}
	
	@GetMapping("/editResearch/{researchid}")
	public String editResearch(Model model, @PathVariable int researchid) {
		
		ResearchStudy research = researchRepository.findById(researchid).get();

		model.addAttribute("research", research);
		
		return "editResearch.html";
		
	}
	
	@GetMapping("/updateResearch")
	public String updateResearch(Model model, @ModelAttribute ResearchStudy research, Authentication authentication) {
		
		researchRepository.save(research);

		model.addAttribute("researches", researchRepository.findByUsername(authentication.getName()));

		model.addAttribute("criterias", getCriterias());
		
		return "manageResearch.html";
	}
	
	@GetMapping("/viewApplications/{researchid}")
	public String viewApplications(Model model, @PathVariable int researchid) {
		
		ResearchStudy research = researchRepository.findById(researchid).get();
		
		model.addAttribute("applications", applicationRepository.findByResearchIDAndState(researchid, "Not Decided"));
		
		model.addAttribute("research", research);
		
		this.researchid = research.getResearchStudyId();
		
		return "viewApplications.html";
	}
	
	@GetMapping("/viewApplication/{id}")
	public String viewApplication(Model model, @PathVariable int id) {
		
		applicationid = id;
		
		model.addAttribute("app", applicationRepository.findById(id));
		
		model.addAttribute("researchid", researchid);
		
		return "viewApplication.html";
		
		
	}
	
	@GetMapping("/rejectApplication")
	public String rejectApplication(Model model) {

		Application application = applicationRepository.findById(applicationid);

		String email = application.getEmail();

		int researchid = application.getResearchID();

		ResearchStudy research = researchRepository.findById(researchid).get();
		
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
		
		applicationRepository.save(application);
		

		model.addAttribute("applications", applicationRepository.findByResearchIDAndState(researchid, "Not Decided"));
		
		model.addAttribute("research", research);
		
		return "viewApplications.html";
	}
	
	@GetMapping("/acceptApplication")
	public String acceptApplication(Model model) {

		Application application = applicationRepository.findById(applicationid);
		
		String email = application.getEmail();

		int researchid = application.getResearchID();
		
		ResearchStudy research = researchRepository.findById(researchid).get();
		
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
		
		applicationRepository.save(application);

		model.addAttribute("applications", applicationRepository.findByResearchIDAndState(researchid, "Not Decided"));
		
		model.addAttribute("research", research);
		
		return "viewApplications.html";
	}
	
	
}

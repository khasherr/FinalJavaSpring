package ca.sheridancollege.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import ca.sheridancollege.bean.Application;
import ca.sheridancollege.bean.ResearchStudy;
import ca.sheridancollege.repository.ApplicationRepository;
import ca.sheridancollege.repository.ResearchStudyRepository;
import ca.sheridancollege.repository.ResearchersRepository;

@Controller
public class ResearchController {
	
	//Variables
	@Autowired
	private ResearchStudyRepository researchRepository;
	
	@Autowired
	private ResearchersRepository researcherRepository;
	
	@Autowired
	private ApplicationRepository applicationRepository;
	
	//List researches
	@GetMapping("/studies")
	public String listResearch(Model model) {
		
		model.addAttribute("researches", researchRepository.findAll());
		
		model.addAttribute("criterias", getCriterias());
		
		return "viewResearch.html";
	}

	//Search criterias
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
	
	//Apply to a research
	@GetMapping("/apply/{researchid}")
	public String apply(Model model, @PathVariable int researchid) {
		
		model.addAttribute("research", researchRepository.findById(researchid).get());
		
		ResearchStudy research = researchRepository.findById(researchid).get();
		
		Application application = new Application();
		
		//Set research id of the application
		application.setResearchID(research.getResearchStudyId());
		
		model.addAttribute("application", application);
		
		return "apply.html";
		
	}
	
	@GetMapping("/saveApplication")
	public String saveApplication(Model model, @ModelAttribute Application application) {
		
		//Set applied date
		SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy");

		Date date = new Date();
		
		application.setAppliedDate(formatter.format(date));
		
		//Set state
		
		application.setState("Not Decided");
		
		//Save
		applicationRepository.save(application);
		
		
		ResearchStudy research = researchRepository.findById(application.getResearchID()).get();
		
		research.getApplications().add(application);
		
		researchRepository.save(research);

		model.addAttribute("researches", researchRepository.findAll());

		model.addAttribute("criterias", getCriterias());
		
		return "viewResearch.html";
	}
	
	//View details of a research
	@GetMapping("/viewDetails/{researchid}")
	public String viewDetails(Model model, @PathVariable int researchid) {
		
		model.addAttribute("research", researchRepository.findById(researchid).get());
		
		return "viewDetails.html";
		
	}

	//Search features for users
	@GetMapping("/searchResearch")
	public String search(Model model, @RequestParam String search, @RequestParam String criteria) {
		
		if(criteria.equals("Research Title")) {

			model.addAttribute("researches", researchRepository.findByResearchTitleContaining(search));
		} else if(criteria.equals("Research Area")) {

			model.addAttribute("researches", researchRepository.findByResearchAreaContaining(search));
		}else if(criteria.equals("Research Institution")) {

			model.addAttribute("researches", researchRepository.findByResearchInstitutionContaining(search));
		}else if(criteria.equals("Research Duration")) {

			model.addAttribute("researches", researchRepository.findByResearchDurationContaining(search));
		}else if(criteria.equals("Researcher")) {

			model.addAttribute("researches", researchRepository.findByPostedByContaining(search));
		}else if(criteria.equals("Posted Date")) {

			model.addAttribute("researches", researchRepository.findByPostedDateContaining(search));
		}else if(criteria.equals("Research Detail")) {

			model.addAttribute("researches", researchRepository.findByResearchDetailContaining(search));
		}else if(criteria.equals("Minimum Number of Participants")) {
			try {
				model.addAttribute("researches", researchRepository
						.findByNumParticipantsGreaterThanEqual(Integer.parseInt(search)));
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
		}else {
			
			//Maximum number of participants

			try {
				model.addAttribute("researches", researchRepository
						.findByNumParticipantsLessThanEqual(Integer.parseInt(search)));
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
			
			
		}

		model.addAttribute("criterias", getCriterias());

		return "viewResearch.html";
	}
}

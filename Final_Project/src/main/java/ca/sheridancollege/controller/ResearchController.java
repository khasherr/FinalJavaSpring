package ca.sheridancollege.controller;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import ca.sheridancollege.bean.Application;
import ca.sheridancollege.bean.ResearchStudy;
import ca.sheridancollege.repository.ApplicationRepository;
import ca.sheridancollege.repository.ResearchStudyRepository;
import ca.sheridancollege.repository.ResearchersRepository;

@Controller
public class ResearchController {
	
	@Autowired
	private ResearchStudyRepository researchRepository;
	
	@Autowired
	private ResearchersRepository researcherRepository;
	
	@Autowired
	private ApplicationRepository applicationRepository;
	
	@GetMapping("/studies")
	public String listResearch(Model model) {
		
		model.addAttribute("researches", researchRepository.findAll());
		
		ArrayList<String> criterias = new ArrayList<>();
		
		criterias.add("Research Title");
		criterias.add("Research Area");
		criterias.add("Research Institution");
		criterias.add("Research Duration");
		criterias.add("Researcher");
		criterias.add("Posted Date");
		criterias.add("Research Detail");
		
		model.addAttribute("criterias", criterias);
		
		return "viewResearch.html";
	}
	
	@GetMapping("/apply/{researchid}")
	public String apply(Model model, @PathVariable int id) {
		
		model.addAttribute("research", researchRepository.findById(id).get());
		
		ResearchStudy research = researchRepository.findById(id).get();
		
		Application application = new Application();
		
		application.setResearchID(research.getResearchStudyId());
		
		model.addAttribute("application", application);
		
		return "apply.html";
		
	}
	
	@GetMapping("/saveApplication")
	public String saveApplication(Model model, @ModelAttribute Application application) {
		
		
		applicationRepository.save(application);

		model.addAttribute("researches", researchRepository.findAll());
		
		return "viewResearch.html";
	}
	
	@GetMapping("/viewDetails/{researchid}")
	public String viewDetails(Model model, @PathVariable int id) {
		
		model.addAttribute("research", researchRepository.findById(id).get());
		
		return "viewDetails.html";
		
	}
}

package ca.sheridancollege.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import ca.sheridancollege.bean.ResearchStudy;
import ca.sheridancollege.repository.ResearchStudyRepository;

@Controller 
public class ResearcherController {

	@Autowired
	private ResearchStudyRepository researchRepository;
	
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
	public String saveResearch(Model model, @ModelAttribute ResearchStudy research) {
		
		researchRepository.save(research);
		
		model.addAttribute("research", new ResearchStudy());
		
		return "registerResearch.html";
	}
	
	
}

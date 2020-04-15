package ca.sheridancollege.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import ca.sheridancollege.repository.ResearchStudyRepository;
import ca.sheridancollege.repository.ResearchersRepository;

@Controller
public class ResearchController {
	
	@Autowired
	private ResearchStudyRepository researchRepository;
	
	@Autowired
	private ResearchersRepository researcherRepository;
	
	@GetMapping("/studies")
	public String listResearch(Model model) {
		
		model.addAttribute("researches", researchRepository.findAll());
		
		return "viewResearch.html";
	}
}

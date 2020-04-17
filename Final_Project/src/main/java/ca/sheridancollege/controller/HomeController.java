package ca.sheridancollege.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
	
	
	
	@GetMapping("/")
	public String displayHome( ) { 
		return "home.html";
	}
	
	@GetMapping("/learnMore")
	public String learnMore() {
		
		return "learnMore.html";
	}
	
	@GetMapping("/get-involved")
	public String getInvolved() {
		
		return "learnMore.html";
	}
	
	@GetMapping("/access-denied")
	public String goaccessdenied() {
		
		return "access-denied.html";
		
	}

}

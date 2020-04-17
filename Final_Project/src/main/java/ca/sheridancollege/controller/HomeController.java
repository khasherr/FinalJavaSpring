package ca.sheridancollege.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
	
	
	//Home
	@GetMapping("/")
	public String displayHome( ) { 
		return "home.html";
	}
	
	//Learn More page
	@GetMapping("/learnMore")
	public String learnMore() {
		
		return "learnMore.html";
	}
	
	//Get Involved page
	@GetMapping("/get-involved")
	public String getInvolved() {
		
		return "learnMore.html";
	}
	
	//Access denied page
	@GetMapping("/access-denied")
	public String goaccessdenied() {
		
		return "access-denied.html";
		
	}

}

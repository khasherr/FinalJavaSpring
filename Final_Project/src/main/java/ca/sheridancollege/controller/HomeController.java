package ca.sheridancollege.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
	
	Authentication auth = null;
	
	//Home
	@GetMapping("/")
	public String displayHome(Model model) {
		
		auth = SecurityContextHolder.getContext().getAuthentication();
		
		model = add(model, auth, "home");
		
		return "home.html";
	}
	
	@GetMapping("/login")
	public String login() {
		return "login.html";
	}
	
	//Learn More page
	@GetMapping("/about-us")
	public String aboutUs(Model model) {
		
		model = add(model, auth, "");
		
		return "aboutUs.html";
	}
	
	//Access denied page
	@GetMapping("/access-denied")
	public String goaccessdenied(Model model) {
		
		model = add(model, auth, "");
		
		return "access-denied.html";
		
	}
	
	@GetMapping("/error")
	public String handleError(Model model) {
		return "error.html";
	}
	
	protected static Model add(Model model, Authentication auth, String home) {
		
		if(auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
			//If the user is logged in
			if(home.equals("home")) {
				model.addAttribute("status", "logged in");
			}
			
			if(ca.sheridancollege.util.Functions.getUserType(auth).equals("ADMINISTRATOR")) {
				model.addAttribute("role", "ADMINISTRATOR");
			} else {
				model.addAttribute("role", "USER");
			}
			
		} else {
			if(home.equals("home")) {
			//If the user is not logged in
				model.addAttribute("status", "logged out");
			}
			
			model.addAttribute("role", "NONE");
		}
		
		
		return model;
	}
}

package ca.sheridancollege.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import ca.sheridancollege.bean.UserAccount;
import ca.sheridancollege.repository.RoleRepository;
import ca.sheridancollege.repository.UserAccountRepository;


@Controller
public class SecurityController {
	
	//we want to encrypt the password 
	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired 
	UserAccountRepository accountRepo;
	
	@Autowired
	private RoleRepository roleRepository;

	//register
	@GetMapping("/register")
	public String register(Model model) { 
		// already made an entity for userAccount
		// get everything from userAccount entity
		UserAccount userAccount = new UserAccount();
		//add it to the model - we calling it userAccount(in blue) and the object 
		// we instantiated from line 16 userAccount 
		model.addAttribute("userAccount", userAccount);
		return "register";
		
	}
	
	//save
	
	@PostMapping("/register/save")
	public String saveUser(Model model, UserAccount user) { 
		//get the user password through lombok getter and setter
		user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
		user.getRoles().add(roleRepository.findByRolename("ROLE_USER"));
		accountRepo.save(user);
		//
		return "redirect:/";
}
}

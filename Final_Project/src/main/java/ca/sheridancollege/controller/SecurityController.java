package ca.sheridancollege.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;

import ca.sheridancollege.bean.Role;
import ca.sheridancollege.bean.UserAccount;
import ca.sheridancollege.util.Functions;


@Controller
public class SecurityController {
	
	//we want to encrypt the password 
	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;

	//register
	@GetMapping("/register/{options}")
	public String register(Model model, @PathVariable String options) { 
		// already made an entity for userAccount
		// get everything from userAccount entity
		UserAccount userAccount = new UserAccount();
		//add it to the model - we calling it userAccount(in blue) and the object 
		// we instantiated from line 16 userAccount 
		model.addAttribute("userAccount", userAccount);
		
		int option = Integer.parseInt(options);
		
		model.addAttribute("options", option);
		
		if(option == 0) {
			//Organization code
			
		} else {
			//Organization mail
			
		}
		
		return "register";
		
	}
	
	@GetMapping("/registerOptions")

	public String registerOptions(Model model) {
		
		return "registerOptions.html";
	}
	
	//save
	
	@PostMapping("/register/save/{options}")
	public String saveUser(Model model, UserAccount user, @PathVariable String options) 
			throws InterruptedException, ExecutionException {
		
		Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
		Set<ConstraintViolation<UserAccount>> validationErrors = validator.validate(user);

		if (!validationErrors.isEmpty()) {
			List<String> errorUsername = new ArrayList<String>();
			List<String> errorEmail = new ArrayList<String>();
			List<String> errorPassword = new ArrayList<String>();
			for (ConstraintViolation<UserAccount> e : validationErrors) {
				if(e.getPropertyPath().toString().equals("username")) {
					errorUsername.add(e.getMessage());
				} else if(e.getPropertyPath().toString().equals("email")) {
					errorEmail.add(e.getMessage());
				} else if(e.getPropertyPath().toString().equals("password")) {
					errorPassword.add(e.getMessage());
				}
			}
			model.addAttribute("errorUsername", errorUsername);
			model.addAttribute("errorEmail", errorEmail);
			model.addAttribute("errorPassword", errorPassword);

			model.addAttribute("userAccount", user);

			return "register.html";
		}
		
		//Initialize firestore instance
		Firestore firestore = FirestoreClient.getFirestore();
		
		//get the user password through lombok getter and setter
		user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
		user.getRoles().add(new Role("ROLE_USER", Integer.toUnsignedLong(0)));
		
		
		int numUsers = Functions.getCollectionCount("user");
		
		user.setUserId(numUsers + 1);
		//Save the new user details as a document
		try {
			firestore.collection("user").document(Long.toString(numUsers)).set(user);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return "redirect:/";
}
}

package ca.sheridancollege.controller;

import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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
	public String saveUser(Model model, UserAccount user) throws InterruptedException, ExecutionException {
		
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

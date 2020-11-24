package ca.sheridancollege.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import javax.mail.MessagingException;
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

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;

import ca.sheridancollege.bean.AccountHolder;
import ca.sheridancollege.bean.Role;
import ca.sheridancollege.bean.UserAccount;
import ca.sheridancollege.email.EmailServiceImpl;
import ca.sheridancollege.util.Functions;


@Controller
public class SecurityController {
	
	//we want to encrypt the password 
	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private EmailServiceImpl esi;
	
	private static AccountHolder account = new AccountHolder();
	
	//register
	@GetMapping("/register/{options}")
	public String register(Model model, @PathVariable String options) { 
		// already made an entity for userAccount
		// get everything from userAccount entity
		//UserAccount userAccount = new UserAccount();
		
		//add it to the model - we calling it userAccount(in blue) and the object 
		// we instantiated from line 16 userAccount 
		
		//Create a holder object
		AccountHolder holder = new AccountHolder(new UserAccount(), "");
		
		model.addAttribute("holder", holder);
		
		
		//Option 0 - code
		//Option 1 - organization domain
		model.addAttribute("options", options);
		
		return "register";
		
	}
	
	@GetMapping("/registerOptions")
	public String registerOptions(Model model) {
		
		return "registerOptions.html";
	}
	
	@GetMapping("/resend")
	public String resend(Model model) {
		
		account.setCode(Functions.getCode(6));

		String msg = "Verification Code: " + account.getCode() + "\nResearva";

		try {
			esi.sendMailWithInline(account.getAccount().getEmail(), 
					"Verification", "Researva", msg, "Team Guacamole");
		} catch (MessagingException e) {
			System.out.println(e);
		}

		return "verify.html";
	}
	
	@GetMapping("/verifyCode")
	public String verifyCode(Model model, AccountHolder holder) throws InterruptedException, ExecutionException {
		
		String code = holder.getCode();
		
		if(!account.getCode().equals(code)) {

			List<String> errorCode = new ArrayList<String>();
			errorCode.add("The code does not match");
			
			model.addAttribute("errorCode", errorCode);
			
			model.addAttribute("holder", holder);
			
			return "verify.html";
		}
		
		UserAccount user = account.getAccount();

		// get the user password through lombok getter and setter
		user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
		user.getRoles().add(new Role("ROLE_USER", Integer.toUnsignedLong(0)));

		int numUsers = getUserId();
		
		// Initialize firestore instance
		Firestore firestore = FirestoreClient.getFirestore();

		user.setUserId(numUsers);
		// Save the new user details as a document
		try {
			firestore.collection("user").document(Long.toString(numUsers)).set(user);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return "redirect:/";
	}
	
	
	//save
	
	@PostMapping("/register/save/{options}")
	public String saveUser(Model model, AccountHolder holder, @PathVariable String options) 
			throws InterruptedException, ExecutionException {
		
		model.addAttribute("holder", holder);
		
		UserAccount user = holder.getAccount();
		String code = holder.getCode();
		
		//validation
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

			return "register.html";
		}
		
		// Initialize firestore instance
		Firestore firestore = FirestoreClient.getFirestore();
		
		ApiFuture<QuerySnapshot> future = firestore.collection("user")
				.whereEqualTo("email", user.getEmail()).get();
		
		List<QueryDocumentSnapshot> documents = future.get().getDocuments();
		
		//Check if the email already exists
		if(!documents.isEmpty()) {

			List<String> errorEmail = new ArrayList<String>();
			
			errorEmail.add("The email is already being used by another account.");
			
			model.addAttribute("errorEmail", errorEmail);
			
			return "register.html";
			
		}
		
		//Validation with options
		int option = Integer.parseInt(options);
		
		if(option == 0) {
			//Organization code
			//Check the code
			
			boolean codeCheck = Pattern.compile("^[a-zA-Z0-9]*$").matcher(code).matches();
			
			List<String> errorCode = new ArrayList<String>();
			
			if(code.length() != 20 || !codeCheck) {
				errorCode.add("The Researva code consists of 20 alphanumeric characters.");
				
				model.addAttribute("errorCode", errorCode);
				
				return "register.html";
				
			}
			
			future = firestore.collection("organization")
					.whereEqualTo("code", code)
					.get();
			
			documents = future.get().getDocuments();
			
			//Check if the code exists
			if(documents.isEmpty()) {
				
				errorCode.add("There is no approved organization with the code. "
						+ "Please check with your organization.");
				
				model.addAttribute("errorCode", errorCode);
				
				return "register.html";
				
			}
			
		} else {
			//Organization mail
			//Check the domain
			
			String email = user.getEmail();
			
			String[] substring = email.split("@");
			
			String domain = substring[1];
			
			future = firestore.collection("organization")
					.whereEqualTo("domain", domain).get();
			
			documents = future.get().getDocuments();
			
			//Check if the domain exists
			if(documents.isEmpty()) {

				List<String> errorEmail = new ArrayList<String>();
				
				errorEmail.add("There is no approved organization with the domain provided. "
						+ "Please double check with your organization.");
				
				model.addAttribute("errorEmail", errorEmail);
				
				return "register.html";
				
			}

		}

		// Send verification code
		account.setCode(Functions.getCode(6));
		account.setAccount(user);

		String msg = "Verification Code: " + account.getCode() + "\nResearva";

		try {
			if (esi != null) {
				esi.sendMailWithInline(user.getEmail(), "Verification", "Researva", msg, "Team Guacamole");
			} else {
				System.out.println("esi == null");
			}
		} catch (MessagingException e) {
			System.out.println(e);
		}
		
		holder.setCode("");
		
		model.addAttribute("holder", holder);
		
		return "verify.html";
	}
	
	private static int getUserId() throws InterruptedException, ExecutionException {
		
		boolean found = false;
		
		int userid = Functions.getCollectionCount("user");

		// Initialize firestore instance
		Firestore firestore = FirestoreClient.getFirestore();
				
		while(!found) {
			
			ApiFuture<QuerySnapshot> future = firestore.collection("user")
					.whereEqualTo("userId", userid).get();

			List<QueryDocumentSnapshot> documents = future.get().getDocuments();
			
			//Check if the user id exists
			if(documents.isEmpty()) { //If found
				found = true;
			} else { //Not found
				userid += 1;
			}
		}
		
		return userid;
	}
	
}

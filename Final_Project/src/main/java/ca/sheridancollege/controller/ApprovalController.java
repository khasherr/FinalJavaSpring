package ca.sheridancollege.controller;

import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.mail.MessagingException;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;

import ca.sheridancollege.bean.ApprovalRequest;
import ca.sheridancollege.bean.Organization;
import ca.sheridancollege.email.EmailServiceImpl;
import ca.sheridancollege.util.Functions;

@Controller
public class ApprovalController {

	@Autowired
	private EmailServiceImpl esi;
	
	private Firestore firestore = null;

	Authentication auth = null;
	
	private static Hashtable<String, ApprovalRequest> requestTable = new Hashtable<>();
	
	//Display the request form
	@GetMapping("/request")
	public String request(Model model) {
		
		ApprovalRequest approval = new ApprovalRequest();
		
		model.addAttribute("request", approval);
		
		//Initialize the authentication instance
		auth = SecurityContextHolder.getContext().getAuthentication();
		
		model = HomeController.add(model, auth, "home");
		
		return "approvalRequest.html";
	}
	
	//Reject an organization request
	@GetMapping("/rejectRequest/{name}")
	public String rejectRequest(Model model, @PathVariable String name) {
		
		ApprovalRequest request = requestTable.get(name);
		
		
		// Send rejection email
		String msg = "Hello, " + request.getName()
				+ "\nSorry, your approval request has been rejected."
				+ "\n\nOur administrators reviewed your request and decided to reject it."
				+ "\nThank you for applying to Researva";
		
		try {
			esi.sendMailWithInline(request.getEmail(), "Your request has been rejected", "Researva", 
					msg, "Team Guacamole");
		} catch(MessagingException e) {
			System.out.println(e);
		}

		firestore.collection("approvalrequest").document(name).delete();

		return "redirect:/approval";
	}
	
	//Approve an organization request
	@GetMapping("/approveRequest/{name}")
	public String approveRequest(Model model, @PathVariable String name) throws InterruptedException {
		
		ApprovalRequest request = requestTable.get(name);
		
		String code = Functions.getCode(20);
		
		Organization organization = new Organization(
				request.getName(),
				request.getWebsite(),
				request.getEmail(),
				request.getAddress(),
				request.getPoc(),
				request.getDomain(),
				code);
		
		firestore.collection("organization").document(name).set(organization);
		
		// Send approval email with a 20 digit randomly generated code
		String msg = "Hello, " + request.getName()
				+ "\nYour approval request has been approved."
				+ "\n\nOur administrators reviewed your request and decided to approve it."
				+ "\nThis is the code for your organization: " + code
				+ "\nWelcome to Researva";
		
		try {
			esi.sendMailWithInline(request.getEmail(), "Your request has been approved", "Researva", 
					msg, "Team Guacamole");
		} catch(MessagingException e) {
			System.out.println(e);
		}

		firestore.collection("approvalrequest").document(name).delete();

		// Sleep for 1 second
		// Wait for the update
		TimeUnit.MILLISECONDS.sleep(1000);
				
		return "redirect:/approval";
	}

	//View the list of approval requests
	@GetMapping("/approval")
	public String approval(Model model) throws InterruptedException, ExecutionException {
		
		firestore = FirestoreClient.getFirestore();

		ApiFuture<QuerySnapshot> snapshot = firestore.collection("approvalrequest").get();
		
		List<Object> requests = Functions.getDocuments(snapshot, ApprovalRequest.class);
		
		model.addAttribute("requests", requests);
		
		requestTable.clear();
		
		for(int i = 0; i < requests.size(); i++) {
			
			ApprovalRequest request = (ApprovalRequest) requests.get(i);
			requestTable.put(request.getName(), request);
		}
		
		
		return "approval.html";
		
	}
	
	//View the details of a request
	@GetMapping("/viewRequest/{name}")
	public String viewRequest(Model model, @PathVariable String name) {
		
		ApprovalRequest request = requestTable.get(name);
		
		model.addAttribute("request", request);
		
		
		return "viewRequest.html";
	}
	
	//Submit the request
	@PostMapping("/submitRequest")
	public String submit(Model model, ApprovalRequest request) {
		
		model.addAttribute("request", request);
		
		//Validation
		Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
		Set<ConstraintViolation<ApprovalRequest>> validationErrors = validator.validate(request);
		
		if (!validationErrors.isEmpty()) {
			List<String> errorName = new ArrayList<String>();
			List<String> errorWebsite = new ArrayList<String>();
			List<String> errorAddress = new ArrayList<String>();
			List<String> errorDomain = new ArrayList<String>();
			List<String> errorPOC = new ArrayList<String>();
			List<String> errorEmail = new ArrayList<String>();

			for (ConstraintViolation<ApprovalRequest> e : validationErrors) {
				if (e.getPropertyPath().toString().equals("name")) {
					errorName.add(e.getMessage());
				} else if (e.getPropertyPath().toString().equals("website")) {
					errorWebsite.add(e.getMessage());
				} else if (e.getPropertyPath().toString().equals("address")) {
					errorAddress.add(e.getMessage());
				} else if (e.getPropertyPath().toString().equals("poc")) {
					errorPOC.add(e.getMessage());
				} else if (e.getPropertyPath().toString().equals("email")) {
					errorEmail.add(e.getMessage());
				}else if (e.getPropertyPath().toString().equals("domain")) {
					errorDomain.add(e.getMessage());
				}
			}

			model.addAttribute("errorName", errorName);
			model.addAttribute("errorWebsite", errorWebsite);
			model.addAttribute("errorAddress", errorAddress);
			model.addAttribute("errorPOC", errorPOC);
			model.addAttribute("errorEmail", errorEmail);
			model.addAttribute("errorDomain", errorDomain);
			
			return "approvalRequest.html";
		}
		
		Firestore firestore = FirestoreClient.getFirestore();
		
		// Save the new request
		//Organization name as the primary key
		try {
			firestore.collection("approvalrequest").document(request.getName()).set(request);
		} catch (Exception e) {
			e.printStackTrace();
			
			List<String> errors = new ArrayList<String>();
			
			errors.add(e.getMessage());
			
			model.addAttribute("errors", errors);
			
			return "approvalRequest.html";
		}
		
		return "redirect:/request?requestsubmitted";
	}

}

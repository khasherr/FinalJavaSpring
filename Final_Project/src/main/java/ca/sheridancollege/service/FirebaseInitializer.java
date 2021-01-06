package ca.sheridancollege.service;

import java.io.FileInputStream;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

@Service
public class FirebaseInitializer {

	//Firebase Initializer
	//The Web part needs to have an initializer
	//Read the given JSON file which contains the given API key
	//Connect to the Firebase service using the given information
	@PostConstruct
	public void initialize(){
		try {

			FileInputStream serviceAccount = new FileInputStream("./capstone-c5c44-firebase-adminsdk-qpjnq-7e56bdcc94.json");

			FirebaseOptions options = new FirebaseOptions.Builder()
					.setCredentials(GoogleCredentials.fromStream(serviceAccount))
					.setDatabaseUrl("https://capstone-c5c44.firebaseio.com").build();

			FirebaseApp.initializeApp(options);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

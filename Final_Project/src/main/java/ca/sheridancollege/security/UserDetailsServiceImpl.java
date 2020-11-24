package ca.sheridancollege.security;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;

import ca.sheridancollege.bean.Role;
import ca.sheridancollege.bean.UserAccount;


@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		Firestore firestore = FirestoreClient.getFirestore();
		
		ApiFuture<QuerySnapshot> userCollection = firestore.collection("user").whereEqualTo("username", username).get();
		
		List<QueryDocumentSnapshot> documents = null;
		
		
		try {
			documents = userCollection.get().getDocuments();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		
		ca.sheridancollege.bean.UserAccount user = null;
		
		//1 document will be returned
		for (DocumentSnapshot document : documents) {
			user = document.toObject(UserAccount.class);
		}
		
		if (user == null) {
			System.out.println("User " + username + " not found");
			throw new UsernameNotFoundException("User " + username + " was not found in the database");
		}
		
		List<GrantedAuthority> grantList = new ArrayList<GrantedAuthority>();
		
		for (Role role : user.getRoles()) {
			
			grantList.add(new SimpleGrantedAuthority(role.getRolename()));
		}
		
		UserDetails userDetails = (UserDetails) new User(user.getUsername(), user.getPassword(), grantList);
		
		
		return userDetails;
	}

}

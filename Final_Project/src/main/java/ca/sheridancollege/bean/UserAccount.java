package ca.sheridancollege.bean;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
//import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
//import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import ca.sheridancollege.bean.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//associating this UserAccount
//UserAccount = user_account in the Postgres

@Data
@AllArgsConstructor
@NoArgsConstructor

@Entity

@Table(name="user_accounts")
public class UserAccount {

	// here I am using my on generation strategy using the the user_accounts_seq . sql file 
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	//PostgreSQL
	//@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "generator_name")
	//@SequenceGenerator(name="generator_name", sequenceName = "user_accounts_seq", allocationSize=1)
   
	// associating userId with column name user_id in postgres
	//@Column(name="user_id")
    private long userId; 
	
	
	//column annotati used bc  if we have camelcase by default spring looks for 
	// underscors in postgres table properties but in postgres there aren't any underscore
	// so here I am associating the property username == in postgres with userName
	// Code for PostgreSQL
	
	@NotNull(message="Please type the User Name")
	@NotEmpty(message="Please type the User Name")
	@Pattern(regexp="^[a-zA-Z0-9]*$", message="Must contain alphanumeric characters only")
	private String username;
	@NotNull(message="Please type the Email")
	@NotEmpty(message="Please type the Email")
	@Email(message = "Email is not valid. The email should be in a proper email format.")
	private String email;
	@NotNull(message="Please type the Password")
	@NotEmpty(message="Please type the Password")
	private String password;
	private boolean enabled = true;
	
	@ManyToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	List<Role> roles = new ArrayList<Role>();

}

package ca.sheridancollege.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
//A holder class for an Account object and a String code
//Used for validations during the registration process
public class AccountHolder {
	
	private UserAccount account;
	private String code;
}

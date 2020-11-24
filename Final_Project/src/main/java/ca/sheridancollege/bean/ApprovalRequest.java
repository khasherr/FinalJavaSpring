package ca.sheridancollege.bean;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name="approval_request")
public class ApprovalRequest {
	
	//Organization name
	@Id
	@NotBlank(message="Please type at least one non-whitespace character")
	@NotNull(message="Please type the name of the organization")
	@NotEmpty(message="Please type the name of the organization")
	private String name;
	//Website is not required
	private String website;
	@NotBlank(message="Please type at least one non-whitespace character")
	@NotNull(message="Please type the Email")
	@NotEmpty(message="Please type the Email")
	@Email(message = "Email is not valid. The email should be in a proper email format.")
	private String email;
	//Address can be written in any format
	@NotBlank(message="Please type at least one non-whitespace character")
	@NotNull(message="Please type the address")
	@NotEmpty(message="Please type the address")
	private String address;
	//Point of Contact
	@NotBlank(message="Please type at least one non-whitespace character")
	@NotNull(message="Please type the Point of Contact")
	@NotEmpty(message="Please type the Point of Contact")
	@Pattern(regexp="^[a-zA-Z0-9,\\-. ]*$", message="Alphanumeric characters, \",\", and the whitespace are "
				+ "allowed")
	private String poc;
	@NotBlank(message="Please type at least one non-whitespace character")
	@NotNull(message="Please type the email domain")
	@NotEmpty(message="Please type the email domain")
	@Pattern(regexp="^[a-zA-Z0-9.-]*.[a-zA-Z0-9-]*$", message="Please type a proper email domain")
	private String domain;

}

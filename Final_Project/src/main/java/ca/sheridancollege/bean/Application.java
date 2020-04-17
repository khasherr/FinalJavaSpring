package ca.sheridancollege.bean;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class Application {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	private String title;
	private String name;
	private String qualification;
	private String introduction;
	private String appliedDate;
	private String email;
	//Values: "Accepted", "Rejected", "Not Decided"
	private String state;
	
	private int researchID;
}

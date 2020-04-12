package ca.sheridancollege.bean;

import java.util.List;

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
public class Researchers {
	

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long researchId; 
	private String researchName; 

}

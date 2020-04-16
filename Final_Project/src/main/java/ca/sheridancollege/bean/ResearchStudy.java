package ca.sheridancollege.bean;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import ca.sheridancollege.bean.Researchers.ResearchersBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class ResearchStudy {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long researchStudyId; 
	private String researchDuration;
	private String researchArea; 
	
	@ManyToMany(cascade= {CascadeType.DETACH, CascadeType.REFRESH, CascadeType.PERSIST},
			fetch=FetchType.LAZY) 
	@JoinTable(name="reachers_study", 
	joinColumns = @JoinColumn(name ="researchers_id"), 
	inverseJoinColumns=@JoinColumn(name="researchStudy_id")
	)
	private List <Researchers> researchers;
	

}

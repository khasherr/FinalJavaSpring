package ca.sheridancollege.bean;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import ca.sheridancollege.bean.Researcher.ResearcherBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name="research_study")
public class ResearchStudy {
	@Id
	//@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "generator_name")
	//@SequenceGenerator(name="generator_name", sequenceName = "research_study_seq", allocationSize=1)
	@GeneratedValue(strategy = GenerationType.IDENTITY) //GenerationType.AUTO
	
	private Integer researchStudyId; 
	private String researchTitle;
	private String researchArea; 
	private String researchInstitution;
	private String researchDuration;
	//A name of the researcher for a research
	private String postedBy;
	private String postedDate;
	private String researchDetail;
	private int numParticipants;
	//A username of the account that posted the research
	private String username;
	
	
	@ManyToMany(cascade= {CascadeType.DETACH, CascadeType.REFRESH, CascadeType.PERSIST},
			fetch=FetchType.LAZY) 
	@JoinTable(name="researchers_study", 
	joinColumns = @JoinColumn(name ="researchers_id"), 
	inverseJoinColumns=@JoinColumn(name="researchStudy_id")
	)
	private List<Researcher> researchers;
	
	
	@ManyToMany(cascade= {CascadeType.DETACH, CascadeType.REFRESH, CascadeType.PERSIST},
            fetch=FetchType.LAZY)
	private List<Application> applications;
	

}

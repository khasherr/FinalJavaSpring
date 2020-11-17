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
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.springframework.beans.factory.annotation.Autowired;

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
	@NotNull(message="Please type the Research Title")
	@NotEmpty(message="Please type the Research Title")
	private String researchTitle;
	@NotNull(message="Please type at least one Research Area")
	@NotEmpty(message="Please type at least one Research Area")
	private String researchArea; 
	@NotNull(message="Please type your Research Institution")
	@NotEmpty(message="Please type your Research Institution")
	private String researchInstitution;
	@NotNull(message="Please type the duration in any format")
	@NotEmpty(message="Please type the duration in any format")
	private String researchDuration;
	//A name of the researcher for a research
	@NotNull(message="Please type the Researcher Name")
	@NotEmpty(message="Please type the Researcher Name")
	@Pattern(regexp="^[a-zA-Z0-9,;- ]*$", message="Please type the name of the researcher")
	private String postedBy;
	private String postedDate;
	private String researchDetail;
	private int numParticipants;
	//A username of the account that posted the research
	private String username;
	private int applyCount;
	
	/*
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
	*/
	

}

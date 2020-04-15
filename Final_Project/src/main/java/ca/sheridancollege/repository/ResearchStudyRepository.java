package ca.sheridancollege.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import ca.sheridancollege.bean.ResearchStudy;

public interface ResearchStudyRepository extends CrudRepository<ResearchStudy, Integer>{
	
	List<ResearchStudy> findByResearchTitleContaining(String researchTitle);
	List<ResearchStudy> findByResearchAreaContaining(String researchArea);
	List<ResearchStudy> findByResearchDetailContaining(String researchDetail);
	List<ResearchStudy> findByResearchDurationContaining(String researchDuration);
	List<ResearchStudy> findByResearchInstitutionContaining(String researchInstitution);
	List<ResearchStudy> findByPostedByContaining(String postedBy);
	List<ResearchStudy> findByPostedDateContaining(String postedDate);
	List<ResearchStudy> findByNumParticipantsLessThanEqual(int max);
	List<ResearchStudy> findByNumParticipantsGreaterThanEqual(int min);
	
	
}

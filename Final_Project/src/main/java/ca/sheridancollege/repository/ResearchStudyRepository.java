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
	List<ResearchStudy> findByUsername(String username);
	
	List<ResearchStudy> findByResearchTitleContainingAndUsername(String researchTitle, String username);
	List<ResearchStudy> findByResearchAreaContainingAndUsername(String researchArea, String username);
	List<ResearchStudy> findByResearchDetailContainingAndUsername(String researchDetail, String username);
	List<ResearchStudy> findByResearchDurationContainingAndUsername(String researchDuration, String username);
	List<ResearchStudy> findByResearchInstitutionContainingAndUsername(String researchInstitution, String username);
	List<ResearchStudy> findByPostedByContainingAndUsername(String postedBy, String username);
	List<ResearchStudy> findByPostedDateContainingAndUsername(String postedDate, String username);
	List<ResearchStudy> findByNumParticipantsLessThanEqualAndUsername(int max, String username);
	List<ResearchStudy> findByNumParticipantsGreaterThanEqualAndUsername(int min, String username);
	
	
}

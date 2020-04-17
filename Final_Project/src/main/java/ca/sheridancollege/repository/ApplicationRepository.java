package ca.sheridancollege.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import ca.sheridancollege.bean.Application;

public interface ApplicationRepository extends CrudRepository<Application, Integer> {
	
	List<Application> findByResearchIDAndState(int researchID, String state);
	Application findById(int id);
}

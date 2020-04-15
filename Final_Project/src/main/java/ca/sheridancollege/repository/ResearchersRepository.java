package ca.sheridancollege.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import ca.sheridancollege.bean.Researcher;


public interface ResearchersRepository extends CrudRepository<Researcher, Integer> {

	@Override 
	List<Researcher> findAll(); 
}

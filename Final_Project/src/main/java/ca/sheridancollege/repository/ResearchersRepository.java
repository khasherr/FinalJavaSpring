package ca.sheridancollege.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import ca.sheridancollege.bean.Researchers;


public interface ResearchersRepository extends CrudRepository<Researchers, Long> {

	@Override 
	List<Researchers> findAll(); 
}

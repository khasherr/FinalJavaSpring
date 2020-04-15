package ca.sheridancollege.repository;

import org.springframework.data.repository.CrudRepository;

import ca.sheridancollege.bean.Application;

public interface ApplicationRepository extends CrudRepository<Application, Integer> {

}

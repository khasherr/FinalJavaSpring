package ca.sheridancollege.repository;

import org.springframework.data.repository.CrudRepository;

import ca.sheridancollege.bean.UserAccount;



public interface UserAccountRepository extends CrudRepository<UserAccount, Long> {

	UserAccount findByUsername(String username);

}

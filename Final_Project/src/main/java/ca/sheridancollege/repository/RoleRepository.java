package ca.sheridancollege.repository;

import org.springframework.data.repository.CrudRepository;

import ca.sheridancollege.bean.Role;

public interface RoleRepository extends CrudRepository<Role, Long> {
	
	public Role findByRolename(String rolename);
}

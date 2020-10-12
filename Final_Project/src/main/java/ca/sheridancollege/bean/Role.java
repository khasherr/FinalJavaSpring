package ca.sheridancollege.bean;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import lombok.*;

@NoArgsConstructor
@Data
@Entity
public class Role {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	private String rolename;
	
	public Role(String rolename, Long id) {
		this.rolename = rolename;
		this.id = id;
	}
	
	@ManyToMany(cascade=CascadeType.ALL, mappedBy="roles")
	List<UserAccount> users = new ArrayList<UserAccount>();
}

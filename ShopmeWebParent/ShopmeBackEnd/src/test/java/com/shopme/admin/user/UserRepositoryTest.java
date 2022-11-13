package com.shopme.admin.user;

import static org.assertj.core.api.Assertions.assertThat;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import com.shopme.common.entity.Role;
import com.shopme.common.entity.User;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
public class UserRepositoryTest {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private EntityManager entityManager;
	
	@Test
	public void testCreateNewUserWithOneRole() {
		
		Role roleAdmin = entityManager.find(Role.class, 1);		// 1: for admin user
		User userYakup = new User("yakup@gmail.com", "1990", "Yakup", "Baykara");
		userYakup.addRole(roleAdmin);
		
		User savedUser = userRepository.save(userYakup);
		
		assertThat(savedUser.getId()).isGreaterThan(0);
	}
	
	@Test
	public void testCreateNewUserWithTwoRoles() {
		
		Role roleEditor = new Role(3);
		Role roleAssistant = new Role(5);
		User userYusuf = new User("yusuf@gmail.com", "2020", "Yusuf", "Baykara");
		userYusuf.addRole(roleAssistant);
		userYusuf.addRole(roleEditor);
		
		User savedUser = userRepository.save(userYusuf);
		
		assertThat(savedUser.getId()).isGreaterThan(0);
	}
	
	@Test
	public void testListAllUsers() {
		Iterable<User> listUsers = userRepository.findAll();
		
		listUsers.forEach(user -> System.out.println(user));
	}
	
	@Test
	public void testGetUserById() {
		
		User user = userRepository.findById(1).get();
		System.out.println(user);
		
		assertThat(user).isNotNull();
	}
	
	@Test
	public void testUpdateUserDetails() {
		
		User user = userRepository.findById(1).get();
		user.setEnabled(true);
		user.setEmail("yakup.baykara@gmail.com");
		
		userRepository.save(user);
	}
	
	@Test
	public void testUpdateUserRoles() {
		
		User user = userRepository.findById(2).get();
		Role roleEditor = new Role(3);
		Role roleSalesperson = new Role(2);
		
		user.getRoles().remove(roleEditor);
		user.addRole(roleSalesperson);
		
		userRepository.save(user);
	}
	
	@Test
	public void testDeleteUser() {
		
		Integer id = 2;
		userRepository.deleteById(id);
	}
}
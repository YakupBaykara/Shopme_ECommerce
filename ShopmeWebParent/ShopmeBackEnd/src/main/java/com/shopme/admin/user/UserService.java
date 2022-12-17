package com.shopme.admin.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.shopme.common.entity.Role;
import com.shopme.common.entity.User;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private RoleRepository	roleRepository;
	
	public List<User> listUsers() {
		return (List<User>) userRepository.findAll();
	}
	
	public List<Role> listRoles() {
		return (List<Role>) roleRepository.findAll();
	}

	public void save(User user) {

		boolean isUpdatingUser = (user.getId() != null);
		if (isUpdatingUser) {
			User existUser = userRepository.findById(user.getId()).get();
			if (user.getPassword().isEmpty()) {
				user.setPassword(existUser.getPassword());
			} else {
				encodePassword(user);
			}
		} else {
			encodePassword(user);
		}
		userRepository.save(user);
	}

	private void encodePassword(User user) {
		String encodedPassword = passwordEncoder.encode(user.getPassword());
		user.setPassword(encodedPassword);
	}
 	public boolean isEmailUnique(Integer id, String email) {

		User user = userRepository.findUserByEmail(email);
		if (user == null) return true;

		boolean isCreatingNew = (id == null);
		if (isCreatingNew) {
			if (user != null) return false;
		} else {
			if (user.getId() != id) return false;
		}

		return true;
	}
	public User getUserById(Integer id) throws UserNotFoundException {
		return userRepository.findById(id)
				.orElseThrow(() -> new UserNotFoundException("Couldn't find any user with ID : " +id));
	}
}

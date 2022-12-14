package com.shopme.admin.user.controller;

import java.io.IOException;
import java.util.List;

import com.shopme.admin.FileUploadUtil;
import com.shopme.admin.user.UserNotFoundException;
import com.shopme.admin.user.UserService;
import com.shopme.admin.user.export.UserCsvExporter;
import com.shopme.admin.user.export.UserExcelExporter;
import com.shopme.admin.user.export.UserPdfExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.shopme.common.entity.Role;
import com.shopme.common.entity.User;

import javax.servlet.http.HttpServletResponse;

@Controller
public class UserController {

	@Autowired
	private UserService userService;
	
	@GetMapping("/users")
	public String listFirstPage(Model model) {
		return listByPage(1, model, "id", "asc", null);
	}

	@GetMapping("/users/page/{pageNum}")
	public String listByPage(
			@PathVariable(name = "pageNum") int pageNum,
							 Model model,
							 @Param("sortField") String sortField,
							 @Param("sortDir") String sortDir,
							 @Param("keyword") String keyword
			) {
		System.out.println("Sort Field: " + sortField);
		System.out.println("Sort Order: " + sortDir);

		Page<User> page = userService.listByPage(pageNum, sortField, sortDir, keyword);
		List<User> listUsers = page.getContent();

		long startCount = (pageNum - 1) * UserService.USERS_PER_PAGE + 1;
		long endCount = startCount + UserService.USERS_PER_PAGE - 1;

		if (endCount > page.getTotalElements()) {
			endCount = page.getTotalElements();
		}

		String reverseSortDir = sortDir.equals("asc") ? "desc" : "asc";

		model.addAttribute("currentPage", pageNum);
		model.addAttribute("totalPages", page.getTotalPages());
		model.addAttribute("startCount", startCount);
		model.addAttribute("endCount", endCount);
		model.addAttribute("totalItems", page.getTotalElements());
		model.addAttribute("listUsers", listUsers);
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSortDir", reverseSortDir);
		model.addAttribute("keyword", keyword);

		return "users/users";
	}
	
	@GetMapping("/users/new")
	public String newUser(Model model) {
		
		User user = new User();
		user.setEnabled(true);
		
		List<Role> listRoles = userService.listRoles();
		
		model.addAttribute("user", user);
		model.addAttribute("listRoles" ,listRoles);
		model.addAttribute("pageTitle", "Create New User");
		
		return "users/user_form";
	}
	
	@PostMapping("/users/save")
	public String saveUser(User user, RedirectAttributes redirectAttributes,
						   @RequestParam("image") MultipartFile multipartFile) throws IOException {

		if (!multipartFile.isEmpty()) {
			String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
			user.setPhotos(fileName);
			User savedUser = userService.save(user);

			String uploadDir = "user-photos/" + savedUser.getId();

			FileUploadUtil.cleanDir(uploadDir);
			FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);

		} else {
			if (user.getPhotos().isEmpty()) user.setPhotos(null);
			userService.save(user);
		}

		redirectAttributes.addFlashAttribute("message", "The user has been saved successfully.");

		return "redirect:/users";
	}

	@GetMapping("/users/update/{id}")
	public String updateUser(Integer id, Model model, RedirectAttributes redirectAttributes) {
		try {
			User user = userService.getUserById(id);
			List<Role> listRoles = userService.listRoles();

			model.addAttribute("user", user);
			model.addAttribute("pageTitle", "UpdateUser (ID: " +id+ ")");
			model.addAttribute("listRoles" ,listRoles);
			return "users/user_form";

		} catch (UserNotFoundException ex) {
			redirectAttributes.addFlashAttribute("message", ex.getMessage());
			return "redirect:/users";
		}
	}
	@GetMapping("/users/delete/{id}")
	public String deleteUser(Integer id, Model model, RedirectAttributes redirectAttributes) {
		try {
			userService.deleteUser(id);
			redirectAttributes.addFlashAttribute("message ",
					"The user ID : " +id+ " has been deleted successfully");
		} catch (UserNotFoundException ex) {
			redirectAttributes.addFlashAttribute("message ", ex.getMessage());
		}
		return "redirect:/users";
	}
	@GetMapping("/users/{id}/enabled/{status}")
	public String updateUserEnabledStatus(@PathVariable("id") Integer id,
										  @PathVariable("status") boolean enabled,
										  RedirectAttributes redirectAttributes) {
		userService.updateUserEnabledStatus(id, enabled);
		String status = enabled ? "enabled" : "disabled";
		String message = "The user ID " + id + " has been " + status;
		redirectAttributes.addFlashAttribute("message", message);

		return "redirect:/users";
	}

	@GetMapping("/users/export/csv")
	public void exportToCSV(HttpServletResponse response) throws IOException {
		List<User> listUsers = userService.listAll();
		UserCsvExporter exporter = new UserCsvExporter();
		exporter.export(listUsers, response);
	}

	@GetMapping("/users/export/excel")
	public void exportToExcel(HttpServletResponse response) throws IOException {
		List<User> listUsers = userService.listAll();

		UserExcelExporter exporter = new UserExcelExporter();
		exporter.export(listUsers, response);
	}

	@GetMapping("/users/export/pdf")
	public void exportToPDF(HttpServletResponse response) throws IOException {
		List<User> listUsers = userService.listAll();

		UserPdfExporter exporter = new UserPdfExporter();
		exporter.export(listUsers, response);
	}
}

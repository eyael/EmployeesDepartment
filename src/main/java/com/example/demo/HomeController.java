package com.example.demo;

import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.security.Principal;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private UserService userService;


    @Autowired
    DepartmentRepository departmentRepository;

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    CloudinaryConfig cloudc;

    @GetMapping("/register")
    public String showRegistrationPage(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("employee",new Employee());
        return "registration";
    }
    @PostMapping ("/register")
    public String processRegistrationPage (@Valid
                                           @ModelAttribute("user") User user, BindingResult result,Model model){
        model.addAttribute("user",user);
        if (result.hasErrors())
        {
            return "registration";
        }
        else{
            userService.saveUser(user);
            model.addAttribute("message", "User Account Created");
        }
        return "login";
    }
    @RequestMapping("/index")
    public String secure(Principal principal, Model model){
        User myuser = ((CustomUserDetails)((UsernamePasswordAuthenticationToken) principal)
                .getPrincipal()).getUser();
        model.addAttribute("myuser",myuser);
        return "index";

    }
    @RequestMapping("/login")
    public String login() {
        return "login";
    }

    @RequestMapping("/")
    public String listEmployees(Model model){
        model.addAttribute("employees", employeeRepository.findAll());
        model.addAttribute("departments", departmentRepository.findAll());
        return "list";
    }

    @GetMapping("/add")
    public String employeeForm(Model model){
        model.addAttribute("employee", new Employee());
        model.addAttribute("departments", departmentRepository.findAll());
        return "employeeform";
    }

    @PostMapping("/process")
    public String processForm(@Valid
                                  @ModelAttribute Employee employee, BindingResult result,@RequestParam("file") MultipartFile file ) {

        if (file.isEmpty()) {
            return "redirect:/add";
        }
        if (result.hasErrors()){
            return "employeeform";
        }
        employeeRepository.save(employee);
        try {
            Map uploadResult = cloudc.upload(file.getBytes(),
                    ObjectUtils.asMap("resourcetype", "auto"));
            employee.setHeadshot(uploadResult.get("url").toString());
            employeeRepository.save(employee);
        }catch (IOException e) {
            e.printStackTrace();
            return "redirect:/add";
        }
        return "redirect:/";
    }
    @GetMapping("/adddepartment")
    public String departmentForm(Model model) {
        model.addAttribute("department", new Department());
        return "department";
    }
    @PostMapping("/processdepartment")
    public String processDepartment(@Valid Department department, BindingResult result,
                                 Model model){
        if(result.hasErrors()){
            return "department";
        }
        departmentRepository.save(department);
        return "redirect:/";
    }
    @RequestMapping("/detail/{id}")
    public String showEmployee(@PathVariable("id") long id, Model model){
        model.addAttribute("employee", employeeRepository.findById(id).get());
        return "show";
    }

    @RequestMapping("/update/{id}")
    public String updateEmployee(@PathVariable("id") long id, Model model){
        model.addAttribute("Departments", departmentRepository.findAll());
        model.addAttribute("employee", employeeRepository.findById(id).get());
        return "employeeform";
    }

    @RequestMapping("/delete/{id}")
    public String delEmployee(@PathVariable("id") long id){
        employeeRepository.deleteById(id);
        return "redirect:/";
    }
}

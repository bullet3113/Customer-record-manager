package com.sunbase.CRMApplication.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sunbase.CRMApplication.Entity.Customer;
import com.sunbase.CRMApplication.Entity.User;
import com.sunbase.CRMApplication.Repository.CustomerRepository;
import com.sunbase.CRMApplication.Service.HomeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
public class HomeController {

    @Autowired
    HomeService homeService;

    @Autowired
    CustomerRepository customerRepository;

    // Home route
    @RequestMapping("/")
    public String Home() {
        return "home";
    }

    // login form route
    @RequestMapping("/login")
    public String loginpage(Model model) {
        model.addAttribute("user", new User());
        return "loginpage";
    }

    // login form post handler
    @RequestMapping(value = "/dologin", method = RequestMethod.POST, consumes = "application/x-www-form-urlencoded", produces = "application/text")
    public String loginhandler(User user, Model model, HttpSession session) throws JsonProcessingException {

        if(homeService.userLoginHandler(user, session)) {
            return "redirect:/show-customers/0";
        } else {
            model.addAttribute("user", user);
        }
        return "unauthorize";
    }

    // Synchronizing data with External API
    @RequestMapping("/sync-customers")
    public String syncHandler(HttpSession session) throws JsonProcessingException {
        if(session.getAttribute("token") == null) return "unauthorize";
        String access_token = session.getAttribute("token").toString();
        boolean result = homeService.syncData(access_token);
        System.out.println(result);
        return "redirect:/show-customers/0";
    }


    // Add customer route
    @GetMapping("/add-customer")
    public String addCustomer(Model model, HttpSession session) {
        if(session.getAttribute("token") == null) return "unauthorize";
        model.addAttribute("customer", new Customer());
        return "add-customer";
    }

    // Add customer DAO handler (post request)
    @PostMapping("/new-customer")
    public String createNewCustomer(@ModelAttribute Customer customer, Model model, HttpSession session) {
        if(session.getAttribute("token") == null) return "unauthorize";
        if(homeService.handleNewCustomer(customer)) {
            return "redirect:/show-customers/0";
        }

        model.addAttribute("customer", customer);
        return "add-customer";
    }

    // Edit customer route
    @GetMapping("/edit-customer/{customerId}")
    public String openEditCustomer(@PathVariable("customerId") String customerId, Model model, HttpSession session) {
        if(session.getAttribute("token") == null) return "unauthorize";
        Customer customer = customerRepository.findById(customerId).orElse(null);
        model.addAttribute("customer", customer);

        return "edit-customer";
    }

    // Edit customer DAO handler
    @PostMapping("/exist-customer")
    public String editCustomer(@ModelAttribute Customer customer, Model model, HttpSession session, Principal principal) {
        if(session.getAttribute("token") == null) return "unauthorize";
        try {
            System.out.println(customer.getEmail());
            Customer currCustomer = customerRepository.findById(customer.getEmail()).orElse(null);
            currCustomer.setFirst_name(customer.getFirst_name());
            currCustomer.setLast_name(customer.getLast_name());
            currCustomer.setStreet(customer.getStreet());
            currCustomer.setAddress(customer.getAddress());
            currCustomer.setEmail(customer.getEmail());
            currCustomer.setPhone(customer.getPhone());
            currCustomer.setCity(customer.getCity());
            currCustomer.setState(customer.getState());

            customerRepository.save(currCustomer);
        } catch (Exception e) {
            System.out.println(e);
        }

        return "redirect:/show-customers/0";
    }

    // Delete customer route (no additional handler needed)
    @GetMapping("/delete-customer/{customerId}")
    public String deleteCustomer(@PathVariable("customerId") String customerId, HttpSession session) {
        if(session.getAttribute("token") == null) return "unauthorize";
        Customer customer = customerRepository.findById(customerId).orElse(null);

        customerRepository.delete(customer);
        return "redirect:/show-customers/0";
    }


    // Getting total list of customers from database (pagination supported)
    @GetMapping("/show-customers/{page}")
    public String showCustomers(@PathVariable("page") int page, Model model, Principal principal,@RequestParam(required = false) String searchby,@RequestParam(required = false) String query, HttpSession session) {
        // creating pageable object
        if(session.getAttribute("token") == null) return "unauthorize";

        Pageable pageable = PageRequest.of(page, 5);
        Page<Customer> customerPage = null;
        if(searchby == null) {
            customerPage = customerRepository.findAll(pageable);
        } else if(searchby.equals("first_name")) {
            customerPage = customerRepository.findCustomerByFirstName(query, pageable);
        } else if(searchby.equals("city")) {
            customerPage = customerRepository.findByCity(query, pageable);
        } else if(searchby.equals("phone")) {
            customerPage = customerRepository.findByPhone(query, pageable);
        } else if(searchby.equals("email")) {
            customerPage = customerRepository.findByEmail(query, pageable);
        }

        if(customerPage == null) System.out.println(true);

        for(Customer c: customerPage) {
            System.out.println(c.getFirst_name());
        }

        System.out.println(customerPage.getSize());

        model.addAttribute("customers", customerPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", customerPage.getTotalPages());

        return "show-customers";
    }

    // Logout route (for invalidating session)
    @RequestMapping("/logout")
    public String logoutHandler(Model model, HttpSession session) {
        session.invalidate();
        model.addAttribute("user", new User());
        return "loginpage";
    }

}

package com.sunbase.CRMApplication.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunbase.CRMApplication.Entity.Customer;
import com.sunbase.CRMApplication.Entity.User;
import com.sunbase.CRMApplication.Repository.CustomerRepository;
import com.sunbase.CRMApplication.Repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class HomeService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    CustomerRepository customerRepository;

    // Login handler (requsting JWT token from API, and storing in session)
    public boolean userLoginHandler(User user, HttpSession session) throws JsonProcessingException {
        if (user != null) {

            // request url
            String url = "https://qa.sunbasedata.com/sunbase/portal/api/assignment_auth.jsp";

            // create an instance of RestTemplate
            RestTemplate restTemplate = new RestTemplate();

            // request body parameters
            Map<String, String> map = new HashMap<>();
            map.put("login_id", user.getLogin_id());
            map.put("password", user.getPassword());

            // send POST request
            ResponseEntity<String> response = restTemplate.postForEntity(url, map, String.class);

            // check response
            if (response.getStatusCode() == HttpStatus.OK) {
                ObjectMapper objectMapper = new ObjectMapper();
                User temp = objectMapper.readValue(response.getBody(), User.class);
                user.setAccess_token(temp.getAccess_token());
                session.setAttribute("token", temp.getAccess_token());
                return true;
            } else {
                return false;
            }
        }

        return false;
    }

    // Synchronizing API data with local database (only unique data)
    public boolean syncData(String token) throws JsonProcessingException {
        System.out.println(token);
            // request url
            String url = "https://qa.sunbasedata.com/sunbase/portal/api/assignment.jsp?cmd=get_customer_list";

            // create an instance of RestTemplate
            RestTemplate restTemplate = new RestTemplate();

            // create headers
            HttpHeaders headers = new HttpHeaders();

            // custom header
            headers.set("X-Request-Source", "Desktop");
            headers.set("Authorization", "Bearer " + token);

            // build the request
            HttpEntity request = new HttpEntity(headers);

            // make an HTTP GET request with headers
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    String.class,
                    1
            );

            // check response
            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println(response);
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                Customer[] customers = objectMapper.readValue(response.getBody(), Customer[].class);
                for(Customer customer: customers) {
                    if(customerRepository.findById(customer.getEmail()).orElse(null) == null)
                        customerRepository.save(customer);
                }
            } else {
                System.out.println(response.getStatusCode());
            }
            return true;

    }

    // adding new customer (if not exist)
    public boolean handleNewCustomer(Customer customer) {
        Customer temp = customerRepository.findById(customer.getEmail()).orElse(null);
        if(temp != null) return false;
        customerRepository.save(customer);

        return true;
    }
}

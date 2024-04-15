package com.codewitharjun.fullstackbackend.controller;

import com.codewitharjun.fullstackbackend.exception.UserNotFoundException;
import com.codewitharjun.fullstackbackend.model.User;
import com.codewitharjun.fullstackbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Random;

@RestController
@CrossOrigin("*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender javaMailSender;

    @PostMapping("/user")
    User newUser(@RequestBody User newUser) {
        String otp=generateOTP();
        newUser.setOtp(otp);
        User savedUser = userRepository.save(newUser);
        sendRegistrationEmail(newUser.getEmail(),otp);
        return savedUser;
    }

    private void sendRegistrationEmail(String email,String Otp) {
        String subject = "Registration OTP";
        String otp = Otp;
        String text = "Your OTP for registration is: " + otp;
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(text);

            javaMailSender.send(message);
        } catch (MessagingException | MailException e) {
            // Handle exception
            e.printStackTrace(); // Or log the error
        }
    }

    private String generateOTP() {
       /* int otp = 198762; // Generates a six-digit number
        return String.valueOf(otp);*/

        String allChars = "1234567890";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        while (sb.length() < 6) // length of the random string.
        {
            int index = (int) (random.nextFloat() * allChars.length());
            sb.append(allChars.charAt(index));
        }

        return sb.toString();
    }
    @RequestMapping(value = "/verify")
    public Object submitConfirmationCode(@RequestBody User userForm)
    {
        User userInstance = userRepository.findByEmail(userForm.getEmail());
        if (userInstance == null || !(userInstance.getOtp().matches(userForm.getOtp())))
        {
            System.out.println("Incorrect Verification Code");
            System.out.println(userInstance);
            return "error";
        }

        if (!(userInstance == null || !(userInstance.getOtp().matches(userForm.getOtp()))))
        {
            userRepository.save(userInstance);
            System.out.println("Account is now active");
            return userForm;
        }
        return "error" ;
    }
    @GetMapping("/users")
    List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/user/{id}")
    User getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @PutMapping("/user/{id}")
    User updateUser(@RequestBody User newUser, @PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setFirstname(newUser.getFirstname());
                    user.setLastname(newUser.getLastname());
                    user.setEmail(newUser.getEmail());
                    user.setPhone(newUser.getPhone());
                    user.setPassword(newUser.getPassword());
                    return userRepository.save(user);
                }).orElseThrow(() -> new UserNotFoundException(id));
    }

    @DeleteMapping("/user/{id}")
    String deleteUser(@PathVariable Long id){
        if(!userRepository.existsById(id)){
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
        return  "User with id "+id+" has been deleted success.";
    }

    @PostMapping("/login")
    User loginUser(@RequestBody User loginUser) {
        String email = loginUser.getEmail();
        String password = loginUser.getPassword();

        User user = userRepository.findByEmailAndPassword(email, password);
        if (user == null) {
            throw new UserNotFoundException("Invalid email or password.");
        }
        return user;
    }
}

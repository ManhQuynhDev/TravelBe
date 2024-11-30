package com.quynhlm.dev.be.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.quynhlm.dev.be.model.entity.User;
import com.quynhlm.dev.be.service.GroupService;
import com.quynhlm.dev.be.service.PostService;
import com.quynhlm.dev.be.service.UserService;

@Controller
@RequestMapping("/web-server")
public class HomeController {

    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    @Autowired
    private GroupService groupService;


    @GetMapping("/")
    public String home(Model model) {
        List<User> userList = userService.getAllListUser();

        long userCount = userList.stream()
                .filter(user -> user.getRoles().contains("USER"))
                .count();
        double percentageChange = ((double) (userCount - 9) / 9) * 100;
        String formattedPercentage = String.format("%.1f", percentageChange);
        // model.addAttribute("body", "home"); // Gửi fragment home
        model.addAttribute("userCount", userCount);
        model.addAttribute("formattedPercentage", formattedPercentage);
        return "home"; // Trả về template index.html
    }

    @GetMapping("/users")
    public String user(Model model) {
        List<User> userList = userService.getAllListUser();

        long userCount = userList.stream()
                .filter(user -> user.getRoles().contains("USER"))
                .count();
        double percentageChange = ((double) (userCount - 9) / 9) * 100;
        String formattedPercentage = String.format("%.1f", percentageChange);
        model.addAttribute("userList", userService.getListData(0, 1000));
        model.addAttribute("userCount", userCount);
        model.addAttribute("formattedPercentage", formattedPercentage);
        return "users"; // Trả về template index.html
    }

    @GetMapping("/posts")
    public String post(Model model) {
        // model.addAttribute("body", "posts");
        Pageable pageable = PageRequest.of(0, 1000);
        model.addAttribute("postList", postService.getAllPost(pageable));
        return "posts";
    }

    @GetMapping("/groups")
    public String group(Model model) {
        model.addAttribute("groupList", groupService.getAllGroup(0, 1000));
        return "groups";
    }

    @GetMapping("/register")
    public String regiter(Model model) {
        return "register";
    }

    @GetMapping("/login")
    public String login(Model model) {
        return "login";
    }

}

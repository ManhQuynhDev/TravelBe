package com.quynhlm.dev.be.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.quynhlm.dev.be.model.dto.responseDTO.GroupResponseDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.PostMediaDTO;
import com.quynhlm.dev.be.model.entity.Comment;
import com.quynhlm.dev.be.model.entity.Post;
import com.quynhlm.dev.be.model.entity.User;
import com.quynhlm.dev.be.service.ActivitiesService;
import com.quynhlm.dev.be.service.CommentService;
import com.quynhlm.dev.be.service.GroupService;
import com.quynhlm.dev.be.service.PostService;
import com.quynhlm.dev.be.service.ReportService;
import com.quynhlm.dev.be.service.ReviewService;
import com.quynhlm.dev.be.service.StoryService;
import com.quynhlm.dev.be.service.TravelPlanService;
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

    @Autowired
    private CommentService commentService;

    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private ReportService reportService;

    @Autowired
    private TravelPlanService travelPlanService;

    @Autowired
    private ActivitiesService activitiesService;

    @Autowired
    private StoryService storyService;

    @GetMapping("/")
    public String home(Model model) {
        List<User> userList = userService.getAllListUser();
        Page<GroupResponseDTO> groupResponseDTOPage = groupService.getAllGroup(0, 1000);
        long groupCount = groupResponseDTOPage.getTotalElements();
        Pageable pageable = PageRequest.of(0, 1000);
        Page<PostMediaDTO> postMediaDTOPage = postService.getAllPost(pageable);
        long postCount = postMediaDTOPage.getTotalElements();

        long userCount = userList.stream()
                .filter(user -> user.getRoles().contains("USER"))
                .count();
        long managerCount = userList.stream()
                .filter(user -> user.getRoles().contains("MANAGER"))
                .count();
        model.addAttribute("userCount", userCount);
        model.addAttribute("groupCount", groupCount);
        model.addAttribute("postCount", postCount);
        model.addAttribute("managerCount", managerCount);
        return "home";
    }

    @GetMapping("/manager")
    public String getContentPage(Model model) {
        Page<User> managers = userService.getAllListManager(0, 1000);
        model.addAttribute("managers", managers);
        return "manager";
    }

    @GetMapping("/message-group")
    public String getMessagePage(Model model) {
        Page<User> managers = userService.getAllListManager(0, 1000);
        model.addAttribute("managers", managers);
        return "messageGroup";
    }

    @GetMapping("/users")
    public String user(Model model) {
        List<User> userList = userService.getAllListUser();

        long userCount = userList.stream()
                .filter(user -> user.getRoles().contains("USER"))
                .count();
        double percentageChange = ((double) (userCount - 9) / 9) * 100;
        String formattedPercentage = String.format("%.1f", percentageChange);
        model.addAttribute("userList", userService.getAllListUser());
        model.addAttribute("userCount", userCount);
        model.addAttribute("formattedPercentage", formattedPercentage);
        return "users";
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

    @GetMapping("/travel-plan")
    public String travelPlan(Model model) {
        model.addAttribute("listTravelPlans", travelPlanService.getAllPlans(0, 1000));
        return "travelplan";
    }

    @GetMapping("/activity")
    public String activity(Model model) {
        model.addAttribute("activityList", activitiesService.getListData(0, 1000));
        return "activity";
    }

    @GetMapping("/stories")
    public String stories(Model model) {
        model.addAttribute("storyList", storyService.getAllStory(0, 1000));
        return "stories";
    }

    @GetMapping("/comments")
    public String comments(Model model) {
        model.addAttribute("listComment", commentService.getListData(0, 1000));
        return "comments";
    }

    @GetMapping("/reviews")
    public String reviews(Model model) {
        model.addAttribute("listReview", reviewService.getListData(0, 1000));
        return "reviews";
    }

    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAttribute("listReports", reportService.getAllReport(0, 1000));
        return "reports";
    }

    @GetMapping("/login")
    public String login(Model model) {
        return "login";
    }

    @GetMapping("/edit-profile")
    public String editprofile(Model model) {
        return "editProfile";
    }

    @GetMapping("/profile")
    public String getMethodName(Model model) {
        return "profile";
    }

}

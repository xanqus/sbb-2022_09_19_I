package com.mysite.sbb.question.controller;

import com.mysite.sbb.answer.AnswerForm;
import com.mysite.sbb.question.QuestionForm;
import com.mysite.sbb.question.domain.Question;
import com.mysite.sbb.question.service.QuestionService;
import com.mysite.sbb.siteuser.domain.SiteUser;
import com.mysite.sbb.siteuser.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.security.Principal;

@RequestMapping("/question")
@Controller
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    private final UserService userService;

    @RequestMapping("/list")
    public String list(Model model, @RequestParam(value="page", defaultValue="0") int page, @RequestParam(value = "kw", defaultValue = "") String kw) {


        Page<Question> paging = this.questionService.getList(page, kw);
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
//        List<Question> questionList = questionService.getList();
//        model.addAttribute("questionList", questionList);
        return "question_list";
    }

    @RequestMapping("/detail/{id}")
    public String detail(Model model, @PathVariable Integer id, AnswerForm answerForm, @AuthenticationPrincipal SiteUser siteUser, Principal principal) {
        Question question = questionService.getQuestion(id);
        model.addAttribute("question", question);
        return "question_detail";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create")
    public String questionCreate(QuestionForm questionForm) {
        return "question_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String questionCreate(@Valid QuestionForm questionForm, BindingResult bindingResult, @AuthenticationPrincipal SiteUser siteUser){
        if(bindingResult.hasErrors()) {
            return "question_form";
        }

        questionService.create(questionForm.getSubject(), questionForm.getContent(), siteUser);

        return "redirect:/question/list";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String questionModify(QuestionForm questionForm, @PathVariable("id") Integer id, @AuthenticationPrincipal SiteUser siteUser) {
        System.out.println("authority: " + siteUser.getRole()!="ROLE_ADMIN");
        Question question = this.questionService.getQuestion(id);
        if((!question.getAuthor().getUsername().equals(siteUser.getUsername())) && !(siteUser.getRole().equals("ROLE_ADMIN"))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "??????????????? ????????????.");
        }
        questionForm.setSubject(question.getSubject());
        questionForm.setContent(question.getContent());
        return "question_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String questionModify(@Valid QuestionForm questionForm, BindingResult bindingResult,
                                @AuthenticationPrincipal SiteUser siteUser, @PathVariable("id") Integer id) {
        if (bindingResult.hasErrors()) {
            return "question_form";
        }
        Question question = this.questionService.getQuestion(id);
        if((!question.getAuthor().getUsername().equals(siteUser.getUsername())) && !(siteUser.getRole().equals("ROLE_ADMIN"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "??????????????? ????????????.");
        }
        this.questionService.modify(question, questionForm.getSubject(), questionForm.getContent());
        return String.format("redirect:/question/detail/%s", id);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String questionDelet(@AuthenticationPrincipal SiteUser siteUser, @PathVariable("id") Integer id) {
        Question question = this.questionService.getQuestion(id);
        if(!question.getAuthor().getUsername().equals(siteUser.getUsername()) && !(siteUser.getRole().equals("ROLE_ADMIN"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "??????????????? ????????????.");
        }
        questionService.delete(question);
        return "redirect:/";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    public String questionVote(@AuthenticationPrincipal SiteUser siteUser, @PathVariable("id") Integer id) {
        Question question = this.questionService.getQuestion(id);
        this.questionService.vote(question, siteUser);
        return String.format("redirect:/question/detail/%s", id);
    }

}

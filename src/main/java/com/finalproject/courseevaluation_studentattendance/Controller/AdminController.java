package com.finalproject.courseevaluation_studentattendance.Controller;

import com.finalproject.courseevaluation_studentattendance.Model.Course;
import com.finalproject.courseevaluation_studentattendance.Model.Evaluation;
import com.finalproject.courseevaluation_studentattendance.Model.Person;
import com.finalproject.courseevaluation_studentattendance.Repositories.CourseRepository;
import com.finalproject.courseevaluation_studentattendance.Repositories.EvaluationRepository;
import com.finalproject.courseevaluation_studentattendance.Repositories.PersonRepository;
import com.finalproject.courseevaluation_studentattendance.Services.AttendanceService;
import com.finalproject.courseevaluation_studentattendance.Services.CourseService;
import com.finalproject.courseevaluation_studentattendance.Services.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Set;


@Controller
@RequestMapping("/admin")
public class AdminController {
    //@Autowired
    //AttendanceService attendenceService;

    @Autowired
    PersonService personService;

    @Autowired
    CourseService courseService;

    @Autowired
    PersonRepository personRepo;

    @Autowired
    CourseRepository courseRepo;

    @Autowired
    EvaluationRepository evaluationRepo;





    @RequestMapping("/home")
    public String adminHome(Model model){


        model.addAttribute("allcourses", courseRepo.findAll());
        return "adminpages/adminhome";
    }

    @GetMapping("/addcourse")
    public String addCourse(Model model)
    {
        model.addAttribute("newcourse", new Course());
        return "adminpages/addcourse";
    }

    //End date for the course isn't going to be entered here but it will be set when the teacher says the course ended
    @PostMapping("/addcourse")
    public String postCourse(@ModelAttribute("newcourse")Course newcourse)
    {
        courseRepo.save(newcourse);

         return"redirect:/admin/addcourse";
    }
    @GetMapping("/updatecourse/{id}")
    public String editCourse(@PathVariable("id") long id, Model model)
    {
        model.addAttribute("newcourse",courseRepo.findOne(id));

        return "adminpages/addcourse";
    }

    @GetMapping("/admincourcedatails/{id}")  //id - course id
    public String displayCourse (@PathVariable("id")long id,
                                         Model model) {

        Course currentCourse = courseRepo.findOne(id);
        model.addAttribute("course", currentCourse);

        Set<Person> courseInstructors = currentCourse.getInstructor();
        model.addAttribute("courseInstructors", courseInstructors);


        Set<Person> courseStudents = currentCourse.getStudent();
        model.addAttribute("courseStudents", courseStudents);

        return "adminpages/admincourcedatails";
    }

    //need to taste viewing after the team is done with evaluation
    @GetMapping("viewcourseevaluations/{id}")
    public String viewEvaluation(@PathVariable("id") long id ,Model model){
        Course thiscourse=courseRepo.findOne(id);
        Iterable<Evaluation>thiscourseevaluation=thiscourse.getEvaluations();
        model.addAttribute("evaluation",thiscourseevaluation);
        model.addAttribute("course",thiscourse);
        return"courseevaluation";
    }
    //this will allow the the admin to add an existing student to a course
    @GetMapping("/addstudenttocourse/{id}")
    public String addStudent(@PathVariable("id") long crsID, Model model)
    {
        model.addAttribute("course",courseRepo.findOne(crsID));
        model.addAttribute("students",personRepo.findAllByPersonRoles("default"));

        return "courseaddstudent";
    }
    @PostMapping("/addstudenttocourse/{crsid}")
    public String studentSavedToCourse(@PathVariable("crsid") long id,
                                       @RequestParam("crs") String crsID,
                                       @ModelAttribute("aStudent") Person p,
                                       Model model)

    {
        Course ncourse=courseRepo.findOne(id);
        ncourse.addStudent(personRepo.findOne(new Long(crsID)));
        courseRepo.save(ncourse);
        return "redirect:/admin/detailsofacourse"+crsID;
    }

    // ===   Remove Student from the Course
    @GetMapping("course/{courseid}/removestudentfromcourse/{studentid}")
    public String removeStudentFromCourse (@PathVariable("courseid")long courseid,
                                           @PathVariable("studentid")long studentid,
                                           Model model)
    {
        Person student = personService.findById(studentid);
        Course course = courseRepo.findOne(courseid);
        courseService.removeStudentFromCourse(course, student);

        String courseIDString = Long.toString(courseid);
        return "rdirect:/admin/detailsofacourse/" + courseIDString;
    }


}

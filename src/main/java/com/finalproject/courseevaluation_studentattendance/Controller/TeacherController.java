package com.finalproject.courseevaluation_studentattendance.Controller;

import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.fasterxml.jackson.databind.util.ArrayIterator;
import com.finalproject.courseevaluation_studentattendance.Config.CloudinaryConfig;
import com.finalproject.courseevaluation_studentattendance.Model.*;
import com.finalproject.courseevaluation_studentattendance.Repositories.*;
import com.finalproject.courseevaluation_studentattendance.Services.CourseService;
import com.finalproject.courseevaluation_studentattendance.Services.EvaluationService;
import com.finalproject.courseevaluation_studentattendance.Services.PersonService;
import com.finalproject.courseevaluation_studentattendance.Services.RoleService;
import com.google.common.collect.Lists;
import it.ozimov.springboot.mail.model.Email;
import it.ozimov.springboot.mail.model.EmailAttachment;
import it.ozimov.springboot.mail.model.defaultimpl.DefaultEmail;
import it.ozimov.springboot.mail.model.defaultimpl.DefaultEmailAttachment;
import it.ozimov.springboot.mail.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import com.finalproject.courseevaluation_studentattendance.Model.Course;
import com.finalproject.courseevaluation_studentattendance.Model.Person;
import com.finalproject.courseevaluation_studentattendance.Repositories.PersonRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.internet.InternetAddress;
import javax.validation.Valid;
import javax.websocket.server.PathParam;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.Principal;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Date;
import java.util.Map;
import java.util.logging.SimpleFormatter;


import javax.websocket.server.PathParam;

@Controller
@RequestMapping("/teacher")
public class TeacherController {


    @Autowired
    PersonRepository personRepository;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    PersonRepository personRepo;
    @Autowired
    RoleService roleService;
    @Autowired
    PersonService personService;

    @Autowired
    CourseService courseService;

    @Autowired
    AttendanceRepository attendanceRepository;

    @Autowired
    EvaluationRepository evaluationRepository;

    @Autowired
    EvaluationService evaluationService;

    @Autowired
    public EmailService emailService;

    @Autowired
    CloudinaryConfig cloudc;

    @RequestMapping("/home")
    public String teacherHome(Principal p, Model model)
    {
        model.addAttribute("instructor", personRepository.findByUsername(p.getName()));
        model.addAttribute("allcoursesofaInstructor",personRepository.findByUsername(p.getName()).getCourseInstructor() );
        return "teacherpages/teacherhome";
    }



    //just for testing until security/login option is added
//    public String teacherHometest(Model model)
//    {
//        model.addAttribute("instructor", personRepository.findByUsername("teacher"));
//        return "teacherpages/teacherhome";
//    }

    //this route can be combine with the teacherhome page later
    @GetMapping("/listallcourses")
    public String listCourse(Principal p, Model model)
    {
        Person instructor = personRepository.findByUsername(p.getName());

        Iterable<Course> allCoursesofAInstructor = instructor.getCourseInstructor();

        model.addAttribute("allcoursesofaInstructor", allCoursesofAInstructor);

        return "teacherpages/listallcourses";
    }

    //list course info and all students/mark attendance
    @GetMapping("/detailsofacourse/{id}")
    public String detailsofcourse(@PathVariable("id") Long courseId, Model model)
    {

        Course currentCourse = courseRepository.findOne(courseId);
        model.addAttribute("test4att",currentCourse.getCourseAttendances());
        model.addAttribute("test4stt",currentCourse.getStudent());
        Iterable<Person> studentsofACourse = currentCourse.getStudent();
        model.addAttribute("courseInstructor", currentCourse.getInstructor());

        model.addAttribute("att", currentCourse.getCourseAttendances());

        model.addAttribute("course", currentCourse);
        model.addAttribute("studentsofACourse", studentsofACourse);





        //move it to new route so it can stamp the time of the time actually submitted
        //add a new attendance and set date (for a course)
//       Date now= new Date();

//        Attendance oneAttendancecourse = new Attendance();
//        Attendance oneAttendance = new Attendance();
//
//        oneAttendancecourse.setDate(now);
//        oneAttendance.setDate(now);

//        currentCourse.addAttendance(oneAttendancecourse);
//
//                for (Person student : studentsofACourse) {
//            student.addAttendance(oneAttendance);
//        }


        return "teacherpages/detailsofacourse";
    }

    @GetMapping("/markattendance/{courseId}")
    public String listAttendanceofaCourse(@PathVariable("courseId") Long courseId, Model model)
    {

        Course currentCourse = courseRepository.findOne(courseId);
        Iterable<Person> studentsofACourse = currentCourse.getStudent();

        Date now= new Date();

        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");

        String nowdate= df.format(now);

        model.addAttribute("nowdate", nowdate);
        model.addAttribute("course", currentCourse);
        model.addAttribute("studentsofACourse", studentsofACourse);

        return "teacherpages/attendanceofacourseform";
    }

    //display the attendance for a course of all students
    @PostMapping("/markattendancepo/{courseId}")
    public String postattendance(@PathVariable("courseId") Long courseId, @RequestParam("attdate") String attdate,
                                 @RequestParam(value = "attendanceStatus") String[] attendanceStatus,Model model)
    {

        Course currentCourse = courseRepository.findOne(courseId);
        Iterable<Person> studentsofACourse = currentCourse.getStudent();

        int i=0;

        for (Person student: studentsofACourse) {

            if (attendanceRepository.findAllByAttendanceCourseEqualsAndDateEqualsAndPersonAttendanceEquals(currentCourse, attdate, student) != null) {
                Attendance attdel = attendanceRepository.findAllByAttendanceCourseEqualsAndDateEqualsAndPersonAttendanceEquals(currentCourse, attdate, student);
                System.out.println(attdel.toString());
                student.removeAttendance(attdel);
                System.out.println("here1111111");
                attendanceRepository.delete(attdel);
//                System.out.println("here2222211");
            }

                Attendance attnew = new Attendance();
                attnew.setDate(attdate);
                System.out.println("printing status" + attendanceStatus[i]);
                attnew.setStatus(attendanceStatus[i]);
                System.out.println("set status done----");
                i += 1;
                attnew.setPersonAttendance(student);
                student.addAttendance(attnew);
                System.out.println("!!!!!!-----add att to student");
                attnew.setAttendanceCourse(currentCourse);
                attendanceRepository.save(attnew);

                System.out.println("newset-------");

                // problem solved
                System.out.println("!!!!!!!!"+student.getAttendances().toString());

        }


        System.out.println("end loop------");

        Person onestu = studentsofACourse.iterator().next();

        model.addAttribute("onestu", onestu);

        model.addAttribute("attdate", attdate);
        model.addAttribute("course", currentCourse);
        model.addAttribute("studentofacourse", studentsofACourse);


        return "teacherpages/tableattofonecourse";
    }

    //adding update attendence option
    @GetMapping("/updateattendance/{courseId}")
    public String updateAttendanceofaCourse(@PathVariable("courseId") Long courseId, Model model)
    {

        Course currentCourse = courseRepository.findOne(courseId);
        Iterable<Person> studentsofACourse = currentCourse.getStudent();

        Date now= new Date();
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        String nowdate= df.format(now);

        model.addAttribute("onestu",studentsofACourse.iterator().next() );

        model.addAttribute("nowdate", nowdate);
        model.addAttribute("course", currentCourse);
        model.addAttribute("studentsofACourse", studentsofACourse);

        return "teacherpages/updateattofacourseform";
    }


    //for testing: display attendance of in a table rather than list
    @GetMapping("/attforonecourse/{id}")
    public String attforonecourse(@PathVariable("id") Long courseId, Model model)
    {

        Course currentCourse = courseRepository.findOne(courseId);
        Iterable<Person> studentsofACourse = currentCourse.getStudent();

        Person onestu = studentsofACourse.iterator().next();

        model.addAttribute("onestu", onestu);
        model.addAttribute("course", currentCourse);
        model.addAttribute("studentofacourse", studentsofACourse);
        return "teacherpages/tableattofonecourse";
    }

    @GetMapping("/updateperson")
    public String editPerson(Principal principal,Model model){

        Person instructor=personRepo.findByUsername(principal.getName());
        model.addAttribute("instructor", instructor);



        return "teacherpages/teacheredit";
    }


    @PostMapping("/updateperson")
    public String savePerson(@Valid @ModelAttribute("instructor") Person instructor, BindingResult bindingResult,@RequestParam("file")MultipartFile file)
    {



        if (file.isEmpty()) {
            return "teacherpages/teacheredit";
        }
        try {
            Map uploadResult = cloudc.upload (file.getBytes(),
                    ObjectUtils.asMap("resourcetype","fixed","tra", new Transformation ().crop("crop").width ( 20 ).height ( 20 )));
            //set url as headshot
            instructor.setHeadShot (uploadResult.get("url").toString());

//            Map result = cloudinary.uploader().upload(new File("daisy.png"), ObjectUtils.asMap(
//                    "public_id", "sample_id",
//                    "transformation", new Transformation().crop("limit").width(40).height(40),
//                    "eager", Arrays.asList(
//                            new Transformation().width(200).height(200)
//                                    .crop("thumb").gravity("face").radius(20)
//                                    .effect("sepia"),
//                            new Transformation().width(100).height(150)
//                                    .crop("fit").fetchFormat("png")
//                    ),
//                    "tags", "special, for_homepage"));



            personService.update (instructor);


        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/teacher/home/";
        }
        personService.update(instructor);
        return "redirect:/teacher/home/";
    }

    //for delete or update M number for the student
    @GetMapping("/listallstudents/{courseId}")
    public String updateMnumber(@PathVariable("courseId") Long courseId, Model model) {

        Course currentCourse = courseRepository.findOne(courseId);

        model.addAttribute("course", currentCourse);
        model.addAttribute("unvalidatedstudent", courseService.unvalidatedStudent(courseId));
        model.addAttribute("validatedstudent", courseService.validatedStudent(courseId));

        return "teacherpages/liststudentsofacourse";

    }


    // update student M number from teacher side
    @GetMapping("/update/{courseId}/{studentId}")
    public String updateMnumber(@PathVariable("courseId") Long courseId, @PathVariable("studentId") Long studentId, Model model) {

        Course currentCourse = courseRepository.findOne(courseId);
        Person currentStudent= personRepository.findOne(studentId);
        model.addAttribute("student", currentStudent);
        model.addAttribute("course", currentCourse);

        return "teacherpages/updateMform";
    }


    @PostMapping("/update/{courseId}/{studentId}")
    public String updateMnumberstudent(@PathVariable("courseId") Long courseId,
                                       @PathVariable("studentId") Long studentId,
                                       @RequestParam(value="newMId") String newMId,
                                       Model model) {

        Course currentCourse = courseRepository.findOne(courseId);
        Person currentStudent= personRepository.findOne(studentId);
        currentStudent.setmNumber(newMId);
        personRepository.save(currentStudent);
        model.addAttribute("course", currentCourse);

        return "redirect:/teacher/listallstudents/{courseId}";
    }






//    @GetMapping("/displayoneeval/{id}")
//    public String displayoneEval(@PathVariable("id") long evalId, Model model) {
//
//
//        model.addAttribute("neweval", evaluationRepository.findOne(evalId));
//
//        return "teacherpages/displayoneeval";
//    }


    @GetMapping("/displayallevalsofonecourse/{courseId}")
    public String displayallEvalofonecourse(@PathVariable("courseId") long courseId, Model model) {

        Course currentCourse = courseRepository.findOne(courseId);

        Iterable<Evaluation> allevalofonecourse= currentCourse.getEvaluations();

        model.addAttribute("course", currentCourse);

        model.addAttribute("neweval", allevalofonecourse);

        return "teacherpages/displayallevalofacourse";
    }


    @RequestMapping("/delete/{courseId}/{studentId}")
    public String removestufromcourse(@PathVariable("courseId") Long courseId,@PathVariable("studentId") Long studentId, Model model) {

        Course currentCourse = courseRepository.findOne(courseId);
        Person currentStudent= personRepository.findOne(studentId);

        //keep student in the database, just remove the relationship
        courseService.removeStudentFromCourse(currentCourse,currentStudent);

        model.addAttribute("course", currentCourse);

        String message= "<h2>You have successfully remove the student from the course.</h2>";


//       String link = "<a th:href=\"@{/teacher/listallstudents/{courseId}(courseId=course.id)}\">Back to student's list</a>";

        model.addAttribute("message", message);
        model.addAttribute("courseId", courseId);
        return "teacherpages/delconfirmation";
    }




    @RequestMapping("/searchstudent/{courseId}")
    public String findstudents(@PathVariable("courseId") Long courseId, @RequestParam(value = "searchBy") String searchBy, @RequestParam(value ="fname", required=false) String fname,
                               @RequestParam(value ="lname", required=false) String lname, @RequestParam(value ="email", required=false) String email,
                               Model model)
    {

        Course currentCourse = courseRepository.findOne(courseId);

        model.addAttribute("course", currentCourse);
        model.addAttribute("unvalidatedstudent", courseService.unvalidatedStudent(courseId));
        model.addAttribute("validatedstudent", courseService.validatedStudent(courseId));

        if (searchBy.equalsIgnoreCase("all"))
        {
            System.out.println("!!!!!!!!");
            Iterable<Person> searchstudent = personRepository.findAllByFirstNameLikeAndLastNameLikeAndEmailLike(fname,lname,email);
            model.addAttribute("searchstudent", searchstudent );
            System.out.println("added to model !!");
            return "teacherpages/studentsearchresult";
//            return "redirect:/teacher/listallstudents/{courseId}";
        }

        if (searchBy.equalsIgnoreCase("first"))
        {
            model.addAttribute("searchstudent", personRepository.findAllByFirstNameLike(fname) );
//            return "redirect:/teacher/listallstudents/{courseId}";
            return "teacherpages/studentsearchresult";
        }


        if (searchBy.equalsIgnoreCase("last"))
        {
            model.addAttribute("searchstudent", personRepository.findAllByLastNameLike(lname) );
//            return "redirect:/teacher/listallstudents/{courseId}";
            return "teacherpages/studentsearchresult";
        }

        if (searchBy.equalsIgnoreCase("email"))
        {
            model.addAttribute("searchstudent", personRepository.findAllByEmailLike(email) );
//            return "redirect:/teacher/listallstudents/{courseId}";
            return "teacherpages/studentsearchresult";
        }

        if (searchBy.equalsIgnoreCase("fandl"))
        {
            model.addAttribute("searchstudent", personRepository.findAllByFirstNameLikeAndLastNameLike(fname, lname) );
//            return "redirect:/teacher/listallstudents/{courseId}";
            return "teacherpages/studentsearchresult";
        }

        else {

            model.addAttribute("searchstudent", new ArrayList<Person>());
            model.addAttribute("message", "Error with the search, try again!");
//            return "redirect:/teacher/listallstudents/{courseId}";
            return "teacherpages/studentsearchresult";
        }
    }

   @GetMapping("/addstudentstocourse/{id}")
   public String getCourse(@PathVariable("id")Long id, Model model)
   {

       Date now= new Date();

       Person student = new Person();

       student.setStartDate(now);

       System.out.println(student.getStartDate());

       model.addAttribute("course", courseRepository.findOne(id));

       model.addAttribute("newstudent", student);

       return "teacherpages/addstudent";
   }


   @PostMapping("/addstudent/{id}")
    public String postCourse(@PathVariable("id") Long id, @Valid @ModelAttribute("newstudent") Person student, BindingResult bindingResult,Model model)
   {

        if(bindingResult.hasErrors())
        {
            return "redirect:/teacher/addstudentstocourse/" +id;
        }

       Course c = courseRepository.findOne(id);
       student.setCourseStudent(c);
        student.addRole(roleService.findByRoleName("DEFAULT"));
        personRepository.save(student);
        model.addAttribute("course", c);
        model.addAttribute("newstudent", student);

        return "teacherpages/confirmstudent";
   }

    //the method to send email
  //to send an email of attendance to the admin when course ends
    @GetMapping("/courseend/{id}")
    public String emailAtCourseEnd(@PathVariable("id") long id, Model model) throws UnsupportedEncodingException {

        Course course=courseRepository.findOne(id);
        Date date= new Date();
        course.setEndDate(date);
        courseRepository.save(course);
        System.out.println("test after save End date");

        sendEmailWithoutTemplating(course);
        return "redirect:/teacher/listallcourses/";

    }



    public void sendEmailWithoutTemplating(Course course) throws UnsupportedEncodingException {
        System.out.println("test before email");
        System.out.println(course.getCourseName());
        Person admin=personRepository.findByUsername("admin");
        System.out.println("admin emaillllllllllllllllllllll"+admin.getEmail());
        final Email email= DefaultEmail.builder()
                .from(new InternetAddress("mahifentaye@gmail.com", "Attendance INFO"))
                .to(Lists.newArrayList(new InternetAddress(admin.getEmail(),"admin")))
                .subject("Attendance for" + course.getCourseName())
                .body("Course Closed.  Attendance for the class has been attached.")
                .attachment(getCsvAttendanceAttachment("Attendance",course))
                .encoding("UTF-8").build();
        System.out.println("test it");
        emailService.send(email);
    }
    private EmailAttachment getCsvAttendanceAttachment(String filename,Course course) {

        String testData = "Course CRN: " + course.getCrn() + "," + "Course Name: "+ course.getCourseName() + "\n";

        testData += "Instructor" + course.getInstructor().getFirstName() + " " + course.getInstructor().getLastName() +"\n";

        testData += "\n";

        testData +="Record Number,Student Name,M_Number\n";
        Iterable<Person> students = course.getStudent();
        Person onestu = students.iterator().next();

        testData += " " + "," + " " + "," + " " + ",";

        for (Attendance att : onestu.getAttendances())
        {

            testData += att.getDate().toString() + ",";
        }


        testData += " "+"\n";



        for (Person std : students) {
            String studName= std.getFirstName() +" "+ std.getLastName();
            String studId = String.valueOf(std.getId());
            String mnum = String.valueOf(std.getmNumber());
            Iterable<Attendance> attendances=std.getAttendances();
            testData += studId+","+ studName+","+mnum+",";
            for (Attendance att: attendances) {
                String dates=String.valueOf(att.getDate());
                String status=att.getStatus();
                testData += status+ ",";

            }
            testData += "\n";
        }

        DefaultEmailAttachment attachment = DefaultEmailAttachment.builder()
                .attachmentName(filename + ".csv")
                .attachmentData(testData.getBytes(Charset.forName("UTF-8")))
                .mediaType(MediaType.TEXT_PLAIN).build();

        return attachment;
    }

    //this is to send the email of evaluation to the logged in teacher
    @GetMapping("/sendevaluation/{id}")
    public String emailEvaluation(@PathVariable("id") long id,Principal principal, Model model) throws UnsupportedEncodingException {
        Course course=courseRepository.findOne(id);
        Iterable<Evaluation>thiscrseval=course.getEvaluations();
        System.out.println("test after save End date"+course.getCourseName());
        sendEvaluationWithoutTemplating(thiscrseval,principal,course);
        return "redirect:/teacher/home";

    }

    public void sendEvaluationWithoutTemplating(Iterable<Evaluation>evaluations, Principal principal,Course course) throws UnsupportedEncodingException {
        System.out.println("test before email");
        Evaluation eval=new Evaluation();
        System.out.println("**********************************************");

        System.out.println("--------------------"+principal.getName());
//        Person admin=new Person();
        System.out.println();
        for (Evaluation neval:evaluations) {
            eval=neval;
            System.out.println("Courssssssssssssss"+eval.getCourseEvaluation().toString());
        }
        Person inst=personRepo.findByUsername(principal.getName());

        String instemail=inst.getEmail();
        System.out.println("emailllllllllllll"+instemail);
        System.out.println(eval.getContent());
        final Email email = DefaultEmail.builder()
                .from(new InternetAddress("mahifentaye@gmail.com", "Evaluation INFO"))
                .to(Lists.newArrayList(new InternetAddress(inst.getEmail(), "Instructor")))
                .subject("Evaluation for " + course.getCourseName()+", CRN:"+ course.getCrn())
                .body("Evaluation for "+course.getCourseName()+ " has been attached.")
                .attachment(getCsvEvaluationAttachment("Evaluation", evaluations))
                .encoding("UTF-8").build();
        System.out.println("test it");
        emailService.send(email);

    }
    private EmailAttachment getCsvEvaluationAttachment(String filename, Iterable<Evaluation>evaluations) {

        String testData = "Course Content"+","+"Instruction Quality"+","+"Training Experience"+","+"Textbooks or Handouts"+","+
                "Environment and Seating"+","+"Computer Equipment"+","+"Likes"+","+"Dislikes"+","+"Suggegstions"+","+"Other classes"+","+
                "How did you find about this class"+"\n";

        for(Evaluation eval:evaluations) {
            String content = eval.getContent();
            String quality = eval.getQuality();
            String experience = eval.getExperience();
            String textbook = eval.getMaterials();
            String environment = eval.getEnvironment();
            String equipment = eval.getEquipment();
            String likes = eval.getLikes();
            String dislikes = eval.getDislikes();
            String suggestion = eval.getSuggestions();
            String otherclass = eval.getOtherClass();
            String hearaboutclass=eval.getFindings();

            testData += content + "," + quality + "," + experience + "," + textbook + "," + environment + "," + equipment
                    + "," + likes + "," + dislikes + "," + suggestion + "," + otherclass + ","+hearaboutclass+"\n";
        }
        DefaultEmailAttachment attachment = DefaultEmailAttachment.builder()
                .attachmentName(filename + ".csv")
                .attachmentData(testData.getBytes(Charset.forName("UTF-8")))
                .mediaType(MediaType.TEXT_PLAIN).build();

        return attachment;
    }


}

package study.git.gitexam;

import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import study.git.gitexam.model.Department;
import study.git.gitexam.model.Professor;
import study.git.gitexam.model.Student;
import study.git.gitexam.model.StudentDepartment;
import study.git.gitexam.service.DepartmentService;
import study.git.gitexam.service.ProfessorService;
import study.git.gitexam.service.StudentJoinService;
import study.git.gitexam.service.StudentService;
import study.git.helper.PageHelper;
import study.git.helper.WebHelper;

@Controller
public class StudentController {
	
	/** log4j 객체 생성 및 사용할 객체 주입받기 */
	private static final Logger logger = LoggerFactory.getLogger(StudentController.class);
	
	@Autowired
	WebHelper web;
	
	@Autowired
	PageHelper page;
	
	//목록, 상세보기에서 사용할 서비스 객체 --> Department와의 Join처리
	@Autowired
	StudentJoinService studentJoinService;
	
	//등록,삭제,수정에서 사용할 서비스 객체
	@Autowired
	StudentService studentService;
	
	//등록, 수정시에 소속학과에 대한 드롭다운을 구현하기 위한 서비스 객체
	@Autowired
	DepartmentService departmentService;
	
	//등록, 수정시에 소속학과에 대한 드롭다운을 구현하기 위한 서비스 객체
	@Autowired
	ProfessorService professorService;
	
	/**학생 목록 페이지*/
	@RequestMapping(value="/student/stud_list.do", method=RequestMethod.GET)
	public ModelAndView StudList(Locale locale, Model model) {
		
		/** 1)WebHelper 초기화 및 파라미터 처리*/
		web.init();
		
		StudentDepartment student = new StudentDepartment();
		
		// 검색어 파라미터 받기 + Beans 설정
		String keyword = web.getString("keyword", "");
		student.setName(keyword);
		
		// 현재 페이지 번호에 대한 파라미터 받기
		int nowPage = web.getInt("page", 1);

		/** (2) Service 객체 생성하기 --> DB접속 */		
		// 전체 데이터 수 조회하기
		int totalCount = 0;
		try {
			totalCount = studentJoinService.getStudentCount(student);
		}  catch (Exception e) {
			return web.redirect(null, e.getLocalizedMessage());
		}
		
		// 페이지 번호에 대한 연산 수행 후 조회조건값 지정을 위한 Beans에 추가하기
		page.pageProcess(nowPage, totalCount, 10, 5);
		student.setLimitStart(page.getLimitStart());
		student.setListCount(page.getListCount());
		
		/** (3) Service를 통한 SQL 수행 */
		// 조회 결과를 저장하기 위한 객체
		List<StudentDepartment> list = null;
		try {
			list = studentJoinService.getStudentJoinList(student);
		} catch (Exception e) {
			return web.redirect(null, e.getLocalizedMessage());
		}
		
		//view에게 전달할 데이터를 request 객체에 담는다.
		model.addAttribute("list", list);
		model.addAttribute("page", page); //페이지에 필요한 멤버변수가 들어있음.
		model.addAttribute("keyword", keyword);

		return new ModelAndView("student/stud_list");
	}
	
	/**학생 정보 상세보기 페이지*/
	@RequestMapping(value="/student/stud_view.do", method=RequestMethod.GET)
	public ModelAndView StudView(Locale locale, Model model) {
		
		/** 1)WebHelper 초기화 및 파라미터 처리*/
		web.init();
		
		int studno = web.getInt("studno");
		logger.debug("studno=" + studno);
		
		if(studno == 0){
			web.redirect(null,"학생번호가  없습니다.");
			return null;
		}
		
		//전달된 파라미터를 Beans에 저장한다.
		StudentDepartment student = new StudentDepartment();
		student.setStudno(studno);

		/** 2) Service 통한 SQL 수행*/
		//조회 결과를 저장하기 위한 객체
		StudentDepartment item = null;
		try{
			item = studentJoinService.getStudentJoinItem(student);
		} catch (Exception e){
			return web.redirect(null, e.getLocalizedMessage());
		} 
		
		/** 3) 데이터 조회 결과를 View에게 전달*/
		model.addAttribute("item", item);
		
		return new ModelAndView("student/stud_view");
	}
	
	/**학생 등록 페이지*/
	@RequestMapping(value="/student/stud_add.do", method=RequestMethod.GET)
	public ModelAndView StudAdd(Locale locale, Model model) {
		
		/** 1)WebHelper 초기화 및 파라미터 처리*/
		web.init();
		
		/** 2) Service를 통한 SQL 수행*/
		//조회 결과를 저장하기 위한 객체
		List<Department> deptList = null;
		List<Professor> profList = null;
		try{
			deptList = departmentService.getDepartmentList(null);
			profList = professorService.getProfessorList(null);
		} catch (Exception e){
			return web.redirect(null, e.getLocalizedMessage());
		}
				
		/** 3) 데이터 조회 결과를 View에게 전달*/
		model.addAttribute("dept_list", deptList);
		model.addAttribute("prof_list", profList);
		
		return new ModelAndView("student/stud_add");
	}
	
	/**학생 등록 처리 페이지(action page로 사용된다.)*/
	@RequestMapping(value="/student/stud_add_ok.do", method=RequestMethod.POST)
	public ModelAndView StudAddOk(Locale locale, Model model) {
		
		/** 1)WebHelper 초기화 및 파라미터 처리*/
		web.init();
		
		//input 태그의 name속성에 명시된 값을 사용한다.
		String name = web.getString("name");
		String userid = web.getString("userid");
		int grade = web.getInt("grade");
		String idnum = web.getString("idnum");
		String birthdate = web.getString("birthdate");
		String tel = web.getString("tel");
		int height = web.getInt("height");
		int weight = web.getInt("weight");
		int deptno = web.getInt("deptno");
		int profno = web.getInt("profno");
		
		//전달받은 파라미터는 로그로 값을 확인하는 것이 좋다.
		logger.debug("name=" + name);
		logger.debug("userid=" + userid);
		logger.debug("grade=" + grade);
		logger.debug("idnum=" + idnum);
		logger.debug("birthdate=" + birthdate);
		logger.debug("tel=" + tel);
		logger.debug("height=" + height);
		logger.debug("weight=" + weight);
		logger.debug("deptno=" + deptno);
		logger.debug("profno=" + profno);
				
		/** 2) 필수항목에 대한 입력 여부 검사하기*/
		//RegexHelper를 사용하여 입력값의 형식을 검사할 수도 있다. (여기선 생략)
		if(name == null) {
			web.redirect(null,"이름을 입력하세요.");
		 	return null;
		}
			 	
		if(userid == null) {
			web.redirect(null,"아이디를 입력하세요.");
			return null;
		}
			 	
		if(grade == 0) {
			web.redirect(null,"학년을 입력하세요.");
			return null;
		}
			 	
		if(idnum == null) {
			web.redirect(null,"주민등록번호를 입력하세요.");
			return null;
		}
			 	
		if(birthdate == null) {
			web.redirect(null,"전화번호을 입력하세요");
			return null;
		}

		if(tel == null) {
			web.redirect(null,"키를 입력하세요");
			return null;
		}

		if(height == 0) {
			web.redirect(null,"몸무게를 입력하세요");
			return null;
		}
					
		if(weight == 0) {
			 web.redirect(null,"생년월일을 입력하세요");
			 return null;
		}
			 	
		if(deptno == 0) {
			web.redirect(null,"학과번호를 입력하세요.");
			return null;
		}

		if(profno == 0) {
			web.redirect(null,"교수번호를 입력하세요");
			 return null;
		}
			 	
		/** 3) 저장을 위한 JavaBeans 구성하기*/
		Student student = new Student();
		student.setName(name);
		student.setUserid(userid);
		student.setGrade(grade);
		student.setIdnum(idnum);
		student.setBirthdate(birthdate);
		student.setTel(tel);
		student.setHeight(height);
		student.setWeight(weight);
		student.setDeptno(deptno);
		student.setProfno(profno);
		
		/** 4) Service를 통한 SQL 수행 */
		try{
			studentService.addStudent(student);
		} catch (Exception e){
			return web.redirect(null, e.getLocalizedMessage());
		}
		
		/** 5) 결과를 확인하기 위한 페이지로 이동하기*/
		String url = web.getRootPath() + "/student/stud_view.do?studno=" + student.getStudno();
		return web.redirect(url, "저장되었습니다.");
	}
	
	/**학생 정보 삭제 페이지*/
	@RequestMapping(value="/student/stud_delete.do", method=RequestMethod.GET)
	public ModelAndView StudDelete(Locale locale, Model model) {
		
		/** 1)WebHelper 초기화 및 파라미터 처리*/
		web.init();
		
		int studno = web.getInt("studno");
		logger.debug("studno=" + studno);
		
		if(studno == 0){
			return web.redirect(null,"학생번호가 없습니다.");
		}
		
		//MyBatis의 Where절에 사용할 값을 담은 객체
		Student student = new Student();
		student.setStudno(studno);
		
		/** 2) Service를 통한 sql 수행*/
		try{
			studentService.deleteStudent(student);
		} catch (Exception e) {
			return web.redirect(null, e.getLocalizedMessage());
		}
		
		/** 3)목록페이지로 이동*/
		return web.redirect(web.getRootPath() + "/student/stud_list.do", "삭제되었습니다.");
	}
	
	/**학생 정보 수정 페이지*/
	@RequestMapping(value="/student/stud_edit.do", method=RequestMethod.GET)
	public ModelAndView StudEdit(Locale locale, Model model) {
		
		/** 1)WebHelper 초기화 및 파라미터 처리*/
		web.init();
		
		int studno = web.getInt("studno");
		logger.debug("studno=" + studno);
		
		if(studno == 0){
			web.redirect(null,"교수번호가  없습니다.");
			return null;
		}

		//파라미터를 Beans에 저장한다.
		Student student = new Student();
		student.setStudno(studno);
	
		/** 2) Service를 통한 sql 수행*/
		//조회결과를 저장하기 위한 객체
		Student item = null;
		List<Department> deptList = null;
		List<Professor> profList = null;
		try{
			item = studentService.getStudentItem(student);
			deptList = departmentService.getDepartmentList(null);
			profList = professorService.getProfessorList(null);
		} catch (Exception e){
			return web.redirect(null, e.getLocalizedMessage());
		}
		
		/**(4) 데이터 조회 결과를 View에게 전달*/
		model.addAttribute("item", item);
		model.addAttribute("dept_list", deptList);
		model.addAttribute("prof_list", profList);
		
		return new ModelAndView("student/stud_edit");
	}
	
	/**학생 정보 수정 처리 페이지(action page로 사용된다.)*/
	@RequestMapping(value="/student/stud_edit_ok.do", method=RequestMethod.POST)
	public ModelAndView StudEditOk(Locale locale, Model model) {
		
		/** 1)WebHelper 초기화 및 파라미터 처리*/
		web.init();
		
		//input 태그의 name속성에 명시된 값을 사용한다.
		int studno = web.getInt("studno");
		String name = web.getString("name");
		String userid = web.getString("userid");
		int grade = web.getInt("grade");
		String idnum = web.getString("idnum");
		String birthdate = web.getString("birthdate");
		String tel = web.getString("tel");
		int height = web.getInt("height");
		int weight = web.getInt("weight");
		int deptno = web.getInt("deptno");
		int profno = web.getInt("profno");

		//전달받은 파라미터는 로그로 값을 확인하는 것이 좋다.
		logger.debug("studno=" + studno);
		logger.debug("name=" + name);
		logger.debug("userid=" + userid);
		logger.debug("grade=" + grade);
		logger.debug("idnum=" + idnum);
		logger.debug("birthdate=" + birthdate);
		logger.debug("tel=" + tel);
		logger.debug("height=" + height);
		logger.debug("weight=" + weight);
		logger.debug("deptno=" + deptno);
		logger.debug("profno=" + profno);

		/** 2) 필수항목에 대한 입력 여부 검사하기*/
		 //RegexHelper를 사용하여 입력값의 형식을 검사할 수도 있다. (여기선 생략)
		 if(studno == 0) {
		 web.redirect(null,"학생번호가 없습니다.");
		 return null;
		 }
		 
		 if(name == null) {
		 web.redirect(null,"이름을 입력하세요.");
		 return null;
		 }
		 
		 if(userid == null) {
		 web.redirect(null,"아이디를 입력하세요.");
		 return null;
		 }
		 
		 if(grade == 0) {
		 web.redirect(null,"학년을 입력하세요.");
		 return null;
		 }
		 
		 if(idnum == null) {
		 web.redirect(null,"주민등록번호를 입력하세요.");
		 return null;
		 }
		 
		 if(birthdate == null) {
		 web.redirect(null,"전화번호을 입력하세요");
		 return null;
		 }

		if(tel == null) {
		 web.redirect(null,"키를 입력하세요");
		 return null;
		 }

		if(height == 0) {
		 web.redirect(null,"몸무게를 입력하세요");
		 return null;
		 }

		if(weight == 0) {
		 web.redirect(null,"생년월일을 입력하세요");
		 return null;
		 }
		 
		 if(deptno == 0) {
		 web.redirect(null,"학과번호를 입력하세요.");
		 return null;
		 }

		if(profno == 0) {
		 web.redirect(null,"교수번호를 입력하세요");
		 return null;
		 }
		 
		/** 3) 저장을 위한 JavaBeans 구성하기*/
		Student student = new Student();
		student.setStudno(studno);
		student.setName(name);
		student.setUserid(userid);
		student.setGrade(grade);
		student.setIdnum(idnum);
		student.setBirthdate(birthdate);
		student.setTel(tel);
		student.setHeight(height);
		student.setWeight(weight);
		student.setDeptno(deptno);
		student.setProfno(profno);

		/** 4) Service를 통한 SQL 수행*/
		try{
			studentService.editStudent(student);
		} catch (Exception e){
			return web.redirect(null, e.getLocalizedMessage());
		}
		
		/** 5) 결과를 확인하기 위한 페이지로 이동하기*/
		//저장결과를 확인하기 위해서 상세페이지로 이동한다.
		//상세페이지에서 읽어올 데이터를 식별하기 위해서는 Primary Key값을 전달해야한다.
		String url = web.getRootPath()+ "/stud_view.do?studno=" + student.getStudno();
		return web.redirect(url, "수정되었습니다.");
	}
}



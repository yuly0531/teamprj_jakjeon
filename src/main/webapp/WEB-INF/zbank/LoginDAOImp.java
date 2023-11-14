package com.naver.erp;

import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


//	LoginDAOImp 클래스 선언.
	//관용적으로 DAO가 들어간 클래스는 직접 DB연동을 하는 클래스이다.
	// 직접 DB연동을 하는 클래스 앞에는 @Repository라는 어노테이션을 붙인다.
	// 직접 DB연동을 하는 클래스는 보통 인터페이스를 구현하여 만단다
	//여기서는 LoginDAO 인터페이스를 구현하여 선언.
@Repository
public class LoginDAOImp implements LoginDAO{
	
	
	//SqlSessionTemlate 객체를 생성해 멤버변수 sqlSession 에 저장.
		//Autowired 어노테이션을 붙이면 속성변수 자료형에 맞는 
		//SqlSessionTemlate 객체를 생성한 후
		// 객체의 메위주를 속성 변수에 저장한다.
		//------------------------------
		//SqlSessionTemlate 객체 기능
		//------------------------------
			//DB연동 시 사용하는 객체이다
			//xml 파일에 있는 sql구문을 읽어서 DB에 sql명령을 내린후
			//그 결과를 받아오는 객체. 그렇게 해주는 메소드가 존재.
	
	@Autowired
	private SqlSessionTemplate sqlSession;
	
	//mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm
	// 로그인 아이디와 암호 존재 개수를 리턴하는 메소드 선언
	// 이 메소드는 현재 LoginController객체 안의 메소드에서 호출하고 있다.
	//mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm
	@Override
	public int getLoginIdCnt(
			
		//로그인 아이디와 암호가 저장된 HashMap객체가 들어오는 매개변수 선언
			Map<String,String> idPwd
	) {
		//-----------------------------------------------
		//SqlSessionTemplate 객체의 [selectOne 메소드] 호출로
		//xml파일에 있는 select구문을 호출하여
		//[로그인 아이디, 암호 존재 개수]얻기
		//-----------------------------------------------
			//------------------------------
			//selectOne() 메소드 특징
			//------------------------------
				//select 구문을 DB에 날리고 [1행n열]의 결과를 리턴하는 메소드이다.
				//만약 selectOne 메소드 결과가 2행 이상이면 에러가 터진다.
					//--------------------------------
					//selectOne메소드의 결과가 1행 1열일 경우
					//--------------------------------
						//int 또는 String 또는 String 또는 double로 리턴.
					//--------------------------------
					//selectOne메소드의 결과가 1행 1열일 경우
					//--------------------------------
						//자바의 HashMap객체 또는 DTO객체에 저장해서 리턴된다.
		int loginIdCnt = this.sqlSession.selectOne(
				//실행할 [SQL 구문의 위치]를 문자열로 지정
				"com.naver.erp.LoginDAO.getLoginIdCnt"
				//com.naver.erp.LoginDAO => xml파일안의 mapper태그 안의 namespace값을 의미
				//							com.naver.erp.LoginDAO는 실제 존재하는 인터페이스 경로이여야한다.
				//getLoginIdCnt			 => 그 mapper태그안의 <~ id="getCntLogin">이 태그 내부의  SQL문을 말한다.
				//							이 <~ id="getCntLogin">에 getLoginCnt 는 인터페이스의 메소드 이름하고
				//							일치하여야 한다.
				
				//실행할 SQL구문에서 사용할 데이터를 지정하기
				//밑의 idPwd 변수에는 아이디와 암호가 저장된 HashMap객체가 들어있다.
				,idPwd
		);
		//[로그인 아이디, 암호 존재 개수] 리턴.
		return loginIdCnt;
	}
	
}

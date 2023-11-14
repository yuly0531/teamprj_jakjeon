package com.naver.erp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

//MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
// BoardController 클래스 선언하기
//MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
   /*
   스프링에서 관용적으로   Controller 라는 단어가 들어간 클래스는  
   URL 주소 접속 시 대응해서 호출되는 메소드를 소유하고 있다.
   클래스 이름 앞에는 @Controller  라는 어노테이션이 붙는다.
   클래스 내부의 URL 주소 접속 시 호출되는 메소드명 앞에는 
       @RequestMapping  라는 어노테이션이 붙는다.
     ------------------------------------------------------------
     @Controller 어노테이션이 붙은 클래스 특징
     ------------------------------------------------------------
         (1) 스프링프레임워크가 알아서 객체를 생성하고 관리한다.
         (2) URL 주소 접속 시 대응해서 호출되는 메소드를 소유하고 있다.
   */
@Controller
public class BoardController {

   @Autowired
   private BoardDAO boardDAO;  

   @Autowired
   private BoardService boardService; 
   
   @Autowired
	private ExchangeDAO exchangeDAO;
   @Autowired
	private KospiDaqDAO kospiDaqDAO;

   @RequestMapping( value="/boardList.do")
   public ModelAndView boardList( 
         //----------------------------------------------------------------
         // 웹브라우저가 가져온 파값을 저장하고있는 DTO 객체를 매개변수로 선언
         //----------------------------------------------------------------
            // [파라미터명]과 [BoardSearchDTO 객체]의 [멤버변수명]이 같으면
            // setter 메소드가 작동되어 [파라미터값]이 [멤버변수명]에 저장된다.
            // 만약 [파라미터명] 중에 [멤버변수명]과 같은 놈이 없다면
            // setter 메소드가 작동되어 않아서 [멤버변수명]에 저장되지 않는다.
         BoardSearchDTO    boardSearchDTO      //=> 근데 여기 아무것도 없음 이따 호출할꺼라서 만듦
         ,HttpSession session
                  /*SessionInterceptor에서 로그인 성공하면 들어오게 설정해놔서 필요 없음
                  //--------------------------------------
                  // HttpSession 객체의 메위주를 저장하고있는 매개변수 session 선언하기
                  // 이 객체는 이전에 로그인할 때 생성된 객체로 없어지지않고 살아있다.
                  //--------------------------------------
                  ,HttpSession session
                  */
   ){
	   
	   
	   
		session.setAttribute("selectPageNo", 1);
	   				
	   
	   			String mem_nick = (String)session.getAttribute("mem_nick");
                 
      Map<String,Object> boardMap = getBoardSearchResultMap( boardSearchDTO );
      
      Map<String, Object> kospiListDescMap = getKospiListDescMap();
	  Map<String, Object> kosdaqListDescMap = getKosdaqListDescMap();
	  List<Map<String, String>> exchangeDescList = getExchangeDescList();

      //-------------------------------------------------------------
      // [ModelAndView 객체] 생성하기
      // [ModelAndView 객체]의 setViewName 메소드 호출하여  
      //               [호출할 JSP 페이지명]을 문자로 저장하기
      // [ModelAndView 객체]에 변수 boardMap 에 저장된 객체를 저장하기
      // <주의>
      //   [ModelAndView 객체] 에 addObject 메소드로 저장된 객체는
      //    HttpServletRequset 객체에 저장된다.
      //-------------------------------------------------------------
      ModelAndView mav = new ModelAndView();
      mav.setViewName( "boardList.jsp" );
      mav.addObject(   "boardMap" , boardMap     );
      
      mav.addObject("kospiListDescMap", kospiListDescMap);// 
      mav.addObject("kosdaqListDescMap", kosdaqListDescMap);// 
      mav.addObject("exchangeDescList", exchangeDescList);// 
      
      //-------------------------------------------------------------
      // [ModelAndView 객체] 리턴하기
      //-------------------------------------------------------------
      return  mav;
   }
   


   //mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm
   //mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm
   // 게시판 검색 관련 각종 정보들 구해서, Map<String,Object> 객체에 담아 리턴하기
   // 게시판 검색 관련 각종 정보들은 JSP 페이지에서 꺼내어 출력할 예정
   //mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm
   public Map<String,Object> getBoardSearchResultMap( 
         // 웹브라우저가 가져온 파값을 저장할 DTO 객체를 매개변수로 선언
         BoardSearchDTO    boardSearchDTO
   ){
      //-------------------------------------------------------------
      // 검색 화면에 필요한 각종 데이터를 저장할 Map<String,Object> 객체  생성.
      //-------------------------------------------------------------
      // 게시판 검색 결과 목록(=n행m열)을 저장할 List<Map<String,String>> 객체 메위주 저장할 변수 boardList 선언하기.
      // 게시판 검색 결과 개수 저장할 변수 boardListCnt 선언하기.
      // 게시판 모든 개수 저장할 변수 boardListCntAll 선언하기.
      // 페이징 처리 관련 데이터 저장한 Map<String,Integer> 객체 저장할 변수 pagingMap 선언하기.
      //-------------------------------------------------------------
      Map<String,Object> resultMap = new HashMap<String,Object>();
      
      List<Map<String,String>> boardList;            //boardList에는 1개의 ArrayList 객체 메위주가 들어가는데 n개의 해시맵객체가 들어있음
                                          //그 해시맵객체의 키값은 String이고 맞물려있는 건 다양한 객체임
      int boardListCnt;                        //게시판의 개수
      int boardListCntAll;                     //검색결과 모든개수
      Map<String,Integer> pagingMap;
      //-------------------------------------------------------------
      // 게시판 모든 개수 구해서 변수 boardListCntAll 에 저장하기
      // 게시판 검색 결과 개수 구해서 변수 boardListCnt 에 저장하기
      // 페이징 처리 관련 데이터가 저장된 Map<String,Integer> 객체를 구해서 변수 pagingMap에 저장하기
      //-------------------------------------------------------------
      boardListCntAll =  this.boardDAO.getBoardListCntAll(   );
      boardListCnt =  this.boardDAO.getBoardListCnt(  boardSearchDTO );      //getBoardListCnt 검색 결과의 개수 
      pagingMap = Util.getPagingMap(
            boardSearchDTO.getSelectPageNo()       // 선택한 페이지 번호
            , boardSearchDTO.getRowCntPerPage()    // 한 화면에 보여지는 행의 개수
            , boardListCnt                          // 검색된 게시판의 총개수
      );
      //-------------------------------------------------------------
      // BoardSearchDTO 객체에 
      //      선택 페이지 번호, 페이지 당 보일 행 개수
      //      테이블에서 검색 시 사용할 시작 행 번호
      //      테이블에서 검색 시 사용할 끝 행 번호
      // 저장하기.
      // BoardSearchDTO 객체에 저장된 데이터들은 SQL 구문에서 사용할 데이터이다.
      // SQL 구문은 mapper_board.xml 에 들어 있다.
      //-------------------------------------------------------------
      boardSearchDTO.setSelectPageNo(  (int)pagingMap.get("selectPageNo")  );  // 보정된 선택 페이지 번호 저장하기
      boardSearchDTO.setRowCntPerPage( (int)pagingMap.get("rowCntPerPage") );  // 페이지 당 보일 행 개수 저장하기
      boardSearchDTO.setBegin_rowNo(   (int)pagingMap.get("begin_rowNo")   );  // 테이블에서 검색 시 시작 행 번호 저장하기
      boardSearchDTO.setEnd_rowNo(     (int)pagingMap.get("end_rowNo")     );  // 테이블에서 검색 시 끝 행 번호 저장하기
      //-------------------------------------------------------------
      // 게시판 검색 결과 목록 저장한 List<Map<String,String>> 객체 메위주 boardList 에 저장하기
      // BoardDAO 인터페이스를 구현한 객체의 getBoardList 메소드를 호출하여
      // 게시판 검색 결과물은 저장한 List<Map<String,String>> 객체를 얻어
      // 변수 boardList 에 저장하기
      //-------------------------------------------------------------
      boardList       =  this.boardDAO.getBoardList( boardSearchDTO  );

      //-------------------------------------------------------------
      // Map<String,Object> 객체에 위에서 구한 모든 데이터를 저장하기
      //-------------------------------------------------------------
      resultMap.put(  "boardList"       , boardList        );
      resultMap.put(  "boardListCnt"    , boardListCnt     );
      resultMap.put(  "boardListCntAll" , boardListCntAll  );
      resultMap.put(  "boardSearchDTO"  , boardSearchDTO   );
      //--------------
      resultMap.put(  "begin_pageNo"          , pagingMap.get("begin_pageNo")        );
      resultMap.put(  "end_pageNo"            , pagingMap.get("end_pageNo")          );
      resultMap.put(  "selectPageNo"          , pagingMap.get("selectPageNo")        );
      resultMap.put(  "last_pageNo"           , pagingMap.get("last_pageNo")         );
      resultMap.put(  "begin_serialNo_asc"    , pagingMap.get("begin_serialNo_asc")  );
      resultMap.put(  "begin_serialNo_desc"   , pagingMap.get("begin_serialNo_desc") );

      //--------------------------------------------------
      // [Map<String,Object> 객체] 리턴하기
      //--------------------------------------------------
      return  resultMap;
   }


   //mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm
   // /boardDetailForm.do 접속 시 호출되는 메소드 선언
   //mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm
   @RequestMapping( value="/boardDetailForm.do" )
   public ModelAndView boardDetailForm(    
         //---------------------------------------
         // "b_no" 라는 파라미터명의 파라미터값이 저장되는 매개변수 b_no 선언
         // 상세보기 할 게시판 고유 번호가 들어오는 매개변수 선언
         //---------------------------------------
         @RequestParam(value="b_no") int b_no   
   ){
      //*******************************************************
      // 상세보기 화면에서 필요한 [1개의 게시판 글]을 가져오기
      //*******************************************************   
      BoardDTO boardDTO = this.boardService.getBoard(b_no,true);  
      
      Map<String, Object> kospiListDescMap = getKospiListDescMap();
      Map<String, Object> kosdaqListDescMap = getKosdaqListDescMap();
      List<Map<String, String>> exchangeDescList = getExchangeDescList();

      //*******************************************************
      // ModelAndView 객체 생성하여 변수 mav 저장하기
      //*******************************************************   
      ModelAndView mav = new ModelAndView( );
      
      //*******************************************************
      //만약에 변수 boardDTO 가 null 이 아니면, 즉 게시판글이 존재하면
      //*******************************************************
      if( boardDTO!=null ) {
         //ModelAndView 객체에 DB연동 결과물 저장하기
         mav.setViewName( "boardDetailForm.jsp");
         mav.addObject("boardDTO", boardDTO);
      }
      //*******************************************************
      //만약에 변수 boardDTO 가 null 이면, 즉 게시판 글이 삭제 되었으면
      //*******************************************************
      else {
         //*******************************************************
         //ModelAndView 객체에 호출할 JSP 페이지를 "boardEmty.jsp" 저장하기
         //*******************************************************
         mav.setViewName("boardEmpty.jsp");
         
      }
      mav.addObject("kospiListDescMap", kospiListDescMap);// 
      mav.addObject("kosdaqListDescMap", kosdaqListDescMap);// 
      mav.addObject("exchangeDescList", exchangeDescList);// 
      
      //*******************************************************
      // [ModelAndView 객체] 리턴하기
      //*******************************************************
      return mav;
      
   }
   
   //mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm
   // /boardUpDelForm.do 접속 시 호출되는 메소드 선언
   //mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm
   @RequestMapping(value="/boardUpDelForm.do" )
   public ModelAndView boardUpDelForm( 
         //---------------------------------------
         // "b_no" 라는 파라미터명의 파라미터값이 저장되는 매개변수 b_no 선언
         // 상세보기 할 게시판 고유 번호가 들어오는 매개변수 선언
         //---------------------------------------
         @RequestParam(value="b_no") int b_no   
   ) {

      //*******************************************************
      // 수정/삭제 화면에서 필요한 [1개의 게시판 글]을 기져오기
      //*******************************************************   
      BoardDTO boardDTO = this.boardDAO.getBoard(b_no);  
      
      //*******************************************************
      // [ModelAndView 객체] 생성하기
      // [ModelAndView 객체]에 [호출 JSP 페이지명]을 저장하기
      //*******************************************************
      ModelAndView mav = new ModelAndView( );
      //***************************************
      //만약에 변수 boardDTO가 null이 아니면. 즉, 게시판글이 존재하면
      //***************************************
      if( boardDTO!=null) {
         mav.setViewName("boardUpDelForm.jsp");
         mav.addObject("boardDTO",boardDTO);
      }
      //***************************************
      //만약에 변수 boardDTO가 null이면. 즉, 게시판글이 삭제 되었으면
      //***************************************
      else{
         //**************************************
         //ModelAndView 객체에 호출할 JSP페이지를 "boardEmpty.jsp" 저장하기.
         //**************************************
         mav.setViewName("boardEmpty.jsp");
      }
      
      //*******************************************************
      // [ModelAndView 객체] 리턴하기
      //*******************************************************
      return mav;
   }
   
   
   
   
   
   
   //mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm
   // /boardUpProc.do 접속 시 호출되는 메소드 선언
   //mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm
   @RequestMapping( 
         value="/boardUpProc.do" 
         ,method=RequestMethod.POST
         ,produces="application/json;charset=UTF-8"
   )
   @ResponseBody
   public Map<String,String> boardUpProc( 
      //*******************************************
      // 파라미터값이 저장된 [BoardDTO 객체]가 들어올 매개변수 선언
      //*******************************************
         // [파라미터명]과 [BoardDTO 객체]의 [멤버변수명]이 같으면
         // setter 메소드가 작동되어 [파라미터값]이 [멤버변수]에 저장된다.
         
      @Valid
      BoardDTO boardDTO
      
      //====================
      //@Valid ???
      //====================
      //매개변수로 들어오는 DTO 객체 앞에 붙어 아래 기능을 수행한다.
      //DTO 객체 안의 멤버변수에 붙은 유효성 체크 어노테이션으로 유효성 체크하고
      //유효성 체크 시 발생하는 메시지 문자는 BindingResult 객체에 저장한다.
      
      //**********************************************
      //동료 매개변수 중 @Valid가 붙은 DTO 객체에서
      //유효성 체크 시 발생하는 에러 메시지를 관리하는
      //Error 객체들을 관리하는 BindingResult 객체가 들어오는
      //매개변수 bindingResult 선언
      //**********************************************
      ,BindingResult bindingResult
   ){
      //*******************************************************
      // 게시판 수정 결과물을 저장할 HashMap 객체 생성하기.
      // 게시판 수정 행의 개수를 저장할 변수 boardUpCnt 선언하기.
      // 경고 메시지 저장 변수 errorMsg 선언하기.
      //*******************************************************
      Map<String,String> responseMap = new HashMap<String,String>();
      int boardUpCnt = 0;
      String errorMsg = "";
      
      try {
         //-----------------------------------------
         //Util 클래스의 getErrormsgFromBindingResult 메소드를 호출해서
         //유효성 체크시 발생한 에러 메시지를 얻어 변수 errorMsg에 저장하기
         //-----------------------------------------
         errorMsg = Util.getErrormsgFromBindingResult(bindingResult);
         
         //-----------------------------------------
         //만약에 변수 errorMsg에 에러문자가 있다면
         //즉, 유효성 체크시 에러가 발생했으면
         //-----------------------------------------
         if(errorMsg!=null && errorMsg.length()>0 ) {
            boardUpCnt = -21; 
         }
         //-----------------------------------------
         //만약에 변수 errorMsg에 에러문자가 없다면
         //즉, 유효성 체크시 에러가 발생 안했으면
         //-----------------------------------------
         else {
            //-------------------------------------------
            // [BoardServiceImpl 객체]의 insertBoard 메소드 호출로 
            // 게시판 글 입력하고 [입력 적용행의 개수] 얻기
            //-------------------------------------------
            boardUpCnt = this.boardService.updateBoard(boardDTO);
            }
         
         
      }
      catch(Exception ex) {
         errorMsg = "게시판 수정이 실패했습니다. 관리자에게 문의 바랍니다.";
         boardUpCnt = -9;
      }
      
      
      
      
      
      //*******************************************************
      // HashMap 객체에 경고 메시지, 게시판 수정 행의 개수 저장하기
      //*******************************************************
      responseMap.put("errorMsg"    , errorMsg       );
      responseMap.put("boardUpCnt"  , boardUpCnt+"" );
      //*******************************************************
      // HashMap 객체의 메위주 리턴하기
      //*******************************************************
      return responseMap;
   }
   

   //mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm
   // /boardDelProc.do 로 접근하면 호출되는 메소드 선언하기
   //mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm 
   @RequestMapping( 
         value="/boardDelProc.do" 
         ,method=RequestMethod.POST
         ,produces="application/json;charset=UTF-8"
   )
   @ResponseBody
   public int boardDelProc( 
         //*******************************************
         // 파라미터값이 저장된 [BoardDTO 객체]가 들어올 매개변수 선언
         //*******************************************
            // [파라미터명]과 [BoardDTO 객체]의 [속성변수명]이 같으면
            // setter 메소드가 작동되어 [파라미터값]이 [속성변수]에 저장된다.
         BoardDTO boardDTO
   ) {
      //**************************************************************
      // BoardServeImpl 객체의 deleteBoard 메소드 호출로 
      // 게시판글을 삭제하고 삭제된 행의 개수얻기
      //**************************************************************
      int deleteBoardcnt = this.boardService.deleteBoard(boardDTO);

      
      //**************************************************************
      // 게시판글 삭제된 행의 개수 리턴하기
      //**************************************************************
      return deleteBoardcnt;
   }
   

   //mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm
   //mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm
   // URL 주소 /boardRegForm.do 로 접근하면 호출되는 메소드 선언
   //mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm
   @RequestMapping( value="/boardRegForm.do")
   public ModelAndView boardRegForm(   ){
      //----------------------------------------------------
      // [ModelAndView 객체] 생성하기
      // [ModelAndView 객체]에 [호출 JSP 페이지명]을 저장하기
      //-------------------------------------------
      ModelAndView mav = new ModelAndView( );
      mav.setViewName( "boardRegForm.jsp");
      //----------------------------------------------------
      // [ModelAndView 객체] 리턴하기
      //----------------------------------------------------
      return mav;
   }
   


   //mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm
   //mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm
   // /boardRegProc.do 로 접근하면 호출되는 메소드 선언하기
   //mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm
   @RequestMapping( 
         value="/boardRegProc.do" 
         ,method=RequestMethod.POST
         ,produces="application/json;charset=UTF-8"
   )
   @ResponseBody
   public Map<String,String> boardRegProc(  
         //*******************************************
         // 파라미터값이 저장된 [BoardDTO 객체]가 저장되어 들어오는 매개변수 선언
         //*******************************************
            // [파라미터명]과 [DTO 객체]의 [멤버변수명]이 같으면
            // setter 메소드가 작동되어 [파라미터값]이 [속성변수]에 저장된다.
         
         
         @Valid
         BoardDTO  boardDTO
               //====================
               //@Valid ???
               //====================
               //매개변수로 들어오는 DTO 객체 앞에 붙어 아래 기능을 수행한다.
               //DTO 객체 안의 멤버변수에 붙은 유효성 체크 어노테이션으로 유효성 체크하고
               //유효성 체크 시 발생하는 메시지 문자는 BindingResult 객체에 저장한다.
         
         //**********************************************
         //동료 매개변수 중 @Valid가 붙은 DTO 객체에서
         //유효성 체크 시 발생하는 에러 메시지를 관리하는
         //Error 객체들을 관리하는 BindingResult 객체가 들어오는
         //매개변수 bindingResult 선언
         //**********************************************
         ,BindingResult bindingResult
   ){
      //*******************************************************
      // 게시판 입력 결과물을 저장할 HashMap 객체 생성하기.
      // 경고 메시지 저장 변수 선언
      // 게시판 입력 행의 개수를 저장할 변수 boardRegCnt 선언하기.
      //*******************************************************
      Map<String,String> responseMap = new HashMap<String,String>();
      String errorMsg = "";
      int boardRegCnt = 0;
   
    
      //*******************************************************
      // 예외가 발생할 가능성이 있는 코드는 try{} 영역에 삽입하기
      //*******************************************************
      try{     
         
         //-----------------------------------------
         //Util 클래스의 getErrormsgFromBindingResult 메소드를 호출해서
         //유효성 체크시 발생한 에러 메시지를 얻어 변수 errorMsg에 저장하기
         //-----------------------------------------
         errorMsg = Util.getErrormsgFromBindingResult(bindingResult);
         
         //-----------------------------------------
         //만약에 변수 errorMsg에 에러문자가 있다면
         //즉, 유효성 체크시 에러가 발생했으면
         //-----------------------------------------
         if(errorMsg!=null && errorMsg.length()>0 ) {
            boardRegCnt = -21; 
         }
         //-----------------------------------------
         //만약에 변수 errorMsg에 에러문자가 없다면
         //즉, 유효성 체크시 에러가 발생 안했으면
         //-----------------------------------------
         else {
            //-------------------------------------------
            // [BoardServiceImpl 객체]의 insertBoard 메소드 호출로 
            // 게시판 글 입력하고 [입력 적용행의 개수] 얻기
            //-------------------------------------------
            boardRegCnt = this.boardService.insertBoard(boardDTO);
            }
      }
      
      
      
      //*******************************************************
      // try 영역에서 예외 발생 시 실행할 catch 구문 선언하기
         //  catch 구문의 매개변수에는 예외처리 관리객체가 들어온다.
      //*******************************************************
      catch(Exception ex){
         errorMsg = "게시판 입력이 실패했습니다. 관리자에게 문의 바랍니다.";
         boardRegCnt = -1;
      }
      //*******************************************************
      // HashMap 객체에 경고 메시지, 게시판 저장 행의 개수 저장하기
      //*******************************************************
      responseMap.put("errorMsg"    , errorMsg       );
      responseMap.put("boardRegCnt" , boardRegCnt+"" );
      //*******************************************************
      // HashMap 객체 리턴하기
      //*******************************************************
      return responseMap;
   }
   
   

   //mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm
   //mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm
   // 현재 이 [컨트롤러 클래스] 내의 @ReqeustMapping이 붙은 메소드 호출 시
   // 예외 발생하면 호출되는 메소드 선언
   //mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm
   //mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm
   @ExceptionHandler(Exception.class)
   public String handleException(
         HttpServletRequest request
   ) {
      //*******************************************
      // 호출할 error.jsp 페이지를 문자열로 리턴
      //*******************************************
      return "error.jsp";
   }
   
   
   

   //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   public List<Map<String, String>> getExchangeDescList( //환율

		   ) {

	   Map<String, Object> resultMap = new HashMap<String, Object>();
	   List<Map<String, String>> exchangeDescList;

	   exchangeDescList = this.exchangeDAO.getExchangeDescList();

	   return exchangeDescList;
   }

   //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   public Map<String, Object> getKosdaqListDescMap( // kospi 리스트 가져오기..

		   ) {

	   Map<String, Object> resultMap = new HashMap<String, Object>();
	   List<Map<String, String>> kosdaqListDesc;

	   kosdaqListDesc = this.kospiDaqDAO.getKosdaqListDesc();


	   resultMap.put("kosdaqListDesc", kosdaqListDesc);
	   //System.out.println(kospiList);
	   //resultMap.put("kospiListCnt", kospiList.size());
	   return resultMap;
   }

   //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   public Map<String, Object> getKospiListDescMap( // kospi 리스트 가져오기..

		   ) {

	   Map<String, Object> resultMap = new HashMap<String, Object>();
	   List<Map<String, String>> kospiListDesc;

	   kospiListDesc = this.kospiDaqDAO.getKospiListDesc();

	   //System.out.println(kospiListDesc);
	   resultMap.put("kospiListDesc", kospiListDesc);
	   //System.out.println(kospiList);
	   //resultMap.put("kospiListCnt", kospiList.size());
	   return resultMap;
   }

   //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////







   
   
   
   
   
   
   
   
   
   
   
   
   
   

   /*
   //mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm
   // URL 주소  /boardListProc.do 로 접근하면 호출되는 메소드 선언
   //mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm
      // 메소드 앞에 
      // @RequestMapping(~,~,produces="application/json;charset=UTF-8") 하고
      // @ResponseBody  가 붙으면 리턴하는 데이터가 웹브라우저에게 전송된다.
   @RequestMapping( 
         value="/boardListProc.do" 
         ,method=RequestMethod.POST
         ,produces="application/json;charset=UTF-8"
   )
   @ResponseBody
   public Map<String,Object> boardListProc( 
         BoardSearchDTO    boardSearchDTO
   ){

      System.out.println( boardSearchDTO.getKeyword1() );
      Map<String,Object> map = new HashMap<String,Object>();
      

      List<Map<String,String>> boardList =  this.boardDAO.getBoardList2( boardSearchDTO  );
      map.put(  "boardList" , boardList );
      map.put(  "boardListCnt" , boardList.size());
      
      

      //--------------------------------------------------
      // [ModelAndView 객체] 리턴하기
      //--------------------------------------------------
      return  map;
   }
    */
}







package ch.uzh.ifi.web2.howler;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import ch.uzh.ifi.feedback.library.rest.IRestManager;
import ch.uzh.ifi.feedback.library.rest.RestManager;

/**
 * Servlet implementation class MainServlet
 */
@WebServlet("/")
public class MainServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private IRestManager restManager;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MainServlet() {
        super();
        InitController();
    }
    
    private void InitController(){
    	
    	restManager = new RestManager();
        
        try{
        	restManager.Init("ch.uzh.ifi.web2.howler");
        }
        catch(Exception ex){
        	System.out.println(ex.getMessage());
        	ex.printStackTrace();
        	super.destroy();
        }
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		restManager.Get(request, response);
	}
	
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		restManager.Put(request, response);
	}
}

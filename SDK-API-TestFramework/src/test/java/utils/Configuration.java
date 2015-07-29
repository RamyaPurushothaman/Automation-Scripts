package utils;

/**   @author: Grace Xu
 *    @created: 2012-9-13
 *    
 */

import java.io.*;
import java.util.Properties;
	 
public class Configuration {
    private static Configuration _instance = null;
    public static String test_env = null;
    public static String URL = "";
	public static String jdbcUrl = "";
	public static String username = "";
	public static String password = "";
	 
    protected Configuration(){
	    try{
	    	InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("env.properties");
	        Properties props = new Properties();
	        props.load(inputStream);
	        test_env = props.getProperty("host"); 
	        jdbcUrl = props.getProperty("jdbcUrl");
	        username = props.getProperty("username");
	        password = props.getProperty("password");
	    } 
	    catch(Exception e){  System.out.println("error" + e); }	 
    }
	 
    public static Configuration instance(){
        if (_instance == null) {
            _instance = new Configuration();
        }
        return _instance;
    }
}

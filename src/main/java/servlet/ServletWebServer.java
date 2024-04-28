package servlet;

import java.io.File;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServletWebServer {
	private static final Logger log = LoggerFactory.getLogger(ServletWebServer.class);

	private static final String WEB_APP_DIRECTORY = "webapp/";
	private static final int PORT = 8080;

	public static void main(String[] args) throws LifecycleException {
		Tomcat tomcat = new Tomcat();
		tomcat.setPort(PORT);

		String webAppAbsolutePath = new File(WEB_APP_DIRECTORY).getAbsolutePath();
		tomcat.addWebapp("/", webAppAbsolutePath);

		Connector connector = tomcat.getConnector();
		connector.setURIEncoding("UTF-8");

		log.debug(String.format("tomcat start at port %d, webapp directory: %s", PORT, webAppAbsolutePath));

		tomcat.start();
		tomcat.getServer().await();
	}
}

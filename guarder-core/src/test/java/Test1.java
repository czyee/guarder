import org.czyee.guarder.annotated.controller.ResourceController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Test1 {

	public static void main(String[] args) {
		String l = "PermissionHandler GuarderInterceptor InternalResourceViewResolver ResourceController ResourceHttpRequestHandler RefreshListener HttpRequestHandlerAdapter RequestMappingHandlerAdapter SimpleUrlHandlerMapping";
		String[] s1 = l.split(" ");
		for (String s : s1) {
			s = s.trim();
			System.out.println("registerBean("+s+".class);");
		}
	}
}

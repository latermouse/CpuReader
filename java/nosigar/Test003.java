package nosigar;
import java.lang.management.ManagementFactory;

import com.sun.management.OperatingSystemMXBean;

public class Test003 {
	public static void main(String[] args) {
		Thread thread = new Thread(new Runnable()  
	       {  
	           @Override  
	           public void run()  
	           {  
	               while (true)  
	               {  
	               }  
	           }  
	       });  
	       long result = 0l;  
	       long result2 = 0l;  
			OperatingSystemMXBean osMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

	       if (osMXBean instanceof com.sun.management.OperatingSystemMXBean)  
	       {  
	           com.sun.management.OperatingSystemMXBean sunOSMXBean = (com.sun.management.OperatingSystemMXBean) osMXBean;  
	           result = sunOSMXBean.getProcessCpuTime();  
	       }  
	       thread.start();  
	       try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	  
	       OperatingSystemMXBean osMXBean2 = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
	       if (osMXBean2 instanceof com.sun.management.OperatingSystemMXBean)  
	       {  
	           com.sun.management.OperatingSystemMXBean sunOSMXBean2 = (com.sun.management.OperatingSystemMXBean) osMXBean2;  
	           result2 = sunOSMXBean2.getProcessCpuTime();  
	       }  
	       System.out.println(result + "          " + result2);  

	}
}

package sigar;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

public class CPUReaderUtil {
	static Sigar sigar = new Sigar();

	public static void main(String[] args) {
		System.out.println(getCPU());
		System.out.println(getJVM());
	}

	public static String getCPU() {
		try {
			Mem mem = sigar.getMem();
			CpuPerc cpuList[] = sigar.getCpuPercList();
			System.out.println("*****��ǰCPUʹ����� ��");
			double all = 0;
			for (int i = 0; i < cpuList.length; i++) {
				all += cpuList[i].getCombined();

			}
			return "#��ʹ����: " + all * 100 / cpuList.length + "%";
		} catch (SigarException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "0%";
		}

	}

	public static String getJVM() {

		NumberFormat format = new DecimalFormat("0");

		RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();

		String pid = runtime.getName().split("@")[0]; // javaw��pid��windows���ã�linuxû��

		int availableProcessors = Runtime.getRuntime().availableProcessors();

		try {
			return format.format(sigar.getProcCpu(pid).getPercent()
					/ availableProcessors * 100) + '%';
		} catch (SigarException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "0%";
		}

	}

}

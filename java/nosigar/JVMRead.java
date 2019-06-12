package nosigar;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.PostConstruct;



/**
 * 网管信息信息信息采集类
 * 
 * @author liming.cen
 * @date 2017-4-11
 */

public class JVMRead {

 private static final int CPUTIME = 2000;
	/**
	 * 网管进程信息采集周期(注意：PERIOD_TIME 一定要大于 SLEEP_TIME )
	 */
	private static final int PERIOD_TIME = 1000 * 60 * 15;
	/**
	 * 此类中Thread.sleep()里的线程睡眠时间
	 */
	private static final int SLEEP_TIME = 1000 * 60 * 9;
	private static final int PERCENT = 100;
	private static final int FAULTLENGTH = 10;
	private String isWindowsOrLinux = isWindowsOrLinux();
	private String pid = "";
	private Timer sysInfoGetTimer = new Timer("sysInfoGet");
	
	public int getCpu() {
		int cpuUsage = 0;
		String osName = System.getProperty("os.name");//查看系统类型
		if (osName.toLowerCase().contains("windows") || osName.toLowerCase().contains("win")) {
			try {
				String procCmd = System.getenv("windir")
						+ "//system32//wbem//wmic.exe process get Caption,CommandLine,KernelModeTime,ReadOperationCount,ThreadCount,UserModeTime,WriteOperationCount";
				// 取进程信息
				long[] c0 = readCpu(Runtime.getRuntime().exec(procCmd));// 第一次读取CPU信息
				Thread.sleep(500);// 睡500ms
				long[] c1 = readCpu(Runtime.getRuntime().exec(procCmd));// 第二次读取CPU信息
				if (c0 != null && c1 != null) {
					long idletime = c1[0] - c0[0];// 空闲时间
					long busytime = c1[1] - c0[1];// 使用时间
					Double cpusage = Double.valueOf(100 * (busytime) * 1.0 / (busytime + idletime));
					BigDecimal b1 = new BigDecimal(cpusage);
					cpuUsage = b1.setScale(2, BigDecimal.ROUND_HALF_UP).intValue();
				} else {
					cpuUsage = 0;
				}
			} catch (Exception ex) {
				cpuUsage = 0;
			}
 
		} else {
			try {
				Map<?, ?> map1 = cpuinfo();
				Thread.sleep(500);
				Map<?, ?> map2 = cpuinfo();
 
				long user1 = Long.parseLong(map1.get("user").toString());
				long nice1 = Long.parseLong(map1.get("nice").toString());
				long system1 = Long.parseLong(map1.get("system").toString());
				long idle1 = Long.parseLong(map1.get("idle").toString());
 
				long user2 = Long.parseLong(map2.get("user").toString());
				long nice2 = Long.parseLong(map2.get("nice").toString());
				long system2 = Long.parseLong(map2.get("system").toString());
				long idle2 = Long.parseLong(map2.get("idle").toString());
 
				long total1 = user1 + system1 + nice1;
				long total2 = user2 + system2 + nice2;
				float total = total2 - total1;
 
				long totalIdle1 = user1 + nice1 + system1 + idle1;
				long totalIdle2 = user2 + nice2 + system2 + idle2;
				float totalidle = totalIdle2 - totalIdle1;
 
				float cpusage = (total / totalidle) * 100;
 
				BigDecimal b1 = new BigDecimal(cpusage);
				cpuUsage = b1.setScale(2, BigDecimal.ROUND_HALF_UP).intValue();
			} catch (InterruptedException e) {
				cpuUsage = 0;
			}
		}
 
		return cpuUsage;
	}
	/**
	 * 功能：Linux CPU使用信息
	 */
	public Map<?, ?> cpuinfo() {
		InputStreamReader inputs = null;
		BufferedReader buffer = null;
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			inputs = new InputStreamReader(new FileInputStream("/proc/stat"));
			buffer = new BufferedReader(inputs);
			String line = "";
			while (true) {
				line = buffer.readLine();
				if (line == null) {
					break;
				}
				if (line.startsWith("cpu")) {
					StringTokenizer tokenizer = new StringTokenizer(line);
					List<String> temp = new ArrayList<String>();
					while (tokenizer.hasMoreElements()) {
						String value = tokenizer.nextToken();
						temp.add(value);
					}
					map.put("user", temp.get(1));
					map.put("nice", temp.get(2));
					map.put("system", temp.get(3));
					map.put("idle", temp.get(4));
					map.put("iowait", temp.get(5));
					map.put("irq", temp.get(6));
					map.put("softirq", temp.get(7));
					map.put("stealstolen", temp.get(8));
					break;
				}
			}
		} catch (Exception e) {
		} finally {
			try {
				buffer.close();
				inputs.close();
			} catch (Exception e2) {
			}
		}
		return map;
	}
	
	

	/**
	 * 初始化bean的时候就立即获取JVM进程的PID及执行任务
	 * 
	 * @return
	 */
	public void init() {		
		//System.out.println("CPU使用率："+getCpu());
		//long startTime = System.currentTimeMillis();
		//System.out.println("JVM  PID：" + pid);
		//System.out.println("JVM的CPU使用率：" + getCPURate() + "%");
		sysInfoGetTimer.schedule(new SysInfoAcquirerTimerTask(), 10 * 1000, PERIOD_TIME);
	}

 /**
	 * 判断是服务器的系统类型是Windows 还是 Linux
	 * 
	 * @return
	 */
	public String isWindowsOrLinux() {
		String osName = System.getProperty("os.name");
		String sysName = "";
		if (osName.toLowerCase().startsWith("windows")) {
			sysName = "windows";
		} else if (osName.toLowerCase().startsWith("linux")) {
			sysName = "linux";
		}
		return sysName;
	}

 /**
	 * 获取JVM 的CPU占用率（%）
	 * 
	 * @return
	 */
	public String getCPURate() {
		String cpuRate = "";
		if (isWindowsOrLinux.equals("windows")) { // 判断操作系统类型是否为：windows
			cpuRate = getCPURateForWindows();
		} else {
			cpuRate = getCPURateForLinux();
		}
		return cpuRate;
	}


 
 /**
	 * 获取windows环境下JVM的cpu占用率
	 * 
	 * @return
	 */
	public String getCPURateForWindows() {
		try {
			String procCmd = System.getenv("windir") + "\\system32\\wbem\\wmic.exe  process "
					+ "  get Caption,CommandLine,KernelModeTime,ReadOperationCount,ThreadCount,UserModeTime,WriteOperationCount";
			// 取进程信息
			long[] c0 = readCpu(Runtime.getRuntime().exec(procCmd));
			Thread.sleep(CPUTIME);
			long[] c1 = readCpu(Runtime.getRuntime().exec(procCmd));
			if (c0 != null && c1 != null) {
				long idletime = c1[0] - c0[0];
				long busytime = c1[1] - c0[1];
				long cpuRate = PERCENT * (busytime) / (busytime + idletime);
				if (cpuRate > 100) {
					cpuRate = 100;
				} else if (cpuRate < 0) {
					cpuRate = 0;
				}
				return String.valueOf(PERCENT * (busytime) / (busytime + idletime));

 } else {
				return "0.0";
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return "0.0";
		}
	}

 /**
	 * 获取linux环境下JVM的cpu占用率
	 * 
	 * @return
	 */
	public String getCPURateForLinux() {
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader brStat = null;
		StringTokenizer tokenStat = null;
		String user = "";
		String linuxVersion = System.getProperty("os.version");
		try {
			System.out.println("Linux版本: " + linuxVersion);

 Process process = Runtime.getRuntime().exec(new String[] { "sh", "-c", "top -b -p " + pid });
			try {
				// top命令默认3秒动态更新结果信息，让线程睡眠5秒以便获取最新结果
				Thread.sleep(CPUTIME);
				is = process.getInputStream();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			isr = new InputStreamReader(is);
			brStat = new BufferedReader(isr);

 if (linuxVersion.equals("2.4")) {
				brStat.readLine();
				brStat.readLine();
				brStat.readLine();
				brStat.readLine();

 tokenStat = new StringTokenizer(brStat.readLine());
				tokenStat.nextToken();
				tokenStat.nextToken();
				user = tokenStat.nextToken();
				tokenStat.nextToken();
				String system = tokenStat.nextToken();
				tokenStat.nextToken();
				String nice = tokenStat.nextToken();

System.out.println(user + " , " + system + " , " + nice);

 user = user.substring(0, user.indexOf("%"));
				system = system.substring(0, system.indexOf("%"));
				nice = nice.substring(0, nice.indexOf("%"));

 float userUsage = new Float(user).floatValue();
				float systemUsage = new Float(system).floatValue();
				float niceUsage = new Float(nice).floatValue();
				return String.valueOf((userUsage + systemUsage + niceUsage) / 100);
			} else {
				brStat.readLine();
				brStat.readLine();
				brStat.readLine();
				brStat.readLine();
				brStat.readLine();
				brStat.readLine();
				brStat.readLine();
				brStat.readLine();
				brStat.readLine();
				brStat.readLine();
				brStat.readLine();
				brStat.readLine();
				tokenStat = new StringTokenizer(brStat.readLine());
				tokenStat.nextToken();
				String userUsage = tokenStat.nextToken(); // 用户空间占用CPU百分比
				user = userUsage.substring(0, userUsage.indexOf("%"));
				process.destroy();
			}

 } catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			freeResource(is, isr, brStat);
			return "100";
		} finally {
			freeResource(is, isr, brStat);
		}
		return user; // jvm cpu占用率
	}

 private void freeResource(InputStream is, InputStreamReader isr, BufferedReader br) {
		try {
			if (is != null)
				is.close();
			if (isr != null)
				isr.close();
			if (br != null)
				br.close();
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
	}

 /**
	 * 
	 * 读取CPU信息
	 * 
	 * @param proc
	 * @return
	 * 
	 */
	private long[] readCpu(final Process proc) {
		long[] retn = new long[2];
		try {
			proc.getOutputStream().close();
			InputStreamReader ir = new InputStreamReader(proc.getInputStream());
			LineNumberReader input = new LineNumberReader(ir);
			String line = input.readLine();
			if (line == null || line.length() < FAULTLENGTH) {
				return null;
			}
			int capidx = line.indexOf("Caption");
			int cmdidx = line.indexOf("CommandLine");
			int rocidx = line.indexOf("ReadOperationCount");
			int umtidx = line.indexOf("UserModeTime");
			int kmtidx = line.indexOf("KernelModeTime");
			int wocidx = line.indexOf("WriteOperationCount");
			// Caption,CommandLine,KernelModeTime,ReadOperationCount,ThreadCount,UserModeTime,WriteOperationCount
			long idletime = 0;
			long kneltime = 0;
			long usertime = 0;
			while ((line = input.readLine()) != null) {
				if (line.length() < wocidx) {
					continue;
				}
				// 字段出现顺序：Caption,CommandLine,KernelModeTime,ReadOperationCount,
				// ThreadCount,UserModeTime,WriteOperation
				String caption = this.substring(line, capidx, cmdidx - 1).trim();
				String cmd = this.substring(line, cmdidx, kmtidx - 1).trim();
				if (cmd.indexOf("javaw.exe") >= 0) {
					continue;
				}
				// log.info("line="+line);
				if (caption.equals("System Idle Process") || caption.equals("System")) {
					idletime += Long.valueOf(this.substring(line, kmtidx, rocidx - 1).trim()).longValue();
					idletime += Long.valueOf(this.substring(line, umtidx, wocidx - 1).trim()).longValue();
					continue;
				}

 kneltime += Long.valueOf(this.substring(line, kmtidx, rocidx - 1).trim()).longValue();
				usertime += Long.valueOf(this.substring(line, umtidx, wocidx - 1).trim()).longValue();
			}
			retn[0] = idletime;
			retn[1] = kneltime + usertime;
			return retn;
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				proc.getInputStream().close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

 
 /**
	 * 由于String.subString对汉字处理存在问题（把一个汉字视为一个字节)，因此在 包含汉字的字符串时存在隐患，现调整如下：
	 * 
	 * @param src
	 *            要截取的字符串
	 * @param start_idx
	 *            开始坐标（包括该坐标)
	 * @param end_idx
	 *            截止坐标（包括该坐标）
	 * @return
	 */
	private String substring(String src, int start_idx, int end_idx) {
		byte[] b = src.getBytes();
		String tgt = "";
		for (int i = start_idx; i <= end_idx; i++) {
			tgt += (char) b[i];
		}
		return tgt;
	}

 /**
	 * 格式化浮点数(float 和 double)，保留两位小数
	 * 
	 * @param obj
	 * @return
	 */
	private String formatNumber(Object obj) {
		String result = "";
		if (obj.getClass().getSimpleName().equals("Float")) {
			result = new Formatter().format("%.2f", Float.parseFloat(obj+"")).toString();
		} else if (obj.getClass().getSimpleName().equals("Double")) {
			result = new Formatter().format("%.2f", Double.parseDouble(obj+"")).toString();
		}
		return result;
	}


class SysInfoAcquirerTimerTask extends TimerTask {

 @Override
		public void run() {
			try {
				System.out.println("任务开始：");
				System.out.println("CPU使用率："+getCpu());
				long startTime = System.currentTimeMillis();
				String cpuRate = getCPURate(); // CPU使用率
				//System.out.println("JVM  PID：" + pid);
				System.out.println("JVM的CPU使用率：" + cpuRate + "%");
				//后续操作为将采集到的数据存放到数据库，可自行设计
				long endTime = System.currentTimeMillis();
				System.out.println("任务总耗时：" + (endTime - startTime) / (1000 * 60) + "分钟");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
}

}
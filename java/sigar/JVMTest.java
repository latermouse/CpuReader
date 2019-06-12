package sigar;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Random;

import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

public class JVMTest {
	public static void main(String[] args) throws SigarException {

		final Random rand = new Random();

		new Thread() {

			public void run() {

				while (true) {

					if (rand.nextBoolean()) {

						for (int i = 0, l = 10000000; i < l; i++) {

							rand.nextDouble();

						}

					} else {

						try {

							Thread.sleep(1000L);

						} catch (InterruptedException e) {

							e.printStackTrace();

						}

					}

				}

			}

		}.start();

		NumberFormat format = new DecimalFormat("0");

		Sigar sigar = new Sigar();

		RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();

		String pid = runtime.getName().split("@")[0]; // javaw的pid，windows可用，linux没测
														
		System.out.println(pid);

		int availableProcessors = Runtime.getRuntime().availableProcessors();

		while (true) {

			System.out.println(format.format(sigar.getProcCpu(pid).getPercent()
					/ availableProcessors * 100) + '%');

			try {

				Thread.sleep(1000L);

			} catch (InterruptedException e) {

				e.printStackTrace();

			}

		}

	}

}

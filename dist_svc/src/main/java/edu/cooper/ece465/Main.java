package edu.cooper.ece465;

import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import edu.cooper.ece465.zk.listeners.SpringAppEventsListener;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;

/** @author "Rob Marano" 2023/11/27 */
@SpringBootApplication(scanBasePackages={"edu.cooper.ece465"})
//@EnableAutoConfiguration()
public class Main {
    static String PID_FILE_NAME = "zkDistApp";
    @Getter(AccessLevel.PUBLIC)
    final long pid;

    public Main() {
        this.pid = this.fetchPid();
        try {
            writePidToLocalFile(PID_FILE_NAME + "." + this.pid+".pid", this.pid);
        } catch (IOException ex) {
            System.err.println("Cannot write pid file for this pid = " + this.pid);
        }
    }

    public static void main(String[] args) {
//        SpringApplication.run(Application.class, args);
        SpringApplication application = new SpringApplication(Main.class);
        application.addListeners(new SpringAppEventsListener());
        application.run(args);
        System.out.println("Application started");
        System.out.println("Application pid file location = " + System.getProperty("user.dir") + "/" + PID_FILE_NAME + "." + new Main().pid + ".pid");
    }

    public long fetchPid() {
        String processName = ManagementFactory.getRuntimeMXBean().getName();
        return Long.parseLong(processName.split("@")[0]);
    }

    public static void writePidToLocalFile(String fileName, final long pid) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        writer.write(String.format("%d", pid));
        writer.close();
    }
}
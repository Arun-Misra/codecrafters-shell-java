import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    static class Job {
        int id;
        Process process;
        String command;
        // boolean doneShown = false;

        Job(int id, Process process, String command) {
            this.id = id;
            this.process = process;
            this.command = command;
        }
    }

    public static void main(String[] args) throws Exception {
        Scanner s = new Scanner(System.in);

        String[] pth = System.getenv("PATH").split(File.pathSeparator);
        File currentDir = new File(System.getProperty("user.dir"));
        int nextJobId = 1;
        List<Job> jobs = new ArrayList<>();
        while (true) {
            reapJobs(jobs);
            System.out.print("$ ");
            String cmd = s.nextLine();
            List<String> parts = parse(cmd);
            String outputFile = null;
            boolean appendOutput = false;
            String errorFile = null;
            boolean appendError = false;

            for (int i = 0; i < parts.size(); i++) {
                String token = parts.get(i);
                if (token.equals(">") || token.equals("1>")) {
                    outputFile = parts.get(i + 1);
                    appendOutput = false;
                    parts = new ArrayList<>(parts.subList(0, i));
                    break;
                }

                if (token.equals(">>") || token.equals("1>>")) {
                    outputFile = parts.get(i + 1);
                    appendOutput = true;
                    parts = new ArrayList<>(parts.subList(0, i));
                    break;
                }

                if (token.equals("2>")) {
                    errorFile = parts.get(i + 1);
                    appendError = false;
                    parts = new ArrayList<>(parts.subList(0, i));
                    break;
                }

                if (token.equals("2>>")) {
                    errorFile = parts.get(i + 1);
                    appendError = true;
                    parts = new ArrayList<>(parts.subList(0, i));
                    break;
                }
            }
            boolean background = false;

            if (!parts.isEmpty() && parts.get(parts.size() - 1).equals("&")) {
                background = true;
                parts.remove(parts.size() - 1);
            }
            if (parts.isEmpty()) {
                continue;
            }

            if (cmd.equals("exit")) {
                break;
            }

            else if (parts.get(0).equals("pwd")) {
                System.out.println(currentDir.getAbsolutePath());
            }

            else if (parts.get(0).equals("cd")) {
                if (parts.size() < 2) {
                    continue;
                }

                String path = parts.get(1);
                File newDir;

                if (path.equals("~")) {
                    newDir = new File(System.getenv("HOME"));
                } else if (new File(path).isAbsolute()) {
                    newDir = new File(path);
                } else {
                    newDir = new File(currentDir, path);
                }

                if (newDir.exists() && newDir.isDirectory()) {
                    currentDir = newDir.getCanonicalFile();
                } else {
                    System.out.println("cd: " + path + ": No such file or directory");
                }
            }

            else if (parts.get(0).equals("echo")) {

                if (errorFile != null) {
                    if (appendError) {
                        Files.writeString(
                                Path.of(errorFile),
                                "",
                                java.nio.file.StandardOpenOption.CREATE,
                                java.nio.file.StandardOpenOption.APPEND);
                    } else {
                        Files.writeString(Path.of(errorFile), "");
                    }
                }
                StringBuilder sb = new StringBuilder();

                for (int i = 1; i < parts.size(); i++) {
                    if (i > 1)
                        sb.append(" ");
                    sb.append(parts.get(i));
                }

                String out = sb.toString();

                if (outputFile == null) {
                    System.out.println(out);
                } else {
                    if (appendOutput) {
                        Files.writeString(
                                Path.of(outputFile),
                                out + System.lineSeparator(),
                                java.nio.file.StandardOpenOption.CREATE,
                                java.nio.file.StandardOpenOption.APPEND);
                    } else {
                        Files.writeString(
                                Path.of(outputFile),
                                out + System.lineSeparator());
                    }
                }
            }

            else if (parts.get(0).equals("type")) {
                if (parts.size() < 2) {
                    continue;
                }

                String chk = parts.get(1);

                if (chk.equals("exit") || chk.equals("echo") || chk.equals("type") || chk.equals("pwd")
                        || chk.equals("cd") || chk.equals("jobs")) {
                    System.out.println(chk + " is a shell builtin");
                } else {
                    boolean find = false;
                    for (String dir : pth) {
                        File f = new File(dir, chk);

                        if (f.exists() && f.canExecute()) {
                            System.out.println(chk + " is " + f.getAbsolutePath());
                            find = true;
                            break;
                        }
                    }
                    if (!find) {
                        System.out.println(chk + ": not found");
                    }
                }
            }

            else if (parts.get(0).equals("jobs")) {

                List<Job> doneJobs = new ArrayList<>();

                int last = jobs.size() - 1;
                int secondLast = jobs.size() - 2;

                for (int i = 0; i < jobs.size(); i++) {
                    Job job = jobs.get(i);

                    char marker = ' ';
                    if (i == last)
                        marker = '+';
                    else if (i == secondLast)
                        marker = '-';

                    if (job.process.isAlive()) {

                        System.out.printf(
                                "[%d]%c  %-24s%s%n",
                                job.id,
                                marker,
                                "Running",
                                job.command);

                    } else {

                        String cmdText = job.command;
                        if (cmdText.endsWith(" &")) {
                            cmdText = cmdText.substring(0, cmdText.length() - 2);
                        }

                        System.out.printf(
                                "[%d]%c  %-24s%s%n",
                                job.id,
                                marker,
                                "Done",
                                cmdText);

                        doneJobs.add(job);
                    }
                }

                jobs.removeAll(doneJobs);
            }

            else {
                String prog = parts.get(0);

                File exe = null;

                for (String dir : pth) {
                    File f = new File(dir, prog);

                    if (f.exists() && f.canExecute()) {
                        exe = f;
                        break;
                    }
                }

                if (exe != null) {
                    ProcessBuilder pb = new ProcessBuilder(parts);
                    pb.directory(currentDir);

                    pb.redirectError(ProcessBuilder.Redirect.INHERIT);

                    if (outputFile != null) {
                        if (appendOutput) {
                            pb.redirectOutput(ProcessBuilder.Redirect.appendTo(new File(outputFile)));
                        } else {
                            pb.redirectOutput(new File(outputFile));
                        }
                    } else {
                        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                    }
                    if (errorFile != null) {
                        if (appendError) {
                            pb.redirectError(
                                    ProcessBuilder.Redirect.appendTo(new File(errorFile)));
                        } else {
                            pb.redirectError(new File(errorFile));
                        }
                    } else {
                        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
                    }

                    Process p = pb.start();

                    if (background) {
                        int jobId = nextJobId++;

                        jobs.add(new Job(jobId, p, cmd));

                        System.out.println("[" + jobId + "] " + p.pid());
                    } else {
                        p.waitFor();
                    }
                } else {
                    System.out.println(cmd + ": command not found");
                }
            }

        }
    }

    static void reapJobs(List<Job> jobs) {
        List<Job> doneJobs = new ArrayList<>();

        for (Job job : jobs) {
            if (!job.process.isAlive()) {
                doneJobs.add(job);
            }
        }

        int last = jobs.size() - 1;
        int secondLast = jobs.size() - 2;

        for (int i = 0; i < jobs.size(); i++) {
            Job job = jobs.get(i);

            if (!doneJobs.contains(job))
                continue;

            char marker = ' ';
            if (i == last)
                marker = '+';
            else if (i == secondLast)
                marker = '-';

            String cmdText = job.command;
            if (cmdText.endsWith(" &")) {
                cmdText = cmdText.substring(0, cmdText.length() - 2);
            }

            System.out.printf(
                    "[%d]%c  %-24s%s%n",
                    job.id,
                    marker,
                    "Done",
                    cmdText);
        }

        jobs.removeAll(doneJobs);
    }

    static List<String> parse(String s) {
        List<String> args = new ArrayList<>();
        StringBuilder cur = new StringBuilder();

        boolean inSingle = false;
        boolean inDouble = false;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (inSingle) {
                if (c == '\'') {
                    inSingle = false;
                } else {
                    cur.append(c);
                }
            }

            else if (inDouble) {
                if (c == '"') {
                    inDouble = false;
                } else if (c == '\\') {
                    if (i + 1 < s.length()) {
                        char nxt = s.charAt(i + 1);
                        if (nxt == '"' || nxt == '\\') {
                            cur.append(nxt);
                            i++;
                        } else {
                            cur.append('\\');
                        }
                    } else {
                        cur.append('\\');
                    }
                } else {
                    cur.append(c);
                }
            }

            else {
                if (c == '\'') {
                    inSingle = true;
                } else if (c == '"') {
                    inDouble = true;
                } else if (c == '\\') {
                    if (i + 1 < s.length()) {
                        cur.append(s.charAt(++i));
                    }
                } else if (Character.isWhitespace(c)) {
                    if (cur.length() > 0) {
                        args.add(cur.toString());
                        cur.setLength(0);
                    }
                } else {
                    cur.append(c);
                }
            }
        }

        if (cur.length() > 0) {
            args.add(cur.toString());
        }

        return args;
    }
}
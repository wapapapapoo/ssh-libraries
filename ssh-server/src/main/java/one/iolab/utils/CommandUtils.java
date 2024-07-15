package one.iolab.utils;

import java.util.HashSet;
import java.util.Set;

import org.apache.sshd.server.command.CommandFactory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class CommandUtils {

    /**
     * 解析cli命令
     * 
     * @param command
     * @return
     */
    public static String[] parse(String command) {
        return CommandFactory.split(command).toArray(new String[0]);
    }

    /**
     * SCP协议命令
     */
    @Data
    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SCPArgs {
        // 生产/消费模式，对应-t/-f
        private SCPMode mode;
        // 是否递归，对应-r
        private boolean recursive;
        // 文件路径
        private String path;

        public enum SCPMode {
            T(true), // producer side
            F(false); // consumer side

            private boolean mode;

            private SCPMode(boolean mode) {
                this.mode = mode;
            }

            public String toString() {
                return this.mode ? "-t" : "-f";
            }
        }
    }

    public static SCPArgs parseSCP(String command) {
        command = command.trim();
        if (!command.startsWith("scp "))
            return null;
        return parseScpArgs(command.substring(4));
    }

    /**
     * 解析scp参数
     * 
     * @param command
     * @return
     */
    public static SCPArgs parseScpArgs(String command) {
        String[] args = parseScpFormatArgs(command);

        SCPArgs res = new SCPArgs();
        res.path = args[args.length - 1];

        boolean mode = false;
        for (String arg : args) {
            if (arg.equals("-r")) {
                res.setRecursive(true);
            } else if (arg.equals("-f")) {
                if (mode)
                    return null;
                res.setMode(SCPArgs.SCPMode.F);
                mode = true;
            } else if (arg.equals("-t")) {
                if (mode)
                    return null;
                res.setMode(SCPArgs.SCPMode.T);
                mode = true;
            }
        }

        return res;
    }

    /**
     * 解析scp格式的参数
     * 
     * scp格式的命令：
     * [-r] <-f|-t> `remote path`
     * <-rf|-rt> `remote path`
     * [-r] <-f|-t> -- `-remote path`
     * <-rf|-rt> -- `-remote path`
     * 
     * 即：opr前缀为`-`，路径前缀一般不为`-`，若路径前缀为`-`，则路径前有一个单独的`--`opr标识
     * 
     * @param command
     * @return
     */
    public static String[] parseScpFormatArgs(String command) {
        int len = command.length();
        int ptr = 0;

        Set<String> opr = new HashSet<String>();

        while (ptr < len) {
            if (command.charAt(ptr) == ' ') {
                ptr++;
                continue;
            }

            int curStrStart = ptr;
            String curStr = "";
            while (ptr < len && command.charAt(ptr) != ' ') {
                curStr += command.charAt(ptr);
                ptr++;
            }

            if (curStr.startsWith("-")) {
                if (curStr.equals("--"))
                    break;
                for (char ch : curStr.substring(1).toCharArray())
                    opr.add(String.format("-%c", ch));
            } else {
                ptr = curStrStart;
                break;
            }
        }

        int i = 0;
        String[] ret = new String[opr.size() + 1];
        for (String str : opr) {
            ret[i++] = str;
        }

        ret[i] = command.substring(ptr, len).trim();
        return ret;
    }
}

package fansirsqi.xposed.sesame.util;

import android.annotation.SuppressLint;
import android.os.Environment;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.Objects;

import fansirsqi.xposed.sesame.hook.Toast;


public class Files {

    @SuppressLint("StaticFieldLeak")

    private static final String TAG = Files.class.getSimpleName();

    /**
     * 配置文件夹名称
     */
    public static final String CONFIG_DIR_NAME = "sesame-TK";

    /**
     * 应用配置文件夹主路径
     */
    public static final File MAIN_DIR = getMainDir();

    /**
     * 配置文件夹路径
     */
    public static final File CONFIG_DIR = getConfigDir();

    /**
     * 日志文件夹路径
     */
    public static final File LOG_DIR = getLogDir();


    /**
     * 确保指定的目录存在且不是一个文件。
     * 如果目录是一个文件，则将其删除并创建新的目录。
     * 如果目录不存在，则创建该目录。
     *
     * @param directory 要确保的目录对应的File对象。
     */
    public static void ensureDir(File directory) {
        try {
            if (directory == null) {
                Log.error(TAG, "Directory cannot be null");
                return;
            }
            if (!directory.exists()) {
                if (!directory.mkdirs()) {
                    Log.error(TAG, "Failed to create directory: " + directory.getAbsolutePath());
                }
            } else if (directory.isFile()) {
                if (!directory.delete() || !directory.mkdirs()) {
                    Log.error(TAG, "Failed to replace file with directory: " + directory.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            Log.printStackTrace(TAG + " ensureDir error", e);
        }
    }


    /**
     * 获取配置文件夹主路径
     *
     * @return mainDir 主路径
     */
    private static File getMainDir() {
        String storageDirStr = Environment.getExternalStorageDirectory() + File.separator + "Android" + File.separator + "media" + File.separator + ClassUtil.PACKAGE_NAME;
        File storageDir = new File(storageDirStr);
        File mainDir = new File(storageDir, CONFIG_DIR_NAME);
        ensureDir(mainDir);
        return mainDir;
    }

    /**
     * 获取日志文件夹路径
     *
     * @return logDir 日志文件夹路径
     */
    private static File getLogDir() {
        File logDir = new File(MAIN_DIR, "log");
        ensureDir(logDir);
        return logDir.exists() ? logDir : null;
    }


    /**
     * 获取配置文件夹路径
     *
     * @return configDir 配置文件夹路径
     */
    private static File getConfigDir() {
        File configDir = new File(MAIN_DIR, "config");
        ensureDir(configDir);
        return configDir;
    }

    /**
     * 获取指定用户的配置文件夹路径。
     *
     * @param userId 用户ID
     */
    public static File getUserConfigDir(String userId) {
        File configDir = new File(CONFIG_DIR, userId);
        ensureDir(configDir);
        return configDir;
    }

    /**
     * 获取默认的配置文件
     *
     * @return configFile 默认配置文件
     */
    public static File getDefaultConfigV2File() {
        return new File(MAIN_DIR, "config_v2.json");
    }

    /**
     * 设置默认的配置文件
     *
     * @param json 新的配置文件内容
     */
    public static synchronized boolean setDefaultConfigV2File(String json) {
        return write2File(json, new File(MAIN_DIR, "config_v2.json"));
    }

    /**
     * 获取指定用户的配置文件
     *
     * @param userId 用户ID
     * @return 指定用户的配置文件
     */
    public static synchronized File getConfigV2File(String userId) {
        File confV2File = new File(CONFIG_DIR + File.separator + userId, "config_v2.json");
        // 如果新配置文件不存在，则尝试从旧配置文件迁移
        if (!confV2File.exists()) {
            File oldFile = new File(CONFIG_DIR, "config_v2-" + userId + ".json");
            if (oldFile.exists()) {
                String content = readFromFile(oldFile);
                if (write2File(content, confV2File)) {
                    if (!oldFile.delete()) {
                        Log.error(TAG, "Failed to delete old config file: " + oldFile.getAbsolutePath());
                    }
                } else {
                    confV2File = oldFile;
                    Log.error(TAG, "Failed to migrate config file for user: " + userId);
                }
            }
        }
        return confV2File;
    }

    public static synchronized boolean setConfigV2File(String userId, String json) {
        return write2File(json, new File(CONFIG_DIR + File.separator + userId, "config_v2.json"));
    }

    public static synchronized boolean setUIConfigFile(String json) {
        return write2File(json, new File(MAIN_DIR, "ui_config.json"));
    }

    public static File getTargetFileofUser(String userId, String fullTargerFileName) {
        File targetFile = new File(CONFIG_DIR + File.separator + userId, fullTargerFileName);
        if (!targetFile.exists() && !targetFile.isDirectory()) {
            if (targetFile.delete()) {
                Log.runtime(TAG, targetFile.getName() + "[old] delete success");
            } else {
                Log.runtime(TAG, targetFile.getName() + "[old] delete failed");
            }
        }
        if (!targetFile.exists()) {
            try {
                if (targetFile.createNewFile()) {
                    Log.runtime(TAG, targetFile.getName() + "create success");
                } else {
                    Log.runtime(TAG, targetFile.getName() + "create failed");
                }
            } catch (Throwable ignored) {
            }
        } else {
            Log.system(TAG, fullTargerFileName+" permission: r;w" + targetFile.canRead() + ";" + targetFile.canWrite());
        }
        return targetFile;
    }

    public static File getTargetFileofDir(File dir, String fullTargerFileName) {
        File targetFile = new File(dir, fullTargerFileName);
        if (!targetFile.exists() && !targetFile.isDirectory()) {
            if (targetFile.delete()) {
                Log.runtime(TAG, targetFile.getName() + "[old] delete success");
            } else {
                Log.runtime(TAG, targetFile.getName() + "[old] delete failed");
            }
        }
        if (!targetFile.exists()) {
            try {
                if (targetFile.createNewFile()) {
                    Log.runtime(TAG, targetFile.getName() + "create success");
                } else {
                    Log.runtime(TAG, targetFile.getName() + "create failed");
                }
            } catch (Throwable ignored) {
            }
        } else {
            Log.system(TAG, "permission: r;w" + targetFile.canRead() + ";" + targetFile.canWrite());
        }
        return targetFile;
    }

    public static File getSelfIdFile(String userId) {
        return getTargetFileofUser(userId, "self.json");
    }

    public static File getFriendIdMapFile(String userId) {
        return getTargetFileofUser(userId, "friend.json");
    }

    public static File runtimeInfoFile(String userId) {
        return getTargetFileofUser(userId, "runtime.json");
    }

    /**
     * 获取用户状态文件
     *
     * @param userId 用户ID
     * @return 用户状态文件
     */
    public static File getStatusFile(String userId) {
        return getTargetFileofUser(userId, "status.json");
    }

    /**
     * 获取统计文件
     */
    public static File getStatisticsFile() {
        return getTargetFileofDir(MAIN_DIR, "statistics.json");
    }


    public static File getUIConfigFile() {
        return getTargetFileofDir(MAIN_DIR, "ui_config.json");
    }

    /**
     * 获取已经导出的统计文件在载目录中
     *
     * @return 导出的统计文件
     */
    public static File getExportedStatisticsFile() {
        try {
            String storageDirStr = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + CONFIG_DIR_NAME;
            File storageDir = new File(storageDirStr);
            if (!storageDir.exists()) {
                if (storageDir.mkdirs()) {
                    Log.system(TAG, "create downloads's " + CONFIG_DIR_NAME + " directory success");
                } else {
                    Log.error(TAG, "create downloads's " + CONFIG_DIR_NAME + " directory failed");
                }
            }
            return getTargetFileofDir(storageDir, "statistics.json");
        } catch (Exception e) {
            Log.printStackTrace(TAG + "export statistics file error", e);
            return null;
        }
    }

    public static File getFriendWatchFile() {
        return getTargetFileofDir(MAIN_DIR, "friendWatch.json");
    }

    public static File getWuaFile() {
        return getTargetFileofDir(MAIN_DIR, "wua.list");
    }

    /**
     * 导出文件到下载目录
     *
     * @param file 要导出的文件
     * @return 导出后的文件
     */
    public static File exportFile(File file) {
        File exportDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), CONFIG_DIR_NAME);
        if (!exportDir.exists() && !exportDir.mkdirs()) {
            Log.error(TAG, "Failed to create export directory: " + exportDir.getAbsolutePath());
            return null;
        }
        File exportFile = new File(exportDir, file.getName());
        if (exportFile.exists() && exportFile.isDirectory()) {
            if (!exportFile.delete()) {
                // 如果删除失败，记录错误日志
                Log.error(TAG, "Failed to delete existing directory: " + exportFile.getAbsolutePath());
                return null;
            }
        }
        Files.copyTo(file, exportFile);
        return exportFile;
    }

    /**
     * 获取城市代码文件
     *
     * @return 城市代码文件
     */
    public static File getCityCodeFile() {
        File cityCodeFile = new File(MAIN_DIR, "cityCode.json");
        if (cityCodeFile.exists() && cityCodeFile.isDirectory()) {
            if (!cityCodeFile.delete()) {
                Log.error(TAG, "Failed to delete directory: " + cityCodeFile.getAbsolutePath());
            }
        }
        return cityCodeFile;
    }

    /**
     * 确保日志文件存在，如果文件是一个目录则删除并创建新文件。 如果文件不存在，则创建新文件。
     *
     * @param logFileName 日志文件的名称
     * @return 日志文件的File对象
     */
    private static File ensureLogFile(String logFileName) {
        File logFile = new File(Files.LOG_DIR, logFileName);
        if (logFile.exists() && logFile.isDirectory()) {
            if (logFile.delete()) {
                Log.system(TAG, "日志" + logFile.getName() + "目录存在，删除成功！");
            } else {
                Log.error(TAG, "日志" + logFile.getName() + "目录存在，删除失败！");
            }
        }
        if (!logFile.exists()) {
            try {
                if (logFile.createNewFile()) {
                    Log.system(TAG, "日志" + logFile.getName() + "文件不存在，创建成功！");
                } else {
                    Log.error(TAG, "日志" + logFile.getName() + "文件不存在，创建失败！");
                }
            } catch (IOException ignored) {
                // 忽略创建文件时可能出现的异常
            }
        }
        return logFile;
    }

    /**
     * 根据日志名称生成带有日期的日志文件名。
     *
     * @param logName 日志名称
     * @return 对应文件
     */
    public static String getLogFile(String logName) {
        return logName + ".log";
    }

    public static File getRuntimeLogFile() {
        return ensureLogFile(getLogFile("runtime"));
    }

    public static File getRecordLogFile() {
        return ensureLogFile(getLogFile("record"));
    }

    public static File getSystemLogFile() {
        return ensureLogFile(getLogFile("system"));
    }

    public static File getDebugLogFile() {
        return ensureLogFile(getLogFile("debug"));
    }

    public static File getCaptureLogFile() {
        return ensureLogFile(getLogFile("capture"));
    }

    public static File getForestLogFile() {
        return ensureLogFile(getLogFile("forest"));
    }

    public static File getFarmLogFile() {
        return ensureLogFile(getLogFile("farm"));
    }

    public static File getOtherLogFile() {
        return ensureLogFile(getLogFile("other"));
    }

    public static File getErrorLogFile() {
        return ensureLogFile(getLogFile("error"));
    }



    /**
     * 关闭流对象
     *
     * @param c 要关闭的流对象
     */
    public static void close(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                Log.printStackTrace(TAG, e); // 捕获并记录关闭流时的 IO 异常
            }
        }
    }


    /**
     * 从文件中读取内容
     *
     * @param f 要读取的文件
     * @return 文件内容，如果读取失败或没有权限，返回空字符串
     */
    public static String readFromFile(File f) {
        // 检查文件是否存在
        if (!f.exists()) {
            return "";
        }
        // 检查文件是否可读
        if (!f.canRead()) {
            //      Toast.show(f.getName() + "没有读取权限！", true);
            ToastUtil.showToast(f.getName() + "没有读取权限！");
            return "";
        }
        StringBuilder result = new StringBuilder();
        FileReader fr = null;
        try {
            // 使用 FileReader 读取文件内容
            fr = new FileReader(f);
            char[] chs = new char[1024];
            int len;
            // 按块读取文件内容
            while ((len = fr.read(chs)) >= 0) {
                result.append(chs, 0, len);
            }
        } catch (Throwable t) {
            // 捕获并记录异常
            Log.printStackTrace(TAG, t);
        } finally {
            // 关闭文件流
            close(fr);
        }
        return result.toString();
    }

    public static boolean beforWrite(File f) {
        // 检查文件权限和目录结构
        if (f.exists()) {
            if (!f.canWrite()) {
                ToastUtil.showToast(f.getAbsoluteFile() + "没有写入权限！");
                return false;
            }
            if (f.isDirectory()) {
                // 删除目录并重新创建文件
                if (!f.delete()) {
                    ToastUtil.showToast(f.getAbsoluteFile() + "无法删除目录！");
                    return false;
                }
            }
        } else {
            if (!Objects.requireNonNull(f.getParentFile()).mkdirs() && !f.getParentFile().exists()) {
                ToastUtil.showToast(f.getAbsoluteFile() + "无法创建目录！");
                return false;
            }
        }
        return true;
    }

    /**
     * 将字符串写入文件
     *
     * @param s 要写入的字符串
     * @param f 目标文件
     * @return 写入是否成功
     */
    public static synchronized boolean write2File(String s, File f) {
        if (!beforWrite(f)) return false;
        try {
            try (FileWriter fw = new FileWriter(f, false)) {
                fw.write(s);
                fw.flush();
                return true;
            }
        } catch (IOException e) {
            Log.printStackTrace(TAG, e);
            return false;
        }
    }

    public static synchronized boolean write2File(String s, File f,boolean isFormat) {
        if (!beforWrite(f)) return false;
        try {
            try (FileWriter fw = new FileWriter(f, false)) {
                if (isFormat) s = JsonUtil.formatJson(s);
                fw.write(s);
                fw.flush();
                return true;
            }
        } catch (IOException e) {
            Log.printStackTrace(TAG, e);
            return false;
        }
    }


    /**
     * 将字符串追加到文件末尾
     *
     * @param s 要追加的字符串
     * @param f 目标文件
     * @return 追加是否成功
     */
    public static boolean append2File(String s, File f) {
        // 文件已存在，检查是否有写入权限
        if (f.exists() && !f.canWrite()) {
            Toast.show(f.getAbsoluteFile() + "没有写入权限！");
            return false;
        }
        boolean success = false;
        FileWriter fw = null;
        try {
            // 使用 FileWriter 追加内容到文件末尾
            fw = new FileWriter(f, true);
            fw.append(s);
            fw.flush();
            success = true;
        } catch (Throwable t) {
            // 捕获并记录异常
            Log.printStackTrace(TAG, t);
        } finally {
            // 关闭文件流
            close(fw);
        }
        return success;
    }

    /**
     * 将源文件的内容复制到目标文件
     *
     * @param source 源文件
     * @param dest   目标文件
     * @return 如果复制成功返回 true，否则返回 false
     */
    public static boolean copyTo(File source, File dest) {
        // 使用 try-with-resources 来自动管理 FileInputStream 和 FileOutputStream 以及 FileChannel 的关闭
        try (FileInputStream fileInputStream = new FileInputStream(source);
             FileOutputStream fileOutputStream = new FileOutputStream(createFile(dest));
             FileChannel inputChannel = fileInputStream.getChannel();
             FileChannel outputChannel = fileOutputStream.getChannel()) {
            // 将源文件的内容传输到目标文件
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
            return true; // 复制成功
        } catch (IOException e) {
            // 捕获并打印文件操作中的异常
            Log.printStackTrace(e);
        }
        return false; // 复制失败
    }

    /**
     * 将输入流（source）中的数据拷贝到输出流（dest）中。 会循环读取输入流的数据并写入输出流，直到读取完毕。 最终关闭输入输出流。
     *
     * @param source 输入流
     * @param dest   输出流
     * @return 如果数据拷贝成功，返回 true；如果发生 IO 异常或拷贝失败，返回 false
     */
    public static boolean streamTo(InputStream source, OutputStream dest) {
        byte[] buffer = new byte[1024]; // 创建一个缓冲区，每次读取 1024 字节
        int length;

        try {
            // 循环读取输入流中的数据并写入输出流
            while ((length = source.read(buffer)) > 0) {
                dest.write(buffer, 0, length); // 写入数据到输出流
                dest.flush(); // 强制将数据从输出流刷新到目的地
            }
            return true; // 成功拷贝数据
        } catch (IOException e) {
            // 捕获 IO 异常并打印堆栈信息
            Log.printStackTrace(e);
        } finally {
            // 关闭输入流和输出流
            closeStream(source);
            closeStream(dest);
        }
        return false; // 拷贝失败或发生异常
    }

    /**
     * 关闭流并处理可能发生的异常
     *
     * @param stream 需要关闭的流对象
     */
    private static void closeStream(AutoCloseable stream) {
        if (stream != null) {
            try {
                stream.close(); // 关闭流
            } catch (Exception e) {
                // 捕获并打印关闭流时的异常
                Log.printStackTrace(e);
            }
        }
    }

    /**
     * 创建一个文件，如果文件已存在且是目录，
     * 则先删除该目录再创建文件。
     * 如果文件不存在，则会先创建父目录，再创建该文件。
     *
     * @param file 需要创建的文件对象
     * @return 创建成功返回文件对象；如果创建失败或发生异常，返回 null
     */
    public static File createFile(File file) {
        // 如果文件已存在且是目录，则先删除该目录
        if (file.exists() && file.isDirectory()) {
            // 如果删除目录失败，返回 null
            if (!file.delete()) return null;
        }
        // 如果文件不存在，则尝试创建文件
        if (!file.exists()) {
            try {
                // 获取父目录文件对象
                File parentFile = file.getParentFile();
                if (parentFile != null) {
                    // 如果父目录不存在，则创建父目录
                    boolean ignore = parentFile.mkdirs();
                }
                // 创建新的文件
                // 如果文件创建失败，返回 null
                if (!file.createNewFile()) return null;
            } catch (Exception e) {
                // 捕获异常并打印堆栈信息
                Log.printStackTrace(e);
                return null;
            }
        }
        // 文件已存在或成功创建，返回文件对象
        return file;
    }



    /**
     * 清空文件内容, 并返回是否清空成功
     *
     * @param file 文件
     * @return 是否清空成功
     */
    public static Boolean clearFile(File file) {
        // 检查文件是否存在
        if (file.exists()) {
            FileWriter fileWriter = null;
            try {
                // 使用 FileWriter 清空文件内容
                fileWriter = new FileWriter(file);
                fileWriter.write(""); // 写入空字符串，清空文件内容
                fileWriter.flush(); // 刷新缓存，确保内容写入文件
                return true; // 返回清空成功
            } catch (IOException e) {
                // 发生 IO 异常时打印堆栈信息
                Log.printStackTrace(e);
            } finally {
                // 确保 FileWriter 在操作完成后关闭，防止资源泄露
                try {
                    if (fileWriter != null) {
                        fileWriter.close(); // 关闭文件写入流
                    }
                } catch (IOException e) {
                    // 如果关闭流时发生异常，打印堆栈信息
                    Log.printStackTrace(e);
                }
            }
        }
        // 如果文件不存在，则返回 false
        return false;
    }

    /**
     * 删除文件或目录（包括子文件和子目录）。如果是目录，则递归删除其中的所有文件和目录。
     *
     * @param file 要删除的文件或目录
     * @return 如果删除成功返回 true，失败返回 false
     */
    public static Boolean delFile(File file) {
        // 如果文件或目录不存在，则返回删除失败
        if (!file.exists()) return false;

        // 如果是文件，直接删除文件
        if (file.isFile()) return file.delete();

        // 如果是目录，获取目录下的所有文件和子目录
        File[] files = file.listFiles();

        // 如果目录为空或无法列出文件，尝试删除目录
        if (files == null) return file.delete();

        // 遍历所有文件和子目录，递归调用 deleteFile 删除
        for (File innerFile : files) {
            // 如果递归删除失败，返回 false
            if (!delFile(innerFile)) return false;
        }

        // 删除空目录
        return file.delete();
    }


}

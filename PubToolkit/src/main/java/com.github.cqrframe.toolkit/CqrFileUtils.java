package com.github.cqrframe.toolkit;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.WorkerThread;
import android.webkit.MimeTypeMap;

import com.github.cqrframe.logger.CqrLog;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Pattern;

/**
 * 文件处理
 * <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
 * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
 * <p>
 * Created by liyujiang on 2014-4-18.
 */
@SuppressWarnings("WeakerAccess")
public class CqrFileUtils {

    @IntDef(value = {
            SortType.BY_NAME_ASC,
            SortType.BY_NAME_DESC,
            SortType.BY_TIME_ASC,
            SortType.BY_TIME_DESC,
            SortType.BY_SIZE_ASC,
            SortType.BY_SIZE_DESC,
            SortType.BY_EXTENSION_ASC,
            SortType.BY_EXTENSION_DESC
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface SortType {
        int BY_NAME_ASC = 0;
        int BY_NAME_DESC = 1;
        int BY_TIME_ASC = 2;
        int BY_TIME_DESC = 3;
        int BY_SIZE_ASC = 4;
        int BY_SIZE_DESC = 5;
        int BY_EXTENSION_ASC = 6;
        int BY_EXTENSION_DESC = 7;
    }

    protected CqrFileUtils() {
        throw new UnsupportedOperationException("You can't instantiate me");
    }

    /**
     * 将目录分隔符统一为平台默认的分隔符，并为目录结尾添加分隔符
     */
    public static String separator(String path) {
        String separator = File.separator;
        path = path.replace("\\", separator);
        if (!path.endsWith(separator)) {
            path += separator;
        }
        return path;
    }

    public static void closeSilently(Closeable c) {
        if (c == null) {
            return;
        }
        try {
            c.close();
        } catch (IOException e) {
            // do nothing
            CqrLog.debug(e);
        }
    }

    /**
     * 列出指定目录下的所有子目录
     */
    public static File[] listDirs(String startDirPath, String[] excludeDirs, @SortType int sortType) {
        CqrLog.debug(String.format("list dir %s", startDirPath));
        ArrayList<File> dirList = new ArrayList<File>();
        File startDir = new File(startDirPath);
        if (!startDir.isDirectory()) {
            return new File[0];
        }
        File[] dirs = startDir.listFiles(new FileFilter() {
            public boolean accept(File f) {
                if (f == null) {
                    return false;
                }
                //noinspection RedundantIfStatement
                if (f.isDirectory()) {
                    return true;
                }
                return false;
            }
        });
        if (dirs == null) {
            return new File[0];
        }
        if (excludeDirs == null) {
            excludeDirs = new String[0];
        }
        for (File dir : dirs) {
            File file = dir.getAbsoluteFile();
            if (!CqrConvertUtils.toString(excludeDirs).contains(file.getName())) {
                dirList.add(file);
            }
        }
        if (sortType == SortType.BY_NAME_ASC) {
            Collections.sort(dirList, new SortByName());
        } else if (sortType == SortType.BY_NAME_DESC) {
            Collections.sort(dirList, new SortByName());
            Collections.reverse(dirList);
        } else if (sortType == SortType.BY_TIME_ASC) {
            Collections.sort(dirList, new SortByTime());
        } else if (sortType == SortType.BY_TIME_DESC) {
            Collections.sort(dirList, new SortByTime());
            Collections.reverse(dirList);
        } else if (sortType == SortType.BY_SIZE_ASC) {
            Collections.sort(dirList, new SortBySize());
        } else if (sortType == SortType.BY_SIZE_DESC) {
            Collections.sort(dirList, new SortBySize());
            Collections.reverse(dirList);
        } else if (sortType == SortType.BY_EXTENSION_ASC) {
            Collections.sort(dirList, new SortByExtension());
        } else if (sortType == SortType.BY_EXTENSION_DESC) {
            Collections.sort(dirList, new SortByExtension());
            Collections.reverse(dirList);
        }
        return dirList.toArray(new File[dirList.size()]);
    }

    /**
     * 列出指定目录下的所有子目录
     */
    public static File[] listDirs(String startDirPath, String[] excludeDirs) {
        return listDirs(startDirPath, excludeDirs, SortType.BY_NAME_ASC);
    }

    /**
     * 列出指定目录下的所有子目录
     */
    public static File[] listDirs(String startDirPath) {
        return listDirs(startDirPath, null, SortType.BY_NAME_ASC);
    }

    /**
     * 列出指定目录下的所有子目录及所有文件
     */
    public static File[] listDirsAndFiles(String startDirPath, String[] allowExtensions) {
        File[] dirs, files, dirsAndFiles;
        dirs = listDirs(startDirPath);
        if (allowExtensions == null) {
            files = listFiles(startDirPath);
        } else {
            files = listFiles(startDirPath, allowExtensions);
        }
        if (dirs == null || files == null) {
            return null;
        }
        dirsAndFiles = new File[dirs.length + files.length];
        System.arraycopy(dirs, 0, dirsAndFiles, 0, dirs.length);
        System.arraycopy(files, 0, dirsAndFiles, dirs.length, files.length);
        return dirsAndFiles;
    }

    /**
     * 列出指定目录下的所有子目录及所有文件
     */
    public static File[] listDirsAndFiles(String startDirPath) {
        return listDirsAndFiles(startDirPath, null);
    }

    /**
     * 列出指定目录下的所有文件
     */
    public static File[] listFiles(String startDirPath, final Pattern filterPattern, @SortType int sortType) {
        CqrLog.debug(String.format("list file %s", startDirPath));
        ArrayList<File> fileList = new ArrayList<File>();
        File f = new File(startDirPath);
        if (!f.isDirectory()) {
            return new File[0];
        }
        File[] files = f.listFiles(new FileFilter() {
            public boolean accept(File f) {
                if (f == null) {
                    return false;
                }
                if (f.isDirectory()) {
                    return false;
                }
                //noinspection SimplifiableIfStatement
                if (filterPattern == null) {
                    return true;
                }
                return filterPattern.matcher(f.getName()).find();
            }
        });
        if (files == null) {
            return new File[0];
        }
        for (File file : files) {
            fileList.add(file.getAbsoluteFile());
        }
        if (sortType == SortType.BY_NAME_ASC) {
            Collections.sort(fileList, new SortByName());
        } else if (sortType == SortType.BY_NAME_DESC) {
            Collections.sort(fileList, new SortByName());
            Collections.reverse(fileList);
        } else if (sortType == SortType.BY_TIME_ASC) {
            Collections.sort(fileList, new SortByTime());
        } else if (sortType == SortType.BY_TIME_DESC) {
            Collections.sort(fileList, new SortByTime());
            Collections.reverse(fileList);
        } else if (sortType == SortType.BY_SIZE_ASC) {
            Collections.sort(fileList, new SortBySize());
        } else if (sortType == SortType.BY_SIZE_DESC) {
            Collections.sort(fileList, new SortBySize());
            Collections.reverse(fileList);
        } else if (sortType == SortType.BY_EXTENSION_ASC) {
            Collections.sort(fileList, new SortByExtension());
        } else if (sortType == SortType.BY_EXTENSION_DESC) {
            Collections.sort(fileList, new SortByExtension());
            Collections.reverse(fileList);
        }
        return fileList.toArray(new File[fileList.size()]);
    }

    /**
     * 列出指定目录下的所有文件
     */
    public static File[] listFiles(String startDirPath, Pattern filterPattern) {
        return listFiles(startDirPath, filterPattern, SortType.BY_NAME_ASC);
    }

    /**
     * 列出指定目录下的所有文件
     */
    public static File[] listFiles(String startDirPath) {
        return listFiles(startDirPath, null, SortType.BY_NAME_ASC);
    }

    /**
     * 列出指定目录下的所有文件
     */
    public static File[] listFiles(String startDirPath, final String[] allowExtensions) {
        CqrLog.debug(String.format("list file %s", startDirPath));
        File file = new File(startDirPath);
        return file.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                //返回当前目录所有以某些扩展名结尾的文件
                String extension = obtainExtension(name);
                return CqrConvertUtils.toString(allowExtensions).contains(extension);
            }

        });
    }

    /**
     * 列出指定目录下的所有文件
     */
    public static File[] listFiles(String startDirPath, String allowExtension) {
        return listFiles(startDirPath, new String[]{allowExtension});
    }

    /**
     * 判断文件或目录是否存在
     */
    public static boolean exist(String path) {
        File file = new File(path);
        return file.exists();
    }

    /**
     * 删除文件或目录
     */
    public static boolean delete(File file, boolean deleteRootDir) {
        CqrLog.debug(String.format("delete file %s", file.getAbsolutePath()));
        boolean result = false;
        if (file.isFile()) {
            //是文件
            result = deleteResolveEBUSY(file);
        } else {
            //是目录
            File[] files = file.listFiles();
            if (files == null) {
                return false;
            }
            if (files.length == 0) {
                result = deleteRootDir && deleteResolveEBUSY(file);
            } else {
                for (File f : files) {
                    delete(f, deleteRootDir);
                    result = deleteResolveEBUSY(f);
                }
            }
            if (deleteRootDir) {
                result = deleteResolveEBUSY(file);
            }
        }
        return result;
    }

    /**
     * bug: open failed: EBUSY (Device or resource busy)
     * fix: http://stackoverflow.com/questions/11539657/open-failed-ebusy-device-or-resource-busy
     */
    private static boolean deleteResolveEBUSY(File file) {
        // Before you delete a Directory or File: rename it!
        final File to = new File(file.getAbsolutePath() + System.currentTimeMillis());
        //noinspection ResultOfMethodCallIgnored
        file.renameTo(to);
        return to.delete();
    }

    /**
     * 删除文件或目录
     */
    public static boolean delete(String path, boolean deleteRootDir) {
        File file = new File(path);
        //noinspection SimplifiableIfStatement
        if (file.exists()) {
            return delete(file, deleteRootDir);
        }
        return false;
    }

    /**
     * 删除文件或目录, 不删除最顶层目录
     */
    public static boolean delete(String path) {
        return delete(path, false);
    }

    /**
     * 删除文件或目录, 不删除最顶层目录
     */
    public static boolean delete(File file) {
        return delete(file, false);
    }

    /**
     * 复制文件为另一个文件，或复制某目录下的所有文件及目录到另一个目录下
     */
    public static boolean copy(String src, String tar) {
        File srcFile = new File(src);
        return srcFile.exists() && copy(srcFile, new File(tar));
    }

    /**
     * 复制文件或目录
     */
    public static boolean copy(File src, File tar) {
        try {
            CqrLog.debug(String.format("copy %s to %s", src.getAbsolutePath(), tar.getAbsolutePath()));
            if (src.isFile()) {
                InputStream is = new FileInputStream(src);
                OutputStream op = new FileOutputStream(tar);
                BufferedInputStream bis = new BufferedInputStream(is);
                BufferedOutputStream bos = new BufferedOutputStream(op);
                byte[] bt = new byte[1024 * 8];
                while (true) {
                    int len = bis.read(bt);
                    if (len == -1) {
                        break;
                    } else {
                        bos.write(bt, 0, len);
                    }
                }
                bis.close();
                bos.close();
            } else if (src.isDirectory()) {
                File[] files = src.listFiles();
                //noinspection ResultOfMethodCallIgnored
                tar.mkdirs();
                for (File file : files) {
                    copy(file.getAbsoluteFile(), new File(tar.getAbsoluteFile(), file.getName()));
                }
            }
            return true;
        } catch (Exception e) {
            CqrLog.error(e);
            return false;
        }
    }

    /**
     * 移动文件或目录
     */
    public static boolean move(String src, String tar) {
        return move(new File(src), new File(tar));
    }

    /**
     * 移动文件或目录
     */
    public static boolean move(File src, File tar) {
        return rename(src, tar);
    }

    /**
     * 文件重命名
     */
    public static boolean rename(String oldPath, String newPath) {
        return rename(new File(oldPath), new File(newPath));
    }

    /**
     * 文件重命名
     */
    public static boolean rename(File src, File tar) {
        CqrLog.debug(String.format("rename %s to %s", src.getAbsolutePath(), tar.getAbsolutePath()));
        return src.renameTo(tar);
    }

    /**
     * 读取文本文件, 失败将返回空串
     */
    public static String readText(String filepath, String charset) {
        CqrLog.debug(String.format("read %s use %s", filepath, charset));
        try {
            byte[] data = readBytes(filepath);
            if (data != null) {
                return new String(data, charset).trim();
            }
        } catch (UnsupportedEncodingException e) {
            CqrLog.debug(e);
        }
        return "";
    }

    /**
     * 读取文本文件, 失败将返回空串
     */
    public static String readText(String filepath) {
        return readText(filepath, "utf-8");
    }

    /**
     * 读取文件内容, 失败将返回空串
     */
    public static byte[] readBytes(String filepath) {
        CqrLog.debug(String.format("read %s", filepath));
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(filepath);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            while (true) {
                int len = fis.read(buffer, 0, buffer.length);
                if (len == -1) {
                    break;
                } else {
                    baos.write(buffer, 0, len);
                }
            }
            byte[] data = baos.toByteArray();
            baos.close();
            return data;
        } catch (IOException e) {
            CqrLog.debug(e);
            return null;
        } finally {
            closeSilently(fis);
        }
    }

    /**
     * 保存文本内容
     */
    public static boolean writeText(String filepath, String content, String charset) {
        try {
            writeBytes(filepath, content.getBytes(charset));
            return true;
        } catch (UnsupportedEncodingException e) {
            CqrLog.debug(e);
            return false;
        }
    }

    /**
     * 保存文本内容
     */
    public static boolean writeText(String filepath, String content) {
        return writeText(filepath, content, "utf-8");
    }

    /**
     * 保存文件内容
     */
    public static boolean writeBytes(String filepath, byte[] data) {
        CqrLog.debug(String.format("write %s", filepath));
        File file = new File(filepath);
        FileOutputStream fos = null;
        try {
            if (!file.exists()) {
                //noinspection ResultOfMethodCallIgnored
                file.getParentFile().mkdirs();
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            }
            fos = new FileOutputStream(filepath);
            fos.write(data);
            return true;
        } catch (IOException e) {
            CqrLog.debug(e);
            return false;
        } finally {
            closeSilently(fos);
        }
    }

    /**
     * 追加文本内容
     */
    public static boolean appendText(String path, String content) {
        CqrLog.debug(String.format("append %s", path));
        File file = new File(path);
        FileWriter writer = null;
        try {
            if (!file.exists()) {
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            }
            writer = new FileWriter(file, true);
            writer.write(content);
            return true;
        } catch (IOException e) {
            CqrLog.debug(e);
            return false;
        } finally {
            closeSilently(writer);
        }
    }

    /**
     * 获取文件大小
     */
    public static long obtainLength(String path) {
        File file = new File(path);
        if (!file.isFile() || !file.exists()) {
            return 0;
        }
        return file.length();
    }

    /**
     * 获取文件或网址的名称（包括后缀）
     */
    public static String obtainName(String pathOrUrl) {
        if (pathOrUrl == null) {
            return "";
        }
        int pos = pathOrUrl.lastIndexOf('/');
        if (0 <= pos) {
            return pathOrUrl.substring(pos + 1);
        } else {
            return String.valueOf(System.currentTimeMillis()) + "." + obtainExtension(pathOrUrl);
        }
    }

    /**
     * 获取文件名（不包括扩展名）
     */
    public static String obtainNameExcludeExtension(String path) {
        try {
            String fileName = (new File(path)).getName();
            int lastIndexOf = fileName.lastIndexOf(".");
            if (lastIndexOf != -1) {
                fileName = fileName.substring(0, lastIndexOf);
            }
            return fileName;
        } catch (Exception e) {
            CqrLog.debug(e);
            return "";
        }
    }

    /**
     * 获取格式化后的文件大小
     */
    public static String obtainSizeFormatted(String path) {
        long fileSize = obtainLength(path);
        return CqrConvertUtils.toFileSizeString(fileSize);
    }

    /**
     * 获取文件后缀,不包括“.”
     */
    public static String obtainExtension(String pathOrUrl) {
        int dotPos = pathOrUrl.lastIndexOf('.');
        if (0 <= dotPos) {
            return pathOrUrl.substring(dotPos + 1);
        } else {
            return "ext";
        }
    }

    /**
     * 获取文件的MIME类型
     */
    public static String obtainMimeType(String pathOrUrl) {
        String ext = obtainExtension(pathOrUrl);
        MimeTypeMap map = MimeTypeMap.getSingleton();
        String mimeType;
        if (map.hasExtension(ext)) {
            mimeType = map.getMimeTypeFromExtension(ext);
        } else {
            mimeType = "*/*";
        }
        CqrLog.debug(pathOrUrl + ": " + mimeType);
        return mimeType;
    }

    /**
     * 比较两个文件的最后修改时间
     */
    public static int compareLastModified(String path1, String path2) {
        long stamp1 = (new File(path1)).lastModified();
        long stamp2 = (new File(path2)).lastModified();
        if (stamp1 > stamp2) {
            return 1;
        } else if (stamp1 < stamp2) {
            return -1;
        } else {
            return 0;
        }
    }

    /**
     * 创建多级别的目录
     */
    public static boolean makeDirs(String path) {
        return makeDirs(new File(path));
    }

    /**
     * 创建多级别的目录
     */
    public static boolean makeDirs(File file) {
        return file.mkdirs();
    }

    /**
     * 清除缓存，包括WebView的，但不包括自定义磁盘的缓存（如三方图片缓存）
     */
    @WorkerThread
    public static synchronized void clearCache(Context context) {
        try {
            context.deleteDatabase("webview.db");
            context.deleteDatabase("webviewCache.db");
        } catch (Exception e) {
            e.printStackTrace();
        }
        File appCacheDir = context.getCacheDir();
        CqrLog.debug("app cache dir: " + appCacheDir.getAbsolutePath());
        //删除APP缓存目录
        if (appCacheDir.exists()) {
            delete(appCacheDir);
        }
        //参见WebSettings.setAppCachePath
        File webviewCacheDir = context.getApplicationContext().getDir("webview", Context.MODE_PRIVATE);
        CqrLog.debug("webview cache dir: " + webviewCacheDir.getAbsolutePath());
        //删除WebView缓存目录
        if (webviewCacheDir.exists()) {
            delete(webviewCacheDir);
        }
    }

    /**
     * 获取磁盘缓存大小，包括WebView的，但不包括自定义磁盘的缓存（如三方图片缓存）
     */
    @WorkerThread
    public static synchronized long getCacheSize(Context context) {
        long size = 0;
        File appCacheDir = context.getCacheDir();
        CqrLog.debug("app cache dir: " + appCacheDir.getAbsolutePath());
        File[] appCacheFiles = appCacheDir.listFiles();
        for (File appCacheFile : appCacheFiles) {
            size += obtainSize(appCacheFile);
        }
        //参见WebSettings.setAppCachePath
        File webviewCacheDir = context.getApplicationContext().getDir("webview", Context.MODE_PRIVATE);
        CqrLog.debug("webview cache dir: " + webviewCacheDir.getAbsolutePath());
        File[] webviewCacheFiles = webviewCacheDir.listFiles();
        for (File webviewCacheFile : webviewCacheFiles) {
            size += obtainSize(webviewCacheFile);
        }
        return size;
    }

    public static long obtainSize(File file) {
        if (file == null || !file.exists()) {
            return 0;
        }
        long size = 0;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                size += obtainSize(f);
            }
        } else {
            size += file.length();
        }
        return size;
    }

    public static class SortByExtension implements Comparator<File> {

        public SortByExtension() {
            super();
        }

        public int compare(File f1, File f2) {
            if (f1 == null || f2 == null) {
                if (f1 == null) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
                if (f1.isDirectory() && f2.isFile()) {
                    return -1;
                } else if (f1.isFile() && f2.isDirectory()) {
                    return 1;
                } else {
                    return f1.getName().compareToIgnoreCase(f2.getName());
                }
            }
        }

    }

    public static class SortByName implements Comparator<File> {
        private boolean caseSensitive;

        public SortByName(boolean caseSensitive) {
            this.caseSensitive = caseSensitive;
        }

        public SortByName() {
            this.caseSensitive = false;
        }

        public int compare(File f1, File f2) {
            if (f1 == null || f2 == null) {
                if (f1 == null) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
                if (f1.isDirectory() && f2.isFile()) {
                    return -1;
                } else if (f1.isFile() && f2.isDirectory()) {
                    return 1;
                } else {
                    String s1 = f1.getName();
                    String s2 = f2.getName();
                    if (caseSensitive) {
                        return s1.compareTo(s2);
                    } else {
                        return s1.compareToIgnoreCase(s2);
                    }
                }
            }
        }

    }

    public static class SortBySize implements Comparator<File> {

        public SortBySize() {
            super();
        }

        public int compare(File f1, File f2) {
            if (f1 == null || f2 == null) {
                if (f1 == null) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
                if (f1.isDirectory() && f2.isFile()) {
                    return -1;
                } else if (f1.isFile() && f2.isDirectory()) {
                    return 1;
                } else {
                    if (f1.length() < f2.length()) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            }
        }

    }

    public static class SortByTime implements Comparator<File> {

        public SortByTime() {
            super();
        }

        public int compare(File f1, File f2) {
            if (f1 == null || f2 == null) {
                if (f1 == null) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
                if (f1.isDirectory() && f2.isFile()) {
                    return -1;
                } else if (f1.isFile() && f2.isDirectory()) {
                    return 1;
                } else {
                    if (f1.lastModified() > f2.lastModified()) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            }
        }

    }

}

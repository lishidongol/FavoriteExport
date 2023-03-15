package com.lishidong.idea.plugin.favexport.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.lishidong.idea.plugin.favexport.UI.FileExportDialog;
import com.lishidong.idea.plugin.favexport.model.ExportClass;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class FavUtil {

    /**
     * @param psiClass       需要获取内部类的类
     * @param superClassName 需要获取内部类的类的类名
     * @param resultList     返回结果
     */
    public static void getAllInnerClassName(PsiClass psiClass, String superClassName, List<String> resultList) {
        PsiClass[] allInnerClasses = psiClass.getInnerClasses();
        for (PsiClass allInnerClass : allInnerClasses) {
            String curClassName = superClassName + "$" + allInnerClass.getName();
            getAllInnerClassName(allInnerClass, curClassName, resultList);
            resultList.add(curClassName);
        }
    }

    /**
     * 根据传入的文件列表、名称、路径，生成jar包
     */
    public static void createJar(String jarOutputPath, String jarOutputName, Set<ExportClass> exportClassSet) {
        // 创建Jar包
        try {
            Manifest manifest = new Manifest();
            Attributes mainAttributes = manifest.getMainAttributes();
            mainAttributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
            mainAttributes.put(new Attributes.Name("Created-By"), "lishidongol");
            mainAttributes.put(new Attributes.Name("Created-Date"), LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            jarOutputPath = jarOutputPath.replace("\\", "/").replace("//", "/");

            if (!jarOutputName.endsWith(".jar")) {
                jarOutputName += ".jar";
            }

            if (!jarOutputPath.endsWith("/")) {
                jarOutputPath += "/";
            }

            File filepath = new File(jarOutputPath);
            if (!filepath.exists()) {
                filepath.mkdirs();
            }

            FileOutputStream os = new FileOutputStream(jarOutputPath + jarOutputName, true);
            BufferedOutputStream bos = new BufferedOutputStream(os);
            JarOutputStream jos = new JarOutputStream(bos, manifest);

            for (ExportClass exportClass : exportClassSet) {
                JarEntry je = new JarEntry(exportClass.getClassPackage() + "/" + exportClass.getClassName());
                jos.putNextEntry(je);
                jos.write(Files.readAllBytes(Path.of(exportClass.getClassPath())));
                jos.closeEntry();
            }
            jos.close();
            bos.close();
            os.close();
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static void openFileSelect(Project project, List<VirtualFile> virtualFiles) {
        FileExportDialog dialog = new FileExportDialog(project, virtualFiles, null, null, true, false);
        dialog.setSize(600, 400);
        dialog.show();
    }

}

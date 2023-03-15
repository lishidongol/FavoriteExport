package com.lishidong.idea.plugin.favexport.listener;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.compiler.CompilerPaths;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.lishidong.idea.plugin.favexport.model.ExportClass;
import com.lishidong.idea.plugin.favexport.util.FavUtil;

import java.util.*;

/**
 * 编译完成后事件监听
 */
public class CompileDoneListener implements FilesCompileListener {

    private Project project;

    public CompileDoneListener(Project project) {
        this.project = project;
    }

    @Override
    public void filesCompile(String outputPath, String outputName, Collection<VirtualFile> selectedFiles) {
        CompilerManager compilerManager = CompilerManager.getInstance(this.project);
        compilerManager.compile(selectedFiles.toArray(new VirtualFile[0]), (aborted, errors, warnings, compileContext) -> {

            for (VirtualFile datum : selectedFiles) {
                // 获取文件所属模块
                Module moduleForFile = ModuleUtil.findModuleForFile(datum, project);
                // 获取编译后的目录
                VirtualFile moduleOutputDirectory = CompilerPaths.getModuleOutputDirectory(moduleForFile, false);
                ApplicationManager.getApplication().invokeLater(() -> moduleOutputDirectory.refresh(false, true));
            }

            ApplicationManager.getApplication().invokeLater(() -> {
                // 在索引完成后执行的代码
                // 获取编译后的目录
                Set<ExportClass> exportClasses = new HashSet<>();
                PsiManager psiManager = PsiManager.getInstance(project);
                for (VirtualFile virtualFile : selectedFiles) {
                    // 获取文件所属模块
                    Module module = ModuleUtil.findModuleForFile(virtualFile, project);
                    // 获取编译后的目录
                    VirtualFile moduleOutputDirectory = CompilerPaths.getModuleOutputDirectory(module, false);
                    // 强转成PsiJavaFile，以便获取包路径
                    PsiJavaFile psiJavaFile = (PsiJavaFile) psiManager.findFile(virtualFile);
                    PsiClass[] classes = psiJavaFile.getClasses();
                    // 获取所有类
                    List<String> classNameList = new ArrayList<>();
                    for (PsiClass aClass : classes) {
                        // 获取类下的所有内部类
                        FavUtil.getAllInnerClassName(aClass, aClass.getName(), classNameList);
                        // 添加自身
                        classNameList.add(aClass.getName());
                    }
                    // 拼接获取到完整的包路径
                    String replace = psiJavaFile.getPackageName().replace(".", "/");
                    String filePackage = moduleOutputDirectory.getPath() + "/" + replace;
                    // 获取到最底级包
                    VirtualFile lastPath = LocalFileSystem.getInstance().findFileByPath(filePackage);
                    for (String name : classNameList) {
                        VirtualFile child = lastPath.findChild(name + ".class");
                        if (child != null) {
                            ExportClass exportClass = new ExportClass();
                            exportClass.setClassName(child.getName());
                            exportClass.setClassPackage(replace);
                            exportClass.setClassPath(child.getPath());
                            exportClasses.add(exportClass);
                        }
                    }
                }

                FavUtil.createJar(outputPath, outputName, exportClasses);
            }, ModalityState.any());
        });
    }
}

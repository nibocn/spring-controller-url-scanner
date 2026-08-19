package me.nibo.springurlscanner;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AllClassesSearch;

import java.util.*;

final class SpringEndpointScanner {
    private SpringEndpointScanner() {}

    static List<Endpoint> scan(Project project, ProgressIndicator indicator, boolean includeFeignClients) {
        List<Endpoint> endpoints = new ArrayList<>();
        Set<String> seenClasses = new HashSet<>();
        ProjectFileIndex fileIndex = ProjectFileIndex.getInstance(project);

        AllClassesSearch.search(GlobalSearchScope.allScope(project), project).forEach(psiClass -> {
            indicator.checkCanceled();
            String qName = psiClass.getQualifiedName();
            if (qName == null || !seenClasses.add(qName)) return true;
            boolean controller = SpringMappingUtil.isController(psiClass);
            boolean feignClient = SpringMappingUtil.isFeignClient(psiClass);
            if (!controller && !(includeFeignClients && feignClient)) return true;

            String type = feignClient && !controller ? "FeignClient" : "Controller";
            List<String> classPaths = SpringMappingUtil.classPaths(psiClass);
            String source = sourceOf(psiClass, fileIndex);

            for (PsiMethod method : psiClass.getMethods()) {
                for (SpringMappingUtil.MethodMapping mapping : SpringMappingUtil.methodMappings(method)) {
                    for (String classPath : classPaths) {
                        for (String methodPath : mapping.paths()) {
                            endpoints.add(new Endpoint(
                                    type,
                                    mapping.httpMethod(),
                                    SpringMappingUtil.joinPath(classPath, methodPath),
                                    qName,
                                    method.getName(),
                                    source,
                                    method
                            ));
                        }
                    }
                }
            }
            return true;
        });

        endpoints.sort(Comparator.comparing(Endpoint::type)
                .thenComparing(Endpoint::url)
                .thenComparing(Endpoint::httpMethod)
                .thenComparing(Endpoint::controller));
        return endpoints;
    }

    private static String sourceOf(PsiClass psiClass, ProjectFileIndex fileIndex) {
        if (psiClass.getContainingFile() == null) return "unknown";
        VirtualFile file = psiClass.getContainingFile().getVirtualFile();
        if (file == null) return "unknown";

        if (fileIndex.isInContent(file)) return "project";

        String path = file.getPath();
        int jarSep = path.indexOf("!/");
        if (jarSep >= 0) {
            String jarPath = path.substring(0, jarSep);
            int slash = Math.max(jarPath.lastIndexOf('/'), jarPath.lastIndexOf('\\'));
            return slash >= 0 ? jarPath.substring(slash + 1) : jarPath;
        }
        return path;
    }
}

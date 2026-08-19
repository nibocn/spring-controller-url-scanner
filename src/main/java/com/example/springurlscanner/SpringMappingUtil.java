package com.example.springurlscanner;

import com.intellij.psi.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;

final class SpringMappingUtil {
    private SpringMappingUtil() {}

    static final String FEIGN_CLIENT = "org.springframework.cloud.openfeign.FeignClient";
    static final String LEGACY_FEIGN_CLIENT = "org.springframework.cloud.netflix.feign.FeignClient";

    static final String REQUEST_MAPPING = "org.springframework.web.bind.annotation.RequestMapping";
    static final String GET_MAPPING = "org.springframework.web.bind.annotation.GetMapping";
    static final String POST_MAPPING = "org.springframework.web.bind.annotation.PostMapping";
    static final String PUT_MAPPING = "org.springframework.web.bind.annotation.PutMapping";
    static final String DELETE_MAPPING = "org.springframework.web.bind.annotation.DeleteMapping";
    static final String PATCH_MAPPING = "org.springframework.web.bind.annotation.PatchMapping";

    static boolean isController(PsiClass psiClass) {
        PsiModifierList list = psiClass.getModifierList();
        if (list == null) return false;
        return hasAnnotation(list, "org.springframework.web.bind.annotation.RestController")
                || hasAnnotation(list, "org.springframework.stereotype.Controller");
    }

    static boolean isFeignClient(PsiClass psiClass) {
        PsiModifierList list = psiClass.getModifierList();
        if (list == null) return false;
        return hasAnnotation(list, FEIGN_CLIENT) || hasAnnotation(list, LEGACY_FEIGN_CLIENT);
    }

    static List<String> classPaths(PsiClass psiClass) {
        PsiModifierList list = psiClass.getModifierList();
        if (list == null) return List.of("");
        PsiAnnotation ann = list.findAnnotation(REQUEST_MAPPING);
        return ann == null ? List.of("") : paths(ann);
    }

    static List<MethodMapping> methodMappings(PsiMethod method) {
        PsiModifierList list = method.getModifierList();
        List<MethodMapping> result = new ArrayList<>();
        addMapping(result, list.findAnnotation(GET_MAPPING), "GET");
        addMapping(result, list.findAnnotation(POST_MAPPING), "POST");
        addMapping(result, list.findAnnotation(PUT_MAPPING), "PUT");
        addMapping(result, list.findAnnotation(DELETE_MAPPING), "DELETE");
        addMapping(result, list.findAnnotation(PATCH_MAPPING), "PATCH");

        PsiAnnotation request = list.findAnnotation(REQUEST_MAPPING);
        if (request != null) {
            List<String> methods = requestMethods(request);
            if (methods.isEmpty()) methods = List.of("ANY");
            for (String methodName : methods) {
                result.add(new MethodMapping(methodName, paths(request)));
            }
        }
        return result;
    }

    private static void addMapping(List<MethodMapping> result, @Nullable PsiAnnotation annotation, String httpMethod) {
        if (annotation != null) result.add(new MethodMapping(httpMethod, paths(annotation)));
    }

    private static boolean hasAnnotation(PsiModifierList list, String qName) {
        return list.findAnnotation(qName) != null;
    }

    static List<String> paths(PsiAnnotation annotation) {
        List<String> value = stringValues(annotation.findDeclaredAttributeValue("value"));
        List<String> path = stringValues(annotation.findDeclaredAttributeValue("path"));
        LinkedHashSet<String> all = new LinkedHashSet<>();
        all.addAll(value);
        all.addAll(path);
        if (all.isEmpty()) all.add("");
        return List.copyOf(all);
    }

    private static List<String> requestMethods(PsiAnnotation annotation) {
        PsiAnnotationMemberValue v = annotation.findDeclaredAttributeValue("method");
        if (v == null) return List.of();
        List<String> result = new ArrayList<>();
        collectEnumNames(v, result);
        return result;
    }

    private static void collectEnumNames(PsiAnnotationMemberValue value, List<String> out) {
        if (value instanceof PsiArrayInitializerMemberValue array) {
            for (PsiAnnotationMemberValue item : array.getInitializers()) collectEnumNames(item, out);
            return;
        }
        String text = value.getText();
        int idx = text.lastIndexOf('.');
        out.add((idx >= 0 ? text.substring(idx + 1) : text).replace("}", "").trim());
    }

    private static List<String> stringValues(@Nullable PsiAnnotationMemberValue value) {
        if (value == null) return List.of();
        List<String> out = new ArrayList<>();
        collectStringValues(value, out);
        return out;
    }

    private static void collectStringValues(PsiAnnotationMemberValue value, List<String> out) {
        if (value instanceof PsiArrayInitializerMemberValue array) {
            for (PsiAnnotationMemberValue item : array.getInitializers()) collectStringValues(item, out);
            return;
        }
        if (value instanceof PsiLiteralExpression literal && literal.getValue() instanceof String s) {
            out.add(s);
            return;
        }
        // Constants / placeholders are kept as source text so they remain searchable.
        String text = value.getText();
        if (!text.isBlank()) out.add(text.replaceAll("^\"|\"$", ""));
    }

    static String joinPath(String classPath, String methodPath) {
        String a = normalize(classPath);
        String b = normalize(methodPath);
        if (a.equals("/")) return b;
        if (b.equals("/")) return a;
        return (a + b).replaceAll("/{2,}", "/");
    }

    private static String normalize(String p) {
        if (p == null || p.isBlank()) return "/";
        String x = p.trim();
        if (!x.startsWith("/")) x = "/" + x;
        if (x.length() > 1 && x.endsWith("/")) x = x.substring(0, x.length() - 1);
        return x;
    }

    record MethodMapping(String httpMethod, List<String> paths) {}
}

package me.nibo.springurlscanner;

import com.intellij.psi.PsiMethod;

record Endpoint(
        String type,
        String httpMethod,
        String url,
        String controller,
        String handler,
        String source,
        PsiMethod psiMethod
) {}

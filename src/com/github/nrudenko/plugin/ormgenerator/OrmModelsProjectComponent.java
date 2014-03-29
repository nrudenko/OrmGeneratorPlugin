package com.github.nrudenko.plugin.ormgenerator;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;

public class OrmModelsProjectComponent implements ProjectComponent {
    private final Project project;
    private Disposable disposable;

    public OrmModelsProjectComponent(Project project) {
        this.project = project;
    }

    public void initComponent() {
        disposable = new Disposable() {
            @Override
            public void dispose() {
            }
        };
//        final OrmModelListener listener = new OrmModelListener(project);
//        Disposer.register(disposable, listener);
    }

    public void disposeComponent() {
        Disposer.dispose(disposable);
    }

    @NotNull
    public String getComponentName() {
        return "OrmModelsProjectComponent";
    }

    public void projectOpened() {
    }

    public void projectClosed() {
    }
}

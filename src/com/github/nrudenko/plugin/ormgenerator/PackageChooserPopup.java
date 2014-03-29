package com.github.nrudenko.plugin.ormgenerator;

import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiPackage;
import com.intellij.spellchecker.ui.SpellCheckingEditorCustomization;
import com.intellij.ui.TextFieldWithAutoCompletion;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.panels.NonOpaquePanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.JavaSourceRootType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;

public class PackageChooserPopup extends TextFieldWithAutoCompletion<String> {

    @Nullable
    private JBPopup myPopup;

    public PackageChooserPopup(@NotNull Project project, @NotNull VirtualFile sourceRoot) {
        super(project, new StringsCompletionProvider(new ArrayList<String>(), null), false, null);
        PsiDirectory directory = PsiManager.getInstance(project).findDirectory(sourceRoot);

        ArrayList<String> variants = new ArrayList<String>();
        getAllPackages(variants, directory);
        setVariants(variants);
        setBorder(new EmptyBorder(3, 3, 3, 3));
    }

    private void getAllPackages(List<String> packages, PsiDirectory directory) {
        PsiPackage aPackage = JavaDirectoryService.getInstance().getPackage(directory);
        String qualifiedName = aPackage.getQualifiedName();
        packages.add(qualifiedName);
        PsiDirectory[] subdirectories = directory.getSubdirectories();
        for (int i = 0; i < subdirectories.length; i++) {
            PsiDirectory subdirectory = subdirectories[i];
            getAllPackages(packages, subdirectory);
        }
    }

    public JBPopup createPopup(@NotNull final OnPackageSelectedListener onPackageSelectedListener) {
        final NonOpaquePanel popupPanel = new NonOpaquePanel();
        popupPanel.add(PackageChooserPopup.this);
        myPopup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(popupPanel, this)
                .setTitle("Choose destination package ...")
                .setCancelOnClickOutside(true)
                .setCancelKeyEnabled(true)
                .setRequestFocus(true)
                .setShowShadow(true)
                .createPopup();

        myPopup.addListener(new JBPopupListener() {
            @Override
            public void beforeShown(LightweightWindowEvent event) {

            }

            @Override
            public void onClosed(LightweightWindowEvent event) {
                if (event.isOk()) {
                    onPackageSelectedListener.onPackageSelected(getText());
                }
            }
        });
        final JBTextField field = new JBTextField(40);
        final Dimension size = field.getPreferredSize();
        final Insets insets = getBorder().getBorderInsets(this);
        size.height = size.height * 2 + 6 + insets.top + insets.bottom;
        size.width += 4 + insets.left + insets.right;
        myPopup.setSize(size);
        return myPopup;
    }

    @Override
    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (myPopup != null) {
                myPopup.closeOk(e);
            }
            return true;
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (myPopup != null) {
                myPopup.cancel(e);
            }
            return true;
        }
        return false;
    }

    @Override
    protected EditorEx createEditor() {
        // spell check is not needed
        EditorEx editor = super.createEditor();
        SpellCheckingEditorCustomization.getInstance(false).customize(editor);
        return editor;
    }


    public interface OnPackageSelectedListener {
        public void onPackageSelected(String packageQualifiedName);
    }
}

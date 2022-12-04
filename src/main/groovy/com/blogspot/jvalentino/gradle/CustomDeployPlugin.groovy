package com.blogspot.jvalentino.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

import com.blogspot.jvalentino.gradle.ext.DeployExt
import com.blogspot.jvalentino.gradle.task.LoadConfigTask
import com.blogspot.jvalentino.gradle.task.MoveTask
import com.blogspot.jvalentino.gradle.task.PrepareTask
import com.blogspot.jvalentino.gradle.task.ShutdownTask
import com.blogspot.jvalentino.gradle.task.StartupTask
import com.blogspot.jvalentino.gradle.task.UploadTask

/**
 * <p>A basic gradle plugin.</p>
 * @author jvalentino2
 */
@SuppressWarnings(['DuplicateStringLiteral', 'UnnecessaryObjectReferences'])
class CustomDeployPlugin implements Plugin<Project> {

    void apply(Project project) {
        project.extensions.create 'deploy', DeployExt
        project.task('load', type:LoadConfigTask)
        project.task('prepare', type:PrepareTask, dependsOn:'load')
        project.task('upload', type:UploadTask, dependsOn:'prepare')
        project.task('shutdown', type:ShutdownTask, dependsOn:'upload')
        project.task('move', type:MoveTask, dependsOn:'shutdown')
        project.task('startup', type:StartupTask, dependsOn:'move')
    }
}

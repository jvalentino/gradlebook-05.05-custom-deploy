package com.blogspot.jvalentino.gradle.task

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

import com.blogspot.jvalentino.gradle.ext.DeployExt
import com.blogspot.jvalentino.gradle.ext.Health
import com.blogspot.jvalentino.gradle.service.UrlService

/**
 * <p>A basic gradle plugin.</p>
 * @author jvalentino2
 */
@SuppressWarnings(['Println'])
class ShutdownTask extends DefaultTask {

    ShutdownTask instance = this
    UrlService urlService = new UrlService()

    @TaskAction
    void perform() {
        Project p = instance.project
        DeployExt ext = p.extensions.findByType(DeployExt)

        println '- Attempting to nicely shutdown the app'
        urlService.post(ext.shutdownUrl)

        urlService.waitUntilHealth(
                Health.UP, Health.NOT_RUNNING,
                10_000L, 1_000L, ext.healthUrl)
    }
}

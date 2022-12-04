package com.blogspot.jvalentino.gradle.task

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

import com.blogspot.jvalentino.gradle.ext.DeployExt
import com.blogspot.jvalentino.gradle.ext.Health
import com.blogspot.jvalentino.gradle.service.RemoteService
import com.blogspot.jvalentino.gradle.service.UrlService

/**
 * <p>AStarts the application</p>
 * @author jvalentino2
 */
@SuppressWarnings(['Println'])
class StartupTask extends DefaultTask {

    StartupTask instance = this
    RemoteService remoteService = new RemoteService()
    UrlService urlService = new UrlService()

    @TaskAction
    void perform() {
        Project p = instance.project
        DeployExt ext = p.extensions.findByType(DeployExt)

        println '- Starting up application'

        String command = "cd ${ext.targetDir}/deploy; "
        command += "nohup ${ext.startCommand} >/dev/null 2>&1 &"

        remoteService.execute(command, ext.session)

        urlService.waitUntilHealth(
                Health.NOT_RUNNING, Health.UP,
                30_000L, 5_000L, ext.healthUrl)
    }
}

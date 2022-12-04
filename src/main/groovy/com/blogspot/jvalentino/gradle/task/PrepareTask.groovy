package com.blogspot.jvalentino.gradle.task

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

import com.blogspot.jvalentino.gradle.ext.DeployExt
import com.blogspot.jvalentino.gradle.service.RemoteService

/**
 * <p>Creates the directory structure on the remote system</p>
 * @author jvalentino2
 */
@SuppressWarnings(['Println'])
class PrepareTask extends DefaultTask {

    PrepareTask instance = this
    RemoteService remoteService = new RemoteService()

    @TaskAction
    void perform() {
        Project p = instance.project
        DeployExt ext = p.extensions.findByType(DeployExt)

        println "- Connecting using ${ext.username}@${ext.host}"
        ext.session = remoteService.generateSession(
                ext.username, ext.password, ext.host)
        ext.session.connect()

        remoteService.execute("mkdir -p ${ext.targetDir} || true", 
            ext.session)
        remoteService.execute("mkdir -p ${ext.targetDir}/backup || true",
             ext.session)
        remoteService.execute("mkdir -p ${ext.targetDir}/upload || true", 
            ext.session)
        remoteService.execute("mkdir -p ${ext.targetDir}/deploy || true",
             ext.session)
    }
}

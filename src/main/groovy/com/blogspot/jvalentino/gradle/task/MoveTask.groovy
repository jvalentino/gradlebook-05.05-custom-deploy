package com.blogspot.jvalentino.gradle.task

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

import com.blogspot.jvalentino.gradle.ext.DeployExt
import com.blogspot.jvalentino.gradle.service.RemoteService

/**
 * <p>Backs up the current app, and moves the one in upload to deploy</p>
 * @author jvalentino2
 */
@SuppressWarnings(['Println'])
class MoveTask extends DefaultTask {

    MoveTask instance = this
    RemoteService remoteService = new RemoteService()

    @TaskAction
    void perform() {
        Project p = instance.project
        DeployExt ext = p.extensions.findByType(DeployExt)

        println '- Deleting any current backups'
        remoteService.execute(
                "rm -f ${ext.targetDir}/backup/*.*", 
                ext.session)

        println '- Backing up the current deployment'
        remoteService.execute(
                "mv ${ext.targetDir}/deploy/*.* ${ext.targetDir}/backup", 
                ext.session)

        println '- Putting the current version in the deploy directory'
        remoteService.execute(
                "mv ${ext.targetDir}/upload/*.* ${ext.targetDir}/deploy", 
                ext.session)
    }
}

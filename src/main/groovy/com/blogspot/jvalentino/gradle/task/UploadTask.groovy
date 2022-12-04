package com.blogspot.jvalentino.gradle.task

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.TaskAction

import com.blogspot.jvalentino.gradle.ext.DeployExt
import com.blogspot.jvalentino.gradle.service.RemoteService

/**
 * <p>Uploads teh jar</p>
 * @author jvalentino2
 */
@SuppressWarnings(['Println'])
class UploadTask extends DefaultTask {

    UploadTask instance = this
    RemoteService remoteService = new RemoteService()

    @TaskAction
    void perform() {
        Project p = instance.project
        DeployExt ext = p.extensions.findByType(DeployExt)

        String deployFileName = ext.deployFile.split('/').last()
        String deployFromDir = ext.deployFile.replace(
            "/${deployFileName}", '')

        println "- Searching ${deployFromDir} for ${deployFileName}"

        FileTree fileTree = p.fileTree(deployFromDir) { 
            include deployFileName 
        }
        List<File> results = []
        fileTree.files.each { File f ->
            println "  * Found: ${f.name}"
            results.add(f)
        }

        if (results.size() != 1) {
            throw new GradleException(
                'There must be only 1 file match for upload')
        }

        File file = results.first()
        String targetFile = "${ext.targetDir}/upload/${file.name}"
        println "- Uploading ${file.name} to ${targetFile}"
        remoteService.upload(file, targetFile, ext.session)
    }
}

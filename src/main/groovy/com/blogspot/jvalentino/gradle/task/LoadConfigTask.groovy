package com.blogspot.jvalentino.gradle.task

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.yaml.snakeyaml.Yaml

import com.blogspot.jvalentino.gradle.ext.DeployExt

/**
 * <p>Handles loading the configuration based on environment</p>
 * @author jvalentino2
 */
@SuppressWarnings(['Println', 'UnnecessaryObjectReferences'])
class LoadConfigTask extends DefaultTask {

    LoadConfigTask instance = this

    @TaskAction
    void perform() {
        Project p = instance.project

        DeployExt ext = p.extensions.findByType(DeployExt)

        ext.env = p.properties.env
        if (ext.env == null) {
            throw new GradleException('-Penv must be given')
        }

        println "- Loading for ${ext.env} from ${ext.config}"
        Yaml yaml = new Yaml()
        Map map = yaml.load(new FileInputStream(new File(ext.config)))

        Map envMap = map[ext.env]
        ext.username = envMap.username
        ext.password = envMap.password
        ext.host = envMap.host
        ext.targetDir = envMap.targetDir
        ext.deployFile = envMap.deployFile
        ext.shutdownUrl = envMap.shutdownUrl
        ext.healthUrl = envMap.healthUrl
        ext.startCommand = envMap.startCommand
    }
}

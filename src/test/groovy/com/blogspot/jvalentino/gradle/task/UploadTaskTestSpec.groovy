package com.blogspot.jvalentino.gradle.task

import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.internal.plugins.ExtensionContainerInternal
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.testfixtures.ProjectBuilder

import com.blogspot.jvalentino.gradle.ext.DeployExt
import com.blogspot.jvalentino.gradle.service.RemoteService
import com.jcraft.jsch.Session

import spock.lang.Specification
import spock.lang.Subject

class UploadTaskTestSpec extends Specification {

    @Subject
    UploadTask task
    Project project
    DeployExt ext
    ExtensionContainer extensions
    
    def setup() {
        Project p = ProjectBuilder.builder().build()
        task = p.task('upload', type:UploadTask)
        task.instance = Mock(UploadTask)
        task.remoteService = Mock(RemoteService)
        project = Mock(ProjectInternal)
        ext = new DeployExt()
        extensions = Mock(ExtensionContainerInternal)
    }
    
    void "test perform"() {
        given:
        ext.deployFile = 'build/libs/demo-*.jar'
        ext.targetDir = '/foo/bar'
        ext.session = Mock(Session)
        
        and:
        File file = Mock(File)
        ConfigurableFileTree fileTree = 
            Mock(ConfigurableFileTree)
        fileTree.files >> [ file ]
        
        when:
        task.perform()
        
        then:
        1 * task.instance.project >> project
        1 * project.extensions >> extensions
        1 * extensions.findByType(DeployExt) >> ext
        
        and:
        1 * project.fileTree('build/libs', _) >> fileTree
        _ * file.name >> 'demo-1.2.3.jar'
        
        and:
        1 * task.remoteService.upload(file, '/foo/bar/upload/demo-1.2.3.jar', ext.session)
    }

}

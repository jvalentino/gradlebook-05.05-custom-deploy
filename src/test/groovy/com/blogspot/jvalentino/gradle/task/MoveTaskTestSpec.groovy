package com.blogspot.jvalentino.gradle.task

import org.gradle.api.Project
import org.gradle.api.internal.plugins.ExtensionContainerInternal
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.testfixtures.ProjectBuilder

import com.blogspot.jvalentino.gradle.ext.DeployExt
import com.blogspot.jvalentino.gradle.service.RemoteService
import com.jcraft.jsch.Session

import spock.lang.Specification
import spock.lang.Subject

class MoveTaskTestSpec extends Specification {

    @Subject
    MoveTask task
    Project project
    DeployExt ext
    ExtensionContainer extensions
    
    def setup() {
        Project p = ProjectBuilder.builder().build()
        task = p.task('move', type:MoveTask)
        task.instance = Mock(MoveTask)
        task.remoteService = Mock(RemoteService)
        project = Mock(ProjectInternal)
        ext = new DeployExt()
        extensions = Mock(ExtensionContainerInternal)
    }
    
    void "test perform"() {
        given:
        ext.session = Mock(Session)
        
        when:
        task.perform()
        
        then:
        1 * task.instance.project >> project
        1 * project.extensions >> extensions
        1 * extensions.findByType(DeployExt) >> ext
        
        and:
        1 * task.remoteService.execute(
                "rm -f ${ext.targetDir}/backup/*.*", 
                ext.session)
        
        1 * task.remoteService.execute(
                "mv ${ext.targetDir}/deploy/*.* ${ext.targetDir}/backup", 
                ext.session)
        
        1 * task.remoteService.execute(
                "mv ${ext.targetDir}/upload/*.* ${ext.targetDir}/deploy", 
                ext.session)
    }

}

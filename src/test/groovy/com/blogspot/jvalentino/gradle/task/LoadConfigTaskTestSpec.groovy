package com.blogspot.jvalentino.gradle.task

import org.gradle.api.Project
import org.gradle.api.internal.plugins.ExtensionContainerInternal
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.testfixtures.ProjectBuilder

import com.blogspot.jvalentino.gradle.ext.DeployExt

import spock.lang.Specification
import spock.lang.Subject

class LoadConfigTaskTestSpec extends Specification {

    @Subject
    LoadConfigTask task
    Project project
    DeployExt ext
    ExtensionContainer extensions
    
    def setup() {
        Project p = ProjectBuilder.builder().build()
        task = p.task('add', type:LoadConfigTask)
        task.instance = Mock(LoadConfigTask)
        project = Mock(ProjectInternal)
        ext = new DeployExt()
        extensions = Mock(ExtensionContainerInternal)
    }
    
    void "Test perform"() {
        given:
        Map props = ['env':'foo']
        ext.config = 'src/test/resources/deploy.yml'
        
        when:
        task.perform()
        
        then:
        1 * task.instance.project >> project
        1 * project.properties >> props
        1 * project.extensions >> extensions 
        1 * extensions.findByType(DeployExt) >> ext
        
        and:
        ext.username == 'alpha'
        ext.password == 'bravo'
        ext.host == 'localhost'
        ext.targetDir == '/Users/testuser/demo'
        ext.deployFile == 'build/libs/demo-*.jar'
        ext.shutdownUrl == 'http://localhost:8080/shutdown'
        ext.healthUrl == 'http://localhost:8080/health'
        ext.startCommand == 'java -jar -Dspring.profiles.active=dev demo-*.jar'
    }
}

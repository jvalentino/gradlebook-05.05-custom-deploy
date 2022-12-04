package com.blogspot.jvalentino.gradle.task

import org.gradle.api.Project
import org.gradle.api.internal.plugins.ExtensionContainerInternal
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.testfixtures.ProjectBuilder

import com.blogspot.jvalentino.gradle.ext.DeployExt
import com.blogspot.jvalentino.gradle.ext.Health
import com.blogspot.jvalentino.gradle.service.UrlService

import spock.lang.Specification
import spock.lang.Subject

class ShutdownTaskTestSpec extends Specification {

    @Subject
    ShutdownTask task
    Project project
    DeployExt ext
    ExtensionContainer extensions
    
    def setup() {
        Project p = ProjectBuilder.builder().build()
        task = p.task('shutdown', type:ShutdownTask)
        task.instance = Mock(ShutdownTask)
        task.urlService = Mock(UrlService)
        project = Mock(ProjectInternal)
        ext = new DeployExt()
        extensions = Mock(ExtensionContainerInternal)
    }
    
    void "test perform"() {
        
        when:
        task.perform()
        
        then:
        1 * task.instance.project >> project
        1 * project.extensions >> extensions
        1 * extensions.findByType(DeployExt) >> ext
        
        and:
        1 * task.urlService.post(ext.shutdownUrl)
        1 *  task.urlService.waitUntilHealth(
                Health.UP, Health.NOT_RUNNING,
                10_000L, 1_000L, ext.healthUrl)
    }

}

package com.blogspot.jvalentino.gradle.task

import org.gradle.api.Project
import org.gradle.api.internal.plugins.ExtensionContainerInternal
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.testfixtures.ProjectBuilder

import com.blogspot.jvalentino.gradle.ext.DeployExt
import com.blogspot.jvalentino.gradle.ext.Health
import com.blogspot.jvalentino.gradle.service.RemoteService
import com.blogspot.jvalentino.gradle.service.UrlService
import com.jcraft.jsch.Session

import spock.lang.Specification
import spock.lang.Subject

class StartupTaskTestSpec extends Specification {

    @Subject
    StartupTask task
    Project project
    DeployExt ext
    ExtensionContainer extensions
    
    def setup() {
        Project p = ProjectBuilder.builder().build()
        task = p.task('startup', type:StartupTask)
        task.instance = Mock(StartupTask)
        task.urlService = Mock(UrlService)
        task.remoteService = Mock(RemoteService)
        project = Mock(ProjectInternal)
        ext = new DeployExt()
        extensions = Mock(ExtensionContainerInternal)
    }
    
    void "test perform"() {
        given:
        String command = "cd ${ext.targetDir}/deploy; "
        command += "nohup ${ext.startCommand} >/dev/null 2>&1 &"
        
        and:
        ext.session = Mock(Session)
        
        when:
        task.perform()
        
        then:
        1 * task.instance.project >> project
        1 * project.extensions >> extensions
        1 * extensions.findByType(DeployExt) >> ext
        
        and:
        1 * task.remoteService.execute(command, ext.session)

        1 * task.urlService.waitUntilHealth(
                Health.NOT_RUNNING, Health.UP,
                30_000L, 5_000L, ext.healthUrl)
    }

}

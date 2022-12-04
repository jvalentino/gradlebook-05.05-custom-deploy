package com.blogspot.jvalentino.gradle.service

import java.io.File
import java.io.InputStream

import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session

import spock.lang.Specification
import spock.lang.Subject

class RemoteServiceTestSpec extends Specification {

    @Subject
    RemoteService service
    
    def setup() {
        service = new RemoteService()
    }
    
    void "test generateSession"() {
        given:
        GroovyMock(JSch, global:true)
        JSch jsch = Mock(JSch)
        Session session = Mock(Session)
        
        and:
        String username = 'foo'
        String password = 'bar'
        String host = 'abc'
        
        when:
        Session result = service.generateSession(username, password, host)
        
        then:
        1 * new JSch() >> jsch
        1 * jsch.getSession(username, host, 22) >> session
        1 * session.setPassword(password)
        1 * session.setConfig('StrictHostKeyChecking', 'no')
        
        and:
        session == result
    }
    
    void "test execute"() {
        given:
        String command = 'a'
        Session session = Mock(Session)
        ChannelExec channelExec = Mock(ChannelExec)
        
        and:
        InputStream is = Mock(InputStream)
        InputStream.class.metaClass.getText = { 
            'foo'
        }
        
        when:
        String result = service.execute(command, session)
        
        then:
        1 * session.openChannel('exec') >> channelExec
        1 * channelExec.disconnect()
        1 * channelExec.inputStream >> is
        
        and:
        result == 'foo'
    }
    
    void "test upload"() {
        given:
        File file = new File('src/test/resources/foo.txt')
        String toRemoteFile = 'foo'
        Session session = Mock(Session)
        
        and:
        ChannelSftp sftp = Mock(ChannelSftp)
        
        when:
        service.upload(file, toRemoteFile, session)
        
        then:
        1 * session.openChannel('sftp') >> sftp
        1 * sftp.connect()
        1 * sftp.put(_, toRemoteFile)
        1 * sftp.disconnect()
    }
}
